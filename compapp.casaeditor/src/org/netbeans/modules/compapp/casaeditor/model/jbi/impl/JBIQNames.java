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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;

/**
 *
 * @author jqian
 */
public enum JBIQNames {
    JBI("jbi"),                 // NOI18N
    
    SERVICES("services"),       // NOI18N
    PROVIDES("provides"),       // NOI18N
    CONSUMES("consumes"),       // NOI18N
    
    SERVICE_ASSEMBLY("service-assembly"),       // NOI18N
    SERVICE_UNIT("service-unit"),               // NOI18N
    CONNECTIONS("connections"),                 // NOI18N
    CONNECTION("connection"),                   // NOI18N
    PROVIDER("provider"),                       // NOI18N
    CONSUMER("consumer"),                       // NOI18N
    IDENTIFICATION("identification"),           // NOI18N
    TARGET("target"),                           // NOI18N
    NAME("name"),                               // NOI18N
    DESCRIPTION("description"),                 // NOI18N
    ARTIFACTS_ZIP("artifacts-zip"),             // NOI18N
    COMPONENT_NAME("component-name");           // NOI18N
    
    public static final String JBI_NS_URI = "http://java.sun.com/xml/ns/jbi";   // NOI18N
    public static final String JBI_NS_PREFIX = "jbi";                           // NOI18N
    
    private static Set<QName> mappedQNames = new HashSet<QName>();
    static {
        mappedQNames.add(JBI.getQName());
        mappedQNames.add(SERVICES.getQName());
        mappedQNames.add(PROVIDES.getQName());
        mappedQNames.add(CONSUMES.getQName());
        mappedQNames.add(SERVICE_ASSEMBLY.getQName());
        mappedQNames.add(SERVICE_UNIT.getQName());
        mappedQNames.add(CONNECTIONS.getQName());
        mappedQNames.add(CONNECTION.getQName());
        mappedQNames.add(PROVIDER.getQName());
        mappedQNames.add(CONSUMER.getQName());
        mappedQNames.add(IDENTIFICATION.getQName());
        mappedQNames.add(TARGET.getQName());
//        mappedQNames.add(NAME.getQName());
//        mappedQNames.add(DESCRIPTION.getQName());
//        mappedQNames.add(ARTIFACTS_ZIP.getQName());
//        mappedQNames.add(COMPONENT_NAME.getQName());
    }
    
    private QName qname;
    
    JBIQNames(String localName) {
        qname = new QName(JBI_NS_URI, localName, JBI_NS_PREFIX);
    }
    
    public QName getQName() {
        return qname;
    }
    
    public String getLocalName() {
        return qname.getLocalPart();
    }
    
    public String getQualifiedName() {
        return qname.getPrefix() + Constants.COLON_STRING + qname.getLocalPart();
    }
    
    public static Set<QName> getMappedQNames() {
        return Collections.unmodifiableSet(mappedQNames);
    }
}
