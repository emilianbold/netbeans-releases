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

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSBinding;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.HelpCtx;

/**
 *
 * @author skini
 */
public class JMSConfigurationEditorComponent 
        implements ExtensibilityElementConfigurationEditorComponent {

    JMSBindingConfigurationPanel jmsEditorPanel = null;
    private QName mQName;
    private WSDLComponent component;

    public JMSConfigurationEditorComponent(QName qName, WSDLComponent component) {
        this.component = component;
        if (jmsEditorPanel == null) {
            jmsEditorPanel = new JMSBindingConfigurationPanel(qName, component);
        } else {
            jmsEditorPanel.populateView(qName, component);
        }
        mQName = qName;
        this.component = component;
        jmsEditorPanel.setName(getTitle());
    }

    public JPanel getEditorPanel() {
        return jmsEditorPanel;
    }

    public String getTitle() {
        return "Configure JMS Binding";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public ActionListener getActionListener() {
        return null;
    }

    public boolean commit() {
        if (component instanceof JMSAddress) {
            commitAddress((JMSAddress) component);
        } else if (component instanceof JMSBinding) {
            commitBinding((JMSBinding) component);
        } else if (component instanceof Port) {
            return commitPort((Port) component);
        } else if (component instanceof JMSMessage) {
            return commitMessage((JMSMessage) component);
        } else if (component instanceof JMSOperation) {
            return commitOperation((JMSOperation) component);
        }
        return false;
    }

    public boolean rollback() {
        return true;
    }

    private boolean commitAddress(JMSAddress jmsAddress) {
        WSDLModel wsdlModel = component.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }                 
            commitServices(jmsAddress);     
            Port port = (Port) jmsAddress.getParent();
            Binding binding = port.getBinding().get();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = jmsEditorPanel.getOperationName();
            commitBindingOperations(bindingOperations, operationName);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (wsdlModel.isIntransaction()) {
               wsdlModel.endTransaction(); 
            }             
            return true;
        }     
    }
    
    private boolean commitBinding(JMSBinding jmsBinding) {
        WSDLModel wsdlModel = jmsBinding.getModel();
        try {                        
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }           
            JMSAddress jmsAddress = jmsEditorPanel.
                    getJMSAddressPerSelectedPort();
            commitServices(jmsAddress);
            Binding binding = (Binding) jmsBinding.getParent();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = jmsEditorPanel.getOperationName();
            commitBindingOperations(bindingOperations, operationName);          
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } finally {
            if (wsdlModel.isIntransaction()) {
               wsdlModel.endTransaction(); 
            }             
            return true;
        }                    
    }    
    
    private void commitJMSOperation(JMSOperation jmsOp) {
        if (jmsOp != null) {
            jmsOp.setAttribute(JMSOperation.ATTR_DESTINATION,
                    jmsEditorPanel.getDestination());                                      
            jmsOp.setAttribute(JMSOperation.ATTR_DESTINATION_TYPE,
                    jmsEditorPanel.getDestinationType());
            jmsOp.setAttribute(JMSOperation.ATTR_TRANSACTION,
                    jmsEditorPanel.getTransaction());
            jmsOp.setAttribute(JMSOperation.ATTR_CLIENT_ID,
                    jmsEditorPanel.getClientID());
            jmsOp.setAttribute(JMSOperation.ATTR_MESSAGE_SELECTOR,
                    jmsEditorPanel.getMessageSelector());             
            jmsOp.setAttribute(JMSOperation.ATTR_SUBSCRIPTION_NAME,
                    jmsEditorPanel.getSubscriptionName()); 
            jmsOp.setAttribute(JMSOperation.ATTR_SUBSCRIPTION_DURABILITY,
                    jmsEditorPanel.getSubscriptionDurability());  
            jmsOp.setAttribute(JMSOperation.ATTR_MAX_CONCURRENT_CONSUMERS,
                    jmsEditorPanel.getMaxConcurrentConsumer());             
            jmsOp.setAttribute(JMSOperation.ATTR_CONCURRENCY_MODE,
                    jmsEditorPanel.getConcurrencyMode());
            jmsOp.setAttribute(JMSOperation.ATTR_REDELIVERY_HANDLING,
                    jmsEditorPanel.getRedelivery()); 
            jmsOp.setAttribute(JMSOperation.ATTR_BATCH_SZIE, 
                    Long.toString(jmsEditorPanel.getBatchSize()));      
            //jmsOp.setAttribte(JMSOperation.ATTR_) TODO validate msg selector
            jmsOp.setAttribute(JMSOperation.ATTR_DELIVERY_MODE,
                    jmsEditorPanel.getDeliverMode());
            jmsOp.setAttribute(JMSOperation.ATTR_TIME_TO_LIVE,
                    jmsEditorPanel.getTimeToLive()); 
            jmsOp.setAttribute(JMSOperation.ATTR_DISABLE_MESSAGE_ID,
                    Boolean.toString(jmsEditorPanel.getDisableMessageID())); 
            jmsOp.setAttribute(JMSOperation.ATTR_PRIORITY,
                    jmsEditorPanel.getPriority()); 
            jmsOp.setAttribute(JMSOperation.ATTR_TIMEOUT, 
                    Long.toString(jmsEditorPanel.getTimeout()));              
            jmsOp.setAttribute(JMSOperation.ATTR_DISABLE_MESSAGE_TIMESTAMP,
                    Boolean.toString(jmsEditorPanel.
                    getDisableMessageTimestamp()));             
        }     
    }
    private void commitInputJMSMessage(JMSMessage jmsMessage) {
        if (jmsMessage != null) {
            jmsMessage.setMessageType(jmsEditorPanel.
                    getInputMessageType());
            jmsMessage.setTextPart(jmsEditorPanel.
                    getInputMessageText());
            jmsMessage.setCorrelationIdPart(jmsEditorPanel.
                    getInputCorrelationPart());
            jmsMessage.setDeliveryModePart(jmsEditorPanel.
                    getInputDeliveryModePart());
            jmsMessage.setPriorityPart(jmsEditorPanel.
                    getInputPriorityPart());
            jmsMessage.setTypePart(jmsEditorPanel.
                    getInputTypePart());
            jmsMessage.setMessageIDPart(jmsEditorPanel.
                    getInputMessageIDPart());
            jmsMessage.setRedeliveredPart(jmsEditorPanel.
                    getInputRedeliveredPart());
            jmsMessage.setTimestampPart(jmsEditorPanel.
                    getInputTimestamp());
            jmsMessage.setUse(jmsEditorPanel.
                    getInputUse());
            jmsMessage.setJMSEncodingStyle(jmsEditorPanel.
                    getInputEncodingStyle());            
        }
    }
    
    private void commitOutputJMSMessage(JMSMessage jmsMessage) {
        if (jmsMessage != null) {
            jmsMessage.setMessageType(jmsEditorPanel.
                    getOutputMessageType());
            jmsMessage.setTextPart(jmsEditorPanel.
                    getOutputMessageText());
            jmsMessage.setCorrelationIdPart(jmsEditorPanel.
                    getOutputCorrelationPart());
            jmsMessage.setDeliveryModePart(jmsEditorPanel.
                    getOutputDeliveryModePart());
            jmsMessage.setPriorityPart(jmsEditorPanel.
                    getOutputPriorityPart());
            jmsMessage.setTypePart(jmsEditorPanel.
                    getOutputTypePart());
            jmsMessage.setMessageIDPart(jmsEditorPanel.
                    getOutputMessageIDPart());
            jmsMessage.setRedeliveredPart(jmsEditorPanel.
                    getOutputRedeliveredPart());
            jmsMessage.setTimestampPart(jmsEditorPanel.
                    getOutputTimestamp());
            jmsMessage.setUse(jmsEditorPanel.
                    getOutputUse());
            jmsMessage.setJMSEncodingStyle(jmsEditorPanel.
                    getOutputEncodingStyle());            
        }
    }
    
    public void commitServices(JMSAddress jmsAddress) {
        if (jmsAddress != null) {
            jmsAddress.setAttribute(JMSAddress.ATTR_CONNECTION_URL,
                    jmsEditorPanel.getConnectionURL()); 
            jmsAddress.setAttribute(JMSAddress.ATTR_USERNAME,
                    jmsEditorPanel.getUserName());
            jmsAddress.setAttribute(JMSAddress.ATTR_PASSWORD,
                    jmsEditorPanel.getPassword()); 
            jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_CONNECTION_FACTORY_NAME,
                    jmsEditorPanel.getConnectionFactoryName());
            jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_INITIAL_CONTEXT_FACTORY,
                    jmsEditorPanel.getInitialContextFactory());
            jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_PROVIDER_URL,
                    jmsEditorPanel.getProviderURL());
            jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_SECURITY_PRINCIPAL,
                    jmsEditorPanel.getSecurityPrincipal());
            jmsAddress.setAttribute(JMSAddress.ATTR_JNDI_SECURITY_CRDENTIALS,
                    jmsEditorPanel.getSecurityCredentials());                  
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
    
    private void commitBindingOperations(Collection<BindingOperation> bops,
            String operationName) {
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
                    BindingInput bi = bop.getBindingInput();
                    List<JMSMessage> inputJMSMessages =
                            bi.getExtensibilityElements(JMSMessage.class);
                    JMSMessage inputJMSMessage = inputJMSMessages.get(0);
                    
                    // save the jms input message info
                    commitInputJMSMessage(inputJMSMessage);

                    BindingOutput bo = bop.getBindingOutput();
                    List<JMSMessage> outputJMSMessages =
                            bo.getExtensibilityElements(JMSMessage.class);
                    JMSMessage outputJMSMessage = outputJMSMessages.get(0);
                    
                    //save the jms output message info
                    commitOutputJMSMessage(outputJMSMessage);
                }
            }            
        }
    }
    
    public boolean isValid() {
        return jmsEditorPanel.validateContent();
    }    
}
