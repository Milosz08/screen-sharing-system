/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.host.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pl.polsl.screensharing.host.model.SessionDetails;
import pl.polsl.screensharing.host.net.ConnectionHandler;
import pl.polsl.screensharing.host.net.DatagramKeys;
import pl.polsl.screensharing.host.net.ServerTcpSocker;
import pl.polsl.screensharing.host.state.HostState;
import pl.polsl.screensharing.host.state.SessionState;
import pl.polsl.screensharing.host.view.HostWindow;
import pl.polsl.screensharing.host.view.dialog.SessionDetailsDialogWindow;
import pl.polsl.screensharing.lib.Utils;
import pl.polsl.screensharing.lib.gui.component.JAppPasswordTextField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
public class SessionDetailsController implements ConnectionHandler {
    private final HostWindow hostWindow;
    private final SessionDetailsDialogWindow sessionDetailsDialogWindow;

    public void createSession() {
        final HostState hostState = hostWindow.getHostState();
        if (sessionDetailsDialogWindow.getIpAddressTextField().getText().equals(StringUtils.EMPTY) ||
            sessionDetailsDialogWindow.getPortTextField().getText().equals(StringUtils.EMPTY)
        ) {
            JOptionPane.showMessageDialog(null, "Fill all necesarry fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final String password = new String(sessionDetailsDialogWindow.getPasswordTextField().getPassword());
        final SessionDetails sessionDetails = instantiateSessionDetails(password);
        hostState.updateSessionDetails(sessionDetails);

        final DatagramKeys datagramKeys = new DatagramKeys();
        datagramKeys.generateKeys();
        hostWindow.setDatagramKeys(datagramKeys);

        final ServerTcpSocker serverTcpSocker = new ServerTcpSocker(hostWindow, this);
        hostWindow.setServerTcpSocker(serverTcpSocker);
        serverTcpSocker.start();
    }

    public void closeWindow() {
        sessionDetailsDialogWindow.closeWindow();
        sessionDetailsDialogWindow.dispose();
    }

    public void resetSaveButtonState() {
        final JButton saveDetailsButton = sessionDetailsDialogWindow.getSaveDetailsButton();
        saveDetailsButton.setEnabled(true);
    }

    public void togglePasswordField(ActionEvent event) {
        final JCheckBox checkBox = (JCheckBox) event.getSource();
        final JAppPasswordTextField passwordField = sessionDetailsDialogWindow.getPasswordTextField();
        passwordField.toggleVisibility(checkBox.isSelected());
    }

    public void togglePasswordInputField(ActionEvent event) {
        final JCheckBox checkBox = (JCheckBox) event.getSource();
        final JAppPasswordTextField passwordField = sessionDetailsDialogWindow.getPasswordTextField();
        final JCheckBox showPasswordField = sessionDetailsDialogWindow.getPasswordTogglerCheckbox();
        passwordField.setEnabled(checkBox.isSelected());
        showPasswordField.setEnabled(checkBox.isSelected());
        resetSaveButtonState();
    }

    public void toggleMachineIpAddressField(ActionEvent event) {
        final JCheckBox checkBox = (JCheckBox) event.getSource();
        final JTextField ipAddressField = sessionDetailsDialogWindow.getIpAddressTextField();
        if (checkBox.isSelected()) {
            ipAddressField.setText(Utils.getMachineAddress());
        }
        ipAddressField.setEnabled(!checkBox.isSelected());
        resetSaveButtonState();
    }

    public void saveConnectionDetails(ActionEvent event) {
        final HostState hostState = hostWindow.getHostState();
        final JButton saveDetailsButton = (JButton) event.getSource();

        final String password = new String(sessionDetailsDialogWindow.getPasswordTextField().getPassword());
        final SessionDetails sessionDetails = instantiateSessionDetails(Base64.getEncoder()
            .encodeToString(password.getBytes()));

        hostState.updateSessionDetails(sessionDetails);
        hostState.getPersistedStateLoader().persistSessionDetails();
        log.info("Updated session details: {}", sessionDetails);

        saveDetailsButton.setEnabled(false);

        JOptionPane.showConfirmDialog(sessionDetailsDialogWindow, "Your session details was successfully saved.",
            "Info", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }

    private SessionDetails instantiateSessionDetails(String password) {
        return SessionDetails.builder()
            .ipAddress(sessionDetailsDialogWindow.getIpAddressTextField().getText())
            .port(Integer.parseInt(sessionDetailsDialogWindow.getPortTextField().getText()))
            .isMachineIp(sessionDetailsDialogWindow.getIsMachineIpAddress().isSelected())
            .hasPassword(sessionDetailsDialogWindow.getHasPasswordCheckbox().isSelected())
            .password(password)
            .build();
    }

    @Override
    public void onSuccess() {
        final HostState hostState = hostWindow.getHostState();
        final BottomInfobarController bottomInfoBarController = hostWindow.getBottomInfobarController();
        closeWindow();
        JOptionPane.showMessageDialog(null,
            "Session was successfully created. Users can join with provided credentials.");
        hostState.updateSessionState(SessionState.CREATED);
        bottomInfoBarController.startSessionTimer();
    }

    @Override
    public void onFailure(Throwable throwable) {
        final HostState hostState = hostWindow.getHostState();
        JOptionPane.showMessageDialog(sessionDetailsDialogWindow, throwable.getMessage(), "Error",
            JOptionPane.ERROR_MESSAGE);
        hostState.updateSessionState(SessionState.INACTIVE);
    }
}