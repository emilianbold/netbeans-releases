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

import org.netbeans.modules.xml.xam.dom.Attribute;

import org.netbeans.modules.wsdlextensions.snmp.SNMPAddress;
import org.netbeans.modules.wsdlextensions.snmp.SNMPMessage;
import org.netbeans.modules.wsdlextensions.snmp.SNMPOperation;


public enum SNMPAttribute implements Attribute {
    SNMP_ADDRESS_PORT(SNMPAddress.ATTR_PORT),
 
    SNMP_OPERATION_TYPE(SNMPOperation.ATTR_TYPE),
    SNMP_OPERATION_MOF_ID(SNMPOperation.ATTR_MOF_ID),
    SNMP_OPERATION_ADAPTATION_ID(SNMPOperation.ATTR_ADAPTATION_ID),
    SNMP_OPERATION_MOF_ID_REF(SNMPOperation.ATTR_MOF_ID_REF),
    SNMP_MESSAGE_TRAP_TYPE(SNMPMessage.ATTR_TRAPPART);
    
    private String name;

    private Class type;
    private Class subtype;
    
    SNMPAttribute(String name) {
        this(name, String.class);
    }
    
    SNMPAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    SNMPAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { return name; }
    
    public Class getType() {
        return type;
    }
    
    public String getName() { return name; }
    
    public Class getMemberType() { return subtype; }
}
