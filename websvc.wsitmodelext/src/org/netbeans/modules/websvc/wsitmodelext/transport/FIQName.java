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
package org.netbeans.modules.websvc.wsitmodelext.transport;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum FIQName {
    OPTIMIZEDFASTINFOSETSERIALIZATION(createFIQName("OptimizedFastInfosetSerialization")), //NOI18N
    AUTOMATICALLYSELECTFASTINFOSET(createFIClientQName("AutomaticallySelectOptimalEncoding")); //NOI18N

    public static final String FI_NS_URI = "http://java.sun.com/xml/ns/wsit/2006/09/policy/fastinfoset/service";    //NOI18N
    public static final String FI_CLIENT_NS_URI = "http://java.sun.com/xml/ns/wsit/2006/09/policy/encoding/client";    //NOI18N
    public static final String FI_NS_PREFIX = "fi";       //NOI18N
    
    public static QName createFIQName(String localName){
        return new QName(FI_NS_URI, localName, FI_NS_PREFIX);
    }

    public static QName createFIClientQName(String localName){
        return new QName(FI_CLIENT_NS_URI, localName, FI_NS_PREFIX);
    }
    
    FIQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (FIQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
