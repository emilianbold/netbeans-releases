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

package org.netbeans.modules.java.source.builder;

import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CommentHandler;
import org.netbeans.api.java.source.CommentSet;
import org.netbeans.modules.java.source.engine.ASTModel;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;

import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeScanner;
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
    protected ASTModel model;
    
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

    /**
     * Associates comment texts with their respective trees.
     */
    public void mapComments(final CompilationUnitTree compilationUnit, CharSequence sbuf, BufferRunQueue runs) {
        final JCCompilationUnit toplevel = (JCCompilationUnit)compilationUnit;
        final SortedMap<Integer, JCTree> positions = getPositions(toplevel);
        if (positions.size() == 0)
            return;
        JCTree lastTree = null;
        LineColMapper lineCol = new LineColMapper() {
            public int getColumn(int offset) {
                return toplevel.lineMap.getColumnNumber(offset);
            }
            public int getLine(int offset) {
                return toplevel.lineMap.getLineNumber(offset);
            }
        };
        
        // handle TopLevel comments separately
        CommentSetImpl topComments = (CommentSetImpl)getComments(toplevel);
        for (Iterator<BufferRun> iter = runs.iterator(); iter.hasNext();) {
            BufferRun run = iter.next();
            if (run.getKind() == TOKEN)
                break;
            else if (run.getKind() == COMMENT) {
                CommentRun cr = (CommentRun)run;
                if (cr.getStyle() == JAVADOC)
                    break;  // Javadoc comments belong to classdef
                Comment c = cr.toComment(cr.getString(sbuf), lineCol.getColumn(cr.getStart()));
                topComments.addPrecedingComment(c);
            }
        }

        for (int pos : positions.keySet()) {
            JCTree tree = positions.get(pos);
            int start = runs.findRunStartingAt(model.getStartPos(tree));
            if (start == (-1)) //see CommentHandlerServiceTest.testBrokenFile1
                continue;
            mapComments(start, tree, lastTree, sbuf, runs, lineCol);
            int endPos = model.getEndPos(tree, toplevel);
            int tail = runs.findRunEndingWith(endPos);
            if (tail == (-1))
                continue;
            BufferRun br = runs.get(tail);
            if (br.getKind() == TOKEN && ((TokenRun)br).getToken() == RBRACE)
                mapTrailingBlockComments(start, tail, tree, getComments(tree), sbuf, runs, lineCol);
            lastTree = tree;
        }

        // append any final comments, such as revision history
        int tail = runs.findRunEndingWith(model.getEndPos(toplevel, toplevel)) + 1;
        while (tail < runs.size()) {
            if (runs.get(tail).getKind() == COMMENT) {
                CommentRun cr = (CommentRun)runs.get(tail);
                Comment c = cr.toComment(cr.getString(sbuf), lineCol.getColumn(cr.getStart()));
                topComments.addTrailingComment(c);
            }
            tail++;
        }
    }
    
    private void mapTrailingBlockComments(int begin, int end, JCTree tree, CommentSet comments, CharSequence sbuf, 
                                          BufferRunQueue runs, LineColMapper lineCol) {
        while (--end > begin && runs.get(end).getKind() != TOKEN) {
            BufferRun br = runs.get(end);
            if (br.getKind() == COMMENT) {
                CommentRun cr = (CommentRun)br;
                Comment c = cr.toComment(cr.getString(sbuf), lineCol.getColumn(cr.getStart()));
                comments.addTrailingComment(c);
            }
        }
    }
    
    private interface LineColMapper {
        int getColumn(int offset);
        int getLine(int offset);
    }
    
    private void mapComments(int startRun, JCTree tree, JCTree lastTree, CharSequence sbuf, BufferRunQueue runs, LineColMapper lineCol) {
        assert startRun != -1;
        int i = startRun - 1;
        int lastEOL = -1;
        while (i >= 0 && runs.get(i).getKind() != TOKEN) {
            if (runs.get(i).getKind() == LINE_ENDING)
                lastEOL = lineCol.getLine(runs.get(i).getStart());
            i--;
        }

        while (++i < startRun) {
            if (runs.get(i).getKind() == COMMENT) {
                CommentRun cr = (CommentRun)runs.get(i);
                Comment c = cr.toComment(cr.getString(sbuf), lineCol.getColumn(cr.getStart()));
                if (lastTree != null && lineCol.getLine(cr.getEnd()) == lastEOL) {
                    // Add comments on the same line as the previous tree to that tree.
                    CommentSetImpl cs = (CommentSetImpl)getComments(lastTree);
                    cs.addTrailingComment(c);
                } else 
                    addComment(tree, c);
            }
        }
    }
    
    /**
     * Move the comments stored in a comment set to the top CommentSet as trailing
     * comments.
     */
    private CommentSetImpl flushTrailingComments(CommentSetImpl comments, CommentSetImpl lastComments) {
        List<Comment> cmts = comments.getPrecedingComments();
        if (cmts.isEmpty())
            return comments;
       for (Comment c : cmts)
            lastComments.addTrailingComment(c);
        return new CommentSetImpl(); // equivalent to resetting it
    }

    /** Create map of trees, indexed by position.  Because a depth-first scan
     * is used, parent trees which have the same start position as child
     * trees overwrite their children.  For example, the start position of
     * "public void foo {...}" returns a MethodDef, not Modifiers.  
     *
     * Note:  the TopLevel is handled separately, since a TopLevel without
     * a package or import statements has the same start position as its
     * ClassDef.
     */
    private SortedMap<Integer, JCTree> getPositions(JCCompilationUnit tree) {
        final SortedMap<Integer, JCTree> positions = new TreeMap<Integer, JCTree>();
        tree.accept(new TreeScanner() {
            @Override
            public void scan(JCTree tree) {
                if (tree != null) {
                    // scan children first
                    tree.accept(this);
                    int pos = model.getStartPos(tree);
                    if (pos != com.sun.tools.javac.util.Position.NOPOS)
                        positions.put(pos, tree);
                }
            }
            @Override
            public void visitTypeIdent(JCPrimitiveTypeTree tree) {
                super.visitTypeIdent(tree);
            }
            @Override
            public void visitMethodDef(JCMethodDecl tree) {
                super.visitMethodDef(tree);
            }
        });
        return positions;
    }
}
