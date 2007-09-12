/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
