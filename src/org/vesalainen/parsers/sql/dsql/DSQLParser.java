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
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.Trace;
import org.vesalainen.parser.TraceHelper;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
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
 */
@GenClassname("org.vesalainen.parsers.sql.dsql.DSQLParserImpl")
@GrammarDef()
public abstract class DSQLParser extends SqlParser<Entity,Object> implements ParserInfo
{
    @Rule(left="comparisonPredicate", value="rowValuePredicant is key of identifier")
    protected Condition comparisonKeyOf(
            RowValue rv1, 
            String identifier, 
            @ParserContext("engine") Engine<Entity,Object> engine,
            @ParserContext("correlationMap") Map<String,Table> correlationMap
            )
    {
        DSTable table = (DSTable) getTableForCorrelation(identifier, engine, correlationMap);
        Condition<Object, Object> comparisonCondition = newComparisonCondition(rv1, Relation.EQ, new ColumnReferenceImpl(table, Entity.KEY_RESERVED_PROPERTY));
        table.addIndexedColumn(Entity.KEY_RESERVED_PROPERTY);
        return comparisonCondition;
    }
    @Rule(left="comparisonPredicate", value="identifier is ancestor of identifier")
    protected Condition comparisonAncestorOf(
            String identifier1, 
            String identifier2, 
            @ParserContext("engine") Engine<Entity,Object> engine,
            @ParserContext("correlationMap") Map<String,Table> correlationMap
            )
    {
        DSTable table1 = (DSTable) getTableForCorrelation(identifier1, engine, correlationMap);
        DSTable table2 = (DSTable) getTableForCorrelation(identifier2, engine, correlationMap);
        return new AncestorOfCondition(table1, table2);
    }
    @Rule(left="comparisonPredicate", value="identifier is parent of identifier")
    protected Condition comparisonParentOf(
            String identifier1, 
            String identifier2, 
            @ParserContext("engine") Engine<Entity,Object> engine,
            @ParserContext("correlationMap") Map<String,Table> correlationMap
            )
    {
        DSTable table1 = (DSTable) getTableForCorrelation(identifier1, engine, correlationMap);
        DSTable table2 = (DSTable) getTableForCorrelation(identifier2, engine, correlationMap);
        Condition<Entity, Object> parentOfCondition = new ParentOfCondition(table1, table2);
        return parentOfCondition;
    }
    
    @Rule(left="rowValuePredicant", value="key")
    protected RowValue keyPredicant1(
            @ParserContext("engine") Engine engine,
            @ParserContext("correlationMap") Map<String,Table> correlationMap
            )
    {
        Table table = getTableForCorrelation(null, engine, correlationMap);
        table.addColumn(Entity.KEY_RESERVED_PROPERTY);
        return new ColumnReferenceImpl<>(table, Entity.KEY_RESERVED_PROPERTY);
    }
    
    @Rule(left="rowValuePredicant", value="identifier '\\.' key")
    protected RowValue keyPredicant2(
            String id1,
            @ParserContext("engine") Engine engine,
            @ParserContext("correlationMap") Map<String,Table> correlationMap
            )
    {
        Table table = getTableForCorrelation(id1, engine, correlationMap);
        table.addColumn(Entity.KEY_RESERVED_PROPERTY);
        return new ColumnReferenceImpl<>(table, id1, Entity.KEY_RESERVED_PROPERTY);
    }
    
    @Rule(value="key")
    protected String column()
    {
        return Entity.KEY_RESERVED_PROPERTY;
    }
    
    @Rule(left="literal", value="keyLiteral")
    protected RowValue rowValuePredicantKeyLiteral(Key key)
    {
        return new LiteralImpl<>(key);
    }
    
    @Rule("identifier '\\(' string '\\)'")
    protected Key keyLiteral(String kind, String name, @ParserContext("engine") Engine<Entity,Object> engine)
    {
        DSQLEngine dse = (DSQLEngine) engine;
        return dse.createKey(kind, name);
    }
    
    @Rule("identifier '\\(' integer '\\)'")
    protected Key keyLiteral(String kind, Number id, @ParserContext("engine") Engine<Entity,Object> engine)
    {
        DSQLEngine dse = (DSQLEngine) engine;
        return dse.createKey(kind, id.longValue());
    }
    
    @Rule("keyLiteral '/' identifier '\\(' string '\\)'")
    protected Key keyLiteral(Key parent, String kind, String name, @ParserContext("engine") Engine<Entity,Object> engine)
    {
        DSQLEngine dse = (DSQLEngine) engine;
        return dse.createKey(parent, kind, name);
    }
    
    @Rule(left="keyLiteral", value="keyLiteral '/' identifier '\\(' integer '\\)'")
    protected Key keyLiteral(Key parent, String kind, Number id, @ParserContext("engine") Engine<Entity,Object> engine)
    {
        DSQLEngine dse = (DSQLEngine) engine;
        return dse.createKey(parent, kind, id.longValue());
    }
    
    @Rule("googleTextType '\\(' string '\\)'")
    protected Literal<Entity, Object> literal(Class<?> type, String string)
    {
        try
        {
            Constructor constructor = type.getConstructor(String.class);
            Object newInstance = constructor.newInstance(string);
            return new LiteralImpl<>(newInstance);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex)
        {
            // TODO localisation
            throw new UnsupportedOperationException(type+" not supported String type", ex);
        }
    }

    @Rule("googleIntegerType '\\(' integer '\\)'")
    protected Literal<Entity, Object> literal(Class<?> type, Number number)
    {
        try
        {
            Constructor constructor = type.getConstructor(int.class);
            Object newInstance = constructor.newInstance(number.intValue());
            return new LiteralImpl<>(newInstance);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex)
        {
            throw new UnsupportedOperationException(type+" not supported String type", ex);
        }
    }

    @Rule("googleTypeGeoPt '\\(' decimal '\\,' decimal '\\)'")
    protected Literal<Entity, Object> literal(Class<?> type, Number lat, Number lon)
    {
        Object geoPt = new GeoPt(lat.floatValue(), lon.floatValue());
        return new LiteralImpl<>(geoPt);
    }

    @Rule("googleTypeGeoPt '\\(' ns latitude '\\,' we longitude '\\)'")
    protected Literal<Entity, Object> literal(Class<?> type, int ns, Number lat, int we, Number lon)
    {
        Object geoPt = new GeoPt(lat.floatValue(), lon.floatValue());
        return new LiteralImpl<>(geoPt);
    }

    @Rules({
    @Rule("googleTypeGeoPt"),
    @Rule("googleOtherType"),
    @Rule("googleIntegerType"),
    @Rule("googleTextType")
    })
    protected Class<?>  placeholderType(Class<?> type)
    {
        return type;
    }

    @Rules({
    @Rule("googleTypeCategory"),
    @Rule("googleTypeLink"),
    @Rule("googleTypePhoneNumber"),
    @Rule("googleTypePostalAddress"),
    @Rule("googleTypeText"),
    @Rule("googleTypeEmail")
    })
    protected Class<?> googleTextType(Class<?> type)
    {
        return type;
    }

    @Rules({
    @Rule("googleTypeLong"),
    @Rule("googleTypeRating")
    })
    protected Class<?> googleIntegerType(Class<?> type)
    {
        return type;
    }

    @Rules({
    @Rule("googleTypeShortBlob"),
    @Rule("googleTypeBlob")
    })
    protected Class<?> googleOtherType(Class<?> type)
    {
        return type;
    }

    @Rule("googleTypeGeoPt")
    protected Class<?> googlePairType(Class<?> type)
    {
        return type;
    }

    @Rules({
    @Rule("long")
    })
    protected Class<?> googleTypeLong()
    {
        return Long.class;
    }

    @Rules({
    @Rule("double")
    })
    protected Class<?>  googleTypeDouble()
    {
        return Double.class;
    }

    @Rule("date")
    protected Class<?>  googleTypeDate()
    {
        return Date.class;
    }

    @Rule("category")
    protected Class<?>  googleTypeCategory()
    {
        return Category.class;
    }

    @Rule("email")
    protected Class<?>  googleTypeEmail()
    {
        return Email.class;
    }

    @Rule("link")
    protected Class<?>  googleTypeLink()
    {
        return Link.class;
    }

    @Rule("phonenumber")
    protected Class<?>  googleTypePhoneNumber()
    {
        return PhoneNumber.class;
    }

    @Rule("postaladdress")
    protected Class<?>  googleTypePostalAddress()
    {
        return PostalAddress.class;
    }

    @Rule("rating")
    protected Class<?>  googleTypeRating()
    {
        return Rating.class;
    }

    @Rule("blob")
    protected Class<?>  googleTypeBlob()
    {
        return Blob.class;
    }

    @Rule("shortblob")
    protected Class<?>  googleTypeShortBlob()
    {
        return ShortBlob.class;
    }

    @Rule("text")
    protected Class<?>  googleTypeText()
    {
        return Text.class;
    }

    @Rule("key")
    protected Class<?>  googleTypeKey()
    {
        return Key.class;
    }

    @Rule("user")
    protected Class<?>  googleTypeUser()
    {
        return User.class;
    }

    @Rule("geopt")
    protected Class<?>  googleTypeGeoPt()
    {
        return GeoPt.class;
    }

    
    @Rule("integer degreeChar? decimal secondChar?")
    protected Number latitude(Number degree, Number minutes,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double d = degree.doubleValue() + 
                minutes.doubleValue()/60.0;
        if (d < 0 || d > 90)
        {
            reader.throwSyntaxErrorException("latitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }
    
    @Rule("integer degreeChar? integer secondChar? integer minuteChar?")
    protected Number latitude(Number degree, Number minutes, Number seconds,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double d = degree.doubleValue() + 
                minutes.doubleValue()/60.0 +
                seconds.doubleValue()/360.0;
        if (d < 0 || d > 90)
        {
            reader.throwSyntaxErrorException("latitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }
    
    @Rule("integer degreeChar? decimal secondChar?")
    protected Number longitude(Number degree, Number minutes,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double d = degree.doubleValue() + minutes.doubleValue()/60.0;
        if (d < 0 || d > 180)
        {
            reader.throwSyntaxErrorException("longitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }
    
    @Rule("integer degreeChar? integer secondChar? integer minuteChar?")
    protected Number longitude(Number degree, Number minutes, Number seconds,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double d = degree.doubleValue() + 
                minutes.doubleValue()/60.0 +
                seconds.doubleValue()/360.0;
        if (d < 0 || d > 180)
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
    
    @Rule("'N'")
    protected int north()
    {
        return 1;
    }
    
    @Rule("'E'")
    protected int east()
    {
        return 1;
    }
    
    @Rule("'S'")
    protected int south()
    {
        return -1;
    }
    
    @Rule("'W'")
    protected int west()
    {
        return -1;
    }
    
    @ReservedWords(value =
    {
        "long",
        "double",
        "user",
        "date",
        "category",
        "email",
        "link",
        "phonenumber",
        "postaladdress",
        "rating",
        "blob",
        "shortblob",
        "geopt",
        "text",
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
    
}
