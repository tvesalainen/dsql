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
import org.vesalainen.parsers.sql.Engine;

/**
 * @author Timo Vesalainen
 */
public class DatastoreEngineProxy extends DatastoreProxy<DSProxyInterface>
{
    private DSProxyInterface engine;
    
    public DatastoreEngineProxy(String server, String namespace, String user, String password) throws InterruptedException
    {
        super(server, namespace, user, password, DSProxyInterface.class);
    }

    @Override
    public void stop()
    {
        if (engine != null)
        {
            engine.exit();
        }
        super.stop();
    }

    
    @Override
    protected DSProxyInterface create(DatastoreService datastore)
    {
        engine = new DatastoreEngine(datastore);
        return (DSProxyInterface) engine;
    }

}
