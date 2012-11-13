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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.Table;
import org.vesalainen.parsers.sql.InsertStatement;
import org.vesalainen.parsers.sql.ObjectComparator;
import org.vesalainen.parsers.sql.SQLConverter;
import org.vesalainen.parsers.sql.TableContext;
import org.vesalainen.parsers.sql.TableContextComparator;
import org.vesalainen.parsers.sql.TableMetadata;
import org.vesalainen.parsers.sql.Updateable;

/**
 * @author Timo Vesalainen
 */
public class DSQLEngine extends Engine<Entity, Object> implements DSConstants, DSProxyInterface
{
    private DSProxyInterface proxy;
    private static Statistics statistics;

    private DSQLEngine(DSProxyInterface proxy)
    {
        super(DSQLParser.class);
        this.proxy = proxy;
        statistics = proxy.getStatistics();
        proxy.setConverter(this);
    }

    public static DSQLEngine getInstance(String server, String user, String password) throws IOException
    {
        RemoteApiInstaller installer = new RemoteApiInstaller();
        RemoteApiOptions options = new RemoteApiOptions();
        options.server(server, 443);
        options.credentials(user, password);
        installer.install(options);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        return getInstance(datastore);
    }
    public static DSQLEngine getInstance(DatastoreService datastore)
    {
        DatastoreEngine dse = new DatastoreEngine(datastore);
        return new DSQLEngine(dse);
    }
    public static DSQLEngine getProxyInstance(String server, String user, String password) throws IOException, InterruptedException
    {
        DataStoreEngineProxy dep = new DataStoreEngineProxy(server, user, password);
        dep.start();
        return new DSQLEngine(dep.getProxy());
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
    public Object get(Entity r, String column)
    {
        if (Entity.KEY_RESERVED_PROPERTY.equals(column))
        {
            return r.getKey();
        }
        if (PARENT.equals(column))
        {
            return r.getParent();
        }
        return r.getProperty(column);
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
    public Updateable<Entity, Object> getUpdateable(Entity r, String column)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(Collection<Entity> rows)
    {
        proxy.update(rows);
    }

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

    public Collection<Entity> fetch(TableContext<Entity, Object> tc)
    {
        return proxy.fetch(tc);
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

}
