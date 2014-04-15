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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.magic.Magic;
import org.vesalainen.parsers.sql.dsql.DSQLParser;
import org.vesalainen.parsers.sql.dsql.GObjectHelper;
import org.vesalainen.parsers.sql.dsql.ui.AbstractEditableListDialog;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultPlugin;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;
import org.vesalainen.regex.Regex;
import org.vesalainen.regex.Replacer;

/**
 * @author Timo Vesalainen
 */
public abstract class AbstractSendAction<T> extends AbstractAction
{
    static final Magic magic = Magic.getInstance();
    /**
     * Replacer buffer size. Size depends on the size of replacement strings.
     */
    protected int BufferSize = 4096;
    protected Regex dollarTag;
    protected FetchResultPlugin<T> plugin;
    protected MessageDialog dialog;
    protected FetchResultTableModel model;
    private ReplacerImpl replacer;
    protected int row;

    public AbstractSendAction(String name)
    {
        super(name);
        dollarTag = DSQLParser.getInstance().dollarTag;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Map<String,Integer> targetMap = new TreeMap<>();
        for (int row = 0; row < model.getRowCount(); row++)
        {
            for (int col = 0; col < model.getColumnCount(); col++)
            {
                Object columnValue = model.getValueAt(row, col);
                if (plugin.accept(columnValue))
                {
                    T target = (T) columnValue;
                    targetMap.put(plugin.getString(target), row);
                }
            }
        }
        List<String> list = new ArrayList<>();
        list.addAll(targetMap.keySet());
        AbstractEditableListDialog dia = new AbstractEditableListDialog<String>(null, list)
        {
            @Override
            protected String create(String str)
            {
                return str;
            }
        };
        dia.setTitle(org.vesalainen.parsers.sql.dsql.ui.I18n.get("RECIPIENTS"));
        if (dia.input())
        {
            for (String recipient : list)
            {
                Integer rowI = targetMap.get(recipient);
                if (rowI != null)
                {
                    row = rowI;
                }
                else
                {
                    row = -1;
                }
                try
                {
                    sendTo(recipient);
                }
                catch (IOException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
        }
                
    }

    protected String replaceTags(String text) throws IOException
    {
        return dollarTag.replace(text, BufferSize, replacer);
    }
    
    public void setPlugin(FetchResultPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void setDialog(MessageDialog dialog)
    {
        this.dialog = dialog;
    }

    public void setModel(FetchResultTableModel model)
    {
        if (this.model == null || this.model != model)
        {
            this.model = model;
            replacer = new ReplacerImpl();
        }
    }

    protected void replace(String tag, String mimeType, String ext, byte[] bytes, Writer writer) throws IOException
    {
    }

    protected abstract void sendTo(String recipient) throws IOException;

    private class ReplacerImpl implements Replacer 
    {
        @Override
        public void replace(InputReader reader, Writer writer) throws IOException
        {
            String tag = reader.toString();
            if (row != -1)
            {
                tag = tag.substring(2, tag.length() - 1);
                Object value = model.getFetchResult().getValueAt(row, tag);
                if (value != null)
                {
                    if (String.class.equals(GObjectHelper.getInnerType(value)))
                    {
                        reader.insert(GObjectHelper.getString(value));
                        return;
                    }
                    if (byte[].class.equals(GObjectHelper.getInnerType(value)))
                    {
                        byte[] bytes = GObjectHelper.getBytes(value);
                        Magic.MagicResult guess = magic.guess(bytes);
                        if (guess != null && guess.getExtensions().length > 0)
                        {
                            String ext = guess.getExtensions()[0];
                            String mimeType = Magic.getMimeType(ext);
                            AbstractSendAction.this.replace(tag, mimeType, ext, bytes, writer);
                        }
                        return;
                    }
                    writer.write(value.toString());
                }
            }
            else
            {
                // inserted recipient
                writer.write(tag);
            }
        }

    }
}
