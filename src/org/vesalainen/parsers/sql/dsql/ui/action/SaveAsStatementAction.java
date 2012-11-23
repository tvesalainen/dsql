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
import com.google.appengine.api.datastore.Text;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.Statement;
import org.vesalainen.parsers.sql.dsql.ui.WorkBench;

/**
 * @author Timo Vesalainen
 */
public class SaveAsStatementAction extends PersistenceStatementAction
{

    public SaveAsStatementAction(String name, WorkBench workBench, String storedStatementsKind)
    {
        super(name, workBench, storedStatementsKind);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (confirmInstalled())
        {
            String name = JOptionPane.showInputDialog(workBench.getFrame(), "Enter name for statement", "");
            if (name != null)
            {
                name = name.replace("'", "");
                FetchResult result = workBench.getEngine().execute(
                                            "select "+Entity.KEY_RESERVED_PROPERTY+" from "+
                                            storedStatementsKind+
                                            " where key = key("+storedStatementsKind+"( '"+name+"' ))"
                                            );
                if (result.getRowCount() > 0)
                {
                    int confirm = JOptionPane.showConfirmDialog(
                            workBench.getFrame(), 
                            name, 
                            "Exist Already! Overwrite?",
                            JOptionPane.OK_CANCEL_OPTION
                            );
                    if (JOptionPane.YES_OPTION != confirm)
                    {
                        return;
                    }
                }
                Statement insert = workBench.getEngine().prepare(
                                            "insert into "+
                                            storedStatementsKind+
                                            " ( key, sql ) values ( key("+storedStatementsKind+"( '"+name+"' )), :sql text )"
                                            );
                insert.bindValue("sql", new Text(workBench.getActiveTextPane().getText()));
                insert.execute();
                workBench.setOpenStatement(name);
                refresh();
            }
        }
    }

}
