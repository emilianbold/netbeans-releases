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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.Name;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.platform.JavaPlatformManager;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.source.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.modules.java.source.JavaSourceAccessor;
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
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 *
 * @author Dusan Balek
 */
public class Reformatter implements ReformatTask {
    
    private static final Object CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N

    private Source source;
    private Context context;
    private CompilationController controller;
    private Document doc;
    private int shift;

    public Reformatter(Source source, Context context) {
        this.source = source;
        this.context = context;
        this.doc = context.document();
    }

    public void reformat() throws BadLocationException {
        if (controller == null) {
            try {
                if (JavacParser.MIME_TYPE.equals(source.getMimeType())) {
                    controller = JavaSourceAccessor.getINSTANCE().createCompilationController(source);
                } else {
                    ParserManager.parse(Collections.singletonList(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Parser.Result result = findEmbeddedJava(resultIterator);
                            if (result != null)
                                controller = CompilationController.get(result);
                        }
                        private Parser.Result findEmbeddedJava(final ResultIterator theMess) throws ParseException {
                            final Collection<Embedding> todo = new LinkedList<Embedding>();
                            //BFS should perform better than DFS in this dark.
                            for (Embedding embedding : theMess.getEmbeddings()) {
                                if (JavacParser.MIME_TYPE.equals(embedding.getMimeType())) {
                                    return theMess.getResultIterator(embedding).getParserResult();
                                } else {
                                    todo.add(embedding);
                                }
                            }
                            for (Embedding embedding : todo) {
                                Parser.Result result = findEmbeddedJava(theMess.getResultIterator(embedding));
                                if (result != null) {
                                    return result;
                                }
                            }
                            return null;
                        }
                    });
                }
                if (controller == null)
                    return;
                controller.toPhase(JavaSource.Phase.PARSED);
            } catch (Exception ex) {
                controller = null;
                return;
            }
        }
        CodeStyle cs = CodeStyle.getDefault(doc);
        for (Context.Region region : context.indentRegions())
            reformatImpl(region, cs);
    }
    
    public static String reformat(String text, CodeStyle style) {
        return reformat(text, style, style.getRightMargin());
    }

    public static String reformat(String text, CodeStyle style, int rightMargin) {
        StringBuilder sb = new StringBuilder(text);
        try {
            ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
            ClasspathInfo cpInfo = ClasspathInfo.create(JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries(), empty, empty);
            JavacTaskImpl javacTask = JavacParser.createJavacTask(cpInfo, null, null, null, null, null);
            com.sun.tools.javac.util.Context ctx = javacTask.getContext();
            JavaCompiler.instance(ctx).genEndPos = true;
            CompilationUnitTree tree = javacTask.parse(FileObjects.memoryFileObject("","", text)).iterator().next(); //NOI18N
            SourcePositions sp = JavacTrees.instance(ctx).getSourcePositions();
            TokenSequence<JavaTokenId> tokens = TokenHierarchy.create(text, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
            for (Diff diff : Pretty.reformat(text, tokens, new TreePath(tree), sp, style, rightMargin)) {
                int start = diff.getStartOffset();
                int end = diff.getEndOffset();
                sb.delete(start, end);
                String t = diff.getText();
                if (t != null && t.length() > 0) {
                    sb.insert(start, t);
                }
            }

        } catch (IOException ioe) {
        }
        return sb.toString();
    }
    
    private void reformatImpl(Context.Region region, CodeStyle cs) throws BadLocationException {
        boolean templateEdit = doc.getProperty(CT_HANDLER_DOC_PROPERTY) != null;
        int startOffset = region.getStartOffset() - shift;
        int endOffset = region.getEndOffset() - shift;
        int originalEndOffset = endOffset;
        startOffset = controller.getSnapshot().getEmbeddedOffset(startOffset);
        if (startOffset < 0)
            return;
        endOffset = controller.getSnapshot().getEmbeddedOffset(endOffset);
        if (endOffset < 0)
            return;
        int embeddingOffset = -1;
        int firstLineIndent = 0;
        if (!"text/x-java".equals(context.mimePath())) { //NOI18N
            firstLineIndent = context.lineIndent(context.lineStartOffset(region.getStartOffset()));
            TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            if (ts != null) {
                ts.move(startOffset);
                if (ts.moveNext()) {
                    if (ts.token().id() == WHITESPACE) {
                        String t = ts.token().text().toString();
                        if (ts.offset() < startOffset)
                            t = t.substring(startOffset - ts.offset());
                        if (t.indexOf('\n') < 0) //NOI18N
                            embeddingOffset = ts.offset() + ts.token().length();
                    } else {
                        embeddingOffset = startOffset;
                    }
                }
                ts.move(endOffset);
                if (ts.moveNext() && ts.token().id() == WHITESPACE) {
                    String t = ts.token().text().toString();
                    if (ts.offset() + t.length() > endOffset)
                        t = t.substring(0, endOffset - ts.offset());
                    int i = t.lastIndexOf('\n'); //NOI18N
                    if (i >= 0)
                        endOffset -= (t.length() - i);
                }
            }
        }
        if (startOffset >= endOffset)
            return;
        TreePath path = getCommonPath(startOffset, endOffset);
        if (path == null)
            return;
        for (Diff diff : Pretty.reformat(controller, path, cs, startOffset, endOffset, templateEdit, firstLineIndent)) {
            int start = diff.getStartOffset();
            int end = diff.getEndOffset();
            String text = diff.getText();
            if (startOffset > end)
                continue;
            if (endOffset < start)
                continue;
            if (endOffset == start && (text == null || !text.trim().equals("}"))) //NOI18N
                continue;
            if (embeddingOffset >= start)
                continue;
            if (startOffset >= start) {
                if (text != null && text.length() > 0) {
                    TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                    if (ts == null)
                        continue;
                    if (ts.move(startOffset) == 0) {
                        if (!ts.movePrevious() && !ts.moveNext())
                            continue;
                    } else {
                        if (!ts.moveNext() && !ts.movePrevious())
                            continue;
                    }
                    if (ts.token().id() == WHITESPACE) {
                        String t = ts.token().text().toString();
                        t = t.substring(0, startOffset - ts.offset());
                        if (templateEdit) {
                            int idx = t.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                t = t.substring(idx + 1);
                                idx = text.lastIndexOf('\n'); //NOI18N
                                if (idx >= 0)
                                    text = text.substring(idx + 1);
                                if (text.trim().length() > 0)
                                    text = null;
                                else if (text.length() > t.length())
                                    text = text.substring(t.length());
                                else
                                    text = null;
                            } else {
                                text = null;
                            }
                        } else {
                            int idx1 = 0;
                            int idx2 = 0;
                            int lastIdx1 = 0;
                            int lastIdx2 = 0;
                            while ((idx1 = t.indexOf('\n', lastIdx1)) >=0 && (idx2 = text.indexOf('\n', lastIdx2)) >= 0) { //NOI18N
                                lastIdx1 = idx1 + 1;
                                lastIdx2 = idx2 + 1;
                            }
                            if ((idx2 = text.lastIndexOf('\n')) >= 0 && idx2 >= lastIdx2) { //NOI18N
                                if (lastIdx1 == 0) {
                                    t = null;
                                } else {
                                    text = text.substring(idx2 + 1);
                                    t = t.substring(lastIdx1);
                                }
                            } else if ((idx1 = t.lastIndexOf('\n')) >= 0 && idx1 >= lastIdx1) { //NOI18N
                                t = t.substring(idx1 + 1);
                                text = text.substring(lastIdx2);
                            } else {
                                t = t.substring(lastIdx1);
                                text = text.substring(lastIdx2);
                            }
                            if (text != null && t != null)
                                text = text.length() > t.length() ? text.substring(t.length()) : null;
                        }
                    } else if (templateEdit) {
                        text = null;
                    }
                }
                start = startOffset;
            }
            if (endOffset < end) {
                if (text != null && text.length() > 0 && !templateEdit) {
                    TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                    if (ts != null) {
                        ts.move(endOffset);
                        if (ts.moveNext() && ts.token().id() == WHITESPACE) {
                            String t = ts.token().text().toString();
                            t = t.substring(endOffset - ts.offset());
                            int idx1, idx2;
                            while ((idx1 = t.lastIndexOf('\n')) >=0 && (idx2 = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                t = t.substring(0, idx1);
                                text = text.substring(0, idx2);
                            }
                            text = text.length() > t.length() ? text.substring(0, text.length() - t.length()) : null;
                        }
                    }
                }
                end = endOffset;
            }
            start = controller.getSnapshot().getOriginalOffset(start);
            end = controller.getSnapshot().getOriginalOffset(end);
            if (start == (-1) || end == (-1)) continue;
            start += shift;
            end += shift;
            doc.remove(start, end - start);
            if (text != null && text.length() > 0)
                doc.insertString(start, text, null);
        }
        shift = region.getEndOffset() - originalEndOffset;
        return;
    }
    
    public ExtraLock reformatLock() {
        return JavacParser.MIME_TYPE.equals(source.getMimeType()) ? null : new ExtraLock() {
            public void lock() {
                Utilities.acquireParserLock();
            }
            public void unlock() {
                Utilities.releaseParserLock();
            }
        };
    }
    
    private TreePath getCommonPath(int startOffset, int endOffset) {
        TreeUtilities tu = controller.getTreeUtilities();
        TreePath startPath = tu.pathFor(startOffset);
        com.sun.tools.javac.util.List<Tree> reverseStartPath = com.sun.tools.javac.util.List.<Tree>nil();
        for (Tree t : startPath)
            reverseStartPath = reverseStartPath.prepend(t);
        TreePath endPath = tu.pathFor(endOffset);
        com.sun.tools.javac.util.List<Tree> reverseEndPath = com.sun.tools.javac.util.List.<Tree>nil();
        for (Tree t : endPath)
            reverseEndPath = reverseEndPath.prepend(t);
        TreePath path = null;
        TreePath statementPath = null;
        while(reverseStartPath.head != null && reverseStartPath.head == reverseEndPath.head) {
            path = reverseStartPath.head instanceof CompilationUnitTree ? new TreePath((CompilationUnitTree)reverseStartPath.head) : new TreePath(path, reverseStartPath.head);
            if (reverseStartPath.head instanceof StatementTree)
                statementPath = path;
            reverseStartPath = reverseStartPath.tail;
            reverseEndPath = reverseEndPath.tail;
        }
        return statementPath != null ? statementPath : path;
    }
    
    public static class Factory implements ReformatTask.Factory {

        public ReformatTask createTask(Context context) {
            Source source = Source.create(context.document());
            return source != null ? new Reformatter(source, context) : null;
        }        
    }

    private static class Pretty extends TreePathScanner<Boolean, Void> {

        private static final String OPERATOR = "operator"; //NOI18N
        private static final String EMPTY = ""; //NOI18N
        private static final String SPACE = " "; //NOI18N
        private static final String NEWLINE = "\n"; //NOI18N
        private static final String ERROR = "<error>"; //NOI18N
        private static final int ANY_COUNT = -1;

        private final String fText;
        private final SourcePositions sp;
        private final CodeStyle cs;

        private final int rightMargin;
        private final int tabSize;
        private final int indentSize;
        private final int continuationIndentSize;
        private final boolean expandTabToSpaces;

        private TokenSequence<JavaTokenId> tokens;    
        private int indent;
        private int col;
        private int endPos;
        private int lastBlankLines;
        private int lastBlankLinesTokenIndex;
        private Diff lastBlankLinesDiff;
        private boolean afterAnnotation;
        private boolean wrapAnnotation;
        private boolean fieldGroup;
        private boolean templateEdit;
        private LinkedList<Diff> diffs = new LinkedList<Diff>();
        private DanglingElseChecker danglingElseChecker = new DanglingElseChecker();
        private CompilationUnitTree root;
        private int startOffset;
        private int endOffset;
        private int tpLevel;

        private Pretty(CompilationInfo info, TreePath path, CodeStyle cs, int startOffset, int endOffset, boolean templateEdit) {
            this(info.getText(), info.getTokenHierarchy().tokenSequence(JavaTokenId.language()),
                    path, info.getTrees().getSourcePositions(), cs, startOffset, endOffset);
            this.templateEdit = templateEdit;
        }
        
        private Pretty(String text, TokenSequence<JavaTokenId> tokens, TreePath path, SourcePositions sp, CodeStyle cs, int startOffset, int endOffset) {
            this(text, tokens, path, sp, cs, startOffset, endOffset, cs.getRightMargin());
        }

        private Pretty(String text, TokenSequence<JavaTokenId> tokens, TreePath path, SourcePositions sp, CodeStyle cs, int startOffset, int endOffset, int rightMargin) {
            this.fText = text;
            this.sp = sp;
            this.cs = cs;
            this.rightMargin = rightMargin;
            this.tabSize = cs.getTabSize();
            this.indentSize = cs.getIndentSize();
            this.continuationIndentSize = cs.getContinuationIndentSize();
            this.expandTabToSpaces =  cs.expandTabToSpaces();
            this.lastBlankLines = -1;
            this.lastBlankLinesTokenIndex = -1;
            this.lastBlankLinesDiff = null;
            this.afterAnnotation = false;
            this.wrapAnnotation = false;
            this.fieldGroup = false;
            Tree tree = path.getLeaf();
            this.indent = tokens != null ? getIndentLevel(tokens, path) : 0;
            this.col = this.indent;
            this.tokens = tokens;
            if (tree.getKind() == Tree.Kind.COMPILATION_UNIT) {
                tokens.moveEnd();
                tokens.movePrevious();
            } else {
                tokens.move((int)sp.getEndPosition(path.getCompilationUnit(), tree));
                if (!tokens.moveNext())
                    tokens.movePrevious();
            }
            this.endPos = tokens.offset();
            if (tree.getKind() == Tree.Kind.COMPILATION_UNIT) {
                tokens.moveStart();
            } else {
                tokens.move((int)sp.getStartPosition(path.getCompilationUnit(), tree));
            }
            tokens.moveNext();
            this.root = path.getCompilationUnit();
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.tpLevel = 0;
        }

        public static LinkedList<Diff> reformat(CompilationInfo info, TreePath path, CodeStyle cs, int startOffset, int endOffset, boolean templateEdit, int firstLineIndent) {
            Pretty pretty = new Pretty(info, path, cs, startOffset, endOffset, templateEdit);
            if (pretty.indent >= 0) {
                pretty.indent += firstLineIndent;
                pretty.scan(path, null);
            }
            if (path.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                CompilationUnitTree cut = (CompilationUnitTree) path.getLeaf();
                List<? extends Tree> typeDecls = cut.getTypeDecls();
                int size = typeDecls.size();
                int cnt = size > 0 && typeDecls.get(size - 1).getKind() == Tree.Kind.CLASS ? cs.getBlankLinesAfterClass() : 1;
                if (cnt < 1)
                    cnt = 1;
                String s = pretty.getNewlines(cnt);
                pretty.tokens.moveEnd();
                pretty.tokens.movePrevious();
                if (pretty.tokens.token().id() != WHITESPACE) {
                    String text = info.getText();
                    pretty.diffs.addFirst(new Diff(text.length(), text.length(), s));
                } else if (!s.contentEquals(pretty.tokens.token().text())) {
                    pretty.diffs.addFirst(new Diff(pretty.tokens.offset(), pretty.tokens.offset() + pretty.tokens.token().length(), s));
                }
            }
            return pretty.diffs;
        }

        public static LinkedList<Diff> reformat(String text, TokenSequence<JavaTokenId> tokens, TreePath path, SourcePositions sp, CodeStyle cs, int rightMargin) {
            Pretty pretty = new Pretty(text, tokens, path, sp, cs, 0, text.length(), rightMargin);
            pretty.scan(path, null);
            CompilationUnitTree cut = (CompilationUnitTree) path.getLeaf();
            List<? extends Tree> typeDecls = cut.getTypeDecls();
            int size = typeDecls.size();
            int cnt = size > 0 && typeDecls.get(size - 1).getKind() == Tree.Kind.CLASS ? cs.getBlankLinesAfterClass() : 1;
            if (cnt < 1)
                cnt = 1;
            String s = pretty.getNewlines(cnt);
            tokens.moveEnd();
            tokens.movePrevious();
            if (tokens.token().id() != WHITESPACE)
                pretty.diffs.addFirst(new Diff(text.length(), text.length(), s));
            else if (!s.contentEquals(tokens.token().text()))
                pretty.diffs.addFirst(new Diff(tokens.offset(), tokens.offset() + tokens.token().length(), s));
            return pretty.diffs;
        }

        @Override
        public Boolean scan(Tree tree, Void p) {
            int lastEndPos = endPos;
            if (tree != null && tree.getKind() != Tree.Kind.COMPILATION_UNIT) {
                if (tree instanceof FakeBlock) {
                    endPos = Integer.MAX_VALUE;
                } else {
                    endPos = (int)sp.getEndPosition(getCurrentPath().getCompilationUnit(), tree);
                }
            }
            try {
                return endPos < 0 ? false : tokens.offset() <= endPos ? super.scan(tree, p) : true;
            }
            finally {
                endPos = lastEndPos;
            }
        }

        @Override
        public Boolean visitCompilationUnit(CompilationUnitTree node, Void p) {
            ExpressionTree pkg = node.getPackageName();
            if (pkg != null) {
                blankLines(cs.getBlankLinesBeforePackage());
                if (!node.getPackageAnnotations().isEmpty()) {
                    wrapList(cs.wrapAnnotations(), false, false, node.getPackageAnnotations());
                    newline();
                }
                accept(PACKAGE);
                int old = indent;
                indent += continuationIndentSize;
                space();
                scan(pkg, p);
                accept(SEMICOLON);
                indent = old;
                blankLines(cs.getBlankLinesAfterPackage());
            }
            List<? extends ImportTree> imports = node.getImports();
            if (imports != null && !imports.isEmpty()) {
                blankLines(cs.getBlankLinesBeforeImports());
                for (ImportTree imp : imports) {
                    blankLines();
                    scan(imp, p);
                }
                blankLines(cs.getBlankLinesAfterImports());
            }
            boolean semiRead = false;
            for (Tree typeDecl : node.getTypeDecls()) {
                if (semiRead && typeDecl.getKind() == Tree.Kind.EMPTY_STATEMENT)
                    continue;
                blankLines(cs.getBlankLinesBeforeClass());
                scan(typeDecl, p);
                int index = tokens.index();
                int c = col;
                Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                if (accept(SEMICOLON) == SEMICOLON) {
                    semiRead = true;
                } else {
                    rollback(index, c, d);
                    semiRead = false;
                }
                blankLines(cs.getBlankLinesAfterClass());
            }
            return true;
        }

        @Override
        public Boolean visitImport(ImportTree node, Void p) {
            accept(IMPORT);
            int old = indent;
            indent += continuationIndentSize;
            space();
            if (node.isStatic()) {
                accept(STATIC);
                space();
            }
            scan(node.getQualifiedIdentifier(), p);
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitClass(ClassTree node, Void p) {
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            if (parent.getKind() != Tree.Kind.NEW_CLASS && (parent.getKind() != Tree.Kind.VARIABLE || !isEnumerator((VariableTree)parent))) {
                int old = indent;
                ModifiersTree mods = node.getModifiers();
                if (mods != null) {
                    if (scan(mods, p)) {
                        indent += continuationIndentSize;
                        if (cs.placeNewLineAfterModifiers())
                            newline();
                        else
                            space();
                    } else if (afterAnnotation) {
                        blankLines();
                    }
                    afterAnnotation = false;
                }
                JavaTokenId id = accept(CLASS, INTERFACE, ENUM, AT);
                if (indent == old)
                    indent += continuationIndentSize;
                if (id == AT)
                    accept(INTERFACE);
                space();
                if (!ERROR.contentEquals(node.getSimpleName()))
                    accept(IDENTIFIER);
                List<? extends TypeParameterTree> tparams = node.getTypeParameters();
                if (tparams != null && !tparams.isEmpty()) {
                    if (LT == accept(LT))
                        tpLevel++;
                    for (Iterator<? extends TypeParameterTree> it = tparams.iterator(); it.hasNext();) {
                        TypeParameterTree tparam = it.next();
                        scan(tparam, p);
                        if (it.hasNext()) {
                            spaces(cs.spaceBeforeComma() ? 1 : 0);
                            accept(COMMA);
                            spaces(cs.spaceAfterComma() ? 1 : 0);
                        }
                    }
                    JavaTokenId accepted;
                    if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                        switch (accepted) {
                            case GTGTGT:
                                tpLevel -= 3;
                                break;
                            case GTGT:
                                tpLevel -= 2;
                                break;
                            case GT:
                                tpLevel--;
                                break;
                        }
                    }
                }
                Tree ext = node.getExtendsClause();
                if (ext != null) {
                    wrapToken(cs.wrapExtendsImplementsKeyword(), -1, 1, EXTENDS);
                    space();
                    scan(ext, p);
                }
                List<? extends Tree> impls = node.getImplementsClause();
                if (impls != null && !impls.isEmpty()) {
                    wrapToken(cs.wrapExtendsImplementsKeyword(), -1, 1, id == INTERFACE ? EXTENDS : IMPLEMENTS);
                    wrapList(cs.wrapExtendsImplementsList(), cs.alignMultilineImplements(), true, impls);
                }
                indent = old;
            }
            CodeStyle.BracePlacement bracePlacement = cs.getClassDeclBracePlacement();
            boolean spaceBeforeLeftBrace = cs.spaceBeforeClassDeclLeftBrace();
            int old = indent;
            int halfIndent = indent;
            switch(bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0);
                    accept(LBRACE);
                    indent += indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    accept(LBRACE);
                    indent += indentSize;
                    break;
                case NEW_LINE_HALF_INDENTED:
                    indent += (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    indent = old + indentSize;
                    break;
                case NEW_LINE_INDENTED:
                    indent += indentSize;
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    break;
            }
            boolean emptyClass = true;
            for (Tree member : node.getMembers()) {
                if (!isSynthetic(getCurrentPath().getCompilationUnit(), member)) {
                    emptyClass = false;
                    break;
                }
            }
            if (emptyClass) {
                newline();
            } else {
                if (!cs.indentTopLevelClassMembers())
                    indent = old;
                blankLines(cs.getBlankLinesAfterClassHeader());
                JavaTokenId id = null;
                boolean first = true;
                boolean semiRead = false;
                for (Tree member : node.getMembers()) {
                    if (!isSynthetic(getCurrentPath().getCompilationUnit(), member)) {
                        switch(member.getKind()) {
                            case VARIABLE:
                                if (isEnumerator((VariableTree)member)) {
                                    wrapTree(cs.wrapEnumConstants(), -1, id == COMMA ? 1 : 0, member);
                                    int index = tokens.index();
                                    int c = col;
                                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                                    id = accept(COMMA, SEMICOLON);
                                    if (id == COMMA) {
                                        index = tokens.index();
                                        c = col;
                                        d = diffs.isEmpty() ? null : diffs.getFirst();
                                        if (accept(SEMICOLON) == null)
                                            rollback(index, c, d);
                                    } else if (id == SEMICOLON) {
                                        blankLines(cs.getBlankLinesAfterFields());
                                    } else {
                                        rollback(index, c, d);
                                        blankLines(cs.getBlankLinesAfterFields());
                                    }
                                } else {
                                    boolean b = tokens.moveNext();
                                    if (b) {
                                        tokens.movePrevious();
                                        if (!fieldGroup && !first)
                                            blankLines(cs.getBlankLinesBeforeFields());
                                        scan(member, p);
                                        if(!fieldGroup)
                                            blankLines(cs.getBlankLinesAfterFields());
                                    }
                                }
                                break;
                            case METHOD:
                                if (!first)
                                   blankLines(cs.getBlankLinesBeforeMethods());
                                scan(member, p);
                                blankLines(cs.getBlankLinesAfterMethods());
                                break;
                            case BLOCK:
                                if (semiRead && !((BlockTree)member).isStatic() && ((BlockTree)member).getStatements().isEmpty()) {
                                    semiRead = false;
                                    continue;
                                }
                                if (!first)
                                   blankLines(cs.getBlankLinesBeforeMethods());
                                int index = tokens.index();
                                int c = col;
                                Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                                if (accept(SEMICOLON) == SEMICOLON) {
                                    continue;
                                } else {
                                    rollback(index, c, d);
                                }
                                scan(member, p);
                                blankLines(cs.getBlankLinesAfterMethods());
                                break;
                            case CLASS:
                                if (!first)
                                    blankLines(cs.getBlankLinesBeforeClass());
                                scan(member, p);
                                index = tokens.index();
                                c = col;
                                d = diffs.isEmpty() ? null : diffs.getFirst();
                                if (accept(SEMICOLON) == SEMICOLON) {
                                    semiRead = true;
                                } else {
                                    rollback(index, c, d);
                                    semiRead = false;
                                }
                                blankLines(cs.getBlankLinesAfterClass());
                                break;
                        }
                        first = false;
                    }
                }
                if (lastBlankLinesTokenIndex < 0)
                    newline();
            }
            indent = halfIndent;
            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
            if (diff != null && diff.end == tokens.offset()) {
                if (diff.text != null) {
                    int idx = diff.text.lastIndexOf('\n'); //NOI18N
                    if (idx < 0)
                        diff.text = getIndent();
                    else
                        diff.text = diff.text.substring(0, idx + 1) + getIndent();
                    
                }
                String spaces = diff.text != null ? diff.text : getIndent();
                if (spaces.equals(fText.substring(diff.start, diff.end)))
                    diffs.removeFirst();
            } else if (tokens.movePrevious()) {
                if (tokens.token().id() == WHITESPACE) {
                    String text =  tokens.token().text().toString();
                    int idx = text.lastIndexOf('\n'); //NOI18N
                    if (idx >= 0) {
                        text = text.substring(idx + 1);
                        String ind = getIndent();
                        if (!ind.equals(text))
                            addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                    } else if (tokens.movePrevious()) {
                        if (tokens.token().id() == LINE_COMMENT) {
                            tokens.moveNext();
                            String ind = getIndent();
                            if (!ind.equals(text))
                                addDiff(new Diff(tokens.offset(), tokens.offset() + tokens.token().length(), ind));

                        } else {
                            tokens.moveNext();
                        }
                    }
                }
                tokens.moveNext();
            }
            accept(RBRACE);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitVariable(VariableTree node, Void p) {
            int old = indent;
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            boolean insideFor = parent.getKind() == Tree.Kind.FOR_LOOP;
            ModifiersTree mods = node.getModifiers();
            if (mods != null && !fieldGroup) {
                if (scan(mods, p)) {
                    if (!insideFor) {
                        indent += continuationIndentSize;
                        if (cs.placeNewLineAfterModifiers())
                            newline();
                        else
                            space();
                    } else {
                        space();
                    }
                } else if (afterAnnotation) {
                    if (parent.getKind() == Tree.Kind.CLASS || parent.getKind() == Tree.Kind.BLOCK) {
                        blankLines();
                    } else {
                        space();
                    }
                }
                afterAnnotation = false;
            }
            if (isEnumerator(node)) {
                accept(IDENTIFIER);
                ExpressionTree init = node.getInitializer();
                if (init != null && init.getKind() == Tree.Kind.NEW_CLASS) {
                    NewClassTree nct = (NewClassTree)init;
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null :diffs.getFirst();
                    spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);            
                    JavaTokenId id = accept(LPAREN);
                    if (id != LPAREN)
                        rollback(index, c, d);
                    List<? extends ExpressionTree> args = nct.getArguments();
                    if (args != null && !args.isEmpty()) {
                        spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                        wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, args);
                        spaces(cs.spaceWithinMethodCallParens() ? 1 : 0);            
                    }
                    if (id == LPAREN)
                        accept(RPAREN);
                    ClassTree body = nct.getClassBody();
                    if (body != null)
                        scan(body, p);
                }
            } else {
                if (indent == old && !insideFor)
                    indent += continuationIndentSize;
                if (scan(node.getType(), p)) {
                    spaces(1, fieldGroup);
                    if (!ERROR.contentEquals(node.getName()))
                        accept(IDENTIFIER);
                }
                ExpressionTree init = node.getInitializer();
                if (init != null) {
                    int alignIndent = -1;
                    if (cs.alignMultilineAssignment()) {
                        alignIndent = col;
                        if (!ERROR.contentEquals(node.getName()))
                            alignIndent -= node.getName().length();
                    }
                    spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                    accept(EQ);
                    if (init.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree)init).getType() == null) {
                        if (cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE)
                            spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                        scan(init, p);
                    } else {
                        wrapTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, init);
                    }
                }
                fieldGroup = accept(SEMICOLON, COMMA) == COMMA;
            }
            indent = old;
            return true;
        }

        @Override
        public Boolean visitMethod(MethodTree node, Void p) {
            int old = indent;
            ModifiersTree mods = node.getModifiers();
            if (mods != null) {
                if (scan(mods, p)) {
                    indent += continuationIndentSize;
                    if (cs.placeNewLineAfterModifiers())
                        newline();
                    else
                        space();
                } else {
                    blankLines();
                }
                afterAnnotation = false;
            }
            List<? extends TypeParameterTree> tparams = node.getTypeParameters();
            if (tparams != null && !tparams.isEmpty()) {
                if (LT == accept(LT))
                    tpLevel++;
                if (indent == old)
                    indent += continuationIndentSize;
                for (Iterator<? extends TypeParameterTree> it = tparams.iterator(); it.hasNext();) {
                    TypeParameterTree tparam = it.next();
                    scan(tparam, p);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                JavaTokenId accepted;
                if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                    switch (accepted) {
                        case GTGTGT:
                            tpLevel -= 3;
                            break;
                        case GTGT:
                            tpLevel -= 2;
                            break;
                        case GT:
                            tpLevel--;
                            break;
                    }
                }
                space();
            }
            Tree retType = node.getReturnType();
            if (retType != null) {
                scan(retType, p);
                if (indent == old)
                    indent += continuationIndentSize;
                space();
            }
            if (!ERROR.contentEquals(node.getName()))
                accept(IDENTIFIER);
            if (indent == old)
                indent += continuationIndentSize;
            spaces(cs.spaceBeforeMethodDeclParen() ? 1 : 0);
            accept(LPAREN);
            List<? extends VariableTree> params = node.getParameters();
            if (params != null && !params.isEmpty()) {
                spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0, true);
                wrapList(cs.wrapMethodParams(), cs.alignMultilineMethodParams(), false, params);
                spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0);
            }
            accept(RPAREN);
            List<? extends ExpressionTree> threxs = node.getThrows();
            if (threxs != null && !threxs.isEmpty()) {
                wrapToken(cs.wrapThrowsKeyword(), -1, 1, THROWS);
                wrapList(cs.wrapThrowsList(), cs.alignMultilineThrows(), true, threxs);
            }
            Tree init = node.getDefaultValue();
            if (init != null) {
                spaces(1, true);
                accept(DEFAULT);
                space();
                scan(init, p);
            }
            indent = old;
            BlockTree body = node.getBody();
            if (body != null) {
                scan(body, p);
            } else {
                accept(SEMICOLON);
            }
            return true;
        }

        @Override
        public Boolean visitModifiers(ModifiersTree node, Void p) {
            boolean ret = true;
            JavaTokenId id = null;
            afterAnnotation = false;
            Iterator<? extends AnnotationTree> annotations = node.getAnnotations().iterator();
            TreePath path = getCurrentPath().getParentPath();
            Tree parent = path.getLeaf();
            path = path.getParentPath();
            Tree grandParent = path.getLeaf();
            boolean isStandalone = parent.getKind() != Tree.Kind.VARIABLE ||
                    grandParent.getKind() == Tree.Kind.CLASS || grandParent.getKind() == Tree.Kind.BLOCK;
            while (tokens.offset() < endPos) {
                if (afterAnnotation) {
                    if (!isStandalone) {
                        spaces(1, true);
                    } else {
                        switch (cs.wrapAnnotations()) {
                            case WRAP_ALWAYS:
                                newline();
                                break;
                            case WRAP_IF_LONG:
                                if (col >= rightMargin)
                                    newline();
                                else
                                    spaces(1, true);
                                break;
                            case WRAP_NEVER:
                                spaces(1, true);
                        }
                    }
                } else if (id != null) {
                    space();
                }
                int index = tokens.index();
                int c = col;
                Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                int lbl = lastBlankLines;
                int lblti = lastBlankLinesTokenIndex;
                Diff lbld = lastBlankLinesDiff;
                id = accept(PRIVATE, PROTECTED, PUBLIC, STATIC, TRANSIENT, FINAL,
                        ABSTRACT, NATIVE, VOLATILE, SYNCHRONIZED, STRICTFP, AT);
                if (id == null)
                    break;
                if (id == AT) {
                    if (annotations.hasNext()) {
                        rollback(index, c, d);
                        lastBlankLines = lbl;
                        lastBlankLinesTokenIndex = lblti;
                        lastBlankLinesDiff = lbld;
                        if (!isStandalone) {
                            scan(annotations.next(), p);
                        } else {
                            wrapAnnotation = cs.wrapAnnotations() == CodeStyle.WrapStyle.WRAP_ALWAYS;
                            wrapTree(cs.wrapAnnotations(), -1, 0, annotations.next());
                            wrapAnnotation = false;
                        }
                        afterAnnotation = true;
                        ret = false;
                        continue;
                    }
                    afterAnnotation = false;
                    ret = false;
                } else {
                    afterAnnotation = false;
                    ret = true;
                }
            }
            return ret;
        }

        @Override
        public Boolean visitAnnotation(AnnotationTree node, Void p) {
            accept(AT);
            scan(node.getAnnotationType(), p);
            List<? extends ExpressionTree> args = node.getArguments();
            spaces(cs.spaceBeforeAnnotationParen() ? 1 : 0);
            accept(LPAREN);
            if (args != null && !args.isEmpty()) {
                spaces(cs.spaceWithinAnnotationParens() ? 1 : 0);
                wrapList(cs.wrapAnnotationArgs(), cs.alignMultilineAnnotationArgs(), false, args);
                spaces(cs.spaceWithinAnnotationParens() ? 1 : 0);
            }
            accept(RPAREN);
            return true;
        }

        @Override
        public Boolean visitTypeParameter(TypeParameterTree node, Void p) {
            if (!ERROR.contentEquals(node.getName()))
                accept(IDENTIFIER);
            List<? extends Tree> bounds = node.getBounds();
            if (bounds != null && !bounds.isEmpty()) {
                space();
                accept(EXTENDS);
                space();
                for (Iterator<? extends Tree> it = bounds.iterator(); it.hasNext();) {
                    Tree bound = it.next();
                    scan(bound, p);
                    if (it.hasNext()) {
                        space();
                        accept(AMP);
                        space();
                    }
                }
            }
            return true;
        }

        @Override
        public Boolean visitParameterizedType(ParameterizedTypeTree node, Void p) {
            scan(node.getType(), p);
            List<? extends Tree> targs = node.getTypeArguments();
            if (targs != null && !targs.isEmpty()) {
                if (LT == accept(LT))
                    tpLevel++;
                for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
                    Tree targ = it.next();
                    scan(targ, p);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                JavaTokenId accepted;
                if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                    switch (accepted) {
                        case GTGTGT:
                            tpLevel -= 3;
                            break;
                        case GTGT:
                            tpLevel -= 2;
                            break;
                        case GT:
                            tpLevel--;
                            break;
                    }
                }
            }
            return true;
        }

        @Override
        public Boolean visitWildcard(WildcardTree node, Void p) {
            accept(QUESTION);
            Tree bound = node.getBound();
            if (bound != null) {
                space();
                accept(EXTENDS, SUPER);
                space();
                scan(bound, p);
            }
            return true;
        }

        @Override
        public Boolean visitBlock(BlockTree node, Void p) {
            if (node.isStatic())
                accept(STATIC);
            CodeStyle.BracePlacement bracePlacement;
            boolean spaceBeforeLeftBrace = false;
            switch (getCurrentPath().getParentPath().getLeaf().getKind()) {
                case CLASS:
                    bracePlacement = cs.getOtherBracePlacement();
                    if (node.isStatic())
                        spaceBeforeLeftBrace = cs.spaceBeforeStaticInitLeftBrace();
                    break;
                case METHOD:
                    bracePlacement = cs.getMethodDeclBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeMethodDeclLeftBrace();
                    break;
                case TRY:
                    bracePlacement = cs.getOtherBracePlacement();
                    if (((TryTree)getCurrentPath().getParentPath().getLeaf()).getBlock() == node)
                        spaceBeforeLeftBrace = cs.spaceBeforeTryLeftBrace();
                    else
                        spaceBeforeLeftBrace = cs.spaceBeforeFinallyLeftBrace();
                    break;
                case CATCH:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeCatchLeftBrace();
                    break;
                case WHILE_LOOP:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeWhileLeftBrace();
                    break;
                case FOR_LOOP:
                case ENHANCED_FOR_LOOP:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeForLeftBrace();
                    break;
                case DO_WHILE_LOOP:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeDoLeftBrace();
                    break;
                case IF:
                    bracePlacement = cs.getOtherBracePlacement();
                    if (((IfTree)getCurrentPath().getParentPath().getLeaf()).getThenStatement() == node)
                        spaceBeforeLeftBrace = cs.spaceBeforeIfLeftBrace();
                    else
                        spaceBeforeLeftBrace = cs.spaceBeforeElseLeftBrace();
                    break;
                case SYNCHRONIZED:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeSynchronizedLeftBrace();
                    break;
                case CASE:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = true;
                    break;
                default:
                    bracePlacement = cs.getOtherBracePlacement();
                    break;
            }
            int old = indent;
            int halfIndent = indent;
            switch(bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0);
                    if (node instanceof FakeBlock) {
                        appendToDiff("{"); //NOI18N
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        lastBlankLinesDiff = null;
                    } else {
                        accept(LBRACE);
                    }
                    indent += indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    if (node instanceof FakeBlock) {
                        indent += indentSize;
                        appendToDiff("{"); //NOI18N
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        lastBlankLinesDiff = null;
                    } else {
                        accept(LBRACE);
                        indent += indentSize;
                    }
                    break;
                case NEW_LINE_HALF_INDENTED:
                    indent += (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    if (node instanceof FakeBlock) {
                        indent = old + indentSize;
                        appendToDiff("{"); //NOI18N
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        lastBlankLinesDiff = null;
                    } else {
                        accept(LBRACE);
                        indent = old + indentSize;
                    }
                    break;
                case NEW_LINE_INDENTED:
                    indent += indentSize;
                    halfIndent = indent;
                    newline();
                    if (node instanceof FakeBlock) {
                        appendToDiff("{"); //NOI18N
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        lastBlankLinesDiff = null;
                    } else {
                        accept(LBRACE);
                    }
                    break;
            }
            boolean isEmpty = true;
            for (StatementTree stat  : node.getStatements()) {
                if (!isSynthetic(getCurrentPath().getCompilationUnit(), stat)) {
                    isEmpty = false;
                    if (stat.getKind() == Tree.Kind.LABELED_STATEMENT && cs.absoluteLabelIndent()) {
                        int o = indent;
                        indent = 0;
                        if (node instanceof FakeBlock) {
                            appendToDiff(getNewlines(1) + getIndent());
                            col = indent;
                        } else {
                            blankLines();
                        }
                        indent = o;
                    }
                    if (node instanceof FakeBlock) {
                        appendToDiff(getNewlines(1) + getIndent());
                        col = indent;
                    } else if (!fieldGroup || stat.getKind() != Tree.Kind.VARIABLE) {
                        blankLines();
                    }
                    scan(stat, p);
                }
            }
            if (isEmpty || templateEdit)
                newline();
            if (node instanceof FakeBlock) {
                indent = halfIndent;
                int i = tokens.index();
                boolean loop = true;
                while(loop) {
                    switch (tokens.token().id()) {
                        case WHITESPACE:
                            if (tokens.token().text().toString().indexOf('\n') < 0) {
                                tokens.moveNext();
                            } else {
                                loop = false;
                                appendToDiff("\n"); //NOI18N
                                col = 0;
                            }
                            break;
                        case LINE_COMMENT:
                            loop = false;
                        case BLOCK_COMMENT:
                            tokens.moveNext();
                            break;
                        default:
                            if (tokens.index() != i) {
                                tokens.moveIndex(i);
                                tokens.moveNext();
                            }
                            loop = false;
                            appendToDiff("\n"); //NOI18N
                            col = 0;
                    }
                }
                appendToDiff(getIndent() + "}"); //NOI18N
                col = indent + 1;
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
            } else {
                blankLines();
                indent = halfIndent;
                Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                if (diff != null && diff.end == tokens.offset()) {
                    if (diff.text != null) {
                        int idx = diff.text.lastIndexOf('\n'); //NOI18N
                        if (idx < 0)
                            diff.text = getIndent();
                        else
                            diff.text = diff.text.substring(0, idx + 1) + getIndent();

                    }
                    String spaces = diff.text != null ? diff.text : getIndent();
                    if (spaces.equals(fText.substring(diff.start, diff.end)))
                        diffs.removeFirst();
                } else if (tokens.movePrevious()) {
                    if (tokens.token().id() == WHITESPACE) {
                        String text =  tokens.token().text().toString();
                        int idx = text.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0) {
                            text = text.substring(idx + 1);
                            String ind = getIndent();
                            if (!ind.equals(text))
                                addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                        } else if (tokens.movePrevious()) {
                            if (tokens.token().id() == LINE_COMMENT) {
                                tokens.moveNext();
                                String ind = getIndent();
                                if (!ind.equals(text))
                                    addDiff(new Diff(tokens.offset(), tokens.offset() + tokens.token().length(), ind));

                            } else {
                                tokens.moveNext();
                            }
                        }
                    }
                    tokens.moveNext();
                }
                accept(RBRACE);
            }
            indent = old;
            return true;
        }       

        @Override
        public Boolean visitMemberSelect(MemberSelectTree node, Void p) {
            scan(node.getExpression(), p);
            accept(DOT);
            accept(IDENTIFIER, STAR, THIS, SUPER, CLASS);
            return true;
        }

        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
            ExpressionTree ms = node.getMethodSelect();
            if (ms.getKind() == Tree.Kind.MEMBER_SELECT) {
                ExpressionTree exp = ((MemberSelectTree)ms).getExpression();
                scan(exp, p);
                accept(DOT);
                List<? extends Tree> targs = node.getTypeArguments();
                if (targs != null && !targs.isEmpty()) {
                    if (LT == accept(LT))
                        tpLevel++;
                    for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
                        Tree targ = it.next();
                        scan(targ, p);
                        if (it.hasNext()) {
                            spaces(cs.spaceBeforeComma() ? 1 : 0);
                            accept(COMMA);
                            spaces(cs.spaceAfterComma() ? 1 : 0);
                        }
                    }
                    JavaTokenId accepted;
                    if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                        switch (accepted) {
                            case GTGTGT:
                                tpLevel -= 3;
                                break;
                            case GTGT:
                                tpLevel -= 2;
                                break;
                            case GT:
                                tpLevel--;
                                break;
                        }
                    }
                }
                CodeStyle.WrapStyle wrapStyle = cs.wrapChainedMethodCalls();
                if(exp.getKind() == Tree.Kind.METHOD_INVOCATION) {
                    wrapToken(wrapStyle, -1, 0, IDENTIFIER, THIS, SUPER);
                } else {
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    accept(IDENTIFIER, THIS, SUPER);
                    if (wrapStyle != CodeStyle.WrapStyle.WRAP_NEVER && col > rightMargin && c > indent) {
                        rollback(index, c, d);
                        newline();
                        accept(IDENTIFIER, THIS, SUPER);
                    }
                }
            } else {
                scan(node.getMethodSelect(), p);
            }
            spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);
            accept(LPAREN);
            List<? extends ExpressionTree> args = node.getArguments();
            if (args != null && !args.isEmpty()) {
                spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, args);
                spaces(cs.spaceWithinMethodCallParens() ? 1 : 0);            
            }
            accept(RPAREN);
            return true;
        }

        @Override
        public Boolean visitNewClass(NewClassTree node, Void p) {
            ExpressionTree encl = node.getEnclosingExpression();
            if (encl != null) {
                scan(encl, p);
                accept(DOT);
            }
            boolean indented = false;
            if (col == indent) {
                Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                if (d != null && d.getStartOffset() == tokens.offset() && d.getText() != null && d.getText().indexOf('\n') >= 0) {                    
                    indented = true;
                } else {
                    tokens.movePrevious();
                    if (tokens.token().id() == WHITESPACE && tokens.token().text().toString().indexOf('\n') >= 0)
                        indented = true;
                    tokens.moveNext();
                }
            }
            accept(NEW);
            space();
            List<? extends Tree> targs = node.getTypeArguments();
            if (targs != null && !targs.isEmpty()) {
                if (LT == accept(LT))
                    tpLevel++;
                for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
                    Tree targ = it.next();
                    scan(targ, p);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                JavaTokenId accepted;
                if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                    switch (accepted) {
                        case GTGTGT:
                            tpLevel -= 3;
                            break;
                        case GTGT:
                            tpLevel -= 2;
                            break;
                        case GT:
                            tpLevel--;
                            break;
                    }
                }
            }
            scan(node.getIdentifier(), p);
            spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);
            accept(LPAREN);
            List<? extends ExpressionTree> args = node.getArguments();
            if (args != null && !args.isEmpty()) {
                spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true); 
                wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, args);
                spaces(cs.spaceWithinMethodCallParens() ? 1 : 0);
            }
            accept(RPAREN);
            ClassTree body = node.getClassBody();
            if (body != null) {
                int old = indent;
                if (!indented)
                    indent -= continuationIndentSize;
                scan(body, p);
                indent = old;
            }
            return true;
        }

        @Override
        public Boolean visitAssert(AssertTree node, Void p) {
            accept(ASSERT);
            int old = indent;
            indent += continuationIndentSize;
            space();
            scan(node.getCondition(), p);
            ExpressionTree detail = node.getDetail();
            if (detail != null) {
                spaces(cs.spaceBeforeColon() ? 1 : 0);
                accept(COLON);
                wrapTree(cs.wrapAssert(), -1, cs.spaceAfterColon() ? 1 : 0, detail);
            }
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitReturn(ReturnTree node, Void p) {
            accept(RETURN);
            int old = indent;
            indent += continuationIndentSize;
            ExpressionTree exp = node.getExpression();
            if (exp != null) {
                space();
                scan(exp, p);
            }
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitThrow(ThrowTree node, Void p) {
            accept(THROW);
            int old = indent;
            indent += continuationIndentSize;
            ExpressionTree exp = node.getExpression();
            if (exp != null) {
                space();
                scan(exp, p);
            }
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitTry(TryTree node, Void p) {
            accept(TRY);
            scan(node.getBlock(), p);
            for (CatchTree catchTree : node.getCatches()) {
                if (cs.placeCatchOnNewLine())
                    newline();
                else
                    spaces(cs.spaceBeforeCatch() ? 1 : 0);
                scan(catchTree, p);
            }
            BlockTree finallyBlockTree = node.getFinallyBlock();
            if (finallyBlockTree != null) {
                if (cs.placeFinallyOnNewLine())
                    newline();
                else
                    spaces(cs.spaceBeforeFinally() ? 1 : 0);
                accept(FINALLY);
                scan(finallyBlockTree, p);
            }
            return true;
        }

        @Override
        public Boolean visitCatch(CatchTree node, Void p) {
            accept(CATCH);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeCatchParen() ? 1 : 0);
            accept(LPAREN);
            spaces(cs.spaceWithinCatchParens() ? 1 : 0);
            scan(node.getParameter(), p);
            spaces(cs.spaceWithinCatchParens() ? 1 : 0);
            accept(RPAREN);
            indent = old;
            scan(node.getBlock(), p);
            return true;
        }

        @Override
        public Boolean visitIf(IfTree node, Void p) {
            accept(IF);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeIfParen() ? 1 : 0);
            scan(node.getCondition(), p);
            indent = old;
            StatementTree elseStat = node.getElseStatement();
            CodeStyle.BracesGenerationStyle redundantIfBraces = cs.redundantIfBraces();
            if ((elseStat != null && redundantIfBraces == CodeStyle.BracesGenerationStyle.ELIMINATE && danglingElseChecker.hasDanglingElse(node.getThenStatement())) ||
                    (redundantIfBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < sp.getEndPosition(root, node))))
                redundantIfBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
            boolean prevblock = wrapStatement(cs.wrapIfStatement(), redundantIfBraces, cs.spaceBeforeIfLeftBrace() ? 1 : 0, node.getThenStatement());
            if (elseStat != null) {
                if (cs.placeElseOnNewLine() || !prevblock) {
                    newline();
                } else {
                    spaces(cs.spaceBeforeElse() ? 1 : 0);
                }
                accept(ELSE);
                if (elseStat.getKind() == Tree.Kind.IF && cs.specialElseIf()) {
                    space();
                    scan(elseStat, p);
                } else {
                    redundantIfBraces = cs.redundantIfBraces();
                    if (redundantIfBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < sp.getEndPosition(root, node)))
                        redundantIfBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
                    wrapStatement(cs.wrapIfStatement(), redundantIfBraces, cs.spaceBeforeElseLeftBrace() ? 1 : 0, elseStat);
                }
                indent = old;
            }
            return true;
        }

        @Override
        public Boolean visitDoWhileLoop(DoWhileLoopTree node, Void p) {
            accept(DO);
            int old = indent;
            CodeStyle.BracesGenerationStyle redundantDoWhileBraces = cs.redundantDoWhileBraces();
            if (redundantDoWhileBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < sp.getEndPosition(root, node)))
                redundantDoWhileBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
            boolean prevblock = wrapStatement(cs.wrapDoWhileStatement(), redundantDoWhileBraces, cs.spaceBeforeDoLeftBrace() ? 1 : 0, node.getStatement());
            if (cs.placeWhileOnNewLine() || !prevblock) {
                newline();
            } else {
                spaces(cs.spaceBeforeWhile() ? 1 : 0);
            }
            accept(WHILE);
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeWhileParen() ? 1 : 0);
            scan(node.getCondition(), p);
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitWhileLoop(WhileLoopTree node, Void p) {
            accept(WHILE);
            int old = indent;
            indent += continuationIndentSize;            
            spaces(cs.spaceBeforeWhileParen() ? 1 : 0);
            scan(node.getCondition(), p);
            indent = old;
            CodeStyle.BracesGenerationStyle redundantWhileBraces = cs.redundantWhileBraces();
            if (redundantWhileBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < sp.getEndPosition(root, node)))
                redundantWhileBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
            wrapStatement(cs.wrapWhileStatement(), redundantWhileBraces, cs.spaceBeforeWhileLeftBrace() ? 1 : 0, node.getStatement());
            return true;
        }

        @Override
        public Boolean visitForLoop(ForLoopTree node, Void p) {
            accept(FOR);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeForParen() ? 1 : 0);
            accept(LPAREN);
            spaces(cs.spaceWithinForParens() ? 1 : 0);
            List<? extends StatementTree> inits = node.getInitializer();
            int alignIndent = -1;
            if (inits != null && !inits.isEmpty()) {
                if (cs.alignMultilineFor())
                    alignIndent = col;
                for (Iterator<? extends StatementTree> it = inits.iterator(); it.hasNext();) {
                    scan(it.next(), p);
                    if (it.hasNext() && !fieldGroup) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                spaces(cs.spaceBeforeSemi() ? 1 : 0);
            }
            accept(SEMICOLON);
            ExpressionTree cond = node.getCondition();
            if (cond != null) {
                wrapTree(cs.wrapFor(), alignIndent, cs.spaceAfterSemi() ? 1 : 0, cond);
                spaces(cs.spaceBeforeSemi() ? 1 : 0);
            }
            accept(SEMICOLON);
            List<? extends ExpressionStatementTree> updates = node.getUpdate();
            if (updates != null && !updates.isEmpty()) {
                boolean first = true;
                for (Iterator<? extends ExpressionStatementTree> it = updates.iterator(); it.hasNext();) {
                    ExpressionStatementTree update = it.next();
                    if (first) {
                        wrapTree(cs.wrapFor(), alignIndent, cs.spaceAfterSemi() ? 1 : 0, update);
                    } else {
                        scan(update, p);
                    }
                    first = false;
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
            }
            spaces(cs.spaceWithinForParens() ? 1 : 0);
            accept(RPAREN);
            indent = old;
            CodeStyle.BracesGenerationStyle redundantForBraces = cs.redundantForBraces();
            if (redundantForBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < sp.getEndPosition(root, node)))
                redundantForBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
            wrapStatement(cs.wrapForStatement(), redundantForBraces, cs.spaceBeforeForLeftBrace() ? 1 : 0, node.getStatement());
            return true;            
        }

        @Override
        public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
            accept(FOR);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeForParen() ? 1 : 0);
            accept(LPAREN);
            spaces(cs.spaceWithinForParens() ? 1 : 0);
            int alignIndent = cs.alignMultilineFor() ? col : -1;
            scan(node.getVariable(), p);
            spaces(cs.spaceBeforeColon() ? 1 : 0);
            accept(COLON);
            wrapTree(cs.wrapFor(), alignIndent, cs.spaceAfterColon() ? 1 : 0, node.getExpression());
            spaces(cs.spaceWithinForParens() ? 1 : 0);
            accept(RPAREN);
            indent = old;
            CodeStyle.BracesGenerationStyle redundantForBraces = cs.redundantForBraces();
            if (redundantForBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < sp.getEndPosition(root, node)))
                redundantForBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
            wrapStatement(cs.wrapForStatement(), redundantForBraces, cs.spaceBeforeForLeftBrace() ? 1 : 0, node.getStatement());
            return true;
        }

        @Override
        public Boolean visitSynchronized(SynchronizedTree node, Void p) {
            accept(SYNCHRONIZED);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeSynchronizedParen() ? 1 : 0);
            scan(node.getExpression(), p);
            indent = old;
            scan(node.getBlock(), p);
            return true;
        }

        @Override
        public Boolean visitSwitch(SwitchTree node, Void p) {
            accept(SWITCH);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeSwitchParen() ? 1 : 0);
            scan(node.getExpression(), p);
            CodeStyle.BracePlacement bracePlacement = cs.getOtherBracePlacement();
            boolean spaceBeforeLeftBrace = cs.spaceBeforeSwitchLeftBrace();
            boolean indentCases = cs.indentCasesFromSwitch();
            indent = old;
            int halfIndent = indent;
            switch(bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0);
                    accept(LBRACE);
                    if (indentCases)
                        indent += indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    accept(LBRACE);
                    if (indentCases)
                        indent += indentSize;
                    break;
                case NEW_LINE_HALF_INDENTED:
                    indent += (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    if (indentCases)
                        indent = old + indentSize;
                    else
                        indent = old;
                    break;
                case NEW_LINE_INDENTED:
                    indent += indentSize;
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    if (!indentCases)
                        indent = old;
                    break;
            }
            for (CaseTree caseTree : node.getCases()) {
                blankLines();
                scan(caseTree, p);
            }
            blankLines();
            indent = halfIndent;
            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
            if (diff != null && diff.end == tokens.offset()) {
                if (diff.text != null) {
                    int idx = diff.text.lastIndexOf('\n'); //NOI18N
                    if (idx < 0)
                        diff.text = getIndent();
                    else
                        diff.text = diff.text.substring(0, idx + 1) + getIndent();
                    
                }
                String spaces = diff.text != null ? diff.text : getIndent();
                if (spaces.equals(fText.substring(diff.start, diff.end)))
                    diffs.removeFirst();
            } else if (tokens.movePrevious()) {
                if (tokens.token().id() == WHITESPACE) {
                    String text =  tokens.token().text().toString();
                    int idx = text.lastIndexOf('\n'); //NOI18N
                    if (idx >= 0) {
                        text = text.substring(idx + 1);
                        String ind = getIndent();
                        if (!ind.equals(text))
                            addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                    }
                }
                tokens.moveNext();
            }
            accept(RBRACE);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitCase(CaseTree node, Void p) {
            ExpressionTree exp = node.getExpression();
            if (exp != null) {
                accept(CASE);
                space();
                scan(exp, p);
            } else {
                accept(DEFAULT);
            }
            accept(COLON);
            int old = indent;
            indent += indentSize;
            for (StatementTree stat : node.getStatements()) {
                if (stat.getKind() == Tree.Kind.BLOCK) {
                    indent = old;
                    scan(stat, p);
                } else {
                    blankLines();
                    scan(stat, p);
                }
            }
            indent = old;
            return true;
        }

        @Override
        public Boolean visitBreak(BreakTree node, Void p) {
            accept(BREAK);
            Name label = node.getLabel();
            if (label != null) {
                space();
                accept(IDENTIFIER);
            }
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitContinue(ContinueTree node, Void p) {
            accept(CONTINUE);
            Name label = node.getLabel();
            if (label != null) {
                space();
                accept(IDENTIFIER);
            }
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitAssignment(AssignmentTree node, Void p) {
            int alignIndent = cs.alignMultilineAssignment() ? col : -1;
            boolean b = scan(node.getVariable(), p);
            if (b || getCurrentPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION) {
                spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                accept(EQ);
                ExpressionTree expr = node.getExpression();
                if (expr.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree)expr).getType() == null) {
                    if (cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE)
                        spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                    scan(expr, p);
                } else {
                    if (wrapAnnotation && expr.getKind() == Tree.Kind.ANNOTATION) {
                        wrapTree(CodeStyle.WrapStyle.WRAP_ALWAYS, alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, expr);
                    } else {
                        wrapTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, expr);
                    }
                }
            } else {
                scan(node.getExpression(), p);
            }
            return true;
        }

        @Override
        public Boolean visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
            int alignIndent = cs.alignMultilineAssignment() ? col : -1;
            scan(node.getVariable(), p);
            spaces(cs.spaceAroundAssignOps() ? 1 : 0);
            if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                col += tokens.token().length();
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
                tokens.moveNext();
            }
            wrapTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, node.getExpression());
            return true;
        }

        @Override
        public Boolean visitPrimitiveType(PrimitiveTypeTree node, Void p) {
            switch (node.getPrimitiveTypeKind()) {
            case BOOLEAN:
                accept(BOOLEAN);
                break;
            case BYTE:
                accept(BYTE);
                break;
            case CHAR:
                accept(CHAR);
                break;
            case DOUBLE:
                accept(DOUBLE);
                break;
            case FLOAT:
                accept(FLOAT);
                break;
            case INT: 
                accept(INT);
                break;
            case LONG:
                accept(LONG);
                break;
            case SHORT:
                accept(SHORT);
                break;
            case VOID:
                accept(VOID);
                break;
            }
            return true;
        }

        @Override
        public Boolean visitArrayType(ArrayTypeTree node, Void p) {
            boolean ret = scan(node.getType(), p);
            int index = tokens.index();
            int c = col;
            Diff d = diffs.isEmpty() ? null : diffs.getFirst();
            JavaTokenId id = accept(LBRACKET, ELLIPSIS, IDENTIFIER);
            if (id == ELLIPSIS)
                return ret;
            if (id != IDENTIFIER) {
                accept(RBRACKET);
                return ret;
            }
            rollback(index, c, d);
            spaces(1, fieldGroup);
            accept(IDENTIFIER);
            accept(LBRACKET);
            accept(RBRACKET);
            return false;
        }

        @Override
        public Boolean visitArrayAccess(ArrayAccessTree node, Void p) {
            scan(node.getExpression(), p);
            accept(LBRACKET);
            scan(node.getIndex(), p);
            accept(RBRACKET);
            return true;
        }

        @Override
        public Boolean visitNewArray(NewArrayTree node, Void p) {
            Tree type = node.getType();
            List<? extends ExpressionTree> inits = node.getInitializers();
            if (type != null) {
                accept(NEW);
                space();
                int n = inits != null ? 1 : 0;
                while (type.getKind() == Tree.Kind.ARRAY_TYPE) {
                    n++;
                    type = ((ArrayTypeTree)type).getType();
                }
                scan(type, p);
                for (ExpressionTree dim : node.getDimensions()) {
                    accept(LBRACKET);
                    spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
                    scan(dim, p);
                    spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
                    accept(RBRACKET);
                }
                while(--n >= 0) {
                    accept(LBRACKET);
                    accept(RBRACKET);
                }
            }
            if (inits != null) {
                CodeStyle.BracePlacement bracePlacement = cs.getOtherBracePlacement();
                boolean spaceBeforeLeftBrace = cs.spaceBeforeArrayInitLeftBrace();
                int oldIndent = indent;
                Tree parent = getCurrentPath().getParentPath().getLeaf();
                switch (parent.getKind()) {
                    case ASSIGNMENT:
                        Tree grandParent = getCurrentPath().getParentPath().getParentPath().getLeaf();
                        if (grandParent.getKind() != Tree.Kind.BLOCK && grandParent.getKind() != Tree.Kind.CLASS)
                            break;
                    case VARIABLE:
                    case METHOD:
                        indent -= continuationIndentSize;
                        break;
                }
                int old = indent;
                int halfIndent = indent;
                switch(bracePlacement) {
                    case SAME_LINE:
                        if (type != null)
                            spaces(spaceBeforeLeftBrace ? 1 : 0);
                        accept(LBRACE);
                        indent += indentSize;
                        break;
                    case NEW_LINE:
                        newline();
                        accept(LBRACE);
                        indent += indentSize;
                        break;
                    case NEW_LINE_HALF_INDENTED:
                        indent += (indentSize >> 1);
                        halfIndent = indent;
                        newline();
                        accept(LBRACE);
                        indent = old + indentSize;
                        break;
                    case NEW_LINE_INDENTED:
                        indent += indentSize;
                        halfIndent = indent;
                        newline();
                        accept(LBRACE);
                        break;
                }
                boolean afterNewline = bracePlacement != CodeStyle.BracePlacement.SAME_LINE;
                if (!inits.isEmpty()) {
                    if (afterNewline)
                        newline();
                    else
                        spaces(cs.spaceWithinBraces() ? 1 : 0, true);
                    wrapList(cs.wrapArrayInit(), cs.alignMultilineArrayInit(), false, inits);
                    if (tokens.token().text().toString().indexOf('\n') >= 0)
                        afterNewline = true;
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    if (accept(COMMA) == null)
                        rollback(index, c, d);
                    indent -= indentSize;
                    if (afterNewline)
                        newline();
                    else
                        spaces(cs.spaceWithinBraces() ? 1 : 0);
                } else if (afterNewline) {
                    newline();
                }
                indent = halfIndent;
                if (afterNewline) {
                    Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                    if (diff != null && diff.end == tokens.offset()) {
                        if (diff.text != null) {
                            int idx = diff.text.lastIndexOf('\n'); //NOI18N
                            if (idx < 0)
                                diff.text = getIndent();
                            else
                                diff.text = diff.text.substring(0, idx + 1) + getIndent();

                        }
                        String spaces = diff.text != null ? diff.text : getIndent();
                        if (spaces.equals(fText.substring(diff.start, diff.end)))
                            diffs.removeFirst();
                    } else if (tokens.movePrevious()) {
                        if (tokens.token().id() == WHITESPACE) {
                            String text =  tokens.token().text().toString();
                            int idx = text.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                text = text.substring(idx + 1);
                                String ind = getIndent();
                                if (!ind.equals(text))
                                    addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                            }
                        }
                        tokens.moveNext();
                    }
                }
                accept(RBRACE);
                indent = oldIndent;
            }
            return true;
        }

        @Override
        public Boolean visitIdentifier(IdentifierTree node, Void p) {
            accept(IDENTIFIER, THIS, SUPER);
            return true;
        }

        @Override
        public Boolean visitUnary(UnaryTree node, Void p) {
            JavaTokenId id = tokens.token().id();
            if (OPERATOR.equals(id.primaryCategory())) {
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                col += tokens.token().length();
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
                tokens.moveNext();
                int index = tokens.index();
                int c = col;
                Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                if (tokens.token().id() == id) {
                    rollback(index, c, d);
                    space();
                }
                scan(node.getExpression(), p);
            } else {
                scan(node.getExpression(), p);
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                col += tokens.token().length();
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
                tokens.moveNext();
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
            }
            return true;
        }

        @Override
        public Boolean visitBinary(BinaryTree node, Void p) {
            int alignIndent = cs.alignMultilineBinaryOp() ? col : -1;
            scan(node.getLeftOperand(), p);
            wrapOperatorAndTree(cs.wrapBinaryOps(), alignIndent, cs.spaceAroundBinaryOps() ? 1 : 0, node.getRightOperand());
            return true;
        }

        @Override
        public Boolean visitConditionalExpression(ConditionalExpressionTree node, Void p) {
            int alignIndent = cs.alignMultilineTernaryOp() ? col : -1;
            scan(node.getCondition(), p);
            wrapOperatorAndTree(cs.wrapTernaryOps(), alignIndent, cs.spaceAroundTernaryOps() ? 1 : 0, node.getTrueExpression());
            wrapOperatorAndTree(cs.wrapTernaryOps(), alignIndent, cs.spaceAroundTernaryOps() ? 1 : 0, node.getFalseExpression());
            return true;
        }

        @Override
        public Boolean visitEmptyStatement(EmptyStatementTree node, Void p) {
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitExpressionStatement(ExpressionStatementTree node, Void p) {
            int old = indent;
            indent += continuationIndentSize;
            scan(node.getExpression(), p);
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitInstanceOf(InstanceOfTree node, Void p) {
            scan(node.getExpression(), p);
            space();
            accept(INSTANCEOF);
            space();
            scan(node.getType(), p);
            return true;
        }

        @Override
        public Boolean visitLabeledStatement(LabeledStatementTree node, Void p) {
            if (!ERROR.contentEquals(node.getLabel()))
                accept(IDENTIFIER);            
            accept(COLON);
            int old = indent;
            indent += cs.getLabelIndent();
            int cnt = indent - col;
            if (cnt < 0)
                newline();
            else
                spaces(cnt);
            scan(node.getStatement(), p);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitTypeCast(TypeCastTree node, Void p) {
            accept(LPAREN);
            boolean spaceWithinParens = cs.spaceWithinTypeCastParens();
            spaces(spaceWithinParens ? 1 : 0);
            scan(node.getType(), p);
            spaces(spaceWithinParens ? 1 : 0);
            accept(RPAREN);
            spaces(cs.spaceAfterTypeCast() ? 1 : 0);
            scan(node.getExpression(), p);
            return true;
        }

        @Override
        public Boolean visitParenthesized(ParenthesizedTree node, Void p) {
            accept(LPAREN);
            boolean spaceWithinParens;
            int old = indent;
            switch(getCurrentPath().getParentPath().getLeaf().getKind()) {
                case IF:
                    spaceWithinParens = cs.spaceWithinIfParens();
                    break;
                case FOR_LOOP:
                    spaceWithinParens = cs.spaceWithinForParens();
                    break;
                case DO_WHILE_LOOP:
                case WHILE_LOOP:
                    spaceWithinParens = cs.spaceWithinWhileParens();
                    break;
                case SWITCH:
                    spaceWithinParens = cs.spaceWithinSwitchParens();
                    break;
                case SYNCHRONIZED:
                    spaceWithinParens = cs.spaceWithinSynchronizedParens();
                    break;
                default:
                    spaceWithinParens = cs.spaceWithinParens();
                    if (cs.alignMultilineParenthesized())
                        indent = col;
            }
            spaces(spaceWithinParens ? 1 : 0);
            scan(node.getExpression(), p);
            spaces(spaceWithinParens ? 1 : 0);
            indent = old;
            accept(RPAREN);
            return true;
        }

        @Override
        public Boolean visitLiteral(LiteralTree node, Void p) {
            do {
                col += tokens.token().length();
            } while (tokens.moveNext() && tokens.offset() < endPos);
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            lastBlankLinesDiff = null;
            return true;
        }

        @Override
        public Boolean visitErroneous(ErroneousTree node, Void p) {
            for (Tree tree : node.getErrorTrees()) {
                int pos = (int)sp.getStartPosition(getCurrentPath().getCompilationUnit(), tree);
                do {
                    col += tokens.token().length();
                } while (tokens.moveNext() && tokens.offset() < pos);
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
                scan(tree, p);
            }
            do {
                col += tokens.token().length();
            } while (tokens.moveNext() && tokens.offset() < endPos);
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            lastBlankLinesDiff = null;
            return true;
        }

        @Override
        public Boolean visitOther(Tree node, Void p) {
            do {
                col += tokens.token().length();
            } while (tokens.moveNext() && tokens.offset() < endPos);
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            lastBlankLinesDiff = null;
            return true;
        }

        private JavaTokenId accept(JavaTokenId first, JavaTokenId... rest) {
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            lastBlankLinesDiff = null;
            EnumSet<JavaTokenId> tokenIds = EnumSet.of(first, rest);
            Token<JavaTokenId> lastWSToken = null;
            int after = 0;
            do {
                if (tokens.offset() >= endPos) {
                    if (lastWSToken != null) {
                        lastBlankLines = 0;
                        lastBlankLinesTokenIndex = tokens.index() - 1;
                        lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                    }
                    return null;
                }
                JavaTokenId id = tokens.token().id();
                if (tokenIds.contains(id)) {
                    String spaces = after == 1 //after line comment
                            ? getIndent()
                            : after == 2 //after javadoc comment
                            ? getNewlines(1) + getIndent()
                            : null;
                    if (lastWSToken != null) {
                        if (spaces == null || !spaces.contentEquals(lastWSToken.text()))
                            addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                    } else {
                        if (spaces != null && spaces.length() > 0)
                            addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                    }
                    if (after > 0)
                        col = indent;
                    col += tokens.token().length();
                    return tokens.moveNext() ? id : null;
                }
                switch(id) {
                    case WHITESPACE:
                        lastWSToken = tokens.token();
                        break;
                    case LINE_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                        }
                        col = 0;
                        after = 1; //line comment
                        break;
                    case JAVADOC_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent;
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent;
                        }
                        String tokenText = tokens.token().text().toString();
                        int idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        indentComment();
                        after = 2; //javadoc comment
                        break;
                    case BLOCK_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent;
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent;
                        }
                        tokenText = tokens.token().text().toString();
                        idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        indentComment();
                        after = 0;
                        break;
                    default:
                        return null;
                }
            } while(tokens.moveNext());
            return null;
        }

        private void space() {
            spaces(1);
        }

        private void spaces(int count) {
            spaces(count, false);
        }
        
        private boolean spaces(int count, boolean preserveNewline) {
            Token<JavaTokenId> lastWSToken = null;
            boolean containedNewLine = false;
            int after = 0;
            do {
                if (tokens.offset() >= endPos)
                    return containedNewLine;
                switch(tokens.token().id()) {
                    case WHITESPACE:
                        lastWSToken = tokens.token();
                        break;
                    case LINE_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            String text = lastWSToken.text().toString();
                            int idx = text.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                containedNewLine = true;
                                if (preserveNewline) {
                                    spaces = getNewlines(1) + getIndent();
                                    lastBlankLines = 1;
                                    lastBlankLinesTokenIndex = tokens.index();
                                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                        }
                        col = 0;
                        after = 1; //line comment
                        break;
                    case JAVADOC_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            String text = lastWSToken.text().toString();
                            int idx = text.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                containedNewLine = true;
                                if (preserveNewline) {
                                    spaces = getNewlines(1) + getIndent();
                                    after = 3;
                                    lastBlankLines = 1;
                                    lastBlankLinesTokenIndex = tokens.index();
                                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent;
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent;
                        }
                        String tokenText = tokens.token().text().toString();
                        int idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        indentComment();
                        after = 2; //javadoc comment
                        break;
                    case BLOCK_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            String text = lastWSToken.text().toString();
                            idx = text.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                containedNewLine = true;
                                if (preserveNewline) {
                                    spaces = getNewlines(1) + getIndent();
                                    after = 3;
                                    lastBlankLines = 1;
                                    lastBlankLinesTokenIndex = tokens.index();
                                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent;
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent;
                        }
                        tokenText = tokens.token().text().toString();
                        idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        indentComment();
                        after = 0;
                        break;
                    default:
                        String spaces = after == 1 //after line comment
                                ? getIndent()
                                : after == 2 //after javadoc comment
                                ? getNewlines(1) + getIndent()
                                : getSpaces(count);
                        if (lastWSToken != null) {
                            String text = lastWSToken.text().toString();
                            idx = text.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                containedNewLine = true;
                                if (preserveNewline) {
                                    spaces = getNewlines(1) + getIndent();
                                    after = 3;
                                    lastBlankLines = 1;
                                    lastBlankLinesTokenIndex = tokens.index();
                                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                        } else if (spaces.length() > 0) {
                            addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                        }
                        if (after > 0)
                            col = indent;
                        else
                            col += count;
                        return containedNewLine;
                }
            } while(tokens.moveNext());
            return containedNewLine;
        }

        private void newline() {
            blankLines(templateEdit ? ANY_COUNT : 0);
        }

        private void blankLines() {
            blankLines(ANY_COUNT);
        }

        private void blankLines(int count) {
            if (count >= 0) {
                if (lastBlankLinesTokenIndex < 0) {
                    lastBlankLines = count;
                    lastBlankLinesTokenIndex = tokens.index();
                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                } else if (lastBlankLines < count) {
                    lastBlankLines = count;
                    rollback(lastBlankLinesTokenIndex, lastBlankLinesTokenIndex, lastBlankLinesDiff);
                } else {
                    return;
                }
            } else {
                if (lastBlankLinesTokenIndex < 0) {
                    lastBlankLinesTokenIndex = tokens.index();
                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                } else {
                    return;
                }
            }        
            Token<JavaTokenId> lastToken = null;
            int after = 0;
            do {
                if (tokens.offset() >= endPos)
                    return;
                switch(tokens.token().id()) {
                    case WHITESPACE:
                        lastToken = tokens.token();
                        break;
                    case BLOCK_COMMENT:
                        if (count >= 0 && tokens.index() > 1 && after != 1)
                            count++;
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                count--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx > 0) {
                                String indent = getIndent();
                                if (!indent.contentEquals(text.substring(lastIdx)))
                                    addDiff(new Diff(offset + lastIdx, tokens.offset(), indent));
                            }
                            lastToken = null;
                        }
                        indentComment();
                        after = 3;
                        break;
                    case JAVADOC_COMMENT:
                        if (count >= 0 && tokens.index() > 1 && after != 1)
                            count++;
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                count--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                after = 0;
                                if (idx >= lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx == 0 && count < 0 && after != 1) {
                                count = count == ANY_COUNT ? 1 : 0;
                            }
                            String indent = after == 3 ? SPACE : getNewlines(count) + getIndent();
                            if (!indent.contentEquals(text.substring(lastIdx)))
                                addDiff(new Diff(offset + lastIdx, tokens.offset(), indent));
                            lastToken = null;
                        } else {
                            if (lastBlankLines < 0 && count == ANY_COUNT)
                                count = lastBlankLines = 1;
                            String text = getNewlines(count) + getIndent();
                            if (text.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), text));
                        }
                        indentComment();
                        count = 0;
                        after = 2;
                        break;
                    case LINE_COMMENT:
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            if (count >= 0 && tokens.index() > 1 && after != 1 && text.indexOf('\n') >= 0)
                                count++;
                            int idx = 0;
                            int lastIdx = 0;
                            while(count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                count--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                if (idx >= lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx == 0 && after == 1) {
                                String indent = getIndent();
                                if (!indent.contentEquals(text))
                                    addDiff(new Diff(offset, tokens.offset(), indent));
                            } else if (lastIdx > 0 && lastIdx < lastToken.length()) {
                                String indent = getIndent();
                                if (!indent.contentEquals(text.substring(lastIdx)))
                                    addDiff(new Diff(offset + lastIdx, tokens.offset(), indent));
                            }
                            lastToken = null;
                        }
                        after = 1;
                        break;
                    default:
                        if (count >= 0 && tokens.index() > 1 && after != 1)
                            count++;
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx, templateEdit ? getIndent() : null));
                                lastIdx = idx + 1;
                                count--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                after = 0;
                                if (idx >= lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx == 0 && count < 0 && after != 1) {
                                count = count == ANY_COUNT ? 1 : 0;
                            }
                            String indent = after == 3 ? SPACE : getNewlines(count) + getIndent();
                            if (!indent.contentEquals(text.substring(lastIdx)))
                                addDiff(new Diff(offset + lastIdx, tokens.offset(), indent));
                        } else {
                            if (lastBlankLines < 0 && count == ANY_COUNT)
                                count = lastBlankLines = 1;
                            String text = after == 1 ? getIndent() : getNewlines(count) + getIndent();
                            if (text.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), text));
                        }
                        col = indent;
                        return;
                }
            } while(tokens.moveNext());
        }

        private void rollback(int index, int col, Diff diff) {
            tokens.moveIndex(index);
            tokens.moveNext();
            if (diff == null) {
                diffs.clear();
            } else {
                while (!diffs.isEmpty() && diffs.getFirst() != diff)
                    diffs.removeFirst();
            }
            this.col = col;
        }
        
        private void appendToDiff(String s) {
            int offset = tokens.offset();
            Diff d = diffs.isEmpty() ? null : diffs.getFirst();
            if (d != null && d.getEndOffset() == offset) {
                d.text += s;
            } else {
                addDiff(new Diff(offset, offset, s));
            }    
        }

        private void addDiff(Diff diff) {
            Diff d = diffs.isEmpty() ? null : diffs.getFirst();
            if (d == null || d.getStartOffset() <= diff.getStartOffset())
                diffs.addFirst(diff);
        }

        private int wrapToken(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, JavaTokenId first, JavaTokenId... rest) {
            int ret = -1;
            switch (wrapStyle) {
                case WRAP_ALWAYS:
                    int old = indent;
                    if (alignIndent >= 0)
                        indent = alignIndent;
                    newline();
                    indent = old;
                    ret = col;
                    accept(first, rest);
                    break;
                case WRAP_IF_LONG:
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    old = indent;
                    if (alignIndent >= 0)
                        indent = alignIndent;
                    spaces(spacesCnt, true);
                    indent = old;
                    ret = col;
                    accept(first, rest);
                    if (this.col > rightMargin) {
                        rollback(index, c, d);
                        old = indent;
                        if (alignIndent >= 0)
                            indent = alignIndent;
                        newline();
                        indent = old;
                        ret = col;
                        accept(first, rest);
                    }
                    break;
                case WRAP_NEVER:
                    old = indent;
                    if (alignIndent >= 0)
                        indent = alignIndent;
                    spaces(spacesCnt, true);
                    indent = old;
                    ret = col;
                    accept(first, rest);
                    break;
            }
            return ret;
        }

        private int wrapTree(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, Tree tree) {
            int ret = -1;
            switch (wrapStyle) {
                case WRAP_ALWAYS:
                    int old = indent;
                    if (alignIndent >= 0)
                        indent = alignIndent;
                    newline();
                    indent = old;
                    ret = col;
                    scan(tree, null);
                    break;
                case WRAP_IF_LONG:
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    old = indent;
                    if (alignIndent >= 0)
                        indent = alignIndent;
                    spaces(spacesCnt, true);
                    indent = old;
                    ret = col;
                    scan(tree, null);
                    if (col > rightMargin) {
                        rollback(index, c, d);
                        old = indent;
                        if (alignIndent >= 0)
                            indent = alignIndent;
                        newline();
                        indent = old;
                        ret = col;
                        scan(tree, null);
                    }
                    break;
                case WRAP_NEVER:
                    old = indent;
                    if (alignIndent >= 0)
                        indent = alignIndent;
                    spaces(spacesCnt, true);
                    indent = old;
                    ret = col;
                    scan(tree, null);
                    break;
            }
            return ret;
        }

        private int wrapOperatorAndTree(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, Tree tree) {
            int ret = -1;
            switch (wrapStyle) {
                case WRAP_ALWAYS:
                    int old = indent;
                    if (alignIndent >= 0)
                        indent = alignIndent;
                    newline();
                    indent = old;
                    ret = col;
                    if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                        col += tokens.token().length();
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        tokens.moveNext();
                    }
                    spaces(spacesCnt);
                    scan(tree, null);
                    break;
                case WRAP_IF_LONG:
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    old = indent;
                    if (alignIndent >= 0)
                        indent = alignIndent;
                    spaces(spacesCnt, true);
                    indent = old;
                    ret = col;
                    if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                        col += tokens.token().length();
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        tokens.moveNext();
                    }
                    spaces(spacesCnt);
                    scan(tree, null);
                    if (col > rightMargin) {
                        rollback(index, c, d);
                        old = indent;
                        if (alignIndent >= 0)
                            indent = alignIndent;
                        newline();
                        indent = old;
                        ret = col;
                        if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                            col += tokens.token().length();
                            lastBlankLines = -1;
                            lastBlankLinesTokenIndex = -1;
                            tokens.moveNext();
                        }
                        spaces(spacesCnt);
                        scan(tree, null);
                    }
                    break;
                case WRAP_NEVER:
                    index = tokens.index();
                    c = col;
                    d = diffs.isEmpty() ? null : diffs.getFirst();
                    old = indent;
                    if (alignIndent >= 0)
                        indent = alignIndent;
                    spaces(spacesCnt, true);
                    indent = old;
                    ret = col;
                    if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                        col += tokens.token().length();
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        tokens.moveNext();
                    }
                    if (spaces(spacesCnt, false)) {
                        rollback(index, c, d);
                        old = indent;
                        if (alignIndent >= 0)
                            indent = alignIndent;
                        newline();
                        indent = old;
                        ret = col;
                        if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                            col += tokens.token().length();
                            lastBlankLines = -1;
                            lastBlankLinesTokenIndex = -1;
                            tokens.moveNext();
                        }
                        spaces(spacesCnt);
                    }
                    scan(tree, null);
                    break;
            }
            return ret;
        }

        private boolean wrapStatement(CodeStyle.WrapStyle wrapStyle, CodeStyle.BracesGenerationStyle bracesGenerationStyle, int spacesCnt, StatementTree tree) {
            if (tree.getKind() == Tree.Kind.EMPTY_STATEMENT) {
                scan(tree, null);
                return true;
            }
            if (tree.getKind() == Tree.Kind.BLOCK) {
                if (bracesGenerationStyle == CodeStyle.BracesGenerationStyle.ELIMINATE) {
                    Iterator<? extends StatementTree> stats = ((BlockTree)tree).getStatements().iterator();
                    if (stats.hasNext()) {
                        StatementTree stat = stats.next();
                        if (!stats.hasNext() && stat.getKind() != Tree.Kind.VARIABLE) {
                            int start = tokens.offset();
                            accept(LBRACE);
                            Diff d;
                            while (!diffs.isEmpty() && (d = diffs.getFirst()) != null && d.getStartOffset() >= start)
                                diffs.removeFirst();
                            addDiff(new Diff(start, tokens.offset(), null));
                            int old = indent;
                            indent += indentSize;
                            wrapTree(wrapStyle, -1, spacesCnt, stat);
                            indent = old;
                            start = tokens.offset();
                            accept(RBRACE);
                            while (!diffs.isEmpty() && (d = diffs.getFirst()) != null && d.getStartOffset() >= start)
                                diffs.removeFirst();
                            addDiff(new Diff(start, tokens.offset(), null));
                            return false;
                        }
                    }
                }
                scan(tree, null);
                return true;
            }
            if (bracesGenerationStyle == CodeStyle.BracesGenerationStyle.GENERATE) {
                scan(new FakeBlock(tree), null);
                return true;
            }
            int old = indent;
            indent += indentSize;
            int ret = wrapTree(wrapStyle, -1, spacesCnt, tree);
            indent = old;
            return false;
        }

        private void wrapList(CodeStyle.WrapStyle wrapStyle, boolean align, boolean prependSpace, List<? extends Tree> trees) {
            boolean first = true;
            int alignIndent = -1;
            for (Iterator<? extends Tree> it = trees.iterator(); it.hasNext();) {
                Tree impl = it.next();
                if (wrapAnnotation && impl.getKind() == Tree.Kind.ANNOTATION) {
                    wrapTree(CodeStyle.WrapStyle.WRAP_ALWAYS, alignIndent, cs.spaceAfterComma() ? 1 : 0, impl);
                } else if (impl.getKind() == Tree.Kind.ERRONEOUS) {
                    scan(impl, null);
                } else if (first) {
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    if (prependSpace)
                        spaces(1, true);
                    if (align)
                        alignIndent = col;
                    scan(impl, null);
                    if (wrapStyle != CodeStyle.WrapStyle.WRAP_NEVER && col > rightMargin && c > indent) {
                        rollback(index, c, d);
                        newline();
                        scan(impl, null);
                    }
                } else {
                    wrapTree(wrapStyle, alignIndent, cs.spaceAfterComma() ? 1 : 0, impl);
                }
                first = false;
                if (it.hasNext()) {
                    spaces(cs.spaceBeforeComma() ? 1 : 0);
                    accept(COMMA);
                }
            }
        }
        
        private void indentComment() {
            if (tokens.token().id() != BLOCK_COMMENT && tokens.token().id() != JAVADOC_COMMENT)
                return;
            String indent = getIndent();
            String text = tokens.token().text().toString();
            int idx = 0;
            while ((idx = text.indexOf('\n', idx)) >= 0) { //NOI18N
                int i = idx + 1;
                while(i < text.length() && text.charAt(i) <= ' ' && text.charAt(i) != '\n') //NOI18N
                    i++;
                if (i >= text.length())
                    break;
                String s = text.charAt(i) == '*' ? indent + SPACE : indent;
                if (!s.equals(text.substring(idx + 1, i)))
                    addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + i, s)); //NOI18N
                idx = i;
            }
        }

        private String getSpaces(int count) {
            if (count <= 0)
                return EMPTY;
            if (count == 1)
                return SPACE;
            StringBuilder sb = new StringBuilder(); 
            while (count-- > 0)
                sb.append(' '); //NOI18N
            return sb.toString();
        }

        private String getNewlines(int count) {
            if (count <= 0)
                return EMPTY;
            if (count == 1)
                return NEWLINE;
            StringBuilder sb = new StringBuilder(); 
            while (count-- > 0)
                sb.append('\n'); //NOI18N
            return sb.toString();
        }

        private String getIndent() {
            StringBuilder sb = new StringBuilder(); 
            int col = 0;
            if (!expandTabToSpaces) {
                while (col + tabSize <= indent) {
                    sb.append('\t'); //NOI18N
                    col += tabSize;
                }
            }
            while (col < indent) {
                sb.append(SPACE); //NOI18N
                col++;
            }
            return sb.toString();
        }

        private int getIndentLevel(TokenSequence<JavaTokenId> tokens, TreePath path) {
            if (path.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT)
                return 0;
            Tree lastTree = null;
            int indent = -1;
            while (path != null) {
                int offset = (int)sp.getStartPosition(path.getCompilationUnit(), path.getLeaf());
                if (offset < 0)
                    return indent;
                tokens.move(offset);
                String text = null;
                while (tokens.movePrevious()) {
                    Token<JavaTokenId> token = tokens.token();
                    if (token.id() == WHITESPACE) {
                        text = token.text().toString();
                        int idx = text.lastIndexOf('\n');
                        if (idx >= 0) {
                            text = text.substring(idx + 1);
                            indent = getCol(text);
                            break;
                        }
                    } else if (token.id() == LINE_COMMENT) {
                        indent = text != null ? getCol(text) : 0;
                        break;
                    } else if (token.id() == BLOCK_COMMENT || token.id() == JAVADOC_COMMENT) {
                        text = null;
                    } else {
                        break;
                    }
                }
                if (indent >= 0)
                    break;
                lastTree = path.getLeaf();
                path = path.getParentPath();
            }
            if (lastTree != null && path != null) {
                switch (path.getLeaf().getKind()) {
                case CLASS:
                    for (Tree tree : ((ClassTree)path.getLeaf()).getMembers()) {
                        if (tree == lastTree) {
                            indent += tabSize;
                            break;
                        }
                    }
                    break;
                case BLOCK:
                    for (Tree tree : ((BlockTree)path.getLeaf()).getStatements()) {
                        if (tree == lastTree) {
                            indent += tabSize;
                            break;
                        }
                    }
                    break;
                }
            }
            return indent;
        }

        private int getCol(String text) {
            int col = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\t') {
                    col += tabSize;
                    col -= (col % tabSize);
                } else {
                    col++;
                }
            }
            return col;
        }

        private boolean isEnumerator(VariableTree tree) {
            return (((JCModifiers)tree.getModifiers()).flags & Flags.ENUM) != 0;
        }
        
        private boolean isSynthetic(CompilationUnitTree cut, Tree leaf) {
            JCTree tree = (JCTree) leaf;
            if (tree.pos == (-1))
                return true;
            if (leaf.getKind() == Tree.Kind.METHOD) {
                //check for synthetic constructor:
                return (((JCMethodDecl)leaf).mods.flags & Flags.GENERATEDCONSTR) != 0L;
            }
            //check for synthetic superconstructor call:
            if (leaf.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                ExpressionStatementTree est = (ExpressionStatementTree) leaf;
                if (est.getExpression().getKind() == Tree.Kind.METHOD_INVOCATION) {
                    MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                    if (mit.getMethodSelect().getKind() == Tree.Kind.IDENTIFIER) {
                        IdentifierTree it = (IdentifierTree) mit.getMethodSelect();
                        if ("super".equals(it.getName().toString())) {
                            return sp.getEndPosition(cut, leaf) == (-1);
                        }
                    }
                }
            }
            return false;
        }
    
        private static class FakeBlock extends JCBlock {
            
            private StatementTree stat;
            
            private FakeBlock(StatementTree stat) {
                super(0L, com.sun.tools.javac.util.List.of((JCStatement)stat));
                this.stat = stat;
            }
        }
        
        private static class DanglingElseChecker extends SimpleTreeVisitor<Void, Void> {

            private boolean foundDanglingElse;

            public boolean hasDanglingElse(Tree t) {
                if (t == null)
                    return false;
                foundDanglingElse = false;
                visit(t, null);
                return foundDanglingElse;
            }

            @Override
            public Void visitBlock(BlockTree node, Void p) {
                // Do dangling else checks on single statement blocks since
                // they often get eliminated and replaced by their constained statement
                Iterator<? extends StatementTree> it = node.getStatements().iterator();
                StatementTree stat = it.hasNext() ? it.next() : null;
                if (stat != null && !it.hasNext())
                    visit(stat, p);
                return null;
            }

            @Override
            public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
                return visit(node.getStatement(), p);
            }

            @Override
            public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
                return visit(node.getStatement(), p);
            }

            @Override
            public Void visitForLoop(ForLoopTree node, Void p) {
                return visit(node.getStatement(), p);
            }

            @Override
            public Void visitIf(IfTree node, Void p) {
                if (node.getElseStatement() == null)
                    foundDanglingElse = true;
                else
                    visit(node.getElseStatement(), p);
                return null;
            }

            @Override
            public Void visitLabeledStatement(LabeledStatementTree node, Void p) {
                return visit(node.getStatement(), p);
            }

            @Override
            public Void visitSynchronized(SynchronizedTree node, Void p) {
                return visit(node.getBlock(), p);
            }

            @Override
            public Void visitWhileLoop(WhileLoopTree node, Void p) {
                return visit(node.getStatement(), p);
            }
        }
    }

    private static class Diff {
        private int start;
        private int end;
        private String text;

        private Diff(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }

        public int getStartOffset() {
            return start;
        }
        
        public int getEndOffset() {
            return end;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "Diff<" + start + "," + end + ">:" + text; //NOI18N
        }
    }
}

