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

import javax.swing.JTextPane;
import javax.swing.tree.TreePath;
import org.vesalainen.parsers.sql.dsql.Statistics;

/**
 * @author Timo Vesalainen
 */
public class InsertPropertiesHandler implements MetadataHandler
{
    private JTextPane text;

    public InsertPropertiesHandler(JTextPane text)
    {
        this.text = text;
    }
    
    @Override
    public void selected(Statistics statistics, TreePath[] paths)
    {
        StringBuilder sb = new StringBuilder();
        for (TreePath path : paths)
        {
            Object[] pathOb = path.getPath();
            switch (pathOb.length)
            {
                case 2:
                    if (sb.length() != 0)
                    {
                        sb.append(", ");
                    }
                    sb.append(pathOb[1].toString());
                    break;
                case 3:
                    if (sb.length() != 0)
                    {
                        sb.append(", ");
                    }
                    sb.append(pathOb[1].toString()+"."+pathOb[2].toString());
                    break;
            }
        }
        text.replaceSelection(sb.toString());
    }
    
}
