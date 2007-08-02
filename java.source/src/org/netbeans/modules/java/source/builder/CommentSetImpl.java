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
package org.netbeans.modules.java.source.builder;

import org.netbeans.api.java.source.Comment;
import org.netbeans.modules.java.source.query.CommentSet;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.List;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;

/**
 * Class that associates the before and after comments to a tree.
 */
public class CommentSetImpl implements Cloneable, CommentSet {
    Tree tree;
    List<Comment> precedingComments = new ArrayList<Comment>();
    List<Comment> trailingComments = new ArrayList<Comment>();

    /**
     * Add the specified comment string to the list of preceding comments. 
     */
    public void addPrecedingComment(String s) {
        addPrecedingComment(Comment.create(s));
    }

    /**
     * Add the specified comment to the list of preceding comments. 
     */
    public void addPrecedingComment(Comment c) {
        precedingComments.add(c);
    }

    /**
     * Add a list of comments to the list of preceding comments.
     */
    public void addPrecedingComments(List<Comment> comments) {
        precedingComments.addAll(comments);
    }
    
    /**
     * Add the specified comment string to the list of trailing comments. 
     */
    public void addTrailingComment(String s) {
        addTrailingComment(Comment.create(s));
    }

    /**
     * Add the specified comment to the list of trailing comments. 
     */
    public void addTrailingComment(Comment c) {
        trailingComments.add(c);
    }

    /**
     * Add a list of comments to the list of preceding comments.
     */
    public void addTrailingComments(List<Comment> comments) {
        trailingComments.addAll(comments);
    }
    
    public List<Comment> getPrecedingComments() {
        return precedingComments;
    }
    
    public List<Comment> getTrailingComments() {
        return trailingComments;
    }
    
    public boolean hasComments() {
        return precedingComments.size() > 0 || trailingComments.size() > 0;
    }
    
    /** 
     * Returns the first character position, which is either the initial
     * position of the first preceding comment, or NOPOS if there are no comments.
     */
    public int pos() {
        return precedingComments.size() > 0 ? 
            precedingComments.get(0).pos() : NOPOS;
    }
        
    void setTree(Tree newTree) {
        tree = newTree;
    }
    
    public boolean hasChanges() {
        for (Comment c : precedingComments)
            if (c.isNew())
                return true;
        for (Comment c : trailingComments)
            if (c.isNew())
                return true;
        return false;
    }
    
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
	    throw new InternalError("Unexpected " + e);
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        sb.append('{');
        for (Comment c : precedingComments) {
            if (!first)
                sb.append(',');
            sb.append(c.getText());
            first = false;
        }
        for (Comment c : trailingComments) {
            if (!first)
                sb.append(',');
            sb.append(c.getText());
            first = false;
        }
        sb.append('}');
        return sb.toString();
    }
}
