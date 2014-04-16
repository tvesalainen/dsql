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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultPlugin;
import org.vesalainen.parsers.sql.dsql.ui.I18n;
import org.vesalainen.parsers.sql.dsql.ui.ListDialog;

/**
 * @author Timo Vesalainen
 */
public class StatementDialog extends ListDialog<String>
{
    private AutoAction executeAction;
    private List<AutoAction> autoActions;
    private DSQLEngine engine;
    private final String storedStatementsKind;
    private Entity newEntity;
    private List<Entity> entityList;
    private Map<AutoAction,JButton> map = new HashMap<>();
    
    public StatementDialog(
            Frame owner, 
            String storedStatementsKind, 
            DSQLEngine engine, 
            AutoAction executeAction, 
            List<AutoAction> autoActions
            )
    {
        super(owner, new ArrayList<String>());
        this.executeAction = executeAction;
        this.autoActions = autoActions;
        this.engine = engine;
        this.storedStatementsKind = storedStatementsKind;
        init();
        refresh();
    }

    public void setEmbed(boolean embed)
    {
        okButton.setEnabled(!embed);
    }
    private void refresh()
    {
        model.clear();
        entityList = engine.getAll(storedStatementsKind);
        for (Entity entity : entityList)
        {
            model.add(entity.getKey().getName());
        }
    }
    private void init()
    {
        okButton.setText(I18n.get("EDIT"));
        for (AutoAction aa : autoActions)
        {
            aa.setAuto(false);
            JButton jButton = new JButton(new Starter(aa));
            map.put(aa, jButton);
            buttonPanel.add(jButton);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        int selected = getSelectedIndex();
        if (selected != -1)
        {
            for (AutoAction aa : autoActions)
            {
                if (aa instanceof FetchResultPlugin)
                {
                    FetchResultPlugin plugin = (FetchResultPlugin) aa;
                    map.get(aa).setEnabled(plugin.activate(entityList.get(selected)));
                }
                else
                {
                    map.get(aa).setEnabled(true);
                }
            }
        }
    }

    public Entity inputEntity()
    {
        if (input())
        {
            int selected = getSelectedIndex();
            if (selected != -1)
            {
                return entityList.get(selected);
            }
        }
        return null;
    }
    private class Starter extends AbstractAction
    {
        private AutoAction autoAction;

        public Starter(AutoAction autoAction)
        {
            super((String)autoAction.getValue(NAME));
            this.autoAction = autoAction;
            setEnabled(false);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            executeAction.setAuto(true);
            autoAction.setAuto(true);
            accepted = true;
            setVisible(false);
        }
        
    }
}
