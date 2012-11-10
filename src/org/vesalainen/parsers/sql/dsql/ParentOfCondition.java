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
import org.vesalainen.parsers.sql.ColumnReferenceImpl;
import org.vesalainen.parsers.sql.JoinComparison;
import org.vesalainen.parsers.sql.Relation;
import org.vesalainen.parsers.sql.SelectStatement;

/**
 * @author Timo Vesalainen
 */
public class ParentOfCondition extends JoinComparison<Entity,Object>
{
    private DSTable parent;
    private DSTable child;
    public ParentOfCondition(DSTable parent, DSTable child)
    {
        super(
                new ColumnReferenceImpl<Entity,Object>(parent, Entity.KEY_RESERVED_PROPERTY),
                Relation.EQ,
                new ColumnReferenceImpl<Entity,Object>(child, DSQLEngine.PARENT)
                );
        this.parent = parent;
        this.child = child;
    }

    @Override
    public void associateCondition(SelectStatement select, boolean andPath)
    {
        child.setDescendantOf(parent);
        super.associateCondition(select, andPath);
    }

}
