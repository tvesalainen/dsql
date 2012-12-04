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

import com.google.appengine.api.datastore.Email;
import javax.swing.JFrame;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;
import org.vesalainen.parsers.sql.dsql.ui.I18n;

/**
 * <p>
 * Note! Blogs and ShortBlogs are included as attachments. Link to the attachment
 * is added to message. However Google Appengine doesn't care about Content-ID
 * attachment header and changes ContentType multipart/related to multipart/mixed.
 * 
 * @author Timo Vesalainen
 */
public class MailPlugin extends AbstractMessagePlugin<Email>
{

    public MailPlugin(DSQLEngine engine)
    {
        super(I18n.get("MAIL"), new SendEmailAction(engine), Email.class);
    }

    @Override
    public void handle(JFrame owner, FetchResultTableModel model)
    {
        super.handle(owner, model);
        SendEmailAction sa = (SendEmailAction) sendAction;
        sa.setModel(model);
        sa.setDialog(dialog);
    }

    @Override
    public String getString(Email t)
    {
        return t.getEmail();
    }

}
