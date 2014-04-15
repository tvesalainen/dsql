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
import java.util.Set;
import org.vesalainen.parsers.sql.Updateable;

/**
 * @author Timo Vesalainen
 */
class UpdateableImpl implements Updateable<Entity, Object>
{
    private Entity entity;
    private String property;
    private boolean indexes;

    public UpdateableImpl(Entity entity, String property, boolean indexes)
    {
        this.entity = entity;
        this.property = property;
        this.indexes = indexes;
    }

    @Override
    public Object getValue()
    {
        return entity.getProperty(property);
    }

    @Override
    public Entity setValue(Object value)
    {
        Object old = entity.getProperty(property);
        if (
                (old == null && value != null) ||
                (old != null && value == null) ||
                old != null && !old.equals(value)
                )
        {
            if (indexes)
            {
                entity.setProperty(property, value);
            }
            else
            {
                entity.setUnindexedProperty(property, value);
            }
            return entity;
        }
        else
        {
            return null;
        }
    }

    @Override
    public Entity getRow()
    {
        return entity;
    }

}
