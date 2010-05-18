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

package org.netbeans.modules.wsdlextensions.ldap.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperationInput;
import org.netbeans.modules.wsdlextensions.ldap.LDAPComponent;
import org.netbeans.modules.wsdlextensions.ldap.LDAPQName;
import org.w3c.dom.Element;

/**
 * @author 
 */
public class LDAPOperationInputImpl extends LDAPComponentImpl implements LDAPOperationInput {
    
    public LDAPOperationInputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public LDAPOperationInputImpl(WSDLModel model){
        this(model, createPrefixedElement(LDAPQName.INPUT.getQName(), model));
    }
    
    public void accept(LDAPComponent.Visitor visitor) {
        visitor.visit(this);
    }

	public String getOperationType() {
        String opType = getAttribute(LDAPAttribute.LDAP_OPERATIONTYPE_PROPERTY);
        String opTypeVal = null;
        if ( opType != null ) {
            try {
                opTypeVal = opType;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return opTypeVal;
    }

	public void setOperationType(String opType) {
        setAttribute(LDAP_OPERATIONTYPE_PROPERTY, LDAPAttribute.LDAP_OPERATIONTYPE_PROPERTY, "" + opType);
    }

}
