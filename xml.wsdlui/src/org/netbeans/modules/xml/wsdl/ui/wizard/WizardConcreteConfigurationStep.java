/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

/*
 * WSDLWizardConstants.java
 *
 * Created on August 31, 2006, 3:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import org.netbeans.modules.xml.wsdl.ui.wizard.common.BindingGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardConstants;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.netbeans.modules.xml.wsdl.bindingsupport.configeditor.ConfigurationEditorProviderFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ValidationInfo;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.BindingConfigurationPanel;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.OperationPanel;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;

/**
 *
 * @author radval
 */
public class WizardConcreteConfigurationStep extends WSDLWizardDescriptorPanel implements WSDLWizardConstants  {
    
    private BindingConfigurationPanel mPanel;
    
    private String mErrorMessage;
    
    private String mBindingSubTypeError;
    
    private WizardDescriptor wiz = null;
   
    private WSDLModel mTempModel;
    
    private PortType mPortType;
    
    private Binding mBinding;
    
    private Service mService;
    
    private Port mPort;
    private boolean hasNextStep = true;
    private String fileName;
    
    /** Creates a new instance of WSDLWizardConstants */
    public WizardConcreteConfigurationStep(WSDLWizardContext context) {
        super(context);
    }

    public Component getComponent() {
        if (mPanel == null) {
            this.mPanel = new BindingConfigurationPanel();
            
            TextChangeListener listener  = new TextChangeListener();
            this.mPanel.getBindingNameTextField().getDocument().addDocumentListener(listener);
            this.mPanel.getServiceNameTextField().getDocument().addDocumentListener(listener);
            this.mPanel.getServicePortTextField().getDocument().addDocumentListener(listener);
                    
            if (this.mPanel.getBindingName() == null || this.mPanel.getBindingName().trim().equals("")) {
                this.mPanel.setBindingName(fileName + "Binding"); //NOI18N
            }
            if (this.mPanel.getServiceName() == null || this.mPanel.getServiceName().trim().equals("")) {
                this.mPanel.setServiceName(fileName + "Service"); //NOI18N
            }
            if (this.mPanel.getServicePortName() == null || this.mPanel.getServicePortName().trim().equals("")) {
                this.mPanel.setServicePortName(fileName + "Port"); //NOI18N
            }
            this.mPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WizardConcreteConfigurationStep.class, "LBL_WizardBindingConfigurationStep"));
            
        }
        return this.mPanel;
    }

    
    
    public HelpCtx getHelp() {
        return new HelpCtx(WizardConcreteConfigurationStep.class);
    }

    public boolean isValid() {
/*        String errorMessage = null;
        //This should be good enough to disable html code.
        // If not try to use the StringEscapeUtils.escapeHtml from common lang.
        if (mErrorMessage != null) {
            errorMessage = "<html>" + Utility.escapeHtml(mErrorMessage) + "</html>";
        }*/
        
        wiz.putProperty ("WizardPanel_errorMessage", mErrorMessage); // NOI18N
        return this.mErrorMessage == null;

    }

    public void readSettings(Object settings) {
        TemplateWizard templateWizard = (TemplateWizard)settings;
        wiz = templateWizard;
        fileName = (String) templateWizard.getProperty(WsdlPanel.FILE_NAME);
        
        this.mPortType = (PortType) templateWizard.getProperty(WizardAbstractConfigurationStep.PORTTYPE);
        this.mTempModel = (WSDLModel) templateWizard.getProperty(WizardAbstractConfigurationStep.TEMP_WSDLMODEL);
        LocalizedTemplateGroup group = (LocalizedTemplateGroup) templateWizard.getProperty(BINDING_TYPE);
        LocalizedTemplate template = (LocalizedTemplate) templateWizard.getProperty(BINDING_SUBTYPE);
        processBindingSubType(group, template, true);
        
    }

    public void storeSettings(Object settings) {
        TemplateWizard templateWizard = (TemplateWizard)settings;
        if(templateWizard.getValue() == NotifyDescriptor.CANCEL_OPTION) {
            cleanup();
            return;
        }
        
        if (templateWizard.getValue() == WizardDescriptor.PREVIOUS_OPTION) {
            mTempModel.startTransaction();
            cleanUpBindings();
            mTempModel.endTransaction();
            templateWizard.putProperty(BINDING_NAME, null);
            templateWizard.putProperty(SERVICE_NAME, null);
            templateWizard.putProperty(SERVICEPORT_NAME, null);
            return;
        }
        LocalizedTemplateGroup group = (LocalizedTemplateGroup) templateWizard.getProperty(BINDING_TYPE);
        LocalizedTemplate template = (LocalizedTemplate) templateWizard.getProperty(BINDING_SUBTYPE);
        processBindingSubType(group, template, false);
        
        
        String bindingName = this.mPanel.getBindingName();
        String serviceName = this.mPanel.getServiceName();
        String servicePortName = this.mPanel.getServicePortName();
        
        templateWizard.putProperty(BINDING_NAME, bindingName);
        templateWizard.putProperty(SERVICE_NAME, serviceName);
        templateWizard.putProperty(SERVICEPORT_NAME, servicePortName);
    }
    
    void cleanup() {
        DataObject dobj = ActionHelper.getDataObject(mTempModel);
        if (dobj != null) {
            dobj.setModified(false);
            try {
                dobj.delete();
            } catch (Exception e) {
                //ignore
            }
        }
    }
    
    private void cleanUpBindings() {
        if(this.mBinding != null) {
            this.mTempModel.getDefinitions().removeBinding(this.mBinding);
        }
        
        if(this.mService != null) {
            this.mTempModel.getDefinitions().removeService(this.mService);
        }
        
        mBinding = null;
        mService = null;
    }

    private boolean isValidName(Document doc) {
        try {
            String text = doc.getText(0, doc.getLength());
            boolean isValid  = org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(text);
            if(!isValid) {
                mErrorMessage = NbBundle.getMessage(OperationPanel.class, "ERR_MSG_INVALID_NAME" , text);
            } else {
                mErrorMessage = null;
            }
            
            fireChange();
        }  catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return mErrorMessage == null;
    }
    
    private void validate() {
        if (mPanel != null) {
            boolean isValidBinding = isValidName(this.mPanel.getBindingNameTextField().getDocument());
            if (!isValidBinding) {
                fireChange();
                return;
            }

            boolean isValidService = isValidName(this.mPanel.getServiceNameTextField().getDocument());
            if (!isValidService) {
                fireChange();
                return;
            }

            boolean isValidPort = isValidName(this.mPanel.getServicePortTextField().getDocument());
            if (!isValidPort) {
                fireChange();
                return;
            }
        }
        
        if(this.mBindingSubTypeError != null) {
            this.mErrorMessage = this.mBindingSubTypeError;
            fireChange();
            return;
        }
        
        fireChange();
    }
    
    public boolean isFinishPanel() {
        return true;
    }
    
    private void processBindingSubType(LocalizedTemplateGroup group, LocalizedTemplate bindingSubType, boolean validateOnly) {
        if(bindingSubType != null) {
            String bindingName = "binding1";
            //service and port
            String serviceName = "service1";
            String servicePortName = "port1";
            if (mPanel != null) {
                bindingName = this.mPanel.getBindingName();
                //service and port
                serviceName = this.mPanel.getServiceName();
                servicePortName = this.mPanel.getServicePortName();
            }

            
            Map configurationMap = new HashMap();
            configurationMap.put(WizardConcreteConfigurationStep.BINDING_NAME, bindingName);
            configurationMap.put(WizardConcreteConfigurationStep.BINDING_TYPE, group);
           
            //this could be null for a binding which does not have a sub type
            configurationMap.put(WizardConcreteConfigurationStep.BINDING_SUBTYPE, bindingSubType);
            
            //service and port
            configurationMap.put(WizardConcreteConfigurationStep.SERVICE_NAME, serviceName);
            configurationMap.put(WizardConcreteConfigurationStep.SERVICEPORT_NAME, servicePortName);
            
            this.mTempModel.startTransaction();
            cleanUpBindings();
            
            BindingGenerator bGen = new BindingGenerator(this.mTempModel, this.mPortType, configurationMap);
            bGen.execute();
            
            this.mBinding = bGen.getBinding();
            this.mService = bGen.getService();
            this.mPort = bGen.getPort();
            
            if(this.mBinding != null) {
                String targetNamespace = (String) wiz.getProperty(WsdlPanel.WSDL_TARGETNAMESPACE);
                List<ValidationInfo> vAllInfos =new ArrayList<ValidationInfo>();
                
                List<ValidationInfo> vBindingInfos = bindingSubType.getMProvider().validate(this.mBinding);
                if(vBindingInfos != null) {
                    vAllInfos.addAll(vBindingInfos);
                }
                
                if(this.mPort != null) {
                    List<ValidationInfo> vPortInfos = bindingSubType.getMProvider().validate(this.mPort);
                    if(vPortInfos != null) {
                        vAllInfos.addAll(vPortInfos);
                    }
                }
                if(vAllInfos.size() > 0) {
                    ValidationInfo vInfo = vAllInfos.get(0);
                    this.mBindingSubTypeError =  vInfo.getDescription();
                    IOProvider.getDefault().getStdOut().print(this.mBindingSubTypeError);
                    validate();
                } else {
                    //no errors
                	this.mBindingSubTypeError =  null;
                    validate();
                    if(this.mBinding != null) {
                        bindingSubType.getMProvider().postProcess(targetNamespace, this.mBinding);
                    }
                    if(this.mPort != null) {
                        bindingSubType.getMProvider().postProcess(targetNamespace, this.mPort);
                    }
                    this.wiz.putProperty(BINDING, this.mBinding);
                    this.wiz.putProperty(SERVICE, this.mService);
                    this.wiz.putProperty(PORT, this.mPort);
                }
            }
            
            if (validateOnly) {
                mTempModel.rollbackTransaction();
                mBinding = null;
                mService = null;
                mPort = null;
            } else {
                mTempModel.endTransaction();
            }
            String namespace = bindingSubType.getTemplateGroup().getNamespace();
            ExtensibilityElementConfigurationEditorProvider configurationProvider = ConfigurationEditorProviderFactory.getDefault().getConfigurationProvider(namespace);
            hasNextStep = configurationProvider != null;
        }
    }
    
    class TextChangeListener implements DocumentListener {
     
         public void changedUpdate(DocumentEvent e) {
            validate();
         }
         
         public void insertUpdate(DocumentEvent e) {
             validate();
         }

         public void removeUpdate(DocumentEvent e) {
             validate();
         }
    
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(WizardConcreteConfigurationStep.class, "LBL_WizardBindingConfigurationStep");
    }
}

