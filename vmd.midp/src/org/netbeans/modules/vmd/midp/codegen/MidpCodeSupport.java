/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.midp.codegen;

import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.CodeWriter;
import org.netbeans.modules.vmd.api.codegen.CodeSupport;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import java.util.List;

/**
 * @author David Kaspar
 */
// HINT - after making change, update MidpPrimitiveDescriptor, MidpEnumDescriptor too
public class MidpCodeSupport {

    public static void generateCodeForPropertyValue (CodeWriter writer, PropertyValue value) {
        switch (value.getKind ()) {
            case ARRAY: {
                TypeID type = value.getType ();
                int dimension = type.getDimension ();
                boolean newLines = dimension > 1;
                List<PropertyValue> array = value.getArray ();

                writer.write ("new " + MidpTypes.getOptimalizedFQNClassName (type)); // NOI18N
                for (int a = 0; a < dimension; a ++)
                    writer.write ("[]"); // NOI18N
                writer.write (" {"); // NOI18N
                if (array.size () > 0)
                    writer.write (newLines ? "\n" : " "); // NOI18N

                for (int a = 0; a < array.size (); a ++) {
                    if (a > 0)
                        writer.write (newLines ? ",\n" : ", "); // NOI18N
                    PropertyValue propertyValue = array.get (a);
                    generateCodeForPropertyValue (writer, propertyValue);
                }

                writer.write (newLines ? "}" : " }"); // NOI18N
            } break;
            case ENUM:
                generateEnumTypes (writer, value);
                break;
            case NULL:
                writer.write ("null"); // NOI18N
                break;
            case REFERENCE:
                writer.write (CodeReferencePresenter.generateAccessCode (value.getComponent ()));
                break;
            case USERCODE:
                writer.write (value.getUserCode ());
                break;
            case VALUE:
                generatePrimitiveTypes (writer, value);
                break;
            default:
                throw Debug.illegalState ();
        }
    }

    // TODO - convert to global lookup - or could be handled as a default generator, a custom could be defined by Parameter
    private static void generatePrimitiveTypes (CodeWriter writer, PropertyValue value) {
        TypeID type = value.getType ();
        if (MidpTypes.TYPEID_INT.equals (type))
            writer.write (value.getPrimitiveValue ().toString ());
        else if (MidpTypes.TYPEID_LONG.equals (type))
            writer.write (value.getPrimitiveValue ().toString () + "l"); // NOI18N
        else if (MidpTypes.TYPEID_CHAR.equals (type))
            writer.write (Integer.toString ((Character) value.getPrimitiveValue ()));
        else if (MidpTypes.TYPEID_BYTE.equals (type))
            writer.write (value.getPrimitiveValue ().toString ());
        else if (MidpTypes.TYPEID_SHORT.equals (type))
            writer.write (value.getPrimitiveValue ().toString ());
        else if (MidpTypes.TYPEID_FLOAT.equals (type))
            writer.write (value.getPrimitiveValue ().toString () + "f"); // NOI18N
        else if (MidpTypes.TYPEID_DOUBLE.equals (type))
            writer.write (value.getPrimitiveValue ().toString ());
        else if (MidpTypes.TYPEID_BOOLEAN.equals (type))
            writer.write ((Boolean) value.getPrimitiveValue () ? "true" : "false"); // NOI18N
        else if (MidpTypes.TYPEID_JAVA_LANG_STRING.equals (type))
            writer.write ("\"" + CodeSupport.encryptStringToJavaCode (value.getPrimitiveValue ().toString ()) + "\"");
        else if (MidpTypes.TYPEID_JAVA_CODE.equals (type))
            writer.write (value.getPrimitiveValue ().toString ());
        //TODO
    }

    // TODO - convert to global lookup - or could be handled as a default generator, a custom could be defined by Parameter
    private static void generateEnumTypes (CodeWriter writer, PropertyValue value) {
        TypeID type = value.getType ();
        if (MidpTypes.TYPEID_ALERT_TYPE.equals (type))
            writer.write ("AlertType." + value.getPrimitiveValue ().toString ()); // NOI18N
        //TODO
    }

}
