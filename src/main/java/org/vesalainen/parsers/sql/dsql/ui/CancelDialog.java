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
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 * @author Timo Vesalainen
 */
public class CancelDialog extends BaseDialog
{
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

    private void init()
    {
        // buttons
        cancelButton = new JButton(I18n.get("CANCEL"));
        cancelButton.setToolTipText(I18n.get("CANCEL THE ACTION"));
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

}
