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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.text.TextAction;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;
import org.vesalainen.parsers.sql.dsql.ui.ListDialog;
import org.vesalainen.parsers.sql.dsql.ui.WorkBench;

/**
 * @author Timo Vesalainen
 */
public class PersistenceHandler
{
    public static final String OPEN = "open";
    public static final String SAVE = "save";
    
    private VetoableChangeSupport changeSupport;
    private WorkBench workBench;
    private DSQLEngine engine;
    private String storedStatementsKind;
    private FetchResult statements;
    private static ListDialog<String> dialog;
    private Entity oldEntity;
    private Entity newEntity;
    private Action[] actions;
    private Action newAction;
    private Action opentAction;
    private Action saveAction;
    private Action saveAsAction;
    private Action removeAction;

    public PersistenceHandler(WorkBench workBench, String storedStatementsKind)
    {
        this.workBench = workBench;
        engine = workBench.getEngine();
        changeSupport = new VetoableChangeSupport(this);
        this.storedStatementsKind = storedStatementsKind;
        newAction = new NewStatementAction();
        opentAction = new OpenStatementAction();
        saveAction = new SaveStatementAction();
        saveAsAction = new SaveAsStatementAction();
        removeAction = new RemoveStatementAction();
        actions = new Action[] {
            newAction,
            opentAction,
            saveAction,
            saveAsAction,
            removeAction
        };
    }
    
    public Action[] getActions()
    {
        return actions;
    }
    private boolean confirmInstall()
    {
        if (statements == null)
        {
            statements = engine.execute("select key from "+storedStatementsKind);
        }
        if (statements.getRowCount() == 0)
        {
            int confirm = JOptionPane.showConfirmDialog(
                    workBench.getFrame(), 
                    "Create "
                    +storedStatementsKind+
                    " kind for saved statements? (if kind is not ok, change kind name in properties)", 
                    "Connected datastore doesn't have store for saved statements", 
                    JOptionPane.OK_CANCEL_OPTION
                    );
            return JOptionPane.YES_OPTION == confirm;
        }
        return true;
    }
    protected void open()
    {
        if (dialog == null)
        {
            dialog = new ListDialog(workBench.getFrame(), getStatements());
        }
        if (dialog.input())
        {
            String selected = dialog.getSelected();
            Key key = engine.createKey(storedStatementsKind, selected);
            try
            {
                newEntity = engine.get(key);
                fireVetoableChange(OPEN, oldEntity, newEntity);
                oldEntity = newEntity;
            }
            catch (PropertyVetoException ex)
            {
                JOptionPane.showMessageDialog(workBench.getFrame(), ex.getLocalizedMessage(), "Refuced", JOptionPane.ERROR_MESSAGE);
            }
            catch (EntityNotFoundException ex)
            {
                JOptionPane.showMessageDialog(workBench.getFrame(), ex.getLocalizedMessage(), "Not found", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void save()
    {
        if (newEntity != null)
        {
            try
            {
                fireVetoableChange(SAVE, oldEntity, newEntity);
                engine.update(newEntity);
                oldEntity = newEntity;
            }
            catch (PropertyVetoException ex)
            {
                JOptionPane.showMessageDialog(workBench.getFrame(), ex.getLocalizedMessage(), "Refuced", JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            saveAs();
        }
    }
    private void saveAs()
    {
        if (confirmInstall())
        {
            String name = JOptionPane.showInputDialog(workBench.getFrame(), "Enter name for statement", "");
            if (name != null)
            {
                try
                {
                    oldEntity = newEntity;
                    Key key = engine.createKey(storedStatementsKind, name);
                    newEntity = new Entity(key);
                    newEntity.setPropertiesFrom(oldEntity);
                    fireVetoableChange(SAVE, oldEntity, newEntity);
                    engine.update(newEntity);
                }
                catch (PropertyVetoException ex)
                {
                    JOptionPane.showMessageDialog(workBench.getFrame(), ex.getLocalizedMessage(), "Refuced", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private void remove()
    {
        if (oldEntity != null)
        {
            int confirm = JOptionPane.showConfirmDialog(
                    workBench.getFrame(), 
                    oldEntity.getKey().getName(), 
                    "Confirm Remove?",
                    JOptionPane.OK_CANCEL_OPTION
                    );
            if (JOptionPane.YES_OPTION != confirm)
            {
                try
                {
                    fireVetoableChange(SAVE, oldEntity, null);
                    engine.delete(oldEntity);
                    newEntity = null;
                    oldEntity = null;
                }
                catch (PropertyVetoException ex)
                {
                    JOptionPane.showMessageDialog(workBench.getFrame(), ex.getLocalizedMessage(), "Refuced", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private void clear()
    {
        try
        {
            fireVetoableChange(OPEN, oldEntity, null);
            newEntity = null;
            oldEntity = null;
        }
        catch (PropertyVetoException ex)
        {
            Logger.getLogger(PersistenceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void refresh()
    {
        dialog = new ListDialog(workBench.getFrame(), getStatements());
    }
    private List<String> getStatements()
    {
        List<String> list = new ArrayList<>();
        FetchResult results = workBench.getEngine().execute("select "+Entity.KEY_RESERVED_PROPERTY+" from "+storedStatementsKind);
        for (int row = 0;row < results.getRowCount();row++)
        {
            Key key = (Key) results.getValueAt(row, 0);
            list.add(key.getName());
        }
        return list;
    }

    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        changeSupport.addVetoableChangeListener(listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        changeSupport.removeVetoableChangeListener(listener);
    }

    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException
    {
        changeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    public class NewStatementAction extends TextAction
    {
        public NewStatementAction()
        {
            super("New");
            putValue(Action.SHORT_DESCRIPTION, "Clears the statement editor.");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            clear();
        }
    }
    public class OpenStatementAction extends TextAction
    {
        public OpenStatementAction()
        {
            super("Open");
            putValue(Action.SHORT_DESCRIPTION, "Opens a saved statement");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            open();
        }
    }
    public class SaveStatementAction extends TextAction
    {
        public SaveStatementAction()
        {
            super("Save");
            putValue(Action.SHORT_DESCRIPTION, "Saves the statement and plugins data. Plugins are mail etc.");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            save();
        }
    }
    public class SaveAsStatementAction extends TextAction
    {
        public SaveAsStatementAction()
        {
            super("SaveAs");
            putValue(Action.SHORT_DESCRIPTION, "Saves the statement and plugins data with another name. Plugins are mail etc.");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            saveAs();
        }
    }
    public class RemoveStatementAction extends TextAction
    {
        public RemoveStatementAction()
        {
            super("Remove");
            putValue(Action.SHORT_DESCRIPTION, "Removes the current statement and all it's plugin data from storage. Plugins are mail etc.");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            remove();
        }
    }
}
