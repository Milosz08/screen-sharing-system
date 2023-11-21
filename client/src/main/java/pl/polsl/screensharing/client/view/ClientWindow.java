/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.client.view;

import lombok.Getter;
import lombok.Setter;
import pl.polsl.screensharing.client.controller.BottomInfobarController;
import pl.polsl.screensharing.client.net.ClientDatagramSocket;
import pl.polsl.screensharing.client.state.ClientState;
import pl.polsl.screensharing.client.view.dialog.AboutDialogWindow;
import pl.polsl.screensharing.client.view.dialog.ConnectWindow;
import pl.polsl.screensharing.client.view.dialog.LastConnectionsWindow;
import pl.polsl.screensharing.client.view.dialog.LicenseDialogWindow;
import pl.polsl.screensharing.client.view.fragment.BottomInfobar;
import pl.polsl.screensharing.client.view.fragment.TopMenuBar;
import pl.polsl.screensharing.client.view.fragment.TopToolbar;
import pl.polsl.screensharing.client.view.fragment.VideoCanvas;
import pl.polsl.screensharing.client.view.tabbed.TabbedPaneWindow;
import pl.polsl.screensharing.lib.AppType;
import pl.polsl.screensharing.lib.gui.AbstractRootFrame;

import javax.swing.*;
import java.awt.*;

@Getter
public class ClientWindow extends AbstractRootFrame {
    private final ClientState clientState;

    private final TopMenuBar topMenuBar;
    private final TopToolbar topToolbar;
    private final TabbedPaneWindow tabbedPaneWindow;
    private final BottomInfobar bottomInfobar;

    private final ConnectWindow connectWindow;
    private final LastConnectionsWindow lastConnectionsWindow;
    private final AboutDialogWindow aboutDialogWindow;
    private final LicenseDialogWindow licenseDialogWindow;

    @Setter
    private ClientDatagramSocket clientDatagramSocket;

    public ClientWindow(ClientState clientState) {
        super(AppType.CLIENT, clientState, ClientWindow.class);
        this.clientState = clientState;

        topMenuBar = new TopMenuBar(this);
        topToolbar = new TopToolbar(this);
        tabbedPaneWindow = new TabbedPaneWindow(this);
        bottomInfobar = new BottomInfobar(this);

        connectWindow = new ConnectWindow(this);
        lastConnectionsWindow = new LastConnectionsWindow(this);
        aboutDialogWindow = new AboutDialogWindow(this);
        licenseDialogWindow = new LicenseDialogWindow(this);
    }

    @Override
    protected void extendsFrame(JFrame frame, JPanel rootPanel) {
        frame.setJMenuBar(topMenuBar);
        frame.add(topToolbar, BorderLayout.NORTH);
        frame.add(tabbedPaneWindow, BorderLayout.CENTER);
        frame.add(bottomInfobar, BorderLayout.SOUTH);
    }

    public BottomInfobarController getBottomInfobarController() {
        return bottomInfobar.getBottomInfobarController();
    }

    public VideoCanvas getVideoCanvas() {
        return tabbedPaneWindow.getTabbedVideoStreamPanel().getVideoCanvas();
    }
}
