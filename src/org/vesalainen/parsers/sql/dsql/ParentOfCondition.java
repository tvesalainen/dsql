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
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.parsers.sql.ColumnReference;
import org.vesalainen.parsers.sql.ColumnReferenceImpl;
import org.vesalainen.parsers.sql.JoinComparison;
import org.vesalainen.parsers.sql.Relation;
import org.vesalainen.parsers.sql.SelectStatement;

/**
 * @author Timo Vesalainen
 */
public class ParentOfCondition extends JoinComparison<Entity,Object>
{
    public ParentOfCondition(String parent, String child)
    {
        super(
                parentInit(parent),
                Relation.EQ,
                childInit(child)
                );
    }

    private static ColumnReference<Entity,Object> parentInit(String parent)
    {
        List<String> parentList = new ArrayList<>();
        parentList.add(parent);
        parentList.add(Entity.KEY_RESERVED_PROPERTY);
        return new ColumnReferenceImpl<Entity,Object>(parentList);
    }
    private static ColumnReference<Entity,Object> childInit(String child)
    {
        List<String> childList = new ArrayList<>();
        childList.add(child);
        childList.add(DSQLEngine.PARENT);
        return new ColumnReferenceImpl<Entity,Object>(childList);
    }
    @Override
    public void associateCondition(SelectStatement select, boolean andPath)
    {
        DSTable child = (DSTable) columnReference2.getTable();
        DSTable parent = (DSTable) columnReference.getTable();
        child.setDescendantOf(parent);
        super.associateCondition(select, andPath);
    }

}
