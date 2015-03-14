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

import com.google.appengine.api.datastore.Entity;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.FetchResultComboBoxModel;
import org.vesalainen.parsers.sql.Placeholder;
import org.vesalainen.parsers.sql.SelectStatement;
import org.vesalainen.parsers.sql.Statement;
import org.vesalainen.parsers.sql.dsql.GObjectHelper;
import org.vesalainen.parsers.sql.dsql.ui.I18n;
import org.vesalainen.parsers.sql.dsql.ui.InputDialog;

/**
 * @author Timo Vesalainen
 */
public class ExecuteAction extends AbstractAutoAction implements PropertyChangeListener
{
    public static final String PropertyName = "fetchResult";
    public static final String OptionsProperty = "optionsProperty";
    protected JFrame frame;
    protected Statement statement;
    
    public ExecuteAction(JFrame frame)
    {
        super(I18n.get("EXECUTE"));
        putValue(Action.SHORT_DESCRIPTION, I18n.get("EXECUTE THE STATEMENT"));
        this.frame = frame;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK));
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (enterPlaceHolders(statement))
        {
            final ExecuteAction act = this;
            SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
            {
                private FetchResult fetchResult;
                @Override
                protected Void doInBackground() throws Exception
                {
                    ProgressMonitor mon = new ProgressMonitor(frame, "Exec", "", 0, 100);
                    mon.setNote("");
                    statement.getEngine().createProgressMonitor(mon);
                    fetchResult = statement.execute();
                    mon.close();
                    return null;
                }

                @Override
                protected void done()
                {
                    act.firePropertyChange(PropertyName, null, fetchResult);
                }
                
            };
            task.execute();
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
                    putValue(NAME, I18n.get("SELECT"));
                }
                else
                {
                    putValue(NAME, I18n.get("EXECUTE"));
                }
            }
            else
            {
                setEnabled(false);
            }
        }
    }

    protected boolean enterPlaceHolders(Statement<Entity,Object> statement)
    {
        LinkedHashMap<String,Placeholder<Entity,Object>> placeholderMap = statement.getPlaceholderMap();
        if (!placeholderMap.isEmpty())
        {
            
            InputDialog inputDialog = new InputDialog(frame, I18n.get("ENTER PLACEHOLDER VALUES"));
            for (Map.Entry<String,Placeholder<Entity,Object>> entry : placeholderMap.entrySet())
            {
                Placeholder ph = entry.getValue();
                inputDialog.add(ph.getName(), ph.getDefaultValue(), ph.getType());
            }
            if (inputDialog.input())
            {
                int row = 0;
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String,Placeholder<Entity,Object>> entry : placeholderMap.entrySet())
                {
                    Placeholder ph = entry.getValue();
                    Object value = inputDialog.get(row++);
                    statement.bindValue(ph.getName(), value);
                    sb.append(entry.getKey());
                    sb.append("=");
                    if (value instanceof FetchResultComboBoxModel)
                    {
                        FetchResultComboBoxModel<Entity,Object> model = (FetchResultComboBoxModel) value;
                        sb.append(model.getSelectedItem());
                    }
                    else
                    {
                        sb.append(GObjectHelper.getString(value));
                    }
                    sb.append(" ");
                }
                firePropertyChange(OptionsProperty, null, sb.toString());
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            firePropertyChange(OptionsProperty, null, " ");
        }
        return true;
    }

}
