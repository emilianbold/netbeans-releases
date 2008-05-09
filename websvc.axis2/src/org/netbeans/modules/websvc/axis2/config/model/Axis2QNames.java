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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.websvc.axis2.config.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

public enum Axis2QNames {
    AXIS2("axis2"), //NOI18N
    SERVICE("service"), //NOI18N
    SERVICE_CLASS("service-class"), //NOI18N
    WSDL_URL("wsdl-url"), //NOI18N
    JAVA_GENERATOR("java-generator"), //NOI18N
    GENERATE_WSDL("generate-wsdl"), //NOI18N
    LIBRARIES("libraries"), //NOI18N
    LIBRARY_REF("library-ref"); //NOI18N

    public static final String AXIS2_NS = "http://www.netbeans.org/ns/axis2/1";
    
    private static Set<QName> mappedQNames = new HashSet<QName>();
    static {
        mappedQNames.add(AXIS2.getQName());
        mappedQNames.add(SERVICE.getQName());
        mappedQNames.add(SERVICE_CLASS.getQName());
        mappedQNames.add(WSDL_URL.getQName());
        mappedQNames.add(JAVA_GENERATOR.getQName());
        mappedQNames.add(GENERATE_WSDL.getQName());
        mappedQNames.add(LIBRARIES.getQName());
        mappedQNames.add(LIBRARY_REF.getQName());
    }

    private QName qname;
    
    Axis2QNames(String localName) {
        qname = new QName(AXIS2_NS, localName);
    }
    
    public QName getQName() { 
        return qname; 
    }

    public String getLocalName() { 
        return qname.getLocalPart();
    }
    
    public String getQualifiedName() {
        return qname.getPrefix() + ":" + qname.getLocalPart(); //NOI18N
    }
    
    public static Set<QName> getMappedQNames() {
        return Collections.unmodifiableSet(mappedQNames);
    }
}
