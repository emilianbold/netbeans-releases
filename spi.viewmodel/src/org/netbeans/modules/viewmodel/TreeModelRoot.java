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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.viewmodel;

import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import org.netbeans.spi.viewmodel.Models;

import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;

import org.netbeans.spi.viewmodel.Models.TreeFeatures;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.TreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


/**
 * Implements root node of hierarchy created for given TreeModel.
 *
 * @author   Jan Jancura
 */
public class TreeModelRoot {
    /** generated Serialized Version UID */
    static final long                 serialVersionUID = -1259352660663524178L;

    
    // variables ...............................................................

    private Models.CompoundModel model;
    private HyperCompoundModel hyperModel;
    private ModelChangeListener[] modelListeners;
    private TreeModelNode rootNode;
    private final WeakHashMap<Object, WeakReference<TreeModelNode>[]> objectToNode = new WeakHashMap<Object, WeakReference<TreeModelNode>[]>();
    private DefaultTreeFeatures treeFeatures;
    private ExplorerManager manager;
    private OutlineView outlineView;
    
    /** The children evaluator for view of this root. *
    private final Map<RequestProcessor, TreeModelNode.LazyEvaluator> childrenEvaluators
            = new WeakHashMap<RequestProcessor, TreeModelNode.LazyEvaluator>();
    /** The values evaluator for view of this root. *
    private final Map<RequestProcessor, TreeModelNode.LazyEvaluator> valuesEvaluators
            = new WeakHashMap<RequestProcessor, TreeModelNode.LazyEvaluator>();
     */

    public TreeModelRoot (Models.CompoundModel model, TreeView treeView) {
        this.model = model;
        this.manager = ExplorerManager.find(treeView);
        this.treeFeatures = new DefaultTreeFeatures(treeView);
        modelListeners = new ModelChangeListener[] { new ModelChangeListener(model) };
        model.addModelListener (modelListeners[0]);
    }

    public TreeModelRoot (HyperCompoundModel model, TreeView treeView) {
        this.hyperModel = model;
        this.model = model.getMain();
        this.manager = ExplorerManager.find(treeView);
        this.treeFeatures = new DefaultTreeFeatures(treeView);
        int nl = model.getModels().length;
        modelListeners = new ModelChangeListener[nl];
        for (int i = 0; i < nl; i++) {
            Models.CompoundModel m = model.getModels()[i];
            modelListeners[i] = new ModelChangeListener(m);
            m.addModelListener(modelListeners[i]);
        }
    }

    public TreeModelRoot (Models.CompoundModel model, OutlineView outlineView) {
        this.model = model;
        this.manager = ExplorerManager.find(outlineView);
        this.treeFeatures = new DefaultTreeFeatures(outlineView);
        this.outlineView = outlineView;
        modelListeners = new ModelChangeListener[] { new ModelChangeListener(model) };
        model.addModelListener (modelListeners[0]);
    }

    public TreeModelRoot (HyperCompoundModel model, OutlineView outlineView) {
        this.hyperModel = model;
        this.model = model.getMain();
        this.manager = ExplorerManager.find(outlineView);
        this.treeFeatures = new DefaultTreeFeatures(outlineView);
        this.outlineView = outlineView;
        int nl = model.getModels().length;
        modelListeners = new ModelChangeListener[nl];
        for (int i = 0; i < nl; i++) {
            Models.CompoundModel m = model.getModels()[i];
            modelListeners[i] = new ModelChangeListener(m);
            m.addModelListener(modelListeners[i]);
        }
    }

    public TreeFeatures getTreeFeatures () {
        return treeFeatures;
    }

    public OutlineView getOutlineView() {
        return outlineView;
    }

    public TreeModelNode getRootNode () {
        if (rootNode == null) {
            if (hyperModel != null) {
                rootNode = new TreeModelHyperNode (hyperModel, this, model.getRoot ());
            } else {
                rootNode = new TreeModelNode (model, this, model.getRoot ());
            }
        }
        return rootNode;
    }
    
    void registerNode (Object o, TreeModelNode n) {
        synchronized (objectToNode) {
            WeakReference<TreeModelNode>[] wrs = objectToNode.get(o);
            if (wrs == null) {
                objectToNode.put (o, new WeakReference[] { new WeakReference<TreeModelNode>(n) });
            } else {
                for (int i = 0; i < wrs.length; i++) {
                    WeakReference<TreeModelNode> wr = wrs[i];
                    TreeModelNode tn = wr.get();
                    if (tn == n) {
                        return ;
                    } else if (tn == null) {
                        wrs[i] = new WeakReference<TreeModelNode>(n);
                        return ;
                    }
                }
                WeakReference<TreeModelNode>[] wrs2 = new WeakReference[wrs.length + 1];
                System.arraycopy(wrs, 0, wrs2, 0, wrs.length);
                wrs2[wrs.length] = new WeakReference<TreeModelNode>(n);
                objectToNode.put (o, wrs2);
            }
        }
    }
    
    TreeModelNode[] findNode (Object o) {
        WeakReference<TreeModelNode>[] wrs;
        synchronized (objectToNode) {
            wrs = objectToNode.get (o);
        }
        TreeModelNode[] tns = null;
        if (wrs != null) {
            for (int i = 0; i < wrs.length; i++) {
                // Suppose that it's unlikely that wrs.length > 1
                WeakReference<TreeModelNode> wr = wrs[i];
                TreeModelNode tn = wr.get ();
                if (tn == null) continue;
                if (tns == null) {
                    tns = new TreeModelNode[] { tn };
                } else {
                    TreeModelNode[] ntns = new TreeModelNode[tns.length + 1];
                    System.arraycopy(tns, 0, ntns, 0, tns.length);
                    ntns[tns.length] = tn;
                    tns = ntns;
                }
            }
        }
        if (tns == null) {
            return new TreeModelNode[0];
        } else {
            return tns;
        }
    }
    
//    public void treeNodeChanged (Object parent) {
//        final TreeModelNode tmn = findNode (parent);
//        if (tmn == null) return;
//        SwingUtilities.invokeLater (new Runnable () {
//            public void run () {
//                tmn.refresh (); 
//            }
//        });
//    }

    /*
    synchronized TreeModelNode.LazyEvaluator getChildrenEvaluator(RequestProcessor rp) {
        TreeModelNode.LazyEvaluator childrenEvaluator = childrenEvaluators.get(rp);
        if (childrenEvaluator == null) {
            childrenEvaluator = new TreeModelNode.LazyEvaluator(rp);
            childrenEvaluators.put(rp, childrenEvaluator);
        }
        return childrenEvaluator;
    }

    synchronized TreeModelNode.LazyEvaluator getValuesEvaluator(RequestProcessor rp) {
        TreeModelNode.LazyEvaluator valuesEvaluator = valuesEvaluators.get(rp);
        if (valuesEvaluator == null) {
            valuesEvaluator = new TreeModelNode.LazyEvaluator(rp);
            valuesEvaluators.put(rp, valuesEvaluator);
        }
        return valuesEvaluator;
    }
     */

    public synchronized void destroy () {
        if (model != null) {
            for (ModelChangeListener mchl : modelListeners) {
                mchl.model.removeModelListener (mchl);
            }
            treeFeatures.destroy();
            treeFeatures = null;
        }
        model = null;
        synchronized (objectToNode) {
            objectToNode.clear();
        }
    }

    public synchronized Models.CompoundModel getModel() {
        return model;
    }

    private final class ModelChangeListener implements ModelListener {

        private final Models.CompoundModel model;

        public ModelChangeListener(Models.CompoundModel model) {
            this.model = model;
        }

        public void modelChanged (final ModelEvent event) {
            //System.err.println("TreeModelRoot.modelChanged("+event.getClass()+") from "+model);
            //Thread.dumpStack();
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    if (model == null)
                        return; // already disposed
                    if (event instanceof ModelEvent.TableValueChanged) {
                        ModelEvent.TableValueChanged tvEvent = (ModelEvent.TableValueChanged) event;
                        Object node = tvEvent.getNode();
                        //System.err.println("TableValueChanged("+node+")");
                        if (node != null) {
                            TreeModelNode[] tmNodes = findNode(node);
                            //System.err.println("  nodes = "+Arrays.toString(tmNodes));
                            for (TreeModelNode tmNode : tmNodes) {
                                String column = tvEvent.getColumnID();
                                if (column != null) {
                                    tmNode.refreshColumn(column);
                                } else {
                                    tmNode.refresh(model);
                                }
                            }
                            return ; // We're done
                        }
                    }
                    if (event instanceof ModelEvent.NodeChanged) {
                        ModelEvent.NodeChanged nchEvent = (ModelEvent.NodeChanged) event;
                        Object node = nchEvent.getNode();
                        //System.err.println("NodeChanged("+node+")");
                        if (node != null) {
                            TreeModelNode[] tmNodes = findNode(node);
                            //System.err.println("  nodes = "+Arrays.toString(tmNodes));
                            for (TreeModelNode tmNode : tmNodes) {
                                tmNode.refresh(model, nchEvent.getChange());
                            }
                            return ; // We're done
                        } else { // Refresh all nodes
                            List<TreeModelNode> nodes = new ArrayList<TreeModelNode>(objectToNode.size());
                            for (WeakReference<TreeModelNode>[] wrs : objectToNode.values()) {
                                for (WeakReference<TreeModelNode> wr : wrs) {
                                    TreeModelNode tm = wr.get();
                                    if (tm != null) {
                                        nodes.add(tm);
                                    }
                                }
                            }
                            for (TreeModelNode tmNode : nodes) {
                                tmNode.refresh(model, nchEvent.getChange());
                            }
                            return ; // We're done
                        }
                    }
                    if (event instanceof ModelEvent.SelectionChanged) {
                        final Object[] nodes = ((ModelEvent.SelectionChanged) event).getNodes();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                List<TreeModelNode> tmNodes = new ArrayList<TreeModelNode>(nodes.length);
                                for (Object node : nodes) {
                                    TreeModelNode[] tmNodesf = findNode(node);
                                    for (TreeModelNode tmNode : tmNodesf) {
                                        tmNodes.add(tmNode);
                                    }
                                }
                                try {
                                    manager.setSelectedNodes(tmNodes.toArray(new Node[] {}));
                                } catch (PropertyVetoException ex) {
                                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Selection of "+Arrays.toString(nodes)+" vetoed.", ex); // NOI18N
                                }
                            }
                        });
                        return ;
                    }
                    rootNode.setObject (model, model.getRoot ());
                }
            });
        }

    }

    /**
     * Implements set of tree view features.
     */
    private final class DefaultTreeFeatures extends TreeFeatures implements TreeExpansionListener {
        
        private TreeView view;
        private OutlineView outline;
        
        private DefaultTreeFeatures (TreeView view) {
            this.view = view;
            JTree tree;
            try {
                java.lang.reflect.Field treeField = TreeView.class.getDeclaredField("tree");
                treeField.setAccessible(true);
                tree = (JTree) treeField.get(view);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return ;
            }
            tree.addTreeExpansionListener(this);
        }

        private DefaultTreeFeatures (OutlineView view) {
            this.outline = view;
            view.addTreeExpansionListener(this);
        }
        
        public void destroy() {
            if (outline != null) {
                outline.removeTreeExpansionListener(this);
            } else {
                JTree tree;
                try {
                    java.lang.reflect.Field treeField = TreeView.class.getDeclaredField("tree");
                    treeField.setAccessible(true);
                    tree = (JTree) treeField.get(view);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return ;
                }
                tree.removeTreeExpansionListener(this);
            }
        }
        
        /**
         * Returns <code>true</code> if given node is expanded.
         *
         * @param node a node to be checked
         * @return <code>true</code> if given node is expanded
         */
        public boolean isExpanded (
            Object node
        ) {
            Node[] ns = findNode (node);
            if (ns.length == 0) return false; // Something what does not exist is not expanded ;-)
            if (outline != null) {
                return outline.isExpanded(ns[0]);
            } else {
                return view.isExpanded (ns[0]);
            }

        }

        /**
         * Expands given list of nodes.
         *
         * @param node a list of nodes to be expanded
         */
        public void expandNode (
            Object node
        ) {
            Node[] ns = findNode (node);
            for (Node n : ns) {
                if (outline != null) {
                    outline.expandNode(n);
                } else {
                    view.expandNode (n);
                }
            }
        }

        /**
         * Collapses given node.
         *
         * @param node a node to be expanded
         */
        public void collapseNode (
            Object node
        ) {
            Node[] ns = findNode (node);
            for (Node n : ns) {
                if (outline != null) {
                    outline.collapseNode(n);
                } else {
                    view.collapseNode (n);
                }
            }
        }
        
        /**
          * Called whenever an item in the tree has been expanded.
          */
        public void treeExpanded (TreeExpansionEvent event) {
            Models.CompoundModel model = getModel();
            if (model != null) {
                model.nodeExpanded (initExpandCollapseNotify(event));
            }
        }

        /**
          * Called whenever an item in the tree has been collapsed.
          */
        public void treeCollapsed (TreeExpansionEvent event) {
            Models.CompoundModel model = getModel();
            if (model != null) {
                model.nodeCollapsed (initExpandCollapseNotify(event));
            }
        }

        private Object initExpandCollapseNotify(TreeExpansionEvent event) {
            Node node = Visualizer.findNode(event.getPath ().getLastPathComponent());
            Object obj = node.getLookup().lookup(Object.class);
            Object actOn;
            node = node.getParentNode();
            if (node == null) {
                actOn = new Integer(0);
            } else {
                Children ch = node.getChildren();
                if (ch instanceof TreeModelNode.TreeModelChildren) {
                    actOn = ((TreeModelNode.TreeModelChildren) ch).getTreeDepth();
                } else {
                    actOn = ch;
                }
            }
            Models.CompoundModel model = getModel();
            if (model != null) {
                DefaultTreeExpansionManager.get(model).setChildrenToActOn(actOn);
            }
            return obj;
        }

    }

}

