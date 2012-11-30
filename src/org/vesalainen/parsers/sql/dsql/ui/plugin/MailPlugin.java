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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Writer;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.magic.Magic;
import org.vesalainen.parsers.magic.Magic.MagicResult;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;
import org.vesalainen.parsers.sql.dsql.DSQLParser;
import org.vesalainen.parsers.sql.dsql.GObjectHelper;
import org.vesalainen.parsers.sql.dsql.ui.FetchResultTableModel;
import org.vesalainen.parsers.sql.dsql.ui.I18n;
import org.vesalainen.regex.Regex;
import org.vesalainen.regex.Replacer;

/**
 * <p>
 * Note! Blogs and ShortBlogs are included as attachments. Link to the attachment
 * is added to message. However Google Appengine doesn't care about Content-ID
 * attachment header and changes ContentType multipart/related to multipart/mixed.
 * 
 * @author Timo Vesalainen
 */
public class MailPlugin extends AbstractMessagePlugin
{

    public MailPlugin(DSQLEngine engine)
    {
        super(I18n.get("MAIL"), new SendAction(engine));
    }

    @Override
    public boolean accept(Class<?> type)
    {
        return Email.class.equals(type);
    }

    @Override
    public void handle(JFrame owner, FetchResultTableModel model)
    {
        super.handle(owner, model);
        SendAction sa = (SendAction) sendAction;
        sa.setModel(model);
        sa.setDialog(dialog);
    }

    public static class SendAction extends AbstractAction
    {

        private String from;
        private DSQLEngine engine;
        private FetchResultTableModel model;
        private MessageDialog dialog;
        private Regex dollarTag;

        public SendAction(DSQLEngine engine)
        {
            super(I18n.get("SEND"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("SEND THE MESSAGE TO EMAIL ADDRESSES IN RESULT TABLE"));
            this.from = engine.getEmail();
            this.engine = engine;
            this.dollarTag = DSQLParser.getInstance().dollarTag;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            String subject = dialog.getSubject();
            String body = dialog.getText();
            ReplacerImpl replacer = new ReplacerImpl(model);
            for (int row = 0; row < model.getRowCount(); row++)
            {
                try
                {
                    Session session = engine.getSession();
                    MimeMessage message = new MimeMessage(session);
                    for (int col = 0; col < model.getColumnCount(); col++)
                    {
                        Object columnValue = model.getValueAt(row, col);
                        if (columnValue != null && (columnValue instanceof Email))
                        {
                            Email email = (Email) columnValue;
                            message.addRecipients(RecipientType.TO, email.getEmail());
                        }
                    }
                    Address[] allRecipients = message.getAllRecipients();
                    if (allRecipients != null && allRecipients.length > 0)
                    {
                        Multipart multiPart = new MimeMultipart("related");
                        MimeBodyPart htmlPart = new MimeBodyPart();
                        multiPart.addBodyPart(htmlPart);
                        replacer.set(row, multiPart);
                        String rs = dollarTag.replace(subject, 256, replacer);
                        String rb = dollarTag.replace(body, 4096, replacer);
                        message.setFrom(new InternetAddress(from));
                        message.setSubject(rs);
                        htmlPart.setContent(rb, "text/html");
                        message.setContent(multiPart);
                        engine.send(message);
                    }
                }
                catch (IOException | MessagingException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
        }

        public void setModel(FetchResultTableModel model)
        {
            this.model = model;
        }

        public void setDialog(MessageDialog dialog)
        {
            this.dialog = dialog;
        }
    }

    private static class ReplacerImpl implements Replacer
    {

        static final Magic magic = Magic.getInstance();
        FetchResultTableModel model;
        Multipart multiPart;
        int row;

        public ReplacerImpl(FetchResultTableModel model)
        {
            this.model = model;
        }

        public void set(int row, Multipart multiPart)
        {
            this.row = row;
            this.multiPart = multiPart;
        }

        @Override
        public void replace(InputReader reader, Writer writer) throws IOException
        {
            String tag = reader.toString();
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
                    MagicResult guess = magic.guess(bytes);
                    if (guess != null && guess.getExtensions().length > 0)
                    {
                        try
                        {
                            String ext = guess.getExtensions()[0];
                            String mimeType = Magic.getMimeType(ext);
                            MimeBodyPart attachment = new MimeBodyPart();
                            attachment.setFileName(tag + "." + ext);
                            DataSource src = new ByteArrayDataSource (bytes, mimeType); 
                            attachment.setDataHandler(new DataHandler(src));
                            String cid = tag + "." + ext;
                            attachment.setHeader("Content-ID","&#60;"+cid+"&#62;");
                            multiPart.addBodyPart(attachment);
                            if (mimeType.startsWith("image"))
                            {
                                writer.write("<div><br></div><div><img src=\"cid:"+cid+"\"><br></div>");
                            }
                            else
                            {
                                writer.write("<div><br></div><div><a href=\"cid:"+cid+"\"><br></div>");
                            }
                            
                        }
                        catch (MessagingException ex)
                        {
                            throw new IOException(ex);
                        }
                    }
                    return;
                }
                writer.write(value.toString());
            }
        }
    }
}
