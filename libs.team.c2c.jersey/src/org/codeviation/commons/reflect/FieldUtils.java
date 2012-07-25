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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.codeviation.commons.patterns.Filter;


/**
 *
 * @author Petr Hrebejk
 */
public class FieldUtils {

    private FieldUtils() {}
    
    /** Returns list of fields in given class, which are accepted by the filter
     * 
     * @param clazz Class to be instpected
     * @param filter Filter for accepring the field or null to accept all.
     * @return List of fields found
     */
    public static Collection<Field> getDeclared(Class clazz, Filter<Field> filter) {
        
        if ( clazz == null) {
            throw new IllegalArgumentException("Clazz parameter must not be null!" );
        }
        
        Field fields[] = clazz.getDeclaredFields();
        Collection<Field> result = new ArrayList<Field>(fields.length);
        
        for (Field field : fields) {
            if ( filter == null || filter.accept(field)) {
                result.add(field);
            }
        }
    
        return result;
    }
        
    /** Returns list of fields in given class, which are accepted by the filter
     * 
     * @param clazz Class to be instpected
     * @param stopAt Class where to stop at going down the class hierarchy (inclusive)
     * @param filter Filter for accepring the field or null to accept all.
     * @return List of fields found
     */
    public static Map<String,Field> getAll(Class clazz, Class stopAt, Filter<Field> filter) {
        if ( clazz == null) {
            throw new IllegalArgumentException("Clazz parameter must not be null!" );
        }
        
        if ( stopAt == null ) {
            stopAt = Object.class;
        }
        
        if (stopAt.isInterface() ) {
            throw new IllegalArgumentException("Stop at class " + stopAt.getName() + " must not be an interface");
        }
        
        if (!ClassUtils.isSuperclass(clazz, stopAt) && !clazz.equals(stopAt)) {
            throw new IllegalArgumentException("Stop At class class " + stopAt.getName() + " is not superclass of " + clazz.getName());
        }
        
        HashMap<String,Field> name2Field = new LinkedHashMap<String, Field>();
        
        for( boolean end = false; !end; clazz = clazz.getSuperclass() ) {
        
            for( Field field : clazz.getDeclaredFields() ) {
                if ( name2Field.get(field.getName()) == null &&
                     ( filter == null || filter.accept(field) ) ) {                
                    name2Field.put(field.getName(), field);
                }
            }
            
            if (clazz.equals(stopAt)) {
                end = true;
            }
        }
               
        return name2Field;
    }
            
    /** Creates a filter which accepts all fields with the specified modifires.
     * 
     * @param modifier Modifiers to accept
     * @return Filter for fields
     */
    public static Filter<Field> modifierFilterPositive(int... modifier) {
        return new FieldModifierFilter(true, modifier);
    }
    
    /** Creates a filter which skips all fields with the specified modifires.
     * 
     * @param modifier Modifiers to skip
     * @return Filter for fields
     */
    public static Filter<Field> modifierFilterNegative(int... modifier) {
        return new FieldModifierFilter(false, modifier);
    }

    // Private Members ---------------------------------------------------------
    
    private static class FieldModifierFilter implements Filter<Field> {

        private boolean isPositive;
        private int modifs;
        
        public FieldModifierFilter(boolean isPositive, int... modifier) {
            this.isPositive = isPositive;
            for (int m : modifier) {
                this.modifs |= m;
            }
        }

        public boolean accept(Field field) {                     
            return isPositive ? (field.getModifiers() & modifs ) == modifs : 
                                (field.getModifiers() & modifs ) == 0 ; 
        }
            
    }
        
    
}
