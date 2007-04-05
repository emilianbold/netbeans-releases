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

import org.netbeans.api.java.source.Comment;

/**
 * The set of comments associated with a single tree node.
 */
public interface CommentSet {
    /**
     * Add the specified comment to the list of preceding comments.
     */
    void addPrecedingComment(Comment c);

    /**
     * Add the specified comment string to the list of preceding comments.
     */
    void addPrecedingComment(java.lang.String s);

    /**
     * Add a list of comments to the list of preceding comments.
     */
    void addPrecedingComments(java.util.List<Comment> comments);

    /**
     * Add the specified comment to the list of trailing comments.
     */
    void addTrailingComment(Comment c);

    /**
     * Add the specified comment string to the list of trailing comments.
     */
    void addTrailingComment(java.lang.String s);

    /**
     * Add a list of comments to the list of preceding comments.
     */
    void addTrailingComments(java.util.List<Comment> comments);

    java.util.List<Comment> getPrecedingComments();

    java.util.List<Comment> getTrailingComments();

    boolean hasChanges();

    boolean hasComments();

    /**
     * 
     * Returns the first character position, which is either the initial
     * position of the first preceding comment, or NOPOS if there are no comments.
     * 
     * @see org.netbeans.modules.java.source.query.Query#NOPOS
     */
    int pos();
    
}
