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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.websvc.wsitmodelext.addressing;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum AddressingQName {
    ENDPOINTREFERENCE(createAddressingQName("EndpointReference")),              //NOI18N
    ADDRESS(createAddressingQName("Address")),                                  //NOI18N
    REFERENCEPROPERTIES(createAddressingQName("ReferenceProperties")),          //NOI18N
    REFERENCEPARAMETERS(createAddressingQName("ReferenceParameters")),          //NOI18N
    SERVICENAME(createAddressingQName("ServiceName")),                          //NOI18N
    PORTTYPE(createAddressingQName("PortType"));                                //NOI18N

    public static final String ADDRESSING_NS_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing/policy";  //NOI18N
    public static final String ADDRESSING_NS_PREFIX = "wsa";                                           //NOI18N
    
    public static QName createAddressingQName(String localName){
        return new QName(ADDRESSING_NS_URI, localName, ADDRESSING_NS_PREFIX);
    }
    
    AddressingQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (AddressingQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
