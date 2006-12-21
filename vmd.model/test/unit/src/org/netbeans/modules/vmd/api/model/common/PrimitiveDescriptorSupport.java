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

import org.netbeans.modules.vmd.api.model.PrimitiveDescriptor;
import org.netbeans.modules.vmd.api.model.PrimitiveDescriptorFactory;

/**
 * @author David Kaspar
 */

public final class PrimitiveDescriptorSupport implements PrimitiveDescriptorFactory {

    static final IntPD intPD = new IntPD ();
    static final LongPD longPD = new LongPD ();
    static final StringPD stringPD = new StringPD ();
    static final BooleanPD booleanPD = new BooleanPD ();
    
    private static String PROJECT_TYPE = "vmd-midp"; // NOI18N
    

    public String getProjectType () {
        return PROJECT_TYPE;
    }

    public PrimitiveDescriptor getDescriptorForTypeIDString (String string) {
        if (TypesSupport.TYPEID_INT.getString ().equals (string))
            return intPD;
        if (TypesSupport.TYPEID_LONG.getString ().equals (string))
            return longPD;
        if (TypesSupport.TYPEID_BOOLEAN.getString ().equals (string))
            return booleanPD;
        if (TypesSupport.TYPEID_JAVA_LANG_STRING.getString ().equals (string))
            return stringPD;
        if (TypesSupport.TYPEID_JAVA_CODE.getString ().equals (string))
            return stringPD;
        //TODO
        return null;
    }

    private static class IntPD implements PrimitiveDescriptor {

        public String serialize (Object value) {
            return value.toString ();
        }

        public Object deserialize (String serialized) {
            return Integer.parseInt (serialized);
        }

        public boolean isValidInstance (Object object) {
            return object instanceof Integer;
        }

    }

    private static class LongPD implements PrimitiveDescriptor {

        public String serialize (Object value) {
            return value.toString ();
        }

        public Object deserialize (String serialized) {
            return Long.parseLong (serialized);
        }

        public boolean isValidInstance (Object object) {
            return object instanceof Long;
        }

    }

    private static class BooleanPD implements PrimitiveDescriptor {

        public String serialize (Object value) {
            return value.toString ();
        }

        public Object deserialize (String serialized) {
            return Boolean.parseBoolean (serialized);
        }

        public boolean isValidInstance (Object object) {
            return object instanceof Boolean;
        }

    }

    private static class StringPD implements PrimitiveDescriptor {

        public String serialize (Object value) {
            return (String) value;
        }

        public Object deserialize (String serialized) {
            return serialized;
        }

        public boolean isValidInstance (Object object) {
            return object instanceof String;
        }

    }

}
