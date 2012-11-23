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
import javax.swing.JFrame;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.SelectStatement;
import org.vesalainen.parsers.sql.Statement;

/**
 * @author Timo Vesalainen
 */
public class SelectForUpdateAction extends ExecuteAction
{

    public SelectForUpdateAction(JFrame frame)
    {
        super(frame);
        putValue(NAME, "Select for Update");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (enterPlaceHolders(statement))
        {
            SelectStatement select = (SelectStatement) statement;
            FetchResult fetchResult = select.selectForUpdate();
            firePropertyChange(PropertyName, null, fetchResult);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (DSqlParseAction.PropertyName.equals(evt.getPropertyName()))
        {
            statement = (Statement) evt.getNewValue();
            if (statement != null && (statement instanceof SelectStatement))
            {
                setEnabled(true);
            }
            else
            {
                setEnabled(false);
            }
        }
    }

}
