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

package org.codeviation.commons.reflect;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.codeviation.commons.utils.StreamUtil;

/**
 *
 * @author phrebejk
 */
public class ClassUtils {

    private static Map<Class,Class> P2O = new HashMap<Class,Class>();
    static {
        P2O.put(Boolean.TYPE, Boolean.class);
        P2O.put(Byte.TYPE, Byte.class);
        P2O.put(Character.TYPE, Character.class);
        P2O.put(Short.TYPE, Short.class);
        P2O.put(Integer.TYPE, Integer.class);
        P2O.put(Long.TYPE, Long.class);
        P2O.put(Float.TYPE, Float.class);
        P2O.put(Double.TYPE, Double.class);
        P2O.put(Void.TYPE, Void.class);        
    }
    
    public static boolean isSuperinterface( Class implementor, Class iface ) {
        
        if ( !iface.isInterface() ) {
            throw new IllegalArgumentException( "iface parameter " + iface + "must be an interface!");
        } 
        
        return iface.isAssignableFrom(implementor);
    }
    
    
    public static boolean isSuperclass( Class clazz, Class superclass ) {
        
        if ( clazz == null ) {
            throw new IllegalArgumentException( "clazz parameter must not be null!");
        }
        if ( superclass == null ) {
            throw new IllegalArgumentException( "superclass parameter must not be null!");
        }
        
        if ( superclass.isInterface() ) {
            throw new IllegalArgumentException( "superclass parameter " + superclass + "must not be an interface!");
        } 
        
        return superclass.isAssignableFrom(clazz);
    }
    
    /** Returns name of the class without package in the form of
     * [OuterClassSimpleName].ClassSimpleName
     */
    public static String getDotClassName(Class clazz) {
        
        StringBuilder sb = new StringBuilder(clazz.getSimpleName());
        
        clazz = clazz.getEnclosingClass(); 
        
        while( clazz != null ) {            
            sb.insert( 0, clazz.getSimpleName() + "." );
            clazz = clazz.getEnclosingClass(); 
        }
                
        return sb.toString();
    }
    
    public static String getResourceAsString(Class clazz, String resourceName) throws IOException {
        InputStream is = clazz.getResourceAsStream(resourceName);
        return is == null ? null : StreamUtil.asString(is);
    }
    
    // XXX Should be removed
    public static Vector arrayToVector(Object array) {
        int len = Array.getLength(array);
        Vector result = new Vector(len);
        
        for( int i = 0; i <len; i++) {
            result.add(Array.get(array, i));
        }
        
        return result;
    }
    
    public static Class primitive2Object( Class clazz ) {

        if ( !clazz.isPrimitive() ) {
            throw new IllegalArgumentException( clazz + "is not primitive");
        }        
        else {
            return P2O.get(clazz);
        }
    }

    public static Field findField(Class<?> clazz, String name) {
        Field f;
        try {
            f = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), name);
            } else {
                return null;
            }
        }
        return f;
    }
}
