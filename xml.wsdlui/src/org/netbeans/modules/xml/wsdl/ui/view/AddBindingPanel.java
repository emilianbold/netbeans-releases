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

/*
 * BindingAndServiceConfigurationPanel.java
 *
 * Created on August 25, 2006, 2:51 PM
 */
package org.netbeans.modules.xml.wsdl.ui.view;

import java.awt.CardLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyUtil;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;

/**
 *
 * @author  radval
 */
public class AddBindingPanel extends javax.swing.JPanel {

    public static final String PROP_BINDING_TYPE = "PROP_BINDING_TYPE";
    public static final String PROP_BINDING_SUBTYPE = "PROP_BINDING_SUBTYPE";
    public static final String PROP_AUTOCREATE_SERVICE = "PROP_AUTOCREATE_SERVICE";
    private ExtensibilityElementTemplateFactory factory;
    private Vector<LocalizedTemplateGroup> protocols = new Vector<LocalizedTemplateGroup>();
    private LocalizedTemplateGroup defaultSelection; //Select SOAP as default
    private DocumentListener serviceNameDocListener;

    /** Creates new form BindingAndServiceConfigurationPanel */
    public AddBindingPanel() {
        factory = ExtensibilityElementTemplateFactory.getDefault();
        Collection<TemplateGroup> groups = factory.getExtensibilityElementTemplateGroups();
        protocols = new Vector<LocalizedTemplateGroup>();

        SortedSet<LocalizedTemplateGroup> set = new TreeSet<LocalizedTemplateGroup>();
        for (TemplateGroup group : groups) {
            LocalizedTemplateGroup ltg = factory.getLocalizedTemplateGroup(group);
            if (ltg.getNamespace().equals(SOAPQName.SOAP_NS_URI)) {
                defaultSelection = ltg;
            }
            set.add(ltg);
        }

        protocols.addAll(set);

        initComponents();
        initGUI();
    }

    boolean canAutoCreateServicePort() {
        return shouldCreateServicePort.isSelected();
    }

    void setAutoCreateServicePort(boolean enabled) {
        shouldCreateServicePort.setSelected(enabled);
        enableServicePort();

    }


    void setPanelEnabled(boolean enabled) {
        bindingNameLabel.setEnabled(enabled);
        bindingNameTextField.setEnabled(enabled);
        bindingSubTypeLabel.setEnabled(enabled);
        bindingSubTypePanel.setEnabled(enabled);
        subTypePanel.setPanelEnabled(enabled);
        bindingTypeComboBox.setEnabled(enabled);
        bindingTypeLabel.setEnabled(enabled);
        portNameLabel.setEnabled(enabled);
        portTypeComboPanel.setEnabled(enabled);
        portTypeLabel.setEnabled(enabled);
        portTypeNameLabel.setEnabled(enabled);
        portTypeSelectionComboBox.setEnabled(enabled);
        serviceComboPanel.setEnabled(enabled);
        serviceNameLabel.setEnabled(enabled);
        serviceNameTextField.setEnabled(enabled);
        servicePortTextField.setEnabled(enabled);
        serviceSelectionComboBox.setEnabled(enabled);
        shouldCreateServicePort.setEnabled(enabled);
    }

    void setServiceNameDocumentListener(DocumentListener serviceNameListener) {
        serviceNameDocListener = serviceNameListener;
    }

    private void enableServicePort() {
        boolean selected = shouldCreateServicePort.isSelected();
        serviceNameLabel.setEnabled(selected);
        portNameLabel.setEnabled(selected);
        serviceComboPanel.setEnabled(selected);



        serviceNameTextField.setEnabled(selected);
        serviceSelectionComboBox.setEnabled(selected);

        servicePortTextField.setEnabled(selected);
        ComboBoxEditor editor = serviceSelectionComboBox.getEditor();

        if (editor instanceof JTextComponent) {
            ((JTextComponent) editor).getDocument().removeDocumentListener(serviceNameDocListener);
            ((JTextComponent) editor).getDocument().addDocumentListener(serviceNameDocListener);
        }
        
        firePropertyChange(PROP_AUTOCREATE_SERVICE, !shouldCreateServicePort.isSelected(), shouldCreateServicePort.isSelected());
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        portTypeNameLabel = new javax.swing.JLabel();
        portTypeComboPanel = new javax.swing.JPanel();
        portTypeSelectionComboBox = new javax.swing.JComboBox();
        portTypeLabel = new javax.swing.JLabel();
        bindingNameLabel = new javax.swing.JLabel();
        bindingNameTextField = new javax.swing.JTextField();
        bindingTypeLabel = new javax.swing.JLabel();
        bindingTypeComboBox = new javax.swing.JComboBox();
        bindingSubTypeLabel = new javax.swing.JLabel();
        bindingSubTypePanel = new javax.swing.JPanel();
        shouldCreateServicePort = new javax.swing.JCheckBox();
        serviceComboPanel = new javax.swing.JPanel();
        serviceNameTextField = new javax.swing.JTextField();
        serviceSelectionComboBox = new javax.swing.JComboBox();
        portNameLabel = new javax.swing.JLabel();
        serviceNameLabel = new javax.swing.JLabel();
        servicePortTextField = new javax.swing.JTextField();

        setName("AddBindingPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portTypeNameLabel, org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.portTypeNameLabel.text")); // NOI18N
        portTypeNameLabel.setName("portTypeNameLabel"); // NOI18N

        portTypeComboPanel.setName("portTypeHolderPanel"); // NOI18N
        portTypeComboPanel.setLayout(new java.awt.CardLayout());

        portTypeSelectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        portTypeSelectionComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.portTypeSelectionComboBox.toolTipText")); // NOI18N
        portTypeSelectionComboBox.setName("portTypeSelectionComboBox"); // NOI18N
        portTypeComboPanel.add(portTypeSelectionComboBox, "combobox");
        portTypeSelectionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.portTypeSelectionComboBox.AccessibleContext.accessibleName")); // NOI18N
        portTypeSelectionComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.portTypeSelectionComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.portTypeLabel.text")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N
        portTypeComboPanel.add(portTypeLabel, "label");

        bindingNameLabel.setLabelFor(bindingNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.bindingNameLabel.text")); // NOI18N
        bindingNameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.bindingNameLabel.toolTipText")); // NOI18N
        bindingNameLabel.setName("bindingNameLabel"); // NOI18N

        bindingNameTextField.setName("bindingNameTextField"); // NOI18N

        bindingTypeLabel.setLabelFor(bindingTypeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(bindingTypeLabel, org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.bindingTypeLabel.text")); // NOI18N
        bindingTypeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.bindingTypeLabel.toolTipText")); // NOI18N
        bindingTypeLabel.setName("bindingTypeLabel"); // NOI18N

        DefaultComboBoxModel model = new DefaultComboBoxModel(protocols);
        model.setSelectedItem(defaultSelection);
        bindingTypeComboBox.setModel(model);
        bindingTypeComboBox.setName("bindingTypeComboBox"); // NOI18N
        bindingTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bindingTypeComboBoxActionPerformed(evt);
            }
        });

        bindingSubTypeLabel.setLabelFor(bindingSubTypePanel);
        org.openide.awt.Mnemonics.setLocalizedText(bindingSubTypeLabel, org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.bindingSubTypeLabel.text")); // NOI18N
        bindingSubTypeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.bindingSubTypeLabel.toolTipText")); // NOI18N
        bindingSubTypeLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        bindingSubTypeLabel.setName("bindingSubTypeLabel"); // NOI18N

        bindingSubTypePanel.setName("bindingSubTypePanel"); // NOI18N
        bindingSubTypePanel.setLayout(new javax.swing.BoxLayout(bindingSubTypePanel, javax.swing.BoxLayout.X_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(shouldCreateServicePort, org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.shouldCreateServicePort.text")); // NOI18N
        shouldCreateServicePort.setToolTipText(org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.shouldCreateServicePort.toolTipText")); // NOI18N
        shouldCreateServicePort.setName("shouldCreateServicePort"); // NOI18N
        shouldCreateServicePort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shouldCreateServicePortActionPerformed(evt);
            }
        });

        serviceComboPanel.setEnabled(false);
        serviceComboPanel.setName("serviceHolderPanel"); // NOI18N
        serviceComboPanel.setLayout(new java.awt.CardLayout());

        serviceNameTextField.setEnabled(false);
        serviceNameTextField.setName("serviceNameTextField"); // NOI18N
        serviceComboPanel.add(serviceNameTextField, "textfield");

        serviceSelectionComboBox.setEditable(true);
        serviceSelectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        serviceSelectionComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.serviceSelectionComboBox.toolTipText")); // NOI18N
        serviceSelectionComboBox.setEnabled(false);
        serviceSelectionComboBox.setName("serviceSelectionComboBox"); // NOI18N
        serviceComboPanel.add(serviceSelectionComboBox, "combobox");
        serviceSelectionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.serviceSelectionComboBox.AccessibleContext.accessibleName")); // NOI18N

        portNameLabel.setLabelFor(servicePortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(portNameLabel, org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.portNameLabel.text")); // NOI18N
        portNameLabel.setEnabled(false);
        portNameLabel.setName("portNameLabel"); // NOI18N

        serviceNameLabel.setLabelFor(serviceComboPanel);
        org.openide.awt.Mnemonics.setLocalizedText(serviceNameLabel, org.openide.util.NbBundle.getMessage(AddBindingPanel.class, "AddBindingPanel.serviceNameLabel.text")); // NOI18N
        serviceNameLabel.setEnabled(false);
        serviceNameLabel.setName("serviceNameLabel"); // NOI18N

        servicePortTextField.setEnabled(false);
        servicePortTextField.setName("servicePortTextField"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, bindingTypeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, bindingNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(portTypeNameLabel)
                            .add(bindingSubTypeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(bindingSubTypePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, bindingNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                            .add(bindingTypeComboBox, 0, 265, Short.MAX_VALUE)
                            .add(portTypeComboPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)))
                    .add(shouldCreateServicePort)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(serviceNameLabel)
                            .add(portNameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(servicePortTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                            .add(serviceComboPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(portTypeNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(portTypeComboPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bindingNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bindingNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bindingTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bindingTypeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bindingSubTypePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                    .add(bindingSubTypeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(shouldCreateServicePort)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serviceNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(serviceComboPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(portNameLabel)
                    .add(servicePortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    private void bindingTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bindingTypeComboBoxActionPerformed
        setBindingSubType(getBindingType());
        this.firePropertyChange(PROP_BINDING_TYPE, null, getBindingType());
        Window windowAncestor = SwingUtilities.getWindowAncestor(this);
        if (windowAncestor != null) {
            windowAncestor.pack();
        }
    }//GEN-LAST:event_bindingTypeComboBoxActionPerformed

    private void shouldCreateServicePortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shouldCreateServicePortActionPerformed
        enableServicePort();
}//GEN-LAST:event_shouldCreateServicePortActionPerformed

    public String getBindingName() {
        return this.bindingNameTextField.getText();
    }

    public void setBindingName(String bindingName) {
        this.bindingNameTextField.setText(bindingName);
    }

    public LocalizedTemplateGroup getBindingType() {
        return (LocalizedTemplateGroup) bindingTypeComboBox.getSelectedItem();
    }

    public void setBindingType(String bindingSubType) {
        this.bindingTypeComboBox.setSelectedItem(bindingSubType);
    }

    public LocalizedTemplate getBindingSubType() {
        return subTypePanel.getBindingSubType();
    }

    private void setBindingSubType(LocalizedTemplateGroup bindingType) {
        subTypePanel.reset(bindingType);
    }

    public String getServiceName() {
        if (serviceComboCurrentCard.equals("textfield")) {
            return this.serviceNameTextField.getText();
        } else {
            return (String) serviceSelectionComboBox.getSelectedItem();
        }
    }

    public void setServiceName(String serviceName) {
        if (serviceComboCurrentCard.equals("textfield")) {
            serviceNameTextField.getDocument().removeDocumentListener(serviceNameDocListener);
            this.serviceNameTextField.setText(serviceName);
            serviceNameTextField.getDocument().addDocumentListener(serviceNameDocListener);
        } else {
            ComboBoxEditor editor = serviceSelectionComboBox.getEditor();
            if (editor instanceof JTextComponent) {
                ((JTextComponent) editor).getDocument().removeDocumentListener(serviceNameDocListener);
                serviceSelectionComboBox.setSelectedItem(serviceName);
                ((JTextComponent) editor).getDocument().addDocumentListener(serviceNameDocListener);
            }
        }
    }

    public String getServicePortName() {
        return servicePortTextField.getText();
    }

    public void setServicePortName(String servicePortName) {
        this.servicePortTextField.setText(servicePortName);
    }

    public JTextField getBindingNameTextField() {
        return this.bindingNameTextField;
    }

    public JTextField getServicePortTextField() {
        return this.servicePortTextField;
    }

    private void initGUI() {
        if (protocols.size() > 0) {
            subTypePanel = new BindingSubTypePanel(defaultSelection, new BindingSubTypeActionListener());
            bindingSubTypePanel.add(subTypePanel);
        }
    }

    class BindingSubTypeActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            firePropertyChange(PROP_BINDING_SUBTYPE, null, getBindingSubType());
        }
    }

    public void initPortTypeSelection(WSDLModel mModel, PortType portType) {
        this.mModel = mModel;
        CardLayout layout = (CardLayout) portTypeComboPanel.getLayout();
        if (portType == null) {
            String[] portTypes = PropertyUtil.getAllPortTypes(mModel, false);
            DefaultComboBoxModel model = new DefaultComboBoxModel(portTypes);


            portTypeSelectionComboBox.setModel(model);

            if (portTypes == null || portTypes.length == 0) {
                portTypeComboCurrentCard = "label";
                setEnabled(false);
            } else if (portTypes.length == 1) {
                portTypeComboCurrentCard = "label";
                portTypeLabel.setText(portTypes[0]);
            } else if (portTypes.length > 1) {
                portTypeComboCurrentCard = "combobox";
                portTypeSelectionComboBox.setSelectedIndex(0);
            }
        } else {
            QName qname = Utility.getQNameForWSDLComponent(portType, mModel);
            if (qname != null) {
                portTypeLabel.setText(Utility.fromQNameToString(qname));
            }

            portTypeComboCurrentCard = "label";
        }
        layout.show(portTypeComboPanel, portTypeComboCurrentCard);


        Collection<Service> services = mModel.getDefinitions().getServices();
        CardLayout servicesComboLayout = (CardLayout) serviceComboPanel.getLayout();

        if (services == null || services.isEmpty()) {
            serviceComboCurrentCard = "textfield";
        } else {
            serviceComboCurrentCard = "combobox";

            Vector<String> serviceNames = new Vector<String>();
            for (Service service : services) {
                serviceNames.add(service.getName());
            }
            DefaultComboBoxModel sModel = new DefaultComboBoxModel(serviceNames);
            serviceSelectionComboBox.setModel(sModel);
            serviceSelectionComboBox.setSelectedIndex(0);
        }
        servicesComboLayout.show(serviceComboPanel, serviceComboCurrentCard);

    }

    public void setWSDLModel(WSDLModel mModel) {
        this.mModel = mModel;
    }

    WSDLModel getWSDLModel() {
        return mModel;
    }

    public JComboBox getPortTypeSelectionComboBox() {
        return portTypeSelectionComboBox;
    }

    public PortType getSelectedPortType() {
        String portTypeName = (String) portTypeSelectionComboBox.getSelectedItem();
        if (portTypeName == null) {
            return null;
        }

        String localName = org.netbeans.modules.xml.wsdl.ui.common.QName.getLocalName(portTypeName);
        String namespace = org.netbeans.modules.xml.wsdl.ui.common.QName.getNamespaceURI(portTypeName);
        String prefix = org.netbeans.modules.xml.wsdl.ui.common.QName.getPrefix(portTypeName);
        QName qname = null;
        if (namespace != null) {
            qname = new QName(namespace, localName);
        } else if (prefix != null) {
            namespace = Utility.getNamespaceURI(prefix, getWSDLModel());
            if (namespace != null) {
                qname = new QName(namespace, localName, prefix);
            }
        } else {
            qname = new QName(localName);
        }

        return getWSDLModel().findComponentByName(qname, PortType.class);
    }
    private String portTypeComboCurrentCard = "textfield";
    private String serviceComboCurrentCard = "textfield";
    private WSDLModel mModel;
    private BindingSubTypePanel subTypePanel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JTextField bindingNameTextField;
    private javax.swing.JLabel bindingSubTypeLabel;
    private javax.swing.JPanel bindingSubTypePanel;
    private javax.swing.JComboBox bindingTypeComboBox;
    private javax.swing.JLabel bindingTypeLabel;
    private javax.swing.JLabel portNameLabel;
    private javax.swing.JPanel portTypeComboPanel;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JLabel portTypeNameLabel;
    private javax.swing.JComboBox portTypeSelectionComboBox;
    private javax.swing.JPanel serviceComboPanel;
    private javax.swing.JLabel serviceNameLabel;
    private javax.swing.JTextField serviceNameTextField;
    private javax.swing.JTextField servicePortTextField;
    private javax.swing.JComboBox serviceSelectionComboBox;
    private javax.swing.JCheckBox shouldCreateServicePort;
    // End of variables declaration//GEN-END:variables
}
