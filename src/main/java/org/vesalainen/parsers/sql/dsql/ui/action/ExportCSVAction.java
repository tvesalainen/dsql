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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.vesalainen.parsers.sql.dsql.GObjectHelper;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;
import org.vesalainen.parsers.sql.dsql.ui.I18n;

/**
 * @author Timo Vesalainen
 */
public class ExportCVSAction extends AbstractAction implements PropertyChangeListener, VetoableChangeListener
{
    private FetchResultTableModel model;
    private File currentDirectory;
    private String name = "file";

    public ExportCVSAction()
    {
        super(I18n.get("EXPORT CVS"));
        putValue(Action.SHORT_DESCRIPTION, I18n.get("EXPORT CVS TOOLTIP"));
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter ff = new FileNameExtensionFilter(I18n.get("CSV FILE"), "csv");
        fc.setFileFilter(ff);
        if (currentDirectory != null)
        {
            fc.setCurrentDirectory(currentDirectory);
        }
        fc.setSelectedFile(new File(name+".csv"));
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            if (file != null)
            {
                if (file.getName().indexOf('.') == -1)
                {
                    file = new File(file.getPath() + ".csv");
                }
                currentDirectory = file.getParentFile();
                if (file.exists())
                {
                    int confirm = JOptionPane.showConfirmDialog(null, file, I18n.get("FILE EXISTS! OVERWRITE?"), JOptionPane.OK_CANCEL_OPTION);
                    if (JOptionPane.YES_OPTION != confirm)
                    {
                        return;
                    }
                }
                try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file))))
                {
                    for (int col=0;col<model.getColumnCount();col++)
                    {
                        if (col > 0)
                        {
                            pw.print(',');
                        }
                        pw.print('"');
                        pw.print(model.getColumnName(col));
                        pw.print('"');
                    }
                    pw.println();
                    for (int row=0;row<model.getRowCount();row++)
                    {
                        for (int col=0;col<model.getColumnCount();col++)
                        {
                            if (col > 0)
                            {
                                pw.print(',');
                            }
                            Object valueAt = model.getValueAt(row, col);
                            pw.print('"');
                            if (valueAt != null)
                            {
                                pw.print(GObjectHelper.getString(valueAt));
                            }
                            pw.print('"');
                        }
                        pw.println();
                    }
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (FetchResultHandler.ModelPropertyName.equals(evt.getPropertyName()))
        {
            model = (FetchResultTableModel) evt.getNewValue();
            if (model != null)
            {
                if (model.getRowCount() > 0)
                {
                    setEnabled(true);
                }
                else
                {
                    setEnabled(false);
                }
            }
            else
            {
                setEnabled(false);
            }
        }
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException
    {
        Entity entity = (Entity) evt.getNewValue();
        if (entity != null)
        {
            name = entity.getKey().getName();
        }
        else
        {
            name = "file";
        }
    }

}
