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

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

/**
 * @author Timo Vesalainen
 */
public class UndoableEditListenerSwitch implements UndoableEditListener
{
    private UndoableEditListener listener;
    private boolean off;

    public UndoableEditListenerSwitch(UndoableEditListener listener)
    {
        this.listener = listener;
    }

    public void setOn()
    {
        this.off = false;
    }
    
    public void setOff()
    {
        this.off = true;
    }
    
    @Override
    public void undoableEditHappened(UndoableEditEvent e)
    {
        if (!off)
        {
            listener.undoableEditHappened(e);
        }
    }

}
