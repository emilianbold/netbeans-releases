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

package org.netbeans.modules.wsdleditorapi.generator;

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
    PROPERTY(createVarpropQName("property")),
    PARTNER_LINK_TYPE(createPLNKQName("partnerLinkType")),
    ROLE(createPLNKQName("role")),
    PROPERTY_ALIAS(createVarpropQName("propertyAlias")),
    QUERY(createVarpropQName("query")),
    DOCUMENTATION_PLNK(createPLNKQName("documentation")),
    DOCUMENTATION_VARPROP(createVarpropQName("documentation"))
    ;
    
    public static final String VARPROP_NS = //"http://schemas.xmlsoap.org/ws/2004/03/business-process/";
        "http://docs.oasis-open.org/wsbpel/2.0/varprop";        // NOI18N
    public static final String VPROP_PREFIX = "vprop";
    public static final String PLNK_NS = //"http://schemas.xmlsoap.org/ws/2004/03/partner-link/";
        "http://docs.oasis-open.org/wsbpel/2.0/plnktype";       // NOI18N
    public static final String PLNK_PREFIX = "plnk";
    
    public static QName createVarpropQName(String localName){
        return new QName(VARPROP_NS, localName, VPROP_PREFIX);
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
