/*
 * Copyright (C) 2012 Helsingfors Segelklubb ry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.vesalainen.parsers.sql.dsql.ui;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import javax.swing.JOptionPane;

/**
 * @author Timo Vesalainen
 */
public class ExceptionHandler extends EventQueue
{

    @Override
    protected void dispatchEvent(AWTEvent event)
    {
        try
        {
            super.dispatchEvent(event);
        }
        catch (Throwable thr)
        {
            thr.printStackTrace();  // TODO logging
            String message = thr.getMessage();
            if (message == null || message.isEmpty())
            {
                message = "Fatal: "+thr.getClass();
            }
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
