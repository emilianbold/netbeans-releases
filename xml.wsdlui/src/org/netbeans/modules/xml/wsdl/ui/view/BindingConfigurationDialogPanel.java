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
 * BindingConfigurationDialogPanel.java
 *
 * Created on September 8, 2006, 2:22 PM
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.bindingsupport.configeditor.ConfigurationEditorProviderFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ValidationInfo;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.OperationPanel;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.BindingGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardConstants;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;

/**
 *
 * @author  skini
 */
public class BindingConfigurationDialogPanel extends javax.swing.JPanel {

    private String mErrorMessage = null;
    private ServiceNameChangeListener serviceNameListener;
    private BindingNameChangeListener bindingNameListener;
    private ServicePortNameChangeListener servicePortNameListener;
    private final PortType mPortType;
    private String configurationPanelErrorMessage = null;

    private ExtensibilityElementConfigurationEditorComponent editorComponent;
    /** Creates new form BindingConfigurationDialogPanel */
    public BindingConfigurationDialogPanel(WSDLModel model) {
        mModel = model;
        mPortType = null;
        initComponents();
        initGUI();
    }

    public BindingConfigurationDialogPanel(WSDLModel model, PortType portType) {
        mModel = model;
        mPortType = portType;
        initComponents();
        initGUI();
    }

    public boolean canAutoCreateServicePort() {
        return addBindingPanel.canAutoCreateServicePort();
    }
    
    public void setAutoCreateServicePort(boolean enabled) {
        addBindingPanel.setAutoCreateServicePort(enabled);
    }

    private void cleanupArtifacts() {
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addBindingPanel = new org.netbeans.modules.xml.wsdl.ui.view.AddBindingPanel();
        bindingConfigurationPanel = new javax.swing.JPanel();
        bindingConfigurationLabel = new javax.swing.JLabel();
        commonMessagePanel = new org.netbeans.modules.xml.wsdl.bindingsupport.common.CommonMessagePanel();

        setName("BindingConfigurationDialogPanel"); // NOI18N

        addBindingPanel.setName("addBindingPanel"); // NOI18N

        bindingConfigurationPanel.setName("bindingConfigurationPanel"); // NOI18N
        bindingConfigurationPanel.setLayout(new java.awt.BorderLayout());

        bindingConfigurationLabel.setText(org.openide.util.NbBundle.getMessage(BindingConfigurationDialogPanel.class, "BindingConfigurationDialogPanel.bindingConfigurationLabel.text")); // NOI18N
        bindingConfigurationLabel.setName("bindingConfigurationLabel"); // NOI18N

        commonMessagePanel.setName("ShowErrorWarningPanel"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(bindingConfigurationLabel)
                .addContainerGap(316, Short.MAX_VALUE))
            .add(addBindingPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
            .add(bindingConfigurationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(commonMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(addBindingPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bindingConfigurationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bindingConfigurationPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(commonMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    private void initGUI() {
        bindingNameListener = new BindingNameChangeListener();
        serviceNameListener = new ServiceNameChangeListener();
        servicePortNameListener = new ServicePortNameChangeListener();

        
        
        this.addBindingPanel.setServiceNameDocumentListener(serviceNameListener);

        addBindingPanel.initPortTypeSelection(mModel, mPortType);

        NameGenerator nameGen = NameGenerator.getInstance();
        PortType pt = getPortType();
        String portTypeName = null;
        if (pt != null) {
            portTypeName = pt.getName();
        } else {
            mErrorMessage = NbBundle.getMessage(BindingConfigurationDialogPanel.class, "ERR_MSG_NoPortTypeForCreatingNewBinding");
            addBindingPanel.setPanelEnabled(false);
            updateMessagePanel();
        }
        String bindingName = null;
        if (portTypeName != null) {
            bindingName = portTypeName + "Binding";
        } else {
            bindingName = nameGen.generateUniqueBindingName(mModel);
        }
        if (bindingName != null) {
            if (nameGen.isBindingExists(bindingName, mModel)) {
                bindingName = nameGen.generateUniqueBindingName(bindingName, mModel);
            }
        }
        setBindingName(bindingName);

        FileObject fo = mModel.getModelSource().getLookup().lookup(FileObject.class);
        String svcName = fo.getName() + "Service";
        Definitions def = mModel.getDefinitions();
        Collection<Service> services = def.getServices();
        Service service = null;
        if (services != null && !services.isEmpty()) {
            service = services.iterator().next();
            svcName = service.getName();
        }

        setServiceName(svcName);
        String portName = getBindingName() + "Port";
        if (service != null) {
            if (nameGen.isServicePortExists(portName, service)) {
                portName = nameGen.generateUniqueServicePortName(portName, service);
            }
        }
        setServicePortName(portName);

        addBindingPanel.getPortTypeSelectionComboBox().addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    PortType pt = getPortType();
                    validateAll();
                    boolean bindingNameChangedHere = false;
                    if (!bindingNameChanged) {
                        String bindingName = pt.getName() + "Binding";
                        NameGenerator nGen = NameGenerator.getInstance();
                        if (nGen.isBindingExists(bindingName, mModel)) {
                            bindingName = nGen.generateUniqueBindingName(bindingName, mModel);
                        }
                        if (!getBindingName().equals(bindingName)) {
                            setBindingName(bindingName);
                            bindingNameChangedHere = true;
                        }
                    }

                    if (bindingNameChangedHere && !portNameChanged) {
                        String portName = getBindingName() + "Port";
                        NameGenerator nGen = NameGenerator.getInstance();
                        Service service = mModel.findComponentByName(getServiceName(), Service.class);
                        if (service != null) {
                            if (nGen.isServicePortExists(portName, service)) {
                                portName = nGen.generateUniqueServicePortName(portName, service);
                            }
                        }
                        if (!getServicePortName().equals(portName)) {
                            setServicePortName(portName);
                        }
                    }
                }
            }
        });


        PropertyChangeListener propListener = new BindingConfigurationListener();
        addBindingPanel.addPropertyChangeListener(propListener);
        
        bindingConfigurationLabel.setVisible(false);
        bindingConfigurationPanel.setVisible(false);
        
        validateAll();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        processBindingSubType(addBindingPanel.getBindingSubType());
    }

    
    
    public String getBindingName() {
        return addBindingPanel.getBindingName();
    }

    public void setBindingName(String bindingName) {
        addBindingPanel.getBindingNameTextField().getDocument().removeDocumentListener(bindingNameListener);
        addBindingPanel.setBindingName(bindingName);
        addBindingPanel.getBindingNameTextField().getDocument().addDocumentListener(bindingNameListener);

    }

    public void setServiceName(String svcName) {
        addBindingPanel.setServiceName(svcName);
    }

    public void setServicePortName(String string) {
        addBindingPanel.getServicePortTextField().getDocument().removeDocumentListener(servicePortNameListener);
        addBindingPanel.setServicePortName(string);
        addBindingPanel.getServicePortTextField().getDocument().addDocumentListener(servicePortNameListener);
    }

    public LocalizedTemplateGroup getBindingType() {
        return addBindingPanel.getBindingType();
    }

    public LocalizedTemplate getBindingSubType() {
        return addBindingPanel.getBindingSubType();
    }

    public String getServiceName() {
        return addBindingPanel.getServiceName();
    }

    public String getServicePortName() {
        return addBindingPanel.getServicePortName();
    }

    public PortType getPortType() {
        if (mPortType != null) {
            return mPortType;
        }
        return addBindingPanel.getSelectedPortType();
    }

    public void setDialogDescriptor(DialogDescriptor dd) {
        this.mDD = dd;
        updateMessagePanel();
    }

    private boolean isValidName(String text) {
        try {
            boolean isValid = org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(text);
            if (!isValid) {
                mErrorMessage = NbBundle.getMessage(OperationPanel.class, "ERR_MSG_INVALID_NAME", text);
            } else {
                mErrorMessage = null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return mErrorMessage == null;
    }

    private void validateAll() {
        boolean isPortTypeValid = validatePortType();
        if (!isPortTypeValid) {
            updateMessagePanel();
            return;
        }
        

        if (mBindingSubTypeError != null) {
            mErrorMessage = mBindingSubTypeError;
            updateMessagePanel();
            return;
        }

        boolean validBinding = isValidName(this.addBindingPanel.getBindingName());
        if (!validBinding) {
            updateMessagePanel();
            return;
        }


        boolean isBindingExist = isBindingExists();
        if (isBindingExist) {
            updateMessagePanel();
            return;
        }

        if (canAutoCreateServicePort()) {
            boolean validService = isValidName(this.addBindingPanel.getServiceName());
            if (!validService) {
                updateMessagePanel();
                return;
            }


            boolean validPort = isValidName(this.addBindingPanel.getServicePortName());
            if (!validPort) {
                updateMessagePanel();
                return;
            }


            String serviceName = this.addBindingPanel.getServiceName();
            String portName = this.addBindingPanel.getServicePortName();

            boolean isServicePortExist = isServicePortExists(serviceName, portName);
            if (isServicePortExist) {
                updateMessagePanel();
                return;
            }
        }
        
        if (configurationPanelErrorMessage != null) {
            mErrorMessage = configurationPanelErrorMessage;
            updateMessagePanel();
            return;
        }
        
        this.mErrorMessage = null;
        updateMessagePanel();

    }

    private boolean validatePortType() {
        Set<String> operationNames = new HashSet<String>();
        PortType pt = getPortType();
        if (pt == null) {
            return false;
        }
        for (Operation operation : pt.getOperations()) {
            String opName = operation.getName();
            String inputName = "";
            try { 
                inputName = operation.getInput().getName();
            } catch (Exception e) {
                
            }
            String outputName = "";
            try { 
                outputName = operation.getOutput().getName();
            } catch (Exception e) {
                
            }
            
            String opUniqueName = opName + inputName + outputName;
            if (operationNames.contains(opUniqueName)) {
                mErrorMessage = NbBundle.getMessage(BindingConfigurationDialogPanel.class, "ERR_MSG_ImproperlyOverloadedOperations");
                return false;
            } else {
                operationNames.add(opUniqueName);
            }
        }
        return true;
    }

    private boolean isBindingExists() {
        boolean exist = false;

        String text = this.addBindingPanel.getBindingName();
        Binding b = mModel.findComponentByName(text, Binding.class);

        if (b != null) {
            this.mErrorMessage = NbBundle.getMessage(BindingConfigurationDialogPanel.class, "ERR_MSG_BindingAlreadyExists", text);
            exist = true;
        }

        return exist;
    }

    public boolean isServicePortExists(String serviceName, String portName) {
        boolean exist = false;
        if (serviceName != null && portName != null) {
            Service service = mModel.findComponentByName(serviceName, Service.class);
            if (service != null) {
                exist = NameGenerator.getInstance().isServicePortExists(getServicePortName(), service);
                if (exist) {
                    this.mErrorMessage = NbBundle.getMessage(BindingConfigurationDialogPanel.class, "ERR_MSG_ServicePortAlreadyExists", getServicePortName());
                }
            }
        }
        return exist;
    }

    private void updateMessagePanel() {
        if (this.mErrorMessage != null) {
            commonMessagePanel.setErrorMessage(mErrorMessage);
            if (this.mDD != null) {
                mDD.setValid(false);
            }
        //firePropertyChange(APPLY_CHANGE, !commonMessagePanel1.isStateValid(), commonMessagePanel1.isStateValid());
        } else {
            commonMessagePanel.setMessage("");
            if (mDD != null) {
                mDD.setValid(true);
            }
        //firePropertyChange(APPLY_CHANGE, !commonMessagePanel1.isStateValid(), commonMessagePanel1.isStateValid());
        }
        Window windowAncestor = SwingUtilities.getWindowAncestor(this);
        if (windowAncestor != null) {
            windowAncestor.pack();
        }
    }

    class BindingNameChangeListener implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            bindingChanged();
        }

        public void insertUpdate(DocumentEvent e) {
            bindingChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            bindingChanged();
        }

        private void bindingChanged() {
            bindingNameChanged = true;
            validateAll();
        }
    }

    class ServiceNameChangeListener implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            serviceChanged();
        }

        public void insertUpdate(DocumentEvent e) {
            serviceChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            serviceChanged();
        }

        private void serviceChanged() {
            validateAll();
        }
    }

    class ServicePortNameChangeListener implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            portChanged();
        }

        public void insertUpdate(DocumentEvent e) {
            portChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            portChanged();
        }

        private void portChanged() {
            portNameChanged = true;
            validateAll();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.xml.wsdl.ui.view.AddBindingPanel addBindingPanel;
    private javax.swing.JLabel bindingConfigurationLabel;
    private javax.swing.JPanel bindingConfigurationPanel;
    private org.netbeans.modules.xml.wsdl.bindingsupport.common.CommonMessagePanel commonMessagePanel;
    // End of variables declaration//GEN-END:variables
    private WSDLModel mModel;
    private DialogDescriptor mDD;
    private boolean bindingNameChanged = false;
    private boolean portNameChanged = false;
    private String mBindingSubTypeError;

    
        
    
    class BindingConfigurationListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if(AddBindingPanel.PROP_BINDING_SUBTYPE.equals(propertyName)) {
                LocalizedTemplate bindingSubType = (LocalizedTemplate) evt.getNewValue();
                processBindingSubType(bindingSubType);
            } else if(AddBindingPanel.PROP_BINDING_TYPE.equals(propertyName)) {
                processBindingSubType(getBindingSubType());
            } else if (AddBindingPanel.PROP_AUTOCREATE_SERVICE.equals(propertyName)) {
                validateAll();
            }
        }
    }
    
    private void processBindingSubType(LocalizedTemplate bindingSubType) {
        if(bindingSubType != null) {
            String bindingName = getBindingName();
            LocalizedTemplateGroup bindingType = getBindingType();
            
            //service and port
            String serviceName = getServiceName();
            String servicePortName = getServicePortName();
            
            Map configurationMap = new HashMap();
            configurationMap.put(WSDLWizardConstants.BINDING_NAME, bindingName);
            configurationMap.put(WSDLWizardConstants.BINDING_TYPE, bindingType);
           
            //this could be null for a binding which does not have a sub type
            configurationMap.put(WSDLWizardConstants.BINDING_SUBTYPE, bindingSubType);
            
            //service and port
            configurationMap.put(WSDLWizardConstants.SERVICE_NAME, serviceName);
            configurationMap.put(WSDLWizardConstants.SERVICEPORT_NAME, servicePortName);
            configurationMap.put(WSDLWizardConstants.AUTO_CREATE_SERVICEPORT, canAutoCreateServicePort());
            cleanupArtifacts();
            if (mModel.isIntransaction()) {
                mModel.rollbackTransaction();
            }
            mModel.startTransaction();
            BindingGenerator bGen = new BindingGenerator(mModel, getPortType(), configurationMap);
            bGen.execute();

            Binding binding = bGen.getBinding();
            Port port = bGen.getPort();

            if (binding != null) {
                String targetNamespace = mModel.getDefinitions().getTargetNamespace();
                List<ValidationInfo> vAllInfos = new ArrayList<ValidationInfo>();

                List<ValidationInfo> vBindingInfos = bindingSubType.getMProvider().validate(binding);
                if (vBindingInfos != null) {
                    vAllInfos.addAll(vBindingInfos);
                }

                if (port != null) {
                    List<ValidationInfo> vPortInfos = bindingSubType.getMProvider().validate(port);
                    if (vPortInfos != null) {
                        vAllInfos.addAll(vPortInfos);
                    }
                }
                if (vAllInfos.size() > 0) {
                    ValidationInfo vInfo = vAllInfos.get(0);
                    mBindingSubTypeError = vInfo.getDescription();
                    IOProvider.getDefault().getStdOut().print(mBindingSubTypeError);
                    mErrorMessage = mBindingSubTypeError;
                    updateMessagePanel();
                } else {
                    //no errors
                    this.mBindingSubTypeError = null;
                    mErrorMessage = mBindingSubTypeError;
                    updateMessagePanel();
                    bindingSubType.getMProvider().postProcess(targetNamespace, binding);
                    if (port != null) {
                        bindingSubType.getMProvider().postProcess(targetNamespace, port);
                    }
                }
                
            }
            mModel.rollbackTransaction();
//            String namespace = getBindingType().getNamespace();
//            QName qname = new QName(namespace, "address");
//            if (port != null) {
//                Collection<ExtensibilityElement> coll = port.getExtensibilityElements();
//                if (coll != null && !coll.isEmpty()) {
//                    ExtensibilityElement elem = coll.iterator().next();
//                    qname = elem.getQName();
//                }
//            }
//            ExtensibilityElementConfigurationEditorProvider configurationProvider = ConfigurationEditorProviderFactory.getDefault().getConfigurationProvider(namespace);
//            configurationProvider = null;
//            if (configurationProvider != null && configurationProvider.isConfigurationSupported(qname)) {
//                editorComponent = configurationProvider.getComponent(qname, port);
//                JPanel panel = editorComponent.getEditorPanel();
//                panel.addPropertyChangeListener(new PropertyChangeListener() {
//
//                    public void propertyChange(PropertyChangeEvent evt) {
//                        if (evt.getNewValue() instanceof String) {
//                            String message = (String) evt.getNewValue();
//                            if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
//                                commonMessagePanel.setErrorMessage(message);
//                            } else if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_WARNING_EVT)) {
//                                commonMessagePanel.setWarningMessage(message);
//                            } else if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT)) {
//                                commonMessagePanel.setMessage("");
//                            } else if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_NORMAL_MESSAGE_EVT)) {
//                                commonMessagePanel.setMessage(message);
//                            }
//                        }
//                    }
//                });
//                bindingConfigurationPanel.removeAll();
//                bindingConfigurationPanel.add(panel, BorderLayout.CENTER);
//                bindingConfigurationLabel.setVisible(true);
//                bindingConfigurationPanel.setVisible(true);
//
//            } else {
//                bindingConfigurationLabel.setVisible(false);
//                bindingConfigurationPanel.setVisible(false);
//            }
//            Window windowAncestor = SwingUtilities.getWindowAncestor(this);
//            if (windowAncestor != null) {
//                windowAncestor.pack();
//            }
        }
    }
    
    public boolean commit() {
        mModel.startTransaction();
        Map configurationMap = new HashMap();
        configurationMap.put(WSDLWizardConstants.BINDING_NAME, getBindingName());
        configurationMap.put(WSDLWizardConstants.BINDING_TYPE, getBindingType());

        //this could be null for a binding which does not have a sub type
        configurationMap.put(WSDLWizardConstants.BINDING_SUBTYPE, getBindingSubType());

        //service and port
        configurationMap.put(WSDLWizardConstants.SERVICE_NAME, getServiceName());
        configurationMap.put(WSDLWizardConstants.SERVICEPORT_NAME, getServicePortName());
        configurationMap.put(WSDLWizardConstants.AUTO_CREATE_SERVICEPORT, canAutoCreateServicePort());
        BindingGenerator bGen = new BindingGenerator(mModel, getPortType(), configurationMap);
        bGen.execute();
        
        boolean commit = true;
        if (editorComponent != null) {
             commit = editorComponent.commit();
        }
        return commit;
    }
    
    public boolean rollback() {
        if (mModel.isIntransaction()) {
            mModel.rollbackTransaction();
        }
        if (editorComponent != null) {
            return editorComponent.rollback();
        }
        return true;
    }

//    public void doesBindingExist() {
//        boolean exists = NameGenerator.getInstance().isBindingExists(getBindingName(), mModel);
//        if (commonMessagePanel1.isStateValid()) {
//            if (exists)
//                commonMessagePanel1.setErrorMessage("Binding Name " + getBindingName() + " already exists.");
//        }
//    }
//
//    public boolean doesServiceExists() {
//        return NameGenerator.getInstance().isServiceExists(getServiceName(), mModel);
//    }
//    
//    public void doesServicePortExists() {
//        if (doesServiceExists()) {
//            Service service = mModel.findComponentByName(getServiceName(), Service.class);
//            boolean exists = NameGenerator.getInstance().isServicePortExists(getServicePortName(), service);
//            if (commonMessagePanel1.isStateValid()) {
//                if (exists)
//                    commonMessagePanel1.setErrorMessage("Service port" + getServicePortName() + " already exists.");
//            }
//        }
//    }
}
