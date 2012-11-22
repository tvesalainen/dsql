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
import javax.swing.JFrame;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.vesalainen.parsers.sql.dsql.Statistics;
import org.vesalainen.parsers.sql.dsql.ui.MetadataHandler;
import org.vesalainen.parsers.sql.dsql.ui.MetadataTreeDialog;

/**
 * @author Timo Vesalainen
 */
public class MetadataTreeAction extends TextAction
{
    private static MetadataTreeDialog tree;
    
    private Statistics statistics;
    private MetadataHandler handler;
    
    public MetadataTreeAction(String name, Statistics statistics, MetadataHandler handler)
    {
        super(name);
        this.statistics = statistics;
        this.handler = handler;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JTextComponent focusedComponent = getFocusedComponent();
        if (tree == null)
        {
            tree = new MetadataTreeDialog(statistics);
        }
        tree.setLocationRelativeTo(focusedComponent);
        tree.doIt(handler);
    }

}
