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
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.ToPartContainer;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.nodes.VariableNode;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.VirtualVariableContainer;
import org.netbeans.modules.bpel.properties.choosers.NewMessageVarChooser;
import org.netbeans.modules.bpel.properties.editors.controls.PartVariableTable;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.bpel.properties.choosers.VariableChooserPanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.bpel.properties.editors.controls.MessageConfigurationController;
import org.netbeans.modules.bpel.properties.editors.controls.MessageExchangeController;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.bpel.properties.editors.controls.TreeNodeChooser;
import org.netbeans.modules.bpel.properties.editors.controls.filter.PreferredFaultFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VisibilityScope;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.bpel.editors.api.ui.valid.NodeEditorDescriptor;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.props.editors.FaultNamePropertyCustomizer;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author  nk160297
 */
public class ReplyMainPanel extends EditorLifeCycleAdapter
        implements Validator.Provider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<Reply> myEditor;
    
    private MessageConfigurationController mcc;
    private MessageExchangeController mec;
    
    private QName myFaultName;
    private VirtualVariableContainer currFaultVar;
    
    private DefaultValidator myValidator;
    
    /**
     * If true then the normal output is implied.
     * Otherwize the fault output is implied.
     */
    private boolean useNormalOutput;
    
    public ReplyMainPanel(CustomNodeEditor<Reply> anEditor) {
        this.myEditor = anEditor;
        createContent();
    }
    
    public void createContent() {
        mcc = new MessageConfigurationController(myEditor);
        mcc.createContent();
        mcc.setVisibleVariables(false, true, false);
        mcc.useMyRole();
        mcc.setConfigurationListener(
                new MessageConfigurationController.ConfigurationListener() {
            public void partnerLinkChanged() {
                updateEnabledState();
                //
                getValidator().revalidate(true);
            }
            public void operationChanged() {
                updateEnabledState();
                //
                if (rbtnFaultResponse.isSelected()) {
                    presetFaultName();
                }
                //
                getValidator().revalidate(true);
            }
            
        });
        mec = new MessageExchangeController(myEditor);
        mec.createContent();
        //
        initComponents();
        bindControls2PropertyNames();
        
//        Reply reply = (Reply) mcc.getEditedObject();
//        
//        boolean hasFaultName = (reply.getFaultName() != null);
//        boolean hasToParts = false;
//        boolean hasVariable = false;
//        
//        ToPartContainer container = reply.getToPartContaner();
//        if (container != null) {
//            hasToParts = (container.sizeOfToParts() > 0);
//        }
//        
//        BpelReference<VariableDeclaration> variableDeclaration = reply.getVariable();
//        if (variableDeclaration != null) {
//            hasVariable = (variableDeclaration.get() != null);
//        }
//        
        // Issue 85553 start
        lblMessageExchange.setVisible(false);
        fldMessageExchange.setVisible(false);
        btnChooseMessEx.setVisible(false);
        // Issue 85553 end
        
        //
        btnChooseFaultName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseFaultName();
            }
        });
        //
        ActionListener responseTypeListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == rbtnFaultResponse) {
                    presetFaultName();
                }
                //
                updateEnabledState();
                getValidator().revalidate(true);
            }
        };
        rbtnNormalResponse.addActionListener(responseTypeListener);
        rbtnFaultResponse.addActionListener(responseTypeListener);
        //
        btnChooseFaultVariable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseFaultVariable();
            }
        });
        //
        btnNewFaultVariable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VirtualVariableContainer vvc = prepareNewFaultVariable();
                if (vvc != null) {
                    setCurrFaultVar(vvc);
                }
            }
        });
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
        
        
//        if (hasFaultName) {
//            rbtnFaultVariable.setSelected(!hasToParts);
//            rbtnFaultParts.setSelected(hasToParts);
//        } else {
//            rbtnFaultVariable.setSelected(true);
//            rbtnFaultParts.setSelected(false);
//        }
//        
//        updateEnabledStateOfFaultUI();
//        
        ActionListener faultRadioActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateEnabledStateOfFaultUI();
            }
        };
        
        rbtnFaultVariable.addActionListener(faultRadioActionListener);
        rbtnFaultParts.addActionListener(faultRadioActionListener);
    }
    
    /**
     * Binds simple controls to names of properties.
     * This is necessary for automatic value inicialization and value inquiry.
     */
    private void bindControls2PropertyNames() {
        fldName.putClientProperty(
                CustomNodeEditor.PROPERTY_BINDER, PropertyType.NAME);
    }
    
    public boolean initControls() {
        Reply reply = myEditor.getEditedObject();
        if (reply != null) {
            QName faultName = reply.getFaultName();
            if (faultName != null) {
                setFaultName(faultName);
                //
                VariableDeclaration faultVar = null;
                BpelReference<VariableDeclaration> faultVarRef = reply.getVariable();
                if (faultVarRef != null) {
                    faultVar = faultVarRef.get();
                }
                Lookup lookup = myEditor.getLookup();
                VirtualVariableContainer vvc =
                        new VirtualVariableContainer(faultVar, lookup);
                setCurrFaultVar(vvc);
            }
        }
        //
        //

        //
        if (myFaultName != null) {
            rbtnFaultResponse.setSelected(true);
        } else {
            rbtnNormalResponse.setSelected(true);
        }
        ((PartVariableTable) tblFaultParts).setMessage(getFaultMessage());

        boolean hasFaultName = (reply.getFaultName() != null);
        boolean hasToParts = false;
        boolean hasVariable = false;
        
        ToPartContainer container = reply.getToPartContaner();
        if (container != null) {
            hasToParts = (container.sizeOfToParts() > 0);
        }
        
        BpelReference<VariableDeclaration> variableDeclaration = reply.getVariable();
        if (variableDeclaration != null) {
            hasVariable = (variableDeclaration.get() != null);
        }
        
        if (hasFaultName) {
            Message faultMessage = null;
            QName faultQName = reply.getFaultName();
            String faultNameLocalPart = faultQName.getLocalPart();
            
            if (faultNameLocalPart != null) {
                WSDLReference<Operation> operationReference = reply.getOperation();
                Operation operation = (operationReference == null) ? null 
                        : operationReference.get();
                Collection<Fault> faults = (operation == null) ? null
                        : operation.getFaults();
                if (faults != null) {
                    for (Fault fault : faults) {
                        String faultName = fault.getName();
                        if (faultName != null 
                                && faultName.equals(faultNameLocalPart))
                        {
                            NamedComponentReference<Message> faultMessageReference 
                                    = fault.getMessage();
                            faultMessage = (faultMessageReference == null) ? null
                                    : faultMessageReference.get();
                            if (faultMessage != null) {
                                break;
                            }
                        }
                    }
                }
            }
            
            rbtnFaultVariable.setSelected(!hasToParts);
            rbtnFaultParts.setSelected(hasToParts);
            ((PartVariableTable) tblFaultParts).setMessage(faultMessage);
        } else {
            rbtnFaultVariable.setSelected(true);
            rbtnFaultParts.setSelected(false);
        }
        
        updateEnabledState();
        
        mcc.initControls();
        mec.initControls();
        //
        getValidator().revalidate(true);
        //
        return true;
    }
    
    public boolean subscribeListeners() {
        mcc.subscribeListeners();
        mec.subscribeListeners();
        return true;
    }
    
    public boolean unsubscribeListeners() {
        mcc.unsubscribeListeners();
        mec.unsubscribeListeners();
        return true;
    }
    
    public boolean applyNewValues() throws VetoException {
        mcc.applyNewValues();
        mec.applyNewValues();
        //
        Reply reply = myEditor.getEditedObject();
        if (reply != null) {
            if (!useNormalOutput && myFaultName != null) {
                reply.setFaultName(myFaultName);
            } else {
                reply.removeFaultName();
            }
            //
            if (!useNormalOutput) {
                if (rbtnFaultVariable.isSelected()) {
                    if (currFaultVar != null) {
                        VariableDeclaration varDecl = currFaultVar
                                .createNewVariable();
                        BpelReference<VariableDeclaration> varRef = reply
                                .createReference(varDecl, VariableDeclaration.class);
                        reply.setVariable(varRef);
                    }
                    ((PartVariableTable) tblFaultParts).removeParts();
                } else {
                    ((PartVariableTable) tblFaultParts).applyChanges();
                }
            }
            // no need to clear the variable property here if the fault variable
            // isn't specified because it has to be cleared before
            // by the MessageConfigurationController
            //
        }
        return true;
    }
    
    public boolean afterClose() {
        mcc.afterClose();
        mec.afterClose();
        return true;
    }
    
    private void setFaultName(QName newValue) {
        myFaultName = newValue;
        //
        if (newValue == null) {
            fldFaultName.setText("");
        } else {
            Reply reply = myEditor.getEditedObject();
            String text = ResolverUtility.qName2DisplayText(myFaultName, reply);
            fldFaultName.setText(text);
        }
        
        ((PartVariableTable) tblFaultParts).setMessage(getFaultMessage());
        updateEnabledStateOfFaultUI();
    }
    
    private void chooseFaultName() throws MissingResourceException {
        Lookup lookup = myEditor.getLookup();
        //
        Collection<QName> preferredFaults = getPreferredFaults();
        if (preferredFaults != null && preferredFaults.size() != 0) {
            PreferredFaultFilter filter =
                    new PreferredFaultFilter(preferredFaults);
            lookup = new ExtendedLookup(lookup, filter);
        }
        //
        FaultNamePropertyCustomizer faultNameChooser =
                new FaultNamePropertyCustomizer(lookup);
        faultNameChooser.initControls();
        if (myFaultName != null) {
            faultNameChooser.setSelectedValue(myFaultName);
        }
        String title = NbBundle.getMessage(FormBundle.class,
                "DLG_FaultNameChooserTitle"); // NOI18N
        DefaultDialogDescriptor descriptor =
                new DefaultDialogDescriptor(faultNameChooser, title);
        SoaDialogDisplayer.getDefault().notify( descriptor );
        if (descriptor.isOkHasPressed()) {
            Object newValue = faultNameChooser.getSelectedValue();
            if (newValue instanceof QName) {
                setFaultName((QName)newValue);
            }
// issue 85149 No way to clear the Fault Name field in Reply editor
            else if (newValue == null) {
                setFaultName(null);
            }
            //
            ((PartVariableTable) tblFaultParts).setMessage(getFaultMessage());
            updateEnabledState();
        }
    }
    
    private Collection<QName> getPreferredFaults() {
        Collection<QName> faultNames = new HashSet<QName>();
        //
        Object item = cbxOperation.getSelectedItem();
        if (item != null) {
            Operation operation = (Operation)item;
            String namespace = operation.getModel().
                    getDefinitions().getTargetNamespace();
            Message requiredType = null;
            Collection<Fault> faults = operation.getFaults();
            for (Fault fault : faults) {
                QName faultQName = new QName(namespace, fault.getName());
                faultNames.add(faultQName);
            }
        }
        //
        return faultNames;
    }
    
    private void presetFaultName() {
        if (myFaultName == null) {
            Object item = cbxOperation.getSelectedItem();
            if (item != null) {
                Operation operation = (Operation)item;
                Message requiredType = null;
                Collection<Fault> faults = operation.getFaults();
                Iterator<Fault> faultItr = faults.iterator();
                if (faultItr.hasNext()) {
                    Fault firstFault = faultItr.next();
                    String faultName = firstFault.getName();
                    String namespace = operation.getModel().
                            getDefinitions().getTargetNamespace();
                    QName faultQName = new QName(namespace, faultName);
                    setFaultName(faultQName);
                }
            }
        }
    }
    
    
    private void setEnabled(boolean enabled, Component... components) {
        for (Component c : components) {
            c.setEnabled(enabled);
        }
    }
    
    
    private Message getFaultMessage() {
        NamedComponentReference<Message> messageReference 
                = getRequiredFaultTypeRef();
        return (messageReference == null) ? null : messageReference.get();
    }
    
    
    private void updateEnabledStateOfFaultUI() {
        boolean faultEnabled = rbtnFaultResponse.isSelected();
        boolean variableEnabled;
        boolean partsEnabled;

        if (getFaultMessage() == null || !faultEnabled) {
            variableEnabled = false;
            partsEnabled = false;
        } else {
            variableEnabled = rbtnFaultVariable.isSelected();
            partsEnabled = rbtnFaultParts.isSelected();
        }
        
        setEnabled(faultEnabled, rbtnFaultVariable, rbtnFaultParts, 
                fldFaultName, btnChooseFaultName, lblFaultName);
        setEnabled(variableEnabled, fldFaultVariable, btnNewFaultVariable, 
                btnChooseFaultVariable);
        setEnabled(partsEnabled, tblFaultParts);
    }
    
    
    private void updateEnabledState() {
        useNormalOutput = rbtnNormalResponse.isSelected();
        mcc.setOutputVarEnabled(useNormalOutput);
        updateEnabledStateOfFaultUI();
//        int index = cbxOperation.getSelectedIndex();
//        if (index == -1) {
//            lblFaultName.setEnabled(false);
//            fldFaultName.setEnabled(false);
//            btnChooseFaultName.setEnabled(false);
//            rbtnFaultVariable.setEnabled(false);
//            fldFaultVariable.setEnabled(false);
//            btnNewFaultVariable.setEnabled(false);
//            btnChooseFaultVariable.setEnabled(false);
//            tblFaultParts.setEnabled(false);
//        } else {
//            useNormalOutput = rbtnNormalResponse.isSelected();
//            mcc.setOutputVarEnabled(useNormalOutput);
//            updateEnabledStateOfFaultUI();
//        }
    }
    
    private void chooseFaultVariable() {
        Collection<QName> varTypesQName = getAllowedFaultTypesQName();
        
        if (varTypesQName.isEmpty()) {
            // Null filter allows to show all fault variables
            varTypesQName = null;
        }
        String title = NbBundle.getMessage(FormBundle.class,
                "DLG_FaultVariableChooser"); // NOI18N
        //
        // Construct context lookup
        Lookup lookup = myEditor.getLookup();
        BpelEntity entry = (BpelEntity)myEditor.getEditedObject();
        VisibilityScope visScope = new VisibilityScope(entry, lookup);
        final VariableTypeFilter typeFilter = new VariableTypeFilter(
                null, varTypesQName);
        typeFilter.setShowAppropriateVarOnly(true);
        //
        NodesTreeParams treeParams = new NodesTreeParams();
        treeParams.setTargetNodeClasses(VariableNode.class);
        treeParams.setLeafNodeClasses(VariableNode.class);
        //
        Lookup contextLookup = new ExtendedLookup(
                lookup, visScope, typeFilter, treeParams);
        //
        VariableChooserPanel varChooserPanel =
                new VariableChooserPanel(contextLookup);
        TreeNodeChooser chooser = new TreeNodeChooser(varChooserPanel);
        chooser.initControls();
        if (currFaultVar != null && currFaultVar.isExisting()) {
            varChooserPanel.setSelectedValue(currFaultVar.getVariableDeclaration());
        }
        DefaultDialogDescriptor descriptor =
                new DefaultDialogDescriptor(chooser, title);
        SoaDialogDisplayer.getDefault().notify( descriptor );
        if (descriptor.isOkHasPressed()) {
            VariableDeclaration newVariable = varChooserPanel.getSelectedValue();
            VirtualVariableContainer newVvc =
                    new VirtualVariableContainer(newVariable, lookup);
            setCurrFaultVar(newVvc);
        }
    }
    
    private Collection<QName> getAllowedFaultTypesQName() {
        Collection<QName> faultTypes = new HashSet<QName>();
        Object item = cbxOperation.getSelectedItem();
        
        if (item == null || myFaultName == null) {
            return faultTypes;
        }
        Operation operation = (Operation) item;
        Collection<Fault> faults = operation.getFaults();
        
        WSDLModel model = operation.getModel();
        String namespace = model.getDefinitions().getTargetNamespace();
//System.out.println();
//System.out.println("my  namespace: " + myFaultName.getNamespaceURI());
//System.out.println("see namespace: " + namespace);
        
        if ( !namespace.equals(myFaultName.getNamespaceURI())) {
            return faultTypes;
        }
        String location = myFaultName.getLocalPart();
//System.out.println("my   location: " + location);
        
        for (Fault fault : faults) {
//System.out.println("see  location: " + fault.getName());
            if ( !fault.getName().equals(location)) {
                continue;
            }
            NamedComponentReference<Message> faultMsgRef = fault.getMessage();
            
            if (faultMsgRef == null) {
                continue;
            }
            QName faultMsgQName = faultMsgRef.getQName();
            
            if (faultMsgQName != null) {
                faultTypes.add(faultMsgQName);
            }
        }
        return faultTypes;
    }
    
    private VirtualVariableContainer prepareNewFaultVariable() {
        Lookup lookup = myEditor.getLookup();
        BpelModel model = (BpelModel)lookup.lookup(BpelModel.class);
        Process process = model.getProcess();
        BPELElementsBuilder elementBuilder = model.getBuilder();
        //
        Operation operation = (Operation)cbxOperation.getSelectedItem();
        if (operation == null) {
            return null;
        }
        //
        NamedComponentReference<Message> msgRef = getRequiredFaultTypeRef();
        if (msgRef == null) {
            String text = NbBundle.getMessage(
                    FormBundle.class, "MSG_FaultMessageNotSpecified");
            //
            UserNotification.showMessageAsinc(text);
            return null;
        }
        //
        BpelEntity omElement = (BpelEntity)myEditor.getEditedObject();
        NewMessageVarChooser chooser = new NewMessageVarChooser(
                omElement, myFaultName.getLocalPart(), msgRef.get(),
                Constants.MessageDirection.FAULT);
        chooser.initControls();
        //
        String title = NbBundle.getMessage(
                FormBundle.class, "DLG_NewFaultVariable"); // NOI18N
        DefaultDialogDescriptor descriptor =
                new DefaultDialogDescriptor(chooser, title);
        //
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.isOkHasPressed()) {
            return chooser.getSelectedValue();
        }
        //
        return null;
    }
    
    public void setCurrFaultVar(VirtualVariableContainer newValue) {
        if ((currFaultVar == null && newValue != null) ||
                (currFaultVar != null && !currFaultVar.equals(newValue))) {
            currFaultVar = newValue;
            fldFaultVariable.setText(
                    currFaultVar == null ? "" : currFaultVar.getName());
        }
    }
    
    public DefaultValidator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(myEditor, ErrorMessagesBundle.class) {
                
                public boolean doFastValidation() {
                    
                    if (rbtnNormalResponse.isSelected()){
                        Object item = cbxOperation.getSelectedItem();
                        if (item instanceof Operation &&
                                ((Operation) item).getOutput() == null) {
                            addReasonKey("ERR_OPERATION_NO_OUTPUT",
                                    ((Operation) item).getName());
                            return false;
                        }
                    }
                    return true;
                }
                
                public boolean doDetailedValidation() {
                    if (rbtnFaultResponse.isSelected()) {
                        if (myFaultName == null) {
                            addReasonKey("ERR_FAULT_NAME_EMPTY"); //NOI18N
                        }
                        //
                        isCorrectFaultType(currFaultVar);
                    }
                    return isReasonsListEmpty();
                }
                
                /**
                 * Check if the variable has the correct fault type.
                 */
                private boolean isCorrectFaultType(VirtualVariableContainer vcc) {
                    NamedComponentReference<Message> requiredMessageRef =
                            getRequiredFaultTypeRef();
                    if (requiredMessageRef != null) {
                        Message varType = null;
                        Message varMsg = null;
                        if (vcc != null) {
                            varMsg = vcc.getType().getMessage();
                        }
                        
                        if (rbtnFaultVariable.isSelected()) {
                            if (varMsg == null) {
                                addReasonKey("ERR_FAULT_VAR_EMPTY"); //NOI18N
                                return false;
                            } else if (!requiredMessageRef.get().equals(varMsg)) {
                                String required = ResolverUtility.
                                        qName2DisplayText(requiredMessageRef.getQName());
                                String current = ResolverUtility.
                                        qName2DisplayText(vcc.getType().getTypeQName());
                                addReasonKey("ERR_FAULT_VAR_WRONG_TYPE", required, current); //NOI18N
                                return false;
                            }
                        }
                    }
                    //
                    return true;
                }
                
            };
        }
        return myValidator;
    }
    
    private boolean isFaultSpecifiedForOperation() {
        Object item = cbxOperation.getSelectedItem();
        if (item != null) {
            Operation operation = (Operation)item;
            Message requiredType = null;
            Collection<Fault> faults = operation.getFaults();
            if (faults != null && faults.size() > 0) {
                return true;
            }
        }
        //
        return false;
    }
    
    private Collection<Message> getAllowedFaultTypes() {
        Collection<Message> faultMessages = new HashSet<Message>();
        //
        Object item = cbxOperation.getSelectedItem();
        if (item != null) {
            Operation operation = (Operation)item;
            Message requiredType = null;
            Collection<Fault> faults = operation.getFaults();
            for (Fault fault : faults) {
                NamedComponentReference<Message> faultMsgRef = fault.getMessage();
                if (faultMsgRef != null) {
                    Message faultMsg = faultMsgRef.get();
                    if (faultMsg != null) {
                        faultMessages.add(faultMsg);
                    }
                }
            }
        }
        //
        return faultMessages;
    }
    
    /**
     * Returns the Message type which is required for the fault variable.
     * The not null value is returned only in the fallowing conditions:
     * -- an operation is selected in the dialog
     * -- a fault type is selected in the dialog
     * -- the operation has a fault specified
     * -- the fault has a message type specified
     * -- the fault has the same type as the fault type selected in the dialog
     */
    private NamedComponentReference<Message> getRequiredFaultTypeRef() {
        if (myFaultName == null) {
            return null;
            
        }
        NamedComponentReference<Message> faultMessageRef = null;
        //
        Object item = cbxOperation.getSelectedItem();
        if (item != null) {
            Operation operation = (Operation)item;
            String targetNamespace = operation.getModel().
                    getDefinitions().getTargetNamespace();
            //
            if (myFaultName != null ||
                    targetNamespace.equals(myFaultName.getNamespaceURI())) {
                // The fault is specified by the user is important because
                // the operation can contain a few nested fault declarations.
                // So we are looking for the particular fault which corresponds
                // to the value choosen in the Reply Editor (myFaultName).
                //
                // It is implied further that the namespaces are the same
                // for the operation's fault and myFaultName.
                //
                Collection<Fault> faults = operation.getFaults();
                for (Fault fault : faults) {
                    if (fault != null) {
                        String faultName = fault.getName();
                        if (faultName != null &&
                                faultName.equals(myFaultName.getLocalPart())) {
                            //
                            // The suitable fault is found now
                            faultMessageRef = fault.getMessage();
                            break;
                        }
                    }
                }
            }
        }
        //
        return faultMessageRef;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        faultButtonGroup = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        fldName = new javax.swing.JTextField();
        lblMessageExchange = new javax.swing.JLabel();
        fldMessageExchange = mec.getFldMessageExchange();
        btnChooseMessEx = mec.getBtnChooseMsgEx();
        lblPartnerLink = new javax.swing.JLabel();
        cbxPartnerLink = mcc.getCbxPartnerLink();
        lblOperation = new javax.swing.JLabel();
        cbxOperation = mcc.getCbxOperation();
        lblErrorMessage = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        rbtnNormalResponse = new javax.swing.JRadioButton();
        jRadioButton1 = mcc.getRbtnOutputVariable();
        btnChooseOutputVariable = mcc.getBtnChooseOutputVariable();
        btnNewOutputVariable = mcc.getBtnNewOutputVariable();
        fldOutputVariable = mcc.getFldOutputVariable();
        jRadioButton2 = mcc.getRbtnOutputParts();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = mcc.getTblOutputParts();
        jPanel3 = new javax.swing.JPanel();
        rbtnFaultResponse = new javax.swing.JRadioButton();
        lblFaultName = new javax.swing.JLabel();
        btnChooseFaultName = new javax.swing.JButton();
        fldFaultName = new javax.swing.JTextField();
        rbtnFaultVariable = new javax.swing.JRadioButton();
        btnChooseFaultVariable = new javax.swing.JButton();
        btnNewFaultVariable = new javax.swing.JButton();
        fldFaultVariable = new javax.swing.JTextField();
        rbtnFaultParts = new javax.swing.JRadioButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblFaultParts = new PartVariableTable(mcc, PartVariableTable.Type.FAULT);

        lblName.setLabelFor(fldName);
        lblName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Name"));
        lblName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Name"));
        lblName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Name"));

        fldName.setColumns(40);
        fldName.setName("");

        lblMessageExchange.setLabelFor(fldMessageExchange);
        lblMessageExchange.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_MessageExchange"));
        lblMessageExchange.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_MessageExchange"));
        lblMessageExchange.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_MessageExchange"));

        btnChooseMessEx.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"BTN_ChooseMessageExchange"));
        btnChooseMessEx.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnChooseMessEx.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_ChooseMessageExchange"));
        btnChooseMessEx.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_ChooseMessageExchange"));

        lblPartnerLink.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_PartnerLink"));
        lblPartnerLink.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_PartnerLink"));
        lblPartnerLink.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_PartnerLink"));

        lblOperation.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Operation"));
        lblOperation.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Operation"));
        lblOperation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Operation"));

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.setAlignmentX(0.5F);

        jPanel1.setLayout(new java.awt.GridLayout(2, 1));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Send Data"));
        buttonGroup1.add(rbtnNormalResponse);
        rbtnNormalResponse.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"RBTN_NormalResponse"));
        rbtnNormalResponse.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtnNormalResponse.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbtnNormalResponse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_RBTN_NormalResponse"));
        rbtnNormalResponse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_RBTN_NormalResponse"));

        jRadioButton1.setText("Output Variable:");
        jRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        btnChooseOutputVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_BrowseOutputVarible"));
        btnChooseOutputVariable.setMargin(new java.awt.Insets(0, 2, 0, 2));
        btnChooseOutputVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_BrowseOutputVarible"));
        btnChooseOutputVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_BrowseOutputVarible"));

        btnNewOutputVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_CreateOutputVariable"));
        btnNewOutputVariable.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnNewOutputVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_CreateOutputVariable"));
        btnNewOutputVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_CreateOutputVariable"));

        fldOutputVariable.setColumns(30);
        fldOutputVariable.setEditable(false);

        jRadioButton2.setText("To Parts:");
        jRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jScrollPane1.setViewportView(jTable1);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                        .add(44, 44, 44)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rbtnNormalResponse)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jRadioButton2)
                                    .add(jPanel2Layout.createSequentialGroup()
                                        .add(jRadioButton1)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(fldOutputVariable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(btnNewOutputVariable)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnChooseOutputVariable)))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(rbtnNormalResponse)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jRadioButton1)
                    .add(btnChooseOutputVariable)
                    .add(btnNewOutputVariable)
                    .add(fldOutputVariable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jRadioButton2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1.add(jPanel2);

        buttonGroup1.add(rbtnFaultResponse);
        rbtnFaultResponse.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"RBTN_FaultResponse"));
        rbtnFaultResponse.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtnFaultResponse.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbtnFaultResponse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_RBTN_FaultResponse"));
        rbtnFaultResponse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_RBTN_FaultResponse"));

        lblFaultName.setLabelFor(fldFaultName);
        lblFaultName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_FaultName"));
        lblFaultName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_FaultName"));
        lblFaultName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_FaultName"));

        btnChooseFaultName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"BNT_ChooseFaultName"));
        btnChooseFaultName.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnChooseFaultName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BNT_ChooseFaultName"));
        btnChooseFaultName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BNT_ChooseFaultName"));

        fldFaultName.setEditable(false);

        faultButtonGroup.add(rbtnFaultVariable);
        rbtnFaultVariable.setText("Fault Variable:");
        rbtnFaultVariable.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtnFaultVariable.setMargin(new java.awt.Insets(0, 0, 0, 0));

        btnChooseFaultVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_BrowseFaultVarible"));
        btnChooseFaultVariable.setMargin(new java.awt.Insets(0, 2, 0, 2));
        btnChooseFaultVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_BrowseFaultVarible"));
        btnChooseFaultVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_BrowseFaultVarible"));

        btnNewFaultVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_CreateFaultVariable"));
        btnNewFaultVariable.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnNewFaultVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_CreateFaultVariable"));
        btnNewFaultVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_CreateFaultVariable"));

        fldFaultVariable.setColumns(30);
        fldFaultVariable.setEditable(false);

        faultButtonGroup.add(rbtnFaultParts);
        rbtnFaultParts.setText("To Parts:");
        rbtnFaultParts.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtnFaultParts.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jScrollPane2.setViewportView(tblFaultParts);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rbtnFaultResponse)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(rbtnFaultParts)
                                    .add(jPanel3Layout.createSequentialGroup()
                                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(rbtnFaultVariable)
                                            .add(lblFaultName))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jPanel3Layout.createSequentialGroup()
                                                .add(fldFaultName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(btnChooseFaultName))
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                                                .add(fldFaultVariable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(btnNewFaultVariable)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(btnChooseFaultVariable))))))))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(44, 44, 44)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(rbtnFaultResponse)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblFaultName)
                    .add(btnChooseFaultName)
                    .add(fldFaultName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rbtnFaultVariable)
                    .add(btnChooseFaultVariable)
                    .add(btnNewFaultVariable)
                    .add(fldFaultVariable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbtnFaultParts)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1.add(jPanel3);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblPartnerLink)
                            .add(lblOperation)
                            .add(lblName))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbxPartnerLink, 0, 426, Short.MAX_VALUE)
                            .add(cbxOperation, 0, 426, Short.MAX_VALUE)
                            .add(fldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(lblMessageExchange)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fldMessageExchange, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnChooseMessEx))
                    .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(fldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPartnerLink)
                    .add(cbxPartnerLink, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblOperation)
                    .add(cbxOperation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblMessageExchange)
                    .add(btnChooseMessEx)
                    .add(fldMessageExchange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChooseFaultName;
    private javax.swing.JButton btnChooseFaultVariable;
    private javax.swing.JButton btnChooseMessEx;
    private javax.swing.JButton btnChooseOutputVariable;
    private javax.swing.JButton btnNewFaultVariable;
    private javax.swing.JButton btnNewOutputVariable;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbxOperation;
    private javax.swing.JComboBox cbxPartnerLink;
    private javax.swing.ButtonGroup faultButtonGroup;
    private javax.swing.JTextField fldFaultName;
    private javax.swing.JTextField fldFaultVariable;
    private javax.swing.JTextField fldMessageExchange;
    private javax.swing.JTextField fldName;
    private javax.swing.JTextField fldOutputVariable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblFaultName;
    private javax.swing.JLabel lblMessageExchange;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblOperation;
    private javax.swing.JLabel lblPartnerLink;
    private javax.swing.JRadioButton rbtnFaultParts;
    private javax.swing.JRadioButton rbtnFaultResponse;
    private javax.swing.JRadioButton rbtnFaultVariable;
    private javax.swing.JRadioButton rbtnNormalResponse;
    private javax.swing.JTable tblFaultParts;
    // End of variables declaration//GEN-END:variables
}
