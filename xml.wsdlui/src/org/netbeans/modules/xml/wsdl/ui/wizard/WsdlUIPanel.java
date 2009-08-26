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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.view.BindingSubTypePanel;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.BindingUtils;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.BindingUtils.Type;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/** WsdlUIPanel.java - bottom panel for WSDL wizard
 *
 * @author  mkuchtiak
 */
public class WsdlUIPanel extends javax.swing.JPanel {

    public static final String PROP_BINDING_TYPE = "PROP_BINDING_TYPE";
    public static final String PROP_BINDING_SUBTYPE = "PROP_BINDING_SUBTYPE";
    private static final String TARGET_URL_PREFIX = NbBundle.getMessage(WsdlUIPanel.class,"TXT_defaultTNS"); //NOI18N
    
    private WsdlPanel wizardPanel;
    private javax.swing.JTextField fileNameTF;
    
    private boolean hasUserModifiedNamespace = false;
    
    private NamespaceDocListener mListener = new NamespaceDocListener();
    private Vector<LocalizedTemplateGroup> protocols = new Vector<LocalizedTemplateGroup>();
    private ExtensibilityElementTemplateFactory factory;
    private LocalizedTemplateGroup defaultSelection; //Select SOAP as default
    
    private BindingSubTypePanel subTypePanel;
    private WSDLWizardExtensionIterator defaultIterator;
    private WSDLWizardContextImpl context;
    private InputStream currentStream;
    private String projectName;
    
    
    /** Creates new form WsdlUIPanel */
    
    WsdlUIPanel(WsdlPanel wizardPanel) {
        context = (WSDLWizardContextImpl) wizardPanel.getWSDLWizardContext();
        //Get the Binding Type from Template Wizard.
        BindingUtils.Type bcType = (Type) context.getWizardDescriptor().getProperty(BindingUtils.BINDING_EDITOR_MODE);
        //Can we reuse ExtensibilityElement Enum?. Exposing ExtensibilityElement means 
        // adding the dependent module as friend. 
        LocalizedTemplate.Mode exBCType = null;
        if (bcType != null) {
            if (bcType.equals(BindingUtils.Type.INBOUND)) {
                exBCType = LocalizedTemplate.Mode.INBOUND;
            } else if (bcType.equals(BindingUtils.Type.OUTBOUND)) {
                exBCType = LocalizedTemplate.Mode.OUTBOUND;
            } else if (bcType.equals(BindingUtils.Type.INBOUND_OUTBOUND)) {
                exBCType = LocalizedTemplate.Mode.BOTH;
            }
            
        } 
        factory = ExtensibilityElementTemplateFactory.getDefault();
        
        
        Collection<TemplateGroup> groups = factory.getExtensibilityElementTemplateGroups();
        protocols = new Vector<LocalizedTemplateGroup>();
        
        SortedSet<LocalizedTemplateGroup> set = new TreeSet<LocalizedTemplateGroup>();
        boolean bTemplateGroupMatch = false;
        for (TemplateGroup group : groups) {
            bTemplateGroupMatch = false;
            LocalizedTemplateGroup ltg = factory.getLocalizedTemplateGroup(group);
            //If the Service type mode is not null then look only for the template 
            // that matches  the request.
            if ( exBCType != null && exBCType != LocalizedTemplate.Mode.BOTH) {
                LocalizedTemplate[] allTemplates = ltg.getTemplate();
                for (LocalizedTemplate localTemp: allTemplates) {
                    if ( localTemp.getMode() == LocalizedTemplate.Mode.BOTH ||localTemp.getMode().equals(exBCType )) {
                         bTemplateGroupMatch = true;
                        //With INBOUND and OUTBOUND BCs the default maynot be 
                        //SOAP. So choosing the top one from list as default if 
                        // the default is not selected.
                        if  (defaultSelection == null) {
                            defaultSelection = ltg;
                        }
                        set.add(ltg);
                        break;
                    }
                }
            } else {
                if (ltg.getNamespace().equals(SOAPQName.SOAP_NS_URI)) {
                    defaultSelection = ltg;
                }
                 set.add(ltg);
            }
           
        }
        
        protocols.addAll(set);
        initComponents();
        this.wizardPanel=wizardPanel;
        nsTF.setText(TARGET_URL_PREFIX);
        
        if (protocols.size() > 0) {
            subTypePanel = new BindingSubTypePanel(defaultSelection,  new BindingSubTypeActionListener(),exBCType);
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
            gridBagConstraints.weightx = 0.5; 
            gridBagConstraints.weighty = 0.5;          
            bindingTypePanel.add(subTypePanel, gridBagConstraints);
        }
        recurseEnable(bindingTypePanel, false);
        defaultIterator = new DefaultWizardExtensionIterator(context);
        recurseEnable(bindingConfigurationPanel, concreteWSDLChoice.isSelected());
        bindingConfigurationPanel.setVisible(concreteWSDLChoice.isSelected());
        
        context.setHasNext(true);
        
        Project project = wizardPanel.getProject();
        projectName = project.getProjectDirectory().getName();
    }

    private void enableFinishButton(boolean selected) {
        firePropertyChange("IS_FINISHABLE", !selected, selected);
    }
    
    class BindingSubTypeActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            firePropertyChange(PROP_BINDING_SUBTYPE, null, getBindingSubType());
            bindingSubtypeChanged(getBindingSubType());
        }
    }
    
    private void bindingSubtypeChanged(LocalizedTemplate bindingSubType) {
        //change iterator.
        WSDLWizardExtensionIterator iterator = bindingSubType.getMProvider().getWSDLWizardExtensionIterator(context);
        if (iterator == null) {
            iterator = defaultIterator;
        }
        iterator.setTemplateName(bindingSubType.getDelegate().getName());
        currentStream = bindingSubType.getMProvider().getTemplateFileInputStream(bindingSubType.getWSDLTemplateFile());

        context.setWSDLExtensionIterator(iterator);
    }
    
    public InputStream getWSDLTemplateStream() {
        if (concreteWSDLChoice.isSelected()) {
            return currentStream;
        }
        return null;
    }
    
    public LocalizedTemplate getBindingSubType() {
        if (concreteWSDLChoice.isSelected()) {
            return subTypePanel.getBindingSubType();
        }
        
        return null;
    }

    private void setBindingSubType(LocalizedTemplateGroup bindingType) {
        subTypePanel.reset(bindingType);
        bindingSubtypeChanged(getBindingSubType());
    }
    
    void attachFileNameListener(javax.swing.JTextField fileNameTextField) {
        this.fileNameTF = fileNameTextField;
        if (fileNameTF!=null) {
            nsTF.setText(TARGET_URL_PREFIX+ projectName + "/" + fileNameTF.getText());
            DocListener list = new DocListener();
            javax.swing.text.Document doc = fileNameTF.getDocument();
            doc.addDocumentListener(list);
        } else {
            nsTF.setText(TARGET_URL_PREFIX);
        }
        nsTF.getDocument().addDocumentListener(mListener);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        schemaLB = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        schemaTF = new javax.swing.JTextField();
        cbImport = new javax.swing.JCheckBox();
        bindingSubtypeGroup = new javax.swing.ButtonGroup();
        wsdlTypeGroup = new javax.swing.ButtonGroup();
        namespaceLB = new javax.swing.JLabel();
        nsTF = new javax.swing.JTextField();
        bindingConfigurationPanel = new javax.swing.JPanel();
        bindingTypeLabel = new javax.swing.JLabel();
        bindingLabel = new javax.swing.JLabel();
        bindingTypePanel = new javax.swing.JPanel();
        bindingComboBox = new javax.swing.JComboBox();
        wsdlTypeLabel = new javax.swing.JLabel();
        abstractWSDLChoice = new javax.swing.JRadioButton();
        concreteWSDLChoice = new javax.swing.JRadioButton();

        schemaLB.setLabelFor(schemaTF);
        org.openide.awt.Mnemonics.setLocalizedText(schemaLB, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_schemaFiles")); // NOI18N
        schemaLB.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_schemaFiles")); // NOI18N
        schemaLB.setName("schemaLB"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_browse")); // NOI18N
        browseButton.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "A11Y_browse")); // NOI18N
        browseButton.setEnabled(false);
        browseButton.setName("browseButton"); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_browse")); // NOI18N

        schemaTF.setEditable(false);
        schemaTF.setToolTipText(org.openide.util.NbBundle.getBundle(WsdlUIPanel.class).getString("HINT_schemaFiles")); // NOI18N
        schemaTF.setName("schemaTF"); // NOI18N
        schemaTF.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_schemaFiles")); // NOI18N
        schemaTF.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "A11Y_schemaTF")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbImport, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_importSchema")); // NOI18N
        cbImport.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_schemaFiles")); // NOI18N
        cbImport.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbImport.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbImport.setName("cbImport"); // NOI18N
        cbImport.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbImportItemStateChanged(evt);
            }
        });
        cbImport.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_importSchema")); // NOI18N
        cbImport.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_schemaFiles")); // NOI18N

        setName("Form"); // NOI18N

        namespaceLB.setLabelFor(nsTF);
        org.openide.awt.Mnemonics.setLocalizedText(namespaceLB, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_targetNamespace")); // NOI18N
        namespaceLB.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_targetNamespace")); // NOI18N
        namespaceLB.setName("namespaceLB"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/wsdl/ui/wizard/Bundle"); // NOI18N
        nsTF.setToolTipText(bundle.getString("HINT_targetNamespace")); // NOI18N
        nsTF.setName("nsTF"); // NOI18N

        bindingConfigurationPanel.setName("bindingConfigurationPanel"); // NOI18N

        bindingTypeLabel.setLabelFor(bindingTypePanel);
        org.openide.awt.Mnemonics.setLocalizedText(bindingTypeLabel, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.bindingTypeLabel.text")); // NOI18N
        bindingTypeLabel.setEnabled(false);
        bindingTypeLabel.setName("bindingTypeLabel"); // NOI18N

        bindingLabel.setLabelFor(bindingComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(bindingLabel, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.bindingLabel.text")); // NOI18N
        bindingLabel.setEnabled(false);
        bindingLabel.setName("bindingLabel"); // NOI18N

        bindingTypePanel.setName("bindingTypePanel"); // NOI18N
        bindingTypePanel.setLayout(new java.awt.GridBagLayout());

        DefaultComboBoxModel model = new DefaultComboBoxModel(protocols);
        bindingComboBox.setModel(model);
        bindingComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.bindingComboBox.toolTipText")); // NOI18N
        bindingComboBox.setEnabled(false);
        bindingComboBox.setName("bindingComboBox"); // NOI18N
        model.setSelectedItem(defaultSelection);
        bindingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bindingComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout bindingConfigurationPanelLayout = new org.jdesktop.layout.GroupLayout(bindingConfigurationPanel);
        bindingConfigurationPanel.setLayout(bindingConfigurationPanelLayout);
        bindingConfigurationPanelLayout.setHorizontalGroup(
            bindingConfigurationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bindingConfigurationPanelLayout.createSequentialGroup()
                .add(bindingConfigurationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bindingLabel)
                    .add(bindingTypeLabel))
                .add(62, 62, 62)
                .add(bindingConfigurationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bindingComboBox, 0, 497, Short.MAX_VALUE)
                    .add(bindingTypePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)))
        );
        bindingConfigurationPanelLayout.setVerticalGroup(
            bindingConfigurationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bindingConfigurationPanelLayout.createSequentialGroup()
                .add(bindingConfigurationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bindingLabel)
                    .add(bindingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bindingConfigurationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bindingTypePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                    .add(bindingTypeLabel))
                .addContainerGap())
        );

        bindingLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.bindingLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bindingComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.bindingComboBox.AccessibleContext.accessibleName")); // NOI18N

        wsdlTypeLabel.setLabelFor(abstractWSDLChoice);
        org.openide.awt.Mnemonics.setLocalizedText(wsdlTypeLabel, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.wsdlTypeLabel.text")); // NOI18N
        wsdlTypeLabel.setName("wsdlTypeLabel"); // NOI18N

        wsdlTypeGroup.add(abstractWSDLChoice);
        abstractWSDLChoice.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(abstractWSDLChoice, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.abstractWSDLChoice.text")); // NOI18N
        abstractWSDLChoice.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.abstractWSDLChoice.toolTipText")); // NOI18N
        abstractWSDLChoice.setActionCommand(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.abstractWSDLChoice.actionCommand")); // NOI18N
        abstractWSDLChoice.setName("abstractWSDLChoice"); // NOI18N
        abstractWSDLChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abstractWSDLChoiceActionPerformed(evt);
            }
        });

        wsdlTypeGroup.add(concreteWSDLChoice);
        org.openide.awt.Mnemonics.setLocalizedText(concreteWSDLChoice, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.concreteWSDLChoice.text")); // NOI18N
        concreteWSDLChoice.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.concreteWSDLChoice.toolTipText")); // NOI18N
        concreteWSDLChoice.setActionCommand(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.concreteWSDLChoice.actionCommand")); // NOI18N
        concreteWSDLChoice.setName("concreteWSDLChoice"); // NOI18N
        concreteWSDLChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                concreteWSDLChoiceActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(namespaceLB)
                            .add(wsdlTypeLabel))
                        .add(5, 5, 5)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(abstractWSDLChoice)
                            .add(concreteWSDLChoice)
                            .add(nsTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)))
                    .add(bindingConfigurationPanel, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(namespaceLB))
                    .add(nsTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wsdlTypeLabel)
                    .add(abstractWSDLChoice))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(concreteWSDLChoice)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bindingConfigurationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        namespaceLB.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_targetNamespace")); // NOI18N
        namespaceLB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_targetNamespace")); // NOI18N
        nsTF.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.nsTF.AccessibleContext.accessibleName")); // NOI18N
        nsTF.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_targetNamespace")); // NOI18N
        wsdlTypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.wsdlTypeLabel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // Create a temporary file and model for the import creator to use.
        Project project = wizardPanel.getProject();
        FileObject prjdir = project.getProjectDirectory();
        //XXX:SKINI: Relook this hack
        // HACK: hard-coded NB project directory name
        FileObject privdir = prjdir.getFileObject("nbproject/private");
        // We prefer to use the private directory, but at the very
        // least our file needs to be inside the project.
        File directory = FileUtil.toFile(privdir != null ? privdir : prjdir);
        String fname = fileNameTF.getText();
        if (fname == null || fname.length() == 0) {
            fname = "wizard";
        }
        File file = null;
        try {
            file = File.createTempFile(fname, ".wsdl", directory);
            wizardPanel.populateFileFromTemplate(file);
        } catch (Exception e) {
            // This is quite unexpected.
            ErrorManager.getDefault().notify(e);
            if (file != null) {
                file.delete();
            }
            return;
        }
        WSDLModel model = wizardPanel.prepareModelFromFile(file, fname);
        model.startTransaction();
        WSDLSchema wsdlSchema = model.getFactory().createWSDLSchema();
        Definitions defs = model.getDefinitions();
        defs.getTypes().addExtensibilityElement(wsdlSchema);
        SchemaModel schemaModel = wsdlSchema.getSchemaModel();
        Schema schema = schemaModel.getSchema();
        // Must set namespace on embedded schema for import dialog to work.
        schema.setTargetNamespace(defs.getTargetNamespace());
        model.endTransaction();

        // Use a specialized import creator for selecting files.
        String original = schemaTF.getText().trim();
        ImportSchemaCreator creator = new ImportSchemaCreator(schema, model, original);
        DialogDescriptor descriptor = UIUtilities.getCreatorDialog(
                creator, NbBundle.getMessage(WsdlUIPanel.class,
                "TITLE_selectSchema"), true);
        descriptor.setValid(false);
        Object result = DialogDisplayer.getDefault().notify(descriptor);
        if (result == DialogDescriptor.OK_OPTION) {
            String selections = creator.getSelectedFiles();
            schemaTF.setText(selections);
            schemaTF.firePropertyChange("VALUE_SET", false, true);
        }

        // Must use DataObject to delete the temporary file.
        file = FileUtil.normalizeFile(file);
        FileObject fobj = FileUtil.toFileObject(file);
        try {
            DataObject.find(fobj).delete();
        } catch (IOException ex) {
            // Ignore, either the file isn't there or we can't delete it.
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    private void cbImportItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbImportItemStateChanged
        if (cbImport.isSelected()) {
            schemaTF.setEditable(true);
            browseButton.setEnabled(true);
        } else {
            schemaTF.setEditable(false);
            browseButton.setEnabled(false);
        }
    }//GEN-LAST:event_cbImportItemStateChanged

private void bindingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bindingComboBoxActionPerformed
    setBindingSubType(getBindingType());
    this.firePropertyChange(PROP_BINDING_TYPE, null, getBindingType());
    //SwingUtilities.getWindowAncestor(this).pack();
}//GEN-LAST:event_bindingComboBoxActionPerformed

private void abstractWSDLChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abstractWSDLChoiceActionPerformed
    boolean abstractWSDLSelected = abstractWSDLChoice.isSelected();
    enableNextButton(abstractWSDLSelected);
    defaultIterator.setTemplateName(null);
    context.setWSDLExtensionIterator(defaultIterator);
    recurseEnable(bindingConfigurationPanel, concreteWSDLChoice.isSelected());
    bindingConfigurationPanel.setVisible(concreteWSDLChoice.isSelected());
    //enableFinishButton(emptyWSDLChoice.isSelected());
}//GEN-LAST:event_abstractWSDLChoiceActionPerformed

private void concreteWSDLChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_concreteWSDLChoiceActionPerformed
    recurseEnable(bindingConfigurationPanel, concreteWSDLChoice.isSelected());
    bindingConfigurationPanel.setVisible(concreteWSDLChoice.isSelected());
    //enableFinishButton(emptyWSDLChoice.isSelected());
    bindingSubtypeChanged(getBindingSubType());
}//GEN-LAST:event_concreteWSDLChoiceActionPerformed

    private void enableNextButton(boolean enable) {
        firePropertyChange("HAS_NEXT", !enable, enable);
    }

    private void recurseEnable(JComponent comp, boolean enable) {
        comp.setEnabled(enable);
        for (Component c : comp.getComponents()) {
            if (c instanceof JComponent) {
                recurseEnable((JComponent) c, enable);
            }
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (context.getWSDLExtensionIterator() == null && abstractWSDLChoice.isSelected()) {
            defaultIterator.setTemplateName(null);
            context.setWSDLExtensionIterator(defaultIterator);
        }
        
        enableFinishButton(false);
        
    }
    
    

    public LocalizedTemplateGroup getBindingType() {
        if (concreteWSDLChoice.isSelected()) {
            return (LocalizedTemplateGroup) bindingComboBox.getSelectedItem();
        }
        return null;
    }
    
    public void setBindingType(String bindingSubType) {
        this.bindingComboBox.setSelectedItem(bindingSubType);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton abstractWSDLChoice;
    private javax.swing.JComboBox bindingComboBox;
    private javax.swing.JPanel bindingConfigurationPanel;
    private javax.swing.JLabel bindingLabel;
    private javax.swing.ButtonGroup bindingSubtypeGroup;
    private javax.swing.JLabel bindingTypeLabel;
    private javax.swing.JPanel bindingTypePanel;
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox cbImport;
    private javax.swing.JRadioButton concreteWSDLChoice;
    private javax.swing.JLabel namespaceLB;
    private javax.swing.JTextField nsTF;
    private javax.swing.JLabel schemaLB;
    private javax.swing.JTextField schemaTF;
    private javax.swing.ButtonGroup wsdlTypeGroup;
    private javax.swing.JLabel wsdlTypeLabel;
    // End of variables declaration//GEN-END:variables
    
    private class NamespaceDocListener implements javax.swing.event.DocumentListener {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        private void documentChanged(javax.swing.event.DocumentEvent e) {
            hasUserModifiedNamespace = true;
        }
    }
    
    private class DocListener implements javax.swing.event.DocumentListener {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        private void documentChanged(javax.swing.event.DocumentEvent e) {
            if(!hasUserModifiedNamespace) {
                nsTF.getDocument().removeDocumentListener(mListener);
                nsTF.setText(TARGET_URL_PREFIX+ projectName + "/" + fileNameTF.getText());
                nsTF.getDocument().addDocumentListener(mListener);
            }
            
        }
    }
    
    /** Class than provides basic informationn about schema file
     */
    static class SchemaInfo {
        private java.net.URL url;
        SchemaInfo(java.net.URL url) {
            this.url=url;
        }
        
        java.net.URL getURL() {
            return url;
        }
        
        String getNamespace() {
            InputSource is = new InputSource(url.toExternalForm());
            try {
                return parse(is);
            } catch (java.io.IOException ex){
            } catch (SAXException ex){}
            return "";
        }
        
        String getSchemaName() {
            return url.toExternalForm();
        }
        
        /** Parses XML document and creates the list of tags
         */
        private String parse(InputSource is) throws java.io.IOException, SAXException {
            XMLReader xmlReader = org.openide.xml.XMLUtil.createXMLReader();
            NsHandler handler = new NsHandler();
            xmlReader.setContentHandler(handler);
            //xmlReader.setFeature("http://xml.org/sax/features/use-locator2",true);
            try {
                xmlReader.parse(is);
            } catch (SAXException ex) {
                if (!"EXIT".equals(ex.getMessage())) throw ex; //NOI18N
            }
            String ns = handler.getNs();
            if (ns==null) return "";
            return ns;
        }
        
        private static class NsHandler extends org.xml.sax.helpers.DefaultHandler {
            private String ns;
            
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (qName.endsWith("schema")) { //NOI18N
                    ns=attributes.getValue("targetNamespace"); //NOI18N
                    throw new SAXException("EXIT"); //NOI18N
                }
            }
            
            String getNs() {
                return ns;
            }
        }
        
    }
    
    String getNS() {
        return nsTF.getText();
    }
    
    boolean isImport() {
        return cbImport.isSelected();
    }
    
    SchemaInfo[] getSchemas() {
        if (cbImport.isSelected()) {
            String schemas = schemaTF.getText();
            String[] urls = schemas.split(",");
            List<SchemaInfo> infos = new ArrayList<SchemaInfo>();
            for (int i=0;i<urls.length;i++) {
                String urlString=urls[i].trim();
                if (urlString.length()==0) continue;
                URL url = null;
                try {
                    File file = new File(urlString);
                    if (file.exists()) {
                        url = file.toURI().toURL();
                    } else {
                        url = new java.net.URL(urlString);
                    }
                    infos.add(new SchemaInfo(url));
                } catch (java.net.MalformedURLException ex) {
                    // testing if target folder contains XML Schema
                    try {
                        org.openide.loaders.DataFolder folder = wizardPanel.getTemplateWizard().getTargetFolder();
                        org.openide.filesystems.FileObject fo = folder.getPrimaryFile();
                        if ((fo.getFileObject(urlString))!=null) {
                            String parentURL = fo.getURL().toExternalForm();
                            infos.add(new SchemaInfo(new java.net.URL(parentURL+urlString)));
                        }
                    } catch (java.io.IOException ex1) {}
                }
            }
            return infos.toArray(new SchemaInfo[infos.size()]);
        }
        return new SchemaInfo[]{};
    }

    public void validateSchemas() throws WizardValidationException {
        if (cbImport.isSelected()) {
            String schemas = schemaTF.getText();
            String[] urls = schemas.split(",");
            for (int i=0;i<urls.length;i++) {
                String urlString=urls[i].trim();
                if (urlString.length()==0) continue;
                createSchemaModel(urlString);
            }
        }
        
    }
    
    private void createSchemaModel(String urlString) throws WizardValidationException {
        File file = new File(urlString);
        if (!file.exists()) {
            file = null;
        }
        if (file == null) {
            URL url = null;
            try {
                url = new java.net.URL(urlString);
            } catch (MalformedURLException e) {
                org.openide.loaders.DataFolder folder;
                try {
                    folder = wizardPanel.getTemplateWizard().getTargetFolder();
                    org.openide.filesystems.FileObject fo = folder.getPrimaryFile();
                    if ((fo.getFileObject(urlString))!=null) {
                        String parentURL = fo.getURL().toExternalForm();
                        try {
                            url = new java.net.URL(parentURL+urlString);
                        } catch (MalformedURLException e1) {
                            throw new WizardValidationException(schemaTF, e1.getMessage(), e1.getLocalizedMessage());
                        }
                    }
                } catch (IOException e1) {
                    throw new WizardValidationException(schemaTF, e1.getMessage(), e1.getLocalizedMessage());
                }
                if (url == null) {
                    String errorString = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
                    throw new WizardValidationException(schemaTF, errorString, errorString);
                }
            }
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new WizardValidationException(schemaTF, e.getMessage(), e.getLocalizedMessage());
            }
        }
        
        if (!file.isFile()) {
            throw new WizardValidationException(schemaTF, "INVALID_SCHEMA_FILE", NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString));
        }
        
        ModelSource source;
        try {
            File normFile = FileUtil.normalizeFile(file);
            FileObject fo = FileUtil.toFileObject(normFile);
            if (fo == null) {
                String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
                throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
            }
            checkAccessibleFromThisProject(wizardPanel.getProject(), fo, urlString);
            source = org.netbeans.modules.xml.retriever.catalog.Utilities.
                    createModelSource(fo, false);
        } catch (WizardValidationException e) {
            throw e;
        } catch (CatalogModelException e) {
            throw new WizardValidationException(schemaTF, e.getMessage(), e.getLocalizedMessage());
        } catch (Throwable e) {
            String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
            throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
        }
        SchemaModel model = null;
        try {
            model = SchemaModelFactory.getDefault().getModel(source);
        } catch (Throwable e) {
            String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
            throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
        }
        
        if (model == null || model.getState().equals(Model.State.NOT_WELL_FORMED)) {
            String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
            throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
        }
    }
    
    private void checkAccessibleFromThisProject(Project project, FileObject file, String fileName) throws WizardValidationException {
        Project filesProject = FileOwnerQuery.getOwner(file);
        if (filesProject == null) {
            throw new WizardValidationException(schemaTF, "INACCESSIBLE_FILE", NbBundle.getMessage(WsdlUIPanel.class, "INACCESSIBLE_FILE", fileName));
        }
        if (project == filesProject) {
            return;
        }
        
        DefaultProjectCatalogSupport ctlgSupp = DefaultProjectCatalogSupport.getInstance(project.getProjectDirectory());
        for (Object pr : ctlgSupp.getProjectReferences()) {
            if (pr == filesProject) {
                return;
            }
        }
        throw new WizardValidationException(schemaTF, "INACCESSIBLE_PROJECT", NbBundle.getMessage(WsdlUIPanel.class, "INACCESSIBLE_PROJECT", fileName, filesProject.getProjectDirectory().getName()));
    }

    public JTextField getSchemaFileTextField() {
        return schemaTF;
    }
    
    /**
     * If this panel was launched from the File->Other->SOA entry, then
     * we need to remove the Abstract/Concrete toggle section
     * 
     * @param disable
     */
    public void disableWSDLTypeSection(boolean disable) {
        if (disable) {
            // make sure the BindingType is visible prior to removing wsdl type section
            recurseEnable(bindingConfigurationPanel, true);
            bindingConfigurationPanel.setVisible(true);
            concreteWSDLChoice.setSelected(true);
            bindingSubtypeChanged(getBindingSubType());
            wsdlTypeLabel.setVisible(!disable);
            abstractWSDLChoice.setVisible(!disable);            
            concreteWSDLChoice.setVisible(!disable);
        }
    }
}