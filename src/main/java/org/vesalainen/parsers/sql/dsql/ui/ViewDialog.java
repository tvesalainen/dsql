/*
 * Copyright (C) 2013 Timo Vesalainen
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
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.vesalainen.parsers.sql.dsql.ui.action.PrintAction;
import org.vesalainen.parsers.sql.dsql.ui.action.StatementDialog;

/**
 * @author Timo Vesalainen
 */
public class ViewDialog extends CancelDialog
{
    private DSJTable table;
    private FetchResultTableModel model;
    private final PrintAction printAction;

    public ViewDialog(FetchResultTableModel model, PrintAction printAction)
    {
        super();
        this.model = model;
        this.printAction = printAction;
        init();
    }

    public void refresh(FetchResultTableModel model)
    {
        this.model = model;
        table.setModel(model);
    }
    
    private void init()
    {
        table = new DSJTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        JButton jButton = new JButton(printAction);
        buttonPanel.add(jButton);
        
        setMinimumSize(new Dimension(800, 500));
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    }
    
}
