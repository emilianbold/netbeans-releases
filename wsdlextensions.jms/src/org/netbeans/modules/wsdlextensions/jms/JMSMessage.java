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
package org.netbeans.modules.wsdlextensions.jms;

/**
 * JMSMessage
 */
public interface JMSMessage extends JMSComponent {
    
    public static final String ELEMENT_PROPERTIES = "properties";
    public static final String ELEMENT_MAPMESSAGE = "mapmessage";
    
    public static final String ATTR_MESSAGE_TYPE = "messageType";
    public static final String ATTR_TEXTPART = "textPart";
    public static final String ATTR_CORRELATION_ID_PART = "correlationIdPart";
    public static final String ATTR_DELIVERY_MODE_PART = "deliveryModePart";    
    public static final String ATTR_PRIORITY_PART = "priorityPart";
    public static final String ATTR_TYPE_PART = "typePart";
    public static final String ATTR_MESSAGE_ID_PART = "messageIDPart";
    public static final String ATTR_REDELIVERED_PART = "redeliveredPart";
    public static final String ATTR_TIMESTAMP_PART = "timestampPart";
    public static final String ATTR_USE = "use";
    public static final String ATTR_ENCODING_STYLE = "encodingStyle";

    public static final String ATTR_USE_TYPE_LITERAL = "literal";
    public static final String ATTR_USE_TYPE_ENCODED = "encoded";
     public static final String ATTR_BYTES_PART = "bytesPart";
    
    public static final String ATTR_FORWARD_AS_ATTACHMENT = "forwardAsAttachment";
            
    public String getMessageType();
    public void setMessageType(String val);

    public String getUse();
    public void setUse(String val);
    
    public String getTextPart();
    public void setTextPart(String val);
    
    public String getBytesPart();
    public void setBytesPart(String val);

    public String getCorrelationIdPart();
    public void setCorrelationIdPart(String val);

    public String getDeliveryModePart();
    public void setDeliveryModePart(String val);

    public String getPriorityPart();
    public void setPriorityPart(String val);

    public String getTypePart();
    public void setTypePart(String val);

    public String getMessageIDPart();
    public void setMessageIDPart(String val);

    public String getRedeliveredPart();
    public void setRedeliveredPart(String val);
    
    public String getTimestampPart();
    public void setTimestampPart(String val);
    
    public void setJMSEncodingStyle(String val);
    public String getJMSEncodingStyle();    
    
    /**
     * Return true if payload is to be send as an attachment
     * @return boolean
     */
    public boolean getForwardAsAttachment();
    
    /**
     * Set the forward as attachment flag
     * @param b
     */
    public void setForwardAsAttachment(boolean b);     
}
