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

import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSBinding;
import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 * Controller to allow for population of the model to the
 * visual components as well as persistence of the model
 * 
 * @author jalmero
 */
public class SynchronousReadConsumerPersistenceController {
    private SynchronousReadPanel jmsEditorPanel = null;
    private WSDLComponent mWSDLComponent = null;
    private SynchronousReadConsumerPersistenceController mController = null;
        
    public SynchronousReadConsumerPersistenceController(WSDLComponent modelComponent,
            SynchronousReadPanel visualComponent) {
        jmsEditorPanel = visualComponent;
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

    public boolean rollback() {
        return true;
    }

    private boolean commitAddress(JMSAddress jmsAddress) {
        WSDLModel wsdlModel = mWSDLComponent.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }                 
    
            Port port = (Port) jmsAddress.getParent();
            Binding binding = port.getBinding().get();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = jmsEditorPanel.getOperationName();
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
            JMSAddress jmsAddress = jmsEditorPanel.
                    getJMSAddressPerSelectedPort();
            Binding binding = (Binding) jmsBinding.getParent();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = jmsEditorPanel.getOperationName();
            commitBindingOperations(binding, bindingOperations, operationName);          
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        }
        return true;
    }    
    
    private void commitJMSOperation(JMSOperation jmsOp) {
        if (jmsOp != null) {
                // From Destination Section       
                if (jmsOp.getDestination() != null) {
                    if (!jmsOp.getDestination().equals(jmsEditorPanel.getDestination())) {
                        jmsOp.setDestination(jmsEditorPanel.getDestination());
                    }
                } else {
                    if (jmsEditorPanel.getDestination() != null) {
                        jmsOp.setDestination(jmsEditorPanel.getDestination());
                    }
                }

                if (jmsOp.getDestinationType() != null) {
                    if (!jmsOp.getDestinationType().equals(jmsEditorPanel.getDestinationType())) {
                        jmsOp.setDestinationType(jmsEditorPanel.getDestinationType());
                    }
                } else {
                    if (jmsEditorPanel.getDestinationType() != null) {
                        jmsOp.setDestinationType(jmsEditorPanel.getDestinationType());
                    }
                }

                if (jmsOp.getTransaction() != null) {
                    if (!jmsOp.getTransaction().equals(jmsEditorPanel.getTransaction())) {
                        jmsOp.setTransaction(jmsEditorPanel.getTransaction());
                    }                    
                } else {
                    if (jmsEditorPanel.getTransaction() != null) {
                        jmsOp.setTransaction(jmsEditorPanel.getTransaction());
                    }                    
                }
                
                if (JMSConstants.TOPIC.equals(jmsEditorPanel.getDestinationType())) {
                    if (jmsOp.getClientID() != null) {
                        if (!jmsOp.getClientID().equals(jmsEditorPanel.getClientID())) {
                            jmsOp.setClientID(jmsEditorPanel.getClientID());
                        }
                    } else {
                        if (jmsEditorPanel.getClientID() != null) {
                            jmsOp.setClientID(jmsEditorPanel.getClientID());
                        }
                    }

                    if (jmsOp.getSubscriptionName() != null) {
                        if (!jmsOp.getSubscriptionName().equals(jmsEditorPanel.getSubscriptionName())) {
                            jmsOp.setSubscriptionName(jmsEditorPanel.getSubscriptionName());
                        }
                    } else {
                        if (jmsEditorPanel.getSubscriptionName() != null) {
                            jmsOp.setSubscriptionName(jmsEditorPanel.getSubscriptionName());
                        }
                    }

                    if (jmsOp.getSubscriptionDurability() != null) {
                        if (!jmsOp.getSubscriptionDurability().equals(jmsEditorPanel.getSubscriptionDurability())) {
                            jmsOp.setSubscriptionDurability(jmsEditorPanel.getSubscriptionDurability());
                        }
                    } else {
                        if (jmsEditorPanel.getSubscriptionDurability() != null) {
                            jmsOp.setSubscriptionDurability(jmsEditorPanel.getSubscriptionDurability());
                        }
                    }
                } else {
                    // null out topic related attributes
                    jmsOp.setAttribute(JMSOperation.ATTR_CLIENT_ID, null);
                    jmsOp.setAttribute(JMSOperation.ATTR_SUBSCRIPTION_NAME, null);
                    jmsOp.setAttribute(JMSOperation.ATTR_SUBSCRIPTION_DURABILITY, null);
                }            
             
            // From JMS Consumer Section            
            if (jmsOp.getMessageSelector() != null) {
                if (!jmsOp.getMessageSelector().equals(jmsEditorPanel.getMessageSelector())) {
                    jmsOp.setMessageSelector(jmsEditorPanel.getMessageSelector());
                }
            } else {
                if (jmsEditorPanel.getMessageSelector() != null) {
                    jmsOp.setMessageSelector(jmsEditorPanel.getMessageSelector());
                }
            }            
                    
            if ((jmsOp.getTimeout() > -1) &&
                    (jmsOp.getTimeout() != jmsEditorPanel.getTimeout())) {
                if (jmsEditorPanel.getTimeout() != -1) {
                    jmsOp.setTimeout((int) jmsEditorPanel.getTimeout());
                } else {
                    jmsOp.setAttribute(JMSOperation.ATTR_TIMEOUT, null);
                }                
            } else {
                if (jmsEditorPanel.getTimeout() > 0) {
                    jmsOp.setTimeout(jmsEditorPanel.getTimeout());
                }                
            }              
        }     
    }
    
    private void commitOutputJMSMessage(Binding binding, JMSMessage jmsMessage,
            String opName) {
        if (jmsMessage != null) {
         // TODO - check how these2 maps to model
//            jmsMessage.setMessageType(jmsEditorPanel.
//                    getInputMessageType());
//            jmsMessage.setTextPart(jmsEditorPanel.
//                    getInputMessageText());            

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
            Collection<BindingOperation> bops, String operationName) {
        if (bops != null) {
           for (BindingOperation bop : bops) {
                if (bop.getName().equals(operationName)) {
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
                    BindingOutput bo = bop.getBindingOutput();
                    List<JMSMessage> outputJMSMessages =
                            bo.getExtensibilityElements(JMSMessage.class);
                    JMSMessage outputJMSMessage = outputJMSMessages.get(0);
                    
                    // save the jms input message info
                    commitOutputJMSMessage(binding, outputJMSMessage,
                            operationName);

                }
            }            
        }
    }

    public static void populate() {
        
    }
}
