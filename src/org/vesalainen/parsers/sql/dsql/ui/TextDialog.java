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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author Timo Vesalainen
 */
public class TextDialog extends OkCancelDialog
{
    protected JTextArea textArea;
    
    public void setText(String text)
    {
        textArea.setText(text);
    }
    
    public String getText()
    {
        return textArea.getText();
    }
    
    @Override
    protected void init()
    {
        textArea = new JTextArea(30, 80);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
        
        super.init();
    }

    @Override
    public boolean input()
    {
        textArea.requestFocusInWindow();
        return super.input();
    }

}
