/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.List;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;

/**
 * Class that associates the before and after comments to a tree.
 */
public class CommentSetImpl implements Cloneable, CommentSet {
    private final List<Comment> precedingComments = new ArrayList<Comment>();
    private final List<Comment> trailingComments = new ArrayList<Comment>();
    private boolean commentsMapped;

    /**
     * Add the specified comment string to the list of preceding comments. 
     */
    public void addPrecedingComment(String s) {
        addPrecedingComment(Comment.create(s));
        commentsMapped();
    }

    /**
     * Add the specified comment to the list of preceding comments. 
     */
    public void addPrecedingComment(Comment c) {
        precedingComments.add(c);
        commentsMapped();
    }

    /**
     * Add a list of comments to the list of preceding comments.
     */
    public void addPrecedingComments(List<Comment> comments) {
        precedingComments.addAll(comments);
        commentsMapped();
    }
    
    /**
     * Add the specified comment string to the list of trailing comments. 
     */
    public void addTrailingComment(String s) {
        addTrailingComment(Comment.create(s));
        commentsMapped();
    }

    /**
     * Add the specified comment to the list of trailing comments. 
     */
    public void addTrailingComment(Comment c) {
        trailingComments.add(c);
        commentsMapped();
    }

    /**
     * Add a list of comments to the list of preceding comments.
     */
    public void addTrailingComments(List<Comment> comments) {
        trailingComments.addAll(comments);
        commentsMapped();
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
    
    public boolean areCommentsMapped() {
        return commentsMapped;
    }
    
    public void commentsMapped() {
        commentsMapped = true;
    }
}
