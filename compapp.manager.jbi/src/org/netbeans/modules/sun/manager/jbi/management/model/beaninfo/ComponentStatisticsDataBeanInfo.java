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

package org.netbeans.modules.sun.manager.jbi.management.model.beaninfo;

import com.sun.esb.management.common.data.ComponentStatisticsData;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.netbeans.modules.sun.manager.jbi.util.BeanInfoHelper;

/**
 * BeanInfo class for ComponentStatisticsData.
 * 
 * @author jqian
 */
public class ComponentStatisticsDataBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
//            PropertyDescriptor namePD =
//                getPropertyDescriptor(
//                        "getInstanceName",                  // NOI18N
//                        null,  
//                        "LBL_COMPONENT_INSTANCE_NAME",      // NOI18N
//                        "DSC_COMPONENT_INSTANCE_NAME");     // NOI18N         
            
            PropertyDescriptor upTimePD =
                getPropertyDescriptor(
                        "getComponentUpTime",                        // NOI18N
                        null,
                        "LBL_COMPONENT_UP_TIME",            // NOI18N
                        "DSC_COMPONENT_UP_TIME");           // NOI18N
            
            PropertyDescriptor numOfActivatedEndpointsPD =
                getPropertyDescriptor(
                        "getNumberOfActivatedEndpoints",    // NOI18N
                        null,         
                        "LBL_NUM_OF_ACTIVATED_ENDPOINTS",   // NOI18N
                        "DSC_NUM_OF_ACTIVATED_ENDPOINTS");  // NOI18N
            
            PropertyDescriptor numOfReceivedRequestsPD =
                getPropertyDescriptor(
                        "getNumberOfReceivedRequests",    // NOI18N
                        null,         
                        "LBL_NUM_OF_RECEIVED_REQUESTS",   // NOI18N
                        "DSC_NUM_OF_RECEIVED_REQUESTS");  // NOI18N
            
            PropertyDescriptor numOfSentRequestsPD =
                getPropertyDescriptor(
                        "getNumberOfSentRequests",    // NOI18N
                        null,         
                        "LBL_NUM_OF_SENT_REQUESTS",   // NOI18N
                        "DSC_NUM_OF_SENT_REQUESTS");  // NOI18N
            
            PropertyDescriptor numOfReceivedRepliesPD =
                getPropertyDescriptor(
                        "getNumberOfReceivedReplies",    // NOI18N
                        null,         
                        "LBL_NUM_OF_RECEIVED_REPLIES",   // NOI18N
                        "DSC_NUM_OF_RECEIVED_REPLIES");  // NOI18N
            
            PropertyDescriptor numOfSentRepliesPD =
                getPropertyDescriptor(
                        "getNumberOfSentReplies",    // NOI18N
                        null,         
                        "LBL_NUM_OF_SENT_REPLIES",   // NOI18N
                        "DSC_NUM_OF_SENT_REPLIES");  // NOI18N
            
            PropertyDescriptor numOfReceivedDonesPD =
                getPropertyDescriptor(
                        "getNumberOfReceivedDones",    // NOI18N
                        null,         
                        "LBL_NUM_OF_RECEIVED_DONES",   // NOI18N
                        "DSC_NUM_OF_RECEIVED_DONES");  // NOI18N
            
            PropertyDescriptor numOfSentDonesPD =
                getPropertyDescriptor(
                        "getNumberOfSentDones",    // NOI18N
                        null,         
                        "LBL_NUM_OF_SENT_DONES",   // NOI18N
                        "DSC_NUM_OF_SENT_DONES");  // NOI18N
            
                                 
            PropertyDescriptor numOfReceivedFaultsPD =
                getPropertyDescriptor(
                        "getNumberOfReceivedFaults",    // NOI18N
                        null,         
                        "LBL_NUM_OF_RECEIVED_FAULTS",   // NOI18N
                        "DSC_NUM_OF_RECEIVED_FAULTS");  // NOI18N
            
            PropertyDescriptor numOfSentFaultsPD =
                getPropertyDescriptor(
                        "getNumberOfSentFaults",    // NOI18N
                        null,         
                        "LBL_NUM_OF_SENT_FAULTS",   // NOI18N
                        "DSC_NUM_OF_SENT_FAULTS");  // NOI18N
            
            PropertyDescriptor numOfReceivedErrorsPD =
                getPropertyDescriptor(
                        "getNumberOfReceivedErrors",    // NOI18N
                        null,         
                        "LBL_NUM_OF_RECEIVED_ERRORS",   // NOI18N
                        "DSC_NUM_OF_RECEIVED_ERRORS");  // NOI18N
            
            PropertyDescriptor numOfSentErrorsPD =
                getPropertyDescriptor(
                        "getNumberOfSentErrors",    // NOI18N
                        null,         
                        "LBL_NUM_OF_SENT_ERRORS",   // NOI18N
                        "DSC_NUM_OF_SENT_ERRORS");  // NOI18N
           
            PropertyDescriptor numOfCompletedExchangesPD =
                getPropertyDescriptor(
                        "getNumberOfCompletedExchanges",    // NOI18N
                        null,         
                        "LBL_NUM_OF_COMPLETED_EXCHANGES",   // NOI18N
                        "DSC_NUM_OF_COMPLETED_EXCHANGES");  // NOI18N
                
            PropertyDescriptor numOfActiveExchangesPD =
                getPropertyDescriptor(
                        "getNumberOfActiveExchanges",    // NOI18N
                        null,         
                        "LBL_NUM_OF_ACTIVE_EXCHANGES",   // NOI18N
                        "DSC_NUM_OF_ACTIVE_EXCHANGES");  // NOI18N
                
            PropertyDescriptor numOfErrorExchangesPD =
                getPropertyDescriptor(
                        "getNumberOfErrorExchanges",    // NOI18N
                        null,         
                        "LBL_NUM_OF_ERROR_EXCHANGES",   // NOI18N
                        "DSC_NUM_OF_ERROR_EXCHANGES");  // NOI18N
            
            PropertyDescriptor msgExchangeResponseTimeAveragePD =
                getPropertyDescriptor(
                        "getMessageExchangeResponseTimeAverage",    // NOI18N
                        null,         
                        "LBL_MSG_EXCHANGE_RESPONSE_TIME_AVERAGE",   // NOI18N
                        "DSC_MSG_EXCHANGE_RESPONSE_TIME_AVERAGE");  // NOI18N
            
            PropertyDescriptor msgExchangeComponentTimeAveragePD =
                getPropertyDescriptor(
                        "getMessageExchangeComponentTimeAverage",    // NOI18N
                        null,         
                        "LBL_MSG_EXCHANGE_COMPONENT_TIME_AVERAGE",   // NOI18N
                        "DSC_MSG_EXCHANGE_COMPONENT_TIME_AVERAGE");  // NOI18N
            
            PropertyDescriptor msgExchangeDeliveryChannelTimeAveragePD =
                getPropertyDescriptor(
                        "getMessageExchangeDeliveryChannelTimeAverage",    // NOI18N
                        null,         
                        "LBL_MSG_EXCHANGE_DELIVERY_CHANNEL_TIME_AVERAGE",   // NOI18N
                        "DSC_MSG_EXCHANGE_DELIVERY_CHANNEL_TIME_AVERAGE");  // NOI18N
            
            PropertyDescriptor msgExchangeMessageServiceTimeAveragePD =
                getPropertyDescriptor(
                        "getMessageExchangeMessageServiceTimeAverage",    // NOI18N
                        null,         
                        "LBL_MSG_EXCHANGE_MESSAGE_SERVICE_TIME_AVERAGE",   // NOI18N
                        "DSC_MSG_EXCHANGE_MESSAGE_SERVICE_TIME_AVERAGE");  // NOI18N
            
             
            /*
    protected CompositeData              componentExtensionStatus;    
    protected String                     componentExtensionStatusAsString;
             */
            
            PropertyDescriptor propertyDescriptors[] = {
//                    namePD, 
                    upTimePD, 
                    numOfActivatedEndpointsPD,
                    numOfReceivedRequestsPD,
                    numOfSentRequestsPD,
                    numOfReceivedRepliesPD,
                    numOfSentRepliesPD,
                    numOfReceivedDonesPD,
                    numOfSentDonesPD,
                    numOfReceivedFaultsPD,
                    numOfSentFaultsPD,
                    numOfReceivedErrorsPD,
                    numOfSentErrorsPD,
                    numOfCompletedExchangesPD,
                    numOfActiveExchangesPD,
                    numOfErrorExchangesPD,
                    msgExchangeResponseTimeAveragePD,
                    msgExchangeComponentTimeAveragePD,
                    msgExchangeDeliveryChannelTimeAveragePD,
                    msgExchangeMessageServiceTimeAveragePD,
            };
            
            return propertyDescriptors;
            
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }       
    
    private PropertyDescriptor getPropertyDescriptor(
            String getter, String setter, 
            String nameBundleKey,
            String descBundleKey) throws IntrospectionException {
        
        return BeanInfoHelper.getPropertyDescriptor(
                ComponentStatisticsData.class, 
                ComponentStatisticsDataBeanInfo.class, 
                getter, setter, nameBundleKey, descBundleKey);
    }
}
