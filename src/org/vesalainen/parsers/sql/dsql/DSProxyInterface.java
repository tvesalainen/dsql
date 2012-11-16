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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import java.util.Collection;
import org.vesalainen.parsers.sql.InsertStatement;
import org.vesalainen.parsers.sql.SQLConverter;
import org.vesalainen.parsers.sql.Table;
import org.vesalainen.parsers.sql.TableContext;

/**
 *
 * @author Timo Vesalainen
 */
public interface DSProxyInterface
{
    void setConverter(SQLConverter converter);
    
    void beginTransaction();

    void commitTransaction();

    void delete(Collection<Entity> rows);

    void insert(InsertStatement insertStatement);
            
    void exit();

    Collection<Entity> fetch(Table<Entity, Object> table);

    Collection<Entity> fetch(TableContext<Entity, Object> tc);

    void rollbackTransaction();

    void update(Collection<Entity> rows);

    Statistics getStatistics();
    
    // KeyFactory methods
    Key	createKey(Key parent, java.lang.String kind, long id);
    Key	createKey(Key parent, java.lang.String kind, java.lang.String name) ;
    Key	createKey(java.lang.String kind, long id) ;
    Key	createKey(java.lang.String kind, java.lang.String name) ;
    String createKeyString(Key parent, java.lang.String kind, long id) ;
    String createKeyString(Key parent, java.lang.String kind, java.lang.String name) ;
    String createKeyString(java.lang.String kind, long id) ;
    String createKeyString(java.lang.String kind, java.lang.String name) ;
    String keyToString(Key key) ;
    Key	stringToKey(java.lang.String encoded) ;

}
