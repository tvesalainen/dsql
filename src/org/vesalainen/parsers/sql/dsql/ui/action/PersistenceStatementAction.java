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
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.text.TextAction;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.dsql.ui.StatementListDialog;
import org.vesalainen.parsers.sql.dsql.ui.WorkBench;

/**
 * @author Timo Vesalainen
 */
public abstract class PersistenceStatementAction extends TextAction
{
    protected WorkBench workBench;
    protected String storedStatementsKind;
    private FetchResult statements;
    private static StatementListDialog dialog;

    PersistenceStatementAction(String name, WorkBench workBench, String storedStatementsKind)
    {
        super(name);
        this.workBench = workBench;
        this.storedStatementsKind = storedStatementsKind;
    }
    
    protected boolean confirmInstalled()
    {
        if (statements == null)
        {
            statements = workBench.getEngine().execute("select key, sql from "+storedStatementsKind);
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
    protected String chooseStatement()
    {
        if (dialog == null)
        {
            dialog = new StatementListDialog(workBench);
        }
        if (dialog.input())
        {
            return dialog.getSelected();
        }
        return null;
    }
    public void refresh()
    {
        if (dialog == null)
        {
            dialog = new StatementListDialog(workBench);
        }
        else
        {
            dialog.refresh();
        }
    }
    
}
