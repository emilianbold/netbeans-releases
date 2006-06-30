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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.awt.*;
import java.beans.*;
import org.openide.ErrorManager;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.PrimitiveTypeKindEnum;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;

import javax.jmi.reflect.JmiException;
import org.openide.util.NbBundle;

/** Property editor for the property type property
*
* @author Martin Matula
*/
public class PropertyTypeEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
    
    /** Default types */
    private final String[] types = new String[] {
                                       "boolean", "char", "byte", "short", "int", // NOI18N
                                       "long", "float", "double", "String" // NOI18N
                                   };

    /** Creates new editor */
    public PropertyTypeEditor () {
    }

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText () {
        Type type = (Type) getValue();
        return (type == null) ? "" : type.getName(); // NOI18N
    }

    /**
    * Set the property value by parsing a given String.
    * @param string  The string to be parsed.
    */
    public void setAsText (String string) throws IllegalArgumentException {
        String normalizedInput;
        if (string == null || (normalizedInput = string.trim()).length() == 0) {
            throw (IllegalArgumentException) ErrorManager.getDefault().annotate(new IllegalArgumentException(string), NbBundle.getMessage(PropertyTypeEditor.class, "MSG_Not_Valid_Type"));
        }
        Type oldType = (Type) getValue();
        Type newType;
        try {
            JMIUtils.beginTrans(false);
            try {
                JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(oldType);
                newType = jmodel.getType().resolve(normalizedInput);
            } finally {
                JMIUtils.endTrans();
            }
            setValue(newType);
        } catch (JmiException e) {
            IllegalArgumentException iae = new IllegalArgumentException();
            iae.initCause(e);
            throw iae;
        }
    }

    /**
    * @param v new value
    */
    public void setValue(Object v) {
        JMIUtils.beginTrans(false);
        try {
            if (!(v instanceof Type) || JMIUtils.isPrimitiveType((Type) v, PrimitiveTypeKindEnum.VOID))
                throw new IllegalArgumentException();
        } finally {
            JMIUtils.endTrans();
        }
        super.setValue(v);
    }

    /**
    * @return A fragment of Java code representing an initializer for the
    * current value.
    */
    public String getJavaInitializationString () {
        return getAsText();
    }

    /**
    * @return The tag values for this property.
    */
    public String[] getTags () {
        return types;
    }

    /**
    * @return Returns custom property editor to be showen inside the property
    *         sheet.
    */
    public Component getInPlaceCustomEditor () {
        return null;
    }

    /**
    * @return true if this PropertyEditor provides a enhanced in-place custom
    *              property editor, false otherwise
    */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    /**
    * @return true if this property editor provides tagged values and
    * a custom strings in the choice should be accepted too, false otherwise
    */
    public boolean supportsEditingTaggedValues () {
        return true;
    }
}
