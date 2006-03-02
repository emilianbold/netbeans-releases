/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
