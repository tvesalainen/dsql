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

package org.vesalainen.parsers.sql.dsql.ui.plugin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import javax.swing.Action;
import javax.swing.JFrame;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultPlugin;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;
import org.vesalainen.parsers.sql.dsql.ui.action.PersistenceHandler;

/**
 * @author Timo Vesalainen
 */
public abstract class AbstractMessagePlugin extends FetchResultPlugin
{
    public static final String SubjectProperty = AbstractMessagePlugin.class.getName()+".subject";
    public static final String BodyProperty = AbstractMessagePlugin.class.getName()+".body";
    
    protected Action sendAction;
    protected MessageDialog dialog;
    protected JFrame owner;
    protected FetchResultTableModel model;

    public AbstractMessagePlugin(String name, Action sendAction)
    {
        super(name);
        this.sendAction = sendAction;
        dialog = createMessageDialog(owner, sendAction);
    }
    
    @Override
    public void handle(JFrame owner, FetchResultTableModel model)
    {
        this.owner = owner;
        this.model = model;
        setEnabled(true);
    }

    @Override
    public void disable()
    {
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        dialog.input(model);
    }

    protected MessageDialog createMessageDialog(JFrame owner, Action sendAction)
    {
        return new MessageDialog(owner, sendAction);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException
    {
        Entity entity = (Entity) evt.getNewValue();
        switch (evt.getPropertyName())
        {
            case PersistenceHandler.OPEN:
                if (entity != null)
                {
                    // Open
                    dialog.setSubject((String) entity.getProperty(SubjectProperty));
                    Text body = (Text) entity.getProperty(BodyProperty);
                    if (body != null)
                    {
                        dialog.setBody(body.getValue());
                    }
                    else
                    {
                        dialog.setBody(null);
                    }
                }
                else
                {
                    // New
                    dialog.setSubject(null);
                    dialog.setBody(null);
                }
                break;
            case PersistenceHandler.SAVE:
                if (entity != null)
                {
                    // Save
                    String subject = dialog.getSubject();
                    entity.setUnindexedProperty(SubjectProperty, subject);
                    String body = dialog.getBody();
                    Text sql = new Text(body);
                    entity.setUnindexedProperty(BodyProperty, sql);
                }
                else
                {
                    // Remove
                    dialog.setSubject(null);
                    dialog.setBody(null);
                }
                break;
        }
    }
    
}
