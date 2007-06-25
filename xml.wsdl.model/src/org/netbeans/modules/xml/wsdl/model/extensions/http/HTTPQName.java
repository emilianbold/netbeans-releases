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
package org.netbeans.modules.xml.wsdl.model.extensions.http;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

public enum HTTPQName {
    
    ADDRESS(createHTTPQName("address")),
    BINDING(createHTTPQName("binding")),
    URLREPLACEMENT(createHTTPQName("urlReplacement")),
    URLENCODED(createHTTPQName("urlEncoded")),
    OPERATION(createHTTPQName("operation"));
    
    public static final String HTTP_NS_URI = "http://schemas.xmlsoap.org/wsdl/http/";
    public static final String HTTP_NS_PREFIX = "http";
    
    public static QName createHTTPQName(String localName){
        return new QName(HTTP_NS_URI, localName, HTTP_NS_PREFIX);
    }
    
    HTTPQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (HTTPQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    
    private static Set<QName> qnames = null;
    private final QName qName;
}
