/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this ldap except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each ldap and include the License ldap at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular ldap as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License ldap that
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
 * your version of this ldap under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.ldap.configeditor;

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.ldap.LDAPAddress;
import org.netbeans.modules.wsdlextensions.ldap.LDAPBinding;
//import org.netbeans.modules.wsdlextensions.ldap.LDAPConstants;
import org.netbeans.modules.wsdlextensions.ldap.LDAPComponent;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperation;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperationInput;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperationOutput;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * @author skini
 */
public class LDAPConfigurationEditorComponent 
        implements ExtensibilityElementConfigurationEditorComponent {

    LDAPBindingConfigurationPanel ldapEditorPanel = null;
    private WSDLComponent component;
    private QName mQName;
    
    
    public LDAPConfigurationEditorComponent(QName qName,
            WSDLComponent component) {
        ldapEditorPanel = new LDAPBindingConfigurationPanel(qName, component);
        mQName = qName;
        this.component = component;
        ldapEditorPanel.setName(getTitle());
    }

    /**
     * Return the main panel
     * @return
     */
    public JPanel getEditorPanel() {
        return ldapEditorPanel;
    }

    /**
     * Return the title
     * @return
     */
    public String getTitle() {
        return NbBundle.getMessage(LDAPConfigurationEditorComponent.class,
                "LDAPConfigurationEditorPanel.CONFIGURE_TITLE");                                 //NOI18N
    }

    /**
     * Return the Help
     * @return
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Return the action listeners if any
     * @return
     */
    public ActionListener getActionListener() {
        return null;
    }
    public void setOperation(Operation operation) {
        if ((ldapEditorPanel != null) && (operation != null)) {
            ldapEditorPanel.setOperationName(operation.getName());
        }
    }
    /**
     * Commit all changes
     * @return
     */
    public boolean commit() {
        if (!ldapEditorPanel.validateMe()) {
            return false;
        }
        if (component instanceof LDAPAddress) {
            return commitAddress((LDAPAddress) component);
        } else if (component instanceof LDAPBinding) {
            return commitBinding((LDAPBinding) component);
        } else if (component instanceof Port) {
            return commitPort((Port) component);
        } else if (component instanceof LDAPOperationInput) {
            return commitInputMessage((LDAPOperationInput) component);
        } else if (component instanceof LDAPOperation) {
            return commitOperation((LDAPOperation) component);
        }else if (component instanceof LDAPOperationOutput) {
            return commitOutputMessage((LDAPOperationOutput) component);
        }
        return false;
    }

    /**
     * Rollback any changes
     * @return
     */
    public boolean rollback() {
        return true;
    }
    
    private boolean commitAddress(LDAPAddress ldapAddress) { 
        WSDLModel wsdlModel = ldapAddress.getModel();
        try {            
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }
            ldapAddress.setAttribute(LDAPAddress.LDAP_LOCATION_PROPERTY,
                    ldapEditorPanel.getLDAPLocation());
            ldapAddress.setAttribute(LDAPAddress.LDAP_PRINCIPAL_PROPERTY,
                    ldapEditorPanel.getLDAPPrincipalProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_CREDENTIAL_PROPERTY,
                    ldapEditorPanel.getCredentials());
            ldapAddress.setAttribute(LDAPAddress.LDAP_SSLTYPE_PROPERTY,
                    ldapEditorPanel.getsslTypeProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_AUTHENTICATION_PROPERTY,
                    ldapEditorPanel.getAuthenticationProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_PROTOCOL_PROPERTY,
                    ldapEditorPanel.getProtocol());
            ldapAddress.setAttribute(LDAPAddress.LDAP_TRUSTSTORE_PROPERTY,
                    ldapEditorPanel.getTrustStoreProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_TRUSTSTOREPASSWORD_PROPERTY,
                    ldapEditorPanel.getTrustStorePasswordProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_TRUSTSTORETYPE_PROPERTY,
                    ldapEditorPanel.getTrustStoreTypeProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_KEYSTORE_PROPERTY,
                    ldapEditorPanel.getKeyStoreProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_KEYSTOREUSERNAME_PROPERTY,
                    ldapEditorPanel.getKeyStoreUserNameProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_KEYSTOREPASSWORD_PROPERTY,
                    ldapEditorPanel.getKeyStorePasswordProperty());       
            ldapAddress.setAttribute(LDAPAddress.LDAP_KEYSTORETYPE_PROPERTY,
                    ldapEditorPanel.getKeyStoreTypeProperty());  
            //ldapAddress.setAttribute(LDAPAddress.LDAP_TLSSECURITY_PROPERTY,
            //        ldapEditorPanel.getTLSSecrurityProperty());
                    
            Port port = (Port) ldapAddress.getParent();
            Binding binding = port.getBinding().get();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = ldapEditorPanel.getOperationName();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    BindingInput bi = bop.getBindingInput();
                    if (bi != null) {
                        List<LDAPOperationInput> inputLDAPMessages =
                                bi.getExtensibilityElements(LDAPOperationInput.class);
                        if (inputLDAPMessages.size() > 0) {
                            LDAPOperationInput inputLDAPMessage =
                                    inputLDAPMessages.get(0);
                            commitInputLDAPMessage(inputLDAPMessage);
                        }
                    }
                    
                    BindingOutput bo = bop.getBindingOutput();
                    if (bo != null) {
                        List<LDAPOperationOutput> outputLDAPMessages =
                                bo.getExtensibilityElements(LDAPOperationOutput.class);
                        if (outputLDAPMessages.size() > 0) {
                            LDAPOperationOutput outputLDAPMessage =
                                    outputLDAPMessages.get(0);
                            commitOutputLDAPMessage(outputLDAPMessage);
                        }
                    }
                }
            }
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
    

    private boolean commitBinding(LDAPBinding ldapBinding) {
        WSDLModel wsdlModel = ldapBinding.getModel();
        try {            
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }
            LDAPAddress ldapAddress = ldapEditorPanel.getLDAPAddressPerSelectedPort();
            if (ldapAddress != null) {
                ldapAddress.setAttribute(LDAPAddress.LDAP_LOCATION_PROPERTY,
                    ldapEditorPanel.getLDAPLocation());
            ldapAddress.setAttribute(LDAPAddress.LDAP_PRINCIPAL_PROPERTY,
                    ldapEditorPanel.getLDAPPrincipalProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_CREDENTIAL_PROPERTY,
                    ldapEditorPanel.getCredentials());
            ldapAddress.setAttribute(LDAPAddress.LDAP_SSLTYPE_PROPERTY,
                    ldapEditorPanel.getsslTypeProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_AUTHENTICATION_PROPERTY,
                    ldapEditorPanel.getAuthenticationProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_PROTOCOL_PROPERTY,
                    ldapEditorPanel.getProtocol());
            ldapAddress.setAttribute(LDAPAddress.LDAP_TRUSTSTORE_PROPERTY,
                    ldapEditorPanel.getTrustStoreProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_TRUSTSTOREPASSWORD_PROPERTY,
                    ldapEditorPanel.getTrustStorePasswordProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_TRUSTSTORETYPE_PROPERTY,
                    ldapEditorPanel.getTrustStoreTypeProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_KEYSTORE_PROPERTY,
                    ldapEditorPanel.getKeyStoreProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_KEYSTOREUSERNAME_PROPERTY,
                    ldapEditorPanel.getKeyStoreUserNameProperty());
            ldapAddress.setAttribute(LDAPAddress.LDAP_KEYSTOREPASSWORD_PROPERTY,
                    ldapEditorPanel.getKeyStorePasswordProperty());       
            ldapAddress.setAttribute(LDAPAddress.LDAP_KEYSTORETYPE_PROPERTY,
                    ldapEditorPanel.getKeyStoreTypeProperty());                  
            }
            Binding binding = (Binding) ldapBinding.getParent();
            
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = ldapEditorPanel.getOperationName();
            for (BindingOperation bop : bindingOperations) {
                 if (bop.getName().equals(operationName)) {
                    BindingInput bi = bop.getBindingInput();
                    if (bi != null) {
                        List<LDAPOperationInput> inputLDAPMessages =
                                bi.getExtensibilityElements(LDAPOperationInput.class);
                        if (inputLDAPMessages.size() > 0) {
                            LDAPOperationInput inputLDAPMessage =
                                    inputLDAPMessages.get(0);
                            commitInputLDAPMessage(inputLDAPMessage);
                        }
                    }
                    BindingOutput bo = bop.getBindingOutput();
                    if (bo != null) {
                        List<LDAPOperationOutput> outputLDAPMessages =
                                bo.getExtensibilityElements(LDAPOperationOutput.class);
                        if (outputLDAPMessages.size() > 0) {
                            LDAPOperationOutput outputLDAPMessage =
                                    outputLDAPMessages.get(0);
                            commitOutputLDAPMessage(outputLDAPMessage);
                        }
                    }
                }
            }  
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

    private void commitInputLDAPMessage(LDAPOperationInput inputLDAPMessage) {
      //  throw new UnsupportedOperationException("Not yet implemented");
      //  inputLDAPMessage.setAttribute(LDAPOperationInput.LDAP_OPERATIONTYPE_PROPERTY, ldapEditorPanel.getOperationType());
    }
   
    private void commitLDAPOperation(LDAPOperation ldapoperation) {
       // throw new UnsupportedOperationException("Not yet implemented");
        
    }

    private void commitOutputLDAPMessage(LDAPOperationOutput outputLDAPMessage) {
        //throw new UnsupportedOperationException("Not yet implemented");
        outputLDAPMessage.setAttribute(LDAPOperationOutput.LDAP_RETPARTNAME_PROPERTY, ldapEditorPanel.getOutputPartName());
        //outputLDAPMessage.setAttribute(LDAPOperationOutput.LDAP_ATTRIBUTES_PROPERTY, ldapEditorPanel.getAttributes());
    }
    
   

    private boolean commitPort(Port port) {
        Collection<LDAPAddress> address = port.
                getExtensibilityElements(LDAPAddress.class);
        LDAPAddress ldapAddress = address.iterator().next();
        return commitAddress(ldapAddress);
    }
    
    private boolean commitInputMessage(LDAPOperationInput ldapInputMessage) {
        Object parentObj = ldapInputMessage.getParent();
        LDAPBinding ldapBinding = null;
        BindingOperation parentOp = null;
        if (parentObj instanceof BindingInput) {
            parentOp = (BindingOperation) ((BindingInput) parentObj).getParent();
        } else if (parentObj instanceof  BindingOutput) {
            parentOp = (BindingOperation) ((BindingOutput) parentObj).getParent();
        }
        if (parentObj != null) {
            Binding parentBinding = (Binding) parentOp.getParent();
            Collection<LDAPBinding> bindings = parentBinding.getExtensibilityElements(LDAPBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }
    
    private boolean commitOutputMessage(LDAPOperationOutput ldapOutputMessage) {
        Object parentObj = ldapOutputMessage.getParent();
        LDAPBinding ldapBinding = null;
        BindingOperation parentOp = null;
        if (parentObj instanceof BindingInput) {
            parentOp = (BindingOperation) ((BindingInput) parentObj).getParent();
        } else if (parentObj instanceof  BindingOutput) {
            parentOp = (BindingOperation) ((BindingOutput) parentObj).getParent();
        }
        if (parentObj != null) {
            Binding parentBinding = (Binding) parentOp.getParent();
            Collection<LDAPBinding> bindings = parentBinding.getExtensibilityElements(LDAPBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }
     
    private boolean commitOperation(LDAPOperation ldapOperation) {
        Object obj = ldapOperation.getParent();
        if (obj instanceof BindingOperation) {
            Binding parentBinding = (Binding) ((BindingOperation) obj).getParent();
            Collection<LDAPBinding> bindings = parentBinding.getExtensibilityElements(LDAPBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }    
    
    /**
     * Check if the model is valid or not
     * @return boolean true if model is valid; otherwise false
     */
    public boolean isValid() {
        return ldapEditorPanel.validateContent();
    }
}
