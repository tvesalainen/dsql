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

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JFrame;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultPlugin;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;

/**
 * @author Timo Vesalainen
 */
public abstract class AbstractMessagePlugin extends FetchResultPlugin
{
    protected Action sendAction;
    protected MessageDialog dialog;
    protected JFrame owner;
    protected FetchResultTableModel model;

    public AbstractMessagePlugin(String name, Action sendAction)
    {
        super(name);
        this.sendAction = sendAction;
    }
    
    @Override
    public void handle(JFrame owner, FetchResultTableModel model)
    {
        this.owner = owner;
        this.model = model;
        setEnabled(true);
    }

    @Override
    public void disable()
    {
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (dialog == null)
        {
            dialog = createMessageDialog(owner, sendAction);
        }
        dialog.input(model);
    }

    protected MessageDialog createMessageDialog(JFrame owner, Action sendAction)
    {
        return new MessageDialog(owner, sendAction);
    }
}
