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
    
    CASA(createCasaQName("casa")),    
    SERVICE_UNITS(createCasaQName("service-units")),    
    ENGINE_ENGINE_SERVICE_UNIT(createCasaQName("service-engine-service-unit")),
    BINDING_COMPONENT_SERVICE_UNIT(createCasaQName("binding-component-service-unit")),
    CONNECTIONS(createCasaQName("connections")),
    CONNECTION(createCasaQName("connection")),
    ENDPOINTS(createCasaQName("endpoints")),
    ENDPOINT(createCasaQName("endpoint")),
    CONSUMES(createCasaQName("consumes")),
    PROVIDES(createCasaQName("provides")),
    PORTS(createCasaQName("ports")),
    PORT(createCasaQName("port")),
    BINDINGS(createCasaQName("bindings")),
    PORTTYPES(createCasaQName("porttypes")),
    SERVICES(createCasaQName("services")),
    LINK(createCasaQName("link")),
    REGIONS(createCasaQName("regions")),
    REGION(createCasaQName("region"));
    
    public static final String CASA_NS_URI = "http://java.sun.com/xml/ns/casa";
    public static final String CASA_NS_PREFIX = "casa";
        
    
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
        return qName.getPrefix() + ":" + qName.getLocalPart();
    }
    
    private final QName qName;
}
