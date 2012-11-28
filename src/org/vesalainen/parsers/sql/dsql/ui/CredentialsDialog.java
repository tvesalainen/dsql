/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.parsers.sql.dsql.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * @author Timo Vesalainen
 */
public class CredentialsDialog extends OkCancelDialog
{
    private final JTextField serverField;
    private final JTextField emailField;
    private final JPasswordField passwordField;

    public CredentialsDialog(String server, String email, String password)
    {
        setTitle("Login");
        serverField = new JTextField(server, 30);
        emailField = new JTextField(email, 30);
        passwordField = new JPasswordField(password, 30);

        JPanel panel = new JPanel(new SpringLayout());
        add(panel, BorderLayout.CENTER);
        
        panel.add(new JLabel("Server", JLabel.TRAILING));
        panel.add(serverField);
        
        panel.add(new JLabel("Email", JLabel.TRAILING));
        panel.add(emailField);
        
        panel.add(new JLabel("Password", JLabel.TRAILING));
        panel.add(passwordField);
        
        SpringUtilities.makeCompactGrid(panel,
                3, 2, //rows, cols
                6, 6, //initX, initY
                6, 6);       //xPad, yPad    
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    }
    
    public String getEmail()
    {
        return emailField.getText();
    }

    public char[] getPassword()
    {
        return passwordField.getPassword();
    }

    public String getServer()
    {
        return serverField.getText();
    }
    
}
