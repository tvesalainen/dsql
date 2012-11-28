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
        setTitle("Blob Editor");
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
        
        loadButton = new JButton("Load");
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
                        JOptionPane.showMessageDialog(BytesDialog.this, file, "File is too big", JOptionPane.ERROR_MESSAGE);
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
        
        storeButton = new JButton("Store");
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
                            int confirm = JOptionPane.showConfirmDialog(BytesDialog.this, file, "File exists! Overwrite?", JOptionPane.OK_CANCEL_OPTION);
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
            openButton = new JButton("Open");
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
                                JOptionPane.showMessageDialog(BytesDialog.this, file, "File is too big", JOptionPane.ERROR_MESSAGE);
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
        
        removeButton = new JButton("Remove");
        ActionListener removeAction = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int confirm = JOptionPane.showConfirmDialog(BytesDialog.this, "Removing blob", "Continue?", JOptionPane.OK_CANCEL_OPTION);
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
        guess = magic.guess(bytes);
    }

    public String getContentDescription()
    {
        if (guess != null)
        {
            return guess.getDescription();
        }
        else
        {
            return "Unknown";
        }
    }
    
    public boolean input()
    {
        if (guess == null || guess.getExtensions().length == 0)
        {
            label.setText(
                    "Blob propertys value type is "+
                    "unknown "+
                    ". Enter file extension if known.");
        }
        else
        {
            String[] extensions = guess.getExtensions();
            if (extensions.length == 1)
            {
                label.setText(
                        "Blob propertys value type is "+
                        guess.getDescription()+
                        ".");
            }
            else
            {
                label.setText(
                        "Blob propertys value type is "+
                        guess.getDescription()+
                        ".Choose the extension before trying to open.");
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
        return System.getProperty("os.name", "").toLowerCase().startsWith("windows");
    }
}
