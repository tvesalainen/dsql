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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.vesalainen.parsers.magic.Magic;
import org.vesalainen.parsers.magic.Magic.MagicResult;

/**
 * @author Timo Vesalainen
 */
public class BytesDialog extends CancelDialog
{
    private static final Magic magic = Magic.getInstance();

    protected JButton loadButton;
    protected JButton storeButton;
    protected JButton openButton;
    protected JButton removeButton;

    protected MagicResult guess;
    private JTextArea label;
    private JComboBox combobox;
    private static File currentDirectory;
    protected byte[] bytes;

    public BytesDialog(Window owner)
    {
        super(owner);
        setTitle(I18n.get("BLOB EDITOR"));
        init();
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
        
        loadButton = new JButton(I18n.get("LOAD"));
        loadButton.setToolTipText(I18n.get("LOAD BINARY PROPERTIES VALUE FROM A FILE TOOLTIP"));
        ActionListener loadAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (guess != null && guess.getExtensions().length == 0)
                {
                    String[] extensions = guess.getExtensions();
                    FileFilter ff = new FileNameExtensionFilter(guess.getDescription(), guess.getExtensions());
                    fc.setFileFilter(ff);
                }
                else
                {
                    String extension = (String) combobox.getSelectedItem();
                    if (extension != null)
                    {
                        FileFilter ff = new FileNameExtensionFilter("", extension);
                        fc.setFileFilter(ff);
                    }
                }
                if (currentDirectory != null)
                {
                    fc.setCurrentDirectory(currentDirectory);
                }
                if (fc.showOpenDialog(BytesDialog.this) == JFileChooser.APPROVE_OPTION)
                {
                    File file = fc.getSelectedFile();
                    long length = file.length();
                    if (length > 1000000)
                    {
                        JOptionPane.showMessageDialog(BytesDialog.this, file, I18n.get("FILE IS TOO BIG"), JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        bytes = new byte[(int)length];
                        try (FileInputStream fis = new FileInputStream(file))
                        {
                            fis.read(bytes);
                            currentDirectory = file.getParentFile();
                        }
                        catch (IOException ex)
                        {
                            JOptionPane.showMessageDialog(BytesDialog.this, ex.getLocalizedMessage());
                        }
                    }
                }
                accepted = true;
                setVisible(false);
            }
        };
        loadButton.addActionListener(loadAction);
        buttonPanel.add(loadButton);
        
        storeButton = new JButton(I18n.get("STORE"));
        storeButton.setToolTipText(I18n.get("STORE PROPERTIES VALUE TO A FILE"));
        ActionListener storeAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                String extension = getExtension();
                if (extension != null && !extension.isEmpty())
                {
                    FileFilter ff = new FileNameExtensionFilter(guess.getDescription(), extension);
                    fc.setFileFilter(ff);
                }
                if (currentDirectory != null)
                {
                    fc.setCurrentDirectory(currentDirectory);
                }
                String suffix = "";
                if (extension != null && !extension.isEmpty())
                {
                    suffix = "."+extension.toLowerCase();
                }
                fc.setSelectedFile(new File("file"+suffix));
                if (fc.showSaveDialog(BytesDialog.this) == JFileChooser.APPROVE_OPTION)
                {
                    File file = fc.getSelectedFile();
                    if (file != null)
                    {
                        if (file.getName().indexOf('.') == -1)
                        {
                            file = new File(file.getPath() + suffix);
                        }
                        currentDirectory = file.getParentFile();
                        if (file.exists())
                        {
                            int confirm = JOptionPane.showConfirmDialog(BytesDialog.this, file, I18n.get("FILE EXISTS! OVERWRITE?"), JOptionPane.OK_CANCEL_OPTION);
                            if (JOptionPane.YES_OPTION == confirm)
                            {
                                return;
                            }
                        }
                        try (FileOutputStream fos = new FileOutputStream(file))
                        {
                            fos.write(bytes);
                        }
                        catch (IOException ex)
                        {
                            JOptionPane.showMessageDialog(BytesDialog.this, ex.getLocalizedMessage());
                        }
                    }
                }
                accepted = true;
                setVisible(false);
            }
        };
        storeButton.addActionListener(storeAction);
        buttonPanel.add(storeButton);
        
        if (openSupported())
        {
            openButton = new JButton(I18n.get("OPEN"));
            openButton.setToolTipText(I18n.get("BYTESDIALOG OPEN TOOLTIP"));
            ActionListener openAction = new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    String extension = getExtension();
                    String suffix = "";
                    if (extension != null && !extension.isEmpty())
                    {
                        suffix = "."+extension.toLowerCase();
                    }
                    try
                    {
                        File file = File.createTempFile("tmp", suffix);
                        Path tempPath = file.toPath();
                        try (FileOutputStream fos = new FileOutputStream(file))
                        {
                            fos.write(bytes);
                        }
                        catch (IOException ex)
                        {
                            JOptionPane.showMessageDialog(BytesDialog.this, ex.getLocalizedMessage());
                        }
                        ExternalEditor ee = new ExternalEditor(BytesDialog.this, tempPath);
                        if (ee.input())
                        {
                            long length = file.length();
                            if (length > 1000000)
                            {
                                JOptionPane.showMessageDialog(BytesDialog.this, file, I18n.get("FILE IS TOO BIG"), JOptionPane.ERROR_MESSAGE);
                            }
                            else
                            {
                                bytes = new byte[(int)length];
                                try (FileInputStream fis = new FileInputStream(file))
                                {
                                    fis.read(bytes);
                                }
                                catch (IOException ex)
                                {
                                    JOptionPane.showMessageDialog(BytesDialog.this, ex.getLocalizedMessage());
                                }
                            }
                        }
                        try
                        {
                            Files.delete(tempPath);
                        }
                        catch (IOException ex)
                        {
                        }
                    }
                    catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(BytesDialog.this, ex.getLocalizedMessage());
                    }
                    accepted = true;
                    setVisible(false);
                }
            };
            openButton.addActionListener(openAction);
            buttonPanel.add(openButton);
        }
        
        removeButton = new JButton(I18n.get("REMOVE"));
        removeButton.setToolTipText(I18n.get("REMOVE THE PROPERTIES CONTENT."));
        ActionListener removeAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int confirm = JOptionPane.showConfirmDialog(BytesDialog.this, I18n.get("REMOVING BLOB"), I18n.get("CONTINUE?"), JOptionPane.OK_CANCEL_OPTION);
                if (JOptionPane.YES_OPTION == confirm)
                {
                    bytes = null;
                }
                accepted = true;
                setVisible(false);
            }
        };
        removeButton.addActionListener(removeAction);
        buttonPanel.add(removeButton);
        
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    }

    public String getExtension()
    {
        return (String) combobox.getSelectedItem();
    }

    public byte[] getBytes()
    {
        return bytes;
    }

    public void setBytes(byte[] bytes)
    {
        this.bytes = bytes;
        try
        {
            guess = magic.guess(bytes);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public String getContentDescription()
    {
        if (guess != null)
        {
            return guess.getDescription();
        }
        else
        {
            return I18n.get("UNKNOWN");
        }
    }
    
    public boolean input()
    {
        if (guess == null || guess.getExtensions().length == 0)
        {
            label.setText(I18n.get("BLOB PROPERTYS VALUE TYPE IS UNKNOWN. ENTER FILE EXTENSION IF KNOWN."));
        }
        else
        {
            String[] extensions = guess.getExtensions();
            if (extensions.length == 1)
            {
                label.setText(
                        String.format(
                        I18n.get("BLOB PROPERTYS VALUE TYPE IS "), guess.getDescription()));
            }
            else
            {
                label.setText(
                        String.format(
                        I18n.get("BLOB PROPERTYS VALUE TYPE IS "), guess.getDescription())+
                        I18n.get("CHOOSE THE EXTENSION BEFORE TRYING TO OPEN."));
            }
        }

        combobox.removeAllItems();
        if (guess != null)
        {
            for (String ext : guess.getExtensions())
            {
                combobox.addItem(ext);
            }
        }
        if (combobox.getItemCount() > 0)
        {
            combobox.setSelectedIndex(0);
        }
        return super.input();
    }

    private boolean openSupported()
    {
        // TODO Linux
        return System.getProperty("os.name", "").toLowerCase().startsWith("windows");
    }
}
