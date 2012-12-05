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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.TableCellEditor;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.UpdateableFetchResult;
import org.vesalainen.parsers.sql.dsql.ui.DSJTable;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;
import org.vesalainen.parsers.sql.dsql.ui.I18n;

/**
 * @author Timo Vesalainen
 */
public class FetchResultHandler implements PropertyChangeListener
{
    public static final String TablePropertyName = "fetchResultTable";
    public static final String ModelPropertyName = "fetchResultModel";
    
    protected SwingPropertyChangeSupport changeSupport;
    
    private JFrame frame;
    private JScrollPane scrollPane;
    private FetchResultTableModel tableModel;
    private DSJTable table;
    private DeleteRowAction deleteRowAction;
    private CommitAction commitAction;
    private RollbackAction rollbackAction;
    private FetchResult fetchResult;
    
    public FetchResultHandler(JFrame frame, JScrollPane scrollPane)
    {
        this.frame = frame;
        this.scrollPane = scrollPane;
        deleteRowAction = new DeleteRowAction();
        commitAction = new CommitAction();
        rollbackAction = new RollbackAction();
        changeSupport = new SwingPropertyChangeSupport(this);
    }

    public DeleteRowAction getDeleteRowAction()
    {
        return deleteRowAction;
    }

    public CommitAction getCommitAction()
    {
        return commitAction;
    }

    public RollbackAction getRollbackAction()
    {
        return rollbackAction;
    }

    private void setButtonsEnabled(boolean enable)
    {
        deleteRowAction.setEnabled(enable);
        commitAction.setEnabled(enable);
        rollbackAction.setEnabled(enable);
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (ExecuteAction.PropertyName.equals(evt.getPropertyName()))
        {
            FetchResult result = (FetchResult) evt.getNewValue();
            if (result != null)
            {
                if (result instanceof UpdateableFetchResult)
                {
                    setButtonsEnabled(true);
                }
                fetchResult = (FetchResult) result;
                if (tableModel == null)
                {
                    tableModel = new FetchResultTableModel(fetchResult);
                    table = new DSJTable(tableModel);
                    table.setOwner(frame);
                    scrollPane.setViewportView(table);
                    firePropertyChange(TablePropertyName, null, table);
                }
                else
                {
                    tableModel.updateData(fetchResult);
                }
                firePropertyChange(ModelPropertyName, null, tableModel);
            }
            else
            {
                if (tableModel != null)
                {
                    tableModel.clear();
                }
                if (    fetchResult != null &&
                        (fetchResult instanceof UpdateableFetchResult)
                        )
                {
                    UpdateableFetchResult ufr = (UpdateableFetchResult) fetchResult;
                    ufr.rollback();
                }
                firePropertyChange(ModelPropertyName, null, null);
                setButtonsEnabled(false);
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    public class DeleteRowAction extends AbstractAction 
    {

        public DeleteRowAction()
        {
            super(I18n.get("DELETE ROW"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("DELETE ROW TOOLTIP"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int rowNum = table.getSelectedRow();
            while (rowNum != -1)
            {
                tableModel.deleteRow(rowNum);
                rowNum = table.getSelectedRow();
            }
        }
        
    }
    public class CommitAction extends AbstractAction 
    {

        public CommitAction()
        {
            super(I18n.get("COMMIT"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("COMMIT TOOLTIP"));
            setEnabled(false);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            TableCellEditor cellEditor = table.getCellEditor();
            if (cellEditor != null)
            {
                cellEditor.stopCellEditing();
            }
            UpdateableFetchResult updateableFetchResult = (UpdateableFetchResult) fetchResult;
            updateableFetchResult.updateAndCommit();
            setButtonsEnabled(false);
        }
    }
    public class RollbackAction extends AbstractAction 
    {

        public RollbackAction()
        {
            super(I18n.get("ROLLBACK"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("ROLLBACK TOOLTIP"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            UpdateableFetchResult updateableFetchResult = (UpdateableFetchResult) fetchResult;
            updateableFetchResult.rollback();
            setButtonsEnabled(false);
        }
        
    }
}
