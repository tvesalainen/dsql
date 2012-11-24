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
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * @author Timo Vesalainen
 */
public class ListDialog<T> extends OkCancelDialog implements MouseListener
{
    private JList list;
    private DefaultListModel model;

    public ListDialog(Frame owner, Collection<T> list)
    {
        super(owner);
        refresh(list);
    }

    public void refresh(Collection<T> list)
    {
        model.clear();
        for (T t : list)
        {
            model.addElement(t);
        }
    }
    public T getSelected()
    {
        return (T) list.getSelectedValue();
    }
    
    @Override
    protected void init()
    {
        super.init();

        model = new DefaultListModel();
        list = new JList(model);
        list.addMouseListener(this);
        JScrollPane scrollPane = new JScrollPane(list);
        add(scrollPane, BorderLayout.CENTER);
        
        setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (e.getClickCount() > 1)
        {
            accepted = true;
            setVisible(false);
        }
    }

}
