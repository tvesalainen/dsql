/*
 * Copyright (C) 2014 tkv
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

package org.vesalainen.parsers.sql.dsql.example;

import com.google.appengine.api.datastore.Entity;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;
import org.vesalainen.parsers.sql.dsql.ui.CredentialsDialog;

/**
 *
 * @author tkv
 */
public class Example
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            DSQLEngine engine = DSQLEngine.getProxyInstance(
                    "app.appspot.com", 
                    "namespace",
                    "user@gmail.com", 
                    "password"
            );
            FetchResult<Entity, Object> res = engine.execute("select Tunnus from Laiturit;");
            for (int row=0;row<res.getRowCount();row++)
            {
                for (int col=0;col<res.getColumnCount();col++)
                {
                    System.err.print(res.getValueAt(row, col));
                    System.err.print(", ");
                }
                System.err.println();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
}
