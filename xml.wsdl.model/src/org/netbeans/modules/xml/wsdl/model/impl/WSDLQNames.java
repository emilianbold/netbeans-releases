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

/*
 * WSDLQNames.java
 *
 * Created on November 17, 2005, 6:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author rico
 */
public enum WSDLQNames {
    BINDING(createWSDLQName("binding")),
    DEFINITIONS(createWSDLQName("definitions")),
    DOCUMENTATION(createWSDLQName("documentation")),
    FAULT(createWSDLQName("fault")),
    IMPORT(createWSDLQName("import")),
    INPUT(createWSDLQName("input")),
    MESSAGE(createWSDLQName("message")),
    OPERATION(createWSDLQName("operation")),
    OUTPUT(createWSDLQName("output")),
    PART(createWSDLQName("part")),
    PORT(createWSDLQName("port")),
    PORTTYPE(createWSDLQName("portType")),
    SERVICE(createWSDLQName("service")),
    TYPES(createWSDLQName("types"));
    
    public static final String WSDL_NS_URI = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_PREFIX = "wsdl";
    
    public static QName createWSDLQName(String localName){
        return new QName(WSDL_NS_URI, localName, WSDL_PREFIX);
    }
    
    WSDLQNames(QName name) {
        qName = name;
    }
    
    QName getQName(){
        return qName;
    }
    
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (WSDLQNames wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    
    private final QName qName;
}
