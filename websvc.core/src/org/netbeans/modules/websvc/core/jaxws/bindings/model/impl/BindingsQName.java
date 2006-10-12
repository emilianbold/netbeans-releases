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
package org.netbeans.modules.websvc.core.jaxws.bindings.model.impl;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;


/**
 *
 * @author Roderico Cruz
 */
public enum BindingsQName {
    HANDLER_CHAINS(createHandlerQName("handler-chains")),
    HANDLER_CHAIN(createHandlerQName("handler-chain")),
    HANDLER(createHandlerQName("handler")),
    HANDLER_CLASS(createHandlerQName("handler-class")),
    BINDINGS(createBindingsQName("bindings"));
    
    public static final String JAVAEE_NS_URI = "http://java.sun.com/xml/ns/javaee";
    public static final String JAVAEE_NS_PREFIX = "jws";
    public static final String JAXWS_NS_URI = "http://java.sun.com/xml/ns/jaxws";
    public static final String JAXWS_NS_PREFIX = "jaxws";
    
    public static QName createHandlerQName(String localName){
        return new QName(JAVAEE_NS_URI, localName, JAVAEE_NS_PREFIX);
    }
    
    public static QName createBindingsQName(String localName){
        return new QName(JAXWS_NS_URI, localName, JAXWS_NS_PREFIX);
    }
    
    BindingsQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (BindingsQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}

