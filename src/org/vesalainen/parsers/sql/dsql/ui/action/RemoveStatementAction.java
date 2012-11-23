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
import javax.swing.JOptionPane;
import org.vesalainen.parsers.sql.Statement;
import org.vesalainen.parsers.sql.dsql.ui.WorkBench;

/**
 * @author Timo Vesalainen
 */
public class RemoveStatementAction extends PersistenceStatementAction
{

    public RemoveStatementAction(String name, WorkBench workBench, String storedStatementsKind)
    {
        super(name, workBench, storedStatementsKind);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String name = workBench.getOpenStatement();
        if (name != null)
        {
            int confirm = JOptionPane.showConfirmDialog(
                    workBench.getFrame(), 
                    name, 
                    "Confirm Remove?",
                    JOptionPane.OK_CANCEL_OPTION
                    );
            if (JOptionPane.YES_OPTION != confirm)
            {
                return;
            }
            Statement update = workBench.getEngine().prepare(
                                        "delete from "+
                                        storedStatementsKind+
                                        " where key = key("+storedStatementsKind+"( '"+name+"' ))"
                                        );
            update.execute();
            workBench.setOpenStatement(null, "");
            refresh();
        }
    }

}
