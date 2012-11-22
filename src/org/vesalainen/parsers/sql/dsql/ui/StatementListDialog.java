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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import org.vesalainen.parsers.sql.FetchResult;

/**
 * @author Timo Vesalainen
 */
public class StatementListDialog extends OkCancelDialog
{
    private JButton refreshButton;
    private JList list;
    private DefaultListModel model;
    private WorkBench workBench;

    public StatementListDialog(WorkBench workBench)
    {
        super(workBench.frame);
        this.workBench = workBench;
        refresh();
    }

    private void refresh()
    {
        model.clear();
        FetchResult results = workBench.engine.execute("select "+Entity.KEY_RESERVED_PROPERTY+" from "+workBench.storedStatementsKind);
        for (int row = 0;row < results.getRowCount();row++)
        {
            Key key = (Key) results.getValueAt(row, 0);
            model.addElement(key.getName());
        }
    }

    public String getSelected()
    {
        return (String) list.getSelectedValue();
    }
    
    @Override
    protected void init()
    {
        super.init();

        model = new DefaultListModel();
        list = new JList(model);
        JScrollPane scrollPane = new JScrollPane(list);
        add(scrollPane, BorderLayout.CENTER);
        
        refreshButton = new JButton("Refresh");
        ActionListener refreshAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                refresh();
            }
        };
        refreshButton.addActionListener(refreshAction);
        buttonPanel.add(refreshButton);
        setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
    }

}
