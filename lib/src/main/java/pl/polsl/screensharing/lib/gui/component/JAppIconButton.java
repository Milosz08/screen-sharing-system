/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.lib.gui.component;

import pl.polsl.screensharing.lib.gui.file.FileUtils;
import pl.polsl.screensharing.lib.gui.icon.AppIcon;

import javax.swing.*;

public class JAppIconButton extends JButton {
    public JAppIconButton(String text, AppIcon iconName) {
        this(text, iconName, false);
    }

    public JAppIconButton(String text, AppIcon iconName, boolean setDescription) {
        if (setDescription) {
            setToolTipText(text);
        } else {
            setText(text);
        }
        FileUtils.getImageIconFromResources(getClass(), iconName).ifPresent(this::setIcon);
    }

    public JAppIconButton(String text, AppIcon iconName, boolean setDescription, boolean isEnabled) {
        this(text, iconName, setDescription);
        setEnabled(isEnabled);
    }
}
