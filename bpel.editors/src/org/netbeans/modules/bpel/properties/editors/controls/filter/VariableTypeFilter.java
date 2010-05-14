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
package org.netbeans.modules.bpel.properties.editors.controls.filter;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.editors.api.Constants;

/**
 * This filter works with Variable Nodes only. It allows nodes of all other types.
 * This filter gets a type information in the constructor and filter
 * all variable nodes according to the type.
 *
 * @author nk160297
 */
public class VariableTypeFilter {

    private Constants.VariableStereotype myStereotype;
    private QName myType;
    private Collection<QName> myTypes;
    
    private boolean isShowAppropriateVarOnly = false;

    public VariableTypeFilter(Constants.VariableStereotype stereotype,
            QName type) {
        myStereotype = stereotype;
        myType = type;
    }
    
    public VariableTypeFilter(Constants.VariableStereotype stereotype,
            Collection<QName> types) {
        myStereotype = stereotype;
        myTypes = types;
    }
    
    public boolean isTypeAllowed(VariableTypeInfoProvider typeProvider) {
        //
        // If single types specified
        if (myType != null) {
            if (myStereotype != null) {
                if (myStereotype != typeProvider.getVariableStereotype()) {
                    return false;
                }
            }
            return myType.equals(typeProvider.getVariableQNameType());
        }
        //
        // If multiple types specified
        if (myTypes != null) {
            if (myStereotype != null) {
                if (myStereotype != typeProvider.getVariableStereotype()) {
                    return false;
                }
            }
            //
            QName varType = typeProvider.getVariableQNameType();
            if (varType == null) {
                return false;
            }
            //
            boolean typeFound = false;
            for (QName qName : myTypes) {
                if (qName.equals(varType)) {
                    typeFound = true;
                    break;
                }
            }
            return typeFound;
        }
        //
        return true;
    }
    
    /**
     * Indicates if it necessary to show variables which tipe doesn't correspond to required. 
     */
    public boolean isShowAppropriateVarOnly() {
        return isShowAppropriateVarOnly;
    }
    
    public void setShowAppropriateVarOnly(boolean newValue) {
        isShowAppropriateVarOnly = newValue;
    }
}
