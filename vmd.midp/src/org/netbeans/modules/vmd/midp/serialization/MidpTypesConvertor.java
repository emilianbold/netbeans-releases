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
 *
 */

package org.netbeans.modules.vmd.midp.serialization;

import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import javax.lang.model.element.VariableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author David Kaspar
 */
public final class MidpTypesConvertor {

    public static PropertyDescriptor createPropertyDescriptorForParameter (String name, boolean usedInConstructor, VariableElement element) {
        TypeMirror type = element.asType ();
        switch (type.getKind ()) {
            case BOOLEAN:
                return createProperty (name, usedInConstructor, MidpTypes.TYPEID_BOOLEAN, PropertyValue.createUserCode ("false")); // NOI18N
            case BYTE:
                return createProperty (name, usedInConstructor, MidpTypes.TYPEID_BYTE, PropertyValue.createUserCode ("(byte) 0")); // NOI18N
            case CHAR:
                return createProperty (name, usedInConstructor, MidpTypes.TYPEID_CHAR, PropertyValue.createUserCode ("(char) 0")); // NOI18N
            case SHORT:
                return createProperty (name, usedInConstructor, MidpTypes.TYPEID_SHORT, PropertyValue.createUserCode ("(short) 0")); // NOI18N
            case DOUBLE:
                return createProperty (name, usedInConstructor, MidpTypes.TYPEID_DOUBLE, PropertyValue.createUserCode ("0.0")); // NOI18N
            case FLOAT:
                return createProperty (name, usedInConstructor, MidpTypes.TYPEID_FLOAT, PropertyValue.createUserCode ("0.0f")); // NOI18N
            case INT:
                return createProperty (name, usedInConstructor, MidpTypes.TYPEID_INT, PropertyValue.createUserCode ("0")); // NOI18N
            case LONG:
                return createProperty (name, usedInConstructor, MidpTypes.TYPEID_LONG, PropertyValue.createUserCode ("0l")); // NOI18N
            case DECLARED:
                String className = ((TypeElement) ((DeclaredType) type).asElement ()).getQualifiedName ().toString ();
                if ("java.lang.String".equals (className)) // NOI18N
                    return new PropertyDescriptor (name, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (), true, true, Versionable.FOREVER);
                // TODO - component references like Image...
            default:
                return new PropertyDescriptor (name, MidpTypes.TYPEID_JAVA_CODE, MidpTypes.createJavaCodeValue ("null"), false, true, Versionable.FOREVER); // NOI18N
        }
    }

    private static PropertyDescriptor createProperty (String name, boolean usedInConstructor, TypeID typeID, PropertyValue defaultValue) {
        return new PropertyDescriptor (name, typeID, usedInConstructor ? defaultValue : PropertyValue.createNull (), ! usedInConstructor, true, Versionable.FOREVER);
    }

}
