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
import org.netbeans.modules.java.source.query.CommentHandler;

import com.sun.source.tree.Tree;

import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;

import java.util.*;


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

    private final Map<Tree, CommentSetImpl> map = new HashMap<Tree, CommentSetImpl>();
    
    private CommentHandlerService(Context context) {
    }
    
    public boolean hasComments(Tree tree) {
        synchronized (map) {
            return map.containsKey(tree);
        }
    }
    
    public CommentSetImpl getComments(Tree tree) {
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
        if (fromTree == toTree) {
            return;
        }
        synchronized (map) {
            CommentSetImpl from = map.get(fromTree);
            if (from != null) {
                CommentSetImpl to = map.get(toTree);
                if (to == null) {
                    to = (CommentSetImpl)from.clone();
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


    public String toString() {
        return "CommentHandlerService[" +
                "map=" + map +
                ']';
    }
}
