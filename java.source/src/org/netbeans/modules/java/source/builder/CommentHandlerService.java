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
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;

import com.sun.source.tree.Tree;

import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;

import java.util.*;

import static org.netbeans.modules.java.source.builder.BufferRun.Kind.*;
import static org.netbeans.api.java.source.Comment.Style.*;
import static com.sun.tools.javac.parser.Token.*;

/**
 * Generate Comments during scanning.
 */
public class CommentHandlerService implements CommentHandler {
    private static final Context.Key<CommentHandlerService> commentHandlerKey = 
        new Context.Key<CommentHandlerService>();
    
    /** Get the CommentMaker instance for this context. */
    public static CommentHandlerService instance(Context context) {
	CommentHandlerService instance = context.get(commentHandlerKey);
	if (instance == null) {
	    instance = new CommentHandlerService(context);
            setCommentHandler(context, instance);
        }
	return instance;
    }
    
    /**
     * Called from reattributor.
     */
    public static void setCommentHandler(Context context, CommentHandlerService instance) {
        assert context.get(commentHandlerKey) == null;
        context.put(commentHandlerKey, instance);
    }

    private Map<Tree, CommentSetImpl> map = new WeakHashMap<Tree, CommentSetImpl>();
    protected ASTService model;
    
    private CommentHandlerService(Context context) {
        model = ASTService.instance(context);
    }
    
    public boolean hasComments(Tree tree) {
        synchronized (map) {
            return map.containsKey(tree);
        }
    }
    
    public CommentSet getComments(Tree tree) {
        synchronized (map) {
            CommentSetImpl cs = map.get(tree);
            if (cs == null) {
                cs = new CommentSetImpl();
                map.put(tree, cs);
            }
            return cs;
        }
    }

    /**
     * Copies preceding and trailing comments from one tree to another,
     * appending the new entries to the existing comment lists.
     */
    public void copyComments(Tree fromTree, Tree toTree) {
        synchronized (map) {
            CommentSetImpl from = map.get(fromTree);
            if (from != null) {
                CommentSetImpl to = map.get(toTree);
                if (to == null) {
                    to = (CommentSetImpl)from.clone();
                    to.setTree(toTree);
                    map.put(toTree, to);
                } 
                else {
                    to.addPrecedingComments(from.getPrecedingComments());
                    to.addTrailingComments(from.getTrailingComments());
                }
            }
        }
    }
    
    /**
     * Add a comment to a tree's comment set.  If a comment set
     * for the tree doesn't exist, one will be created.
     */
    public void addComment(Tree tree, Comment c) {
        synchronized (map) {
            CommentSetImpl set = map.get(tree);
            if (set == null) {
                set = new CommentSetImpl();
                map.put(tree, set);
            }
            set.addPrecedingComment(c);
        }
    }
}
