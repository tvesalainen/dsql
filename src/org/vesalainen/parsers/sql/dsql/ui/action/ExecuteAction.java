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

package org.vesalainen.parsers.sql.dsql.ui.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.event.SwingPropertyChangeSupport;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.Placeholder;
import org.vesalainen.parsers.sql.SelectStatement;
import org.vesalainen.parsers.sql.Statement;
import org.vesalainen.parsers.sql.dsql.ui.InputDialog;

/**
 * @author Timo Vesalainen
 */
public class ExecuteAction extends AbstractAction implements PropertyChangeListener
{
    public static final String PropertyName = "fetchResult";
    protected JFrame frame;
    protected Statement statement;
    
    public ExecuteAction(JFrame frame)
    {
        super("Execute");
        this.frame = frame;
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (enterPlaceHolders(statement))
        {
            FetchResult fetchResult = statement.execute();
            firePropertyChange(PropertyName, null, fetchResult);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (DSqlParseAction.PropertyName.equals(evt.getPropertyName()))
        {
            statement = (Statement) evt.getNewValue();
            if (statement != null)
            {
                setEnabled(true);
                if (statement instanceof SelectStatement)
                {
                    putValue(NAME, "Select");
                }
                else
                {
                    putValue(NAME, "Execute");
                }
            }
            else
            {
                setEnabled(false);
            }
        }
    }

    protected boolean enterPlaceHolders(Statement statement)
    {
        LinkedHashMap<String,Placeholder> placeholderMap = statement.getPlaceholderMap();
        if (!placeholderMap.isEmpty())
        {
            
            InputDialog inputDialog = new InputDialog(frame, "Enter Placeholder Values");
            for (Map.Entry<String,Placeholder> entry : placeholderMap.entrySet())
            {
                Placeholder ph = entry.getValue();
                inputDialog.add(ph.getName(), ph.getValue(), ph.getType());
            }
            if (inputDialog.input())
            {
                int row = 0;
                for (Map.Entry<String,Placeholder> entry : placeholderMap.entrySet())
                {
                    Placeholder ph = entry.getValue();
                    statement.bindValue(ph.getName(), inputDialog.get(row++));
                }
                return true;
            }
            else
            {
                return false;
            }
        }
        return true;
    }

}
