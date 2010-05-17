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
package org.netbeans.modules.bpel.properties.props.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.List;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.soa.ui.form.ValidablePropertyCustomizer;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.CategoryFolderNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.choosers.FaultNameChooserPanel;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.soa.ui.form.CustomNodeChooser;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ChildTypeFilter;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.properties.PropertyVetoError;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/**
 * This panel is intended to be used as cutomizer as well as chooser.
 *
 * @author nk160297
 */
public class FaultNamePropertyCustomizer extends ValidablePropertyCustomizer
        implements CustomNodeChooser<QName>, PropertyChangeListener {
    
    private static final long serialVersionUID = 1L;
    private Timer inputDelayTimer;
    private QName myFaultName;
    
    protected PropertyEditor myPropertyEditor;
    private NodeAction okAction;
    
    // related to CustomNodeChooser
    private Lookup myLookup;
    
    public FaultNamePropertyCustomizer(Lookup lookup) {
        this();
        myLookup = lookup;
    }
    
    public FaultNamePropertyCustomizer() {
        createContent();
    }
    
    public void init(PropertyEnv propertyEnv, PropertyEditor propertyEditor) {
        if (myPropertyEnv != null) {
            myPropertyEnv.removePropertyChangeListener(this);
        }
        //
        super.init(propertyEnv, propertyEditor);
        myPropertyEditor = propertyEditor;
        //
        myPropertyEnv.addPropertyChangeListener(this);
        //
        // Synchronize curent state
        myPropertyEnv.setState(getValidStateManager(true).isValid() ?
            PropertyEnv.STATE_NEEDS_VALIDATION :
            PropertyEnv.STATE_INVALID);
        //
        //----------------------------------------------------------
        //
        setFaultName((QName)propertyEditor.getValue(), null);
        //
        initControls();
        //
        // Expand the "WSDL Faults" node
        Node root = getChooserPanel().getExplorerManager().getRootContext();
        Node wsdlFilesNode = NodeUtils.findFirstNode(
                NodeType.WSDL_FILES_FOLDER,
                CategoryFolderNode.class, root, 2);
        if (wsdlFilesNode != null) {
            getChooserPanel().getTreeView().expandNode(wsdlFilesNode);
        }
    }
    
    public void createContent() {
        initComponents();
        //
        ActionListener timerListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setFaultName(calculateFaultNameFromText(), e.getSource());
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
        fldNamespace.getDocument().addDocumentListener(docListener);
        fldLocalPart.getDocument().addDocumentListener(docListener);
        //
        FocusListener fl = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                inputDelayTimer.stop();
                setFaultName(calculateFaultNameFromText(), e.getSource());
                getValidator().revalidate(true);
            }
        };
        //
        fldNamespace.addFocusListener(fl);
        fldLocalPart.addFocusListener(fl);
        //
        getChooserPanel().getExplorerManager().
                addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.
                        equals(evt.getPropertyName())) {
                    QName newFaultName = getChooserPanel().getSelectedValue();
                    if (newFaultName != null) {
                        setFaultName(newFaultName, getChooserPanel());
                        getValidator().revalidate(true);
                    }
                }
            }
        });
        //
        getValidStateManager(true).ignoreValidator(
                getChooserPanel().getValidator(), true);
        //
        chbShowImportedOnly.setSelected(true);
        //
        chbShowImportedOnly.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Lookup lookup = getLookup();
                //
                BpelModel model = lookup.lookup(BpelModel.class);
                Process process = model.getProcess();
                //
                NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
                    public boolean accept(Node node) {
                        if (node instanceof BpelNode) {
                            NodeType type = ((BpelNode)node).getNodeType();
                            if (type == NodeType.WSDL_FILES_FOLDER) {
                                return true;
                            }
                        }
                        return false;
                    }
                    
                    public boolean drillDeeper(Node node) {
                        NodeType type = ((BpelNode)node).getNodeType();
                        if (type == NodeType.PROCESS) {
                            return true;
                        }
                        return false;
                    }
                };
                Node root = getChooserPanel().getExplorerManager().getRootContext();
                List<Node> nodesList = NodeUtils.findNodes(root, visitor, 2);
                //
                for (Node tempNode: nodesList) {
                    Children childrent = tempNode.getChildren();
                    if (childrent instanceof ReloadableChildren) {
                        ((ReloadableChildren)childrent).reload();
                    }
                }
            }
        });
        //
        getChooserPanel().createContent();
        //
        FaultNameChooserPanel chooserPanel = getChooserPanel();
        Util.attachDefaultDblClickAction(chooserPanel, chooserPanel);
        //
        SoaUtil.activateInlineMnemonics(this);
        //
        HelpCtx.setHelpIDString(this, this.getClass().getName());
    }
    
    protected synchronized FaultNameChooserPanel getChooserPanel() {
        if (pnlChooser == null) {
            pnlChooser = createChooserPanel();
        }
        return (FaultNameChooserPanel)pnlChooser;
    }
    
    protected FaultNameChooserPanel createChooserPanel() {
        return new FaultNameChooserPanel();
    }
    
    protected void applyNewValues() {
        myPropertyEditor.setValue(getFaultName());
    }
    
    private QName calculateFaultNameFromText() {
//System.out.println("calculateFaultNameFromText:");
        String namespace = fldNamespace.getText();
        String localPart = fldLocalPart.getText();
        //
        if ((namespace == null || namespace.length() == 0) &&
                (localPart == null || localPart.length() == 0)) {
            return null;
        } else {
            QName result = new QName(namespace, localPart);
            return result;
        }
    }
    
    private void setFaultName(QName newValue, Object source) {
        myFaultName = newValue;
        //
        if (newValue == null) {
            fldNamespace.setText("");
            fldLocalPart.setText("");
            //
            getChooserPanel().setSelectedValue(null);
        } else {
            //
            // Update text fields
            if (source != inputDelayTimer &&
                    source != fldNamespace &&
                    source != fldLocalPart) {
                fldNamespace.setText(newValue.getNamespaceURI());
                fldLocalPart.setText(newValue.getLocalPart());
            }
            //
            // Update tree view
            if (source != getChooserPanel()) {
                getChooserPanel().setSelectedValue(newValue);
            }
        }
        //
    }
    
    private QName getFaultName() {
        return myFaultName;
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if (PropertyEnv.PROP_STATE.equals(event.getPropertyName()) &&
                event.getNewValue() == PropertyEnv.STATE_VALID) {
            try {
                applyNewValues();
            } catch (PropertyVetoError ex) {
                myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                PropertyVetoError.defaultProcessing(ex);
            }
        }
    }
    
    public Validator createValidator() {
        return new DefaultValidator(
                (ValidStateManager.Provider)this, ErrorMessagesBundle.class) {
            public void doFastValidation() {
/* issue 85149 No way to clear the Fault Name field in Reply editor
                QName currFaultName = getFaultName();
 
                if (currFaultName == null) {
                    addReasonKey("ERR_FAULT_NOT_SPECIFIED"); // NOI18N
                    return false;
                } else {
                    String namespace = currFaultName.getNamespaceURI();
                    if (namespace == null || namespace.length() == 0) {
                        addReasonKey("ERR_NAMESPACE_NOT_SPECIFIED"); // NOI18N
                        return false;
                    }
                    //
                    String localpart = currFaultName.getLocalPart();
                    if (localpart == null || localpart.length() == 0) {
                        addReasonKey("ERR_LOCAL_PART_NOT_SPECIFIED"); // NOI18N
                        return false;
                    }
                }
 */
            }
        };
    }
    
    // -----------------------------------------------------------------------
    // Following methods are related to implementaton of the CustomNodeChooser
    // -----------------------------------------------------------------------
    
    public void setDescriptor(DefaultDialogDescriptor descriptor) {
    }
    
    public boolean unsubscribeListeners() {
        return true;
    }
    
    public boolean subscribeListeners() {
        return true;
    }
    
    public boolean initControls() {
        Lookup lookup = getLookup();
        //
        // Create a filter to prevent showing not imported WSDL or Schema files
        ChildTypeFilter showImportedOnlyFilter = new ChildTypeFilter() {
            public boolean isPairAllowed(
                    NodeType parentType, NodeType childType) {
                if (chbShowImportedOnly.isSelected()) {
                    if (childType.equals(NodeType.WSDL_FILE) ||
                            childType.equals(NodeType.SCHEMA_FILE)) {
                        return false;
                    } else {
                        return true;
                    }
                }
                return true;
            }
        };
        lookup = new ExtendedLookup(lookup, showImportedOnlyFilter);
        //
        getChooserPanel().setLookup(lookup);
        getChooserPanel().initControls();
        //
        getValidStateManager(true).addValidStateListener(new ValidStateListener() {
            public void stateChanged(ValidStateManager source, boolean isValid) {
                if (source.isValid()) {
                    lblErrorMessage.setText("");
                } else {
                    lblErrorMessage.setText(source.getHtmlReasons());
                }
            }
        });
        //
        getValidator().revalidate(true);
        //
        return true;
    }
    
    public QName getSelectedValue() {
        return getFaultName();
    }
    
    public void setSelectedValue(QName newValue) {
        setFaultName(newValue, null);
        //
        getValidator().revalidate(true);
    }
    
    public Lookup getLookup() {
        if (myLookup != null) {
            // This used by chooser
            return myLookup;
        } else if (myPropertyEnv != null){
            // This used by customizer
            Object[] beans = myPropertyEnv.getBeans();
            BpelNode node = (BpelNode)beans[0];
            Lookup lookup = node.getLookup();
            return lookup;
        }
        return null;
    }
    
    public boolean afterClose() {
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        fldNamespace = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        fldLocalPart = new javax.swing.JTextField();
        pnlChooser = getChooserPanel();
        chbShowImportedOnly = new javax.swing.JCheckBox();
        lblErrorMessage = new javax.swing.JLabel();

        jLabel1.setLabelFor(fldNamespace);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_URI"));
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_URI"));
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_URI"));

        jLabel2.setLabelFor(fldLocalPart);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_Local_Name"));
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Local_Name"));
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Local_Name"));

        pnlChooser.setFocusable(false);
        org.jdesktop.layout.GroupLayout pnlChooserLayout = new org.jdesktop.layout.GroupLayout(pnlChooser);
        pnlChooser.setLayout(pnlChooserLayout);
        pnlChooserLayout.setHorizontalGroup(
            pnlChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 361, Short.MAX_VALUE)
        );
        pnlChooserLayout.setVerticalGroup(
            pnlChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 225, Short.MAX_VALUE)
        );

        chbShowImportedOnly.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbShowImportedOnly.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chbShowImportedOnly.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_CHB_Show_Imported_Files_Only"));

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_ErrorLabel"));
        lblErrorMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_ErrorLabel"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pnlChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fldNamespace, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                            .add(fldLocalPart, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, chbShowImportedOnly))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(fldNamespace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(fldLocalPart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbShowImportedOnly)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chbShowImportedOnly;
    private javax.swing.JTextField fldLocalPart;
    private javax.swing.JTextField fldNamespace;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JPanel pnlChooser;
    // End of variables declaration//GEN-END:variables
    
}
