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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TreeEditorView extends JPanel
        implements ExplorerManager.Provider, Lookup.Provider,
        PropertyChangeListener {
    
    /**
     * 
     */
    private static final long serialVersionUID = -6844839168489591934L;
    
    private ExplorerManager explorerManager;
    private transient Lookup lookup;
    
    private WSDLModel mModel;
    
    private BeanTreeView btv;

    public TreeEditorView(WSDLModel model) {
        this.mModel = model;
    }
    
    private void initGUI() {
        setLayout(new BorderLayout());
        // Create the templates view
        btv = new BeanTreeView();
        btv.setRootVisible( true );
        btv.setSelectionMode( javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
        btv.setPopupAllowed( true );
        add(btv, BorderLayout.CENTER);

        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction,
                ExplorerUtils.actionCopy(explorerManager));
        map.put(DefaultEditorKit.cutAction,
                ExplorerUtils.actionCut(explorerManager));
        map.put(DefaultEditorKit.pasteAction,
                ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", //NOI18N
                ExplorerUtils.actionDelete(explorerManager, false));
        lookup = ExplorerUtils.createLookup(explorerManager, map);

        // Must do this when the component is in the UI tree.
        populateRootNode(mModel.getDefinitions());
        //Initially expand root node and the folder nodes below it.
        Node rootNode = explorerManager.getRootContext();
        btv.expandNode(rootNode);
        Utility.expandNodes(btv, 1, rootNode);
        try {
            explorerManager.setSelectedNodes(new Node[] {rootNode});
        } catch (PropertyVetoException pve) {
        }
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public Lookup getLookup() {
        return lookup;
    }
    
    private void populateRootNode(Definitions definitions) {
        if (definitions != null) {
            Node rootNode = NodesFactory.getInstance().create(definitions);
            explorerManager.setRootContext( rootNode );
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
            Node[] nodes = (Node[]) evt.getNewValue();
            if (nodes.length > 0) {
                //nodes[0].getChildren().getNodes(true);
                TopComponent tc = findParentTopComponent();
                // We cannot assume that we are visible, so check for null.
                if (tc != null) {
                    tc.setActivatedNodes(nodes);
                }
            }
        }
    }

    /**
     * Finds the TopComponent that contains us.
     *
     * @return  the parent TopComponent.
     */
    private TopComponent findParentTopComponent() {
        return (TopComponent) SwingUtilities.getAncestorOfClass(
                TopComponent.class, this);
    }
    
    public void showComponent(WSDLComponent sc) {
        List<Node> path = UIUtilities.findPathFromRoot(
                getExplorerManager().getRootContext(), sc);
        if (path == null || path.isEmpty()) {
            return;
        }
        Node node = path.get(path.size() - 1);
        try {
            getExplorerManager().setExploredContextAndSelection(
                    node, new Node[] { node });
        } catch (PropertyVetoException pve) {
        }
    }
    
    public void showComponent(SchemaComponent sc) {
        List<Node> path = UIUtilities.findPathFromRoot(
                getExplorerManager().getRootContext(), sc, mModel);
        if (path == null || path.isEmpty()) {
            return;
        }
        Node node = path.get(path.size() - 1);
        try {
            getExplorerManager().setExploredContextAndSelection(
                    node, new Node[] { node });
        } catch (PropertyVetoException pve) {
        }
    }
    
// IZ 96828: suppress help for nodes, just use WSDL view help topic.
//    public HelpCtx getHelpCtx() {
//        HelpCtx ctx = new HelpCtx(TreeEditorView.class);
//        Node[] selNodes = getExplorerManager().getSelectedNodes();
//        if (selNodes != null && selNodes.length > 0) {
//            for (Node node : selNodes) {
//                if (node.getHelpCtx() != null) {
//                    return node.getHelpCtx();
//                }
//            }
//        }
//        return ctx;
//    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        if (btv != null) {
            btv.requestFocus();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean ret = super.requestFocusInWindow();
        if (btv != null) {
            return btv.requestFocusInWindow();
        }
        return ret;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        initGUI();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        explorerManager.setRootContext(Node.EMPTY);
        explorerManager.setExploredContext(Node.EMPTY, new Node[0]);
        explorerManager.removePropertyChangeListener(this);
        removeAll();
        btv = null;
    }
    
    public void refreshNodes() {
        Node rootNode = explorerManager.getRootContext();
        DefinitionsNode dNode = rootNode.getLookup().lookup(DefinitionsNode.class);
        refreshNodes(rootNode);
    }
    
    private void refreshNodes(Node node) {
        ExtensibilityElementNode rNode = node.getLookup().lookup(ExtensibilityElementNode.class);
        if (rNode != null) {
            rNode.refresh();
        }
        Children children = node.getChildren();
        if (children != null) {
            for (Node child : children.getNodes()) {
                refreshNodes(child);
            }
        }
    }
}
