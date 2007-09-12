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
 * JMSConstants
 */
public class JMSConstants {

    // Destination Type (domain)
    public static final String QUEUE = "Queue";
    public static final String TOPIC = "Topic";
    
    // Transaction Support
    public static final String TRANSACTION_NONE  = "NoTransaction";
    public static final String TRANSACTION_LOCAL = "LocalTransaction";
    public static final String TRANSACTION_XA    = "XATransaction";
    
    // Delivery Modes
    public static final String DELIVERYMODE_PERSISTENT = "PERSISTENT";
    public static final String DELIVERYMODE_NON_PERSISTENT = "NON_PERSISTENT";
    
    // Time to live
    public static final long TIME_TO_LIVE_FOREVER = 0;

    // Timeout 
    public static final long TIME_OUT_MSECS_DEFAULT = 5 * 60000;
    
    // Delivery Priority
    public static final int PRIORITY_0 = 0;
    public static final int PRIORITY_1 = 1;
    public static final int PRIORITY_2 = 2;
    public static final int PRIORITY_3 = 3;
    public static final int PRIORITY_4 = 4;
    public static final int PRIORITY_5 = 5;
    public static final int PRIORITY_6 = 6;
    public static final int PRIORITY_7 = 7;
    public static final int PRIORITY_8 = 8;
    public static final int PRIORITY_9 = 9;
    public static final int PRIORITY_DEFAULT = PRIORITY_4;
    
    // Message Types
    public static final String TEXT_MESSAGE = "TextMessage";
    public static final String STREAM_MESSAGE = "StreamMessage";
    public static final String BYTES_MESSAGE = "BytesMessage";
    public static final String MAP_MESSAGE = "MapMessage";
    public static final String OBJECT_MESSAGE = "ObjectMessage";
    public static final String MESSAGE_MESSAGE = "Message";
    
    // Acknowlegement Modes
    public static final String AUTO_ACKNOWLEDGE   = "AUTO_ACKNOWLEDGE";
    public static final String CLIENT_ACKNOWLEDGE = "CLIENT_ACKNOWLEDGE";
    public static final String DUPS_OK_ACKNOWLEGE = "DUPS_OK_ACKNOWLEGE";    
    
    // Subscription Durability Types
    public static final String DURABLE  = "Durable";
    public static final String NON_DURABLE = "NonDurable";
    
    // Some options specific to JMS BC
    public static final String OUTBOUND_MAX_RETRIES = "SendMaxRetries";
    public static final String OUTBOUND_RETRY_INTERVAL = "SendRetryInterval";
    
    // Boolean values
    public static final String BOOLEAN_FALSE = "false";
    public static final String BOOLEAN_TRUE  = "true";
    
    // Batch size
    public static final int BATCH_SIZE_DEFAULT = 0;
    
    // JMS provider protocol
    public static final String JMS_GENERIC_JNDI_PROTOCOL = "jndi://";
    
    public static boolean stringValueIsTrue (String val) {        
        if (val == null || val.equals(BOOLEAN_FALSE) ) {
            return false;
        } else {
            return true;
        }
    }
    
}
