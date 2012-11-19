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

import java.awt.event.ActionEvent;
import org.vesalainen.parsers.sql.FetchResult;

/**
 * @author Timo Vesalainen
 */
class OpenStatementAction extends PersistenceStatementAction
{

    OpenStatementAction(String name, WorkBench workBench)
    {
        super(name, workBench);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String name = chooseStatement();
        if (name != null)
        {
            FetchResult result = workBench.engine.execute(
                                        "select sql from "+
                                        workBench.storedStatementsKind+
                                        " where key = "+workBench.storedStatementsKind+"( '"+name+"' )"
                                        );
            if (result.getRowCount() > 0)
            {
                String sql = (String) result.getValueAt(0, 0);
                workBench.setOpenStatement(name, sql);
            }
        }
    }

}
