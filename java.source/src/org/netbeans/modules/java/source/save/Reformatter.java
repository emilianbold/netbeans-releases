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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.PositionConverter;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.ReformatTask;

/**
 *
 * @author Dusan Balek
 */
public class Reformatter implements ReformatTask {
    
    private JavaSource javaSource;
    private Context context;
    private CompilationController controller;
    private Document doc;
    private int startOffset;
    private int endOffset;
    private int shift;
    private int tabSize;    

    public Reformatter(JavaSource javaSource, Context context) {
        this.javaSource = javaSource;
        this.context = context;
        this.doc = context.document();
        this.tabSize = CodeStyle.getDefault(null).getTabSize();
    }

    public void reformat() throws BadLocationException {
        if (controller == null) {
            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(Phase.PARSED);
                        Reformatter.this.controller = controller;
                    }
                }, true);            
            } catch (IOException ioe) {
                JavaSourceAccessor.INSTANCE.unlockJavaCompiler();
            }
            if (controller == null)
                return;
        }
        this.startOffset = context.startOffset() - shift;
        this.endOffset = context.endOffset() - shift;
        PositionConverter converter = controller.getPositionConverter();
        if (converter != null) {
            this.startOffset = converter.getJavaSourcePosition(this.startOffset);
            this.endOffset = converter.getJavaSourcePosition(this.endOffset);
        }
        if (this.startOffset >= this.endOffset)
            return;
        TreePath path = getCommonPath();
        if (path == null)
            return;
        Tree tree = path.getLeaf();
        int indent = getIndent(path);
        if (indent < 0)
            return;
        String sourceText = controller.getText();
        int start = 0;
        if (tree.getKind() != Tree.Kind.COMPILATION_UNIT) {
            SourcePositions sp = controller.getTrees().getSourcePositions();
            start = (int)sp.getStartPosition(controller.getCompilationUnit(), tree);
            int end = (int)sp.getEndPosition(controller.getCompilationUnit(), tree);
            if (start < 0 || end < 0)
                return;
            sourceText = sourceText.substring(start, end);
        }
        VeryPretty pretty = new VeryPretty(controller);
        String text = pretty.reformat((JCTree)tree, startOffset, endOffset, indent);
        if (text == null)
            return;
        int endPos = context.endOffset();
        for (Diff diff : getDiffs(sourceText, text.trim(), start)) {
            int offset = diff.start.getOffset();
            doc.remove(offset, diff.end.getOffset() - offset);
            if (diff.text != null)
                doc.insertString(offset, diff.text, null);
        }
        shift += context.endOffset() - endPos;
    }
    
    public ExtraLock reformatLock() {
        return new Lock();
    }
    
    private TreePath getCommonPath() {
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
        while(reverseStartPath.head != null && reverseStartPath.head == reverseEndPath.head) {
            path = reverseStartPath.head instanceof CompilationUnitTree ? new TreePath((CompilationUnitTree)reverseStartPath.head) : new TreePath(path, reverseStartPath.head);
            reverseStartPath = reverseStartPath.tail;
            reverseEndPath = reverseEndPath.tail;
        }
        return path;
    }
    
    private int getIndent(TreePath path) {
        TokenSequence<JavaTokenId> sourceTS = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        if (sourceTS == null)
            return -1;
        int indent = 0;
        SourcePositions sp = controller.getTrees().getSourcePositions();
        Tree lastTree = null;
        while (path != null) {
            int offset = (int)sp.getStartPosition(path.getCompilationUnit(), path.getLeaf());
            if (offset < 0)
                return -1;
            sourceTS.move(offset);
            if (sourceTS.movePrevious()) {
                Token<JavaTokenId> token = sourceTS.token();
                if (token.id() == JavaTokenId.WHITESPACE) {
                    String text = token.text().toString();
                    int idx = text.lastIndexOf('\n');
                    if (idx >= 0) {
                        text = text.substring(idx + 1);
                        indent = getCol(text);
                        break;
                    } else if (sourceTS.movePrevious()) {
                        if (sourceTS.token().id() == JavaTokenId.LINE_COMMENT) {
                            indent = getCol(text);
                            break;
                        }                        
                    }
                }
            }
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
                col = col+tabSize & ~(tabSize-1);
            } else {
                col++;
            }
        }
        return col;
    }
    
    private LinkedList<Diff> getDiffs(TokenSequence<JavaTokenId> sourceTS, TokenSequence<JavaTokenId> textTS, int offset) throws BadLocationException {
        LinkedList<Diff> diffs = new LinkedList<Diff>();
        ArrayList<Token<JavaTokenId>> sourceTokens = new ArrayList<Token<JavaTokenId>>();
        while(sourceTS.moveNext())
            sourceTokens.add(sourceTS.token());
        ArrayList<Token<JavaTokenId>> textTokens = new ArrayList<Token<JavaTokenId>>();
        while(textTS.moveNext())
            textTokens.add(textTS.token());
        List<Difference> tokenDiffs = new ComputeDiff<Token<JavaTokenId>>(sourceTokens, textTokens, new TokenComparator()).diff();
        for(Difference tDiff : tokenDiffs) {
            int delStart = tDiff.getDeletedStart();
            int delEnd   = tDiff.getDeletedEnd();
            int addStart = tDiff.getAddedStart();
            int addEnd   = tDiff.getAddedEnd();
            sourceTS.moveIndex(delStart);
            char kind = delEnd < 0 ? 'i' : addEnd < 0 ? 'r' : 'c';
            switch (kind) {
            case 'i':
                int sourceOffset = offset;
                if (sourceTS.moveNext()) {
                    sourceOffset += sourceTS.offset();
                } else if (sourceTS.movePrevious()) {
                    sourceOffset += sourceTS.offset() + sourceTS.token().length();
                }
                if (sourceOffset >= startOffset && sourceOffset <= endOffset) {
                    Position pos = doc.createPosition(sourceOffset + shift);
                    StringBuilder sb = new StringBuilder();
                    textTS.moveIndex(addStart);
                    while (textTS.moveNext() && textTS.index() <= addEnd)
                        sb.append(textTS.token().text());
                    if (sb.length() > 0)
                        diffs.add(new Diff(pos, pos, sb.toString()));
                }
                break;
            case 'r':
                int start = -1;
                int end = -1;
                while (sourceTS.moveNext() && sourceTS.index() <= delEnd) {
                    sourceOffset = sourceTS.offset() + offset;
                    int sourceEndOffset = sourceOffset + sourceTS.token().length();
                    if (sourceEndOffset > startOffset && sourceOffset < endOffset) {
                        if (start < 0) {
                            int delta = startOffset - sourceOffset;
                            start = delta > 0 ? startOffset : sourceOffset;
                        }
                        end = sourceEndOffset <= endOffset ? sourceEndOffset : endOffset;
                    }
                }
                if (start >= 0) {
                    Position startPos = doc.createPosition(start + shift);
                    Position endPos = doc.createPosition(end + shift);
                    diffs.add(new Diff(startPos, endPos, null));
                }
                break;
            case 'c':                
                start = -1;
                end = -1;
                StringBuilder preDelta = new StringBuilder();
                StringBuilder postDelta = new StringBuilder();
                while (sourceTS.moveNext() && sourceTS.index() <= delEnd) {
                    sourceOffset = sourceTS.offset() + offset;
                    int sourceEndOffset = sourceOffset + sourceTS.token().length();
                    if (sourceEndOffset < startOffset) {
                        preDelta.append(sourceTS.token().text());
                    } else if (sourceOffset >= endOffset) {
                        postDelta.append(sourceTS.token().text());                        
                    } else {
                        if (start < 0) {
                            int d = startOffset - sourceOffset;
                            start = d > 0 ? startOffset : sourceOffset;
                            if (d > 0)
                                preDelta.append(sourceTS.token().text().subSequence(0, d));
                        }
                        int d = sourceEndOffset - endOffset;
                        end = d > 0 ? endOffset : sourceEndOffset;
                        if (d > 0) {
                            int len = sourceTS.token().text().length();
                            postDelta.append(sourceTS.token().text().subSequence(len - d, len));
                        }
                    }
                }
                if (start >= 0) {
                    Position startPos = doc.createPosition(start + shift);
                    Position endPos = doc.createPosition(end + shift);
                    StringBuilder sb = new StringBuilder();
                    textTS.moveIndex(addStart);
                    while (textTS.moveNext() && textTS.index() <= addEnd)
                        sb.append(textTS.token().text());
                    String s = sb.toString();
                    if (preDelta.length() > 0 || postDelta.length() > 0) {
                        List<String> sbLines = getLines(s);
                        List<String> preDeltaLines = preDelta.length() > 0 ? getLines(preDelta.toString()) : Collections.<String>emptyList();
                        List<String> postDeltaLines = postDelta.length() > 0 ? getLines(postDelta.toString()) : Collections.<String>emptyList();
                        int idx = Math.min(preDeltaLines.size() - 1, sbLines.size() - 1);
                        sb = new StringBuilder();
                        if (idx >= 0) {
                            String lastPreDelta = preDeltaLines.get(preDeltaLines.size() - 1);
                            String sbLine = sbLines.get(idx);
                            int i = 0;
                            while (i < lastPreDelta.length() && i < sbLine.length() && lastPreDelta.charAt(i) == sbLine.charAt(i))
                                i++;
                            if (i < sbLine.length())
                                sb.append(sbLine.substring(i));
                        }
                        for (int j = idx + 1; j < sbLines.size() - postDeltaLines.size(); j++)
                            sb.append(sbLines.get(j));
                    }
                    diffs.add(new Diff(startPos, endPos, sb.length() > 0 ? sb.toString() : null));
                }
                break;
            }
        }
        return diffs;
    }
        
    private LinkedList<Diff> getDiffs(String source, String text, int offset) throws BadLocationException {
        LinkedList<Diff> diffs = new LinkedList<Diff>();
        List<String> sourceLines = getLines(source);
        List<String> textLines = getLines(text);
        List<Difference> lineDiffs = new ComputeDiff<String>(sourceLines, textLines, new SimpleLineComparator()).diff();
        int i = 0;
        int j = 0;
        int lineOffset = offset;
        int lineEndOffset = offset;
        for (Difference lineDiff : lineDiffs) {
            int lineDelStart = lineDiff.getDeletedStart();
            int lineDelEnd = lineDiff.getDeletedEnd();
            int lineAddStart = lineDiff.getAddedStart();
            int lineAddEnd = lineDiff.getAddedEnd();
            char kind = lineDelEnd < 0 ? 'i' : lineAddEnd < 0 ? 'r' : 'c';
            while (i < lineDelStart && j < lineAddStart) {
                String sourceLine = sourceLines.get(i++);
                lineOffset = lineEndOffset;
                lineEndOffset += sourceLine.length();
                if (lineEndOffset > startOffset && lineOffset < endOffset) {
                    String textLine = textLines.get(j++);
                    if (!sourceLine.equals(textLine)) {
                        TokenSequence<JavaTokenId> sourceTS = TokenHierarchy.create(sourceLine, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
                        TokenSequence<JavaTokenId> textTS = TokenHierarchy.create(textLine, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
                        diffs.addAll(getDiffs(sourceTS, textTS, lineOffset));
                    }
                } else {
                    j++;
                }                
            }
            switch (kind) {
            case 'i':
                lineOffset = lineEndOffset;
                if (lineOffset >= startOffset && lineOffset <= endOffset) {
                    Position pos = doc.createPosition(lineOffset + shift);
                    StringBuilder sb = new StringBuilder();
                    while(j <= lineAddEnd)
                        sb.append(textLines.get(j++));
                    diffs.add(new Diff(pos, pos, sb.toString()));
                } else {
                    j = lineAddEnd + 1;
                }
                break;
            case 'r':
                int start = -1;
                int end = -1;
                while (i <= lineDelEnd) {
                    lineOffset = lineEndOffset;
                    lineEndOffset += sourceLines.get(i++).length();
                    if (lineEndOffset > startOffset && lineOffset < endOffset) {
                        if (start < 0)
                            start = startOffset - lineOffset > 0 ? startOffset : lineOffset;
                        end = lineEndOffset <= endOffset ? lineEndOffset : endOffset;
                    }
                }
                if (start >= 0) {
                    Position startPos = doc.createPosition(start + shift);
                    Position endPos = doc.createPosition(end + shift);
                    diffs.add(new Diff(startPos, endPos, null));
                }
                break;
            case 'c':
                StringBuilder sb = new StringBuilder();
                start = lineEndOffset;
                while (i <= lineDelEnd) {
                    String sourceLine = sourceLines.get(i++);
                    lineOffset = lineEndOffset;
                    lineEndOffset += sourceLine.length();
                    sb.append(sourceLine);
                }
                TokenSequence<JavaTokenId> sourceTS = TokenHierarchy.create(sb, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
                sb = new StringBuilder();
                while (j <= lineAddEnd)
                    sb.append(textLines.get(j++));
                TokenSequence<JavaTokenId> textTS = TokenHierarchy.create(sb, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
                diffs.addAll(getDiffs(sourceTS, textTS, start));
            }
        }
        while (i < sourceLines.size() && j < textLines.size()) {
            String sourceLine = sourceLines.get(i++);
            lineOffset = lineEndOffset;
            lineEndOffset += sourceLine.length();
            if (lineEndOffset > startOffset && lineOffset < endOffset) {
                String textLine = textLines.get(j++);
                if (!sourceLine.equals(textLine)) {
                    TokenSequence<JavaTokenId> sourceTS = TokenHierarchy.create(sourceLine, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
                    TokenSequence<JavaTokenId> textTS = TokenHierarchy.create(textLine, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
                    diffs.addAll(getDiffs(sourceTS, textTS, lineOffset));
                }
            } else {
                j++;
            }                
        }
        return diffs;
    }
    
    private List<String> getLines(String text) {
        List<String> lines = new ArrayList<String>();
        int start = 0;
        int end = 0;
        while ((end = text.indexOf('\n', start)) >= 0) {
            lines.add(text.substring(start, end + 1));
            start = end + 1;
        }
        lines.add(text.substring(start));
        return lines;
    }
    
    private class Lock implements ExtraLock {

        public void lock() {
            JavaSourceAccessor.INSTANCE.lockJavaCompiler();
        }

        public void unlock() {
            controller = null;
            JavaSourceAccessor.INSTANCE.unlockJavaCompiler();
        }        
    }
    
    private static class Diff {
        private Position start;
        private Position end;
        private String text;

        private Diff(Position start, Position end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }
    
    private static class SimpleLineComparator implements Comparator<String> {
        
        public int compare(String line1, String line2) {
            int i = 0;
            int j = 0;
            while(true) {
                char c1 = 0;
                boolean hasNext1;
                while((hasNext1 = (i < line1.length())) && Character.isWhitespace(c1 = line1.charAt(i++)));
                char c2 = 0;
                boolean hasNext2;
                while((hasNext2 = (j < line2.length())) && Character.isWhitespace(c2 = line2.charAt(j++)));
                if (!hasNext1)
                    return hasNext2 ? -1 : 0;
                if (!hasNext2)
                    return 1;
                if (c1 != c2)
                    return c1 - c2;
            }
        }
    }

    private static class TokenComparator implements Comparator<Token<JavaTokenId>> {
        
        private static EnumSet<JavaTokenId> ids = EnumSet.of(JavaTokenId.BLOCK_COMMENT, JavaTokenId.IDENTIFIER, JavaTokenId.JAVADOC_COMMENT,
                JavaTokenId.LINE_COMMENT, JavaTokenId.WHITESPACE);

        public int compare(Token<JavaTokenId> token1, Token<JavaTokenId> token2) {
            if (token1.id() != token2.id())
                return token1.id().ordinal() - token2.id().ordinal();
            if (!ids.contains(token1.id()))
                return 0;
            return token1.text().toString().compareTo(token2.text().toString());
        }        
    }

    public static class Factory implements ReformatTask.Factory {

        public ReformatTask createTask(Context context) {
            JavaSource js = JavaSource.forDocument(context.document());
            return js != null ? new Reformatter(js, context) : null;
        }        
    }
}

