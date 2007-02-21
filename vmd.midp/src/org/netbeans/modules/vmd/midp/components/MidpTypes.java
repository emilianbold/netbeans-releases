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
package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.openide.util.Utilities;

import java.awt.*;
import java.util.HashMap;

/**
 * @author David Kaspar
 */
public final class MidpTypes {

    public static final TypeID TYPEID_CHAR = new TypeID (TypeID.Kind.PRIMITIVE, "char"); // NOI18N
    public static final TypeID TYPEID_BYTE = new TypeID (TypeID.Kind.PRIMITIVE, "byte"); // NOI18N
    public static final TypeID TYPEID_SHORT = new TypeID (TypeID.Kind.PRIMITIVE, "short"); // NOI18N
    public static final TypeID TYPEID_INT = new TypeID (TypeID.Kind.PRIMITIVE, "int"); // NOI18N
    public static final TypeID TYPEID_LONG = new TypeID (TypeID.Kind.PRIMITIVE, "long"); // NOI18N
    public static final TypeID TYPEID_FLOAT = new TypeID (TypeID.Kind.PRIMITIVE, "float"); // NOI18N
    public static final TypeID TYPEID_DOUBLE = new TypeID (TypeID.Kind.PRIMITIVE, "double"); // NOI18N
    public static final TypeID TYPEID_JAVA_LANG_STRING = new TypeID (TypeID.Kind.PRIMITIVE, "java.lang.String"); // NOI18N
    public static final TypeID TYPEID_BOOLEAN = new TypeID (TypeID.Kind.PRIMITIVE, "boolean"); // NOI18N
    public static final TypeID TYPEID_JAVA_CODE = new TypeID (TypeID.Kind.PRIMITIVE, "#javacode"); // NOI18N

    public static final TypeID TYPEID_ALERT_TYPE = new TypeID (TypeID.Kind.ENUM, "javax.microedition.lcdui.AlertType"); // NOI18N

    public static enum AlertType  { ALARM, CONFIRMATION, ERROR, INFO, WARNING }

    private static final String PREFIX_LCDUI = "javax.microedition.lcdui.";
    private static final String PREFIX_MIDLET = "javax.microedition.midlet.";

    private static final HashMap<TypeID, Image> iconsRegister = new HashMap<TypeID, Image> ();

    public static PropertyValue createBooleanValue (boolean value) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.booleanPD, TYPEID_BOOLEAN, value);
    }

    public static PropertyValue createStringValue (String stringValue) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.stringPD, TYPEID_JAVA_LANG_STRING, stringValue);
    }

    public static PropertyValue createIntegerValue (int intValue) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.intPD, TYPEID_INT, intValue);
    }

    public static PropertyValue createLongValue (long longValue) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.longPD, TYPEID_LONG, longValue);
    }

    public static PropertyValue createFloatValue (float floatValue) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.floatPD, TYPEID_FLOAT, floatValue);
    }

    public static PropertyValue createDoubleValue (double doubleValue) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.doublePD, TYPEID_DOUBLE, doubleValue);
    }

    public static PropertyValue createCharValue (char charValue) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.charPD, TYPEID_CHAR, charValue);
    }

    public static PropertyValue createByteValue (byte byteValue) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.bytePD, TYPEID_BYTE, byteValue);
    }

    public static PropertyValue createShortValue (short shortValue) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.shortPD, TYPEID_SHORT, shortValue);
    }

    public static PropertyValue createJavaCodeValue (String javaCode) {
        return PropertyValue.createValue (MidpPrimitiveDescriptor.stringPD, TYPEID_JAVA_CODE, javaCode);
    }

    public static PropertyValue createAlertTypeValue (AlertType alertType) {
        return PropertyValue.createEnumValue (MidpEnumDescriptor.alertTypePD, TYPEID_ALERT_TYPE, alertType);
    }

    public static String getString (PropertyValue propertyValue) {
        return (String) propertyValue.getPrimitiveValue ();
    }

    public static boolean getBoolean (PropertyValue propertyValue) {
        return (Boolean) propertyValue.getPrimitiveValue ();
    }

    public static int getInteger (PropertyValue propertyValue) {
        return (Integer) propertyValue.getPrimitiveValue ();
    }

    public static long getLong (PropertyValue propertyValue) {
        return (Long) propertyValue.getPrimitiveValue ();
    }

    public static float getFloat (PropertyValue propertyValue) {
        return (Float) propertyValue.getPrimitiveValue ();
    }

    public static double getDouble (PropertyValue propertyValue) {
        return (Double) propertyValue.getPrimitiveValue ();
    }

    public static char getChar (PropertyValue propertyValue) {
        return (Character) propertyValue.getPrimitiveValue ();
    }

    public static byte getByte (PropertyValue propertyValue) {
        return (Byte) propertyValue.getPrimitiveValue ();
    }

    public static short getShort (PropertyValue propertyValue) {
        return (Short) propertyValue.getPrimitiveValue ();
    }

    public static String getJavaCode (PropertyValue propertyValue) {
        return (String) propertyValue.getPrimitiveValue ();
    }

    public static AlertType getAlertType (PropertyValue propertyValue) {
        return (AlertType) propertyValue.getPrimitiveValue ();
    }

    public static String getFQNClassName (TypeID type) {
        assert TypeID.Kind.COMPONENT.equals (type.getKind ());
        return type.getString ();
    }

    public static String getOptimalizedFQNClassName (TypeID type) {
//        assert TypeID.Kind.COMPONENT.equals (type.getKind ());
        String className = type.getString ();
        if (className.startsWith (PREFIX_LCDUI))
            return className.substring (PREFIX_LCDUI.length ());
        else if (className.startsWith (PREFIX_MIDLET))
            return className.substring (PREFIX_MIDLET.length ());
        return className;
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
