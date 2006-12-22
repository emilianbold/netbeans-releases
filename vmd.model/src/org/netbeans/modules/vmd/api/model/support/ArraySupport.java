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
package org.netbeans.modules.vmd.api.model.support;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author David Kaspar
 */
// TODO - merge with api.model.PropertValueSupport class
public class ArraySupport {

    public static void append (DesignComponent arrayComponent, String arrayPropertyName, DesignComponent itemToAppend) {
        PropertyValue value = arrayComponent.readProperty (arrayPropertyName);
        value = append (value, itemToAppend);
        arrayComponent.writeProperty (arrayPropertyName, value);
    }

    public static PropertyValue append (PropertyValue array, DesignComponent itemToAppend) {
        return append (array, PropertyValue.createComponentReference (itemToAppend));
    }

    public static PropertyValue append (PropertyValue array, PropertyValue itemToAppend) {
        ArrayList<PropertyValue> list = new ArrayList<PropertyValue> (array.getArray ());
        list.add (itemToAppend);
        array = PropertyValue.createArray (array.getType ().getComponentType (), list);
        return array;
    }

    public static void remove (DesignComponent arrayComponent, String arrayPropertyName, DesignComponent itemToRemove) {
        PropertyValue value = arrayComponent.readProperty (arrayPropertyName);
        value = remove (value, itemToRemove);
        arrayComponent.writeProperty (arrayPropertyName, value);
    }

    public static PropertyValue remove (PropertyValue array, DesignComponent itemToRemove) {
        ArrayList<PropertyValue> list = new ArrayList<PropertyValue> (array.getArray ());
        Iterator<PropertyValue> iterator = list.iterator ();
        while (iterator.hasNext ()) {
            PropertyValue value = iterator.next ();
            if (value.getComponent ().equals (itemToRemove))
                iterator.remove ();
        }
        array = PropertyValue.createArray (array.getType ().getComponentType (), list);
        return array;
    }

}
