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

package org.netbeans.modules.wsdlextensions.snmp.impl;

import org.netbeans.modules.wsdlextensions.snmp.SNMPQName;
import org.netbeans.modules.wsdlextensions.snmp.SNMPOperation;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;


public class SNMPOperationImpl extends SNMPComponentImpl implements SNMPOperation {

    public SNMPOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SNMPOperationImpl(WSDLModel model){
        this(model, createPrefixedElement(SNMPQName.OPERATION.getQName(), model));
    }
    
    public String getType() {
        return getAttribute(SNMPAttribute.SNMP_OPERATION_TYPE);
    }

    public void setType(String val) {
        setAttribute(SNMPOperation.ATTR_TYPE,
                SNMPAttribute.SNMP_OPERATION_TYPE,
                val);
    }
    
    public String getMofId() {
        return getAttribute(SNMPAttribute.SNMP_OPERATION_MOF_ID);
    }
    
    public void setMofId(String val) {
        setAttribute(SNMPOperation.ATTR_MOF_ID, 
                     SNMPAttribute.SNMP_OPERATION_MOF_ID,
                     val);        
    }

    public String getAdaptationId() {
        return getAttribute(SNMPAttribute.SNMP_OPERATION_ADAPTATION_ID);        
    }
    
    public void setAdaptationId(String val) {
        setAttribute(SNMPOperation.ATTR_ADAPTATION_ID, 
                     SNMPAttribute.SNMP_OPERATION_ADAPTATION_ID,
                     val);        
    }

    public String getMofIdRef() {
        return getAttribute(SNMPAttribute.SNMP_OPERATION_MOF_ID_REF);
    }

    public void setMofIdRef(String val) {
        setAttribute(SNMPOperation.ATTR_MOF_ID_REF, 
                     SNMPAttribute.SNMP_OPERATION_MOF_ID_REF,
                     val);
    }
    
}
