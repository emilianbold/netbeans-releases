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

package org.netbeans.modules.xml.wsdl.ui.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLModelCookie;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Navigator component containing a tree of WSDL components.
 *
 * @author  Nathan Fiedler
 */
public class WSDLNavigatorContent extends JPanel
        implements ExplorerManager.Provider, Runnable, PropertyChangeListener {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    public static final String CURRENT_NODES = "WSDLEDITOR_CURRENT_SELECTION";
//    /** The lookup for our component tree. */
//    private static Lookup lookup;
    /** Explorer manager for the tree view. */
    private ExplorerManager explorerManager;
    /** Our component node tree view. */
    private TreeView treeView;
    /** Root node of the tree. */
    private Node rootNode;
    
    private final JLabel notAvailableLabel = new JLabel(
            NbBundle.getMessage(WSDLNavigatorContent.class, "MSG_NotAvailable"));

//    static {
//        // Present a read-only view of the components.
//        lookup = Lookups.singleton(new ReadOnlyCookie(true));
//    }
    
    private WSDLNavigatorContent content;
    

    /**
     * Creates a new instance of WSDLNavigatorContent.
     */
    public WSDLNavigatorContent() {
        setLayout(new BorderLayout());
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        notAvailableLabel.setHorizontalAlignment(SwingConstants.CENTER);
        notAvailableLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        notAvailableLabel.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        // to ensure our background color will have effect
        notAvailableLabel.setOpaque(true);
        getTreeView(); //populates the treeView variable.
    }
    
    public WSDLNavigatorContent getDefault() {
        if (content == null) {
            content = new WSDLNavigatorContent();
        }
        return content;
    }

//    /**
//     * Expand the nodes which should be expanded by default.
//     */
//    protected void expandDefaultNodes() {
//        Node rootNode = getExplorerManager().getRootContext();
//        // Need to prevent looping on malformed trees, so avoid going too
//        // deep when expanding the children of nodes with only one child.
//        int depth = 0;
//        do {
//            Node[] children = rootNode.getChildren().getNodes();
//            if (children.length == 1) {
//                // Expand all nodes that have only a single child.
//                treeView.expandNode(children[0]);
//                rootNode = children[0];
//                depth++;
//            } else {
//                // Expand all first-level children that are meant to be shown
//                // expanded by default.
//                for (Node child : children) {
//                    DefaultExpandedCookie cookie = (DefaultExpandedCookie)
//                    child.getCookie(DefaultExpandedCookie.class);
//                    if (cookie != null && cookie.isDefaultExpanded()) {
//                        treeView.expandNode(child);
//                    }
//                }
//                rootNode = null;
//            }
//        } while (rootNode != null && depth < 5);
//
//        // The following code addresses two issues:
//        //
//        // 1. When viewing large files, expanding the default set of nodes
//        //    generally means that the contents of the column are so long that
//        //    copious amounts of scrolling are necessary to see it all. This is
//        //    not desirable for the user's first experience with the document.
//        //
//        // 2. Because BasicTreeUI essentially ignores the scrollsOnExpand
//        //    setting (or at least it does not work as documented), the tree
//        //    is left scrolled to some random position.
//        //
//        // So, if scrolling is necessary, then collapse root's children.
//        JTree tree = (JTree) treeView.getViewport().getView();
//        if (tree.getRowCount() > tree.getVisibleRowCount()) {
//            rootNode = getExplorerManager().getRootContext();
//            Enumeration kids = rootNode.getChildren().nodes();
//            while (kids.hasMoreElements()) {
//                Node kid = (Node) kids.nextElement();
//                treeView.collapseNode(kid);
//            }
//        }
//    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    /**
     * Show the data object in the navigator.
     *
     * @param  dobj  data object to show.
     */
    public void navigate(DataObject dobj) {
        WSDLModel model = getModel(dobj);
        if (model == null || model.getState() != WSDLModel.State.VALID) {
            showError();
        } else {
            show(model);
        }
        repaint();
    }

    private WSDLModel getModel(DataObject dataobject) {
        WSDLModel model = null;
        try {
            WSDLModelCookie modelCookie = dataobject.getCookie(WSDLModelCookie.class);
            if (modelCookie != null) {
                model = modelCookie.getModel();
                if (model != null) {
                    model.removePropertyChangeListener(this);
                    model.addPropertyChangeListener(this);
                }
            }
        } catch (IOException ioe) {
            // Show a blank page if there is an error.
        }
        return model;
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean ret = super.requestFocusInWindow();
        if (treeView != null) {
            return treeView.requestFocusInWindow();
        }
        return ret;
    }

    public void run() {
        // Initially expand root node and the folder nodes below it.
        if (treeView != null && rootNode != null) {
            explorerManager.setRootContext(rootNode);
            treeView.expandNode(rootNode);
            Utility.expandNodes(treeView, 1, rootNode);
            selectActivatedNodes();
            validate();
            repaint();
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        Object source = event.getSource();
        String property = event.getPropertyName();
        //Logger.getLogger(getClass().getName()).info("\n\nTopComponent=" + TopComponent.getRegistry().getActivated() + "\nProperty Name=" + property + "\nSource=" + event.getSource() + "\noldValue=" + event.getOldValue() + "\nnewValue=" + event.getNewValue());
        if (WSDLModel.STATE_PROPERTY.equals(property) && source instanceof WSDLModel) {
            State newState = (State) event.getNewValue();
            if (newState == WSDLModel.State.VALID) {
                WSDLModel model = (WSDLModel) event.getSource();
                show(model);
            } else {
                showError();
            }
            return;
        }
        if (source instanceof TopComponent.Registry || 
                source instanceof ExplorerManager) {

            TopComponent tc = 
                (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc != null) {
                boolean isActivatedTC = (tc == TopComponent.getRegistry().getActivated());
                if (CURRENT_NODES.equals(property) && !isActivatedTC) {
                    selectActivatedNodes();
                } else if (ExplorerManager.PROP_SELECTED_NODES.equals(property)) {
                    Node[] filteredNodes = (Node[])event.getNewValue();
                    if (filteredNodes != null) {
                        // Set the active nodes for the parent TopComponent.
                        tc.setActivatedNodes(filteredNodes);
                        validate();
                    }
                } else if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(property) &&
                        !isActivatedTC) {
                    selectActivatedNodes();
                    validate();
                } else if (TopComponent.Registry.PROP_ACTIVATED.equals(property) &&
                        isActivatedTC) {
                    tc.setActivatedNodes(getExplorerManager().getSelectedNodes());
                    validate();
                }
            }
        }
    }

    private void selectActivatedNodes() {
        final Node[] activated = TopComponent.getRegistry().getActivatedNodes();
        SwingUtilities.invokeLater(new Runnable() {
        
            public void run() {
                List<Node> selNodes = new ArrayList<Node>();
                for (Node n : activated) {
                    WSDLComponent wc = n.getLookup().lookup(WSDLComponent.class);
                    if (wc != null) {
                        List<Node> path = UIUtilities.findPathFromRoot(
                                getExplorerManager().getRootContext(), wc);
                        if (path != null && !path.isEmpty()) {
                            selNodes.add(path.get(path.size() - 1));
                        }
                    }
                }
                revalidate();
                try {
                    getExplorerManager().setSelectedNodes(
                            selNodes.toArray(new Node[selNodes.size()]));
                } catch (PropertyVetoException pve) {
                }
                validate();
            }
        
        });
    }

    /**
     * Display the "not available" message in place of the tree view.
     */
    private void showError() {
        if (notAvailableLabel.isShowing()) {
            return;
        }
        if (treeView != null && treeView.isShowing()) remove(treeView);
        add(notAvailableLabel, BorderLayout.CENTER);
        validate();
        repaint();
    }

    /**
     * Show the tree view for the given model.
     *
     * @param  model  component model to display.
     */
    private void show(WSDLModel model) {
        remove(notAvailableLabel);
        TreeView tree = getTreeView();
        add(tree, BorderLayout.CENTER);
        validate();
        NodesFactory factory = NodesFactory.getInstance();
        rootNode = factory.create(model.getDefinitions());
        getExplorerManager().setRootContext(rootNode);
        SwingUtilities.invokeLater(this);
    }

    public void release() {
        //cleanup all the elements in the navigator.
        removeAll();
        validate();
        getExplorerManager().setRootContext(Node.EMPTY);
        getExplorerManager().setExploredContext(Node.EMPTY, new Node[0]);
        if (rootNode != null) {
            Definitions component = rootNode.getLookup().lookup(Definitions.class);
            if (component != null && component.isInDocumentModel()) {
                component.getModel().removePropertyChangeListener(this);
            }
        }
        rootNode = null;
        treeView = null;
    }
    
    private TreeView getTreeView() {
        if (treeView == null) {
            treeView = new BeanTreeView();
        }
        return treeView;
    }
    
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (treeView != null) {
                    treeView.setRootVisible(true);
                    explorerManager.setRootContext(new WaitNode());
                }
            } 
        });
    }
    
    private static class WaitNode extends AbstractNode {

        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/xml/text/navigator/resources/wait.gif"); // NOI18N

        WaitNode( ) {
            super( Children.LEAF );
        }

        @Override
        public Image getIcon(int type) {
            return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @java.lang.Override
        public java.lang.String getDisplayName() {
            return "Please Wait...";
        }

    }
}
