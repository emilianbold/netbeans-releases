/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.schema.ui.basic.navigator;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.basic.SchemaModelCookie;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.netbeans.modules.xml.schema.ui.nodes.DefaultExpandedCookie;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.ReadOnlyCookie;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorContent;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.util.RequestProcessor;

/**
 * XML Schema Navigator component containing a tree of schema components.
 *
 * @author  Nathan Fiedler
 */
public class SchemaNavigatorContent extends AbstractXMLNavigatorContent  implements Runnable{
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The lookup for our component tree. */
    private static Lookup lookup;
    /** Explorer root node **/
    private Node explorerRoot;
    
    private PropertyChangeListener mPCL;

    static {
        // Present a read-only view of the schema components.
        lookup = Lookups.singleton(new ReadOnlyCookie(true));
    }
    
    /**
     * Creates a new instance of SchemaNavigatorContent.
     */
    public SchemaNavigatorContent() {
        super();
        mPCL = new XAMUtils.AwtPropertyChangeListener(this);
        setLayout(new BorderLayout());
    }
    
    /**
     * Expand the nodes which should be expanded by default.
     */
    protected void expandDefaultNodes() {
        Node rootNode = getExplorerManager().getRootContext();
        // Need to prevent looping on malformed trees, so avoid going too
        // deep when expanding the children of nodes with only one child.
        int depth = 0;
        do {
            Node[] children = rootNode.getChildren().getNodes();
            if (children.length == 1) {
                // Expand all nodes that have only a single child.
                treeView.expandNode(children[0]);
                rootNode = children[0];
                depth++;
            } else {
                // Expand all first-level children that are meant to be shown
                // expanded by default.
                for (Node child : children) {
                    DefaultExpandedCookie cookie = (DefaultExpandedCookie)
                    child.getCookie(DefaultExpandedCookie.class);
                    if (cookie != null && cookie.isDefaultExpanded()) {
                        treeView.expandNode(child);
                    }
                }
                rootNode = null;
            }
        } while (rootNode != null && depth < 5);
        
        // The following code addresses two issues:
        //
        // 1. When viewing large schemas, expanding the default set of nodes
        //    generally means that the contents of the column are so long that
        //    copious amounts of scrolling are necessary to see it all. This is
        //    not desirable for the user's first experience with the document.
        //
        // 2. Because BasicTreeUI essentially ignores the scrollsOnExpand
        //    setting (or at least it does not work as documented), the tree
        //    is left scrolled to some random position.
        //
        // So, if scrolling is necessary, then collapse root's children.
        JTree tree = (JTree) treeView.getViewport().getView();
        if (tree.getRowCount() > tree.getVisibleRowCount()) {
            rootNode = getExplorerManager().getRootContext();
            Enumeration kids = rootNode.getChildren().nodes();
            while (kids.hasMoreElements()) {
                Node kid = (Node) kids.nextElement();
                treeView.collapseNode(kid);
            }
        }
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    private SchemaModel getSchemaModel(DataObject dobj) {
        try {
            SchemaModelCookie modelCookie = dobj.getCookie(SchemaModelCookie.class);
            //it is possible that the dobj is no longer for a schema.
            if(modelCookie == null)
                return null;
            SchemaModel model = modelCookie.getModel();
            if(model != null) {
                model.removePropertyChangeListener(mPCL);
                model.addPropertyChangeListener(mPCL);
            }
            return model;
        } catch (IOException ioe) {
            //will show blank page if there is an error.
        }
        
        return null;
    }
    
    /**
     * Show the data object in the navigator.
     *
     * @param  dobj  data object to show.
     */
    public void navigate(final DataObject dobj) {
        showWaitPanel();
        
        //get the model and create the new UI on background
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                //find the model in RPT
                final SchemaModel model = getSchemaModel(dobj);
                //finally update the UI in EDT
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (model == null || model.getRootComponent() == null ||
                                model.getState() != SchemaModel.State.VALID) {
                            showError(AbstractXMLNavigatorContent.ERROR_NO_DATA_AVAILABLE);
                        } else {
                            show(model);
                        }
                    }
                });
            }
        });
    }
    
    @Override
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }
    
    public void run() {
        getExplorerManager().setRootContext(explorerRoot);
        expandDefaultNodes();
        selectActivatedNodes();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        //
        assert SwingUtilities.isEventDispatchThread();
        //
        String property = event.getPropertyName();
        if(SchemaModel.STATE_PROPERTY.equals(property)) {
            onModelStateChanged(event);
            return;
        }
        TopComponent tc = (TopComponent) SwingUtilities.
                getAncestorOfClass(TopComponent.class,this);
        if (ExplorerManager.PROP_SELECTED_NODES.equals(property) &&
                tc == TopComponent.getRegistry().getActivated()) {
            Node[] filteredNodes = (Node[])event.getNewValue();
            if (filteredNodes != null && filteredNodes.length >= 1) {
                // Set the active nodes for the parent TopComponent.
                tc.setActivatedNodes(filteredNodes);
            }
        } else if(TopComponent.getRegistry().PROP_ACTIVATED_NODES.equals(property) &&
                tc != null && tc !=TopComponent.getRegistry().getActivated()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    selectActivatedNodes();
                }
            });
        } else if(TopComponent.getRegistry().PROP_ACTIVATED.equals(property) &&
                tc == TopComponent.getRegistry().getActivated()) {
            tc.setActivatedNodes(getExplorerManager().getSelectedNodes());
        }
    }
    
    private void selectActivatedNodes() {
        Node[] activated = TopComponent.getRegistry().getActivatedNodes();
        List<Node> selNodes = new ArrayList<Node>();
        for(Node n:activated) {
            SchemaComponent sc = (SchemaComponent) n.getLookup().
                    lookup(SchemaComponent.class);
            if(sc!=null) {
                List<Node> path = UIUtilities.findPathFromRoot(
                        getExplorerManager().getRootContext(),sc);
                if(path!=null&&!path.isEmpty())
                    selNodes.add(path.get(path.size()-1));
            }
        }
        try {
            getExplorerManager().setSelectedNodes(
                    selNodes.toArray(new Node[0]));
        } catch (PropertyVetoException ex) {
        }
    }
    
    public void onModelStateChanged(PropertyChangeEvent evt) {
        State newState = (State)evt.getNewValue();
        if(newState == SchemaModel.State.VALID) {
            SchemaModel model = (SchemaModel)evt.getSource();
            show(model);
            return;
        }
        
        //model is broken
        showError(AbstractXMLNavigatorContent.ERROR_NO_DATA_AVAILABLE);
        return;
    }
        
    private void show(SchemaModel model) {
        removeAll();
        add(treeView, BorderLayout.CENTER);
        SchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                model, lookup);
        explorerRoot = factory.createRootNode();
        // Expand the default nodes.
        EventQueue.invokeLater(this);
        revalidate();
        repaint();
    }
}
