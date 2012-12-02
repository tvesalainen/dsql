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
package org.vesalainen.parsers.sql.dsql;

import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.RawValue;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.vesalainen.parsers.sql.ColumnMetadata;
import org.vesalainen.parsers.sql.ColumnReference;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.Table;
import org.vesalainen.parsers.sql.InsertStatement;
import org.vesalainen.parsers.sql.SQLConverter;
import org.vesalainen.parsers.sql.TableContext;
import org.vesalainen.parsers.sql.TableContextComparator;
import org.vesalainen.parsers.sql.TableMetadata;
import org.vesalainen.parsers.sql.ToFunction;
import org.vesalainen.parsers.sql.Updateable;

/**
 * @author Timo Vesalainen
 */
public class DSQLEngine extends Engine<Entity, Object> implements DSConstants, DSProxyInterface
{
    private DSProxyInterface proxy;
    private static Statistics statistics;
    private String email;

    private DSQLEngine(DSProxyInterface proxy)
    {
        super(DSQLParser.class);
        this.proxy = proxy;
        statistics = proxy.getStatistics();
        proxy.setConverter(this);
    }

    public static DSQLEngine getInstance(String server, String email, String password) throws IOException
    {
        RemoteApiInstaller installer = new RemoteApiInstaller();
        RemoteApiOptions options = new RemoteApiOptions();
        options.server(server, 443);
        options.credentials(email, password);
        installer.install(options);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        DSQLEngine engine = getInstance(datastore);
        engine.email = email;
        return engine;
    }
    public static DSQLEngine getInstance(DatastoreService datastore)
    {
        DatastoreEngine dse = new DatastoreEngine(datastore);
        return new DSQLEngine(dse);
    }
    public static DSQLEngine getProxyInstance(String server, String email, String password) throws IOException, InterruptedException
    {
        DatastoreEngineProxy dep = new DatastoreEngineProxy(server, email, password);
        dep.start();
        DSQLEngine engine = new DSQLEngine(dep.getProxy());
        engine.email = email;
        return engine;
    }
    @Override
    public Object convert(String string)
    {
        return string;
    }

    @Override
    public Object convert(Number number)
    {
        return number;
    }

    @Override
    public Object convertDate(Date date)
    {
        return date;
    }

    @Override
    public Object convertTime(Date date)
    {
        return date;
    }

    @Override
    public Object convertTimestamp(Date date)
    {
        return date;
    }

    @Override
    public Object get(Entity r, String property)
    {
        if (Entity.KEY_RESERVED_PROPERTY.equals(property))
        {
            return r.getKey();
        }
        if (PARENT.equals(property))
        {
            return r.getParent();
        }
        Object ob = r.getProperty(property);
        if (ob instanceof RawValue)
        {
            RawValue rw = (RawValue) ob;
            return rw.getValue();
        }
        return ob;
    }

    @Override
    public void set(Entity r, String column, Object value)
    {
        if (r.isUnindexedProperty(column))
        {
            r.setUnindexedProperty(column, value);
        }
        else
        {
            r.setProperty(column, value);
        }
    }

    @Override
    public Comparator<Object> getComparator()
    {
        return new DSQLObjectComparator(this);
    }

    @Override
    protected Table<Entity,Object> createTable()
    {
        return new DSTable(this);
    }

    @Override
    public TableMetadata getTableMetadata(String tablename)
    {
        return statistics.getKind(tablename);
    }

    @Override
    protected TableContextComparator getTableContextComparator()
    {
        return new DSTableContextComparator(statistics, this);
    }

    @Override
    public Iterable<TableMetadata> getTables()
    {
        return statistics.getTables();
    }

    @Override
    public Updateable<Entity, Object> getUpdateable(Entity entity, String property, Object value)
    {
        assert entity.hasProperty(property) && !(entity.getProperty(property) instanceof RawValue);
        boolean indexed = false;
        ColumnMetadata cm = statistics.getProperty(entity.getKind(), property);
        if (cm != null)
        {
            indexed = cm.isIndexed();
        }
        if (value != null)
        {
            if (!value.equals(entity.getProperty(property)))
            {
                if (value.getClass().isInstance(entity.getProperty(property)))
                {
                    if (indexed)
                    {
                        entity.setProperty(property, value);
                    }
                    else
                    {
                        entity.setUnindexedProperty(property, value);
                    }
                }
                else
                {
                    throw new IllegalArgumentException("updating through function attempts to change propertys "+property+" type");
                }
            }
        }
        return new UpdateableImpl(entity, property, indexed);
    }

    @Override
    public void update(Collection<Entity> rows)
    {
        proxy.update(rows);
    }

    @Override
    public void setConverter(SQLConverter converter)
    {
        proxy.setConverter(converter);
    }

    public void rollbackTransaction()
    {
        proxy.rollbackTransaction();
    }

    public void insert(InsertStatement insertStatement)
    {
        proxy.insert(insertStatement);
    }

    public Statistics getStatistics()
    {
        return proxy.getStatistics();
    }

    @Override
    public Collection<Entity> fetch(TableContext<Entity, Object> tc, boolean update)
    {
        return proxy.fetch(tc, update);
    }

    public Collection<Entity> fetch(Table<Entity, Object> table)
    {
        return proxy.fetch(table);
    }

    public void exit()
    {
        proxy.exit();
    }

    public void delete(Collection<Entity> rows)
    {
        proxy.delete(rows);
    }

    public void commitTransaction()
    {
        proxy.commitTransaction();
    }

    public void beginTransaction()
    {
        proxy.beginTransaction();
    }

    public Key stringToKey(String encoded)
    {
        return proxy.stringToKey(encoded);
    }

    public String keyToString(Key key)
    {
        return proxy.keyToString(key);
    }

    public String createKeyString(String kind, String name)
    {
        return proxy.createKeyString(kind, name);
    }

    public String createKeyString(String kind, long id)
    {
        return proxy.createKeyString(kind, id);
    }

    public String createKeyString(Key parent, String kind, String name)
    {
        return proxy.createKeyString(parent, kind, name);
    }

    public String createKeyString(Key parent, String kind, long id)
    {
        return proxy.createKeyString(parent, kind, id);
    }

    public Key createKey(String kind, String name)
    {
        return proxy.createKey(kind, name);
    }

    public Key createKey(String kind, long id)
    {
        return proxy.createKey(kind, id);
    }

    public Key createKey(Key parent, String kind, String name)
    {
        return proxy.createKey(parent, kind, name);
    }

    public Key createKey(Key parent, String kind, long id)
    {
        return proxy.createKey(parent, kind, id);
    }

    @Override
    public Class<? extends Object> getDefaultPlaceholderType()
    {
        return String.class;
    }

    public String getEmail()
    {
        return email;
    }

    @Override
    public void send(Message message) throws IOException
    {
        proxy.send(message);
    }

    @Override
    public Session getSession()
    {
        return proxy.getSession();
    }

    @Override
    public void send(MimeMessage message) throws IOException
    {
        proxy.send(message);
    }

    @Override
    public Entity get(Key key) throws EntityNotFoundException
    {
        return proxy.get(key);
    }

    @Override
    public void update(Entity row)
    {
        proxy.update(row);
    }

    @Override
    public void delete(Entity row)
    {
        proxy.delete(row);
    }

    @Override
    public ColumnReference createFunction(ColumnReference inner, String funcName, String... args)
    {
        switch (funcName.toLowerCase())
        {
            case "toemail":
                check(funcName, args.length, 0, 0);
                return new ToFunction(inner, Email.class);
            case "tolink":
                check(funcName, args.length, 0, 0);
                return new ToFunction(inner, Link.class);
            case "tophonenumber":
                check(funcName, args.length, 0, 0);
                return new ToFunction(inner, PhoneNumber.class);
            case "totext":
                check(funcName, args.length, 0, 0);
                return new ToFunction(inner, Text.class);
            case "topostaladdress":
                check(funcName, args.length, 0, 0);
                return new ToFunction(inner, PostalAddress.class);
            case "tocategory":
                check(funcName, args.length, 0, 0);
                return new ToFunction(inner, Category.class);
            case "torating":
                check(funcName, args.length, 0, 0);
                return new ToFunction(inner, Rating.class);
            default:
                try
                {
                    return super.createFunction(inner, funcName, args);
                }
                catch (IllegalArgumentException ex)
                {
                    throw new IllegalArgumentException("expected toemail tophonenumber got"+funcName, ex);
                }
        }
    }

}
