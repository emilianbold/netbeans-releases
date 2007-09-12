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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.bpel.properties.editors.controls.filter;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.properties.Constants;

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
