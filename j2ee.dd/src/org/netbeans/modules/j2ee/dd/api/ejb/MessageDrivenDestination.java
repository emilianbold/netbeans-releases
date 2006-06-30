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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//

public interface MessageDrivenDestination extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {

    public static final String DESTINATION_TYPE = "DestinationType";	// NOI18N
    public static final String DESTINATIONTYPEID = "DestinationTypeId";	// NOI18N
    public static final String SUBSCRIPTION_DURABILITY = "SubscriptionDurability";	// NOI18N
    public static final String SUBSCRIPTIONDURABILITYID = "SubscriptionDurabilityId";	// NOI18N
    public static final String DESTINATION_TYPE_QUEUE = "javax.jms.Queue";  // NOI18N
    public static final String DESTINATION_TYPE_TOPIC = "javax.jms.Topic"; // NOI18N
    public static final String SUBSCRIPTION_DURABILITY_DURABLE = "Durable"; // NOI18N
    public static final String SUBSCRIPTION_DURABILITY_NONDURABLE = "NonDurable"; // NOI18N
        
    public void setDestinationType(String value);
    
    public String getDestinationType();
    
    public void setSubscriptionDurability(String value);
    
    public String getSubscriptionDurability();
        
}

