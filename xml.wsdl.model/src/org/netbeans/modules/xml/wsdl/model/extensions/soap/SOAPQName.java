/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
