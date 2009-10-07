/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
