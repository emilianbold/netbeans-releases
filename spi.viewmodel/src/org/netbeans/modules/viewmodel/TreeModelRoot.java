/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.viewmodel;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.netbeans.spi.viewmodel.Models;

import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
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
    private WeakHashMap objectToNode = new WeakHashMap ();
    private TreeTable treeTable;
    
    /** The children evaluator for view if this root. */
    private TreeModelNode.LazyEvaluator childrenEvaluator;
    /** The values evaluator for view if this root. */
    private TreeModelNode.LazyEvaluator valuesEvaluator;


    public TreeModelRoot (Models.CompoundModel model, TreeTable treeTable) {
        this.model = model;
        this.treeTable = treeTable;
        model.addModelListener (this);
    }
    
    public TreeTable getTreeTable () {
        return treeTable;
    }

    public TreeModelNode getRootNode () {
        if (rootNode == null)
            rootNode = new TreeModelNode (model, this, model.getRoot ());
        return rootNode;
    }
    
    void registerNode (Object o, TreeModelNode n) {
        objectToNode.put (o, new WeakReference (n));
    }
    
    TreeModelNode findNode (Object o) {
        WeakReference wr = (WeakReference) objectToNode.get (o);
        if (wr == null) return null;
        return (TreeModelNode) wr.get ();
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
                            return ; // We're done
                        }
                    }
                }
                if (event instanceof ModelEvent.NodeChanged) {
                    ModelEvent.NodeChanged nchEvent = (ModelEvent.NodeChanged) event;
                    Object node = nchEvent.getNode();
                    if (node != null) {
                        TreeModelNode tmNode = findNode(node);
                        if (tmNode != null) {
                            if (event instanceof javax.naming.ldap.ExtendedResponse) {
                                String id = ((javax.naming.ldap.ExtendedResponse) event).getID();
                                tmNode.refresh(id);
                            } else {
                                tmNode.refresh();
                            }
                            return ; // We're done
                        }
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
            childrenEvaluator = new TreeModelNode.LazyEvaluator();
        }
        return childrenEvaluator;
    }

    synchronized TreeModelNode.LazyEvaluator getValuesEvaluator() {
        if (valuesEvaluator == null) {
            valuesEvaluator = new TreeModelNode.LazyEvaluator();
        }
        return valuesEvaluator;
    }

    public void destroy () {
        if (model != null)
            model.removeModelListener (this);
        model = null;
        objectToNode = new WeakHashMap ();
    }
}

