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
package org.netbeans.modules.wsdlextensions.mq;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import javax.xml.namespace.QName;

import static org.netbeans.modules.wsdlextensions.mq.XmlSchemaDataTypes.*;

/**
 * The set of all (known and supported) MQ message descriptors.
 *
 * @author Noel.Ang@sun.com
 */
public enum MessageDescriptors {
    accountingToken       (BASE64, HEXBINARY),
    applicationId         (STRING),
    applicationOrigin     (STRING),
    backoutCount          (INTEGER, STRING),
    characterSet          (INTEGER, STRING),
    correlationId         (BASE64, HEXBINARY),
    encoding              (INTEGER, STRING),
    expiry                (INTEGER, STRING),
    feedback              (INTEGER, STRING),
    format                (STRING),
    groupId               (BASE64, HEXBINARY),
    messageFlags          (INTEGER, STRING),
    messageId             (BASE64, HEXBINARY),
    messageSequenceNumber (INTEGER, STRING),
    messageType           (INTEGER, STRING),
    offset                (INTEGER, STRING),
    originalLength        (INTEGER, STRING),
    persistence           (INTEGER, STRING),
    priority              (INTEGER, STRING),
    putApplicationName    (STRING),
    putApplicationType    (INTEGER, STRING),
    putDateTime           (DATETIME, STRING),
    replyToQueueManager   (STRING),
    replyToQueueName      (STRING),
    report                (INTEGER, STRING),
    userId                (STRING),
    ;
        
    public boolean isRepresentableAs(QName type) {
        return types.contains(type);
    }
    
    public Set<QName> getRepresentations() {
        return Collections.unmodifiableSet(types);
    }
        
    private MessageDescriptors(XmlSchemaDataTypes... dataTypes) {
        types = new HashSet<QName>();
        for (XmlSchemaDataTypes type : dataTypes) {
            types.add(type.qname);
        }
    }
        
    private final Set<QName> types;
}
