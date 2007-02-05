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
package org.netbeans.modules.vmd.api.model.common;

import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.openide.util.Utilities;

import java.awt.*;
import java.util.HashMap;

/**
 * @author David Kaspar
 */

public final class TypesSupport {

    public static final TypeID TYPEID_INT = new TypeID (TypeID.Kind.PRIMITIVE, "int"); // NOI18N
    public static final TypeID TYPEID_LONG = new TypeID (TypeID.Kind.PRIMITIVE, "long"); // NOI18N
    public static final TypeID TYPEID_JAVA_LANG_STRING = new TypeID (TypeID.Kind.PRIMITIVE, "java.lang.String"); // NOI18N
    public static final TypeID TYPEID_BOOLEAN = new TypeID (TypeID.Kind.PRIMITIVE, "boolean"); // NOI18N
    public static final TypeID TYPEID_JAVA_CODE = new TypeID (TypeID.Kind.PRIMITIVE, "#javacode"); // NOI18N
    public static final TypeID TYPEID_ALERT_TYPE = new TypeID (TypeID.Kind.ENUM, "javax.microedition.lcdui.AlertType"); // NOI18N
    public static enum AlertType  { ALARM, CONFIRMATION, ERROR, INFO, WARNING }
    private static final HashMap<TypeID, Image> iconsRegister = new HashMap<TypeID, Image> ();

    public static PropertyValue createBooleanValue (boolean value) {
        return PropertyValue.createValue (PrimitiveDescriptorSupport.booleanPD, TYPEID_BOOLEAN, value);
    }

    public static PropertyValue createStringValue (String stringValue) {
        return PropertyValue.createValue (PrimitiveDescriptorSupport.stringPD, TYPEID_JAVA_LANG_STRING, stringValue);
    }

    public static PropertyValue createIntegerValue (int intValue) {
        return PropertyValue.createValue (PrimitiveDescriptorSupport.intPD, TYPEID_INT, intValue);
    }

    public static PropertyValue createLongValue (long longValue) {
        return PropertyValue.createValue (PrimitiveDescriptorSupport.longPD, TYPEID_LONG, longValue);
    }

    public static PropertyValue createJavaCodeValue (String javaCode) {
        return PropertyValue.createValue (PrimitiveDescriptorSupport.stringPD, TYPEID_JAVA_CODE, javaCode);
    }

    public static String getString (PropertyValue propertyValue) {
        return (String) propertyValue.getPrimitiveValue();
    }

    public static boolean getBoolean (PropertyValue propertyValue) {
        return (Boolean) propertyValue.getPrimitiveValue();
    }

    public static int getInteger (PropertyValue propertyValue) {
        return (Integer) propertyValue.getPrimitiveValue();
    }

    public static long getLong (PropertyValue propertyValue) {
        return (Long) propertyValue.getPrimitiveValue();
    }

    public static String getJavaCode (PropertyValue propertyValue) {
        return (String) propertyValue.getPrimitiveValue();
    }

    public static String getSimpleClassName (TypeID type) {
        assert TypeID.Kind.COMPONENT.equals (type.getKind ());
        String string = type.getString ();
        int i = string.lastIndexOf ('.');
        return i >= 0 ? string.substring (i + 1) : string;
    }

    public static void registerIconResource (TypeID typeID, String iconResource) {
        if (iconResource == null)
            return;
        iconsRegister.put (typeID, Utilities.loadImage (iconResource));
    }

    public static void registerIcon (TypeID typeID, Image icon) {
        iconsRegister.put (typeID, icon);
    }

    public static Image getRegisteredIcon (TypeID typeID) {
        return iconsRegister.get (typeID);
    }

}
