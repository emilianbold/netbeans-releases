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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.MissingResourceException;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.nodes.VariableNode;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.VirtualVariableContainer;
import org.netbeans.modules.bpel.properties.choosers.NewMessageVarChooser;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.bpel.properties.choosers.VariableChooserPanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.bpel.properties.editors.controls.MessageConfigurationController;
import org.netbeans.modules.bpel.properties.editors.controls.MessageExchangeController;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.bpel.properties.editors.controls.TreeNodeChooser;
import org.netbeans.modules.bpel.properties.editors.controls.filter.PreferredFaultFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeFilter;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
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
    
    @Override
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
    }
    
    /**
     * Binds simple controls to names of properties.
     * This is necessary for automatic value inicialization and value inquiry.
     */
    private void bindControls2PropertyNames() {
        fldName.putClientProperty(
                CustomNodeEditor.PROPERTY_BINDER, PropertyType.NAME);
    }
    
    @Override
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
        if (myFaultName != null) {
            rbtnFaultResponse.setSelected(true);
        } else {
            rbtnNormalResponse.setSelected(true);
        }
        updateEnabledState();
        //
        mcc.initControls();
        mec.initControls();
        //
        getValidator().revalidate(true);
        //
        return true;
    }
    
    @Override
    public boolean subscribeListeners() {
        mcc.subscribeListeners();
        mec.subscribeListeners();
        return true;
    }
    
    @Override
    public boolean unsubscribeListeners() {
        mcc.unsubscribeListeners();
        mec.unsubscribeListeners();
        return true;
    }

    @Override
    public boolean applyNewValues() throws Exception {
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
            if (!useNormalOutput && currFaultVar != null) {
                VariableDeclaration varDecl = currFaultVar.createNewVariable();
                BpelReference<VariableDeclaration> varRef =
                        reply.createReference(varDecl, VariableDeclaration.class);
                reply.setVariable(varRef);
            }
            // no need to clear the variable property here if the fault variable
            // isn't specified because it has to be cleared before
            // by the MessageConfigurationController
            //
        }
        return true;
    }
    
    @Override
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
    
    private void updateEnabledState() {
        int index = cbxOperation.getSelectedIndex();
        if (index == -1) {
            lblFaultName.setEnabled(false);
            fldFaultName.setEnabled(false);
            btnChooseFaultName.setEnabled(false);
            lblFaultVariable.setEnabled(false);
            fldFaultVariable.setEnabled(false);
            btnNewFaultVariable.setEnabled(false);
            btnChooseFaultVariable.setEnabled(false);
        } else {
            if (rbtnNormalResponse.isSelected()) {
                useNormalOutput = true;
                //
                lblFaultName.setEnabled(false);
                fldFaultName.setEnabled(false);
                btnChooseFaultName.setEnabled(false);
                lblFaultVariable.setEnabled(false);
                fldFaultVariable.setEnabled(false);
                btnNewFaultVariable.setEnabled(false);
                btnChooseFaultVariable.setEnabled(false);
                //
                lblOutputVariable.setEnabled(true);
                mcc.setOutputVarEnabled(true);
            } else {
                useNormalOutput = false;
                //
                lblFaultName.setEnabled(true);
                fldFaultName.setEnabled(true);
                btnChooseFaultName.setEnabled(true);
                //
                if (myFaultName == null) {
                    lblFaultVariable.setEnabled(false);
                    fldFaultVariable.setEnabled(false);
                    btnNewFaultVariable.setEnabled(false);
                    btnChooseFaultVariable.setEnabled(false);
                } else {
                    lblFaultVariable.setEnabled(true);
                    fldFaultVariable.setEnabled(true);
                    btnChooseFaultVariable.setEnabled(true);
                    //
                    btnNewFaultVariable.setEnabled(getRequiredFaultTypeRef() != null);
                }
                //
                lblOutputVariable.setEnabled(false);
                mcc.setOutputVarEnabled(false);
            }
        }
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
                
                public void doFastValidation() {
                    if (rbtnNormalResponse.isSelected()){
                        Object item = cbxOperation.getSelectedItem();
                        if (item instanceof Operation &&
                                ((Operation) item).getOutput() == null) {
                            addReasonKey(Severity.ERROR, 
                                    "ERR_OPERATION_NO_OUTPUT",
                                    ((Operation) item).getName()); // NOI18N
                        }
                    }
                }
                
                @Override
                public void doDetailedValidation() {
                    if (rbtnFaultResponse.isSelected()) {
                        if (myFaultName == null) {
                            addReasonKey(Severity.ERROR, "ERR_FAULT_NAME_EMPTY"); //NOI18N
                        }
                        //
                        isCorrectFaultType(currFaultVar);
                    }
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
                        if (varMsg == null) {
                            addReasonKey(Severity.ERROR, "ERR_FAULT_VAR_EMPTY"); //NOI18N
                            return false;
                        } else if (!requiredMessageRef.get().equals(varMsg)) {
                            String required = ResolverUtility.
                                    qName2DisplayText(requiredMessageRef.getQName());
                            String current = ResolverUtility.
                                    qName2DisplayText(vcc.getType().getTypeQName());
                            addReasonKey(Severity.ERROR, 
                                    "ERR_FAULT_VAR_WRONG_TYPE", 
                                    required, current); //NOI18N
                            return false;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        fldName = new javax.swing.JTextField();
        lblMessageExchange = new javax.swing.JLabel();
        fldMessageExchange = mec.getFldMessageExchange();
        btnChooseMessEx = mec.getBtnChooseMsgEx();
        lblFaultName = new javax.swing.JLabel();
        fldFaultName = new javax.swing.JTextField();
        btnChooseFaultName = new javax.swing.JButton();
        lblPartnerLink = new javax.swing.JLabel();
        cbxPartnerLink = mcc.getCbxPartnerLink();
        lblOperation = new javax.swing.JLabel();
        cbxOperation = mcc.getCbxOperation();
        lblOutputVariable = new javax.swing.JLabel();
        fldOutputVariable = mcc.getFldOutputVariable();
        btnNewOutputVariable = mcc.getBtnNewOutputVariable();
        btnChooseOutputVariable = mcc.getBtnChooseOutputVariable();
        rbtnNormalResponse = new javax.swing.JRadioButton();
        rbtnFaultResponse = new javax.swing.JRadioButton();
        lblFaultVariable = new javax.swing.JLabel();
        fldFaultVariable = new javax.swing.JTextField();
        btnNewFaultVariable = new javax.swing.JButton();
        btnChooseFaultVariable = new javax.swing.JButton();
        lblErrorMessage = new javax.swing.JLabel();

        lblName.setLabelFor(fldName);
        lblName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Name")); // NOI18N

        fldName.setColumns(40);
        fldName.setName(""); // NOI18N

        lblMessageExchange.setLabelFor(fldMessageExchange);
        lblMessageExchange.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_MessageExchange")); // NOI18N

        btnChooseMessEx.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"BTN_ChooseMessageExchange")); // NOI18N
        btnChooseMessEx.setMargin(new java.awt.Insets(0, 4, 0, 4));

        lblFaultName.setLabelFor(fldFaultName);
        lblFaultName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_FaultName")); // NOI18N

        fldFaultName.setEditable(false);

        btnChooseFaultName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"BNT_ChooseFaultName")); // NOI18N
        btnChooseFaultName.setMargin(new java.awt.Insets(0, 4, 0, 4));

        lblPartnerLink.setLabelFor(cbxPartnerLink);
        lblPartnerLink.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_PartnerLink")); // NOI18N

        lblOperation.setLabelFor(cbxOperation);
        lblOperation.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Operation")); // NOI18N

        lblOutputVariable.setLabelFor(fldOutputVariable);
        lblOutputVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_OutputVariable")); // NOI18N

        fldOutputVariable.setColumns(30);
        fldOutputVariable.setEditable(false);

        btnNewOutputVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_CreateOutputVariable")); // NOI18N
        btnNewOutputVariable.setMargin(new java.awt.Insets(0, 4, 0, 4));

        btnChooseOutputVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_BrowseOutputVarible")); // NOI18N
        btnChooseOutputVariable.setMargin(new java.awt.Insets(0, 2, 0, 2));

        buttonGroup1.add(rbtnNormalResponse);
        rbtnNormalResponse.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"RBTN_NormalResponse")); // NOI18N
        rbtnNormalResponse.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtnNormalResponse.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup1.add(rbtnFaultResponse);
        rbtnFaultResponse.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"RBTN_FaultResponse")); // NOI18N
        rbtnFaultResponse.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtnFaultResponse.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lblFaultVariable.setLabelFor(fldFaultVariable);
        lblFaultVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_FaultVariable")); // NOI18N

        fldFaultVariable.setColumns(30);
        fldFaultVariable.setEditable(false);

        btnNewFaultVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_CreateFaultVariable")); // NOI18N
        btnNewFaultVariable.setMargin(new java.awt.Insets(0, 4, 0, 4));

        btnChooseFaultVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_BrowseFaultVarible")); // NOI18N
        btnChooseFaultVariable.setMargin(new java.awt.Insets(0, 2, 0, 2));

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.setAlignmentX(0.5F);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblFaultName)
                            .add(lblFaultVariable))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(fldFaultVariable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnNewFaultVariable)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnChooseFaultVariable))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(fldFaultName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnChooseFaultName)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblPartnerLink)
                            .add(lblOperation)
                            .add(lblName))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbxOperation, 0, 396, Short.MAX_VALUE)
                            .add(cbxPartnerLink, 0, 396, Short.MAX_VALUE)
                            .add(fldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(lblOutputVariable)
                        .add(7, 7, 7)
                        .add(fldOutputVariable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnNewOutputVariable)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnChooseOutputVariable))
                    .add(rbtnNormalResponse)
                    .add(rbtnFaultResponse)
                    .add(layout.createSequentialGroup()
                        .add(lblMessageExchange)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fldMessageExchange, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnChooseMessEx)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
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
                .add(11, 11, 11)
                .add(rbtnNormalResponse)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblOutputVariable)
                    .add(btnNewOutputVariable)
                    .add(btnChooseOutputVariable)
                    .add(fldOutputVariable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(rbtnFaultResponse)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblFaultName)
                    .add(fldFaultName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnChooseFaultName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblFaultVariable)
                    .add(btnNewFaultVariable)
                    .add(btnChooseFaultVariable)
                    .add(fldFaultVariable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fldMessageExchange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMessageExchange)
                    .add(btnChooseMessEx))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                .addContainerGap())
        );

        lblName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Name")); // NOI18N
        lblName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Name")); // NOI18N
        fldName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSN_TXTFLD_ReplyName")); // NOI18N
        fldName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSD_TXTFLD_ReplyName")); // NOI18N
        lblMessageExchange.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_MessageExchange")); // NOI18N
        lblMessageExchange.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_MessageExchange")); // NOI18N
        fldMessageExchange.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSN_TXTFLD_MessageExchange")); // NOI18N
        fldMessageExchange.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSD_TXTFLD_MessageExchange")); // NOI18N
        btnChooseMessEx.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_ChooseMessageExchange")); // NOI18N
        btnChooseMessEx.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_ChooseMessageExchange")); // NOI18N
        lblFaultName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_FaultName")); // NOI18N
        lblFaultName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_FaultName")); // NOI18N
        fldFaultName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSN_TXTFLD_FaultName")); // NOI18N
        fldFaultName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSD_TXTFLD_FaultName")); // NOI18N
        btnChooseFaultName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BNT_ChooseFaultName")); // NOI18N
        btnChooseFaultName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BNT_ChooseFaultName")); // NOI18N
        lblPartnerLink.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_PartnerLink")); // NOI18N
        lblPartnerLink.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_PartnerLink")); // NOI18N
        cbxPartnerLink.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSN_CBX_PartnerLink")); // NOI18N
        cbxPartnerLink.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSD_CBX_PartnerLink")); // NOI18N
        lblOperation.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Operation")); // NOI18N
        lblOperation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Operation")); // NOI18N
        cbxOperation.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSN_CBX_Operation")); // NOI18N
        cbxOperation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSD_CBX_Operation")); // NOI18N
        lblOutputVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_OutputVariable")); // NOI18N
        lblOutputVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_OutputVariable")); // NOI18N
        fldOutputVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSN_TXTFLD_OutputVariable")); // NOI18N
        fldOutputVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSD_TXTFLD_OutputVariable")); // NOI18N
        btnNewOutputVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_CreateOutputVariable")); // NOI18N
        btnNewOutputVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_CreateOutputVariable")); // NOI18N
        btnChooseOutputVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_BrowseOutputVarible")); // NOI18N
        btnChooseOutputVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_BrowseOutputVarible")); // NOI18N
        rbtnNormalResponse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_RBTN_NormalResponse")); // NOI18N
        rbtnNormalResponse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_RBTN_NormalResponse")); // NOI18N
        rbtnFaultResponse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_RBTN_FaultResponse")); // NOI18N
        rbtnFaultResponse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_RBTN_FaultResponse")); // NOI18N
        lblFaultVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_FaultVariable")); // NOI18N
        lblFaultVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_FaultVariable")); // NOI18N
        fldFaultVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSN_TXTFLD_FaultVariable")); // NOI18N
        fldFaultVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSD_TXTFLD_FaultVariable")); // NOI18N
        btnNewFaultVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_CreateFaultVariable")); // NOI18N
        btnNewFaultVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_CreateFaultVariable")); // NOI18N
        btnChooseFaultVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_BrowseFaultVarible")); // NOI18N
        btnChooseFaultVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_BrowseFaultVarible")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSN_LBL_Main_Tab")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReplyMainPanel.class, "ACSD_LBL_Main_Tab")); // NOI18N
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
    private javax.swing.JTextField fldFaultName;
    private javax.swing.JTextField fldFaultVariable;
    private javax.swing.JTextField fldMessageExchange;
    private javax.swing.JTextField fldName;
    private javax.swing.JTextField fldOutputVariable;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblFaultName;
    private javax.swing.JLabel lblFaultVariable;
    private javax.swing.JLabel lblMessageExchange;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblOperation;
    private javax.swing.JLabel lblOutputVariable;
    private javax.swing.JLabel lblPartnerLink;
    private javax.swing.JRadioButton rbtnFaultResponse;
    private javax.swing.JRadioButton rbtnNormalResponse;
    // End of variables declaration//GEN-END:variables
}
