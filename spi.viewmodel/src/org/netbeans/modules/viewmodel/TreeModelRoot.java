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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import org.netbeans.spi.viewmodel.Models;

import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;

import org.netbeans.spi.viewmodel.Models.TreeFeatures;
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
public class TreeModelRoot implements ModelListener {
    /** generated Serialized Version UID */
    static final long                 serialVersionUID = -1259352660663524178L;

    
    // variables ...............................................................

    private Models.CompoundModel model;
    private TreeModelNode rootNode;
    private WeakHashMap<Object, WeakReference<TreeModelNode>> objectToNode = new WeakHashMap<Object, WeakReference<TreeModelNode>>();
    private DefaultTreeFeatures treeFeatures;
    
    /** The children evaluator for view of this root. */
    private TreeModelNode.LazyEvaluator childrenEvaluator;
    /** The values evaluator for view of this root. */
    private TreeModelNode.LazyEvaluator valuesEvaluator;

    /** RequestProcessor to be used for evaluations. */
    private RequestProcessor rp;

    public TreeModelRoot (Models.CompoundModel model, TreeView treeView) {
        this.model = model;
        this.treeFeatures = new DefaultTreeFeatures(treeView);
        getRP();
        model.addModelListener (this);
    }

    public TreeModelRoot (Models.CompoundModel model, OutlineView outlineView) {
        this.model = model;
        this.treeFeatures = new DefaultTreeFeatures(outlineView);
        getRP();
        model.addModelListener (this);
    }

    private void getRP() {
        try {
            java.lang.reflect.Field rpf = model.getClass().getDeclaredField("rp");
            rpf.setAccessible(true);
            this.rp = (RequestProcessor) rpf.get(model);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        /*if (rp == null) {
            Exceptions.printStackTrace(new RuntimeException("NULL RP for "+model));
        } else {
            Exceptions.printStackTrace(new RuntimeException("RP for "+model+" is: "+rp));
        }*/
    }

    public RequestProcessor getRequestProcessor() {
        return rp;
    }
    
    public TreeFeatures getTreeFeatures () {
        return treeFeatures;
    }

    public TreeModelNode getRootNode () {
        if (rootNode == null)
            rootNode = new TreeModelNode (model, this, model.getRoot ());
        return rootNode;
    }
    
    void registerNode (Object o, TreeModelNode n) {
        objectToNode.put (o, new WeakReference<TreeModelNode>(n));
    }
    
    TreeModelNode findNode (Object o) {
        WeakReference<TreeModelNode> wr = objectToNode.get (o);
        if (wr == null) return null;
        return wr.get ();
    }
    
    public void modelChanged (final ModelEvent event) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                if (model == null) 
                    return; // already disposed
                if (event instanceof ModelEvent.TableValueChanged) {
                    ModelEvent.TableValueChanged tvEvent = (ModelEvent.TableValueChanged) event;
                    Object node = tvEvent.getNode();
                    if (node != null) {
                        TreeModelNode tmNode = findNode(node);
                        if (tmNode != null) {
                            String column = tvEvent.getColumnID();
                            if (column != null) {
                                tmNode.refreshColumn(column);
                            } else {
                                tmNode.refresh();
                            }
                        }
                        return ; // We're done
                    }
                }
                if (event instanceof ModelEvent.NodeChanged) {
                    ModelEvent.NodeChanged nchEvent = (ModelEvent.NodeChanged) event;
                    Object node = nchEvent.getNode();
                    if (node != null) {
                        TreeModelNode tmNode = findNode(node);
                        if (tmNode != null) {
                            tmNode.refresh(nchEvent.getChange());
                        }
                        return ; // We're done
                    } else { // Refresh all nodes
                        List<TreeModelNode> nodes = new ArrayList<TreeModelNode>(objectToNode.size());
                        for (WeakReference<TreeModelNode> wr : objectToNode.values()) {
                            TreeModelNode tm = wr.get();
                            if (tm != null) {
                                nodes.add(tm);
                            }
                        }
                        for (TreeModelNode tmNode : nodes) {
                            tmNode.refresh(nchEvent.getChange());
                        }
                        return ; // We're done
                    }
                }
                rootNode.setObject (model.getRoot ());
            }
        });
//        Iterator i = new HashSet (objectToNode.values ()).iterator ();
//        while (i.hasNext ()) {
//            WeakReference wr = (WeakReference) i.next ();
//            if (wr == null) continue;
//            TreeModelNode it = (TreeModelNode) wr.get ();
//            if (it != null)
//                it.refresh ();
//        }
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
    
    synchronized TreeModelNode.LazyEvaluator getChildrenEvaluator() {
        if (childrenEvaluator == null) {
            childrenEvaluator = new TreeModelNode.LazyEvaluator(rp);
        }
        return childrenEvaluator;
    }

    synchronized TreeModelNode.LazyEvaluator getValuesEvaluator() {
        if (valuesEvaluator == null) {
            valuesEvaluator = new TreeModelNode.LazyEvaluator(rp);
        }
        return valuesEvaluator;
    }

    public synchronized void destroy () {
        if (model != null) {
            model.removeModelListener (this);
            treeFeatures.destroy();
            treeFeatures = null;
        }
        model = null;
        objectToNode = new WeakHashMap<Object, WeakReference<TreeModelNode>>();
    }

    public synchronized Models.CompoundModel getModel() {
        return model;
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
            Node n = findNode (node);
            if (n == null) return false; // Something what does not exist is not expanded ;-)
            if (outline != null) {
                return outline.isExpanded(n);
            } else {
                return view.isExpanded (n);
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
            Node n = findNode (node);
            if (n != null) {
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
            Node n = findNode (node);
            if (n != null) {
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

