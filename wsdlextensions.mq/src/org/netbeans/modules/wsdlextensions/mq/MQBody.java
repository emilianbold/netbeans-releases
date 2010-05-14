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

/*
 * MQBODY.java
 *
 * Created on December 14, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.mq;

/**
 *
 * @author rchen
 */
public interface MQBody extends MQComponent {
    public static final String ATTR_MESSAGE_TYPE = "messageType";
    public static final String TEXT_MESSAGE = "TextMessage";
    public static final String BYTE_MESSAGE = "ByteMessage";
    
    public static final String ATTR_MESSAGEBODY = "messageBody";
    public static final String ATTR_SYNCPOINT = "syncpoint";
    public static final String ATTR_ENCODING_STYLE = "encodingStyle";
    
    public static final String ATTR_USE = "use";
    public static final String ATTR_USE_TYPE_LITERAL = "literal";
    public static final String ATTR_USE_TYPE_ENCODED = "encoded";
    
    public String getMessageType();
    public void setMessageType(String val);
    
    public String getUse();
    public void setUse(String val);
    
     public String getMessageBodyPart();
    public void setMessageBodyPart(String val);
    
    public Boolean getSyncpoint();
    public void setSyncpoint(Boolean useSyncpoint);
    
}
