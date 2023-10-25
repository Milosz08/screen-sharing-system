/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.host.view;

import pl.polsl.screensharing.host.view.dialog.AboutDialogWindow;
import pl.polsl.screensharing.host.view.dialog.ConnectionSettingsWindow;
import pl.polsl.screensharing.host.view.dialog.LicenseDialogWindow;
import pl.polsl.screensharing.host.view.fragment.TopMenuBar;
import pl.polsl.screensharing.host.view.fragment.TopToolbar;
import pl.polsl.screensharing.lib.AppType;
import pl.polsl.screensharing.lib.gui.AbstractRootFrame;

import javax.swing.*;
import java.awt.*;

public class HostWindow extends AbstractRootFrame {
    private final AboutDialogWindow aboutDialogWindow;
    private final LicenseDialogWindow licenseDialogWindow;
    private final TopMenuBar topMenuBar;
    private final TopToolbar topToolbar;

    private final ConnectionSettingsWindow connectionSettingsWindow;

    public HostWindow() {
        super(AppType.HOST, HostWindow.class);

        this.topMenuBar = new TopMenuBar(this);
        this.topToolbar = new TopToolbar(this);

        this.aboutDialogWindow = new AboutDialogWindow(this);
        this.licenseDialogWindow = new LicenseDialogWindow(this);
        this.connectionSettingsWindow = new ConnectionSettingsWindow(this);
    }

    public ConnectionSettingsWindow getConnectionSettingsWindow() {
        return connectionSettingsWindow;
    }

    public AboutDialogWindow getAboutDialogWindow() {
        return aboutDialogWindow;
    }

    public LicenseDialogWindow getLicenseDialogWindow() {
        return licenseDialogWindow;
    }

    @Override
    protected void extendsFrame(JFrame frame, JPanel rootPanel) {
        frame.setJMenuBar(topMenuBar);
        frame.add(topToolbar, BorderLayout.NORTH);
    }
}
