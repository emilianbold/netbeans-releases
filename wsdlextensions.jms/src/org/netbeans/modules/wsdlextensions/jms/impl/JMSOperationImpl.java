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

package org.netbeans.modules.wsdlextensions.jms.impl;

import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSQName;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 * JMSOperationImpl
 */
public class JMSOperationImpl extends JMSComponentImpl implements JMSOperation {

    public JMSOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public JMSOperationImpl(WSDLModel model){
        this(model, createPrefixedElement(JMSQName.OPERATION.getQName(), model));
    }
    
    /*
    public JMSOptions getOptions() {
        return options;
    }
    
    public void setOptions(JMSOptions val) {
        options = val;
    }
     */
    
    public String getDestination() {
        return getAttribute(JMSAttribute.JMS_OPERATION_DESTINATION);        
    }
    
    public void setDestination(String val) {
        setAttribute(JMSOperation.ATTR_DESTINATION, 
                     JMSAttribute.JMS_OPERATION_DESTINATION,
                     val);        
    }

    public String getDestinationType() {
        return getAttribute(JMSAttribute.JMS_OPERATION_DESTINATION_TYPE);        
    }
    
    public void setDestinationType(String val) {
        setAttribute(JMSOperation.ATTR_DESTINATION_TYPE, 
                     JMSAttribute.JMS_OPERATION_DESTINATION_TYPE,
                     val);        
    }
    
    public String getTransaction() {
        return getAttribute(JMSAttribute.JMS_OPERATION_TRANSACTION);         
    }
    
    public void setTransaction(String val) {
        setAttribute(JMSOperation.ATTR_TRANSACTION, 
                     JMSAttribute.JMS_OPERATION_TRANSACTION,
                     val);                
    }
    
    public String getDeliveryMode() {
        return getAttribute(JMSAttribute.JMS_OPERATION_DELIVERY_MODE);        
    }
    
    public void setDeliveryMode(String val) {
        setAttribute(JMSOperation.ATTR_DELIVERY_MODE, 
                     JMSAttribute.JMS_OPERATION_DELIVERY_MODE,
                     val);         
    }

    public long getTimeToLive() {
        String strVal = getAttribute(JMSAttribute.JMS_OPERATION_TIME_TO_LIVE);        
        
        long numVal = JMSConstants.TIME_TO_LIVE_FOREVER;
        if ( strVal != null ) {
            try {
                numVal = Long.parseLong(strVal);
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return numVal;
    }
    
    public void setTimeToLive(long val) {
        setAttribute(JMSOperation.ATTR_TIME_TO_LIVE, 
                     JMSAttribute.JMS_OPERATION_TIME_TO_LIVE,
                     ""+val);        
    }
    
    public int getPriority() {
        String strVal = getAttribute(JMSAttribute.JMS_OPERATION_PRIORITY);        
        
        int numVal = JMSConstants.PRIORITY_DEFAULT;
        if ( strVal != null ) {
            try {
                numVal = Integer.parseInt(strVal);
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return numVal;        
    }
    
    public void setPriority(int val) {
        setAttribute(JMSOperation.ATTR_PRIORITY, 
                     JMSAttribute.JMS_OPERATION_PRIORITY,
                     ""+val);        
    }

    public boolean getDisableMessageID() {
        String strVal = getAttribute(JMSAttribute.JMS_OPERATION_DISABLE_MESSAGE_ID);
        return JMSConstants.stringValueIsTrue(strVal);
    }
    
    
    public void setDisableMessageID(boolean val) {
        setAttribute(JMSOperation.ATTR_DISABLE_MESSAGE_ID, 
                     JMSAttribute.JMS_OPERATION_DISABLE_MESSAGE_ID,
                     val?JMSConstants.BOOLEAN_TRUE:JMSConstants.BOOLEAN_FALSE);        
    }

    public boolean getDisableMessageTimeStamp() {
        String strVal = getAttribute(JMSAttribute.JMS_OPERATION_DISABLE_MESSAGE_TIMESTAMP);
        return JMSConstants.stringValueIsTrue(strVal);        
    }
    
    public void setDisableMessageTimeStamp(boolean val) {
        setAttribute(JMSOperation.ATTR_DISABLE_MESSAGE_TIMESTAMP, 
                     JMSAttribute.JMS_OPERATION_DISABLE_MESSAGE_TIMESTAMP,
                     val?JMSConstants.BOOLEAN_TRUE:JMSConstants.BOOLEAN_FALSE);      
    }
    
    public long getTimeout() {
        String strVal = getAttribute(JMSAttribute.JMS_OPERATION_TIMEOUT);        
        
        long numVal = JMSConstants.TIME_OUT_MSECS_DEFAULT;
        if ( strVal != null ) {
            try {
                numVal = Long.parseLong(strVal);
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return numVal;        
    }
    
    public void setTimeout(long val) {
        setAttribute(JMSOperation.ATTR_TIMEOUT, 
                     JMSAttribute.JMS_OPERATION_TIMEOUT,
                     ""+val);        
    }
    
    public String getClientID() {
        return getAttribute(JMSAttribute.JMS_OPERATION_CLIENT_ID);        
    }
    
    public void setClientID(String val) {
        setAttribute(JMSOperation.ATTR_CLIENT_ID, 
                     JMSAttribute.JMS_OPERATION_CLIENT_ID,
                     val);        
    }
    
    public String getMessageSelector() {
        return getAttribute(JMSAttribute.JMS_OPERATION_MESSAGE_SELECTOR);                
    }
    
    public void setMessageSelector(String val) {
        setAttribute(JMSOperation.ATTR_MESSAGE_SELECTOR, 
                     JMSAttribute.JMS_OPERATION_MESSAGE_SELECTOR,
                     val);        
    }
    
    public String getSubscriptionDurability() {
        return getAttribute(JMSAttribute.JMS_OPERATION_SUBSCRIPTION_DURABILITY);        
    }
    
    public void setSubscriptionDurability(String val) {        
        setAttribute(JMSOperation.ATTR_SUBSCRIPTION_DURABILITY, 
                     JMSAttribute.JMS_OPERATION_SUBSCRIPTION_DURABILITY,
                     val);        
    }
    
    public String getSubscriptionName() {
        return getAttribute(JMSAttribute.JMS_OPERATION_SUBSCRIPTION_NAME);        
    }  
    
    public void setSubscriptionName(String val) {
        setAttribute(JMSOperation.ATTR_SUBSCRIPTION_NAME, 
                     JMSAttribute.JMS_OPERATION_SUBSCRIPTION_NAME,
                     val);         
    }
        
    public int getBatchSize() {
        String strVal = getAttribute(JMSAttribute.JMS_OPERATION_BATCH_SIZE);        
        
        int numVal = JMSConstants.BATCH_SIZE_DEFAULT;
        if ( strVal != null ) {
            try {
                numVal = Integer.parseInt(strVal);
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return numVal;        
    }
    
    public void setBatchSize(int val) {
        setAttribute(JMSOperation.ATTR_BATCH_SZIE, 
                     JMSAttribute.JMS_OPERATION_BATCH_SIZE,
                     ""+val);         
    }     

    public int getMaxConcurrentConsumers() {
        String strVal = getAttribute(JMSAttribute.JMS_OPERATION_MAX_CONCURRENT_CONSUMERS);        
        
        int numVal = -1;
        if ( strVal != null ) {
            try {
                numVal = Integer.parseInt(strVal);
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return numVal;        
    }

    public void setMaxConcurrentConsumers(int val) {
        setAttribute(JMSOperation.ATTR_MAX_CONCURRENT_CONSUMERS, 
                     JMSAttribute.JMS_OPERATION_MAX_CONCURRENT_CONSUMERS,
                     ""+val);                 
    }

    public String getRedeliveryHandling() {
        return getAttribute(JMSAttribute.JMS_OPERATION_REDELIVERY_HANDLING);        
    }

    public void setRedeliveryHandling(String val) {
        setAttribute(JMSOperation.ATTR_REDELIVERY_HANDLING, 
                     JMSAttribute.JMS_OPERATION_REDELIVERY_HANDLING,
                     val);         
    }

    public String getConcurrencyMode() {
        return getAttribute(JMSAttribute.JMS_OPERATION_CONCURRENCY_MODE);        
    }

    public void setConcurrencyMode(String val) {
        setAttribute(JMSOperation.ATTR_CONCURRENCY_MODE, 
                     JMSAttribute.JMS_OPERATION_CONCURRENCY_MODE,
                     val);         

    }
}
