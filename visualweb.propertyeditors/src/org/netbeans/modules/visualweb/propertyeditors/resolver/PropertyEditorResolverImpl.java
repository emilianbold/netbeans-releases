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
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
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

package org.netbeans.modules.visualweb.propertyeditors.resolver;

import org.netbeans.modules.visualweb.propertyeditors.*;
import com.sun.rave.propertyeditors.resolver.PropertyEditorResolver;
import org.netbeans.modules.visualweb.propertyeditors.binding.ValueBindingPropertyEditor;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.validator.Validator;

/**
 * A utility class for matching property editors defined by this module with
 * information specified in a property descriptor. This implementation is made
 * available via a service provider look-up, specified in the {@code META-INF/services}
 * directory.
 *
 * @author gjmurphy
 */
@org.openide.util.lookup.ServiceProvider(service=com.sun.rave.propertyeditors.resolver.PropertyEditorResolver.class)
public final class PropertyEditorResolverImpl implements PropertyEditorResolver {

    private static final ResourceBundle bundle = ResourceBundle.getBundle(PropertyEditorResolverImpl.class.getName());


    public PropertyEditor getEditor(PropertyDescriptor propertyDescriptor) {

        if (propertyDescriptor == null) {
            return null;
        }
        PropertyEditor editor = null;
        Class propertyEditorClass = propertyDescriptor.getPropertyEditorClass();

        if (propertyEditorClass == null) {
            // If property descriptor does not specify a property editor, choose
            // an appropriate default based on the property type.
            Class propertyType = propertyDescriptor.getPropertyType();
            if (propertyType.isPrimitive()) {
                if (propertyType == Short.TYPE) {
                    editor = new IntegerPropertyEditor();
                } else if (propertyType == Integer.TYPE) {
                    editor = new IntegerPropertyEditor();
                } else if (propertyType == Long.TYPE) {
                    editor = new LongPropertyEditor();
                } else if (propertyType == Float.TYPE) {
                    editor = new DoublePropertyEditor();
                } else if (propertyType == Double.TYPE) {
                    editor = new DoublePropertyEditor();
                }
            }
            if (String.class.isAssignableFrom(propertyType)) {
                editor = new StringPropertyEditor();
            } else if (Integer.class.isAssignableFrom(propertyType)) {
                editor = new IntegerPropertyEditor();
            } else if (Long.class.isAssignableFrom(propertyType)) {
                editor = new LongPropertyEditor();
            } else if (Double.class.isAssignableFrom(propertyType)) {
                editor = new DoublePropertyEditor();
            } else if (Converter.class.isAssignableFrom(propertyType)) {
                editor = new ConverterPropertyEditor();
            } else if (Validator.class.isAssignableFrom(propertyType)) {
                editor = new ValidatorPropertyEditor();
            } else if (ValueBinding.class.isAssignableFrom(propertyType) || ValueExpression.class.isAssignableFrom(propertyType)) {
                editor = new ValueBindingPropertyEditor();
            } else if (MethodBinding.class.isAssignableFrom(propertyType) || MethodExpression.class.isAssignableFrom(propertyType)) {
                editor = new MethodBindingPropertyEditor();
            }
        } else {
            if (isExportedPropertyEditorClass(propertyEditorClass)) {
                try {
                    String propertyEditorClassName = propertyEditorClass.getName();
                    String implClassName = bundle.getString(propertyEditorClassName);
                    if (implClassName != null) {
                        ClassLoader classLoader = PropertyEditorResolverImpl.class.getClassLoader();
                        Class implClass = classLoader.loadClass(implClassName);
                        editor = (PropertyEditor) implClass.newInstance();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (PropertyEditorBase.class.isAssignableFrom(propertyEditorClass)) {
                try {
                    editor = (PropertyEditor) propertyEditorClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return editor;
    }


    private static final String[] exportedPropertyEditorPackageNames = new String[]{"com.sun.rave.propertyeditors", "com.sun.rave.propertyeditors.css", "com.sun.rave.propertyeditors.binding", "com.sun.jsfcl.std.css"};

    private static boolean isExportedPropertyEditorClass(Class propertyEditorClass) {
        for (String name : exportedPropertyEditorPackageNames) {
            if (name.equals(propertyEditorClass.getPackage().getName())) {
                return true;
            }
        }
        return false;
    }
}
