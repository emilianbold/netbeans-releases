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

package org.codeviation.commons.patterns;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Miscelaneous useful factories
 *
 * @author Petr Hrebejk
 */
public class Factories {

    public static final Factory<String,Object> TO_STRING = new ToString();
    public static final Factory<String[],Object[]> TO_STRING_ARRAY = array( TO_STRING );
        
    private Factories() {
    }

    public static <T,P extends T> Factory<T,P> defaultValue(T defaultValue) {
        return new Default<T,P>(defaultValue);
    }

    public static <T, P> Factory<T[], P[]> array(Factory<T, P> elementFactory, Class... productClass) {
        return new ArrayFactory<T, P>(elementFactory, null, ArrayFactory.getProductClass(productClass));
    }

    public static <T, P> Factory<T[], P[]> array(Factory<T, P> elementFactory, Filter<P> filter, Class... productClass) {
        return new ArrayFactory<T, P>(elementFactory, filter, ArrayFactory.getProductClass(productClass));
    }

    public static <T, P> Factory<T, P> fromMap(Map<P, T> map) {
        return new MapFactory<T, P>(map);
    }

    public static <OT, IT, IP> Factory<OT, IP> chain(Factory<OT, IT> outer, Factory<IT, IP> inner) {
        return new Chain<OT, IT, IP>(outer, inner);
    }

    public static <T, P> Factory<T, P> layered(Factory<T, P>... factories) {
        return new LayeredFactory<T, P>(factories);
    }
    
    public static <T, P> Factory<T, P> layered(Iterable<Factory<T, P>> factories) {
        return new LayeredFactory<T, P>(factories);
    }

    
    public static <T, P> Factory<T, P> field(Class<P> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            return new MemberFactory<T, P>(field);
        } catch (NoSuchFieldException ex) {
            throw new IllegalArgumentException("No such field.", ex);
        } catch (SecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    
    public static <T,P,I> Factory<T,I> method( P object, Class<T> productType, Class<I> paramType, String methodName ) {
        try {
            Method method = object.getClass().getMethod(methodName, paramType);
            return new MemberFactory<T,I>(object, method);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("No such method.", ex);
        } catch (SecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    public static <T,P> Factory<T, P> method(Class<P> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName);
            return new MemberFactory<T, P>(method);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("No such method.", ex);
        } catch (SecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static <T, P> Factory<T, P> method(Class<P> parameterClass, Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName, parameterClass);
            return new MemberFactory<T, P>(method);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("No such method.", ex);
        } catch (SecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Factory<T,T> noOp() {
        return (Factory<T,T>)NoOp.INSTACE;
    }

    // Private members ---------------------------------------------------------

    private static class ArrayFactory<T, P> implements Factory<T[], P[]> {

        private Factory<T, P> elementFactory;
        private Filter<P> filter;
        private Class productClass;
    
        public ArrayFactory(Factory<T, P> elementFactory, Filter<P> filter, Class productClass) {
            this.filter = filter;
            this.elementFactory = elementFactory;

            if ( productClass == null ) {
                Class clazz = elementFactory.getClass();
                Type[] gis = clazz.getGenericInterfaces();
                for (Type t : gis) {
                    if (t instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) t;
                        if (pt.getRawType() == Factory.class ) {
                            Type[] tvs = ((ParameterizedType)t).getActualTypeArguments();
                            if ( tvs[0] instanceof Class ) {
                                productClass = (Class)tvs[0];
                            }
                            else {
                                productClass = null;
                            }
                        }
                    }
                }
            }
            else {
                this.productClass = productClass;
            }
            
            
        }

        public T[] create(P[] param) {
            if (filter == null) {
                return createNoFilter(param);
            } else {
                return createFilter(param);
            }
        }

        private T[] createNoFilter(P[] param) {

            if (param == null) {
                return null;
            }

            @SuppressWarnings("unchecked") T[] result = (T[]) java.lang.reflect.Array.newInstance(productClass, param.length);

            for (int i = 0; i < param.length; i++) {
                result[i] = elementFactory.create(param[i]);

            }

            return result;
        }

        private T[] createFilter(P[] param) {

            if (param == null) {
                return null;
            }

            ArrayList<T> al = new ArrayList<T>(param.length);

            for (P p : param) {
                if (filter.accept(p)) {
                    al.add(elementFactory.create(p));
                }
            }

            @SuppressWarnings("unchecked") T[] a = (T[]) java.lang.reflect.Array.newInstance(productClass, al.size());

            return al.toArray(a);
        }
        
        private static Class getProductClass(Class[] classes) {
            if ( classes == null || classes.length < 1) {
                return null;
            }
            else {
                return classes[0];
            }
        }
    }

    private static class Chain<OT, IT, IP> implements Factory<OT, IP> {

        Factory<OT, IT> outer;
        Factory<IT, IP> inner;

        public Chain(Factory<OT, IT> outer, Factory<IT, IP> inner) {
            this.outer = outer;
            this.inner = inner;
        }

        public OT create(IP param) {
            IT t1 = inner.create(param);
            return outer.create(t1);
        }
    }

    private static class MapFactory<T, P> implements Factory<T, P> {

        private Map<P, T> map;

        public MapFactory(Map<P, T> map) {
            this.map = map;
        }

        public T create(P param) {
            return map.get(param);
        }
    }

    private static class MemberFactory<T, P> implements Factory<T, P> {

        Object object;
        Member member;
        boolean isStatic;

        public MemberFactory(Field field) {
            this.member = field;
            this.isStatic = (member.getModifiers() & Modifier.STATIC) > 0;
        }

        public MemberFactory(Method method) {
            // XXX check for number of arguments
            this.member = method;
            this.isStatic = (member.getModifiers() & Modifier.STATIC) > 0;
        }
        
        public MemberFactory(Object object, Method method) {
            this(method);
            this.object = object;
        }

        @SuppressWarnings("unchecked")
        public T create(P param) {
            try {
                if (member instanceof Field) {
                    return (T) ((Field) member).get(param);
                } else {
                    if (isStatic) {
                        ((Method) member).setAccessible(true);
                        return (T) ((Method) member).invoke(null, param);
                    }
                    else if (object != null ) {
                        ((Method) member).setAccessible(true);
                        return (T) ((Method) member).invoke(object, param);
                    } 
                    else {
                        ((Method) member).setAccessible(true);
                        return (T) ((Method) member).invoke(param);
                    }
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(ex);
            } catch (InvocationTargetException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    private static class LayeredFactory<T, P> implements Factory<T, P> {

        List<Factory<T,P>> factories;

        public LayeredFactory(Factory<T, P> factories[]) {
            this.factories = new ArrayList<Factory<T,P>>(Arrays.asList(factories));
        }
        
        public LayeredFactory(Iterable<Factory<T,P>> factories) {
            this.factories = new ArrayList<Factory<T, P>>();
            
            for (Factory<T, P> factory : factories) {
                this.factories.add(factory);
            }
        }

        public T create(P object) {
            for (Factory<T, P> factory : factories) {
                T t = factory.create(object);
                if (t != null) {
                    return t;
                }
            }
            return null;
        }
    }
    
    
    
    private static class NoOp<T> implements Factory<T,T> {

        private static NoOp INSTACE = new NoOp();
        
        public T create(T param) {
            return param;
        }
        
    }
    
    private static class ToString implements Factory<String,Object> {

        private static NoOp INSTACE = new NoOp();
                
        public String create(Object param) {
            return param.toString();
        }
        
    }

    private static class Default<T,P extends T> implements Factory<T, P> {

        private T dflt;

        public Default(T dflt) {
            this.dflt = dflt;
        }

        public T create(P param) {
            return param == null ? dflt : param;
        }

    }
}
