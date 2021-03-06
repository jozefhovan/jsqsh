/*
 * Copyright 2007-2012 Scott C. Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sqsh.jaql;

import java.util.Iterator;
import java.util.Map.Entry;

import org.sqsh.Session;

import com.ibm.jaql.json.type.BufferedJsonArray;
import com.ibm.jaql.json.type.JsonArray;
import com.ibm.jaql.json.type.JsonRecord;
import com.ibm.jaql.json.type.JsonString;
import com.ibm.jaql.json.type.JsonValue;
import com.ibm.jaql.json.util.JsonIterator;
import com.ibm.jaql.json.util.JsonUtil;
import com.ibm.jaql.util.FastPrintStream;

/**
 * Used to format Json output in the "traditional" style of:
 * <pre>
 * [
 *    { 
 *       name: "value",
 *       name2: 10,
 *       name3: {
 *          foo: "a"
 *       },
 *       name4: [
 *          1,
 *          2,
 *          3
 *       ]
 *    }
 *    ...
 * ]
 * </pre>
 */
public class JsonFormatter
    extends JaqlFormatter {
    
    private String defaultIndent;
    
    
    /**
     * Creates a new formatter
     * @param session The session to which the output is to be sent
     * @param indent The indent for the session
     */
    public JsonFormatter (Session session, int indent) {
        
        super(session);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            
            sb.append(' ');
        }
        defaultIndent = sb.toString(); 
    }
    
    @Override
    public String getName() {

        return "json";
    }

    @Override
    public int write (FastPrintStream out, JsonIterator iter)
        throws Exception {
        
        int nrows = 0;
        
        setScale();
        
        if (!isCanceled() && iter.moveNext()) {
            
            /*
             * The getImmutableCopy() is because some functions, such
             * as strSplit(), re-use an internal buffer during each
             * call at a given call-site, so the current value is only
             * good "right now".  That is, the JsonValue I have in my 
             * hand will have its guts re-used on the next call to
             * moveNext(), which has icky side effects in terms of our
             * display.
             */
            JsonValue first = iter.current();
            if (first != null) {

                first = first.getImmutableCopy();
            }

            if (!isCanceled() && iter.moveNext()) {
            
                out.println('[');
                out.print(defaultIndent);
                write(out, first, defaultIndent);
                ++nrows;
                
                /*
                 * Just in case "first" was big, we are holding a copy
                 * right now, so free that up.
                 */
                first = null;
                
                do {
                    
                    out.println(",");
                    out.print(defaultIndent);
                    JsonValue next = iter.current();
                    write(out, next, defaultIndent);
                    ++nrows;
                }
                while (!isCanceled() && iter.moveNext());
                
                out.println();
                out.println(']');
            }
            else {
                
                write(out, first, "");
                ++nrows;
                out.println();
            }
        }
        
        out.flush();
        return nrows;
    }
    
    @Override
    public int write (FastPrintStream out, JsonValue v) 
        throws Exception {
        
        int nrows = write(out, v, "");
        out.println();
        return nrows;
    }
    
    private int write (FastPrintStream out, JsonValue v, String indent)
        throws Exception {
        
        int nrows = 0;
        
        if (v == null) {
            
            out.print("null");
            nrows = 1;
        }
        else if (v instanceof JsonArray) {
            
            JsonArray a = (JsonArray)v;
            if (a.isEmpty()) {
                
                out.print("[ ]");
            }
            else {
                
                out.println('[');
                nrows = printArrayBody(out, a, indent + defaultIndent);
                out.println();
                out.print(indent);
                out.print(']');
            }
        }
        else if (v instanceof JsonRecord) {
            
            JsonRecord r = (JsonRecord)v;
            if (r.isEmpty()) {
                
                out.print("{ }");
            }
            else {
            
                out.println('{');
                printRecordBody(out, (JsonRecord)v, indent + defaultIndent);
                out.println();
                out.print(indent);
                out.print('}');
            }
            nrows = 1;
        }
        else {
            writeScalar(out, v, true);
            nrows = 1;
        }
        
        /*
         * This is a super hack. If the indent is currently zero long
         * then we return the row count because we know that we just 
         * printed the outermost element.
         */
        if (indent.length() != 0)
            nrows = 0;
        
        return nrows;
    }
    
    private int printArrayBody (FastPrintStream out, JsonArray a, String indent)
        throws Exception {
        
        JsonIterator iter = a.iter();
        int count = 0;
            
        while (iter.moveNext()) {
                
            if (count > 0)
                out.println(',');
            ++count;
                
            JsonValue av = iter.current();
            
            out.print(indent);
            write(out, av, indent);
        }
        
        return count;
    }
    
    private void printRecordBody (FastPrintStream out, JsonRecord r, String indent)
        throws Exception {
        
        int idx = 0;
        
        Iterator<Entry<JsonString, JsonValue>> iter = r.iterator();
        while (iter.hasNext()) {
            
            Entry<JsonString, JsonValue> e = iter.next();
            
            String    name = JsonUtil.quote(e.getKey().toString());
            JsonValue val  = e.getValue();
            
            if (idx > 0)
                out.println(",");
            
            out.print(indent);
            out.print(name);
            out.print(": ");
            write(out, val, indent);
            
            ++idx;
        }
    }
}
