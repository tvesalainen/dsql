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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.vesalainen.parsers.magic.Magic.MagicResult;

/**
 * @author Timo Vesalainen
 */
public class BytesDialog extends CancelDialog
{
    public enum Input {CANCEL, LOAD, STORE, OPEN, REMOVE};
    
    protected JButton loadButton;
    protected JButton storeButton;
    protected JButton openButton;
    protected JButton removeButton;

    protected byte[] bytes;
    protected MagicResult magic;
    protected String title;
    protected int maxLength;
    private JLabel label;
    private Input input;

    public BytesDialog()
    {
        setTitle("Blob Editor");
    }

    public void set(byte[] bytes, MagicResult magic, String title, int maxLength)
    {
        this.bytes = bytes;
        this.magic = magic;
        this.title = title;
        this.maxLength = maxLength;
        label.setText(magic.getDescription());
    }

    public byte[] getBytes()
    {
        return bytes;
    }

    public Input getInput()
    {
        return input;
    }
    
    @Override
    protected void init()
    {
        super.init();
        label = new JLabel();
        add(label, BorderLayout.NORTH);
        
        loadButton = new JButton("Load");
        ActionListener loadAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                input = Input.LOAD;
                accepted = true;
                setVisible(false);
            }
        };
        loadButton.addActionListener(loadAction);
        buttonPanel.add(loadButton);
        
        storeButton = new JButton("Store");
        ActionListener storeAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                input = Input.STORE;
                accepted = true;
                setVisible(false);
            }
        };
        storeButton.addActionListener(storeAction);
        buttonPanel.add(storeButton);
        
        openButton = new JButton("Open");
        ActionListener openAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                input = Input.OPEN;
                accepted = true;
                setVisible(false);
            }
        };
        openButton.addActionListener(openAction);
        buttonPanel.add(openButton);
        
        removeButton = new JButton("Remove");
        ActionListener removeAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                input = Input.REMOVE;
                accepted = true;
                setVisible(false);
            }
        };
        removeButton.addActionListener(removeAction);
        buttonPanel.add(removeButton);
        
        setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
    }

    @Override
    public boolean input()
    {
        input = Input.CANCEL;
        return super.input();
    }

}
