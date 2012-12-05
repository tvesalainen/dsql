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

import com.google.appengine.api.datastore.Key;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author Timo Vesalainen
 */
public class ListDialog<T> extends OkCancelDialog implements MouseListener, ListSelectionListener
{
    protected JList<T> list;
    protected Model model;
    public ListDialog(Frame owner, List<T> list)
    {
        super(owner);
        model = new Model();
        init();
        refresh(list);
    }

    public void refresh(List<T> data)
    {
        model.data = data;
    }
    public T getSelected()
    {
        return (T) list.getSelectedValue();
    }
    
    public int getSelectedIndex()
    {
        return list.getSelectedIndex();
    }
    
    private void init()
    {
        list = new JList(model);
        list.addMouseListener(this);
        list.addListSelectionListener(this);
        JScrollPane scrollPane = new JScrollPane(list);
        add(scrollPane, BorderLayout.CENTER);
        
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
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
    public void removeElementAt(int index)
    {
        model.removeElementAt(index);
    }

    public void insertElement(T elem)
    {
        model.insertElement(elem);
    }

    public void insertElementAt(int index, T elem)
    {
        model.insertElementAt(index, elem);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
    }

    protected class Model<T> extends AbstractListModel<T>
    {
        private List<T> data;

        public void clear()
        {
            int size = data.size();
            data.clear();
            fireContentsChanged(this, 0, size);
        }

        public boolean add(T e)
        {
            boolean b = data.add(e);
            fireContentsChanged(this, data.size()-1, data.size());
            return b;
        }
        
        @Override
        public int getSize()
        {
            return data.size();
        }

        @Override
        public T getElementAt(int index)
        {
            return data.get(index);
        }

        public void removeElementAt(int index)
        {
            data.remove(index);
            fireContentsChanged(this, index, data.size());
        }
        
        public void insertElement(T elem)
        {
            data.add(elem);
            fireContentsChanged(this, data.size()-1, data.size());
        }
        public void insertElementAt(int index, T elem)
        {
            data.add(index, elem);
            fireContentsChanged(this, index, data.size());
        }
    }

}
