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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
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

    protected MagicResult magic;
    private JTextArea label;
    private Input input;
    private JComboBox combobox;

    public BytesDialog(Frame owner)
    {
        super(owner);
        setTitle("Blob Editor");
        init();
    }

    public Input getInput()
    {
        return input;
    }
    
    private void init()
    {
        label = new JTextArea();
        label.setOpaque(true);
        label.setLineWrap(true);
        label.setEditable(false);
        label.setRows(3);
        add(label, BorderLayout.NORTH);
        
        combobox = new JComboBox();
        add(combobox, BorderLayout.CENTER);
        combobox.setEditable(true);
        
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
        
        if (openSupported())
        {
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
        }
        
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

    public String getExtension()
    {
        return (String) combobox.getSelectedItem();
    }
    
    public boolean input(MagicResult magic)
    {
        this.magic = magic;
        if (magic == null || magic.getExtensions().length == 0)
        {
            label.setText(
                    "Blob propertys value type is "+
                    "unknown "+
                    ". Enter file extension if known.");
        }
        else
        {
            String[] extensions = magic.getExtensions();
            if (extensions.length == 1)
            {
                label.setText(
                        "Blob propertys value type is "+
                        magic.getDescription()+
                        ".");
            }
            else
            {
                label.setText(
                        "Blob propertys value type is "+
                        magic.getDescription()+
                        ".Choose the extension before trying to open.");
            }
        }

        combobox.removeAllItems();
        if (magic != null)
        {
            for (String ext : magic.getExtensions())
            {
                combobox.addItem(ext);
            }
        }
        if (combobox.getItemCount() > 0)
        {
            combobox.setSelectedIndex(0);
        }
        input = Input.CANCEL;
        return super.input();
    }

    private boolean openSupported()
    {
        return System.getProperty("os.name", "").toLowerCase().startsWith("windows");
    }
}
