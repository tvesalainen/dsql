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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.vesalainen.parsers.sql.dsql.ui.CancelDialog;
import org.vesalainen.parsers.sql.dsql.ui.I18n;

/**
 * @author Timo Vesalainen
 */
public class DSQLHelpAction extends AbstractAction
{
    private HelpDialog dialog;
    public DSQLHelpAction()
    {
        super(I18n.get("DSQL HELP"));
        putValue(Action.SHORT_DESCRIPTION, I18n.get("DSQL SYNTAX"));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (dialog == null)
        {
            dialog = new HelpDialog();
        }
    }

    private class HelpDialog extends JDialog
    {

        public HelpDialog()
        {
            super(null, ModalityType.MODELESS);
            try
            {
                JTextPane pane = new JTextPane();
                pane.setEditable(false);
                String path = "../../doc-files/DSQLParser-batchStatement.html";
                URL url = DSQLHelpAction.class.getResource(path);
                pane.setPage(url);
                JScrollPane scrollPane = new JScrollPane(pane);
                setPreferredSize(new Dimension(450, 410));
                add(scrollPane, BorderLayout.CENTER);
                pack();
                setVisible(true);
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        
    }
}
