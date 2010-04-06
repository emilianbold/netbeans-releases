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
 *
 * Created on August 31, 2006, 3:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.bindingsupport.configeditor.ConfigurationEditorProviderFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ValidationInfo;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.windows.IOProvider;

/**
 *
 * @author radval
 */
public class WizardBindingConfigurationEditorStep implements WizardDescriptor.FinishablePanel {
    private String mErrorMessage;
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    private WizardDescriptor wiz = null;
   
    private WSDLModel mTempModel;
   
    private Port mPort;
    
    private String namespace;
    private ExtensibilityElementConfigurationEditorComponent editorComponent;
    private boolean committed;
    private LocalizedTemplate bindingSubType;
    private boolean validating;
    private JPanel panel;
    
    public WizardBindingConfigurationEditorStep() {
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    public Component getComponent() {
        ExtensibilityElementConfigurationEditorProvider configurationProvider = ConfigurationEditorProviderFactory.getDefault().getConfigurationProvider(namespace);
        QName qname = new QName(namespace, "address");
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BorderLayout());
        }
        panel.removeAll();
        if (configurationProvider != null && configurationProvider.isConfigurationSupported(qname)) {
            editorComponent = configurationProvider.getComponent(qname, mPort);
            if (editorComponent != null) {
                JPanel p = editorComponent.getEditorPanel();

                panel.add(p, BorderLayout.CENTER);
                panel.setName(p.getName());
                panel.getAccessibleContext().setAccessibleDescription(p.getName());
                p.addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getNewValue() instanceof String) {
                            String message = (String) evt.getNewValue();
                            if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
                                mErrorMessage = message;
                                fireChangeEvent();
                            } else if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_WARNING_EVT)) {
                            } else if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT)) {
                                mErrorMessage = null;
                                fireChangeEvent();
                            } else if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_NORMAL_MESSAGE_EVT)) {
                            }
                        }
                    }
                });

                return panel;
            }
        }
        panel.setName("Configure Binding Parameters");
        JLabel label = new JLabel();
        label.setText("Please configure other properties through wsdl editor,since there is no configuration editor is provided. Please click on Finish to create the wsdl.");
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
    

    
    
    public HelpCtx getHelp() {
        return new HelpCtx(WizardBindingConfigurationEditorStep.class);
    }

    public boolean isValid() {
        if (editorComponent != null && mErrorMessage == null && !validating) {
            validate();
        }
        wiz.putProperty ("WizardPanel_errorMessage", mErrorMessage); // NOI18N
        return this.mErrorMessage == null;
    }

    void validate() {
        validating = true;
        List<ValidationInfo> vAllInfos =new ArrayList<ValidationInfo>();
        String targetNamespace = (String) wiz.getProperty(WsdlPanel.WSDL_TARGETNAMESPACE);
        Binding binding = mPort.getBinding().get();
        List<ValidationInfo> vBindingInfos = bindingSubType.getMProvider().validate(binding);
        if (vBindingInfos != null) {
            vAllInfos.addAll(vBindingInfos);
        }

        if (this.mPort != null) {
            List<ValidationInfo> vPortInfos = bindingSubType.getMProvider().validate(this.mPort);
            if (vPortInfos != null) {
                vAllInfos.addAll(vPortInfos);
            }
        }
        
        String error = null;
        if (vAllInfos.size() > 0) {
            ValidationInfo vInfo = vAllInfos.get(0);
            error = vInfo.getDescription();
            IOProvider.getDefault().getStdOut().print(error);
        } else {
            //no errors
            error = null;
            if (binding != null) {
                bindingSubType.getMProvider().postProcess(targetNamespace, binding);
            }
            if (this.mPort != null) {
                bindingSubType.getMProvider().postProcess(targetNamespace, this.mPort);
            }
        }
        
        if (editorComponent != null && !editorComponent.isValid()) {
            
        }
        
        
        if (mErrorMessage == null) {
            mErrorMessage = error;
            fireChangeEvent();
        }
        
        validating = false;
    }
    
    public void readSettings(Object settings) {
        TemplateWizard templateWizard = (TemplateWizard)settings;
        wiz = templateWizard;
        bindingSubType = (LocalizedTemplate) wiz.getProperty(WSDLWizardConstants.BINDING_SUBTYPE);
        if (namespace == null || !namespace.equals(bindingSubType.getTemplateGroup().getNamespace())) {
            namespace = bindingSubType.getTemplateGroup().getNamespace();
            editorComponent = null;
        }
        
        mPort = (Port) wiz.getProperty(WSDLWizardConstants.PORT);
        mTempModel = (WSDLModel) wiz.getProperty(WSDLWizardConstants.TEMP_WSDLMODEL);
    }

    public void storeSettings(Object settings) {
        if (editorComponent == null) return;
        validate();
        TemplateWizard templateWizard = (TemplateWizard)settings;
        if(templateWizard.getValue() == NotifyDescriptor.CANCEL_OPTION) {
            editorComponent.rollback();
            mPort = null;
            editorComponent = null;
            cleanup();
            return;
        }
        
        if (templateWizard.getValue() == WizardDescriptor.PREVIOUS_OPTION) {
            editorComponent.rollback();
            mPort = null;
            editorComponent = null;
            return;
        }
        
        if (!committed) {
            try {
                mTempModel.startTransaction();
                if (!editorComponent.commit()) {
                    mErrorMessage = "Cannot Commit";
                    mTempModel.rollbackTransaction();
                }
            } finally {
                committed = true;
                mTempModel.endTransaction();
            }

        }

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
    private void fireChangeEvent() {
        Iterator<ChangeListener> it = this.listeners.iterator();
        ChangeEvent e = new ChangeEvent(this);
        while(it.hasNext()) {
            ChangeListener l = it.next();
            l.stateChanged(e);
        }
    }

    public boolean isFinishPanel() {
        return true;
    }
    
}

