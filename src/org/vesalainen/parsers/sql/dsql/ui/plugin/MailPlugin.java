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

package org.vesalainen.parsers.sql.dsql.ui.plugin;

import com.google.appengine.api.datastore.Email;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * @author Timo Vesalainen
 */
public class MailPlugin extends AbstractMessagePlugin
{

    public MailPlugin()
    {
        super("Mail", new SendAction());
    }

    @Override
    public boolean accept(Class<?> type)
    {
        return Email.class.equals(type);
    }

    public static class SendAction extends AbstractAction
    {

        public SendAction()
        {
            super("Send");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
