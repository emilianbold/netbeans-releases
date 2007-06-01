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

package org.netbeans.modules.j2ee.api.ejbjar;

/**
 *
 * @author Martin Adamek
 */
public final class MessageDestinationReference {

    private final String messageDestinationRefName;
    private final String messageDestinationType;
    private final String messageDestinationUsage;
    private final String messageDestinationLink;
    
    private MessageDestinationReference(String messageDestinationRefName, String messageDestinationType, String messageDestinationUsage, String messageDestinationLink) {
        this.messageDestinationRefName = messageDestinationRefName;
        this.messageDestinationType = messageDestinationType;
        this.messageDestinationUsage = messageDestinationUsage;
        this.messageDestinationLink = messageDestinationLink;
    }

    public static MessageDestinationReference create(String messageDestinationRefName, String messageDestinationType, String messageDestinationUsage, String messageDestinationLink) {
        return new MessageDestinationReference(messageDestinationRefName, messageDestinationType, messageDestinationUsage, messageDestinationLink);
    }
    
    public String getMessageDestinationRefName() {
        return messageDestinationRefName;
    }

    public String getMessageDestinationType() {
        return messageDestinationType;
    }
    
    public String getMessageDestinationUsage() {
        return messageDestinationUsage;
    }
    
    public String getMessageDestinationLink() {
        return messageDestinationLink;
    }
    
}
