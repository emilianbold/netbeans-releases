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
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.modules.java.source.transform.FieldGroupTree;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.save.CasualDiff.LineInsertionType;

/**
 * Estimates the position for given element or element set. Offsets are
 * available from SourcePositions, but these are not precise enough for
 * generator. -- Generator has to handle comments and empty lines and
 * spaces too.
 *
 * @author Pavel Flaska
 */
public abstract class PositionEstimator {
    
    final List<? extends Tree> oldL;
    final List<? extends Tree> newL;
    final WorkingCopy copy;
    boolean initialized;
    final TokenSequence<JavaTokenId> seq;

    PositionEstimator(final List<? extends Tree> oldL, final List<? extends Tree> newL, final WorkingCopy copy) {
        this.oldL = oldL;
        this.newL = newL;
        this.copy = copy;
        this.seq = copy != null ? copy.getTokenHierarchy().tokenSequence(JavaTokenId.language()) : null;
        initialized = false;
    }
        
    int[][] matrix;
    
    /**
     * Initialize data for provided lists.
     */
    protected abstract void initialize();

    /**
     * Computes the offset position when inserting to {@code index}.
     *
     * @param   index  represents order in list
     * @return  offset where to insert
     */
    public abstract int getInsertPos(int index);
    
    /**
     * Computes the start and end positions for element at {@code index}.
     * 
     * @param   index
     * @return  two integers containing start and end position
     * @throws  IndexOutOfBoundsException {@inheritDoc}
     */
    public abstract int[] getPositions(int index);

    /**
     * In case old list does not contain any element, try to estimate the
     * position where to start. User has to provide empty buffers to allow
     * to put some formatting stuff to head and tail of section.
     *
     * @param   startPos
     * @param   aHead     buffer where head formatting stuff will be added
     * @param   aTail     buffer where tail formatting stuff will be added
     * @return  position where to start
     */
    abstract int prepare(final int startPos, StringBuilder aHead, StringBuilder aTail);

    /**
     * Returns of whole section. Used, when all item in the list are removed,
     * e.g. when all imports are removed.
     * 
     * @param  replacement can contain the text which will replace the whole
     *         section
     * 
     * @return start offset and end offset of the section
     */
    public abstract int[] sectionRemovalBounds(StringBuilder replacement);
            
    /**
     * Return line insertion type for given estimator.
     */
    public LineInsertionType lineInsertType() {
        return LineInsertionType.NONE;
    }
    
    public abstract String head();
    public abstract String sep();
    public abstract String getIndentString();
    
    // remove the method after all calls will be refactored!
    public int[][] getMatrix() { 
        if (!initialized) initialize();
        return matrix; 
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementors
    static class ImplementsEstimator extends BaseEstimator {
        ImplementsEstimator(List<? extends Tree> oldL, 
                            List<? extends Tree> newL,
                            WorkingCopy copy)
        {
            super(IMPLEMENTS, oldL, newL, copy);
        }
    }
    
    static class ExtendsEstimator extends BaseEstimator {
        ExtendsEstimator(List<? extends Tree> oldL, 
                         List<? extends Tree> newL,
                         WorkingCopy copy)
        {
            super(EXTENDS, oldL, newL, copy);
        }
    }
    
    static class ThrowsEstimator extends BaseEstimator {
        ThrowsEstimator(List<? extends ExpressionTree> oldL, 
                        List<? extends ExpressionTree> newL,
                        WorkingCopy copy)
        {
            super(THROWS, oldL, newL, copy);
        }
    }

    /**
     * Provides positions for imports section. Computes positions for exisiting
     * imports and suggest insert position for newly added/inserted import.
     */
    static class ImportsEstimator extends PositionEstimator {
        
        public ImportsEstimator(final List<? extends ImportTree> oldL, 
                                final List<? extends ImportTree> newL, 
                                final WorkingCopy copy) 
        {
            super(oldL, newL, copy);
        }

        List<int[]> data;
        
        @Override()
        public void initialize() {
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                
                seq.move(treeStart);
                seq.moveNext();
                int wideStart = goAfterLastNewLine(seq);
                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().indexOf('\n');
                            if (indexOf > 0) {
                                localResult = seq.offset() + indexOf + 1;
                            }
                            break;
                        case LINE_COMMENT:
                            previousEnd = seq.offset() + token.text().length();
                            break;
                    }
                    if (localResult > 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext()) break;
                }
                seq.move(treeEnd);
                int wideEnd = treeEnd;
                while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                    if (JavaTokenId.WHITESPACE == token.id()) {
                        int indexOf = token.text().toString().indexOf('\n');
                        if (indexOf > -1) {
                            wideEnd = seq.offset() + indexOf + 1;
                        } else {
                            wideEnd = seq.offset();
                        }
                    } else if (JavaTokenId.LINE_COMMENT == token.id()) {
                        wideEnd = seq.offset() + token.text().length();
                        break;
                    } else if (JavaTokenId.JAVADOC_COMMENT == token.id()) {
                        break;
                    }
                }
                if (wideEnd < treeEnd) wideEnd = treeEnd;
                data.add(new int[] { wideStart, wideEnd, previousEnd });
            }
            initialized = true;
        }

        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                return index == data.size() ? data.get(index-1)[2] : data.get(index)[0];
            }
        }

        // when first element is inserted, analyse the spacing and
        // do decision about adding new lines.
        @Override()
        public int prepare(final int startPos, StringBuilder aHead, StringBuilder aTail) {
            if (!initialized) initialize();
            CompilationUnitTree cut = copy.getCompilationUnit();
            int resultPos = 0;
            if (cut.getTypeDecls().isEmpty()) {
                aHead.append('\n');
                return copy.getText().length();
            } else {
                Tree t = cut.getTypeDecls().get(0);
                SourcePositions positions = copy.getTrees().getSourcePositions();
                int typeDeclStart = (int) positions.getStartPosition(cut, t);
                seq.move(typeDeclStart);
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    resultPos = seq.offset() + seq.token().length();
                } else {
                    aTail.append('\n');
                    return 0;
                }
            }
            int counter = 0;
            while (seq.moveNext() && nonRelevant.contains(seq.token().id()) && counter < 3) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    String white = seq.token().text().toString();
                    int index = 0, pos = 0;
                    while ((pos = white.indexOf('\n', pos)) > -1) {
                        ++counter;
                        ++pos;
                        if (counter < 3) {
                            index = pos;
                        }
                    }
                    resultPos += index;
                } else if (JavaTokenId.LINE_COMMENT == seq.token().id()) {
                    ++counter;
                    resultPos += seq.token().text().toString().length();
                } else if (JavaTokenId.BLOCK_COMMENT == seq.token().id() ||
                           JavaTokenId.JAVADOC_COMMENT == seq.token().id()) {
                    // do not continue when javadoc comment was found!
                    break;
                }
            }
            if (counter < 3) {
                if (counter == 0) {
                    aHead.append("\n\n");
                } else if (counter == 1) {
                    aHead.append('\n');
                }
                aTail.append('\n');
            }
            return resultPos;
        }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index);
        }
        
        @Override
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }
        
        @Override()
        public String head() {
            throw new UnsupportedOperationException("Not applicable for imports!");
        }

        @Override()
        public String sep() { 
            throw new UnsupportedOperationException("Not applicable for imports!");
        }

        @Override()
        public String getIndentString() {
            throw new UnsupportedOperationException("Not applicable for imports!");
        }

        @Override
        public String toString() {
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = copy.getText().substring(pos[0], pos[1]);
                result += "\"" + s + "\"\n";
            }
            return result;
        }
    
        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
            }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                    } else {
                        sectionEnd += seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }
    }
    
    static class CasesEstimator extends PositionEstimator {
        
        private List<int[]> data;
        
        public CasesEstimator(final List<? extends Tree> oldL, 
                                final List<? extends Tree> newL, 
                                final WorkingCopy copy)
        {
            super(oldL, newL, copy);
        }
        
        @Override()
        public void initialize() {
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);

                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().indexOf('\n');
                            if (indexOf > -1) {
                                localResult = seq.offset() + indexOf + 1;
                            }
                            break;
                        case LINE_COMMENT:
                            previousEnd = seq.offset() + token.text().length();
                            break;
                    }
                    if (localResult > 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext()) break;
                }
                seq.move(treeEnd);
                int wideEnd = treeEnd;
                while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                    if (JavaTokenId.WHITESPACE == token.id()) {
                        int indexOf = token.text().toString().indexOf('\n');
                        if (indexOf > -1) {
                            wideEnd = seq.offset() + indexOf + 1;
                        } else {
                            wideEnd = seq.offset();
                        }
                    } else if (JavaTokenId.LINE_COMMENT == token.id()) {
                        wideEnd = seq.offset() + token.text().length();
                        break;
                    } else if (JavaTokenId.JAVADOC_COMMENT == token.id()) {
                        break;
                    }
                    if (wideEnd > treeEnd)
                        break;
                }
                if (wideEnd < treeEnd) wideEnd = treeEnd;
                data.add(new int[] { previousEnd, wideEnd, previousEnd });
            }
            initialized = true;
        }
        
        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                return index == data.size() ? data.get(index-1)[2] : data.get(index)[0];
            }
        }

        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                    } else {
                        sectionEnd += seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index);
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            seq.move(startPos);
            seq.moveNext();
            moveToSrcRelevant(seq, Direction.BACKWARD);
            while (seq.moveNext() && nonRelevant.contains(seq.token().id())) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    int newlineInToken = seq.token().text().toString().indexOf('\n');
                    if (newlineInToken > -1) {
                        return seq.offset() + newlineInToken + 1;
                    }
                } else if (JavaTokenId.LINE_COMMENT == seq.token().id()) {
                    return seq.offset() + seq.token().text().length();
                }
            }
            return startPos;
        }
        
        @Override
        public String toString() {
            if (!initialized) initialize();
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = copy.getText().substring(pos[0], pos[1]);
                result += "[" + s + "]";
            }
            return result;
        }

    }
    
    /**
     * Provides position estimator for features in type declaration.
     */
    static class AnnotationsEstimator extends PositionEstimator {
        
        public AnnotationsEstimator(List<? extends Tree> oldL, 
                                 List<? extends Tree> newL,
                                 WorkingCopy copy)
        {
            super(oldL, newL, copy);
        }

        public void initialize() {
            int size = oldL.size();
            matrix = new int[size+1][5];
            matrix[size] = new int[] { -1, -1, -1, -1, -1 };
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int i = 0;
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                // stupid hack, we have to remove syntetic constructors --
                // should be filtered before and shouldn't be part of this
                // collection (oldL)
                if (treeEnd < 0) continue;
                
                seq.move(treeStart);
                int startIndex = seq.index();
                // go back to opening/closing curly, semicolon or other
                // token java-compiler important token.
                moveToSrcRelevant(seq, Direction.BACKWARD);
                seq.moveNext();
                int veryBeg = seq.index();
                seq.move(treeEnd);
                matrix[i++] = new int[] { veryBeg, veryBeg, veryBeg, startIndex, seq.index() };
                if (i == size) {
                    seq.move(treeEnd);
                    matrix[i][2] = seq.index();
                }
            }
        }
        
        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            int tokenIndex = matrix[index][2];
            // cannot do any decision about the position - probably first
            // element is inserted, no information is available. Call has
            // to decide.
            if (tokenIndex == -1) return -1;
            seq.moveIndex(tokenIndex);
            seq.moveNext();
            int off = goAfterLastNewLine(seq);
            return off;
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            int begin = getInsertPos(index);
            if (matrix[index][4] != -1) {
                seq.moveIndex(matrix[index][4]);
                seq.moveNext();
            }
            int end = goAfterFirstNewLine(seq);
            return new int [] { begin, end };
        }
        
        @Override
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            return startPos;
        }
        
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                    } else {
                        sectionEnd += seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }

    }
    
    private static abstract class BaseEstimator extends PositionEstimator {
        
        JavaTokenId precToken;
        private ArrayList<String> separatorList;

        private BaseEstimator(JavaTokenId precToken,
                List<? extends Tree> oldL,
                List<? extends Tree> newL,
                WorkingCopy copy)
        {
            super(oldL, newL, copy);
            this.precToken = precToken;
        }
        
        public String head() { return " " + precToken.fixedText() + " "; }
        public String sep()  { return ", "; }
        
        public void initialize() {
            separatorList = new ArrayList<String>(oldL.size());
            boolean first = true;
            int size = oldL.size();
            matrix = new int[size+1][5];
            matrix[size] = new int[] { -1, -1, -1, -1, -1 };
            int i = 0;
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            for (Tree item : oldL) {
                String separatedText = "";
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                seq.move(treeStart);
                int startIndex = seq.index();
                int beforer = -1;
                if (first) {
                    // go back to throws keywrd.
                    while (seq.movePrevious() && seq.token().id() != precToken) ;
                    int throwsIndex = seq.index();
                    beforer = throwsIndex+1;
                    // go back to closing )
                    moveToSrcRelevant(seq, Direction.BACKWARD);
                    seq.moveNext();
                    int beg = seq.index();
                    seq.move(treeEnd);
                    matrix[i++] = new int[] { beg, throwsIndex, beforer, startIndex, seq.index() };
                    first = false;
                } else {
                    int afterPrevious = matrix[i-1][4];
                    // move to comma
                    while (seq.movePrevious() && (seq.token().id() != COMMA))
                        if (seq.token().id() == WHITESPACE)
                            separatedText = seq.token().text() + separatedText;
                        else if (seq.token().id() == LINE_COMMENT)
                            separatedText = '\n' + separatedText;
                    separatorList.add(separatedText);
                    int separator = seq.index();
                    int afterSeparator = separator + 1; // bug
                    if (afterPrevious == separator) {
                        afterPrevious = -1;
                    }
                    seq.move(treeEnd);
                    matrix[i++] = new int[] { afterPrevious, separator, afterSeparator, startIndex, seq.index() };
                }
                if (i == size) {
                    // go forward to { or ;
                    moveToSrcRelevant(seq, Direction.FORWARD);
                    matrix[i][2] = seq.index();
                }
                seq.move(treeEnd);
            }
            initialized = true;
        }
        
        public String getIndentString() {
            if (!initialized) initialize();
            Map<String, Integer> map = new HashMap<String, Integer>();
            for (String item : separatorList) {
                String s = item;
                if (s.lastIndexOf("\n") > -1) {
                    s = s.substring(item.lastIndexOf("\n"));
                }
                Integer count = map.get(s);
                if (count != null) {
                    map.put(s, count++);
                } else {
                    map.put(s, 1);
                }
            }
            int max = -1;
            String s = null;
            for (String item : map.keySet()) {
                if (map.get(item) > max) {
                    s = item;
                    max = map.get(item);
                }
            }
            return s;
        }
        
        public int getInsertPos(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public int[] getPositions(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    // todo (#pf): remove - used for debugging reasons, doesn't do good job
    public void tablePrint(int[][] matrix, TokenSequence seq) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                int item = matrix[i][j];
                String s = "(nothing)";
                if (item > -1) {
                    seq.moveIndex(item);
                    seq.moveNext();
                    s = "'" + seq.token().text();
                }
                s += "'                                           ";
    //            System.err.print(s.substring(0, 25));
                System.err.print(item + "\t");
            }
            System.err.println("");
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Provides position estimator for features in type declaration.
     */
    static class MembersEstimator extends PositionEstimator {
        
        private List<int[]> data;
        
        public MembersEstimator(final List<? extends Tree> oldL, 
                                final List<? extends Tree> newL, 
                                final WorkingCopy copy)
        {
            super(oldL, newL, copy);
        }
        
        @Override()
        public void initialize() {
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                
                // teribolak
                if (item instanceof FieldGroupTree) { //
                    FieldGroupTree fgt = ((FieldGroupTree) item);
                    List<JCVariableDecl> vars = fgt.getVariables();
                    treeEnd = (int) positions.getEndPosition(compilationUnit, vars.get(vars.size()-1));
                    if (fgt.isEnum()) {
                        seq.move(treeEnd);
                        moveToSrcRelevant(seq, Direction.FORWARD);
                        seq.moveNext();
                        treeEnd = seq.offset();
                    }
                } else {
                    seq.move(treeEnd);
                    if (seq.movePrevious() && nonRelevant.contains(seq.token().id())) {
                        moveToSrcRelevant(seq, Direction.BACKWARD);
                        seq.moveNext();
                        treeEnd = seq.offset();
                    }
                }
                
                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().indexOf('\n');
                            if (indexOf > -1) {
                                localResult = seq.offset() + indexOf + 1;
                            }
                            break;
                        case LINE_COMMENT:
                            previousEnd = seq.offset() + token.text().length();
                            break;
                    }
                    if (localResult > 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext()) break;
                }
                seq.move(treeEnd);
                int wideEnd = treeEnd;
                while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                    if (JavaTokenId.WHITESPACE == token.id()) {
                        int indexOf = token.text().toString().indexOf('\n');
                        if (indexOf > -1) {
                            wideEnd = seq.offset() + indexOf + 1;
                        } else {
                            wideEnd = seq.offset();
                        }
                    } else if (JavaTokenId.LINE_COMMENT == token.id()) {
                        wideEnd = seq.offset() + token.text().length();
                        break;
                    } else if (JavaTokenId.JAVADOC_COMMENT == token.id()) {
                        break;
                    }
                    if (wideEnd > treeEnd)
                        break;
                }
                if (wideEnd < treeEnd) wideEnd = treeEnd;
                data.add(new int[] { previousEnd, wideEnd, previousEnd });
            }
            initialized = true;
        }
        
        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                return index == data.size() ? data.get(index-1)[2] : data.get(index)[0];
            }
        }

        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                    } else {
                        sectionEnd += seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index);
        }
        
        @Override
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            seq.move(startPos);
            seq.moveNext();
            moveToSrcRelevant(seq, Direction.BACKWARD);
            while (seq.moveNext() && nonRelevant.contains(seq.token().id())) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    int newlineInToken = seq.token().text().toString().indexOf('\n');
                    if (newlineInToken > -1) {
                        return seq.offset() + newlineInToken + 1;
                    }
                } else if (JavaTokenId.LINE_COMMENT == seq.token().id()) {
                    return seq.offset() + seq.token().text().length();
                }
            }
            return startPos;
        }
        
        @Override
        public String toString() {
            if (!initialized) initialize();
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = copy.getText().substring(pos[0], pos[1]);
                result += "[" + s + "]";
            }
            return result;
        }

    }
        
    /**
     * Provides position estimator for features in type declaration.
     */
    static class CatchesEstimator extends PositionEstimator {
        
        private List<int[]> data;
        
        public CatchesEstimator(final List<? extends Tree> oldL, 
                                final List<? extends Tree> newL, 
                                final WorkingCopy copy)
        {
            super(oldL, newL, copy);
        }
        
        @Override()
        public void initialize() {
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);

                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().indexOf('\n');
                            if (indexOf > -1) {
                                localResult = seq.offset() + indexOf + 1;
                            }
                            break;
                        case LINE_COMMENT:
                            previousEnd = seq.offset() + token.text().length();
                            break;
                    }
                    if (localResult > 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext()) break;
                }
                data.add(new int[] { previousEnd, treeEnd, previousEnd });
            }
            initialized = true;
        }
        
        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                return index == data.size() ? data.get(index-1)[2] : data.get(index)[0];
            }
        }

        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                    } else {
                        sectionEnd += seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index);
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            seq.move(startPos);
            seq.moveNext();
            moveToSrcRelevant(seq, Direction.BACKWARD);
            while (seq.moveNext() && nonRelevant.contains(seq.token().id())) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    int newlineInToken = seq.token().text().toString().indexOf('\n');
                    if (newlineInToken > -1) {
                        return seq.offset() + newlineInToken + 1;
                    }
                } else if (JavaTokenId.LINE_COMMENT == seq.token().id()) {
                    return seq.offset() + seq.token().text().length();
                }
            }
            return startPos;
        }
        
        @Override
        public String toString() {
            if (!initialized) initialize();
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = copy.getText().substring(pos[0], pos[1]);
                result += "[" + s + "]";
            }
            return result;
        }

    }

    /**
     * Provides position estimator for top-level classes
     */
    static class TopLevelEstimator extends PositionEstimator {
        
        private List<int[]> data;
        
        public TopLevelEstimator(final List<? extends Tree> oldL, 
                                 final List<? extends Tree> newL, 
                                 final WorkingCopy copy)
        {
            super(oldL, newL, copy);
        }
        
        @Override()
        public void initialize() {
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                
                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().indexOf('\n');
                            if (indexOf > -1) {
                                localResult = seq.offset() + indexOf + 1;
                            }
                            break;
                        case LINE_COMMENT:
                            previousEnd = seq.offset() + token.text().length();
                            break;
                    case JAVADOC_COMMENT:
                            previousEnd = seq.offset();
                            break;
                    }
                    if (localResult > 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext()) break;
                }
                int wideStart = previousEnd;
                seq.move(treeStart);
                seq.moveNext();
                seq.movePrevious();
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().lastIndexOf('\n');
                            if (indexOf > -1) {
                                localResult = seq.offset() + indexOf + 1;
                            }
                            break;
                        case LINE_COMMENT:
                            localResult = seq.offset() + token.text().length();
                            break;
                        case JAVADOC_COMMENT:
                        case BLOCK_COMMENT:
                            wideStart = seq.offset();
                            break;
                    }
                    if (wideStart > previousEnd) {
                        break;
                    }
                    if (localResult > 0) {
                        wideStart = localResult;
                    }
                    if (!seq.movePrevious()) break;
                }
                
                seq.move(treeEnd);
                int wideEnd = treeEnd;
                while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                    if (JavaTokenId.WHITESPACE == token.id()) {
                        int indexOf = token.text().toString().indexOf('\n');
                        if (indexOf > -1) {
                            wideEnd = seq.offset() + indexOf + 1;
                        } else {
                            wideEnd = seq.offset();
                        }
                    } else if (JavaTokenId.LINE_COMMENT == token.id()) {
                        wideEnd = seq.offset() + token.text().length();
                        break;
                    } else if (JavaTokenId.JAVADOC_COMMENT == token.id()) {
                        break;
                    }
                    if (wideEnd > treeEnd)
                        break;
                }
                if (wideEnd < treeEnd) wideEnd = treeEnd;
                data.add(new int[] { wideStart, wideEnd, previousEnd });
            }
            initialized = true;
        }
        
        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                return index == data.size() ? data.get(index-1)[2] : data.get(index)[0];
            }
        }

        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                    } else {
                        sectionEnd += seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index);
        }
        
        @Override
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }

        @Override()
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            seq.moveEnd();
            if (seq.movePrevious()) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    int firstNewLineIndex = -1;
                    String tokenText = seq.token().text().toString();
                    if ((firstNewLineIndex = tokenText.indexOf('\n')) > -1) {
                        if (tokenText.lastIndexOf('\n') == firstNewLineIndex) {
                            aHead.append('\n');
                        }
                    } else {
                        aHead.append("\n\n");
                    }
                } else if (JavaTokenId.LINE_COMMENT != seq.token().id()) {
                    aHead.append("\n\n");
                }
                return seq.offset() + seq.token().text().length();
            }
            return startPos;
        }
        
        @Override()
        public String toString() {
            if (!initialized) initialize();
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = copy.getText().substring(pos[0], pos[1]);
                result += "[" + s + "]";
            }
            return result;
        }

    }
    ////////////////////////////////////////////////////////////////////////////
    // Utility methods
    
    /**
     * Moves in specified direction to java source relevant token.
     *
     * In other words, it moves until the token is something important
     * for javac compiler. (every token except WHITESPACE, BLOCK_COMMENT,
     * LINE_COMMENT and JAVADOC_COMMENT)
     *
     * @param  seq  token sequence which is used for move.
     * @param  dir  direction - either forward or backward.
     * @return      relevant token identifier.
     *
     */
    public static JavaTokenId moveToSrcRelevant(TokenSequence<JavaTokenId> seq,
                                                 Direction dir)
    {
        return moveToDifferentThan(seq, dir, nonRelevant);
    }
    
    private static JavaTokenId moveToDifferentThan(
        TokenSequence<JavaTokenId> seq,
        Direction dir,
        EnumSet<JavaTokenId> set)
    {
        boolean notBound = false;
        switch (dir) {
            case BACKWARD:
                while ((notBound = seq.movePrevious()) && set.contains(seq.token().id())) ;
                break;
            case FORWARD:
                while ((notBound = seq.moveNext()) && set.contains(seq.token().id())) ;
                break;
        }
        return notBound ? seq.token().id() : null;
    }
    
    private static int goAfterFirstNewLine(final TokenSequence<JavaTokenId> seq) {
        // ensure that we are not after the last token, if so,
        // go to last
        if (seq.token() == null) 
            seq.movePrevious();
        
        int base = seq.offset();
        seq.movePrevious();
        while (seq.moveNext() && nonRelevant.contains(seq.token().id())) {
            switch (seq.token().id()) {
                case LINE_COMMENT:
                    seq.moveNext();
                    return seq.offset();
                case WHITESPACE:
                    char[] c = seq.token().text().toString().toCharArray();
                    int index = 0;
                    while (index < c.length) {
                        if (c[index++] == '\n') {
//                            while (index < c.length)
//                                if (c[index] != ' ' && c[index] != '\t')
//                                    break;
//                                else
//                                    ++index;
                            return base + index;
                        }
                    }
            }
        }
        return base;
    }
    
    private static int goAfterLastNewLine(final TokenSequence<JavaTokenId> seq) {
        int base = seq.offset();
        seq.movePrevious();
        while (seq.moveNext() && nonRelevant.contains(seq.token().id())) ;
        
        while (seq.movePrevious() && nonRelevant.contains(seq.token().id())) {
            switch (seq.token().id()) {
                case LINE_COMMENT:
                    seq.moveNext();
                    return seq.offset();
                case WHITESPACE:
                    char[] c = seq.token().text().toString().toCharArray();
                    for (int i = c.length; i > 0; ) {
                        if (c[--i] == '\n') {
                            return seq.offset() + i + 1;
                        }
                    }
            }
        }
        if ((seq.index() == 0 || seq.moveNext()) && nonRelevant.contains(seq.token().id())) {
            return seq.offset();
        }
        return base;
    }
    
    public static final boolean isSeparator(JavaTokenId id) {
        return "separator".equals(id.primaryCategory()); //NOI18N
    }
    
    /**
     * Represents non-relevant tokens in java source. (Tokens which are not
     * important for javac, i.e. line and block comments, empty lines and
     * whitespaces.)
     */
    public static final EnumSet<JavaTokenId> nonRelevant = EnumSet.<JavaTokenId>of(
            LINE_COMMENT, 
            BLOCK_COMMENT,
            JAVADOC_COMMENT,
            WHITESPACE
    );

    /**
     * Represents the direction to move. Either forward or backward.
     */
    public enum Direction {
        FORWARD, BACKWARD;
    }
}
