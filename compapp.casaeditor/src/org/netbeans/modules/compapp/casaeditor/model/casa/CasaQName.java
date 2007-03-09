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

package org.netbeans.modules.compapp.casaeditor.model.casa;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author jqian
 */
public enum CasaQName {    
    
    CASA(createCasaQName("casa")),                                                          // NOI18N
    SERVICE_UNITS(createCasaQName("service-units")),                                        // NOI18N
    ENGINE_ENGINE_SERVICE_UNIT(createCasaQName("service-engine-service-unit")),             // NOI18N
    BINDING_COMPONENT_SERVICE_UNIT(createCasaQName("binding-component-service-unit")),      // NOI18N
    CONNECTIONS(createCasaQName("connections")),                                            // NOI18N
    CONNECTION(createCasaQName("connection")),                                              // NOI18N   
    ENDPOINTS(createCasaQName("endpoints")),                                                // NOI18N
    ENDPOINT(createCasaQName("endpoint")),                                                  // NOI18N
    CONSUMES(createCasaQName("consumes")),                                                  // NOI18N
    PROVIDES(createCasaQName("provides")),                                                  // NOI18N    
    PORTS(createCasaQName("ports")),                                                        // NOI18N
    PORT(createCasaQName("port")),                                                          // NOI18N        
    BINDINGS(createCasaQName("bindings")),                                                  // NOI18N        
    PORTTYPES(createCasaQName("porttypes")),                                                // NOI18N
    SERVICES(createCasaQName("services")),                                                  // NOI18N
    LINK(createCasaQName("link")),                                                          // NOI18N    
    REGIONS(createCasaQName("regions")),                                                    // NOI18N
    REGION(createCasaQName("region"));                                                      // NOI18N
    
    public static final String CASA_NS_URI = "http://java.sun.com/xml/ns/casa";             // NOI18N
    public static final String CASA_NS_PREFIX = "casa";                                     // NOI18N
        
    
    public static QName createCasaQName(String localName){
        return new QName(CASA_NS_URI, localName, CASA_NS_PREFIX);
    }
    
    CasaQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (CasaQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }    
    
    public String getQualifiedName() {
        return qName.getPrefix() + ":" + qName.getLocalPart();      // NOI18N
    }
    
    private final QName qName;
}
