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
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextArea;
import org.vesalainen.parsers.sql.dsql.ui.CancelDialog;
import org.vesalainen.parsers.sql.dsql.ui.I18n;

/**
 * @author Timo Vesalainen
 */
public class AboutAction extends AbstractAction
{
    private AboutDialog dialog;
    public AboutAction()
    {
        super(I18n.get("ABOUT"));
        putValue(Action.SHORT_DESCRIPTION, I18n.get("ABOUT DATASTORE STRUCTURED QUERY LANGUAGE"));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (dialog == null)
        {
            dialog = new AboutDialog();
        }
        dialog.input();
    }

    private class AboutDialog extends CancelDialog
    {

        public AboutDialog()
        {
            cancelButton.setText(I18n.get("CLOSE"));
            
            JTextArea label = new JTextArea();
            label.setText(
                    "\n"+
                    "Copyright (C) 2012 Timo Vesalainen\n"+
                    "\n"+
                    "This program is free software: you can redistribute it and/or modify\n"+
                    "it under the terms of the GNU General Public License as published by\n"+
                    "the Free Software Foundation, either version 3 of the License, or\n"+
                    "(at your option) any later version.\n"+
                    "\n"+
                    "This program is distributed in the hope that it will be useful,\n"+
                    "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
                    "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
                    "GNU General Public License for more details.\n"+
                    "\n"+
                    "You should have received a copy of the GNU General Public License\n"+
                    " along with this program.  If not, see <http://www.gnu.org/licenses/>.\n"+
                    "\n"
                   );
            label.setOpaque(true);
            label.setEditable(false);
            add(label, BorderLayout.NORTH);
        }
        
    }
}
