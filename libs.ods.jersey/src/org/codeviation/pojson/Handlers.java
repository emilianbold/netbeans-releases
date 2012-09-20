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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.commons.reflect.ClassUtils;
import org.codeviation.pojson.Parser.Error;

/** Parser Handlers.
 *
 * @author phrebejk
 */
class Handlers {

    public static class Generic implements Parser.Handler {

        private Stack<Info> s = new Stack<Info>();

        Generic(Info rootInfo) {
            s.push(rootInfo);
        }
        
        public void objectStart() {            
            s.push(s.peek().createInfo(Info.OBJECT, null));
        }

        public void arrayStart() {
            s.push(s.peek().createInfo(Info.ARRAY, null));
        }

        public void field(String name) {
            s.push(s.peek().createInfo(Info.FIELD, name));
        }

        public void objectEnd() {
            Info i = s.pop();            
            addValue(i.getValue());
        }

        public void arrayEnd() {
            Info i = s.pop();
            addValue(i.getValue());
        }

        public void bool(boolean value) {
            addValue(value ? Boolean.TRUE : Boolean.FALSE);
        }

        public void string(String value) {
            addValue(value);
        }

        public void nul() {
            addValue(null);
        }

        public void number(long value) {
            addValue(Long.valueOf(value));
        }

        public void number(double value) {
            addValue(Double.valueOf(value));        
        }

        public void comment(String comment) {
            // Do notning
        }

        public void lineComment(String comment) {
            // Do nothing
        }

        public void error(Error error) {
            throw new IllegalArgumentException(error.toString());
        }
        
        private void addValue(Object value) {
            Info i = s.peek();
            if (i.kind == Info.FIELD) {         // We are an object field                
                s.pop(); // Go back to object
                if (!i.isIgnore() ) {
                    s.peek().addValue(value,i.getName());
                }
            }
            else if ( i.kind == Info.ARRAY || i.kind == Info.ROOT ) {  // We are in arrey
                i.addValue(value, null);        // Just store the value
            }
            else {
                throw new IllegalStateException("Should never happen");
            }
        }
    
    }
    
    public static abstract class Info {
        
        public static final int ROOT = -1;
        public static final int ARRAY = 0;
        public static final int OBJECT = 1;
        public static final int FIELD = 2;

        
        protected int kind;
        private String name;
        private boolean ignore;
        
        public Info(int kind, String name) {
            this(kind, name, false);
        }

        public Info(int kind, String name, boolean ignore) {
            this.kind = kind;
            this.name = name;
            this.ignore = ignore;
        }

        public String getName() {
            return this.name;
        }
        
        public abstract Info createInfo(int kind, String name);
                
        public abstract void addValue(Object value, String name);
        
        public abstract Object getValue();

        public boolean isIgnore() {
            return ignore;
        }

    }
    
    static class CollectionsInfo extends Info {

        private Object root;
        private Map<String,Object> om; // Object map
        private List<Object>       al; // List for the array
                
        CollectionsInfo(int kind, String name) {
            super(kind, name);
        }
        
        @Override
        public Info createInfo(int kind, String name) {
            CollectionsInfo i = new CollectionsInfo(kind,name);
            switch( kind ) {
                case Info.OBJECT:
                    i.om = new LinkedHashMap<String, Object>();
                    break;
                case Info.ARRAY:
                    i.al = new ArrayList<Object>();
                    break;
                case Info.FIELD:
                case Info.ROOT:
                    break;                    
                default:
                    throw new IllegalStateException();
            }
            return i;
        }
        
        @Override
        public void addValue(Object value, String name) {
            switch( kind ) {
                case Info.OBJECT:
                    this.om.put(name, value);
                    break;
                case Info.ARRAY:
                    this.al.add(value);
                    break;
                case Info.ROOT:
                    this.root = value;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public Object getValue() {
            switch( kind ) {
                case Info.OBJECT:
                    return this.om;
                case Info.ARRAY:
                    return this.al;
                case Info.ROOT:
                    return root;
                default:
                    throw new IllegalStateException();
            }
        }
        
    }
     
    
    static class PojoInfo extends Info {


        private Field field;
        private Class<?> clazz;
        private Object o;        // The object
        private List<Object> al; // List for the array
                
        PojoInfo(Object object) {
            super(Info.ROOT, null);
            o = object;
            clazz = o.getClass();
            // System.out.println("Creating root" + object);
        }
        
        PojoInfo(int kind, String name) {
            super(kind, name);
        }
        
        @Override
        public Info createInfo(int kind, String name) {
            //System.out.println("CR t." + this.kind + " : " + name + " - " + kind );
            PojoInfo i = new PojoInfo(kind,name);
            switch( kind ) {
                case Info.OBJECT:
                    try {
                        if ( this.kind == ROOT) {
                            i.o = this.o;
                            i.clazz = this.o.getClass();
                        }
                        else {                            
                            i.clazz = clazz;
                            i.o = clazz.newInstance();
                        }
                    } catch (InstantiationException ex) {
                        Logger.getLogger(Handlers.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(Handlers.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Info.ARRAY:
                    if ( this.kind == ROOT) {
                        i.clazz = this.clazz.getComponentType();
                    }
                    i.al = new ArrayList<Object>();
                    break;
                case Info.FIELD:
                    i.field = ClassUtils.findField(clazz, name);
                    i.clazz = i.field.getType();
                    break;
                case Info.ROOT:
                    break;                    
                default:
                    throw new IllegalStateException();
            }
            
            if ( this.kind == Info.FIELD ) {
                Class t = field.getType();
                if (t.isArray()) {
                    i.clazz = field.getType().getComponentType();
                }
                else {
                    i.clazz = field.getType();
                }
                //System.out.println("  Setting class " + i.clazz );
            }
            
            return i;
        }
        
        @Override
        public void addValue(Object value, String name) {
            //System.out.println("aval t." + kind + " " + name + " = " + value);

//            if ( IgnoreInfo.IGNORE == value ) {
//                return;
//            }
//

            switch( kind ) {
                case Info.OBJECT:
                    try {
                        Field f = ClassUtils.findField(o.getClass(), name);
                        f.setAccessible(true);
                        f.set(o, JsonUtils.fromJSON(f.getType(), value));
                    }
                    catch (IllegalAccessException ex ) { 
                        Logger.getLogger(Handlers.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    catch (SecurityException ex) {
                        Logger.getLogger(Handlers.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Info.ARRAY:
                    this.al.add(value);
                    break;
                case Info.ROOT:
                    if ( this.clazz.isArray() ) {
                        this.o = JsonUtils.fromJSON(clazz, value);
                    }
                    else {
                        this.o = value;
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public Object getValue() {
            switch( kind ) {
                case Info.OBJECT:
                    return this.o;
                case Info.ARRAY:
                    return this.al;
                case Info.ROOT:
                    return this.o;           
                default:
                    throw new IllegalStateException();
            }
        }
    }


    static class IgnoreInfo extends Info {

        public IgnoreInfo(int kind, String name) {
            super( kind, name, true);
        }

        @Override
        public Info createInfo(int kind, String name) {
            return new IgnoreInfo(kind, null);
        }

        @Override
        public void addValue(Object value, String name) {
            // Does nothing
        }

        @Override
        public Object getValue() {
            return null;
        }

    }
}
