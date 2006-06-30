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
            throw new NotSupportedException (Util.THIS.getString ("EXC_invalid_current_node_value"));
        }
        
        currentNode = curNode;
    }
    
    /**
     */
    public TreeNode parentNode () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.parentNode ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode firstChild () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.firstChild ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode lastChild () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.lastChild ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode previousSibling () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.previousSibling ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode nextSibling () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.nextSibling ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode previousNode () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.previousNode ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode nextNode () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.nextNode ()"); // NOI18N
        
        return null;
    }
    
}
