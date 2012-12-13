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
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
    public static final String REMOTESERVER = "remoteserver";
    public static final String REMOTENAMESPACE = "remotenamespace";
    public static final String REMOTEUSER = "remoteuser";
    public static final String REMOTEPASSWORD = "remotepassword";
    
    private File propertiesFile;
    private final JComboBox serverField;
    private final JTextField namespaceField;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JButton saveButton;
    private final Properties properties;

    public CredentialsDialog(File propertiesFile) throws IOException
    {
        this.propertiesFile = propertiesFile;
        setTitle(I18n.get("LOGIN"));
        properties = new Properties();
        try (FileInputStream pFile = new FileInputStream(propertiesFile);)
        {
            properties.load(pFile);
        }
        String[] servers = properties.getProperty(REMOTESERVER, "").split(",");
        serverField = new JComboBox(servers);
        serverField.setToolTipText(I18n.get("REMOTE SERVER URL"));
        serverField.setEditable(true);
        namespaceField = new JTextField(properties.getProperty(REMOTENAMESPACE), 30);
        emailField = new JTextField(properties.getProperty(REMOTEUSER), 30);
        emailField.setToolTipText(I18n.get("REMOTE SERVER USERNAME (= EMAIL ADDRESS)"));
        passwordField = new JPasswordField(properties.getProperty(REMOTEPASSWORD), 30);
        passwordField.setToolTipText(I18n.get("REMOTE SERVER PASSWORD"));
        
        saveButton = new JButton(new SaveAction());
        buttonPanel.add(saveButton);

        JPanel panel = new JPanel(new SpringLayout());
        add(panel, BorderLayout.CENTER);
        
        panel.add(new JLabel(I18n.get("REMOTE SERVER"), JLabel.TRAILING));
        panel.add(serverField);
        
        panel.add(new JLabel(I18n.get("NAMESPACE"), JLabel.TRAILING));
        panel.add(namespaceField);
        
        panel.add(new JLabel(I18n.get("EMAIL"), JLabel.TRAILING));
        panel.add(emailField);
        
        panel.add(new JLabel(I18n.get("PASSWORD"), JLabel.TRAILING));
        panel.add(passwordField);
        
        SpringUtilities.makeCompactGrid(panel,
                4, 2, //rows, cols
                6, 6, //initX, initY
                6, 6);       //xPad, yPad    
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    }

    @Override
    public boolean input()
    {
        if (super.input())
        {
            properties.setProperty(REMOTESERVER, (String) serverField.getSelectedItem());
            properties.setProperty(REMOTENAMESPACE, namespaceField.getText());
            properties.setProperty(REMOTEUSER, emailField.getText());
            properties.setProperty(REMOTEPASSWORD, new String(passwordField.getPassword()));
            return true;
        }
        return false;
    }

    public Properties getProperties()
    {
        return properties;
    }
    
    private class SaveAction extends AbstractAction
    {

        public SaveAction()
        {
            super(I18n.get("SAVE"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("SAVE THE CREDENTIALS TO PROPERTIES FILE"));
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            String selected = (String) serverField.getSelectedItem();
            String[] servers = properties.getProperty(REMOTESERVER, "").split(",");
            List<String> sl = new ArrayList<>();
            for (String srv : servers)
            {
                sl.add(srv);
            }
            sl.remove(selected);
            sl.add(0, selected);
            StringBuilder sb = new StringBuilder();
            for (String s : sl)
            {
                sb.append(s);
                sb.append(',');
            }
            sb.setLength(sb.length()-1);
            properties.setProperty(REMOTESERVER, sb.toString());
            properties.setProperty(REMOTENAMESPACE, namespaceField.getText());
            properties.setProperty(REMOTEUSER, emailField.getText());
            properties.setProperty(REMOTEPASSWORD, new String(passwordField.getPassword()));
            try (FileOutputStream fos = new FileOutputStream(propertiesFile))
            {
                properties.store(fos, "");
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }
}
