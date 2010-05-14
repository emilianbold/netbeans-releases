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
package org.netbeans.modules.wsdlextensions.email.imap;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * @author Sainath Adiraju
 *
 */
public enum IMAPQName {

    ADDRESS(createIMAPQName("IMAPaddress")),
    BINDING(createIMAPQName("IMAPbinding")),
    OPERATION(createIMAPQName("IMAPoperation")),
    INPUT(createIMAPQName("IMAPinput"));
    public static final String EMAIL_NS_URI = "http://schemas.sun.com/jbi/wsdl-extensions/email/";
    public static final String EMAIL_NS_PREFIX = "email";

    public static QName createIMAPQName(String localName) {
        return new QName(EMAIL_NS_URI, localName, EMAIL_NS_PREFIX);
    }

    IMAPQName(QName name) {
        qName = name;
    }

    public QName getQName() {
        return qName;
    }
    private static Set<QName> qnames = null;

    public static Set<QName> getQNames() {
        if (qnames != null) {
            return qnames;
        }
        populateQNames();
        return qnames;
    }

    private static synchronized void populateQNames() {
        if (qnames == null) {
            Set<QName> lQNames = new HashSet<QName>();
            for (IMAPQName wq : values()) {
                lQNames.add(wq.getQName());
            }
            qnames = lQNames;
        }
    }
    
    private final QName qName;
}
