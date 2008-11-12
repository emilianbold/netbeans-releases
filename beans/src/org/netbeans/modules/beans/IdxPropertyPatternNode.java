/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.beans;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;

import org.openide.nodes.*;
import static org.netbeans.modules.beans.BeanUtils.*;

/** Node representing a indexed property.
* @see IdxPropertyPattern
* @author Petr Hrebejk
*/
public final class IdxPropertyPatternNode extends PropertyPatternNode  {
    /** Create a new pattern node.
    * @param pattern field element to represent
    * @param writeable <code>true</code> to be writable
    */
    public IdxPropertyPatternNode( IdxPropertyPattern pattern, boolean writeable) {
        super(pattern, writeable);
    }


//    /* Creates property set for this node */
//    protected Sheet createSheet () {
//
//        Sheet sheet = super.createSheet();
//        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
//
//        ps.put(createIndexedTypeProperty( writeable ));
//        ps.put(createIndexGetterProperty( false ));
//        ps.put(createIndexSetterProperty( false ));
//
//        return sheet;
//    }

    /** Gets the localized string name of property pattern type i.e.
     * "Indexed Property", "Property".
     */
    @Override
    String getTypeForHint() {
        return getString( "HINT_IndexedProperty" );
    }

    /** Overrides the default implementation of clone node
    */
    @Override
    public Node cloneNode() {
        return new IdxPropertyPatternNode((IdxPropertyPattern)pattern, writeable);
    }

//    /** Create a property for the indexed property type.
//      * @param canW <code>false</code> to force property to be read-only
//      * @return the property
//      */
//    protected Node.Property createIndexedTypeProperty(boolean canW) {
//        return new PatternPropertySupport(PROP_INDEXEDTYPE, Type.class, canW) {
//
//                   /** Gets the value */
//
//                   public Object getValue () {
//                       return ((IdxPropertyPattern)pattern).getIndexedType();
//                   }
//
//                   /** Sets the value */
//                   public void setValue(Object val) throws IllegalArgumentException,
//                       IllegalAccessException, InvocationTargetException {
//                       super.setValue(val);
//                       if (!(val instanceof Type))
//                           throw new IllegalArgumentException();
//
//                       try {
//                           pattern.patternAnalyser.setIgnore( true );
//                           ((IdxPropertyPattern)pattern).setIndexedType((Type)val);
//                       } catch (JmiException e) {
//                           throw new InvocationTargetException(e);
//                       } finally {
//                           pattern.patternAnalyser.setIgnore( false );
//                       }
//
//                   }
//
//                   /** Define property editor for this property. */
//                   public PropertyEditor getPropertyEditor () {
//                       return new PropertyTypeEditor();
//                   }
//               };
//    }
//
//    protected Node.Property createTypeProperty(boolean canW) {
//        return new PatternPropertySupport(PROP_TYPE, Type.class, canW) {
//
//                   /** Gets the value */
//
//                   public Object getValue () {
//                       return ((PropertyPattern)pattern).getType();
//                   }
//
//                   /** Sets the value */
//                   public void setValue(Object val) throws IllegalArgumentException,
//                       IllegalAccessException, InvocationTargetException {
//                       super.setValue(val);
//                       if (!(val instanceof Type))
//                           throw new IllegalArgumentException();
//
//
//                       try {
//                           pattern.patternAnalyser.setIgnore( true );
//                           ((PropertyPattern)pattern).setType((Type)val);
//                       } catch (JmiException e) {
//                           throw new InvocationTargetException(e);
//                       } finally {
//                           pattern.patternAnalyser.setIgnore( false );
//                       }
//
//                   }
//
//                   public PropertyEditor getPropertyEditor () {
//                       return new IdxPropertyTypeEditor();
//                   }
//               };
//    }
//
//
//    /** Create a property for the getter method.
//     * @param canW <code>false</code> to force property to be read-only
//     * @return the property
//     */
//    protected Node.Property createIndexGetterProperty(boolean canW) {
//        return new PatternPropertySupport(PROP_INDEXEDGETTER, String.class, canW) {
//
//                   public Object getValue () {
//                       Method method = ((IdxPropertyPattern) pattern).getIndexedGetterMethod();
//                       return getFormattedMethodName(method);
//                   }
//               };
//    }
//
//    /** Create a property for the getter method.
//     * @param canW <code>false</code> to force property to be read-only
//     * @return the property
//     */
//    protected Node.Property createIndexSetterProperty(boolean canW) {
//        return new PatternPropertySupport(PROP_INDEXEDSETTER, String.class, canW) {
//
//                   /** Gets the value */
//
//                   public Object getValue () {
//                       Method method = ((IdxPropertyPattern) pattern).getIndexedSetterMethod();
//                       return getFormattedMethodName(method);
//                   }
//               };
//    }

}

