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
import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 * @author Timo Vesalainen
 */
public abstract class AbstractAutoAction extends AbstractAction implements AutoAction 
{
    protected static final String AUTO = "AUTO";
    protected boolean auto;
    
    public AbstractAutoAction()
    {
    }

    public AbstractAutoAction(String name)
    {
        super(name);
    }

    public AbstractAutoAction(String name, Icon icon)
    {
        super(name, icon);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        if (enabled && auto)
        {
            actionPerformed(new ActionEvent(this, 0, AUTO));
            auto = false;
        }
    }

    @Override
    public boolean isAuto()
    {
        return auto;
    }

    @Override
    public void setAuto(boolean auto)
    {
        this.auto = auto;
    }

}
