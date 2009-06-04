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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.parsing.SourceFileObject;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.org">RKo</a>)
 * @todo documentation
 */
public class CommentCollector {

    public void collect(WorkingCopy copy) throws IOException {
        JCTree.JCCompilationUnit unit = (JCTree.JCCompilationUnit) copy.getCompilationUnit();
        TokenSequence<JavaTokenId> seq = ((SourceFileObject) unit.getSourceFile()).getTokenHierarchy().tokenSequence(JavaTokenId.language());        
        collect(seq, copy);
    }

    @SuppressWarnings({"MethodWithMoreThanThreeNegations"})
    public void collect(TokenSequence<JavaTokenId> ts, CompilationInfo ci) {
        CommentHandler ch = CommentHandlerService.instance(ci.impl.getJavacTask().getContext());
        ts.move(0);
        if (!ts.moveNext()) {
            return;
        }
        TreeUtilities tu = new TreeUtilities(ci);
        Tree lastTree = ci.getCompilationUnit();
        CommentsCollection foundComments = new CommentsCollection();
        int newlines = 0;
        while (ts.moveNext()) {
            Token<JavaTokenId> t = ts.token();
            if (t.id() == JavaTokenId.WHITESPACE) {                 
                newlines += numberOfNL(t);
            } else if (isComment(t.id())) {
                foundComments = getCommentsCollection(ts, newlines);
                newlines = 0;
            } else {
                Tree tree = getTree(tu, ts);
                if (tree != null && !tree.equals(lastTree)) {
                    lastTree = tree;
                    if (!foundComments.isEmpty()) {
                        int[] bounds = foundComments.getBounds();
                        if (belongsTo(bounds[0], bounds[1], ts) >= 0) {
                            attachPrecedingComments(createCommentSet(ch, tree), foundComments);
                        } else {
                            attachTrailingComments(createCommentSet(ch, lastTree), foundComments);
                        }
                    }
                }
            }

        }
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    private int belongsTo(int startPos, int endPos, TokenSequence<JavaTokenId> ts) {
        int result = 0;
        ts.move(startPos); 
        while (ts.movePrevious()) {
            if (ts.token().id() == JavaTokenId.WHITESPACE) {
                result -= numberOfNL(ts.token());
            } else {
                break;
            }
        }
        ts.move(endPos);
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.WHITESPACE) {
                result += numberOfNL(ts.token());
            } else {
                break;
            }
        }
        return result;
    }
    
    private void attachTrailingComments(CommentSet previous, Iterable<Token<JavaTokenId>> foundComments) {
        for (Token<JavaTokenId> comment : foundComments) {
            previous.addTrailingComment(comment.text().toString());
        }
    }

    private void attachPrecedingComments(CommentSet cs, Iterable<Token<JavaTokenId>> foundComments) {
        for (Token<JavaTokenId> comment : foundComments) {
            cs.addPrecedingComment(comment.text().toString());
        }
    }


    private Tree getTree(TreeUtilities tu, TokenSequence<JavaTokenId> ts) {
        int start = ts.offset();
        TreePath path = tu.pathFor(start);
        if (path != null) {
            return path.getLeaf();
        }
        return null;
    }

    private int numberOfNL(Token<JavaTokenId> t) {
        int count = 0;
        CharSequence charSequence = t.text();
        for (int i = 0; i < charSequence.length(); i++) {
            char a = charSequence.charAt(i);
            if ('\n' == a) {
                count++;
            }
        }
        return count;
    }

    private CommentsCollection getCommentsCollection(TokenSequence<JavaTokenId> ts, int maxTension) {
        CommentsCollection result = new CommentsCollection();
        Token<JavaTokenId> t = ts.token();
        result.add(t);
        int start = ts.offset();
        int end = ts.offset() + ts.token().length();
        while (ts.moveNext()) {
            t = ts.token();
            if (isComment(t.id())) {
                result.add(t);
                start = Math.min(ts.offset(),  start);
                end = Math.max(t.length(), end);
            } else if (t.id() == JavaTokenId.WHITESPACE) {
                if (numberOfNL(t) > maxTension) {
                    ts.movePrevious();
                    break;
                }
            }
        }
        result.setBounds(new int[]{start, end});
        return result;
    }

    private CommentSet createCommentSet(CommentHandler ch, Tree lastTree) {
        return ch.getComments(lastTree);
    }

    private boolean isComment(JavaTokenId tid) {
        switch (tid) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case JAVADOC_COMMENT:
                return true;
            default:
                return false;
        }
    }
    
    private static class CommentsCollection  implements Iterable<Token<JavaTokenId>> {
        private final int[] bounds = {-1,-1};
        private final List<Token<JavaTokenId>> comments = new LinkedList<Token<JavaTokenId>>();

        void add(Token<JavaTokenId> comment) {
            comments.add(comment);
        }
        
        boolean isEmpty() {
            return comments.isEmpty();
        }
        
        public Iterator<Token<JavaTokenId>> iterator() {
            return comments.iterator();
        }
        
        void setBounds(int[] bounds) {
            this.bounds[0] = bounds[0];
            this.bounds[1] = bounds[1];
        }

        public int[] getBounds() {
            return bounds.clone();
        }
    }

}
