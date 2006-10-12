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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author Nam Nguyen
 * 
 * changed by
 * @author ads
 */
public enum BPELQName {
    PROPERTY(createBPQName("property")),
    PARTNER_LINK_TYPE(createPLNKQName("partnerLinkType")),
    ROLE(createPLNKQName("role")),
    PROPERTY_ALIAS(createBPQName("propertyAlias")),
    QUERY(createBPQName("query")),
    DOCUMENTATION(createPLNKQName("documentation"));
    
    public static final String BPWS_NS = "http://schemas.xmlsoap.org/ws/2004/03/business-process/";
    public static final String BPEL_PREFIX = "bpws";
    public static final String PLNK_NS = "http://schemas.xmlsoap.org/ws/2004/03/partner-link/";
    public static final String PLNK_PREFIX = "plnk";
    
    public static QName createBPQName(String localName){
        return new QName(BPWS_NS, localName, BPEL_PREFIX);
    }
    public static QName createPLNKQName(String localName){
        return new QName(PLNK_NS, localName, PLNK_PREFIX);
    }
    
    BPELQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    
    
    public static Set<QName> getQNames() {
        if (myQnames == null) {
            Set<QName> qnames = new HashSet<QName>();
            for (BPELQName bq : values()) {
                qnames.add(bq.getQName());
            }
            myQnames = qnames;
        }
        return myQnames;
    }
    
    private static Set<QName> myQnames = null;
    
    private final QName qName;
}
