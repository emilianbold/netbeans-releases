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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.editors.api;

import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.openide.util.NbBundle;

/**
 * @author supernikita
 */
public interface Constants {

    enum VariableStereotype {
        GLOBAL_TYPE,
        GLOBAL_SIMPLE_TYPE,
        GLOBAL_COMPLEX_TYPE,
        PRIMITIVE_TYPE,
        MESSAGE,
        GLOBAL_ELEMENT;
        
        private String myDisplayName;
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(
                        Constants.class, this.toString());
            }
            return myDisplayName;
        }
        
        public static VariableStereotype recognizeStereotype(Object type) {
//System.out.println("recognizeStereotype: " + type);
            if (type == null) {
                return null;
            }
            if (type instanceof Message) {
                return VariableStereotype.MESSAGE;
            }
            else if (type instanceof GlobalType) {
                if (type instanceof GlobalSimpleType) {
                    if (((GlobalSimpleType)type).getModel() == 
                            SchemaModelFactory.getDefault().
                            getPrimitiveTypesModel()) {
                        return PRIMITIVE_TYPE;
                    } else {
                        return GLOBAL_SIMPLE_TYPE;
                    }
                } else if (type instanceof GlobalComplexType) {
                    return GLOBAL_COMPLEX_TYPE;
                } else {
                    return VariableStereotype.GLOBAL_TYPE;
                }
            } else if (type instanceof GlobalElement) {
                return VariableStereotype.GLOBAL_ELEMENT;
            }
            assert false : "type paramether can be one of the following types" +
                    "(or subtype of them): Message, Part, GlobalType, GlobalElement";
            return null;
        }
    }
}
