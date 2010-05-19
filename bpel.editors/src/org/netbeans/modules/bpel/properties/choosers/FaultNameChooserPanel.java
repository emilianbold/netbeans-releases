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

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.editors.*;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.FaultNode;
import org.netbeans.modules.bpel.properties.editors.controls.AbstractTreeChooserPanel;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.bpel.properties.editors.nodes.factory.FaultNameChooserNodeFactory;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class FaultNameChooserPanel extends AbstractTreeChooserPanel<QName>
        implements Reusable {
    
    static final long serialVersionUID = 1L;
    
    public FaultNameChooserPanel() {
    }
    
    public FaultNameChooserPanel(Lookup lookup) {
        super(lookup);
    }
    
    public void createContent() {
        //
        initComponents();
        //
        ((BeanTreeView)treeView).setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION );
        ((BeanTreeView)treeView).setRootVisible(true);
        ((BeanTreeView)treeView).setPopupAllowed(false);

        //
        super.createContent();
    }
    
    protected Node constructRootNode() {
        Node result = null;
        //
        BpelModel model = (BpelModel)getLookup().lookup(BpelModel.class);
        Process process = model.getProcess();
        FaultNameChooserNodeFactory factory =
                new FaultNameChooserNodeFactory(
                PropertyNodeFactory.getInstance());
        result = (BpelNode)factory.createNode(
                NodeType.PROCESS, process, getLookup());
        //
        return result;
    }
    
    public void setLookup(Lookup lookup) {
        //
        List lookupObjects = new ArrayList();
        //
        // Create the default tree parameters if not any is specified
        NodesTreeParams treeParams =
                (NodesTreeParams)lookup.lookup(NodesTreeParams.class);
        if (treeParams == null) {
            // Set default Chooser Params
            treeParams = new NodesTreeParams();
            treeParams.setTargetNodeClasses(FaultNode.class);
            treeParams.setLeafNodeClasses(FaultNode.class);
            //
            lookupObjects.add(treeParams);
        }
        //
        if (lookupObjects.isEmpty()) {
            super.setLookup(lookup);
        } else {
            Object[] loArr = lookupObjects.toArray();
            Lookup correctedLookup = new ExtendedLookup(lookup, loArr);
            super.setLookup(correctedLookup);
        }
    }
    
    public BeanTreeView getTreeView() {
        return (BeanTreeView)treeView;
    }
    
    /**
     * Set selection to the node is corresponding to the specified variable.
     * Nothing is doing if the variable is null.
     */
    public void setSelectedValue(QName newValue) {
        if (newValue != null) {
            Node rootNode = getExplorerManager().getRootContext();
            Node node = NodeUtils.findFirstNode(
                    newValue, FaultNode.class, rootNode);
            if (node != null) {
                super.setSelectedNode(node);
            }
        }
    }
    
    public QName getSelectedValue() {
        Node node = super.getSelectedNode();
        if (node instanceof FaultNode) {
            return ((FaultNode)node).getReference();
        } else {
            return null;
        }
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

        treeView.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        org.jdesktop.layout.GroupLayout pnlLookupProviderLayout = new org.jdesktop.layout.GroupLayout(pnlLookupProvider);
        pnlLookupProvider.setLayout(pnlLookupProviderLayout);
        pnlLookupProviderLayout.setHorizontalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        pnlLookupProviderLayout.setVerticalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
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
    private javax.swing.JPanel pnlLookupProvider;
    private javax.swing.JScrollPane treeView;
    // End of variables declaration//GEN-END:variables
    
}
