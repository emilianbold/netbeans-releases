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

package org.netbeans.modules.xml.wsdl.model.extensions.soap;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author Nam Nguyen
 */
public enum SOAPQName {
    
    ADDRESS(createSOAPQName("address")),
    BINDING(createSOAPQName("binding")),
    BODY(createSOAPQName("body")),
    FAULT(createSOAPQName("fault")),
    HEADER(createSOAPQName("header")),
    HEADER_FAULT(createSOAPQName("headerfault")),
    OPERATION(createSOAPQName("operation"));
    
    public static final String SOAP_NS_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String SOAP_NS_PREFIX = "soap";
    
    public static QName createSOAPQName(String localName){
        return new QName(SOAP_NS_URI, localName, SOAP_NS_PREFIX);
    }
    
    SOAPQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (SOAPQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    
    private final QName qName;
}
