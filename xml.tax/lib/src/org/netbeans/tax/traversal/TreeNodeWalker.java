/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.traversal;

import org.netbeans.tax.TreeNode;
import org.netbeans.tax.NotSupportedException;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeNodeWalker {

    /** Root node of this iterator. */
    private TreeNode root;
    
    /** Determines which node types are presented. */
    private int whatToShow;
    
    /** Filter to screen nodes. */
    private TreeNodeFilter filter;

    /** Current node. */
    private TreeNode currentNode;


    //
    // init
    //

    /** Creates new TreeNodeIterator. */
    public TreeNodeWalker (TreeNode node, int wTS, TreeNodeFilter f) {
        root = node;
        whatToShow = wTS;
        filter = f;

        currentNode = root;
    }    

    
    //
    // itself
    //

    /**
     */
    public TreeNode getRoot () {
        return root;
    }

    /**
     */
    public int getWhatToShow () {
        return whatToShow;
    }

    /**
     */
    public TreeNodeFilter getFilter () {
        return filter;
    }

    /**
     */    
    public TreeNode getCurrentNode () {
        return currentNode;
    }

    /**
     */
    public void setCurrentNode (TreeNode curNode) throws NotSupportedException {
        if (curNode == null) {
            throw new NotSupportedException (Util.getString ("EXC_invalid_current_node_value"));
        }
        
        currentNode = curNode;
    }

    /**
     */
    public TreeNode parentNode () {
        Util.debug ("[PENDING]: TreeNodeWalker.parentNode ()"); // NOI18N

        return null;
    }

    /**
     */
    public TreeNode firstChild () {
        Util.debug ("[PENDING]: TreeNodeWalker.firstChild ()"); // NOI18N

        return null;
    }

    /**
     */
    public TreeNode lastChild () {
        Util.debug ("[PENDING]: TreeNodeWalker.lastChild ()"); // NOI18N

        return null;
    }

    /**
     */
    public TreeNode previousSibling () {
        Util.debug ("[PENDING]: TreeNodeWalker.previousSibling ()"); // NOI18N

        return null;
    }

    /**
     */
    public TreeNode nextSibling () {
        Util.debug ("[PENDING]: TreeNodeWalker.nextSibling ()"); // NOI18N

        return null;
    }

    /**
     */
    public TreeNode previousNode () {
        Util.debug ("[PENDING]: TreeNodeWalker.previousNode ()"); // NOI18N

        return null;
    }

    /**
     */
    public TreeNode nextNode () {
        Util.debug ("[PENDING]: TreeNodeWalker.nextNode ()"); // NOI18N

        return null;
    }

}
