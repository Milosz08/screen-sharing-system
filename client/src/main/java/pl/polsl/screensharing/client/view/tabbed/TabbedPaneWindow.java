/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.client.view.tabbed;

import lombok.Getter;
import pl.polsl.screensharing.lib.AppType;
import pl.polsl.screensharing.lib.gui.fragment.JAppTabbedLogsPanel;

import javax.swing.*;

@Getter
public class TabbedPaneWindow extends JTabbedPane {
    private final TabbedVideoStreamPanel tabbedVideoStreamPanel;
    private final JAppTabbedLogsPanel tabbedLogsPanel;

    public TabbedPaneWindow() {
        this.tabbedVideoStreamPanel = new TabbedVideoStreamPanel();
        this.tabbedLogsPanel = new JAppTabbedLogsPanel(AppType.CLIENT);

        addTab("Video stream", tabbedVideoStreamPanel);
        addTab("Logs", tabbedLogsPanel);
    }
}
