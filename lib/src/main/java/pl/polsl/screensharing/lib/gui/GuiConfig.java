/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.lib.gui;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class GuiConfig {
    private GuiConfig() {
    }

    public static void setDefaultLayout() {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }
}
