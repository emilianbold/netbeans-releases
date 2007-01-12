/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.choosers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.bpel.design.nodes.NodeFactory;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.MessagePartNode;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.ExtendedLookup;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.bpel.properties.editors.*;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ChildTypeFilter;
import org.netbeans.modules.bpel.nodes.MessageTypeNode;
import org.netbeans.modules.bpel.nodes.SchemaFileNode;
import org.netbeans.modules.bpel.nodes.WsdlFileNode;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.ImportNode;
import org.netbeans.modules.bpel.nodes.ImportSchemaNode;
import org.netbeans.modules.bpel.nodes.PrimitiveTypeNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
//import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
//import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalElementNode;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.properties.Constants.VariableStereotype;
import org.netbeans.modules.bpel.properties.UserNotification;
import org.netbeans.modules.bpel.properties.editors.controls.AbstractTreeChooserPanel;
import org.netbeans.modules.bpel.properties.editors.controls.NodesTreeParams;
import org.netbeans.modules.bpel.properties.editors.controls.Reusable;
import org.netbeans.modules.bpel.properties.editors.controls.valid.Validator;
import org.netbeans.modules.bpel.properties.editors.nodes.factory.PropertyAliasTypeChooserNodeFactory;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
//import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalComplexTypeNode;
//import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalSimpleTypeNode;
import org.openide.util.NbBundle;

/**
 * @author  nk160297
 * @author changed by Vitaly Bychkov
 * @version 1.1
 */
public class PropAliasTypeChooserPanel extends AbstractTreeChooserPanel
        implements Reusable {
    
    static final long serialVersionUID = 1L;
    
    private static final String DEFAULT_INCORRECT_NODE_SELECTION_REASON_KEY = "ERR_PROP_ALIAS_TYPE_NOT_SPECIFIED"; // NOI18N
    private String incorrectNodeSelectionReasonKey 
            = DEFAULT_INCORRECT_NODE_SELECTION_REASON_KEY;
    /**
     * If attribute is null then the chooser shows all types.
     * If the stereotype is assigned then the chooser shows the only
     * types of corresponding stereotype.
     */
    private StereotypeFilter myStereotypeFilter = Constants.CORRELATION_PROPERTY_ALIAS_STEREO_TYPE_FILTER;
    
    /**
     * The StereotypeFilter and the Lookup have to be specified later if this
     * constructor is used.
     */
    public PropAliasTypeChooserPanel() {
    }
    
    public PropAliasTypeChooserPanel(/*StereotypeFilter stereotypeFilter, */Lookup lookup) {
        super(lookup);
////        assert stereotypeFilter != null : "you should specify a stereotype filter";
////        myStereotypeFilter = stereotypeFilter;
        //
        createContent();
        initControls();
    }
    
    public void init(/*StereotypeFilter stereotypeFilter, */Lookup lookup) {
        //
//        assert stereotypeFilter != null : "you should specify a stereotype filter";
//        myStereotypeFilter = stereotypeFilter;
        //
        NodesTreeParams treeParams = new NodesTreeParams();
        Class<? extends Node>[] nodeClasses =
                myStereotypeFilter.getTargetNodeClasses();
        treeParams.setTargetNodeClasses(nodeClasses);
        treeParams.setLeafNodeClasses(nodeClasses);
        //
        Lookup newLookup = new ExtendedLookup(lookup, treeParams, myStereotypeFilter);
        setLookup(newLookup);
        //
        initControls();
    }
    
    public void createContent() {
        initComponents();
        //
        super.createContent();
        //
        ((BeanTreeView)treeView).setRootVisible(true);
        ((BeanTreeView)treeView).setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION );
        ((BeanTreeView)treeView).setPopupAllowed(false);
        //
        getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Node selectedNode = getSelectedNode();
                //
// TODO m                    setQueryContent(selectedNode);
//                setQueryContent(fldQuery.getText());
                //
                String text;

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
        NodeFactory factory = new PropertyAliasTypeChooserNodeFactory(
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
                NodeType.PROCESS, process, null, lookup);
        return result;
    }
    
    public TypeContainer getSelectedType() {
        Node selectedNode = getSelectedNode();
        /*
        if (selectedNode instanceof SchemaComponentNode) {
            SchemaComponentReference<? extends SchemaComponent> schemaCompRef =
                    ((SchemaComponentNode)selectedNode).getReference();
            if (schemaCompRef != null) {
                SchemaComponent schemaComp = schemaCompRef.get();
                if (schemaComp instanceof GlobalElement) {
                    return new TypeContainer((GlobalElement)schemaComp);
                } else if (schemaComp instanceof GlobalType) {
                    return new TypeContainer((GlobalType)schemaComp);
                }
            }
        } else */if (selectedNode instanceof MessageTypeNode) {
            Message message = ((MessageTypeNode)selectedNode).getReference();
            return new TypeContainer(message);
        } else if (selectedNode instanceof MessagePartNode) {
            Part part = ((MessagePartNode)selectedNode).getReference();
            return new TypeContainer(part);
        } else if (selectedNode instanceof PrimitiveTypeNode) {
            GlobalSimpleType gsType = ((PrimitiveTypeNode)selectedNode).getReference();
            return new TypeContainer(gsType);
        }
        //
        return null;
    }
    
    public void setSelectedType(final TypeContainer typeContainer) {
        if (typeContainer == null) {
            setSelectedValue(null);
            return;
        }
        //
        VariableStereotype stereotype = typeContainer.getStereotype();
        if (stereotype == null) {
            setSelectedValue(null);
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
                        } else {
                            return true;
                        }
                    }
                };
                //
                Node soughtNode = NodeUtils.findFirstNode(
                        getExplorerManager().getRootContext(), visitor, -1);
                setSelectedValue(soughtNode);
                break;
            }
            case MESSAGE_PART: {
                System.out.println("selected type is message part");
                final Part part = typeContainer.getMessagePart();
                //
                boolean isImported = ResolverUtility.isModelImported(
                        part.getModel(), getLookup());
                if (!isImported && chbShowImportedOnly.isSelected()) {
                    chbShowImportedOnly.setSelected(false);
                }
                //
                NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
                    public boolean accept(Node node) {
                        if (node instanceof MessagePartNode) {
                            Part prt =
                                    ((MessagePartNode)node).getReference();
                            if (prt != null && prt.equals(part)) {
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
                        } else {
                            return true;
                        }
                    }
                };
                //
                Node soughtNode = NodeUtils.findFirstNode(
                        getExplorerManager().getRootContext(), visitor, -1);
                setSelectedValue(soughtNode);
                break;            }
//////            case GLOBAL_ELEMENT: {
//////                final GlobalElement element = typeContainer.getGlobalElement();
//////                //
//////                boolean isImported = ResolverUtility.isModelImported(
//////                        element.getModel(), getLookup());
//////                if (!isImported && chbShowImportedOnly.isSelected()) {
//////                    chbShowImportedOnly.setSelected(false);
//////                }
//////                //
//////                NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
//////                    public boolean accept(Node node) {
//////                        if (node instanceof SchemaComponentNode) {
//////                            SchemaComponentReference scRef =
//////                                    ((SchemaComponentNode)node).getReference();
//////                            if (scRef != null) {
//////                                SchemaComponent sComp = scRef.get();
//////                                if (sComp != null && sComp.equals(element)) {
//////                                    return true;
//////                                }
//////                            }
//////                        }
//////                        //
//////                        return false;
//////                    }
//////                    
//////                    public boolean drillDeeper(Node node) {
//////                        if (node instanceof GlobalElementNode ||
//////                                node instanceof GlobalSimpleTypeNode ||
//////                                node instanceof GlobalComplexTypeNode) {
//////                            return false;
//////                        } else {
//////                            return true;
//////                        }
//////                    }
//////                };
//////                Node soughtNode = NodeUtils.findFirstNode(
//////                        getExplorerManager().getRootContext(), visitor, -1);
//////                //
//////                setSelectedValue(soughtNode);
//////                break;
//////            }
//////            case PRIMITIVE_TYPE:
//////            case GLOBAL_SIMPLE_TYPE:
//////            case GLOBAL_COMPLEX_TYPE:
//////            case GLOBAL_TYPE:
//////                final GlobalType type = typeContainer.getGlobalType();
//////                //
//////                boolean isImported = ResolverUtility.isModelImported(
//////                        type.getModel(), getLookup());
//////                if (!isImported && chbShowImportedOnly.isSelected()) {
//////                    chbShowImportedOnly.setSelected(false);
//////                }
//////                //
//////                NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
//////                    public boolean accept(Node node) {
//////                        if (node instanceof SchemaComponentNode) {
//////                            SchemaComponentReference scRef =
//////                                    ((SchemaComponentNode)node).getReference();
//////                            if (scRef != null) {
//////                                SchemaComponent sComp = scRef.get();
//////                                if (sComp != null && sComp.equals(type)) {
//////                                    return true;
//////                                }
//////                            }
//////                        } else if (node instanceof PrimitiveTypeNode) {
//////                            GlobalSimpleType gsType =
//////                                    ((PrimitiveTypeNode)node).getReference();
//////                            if (gsType != null && gsType.equals(type)) {
//////                                return true;
//////                            }
//////                        }
//////                        //
//////                        return false;
//////                    }
//////                    
//////                    public boolean drillDeeper(Node node) {
//////                        if (node instanceof GlobalElementNode ||
//////                                node instanceof GlobalSimpleTypeNode ||
//////                                node instanceof GlobalComplexTypeNode) {
//////                            return false;
//////                        } else {
//////                            return true;
//////                        }
//////                    }
//////                };
//////                Node soughtNode = NodeUtils.findFirstNode(
//////                        getExplorerManager().getRootContext(), visitor, -1);
//////                //
//////                setSelectedValue(soughtNode);
//////                break;
//////                //
//////                // assert false : "The Global Schema type isn't allowed here!";
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
    
    private void setQueryContent(Node selectedNode) {
        
        if (selectedNode instanceof MessageTypeNode) {
            Message message =
                    ((MessageTypeNode)selectedNode).getReference();
            queryContent = ResolverUtility.getDisplayName(message);
            
        } /*else if (selectedNode instanceof GlobalElementNode ||
                selectedNode instanceof GlobalSimpleTypeNode ||
                selectedNode instanceof GlobalComplexTypeNode) 
        {
            SchemaComponentReference<SchemaComponent> schemaCompRef =
                    ((SchemaComponentNode)selectedNode).getReference();
            if (schemaCompRef != null) {
                SchemaComponent schemaComp = schemaCompRef.get();
                if (schemaComp != null) {
                    queryContent = ResolverUtility.getDisplayName(schemaComp);
                }
            }
        } */
        else if (selectedNode instanceof WsdlFileNode) {
            
            WSDLModel wsdlModel =
                    ((WsdlFileNode)selectedNode).getReference();
            FileObject wsdlFo = (FileObject)wsdlModel.getModelSource().
                    getLookup().lookup(FileObject.class);
            queryContent = "";
        } else if (selectedNode instanceof ImportNode) {
            
            Import importObj = ((ImportNode)selectedNode).getReference();
            FileObject fo = ResolverUtility.
                    getImportedFile(importObj.getLocation(), getLookup());
            if (fo != null && fo.isValid()) {
                queryContent = "";
            } else {
                String importInfo = importObj.getLocation();
                
                
                if (importInfo == null || importInfo.length() == 0) {
                    importInfo = importObj.getNamespace();
                }
                
                importInfo = ResolverUtility.decodeLocation(importInfo);
                
                queryContent = NbBundle.getMessage(FormBundle.class,
                        "ERR_IMPORT_FILE_DOESNT_EXIST", // NOI18N
                        importInfo, "");
            }
        } else if (selectedNode instanceof SchemaFileNode) {
            
            SchemaModel schemaModel =
                    ((SchemaFileNode)selectedNode).getReference();
            FileObject wsdlFo = (FileObject)schemaModel.getModelSource().
                    getLookup().lookup(FileObject.class);
            queryContent = "";
            
        } else if (selectedNode instanceof PrimitiveTypeNode) {
            GlobalSimpleType gSimpleType =
                    ((PrimitiveTypeNode)selectedNode).getReference();
            if (gSimpleType != null) {
                queryContent = "";
            }
        } else {
            queryContent = "";
        }
        fldQuery.setText(queryContent == null ? "" : queryContent);
        
    }

    private void setQueryContent(String string) {
        queryContent = string == null || string.length() < 1 ? null : string;
    }
    
    public void setQueryFld(String string) {
        fldQuery.setText(string == null ? "" : string);
        queryContent = string;
    }
    
    public String getQueryContent() {
        return fldQuery.getText();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlLookupProvider = new TreeWrapperPanel();
        treeView = new BeanTreeView();
        chbShowImportedOnly = new javax.swing.JCheckBox();
        queryPanel = new javax.swing.JScrollPane();
        fldQuery = new javax.swing.JTextArea();
        lblQuery = new javax.swing.JLabel();

        pnlLookupProvider.setFocusable(false);
        treeView.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        treeView.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_TypeChooser"));
        treeView.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_TypeChooser"));

        chbShowImportedOnly.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbShowImportedOnly.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chbShowImportedOnly.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_CHB_Show_Imported_Files_Only"));

        fldQuery.setColumns(20);
        fldQuery.setRows(5);
        queryPanel.setViewportView(fldQuery);

        lblQuery.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Query"));
        lblQuery.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Type"));
        lblQuery.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Type"));

        org.jdesktop.layout.GroupLayout pnlLookupProviderLayout = new org.jdesktop.layout.GroupLayout(pnlLookupProvider);
        pnlLookupProvider.setLayout(pnlLookupProviderLayout);
        pnlLookupProviderLayout.setHorizontalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlLookupProviderLayout.createSequentialGroup()
                .add(chbShowImportedOnly)
                .add(302, 302, 302))
            .add(pnlLookupProviderLayout.createSequentialGroup()
                .addContainerGap()
                .add(lblQuery)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE))
            .add(treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
        );
        pnlLookupProviderLayout.setVerticalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlLookupProviderLayout.createSequentialGroup()
                .add(treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbShowImportedOnly)
                .add(pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnlLookupProviderLayout.createSequentialGroup()
                        .add(30, 30, 30)
                        .add(lblQuery, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(pnlLookupProviderLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(queryPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chbShowImportedOnly;
    private javax.swing.JTextArea fldQuery;
    private javax.swing.JLabel lblQuery;
    private javax.swing.JPanel pnlLookupProvider;
    private javax.swing.JScrollPane queryPanel;
    private javax.swing.JScrollPane treeView;
    // End of variables declaration//GEN-END:variables
    private String queryContent;

}
