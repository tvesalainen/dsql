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

package org.vesalainen.parsers.sql.dsql.ui.plugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;
import org.vesalainen.parsers.sql.dsql.ui.I18n;
import org.vesalainen.parsers.sql.dsql.ui.TextDialog;

/**
 * @author Timo Vesalainen
 */
public class MessageDialog extends TextDialog
{
    private Action sendAction;
    private ReplacerAction action;
    private JButton tagButton;
    private JTextField subjectField;
    private JMenuBar menuBar;

    public MessageDialog(JFrame owner, Action sendAction)
    {
        super(owner);
        this.sendAction = sendAction;
        init();
    }

    public boolean input(FetchResultTableModel model)
    {
        action.setFetchResult(model);
        return super.input();
    }

    public String getSubject()
    {
        return subjectField.getText();
    }
    
    public String getBody()
    {
        return textPane.getText();
    }
    
    public void setSubject(String subject)
    {
        subjectField.setText(subject);
    }
    
    public void setBody(String body)
    {
        textPane.setText(body);
    }
    
    private void init()
    {
        textPane.setContentType("text/html");
        // Subject
        JPanel subjectPanel = new JPanel();
        subjectPanel.setLayout(new FlowLayout());
        add(subjectPanel, BorderLayout.NORTH);
        JLabel subjectLabel = new JLabel(I18n.get("SUBJECT"));
        subjectPanel.add(subjectLabel);
        subjectField = new JTextField(60);
        subjectPanel.add(subjectField);
        
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu(I18n.get("EDIT"));
        menuBar.add(fileMenu);
        
        fileMenu.add(sendAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
        okButton.setAction(sendAction);
        
        action = new ReplacerAction((Frame)getParent());
        fileMenu.add(action).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));
        
        tagButton = new JButton(action);
        buttonPanel.add(tagButton);
        
    }

}
