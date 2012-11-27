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
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JFrame;
import javax.swing.table.TableCellEditor;

/**
 * @author Timo Vesalainen
 */
public class InputDialog extends OkCancelDialog
{
    private InputTable table;

    public InputDialog(Frame owner, String title)
    {
        super(owner, title);
        init();
    }

    public InputDialog(String title)
    {
        init();
        setTitle(title);
    }

    public void add(String label, Object value, Class<?> type)
    {
        table.add(label, value, type);
    }

    public void setFrame(JFrame frame)
    {
        table.setFrame(frame);
    }

    public Object get(int row)
    {
        return table.get(row);
    }

    @Override
    public boolean input()
    {
        boolean result = super.input();
        TableCellEditor cellEditor = table.getCellEditor();
        if (cellEditor != null)
        {
            cellEditor.stopCellEditing();
        }
        return result;
    }

    private void init()
    {
        table = new InputTable();
        add(table, BorderLayout.CENTER);
        setMinimumSize(new Dimension(800, 0));
        setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
    }

}
