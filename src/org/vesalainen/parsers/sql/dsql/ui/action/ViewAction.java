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
import org.vesalainen.parsers.sql.dsql.ui.ViewDialog;

/**
 * @author Timo Vesalainen
 */
public class ViewAction extends AbstractAutoAction implements PropertyChangeListener
{
    private FetchResultTableModel model;
    private ViewDialog dialog;

    public ViewAction()
    {
        super(I18n.get("VIEW"));
        putValue(Action.SHORT_DESCRIPTION, I18n.get("VIEW THE RESULTS"));
        setEnabled(false);
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        assert model != null;
        if (dialog == null)
        {
            dialog = new ViewDialog(model);
        }
        else
        {
            dialog.refresh(model);
        }
        dialog.input();
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
    
}
