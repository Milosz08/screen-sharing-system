/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.client.view.fragment;

import pl.polsl.screensharing.lib.gui.AbstractPopupDialog;
import pl.polsl.screensharing.lib.gui.component.JAppPasswordTextField;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PasswordPopupPanel extends JPanel {
    private final AbstractPopupDialog root;

    private final JAppPasswordTextField passwordTextField;
    private final JCheckBox passwordTogglerCheckbox;
    private final String[] options = { "OK", "Cancel" };

    public PasswordPopupPanel(AbstractPopupDialog root) {
        this.root = root;

        this.passwordTextField = new JAppPasswordTextField(10);
        this.passwordTogglerCheckbox = new JCheckBox("Show password");

        this.passwordTogglerCheckbox.addActionListener(this::togglePasswordField);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(passwordTextField);
        add(passwordTogglerCheckbox);
    }

    public String showPopupAndWaitForInput() {
        final int option = JOptionPane.showOptionDialog(root, this, "Insert password",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, options, options[1]);
        if (option != 0) {
            return null;
        }
        char[] password = passwordTextField.getPassword();
        return new String(password);
    }

    private void togglePasswordField(ActionEvent event) {
        final JCheckBox checkBox = (JCheckBox) event.getSource();
        passwordTextField.toggleVisibility(checkBox.isSelected());
    }
}
