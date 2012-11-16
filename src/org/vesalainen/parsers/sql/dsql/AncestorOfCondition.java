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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.vesalainen.parsers.sql.util.ArrayMap;
import org.vesalainen.parsers.sql.Condition;
import org.vesalainen.parsers.sql.ConditionVisitor;
import org.vesalainen.parsers.sql.SelectStatement;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.ParserLocator2Impl;
import org.vesalainen.parsers.sql.SQLConverter;
import org.vesalainen.parsers.sql.Table;
import org.vesalainen.parsers.sql.TruthValue;

/**
 * @author Timo Vesalainen
 */
public class AncestorOfCondition extends ParserLocator2Impl implements Condition<Entity,Object> 
{
    protected DSTable ancestor;
    protected DSTable descendant;

    public AncestorOfCondition(DSTable ancestor, DSTable descendant)
    {
        this.ancestor = ancestor;
        this.descendant = descendant;
    }

    @Override
    public void associateCondition(SelectStatement select, boolean andPath)
    {
        descendant.setDescendantOf(ancestor);
        ancestor.addIndexedColumn(Entity.KEY_RESERVED_PROPERTY);
    }

    @Override
    public TruthValue matches(SQLConverter<Entity, Object> selector, ArrayMap<Table<Entity, Object>, Entity> rowCandidate)
    {
        Entity ae = rowCandidate.get(ancestor);
        Entity de = rowCandidate.get(descendant);
        if (isAncestorOf(ae.getKey(), de.getParent()))
        {
            return TruthValue.TRUE;
        }
        else
        {
            return TruthValue.FALSE;
        }
    }

    private boolean isAncestorOf(Key ancestorKey, Key descendantKey)
    {
        while (descendantKey != null)
        {
            if (ancestorKey.equals(descendantKey))
            {
                return true;
            }
            descendantKey = descendantKey.getParent();
        }
        return false;
    }

    @Override
    public void walk(ConditionVisitor visitor, boolean andPath)
    {
        visitor.visit(this, andPath);
    }

}
