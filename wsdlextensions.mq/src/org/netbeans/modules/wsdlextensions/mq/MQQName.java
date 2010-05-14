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

package org.netbeans.modules.wsdlextensions.mq;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author rchen
 */
public enum MQQName {
    ADDRESS(createMQQName("address")),
    BINDING(createMQQName("binding")),
    OPERATION(createMQQName("operation")),
    FAULT(createMQQName("fault")),
    REDELIVERY(createMQQName("redelivery")),
    BODY(createMQQName("body")),
    HEADER(createMQQName("header"));
    
   
    
    public static final String MQ_NS_URI = "http://schemas.sun.com/jbi/wsdl-extensions/mq/";
    public static final String MQ_NS_PREFIX = "mq";
    
     public static QName createMQQName(String localName){
        return new QName(MQ_NS_URI, localName, MQ_NS_PREFIX);
    }
    
    
    /** Creates a new instance of MQQName */
   MQQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (MQQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    
     private final QName qName;
}
