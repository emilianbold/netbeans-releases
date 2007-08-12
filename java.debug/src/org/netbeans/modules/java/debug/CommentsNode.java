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
package org.netbeans.modules.java.debug;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.Comment;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jan Lahoda
 */
public class CommentsNode extends AbstractNode implements OffsetProvider {

    private List<Comment> comments;

    /** Creates a new instance of CommentNode */
    public CommentsNode(String displayName, List<Comment> comments) {
        super(new ChildrenImpl(comments));
        this.comments = comments;
        setDisplayName(displayName);
    }

    public int getStart() {
        int start = Integer.MAX_VALUE;
        
        for (Comment c : comments) {
            if (start > c.pos()) {
                start = c.pos();
            }
        }
        
        return start == Integer.MAX_VALUE ? (-1) : start;
    }

    public int getEnd() {
        int end = -1;
        
        for (Comment c : comments) {
            if (end < c.endPos()) {
                end = c.endPos();
            }
        }
        
        return end;
    }

    private static final class ChildrenImpl extends Children.Keys {

        private List<Comment> comments;
        
        public ChildrenImpl(List<Comment> comments) {
            this.comments = comments;
        }
        
        public void addNotify() {
            setKeys(comments);
        }
        
        public void removeNotify() {
            setKeys(Collections.emptyList());
        }
        
        protected Node[] createNodes(Object key) {
            return new Node[] {new CommentNode((Comment) key)};
        }
        
    }
    
}
