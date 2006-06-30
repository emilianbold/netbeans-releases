/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            throw new InvalidStateException (Util.THIS.getString ("EXC_TreeNodeIterator.nextNode"));
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeIterator.nextNode ()"); // NOI18N
        return null;
    }
    
    /**
     */
    public TreeNode previousNode () throws InvalidStateException {
        if (!!! valid) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_TreeNodeIterator.previousNode"));
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeIterator.nextNode ()"); // NOI18N
        return null;
    }
    
    /**
     */
    public void detach () {
        valid = false;
    }
    
}
