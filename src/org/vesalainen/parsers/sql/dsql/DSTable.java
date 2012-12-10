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

import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.Table;

/**
 * @author Timo Vesalainen
 */
public class DSTable<R,C> extends Table<R,C> 
{
    private DSTable ancestor;

    public DSTable(Engine<R,C> engine, String schema, String tablename, String correlationName)
    {
        super(engine, schema, tablename, correlationName);
    }

    public void setDescendantOf(DSTable<R,C> ancestor)
    {
        if (this.ancestor != null)
        {
            throw new IllegalArgumentException("more than ona anchestor");
        }
        this.ancestor = ancestor;
    }

    boolean isDescendantOf(DSTable t2)
    {
        return t2.equals(ancestor);
    }

    public DSTable getAncestor()
    {
        return ancestor;
    }

}
