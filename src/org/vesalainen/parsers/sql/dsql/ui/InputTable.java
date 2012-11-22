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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * @author Timo Vesalainen
 */
public class InputTable extends DSJTable
{

    public InputTable()
    {
        super(new Model());
    }
    
    public void add(String label, Object value, Class<?> type)
    {
        Model model = (Model) dataModel;
        model.list.add(new Row(label, value, type));
    }

    public Object get(int row)
    {
        Model model = (Model) dataModel;
        Row r = model.list.get(row);
        return r.value;
    }
    @Override
    public TableCellRenderer getCellRenderer(int row, int column)
    {
        if (column == 1)
        {
            Model model = (Model) dataModel;
            Row r = model.list.get(row);
            return getDefaultRenderer(r.type);
        }
        return super.getCellRenderer(row, column);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column)
    {
        if (column == 1)
        {
            Model model = (Model) dataModel;
            Row r = model.list.get(row);
            return getDefaultEditor(r.type);
        }
        return super.getCellEditor(row, column);
    }
    
    
    private static class Model extends AbstractTableModel
    {
        private List<Row> list = new ArrayList<>();
        
        @Override
        public int getRowCount()
        {
            return list.size();
        }

        @Override
        public int getColumnCount()
        {
            return 2;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            switch (columnIndex)
            {
                case 0:
                    return false;
                case 1:
                    return true;
                default:
                    throw new IllegalArgumentException(columnIndex+" out of range");
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            Row row = list.get(rowIndex);
            switch (columnIndex)
            {
                case 1:
                    row.value = aValue;
                    break;
                default:
                    throw new IllegalArgumentException(columnIndex+" out of range");
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Row row = list.get(rowIndex);
            switch (columnIndex)
            {
                case 0:
                    return row.label;
                case 1:
                    return row.value;
                default:
                    throw new IllegalArgumentException(columnIndex+" out of range");
            }
        }
    }
    
    private class Row
    {
        private String label;
        private Object value;
        private Class<?> type;

        public Row(String label, Object value, Class<?> type)
        {
            this.label = label;
            this.value = value;
            this.type = type;
        }

    }
}
