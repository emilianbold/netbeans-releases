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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.codeviation.commons.patterns.Filter;
import org.codeviation.commons.reflect.ClassUtils;
import org.codeviation.commons.reflect.FieldUtils;

/**
 *
 * @author Petr Hrebejk
 */
public class StoreInfo {

    private Class<?> clazz;
    
    private Kind kind;
    
    private Map<String,Field> fields;
    
    private Map<Field,String> names;
        
    private String namePrefix;
    
    public StoreInfo(Class<?> clazz) {
        
        this.clazz = clazz;
        Pojson.NamePrefix np = clazz.getAnnotation(Pojson.NamePrefix.class);
        if ( np == null ) {
            namePrefix = null;
        }
        else {
            namePrefix = np.value();            
        }
        
    }
    
    public synchronized Kind getKind() {
        
        // XXX add support for ToString annotation
        
        if (kind == null) {
            if (isJsonValueType(clazz)) {
                kind = Kind.VALUE;
            }
            else if ( clazz.isArray() || ClassUtils.isSuperinterface(clazz, Iterable.class)) {
                kind = Kind.ARRAY;            
            }        
            else {
                kind = Kind.OBJECT;
            }
        }
        
        return kind;
    }

    public boolean isSkipNullValues(Field field) {
        
        if (clazz.isAnnotationPresent(Pojson.SkipNullValues.class)) {
            return true;
        }
        else {
            return field.isAnnotationPresent(Pojson.SkipNullValues.class);
        }
        
    }

    public synchronized String getPojsonFieldName(Field f) {
        
        if ( names == null ) {
            names = new  HashMap<Field, String>();
        }
        
        String name = names.get(f);
        
        if ( name != null ) {
            return name;
        }
        
        Pojson.Name na = f.getAnnotation(Pojson.Name.class);
        
        if (na == null) {
            name = f.getName();
        }
        else {
           name = na.value();
           name = name == null ? f.getName() : name;
        }
        
        if ( namePrefix != null ) {
            name = namePrefix + name; 
        }
        
        names.put(f,name);
        return name;
        
    }
    
    
    public synchronized Collection<Field> getFields() {
        
        if ( fields == null ) {
            Pojson.StopAt saa = clazz.getAnnotation(Pojson.StopAt.class);

            Class stopClass;

            if ( saa == null ) {
                stopClass = clazz;
            }
            else {
                stopClass = saa.value();
                if ( saa.value() == Pojson.StopAtCurrentClass.class ) {
                    stopClass = clazz;
                }
            }
        
            fields = FieldUtils.getAll(clazz, stopClass, new FieldFilter());
        }
                
        return fields.values();
    }
    
    
             
    public static enum Kind {
        VALUE,
        OBJECT,
        ARRAY;                
    }
   
    private static boolean isJsonValueType(Class type) {
        
        if ( type.isPrimitive() ) {
            return true;
        }
        
        if ( type.isEnum() ) {
            return true;
        }
        
        if ( Boolean.class.equals(type) ||
             Byte.class.equals(type) ||
             Short.class.equals(type) ||
             Character.class.equals(type) ||
             Integer.class.equals(type) ||
             Long.class.equals(type) ||
             Float.class.equals(type) ||
             Double.class.equals(type) ||
             Void.class.equals(type) ||
             String.class.equals(type) ) {
            return true;
        }
        
         if ( Date.class.equals(type) || ClassUtils.isSuperclass(type, Date.class)) {
            return true;
        }
    
        return false;
    }

    
    private class FieldFilter implements Filter<Field> {

        private int[] positive;
        private int[] negative;
  
        public FieldFilter() {
        
            Pojson.ModifierPositive mp = clazz.getAnnotation(Pojson.ModifierPositive.class);
            if ( mp != null ) {
                positive = mp.value();
                if (positive.length == 0) {
                    positive = null;
                }
            }
            
            Pojson.ModifierNegative mn = clazz.getAnnotation(Pojson.ModifierNegative.class);
            if ( mn != null ) {
                negative = mp.value();
                if (negative.length == 0) {
                    negative = null;
                }
            }
            
        }
        
        public boolean accept(Field field) {
            
            if (field.isAnnotationPresent(Pojson.SuppressStoring.class)) {
                return false;
            }
            
            int m = field.getModifiers();
            
            if ( positive != null ) {
                for( int p : positive ) {
                    if ( (m & p) == 0) {
                        return false;
                    }
                }
            }
            
            if ( negative != null ) {
                for( int p : negative ) {
                    if ( (m & p) != 0) {
                        return false;
                    }
                }
            }
            
            return true;
        }
        
    }
    
}
