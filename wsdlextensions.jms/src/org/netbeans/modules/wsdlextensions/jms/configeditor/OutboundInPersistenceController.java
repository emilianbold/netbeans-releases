/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSBinding;
import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 */
public class OutboundInPersistenceController {
     
    private OutboundOneWayConnectionPanel connectionPanel = null;
    private OutboundOneWayPublisherPanel publisherPanel = null;
    private JMSAdvancedPanel mAdvPanel = null;
    private WSDLComponent mWSDLComponent = null;
    private QName mQName = null;
    private OutboundInPersistenceController mController = null;
    private boolean mSolicited = false;
        
    public OutboundInPersistenceController(WSDLComponent modelComponent,
            OutboundOneWayConnectionPanel visualComponent) {
        connectionPanel = visualComponent;
        mWSDLComponent = modelComponent;
    }
    
    public OutboundInPersistenceController(WSDLComponent modelComponent,
            OutboundOneWayPublisherPanel visualComponent) {
        publisherPanel = visualComponent;
        mWSDLComponent = modelComponent;
    }
    
    public boolean commit() {
        if (mWSDLComponent instanceof JMSAddress) {
            return commitAddress((JMSAddress) mWSDLComponent);
        } else if (mWSDLComponent instanceof JMSBinding) {
            return commitBinding((JMSBinding) mWSDLComponent);
        } else if (mWSDLComponent instanceof Port) {
            return commitPort((Port) mWSDLComponent);
        } else if (mWSDLComponent instanceof JMSMessage) {
            return commitMessage((JMSMessage) mWSDLComponent);
        } else if (mWSDLComponent instanceof JMSOperation) {
            return commitOperation((JMSOperation) mWSDLComponent);
        }
        return false;
    }

    private boolean commitAddress(JMSAddress jmsAddress) {
        WSDLModel wsdlModel = mWSDLComponent.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }                 
            commitServices(jmsAddress);     
            Port port = (Port) jmsAddress.getParent();
            Binding binding = port.getBinding().get();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = "";
            if (connectionPanel != null) {
                operationName = connectionPanel.getOperationName();
            } else if (publisherPanel != null) {
                operationName = publisherPanel.getOperationName();
            }
            commitBindingOperations(binding, bindingOperations, operationName);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } 
        return true;
    }
    
    private boolean commitBinding(JMSBinding jmsBinding) {
        WSDLModel wsdlModel = jmsBinding.getModel();
        try {                        
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }     
            JMSAddress jmsAddress = null;
            if (connectionPanel != null) {
                jmsAddress = connectionPanel.getJMSAddressPerSelectedPort();
            } else if (publisherPanel != null) {
                jmsAddress = publisherPanel.getJMSAddressPerSelectedPort();
            }            

            commitServices(jmsAddress);
            Binding binding = (Binding) jmsBinding.getParent();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = "";
            if (connectionPanel != null) {
                operationName = connectionPanel.getOperationName();
            } else if (publisherPanel != null) {
                operationName = publisherPanel.getOperationName();
            }            
            commitBindingOperations(binding, bindingOperations, operationName);          
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } 
        return false;
    }    
    
    private void commitJMSOperation(JMSOperation jmsOp) {
        if (jmsOp != null) {
            if (connectionPanel != null) {
 
            }
            
            if (publisherPanel != null) {
                // From Destination Section       
                if (jmsOp.getDestination() != null) {
                    if (!jmsOp.getDestination().equals(publisherPanel.getDestination())) {
                        jmsOp.setDestination(publisherPanel.getDestination());
                    }
                } else {
                    if (publisherPanel.getDestination() != null) {
                        jmsOp.setDestination(publisherPanel.getDestination());
                    }
                }

                if (jmsOp.getDestinationType() != null) {
                    if (!jmsOp.getDestinationType().equals(publisherPanel.getDestinationType())) {
                        jmsOp.setDestinationType(publisherPanel.getDestinationType());
                    }
                } else {
                    if (publisherPanel.getDestinationType() != null) {
                        jmsOp.setDestinationType(publisherPanel.getDestinationType());
                    }
                }

                if (jmsOp.getTransaction() != null) {
                    if (!jmsOp.getTransaction().equals(publisherPanel.getTransaction())) {
                        jmsOp.setTransaction(publisherPanel.getTransaction());
                    }                    
                } else {
                    if (publisherPanel.getTransaction() != null) {
                        jmsOp.setTransaction(publisherPanel.getTransaction());
                    }                    
                }
                
                if (JMSConstants.TOPIC.equals(publisherPanel.getDestinationType())) {
                    if (jmsOp.getClientID() != null) {
                        if (!jmsOp.getClientID().equals(publisherPanel.getClientID())) {
                            jmsOp.setClientID(publisherPanel.getClientID());
                        }
                    } else {
                        if (publisherPanel.getClientID() != null) {
                            jmsOp.setClientID(publisherPanel.getClientID());
                        }
                    }

                    if (jmsOp.getSubscriptionName() != null) {
                        if (!jmsOp.getSubscriptionName().equals(publisherPanel.getSubscriptionName())) {
                            jmsOp.setSubscriptionName(publisherPanel.getSubscriptionName());
                        }
                    } else {
                        if (publisherPanel.getSubscriptionName() != null) {
                            jmsOp.setSubscriptionName(publisherPanel.getSubscriptionName());
                        }
                    }

                    if (jmsOp.getSubscriptionDurability() != null) {
                        if (!jmsOp.getSubscriptionDurability().equals(publisherPanel.getSubscriptionDurability())) {
                            jmsOp.setSubscriptionDurability(publisherPanel.getSubscriptionDurability());
                        }
                    } else {
                        if (publisherPanel.getSubscriptionDurability() != null) {
                            jmsOp.setSubscriptionDurability(publisherPanel.getSubscriptionDurability());
                        }
                    }
                } else {
                    // null out topic related attributes
                    jmsOp.setAttribute(JMSOperation.ATTR_CLIENT_ID, null);
                    jmsOp.setAttribute(JMSOperation.ATTR_SUBSCRIPTION_NAME, null);
                    jmsOp.setAttribute(JMSOperation.ATTR_SUBSCRIPTION_DURABILITY, null);
                }            
 
                if ((jmsOp.getTimeToLive() > 0) &&
                        (jmsOp.getTimeToLive() != publisherPanel.getTimeToLive())) {
                    jmsOp.setTimeToLive(publisherPanel.getTimeToLive());
                } else {
                    if (publisherPanel.getTimeToLive() > 0) {
                        jmsOp.setTimeToLive(publisherPanel.getTimeToLive());
                    }
                }

                if (jmsOp.getDeliveryMode() != null) {
                    if (!jmsOp.getDeliveryMode().equals(publisherPanel.getDeliverMode())) {
                        jmsOp.setDeliveryMode(publisherPanel.getDeliverMode());
                    }
                } else {
                    if (publisherPanel.getDeliverMode() != null) {
                        jmsOp.setDeliveryMode(publisherPanel.getDeliverMode());
                    }
                }

                if ((jmsOp.getPriority() > 0) &&
                        (jmsOp.getPriority() != publisherPanel.getPriority())) {
                    jmsOp.setPriority(publisherPanel.getPriority());
                } else {
                    if (publisherPanel.getPriority() > 0) {
                        jmsOp.setPriority(publisherPanel.getPriority());
                    }
                } 
                
                if ((jmsOp.getTimeout() > 0) &&
                        (jmsOp.getTimeout() != publisherPanel.getTimeout())) {
                    jmsOp.setTimeout(publisherPanel.getTimeout());
                } else {
                    if (publisherPanel.getTimeout() > 0) {
                        jmsOp.setTimeout(publisherPanel.getTimeout());
                    }
                }                 
               
            }
        }     
    }
    private void commitInputJMSMessage(Binding binding,String opName,
            JMSMessage jmsMessage) {
        if (jmsMessage != null) {
            if (connectionPanel != null) {
                 
//                jmsMessage.setMessageType(connectionPanel.
//                        getInputMessageType());
//                jmsMessage.setTextPart(connectionPanel.
//                        getInputMessageText());

                if (jmsMessage.getMessageType() != null) {
                    if (!jmsMessage.getMessageType().equals(connectionPanel.getInputMessageType())) {
                        jmsMessage.setMessageType(connectionPanel.getInputMessageType());
                    }
                } else {
                    if (connectionPanel.getInputMessageType() != null) {
                        jmsMessage.setMessageType(connectionPanel.getInputMessageType());
                    }
                } 
                
                 // From Message Type section
                if (connectionPanel.getInputUse().equals(JMSConstants.ENCODED)) {
                    jmsMessage.setJMSEncodingStyle(connectionPanel.getInputEncodingStyle());
                    jmsMessage.setUse(connectionPanel.getInputUse());
                } else {
                    if ((jmsMessage.getUse() != null) &&
                            (connectionPanel.getInputUse().equals(JMSConstants.LITERAL))) {
                        jmsMessage.setJMSEncodingStyle(null);
                        jmsMessage.setUse(null);
                    }
                } 
                
                commitMessageType(binding, opName, jmsMessage);            
            }
            
            if (publisherPanel != null) {

            }
          
        }
    }
    
//    private void commitOutputJMSMessage(JMSMessage jmsMessage) {
//        if (jmsMessage != null) {
//            jmsMessage.setMessageType(connectionPanel.
//                    getOutputMessageType());
//            jmsMessage.setTextPart(connectionPanel.
//                    getOutputMessageText());
//            jmsMessage.setCorrelationIdPart(connectionPanel.
//                    getOutputCorrelationPart());
//            jmsMessage.setDeliveryModePart(connectionPanel.
//                    getOutputDeliveryModePart());
//            jmsMessage.setPriorityPart(connectionPanel.
//                    getOutputPriorityPart());
//            jmsMessage.setTypePart(connectionPanel.
//                    getOutputTypePart());
//            jmsMessage.setMessageIDPart(connectionPanel.
//                    getOutputMessageIDPart());
//            jmsMessage.setRedeliveredPart(connectionPanel.
//                    getOutputRedeliveredPart());
//            jmsMessage.setTimestampPart(connectionPanel.
//                    getOutputTimestamp());
//            jmsMessage.setUse(connectionPanel.
//                    getOutputUse());
//            jmsMessage.setJMSEncodingStyle(connectionPanel.
//                    getOutputEncodingStyle());            
//        }
//    }
    
    public void commitServices(JMSAddress jmsAddress) {
        if (jmsAddress != null) {
            if (connectionPanel != null) {
                jmsAddress.setAttribute(JMSAddress.ATTR_CONNECTION_URL,
                        connectionPanel.getConnectionURL()); 
                jmsAddress.setAttribute(JMSAddress.ATTR_USERNAME,
                        connectionPanel.getUserName());
                jmsAddress.setAttribute(JMSAddress.ATTR_PASSWORD,
                        connectionPanel.getPassword()); 
                // per mgmt,no need to persist individually
//                jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_CONNECTION_FACTORY_NAME,
//                        connectionPanel.getConnectionFactoryName());
//                jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_INITIAL_CONTEXT_FACTORY,
//                        connectionPanel.getInitialContextFactory());
//                jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_PROVIDER_URL,
//                        connectionPanel.getProviderURL());
//                jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_SECURITY_PRINCIPAL,
//                        connectionPanel.getSecurityPrincipal());
//                jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_SECURITY_CRDENTIALS,
//                        connectionPanel.getSecurityCredentials());
                
                // per meeting 08/14/08, values for jndi info below will be
                // persisted/retrieved from a single string attribute.  runtime
                // will be responsible for getting correct info from here  
                if (mAdvPanel != null) {
                    // just get the free-form string from text area
                    String jndiInfoStr = mAdvPanel.getText();
                    JMSUtilities.createJMSJCAOption(jmsAddress, jndiInfoStr);
                }                
            }
        }
    }
    
   private boolean commitPort(Port port) {
        Collection<JMSAddress> address = port.
                getExtensibilityElements(JMSAddress.class);
        JMSAddress jmsAddress = address.iterator().next();
        return commitAddress(jmsAddress);
    }
    
    private boolean commitMessage(JMSMessage jmsMessage) {
        Object parentObj = jmsMessage.getParent();
        BindingOperation parentOp = null;
        if (parentObj instanceof BindingInput) {
            parentOp = (BindingOperation) ((BindingInput) parentObj).
                    getParent();
        } else if (parentObj instanceof  BindingOutput) {
            parentOp = (BindingOperation) ((BindingOutput) parentObj).
                    getParent();
        }
        if (parentObj != null) {
            Binding parentBinding = (Binding) parentOp.getParent();
            Collection<JMSBinding> bindings = parentBinding.
                    getExtensibilityElements(JMSBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }
     
    private boolean commitOperation(JMSOperation fileOperation) {
        Object obj = fileOperation.getParent();
        if (obj instanceof BindingOperation) {
            Binding parentBinding = (Binding) ((BindingOperation) obj).getParent();
            Collection<JMSBinding> bindings = parentBinding.
                    getExtensibilityElements(JMSBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }    
    
    private void commitBindingOperations(Binding binding,
            Collection<BindingOperation> bops, String opName) {
        if (bops != null) {
           for (BindingOperation bop : bops) {
                if (bop.getName().equals(opName)) {
                    List<JMSOperation> jmsOpsList = bop.
                            getExtensibilityElements(JMSOperation.class);
                    Iterator<JMSOperation> jmsOps =
                            jmsOpsList.iterator();
                    // there should only be one jms:operation for the binding op
                    if (jmsOpsList.size() > 0) {
                        JMSOperation jmsOp = jmsOps.next();
                        
                        // save the jms operation info
                        commitJMSOperation(jmsOp);
                    }
                    BindingInput bi = bop.getBindingInput();
                    List<JMSMessage> inputJMSMessages =
                            bi.getExtensibilityElements(JMSMessage.class);
                    JMSMessage inputJMSMessage = inputJMSMessages.get(0);
                    
                    // save the jms input message info
                    commitInputJMSMessage(binding, opName,inputJMSMessage);

//                    BindingOutput bo = bop.getBindingOutput();
//                    List<JMSMessage> outputJMSMessages =
//                            bo.getExtensibilityElements(JMSMessage.class);
//                    JMSMessage outputJMSMessage = outputJMSMessages.get(0);
//                    
//                    //save the jms output message info
//                    commitOutputJMSMessage(outputJMSMessage);
                }
            }            
        }
    }
    
private void commitMessageType(Binding binding, String opName,
            JMSMessage jmsMessage) {
        String partName = connectionPanel.getInputMessageText();

    GlobalType gT = connectionPanel.getSelectedPartType();
    GlobalElement gE = connectionPanel.getSelectedElementType();       
        if (partName != null) {
            // set input part 
            Collection parts = JMSUtilities.getInputParts(binding, opName);
            if ((parts != null) && (parts.size() > 0)) {
                Iterator iter = parts.iterator();
                while (iter.hasNext()) {
                    Part partEntry = (Part) iter.next();
                    if (partEntry.getName().equals(partName)) {
                        JMSUtilities.setPartType(partEntry, 
                                connectionPanel.getMessageType(), gT, gE);
                    } 
                }
            }
            JMSUtilities.setPart(connectionPanel.getMessageType(),
                    jmsMessage, partName);

        } else {
            // need to create a message with part
            Message newMessageIn = JMSUtilities.createMessage(mWSDLComponent.getModel(), "JMSInputMessage"); //NOI18N
            if (newMessageIn != null) {
                Part newPart = JMSUtilities.createPartAndSetTypeIfDefined(mWSDLComponent.getModel(),
                        newMessageIn, "part1", connectionPanel.getMessageType(),
                        gT, gE);
                if (newPart != null) {
                    JMSUtilities.setPart(connectionPanel.getMessageType(),
                                        jmsMessage, newPart.getName());                    
                }
            }
        }
    }

    public void populate() {
        
    }
    
    public void setAdvancedPanel(JMSAdvancedPanel panel) {
        mAdvPanel = panel;
    }    
}
