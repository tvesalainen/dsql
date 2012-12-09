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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Writer;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;
import org.vesalainen.parsers.sql.dsql.ui.I18n;

/**
 * @author Timo Vesalainen
 */
public class SendEmailAction extends AbstractSendAction 
{
    private String from;
    private DSQLEngine engine;
    private MimeMultipart multiPart;

    public SendEmailAction(DSQLEngine engine)
    {
        super(I18n.get("SEND"));
        putValue(Action.SHORT_DESCRIPTION, I18n.get("SEND THE MESSAGE TO EMAIL ADDRESSES IN RESULT TABLE"));
        this.from = engine.getEmail();
        this.engine = engine;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
    }

    @Override
    protected void sendTo(String recipient) throws IOException
    {
        MailDialog mdialog = (MailDialog) dialog;
        String subject = mdialog.getSubject();
        String body = dialog.getText();
        try
        {
            Session session = engine.getSession();
            MimeMessage message = new MimeMessage(session);
            message.addRecipients(RecipientType.TO, recipient);
            multiPart = new MimeMultipart("related");
            MimeBodyPart htmlPart = new MimeBodyPart();
            multiPart.addBodyPart(htmlPart);
            String rs = replaceTags(subject);
            String rb = replaceTags(body);
            message.setFrom(new InternetAddress(from));
            message.setSubject(rs);
            htmlPart.setContent(rb, "text/html");
            message.setContent(multiPart);
            engine.send(message);
        }
        catch (IOException | MessagingException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    protected void replace(String tag, String mimeType, String ext, byte[] bytes, Writer writer) throws IOException
    {
        try
        {
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.setFileName(tag + "." + ext);
            DataSource src = new ByteArrayDataSource(bytes, mimeType);
            attachment.setDataHandler(new DataHandler(src));
            String cid = tag + "." + ext;
            attachment.setHeader("Content-ID", "&#60;" + cid + "&#62;");
            multiPart.addBodyPart(attachment);
            if (mimeType.startsWith("image"))
            {
                writer.write("<div><br></div><div><img src=\"cid:" + cid + "\"><br></div>");
            }
            else
            {
                writer.write("<div><br></div><div><a href=\"cid:" + cid + "\"><br></div>");
            }
        }
        catch (MessagingException ex)
        {
            throw new IOException(ex);
        }
    }

}
