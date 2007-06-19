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
* Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
* Microsystems, Inc. All Rights Reserved.
*/ 
/*
 * JAXWSQName.java
 *
 * Created on February 22, 2006, 8:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author Roderico Cruz
 */
public enum JAXWSQName {
    BINDINGS(createJAXWSQName("bindings")),
    PACKAGE(createJAXWSQName("package")),
    CLASS(createJAXWSQName("class")),
    ENABLEWRAPPERSTYLE(createJAXWSQName("enableWrapperStyle")),
    ENABLEASYNCMAPPING(createJAXWSQName("enableAsyncMapping")),
    ENABLEMIMECONTENT(createJAXWSQName("enableMIMEContent")),
    JAVAEXCEPTION(createJAXWSQName("exception")),
    METHOD(createJAXWSQName("method")),
    PARAMETER(createJAXWSQName("parameter")),
    JAVADOC(createJAXWSQName("javadoc")),
    PROVIDER(createJAXWSQName("provider"));
    
    public static final String JAXWS_NS_URI = "http://java.sun.com/xml/ns/jaxws";
    public static final String JAXWS_NS_PREFIX = "jaxws";
    
    public static QName createJAXWSQName(String localName){
        return new QName(JAXWS_NS_URI, localName, JAXWS_NS_PREFIX);
    }
    
    JAXWSQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (JAXWSQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
