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

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextPane;
import javax.swing.tree.TreePath;
import org.vesalainen.parsers.sql.ColumnMetadata;
import org.vesalainen.parsers.sql.TableMetadata;
import org.vesalainen.parsers.sql.dsql.Statistics;
import org.vesalainen.parsers.sql.dsql.ui.MetadataHandler;

/**
 * @author Timo Vesalainen
 */
public class GenerateSelectHandler implements MetadataHandler
{
    private JTextPane text;

    public GenerateSelectHandler(JTextPane text)
    {
        this.text = text;
    }
    
    @Override
    public void selected(Statistics statistics, TreePath[] paths)
    {
        if (paths != null)
        {
            String kind = null;
            List<String> properties = new ArrayList<>();
            for (TreePath path : paths)
            {
                Object[] pathOb = path.getPath();
                switch (pathOb.length)
                {
                    case 2:
                        kind = pathOb[1].toString();
                        break;
                    case 3:
                        kind = pathOb[1].toString();
                        properties.add(pathOb[2].toString());
                        break;
                }
            }
            if (properties.isEmpty())
            {
                TableMetadata tm = statistics.getKind(kind);
                for (ColumnMetadata cm : tm.getColumns())
                {
                    properties.add(cm.getName());
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("select\n");
            boolean f = true;
            for (String p : properties)
            {
                if (!f)
                {
                    sb.append(",\n");
                }
                f = false;
                sb.append("  "+kind+"."+p);
            }
            sb.append("\nfrom\n  "+kind+"\n;\n");
            text.replaceSelection(sb.toString());
        }
    }
    
}
