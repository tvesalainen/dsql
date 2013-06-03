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

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Timo Vesalainen
 */
public class GObjectHelper 
{
    public static final Map<Class<?>,Class<?>> typeMap = new HashMap<>();
    private static final DSQLParser locationParser = DSQLParser.getInstance();
    static
    {
        typeMap.put(Long.class, Long.class);
        typeMap.put(Double.class, Double.class);
        typeMap.put(Date.class, Date.class);
        typeMap.put(Category.class, String.class);
        typeMap.put(Email.class, String.class);
        typeMap.put(Link.class, String.class);
        typeMap.put(PhoneNumber.class, String.class);
        typeMap.put(PostalAddress.class, String.class);
        typeMap.put(Rating.class, Integer.class);
        typeMap.put(Blob.class, byte[].class);
        typeMap.put(ShortBlob.class, byte[].class);
        typeMap.put(Text.class, String.class);
    }
    public static Class<?> getInnerType(Object ob)
    {
        return getInnerType(ob.getClass());
    }
    public static Class<?> getInnerType(Class<?> gType)
    {
        return typeMap.get(gType);
    }
    public static String getString(Object ob)
    {
        if (ob instanceof Category)
        {
            Category gob = (Category) ob;
            return gob.getCategory();
        }
        else
        {
            if (ob instanceof Email)
            {
                Email gob = (Email) ob;
                return gob.getEmail();
            }
            else
            {
                if (ob instanceof Link)
                {
                    Link gob = (Link) ob;
                    return gob.getValue();
                }
                else
                {
                    if (ob instanceof PhoneNumber)
                    {
                        PhoneNumber gob = (PhoneNumber) ob;
                        return gob.getNumber();
                    }
                    else
                    {
                        if (ob instanceof PostalAddress)
                        {
                            PostalAddress gob = (PostalAddress) ob;
                            return gob.getAddress();
                        }
                        else
                        {
                            if (ob instanceof Text)
                            {
                                Text gob = (Text) ob;
                                return gob.getValue();
                            }
                            else
                            {
                                if (ob instanceof Rating)
                                {
                                    Rating gob = (Rating) ob;
                                    return String.valueOf(gob.getRating());
                                }
                                else
                                {
                                    if (ob instanceof GeoPt)
                                    {
                                        GeoPt p = (GeoPt) ob;
                                        return toString(p);
                                    }
                                    else
                                    {
                                        return ob.toString();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public static String toString(GeoPt pt)
    {
        float lat = pt.getLatitude();
        char ns = lat > 0 ? 'N' : 'S';
        lat = Math.abs(lat);
        int lati = (int) lat;
        lat = lat-lati;
        float lon = pt.getLongitude();
        char we = lon > 0 ? 'E' : 'W';
        lon = Math.abs(lon);
        int loni = (int) lon;
        lon = lon-loni;
        return String.format(Locale.US,
                "%c %d\u00b0 %.3f', %c %d\u00b0 %.3f'", 
                ns,
                lati,
                lat*60,
                we,
                loni,
                lon*60
                );
    }
    public static byte[] getBytes(Object ob)
    {
        if (ob instanceof Blob)
        {
            Blob gob = (Blob) ob;
            return gob.getBytes();
        }
        else
        {
            if (ob instanceof ShortBlob)
            {
                ShortBlob gob = (ShortBlob) ob;
                return gob.getBytes();
            }
            else
            {
                throw new IllegalArgumentException(ob+" is not byte[] datastore type");
            }
        }
    }
    public static int getInt(Object ob)
    {
        if (ob instanceof Rating)
        {
            Rating gob = (Rating) ob;
            return gob.getRating();
        }
        else
        {
            throw new IllegalArgumentException(ob+" is not byte[] datastore type");
        }
    }
    public static Object valueOf(Class<?> type, String... params)
    {
        if (GeoPt.class == type)
        {
            if (params.length != 1)
            {
                throw new IllegalArgumentException(params+" not valid");
            }
            return locationParser.parseCoordinate(params[0], null);
        }
        try
        {
            Class[] paramTypes = new Class[params.length];
            Arrays.fill(paramTypes, String.class);
            Constructor constructor = type.getConstructor(paramTypes);
            return constructor.newInstance((Object[]) params);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex)
        {
            throw new IllegalArgumentException(type+" not valid");
        }
    }
}
