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
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.tools.javac.tree.JCTree;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.save.TreeDiff.LineInsertionType;

/**
 * Estimates the position for given element or element set. Offsets are
 * available from SourcePositions, but these are not precise enough for
 * generator. -- Generator has to handle comments and empty lines and
 * spaces too.
 *
 * @author Pavel Flaska
 */
abstract class PositionEstimator {
    
    int[][] matrix;
    
    /**
     * Initialize data for provided list.
     *
     * @param  oldL  list of existing elements, i.e. imports, members etc.
     * @param  copy  copy used for obtaining positions, tokens etc.
     */
    public abstract void initialize(List<? extends JCTree> oldL, WorkingCopy copy);

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
    public abstract int prepare(final int startPos, StringBuilder aHead, StringBuilder aTail);
    
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
    public int[][] getMatrix() { return matrix; }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementors
    static class ImplementsEstimator extends BaseEstimator {
        ImplementsEstimator() {
            super(IMPLEMENTS);
        }
    }
    
    static class ExtendsEstimator extends BaseEstimator {
        ExtendsEstimator() {
            super(EXTENDS);
        }
    }
    
    static class ThrowsEstimator extends BaseEstimator {
        ThrowsEstimator() {
            super(THROWS);
        }
    }

    /**
     * Provides positions for imports section. Computes positions for exisiting
     * imports and suggest insert position for newly added/inserted import.
     */
    static class ImportsEstimator extends PositionEstimator {
        
        TokenSequence<JavaTokenId> seq;
        int size = 0;
        
        @Override()
        public void initialize(List<? extends JCTree> oldL, WorkingCopy copy) {
            size = oldL.size();
            matrix = new int[size+1][3];
            matrix[size] = new int[] { -1, -1, -1 };
            seq = copy.getTokenHiearchy().tokenSequence();
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int i = 0;
            
            for (JCTree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                
                seq.move(treeStart);
                int startIndex = seq.index();
                
                // go back to opening/closing curly, semicolon or other
                // token java-compiler important token.
                moveToSrcRelevant(seq, Direction.BACKWARD);
                seq.moveNext();
                int start = seq.index();
                seq.move(treeEnd);
                matrix[i++] = new int[] { start, startIndex, seq.index() };
                if (i == size) {
                    seq.move(treeEnd);
                    matrix[i][0] = seq.index();
                }
            }
        }

        @Override()
        public int getInsertPos(int index) {
            int tokenIndex = matrix[index][0];
            // cannot do any decision about the position - probably first
            // element is inserted, no information is available. Call has
            // to decide.
            if (tokenIndex == -1) return -1;
            seq.moveIndex(tokenIndex);
            return index == 0 ? goAfterLastNewLine(seq) : goAfterFirstNewLine(seq);
        }

        // when first element is inserted, analyse the spacing and
        // do decision about adding new lines.
        @Override()
        public int prepare(final int startPos, StringBuilder aHead, StringBuilder aTail) {
            int resultPos = startPos;
            seq.move(startPos);
            seq.movePrevious();
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
            int tokenIndex = matrix[index][0];
            seq.moveIndex(tokenIndex);
            int begin = goAfterLastNewLine(seq);
            seq.moveIndex(matrix[index][2]);
            int end = goAfterFirstNewLine(seq);
            return new int [] { begin, end };
        }
        
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

    }

    /**
     * Provides position estimator for features in type declaration.
     */
    static class MembersEstimator extends PositionEstimator {
        
        TokenSequence<JavaTokenId> seq;
        
        public void initialize(List<? extends JCTree> oldL, WorkingCopy copy) {
            int size = oldL.size();
            matrix = new int[size+1][5];
            matrix[size] = new int[] { -1, -1, -1, -1, -1 };
            seq = copy.getTokenHiearchy().tokenSequence();
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int i = 0;
            
            for (JCTree item : oldL) {
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
            int tokenIndex = matrix[index][2];
            // cannot do any decision about the position - probably first
            // element is inserted, no information is available. Call has
            // to decide.
            if (tokenIndex == -1) return -1;
            seq.moveIndex(tokenIndex);
            return goAfterFirstNewLine(seq);
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        public int[] getPositions(int index) {
            int begin = getInsertPos(index);
            seq.moveIndex(matrix[index][4]);
            int end = goAfterFirstNewLine(seq);
            return new int [] { begin, end };
        }
        
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            return startPos;
        }
        
    }
    
    /**
     * Provides position estimator for features in type declaration.
     */
    static class TopLevelEstimator extends PositionEstimator {
        
        TokenSequence<JavaTokenId> seq;
        
        public void initialize(List<? extends JCTree> oldL, WorkingCopy copy) {
            int size = oldL.size();
            matrix = new int[size+1][5];
            matrix[size] = new int[] { -1, -1, -1, -1, -1 };
            seq = copy.getTokenHiearchy().tokenSequence();
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            int i = 0;
            
            for (JCTree item : oldL) {
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
            int tokenIndex = matrix[index][2];
            // cannot do any decision about the position - probably first
            // element is inserted, no information is available. Call has
            // to decide.
            if (tokenIndex == -1) return -1;
            seq.moveIndex(tokenIndex);
            int off = goAfterFirstNewLine(seq);
            return off;
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        public int[] getPositions(int index) {
            int begin = getInsertPos(index);
            seq.moveIndex(matrix[index][4]);
            int end = goAfterFirstNewLine(seq);
            return new int [] { begin, end };
        }
        
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            return startPos;
        }
        
    }
    
    private static abstract class BaseEstimator extends PositionEstimator {
        
        JavaTokenId precToken;
        private ArrayList<String> separatorList;

        private BaseEstimator(JavaTokenId precToken) {
            this.precToken = precToken;
        }
        
        public String head() { return " " + precToken.fixedText() + " "; }
        public String sep()  { return ", "; }
        
        public void initialize(List<? extends JCTree> oldL, WorkingCopy copy) {
            separatorList = new ArrayList<String>(oldL.size());
            boolean first = true;
            int size = oldL.size();
            matrix = new int[size+1][5];
            matrix[size] = new int[] { -1, -1, -1, -1, -1 };
            TokenSequence<JavaTokenId> seq = copy.getTokenHiearchy().tokenSequence();
            int i = 0;
            SourcePositions positions = copy.getTrees().getSourcePositions();
            CompilationUnitTree compilationUnit = copy.getCompilationUnit();
            for (JCTree item : oldL) {
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
        }
        
        public String getIndentString() {
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
    }

    // todo (#pf): remove - used for debugging reasons, doesn't do good job
    public void tablePrint(int[][] matrix, TokenSequence seq) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < 5; j++) {
                int item = matrix[i][j];
                String s = "(nothing)";
                if (item > -1) {
                    seq.moveIndex(item);
                    s = "'" + seq.token().text();
                }
                s += "'                                           ";
                System.err.print(s.substring(0, 25));
            }
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
    private static JavaTokenId moveToSrcRelevant(TokenSequence<JavaTokenId> seq,
                                                 Direction dir)
    {
        return moveToDifferentThan(seq, dir, nonRelevant);
    }
    
    private static JavaTokenId moveToDifferentThan(
        TokenSequence<JavaTokenId> seq,
        Direction dir,
        EnumSet<JavaTokenId> set)
    {
        switch (dir) {
            case BACKWARD:
                while (seq.movePrevious() && set.contains(seq.token().id())) ;
                break;
            case FORWARD:
                while (seq.moveNext() && set.contains(seq.token().id())) ;
                break;
        }
        return seq.token().id();
    }
    
    private static int goAfterFirstNewLine(final TokenSequence<JavaTokenId> seq) {
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
                            while (index < c.length)
                                if (c[index] != ' ' && c[index] != '\t')
                                    break;
                                else
                                    ++index;
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
        return base;
    }
    
    /**
     * Represents non-relevant tokens in java source. (Tokens which are not
     * important for javac, i.e. line and block comments, empty lines and
     * whitespaces.)
     */
    static final EnumSet<JavaTokenId> nonRelevant = EnumSet.of(
            LINE_COMMENT, 
            BLOCK_COMMENT,
            JAVADOC_COMMENT,
            WHITESPACE
    );

    /**
     * Represents the direction to move. Either forward or backward.
     */
    private enum Direction {
        FORWARD, BACKWARD;
    }
}
