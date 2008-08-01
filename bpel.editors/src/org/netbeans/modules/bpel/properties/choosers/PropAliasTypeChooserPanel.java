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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.soa.ui.axinodes.AxiomNode;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.ImportSchemaNode;
import org.netbeans.modules.bpel.nodes.MessagePartNode;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.editors.*;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ChildTypeFilter;
import org.netbeans.modules.bpel.nodes.MessageTypeNode;
import org.netbeans.modules.bpel.nodes.SchemaFileNode;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.PrimitiveTypeNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.nodes.SchemaComponentNode;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.properties.PropAliasSelectionContainer;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.visitor.AbstractXPathVisitor;
import org.openide.ErrorManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.properties.editors.controls.AbstractTreeChooserPanel;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.editors.nodes.factory.PropertyAliasTypeChooserNodeFactory;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.openide.util.NbBundle;

/**
 * @author  nk160297
 * @author changed by Vitaly Bychkov
 * @version 1.1
 */
public class PropAliasTypeChooserPanel
        extends AbstractTreeChooserPanel<PropAliasSelectionContainer>
        implements Reusable {
    
    static final long serialVersionUID = 1L;
    
    private WSDLModel myTargetWsdlModel;
    
    /**
     * The StereotypeFilter and the Lookup have to be specified later if this
     * constructor is used.
     */
    public PropAliasTypeChooserPanel() {
    }
    
    public PropAliasTypeChooserPanel(WSDLModel targetWsdlModel, Lookup lookup) {
        super(lookup);
        myTargetWsdlModel = targetWsdlModel;
        //
        createContent();
        initControls();
    }
    
    public void init(WSDLModel targetWsdlModel, Lookup lookup) {
        myTargetWsdlModel = targetWsdlModel;
        //
        NodesTreeParams treeParams = new NodesTreeParams();
        treeParams.setTargetNodeClasses(
                MessageTypeNode.class, MessagePartNode.class, AxiomNode.class);
        //
        Lookup newLookup = new ExtendedLookup(lookup, treeParams);
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
                if (chbSynchronous.isSelected()) {
                    synchronizeQueryWithTree();
                }
            }
        });
        //
        chbShowImportedOnly.setSelected(true);
        //
        chbSynchronous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chbSynchronous.isSelected()) {
                    synchronizeQueryWithTree();
                }
            }
        });
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
            }
        });
    }
    
    public boolean afterClose() {
        super.afterClose();
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
        result = factory.createNode(NodeType.PROCESS, process, lookup);
        return result;
    }
    
    public PropAliasSelectionContainer getSelectedValue() {
        Node selectedNode = getSelectedNode();
        return processTree(selectedNode);
    }
    
    private PropAliasSelectionContainer processTree(Node selectedNode) {
        if (selectedNode instanceof AxiomNode) {
            Node parentNode = selectedNode.getParentNode();
            while(parentNode != null) {
                if (parentNode instanceof AxiomNode) {
                    parentNode = parentNode.getParentNode();
                    continue;
                }
                //
                return processTree(parentNode);
            }
        } else if (selectedNode instanceof SchemaComponentNode) {
            SchemaComponent schemaComp =
                    ((SchemaComponentNode)selectedNode).getReference();
            String queryContent = fldQuery.getText();
            if (schemaComp instanceof GlobalElement) {
                return new PropAliasSelectionContainer(
                        new TypeContainer((GlobalElement)schemaComp),
                        queryContent);
            } else if (schemaComp instanceof GlobalType) {
                return new PropAliasSelectionContainer(
                        new TypeContainer((GlobalType)schemaComp),
                        queryContent);
            }
        } else if (selectedNode instanceof MessageTypeNode) {
            Message message = ((MessageTypeNode)selectedNode).getReference();
            String queryContent = fldQuery.getText();
            return new PropAliasSelectionContainer(new TypeContainer(message),
                    queryContent);
        } else if (selectedNode instanceof MessagePartNode) {
            Part part = ((MessagePartNode)selectedNode).getReference();
            String queryContent = fldQuery.getText();
            return new PropAliasSelectionContainer(part, queryContent);
        } else if (selectedNode instanceof PrimitiveTypeNode) {
            GlobalSimpleType gsType = ((PrimitiveTypeNode)selectedNode).getReference();
            String queryContent = fldQuery.getText();
            return new PropAliasSelectionContainer(new TypeContainer(gsType),
                    queryContent);
        }
        //
        return null;
    }
    
    public void setSelectedValue(final PropAliasSelectionContainer selection) {
        if (selection == null) {
            setSelectedNode(null);
            return;
        }
        //
        fldQuery.setText(selection.getQueryContent());
        //
        VariableStereotype stereotype = null;
        TypeContainer tc = selection.getTypeContainer();
        if (tc != null) {
            stereotype = tc.getStereotype();
            if (stereotype == null) {
                setSelectedNode(null);
                String messageText = NbBundle.getMessage(FormBundle.class,
                        "ERR_TYPE_RESOLVE_PROBLEM", // NOI18N
                        selection.getTypeContainer().getRefString(),
                        ""
                        );
                UserNotification.showMessageAsinc(messageText);
                return;
            }
        }
        //
        if (stereotype == null) {
            return;
        }
        //
        switch (stereotype) {
            case MESSAGE: {
                final Message message = selection.getTypeContainer().getMessage();
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
                        if (node instanceof MessageTypeNode ||
                                node instanceof SchemaFileNode ||
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
                //
                final Part part = selection.getMessagePart();
                if (part != null) {
                    soughtNode = NodeUtils.findFirstNode(part, soughtNode, 1);
                }
                //
                String queryContent = selection.getQueryContent();
                if (queryContent != null && queryContent.length() != 0) {
                    soughtNode = findNodeByQuery(queryContent, soughtNode);
                }
                //
                setSelectedNode(soughtNode);
                break;
            }
        }
    }
    
    /**
     * Uses the quiery string to find the corresponding node in the tree view.
     */
    private Node findNodeByQuery(final String queryContent, Node soughtNode) {
        //
        XPathModel xpImpl = AbstractXPathModelHelper.getInstance().newXPathModel();
        //
        try {
            XPathExpression xPathQuery = xpImpl.parseExpression(queryContent);
            //
            class FindPathVisitor extends AbstractXPathVisitor {
                private XPathLocationPath myLocationPath;
                
                public void visit(XPathLocationPath locationPath) {
                    myLocationPath = locationPath;
                }
                
                public XPathLocationPath getLocationPath() {
                    return myLocationPath;
                }
                
            };
            FindPathVisitor visition = new FindPathVisitor();
            xPathQuery.accept(visition);
            XPathLocationPath locationPath = visition.getLocationPath();
            for (LocationStep step : locationPath.getSteps()) {
                Node node = findAxiNodeByName(step.getString(), soughtNode, 1);
                if (node != null) {
                    soughtNode = node;
                    continue;
                }
            }
            //
        } catch (XPathException xpe) {
            ErrorManager.getDefault().notify(xpe);
        }
        return soughtNode;
    }
    
    public static Node findAxiNodeByName(
            final String soughtNodeName,
            Node sourceNode,
            int maxDepth) {
        //
        NodeUtils.SearchVisitor axiomVisitor = new NodeUtils.SearchVisitor() {
            public boolean accept(Node node) {
                if (node instanceof AxiomNode) {
                    String nodeName = ((AxiomNode)node).getName();
                    if (soughtNodeName.equals(nodeName)) {
                        return true;
                    }
                }
                //
                return false;
            }
            
            public boolean drillDeeper(Node node) {
                return true;
            }
        };
        //
        Node resultNode = NodeUtils.findFirstNode(
                sourceNode, axiomVisitor, maxDepth);
        return resultNode;
    }
    
    
    private void synchronizeQueryWithTree() {
        fldQuery.setText(calculateSimpleQueryByTreeSelection());
    }
    
    private String calculateSimpleQueryByTreeSelection() {
        assert myTargetWsdlModel != null;
        //
        String result = "";
        Node currNode = getSelectedNode();
        if (currNode != null && currNode instanceof AxiomNode) {
            AxiomNode axiomNode = (AxiomNode)currNode;
            AbstractDocumentComponent adc = (AbstractDocumentComponent)
            myTargetWsdlModel.getDefinitions();
            result = AxiomUtils.calculateSimpleXPath(axiomNode, adc);
        }
        return result;
        
//        ArrayList<String> path = new ArrayList();
//        while (currNode != null && currNode instanceof AxiomNode) {
//            AxiomNode axiomNode = (AxiomNode)currNode;
//            AXIComponent axiComponent = axiomNode.getReference();
//            String compName = null;
//            if (axiComponent != null) {
//                if (axiComponent instanceof AXIType) {
//                    compName = ((AXIType)axiComponent).getName();
//                } else if (axiComponent instanceof Attribute) {
//                    compName = ((Attribute)axiComponent).getName();
//                }
//            }
//            //
//            if (compName != null && compName.length() != 0) {
//                path.add(compName);
//            } else {
//                path.clear();
//            }
//            //
//            currNode = currNode.getParentNode();
//        }
//        //
//        StringBuffer result = new StringBuffer();
//        ListIterator<String> itr = path.listIterator(path.size());
//        while (itr.hasPrevious()) {
//            String pathItem = itr.previous();
//            result.append(XPATH_SEPARATOR).append(pathItem);
//        }
//        //
//        return result.toString();
    }
    
    protected Validator createValidator() {
        return new MyValidator();
    }
    
    private class MyValidator extends DefaultChooserValidator {
        
        protected String getIncorrectNodeSelectionReasonKey() {
            return "ERR_PROP_ALIAS_TYPE_NOT_SPECIFIED"; // NOI18N
        }
        
    }
    
    public Component getTreeComponent() {
        JTree tree = SoaUtil.lookForChildByClass(treeView, JTree.class);
        return tree;
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
        chbSynchronous = new javax.swing.JCheckBox();
        lblQuery = new javax.swing.JLabel();

        pnlLookupProvider.setFocusable(false);
        treeView.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        treeView.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_TypeChooser"));
        treeView.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_TypeChooser"));

        org.jdesktop.layout.GroupLayout pnlLookupProviderLayout = new org.jdesktop.layout.GroupLayout(pnlLookupProvider);
        pnlLookupProvider.setLayout(pnlLookupProviderLayout);
        pnlLookupProviderLayout.setHorizontalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
        );
        pnlLookupProviderLayout.setVerticalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
        );

        chbShowImportedOnly.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbShowImportedOnly.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chbShowImportedOnly.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_CHB_Show_Imported_Files_Only"));

        queryPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fldQuery.setColumns(20);
        fldQuery.setLineWrap(true);
        fldQuery.setRows(3);
        fldQuery.setWrapStyleWord(true);
        queryPanel.setViewportView(fldQuery);

        chbSynchronous.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "CHB_Synchronous"));
        chbSynchronous.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbSynchronous.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chbSynchronous.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_CHB_Synchronous"));
        chbSynchronous.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSD_CHB_Synchronous"));

        lblQuery.setLabelFor(fldQuery);
        lblQuery.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Query"));
        lblQuery.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Type"));
        lblQuery.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Type"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(queryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(lblQuery)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 70, Short.MAX_VALUE)
                .add(chbSynchronous))
            .add(layout.createSequentialGroup()
                .add(chbShowImportedOnly)
                .addContainerGap())
            .add(pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbShowImportedOnly)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chbSynchronous)
                    .add(lblQuery, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queryPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chbShowImportedOnly;
    private javax.swing.JCheckBox chbSynchronous;
    private javax.swing.JTextArea fldQuery;
    private javax.swing.JLabel lblQuery;
    private javax.swing.JPanel pnlLookupProvider;
    private javax.swing.JScrollPane queryPanel;
    private javax.swing.JScrollPane treeView;
    // End of variables declaration//GEN-END:variables
    
}
