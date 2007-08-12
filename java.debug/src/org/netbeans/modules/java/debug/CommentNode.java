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

import org.netbeans.api.java.source.Comment;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class CommentNode extends AbstractNode implements OffsetProvider {

    private Comment comment;

    /** Creates a new instance of CommentNode */
    public CommentNode(Comment comment) {
        super(Children.LEAF);
        this.comment = comment;
        setDisplayName(NbBundle.getMessage(CommentNode.class, "NM_Comment"));
    }

    public int getStart() {
        return comment.pos();
    }

    public int getEnd() {
        return comment.endPos();
    }

}
