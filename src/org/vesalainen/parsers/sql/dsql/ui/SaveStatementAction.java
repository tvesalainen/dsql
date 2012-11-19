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
import org.vesalainen.parsers.sql.Statement;

/**
 * @author Timo Vesalainen
 */
class SaveStatementAction extends PersistenceStatementAction
{

    public SaveStatementAction(String name, WorkBench workBench)
    {
        super(name, workBench);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String name = workBench.getOpenStatement();
        if (name != null)
        {
            Statement update = workBench.engine.prepare(
                                        "update "+
                                        workBench.storedStatementsKind+
                                        " set sql = :sql where key = "+workBench.storedStatementsKind+"( '"+name+"' )"
                                        );
            update.bindValue("sql", workBench.sqlArea.getText());
            update.execute();
        }
    }

}
