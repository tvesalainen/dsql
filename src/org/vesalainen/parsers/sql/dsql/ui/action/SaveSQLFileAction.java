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

package org.vesalainen.parsers.sql.dsql.ui.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.vesalainen.parsers.sql.dsql.ui.BytesDialog;
import org.vesalainen.parsers.sql.dsql.ui.I18n;

/**
 * @author Timo Vesalainen
 */
public class SaveSQLFileAction extends TextAction
{
    private static File currentDirectory;

    public SaveSQLFileAction()
    {
        super(I18n.get("SAVE FILE"));
        putValue(Action.SHORT_DESCRIPTION, I18n.get("SAVE FILE TOOLTIP"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
         JTextComponent text = getTextComponent(e);
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter ff = new FileNameExtensionFilter(I18n.get("SQL FILE"), "sql");
        fc.setFileFilter(ff);
        if (currentDirectory != null)
        {
            fc.setCurrentDirectory(currentDirectory);
        }
        fc.setSelectedFile(new File("file.sql"));
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            if (file != null)
            {
                if (file.getName().indexOf('.') == -1)
                {
                    file = new File(file.getPath() + ".sql");
                }
                currentDirectory = file.getParentFile();
                if (file.exists())
                {
                    int confirm = JOptionPane.showConfirmDialog(null, file, I18n.get("FILE EXISTS! OVERWRITE?"), JOptionPane.OK_CANCEL_OPTION);
                    if (JOptionPane.YES_OPTION == confirm)
                    {
                        return;
                    }
                }
                try (FileWriter fos = new FileWriter(file))
                {
                    fos.write(text.getText());
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
                }
            }
        }
   }

}
