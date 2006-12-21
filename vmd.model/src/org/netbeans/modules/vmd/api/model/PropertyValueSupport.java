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
package org.netbeans.modules.vmd.api.model;

import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public final class PropertyValueSupport {

    public static PropertyValue addArrayValue (PropertyValue array, PropertyValue value) {
        assert array != null  &&  value != null;
        assert array.getKind ().equals (PropertyValue.Kind.ARRAY);

        TypeID componentType = array.getType ().getComponentType ();
        assert value.isCompatible (componentType);

        ArrayList<PropertyValue> values = new ArrayList<PropertyValue> (array.getArray ());
        values.add (value);
        return PropertyValue.createArray (componentType, values);
    }

    public static PropertyValue addArrayValue (PropertyValue array, int index, PropertyValue value) {
        assert array != null && value != null;
        assert array.getKind ().equals (PropertyValue.Kind.ARRAY);

        TypeID componentType = array.getType ().getComponentType ();
        assert value.isCompatible (componentType);

        ArrayList<PropertyValue> values = new ArrayList<PropertyValue> (array.getArray ());
        values.add (index, value);
        return PropertyValue.createArray (componentType, values);
    }

    public static PropertyValue setArrayValue (PropertyValue array, int index, PropertyValue value) {
        assert array != null && value != null;
        assert array.getKind ().equals (PropertyValue.Kind.ARRAY);

        TypeID componentType = array.getType ().getComponentType ();
        assert value.isCompatible (componentType);

        ArrayList<PropertyValue> values = new ArrayList<PropertyValue> (array.getArray ());
        values.set (index, value);
        return PropertyValue.createArray (componentType, values);
    }

    public static PropertyValue removeArrayValue (PropertyValue array, PropertyValue value) {
        assert array != null  &&  value != null;
        assert array.getKind ().equals (PropertyValue.Kind.ARRAY);

        ArrayList<PropertyValue> values = new ArrayList<PropertyValue> (array.getArray ());
        values.remove (value);
        return PropertyValue.createArray (array.getType ().getComponentType (), values);
    }

    public static PropertyValue removeArrayValue (PropertyValue array, int index) {
        assert array != null;
        assert array.getKind ().equals (PropertyValue.Kind.ARRAY);

        ArrayList<PropertyValue> values = new ArrayList<PropertyValue> (array.getArray ());
        values.remove (index);
        return PropertyValue.createArray (array.getType ().getComponentType (), values);
    }

}
