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

import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;


/**
 * Implements root node of hierarchy created for given TreeModel.
 *
 * @author   Jan Jancura
 */
public class TreeModelRoot implements TreeModelListener {
    /** generated Serialized Version UID */
    static final long                 serialVersionUID = -1259352660663524178L;

    
    // variables ...............................................................

    private CompoundModel model;
    private TreeModelNode rootNode;
    private WeakHashMap objectToNode = new WeakHashMap ();


    public TreeModelRoot (CompoundModel model) {
        this.model = model;
        model.addTreeModelListener (this);
    }

    public TreeModelNode getRootNode () {
        if (rootNode == null)
            rootNode = new TreeModelNode (model, this, model.getRoot ());
        return rootNode;
    }
    
    void registerNode (Object o, TreeModelNode n) {
        objectToNode.put (o, new WeakReference (n));
    }
    
    private TreeModelNode findNode (Object o) {
        WeakReference wr = (WeakReference) objectToNode.get (o);
        if (wr == null) return null;
        return (TreeModelNode) wr.get ();
    }
    
    public void treeChanged () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                if (model == null) 
                    return; // already disposed
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
    
    public void treeNodeChanged (Object parent) {
        final TreeModelNode tmn = findNode (parent);
        if (tmn == null) return;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                tmn.refresh (); 
            }
        });
    }

    public void destroy () {
        if (model != null)
            model.removeTreeModelListener (this);
        model = null;
        objectToNode = new WeakHashMap ();
    }
}

