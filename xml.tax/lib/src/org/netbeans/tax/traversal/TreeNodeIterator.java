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
import org.netbeans.tax.InvalidStateException;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeNodeIterator {
    /** Root node of this iterator. */
    private TreeNode root;
    
    /** Determines which node types are presented. */
    private int whatToShow;
    
    /** Filter to screen nodes. */
    private TreeNodeFilter filter;

    /** State of iterator. */
    private boolean valid;
    
    
    /** Creates new TreeNodeIterator.
     * @param node root node
     * @param wTS what to show
     * @param f used filter
     */
    public TreeNodeIterator (TreeNode node, int wTS, TreeNodeFilter f) {
        root = node;
        whatToShow = wTS;
        filter = f;        

        valid = true;
    }
    
    
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
    public TreeNode nextNode () throws InvalidStateException {
        if (!!! valid) {
            throw new InvalidStateException (Util.getString ("EXC_TreeNodeIterator.nextNode"));
        }
        
        Util.debug ("[PENDING]: TreeNodeIterator.nextNode ()"); // NOI18N
        return null;
    }

    /**
     */
    public TreeNode previousNode () throws InvalidStateException {
        if (!!! valid) {
            throw new InvalidStateException (Util.getString ("EXC_TreeNodeIterator.previousNode"));
        }

        Util.debug ("[PENDING]: TreeNodeIterator.nextNode ()"); // NOI18N
        return null;
    }

    /**
     */
    public void detach () {
        valid = false;
    }

}
