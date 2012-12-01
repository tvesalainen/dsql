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

import java.awt.Component;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.vesalainen.parsers.sql.ColumnMetadata;
import org.vesalainen.parsers.sql.TableMetadata;
import org.vesalainen.parsers.sql.dsql.Statistics;

/**
 * @author Timo Vesalainen
 */
public class MetadataTree extends JTree
{

    public MetadataTree(Statistics statistics)
    {
        super(new MetadataTreeModel(statistics));
        setRootVisible(false);
        setCellRenderer(new MetadataRenderer());
    }

    private class MetadataRenderer extends DefaultTreeCellRenderer
    {
        ImageIcon tableImage = new ImageIcon(WorkBench.class.getResource("images/table.png"));
        ImageIcon greenImage = new ImageIcon(WorkBench.class.getResource("images/greenbox.png"));
        ImageIcon blackImage = new ImageIcon(WorkBench.class.getResource("images/blackbox.png"));

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();
                if (userObject instanceof TableMetadata)
                {
                    setIcon(tableImage);
                    setToolTipText(I18n.get("KIND TOOLTIP"));
                }
                else
                {
                    if (userObject instanceof ColumnMetadata)
                    {
                        ColumnMetadata cm = (ColumnMetadata) userObject;
                        if (cm.isIndexed())
                        {
                            setIcon(greenImage);
                            setToolTipText(I18n.get("INDEXED PROPERTY TOOLTIP"));
                        }
                        else
                        {
                            setIcon(blackImage);
                            setToolTipText(I18n.get("PROPERTY TOOLTIP"));
                        }
                    }
                }
            }
            else
            {
                setToolTipText(null);
            }
            return this;
        }
        
        
    }
}
