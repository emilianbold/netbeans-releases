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
package org.netbeans.modules.bpel.properties.choosers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.soa.ui.axinodes.AxiomNode;
import org.netbeans.modules.soa.ui.axinodes.ElementNode;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ChildTypeFilter;
import org.netbeans.modules.bpel.nodes.MessageTypeNode;
import org.netbeans.modules.bpel.nodes.SchemaFileNode;
import org.netbeans.modules.bpel.nodes.WsdlFileNode;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.ImportNode;
import org.netbeans.modules.bpel.nodes.ImportSchemaNode;
import org.netbeans.modules.bpel.nodes.PrimitiveTypeNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.nodes.SchemaComponentNode;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.bpel.properties.editors.controls.AbstractTreeChooserPanel;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.editors.nodes.factory.TypeChooserNodeFactory;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.openide.util.NbBundle;

/**
 *
 * @author  nk160297
 */
public class TypeChooserPanel extends AbstractTreeChooserPanel<TypeContainer>
        implements Reusable {
    
    static final long serialVersionUID = 1L;
    
    private static final String DEFAULT_INCORRECT_NODE_SELECTION_REASON_KEY = "ERR_TYPE_NOT_SPECIFIED"; // NOI18N
    private String incorrectNodeSelectionReasonKey
            = DEFAULT_INCORRECT_NODE_SELECTION_REASON_KEY;
    
    /**
     * If attribute is null then the chooser shows all types.
     * If the stereotype is assigned then the chooser shows the only
     * types of corresponding stereotype.
     */
    private StereotypeFilter myStereotypeFilter;
    
    /**
     * The StereotypeFilter and the Lookup have to be specified later if this
     * constructor is used.
     */
    public TypeChooserPanel() {
    }
    
    public TypeChooserPanel(StereotypeFilter stereotypeFilter, Lookup lookup) {
        super(lookup);
        assert stereotypeFilter != null : "you should specify a stereotype filter";
        myStereotypeFilter = stereotypeFilter;
        //
        ////
//        NodesTreeParams treeParams = new NodesTreeParams();
//        Class<? extends Node>[] nodeClasses =
//                stereotypeFilter.getTargetNodeClasses();
//        treeParams.setTargetNodeClasses(nodeClasses);
//        treeParams.setLeafNodeClasses(nodeClasses);
//        //
//        Lookup newLookup = new ExtendedLookup(lookup, treeParams, stereotypeFilter);
//        setLookup(newLookup);
        ////
        
        createContent();
        initControls();
    }
    
    public void init(StereotypeFilter stereotypeFilter, Lookup lookup) {
        //
        assert stereotypeFilter != null : "you should specify a stereotype filter";
        myStereotypeFilter = stereotypeFilter;
        //
        NodesTreeParams treeParams = new NodesTreeParams();
        Class<? extends Node>[] nodeClasses =
                stereotypeFilter.getTargetNodeClasses();
        treeParams.setTargetNodeClasses(nodeClasses);
        treeParams.setLeafNodeClasses(nodeClasses);
        treeParams.setHighlightTargetNodes(true);
        //
        Lookup newLookup = new ExtendedLookup(lookup, treeParams, stereotypeFilter);
        setLookup(newLookup);
        //
        
        //
        initControls();
    }
    
    public void createContent() {
        initComponents();
        //
        super.createContent();
        //
        ((BeanTreeView) treeView).setRootVisible(true);
        ((BeanTreeView) treeView).setSelectionMode(TreeSelectionModel
                .SINGLE_TREE_SELECTION );
        ((BeanTreeView) treeView).setPopupAllowed(false);
        //
        getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Node selectedNode = getSelectedNode();
                //
                String text;
                if (selectedNode instanceof MessageTypeNode) {
                    Message message =
                            ((MessageTypeNode)selectedNode).getReference();
                    text = ResolverUtility.getDisplayName(message);
                    fldTypeName.setText(text);
                } else if (selectedNode instanceof SchemaComponentNode) {
                    SchemaComponent schemaComp =
                            ((SchemaComponentNode)selectedNode).getReference();
                    if (schemaComp != null) {
                        text = ResolverUtility.getDisplayName(schemaComp);
                        fldTypeName.setText(text);
                    }
                } else if (selectedNode instanceof AxiomNode) {
                    AXIComponent axiComp = ((AxiomNode)selectedNode).getReference();
                    if (axiComp != null) {
                        SchemaComponent schemaComp = axiComp.getPeer();
                        if (schemaComp != null) {
                            text = ResolverUtility.getDisplayName(schemaComp);
                            fldTypeName.setText(text);
                        }
                    }
                } else if (selectedNode instanceof WsdlFileNode) {
                    WSDLModel wsdlModel =
                            ((WsdlFileNode)selectedNode).getReference();
                    FileObject wsdlFo = wsdlModel.getModelSource().
                            getLookup().lookup(FileObject.class);
                    if (wsdlFo != null ) {
                        text = wsdlFo.getPath();
                    } else {
                        Definitions definitions = wsdlModel.getDefinitions();
                        text = definitions == null ? "" : definitions.getTargetNamespace(); //NOI18N
                    } 
                    fldTypeName.setText(text);
                } else if (selectedNode instanceof ImportNode) {
                    Import importObj = ((ImportNode)selectedNode).getReference();
                    fldTypeName.setText(ResolverUtility.getImportDescription(importObj));
                } else if (selectedNode instanceof SchemaFileNode) {
                    SchemaModel schemaModel =
                            ((SchemaFileNode)selectedNode).getReference();
                    FileObject schemaFo = schemaModel.getModelSource().
                            getLookup().lookup(FileObject.class);
                    if (schemaFo != null ) {
                        text = schemaFo.getPath();
                    } else {
                        Schema schema = schemaModel.getSchema();
                        text = schema == null ? "" : schema.getTargetNamespace(); //NOI18N
                    } 
                    fldTypeName.setText(text);
                } else if (selectedNode instanceof PrimitiveTypeNode) {
                    GlobalSimpleType gSimpleType =
                            ((PrimitiveTypeNode)selectedNode).getReference();
                    if (gSimpleType != null) {
                        text = ResolverUtility.getDisplayName(gSimpleType);
                        fldTypeName.setText(text);
                    }
                } else {
                    fldTypeName.setText("");
                }
            }
        });
        //
        chbShowImportedOnly.setSelected(true);
        //
        chbShowImportedOnly.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BpelModel model = (BpelModel)getLookup().lookup(BpelModel.class);
                Process process = model.getProcess();
                BpelNode soughtNode = NodeUtils.findFirstNode(
                        process, getExplorerManager().getRootContext());
                //
                Children childrent = soughtNode.getChildren();
                if (childrent instanceof ReloadableChildren) {
                    ((ReloadableChildren)childrent).reload();
                }
//                } else {
//                    // boolean importedOnly = chbShowImportedOnly.isSelected();
//                    NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
//                        public boolean accept(Node node) {
//                            if (node instanceof BpelNode) {
//                                NodeType type = ((BpelNode)node).getNodeType();
//                                if (type == NodeType.STEREOTYPE_GROUP) {
//                                    return true;
//                                }
//                            }
//                            return false;
//                        }
//
//                        public boolean drillDeeper(Node node) {
//                            return true;
//                        }
//                    };
//                    List<Node> nodesList = NodeUtils.findNodes(
//                            getExplorerManager().getRootContext(), visitor, 3);
//                    //
//                    for (Node node: nodesList) {
//                        Children childrent = node.getChildren();
//                        if (childrent instanceof ReloadableChildren) {
//                            ((ReloadableChildren)childrent).reload();
//                        }
//                    }
//                }
            }
        });
    }
    
    public boolean afterClose() {
        super.afterClose();
        myStereotypeFilter = null;
        return true;
    }
    
    protected Node constructRootNode() {
        Node result = null;
        //
        BpelModel model = (BpelModel)getLookup().lookup(BpelModel.class);
        Process process = model.getProcess();
        NodeFactory factory = new TypeChooserNodeFactory(
                PropertyNodeFactory.getInstance());
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
        Lookup lookup = new ExtendedLookup(getLookup(), showImportedOnlyFilter);
        //
        result = factory.createNode(
                NodeType.PROCESS, process, lookup);
        return result;
    }
    
    public void hideChbShowImportedOnly(boolean hide) {
        chbShowImportedOnly.setVisible(!hide);
    }
    
    public TypeContainer getSelectedValue() {
        Node selectedNode = getSelectedNode();
        if (selectedNode instanceof SchemaComponentNode) {
            SchemaComponent schemaComp =
                    ((SchemaComponentNode)selectedNode).getReference();
            if (schemaComp instanceof GlobalElement) {
                return new TypeContainer((GlobalElement)schemaComp);
            } else if (schemaComp instanceof GlobalType) {
                return new TypeContainer((GlobalType)schemaComp);
            }
        } else if (selectedNode instanceof MessageTypeNode) {
            Message message = ((MessageTypeNode)selectedNode).getReference();
            return new TypeContainer(message);
        } else if (selectedNode instanceof PrimitiveTypeNode) {
            GlobalSimpleType gsType = ((PrimitiveTypeNode)selectedNode).getReference();
            return new TypeContainer(gsType);
        }
        //
        return null;
    }
    
    public void setSelectedValue(final TypeContainer typeContainer) {
        if (typeContainer == null) {
            setSelectedNode(null);
            return;
        }
        //
        VariableStereotype stereotype = typeContainer.getStereotype();
        if (stereotype == null) {
            setSelectedNode(null);
            String messageText = NbBundle.getMessage(FormBundle.class,
                    "ERR_TYPE_RESOLVE_PROBLEM", // NOI18N
                    typeContainer.getRefString(),
                    ""
                    );
            UserNotification.showMessageAsinc(messageText);
            return;
        }
        //
        if (!myStereotypeFilter.isStereotypeAllowed(stereotype)) {
            return;
        }
        //
        switch (stereotype) {
            case MESSAGE: {
                final Message message = typeContainer.getMessage();
                //
                boolean isImported = ResolverUtility.isModelImported(
                        message.getModel(), getLookup());
                if (!isImported && chbShowImportedOnly.isSelected()) {
                    chbShowImportedOnly.setSelected(false);
                }
                //
                NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
                    public boolean accept(Node node) {
                        if (node instanceof MessageTypeNode) {
                            Message msg =
                                    ((MessageTypeNode)node).getReference();
                            if (msg != null && msg.equals(message)) {
                                return true;
                            }
                        }
                        //
                        return false;
                    }
                    
                    public boolean drillDeeper(Node node) {
                        if (node instanceof SchemaFileNode ||
                                node instanceof ImportSchemaNode) {
                            return false;
                        } else if (node instanceof ElementNode) {
                            Element element = ((ElementNode)node).getReference();
                            SchemaComponent sc = element.getPeer();
                            return sc instanceof GlobalElement || 
                                    sc instanceof GlobalType;
                        } else {
                            return true;
                        }
                    }
                };
                //
                Node soughtNode = NodeUtils.findFirstNode(
                        getExplorerManager().getRootContext(), visitor, -1);
                setSelectedNode(soughtNode);
                break;
            }
            case GLOBAL_ELEMENT: {
                final GlobalElement element = typeContainer.getGlobalElement();
                //
                boolean isImported = ResolverUtility.isModelImported(
                        element.getModel(), getLookup());
                if (!isImported && chbShowImportedOnly.isSelected()) {
                    chbShowImportedOnly.setSelected(false);
                }
                //
                NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
                    public boolean accept(Node node) {
                        if (node instanceof SchemaComponentNode) {
                            SchemaComponent sComp =
                                    ((SchemaComponentNode)node).getReference();
                            if (sComp != null && sComp.equals(element)) {
                                return true;
                            }
                        }
                        //
                        return false;
                    }
                    
                    public boolean drillDeeper(Node node) {
                        if (node instanceof SchemaComponentNode) {
                            return false;
                        } else if (node instanceof ElementNode) {
                            Element element = ((ElementNode)node).getReference();
                            SchemaComponent sc = element.getPeer();
                            return sc instanceof GlobalElement || 
                                    sc instanceof GlobalType;
                        } else {
                            return true;
                        }
                    }
                };
                Node soughtNode = NodeUtils.findFirstNode(
                        getExplorerManager().getRootContext(), visitor, -1);
                //
                setSelectedNode(soughtNode);
                break;
            }
            case PRIMITIVE_TYPE:
            case GLOBAL_SIMPLE_TYPE:
            case GLOBAL_COMPLEX_TYPE:
            case GLOBAL_TYPE:
                final GlobalType type = typeContainer.getGlobalType();
                //
                boolean isImported = ResolverUtility.isModelImported(
                        type.getModel(), getLookup());
                if (!isImported && chbShowImportedOnly.isSelected()) {
                    chbShowImportedOnly.setSelected(false);
                }
                //
                NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
                    public boolean accept(Node node) {
                        if (node instanceof SchemaComponentNode) {
                            SchemaComponent sComp =
                                    ((SchemaComponentNode)node).getReference();
                            if (sComp != null && sComp.equals(type)) {
                                return true;
                            }
                        } else if (node instanceof PrimitiveTypeNode) {
                            GlobalSimpleType gsType =
                                    ((PrimitiveTypeNode)node).getReference();
                            if (gsType != null && gsType.equals(type)) {
                                return true;
                            }
                        }
                        //
                        return false;
                    }
                    
                    public boolean drillDeeper(Node node) {
                        if (node instanceof SchemaComponentNode) {
                            return false;
                        } else if (node instanceof ElementNode) {
                            Element element = ((ElementNode)node).getReference();
                            SchemaComponent sc = element.getPeer();
                            return sc instanceof GlobalElement || 
                                    sc instanceof GlobalType;
                        } else {
                            return true;
                        }
                    }
                };
                Node soughtNode = NodeUtils.findFirstNode(
                        getExplorerManager().getRootContext(), visitor, -1);
                //
                setSelectedNode(soughtNode);
                break;
                //
                // assert false : "The Global Schema type isn't allowed here!";
        }
    }
    
    protected Validator createValidator() {
        return new MyValidator();
    }
    
    public void setIncorrectNodeSelectionReasonKey(String messageKey) {
        if (messageKey != null && ! "".equals(messageKey)) {
            incorrectNodeSelectionReasonKey = messageKey;
        }
    }
    
    private class MyValidator extends DefaultChooserValidator {
        
        protected String getIncorrectNodeSelectionReasonKey() {
            return incorrectNodeSelectionReasonKey == null ?
                DEFAULT_INCORRECT_NODE_SELECTION_REASON_KEY
                    : incorrectNodeSelectionReasonKey;
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        lblType = new javax.swing.JLabel();
        fldTypeName = new javax.swing.JTextField();
        chbShowImportedOnly = new javax.swing.JCheckBox();
        pnlLookupProvider = new TreeWrapperPanel();
        treeView = new BeanTreeView();
        
        lblType.setLabelFor(fldTypeName);
        lblType.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Type"));
        lblType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Type"));
        lblType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Type"));
        
        fldTypeName.setEditable(false);
        fldTypeName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_TypeIndicator"));
        fldTypeName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_TypeIndicator"));
        
        chbShowImportedOnly.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbShowImportedOnly.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chbShowImportedOnly.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_CHB_Show_Imported_Files_Only"));
        
        pnlLookupProvider.setFocusable(false);
        treeView.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        treeView.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_TypeChooser"));
        treeView.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_TypeChooser"));
        
        org.jdesktop.layout.GroupLayout pnlLookupProviderLayout = new org.jdesktop.layout.GroupLayout(pnlLookupProvider);
        pnlLookupProvider.setLayout(pnlLookupProviderLayout);
        pnlLookupProviderLayout.setHorizontalGroup(
                pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                );
        pnlLookupProviderLayout.setVerticalGroup(
                pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                );
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .add(lblType)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fldTypeName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE))
                .add(pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createSequentialGroup()
                .add(chbShowImportedOnly)
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbShowImportedOnly)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(lblType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(fldTypeName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                );
    }
    
// Variables declaration - do not modify
    private javax.swing.JCheckBox chbShowImportedOnly;
    private javax.swing.JTextField fldTypeName;
    private javax.swing.JLabel lblType;
    private javax.swing.JPanel pnlLookupProvider;
    private javax.swing.JScrollPane treeView;
// End of variables declaration
}
