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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.pretty.VeryPretty;

/**
 *
 * @author Dusan Balek
 */
public class Reformatter {
    
    private CompilationController controller;
    private Document doc;
    private int startOffset;
    private int endOffset;

    public Reformatter(CompilationController controller, Document doc, int startOffset, int endOffset) {
        this.controller = controller;
        this.doc = doc;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }
    
    public boolean reformat() throws BadLocationException {
        try {
            controller.toPhase(Phase.PARSED);            
        } catch (IOException ioe) {
            return false;
        }
        TreePath path = getCommonPath();
        if (path == null)
            return false;
        Tree tree = path.getLeaf();
        int indent = getIndent(path);
        if (indent < 0)
            return false;
        SourcePositions sp = controller.getTrees().getSourcePositions();
        int start = (int)sp.getStartPosition(controller.getCompilationUnit(), tree);
        int end = (int)sp.getEndPosition(controller.getCompilationUnit(), tree);
        if (start < 0 || end < 0)
            return false;
        String sourceText = controller.getText().substring(start, end);
        VeryPretty pretty = new VeryPretty(controller);
        String text = pretty.reformat((JCTree)tree, startOffset, endOffset, indent);
        if (text == null)
            return false;
        for (Diff diff : getDiffs(sourceText, text, start)) {
            int offset = diff.start.getOffset();
            doc.remove(offset, diff.end.getOffset() - offset);
            if (diff.text != null)
                doc.insertString(offset, diff.text, null);
        }
        return true;
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
                        indent = text.length() - idx - 1;
                        break;
                    } else if (sourceTS.movePrevious()) {
                        if (sourceTS.token().id() == JavaTokenId.LINE_COMMENT) {
                            indent = text.length();
                            break;
                        }                        
                    }
                }
            }
            path = path.getParentPath();
        }
        return indent;
    }
    
    private LinkedList<Diff> getDiffs(TokenSequence<JavaTokenId> sourceTS, TokenSequence<JavaTokenId> textTS, int offset) throws BadLocationException {
        LinkedList<Diff> diffs = new LinkedList<Diff>();
        ArrayList<Token<JavaTokenId>> sourceTokens = new ArrayList<Token<JavaTokenId>>();
        while(sourceTS.moveNext())
            sourceTokens.add(sourceTS.token());
        ArrayList<Token<JavaTokenId>> textTokens = new ArrayList<Token<JavaTokenId>>();
        while(textTS.moveNext())
            textTokens.add(textTS.token());
        List tokenDiffs = new ComputeDiff(sourceTokens, textTokens, new TokenComparator()).diff();
        for(Object o : tokenDiffs) {
            Difference tDiff = (Difference)o;
            int delStart = tDiff.getDeletedStart();
            int delEnd   = tDiff.getDeletedEnd();
            int addStart = tDiff.getAddedStart();
            int addEnd   = tDiff.getAddedEnd();
            sourceTS.moveIndex(delStart);
            char kind = delEnd < 0 ? 'i' : addEnd < 0 ? 'r' : 'c';
            switch (kind) {
            case 'i':
                if (sourceTS.moveNext() || sourceTS.isEmpty()) {
                    int sourceOffset = sourceTS.isEmpty() ? offset : sourceTS.offset() + offset;
                    if (sourceOffset >= startOffset && sourceOffset <= endOffset) {
                        Position pos = doc.createPosition(sourceOffset);
                        StringBuilder sb = new StringBuilder();
                        textTS.moveIndex(addStart);
                        while (textTS.moveNext() && textTS.index() <= addEnd)
                            sb.append(textTS.token().text());
                        if (sb.length() > 0)
                            diffs.add(new Diff(pos, pos, sb.toString()));
                    }
                }
                break;
            case 'r':
                int start = -1;
                int end = -1;
                while (sourceTS.moveNext() && sourceTS.index() <= delEnd) {
                    int sourceOffset = sourceTS.offset() + offset;
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
                    Position startPos = doc.createPosition(start);
                    Position endPos = doc.createPosition(end);
                    diffs.add(new Diff(startPos, endPos, null));
                }
                break;
            case 'c':                
                start = -1;
                end = -1;
                String delta = null;
                while (sourceTS.moveNext() && sourceTS.index() <= delEnd) {
                    int sourceOffset = sourceTS.offset() + offset;
                    int sourceEndOffset = sourceOffset + sourceTS.token().length();
                    if (sourceEndOffset >= startOffset && sourceOffset < endOffset) {
                        if (start < 0) {
                            int d = startOffset - sourceOffset;
                            start = d > 0 ? startOffset : sourceOffset;
                            if (d > 0)
                                delta = sourceTS.token().text().subSequence(0, d).toString();
                        }
                        end = sourceEndOffset <= endOffset ? sourceEndOffset : endOffset;
                    }
                }
                if (start >= 0) {
                    Position startPos = doc.createPosition(start);
                    Position endPos = doc.createPosition(end);
                    StringBuilder sb = new StringBuilder();
                    textTS.moveIndex(addStart);
                    while (textTS.moveNext() && textTS.index() <= addEnd)
                        sb.append(textTS.token().text());
                    String s = sb.toString();
                    if (delta != null) {
                        List<String> deltaLines = new ArrayList<String>();
                        getLines(delta, deltaLines, null);
                        List<String> sbLines = new ArrayList<String>();
                        getLines(s, sbLines, null);
                        int idx = Math.min(deltaLines.size(), sbLines.size() - 1);
                        String lastDelta = deltaLines.get(deltaLines.size() - 1);
                        String sbLine = sbLines.get(idx);
                        int i = 0;
                        while (i < lastDelta.length() && i < sbLine.length() && lastDelta.charAt(i) == sbLine.charAt(i))
                            i++;
                        sb = new StringBuilder();
                        if (i < sbLine.length() - 1)
                            sb.append(sbLine.substring(i));
                        for (int j = idx + 1; j < sbLines.size(); j++)
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
        List<String> sourceLines = new ArrayList<String>();
        List<Integer> lineOffsets = new ArrayList<Integer>();
        getLines(source, sourceLines, lineOffsets);
        List<String> textLines = new ArrayList<String>();
        getLines(text, textLines, null);
        List lineDiffs = new ComputeDiff(sourceLines, textLines).diff();
        for (Object l : lineDiffs) {
            Difference lineDiff = (Difference) l;
            int lineDelStart = lineDiff.getDeletedStart();
            int lineDelEnd = lineDiff.getDeletedEnd();
            int lineAddStart = lineDiff.getAddedStart();
            int lineAddEnd = lineDiff.getAddedEnd();
            char kind = lineDelEnd < 0 ? 'i' : lineAddEnd < 0 ? 'r' : 'c';
            switch (kind) {
                case 'i':
                    int lineOffset = lineOffsets.get(lineDelStart) + offset;
                    if (lineOffset >= startOffset && lineOffset <= endOffset) {
                        Position pos = doc.createPosition(lineOffset);
                        StringBuilder sb = new StringBuilder();
                        for (int i = lineAddStart; i <= lineAddEnd; i++)
                            sb.append(textLines.get(i));
                        if (sb.length() > 0)
                            diffs.add(new Diff(pos, pos, sb.toString()));
                    }
                    break;
                case 'r':
                    int start = -1;
                    int end = -1;
                    int delta = 0;
                    for (int i = lineDelStart; i <= lineDelEnd; i++) {
                        lineOffset = lineOffsets.get(i) + offset;
                        int lineEndOffset = lineOffset + sourceLines.get(i).length();
                        if (lineEndOffset > startOffset && lineOffset < endOffset) {
                            if (start < 0) {
                                delta = startOffset - lineOffset;
                                start = delta > 0 ? startOffset : lineOffset;
                            }
                            end = lineEndOffset <= endOffset ? lineEndOffset : endOffset;
                        }
                    }
                    if (start >= 0) {
                        if (delta > 0 && sourceLines.size() > lineDelEnd + 1) {
                            String firstLine = sourceLines.get(lineDelStart);
                            String nextLine = sourceLines.get(lineDelEnd + 1);
                            lineOffset = lineOffsets.get(lineDelEnd + 1);
                            int idx = 0;
                            while (idx < delta && lineOffset + idx < endOffset && idx < nextLine.length() && firstLine.charAt(idx) == nextLine.charAt(idx))
                                idx++;
                            end += idx;
                        }
                        Position startPos = doc.createPosition(start);
                        Position endPos = doc.createPosition(end);
                        diffs.add(new Diff(startPos, endPos, null));
                    }
                    break;
                case 'c':
                    StringBuilder sb = new StringBuilder();
                    for (int i = lineDelStart; i <= lineDelEnd; i++)
                        sb.append(sourceLines.get(i));
                    TokenSequence<JavaTokenId> sourceTS = TokenHierarchy.create(sb, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
                    sb = new StringBuilder();
                    for (int i = lineAddStart; i <= lineAddEnd; i++)
                        sb.append(textLines.get(i));
                    TokenSequence<JavaTokenId> textTS = TokenHierarchy.create(sb, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
                    diffs.addAll(getDiffs(sourceTS, textTS, lineOffsets.get(lineDelStart) + offset));
            }
        }
        return diffs;
    }
    
    private void getLines(String text, List<String> lines, List<Integer> lineOffsets) {
        int start = 0;
        int end = 0;
        while ((end = text.indexOf('\n', start)) >= 0) {
            lines.add(text.substring(start, end + 1));
            if(lineOffsets != null)
                lineOffsets.add(start);
            start = end + 1;
        }
        lines.add(text.substring(start));
        if (lineOffsets != null)
            lineOffsets.add(start);
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
}
