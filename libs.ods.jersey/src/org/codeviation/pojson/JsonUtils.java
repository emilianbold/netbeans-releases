/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.codeviation.pojson;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.codeviation.commons.reflect.ClassUtils;

/** Various utility methods for working with JSON Files. Parts of this class
 * are taken from org.json.me
 *
 * @author Petr Hrebejk
 */
class JsonUtils {

    private JsonUtils() {}
    
    public static String toJsonString(Object object) {
             
        if ( object == null || object instanceof Void ) {
            return "null";
        }
        else if ( object instanceof Enum ) {
            return quote(object.toString());
        }
        else if ( object instanceof Boolean ) {
            return object.toString();
        }
        else if ( object instanceof Character ) {
            return quote(object.toString());
        }
        else if ( object instanceof String ) {
            return quote((String)object);
        }
        else if ( object instanceof Date ) {
            return numberToString(((Date)object).getTime() );
        }
        if (object instanceof Byte || 
            object instanceof Short ||
            object instanceof Integer || 
            object instanceof Long || 
            object instanceof Float || 
            object instanceof Double) {
            return numberToString(object);            
        }        
        else {
            throw new IllegalArgumentException( "Cant store object " + object + " in field " );
        }
    }
    
     /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, allowing JSON
     * text to be delivered in HTML. In JSON text, a string cannot contain a
     * control character or an unescaped quote or backslash.
     * @param string A String
     * @return  A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char         b;
        char         c = 0;
        int          i;
        int          len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);
        String       t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                sb.append('\\');
                sb.append(c);
                break;
            case '/':
                if (b == '<') {
                    sb.append('\\');
                }
                sb.append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            default:
                if (c < ' ') {
                    t = "000" + Integer.toHexString(c);
                    sb.append("\\u" + t.substring(t.length() - 4));
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
   
    
    /**
     * Produce a string from a Number.
     * @param  n A Number
     * @return A String.
     * @throws JSONException If n is a non-finite number.
     */
    static public String numberToString(Object n) {
        if (n == null) {
            throw new IllegalArgumentException("Number must not be null");
        }
        testValidity(n);
        return trimNumber(n.toString());
    }

    /**
     * Shave off trailing zeros and decimal point, if possible.
     */
    static public String trimNumber(String s) {
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }
    
    /**
     * Throw an exception if the object is an NaN or infinite number.
     * @param o The object to test.
     * @throws JSONException If o is a non-finite number.
     */
    static void testValidity(Object o) {
        if (o != null) {
            if (o instanceof Double) {
                if (((Double)o).isInfinite() || ((Double)o).isNaN()) {
                    throw new IllegalArgumentException("JSON does not allow non-finite numbers");
                }
            } else if (o instanceof Float) {
                if (((Float)o).isInfinite() || ((Float)o).isNaN()) {
                    throw new IllegalArgumentException("JSON does not allow non-finite numbers.");
                }
            }
        }
    }
    
    static Iterator getIterator(Object object) {
        
        if (object.getClass().isArray() ) {
            return new ReflectiveArrayIterator(object);
        }        
        else if (object instanceof Iterable ) {
            return ((Iterable)object).iterator();
        }
        else {
            return null;
        }
                
    }
    
    private static class ReflectiveArrayIterator implements Iterator {

        private Object a;
        private int len;
        private int ci = 0;
        
        ReflectiveArrayIterator(Object a) {
            this.a = a;
            this.len = Array.getLength(a);
        }
        
        
        public boolean hasNext() {
            return  ci < len;
        }

        public Object next() {
            ci++;
            return Array.get(a, ci - 1);            
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
     public static Object fromJSON( Class type, Object object ) {
        
                  
        if ( object == null) {
            return null;
        }
        
        if ( ClassUtils.isSuperclass(object.getClass(), type) ) {
            return object;
        } 

        if ( type.isEnum() ) {            
            return Enum.valueOf(type, (String)object);
        }
        
        if ( type.isArray() ) {    
            List l = (List)object;
            Object a = Array.newInstance(type.getComponentType(), l.size());        
            int i = 0;
            for( Object o : l ) {
                Array.set(a, i++, fromJSON(type.getComponentType(), o));
            }
            return a;            
        }
        
        if (type.isPrimitive()) {
            type = ClassUtils.primitive2Object(type);
        }
        
        if ( type.isInstance(object) ) {
            return object; // Also handles boolean and String
        }
        else if ( object instanceof Number ) {
            Number n = (Number)object;
        
            if ( Byte.class.equals(type) ) {
                return new Byte(n.byteValue());
            }        
            else if ( Short.class.equals(type) ) {
                return new Short(n.shortValue());
            }
            else if ( Integer.class.equals(type) ) {
                return new Integer(n.intValue());
            }
            else if ( Long.class.equals(type) ) {                        
                return new Long(n.longValue());
            }
            else if ( Float.class.equals(type) ) {
                return new Float(n.floatValue());                
            }
            else if ( Date.class.equals(type) || ClassUtils.isSuperclass(type, Date.class) ) {
                return new Date( n.longValue() );
            }
            else {
                return new Double(n.doubleValue());            
            }
            // XXX add big integers and big decimals
        }
        else if (Character.class.equals(type)) {
            return Character.valueOf(((String)object).charAt(0));
        }
//        else if ( ClassUtils.isSuperinterface(type, Collection.class) ) {            
            // XXX
//            JSONArray ja = (JSONArray)jsonValue;
//            field.set();
//            result = fillArray(Collection.class.cast(result));
//        }       
        
        throw new ClassCastException( object.getClass() + " into " + type );
    } 
    
}
