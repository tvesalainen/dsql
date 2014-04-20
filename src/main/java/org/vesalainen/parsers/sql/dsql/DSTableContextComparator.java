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
import java.util.Comparator;
import org.vesalainen.parsers.sql.ValueComparisonCondition;
import org.vesalainen.parsers.sql.Relation;
import org.vesalainen.parsers.sql.ColumnCondition;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.TableContext;
import org.vesalainen.parsers.sql.TableContextComparator;
import org.vesalainen.parsers.sql.dsql.Statistics.KindEntry;
import org.vesalainen.parsers.sql.dsql.Statistics.PropertyEntry;

/**
 * @author Timo Vesalainen
 */
public class DSTableContextComparator extends TableContextComparator<Entity,Object>
{
    private Statistics stats;

    public DSTableContextComparator(Statistics stats, Engine selector)
    {
        super(selector);
        this.stats = stats;
    }

    @Override
    public int compare(TableContext<Entity,Object> o1, TableContext<Entity,Object> o2)
    {
        DSTable t1 = (DSTable) o1.getTable();
        DSTable t2 = (DSTable) o2.getTable();
        if (t2.isDescendantOf(t1))
        {
            return -1;
        }
        else
        {
            if (t1.isDescendantOf(t2))
            {
                return 1;
            }
        }
        return super.compare(o1, o2);
    }

}
