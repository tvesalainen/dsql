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
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.InputStream;
import java.util.EnumSet;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.vesalainen.bcc.model.Typ;
import static org.vesalainen.parser.ParserFeature.*;
import org.vesalainen.parser.ParserOffsetLocator;
import org.vesalainen.parser.util.Input;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parser.util.OffsetLocatorException;
import org.vesalainen.parsers.sql.ColumnReferenceImpl;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.Statement;

/**
 *
 * @author Timo Vesalainen
 */
public class TestDSQLT
{

    ClassLoader classLoader = TestDSQLT.class.getClassLoader();
    static final String PACKAGE = TestDSQLT.class.getPackage().getName().replace('.', '/') + "/";
    static Engine engine;

    private final LocalServiceTestHelper helper = 
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

    public TestDSQLT()
    {
    }

    @Before
    public void setUp()
    {
        helper.setUp();
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        engine = DSQLEngine.getInstance(ds);
        InputStream is = classLoader.getResourceAsStream(PACKAGE + "populate1.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
    }

    @After
    public void tearDown()
    {
        engine = null;
        helper.tearDown();
    }

    @Test
    public void test1()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE + "test1.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        result.print(System.err);
        assertEquals(1, result.getRowCount());
        assertEquals(5, result.getColumnCount());
        assertEquals("Timo", result.getValueAt(0, 0));
        assertEquals("Vesalainen", result.getValueAt(0, 1));
        assertEquals("Valpuri", result.getValueAt(0, 2));
        assertEquals("PV", result.getValueAt(0, 3));
        assertEquals(123L, result.getValueAt(0, 4));
        System.err.print("end");
    }

    @Test
    public void test2()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE + "test2.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        assertNotNull(result);
        result.print(System.err);
    }

    @Test
    public void test3()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE + "test3.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        assertNotNull(result);
        result.print(System.err);
    }

    @Test
    public void test4()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE + "test4.sql");
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
        InputStream is = classLoader.getResourceAsStream(PACKAGE + "test7.sql");
        assertNotNull(is);
        FetchResult<Entity, Object> result = engine.execute(is);
        assertNotNull(result);
        result.print(System.err);
    }

    @Test
    public void test8()
    {
        InputStream is = classLoader.getResourceAsStream(PACKAGE + "test8.sql");
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
        assertTrue(Typ.isAssignable(
                Typ.getTypeFor(ColumnReferenceImpl.class), 
                Typ.getTypeFor(ParserOffsetLocator.class)
        ));

        try
        {
            engine.execute("select company.name from company order by emp.name;");
            fail();
        }
        catch (OffsetLocatorException ex)
        {
            ex.printStackTrace();
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
            reader = Input.getInstance("select company.name, from company order by emp.name;", EnumSet.of(UseOffsetLocatorException));
            engine.check("select company.name, from company order by emp.name;");
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
