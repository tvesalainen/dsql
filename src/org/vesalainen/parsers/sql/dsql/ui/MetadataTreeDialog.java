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

import java.awt.Dialog;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;
import org.vesalainen.parsers.sql.dsql.Statistics;

/**
 * @author Timo Vesalainen
 */
public class MetadataTreeDialog extends BaseDialog
{
    private Statistics statistics;
    private MetadataHandler handler;
    private final MetadataTree tree;
    public MetadataTreeDialog(Statistics statistics)
    {
        this.statistics = statistics;
        this.handler = handler;
        tree = new MetadataTree(statistics);
        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
        tree.addKeyListener(this);
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        pack();
    }

    public void doIt(MetadataHandler handler)
    {
        this.handler = handler;
        setVisible(true);
        this.handler = null;
    }
    
    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_ENTER:
                TreePath[] paths = tree.getSelectionPaths();
                handler.selected(statistics, paths);
                setVisible(false);
                break;
        }
        super.keyPressed(e);
    }

}
