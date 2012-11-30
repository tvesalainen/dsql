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
import java.io.IOException;
import java.nio.charset.Charset;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import org.vesalainen.parsers.sql.dsql.ui.I18n;
import org.vesalainen.parsers.sql.dsql.ui.WorkBench;

/**
 * @author Timo Vesalainen
 */
public class OpenSQLFileAction extends AbstractAction 
{
    private WorkBench workBench;
    private static File currentDirectory;
    private PersistenceHandler persistenceHandler;

    public OpenSQLFileAction(WorkBench workBench, PersistenceHandler persistenceHandler)
    {
        super(I18n.get("OPEN FILE"));
        putValue(Action.SHORT_DESCRIPTION, I18n.get("OPEN FILE TOOLTIP"));
        this.workBench = workBench;
        this.persistenceHandler = persistenceHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter ff = new FileNameExtensionFilter(I18n.get("SQL FILE"), "sql");
        fc.setFileFilter(ff);
        if (currentDirectory != null)
        {
            fc.setCurrentDirectory(currentDirectory);
        }
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            byte[] bytes = new byte[(int) file.length()];
            try (final FileInputStream fis = new FileInputStream(file))
            {
                fis.read(bytes);
                String sql = new String(bytes, Charset.forName("US-ASCII"));
                try
                {
                    persistenceHandler.fireVetoableChange(PersistenceHandler.OPEN, null, null);
                    workBench.getActiveTextPane().setText(sql);
                }
                catch (PropertyVetoException ex)
                {
                }
                currentDirectory = file.getParentFile();
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
            }
        }
    }

}
