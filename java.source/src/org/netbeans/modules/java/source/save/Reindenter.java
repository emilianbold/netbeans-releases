/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.main.JavaCompiler;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.Context.Region;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Dusan Balek
 */
public class Reindenter implements IndentTask {

    private final Context context;
    private CodeStyle cs;
    private TokenSequence<JavaTokenId> ts;
    private String text;
    private CompilationUnitTree cut;
    private SourcePositions sp;
    private Map<Integer, Integer> newIndents;
    private int currentEmbeddingStartOffset;
    private Embedding currentEmbedding;

    private Reindenter(Context context) {
        this.context = context;
    }

    @Override
    public void reindent() throws BadLocationException {
        ts = null;
        currentEmbedding = null;        
        newIndents = new HashMap<Integer, Integer>();
        cs = CodeStyle.getDefault(context.document());
        for (Region region : context.indentRegions()) {
            if (initRegionData(region)) {
                HashSet<Integer> linesToAddStar = new HashSet<Integer>();
                LinkedList<Integer> startOffsets = getStartOffsets(region);
                for (ListIterator<Integer> it = startOffsets.listIterator(); it.hasNext();) {
                    int originalStartOffset = it.next();
                    int originalEndOffset;
                    if (it.hasNext()) {
                        originalEndOffset = it.next() - 1;
                        it.previous();
                    } else {
                        originalEndOffset = region.getEndOffset();
                    }
                    int startOffset = getEmbeddedOffset(originalStartOffset);
                    int endOffset = getEmbeddedOffset(originalEndOffset);
                    String blockCommentLine;
                    int delta = ts.move(startOffset);
                    if (((startOffset == 0 || delta > 0) && ts.moveNext() || ts.movePrevious())
                            && (ts.token().id() != JavaTokenId.BLOCK_COMMENT || ts.embedded() == null)) {
                        if (cs.addLeadingStarInComment()
                                && (ts.token().id() == JavaTokenId.BLOCK_COMMENT && cs.enableBlockCommentFormatting()
                                || ts.token().id() == JavaTokenId.JAVADOC_COMMENT && cs.enableJavadocFormatting())) {
                            blockCommentLine = ts.token().text().toString();
                            if (delta > 0) {
                                int idx = blockCommentLine.indexOf('\n', delta); //NOI18N
                                blockCommentLine = (idx < 0 ? blockCommentLine.substring(delta) : blockCommentLine.substring(delta, idx)).trim();
                                int off = getOriginalOffset(ts.offset() + delta - 1);
                                int prevLineStartOffset = context.lineStartOffset(off < 0 ? originalStartOffset : off);
                                Integer prevLineIndent = newIndents.get(prevLineStartOffset);
                                newIndents.put(originalStartOffset, (prevLineIndent != null ? prevLineIndent : context.lineIndent(prevLineStartOffset)) + (prevLineStartOffset > getOriginalOffset(ts.offset()) ? 0 : 1)); //NOI18N
                            } else {
                                int idx = blockCommentLine.lastIndexOf('\n'); //NOI18N
                                if (idx > 0) {
                                    blockCommentLine = blockCommentLine.substring(idx).trim();
                                }
                                newIndents.put(originalStartOffset, getNewIndent(startOffset, endOffset) + 1);
                            }
                            if (!blockCommentLine.startsWith("*")) { //NOI18N
                                linesToAddStar.add(originalStartOffset);
                            }
                        } else {
                            if (delta == 0 && ts.moveNext() && ts.token().id() == JavaTokenId.LINE_COMMENT) {
                                newIndents.put(originalStartOffset, 0);
                            } else {
                                newIndents.put(originalStartOffset, getNewIndent(startOffset, endOffset));
                            }
                        }
                    }
                }
                while (!startOffsets.isEmpty()) {
                    int startOffset = startOffsets.removeLast();
                    Integer newIndent = newIndents.get(startOffset);
                    if (linesToAddStar.contains(startOffset)) {
                        context.modifyIndent(startOffset, 0);
                        context.document().insertString(startOffset, "* ", null); //NOI18N
                    }
                    if (newIndent != null) {
                        context.modifyIndent(startOffset, newIndent);
                    }
                    if (!startOffsets.isEmpty()) {
                        char c;
                        int len = 0;
                        while ((c = text.charAt(startOffset - 2 - len)) != '\n' && Character.isWhitespace(c)) { //NOI18N
                            len++;
                        }
                        if (len > 0) {
                            context.document().remove(startOffset - 1 - len, len);
                        }
                    }
                }
            }
        }
    }

    @Override
    public ExtraLock indentLock() {
        return JavacParser.MIME_TYPE.equals(context.mimePath()) ? null : new ExtraLock() {
            @Override
            public void lock() {
                Utilities.acquireParserLock();
            }
            @Override
            public void unlock() {
                Utilities.releaseParserLock();
            }
        };
    }
    
    private boolean initRegionData(final Region region) {
        currentEmbeddingStartOffset = 0;
        if (ts == null || (currentEmbedding != null
                && !(currentEmbedding.containsOriginalOffset(region.getStartOffset())
                && currentEmbedding.containsOriginalOffset(region.getEndOffset())))) {
            if (JavacParser.MIME_TYPE.equals(context.mimePath())) {
                ts = TokenHierarchy.get(context.document()).tokenSequence(JavaTokenId.language());
                if (ts == null) {
                    return false;
                }
                ClassLoader origCL = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(Reindenter.class.getClassLoader());
                    JavacTaskImpl javacTask = (JavacTaskImpl)ToolProvider.getSystemJavaCompiler().getTask(null, null, new DiagnosticListener<JavaFileObject>() {
                        @Override
                        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                        }
                    }, Collections.singletonList("-proc:none"), null, Collections.<JavaFileObject>emptySet()); //NOI18N
                    com.sun.tools.javac.util.Context ctx = javacTask.getContext();
                    JavaCompiler.instance(ctx).genEndPos = true;
                    text = context.document().getText(0, context.document().getLength());
                    cut = javacTask.parse(FileObjects.memoryFileObject("", "", text)).iterator().next(); //NOI18N
                    sp = JavacTrees.instance(ctx).getSourcePositions();
                } catch (Exception ex) {
                    return false;
                } finally {
                    Thread.currentThread().setContextClassLoader(origCL);
                }
            } else {
                Source source = Source.create(context.document());
                if (source == null) {
                    return false;
                }
                TokenSequence<?> tseq = TokenHierarchy.get(context.document()).tokenSequence();
                while(tseq != null && (region.getStartOffset() == 0 || tseq.moveNext())) {
                    tseq.move(region.getStartOffset());
                    if (tseq.language() == JavaTokenId.language() || !(tseq.moveNext() || tseq.movePrevious())) {
                        break;
                    }
                    currentEmbeddingStartOffset = tseq.offset();
                    tseq = tseq.embedded();
                }
                try {
                    ParserManager.parse(Collections.singletonList(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Parser.Result result = findEmbeddedJava(resultIterator, region);
                            if (result != null) {
                                CompilationController controller = CompilationController.get(result);
                                controller.toPhase(JavaSource.Phase.PARSED);
                                ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                                text = controller.getText();
                                cut = controller.getCompilationUnit();
                                sp = controller.getTrees().getSourcePositions();
                            }
                        }
                    });
                    if (ts == null) {
                        return false;
                    }
                } catch (ParseException pe) {
                    return false;
                }
            }
        }
        return true;
    }

    private Parser.Result findEmbeddedJava(final ResultIterator theMess, final Region region) throws ParseException {
        final Collection<Embedding> todo = new LinkedList<Embedding>();
        //BFS should perform better than DFS in this dark.
        for (Embedding embedding : theMess.getEmbeddings()) {
            if (JavacParser.MIME_TYPE.equals(embedding.getMimeType())
                    && embedding.containsOriginalOffset(region.getStartOffset())
                    && embedding.containsOriginalOffset(region.getEndOffset())) {
                return theMess.getResultIterator(currentEmbedding = embedding).getParserResult();
            } else {
                todo.add(embedding);
            }
        }
        for (Embedding embedding : todo) {
            Parser.Result result = findEmbeddedJava(theMess.getResultIterator(embedding), region);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private LinkedList<Integer> getStartOffsets(Region region) throws BadLocationException {
        LinkedList<Integer> offsets = new LinkedList<Integer>();
        int offset = region.getEndOffset();
        int lso;
        while (offset > 0 && (lso = context.lineStartOffset(offset)) >= region.getStartOffset()) {
            offsets.addFirst(lso);
            offset = lso - 1;
        }
        return offsets;
    }

    private int getNewIndent(int startOffset, int endOffset) throws BadLocationException {
        LinkedList<? extends Tree> path = getPath(startOffset);
        if (path.isEmpty()) {
            return 0;
        }
        Tree last = path.getFirst();
        int lastPos = (int)sp.getStartPosition(cut, last);
        int currentIndent = getCurrentIndent(last, path);
        switch (last.getKind()) {
            case COMPILATION_UNIT:
                break;
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
                TokenSequence<JavaTokenId> token = findFirstNonWhitespaceToken(startOffset, endOffset);
                JavaTokenId nextTokenId = token != null ? token.token().id() : null;
                if (nextTokenId != null && nextTokenId == JavaTokenId.RBRACE) {
                    if (isLeftBraceOnNewLine(lastPos, startOffset)) {
                        switch (cs.getClassDeclBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                    }
                } else {
                    Tree t = null;
                    for (Tree member : ((ClassTree)last).getMembers()) {
                        if (sp.getEndPosition(cut, member) > startOffset) {
                            break;
                        }
                        t = member;
                    }
                    if (t != null) {
                        int i = getCurrentIndent(t, path);
                        currentIndent = i < 0 ? currentIndent + (cs.indentTopLevelClassMembers() ? cs.getIndentSize() : 0) : i;
                    } else {
                        token = findFirstNonWhitespaceToken(startOffset, lastPos);
                        JavaTokenId prevTokenId = token != null ? token.token().id() : null;
                        if (prevTokenId != null) {
                            switch (prevTokenId) {
                                case LBRACE:
                                    if (path.get(1).getKind() == Kind.NEW_CLASS && isLeftBraceOnNewLine(lastPos, startOffset)) {
                                        switch (cs.getClassDeclBracePlacement()) {
                                            case SAME_LINE:
                                            case NEW_LINE:
                                                currentIndent += cs.getIndentSize();
                                                break;
                                            case NEW_LINE_HALF_INDENTED:
                                                currentIndent += (cs.getIndentSize() - cs.getIndentSize() / 2);
                                                break;
                                        }
                                    } else {
                                        currentIndent += cs.indentTopLevelClassMembers() ? cs.getIndentSize() : 0;
                                    }
                                    break;
                                case COMMA:
                                    currentIndent = getMultilineIndent(((ClassTree)last).getImplementsClause(), path, token.offset(), currentIndent, cs.alignMultilineImplements(), true);
                                    break;
                                case IDENTIFIER:
                                case GT:
                                case GTGT:
                                case GTGTGT:
                                    if (nextTokenId != null && nextTokenId == JavaTokenId.LBRACE) {
                                        switch (cs.getClassDeclBracePlacement()) {
                                            case NEW_LINE_INDENTED:
                                                currentIndent += cs.getIndentSize();
                                                break;
                                            case NEW_LINE_HALF_INDENTED:
                                                currentIndent += (cs.getIndentSize() / 2);
                                                break;
                                        }
                                    }
                                    break;
                                default:
                                    currentIndent += cs.getContinuationIndentSize();
                            }
                        }
                    }
                }
                break;
            case METHOD:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                JavaTokenId prevTokenId = token != null ? token.token().id() : null;
                if (prevTokenId != null) {
                    switch (prevTokenId) {
                        case COMMA:
                            List<? extends ExpressionTree> thrws = ((MethodTree)last).getThrows();
                            if (!thrws.isEmpty() && sp.getStartPosition(cut, thrws.get(0)) < token.offset()) {
                                currentIndent = getMultilineIndent(thrws, path, token.offset(), currentIndent, cs.alignMultilineThrows(), true);
                            } else {
                                currentIndent = getMultilineIndent(((MethodTree)last).getParameters(), path, token.offset(), currentIndent, cs.alignMultilineMethodParams(), true);
                            }
                            break;
                        case RPAREN:
                        case IDENTIFIER:
                        case GT:
                        case GTGT:
                        case GTGTGT:
                            token = findFirstNonWhitespaceToken(startOffset, endOffset);
                            if (token != null && token.token().id() == JavaTokenId.LBRACE) {
                                switch (cs.getMethodDeclBracePlacement()) {
                                    case NEW_LINE_INDENTED:
                                        currentIndent += cs.getIndentSize();
                                        break;
                                    case NEW_LINE_HALF_INDENTED:
                                        currentIndent += (cs.getIndentSize() / 2);
                                        break;
                                }
                                break;
                            }
                        default:
                            currentIndent += cs.getContinuationIndentSize();
                    }
                }
                break;
            case VARIABLE:
                Tree type = ((VariableTree)last).getType();
                if (type != null && type.getKind() != Kind.ERRONEOUS) {
                    ExpressionTree init = ((VariableTree)last).getInitializer();
                    if (init == null || init.getKind() != Kind.NEW_ARRAY
                            || (token = findFirstNonWhitespaceToken(startOffset, lastPos)) == null
                            || token.token().id() != JavaTokenId.EQ
                            || (token = findFirstNonWhitespaceToken(startOffset, endOffset)) == null
                            || token.token().id() != JavaTokenId.LBRACE) {
                        if (cs.alignMultilineAssignment()) {
                            int c = getColumn(last);
                            if (c >= 0) {
                                currentIndent = c;
                            }
                        } else {
                            currentIndent += cs.getContinuationIndentSize();
                        }
                    } else {
                        switch (cs.getOtherBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                    }
                    break;
                } else {
                    last = ((VariableTree)last).getModifiers();
                    if (last == null)
                        break;
                }
            case MODIFIERS:
                Tree t = null;
                for (Tree ann : ((ModifiersTree)last).getAnnotations()) {
                    if (sp.getEndPosition(cut, ann) > startOffset) {
                        break;
                    }
                    t = ann;
                }
                if (t == null || findFirstNonWhitespaceToken(startOffset, (int)sp.getEndPosition(cut, t)) != null) {
                    currentIndent += cs.getContinuationIndentSize();
                }
                break;
            case DO_WHILE_LOOP:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token != null && !EnumSet.of(JavaTokenId.RBRACE, JavaTokenId.SEMICOLON).contains(token.token().id())) {
                    currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.DO), lastPos, currentIndent);
                }
                break;
            case ENHANCED_FOR_LOOP:
                currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), (int)sp.getEndPosition(cut, ((EnhancedForLoopTree)last).getExpression()), currentIndent);
                break;
            case FOR_LOOP:
                LinkedList<Tree> forTrees = new LinkedList<Tree>();
                for (StatementTree st : ((ForLoopTree)last).getInitializer()) {
                    if (sp.getEndPosition(cut, st) > startOffset) {
                        break;
                    }
                    forTrees.add(st);
                }
                t = ((ForLoopTree)last).getCondition();
                if (t != null && sp.getEndPosition(cut, t) <= startOffset) {
                    forTrees.add(t);
                }
                for (ExpressionStatementTree est : ((ForLoopTree)last).getUpdate()) {
                    if (sp.getEndPosition(cut, est) > startOffset) {
                        break;
                    }
                    forTrees.add(est);
                }
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token != null && token.token().id() == JavaTokenId.SEMICOLON) {
                    currentIndent = getMultilineIndent(forTrees, path, token.offset(), currentIndent, cs.alignMultilineFor(), true);
                } else {
                    currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), forTrees.isEmpty() ? lastPos : (int)sp.getEndPosition(cut, forTrees.getLast()), currentIndent);
                }
                break;
            case IF:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                if (token == null || token.token().id() != JavaTokenId.ELSE) {
                    token = findFirstNonWhitespaceToken(startOffset, lastPos);
                    if (token != null && !EnumSet.of(JavaTokenId.RBRACE, JavaTokenId.SEMICOLON).contains(token.token().id())) {
                        currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN, JavaTokenId.ELSE), (int)sp.getEndPosition(cut, ((IfTree)last).getCondition()) - 1, currentIndent);
                    }
                }
                break;
            case SYNCHRONIZED:
                currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), (int)sp.getEndPosition(cut, ((SynchronizedTree)last).getExpression()) - 1, currentIndent);
                break;
            case TRY:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                if (token == null || !EnumSet.of(JavaTokenId.CATCH, JavaTokenId.FINALLY).contains(token.token().id())) {
                    token = findFirstNonWhitespaceToken(startOffset, lastPos);
                    if (token != null && token.token().id() != JavaTokenId.RBRACE) {
                        t = null;
                        for (Tree res : ((TryTree)last).getResources()) {
                            if (sp.getEndPosition(cut, res) > startOffset) {
                                break;
                            }
                            t = res;
                        }
                        currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.TRY, JavaTokenId.RPAREN, JavaTokenId.FINALLY), t != null ? (int)sp.getEndPosition(cut, t) : lastPos, currentIndent);
                    }
                }
                break;
            case CATCH:
                currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), lastPos, currentIndent);
                break;
            case WHILE_LOOP:
                currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), (int)sp.getEndPosition(cut, ((WhileLoopTree)last).getCondition()) - 1, currentIndent);
                break;
            case BLOCK:
                boolean isStatic = ((BlockTree)last).isStatic();
                if (isStatic) {
                    token = findFirstNonWhitespaceToken(startOffset, lastPos);
                    if (token != null && token.token().id() == JavaTokenId.STATIC && token.offset() == lastPos) {
                        switch (cs.getOtherBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                        break;
                    }
                }
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                if (nextTokenId == null || nextTokenId != JavaTokenId.RBRACE) {
                    t = null;
                    boolean isNextLabeledStatement = false;
                    Iterator<? extends StatementTree> it = ((BlockTree)last).getStatements().iterator();
                    while (it.hasNext()) {
                        StatementTree st = it.next();
                        if (sp.getEndPosition(cut, st) > startOffset) {
                            isNextLabeledStatement = st.getKind() == Kind.LABELED_STATEMENT;
                            break;
                        }
                        t = st;
                    }
                    if (isNextLabeledStatement && cs.absoluteLabelIndent()) {
                        currentIndent = 0;
                    } else if (t != null) {
                        int i = getCurrentIndent(t, path);
                        currentIndent = i < 0 ? currentIndent + cs.getIndentSize() : i;
                    } else if (isStatic) {
                        currentIndent += cs.getIndentSize();
                    } else if (isLeftBraceOnNewLine(lastPos, startOffset)) {
                        switch (path.get(1).getKind() == Kind.METHOD ? cs.getMethodDeclBracePlacement() : cs.getOtherBracePlacement()) {
                            case SAME_LINE:
                            case NEW_LINE:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() - cs.getIndentSize() / 2);
                                break;
                        }
                    } else {
                        int i = getCurrentIndent(path.get(1), path);
                        currentIndent = (i < 0 ? currentIndent : i) + cs.getIndentSize();
                    }
                    if (nextTokenId != null && nextTokenId == JavaTokenId.LBRACE) {
                        switch (cs.getOtherBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                    }
                } else if (isStatic) {
                    switch (cs.getOtherBracePlacement()) {
                        case NEW_LINE_INDENTED:
                            currentIndent += cs.getIndentSize();
                            break;
                        case NEW_LINE_HALF_INDENTED:
                            currentIndent += (cs.getIndentSize() / 2);
                            break;
                    }
                } else if (!isLeftBraceOnNewLine(lastPos, startOffset)) {
                    int i = getCurrentIndent(path.get(1), path);
                    if (i >= 0) {
                        currentIndent = i;
                    }
                }
                break;
            case SWITCH:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                if (nextTokenId != null && nextTokenId == JavaTokenId.RBRACE) {
                    if (isLeftBraceOnNewLine(lastPos, startOffset)) {
                        switch (cs.getOtherBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                    }
                } else {
                    t = null;
                    for (CaseTree ct : ((SwitchTree)last).getCases()) {
                        if (sp.getEndPosition(cut, ct) > startOffset) {
                            break;
                        }
                        t = ct;
                    }
                    if (t != null) {
                        int i = getCurrentIndent(t, path);
                        currentIndent = i < 0 ? currentIndent + (cs.indentCasesFromSwitch() ? cs.getIndentSize() : 0) : i;
                        if (nextTokenId == null || !EnumSet.of(JavaTokenId.CASE, JavaTokenId.DEFAULT).contains(nextTokenId)) {
                            token = findFirstNonWhitespaceToken(startOffset, lastPos);
                            if (token == null || token.token().id() != JavaTokenId.RBRACE) {
                                currentIndent += cs.getIndentSize();
                            }
                        }
                    } else {
                        token = findFirstNonWhitespaceToken(startOffset, lastPos);
                        if (token != null && token.token().id() == JavaTokenId.LBRACE) {
                            currentIndent += (cs.indentCasesFromSwitch() ? cs.getIndentSize() : 0);
                        } else {
                            currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), (int)sp.getEndPosition(cut, ((SwitchTree)last).getExpression()) - 1, currentIndent);
                        }
                    }
                }
                break;
            case CASE:
                t = null;
                for (StatementTree st : ((CaseTree)last).getStatements()) {
                    if (sp.getEndPosition(cut, st) > startOffset) {
                        break;
                    }
                    t = st;
                }
                if (t != null) {
                    int i = getCurrentIndent(t, path);
                    currentIndent = i < 0 ? getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.COLON), (int)sp.getEndPosition(cut, ((CaseTree)last).getExpression()), currentIndent) : i;
                } else {
                    currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.COLON), (int)sp.getEndPosition(cut, ((CaseTree)last).getExpression()), currentIndent);
                }
                break;
            case NEW_ARRAY:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                if (nextTokenId != JavaTokenId.RBRACE) {
                    token = findFirstNonWhitespaceToken(startOffset, lastPos);
                    prevTokenId = token != null ? token.token().id() : null;
                    if (prevTokenId != null) {
                        switch (prevTokenId) {
                            case LBRACE:
                                currentIndent += cs.getIndentSize();
                                break;
                            case COMMA:
                                currentIndent = getMultilineIndent(((NewArrayTree)last).getInitializers(), path, token.offset(), currentIndent, cs.alignMultilineArrayInit(), false);
                                break;
                            case RBRACKET:
                                if (nextTokenId == JavaTokenId.LBRACE) {
                                    switch (cs.getOtherBracePlacement()) {
                                        case NEW_LINE_INDENTED:
                                            currentIndent += cs.getIndentSize();
                                            break;
                                        case NEW_LINE_HALF_INDENTED:
                                            currentIndent += (cs.getIndentSize() / 2);
                                            break;
                                    }
                                    break;
                                }
                            default:
                                currentIndent += cs.getContinuationIndentSize();
                        }
                    }
                }
                break;
            case LAMBDA_EXPRESSION:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                prevTokenId = token != null ? token.token().id() : null;
                if (prevTokenId == JavaTokenId.ARROW && nextTokenId == JavaTokenId.LBRACE) {
                    switch (cs.getOtherBracePlacement()) {
                        case NEW_LINE_INDENTED:
                            currentIndent += cs.getIndentSize();
                            break;
                        case NEW_LINE_HALF_INDENTED:
                            currentIndent += (cs.getIndentSize() / 2);
                            break;
                    }
                } else {
                    currentIndent = getContinuationIndent(path, currentIndent);
                }
                break;
            case NEW_CLASS:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                prevTokenId = token != null ? token.token().id() : null;
                if (prevTokenId == JavaTokenId.RPAREN && nextTokenId == JavaTokenId.LBRACE) {
                    switch (cs.getClassDeclBracePlacement()) {
                        case NEW_LINE_INDENTED:
                            currentIndent += cs.getIndentSize();
                            break;
                        case NEW_LINE_HALF_INDENTED:
                            currentIndent += (cs.getIndentSize() / 2);
                            break;
                    }
                } else {
                    currentIndent = getContinuationIndent(path, currentIndent);
                }
                break;
            case METHOD_INVOCATION:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token != null && token.token().id() == JavaTokenId.COMMA) {
                    currentIndent = getMultilineIndent(((MethodInvocationTree)last).getArguments(), path, token.offset(), currentIndent, cs.alignMultilineCallArgs(), true);
                } else {
                    currentIndent = getContinuationIndent(path, currentIndent);
                }
                break;
            case ANNOTATION:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token != null && token.token().id() == JavaTokenId.COMMA) {
                    currentIndent = getMultilineIndent(((AnnotationTree)last).getArguments(), path, token.offset(), currentIndent, cs.alignMultilineAnnotationArgs(), true);
                } else {
                    currentIndent = getContinuationIndent(path, currentIndent);
                }
                break;
            case LABELED_STATEMENT:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token == null || token.token().id() != JavaTokenId.COLON) {
                    currentIndent = getContinuationIndent(path, currentIndent);
                }
                break;
            default:
                currentIndent = getContinuationIndent(path, currentIndent);
                break;
        }
        return currentIndent;
    }

    private int getEmbeddedOffset(int offset) {
        return currentEmbedding != null ? currentEmbedding.getSnapshot().getEmbeddedOffset(offset) : offset;
    }

    private int getOriginalOffset(int offset) {
        return currentEmbedding != null ? currentEmbedding.getSnapshot().getOriginalOffset(offset) : offset;
    }

    private LinkedList<? extends Tree> getPath(final int startOffset) {
        final LinkedList<Tree> path = new LinkedList<Tree>();

        // When right at the token end move to previous token; otherwise move to the token that "contains" the offset
        if (ts.move(startOffset) == 0 && startOffset > 0 || !ts.moveNext()) {
            ts.movePrevious();
        }
        final int offset = (ts.token().id() == JavaTokenId.IDENTIFIER
                || ts.token().id().primaryCategory().startsWith("keyword") || //NOI18N
                ts.token().id().primaryCategory().startsWith("string") || //NOI18N
                ts.token().id().primaryCategory().equals("literal")) //NOI18N
                ? ts.offset() : startOffset;

        new TreeScanner<Void, Void>() {

            @Override
            public Void scan(Tree node, Void p) {
                if (node != null) {
                    if (sp.getStartPosition(cut, node) < offset && sp.getEndPosition(cut, node) >= offset) {
                        super.scan(node, p);
                        if (node.getKind() != Tree.Kind.ERRONEOUS || !path.isEmpty()) {
                            path.add(node);
                        }
                    }
                }
                return null;
            }
        }.scan(cut, null);

        if (path.isEmpty() || path.getFirst() == cut || sp.getEndPosition(cut, path.getFirst()) > offset) {
            return path;
        }

        if (!path.isEmpty() && ts.move(offset) == 0) {
            if (ts.movePrevious()) {
                switch (ts.token().id()) {
                    case RPAREN:
                        if (!EnumSet.of(Kind.ENHANCED_FOR_LOOP, Kind.FOR_LOOP, Kind.IF, Kind.WHILE_LOOP, Kind.DO_WHILE_LOOP,
                                Kind.TYPE_CAST, Kind.SYNCHRONIZED).contains(path.getFirst().getKind())) {
                            path.removeFirst();
                        }
                        break;
                    case GTGTGT:
                    case GTGT:
                    case GT:
                        if (EnumSet.of(Kind.MEMBER_SELECT, Kind.CLASS, Kind.GREATER_THAN).contains(path.getFirst().getKind())) {
                            break;
                        }
                    case SEMICOLON:
                        if (path.getFirst().getKind() == Kind.FOR_LOOP
                                && ts.offset() <= sp.getStartPosition(null, ((ForLoopTree)path.getFirst()).getUpdate().get(0))) {
                            break;
                        }
                    case RBRACE:
                        path.removeFirst();
                        switch (path.getFirst().getKind()) {
                            case CATCH:
                                path.removeFirst();
                            case METHOD:
                            case FOR_LOOP:
                            case ENHANCED_FOR_LOOP:
                            case IF:
                            case SYNCHRONIZED:
                            case WHILE_LOOP:
                            case TRY:
                                path.removeFirst();
                        }
                        break;
                }
            }
        }

        return path;
    }

    private TokenSequence<JavaTokenId> findFirstNonWhitespaceToken(int startOffset, int endOffset) {
        if (startOffset == endOffset) {
            return null;
        }
        ts.move(startOffset);
        boolean backward = startOffset > endOffset;
        while (backward ? ts.movePrevious() : ts.moveNext()) {
            if (backward && ts.offset() < endOffset || !backward && ts.offset() > endOffset) {
                return null;
            }
            switch (ts.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case JAVADOC_COMMENT:
                    break;
                default:
                    return ts;
            }
        }
        return null;
    }

    private boolean isLeftBraceOnNewLine(int startOffset, int endOffset) {
        ts.move(startOffset);
        while (ts.moveNext()) {
            if (ts.offset() >= endOffset) {
                return false;
            }
            if (ts.token().id() == JavaTokenId.LBRACE) {
                if (!ts.movePrevious()) {
                    return false;
                }
                return ts.token().id() == JavaTokenId.LINE_COMMENT || ts.token().id() == JavaTokenId.WHITESPACE && ts.token().text().toString().indexOf('\n') >= 0;
            }
        }
        return false;
    }
    
    private int getColumn(Tree tree) throws BadLocationException {
        int startOffset = getOriginalOffset((int)sp.getStartPosition(cut, tree));
        if (startOffset < 0) {
            return -1;
        }
        int lineStartOffset = context.lineStartOffset(startOffset);
        return getCol(context.document().getText(lineStartOffset, startOffset - lineStartOffset));
    }

    private int getCurrentIndent(Tree tree, List<? extends Tree> path) throws BadLocationException {
        int startOffset = getOriginalOffset((int)sp.getStartPosition(cut, tree));
        if (startOffset < 0) {
            startOffset = currentEmbeddingStartOffset;
        }
        int lineStartOffset = context.lineStartOffset(startOffset);
        Integer newIndent = newIndents.get(lineStartOffset);
        int currentIndent = newIndent != null ? newIndent : context.lineIndent(lineStartOffset);
        if (cs.absoluteLabelIndent()) {
            for (Iterator<? extends Tree> it = path.iterator(); it.hasNext();) {
                Tree t = it.next();
                if (t.getKind() == Tree.Kind.LABELED_STATEMENT && (int)sp.getStartPosition(cut, t) == lineStartOffset) {
                    Tree parent = it.hasNext() ? it.next() : null;
                    if (parent != null && parent.getKind() == Kind.BLOCK) {
                        Tree stat = null;
                        for (StatementTree st : ((BlockTree)parent).getStatements()) {
                            if (sp.getEndPosition(cut, st) > startOffset) {
                                break;
                            }
                            stat = st;
                        }
                        if (stat != null) {
                            int i = getCurrentIndent(stat, path);
                            currentIndent = i < 0 ? currentIndent + cs.getIndentSize() : i;
                        } else {
                            int i = getCurrentIndent(parent, path);
                            currentIndent = (i < 0 ? currentIndent : i) + cs.getIndentSize();
                        }
                    }
                    break;
                }
            }
        }
        return currentIndent;
    }

    private int getContinuationIndent(LinkedList<? extends Tree> path, int currentIndent) throws BadLocationException {
        for (Tree tree : path) {
            switch (tree.getKind()) {
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                case VARIABLE:
                case METHOD:
                case TRY:
                case RETURN:
                case BLOCK:
                case FOR_LOOP:
                case SWITCH:
                case THROW:
                case WHILE_LOOP:
                case IF:
                case EXPRESSION_STATEMENT:
                case SYNCHRONIZED:
                case ASSERT:
                case CONTINUE:
                case LABELED_STATEMENT:
                case ENHANCED_FOR_LOOP:
                case BREAK:
                case EMPTY_STATEMENT:
                case DO_WHILE_LOOP:
                    int i = getCurrentIndent(tree, path);
                    return (i < 0 ? currentIndent : i) + cs.getContinuationIndentSize();
                case ASSIGNMENT:
                case MULTIPLY_ASSIGNMENT:
                case DIVIDE_ASSIGNMENT:
                case REMAINDER_ASSIGNMENT:
                case PLUS_ASSIGNMENT:
                case MINUS_ASSIGNMENT:
                case LEFT_SHIFT_ASSIGNMENT:
                case RIGHT_SHIFT_ASSIGNMENT:
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                case AND_ASSIGNMENT:
                case XOR_ASSIGNMENT:
                case OR_ASSIGNMENT:
                    if (cs.alignMultilineAssignment()) {
                        int c = getColumn(tree);
                        return c < 0 ? currentIndent : c;
                    }
                    break;
                case AND:
                case CONDITIONAL_AND:
                case CONDITIONAL_OR:
                case DIVIDE:
                case EQUAL_TO:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL:
                case LEFT_SHIFT:
                case LESS_THAN:
                case LESS_THAN_EQUAL:
                case MINUS:
                case MULTIPLY:
                case NOT_EQUAL_TO:
                case OR:
                case PLUS:
                case REMAINDER:
                case RIGHT_SHIFT:
                case UNSIGNED_RIGHT_SHIFT:
                case XOR:
                    if (cs.alignMultilineBinaryOp()) {
                        int c = getColumn(tree);
                        return c < 0 ? currentIndent : c;
                    }
                    break;
                case CONDITIONAL_EXPRESSION:
                    if (cs.alignMultilineTernaryOp()) {
                        int c = getColumn(tree);
                        return c < 0 ? currentIndent : c;
                    }
                    break;
            }
        }
        return currentIndent + cs.getContinuationIndentSize();
    }

    private int getStmtIndent(int startOffset, int endOffset, Set<JavaTokenId> expectedTokenIds, int expectedTokenOffset, int currentIndent) {
        TokenSequence<JavaTokenId> token = findFirstNonWhitespaceToken(startOffset, expectedTokenOffset);
        if (token != null && expectedTokenIds.contains(token.token().id())) {
            token = findFirstNonWhitespaceToken(startOffset, endOffset);
            if (token != null && token.token().id() == JavaTokenId.LBRACE) {
                switch (cs.getOtherBracePlacement()) {
                    case NEW_LINE_INDENTED:
                        currentIndent += cs.getIndentSize();
                        break;
                    case NEW_LINE_HALF_INDENTED:
                        currentIndent += (cs.getIndentSize() / 2);
                        break;
                }
            } else {
                currentIndent += cs.getIndentSize();
            }
        } else {
            currentIndent += cs.getContinuationIndentSize();
        }
        return currentIndent;
    }

    private int getMultilineIndent(List<? extends Tree> trees, LinkedList<? extends Tree> path, int commaOffset, int currentIndent, boolean align, boolean addContinuationIndent) throws BadLocationException {
        Tree tree = null;
        Tree first = null;
        for (Tree t : trees) {
            if (first == null) {
                first = t;
            }
            if (sp.getEndPosition(cut, t) > commaOffset) {
                break;
            }
            tree = t;
        }
        if (tree != null && findFirstNonWhitespaceToken(commaOffset, (int)(sp.getEndPosition(cut, tree))) == null) {
            int firstStartOffset = getOriginalOffset((int)sp.getStartPosition(cut, first));
            int startOffset = first == tree ? firstStartOffset : getOriginalOffset((int)sp.getStartPosition(cut, tree));
            if (firstStartOffset < 0 || startOffset < 0) {
                currentIndent = addContinuationIndent ? getContinuationIndent(path, currentIndent) : currentIndent + cs.getIndentSize();
            } else {
                int firstLineStartOffset = context.lineStartOffset(firstStartOffset);
                int lineStartOffset = firstStartOffset == startOffset ? firstLineStartOffset : context.lineStartOffset(startOffset);
                if (firstLineStartOffset != lineStartOffset) {
                    Integer newIndent = newIndents.get(lineStartOffset);
                    currentIndent = newIndent != null ? newIndent : context.lineIndent(lineStartOffset);
                } else if (align) {
                    currentIndent = getCol(context.document().getText(lineStartOffset, startOffset - lineStartOffset));
                } else {
                    currentIndent = addContinuationIndent ? getContinuationIndent(path, currentIndent) : currentIndent + cs.getIndentSize();
                }
            }
        } else {
            currentIndent = addContinuationIndent ? getContinuationIndent(path, currentIndent) : currentIndent + cs.getIndentSize();
        }
        return currentIndent;
    }

    private int getCol(String text) {
        int col = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\t') {
                col += cs.getTabSize();
                col -= (col % cs.getTabSize());
            } else {
                col++;
            }
        }
        return col;
    }
    
    public static class Factory implements IndentTask.Factory {

        @Override
        public IndentTask createTask(Context context) {
            return new Reindenter(context);
        }
    }
}
