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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.vesalainen.bcc.model.El;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserCompiler;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.Trace;
import org.vesalainen.parser.TraceHelper;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GenRegex;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.ReservedWords;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.sql.Relation;
import org.vesalainen.parsers.sql.ColumnReferenceImpl;
import org.vesalainen.parsers.sql.Condition;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.Literal;
import org.vesalainen.parsers.sql.LiteralImpl;
import org.vesalainen.parsers.sql.RowValue;
import org.vesalainen.parsers.sql.SQLLocator;
import org.vesalainen.parsers.sql.SqlParser;
import org.vesalainen.parsers.sql.Table;
import org.vesalainen.regex.Regex;

/**
 * @author Timo Vesalainen
 * @see <a href="doc-files/DSQLParser-statement.html#BNF">BNF Syntax for DSQL-statement</a>
 * <p>
 * Functions
 * @see Engine.createFunction
 * @see DSQLEngine.createFunction
 */
@GenClassname("org.vesalainen.parsers.sql.dsql.DSQLParserImpl")
@GrammarDef()
public abstract class DSQLParser extends SqlParser<Entity,Object> implements ParserInfo
{
    @GenRegex("\\$\\{[^\\}]+\\}")
    public static Regex dollarTag;

    private static Map<String,Class<?>> googleTypeMap = new HashMap<>();
    static
    {
        addGoogleType(Integer.class);
        addGoogleType(Long.class);
        addGoogleType(Double.class);
        addGoogleType(Boolean.class);
        addGoogleType(Date.class);
        addGoogleType(Category.class);
        addGoogleType(Email.class);
        addGoogleType(Link.class);
        addGoogleType(PhoneNumber.class);
        addGoogleType(PostalAddress.class);
        addGoogleType(Rating.class);
        addGoogleType(Blob.class);
        addGoogleType(ShortBlob.class);
        addGoogleType(Text.class);
        addGoogleType(Key.class);
        addGoogleType(User.class);
        addGoogleType(GeoPt.class);

    }
    public static DSQLParser getInstance()
    {
        return (DSQLParser) GenClassFactory.loadGenInstance(DSQLParser.class);
    }

    private static void addGoogleType(Class<?> aClass)
    {
        googleTypeMap.put(aClass.getSimpleName().toLowerCase(), aClass);
    }
    /**
     * 
     * @param text
     * @return 
     * @see <a href="doc-files/DSQLParser-coordinate.html#BNF">BNF Syntax for Geological Coordinate</a>
     */
    @ParseMethod(start="coordinate", whiteSpace ="whiteSpace")
    public abstract GeoPt parseCoordinate(String text, @ParserContext("locator") SQLLocator locator);
    
    @Rule(left="comparisonPredicate", value="rowValuePredicant is key of identifier")
    protected Condition comparisonKeyOf(
            RowValue rv1, 
            String identifier, 
            @ParserContext("engine") Engine<Entity,Object> engine,
            @ParserContext("tableListStack") Deque<List<Table<Entity,Object>>> tableListStack
            )
    {
        List<String> list = new ArrayList<>();
        list.add(identifier);
        list.add(Entity.KEY_RESERVED_PROPERTY);
        Condition<Object, Object> comparisonCondition = newComparisonCondition(rv1, Relation.EQ, new ColumnReferenceImpl(list), tableListStack.peek());
        return comparisonCondition;
    }
    @Rule(left="comparisonPredicate", value="identifier is ancestor of identifier")
    protected Condition comparisonAncestorOf(String ancestor, String descendant)
    {
        return new AncestorOfCondition(ancestor, descendant);
    }
    @Rule(left="comparisonPredicate", value="identifier is parent of identifier")
    protected Condition comparisonParentOf(
            String parent, 
            String child, 
            @ParserContext("engine") Engine<Entity,Object> engine
            )
    {
        Condition<Entity, Object> parentOfCondition = new ParentOfCondition(parent, child);
        return parentOfCondition;
    }
    
    @Rule(left="rowValuePredicant", value="key")
    protected RowValue keyPredicant1(
            @ParserContext("engine") Engine engine
            )
    {
        List<String> list = new ArrayList<>();
        list.add(Entity.KEY_RESERVED_PROPERTY);
        return new ColumnReferenceImpl<>(list);
    }
    
    @Rule(left="rowValuePredicant", value="identifier '\\.' key")
    protected RowValue keyPredicant2(
            String id1,
            @ParserContext("engine") Engine engine
            )
    {
        List<String> list = new ArrayList<>();
        list.add(id1);
        list.add(Entity.KEY_RESERVED_PROPERTY);
        return new ColumnReferenceImpl<>(list);
    }
    
    @Rule("key")
    protected String column()
    {
        return Entity.KEY_RESERVED_PROPERTY;
    }
    
    @Rule(value="identifier", doc="one of java/google type: integer, long, double, date, category, email, link, phonenumber, postaladdress, rating, blob, shortblob, text, key, user, geopt")
    protected Class<?> placeholderType(
            String typeName,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader
            )
    {
        Class<?> type = googleTypeMap.get(typeName.toLowerCase());
        if (type == null)
        {
            reader.throwSyntaxErrorException("Key", typeName);
        }
        return type;
    }
            
    @Rule("key '\\(' keyValue '\\)'")
    protected Literal<Entity, Object> literal(
            Key key, 
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader
            )
    {
        return new LiteralImpl<Entity, Object>(key);
    }

    @Rule("identifier '\\(' string '\\)'")
    protected Key keyValue(String kind, String name, @ParserContext("engine") Engine<Entity,Object> engine)
    {
        DSQLEngine dse = (DSQLEngine) engine;
        return dse.createKey(kind, name);
    }
    
    @Rule("identifier '\\(' integer '\\)'")
    protected Key keyValue(String kind, Number id, @ParserContext("engine") Engine<Entity,Object> engine)
    {
        DSQLEngine dse = (DSQLEngine) engine;
        return dse.createKey(kind, id.longValue());
    }
    
    @Rule("keyValue '/' identifier '\\(' string '\\)'")
    protected Key keyValue(Key parent, String kind, String name, @ParserContext("engine") Engine<Entity,Object> engine)
    {
        DSQLEngine dse = (DSQLEngine) engine;
        return dse.createKey(parent, kind, name);
    }
    
    @Rule("keyValue '/' identifier '\\(' integer '\\)'")
    protected Key keyValue(Key parent, String kind, Number id, @ParserContext("engine") Engine<Entity,Object> engine)
    {
        DSQLEngine dse = (DSQLEngine) engine;
        return dse.createKey(parent, kind, id.longValue());
    }
    
    @Rule("identifier '\\(' string ('\\,' string)* '\\)'")
    protected Literal<Entity, Object> literal(
            String typeName, 
            String string,
            List<String> list,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader
            )
    {
        list.add(0, string);
        try
        {
            Class<?> type = googleTypeMap.get(typeName.toLowerCase());
            Object obj = GObjectHelper.valueOf(type, list.toArray(new String[list.size()]));
            return new LiteralImpl<>(obj);
        }
        catch (Exception ex)
        {
            reader.throwSyntaxErrorException("Long, Double, Boolean, Category, Email, Link, PhoneNumber, Text, User", typeName);
        }
        return null;
    }
    @Rule("identifier '\\(' integer '\\)'")
    protected Literal<Entity, Object> literal(
            String typeName, 
            Number number, 
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader
            )
    {
        try
        {
            Class<?> type = googleTypeMap.get(typeName.toLowerCase());
            Constructor constructor = type.getConstructor(int.class);
            Object newInstance = constructor.newInstance(number.intValue());
            return new LiteralImpl<>(newInstance);
        }
        catch (NullPointerException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex)
        {
            reader.throwSyntaxErrorException("Long, Rating", typeName);
        }
        return null;
    }

    @Rule("identifier '\\(' coordinate '\\)'")
    protected Literal<Entity, Object> literal(
            String typeName, 
            GeoPt pt, 
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader
            )
    {
        Class<?> type = googleTypeMap.get(typeName.toLowerCase());
        if (!GeoPt.class.equals(type))
        {
            reader.throwSyntaxErrorException("GeoPt", typeName);
        }
        return new LiteralImpl<Entity, Object>(pt);
    }

    @Rule("decimal '\\,' decimal")
    protected GeoPt coordinate(Number lat, Number lon)
    {
        return new GeoPt(lat.floatValue(), lon.floatValue());
    }

    @Rule("ns latitude '\\,' we longitude")
    protected GeoPt coordinate(int ns, Number lat, int we, Number lon)
    {
        return new GeoPt(ns*lat.floatValue(), we*lon.floatValue());
    }
    
    @Rule("integer degreeChar? decimal secondChar?")
    protected Number latitude(Number degree, Number minutes,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double deg = degree.doubleValue();
        double min = minutes.doubleValue();
        double d = deg + min/60.0;
        if (d < 0 || d > 90 || min < 0 || min > 60)
        {
            reader.throwSyntaxErrorException("latitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }
    
    @Rule("integer degreeChar? integer secondChar? integer minuteChar?")
    protected Number latitude(Number degree, Number minutes, Number seconds,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double deg = degree.doubleValue();
        double min = minutes.doubleValue();
        double sec = seconds.doubleValue();
        double d = deg + min/60.0 + sec/3600.0;
        if (d < 0 || d > 90 || min < 0 || min > 60 || sec < 0 || sec > 60)
        {
            reader.throwSyntaxErrorException("latitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }
    
    @Rule("integer degreeChar? decimal secondChar?")
    protected Number longitude(Number degree, Number minutes,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double deg = degree.doubleValue();
        double min = minutes.doubleValue();
        double d = deg + min/60.0;
        if (d < 0 || d > 180 || min < 0 || min > 60)
        {
            reader.throwSyntaxErrorException("longitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }
    
    @Rule("integer degreeChar? integer secondChar? integer minuteChar?")
    protected Number longitude(Number degree, Number minutes, Number seconds,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double deg = degree.doubleValue();
        double min = minutes.doubleValue();
        double sec = seconds.doubleValue();
        double d = deg + min/60.0 + sec/3600.0;
        if (d < 0 || d > 180 || min < 0 || min > 60 || sec < 0 || sec > 60)
        {
            reader.throwSyntaxErrorException("longitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }

    @Terminal(expression="\u00b0")
    protected abstract void degreeChar();
    
    @Terminal(expression="\"")
    protected abstract void minuteChar();
    
    @Terminal(expression="'")
    protected abstract void secondChar();
    
    @Rules({
        @Rule("north"),
        @Rule("south")
    })
    protected abstract int ns(int sign);
    
    @Rules({
        @Rule("west"),
        @Rule("east")
    })
    protected abstract int we(int sign);
    
    @Rule("n")
    protected int north()
    {
        return 1;
    }
    
    @Rule("e")
    protected int east()
    {
        return 1;
    }
    
    @Rule("s")
    protected int south()
    {
        return -1;
    }
    
    @Rule("w")
    protected int west()
    {
        return -1;
    }
    
    @ReservedWords(value =
    {
        "n",
        "s",
        "w",
        "e",
        "key",
        "of",
        "parent",
        "ancestor"
    },
    options =
    {
        Regex.Option.CASE_INSENSITIVE
    })
    protected void reservedWordsD(
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader,
            @ParserContext("locator") SQLLocator locator
            )
    {
        reservedWords(reader, locator);
    }

    //@TraceMethod
    protected void trace(
            int ord,
            int ctx,
            @ParserContext("$inputReader") InputReader reader,
            @ParserContext("$token") int token,
            @ParserContext("$laToken") int laToken,
            @ParserContext("$curTok") int curtok,
            @ParserContext("$stateStack") int[] stack,
            @ParserContext("$sp") int sp,
            @ParserContext("$typeStack") int[] typeStack,
            @ParserContext("$valueStack") Object[] valueStack
            )
    {
        Trace trace = Trace.values()[ord];
        switch (trace)
        {
            case STATE:
                System.err.println("state "+stack[sp]);
                break;
            case INPUT:
                if (ctx >= 0)
                {
                    System.err.println("input"+ctx+"='"+reader.getString()+"' token="+getToken(token));
                }
                else
                {
                    System.err.println("re input='"+reader.getString()+"' token="+getToken(token));
                }
                break;
            case LAINPUT:
                if (ctx >= 0)
                {
                    System.err.println("lainput"+ctx+"='"+reader.getString()+"' token="+getToken(laToken));
                }
                else
                {
                    System.err.println("re lainput='"+reader.getString()+"' token="+getToken(laToken));
                }
                break;
            case PUSHVALUE:
                System.err.println("push value");
                break;
            case EXITLA:
                System.err.println("exit La");
                TraceHelper.printStacks(System.err, stack, typeStack, valueStack, sp);
                break;
            case BEFOREREDUCE:
                System.err.println("Before reducing rule "+getRule(ctx));
                TraceHelper.printStacks(System.err, stack, typeStack, valueStack, sp);
                break;
            case AFTERREDUCE:
                System.err.println("After reducing rule "+getRule(ctx));
                TraceHelper.printStacks(System.err, stack, typeStack, valueStack, sp);
                break;
            case GOTO:
                System.err.println("Goto "+ctx);
                break;
            case SHIFT:
                System.err.println("Shift "+ctx);
                break;
            case SHRD:
                System.err.println("Shift/Reduce");
                break;
            case LASHRD:
                System.err.println("La Shift/Reduce");
                break;
            case GTRD:
                System.err.println("Goto/Reduce");
                break;
            case LASHIFT:
                System.err.println("LaShift State "+ctx);
                TraceHelper.printStacks(System.err, stack, typeStack, valueStack, sp);
                break;
            default:
                System.err.println("unknown action "+trace);
                break;
        }
    }

    public static void main(String... args)
    {
        try
        {
            ParserCompiler pc = new ParserCompiler(El.getTypeElement(DSQLParser.class.getCanonicalName()));
            pc.compile();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
