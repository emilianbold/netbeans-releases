/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.model.PrimitiveDescriptor;
import org.netbeans.modules.vmd.api.model.PrimitiveDescriptorFactory;

/**
 * @author David Kaspar
 */
// HINT - after making change, update MidpCodeSupport too
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.model.PrimitiveDescriptorFactory.class)
public final class MidpPrimitiveDescriptor implements PrimitiveDescriptorFactory {

    static final CharPD charPD = new CharPD ();
    static final BytePD bytePD = new BytePD ();
    static final ShortPD shortPD = new ShortPD ();
    static final IntPD intPD = new IntPD ();
    static final LongPD longPD = new LongPD ();
    static final FloatPD floatPD = new FloatPD ();
    static final DoublePD doublePD = new DoublePD ();
    static final StringPD stringPD = new StringPD ();
    static final BooleanPD booleanPD = new BooleanPD ();

    public String getProjectType () {
        return MidpDocumentSupport.PROJECT_TYPE_MIDP;
    }

    public PrimitiveDescriptor getDescriptorForTypeIDString (String string) {
        if (MidpTypes.TYPEID_CHAR.getString ().equals (string))
            return intPD;
        if (MidpTypes.TYPEID_BYTE.getString ().equals (string))
            return bytePD;
        if (MidpTypes.TYPEID_SHORT.getString ().equals (string))
            return shortPD;
        if (MidpTypes.TYPEID_INT.getString ().equals (string))
            return intPD;
        if (MidpTypes.TYPEID_LONG.getString ().equals (string))
            return longPD;
        if (MidpTypes.TYPEID_FLOAT.getString ().equals (string))
            return floatPD;
        if (MidpTypes.TYPEID_DOUBLE.getString ().equals (string))
            return doublePD;
        if (MidpTypes.TYPEID_BOOLEAN.getString ().equals (string))
            return booleanPD;
        if (MidpTypes.TYPEID_JAVA_LANG_STRING.getString ().equals (string))
            return stringPD;
        if (MidpTypes.TYPEID_JAVA_CODE.getString ().equals (string))
            return stringPD;
        //TODO
        return null;
    }

    private static class CharPD implements PrimitiveDescriptor {

        public String serialize (Object value) {
            return value.toString ();
        }

        public Object deserialize (String serialized) {
            return serialized.charAt (0);
        }

        public boolean isValidInstance (Object object) {
            return object instanceof Character;
        }

    }

    private static class BytePD implements PrimitiveDescriptor {

        public String serialize (Object value) {
            return value.toString ();
        }

        public Object deserialize (String serialized) {
            return Byte.parseByte (serialized);
        }

        public boolean isValidInstance (Object object) {
            return object instanceof Byte;
        }

    }

    private static class ShortPD implements PrimitiveDescriptor {

        public String serialize (Object value) {
            return value.toString ();
        }

        public Object deserialize (String serialized) {
            return Short.parseShort (serialized);
        }

        public boolean isValidInstance (Object object) {
            return object instanceof Short;
        }

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

    private static class FloatPD implements PrimitiveDescriptor {

        public String serialize (Object value) {
            return value.toString ();
        }

        public Object deserialize (String serialized) {
            return Float.parseFloat (serialized);
        }

        public boolean isValidInstance (Object object) {
            return object instanceof Float;
        }

    }

    private static class DoublePD implements PrimitiveDescriptor {

        public String serialize (Object value) {
            return value.toString ();
        }

        public Object deserialize (String serialized) {
            return Double.parseDouble (serialized);
        }

        public boolean isValidInstance (Object object) {
            return object instanceof Double;
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
