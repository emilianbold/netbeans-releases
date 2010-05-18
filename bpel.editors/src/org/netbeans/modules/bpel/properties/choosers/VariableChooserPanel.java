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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListenerAdapter;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.spi.FindHelper;
import org.netbeans.modules.bpel.nodes.BaseScopeNode;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.ContainerBpelNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.nodes.SchemaComponentNode;
import org.netbeans.modules.bpel.nodes.VariableNode;
import org.netbeans.modules.bpel.nodes.VariableNode.DefaultTypeInfoProvider;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.bpel.properties.NodeUtils.SearchVisitor;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.properties.editors.StandardButtonBar;
import org.netbeans.modules.bpel.properties.editors.VariableMainPanel;
import org.netbeans.modules.bpel.properties.editors.controls.AbstractTreeChooserPanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeInfoProvider;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.bpel.editors.api.ui.valid.NodeEditorDescriptor;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.editors.nodes.factory.VariableChooserNodeFactory;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author  supernikita
 */
public class VariableChooserPanel 
        extends AbstractTreeChooserPanel<VariableDeclaration>
        implements Reusable  {
    
    private MyModelListener modelListener;
    
    /** Creates new form VariableChooserPanel1 */
    public VariableChooserPanel() {
    }
    
    public VariableChooserPanel(Lookup lookup) {
        super(lookup);
    }
    
    @Override
    public void createContent() {
        initComponents();
        //
        super.createContent();
        //
        BeanTreeView myTreeView = (BeanTreeView) pnlTreeView;
        myTreeView.setRootVisible(true);
        myTreeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        myTreeView.setPopupAllowed(false);
        
        //
        chbxShowAppropriateOnly.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //
                        // Update filter
                        VariableTypeFilter typeFilter = (VariableTypeFilter)getLookup().
                                lookup(VariableTypeFilter.class);
                        if (typeFilter != null) {
                            typeFilter.setShowAppropriateVarOnly(chbxShowAppropriateOnly.isSelected());
                        }
                        //
                        // Reload tree
                        reloadVariableDeclarationScopes();
                    }
                });
            }
        });
        //
        StandardButtonBar buttonBar = (StandardButtonBar)pnlButtonBar;
        buttonBar.createContent();
        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Object source = evt.getSource();
                StandardButtonBar buttonBar = (StandardButtonBar)pnlButtonBar;
                if (buttonBar.btnAdd.equals(source)) {
                    add();
                } else if (buttonBar.btnEdit.equals(source)) {
                    edit();
                } else if (buttonBar.btnDelete.equals(source)) {
                    delete();
                }
            }
        };
        buttonBar.btnAdd.addActionListener(btnListener);
        buttonBar.btnEdit.addActionListener(btnListener);
        buttonBar.btnDelete.addActionListener(btnListener);
        //
        getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateButtonState();
            }
        });
        //
        buttonBar.btnUp.setVisible(false);
        buttonBar.btnDown.setVisible(false);
        updateButtonState();
    }
    
    @Override
    public boolean initControls() {
        super.initControls();
        //
        // Set state for the ShowAppropriateOnly check-box
        VariableTypeFilter typeFilter = (VariableTypeFilter)getLookup().
                lookup(VariableTypeFilter.class);
        if (typeFilter != null) {
            chbxShowAppropriateOnly.setVisible(true);
            chbxShowAppropriateOnly.setSelected(typeFilter.isShowAppropriateVarOnly());
        } else {
            chbxShowAppropriateOnly.setVisible(false);
            chbxShowAppropriateOnly.setSelected(false);
        }
        //
        return true;
    }
    
    private void reloadVariableDeclarationScopes() {
        Node root = getExplorerManager().getRootContext();
        NodeUtils.SearchVisitor visitor = new SearchVisitor() {
            public boolean accept(Node node) {
                if (node instanceof BpelNode) {
                    Object obj = ((BpelNode)node).getReference();
                    if (obj != null && obj instanceof VariableDeclarationScope) {
                        return true;
                    }
                }
                return false;
            }
            
            public boolean drillDeeper(Node node) {
                return true;
            }
        };
        //
        List<Node> nodesToReload = NodeUtils.findNodes(
                getExplorerManager().getRootContext(), visitor, -1);
        //
        // Update type filter before reloading nodes
        if (!nodesToReload.isEmpty()) {
            VariableTypeFilter typeFilter = (VariableTypeFilter)getLookup().
                    lookup(VariableTypeFilter.class);
            if (typeFilter != null) {
                typeFilter.setShowAppropriateVarOnly(
                        chbxShowAppropriateOnly.isSelected());
            }
        }
        //
        for (Node node : nodesToReload) {
            Children children = node.getChildren();
            if (children instanceof ReloadableChildren) {
                ((ReloadableChildren)children).reload();
            }
        }
    }
    
    @Override
    public void setLookup(Lookup lookup) {
        //
        // Tune up the lookup first
        //
        ArrayList lookupExtensions = new ArrayList();
        //
        // Set the standard tree params if nothing specified.
        NodesTreeParams treeParams =
                (NodesTreeParams)lookup.lookup(NodesTreeParams.class);
        if (treeParams == null) {
            treeParams = new NodesTreeParams();
            treeParams.setTargetNodeClasses(VariableNode.class);
            treeParams.setLeafNodeClasses(VariableNode.class);
            //
            lookupExtensions.add(treeParams);
        }
        //
        if (lookupExtensions.isEmpty()) {
            super.setLookup(lookup);
        } else {
            Lookup newLookup = new ExtendedLookup(lookup, lookupExtensions);
            super.setLookup(newLookup);
        }
        //
    }
    
    @Override
    protected Node constructRootNode() {
        Node result = null;
        //
        VariableChooserNodeFactory factory =
                new VariableChooserNodeFactory(
                PropertyNodeFactory.getInstance());
        Process process = getModel().getProcess();
        result = (BpelNode)factory.createNode(
                NodeType.PROCESS, process, getLookup());
        //
        return result;
    }
    
    @Override
    protected Validator createValidator() {
        return new MyValidator();
    }
    
    @Override
    public boolean subscribeListeners() {
        if (modelListener == null) {
            modelListener = new MyModelListener();
        }
        getModel().addEntityChangeListener(modelListener);
        return true;
    }
    
    @Override
    public boolean unsubscribeListeners() {
        if (modelListener != null) {
            getModel().removeEntityChangeListener(modelListener);
        }
        return true;
    }
    
    /**
     * Set selection to the node is corresponding to the specified variable.
     * Nothing is doing if the variable is null.
     */
    public void setSelectedValue(VariableDeclaration newValue) {
        if (newValue != null) {
            //
            // Check if the newVariable can be filtered
            VariableTypeFilter typeFilter = (VariableTypeFilter)getLookup().
                    lookup(VariableTypeFilter.class);
            if (typeFilter != null) {
                VariableTypeInfoProvider varTypeProv = 
                        new DefaultTypeInfoProvider(newValue);
                boolean isAllowed = typeFilter.isTypeAllowed(varTypeProv);
                if (!isAllowed) {
                    if (chbxShowAppropriateOnly.isSelected()) {
                        chbxShowAppropriateOnly.setSelected(false);
                        typeFilter.setShowAppropriateVarOnly(false);
                        // Reload tree
                        reloadVariableDeclarationScopes();
                    }
                }
            }
            //
            Node rootNode = getExplorerManager().getRootContext();
            Node node =
                    NodeUtils.findFirstNode(newValue, VariableNode.class, rootNode);
            if (node != null) {
                super.setSelectedNode(node);
            }
        }
    }
    
    public VariableDeclaration getSelectedValue() {
        Node node = super.getSelectedNode();
        if (node instanceof VariableNode) {
            return ((VariableNode)node).getReference();
        }
        return null;
    }
    
    private void updateButtonState() {
        Node selectedNode = getSelectedNode();
        boolean isVariableNode = selectedNode != null &&
                selectedNode instanceof VariableNode;
        boolean isContainerNode = selectedNode != null && (
                selectedNode instanceof BaseScopeNode);
        // || selectedNode instanceof BaseScopeNode);
        //
        StandardButtonBar buttonBar = (StandardButtonBar)pnlButtonBar;
        buttonBar.btnAdd.setEnabled(isContainerNode);
        buttonBar.btnDelete.setEnabled(isVariableNode);
        buttonBar.btnEdit.setEnabled(isVariableNode);
    }
    
    private void add() {
        final Node selectedNode = getSelectedNode();
        if (selectedNode != null && selectedNode instanceof BaseScopeNode) {
            //
            BPELElementsBuilder builder = getModel().getBuilder();
            //
            String title = NbBundle.getMessage(
                    FormBundle.class, "LBL_CreateNewVariableTitle"); // NOI18N
            //
            final Variable newVar = builder.createVariable();
            //
            VariableNode varNode = new VariableNode(newVar, getLookup());
            //
            SimpleCustomEditor customEditor =
                    new SimpleCustomEditor<VariableDeclaration>(
                    (VariableNode)varNode, VariableMainPanel.class,
                    EditingMode.CREATE_NEW_INSTANCE);
            NodeEditorDescriptor descriptor =
                    new NodeEditorDescriptor(customEditor, title);
            descriptor.setOkButtonProcessor(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    BaseScope scope = (BaseScope)((BaseScopeNode)selectedNode).
                            getReference();
                    assert scope != null;
                    VariableContainer container = scope.getVariableContainer();
                    if (container == null) {
                        BPELElementsBuilder builder = getModel().getBuilder();
                        container = builder.createVariableContainer();
                        scope.setVariableContainer(container);
                        container = scope.getVariableContainer();
                    }
                    container.addVariable(newVar);
                    return Boolean.TRUE;
                }
            });
            //
            Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
        }
    }
    
    private void edit() {
        Node selectedNode = getSelectedNode();
        if (selectedNode != null && selectedNode instanceof VariableNode) {
            BPELElementsBuilder builder = getModel().getBuilder();
            //
            String title = NbBundle.getMessage(
                    FormBundle.class, "LBL_EditVariableTitle"); // NOI18N
            SimpleCustomEditor customEditor = new SimpleCustomEditor<VariableDeclaration>(
                    (VariableNode)selectedNode, VariableMainPanel.class,
                    EditingMode.EDIT_INSTANCE);
            NodeEditorDescriptor descriptor =
                    new NodeEditorDescriptor(customEditor, title);
            //
            Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
        }
    }
    
    private void delete() {
        Node selectedNode = getSelectedNode();
        if (selectedNode != null && selectedNode instanceof VariableNode) {
            VariableDeclaration varDecl =
                    ((VariableNode)selectedNode).getReference();
            if (varDecl instanceof Variable) {
                Variable variable = (Variable)varDecl;
                //
                FindHelper helper = (FindHelper) Lookup.getDefault().
                        lookup(FindHelper.class);
                Iterator<BaseScope> iterator = helper.scopeIterator(variable);
                assert iterator.hasNext();
                BaseScope scope = iterator.next();
                VariableContainer container = scope.getVariableContainer();
                //
                container.remove(variable);
                if (container.sizeOfVariable() == 0) {
                    scope.removeVariableContainer();
                }
            }
        }
    }
    
    private BpelModel getModel() {
        return (BpelModel)getLookup().lookup(BpelModel.class);
    }
    
    private class MyModelListener extends ChangeEventListenerAdapter {
        @Override
        public void notifyEntityInserted(EntityInsertEvent event) {
            tryUpdateVarContainer(event.getParent(), event.getValue());
        }
        
        @Override
        public void notifyEntityRemoved( EntityRemoveEvent event ) {
            tryUpdateVarContainer(event.getParent(), event.getOutOfModelEntity());
        }
        
        @Override
        public void notifyEntityUpdated( EntityUpdateEvent event ) {
            tryUpdateVarContainer(event.getParent(), event.getNewValue());
        }
        
        @Override
        public void notifyPropertyUpdated(PropertyUpdateEvent event) {
            BpelEntity propertyParent = event.getParent();
            if (NamedElement.NAME.equals(event.getName())) {
                if (propertyParent instanceof Variable) {
                    VariableNode aNode = NodeUtils.findFirstNode(
                            propertyParent,
                            VariableNode.class,
                            getExplorerManager().getRootContext());
                    if (aNode != null) {
                        aNode.updateName();
                    }
                }
            }
        }
        
        @Override
        public void notifyArrayUpdated( ArrayUpdateEvent event ) {
        }
        
        /**
         * Check condition and if they are suitable, then update the
         * Correlation Set Container.
         */
        private void tryUpdateVarContainer(BpelEntity parent, BpelEntity entity) {
            if (parent instanceof VariableContainer &&
                    entity instanceof Variable) {
                //
                final BpelContainer scopeContainer = parent.getParent();
                if (scopeContainer instanceof BaseScope) {
                    //
                    NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
                        public boolean accept(Node node) {
                            if (node instanceof ContainerBpelNode) {
                                Object container =
                                        ((ContainerBpelNode)node).getContainerReference();
                                if (scopeContainer.equals(container)) {
                                    return true;
                                }
                            } else if (node instanceof BpelNode) {
                                Object subject = ((BpelNode)node).getReference();
                                if (scopeContainer.equals(subject)) {
                                    return true;
                                }
                            }
                            //
                            return false;
                        }
                        
                        public boolean drillDeeper(Node node) {
                            if (node instanceof VariableNode) {
                                return false;
                            }
                            if (node instanceof SchemaComponentNode) {
                                return false;
                            }
                            return true;
                        }
                    };
                    //
                    Node scopeNode = NodeUtils.findFirstNode(
                            getExplorerManager().getRootContext(), visitor, -1);
                    //
                    if (scopeNode != null && scopeNode instanceof BaseScopeNode) {
                        Children scopeChildren = scopeNode.getChildren();
                        if (scopeChildren != null &&
                                scopeChildren instanceof ReloadableChildren) {
                            ((ReloadableChildren)scopeChildren).reload();
                        }
                    }
                }
            }
        }
    }
    
    private class MyValidator extends DefaultChooserValidator {
        @Override
        public void doFastValidation() {
            super.doFastValidation();
            boolean result = !hasReasons(Severity.ERROR);
            //
            if (result == true) {
                VariableTypeFilter typeFilter = (VariableTypeFilter)getLookup().
                        lookup(VariableTypeFilter.class);
                //
                // Check the variable type if the type filter is specified
                Node selectedNode = getSelectedNode();
                if (typeFilter != null && selectedNode != null &&
                        selectedNode instanceof VariableTypeInfoProvider) {
                    result = typeFilter.isTypeAllowed(
                            (VariableTypeInfoProvider)selectedNode);
                    if (!result) {
                        addReasonKey(Severity.ERROR, 
                                "ERR_INCORRECT_MESSAGE_TYPE"); // NOI18N
                    }
                }
            }
        }
        
        @Override
        public void doDetailedValidation() {
            super.doDetailedValidation();
            //
            VisibilityScope varVisScope = (VisibilityScope)getLookup().
                    lookup(VisibilityScope.class);
            if (varVisScope != null) {
                BpelEntity modelElement = varVisScope.getBaseModelElement();
                //
                VariableDeclaration selectedVar = getSelectedValue();
                if (selectedVar != null) {
                    String targetVarName = selectedVar.getVariableName();
                    //
                    FindHelper helper = (FindHelper)Lookup.getDefault().
                            lookup(FindHelper.class);
                    Iterator<VariableDeclarationScope> itr =
                            helper.varaibleDeclarationScopes(modelElement);
                    //
                    while(itr.hasNext()) {
                        VariableDeclarationScope varScope = itr.next();
                        final VariableDeclaration varInScope =
                                findVariableByNameInScope(targetVarName, varScope);
                        if (varInScope == null) {
                            // Try find the variable in the next scope
                            continue;
                        }
                        //
                        if (!varInScope.equals(selectedVar)) {
                            addReasonKey(Severity.ERROR,
                                    "ERR_SELECTED_VARIABLE_IS_OVERRIDDEN"); // NOI18N
                            //
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    setSelectedValue(varInScope);
                                }
                            });
                        }
                        break;
                    }
                }
            }
        }
        
        private VariableDeclaration findVariableByNameInScope(
                String targetVarName, VariableDeclarationScope vdScope) {
            if (vdScope == null || targetVarName == null ||
                    targetVarName.length() == 0) {
                return null;
            }
            //
            if (vdScope instanceof BaseScope) {
                VariableContainer vc = ((BaseScope)vdScope).getVariableContainer();
                if (vc != null) {
                    VariableDeclaration[] varArr =
                            (VariableDeclaration[])vc.getVariables();
                    //
                    for (VariableDeclaration varDecl : varArr) {
                        String varName = varDecl.getVariableName();
                        if (targetVarName.equals(varName)) {
                            return varDecl;
                        }
                    }
                }
            } else if (vdScope instanceof VariableDeclaration) {
                VariableDeclaration varDecl = (VariableDeclaration)vdScope;
                String varName = varDecl.getVariableName();
                if (targetVarName.equals(varName)) {
                    return varDecl;
                }
            }
            //
            return null;
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        pnlButtonBar = new StandardButtonBar();
        pnlLookupProvider = new TreeWrapperPanel();
        pnlTreeView = new BeanTreeView();
        chbxShowAppropriateOnly = new javax.swing.JCheckBox();

        pnlButtonBar.setLayout(new java.awt.GridBagLayout());

        pnlLookupProvider.setFocusable(false);
        pnlTreeView.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        org.jdesktop.layout.GroupLayout pnlLookupProviderLayout = new org.jdesktop.layout.GroupLayout(pnlLookupProvider);
        pnlLookupProvider.setLayout(pnlLookupProviderLayout);
        pnlLookupProviderLayout.setHorizontalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlTreeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
        );
        pnlLookupProviderLayout.setVerticalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlTreeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
        );

        chbxShowAppropriateOnly.setText(NbBundle.getMessage(FormBundle.class, "CHB_ShowAppropriate&TypesOnly"));
        chbxShowAppropriateOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbxShowAppropriateOnly.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(chbxShowAppropriateOnly)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//          .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlButtonBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
//              .add(pnlButtonBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbxShowAppropriateOnly))
        );
    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chbxShowAppropriateOnly;
    private javax.swing.JPanel pnlButtonBar;
    private javax.swing.JPanel pnlLookupProvider;
    private javax.swing.JScrollPane pnlTreeView;
    // End of variables declaration//GEN-END:variables
    
}
