/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.editors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Timer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.bpel.design.DnDHandler;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.openide.ErrorManager;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.editors.controls.EmptyComboBoxModel;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELComponentFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.bpel.properties.importchooser.WSDLFileImportDialog;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;

/**
 * @author nk160297
 */
public class PartnerLinkMainPanel extends EditorLifeCycleAdapter
        implements Validator.Provider, HelpCtx.Provider {
    
    static final long serialVersionUID = 1L;
    private CustomNodeEditor<PartnerLink> myEditor;
    private ArrayList<Role> rolesList;
    private Role myRole;
    private Role partnerRole;
    private DefaultValidator myValidator;
    private Timer inputDelayTimer;
    
    private static final String ROLE_NA = "-----"; // NOI18N
    private static final String SLASH = "/"; // NOI18N
    
    private static final EmptyComboBoxModel emptyModel = new EmptyComboBoxModel();
    
    /** Creates new form PartnerLinkMainPanel */
    public PartnerLinkMainPanel(CustomNodeEditor<PartnerLink> anEditor) {
        myEditor = anEditor;
        createContent();
    }
    
    private void bindControls2PropertyNames() {
        fldPartnerLinkName.putClientProperty(
                CustomNodeEditor.PROPERTY_BINDER, NAME);
    }
    
    public void createContent() {
        initComponents();
        bindControls2PropertyNames();
        Lookup lookup = myEditor.getLookup();
        //
        // Load WSDL combo-box
        BusinessProcessHelper bpHelper = (BusinessProcessHelper)lookup.
                lookup(BusinessProcessHelper.class);
        Collection<FileObject> wsdlFiles = bpHelper.getWSDLFilesInProject();
        
        FileObject[] wsdlFilesArr =
                wsdlFiles.toArray(new FileObject[wsdlFiles.size()]);
        cbxWsdlFile.setModel(new DefaultComboBoxModel(wsdlFilesArr));
        //
        cbxWsdlFile.setRenderer(new WsdlFileRenderer());
        //
        cbxProcessPortType.setRenderer(new PortTypeRenderer());
        cbxPartnerPortType.setRenderer(new PortTypeRenderer());
        //
        cbxWsdlFile.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    processWsdlFileChange();
                } else if (event.getStateChange() == ItemEvent.DESELECTED) {
                    cbxPartnerLinkType.setSelectedIndex(-1);
                    cbxPartnerLinkType.setModel(emptyModel);
                }
            }
        });
        //
        cbxPartnerLinkType.setRenderer(new DefaultListCellRenderer() {
            static final long serialVersionUID = 1L;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value != null && value instanceof PartnerLinkType) {
                    PartnerLinkType plType = (PartnerLinkType)value;
                    String text = plType.getName();
                    setText(text);
                }
                return this;
            }
        });
        //
        cbxPartnerLinkType.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    reloadRoles();
                    setRolesByDefault();
                } else if (event.getStateChange() == ItemEvent.DESELECTED) {
                    getRolesList().clear();
                    setRole(false, null);
                    setRole(true, null);
                }
            }
        });
        //
        btnSwapRoles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                swapRoles();
            }
        });
        //
        ActionListener updateStateListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateEnabledState();
                //
                getValidator().revalidate(true);
            }
        };
        //
        rbtnUseExistingPLT.addActionListener(updateStateListener);
        rbtnCreateNewPLT.addActionListener(updateStateListener);
        chbxProcessWillImplement.addActionListener(updateStateListener);
        chbxPartnerWillImpement.addActionListener(updateStateListener);
        //
        updateEnabledState();
        //
        myEditor.getValidStateManager(true).addValidStateListener(
                new ValidStateListener() {
            public void stateChanged(ValidStateManager source, boolean isValid) {
                if (source.isValid()) {
                    lblErrorMessage.setText("");
                } else {
                    lblErrorMessage.setText(source.getHtmlReasons());
                }
            }
        });
        //
        ActionListener timerListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getValidator().revalidate(true);
            }
        };
        inputDelayTimer = new Timer(Constants.INPUT_VALIDATION_DELAY, timerListener);
        inputDelayTimer.setCoalesce(true);
        inputDelayTimer.setRepeats(false);
        //
        DocumentListener docListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
            public void insertUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
            public void removeUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
        };
        fldPartnerLinkName.getDocument().addDocumentListener(docListener);
        fldPartnerRoleName.getDocument().addDocumentListener(docListener);
        fldProcessRoleName.getDocument().addDocumentListener(docListener);
        fldNewPLTName.getDocument().addDocumentListener(docListener);
        //
        FocusListener fl = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                inputDelayTimer.stop();
                getValidator().revalidate(true);
            }
        };
        fldPartnerLinkName.addFocusListener(fl);
        fldPartnerRoleName.addFocusListener(fl);
        fldProcessRoleName.addFocusListener(fl);
        fldNewPLTName.addFocusListener(fl);
        //
    }
    
    private void updateEnabledState() {
        if (cbxWsdlFile.getSelectedIndex() == -1) {
            rbtnUseExistingPLT.setEnabled(false);
            rbtnCreateNewPLT.setEnabled(false);
            //
            cbxPartnerLinkType.setEnabled(false);
            btnSwapRoles.setEnabled(false);
            //
            fldNewPLTName.setEnabled(false);
            chbxProcessWillImplement.setEnabled(false);
            fldProcessRoleName.setEnabled(false);
            cbxProcessPortType.setEnabled(false);
            chbxPartnerWillImpement.setEnabled(false);
            fldPartnerRoleName.setEnabled(false);
            cbxPartnerPortType.setEnabled(false);
            myWsdlWrapperName.setEnabled(false);
            myBrowseButton.setEnabled(false);
        } else {
            rbtnUseExistingPLT.setEnabled(true);
            rbtnCreateNewPLT.setEnabled(true);
            //
            if (rbtnUseExistingPLT.isSelected()) {
                cbxPartnerLinkType.setEnabled(true);
                btnSwapRoles.setEnabled(true);
                //
                myWsdlWrapperName.setEnabled(false);
                myBrowseButton.setEnabled(false);
                fldNewPLTName.setEnabled(false);
                chbxProcessWillImplement.setEnabled(false);
                fldProcessRoleName.setEnabled(false);
                cbxProcessPortType.setEnabled(false);
                chbxPartnerWillImpement.setEnabled(false);
                fldPartnerRoleName.setEnabled(false);
                cbxPartnerPortType.setEnabled(false);
            } else {
                cbxPartnerLinkType.setEnabled(false);
                btnSwapRoles.setEnabled(false);
                //
                myWsdlWrapperName.setEnabled(true);
                myBrowseButton.setEnabled(true);
                fldNewPLTName.setEnabled(true);
                chbxProcessWillImplement.setEnabled(true);
                boolean processWill = chbxProcessWillImplement.isSelected();
                fldProcessRoleName.setEnabled(processWill);
                cbxProcessPortType.setEnabled(processWill);
                //
                chbxPartnerWillImpement.setEnabled(true);
                boolean partnerWill = chbxPartnerWillImpement.isSelected();
                fldPartnerRoleName.setEnabled(partnerWill);
                cbxPartnerPortType.setEnabled(partnerWill);
            }
        }
    }
    
    public boolean initControls() {
        try {
            // remove selection to guarantee that selection will always send event
            cbxWsdlFile.setSelectedIndex(-1);
            //
            // Indicates if the wsdl file was passed as a parameter
            // A wsdl file is usually passed when the DnD of Wsdl is performed
            boolean wsdlFileWasSpecified = false;
            //
            PartnerLink pLink = myEditor.getEditedObject();
            WSDLReference<PartnerLinkType> pltRef = pLink.getPartnerLinkType();
            PartnerLinkType plType = null;
            FileObject resultWsdlFile = null;
            WSDLModel wsdlModel = null;
            //
            if (pltRef != null) {
                plType = pltRef.get();
                if (plType != null){
                    resultWsdlFile = (FileObject)plType.getModel().
                            getModelSource().getLookup().lookup(FileObject.class);
                }
            }
            //
            if (resultWsdlFile == null) {
                Object cookieObj = pLink.getCookie(DnDHandler.class);
                if (cookieObj != null && cookieObj instanceof WSDLModel) {
                    wsdlModel = (WSDLModel)cookieObj;
                    resultWsdlFile = (FileObject)wsdlModel.getModelSource().
                            getLookup().lookup(FileObject.class);
                    wsdlFileWasSpecified = true;
                } else if (cookieObj != null && cookieObj instanceof FileObject){
                    resultWsdlFile = (FileObject)cookieObj;
                    wsdlModel = getWsdlModel(resultWsdlFile);
                    wsdlFileWasSpecified = true;
                }
            }
            //
            // Set selection to the WSDL file combo-box
            if (resultWsdlFile != null) {
                cbxWsdlFile.setSelectedItem(resultWsdlFile);
            } else {
                if (cbxWsdlFile.getModel().getSize() > 0) {
                    cbxWsdlFile.setSelectedIndex(0);
                }
            }
            //
            // Set selection to the Parthner Link Type combo-box
            if (plType != null) {
                cbxPartnerLinkType.setSelectedItem(plType);
            } else {
                if (cbxPartnerLinkType.getModel().getSize() > 0) {
                    cbxPartnerLinkType.setSelectedIndex(0);
                }
            }
            //
            if (myEditor.getEditingMode() == 
                    CustomNodeEditor.EditingMode.EDIT_INSTANCE) {
                //
                // Load roles from the Partner Link
                WSDLReference<Role> myRoleRef = pLink.getMyRole();
                setRoleByRef(true, myRoleRef);
                //
                WSDLReference<Role> partnerRoleRef = pLink.getPartnerRole();
                setRoleByRef(false, partnerRoleRef);
            }
            //
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        // vlv
        setWrapper(getCurrentWsdlFile());
        myWsdlWrapperName.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                getValidator().revalidate(true);
            }
        });
        //
        updateEnabledState();
        getValidator().revalidate(true);
        return true;
    }
    
    private void processWsdlFileChange() {
        reloadPartnerLinkTypes();
        //
        if (cbxPartnerLinkType.getModel().getSize() > 0) {
            cbxPartnerLinkType.setSelectedIndex(0);
        } else {
            cbxPartnerLinkType.setSelectedIndex(-1);
        }
        //
        reloadPortTypes();
        //
        setWrapper(getCurrentWsdlFile());
        //
        boolean isThereAnyPLT = false;
        WSDLModel wsdlModel = getCurrentWsdlModel();
        //
        if (wsdlModel != null) {
            Definitions definitions = wsdlModel.getDefinitions();
            if (definitions != null) {
                List<PartnerLinkType> pltList = definitions.
                        getExtensibilityElements(PartnerLinkType.class);
                isThereAnyPLT = !pltList.isEmpty();
            }
        }
        //
        if (isThereAnyPLT) {
            rbtnUseExistingPLT.setSelected(true);
            setRolesByDefault();
        } else {
            rbtnCreateNewPLT.setSelected(true);
            if (wsdlModel != null) {
                setDefaultParamsForNewPLT(wsdlModel);
            }
        }
        //
        updateEnabledState();
        getValidator().revalidate(true);
    }

    private void setDefaultParamsForNewPLT(final WSDLModel wsdlModel) {
        Definitions definitions = wsdlModel.getDefinitions();
        if (definitions != null) {
            Collection<PortType> portTypeList = definitions.getPortTypes();
            if (!portTypeList.isEmpty()) {
                PortType portType = portTypeList.iterator().next();
                String portTypeName = portType.getName();
                //
                // Try correct the name by cutting the unnecessary suffix
                String suffixToRemove = "PortType"; // NOI18N
                if (portTypeName != null &&
                        portTypeName.endsWith(suffixToRemove)) {
                    int index = portTypeName.length() - suffixToRemove.length();
                    String correctedName = portTypeName.substring(0, index);
                    if (correctedName.length() != 0) {
                        portTypeName = correctedName;
                    }
                }
                //
                chbxPartnerWillImpement.setSelected(true);
                fldNewPLTName.setText(portTypeName + "LinkType"); // NOI18N
                fldPartnerRoleName.setText(portTypeName + "Role"); // NOI18N
                cbxPartnerPortType.setSelectedItem(portType);
            }
        }
    }
    
    private void reloadPartnerLinkTypes() {
        WSDLModel wsdlModel = getCurrentWsdlModel();
        if (wsdlModel != null) {
            List<PartnerLinkType> pltList = wsdlModel.getDefinitions().
                    getExtensibilityElements(PartnerLinkType.class);
            if (pltList != null && pltList.size() > 0){
                cbxPartnerLinkType.setModel(
                        new DefaultComboBoxModel(pltList.toArray()));
                // remove selection to guarantee that selection will always send event
                cbxPartnerLinkType.setSelectedIndex(-1);
            }
        }
    }
    
    private List<Role> getRolesList() {
        if (rolesList == null) {
            rolesList = new ArrayList<Role>(2);
            reloadRoles();
        }
        return rolesList;
    }
    
    private void reloadRoles() {
        PartnerLinkType plType = (PartnerLinkType)cbxPartnerLinkType.getSelectedItem();
        getRolesList().clear();
        //
        if (plType != null) {
            //
            Role role;
            //
            role = plType.getRole1();
            if (role != null) {
                rolesList.add(role);
            }
            //
            role = plType.getRole2();
            if (role != null) {
                rolesList.add(role);
            }
        }
    }
    
    private void setRolesByDefault() {
        Role firstRole = null;
        Role secondRole = null;
        //
        Iterator<Role> itr = getRolesList().iterator();
        if (itr.hasNext()) {
            firstRole = itr.next();
        }
        if (itr.hasNext()) {
            secondRole = itr.next();
        }
        //
        setRole(true, firstRole);
        setRole(false, secondRole);
    }
    
    private void setRoleByRef(boolean isMyRole, WSDLReference<Role> newValue) {
        if (newValue == null) {
            setRole(isMyRole, null);
            return;
        }
        //
        Role role = newValue.get();
        if (role == null) {
            String localRoleName = newValue.getRefString();
            if (isMyRole) {
                if (localRoleName == null || localRoleName.length() == 0) {
                    fldMyRole.setText(ROLE_NA);
                } else {
                    fldMyRole.setText(localRoleName);
                }
            } else {
                if (localRoleName == null || localRoleName.length() == 0) {
                    fldPartnerRole.setText(ROLE_NA);
                } else {
                    fldPartnerRole.setText(localRoleName);
                }
            }
        } else {
            setRole(isMyRole, role);
        }
    }
    
    private void setRole(boolean isMyRole, Role newValue) {
        if (isMyRole) {
            myRole = newValue;
            if (newValue == null) {
                fldMyRole.setText(ROLE_NA);
            } else {
                fldMyRole.setText(newValue.getName());
            }
        } else {
            partnerRole = newValue;
            if (newValue == null) {
                fldPartnerRole.setText(ROLE_NA);
            } else {
                fldPartnerRole.setText(newValue.getName());
            }
        }
    }
    
    private void swapRoles() {
        Role tempRole = myRole;
        String tempRoleName = fldMyRole.getText();
        //
        myRole = partnerRole;
        String partnerRoleName = fldPartnerRole.getText();
        fldMyRole.setText(partnerRoleName);
        //
        partnerRole = tempRole;
        fldPartnerRole.setText(tempRoleName);
    }
    
    private void reloadPortTypes() {
        WSDLModel wsdlModel = getCurrentWsdlModel();
        if (wsdlModel != null) {
            Collection<PortType> portTypeList =
                    wsdlModel.getDefinitions().getPortTypes();
            if (portTypeList != null && portTypeList.size() > 0){
                cbxProcessPortType.setModel(
                        new DefaultComboBoxModel(portTypeList.toArray()));
                cbxPartnerPortType.setModel(
                        new DefaultComboBoxModel(portTypeList.toArray()));
            }
        }
        //
        getValidator().revalidate(true);
    }
    
    public boolean applyNewValues() {
        try {
            PartnerLink pLink = myEditor.getEditedObject();
            PartnerLinkType plType = null;
            //
            if (rbtnUseExistingPLT.isSelected()) {
                plType = tuneForExistingPLT(pLink);
            } else {
                plType = tuneFromNewPLT(pLink);
            }
            //
            if (plType != null) {
                new ImportRegistrationHelper(pLink.getBpelModel()).
                        addImport(plType.getModel());
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
    // vlv
    private WSDLModel getCurrentWsdlModel(boolean isWsdlWrapperSet, boolean isCreate) {
        FileObject currentFile = getCurrentWsdlFile();
        WSDLModel currentModel = getWsdlModel(currentFile);
        
        if (isWsdlWrapperSet) {
            WsdlWrapper wsdlWrapper =
                    new WsdlWrapper(/*getSource().getParent()*/
                        ResolverUtility.getProjectSource(myEditor.getLookup()), 
                        myWsdlWrapperName.getText(), isCreate);
            WSDLModel wrapperModel = wsdlWrapper.getModel();
            
            if (wrapperModel == null) {
                return null;
            }
            FileObject wrapperFile = wsdlWrapper.getFile();
            addImport(wrapperModel, wrapperFile, currentModel, currentFile);
            return wrapperModel;
        }
        return currentModel;
    }
    
    private FileObject getSource() {
        BpelModel model = (BpelModel) myEditor.getLookup().lookup(BpelModel.class);
        
        return org.netbeans.modules.bpel.editors.api.utils.Util.
                getFileObjectByModel(model);
    }
    
    // vlv
    private void addImport(
            WSDLModel model,
            FileObject file,
            WSDLModel importedModel,
            FileObject importedFile) {
        if (file.equals(importedFile)) {
            return;
        }
        Definitions definitions = model.getDefinitions();
        Import inport = model.getFactory().createImport();
        inport.setLocation(getRelativePath(file, importedFile));
        inport.setNamespace(importedModel.getDefinitions().getTargetNamespace());
        
        if (containsImport(model, inport)) {
            return;
        }
        model.startTransaction();
        definitions.addImport(inport);
        model.endTransaction();
    }
    
    // vlv
    private boolean containsImport(WSDLModel model, Import inport) {
        Definitions definitions = model.getDefinitions();
        Iterator<Import> imports = definitions.getImports().iterator();
        
        while (imports.hasNext()) {
            Import impord = imports.next();
            
            if (equals(impord, inport)) {
                return true;
            }
        }
        return false;
    }
    
    // vlv
    private boolean equals(Import import1, Import import2) {
        return
                import1.getLocation().equals(import2.getLocation()) &&
                import1.getNamespace().equals(import2.getNamespace());
    }
    
    // vlv
    private String getRelativePath(FileObject file1, FileObject file2) {
        StringTokenizer stk1 = new StringTokenizer(file1.getPath(), SLASH);
        StringTokenizer stk2 = new StringTokenizer(file2.getPath(), SLASH);
        String relative = ""; // NOI18N
        
        while (stk1.hasMoreTokens() && stk2.hasMoreTokens()) {
            relative = stk2.nextToken();
            
            if ( !stk1.nextToken().equals(relative)) {
                break;
            }
        }
        while (stk1.hasMoreTokens()) {
            relative = "../".concat(relative); // NOI18N
            stk1.nextToken();
        }
        while(stk2.hasMoreTokens()) {
            relative = relative.concat(SLASH); // NOI18N
            relative = relative.concat(stk2.nextToken());
        }
        return relative;
    }
    
    /**
     * Returns the current WSDL model which is selected in the combo-box.
     * Method can return null!
     */
    private WSDLModel getCurrentWsdlModel() {
        return getCurrentWsdlModel(false, false);
    }
    
    private FileObject getCurrentWsdlFile() {
        return (FileObject)cbxWsdlFile.getSelectedItem();
    }
    
    private PartnerLinkType tuneFromNewPLT(final PartnerLink pLink) {
        PartnerLinkType plType = null;
        Role newMyRole = null;
        Role newPartnerRole = null;
        //
        // Create a New Partner Link Type in the WSDL model.
        // It has to be done first because of the PLT will not be
        // visible until end of WSDL transaction.
        WSDLModel wsdlModel = getCurrentWsdlModel(true, true);
        //
        if (wsdlModel != null) {
            wsdlModel.startTransaction();
            try {
                boolean isFirstRoleOccupied = false;
                BPELComponentFactory factory = new BPELComponentFactory(wsdlModel);
                plType = factory.createPartnerLinkType(
                        wsdlModel.getDefinitions());
                //
                String newPLTypeName = fldNewPLTName.getText();
                plType.setName(newPLTypeName);
                //
                wsdlModel.getDefinitions().addExtensibilityElement(plType);
                //
                if (chbxProcessWillImplement.isSelected()) {
                    newMyRole = factory.createRole(wsdlModel.getDefinitions());
                    //
                    String myRoleName = fldProcessRoleName.getText();
                    newMyRole.setName(myRoleName);
                    //
                    plType.setRole1(newMyRole);
                    isFirstRoleOccupied = true;
                    //
                    PortType processPortType =
                            (PortType)cbxProcessPortType.getSelectedItem();
                    NamedComponentReference<PortType> processPortTypeRef =
                            newMyRole.createReferenceTo(
                            processPortType, PortType.class);
                    newMyRole.setPortType(processPortTypeRef);
                }
                //
                if (chbxPartnerWillImpement.isSelected()) {
                    newPartnerRole = factory.createRole(wsdlModel.getDefinitions());
                    //
                    String partnerRoleName = fldPartnerRoleName.getText();
                    newPartnerRole.setName(partnerRoleName);
                    //
                    if (isFirstRoleOccupied) {
                        plType.setRole2(newPartnerRole);
                    } else {
                        plType.setRole1(newPartnerRole);
                    }
                    //
                    PortType partnerPortType =
                            (PortType)cbxPartnerPortType.getSelectedItem();
                    NamedComponentReference<PortType> partnerPortTypeRef =
                            newPartnerRole.createReferenceTo(
                            partnerPortType, PortType.class);
                    newPartnerRole.setPortType(partnerPortTypeRef);
                }
            } finally {
                wsdlModel.endTransaction();
            }
            //Flush changes from WSDL model to file
            
            PartnerLinkHelper.saveModel(wsdlModel);
            
            //
            // Put changes to the BPEL model
            if (plType != null) {
                pLink.setPartnerLinkType(
                        pLink.createWSDLReference(
                        plType, PartnerLinkType.class));
                //
                if (newMyRole != null) {
                    WSDLReference<Role> newMyRoleRef =
                            pLink.createWSDLReference(newMyRole, Role.class);
                    pLink.setMyRole(newMyRoleRef);
                } else {
                    pLink.removeMyRole();
                }
                //
                if (newPartnerRole != null) {
                    WSDLReference<Role> newPatnerRoleRef =
                            pLink.createWSDLReference(newPartnerRole, Role.class);
                    pLink.setPartnerRole(newPatnerRoleRef);
                } else {
                    pLink.removePartnerRole();
                }
            }
        }
        return plType;
    }
    
    private WSDLModel getWsdlModel(FileObject wsdlFile) {
        if (wsdlFile != null) {
            ModelSource modelSource = Utilities.getModelSource(wsdlFile, true);
            if (modelSource != null) {
                WSDLModel wsdlModel =
                        WSDLModelFactory.getDefault().getModel(modelSource);
                if (wsdlModel.getState() != Model.State.NOT_WELL_FORMED) {
                    return wsdlModel;
                }
            }
        }
        //
        return null;
    }
    
    private PartnerLinkType tuneForExistingPLT(final PartnerLink pLink) {
        PartnerLinkType plType =
                (PartnerLinkType)cbxPartnerLinkType.getSelectedItem();
        //
        if(plType != null){
            pLink.setPartnerLinkType(
                    pLink.createWSDLReference(
                    plType, PartnerLinkType.class));
        }
        //
        if (myRole == null) {
            pLink.removeMyRole();
        } else {
            WSDLReference<Role> roleRef =
                    pLink.createWSDLReference(myRole, Role.class);
            pLink.setMyRole(roleRef);
        }
        //
        if (partnerRole == null) {
            pLink.removePartnerRole();
        } else {
            WSDLReference<Role> roleRef =
                    pLink.createWSDLReference(partnerRole, Role.class);
            pLink.setPartnerRole(roleRef);
        }
        //
        return plType;
    }
    
    public DefaultValidator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(myEditor, ErrorMessagesBundle.class) {
                
                public boolean doFastValidation() {
                    // vlv
                    WsdlWrapper wrapper =
                       new WsdlWrapper(/*getSource().getParent()*/
                        ResolverUtility.getProjectSource(myEditor.getLookup()), 
                        myWsdlWrapperName.getText(), false);
                       
                    FileObject file = wrapper.getFile();

                    if (file != null && !file.canWrite()) {
                        addReasonKey("ERR_FILE_IS_READ_ONLY", myWsdlWrapperName.getText()); //NOI18N
                    }

                    String plName = fldPartnerLinkName.getText();
                    //
                    if (plName == null || plName.length() == 0) {
                        addReasonKey("ERR_NAME_EMPTY"); //NOI18N
                    }
                    //
                    if (cbxWsdlFile.getSelectedIndex() == -1) {
                        addReasonKey("ERR_WSDL_FILE_NOT_SPECIFIED"); //NOI18N
                    }
                    //
                    if (rbtnUseExistingPLT.isSelected()) {
                        if (cbxPartnerLinkType.getSelectedIndex() == -1) {
                            addReasonKey("ERR_PL_TYPE_NOT_SPECIFIED"); //NOI18N
                        }
                        //
                        if (myRole == null && partnerRole == null) {
                            addReasonKey("ERR_PL_TYPE_WITHOUT_ROLES"); //NOI18N
                        }
                    } else {
                        String pltName = fldNewPLTName.getText();
                        if (pltName == null || pltName.length() == 0) {
                            addReasonKey("ERR_PLT_NAME_EMPTY"); //NOI18N
                        } else {
                            boolean isCorrectPLTName = Util.isNCName(pltName);
                            if (!isCorrectPLTName) {
                                addReasonKey("ERR_PLT_NAME_INVALID"); //NOI18N
                            } else {
                                // vlv
                                WSDLModel wsdlModel = getCurrentWsdlModel(true, false);
                                
                                if (wsdlModel != null) {
                                    isCorrectPLTName = Util.isUniquePartnerLinkTypeName(wsdlModel, pltName);
                                    if (!isCorrectPLTName) {
                                        addReasonKey("ERR_PLT_NAME_NOT_UNIQUE"); //NOI18N
                                    }
                                }
                            }
                        }
                        //
                        if (!chbxProcessWillImplement.isSelected() &&
                                !chbxPartnerWillImpement.isSelected()) {
                            addReasonKey("ERR_NEW_PLT_ROLES_NOT_SPECIFIED"); //NOI18N
                        }
                        //
                        if (chbxProcessWillImplement.isSelected()) {
                            String myRoleName = fldProcessRoleName.getText();
                            if (myRoleName == null || myRoleName.length() == 0) {
                                addReasonKey("ERR_PLT_MY_ROLE_NAME_EMPTY"); //NOI18N
                            } else {
                                boolean isCorrectMyRoleName = Util.isNCName(myRoleName);
                                if (!isCorrectMyRoleName) {
                                    addReasonKey("ERR_PLT_MY_ROLE_NAME_INVALID"); //NOI18N
                                }
                            }
                            //
                            int processPortTypeIndex =
                                    cbxProcessPortType.getSelectedIndex();
                            if (processPortTypeIndex == -1) {
                                addReasonKey("ERR_PLT_MY_ROLE_PORT_TYPE_EMPTY"); //NOI18N
                            }
                        }
                        //
                        if (chbxPartnerWillImpement.isSelected()) {
                            String myRoleName = fldPartnerRoleName.getText();
                            if (myRoleName == null || myRoleName.length() == 0) {
                                addReasonKey("ERR_PLT_PARTNER_ROLE_NAME_EMPTY"); //NOI18N
                            } else {
                                boolean isCorrectPartnerRoleName = Util.isNCName(myRoleName);
                                if (!isCorrectPartnerRoleName) {
                                    addReasonKey("ERR_PLT_PARTNER_ROLE_NAME_INVALID"); //NOI18N
                                }
                            }
                            //
                            int partnerPortTypeIndex =
                                    cbxPartnerPortType.getSelectedIndex();
                            if (partnerPortTypeIndex == -1) {
                                addReasonKey("ERR_PLT_PARTNER_ROLE_PORT_TYPE_EMPTY"); //NOI18N
                            }
                        }
                        //
                        if (chbxPartnerWillImpement.isSelected()
                        && chbxProcessWillImplement.isSelected()) {
                            String myRoleName = fldProcessRoleName.getText();
                            String partnerRoleName = fldPartnerRoleName.getText();
                            if (myRoleName != null
                                    && myRoleName.length() > 0
                                    && myRoleName.equals(partnerRoleName)) {
                                addReasonKey("ERR_PLT_ROLES_NOT_UNIQUE"); //NOI18N
                            }
                        }
                    }
                    //
                    //
                    return isReasonsListEmpty();
                }
                
            };
        }
        return myValidator;
    }
    
    public HelpCtx getHelpCtx() {
        String helpId = ((BpelNode)myEditor.getEditedNode()).
                getNodeType().getHelpId();
        return new HelpCtx(helpId);
    }
    
    private class WsdlFileRenderer extends DefaultListCellRenderer {
        static final long serialVersionUID = 1L;
        
        public WsdlFileRenderer() {
            super();
            setIcon(new ImageIcon(NodeType.WSDL_FILE.getImage()));
        }
        
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (value != null && value instanceof FileObject) {
                String result = ResolverUtility.calculateRelativePathName(
                        ((FileObject)value), myEditor.getLookup());
                setText(result);
                //
//                    String wsdlFileName = ((FileObject)value).getName();
//                    setText(wsdlFileName);
                // setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
            }
            return this;
        }
    }
    
    private class PortTypeRenderer extends DefaultListCellRenderer {
        static final long serialVersionUID = 1L;
        
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (value != null && value instanceof PortType) {
                String portTypeName = ((PortType)value).getName();
                setText(portTypeName);
                // setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
            }
            return this;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        btngrPLT = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        fldPartnerLinkName = new javax.swing.JTextField();
        lblWsdlFile = new javax.swing.JLabel();
        lblPartnerLinkType = new javax.swing.JLabel();
        lblMyRole = new javax.swing.JLabel();
        lblPartnerRole = new javax.swing.JLabel();
        cbxPartnerLinkType = new javax.swing.JComboBox();
        cbxWsdlFile = new javax.swing.JComboBox();
        lblErrorMessage = new javax.swing.JLabel();
        rbtnUseExistingPLT = new javax.swing.JRadioButton();
        rbtnCreateNewPLT = new javax.swing.JRadioButton();
        lblNewPLTypeName = new javax.swing.JLabel();
        chbxProcessWillImplement = new javax.swing.JCheckBox();
        lblProcessRoleName = new javax.swing.JLabel();
        lblProcessPortType = new javax.swing.JLabel();
        chbxPartnerWillImpement = new javax.swing.JCheckBox();
        lblPartnerRoleName = new javax.swing.JLabel();
        lblPartnerPortType = new javax.swing.JLabel();
        fldNewPLTName = new javax.swing.JTextField();
        fldProcessRoleName = new javax.swing.JTextField();
        cbxProcessPortType = new javax.swing.JComboBox();
        fldPartnerRoleName = new javax.swing.JTextField();
        cbxPartnerPortType = new javax.swing.JComboBox();
        btnSwapRoles = new javax.swing.JButton();
        fldMyRole = new javax.swing.JTextField();
        fldPartnerRole = new javax.swing.JTextField();
        lblNewPLTypeName1 = new javax.swing.JLabel();
        myWsdlWrapperName = new javax.swing.JTextField();
        myBrowseButton = new javax.swing.JButton();

        getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_PNL_PartnerLinkMain"));
        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_PNL_PartnerLinkMain"));
        lblName.setLabelFor(fldPartnerLinkName);
        lblName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Name"));
        lblName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Name"));
        lblName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Name"));

        fldPartnerLinkName.setColumns(30);
        fldPartnerLinkName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_INP_Name"));
        fldPartnerLinkName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_INP_Name"));

        lblWsdlFile.setLabelFor(cbxWsdlFile);
        lblWsdlFile.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_WsdlFile"));
        lblWsdlFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_WsdlFile"));
        lblWsdlFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_WsdlFile"));

        lblPartnerLinkType.setLabelFor(cbxPartnerLinkType);
        lblPartnerLinkType.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_PartnerLinkType"));
        lblPartnerLinkType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_PartnerLinkType"));
        lblPartnerLinkType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_PartnerLinkType"));

        lblMyRole.setLabelFor(fldMyRole);
        lblMyRole.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_MyRole"));
        lblMyRole.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_MyRole"));
        lblMyRole.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_MyRole"));

        lblPartnerRole.setLabelFor(fldPartnerRole);
        lblPartnerRole.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_PartnerRole"));
        lblPartnerRole.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_PartnerRole"));
        lblPartnerRole.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_PartnerRole"));

        cbxPartnerLinkType.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_CMB_PartnerLinkType"));
        cbxPartnerLinkType.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_CMB_PartnerLinkType"));

        cbxWsdlFile.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_CMB_WsdlFile"));
        cbxWsdlFile.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_CMB_WsdlFile"));

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_ErrorLabel"));
        lblErrorMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_ErrorLabel"));

        btngrPLT.add(rbtnUseExistingPLT);
        rbtnUseExistingPLT.setSelected(true);
        rbtnUseExistingPLT.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"RBTN_UseExisingPLType"));
        rbtnUseExistingPLT.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtnUseExistingPLT.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbtnUseExistingPLT.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_RBTN_UseExisingPLType"));
        rbtnUseExistingPLT.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_RBTN_UseExisingPLType"));

        btngrPLT.add(rbtnCreateNewPLT);
        rbtnCreateNewPLT.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"RBTN_CreateNewPLType"));
        rbtnCreateNewPLT.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtnCreateNewPLT.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbtnCreateNewPLT.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_RBTN_CreateNewPLType"));
        rbtnCreateNewPLT.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_RBTN_CreateNewPLType"));

        lblNewPLTypeName.setLabelFor(fldNewPLTName);
        lblNewPLTypeName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_NewPLTypeName"));
        lblNewPLTypeName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_LBL_NewPLTypeName"));
        lblNewPLTypeName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_LBL_NewPLTypeName"));

        chbxProcessWillImplement.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"CHBX_ProcessWillImpement"));
        chbxProcessWillImplement.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbxProcessWillImplement.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chbxProcessWillImplement.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_CHBX_ProcessWillImpement"));
        chbxProcessWillImplement.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_CHBX_ProcessWillImpement"));

        lblProcessRoleName.setLabelFor(fldProcessRoleName);
        lblProcessRoleName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_ProcessRoleName"));
        lblProcessRoleName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_LBL_ProcessRoleName"));
        lblProcessRoleName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_LBL_ProcessRoleName"));

        lblProcessPortType.setLabelFor(cbxProcessPortType);
        lblProcessPortType.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_ProcessProtType"));
        lblProcessPortType.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_LBL_ProcessProtType"));
        lblProcessPortType.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_LBL_ProcessProtType"));

        chbxPartnerWillImpement.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"CHBX_PartnerWillImplement"));
        chbxPartnerWillImpement.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbxPartnerWillImpement.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chbxPartnerWillImpement.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_CHBX_PartnerWillImplement"));
        chbxPartnerWillImpement.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_CHBX_PartnerWillImplement"));

        lblPartnerRoleName.setLabelFor(fldPartnerRoleName);
        lblPartnerRoleName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_PartnerRoleName"));
        lblPartnerRoleName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_LBL_PartnerRoleName"));
        lblPartnerRoleName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_LBL_PartnerRoleName"));

        lblPartnerPortType.setLabelFor(cbxPartnerPortType);
        lblPartnerPortType.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_PartnerPortType"));
        lblPartnerPortType.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_LBL_PartnerPortType"));
        lblPartnerPortType.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_LBL_PartnerPortType"));

        fldNewPLTName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_INP_NewPLTypeName"));
        fldNewPLTName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_INP_NewPLTypeName"));

        fldProcessRoleName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_INP_ProcessRoleName"));
        fldProcessRoleName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_INP_ProcessRoleName"));

        cbxProcessPortType.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_CMB_ProcessProtType"));
        cbxProcessPortType.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_CMB_ProcessProtType"));

        fldPartnerRoleName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_INP_PartnerRoleName"));
        fldPartnerRoleName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_INP_PartnerRoleName"));

        cbxPartnerPortType.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_CMB_PartnerPortType"));
        cbxPartnerPortType.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_CMB_PartnerPortType"));

        btnSwapRoles.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"BTN_SwapRoles"));
        btnSwapRoles.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_BTN_SwapRoles"));
        btnSwapRoles.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_BTN_SwapRoles"));

        fldMyRole.setEditable(false);
        fldMyRole.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_INP_MyRole"));
        fldMyRole.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_INP_MyRole"));

        fldPartnerRole.setEditable(false);
        fldPartnerRole.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSN_INP_PartnerRole"));
        fldPartnerRole.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/bpel/properties/editors/Bundle").getString("ACSD_INP_PartnerRole"));

        lblNewPLTypeName1.setLabelFor(myWsdlWrapperName);
        lblNewPLTypeName1.setText("Create in Fil&e:");

        myBrowseButton.setText("&Browse");
        myBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseWsdlFile(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(lblNewPLTypeName1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myWsdlWrapperName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myBrowseButton))
                    .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .add(rbtnUseExistingPLT)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lblName)
                                    .add(lblWsdlFile))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(fldPartnerLinkName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, cbxWsdlFile, 0, 447, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lblMyRole)
                                    .add(lblPartnerRole)
                                    .add(lblPartnerLinkType))
                                .add(0, 0, 0)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, cbxPartnerLinkType, 0, 397, Short.MAX_VALUE)
                                    .add(layout.createSequentialGroup()
                                        .add(btnSwapRoles)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 300, Short.MAX_VALUE))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, fldMyRole, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, fldPartnerRole, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(rbtnCreateNewPLT)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(chbxProcessWillImplement)
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lblProcessRoleName)
                                    .add(lblProcessPortType))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(cbxProcessPortType, 0, 409, Short.MAX_VALUE)
                                    .add(fldProcessRoleName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)))
                            .add(chbxPartnerWillImpement)
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lblPartnerRoleName)
                                    .add(lblPartnerPortType))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(cbxPartnerPortType, 0, 409, Short.MAX_VALUE)
                                    .add(fldPartnerRoleName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .add(lblNewPLTypeName)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fldNewPLTName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(fldPartnerLinkName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblWsdlFile)
                    .add(cbxWsdlFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(rbtnUseExistingPLT)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbxPartnerLinkType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblPartnerLinkType))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblMyRole)
                    .add(fldMyRole, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPartnerRole)
                    .add(fldPartnerRole, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnSwapRoles)
                .add(11, 11, 11)
                .add(rbtnCreateNewPLT)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblNewPLTypeName1)
                    .add(myBrowseButton)
                    .add(myWsdlWrapperName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblNewPLTypeName)
                    .add(fldNewPLTName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbxProcessWillImplement)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblProcessRoleName)
                    .add(fldProcessRoleName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblProcessPortType)
                    .add(cbxProcessPortType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbxPartnerWillImpement)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPartnerRoleName)
                    .add(fldPartnerRoleName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPartnerPortType)
                    .add(cbxPartnerPortType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // vlv
    private void browseWsdlFile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseWsdlFile
        String title = NbBundle.getMessage(FormBundle.class, "LBL_Select_WSDL_File"); // NOI18N
        WSDLFileImportDialog dialog = new WSDLFileImportDialog(getCurrentWsdlModel());
        DialogDescriptor descriptor = UIUtilities.getCustomizerDialog(dialog, title, true);
        descriptor.setValid(false);
        Object result = DialogDisplayer.getDefault().notify(descriptor);
        
        if (result != DialogDescriptor.OK_OPTION) {
            return;
        }
        FileObject file = org.netbeans.modules.bpel.editors.api.utils.Util.
                getFileObjectByModel(dialog.getModel());
        
        String text = getRelativeName(file);
        myWsdlWrapperName.setText(text);
    
        getValidator().revalidate(true);
    }//GEN-LAST:event_browseWsdlFile
    
    // vlv
    private void setWrapper(FileObject file) {
        if (file != null) {
            myWsdlWrapperName.setText(getRelativeName(file) + "Wrapper"); // NOI18N
        }
    }

    // vlv
    private String getRelativeName(FileObject file) {
        String name = ResolverUtility.calculateRelativePathName(file, myEditor.getLookup());
        int k = name.lastIndexOf(".wsdl"); // NOI18N
        
        if (k == -1) {
            return name;
        }
        return name.substring(0, k);
    }
    
    // vlv
    private void out() {
        System.out.println();
    }
    
    private void out(Object object) {
        System.out.println("*** " + object); // NOI18N
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSwapRoles;
    private javax.swing.ButtonGroup btngrPLT;
    private javax.swing.JComboBox cbxPartnerLinkType;
    private javax.swing.JComboBox cbxPartnerPortType;
    private javax.swing.JComboBox cbxProcessPortType;
    private javax.swing.JComboBox cbxWsdlFile;
    private javax.swing.JCheckBox chbxPartnerWillImpement;
    private javax.swing.JCheckBox chbxProcessWillImplement;
    private javax.swing.JTextField fldMyRole;
    private javax.swing.JTextField fldNewPLTName;
    private javax.swing.JTextField fldPartnerLinkName;
    private javax.swing.JTextField fldPartnerRole;
    private javax.swing.JTextField fldPartnerRoleName;
    private javax.swing.JTextField fldProcessRoleName;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblMyRole;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNewPLTypeName;
    private javax.swing.JLabel lblNewPLTypeName1;
    private javax.swing.JLabel lblPartnerLinkType;
    private javax.swing.JLabel lblPartnerPortType;
    private javax.swing.JLabel lblPartnerRole;
    private javax.swing.JLabel lblPartnerRoleName;
    private javax.swing.JLabel lblProcessPortType;
    private javax.swing.JLabel lblProcessRoleName;
    private javax.swing.JLabel lblWsdlFile;
    private javax.swing.JButton myBrowseButton;
    private javax.swing.JTextField myWsdlWrapperName;
    private javax.swing.JRadioButton rbtnCreateNewPLT;
    private javax.swing.JRadioButton rbtnUseExistingPLT;
    // End of variables declaration//GEN-END:variables
}
