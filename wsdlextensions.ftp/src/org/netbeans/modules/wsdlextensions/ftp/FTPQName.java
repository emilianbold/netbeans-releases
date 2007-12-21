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

package org.netbeans.modules.wsdlextensions.ftp;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * @author jim.fu@sun.com
 *
 */
public enum FTPQName {
    ADDRESS(createFTPQName("address")),
    BINDING(createFTPQName("binding")),
    FAULT(createFTPQName("fault")),
    OPERATION(createFTPQName("operation")),
    MESSAGE(createFTPQName("message")),
    /*MESSAGEACTIVEPASSIVE(createFTPQName("messageActivePassive")),*/
    TRANSFER(createFTPQName("transfer"));
    
    public static final String FTP_NS_URI = "http://schemas.sun.com/jbi/wsdl-extensions/ftp/";
    public static final String FTP_NS_PREFIX = "ftp";
    
    public static QName createFTPQName(String localName){
        return new QName(FTP_NS_URI, localName, FTP_NS_PREFIX);
    }
    
    FTPQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (FTPQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    
    private final QName qName;
}
