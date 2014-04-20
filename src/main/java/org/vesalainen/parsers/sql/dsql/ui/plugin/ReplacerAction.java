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

package org.vesalainen.parsers.sql.dsql.ui.plugin;

import com.google.appengine.api.datastore.Entity;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.vesalainen.parsers.sql.ColumnReference;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;
import org.vesalainen.parsers.sql.dsql.ui.I18n;
import org.vesalainen.parsers.sql.dsql.ui.ListDialog;

/**
 * @author Timo Vesalainen
 */
public class ReplacerAction extends TextAction
{
    private Frame owner;
    private FetchResultTableModel model;
    private ListDialog<String> dialog;
    
    public ReplacerAction(Frame owner)
    {
        super(I18n.get("REPLACE"));
        this.owner = owner;
        this.model = model;
        putValue(Action.SHORT_DESCRIPTION, I18n.get("ADD A PROPERTY REFERENCE TOOLTIP"));
    }

    public void setFetchResult(FetchResultTableModel model)
    {
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        List<String> list = new ArrayList<>();
        for (ColumnReference<Entity, Object> cf : model.getFetchResult().getSelectList())
        {
            list.add(cf.toString());
        }
        if (dialog == null)
        {
            dialog = new ListDialog<>(owner, list);
        }
        else
        {
            dialog.refresh(list);
        }
        JTextComponent text = getTextComponent(e);
        if (dialog.input())
        {
            String selected = dialog.getSelected();
            text.replaceSelection("${"+selected+"}");
        }
    }

}
