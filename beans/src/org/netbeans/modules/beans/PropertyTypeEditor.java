/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.awt.*;
import java.beans.*;

import org.openide.src.Type;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/** Property editor for the property type property
*
* @author Martin Matula
*/
public class PropertyTypeEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
    /** Current value */
    private Type type;
    
    /** Default types */
    private final String[] types = new String[] {
                                       "boolean", "char", "byte", "short", "int", // NOI18N
                                       "long", "float", "double", "String" // NOI18N
                                   };

    /** Creates new editor */
    public PropertyTypeEditor () {
        type = null;
    }

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText () {
        return (type == null) ? "" : type.toString(); // NOI18N
    }

    /**
    * Set the property value by parsing a given String.
    * @param text  The string to be parsed.
    */
    public void setAsText (String string) throws IllegalArgumentException {
        if (string.equals("void"))
            throw new IllegalArgumentException();
        type = Type.parse(string);
    }

    /**
    * @param v new value
    */
    public void setValue(Object v) {
        if (!(v instanceof Type) || Type.VOID.equals(v))
            throw new IllegalArgumentException();
        type = (Type) v;
    }

    /**
    * @return value
    */
    public Object getValue() {
        return type;
    }

    /**
    * @return A fragment of Java code representing an initializer for the
    * current value.
    */
    public String getJavaInitializationString () {
        return (type == null) ? "" : type.toString(); // NOI18N
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
