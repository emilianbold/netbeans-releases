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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.bpel.properties.editors.*;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ClassRulesFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.NodeChildFilter;
import org.netbeans.modules.bpel.nodes.BaseScopeNode;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.CorrelationSetContainerNode;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.editors.controls.AbstractTreeChooserPanel;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.bpel.properties.editors.nodes.factory.CorrSetChooserNodeFactory;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class CorrelationSetChooserPanel
        extends AbstractTreeChooserPanel<Set<CorrelationSet>>
        implements Reusable {
    
    static final long serialVersionUID = 1L;
    
    public static NodeChildFilter STANDARD_FILTER = createStandardFilter();
    
    private StandardButtonBar buttonBar;
    private BeanTreeView myTreeView;
    
    private static NodeChildFilter createStandardFilter() {
        ClassRulesFilter filter = new ClassRulesFilter();
        filter.addAllowRule(BaseScopeNode.class, CorrelationSetContainerNode.class);
        filter.addAllowRule(BaseScopeNode.class, BaseScopeNode.class);
        return filter;
    }
    
    public CorrelationSetChooserPanel() {
    }
    
    public CorrelationSetChooserPanel(Lookup lookup) {
        super(lookup);
    }
    
    public void createContent() {
        //
        setLayout(new BorderLayout());
        //
        buttonBar = new StandardButtonBar();
        buttonBar.createContent();
        buttonBar.btnUp.setVisible(false);
        buttonBar.btnDown.setVisible(false);
        buttonBar.btnAdd.setVisible(false);
        buttonBar.btnEdit.setVisible(false);
        buttonBar.btnDelete.setVisible(false);
        add(buttonBar, BorderLayout.NORTH);
        //
        myTreeView = new BeanTreeView();
        myTreeView.setRootVisible(true);
        myTreeView.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        myTreeView.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        Dimension dim = myTreeView.getPreferredSize();
        dim.setSize(dim.getWidth(), 300d);
        myTreeView.setPreferredSize(dim);
        myTreeView.setPopupAllowed(false);
        
        add(createTreeWrapper(myTreeView), BorderLayout.CENTER);
        //
        super.createContent();
    }
    
    protected Node constructRootNode() {
        Node result = null;
        //
        BpelModel model = (BpelModel)getLookup().lookup(BpelModel.class);
        Process process = model.getProcess();
        CorrSetChooserNodeFactory factory =
                new CorrSetChooserNodeFactory(
                PropertyNodeFactory.getInstance());
        result = (BpelNode)factory.createNode(
                NodeType.PROCESS, process, getLookup());
        //
        return result;
    }
    
    public void setLookup(Lookup lookup) {
        List lookupObjects = new ArrayList();
        //
        // Create the standard filter if not any filter is specified
        NodeChildFilter filter =
                (NodeChildFilter)lookup.lookup(NodeChildFilter.class);
        if (filter == null) {
            lookupObjects.add(STANDARD_FILTER);
        }
        //
        // Create the default tree parameters if not any is specified
        NodesTreeParams treeParams =
                (NodesTreeParams)lookup.lookup(NodesTreeParams.class);
        if (treeParams == null) {
            // Set default Chooser Params
            treeParams = new NodesTreeParams();
            treeParams.setTargetNodeClasses(CorrelationSetNode.class);
            treeParams.setLeafNodeClasses(CorrelationSetNode.class);
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
    
    /**
     * Set selection to the node is corresponding to the specified variable.
     * Nothing is doing if the variable is null.
     */
    public void setSelectedValue(Set<CorrelationSet> newValue) {
        List<Node> nodesList = new ArrayList<Node>(newValue.size());
        //
        if (newValue != null) {
            Node rootNode = getExplorerManager().getRootContext();
            //
            for (CorrelationSet cs : newValue) {
                if (cs != null) {
                    Node node = NodeUtils.findFirstNode(
                            cs, CorrelationSetNode.class, rootNode);
                    if (node != null) {
                        nodesList.add(node);
                    }
                }
            }
        }
        //
        Node[] csNodesArr = nodesList.toArray(new Node[nodesList.size()]);
        setSelectedNodes(csNodesArr);
    }
    
    public Set<CorrelationSet> getSelectedValue() {
        Node[] nodeArr = super.getSelectedNodes();
        //
        Set<CorrelationSet> csSet = new HashSet<CorrelationSet>(nodeArr.length);
        //
        if (nodeArr != null) {
            for (Node node : nodeArr) {
                if(node != null && node instanceof CorrelationSetNode) {
                    CorrelationSet cs = ((CorrelationSetNode)node).getReference();
                    if (cs != null) {
                        csSet.add(cs);
                    }
                }
            }
        }
        //
        return csSet;
    }
    
}
