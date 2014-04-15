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

import com.google.appengine.api.datastore.Key;
import org.vesalainen.parsers.sql.ObjectComparator;

/**
 * @author Timo Vesalainen
 */
public class DSQLObjectComparator extends ObjectComparator 
{
    private DSQLEngine engine;

    public DSQLObjectComparator(DSQLEngine engine)
    {
        this.engine = engine;
    }
    
    @Override
    public int compare(Object o1, Object o2)
    {
        if ((o1 instanceof Key) && (o2 instanceof String))
        {
            String s = (String) o2;
            o2 = engine.stringToKey(s);
        }
        if ((o1 instanceof String) && (o2 instanceof Key))
        {
            String s = (String) o1;
            o1 = engine.stringToKey(s);
        }
        return super.compare(o1, o2);
    }
    
}
