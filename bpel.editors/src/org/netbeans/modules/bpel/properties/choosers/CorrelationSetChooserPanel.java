/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
