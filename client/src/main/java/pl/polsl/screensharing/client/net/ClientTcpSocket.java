/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.client.net;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.polsl.screensharing.client.model.ConnectionDetails;
import pl.polsl.screensharing.client.model.FastConnectionDetails;
import pl.polsl.screensharing.client.state.ClientState;
import pl.polsl.screensharing.client.state.VisibilityState;
import pl.polsl.screensharing.client.view.ClientWindow;
import pl.polsl.screensharing.client.view.fragment.VideoCanvas;
import pl.polsl.screensharing.lib.CryptoUtils;
import pl.polsl.screensharing.lib.UnoperableException;
import pl.polsl.screensharing.lib.net.SocketState;
import pl.polsl.screensharing.lib.net.payload.AuthPasswordReq;
import pl.polsl.screensharing.lib.net.payload.AuthPasswordRes;
import pl.polsl.screensharing.lib.net.payload.ConnectionData;
import pl.polsl.screensharing.lib.net.payload.VideoFrameDetails;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

@Slf4j
public class ClientTcpSocket extends Thread {
    @Getter
    private Socket clientSocket;
    private KeyPair clientKeypair;
    private PublicKey serverPublicKey;
    private SocketState socketState;
    private AuthPasswordRes authPasswordRes;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    private final ClientWindow clientWindow;
    private final ClientState clientState;
    private final FastConnectionDetails fastConnectionDetails;
    private final ClientDatagramSocket clientDatagramSocket;
    private final ConnectionHandler connectionHandler;
    private final ConnectionDetails connectionDetails;
    private final ObjectMapper objectMapper;

    public ClientTcpSocket(
        ClientWindow clientWindow, ConnectionHandler connectionHandler, ConnectionDetails connectionDetails
    ) {
        this.clientWindow = clientWindow;
        clientState = clientWindow.getClientState();
        clientDatagramSocket = clientWindow.getClientDatagramSocket();
        fastConnectionDetails = clientWindow.getClientState().getLastEmittedFastConnectionDetails();
        this.connectionHandler = connectionHandler;
        this.connectionDetails = connectionDetails;
        socketState = SocketState.EXHANGE_KEYS_REQ;
        objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        log.info("Starting TCP client thread...");
        try (
            final BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            final PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            bufferedReader = in;
            printWriter = out;
            while (true) {
                switch (socketState) {
                    // wysłanie klucza publicznego klienta do hosta (serwera)
                    case EXHANGE_KEYS_REQ: {
                        final String keyEnc = CryptoUtils.publicKeyToBase64(clientKeypair.getPublic());
                        out.println(SocketState.EXHANGE_KEYS_REQ.generateBody(keyEnc));
                        socketState = SocketState.EXHANGE_KEYS_RES;
                        log.info("Send public RSA key to server");
                        break;
                    }
                    // otrzymanie klucza publicznego od hosta i zapisanie go w pamięci
                    case EXHANGE_KEYS_RES: {
                        serverPublicKey = CryptoUtils.base64ToPublicKey(readData());
                        socketState = SocketState.CHECK_PASSWORD_REQ;
                        log.info("Persist public RSA key from server and save in-memory storage");
                        break;
                    }
                    // wysłanie haszu hasła do sprawdzenia do hosta (serwera)
                    case CHECK_PASSWORD_REQ: {
                        final String passwordHash = BCrypt.withDefaults()
                            .hashToString(10, connectionDetails.getPassword().toCharArray());
                        final AuthPasswordReq req = new AuthPasswordReq(passwordHash);
                        exchangeSSLRequest(req, SocketState.CHECK_PASSWORD_REQ, SocketState.CHECK_PASSWORD_RES);
                        log.info("Send password for check authority by server");
                        break;
                    }
                    // odebranie wiadomości o poprawności hasła oraz klucz do szyfrowanie symetrycznego UDP
                    case CHECK_PASSWORD_RES: {
                        final AuthPasswordRes res = exchangeSSLResponse(AuthPasswordRes.class);
                        if (!res.isValid()) {
                            connectionHandler.onFailure(connectionDetails, "Invalid password");
                            log.warn("Invalid password. Disconnect from session");
                            break;
                        }
                        authPasswordRes = res;
                        socketState = SocketState.SEND_CLIENT_DATA_REQ;
                        log.info("Successfully validated password.");
                        break;
                    }
                    // wysłanie dodatkowych danych (adres ip, port UDP, nazwa użytkownika)
                    case SEND_CLIENT_DATA_REQ: {
                        final VideoCanvas videoCanvas = clientWindow.getVideoCanvas();
                        final ClientDatagramSocket clientDatagramSocket = new ClientDatagramSocket(clientWindow,
                            videoCanvas, videoCanvas.getController());

                        clientDatagramSocket.createDatagramSocket(authPasswordRes.getSecretKeyUdp(),
                            authPasswordRes.getSecureRandomUdp(), connectionDetails.getClientPort());
                        clientWindow.setClientDatagramSocket(clientDatagramSocket);

                        final ConnectionData connectionData = ConnectionData.builder()
                            .username(connectionDetails.getUsername())
                            .ipAddress(connectionDetails.getClientIpAddress())
                            .udpPort(connectionDetails.getClientPort())
                            .build();

                        exchangeSSLRequest(connectionData, SocketState.SEND_CLIENT_DATA_REQ,
                            SocketState.SEND_CLIENT_DATA_RES);
                        log.info("Successfully sended client connection details");
                        break;
                    }
                    // odebranie od hosta (serwera) dodatkowych informacji, czy stream jest aktywny oraz
                    // aspect ratio obrazu
                    case SEND_CLIENT_DATA_RES: {
                        final VideoFrameDetails videoFrameDetails = exchangeSSLResponse(VideoFrameDetails.class);

                        clientState.updateVisibilityState(videoFrameDetails.isStreaming()
                            ? VisibilityState.VISIBLE
                            : VisibilityState.WAITING_FOR_CONNECTION);
                        connectionHandler.onSuccess(connectionDetails);

                        // uruchomienie wątku grabbera UDP
                        clientWindow.getClientDatagramSocket().start();

                        // stworzenie i uruchomienie wątku pętli zdarzeń z hosta
                        final ReceiveEventsThread receiveEventsThread = new ReceiveEventsThread(this);
                        receiveEventsThread.start();

                        socketState = SocketState.WAITING;
                        log.info("Successfully got video frame details {}", videoFrameDetails);
                        break;
                    }
                }
            }
        } catch (SocketException ignored) {
            stopAndClear();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exchangeSSLRequest(Object req, SocketState sendState, SocketState nextState) throws Exception {
        final String parsedJson = objectMapper.writeValueAsString(req);
        final String encrypted = CryptoUtils.rsaAysmEncrypt(parsedJson, serverPublicKey);
        printWriter.println(sendState.generateBody(encrypted));
        socketState = nextState;
    }

    private <T> T exchangeSSLResponse(Class<T> parseClass) throws Exception {
        final String decrypted = CryptoUtils.rsaAsymDecrypt(readData(), clientKeypair.getPrivate());
        return objectMapper.readValue(decrypted, parseClass);
    }

    private String readData() throws Exception {
        final String data = bufferedReader.readLine();
        if (data == null) {
            throw new SocketException();
        }
        return data;
    }

    @Override
    public synchronized void start() {
        try {
            clientSocket = createSocket();
            clientKeypair = CryptoUtils.generateRsaKeypair();
            if (!isAlive()) {
                setName("Thread-TCP-" + getId());
                super.start();
            }
        } catch (IOException | GeneralSecurityException ex) {
            log.error(ex.getMessage());
            connectionHandler.onFailure(connectionDetails, null);
        }
    }

    public void stopAndClear() {
        log.info("Disconnected with host: {}:{}", fastConnectionDetails.getHostIpAddress(),
            fastConnectionDetails.getHostPort());
        log.info("Stopping TCP connection thread: {}", getName());
        log.debug("Collected detatched thread with TID {} by GC", getName());
        if (clientDatagramSocket != null) {
            clientDatagramSocket.stopAndClear();
        }
        try {
            clientSocket.close();
        } catch (IOException ex) {
            throw new UnoperableException(ex);
        }
    }

    private Socket createSocket() throws IOException {
        final String ipAddress = fastConnectionDetails.getHostIpAddress();
        final int port = fastConnectionDetails.getHostPort();
        final Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, port));
        log.info("Successfully created connection with {}:{} server", ipAddress, port);
        return socket;
    }
}