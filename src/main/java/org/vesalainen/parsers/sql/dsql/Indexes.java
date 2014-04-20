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
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Timo Vesalainen
 */
public class Indexes
{
    private Map<String,Set<String>> map = new HashMap<>();
    
    public Indexes(DatastoreService datastore)
    {
        Query q1 = new Query(Entities.KIND_METADATA_KIND);
        PreparedQuery p1 = datastore.prepare(q1);
        for (Entity kind : p1.asIterable())
        {
            String name = kind.getKey().getName();
            if (!name.startsWith("_"))
            {
                Set<String> set = new HashSet<>();
                map.put(name, set);
                Query q2 = new Query(Entities.PROPERTY_METADATA_KIND);
                q2.setAncestor(kind.getKey());
                PreparedQuery p2 = datastore.prepare(q2);
                for (Entity prop : p2.asIterable())
                {
                    Key k = prop.getKey();
                    set.add(k.getName());
                }
            }
        }
    }
    
    public boolean isIndexed(String kind, String property)
    {
        Set<String> set = map.get(kind);
        return set != null && set.contains(property);
    }
}
