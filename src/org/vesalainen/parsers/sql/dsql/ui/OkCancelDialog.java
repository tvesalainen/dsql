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
public class OkCancelDialog extends CancelDialog
{
    protected JButton okButton;
    
    public OkCancelDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc)
    {
        super(owner, title, modalityType, gc);
        init();
    }

    public OkCancelDialog(Window owner, String title, ModalityType modalityType)
    {
        super(owner, title, modalityType);
        init();
    }

    public OkCancelDialog(Window owner, String title)
    {
        super(owner, title);
        init();
    }

    public OkCancelDialog(Window owner, ModalityType modalityType)
    {
        super(owner, modalityType);
        init();
    }

    public OkCancelDialog(Window owner)
    {
        super(owner);
        init();
    }

    public OkCancelDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc)
    {
        super(owner, title, modal, gc);
        init();
    }

    public OkCancelDialog(Dialog owner, String title, boolean modal)
    {
        super(owner, title, modal);
        init();
    }

    public OkCancelDialog(Dialog owner, String title)
    {
        super(owner, title);
        init();
    }

    public OkCancelDialog(Dialog owner, boolean modal)
    {
        super(owner, modal);
        init();
    }

    public OkCancelDialog(Dialog owner)
    {
        super(owner);
        init();
    }

    public OkCancelDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc)
    {
        super(owner, title, modal, gc);
        init();
    }

    public OkCancelDialog(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        init();
    }

    public OkCancelDialog(Frame owner, String title)
    {
        super(owner, title);
        init();
    }

    public OkCancelDialog(Frame owner, boolean modal)
    {
        super(owner, modal);
        init();
    }

    public OkCancelDialog(Frame owner)
    {
        super(owner);
        init();
    }

    public OkCancelDialog()
    {
        init();
    }

    private void init()
    {
        // buttons
        okButton = new JButton(I18n.get("OK"));
        ActionListener okAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                accepted = true;
                setVisible(false);
            }
        };
        okButton.addActionListener(okAction);
        buttonPanel.add(okButton);
        
        getRootPane().setDefaultButton(okButton);
    }
    
}
