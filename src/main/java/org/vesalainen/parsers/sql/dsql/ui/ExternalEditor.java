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
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JTextArea;

/**
 * @author Timo Vesalainen
 */
public class ExternalEditor extends OkCancelDialog implements WindowFocusListener
{
    private Path path;
    private File file;
    private long lastModified;
    private JTextArea label;

    public ExternalEditor(Window owner, Path path)
    {
        super(owner);
        this.path = path;
        file = path.toFile();
        lastModified = file.lastModified();
        init();
    }

    @Override
    public boolean input()
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/C", path.toString());
            pb.start();
            
            okButton.setEnabled(false);
            boolean result = super.input();
            return result;
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void setLabel(String text)
    {
        label.setText(text);
    }
    
    private void init()
    {
        label = new JTextArea();
        label.setOpaque(true);
        label.setLineWrap(true);
        label.setEditable(false);
        label.setRows(3);
        add(label, BorderLayout.NORTH);
        label.setText(I18n.get("TRYING TO OPEN BLOB CONTENT IN EXTERNAL APPLICATION."));
        
        okButton.setText(I18n.get("SAVE"));
        okButton.setToolTipText(I18n.get("SAVES THE EXTERNALLY MODIFIED CONTENTS TO PROPERTY"));
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        addWindowFocusListener(this);
    }

    @Override
    public void windowGainedFocus(WindowEvent e)
    {
        if (lastModified < file.lastModified())
        {
            label.setText(
                    I18n.get("TRYING TO OPEN BLOB CONTENT IN EXTERNAL APPLICATION. ")+
                    I18n.get("PRESS SAVE TO UPDATE MODIFIED CONTENT TO DATASTORE")
                    );
            okButton.setEnabled(true);
        }
    }

    @Override
    public void windowLostFocus(WindowEvent e)
    {
    }

}
