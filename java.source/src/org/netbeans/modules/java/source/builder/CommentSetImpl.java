/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.source.builder;

import org.netbeans.api.java.source.Comment;
import org.netbeans.modules.java.source.query.CommentSet;
import static org.netbeans.modules.java.source.save.PositionEstimator.NOPOS;

import java.util.*;

/**
 * Class that associates the before and after comments to a tree.
 */
public final class CommentSetImpl implements Cloneable, CommentSet {
//    private final List<Comment> precedingComments = new ArrayList<Comment>();
//    private final List<Comment> trailingComments = new ArrayList<Comment>();
    private boolean commentsMapped;
    private final Map<RelativePosition, List<Comment>> commentsMap = new HashMap<RelativePosition, List<Comment>>();

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
        addComment(RelativePosition.PRECEDING, c);
    }

    /**
     * Add a list of comments to the list of preceding comments.
     */
    public void addPrecedingComments(List<Comment> comments) {
        for (Comment comment : comments) {
            addComment(RelativePosition.PRECEDING, comment);
        }
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
        addComment(RelativePosition.TRAILING, c);
    }

    /**
     * Add a list of comments to the list of preceding comments.
     */
    public void addTrailingComments(List<Comment> comments) {
        for (Comment comment : comments) {
            addComment(RelativePosition.TRAILING, comment);
        }
    }
    
    public List<Comment> getPrecedingComments() {
        return getComments(RelativePosition.PRECEDING);
    }
    
    public List<Comment> getTrailingComments() {
        return getComments(RelativePosition.TRAILING);
    }
    
    public boolean hasComments() {
        return !commentsMap.isEmpty();
    }
    
    /** 
     * Returns the first character position, which is either the initial
     * position of the first preceding comment, or NOPOS if there are no comments.
     */
    public int pos() {
        return pos(RelativePosition.PRECEDING);
    }

    public int pos(RelativePosition position) {
        List<Comment> list = getComments(position);
        return list.isEmpty() ? NOPOS : list.get(0).pos();
    }

    public void addComment(RelativePosition positioning, Comment c) {
        List<Comment> comments;
        if (commentsMap.containsKey(positioning)) {
            comments = commentsMap.get(positioning);
        } else {
            comments = new LinkedList<Comment>();
            commentsMap.put(positioning, comments);
        }
        comments.add(c);
        commentsMapped();
    }

    public void addComments(RelativePosition positioning, Iterable<? extends Comment> comments) {
        for (Comment c : comments) {
            addComment(positioning, c);
        }
    }
    
    public List<Comment> getComments(RelativePosition positioning) {
        if (commentsMap.containsKey(positioning)) {
            return commentsMap.get(positioning);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    public boolean hasChanges() {
        if (commentsMap.isEmpty()) return false;
        for (List<Comment> commentList : commentsMap.values()) {
            for (Comment comment : commentList) {
                if (comment.isNew()) return true;
            }
        }
        return false;
    }
    
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
	        throw new InternalError("Unexpected " + e);
        }
    }
    
    @SuppressWarnings({"MethodWithMultipleLoops"})
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<RelativePosition, List<Comment>> entry : commentsMap.entrySet()) {
            if (!first) {
                sb.append(", "); first = false;
            }
            sb.append("[").append(entry.getKey()).append(" -> ");
            for (Comment comment : entry.getValue()) {
                sb.append(',').append(comment.getText());
            }
            sb.append("]");
        }        
        sb.append('}');
        return sb.toString();
    }
    
    public boolean areCommentsMapped() {
        return commentsMapped;
    }
    
    public void commentsMapped() {
        commentsMapped = true;
    }
}
