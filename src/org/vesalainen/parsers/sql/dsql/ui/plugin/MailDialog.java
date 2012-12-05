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
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.vesalainen.parsers.sql.dsql.ui.I18n;

/**
 * @author Timo Vesalainen
 */
public class MailDialog extends MessageDialog
{
    private JTextField subjectField;

    public MailDialog(JFrame owner, Action sendAction)
    {
        super(owner, sendAction);
        init();
    }

    public String getSubject()
    {
        return subjectField.getText();
    }
    
    public void setSubject(String subject)
    {
        subjectField.setText(subject);
    }

    private void init()
    {
        // Subject
        JPanel subjectPanel = new JPanel();
        subjectPanel.setLayout(new FlowLayout());
        add(subjectPanel, BorderLayout.NORTH);
        JLabel subjectLabel = new JLabel(I18n.get("SUBJECT"));
        subjectPanel.add(subjectLabel);
        subjectField = new JTextField(60);
        subjectPanel.add(subjectField);
        
    }
    

}
