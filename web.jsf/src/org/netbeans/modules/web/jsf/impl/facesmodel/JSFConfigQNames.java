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

package org.netbeans.modules.web.jsf.impl.facesmodel;

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
    
    FACES_CONFIG("faces-config"),
    //general
    DESCRIPTION("description"),
    DISPLAY_NAME("display-name"),
    ICON("icon"),
    //managed-bean
    MANAGED_BEAN("managed-bean"),
    MANAGED_BEAN_NAME("managed-bean-name"),
    MANAGED_BEAN_CLASS("managed-bean-class"),
    MANAGED_BEAN_SCOPE("managed-bean-scope"),
    //navigation-rule
    NAVIGATION_RULE("navigation-rule"),
    FROM_VIEW_ID("from-view-id"),
    //navigation-case
    NAVIGATION_CASE("navigation-case"),
    FROM_OUTCOME("from-outcome"),
    FROM_ACTION("from-action"),
    TO_VIEW_ID("to-view-id"),
    REDIRECT("redirect"),
    //converter
    CONVERTER("converter"),
    CONVERTER_ID("converter-id"),
    CONVERTER_FOR_CLASS("converter-for-class"),
    CONVERTER_CLASS("converter-class");
    
    
//    private static Set<QName> mappedQNames = new HashSet<QName>();
//    static {
//        mappedQNames.add(FACES_CONFIG.getQName());
//        mappedQNames.add(MANAGED_BEAN.getQName());
//    }
    
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
    
    public String getLocalName() {
        return qname_1_2.getLocalPart();
    }
    
    public String getQualifiedName(JSFVersion version) {
        String value = qname_1_1.getPrefix() + ":" + qname_1_1.getLocalPart();
        if (version.equals(JSFVersion.JSF_1_2))
            value = qname_1_2.getPrefix() + ":" + qname_1_2.getLocalPart();
        return value;
    }
    
//    public static Set<QName> getMappedQNames() {
//        return Collections.unmodifiableSet(mappedQNames);
//    }
    
}
