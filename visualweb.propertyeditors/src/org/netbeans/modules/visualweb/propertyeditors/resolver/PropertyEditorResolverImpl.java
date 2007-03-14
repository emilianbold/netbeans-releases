/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
public final class PropertyEditorResolverImpl implements PropertyEditorResolver {
    
    private final static ResourceBundle bundle =
            ResourceBundle.getBundle(PropertyEditorResolverImpl.class.getName());
    
    
    public PropertyEditor getEditor(PropertyDescriptor propertyDescriptor) {
        
        if (propertyDescriptor == null)
            return null;
        PropertyEditor editor = null;
        Class propertyEditorClass = propertyDescriptor.getPropertyEditorClass();
        
        if (propertyEditorClass == null) {
            // If property descriptor does not specify a property editor, choose
            // an appropriate default based on the property type.
            Class propertyType = propertyDescriptor.getPropertyType();
            if (String.class.isAssignableFrom(propertyType))
                editor = new StringPropertyEditor();
            else if (Integer.class.isAssignableFrom(propertyType))
                editor = new IntegerPropertyEditor();
            else if (Long.class.isAssignableFrom(propertyType))
                editor = new LongPropertyEditor();
            else if (Double.class.isAssignableFrom(propertyType))
                editor = new DoublePropertyEditor();
            else if (Converter.class.isAssignableFrom(propertyType))
                editor = new ConverterPropertyEditor();
            else if (Validator.class.isAssignableFrom(propertyType))
                editor = new ValidatorPropertyEditor();
            else if (ValueBinding.class.isAssignableFrom(propertyType)
            || ValueExpression.class.isAssignableFrom(propertyType))
                editor = new ValueBindingPropertyEditor();
            else if (MethodBinding.class.isAssignableFrom(propertyType)
            || MethodExpression.class.isAssignableFrom(propertyType))
                editor = new MethodBindingPropertyEditor();
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
    
    
    private final static String[] exportedPropertyEditorPackageNames = new String[] {
        "com.sun.rave.propertyeditors",
        "com.sun.rave.propertyeditors.css",
        "com.sun.rave.propertyeditors.binding",
        "com.sun.jsfcl.std.css"
    };
    
    private static boolean isExportedPropertyEditorClass(Class propertyEditorClass) {
        for (String name : exportedPropertyEditorPackageNames) {
            if (name.equals(propertyEditorClass.getPackage().getName()))
                return true;
        }
        return false;
    }
    
}
