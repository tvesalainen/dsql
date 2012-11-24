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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * @author Timo Vesalainen
 */
public class CancelDialog extends JDialog implements MouseListener
{
    protected boolean accepted;
    protected JPanel buttonPanel;
    protected JButton cancelButton;

    public CancelDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc)
    {
        super(owner, title, modalityType, gc);
        init();
    }

    public CancelDialog(Window owner, String title, ModalityType modalityType)
    {
        super(owner, title, modalityType);
        init();
    }

    public CancelDialog(Window owner, String title)
    {
        super(owner, title);
        init();
    }

    public CancelDialog(Window owner, ModalityType modalityType)
    {
        super(owner, modalityType);
        init();
    }

    public CancelDialog(Window owner)
    {
        super(owner);
        init();
    }

    public CancelDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc)
    {
        super(owner, title, modal, gc);
        init();
    }

    public CancelDialog(Dialog owner, String title, boolean modal)
    {
        super(owner, title, modal);
        init();
    }

    public CancelDialog(Dialog owner, String title)
    {
        super(owner, title);
        init();
    }

    public CancelDialog(Dialog owner, boolean modal)
    {
        super(owner, modal);
        init();
    }

    public CancelDialog(Dialog owner)
    {
        super(owner);
        init();
    }

    public CancelDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc)
    {
        super(owner, title, modal, gc);
        init();
    }

    public CancelDialog(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        init();
    }

    public CancelDialog(Frame owner, String title)
    {
        super(owner, title);
        init();
    }

    public CancelDialog(Frame owner, boolean modal)
    {
        super(owner, modal);
        init();
    }

    public CancelDialog(Frame owner)
    {
        super(owner);
        init();
    }

    public CancelDialog()
    {
        init();
    }

    protected void init()
    {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        add(buttonPanel, BorderLayout.SOUTH);
        // buttons
        cancelButton = new JButton("Cancel");
        ActionListener cancelAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        };
        cancelButton.addActionListener(cancelAction);
        buttonPanel.add(cancelButton);
        setLocationByPlatform(true);
        getRootPane().setDefaultButton(cancelButton);
    }
    
    public boolean input()
    {
        pack();
        accepted = false;
        setVisible(true);
        return accepted;
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

}
