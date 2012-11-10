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

import javax.swing.table.AbstractTableModel;
import org.vesalainen.parsers.sql.FetchResult;

/**
 * @author Timo Vesalainen
 */
public class FetchResultTableModel extends AbstractTableModel
{
    private FetchResult fetchResult;
    private Class<?>[] columnClass;

    public FetchResultTableModel(FetchResult fetchResult)
    {
        this.fetchResult = fetchResult;
        checkClasses();
    }

    public void updateDate(FetchResult fetchResult)
    {
        this.fetchResult = fetchResult;
        columnClass = null;
        checkClasses();
        fireTableStructureChanged();
    }
    public void clear()
    {
        this.fetchResult = null;
        columnClass = null;
        fireTableStructureChanged();
    }
    @Override
    public Object getValueAt(int row, int column)
    {
        return fetchResult.getValueAt(row, column);
    }

    @Override
    public int getRowCount()
    {
        if (fetchResult != null)
        {
            return fetchResult.getRowCount();
        }
        return 0;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return fetchResult.getColumnName(columnIndex);
    }

    @Override
    public int getColumnCount()
    {
        if (fetchResult != null)
        {
            return fetchResult.getColumnCount();
        }
        return 0;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        if (columnClass[columnIndex] != null)
        {
            return columnClass[columnIndex];
        }
        else
        {
            return Object.class;
        }
    }

    private void checkClasses()
    {
        int rows = fetchResult.getRowCount();
        if (rows > 0)
        {
            columnClass = new Class<?>[fetchResult.getColumnCount()];
            for (int col=0;col<columnClass.length;col++)
            {
                Object value = fetchResult.getValueAt(0, col);
                if (value != null)
                {
                    columnClass[col] = value.getClass();
                }
            }
            for (int row=1;row<rows;row++)
            {
                for (int col=0;col<columnClass.length;col++)
                {
                    Object value = fetchResult.getValueAt(row, col);
                    if (value != null)
                    {
                        if (columnClass[col] == null)
                        {
                            columnClass[col] = value.getClass();
                        }
                        else
                        {
                            if (!columnClass[col].isAssignableFrom(value.getClass()))
                            {
                                if (value.getClass().isAssignableFrom(columnClass[col]))
                                {
                                    columnClass[col] = value.getClass();
                                }
                                else
                                {
                                    columnClass[col] = Object.class;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    
}
