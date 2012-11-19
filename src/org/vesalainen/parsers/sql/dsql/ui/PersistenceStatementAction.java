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
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.vesalainen.parsers.sql.FetchResult;

/**
 * @author Timo Vesalainen
 */
abstract class PersistenceStatementAction extends AbstractAction
{
    protected WorkBench workBench;
    private FetchResult statements;
    private static StatementListDialog dialog;

    PersistenceStatementAction(String name, WorkBench workBench)
    {
        super(name);
        this.workBench = workBench;
    }
    
    protected boolean confirmInstalled()
    {
        if (statements == null)
        {
            statements = workBench.engine.execute("select key, sql from "+workBench.storedStatementsKind);
        }
        if (statements.getRowCount() == 0)
        {
            int confirm = JOptionPane.showConfirmDialog(
                    workBench.frame, 
                    "Create "
                    +workBench.storedStatementsKind+
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
}
