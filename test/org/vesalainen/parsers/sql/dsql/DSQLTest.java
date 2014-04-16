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
import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parser.util.OffsetLocatorException;
import org.vesalainen.parsers.sql.Statement;

/**
 *
 * @author Timo Vesalainen
 */
public class DSQLTest
{
    ClassLoader classLoader = DSQLTest.class.getClassLoader();
    static final String PACKAGE = DSQLTest.class.getPackage().getName().replace('.', '/')+"/";
    static Engine engine;
            
    public DSQLTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        engine = DSQLEngine.getInstance("<app>.appspot.com", "<namespace>", "<user email>", "<password>");
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
        engine.exit();
    }

    @Test
    public void test1()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE+"test1.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        assertTrue(result.getRowCount() == 2);
        result.print(System.err);
        System.err.print("end");
    }
    @Test
    public void test2()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE+"test2.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        assertNotNull(result);
        result.print(System.err);
    }
    @Test
    public void test3()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE+"test3.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        assertNotNull(result);
        result.print(System.err);
    }
    @Test
    public void test4()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE+"test4.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        assertNotNull(result);
        result.print(System.err);
    }
    
    @Test
    public void test5()
    {
        FetchResult<Entity, Object> result = engine.execute("show tables;");
        assertNotNull(result);
        result.print(System.err);
    }
    
    @Test
    public void test6()
    {
        FetchResult<Entity, Object> result = engine.execute("desc Reservation;");
        assertNotNull(result);
        result.print(System.err);
    }
    
    @Test
    public void test7()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE+"test7.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        assertNotNull(result);
        result.print(System.err);
    }
    @Test
    public void test8()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE+"test8.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        assertNotNull(result);
        result.print(System.err);
    }
    
    @Test
    public void test9()
    {
        try
        {
            engine.execute("select emp.name from company;");
            fail();
        }
        catch (OffsetLocatorException ex)
        {
            assertEquals(7, ex.getStart());
        }
    }
    
    @Test
    public void test10()
    {
        try
        {
            engine.execute("select company.name from company order by emp.name;");
            fail();
        }
        catch (OffsetLocatorException ex)
        {
            assertEquals(42, ex.getStart());
        }
    }
    
    @Test
    public void test11()
    {
        try
        {
            engine.execute("select company.name from company where emp.name = 'jack';");
            fail();
        }
        catch (OffsetLocatorException ex)
        {
            assertEquals(39, ex.getStart());
        }
    }
    
    @Test
    public void test12()
    {
        engine.execute("INSERT INTO test (__key__, name, price, date) VALUES(key(test(123)), 'bolt', 1.23, date '2012-10-28');");
        engine.execute("update test set name = 'nut', price = 0.5 where __key__ = key(test(123));");
        engine.execute("delete from test where name = 'nut';");
        engine.execute("INSERT INTO test2 (name, price, date) select name, price, date from test;");
    }
    @Test
    public void test14()
    {
        InputReader reader = null;
        try
        {
            reader = new InputReader("select company.name, from company order by emp.name;");
            engine.check(reader);
            fail();
        }
        catch (OffsetLocatorException ex)
        {
            assertEquals(26, ex.getStart());
        }
        try
        {
            reader.reuse("select ");
            engine.check(reader);
            fail();
        }
        catch (OffsetLocatorException ex)
        {
            assertEquals(7, ex.getStart());
        }
    }
    
    @Test
    public void test15()
    {
        Statement statement = engine.prepare("select Etunimi, Sukunimi from Jasenet where Email = :email;");
        statement.bindValue("email", "timo.vesalainen@iki.fi");
        FetchResult fr = statement.execute();
        fr.print(System.err);
    }
    
}
