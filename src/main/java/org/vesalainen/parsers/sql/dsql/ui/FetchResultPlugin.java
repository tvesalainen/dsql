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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import javax.swing.JFrame;
import org.vesalainen.parsers.sql.dsql.ui.action.AbstractAutoAction;
import org.vesalainen.parsers.sql.dsql.ui.action.FetchResultHandler;

/**
 *
 * @author Timo Vesalainen
 */
public abstract class FetchResultPlugin<T> extends AbstractAutoAction implements PropertyChangeListener, VetoableChangeListener
{
    private JFrame frame;

    public FetchResultPlugin(String name)
    {
        super(name);
        setEnabled(false);
    }
    
    /**
     * Return true if plugin can handle type
     * @return 
     */
    public abstract boolean accept(Object target);
    /**
     * Handle
     * @param result FetchResult that contains at least one column with requested type
     */
    public abstract void handle(JFrame owner, FetchResultTableModel model);
    public abstract void disable();
    /**
     * Returns the String representation of T. Eg. Email address.
     * @param t
     * @return 
     */
    public abstract String getString(T t);
    
    public abstract boolean activate(Entity data);
    

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (FetchResultHandler.ModelPropertyName.equals(evt.getPropertyName()))
        {
            FetchResultTableModel model = (FetchResultTableModel) evt.getNewValue();
            if (model != null)
            {
                boolean handles = false;
                for (int row = 0;row < model.getRowCount();row++)
                {
                    for (int col = 0;col < model.getColumnCount();col++)
                    {
                        if (accept(model.getValueAt(row, col)))
                        {
                            handles = true;
                            handle(frame, model);
                            break;
                        }
                    }
                }
                if (!handles)
                {
                    disable();
                }
            }
            else
            {
                disable();
            }
        }
    }

    public void setFrame(JFrame frame)
    {
        this.frame = frame;
    }

    
}
