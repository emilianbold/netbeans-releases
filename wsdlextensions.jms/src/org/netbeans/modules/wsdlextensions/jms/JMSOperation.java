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
 * JMSOperation
 */
public interface JMSOperation extends JMSComponent {

    public static final String IN_ONLY = "http://www.w3.org/2004/08/wsdl/in-only";
    public static final String IN_OUT = "http://www.w3.org/2004/08/wsdl/in-out";
    public static final String IN_OPTIONAL_OUT = "http://www.w3.org/2004/08/wsdl/in-opt-out";
    public static final String ROBUST_IN_ONLY = "http://www.w3.org/2004/08/wsdl/robust-in-only";
    public static final String OUT_ONLY = "http://www.w3.org/2004/08/wsdl/out-only";
    public static final String OUT_IN = "http://www.w3.org/2004/08/wsdl/out-in";
    public static final String OUT_OPTIONAL_IN = "http://www.w3.org/2004/08/wsdl/out-opt-in";
    public static final String ROBUST_OUT_ONLY = "http://www.w3.org/2004/08/wsdl/robust-out-only";    

    public static final String ELEMENT_OPTIONS = "options";

    // common
    public static final String ATTR_DESTINATION = "destination";
    public static final String ATTR_DESTINATION_TYPE = "destinationType";
    public static final String ATTR_TRANSACTION = "transaction";
    public static final String ATTR_VERB = "verb";
    
    // provider (outbound)
    public static final String ATTR_TIME_TO_LIVE = "timeToLive";
    public static final String ATTR_DELIVERY_MODE = "deliveryMode";
    public static final String ATTR_PRIORITY = "priority";
    public static final String ATTR_DISABLE_MESSAGE_ID = "disableMessageID";
    public static final String ATTR_DISABLE_MESSAGE_TIMESTAMP = "disableMessageTimeStamp";
    public static final String ATTR_TIMEOUT = "timeout";
    
    // consumer (inbound)
    public static final String ATTR_CLIENT_ID = "clientID";
    public static final String ATTR_MESSAGE_SELECTOR = "messageSelector";
    public static final String ATTR_SUBSCRIPTION_DURABILITY = "subscriptionDurability";
    public static final String ATTR_SUBSCRIPTION_NAME = "subscriptionName";    
    public static final String ATTR_BATCH_SZIE = "batchSize";
    public static final String ATTR_MAX_CONCURRENT_CONSUMERS = "maxConcurrentConsumers";
    public static final String ATTR_CONCURRENCY_MODE = "concurrencyMode";
    public static final String ATTR_REDELIVERY_HANDLING = "redeliveryHandling";
  
    /*
    public JMSOptions getOptions() {
        return options;
    }
    
    public void setOptions(JMSOptions val) {
        options = val;
    }
     */
    
    public String getDestination();
    public void setDestination(String val);

    public String getDestinationType();
    public void setDestinationType(String val);
    
    public String getTransaction();
    public void setTransaction(String val);
    
    public String getDeliveryMode();
    public void setDeliveryMode(String val);

    public long getTimeToLive();
    public void setTimeToLive(long val);
    
    public int getPriority();
    public void setPriority(int val);

    public boolean getDisableMessageID();
    public void setDisableMessageID(boolean val);

    public boolean getDisableMessageTimeStamp();
    public void setDisableMessageTimeStamp(boolean val);
    
    public long getTimeout();
    public void setTimeout(long val);
    
    public String getClientID();
    public void setClientID(String val);
    
    public String getMessageSelector();
    public void setMessageSelector(String val);
    
    public String getSubscriptionDurability();
    public void setSubscriptionDurability(String val);
    
    public String getSubscriptionName();    
    public void setSubscriptionName(String val);
        
    public int getBatchSize();
    public void setBatchSize(int val);        
    
    public String getMaxConcurrentConsumers();
    public void setMaxConcurrentConsumers(String val);       
    
    public String getRedeliveryHandling();
    public void setRedeliveryHandling(String val);

    public String getConcurrencyMode();
    public void setConcurrencyMode(String val);
    
    public String getVerb();
    public void setVerb(String val);
}
