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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import java.util.Date;
import java.util.Map;
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
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.sql.Relation;
import org.vesalainen.parsers.sql.ColumnReferenceImpl;
import org.vesalainen.parsers.sql.Condition;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.LiteralImpl;
import org.vesalainen.parsers.sql.RowValue;
import org.vesalainen.parsers.sql.SQLLocator;
import org.vesalainen.parsers.sql.SqlParser;
import org.vesalainen.parsers.sql.Table;
import org.vesalainen.regex.Regex;

/**
 * @author Timo Vesalainen
 * @see <a href="doc-files/statement.html#BNF">BNF Syntax for DSQL-statement</a>
 */
@GenClassname("org.vesalainen.parsers.sql.dsql.DSQLParser")
@GrammarDef()
public abstract class DSQLParserTmpl extends SqlParser<Entity,Object> implements ParserInfo
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
    
    @Rules({
    @Rule(left="placeholderType", value="long")
    })
    protected Class<?>  placeholderTypeLong()
    {
        return Long.class;
    }

    @Rules({
    @Rule(left="placeholderType", value="double")
    })
    protected Class<?>  placeholderTypeDouble()
    {
        return Double.class;
    }

    @Rule(left="placeholderType", value="date")
    protected Class<?>  placeholderTypeDate()
    {
        return Date.class;
    }

    @Rule(left="placeholderType", value="category")
    protected Class<?>  placeholderTypeCategory()
    {
        return Category.class;
    }

    @Rule(left="placeholderType", value="email")
    protected Class<?>  placeholderTypeEmail()
    {
        return Email.class;
    }

    @Rule(left="placeholderType", value="link")
    protected Class<?>  placeholderTypeLink()
    {
        return Link.class;
    }

    @Rule(left="placeholderType", value="phonenumber")
    protected Class<?>  placeholderPhoneNumber()
    {
        return PhoneNumber.class;
    }

    @Rule(left="placeholderType", value="postaladdress")
    protected Class<?>  placeholderPostalAddress()
    {
        return PostalAddress.class;
    }

    @Rule(left="placeholderType", value="rating")
    protected Class<?>  placeholderRating()
    {
        return Rating.class;
    }

    @Rule(left="placeholderType", value="blob")
    protected Class<?>  placeholderBlob()
    {
        return Blob.class;
    }

    @Rule(left="placeholderType", value="shortblob")
    protected Class<?>  placeholderShortBlob()
    {
        return ShortBlob.class;
    }

    @Rule(left="placeholderType", value="text")
    protected Class<?>  placeholderText()
    {
        return Text.class;
    }

    @Rule(left="placeholderType", value="key")
    protected Class<?>  placeholderKey()
    {
        return Key.class;
    }

    @Rule(left="placeholderType", value="user")
    protected Class<?>  placeholderUser()
    {
        return User.class;
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
