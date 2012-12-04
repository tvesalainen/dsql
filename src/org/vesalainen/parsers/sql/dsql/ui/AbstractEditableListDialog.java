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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * @author Timo Vesalainen
 */
public abstract class AbstractEditableListDialog<T> extends ListDialog<T>
{

    public AbstractEditableListDialog(Frame owner, List<T> list)
    {
        super(owner, list);
        init();
    }

    private void init()
    {
        JMenu editMenu = new JMenu(I18n.get("EDIT"));
        menuBar.add(editMenu);
        RemoveAction deleteAction = new RemoveAction();
        editMenu.add(deleteAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        buttonPanel.add(new JButton(deleteAction));
        InsertAction insertAction = new InsertAction();
        editMenu.add(insertAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
        buttonPanel.add(new JButton(insertAction));
    }

    protected abstract T create(String str);
    
    private class RemoveAction extends AbstractAction
    {

        public RemoveAction()
        {
            super(I18n.get("REMOVE"));
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int index = list.getSelectedIndex();
            if (index != -1)
            {
                removeElementAt(index);
            }
        }
        
    }
    private class InsertAction extends AbstractAction
    {

        public InsertAction()
        {
            super(I18n.get("INSERT"));
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int index = list.getSelectedIndex();
            if (index != -1)
            {
                String value = JOptionPane.showInputDialog(rootPane, I18n.get("ENTER VALUE"));
                insertElementAt(index, create(value));
            }
        }
        
    }
}
