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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;

/**
 * @author Timo Vesalainen
 */
public class BaseDialog extends JDialog  implements MouseListener, KeyListener
{
    protected boolean accepted;
    protected JPanel buttonPanel;
    protected JButton cancelButton;
    protected JMenuBar menuBar;

    public BaseDialog()
    {
        init();
    }

    public BaseDialog(Frame owner)
    {
        super(owner);
        init();
    }

    public BaseDialog(Frame owner, boolean modal)
    {
        super(owner, modal);
        init();
    }

    public BaseDialog(Frame owner, String title)
    {
        super(owner, title);
        init();
    }

    public BaseDialog(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        init();
    }

    public BaseDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc)
    {
        super(owner, title, modal, gc);
        init();
    }

    public BaseDialog(Dialog owner)
    {
        super(owner);
        init();
    }

    public BaseDialog(Dialog owner, boolean modal)
    {
        super(owner, modal);
        init();
    }

    public BaseDialog(Dialog owner, String title)
    {
        super(owner, title);
        init();
    }

    public BaseDialog(Dialog owner, String title, boolean modal)
    {
        super(owner, title, modal);
        init();
    }

    public BaseDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc)
    {
        super(owner, title, modal, gc);
        init();
    }

    public BaseDialog(Window owner)
    {
        super(owner);
        init();
    }

    public BaseDialog(Window owner, ModalityType modalityType)
    {
        super(owner, modalityType);
        init();
    }

    public BaseDialog(Window owner, String title)
    {
        super(owner, title);
        init();
    }

    public BaseDialog(Window owner, String title, ModalityType modalityType)
    {
        super(owner, title, modalityType);
        init();
    }

    public BaseDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc)
    {
        super(owner, title, modalityType, gc);
        init();
    }

    private void init()
    {
        setIconImages(WorkBench.icons);
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        add(buttonPanel, BorderLayout.SOUTH);
    }
    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_ESCAPE:
                setVisible(false);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

}
