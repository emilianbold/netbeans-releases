/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.awt.*;
import java.beans.*;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;

import javax.jmi.reflect.JmiException;

/** Property editor for the property type property
*
* @author Martin Matula
*/
public class IdxPropertyTypeEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
    /** Current value */
    private Type type;
    
    /** Default types */
    private final String[] types = new String[] {
                                       "boolean[]", "char[]", "byte[]", "short[]", "int[]", // NOI18N
                                       "long[]", "float[]", "double[]", "String[]" // NOI18N
                                   };

    /** Creates new editor */
    public IdxPropertyTypeEditor() {
        type = null;
    }

    public String getAsText () {
        Type type = (Type) getValue();
        return (type == null) ? "" : type.getName(); // NOI18N
    }

    public void setAsText (String string) throws IllegalArgumentException {
        String normalizedInput;
        if (string == null || (normalizedInput = string.trim()).length() == 0 ||
                !normalizedInput.endsWith("[]")) { // NOI18N
            throw new IllegalArgumentException(string);
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

    public void setValue(Object v) {
        this.type = (Type) v;
    }

    public Object getValue() {
        return type;
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
