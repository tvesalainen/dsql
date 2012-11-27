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
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

/**
 * @author Timo Vesalainen
 */
public class TextDialog extends OkCancelDialog
{
    protected JTextPane textPane;

    public TextDialog(Frame owner)
    {
        super(owner);
        init();
    }
    
    public void setText(String text)
    {
        textPane.setText(text);
    }
    
    public String getText()
    {
        return textPane.getText();
    }
    
    private void init()
    {
        textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);
        setMinimumSize(new Dimension(800, 500));
        setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);

    }

    @Override
    public boolean input()
    {
        textPane.requestFocusInWindow();
        return super.input();
    }

}
