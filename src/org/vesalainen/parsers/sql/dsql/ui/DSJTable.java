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

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.Text;
import java.awt.Component;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * @author Timo Vesalainen
 */
public class DSJTable extends JTable
{

    public DSJTable(Object[][] rowData, Object[] columnNames)
    {
        super(rowData, columnNames);
        init();
    }

    public DSJTable(Vector rowData, Vector columnNames)
    {
        super(rowData, columnNames);
        init();
    }

    public DSJTable(int numRows, int numColumns)
    {
        super(numRows, numColumns);
        init();
    }

    public DSJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
    {
        super(dm, cm, sm);
        init();
    }

    public DSJTable(TableModel dm, TableColumnModel cm)
    {
        super(dm, cm);
        init();
    }

    public DSJTable(TableModel dm)
    {
        super(dm);
        init();
    }

    public DSJTable()
    {
        init();
    }

    private void init()
    {
        setDefaultRenderer(Rating.class, new RatingTableCellRenderer());
        setDefaultRenderer(PostalAddress.class, new PostalAddressTableCellRenderer());
        setDefaultRenderer(PhoneNumber.class, new PhoneNumberTableCellRenderer());
        setDefaultRenderer(Link.class, new LinkTableCellRenderer());
        setDefaultRenderer(Text.class, new TextTableCellRenderer());
        setDefaultRenderer(Email.class, new EmailTableCellRenderer());
    }

    public class RatingTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Rating rating = (Rating) value;
                super.setValue(rating.getRating());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class PostalAddressTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                PostalAddress address = (PostalAddress) value;
                super.setValue(address.getAddress());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class PhoneNumberTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                PhoneNumber number = (PhoneNumber) value;
                super.setValue(number.getNumber());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class LinkTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Link link = (Link) value;
                super.setValue(link.getValue());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class TextTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Text text = (Text) value;
                super.setValue(text.getValue());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class EmailTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Email email = (Email) value;
                super.setValue(email.getEmail());
            }
            else
            {
                super.setValue(value);
            }
        }
    }
    public class TooltippedTableCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setToolTipText(getText());
            return component;
        }
    }
    
}
