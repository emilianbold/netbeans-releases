/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007-2012 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;
import static org.netbeans.modules.java.source.save.PositionEstimator.NOPOS;

/**
 * Attaches comments to trees.
 *
 * @author Pavel Flaska, Rastislav Komara, Jan Lahoda
 */
class AssignComments extends TreeScanner<Void, Void> {
    
    private final CompilationInfo info;
    private final CompilationUnitTree unit;
    private final Tree commentMapTarget;
    private final TokenSequence<JavaTokenId> seq;
    private final CommentHandlerService commentService;
    private final SourcePositions positions;
    private int tokenIndexAlreadyAdded = -1;
    private boolean mapComments;
    private Tree parent = null;

    public AssignComments(final CompilationInfo info,
            final Tree commentMapTarget,
            final TokenSequence<JavaTokenId> seq,
            final CompilationUnitTree cut) {
        this(info, commentMapTarget, seq, cut, info.getTrees().getSourcePositions());
    }

    public AssignComments(final CompilationInfo info,
            final Tree commentMapTarget,
            final TokenSequence<JavaTokenId> seq,
            final SourcePositions positions) {
        this(info, commentMapTarget, seq, info.getCompilationUnit(), positions);
    }

    private AssignComments(final CompilationInfo info,
            final Tree commentMapTarget,
            final TokenSequence<JavaTokenId> seq,
            final CompilationUnitTree cut,
            final SourcePositions positions) {
        this.info = info;
        this.unit = cut;
        this.seq = seq;
        this.commentMapTarget = commentMapTarget;
        this.commentService = CommentHandlerService.instance(info.impl.getJavacTask().getContext());
        this.positions = positions;
    }

    @Override
    public Void scan(Tree tree, Void p) {
        if (tree == null) {
            return null;
        } else {
            //XXX:
            boolean oldMapComments = mapComments;
            try {
                mapComments |= tree == commentMapTarget;
                if ((commentMapTarget != null) && info.getTreeUtilities().isSynthetic(new TreePath(new TreePath(unit), tree)))
                    return null;
                if (commentMapTarget != null) {
                    mapComments2(tree, true, false);
                }
                Tree oldParent = parent;
                try {
                    parent = tree;
                    super.scan(tree, p);
                } finally {
                    parent = oldParent;
                }
                if (commentMapTarget != null) {
                    mapComments2(tree, false, tree.getKind() != Tree.Kind.BLOCK || parent == null || parent.getKind() != Tree.Kind.METHOD);
                    if (mapComments) {
                        ((CommentSetImpl) createCommentSet(commentService, tree)).commentsMapped();
                    }
                }
                return null;
            } finally {
                mapComments = oldMapComments;
            }
        }
    }
        
    private void mapComments2(Tree tree, boolean preceding, boolean trailing) {
        if (((JCTree) tree).pos <= 0) {
            return;
        }
        collect(tree, preceding, trailing);
    }
    
    /*
        Implementation of new gathering algorithm based on comment weighting by natural (my) aligning of comments to statements.
     */
    
    private static Logger log = Logger.getLogger(AssignComments.class.getName());
    
    private void collect(Tree tree, boolean preceding, boolean trailing) {
        if (isEvil(tree)) {
            return;
        }
        if (preceding) {
            int pos = findInterestingStart((JCTree) tree);
            seq.move(pos);
            lookForPreceedings(seq, tree);
            if (tree instanceof BlockTree) {
                BlockTree blockTree = (BlockTree) tree;
                if (blockTree.getStatements().isEmpty()) {
                    lookWithinEmptyBlock(seq, blockTree);
                }
            }
        } else {
            lookForInline(seq, tree);
            if (trailing) {
                lookForTrailing(seq, tree);
            }
        }

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "T: " + tree + "\nC: " + commentService.getComments(tree));
        }
    }

    private void lookForInline(TokenSequence<JavaTokenId> seq, Tree tree) {
        seq.move((int) positions.getEndPosition(unit, tree));
        CommentsCollection result = new CommentsCollection();
        while (seq.moveNext()) {
            if (seq.token().id() == JavaTokenId.WHITESPACE) {
                if (numberOfNL(seq.token()) > 0) {
                    break;
                }
            } else if (isComment(seq.token().id())) {
                if (seq.index() > tokenIndexAlreadyAdded)
                    result.add(seq.token());
                tokenIndexAlreadyAdded = seq.index();
                if (seq.token().id() == JavaTokenId.LINE_COMMENT) {
                    break;
                }
            } else {
                break;
            }
        }
        if (!result.isEmpty()) {
            CommentSet.RelativePosition position = CommentSet.RelativePosition.INLINE;
            attachComments(tree, result, position);
        }
    }

    private void attachComments(Tree tree, CommentsCollection result, CommentSet.RelativePosition position) {
        if (!mapComments) return;
        
        CommentSetImpl cs = commentService.getComments(tree);
        for (Token<JavaTokenId> token : result) {
            attachComment(position, cs, token);
        }
    }

    private boolean isEvil(Tree tree) {
        Tree.Kind kind = tree.getKind();
        switch (kind) {
            case COMPILATION_UNIT:
                CompilationUnitTree cut = (CompilationUnitTree) tree;
                return cut.getPackageName() == null;
            case MODIFIERS:
            case PRIMITIVE_TYPE:
                return true;
            default: return false;
        }
    }

    private void lookForTrailing(TokenSequence<JavaTokenId> seq, Tree tree) {
        //TODO: [RKo] This does not work correctly... need improvemetns.
        seq.move((int) positions.getEndPosition(unit, tree));
        List<TrailingCommentsDataHolder> comments = new LinkedList<TrailingCommentsDataHolder>();
        int maxLines = 0;
        int newlines = 0;
        int lastIndex = -1;
        while (seq.moveNext()) {
            if (lastIndex == (-1)) lastIndex = seq.index();
            Token<JavaTokenId> t = seq.token();
            if (t.id() == JavaTokenId.WHITESPACE) {
                newlines += numberOfNL(t);
            } else if (isComment(t.id())) {
                if (seq.index() > tokenIndexAlreadyAdded)
                    comments.add(new TrailingCommentsDataHolder(newlines, t, lastIndex));
                maxLines = Math.max(maxLines, newlines);
                if (t.id() == JavaTokenId.LINE_COMMENT) {
                    newlines = 1;
                } else {
                    newlines = 0;
                }
                lastIndex = -1;
            } else {
                if (t.id() == JavaTokenId.RBRACE || t.id() == JavaTokenId.ELSE) maxLines = Integer.MAX_VALUE;
                break;
            }

        }

        int index = seq.index() - 1;

        maxLines = Math.max(maxLines, newlines);

        for (TrailingCommentsDataHolder h : comments) {
            if (h.newlines < maxLines) {
                attachComments(Collections.singleton(h.comment), tree, commentService, CommentSet.RelativePosition.TRAILING);
            } else {
                index = h.index - 1;
                break;
            }
        }
        
        tokenIndexAlreadyAdded = index;
    }

    private static final class TrailingCommentsDataHolder {
        private final int newlines;
        private final Token<JavaTokenId> comment;
        private final int index;
        public TrailingCommentsDataHolder(int newlines, Token<JavaTokenId> comment, int index) {
            this.newlines = newlines;
            this.comment = comment;
            this.index = index;
        }
    }

    private void lookWithinEmptyBlock(TokenSequence<JavaTokenId> seq, BlockTree tree) {
        // moving into opening brace.
        if (moveTo(seq, JavaTokenId.LBRACE, true)) {
            int idx = -1;
            if (seq.moveNext()) {
                JavaTokenId id = seq.token().id();
                idx = seq.index();
                if (id == JavaTokenId.WHITESPACE || isComment(id)) {
                    CommentsCollection cc = getCommentsCollection(seq, Integer.MAX_VALUE);
                    attachComments(tree, cc, CommentSet.RelativePosition.INNER);
                }
            }
            if (tokenIndexAlreadyAdded < idx) {
                tokenIndexAlreadyAdded = idx;
            }
        } else {
            int end = (int) positions.getEndPosition(unit, tree);
            seq.move(end); seq.moveNext();
        }
    }

    /**
     * Moves <code>seq</code> to first occurence of specified <code>toToken</code> if specified direction.
     * @param seq sequence of tokens
     * @param toToken token to stop on.
     * @param forward move forward if true, backward otherwise
     * @return true if token has been reached.
     */
    private boolean moveTo(TokenSequence<JavaTokenId> seq, JavaTokenId toToken, boolean forward) {
        if (seq.token() == null) return false;//seq.move(<end-of-stream>) might have been called - see test224577.
        do {
            if (toToken == seq.token().id()) {
                return true;
            } 
        } while (forward ? seq.moveNext() : seq.movePrevious());
        return false;
    }

    private void lookForPreceedings(TokenSequence<JavaTokenId> seq, Tree tree) {
        int reset = ((JCTree) tree).pos;
        CommentsCollection cc = null;
        while (seq.moveNext() && seq.offset() < reset) {
            JavaTokenId id = seq.token().id();
            if (isComment(id)) {
                if (cc == null) {
                    cc = getCommentsCollection(seq, Integer.MAX_VALUE);
                } else {
                    cc.merge(getCommentsCollection(seq, Integer.MAX_VALUE));
                }
            }
        }
        attachComments(cc, tree, commentService, CommentSet.RelativePosition.PRECEDING);
        seq.move(reset);
        seq.moveNext();
        tokenIndexAlreadyAdded = seq.index();
    }

    /**
     * Looking for position where to start looking up for preceeding commnets.
     * @param tree tree to examine.
     * @return position where to start 
     */
    private int findInterestingStart(JCTree tree) {
        int pos = (int) positions.getStartPosition(unit, tree);
        if (pos <= 0) return 0;
        seq.move(pos);
        while (seq.movePrevious() && tokenIndexAlreadyAdded < seq.index()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case JAVADOC_COMMENT:
                case BLOCK_COMMENT:
                    continue;
                case LBRACE:
                    /*
                        we are reaching parent tree element. This tree has no siblings or is first child. We have no 
                        interest in number of NL before this kind of comments. This comments are always considered 
                        as preceeding to tree.
                    */
                    return seq.offset() + seq.token().length();
                default:
                    return seq.offset() + seq.token().length();
            }
        }
        return seq.offset() + (tokenIndexAlreadyAdded >= seq.index() ? seq.token().length() : 0);
    }

    private void attachComments(Iterable<? extends Token<JavaTokenId>> foundComments, Tree tree, CommentHandler ch, CommentSet.RelativePosition positioning) {
        if (foundComments == null || !foundComments.iterator().hasNext() || !mapComments) return;
        CommentSetImpl set = (CommentSetImpl) createCommentSet(ch, tree);
        if (set.areCommentsMapped()) return ;
        for (Token<JavaTokenId> comment : foundComments) {
            attachComment(positioning, set, comment);
        }
    }

    private void attachComment(CommentSet.RelativePosition positioning, CommentSet set, Token<JavaTokenId> comment) {
        Comment c = Comment.create(getStyle(comment.id()), comment.offset(null),
                getEndPos(comment), NOPOS, getText(comment));
        set.addComment(positioning, c);        
    }

    private String getText(Token<JavaTokenId> comment) {
        return String.valueOf(comment.text());
    }

    private int getEndPos(Token<JavaTokenId> comment) {
        return comment.offset(null) + comment.length();
    }

    private Comment.Style getStyle(JavaTokenId id) {
        switch (id) {
            case JAVADOC_COMMENT:
                return Comment.Style.JAVADOC;
            case LINE_COMMENT:
                return Comment.Style.LINE;
            case BLOCK_COMMENT:
                return Comment.Style.BLOCK;
            default:
                return Comment.Style.WHITESPACE;
        }
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

    /**
     * Note - Because of {@link Comment.Style#WHITESPACE}, whitespaces are also
     * recorded
     */
    private CommentsCollection getCommentsCollection(TokenSequence<JavaTokenId> ts, int maxTension) {
        CommentsCollection result = new CommentsCollection();
        Token<JavaTokenId> t = ts.token();
        result.add(t);
        boolean isLC = t.id() == JavaTokenId.LINE_COMMENT;
        int lastCommentIndex = ts.index();
        int start = ts.offset();
        int end = ts.offset() + ts.token().length();
        while (ts.moveNext()) {
            if (ts.index() < tokenIndexAlreadyAdded) continue;
            t = ts.token();
            if (isComment(t.id())) {
                result.add(t);
                start = Math.min(ts.offset(), start);
                end = Math.max(ts.offset() + t.length(), end);
                isLC = t.id() == JavaTokenId.LINE_COMMENT;
                lastCommentIndex = ts.index();                
            } else if (t.id() == JavaTokenId.WHITESPACE) {
                if ((numberOfNL(t) + (isLC ? 1 : 0)) > maxTension) {
                    break;
                }
            } else {
                break;                
            }
        }
        ts.moveIndex(lastCommentIndex);
        ts.moveNext();
        tokenIndexAlreadyAdded = ts.index();
        result.setBounds(new int[]{start, end});
//        System.out.println("tokenIndexAlreadyAdded = " + tokenIndexAlreadyAdded);
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

    private static class CommentsCollection implements Iterable<Token<JavaTokenId>> {
        private final int[] bounds = {NOPOS, NOPOS};
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

        public void merge(CommentsCollection cc) {
            comments.addAll(cc.comments);
            this.bounds[0] = Math.min(this.bounds[0], cc.bounds[0]);
            this.bounds[1] = Math.max(this.bounds[1], cc.bounds[1]);
        }
    }
}
