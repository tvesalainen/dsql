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
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;
import org.vesalainen.parsers.sql.dsql.ui.I18n;

/**
 * @author Timo Vesalainen
 */
public class PrintAction extends AbstractAutoAction implements PropertyChangeListener
{
    private JTable table;

    public PrintAction()
    {
        super(I18n.get("PRINT"));
        putValue(Action.SHORT_DESCRIPTION, I18n.get("PRINT THE RESULTS"));
        setEnabled(false);
        this.table = table;
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            MessageFormat header = new MessageFormat(I18n.get("PAGE {0,NUMBER,INTEGER}"));
            table.print(JTable.PrintMode.FIT_WIDTH, header, null);
        }
        catch (PrinterException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), I18n.get("ERROR"), JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (FetchResultHandler.TablePropertyName.equals(evt.getPropertyName()))
        {
            table = (JTable) evt.getNewValue();
        }
        else
        {
            assert table != null;
            if (FetchResultHandler.ModelPropertyName.equals(evt.getPropertyName()))
            {
                FetchResultTableModel model = (FetchResultTableModel) evt.getNewValue();
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
    }
    
}
