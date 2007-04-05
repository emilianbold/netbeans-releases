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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source.query;

import com.sun.source.tree.Tree;
import org.netbeans.api.java.source.Comment;

/**
 * The service that maps Trees with their associated comments.
 *
 * @see org.netbeans.modules.java.source.model.CommentSet
 */
public interface CommentHandler {
    
    /**
     * Returns true if the specified tree has an associated CommentSet.
     */
    boolean hasComments(Tree tree);
    
    /**
     * Returns the CommentSet associated with a tree, or null if the tree
     * does not have any comments.
     */
    CommentSet getComments(Tree tree);
    
    /**
     * Copies preceding and trailing comments from one tree to another,
     * appending the new entries to the existing comment lists.
     */
    void copyComments(Tree fromTree, Tree toTree);
    
    /**
     * Add a preceding comment to a tree's comment set.  If a comment set
     * for the tree doesn't exist, one will be created.
     */
    void addComment(Tree tree, Comment c);
}
