/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    public int getMaxConcurrentConsumers();
    public void setMaxConcurrentConsumers(int val);       
    
    public String getRedeliveryHandling();
    public void setRedeliveryHandling(String val);

    public String getConcurrencyMode();
    public void setConcurrencyMode(String val);
    
}
