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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.Collections;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;

/**
 *
 * @author Petr Pisl
 */
public enum JSFConfigQNames {
    
    FACES_CONFIG("faces-config"),                   //NOI18N
    //general
    DESCRIPTION("description"),                     //NOI18N
    DISPLAY_NAME("display-name"),                   //NOI18N
    ICON("icon"),                                   //NOI18N
    SMALL_ICON("small-icon"),                       //NOI18N
    LARGE_ICON("large-icon"),                       //NOI18N
    //managed-bean
    MANAGED_BEAN("managed-bean"),                   //NOI18N
    MANAGED_BEAN_NAME("managed-bean-name"),         //NOI18N
    MANAGED_BEAN_CLASS("managed-bean-class"),       //NOI18N
    MANAGED_BEAN_SCOPE("managed-bean-scope"),       //NOI18N
    //navigation-rule
    NAVIGATION_RULE("navigation-rule"),             //NOI18N
    FROM_VIEW_ID("from-view-id"),                   //NOI18N
    //navigation-case
    NAVIGATION_CASE("navigation-case"),             //NOI18N
    FROM_OUTCOME("from-outcome"),                   //NOI18N
    FROM_ACTION("from-action"),                     //NOI18N
    TO_VIEW_ID("to-view-id"),                       //NOI18N
    REDIRECT("redirect"),                           //NOI18N
    //converter
    CONVERTER("converter"),                         //NOI18N
    CONVERTER_ID("converter-id"),                   //NOI18N
    CONVERTER_FOR_CLASS("converter-for-class"),     //NOI18N
    CONVERTER_CLASS("converter-class"),             //NOI18N
    //application
    APPLICATION("application"),                     //NOI18N
    VIEW_HANDLER("view-handler"),                   //NOI18N
    LOCALE_CONFIG("locale-config"),                 //NOI18N
    DEFAULT_LOCALE("default-locale"),               //NOI18N
    SUPPORTED_LOCALE("supported-locale"),           //NOI18N
    //resource_bundle
    RESOURCE_BUNDLE("resource-bundle"),             //NOI18N
    BASE_NAME("base-name"),                         //NOI18N
    VAR("var");                                     //NOI18N
    
    private QName qname_1_1;
    private QName qname_1_2;
    
    
    public static final String JSF_1_2_NS = "http://java.sun.com/xml/ns/javaee";
    public static final String JSF_1_1_NS = javax.xml.XMLConstants.NULL_NS_URI;
    public static final String JSFCONFIG_PREFIX = javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
    
    
    JSFConfigQNames(String localName) {
        qname_1_1 = new QName(JSF_1_1_NS, localName, JSFCONFIG_PREFIX);
        qname_1_2 = new QName(JSF_1_2_NS, localName, JSFCONFIG_PREFIX);
    }
    
    public QName getQName(JSFVersion version) {
        QName value = qname_1_1;
        if (version.equals(JSFVersion.JSF_1_2))
            value = qname_1_2;
        return value;
    }
    
    public QName getQName(String namespaceURI) {
        return new QName(namespaceURI, getLocalName(), JSFCONFIG_PREFIX);
    }

    public String getLocalName() {
        return qname_1_2.getLocalPart();
    }
    
    public String getQualifiedName(JSFVersion version) {
        String value = qname_1_1.getPrefix() + ":" + qname_1_1.getLocalPart();
        if (version.equals(JSFVersion.JSF_1_2))
            value = qname_1_2.getPrefix() + ":" + qname_1_2.getLocalPart();
        return value;
    }
    
    private static Set<QName> mappedQNames_1_1 = new HashSet<QName>();
    private static Set<QName> mappedQNames_1_2 = new HashSet<QName>();
    static {
        mappedQNames_1_1.add(FACES_CONFIG.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(MANAGED_BEAN.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(CONVERTER.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(NAVIGATION_RULE.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(NAVIGATION_CASE.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(DESCRIPTION.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(DISPLAY_NAME.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(ICON.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(APPLICATION.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(VIEW_HANDLER.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_1_1));
        
        mappedQNames_1_2.add(FACES_CONFIG.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(MANAGED_BEAN.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(CONVERTER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(NAVIGATION_RULE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(NAVIGATION_CASE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(DESCRIPTION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(DISPLAY_NAME.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(ICON.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(APPLICATION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(VIEW_HANDLER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_1_2));
    }
    
    public static Set<QName> getMappedQNames(JSFVersion version) {
        Set<QName> mappedQNames = mappedQNames_1_1;
        if (version.equals(JSFVersion.JSF_1_2))
            mappedQNames = mappedQNames_1_2;
        return Collections.unmodifiableSet(mappedQNames);
    }
    
}
