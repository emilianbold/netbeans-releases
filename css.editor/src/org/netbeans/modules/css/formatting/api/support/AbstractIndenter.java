/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.formatting.api.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.formatting.api.LexUtilities;
import org.netbeans.modules.css.formatting.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.css.formatting.api.embedding.JoinedTokenSequence.TokenSequenceWrapper;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.openide.util.Exceptions;

/**
 *
 */
abstract public class AbstractIndenter<T1 extends TokenId> {

    private Language<T1> language;
    private Context context;
    private int indentationSize;
    private boolean compoundIndent;

    protected static final boolean DEBUG = true;

    public AbstractIndenter(Language<T1> language, Context context, boolean compoundIndent) {
        this.language = language;
        this.context = context;
        this.compoundIndent = compoundIndent;
        initialize();
    }

    protected void initialize() {
        indentationSize = IndentUtils.indentLevelSize(getDocument());
    }

    protected final int getIndentationSize() {
        return indentationSize;
    }

    protected final Context getContext() {
        return context;
    }

    protected final BaseDocument getDocument() {
        return (BaseDocument)context.document();
    }

    protected final Language<T1> getLanguage() {
        return language;
    }

    /**
     * Iterate backwards from given offset and return offset in document which
     * is good start to base formatting of the rest of document on.
     */
    abstract protected int getFormatStableStart(JoinedTokenSequence<T1> ts, int startOffset, int endOffset);

    abstract protected List<IndentCommand> getLineIndent(IndenterContextData<T1> context, List<IndentCommand> preliminaryNextLineIndent);

    abstract protected List<T1> getWhiteSpaceTokens();

    protected boolean isWhiteSpaceToken(Token<T1> token) {
        return getWhiteSpaceTokens().contains(token.id());
    }





    // TODO: refactor these two methods:
    protected boolean isInlineBlockStartToken(Token<T1> token) {
        return false;
    }
    protected boolean isInlineBlockEndToken(Token<T1> token) {
        return false;
    }




    

    protected boolean indentEmptyLines() {
        return context.startOffset() != 0 || context.endOffset() != context.document().getLength();
    }

    private boolean isWithinLanguage(int startOffset, int endOffset) {
        for (Context.Region r : context.indentRegions()) {
            if ( (startOffset >= r.getStartOffset() && startOffset <= r.getEndOffset()) ||
                    (endOffset >= r.getStartOffset() && endOffset <= r.getEndOffset()) ||
                    (startOffset <= r.getStartOffset() && endOffset >= r.getEndOffset()) ) {
                    return true;
            }
        }
        return false;
    }

    public void reindent() {
        final BaseDocument doc = getDocument();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

        if (DEBUG) {
            System.err.println(">> AbstractIndenter based indenter: "+this.getClass().toString());
        }

        if (!isWithinLanguage(startOffset, endOffset)) {
            if (DEBUG) {
                System.err.println("Nothing to be done by "+this.getClass().toString());
            }
            return;
        }
        if (endOffset-startOffset < 4) { // for line reindents etc.
            boolean found = false;
            for (int offset = startOffset; offset <= endOffset; offset++) {
                Language l = LexUtilities.getLanguage(doc, offset);
                if (l != null && l.equals(language)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (DEBUG) {
                    System.err.println("Nothing to be done by "+this.getClass().toString());
                }
                return;
            }
        }

        if (DEBUG) {
            System.err.println(">> TokenHierarchy of file to be indented:");
            System.err.println(TokenHierarchy.get(doc));
        }

        try {

            List<JoinedTokenSequence.CodeBlock<T1>> blocks = LexUtilities.createCodeBlocks(doc, language);
            if (blocks == null) {
                // nothing to do:
                return;
            }
            if (DEBUG) {
                System.err.println(">> Code blocks:\n"+blocks);
            }

            JoinedTokenSequence joinedTS = JoinedTokenSequence.createFromCodeBlocks(blocks);

            // start on the beginning of line:
            int start = Utilities.getRowStart(doc, startOffset);
            // end after the last line:
            int end = Utilities.getRowEnd(doc, endOffset)+1;
            if (end > doc.getLength()) {
                end = doc.getLength();
            }

            int initialOffset = 0;
            if (start > 0) {
                TokenSequence<T1> ts = (TokenSequence<T1>)LexUtilities.getTokenSequence(doc, start, language);
                if (ts == null) {
                    initialOffset = start;
                } else {
                    initialOffset = getFormatStableStart(joinedTS, start, end);
                }
            }

            final List<Line> indentedLines = new ArrayList<Line>();

            boolean indentEmptyLines = indentEmptyLines();

            List<LinePair> linePairs = calculateLinePairs(blocks, initialOffset, end);

            processLanguage(joinedTS, linePairs, initialOffset, end, indentedLines);

            int lineStart = Utilities.getLineOffset(doc, start);
            int lineEnd = Utilities.getLineOffset(doc, end);
            // this is attempt (hopefully successful) to indent only single line
            // during indent:
            if (context.isIndent() && lineEnd > lineStart) {
                lineEnd--;
            }
            applyIndents(indentedLines, lineStart, lineEnd, indentEmptyLines);

        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    private List<LinePair> calculateLinePairs(List<JoinedTokenSequence.CodeBlock<T1>> blocks, int startOffset, int endOffset) throws BadLocationException {
        List<LinePair> lps = new ArrayList<LinePair>();
        LinePair lastOne = null;
        int startLine = Utilities.getLineOffset(getDocument(), startOffset);
        int endLine = Utilities.getLineOffset(getDocument(), endOffset);
        for (JoinedTokenSequence.CodeBlock<T1> block : blocks) {
            for (TokenSequenceWrapper<T1> tsw : block.tss) {
                if (tsw.isVirtual()) {
                    continue;
                }
                LinePair lp = new LinePair();
                lp.startingLine = Utilities.getLineOffset(getDocument(), LexUtilities.getTokenSequenceStartOffset(tsw.getTokenSequence()));
                lp.endingLine = Utilities.getLineOffset(getDocument(), LexUtilities.getTokenSequenceEndOffset(tsw.getTokenSequence()));
                if (lp.startingLine < startLine) {
                    if (startLine < lp.endingLine) {
                        lp.startingLine = startLine;
                    } else {
                        continue;
                    }
                }
                if (lp.endingLine > endLine) {
                    lp.endingLine = endLine;
                }
                if (lastOne != null && lastOne.endingLine == lp.startingLine) {
                    lastOne.endingLine = lp.endingLine;
                } else {
                    lps.add(lp);
                    lastOne = lp;
                }
            }
        }
        return lps;
    }

    public final class LinePair {
        private int startingLine;
        private int endingLine;

        public int getEndingLine() {
            return endingLine;
        }

        public int getStartingLine() {
            return startingLine;
        }

        @Override
        public String toString() {
            return "LP[" +startingLine+":"+endingLine+"]";
        }
    }

    protected int getBlockIndent(int offset, int indentSize) throws BadLocationException {
        BaseDocument doc = getDocument();
        int start = Utilities.getRowStart(doc, offset);
        start = Utilities.getFirstNonWhiteRow(doc, start-1, false);
        TokenSequence<? extends TokenId> ts = TokenHierarchy.get((Document)doc).tokenSequence();
        ts.move(start);
        ts.movePrevious();
        ts.moveNext();
        if (ts.language().equals(language) && ts.embedded() == null) {
            return 0;
        } else {
            return indentSize;
        }
    }

    /**
     * Iterates over given code blocks (decribed as pairs of start and end line)
     * and calls formatter on each line.
     *
     * Line can be skipped in two cases:
     * #1) line is blank and its first token is not of language of this formatter; or
     * #2) line does not start with language of our formatter and our language
     * is represented on the line only via whitespace tokens which can be ignored.
     *
     * If line does not start with language of the formatter then line.indentThisLine
     * is set to false.
     */
    protected void processLanguage(JoinedTokenSequence<T1> joinedTS, List<LinePair> lines,
            int overallStartOffset, int overallEndOffset,
            List<Line> lineIndents) throws BadLocationException {

        int lastLineIndex = -1;
        BaseDocument doc = getDocument();

        joinedTS.moveStart();
        joinedTS.moveNext();

        // iterate over blocks of code to indent:
        for (LinePair lp : lines) {
            // iterate over each line:
            for (int line = lp.startingLine; line <= lp.endingLine; line++) {

                // find line starting offset
                int rowStartOffset = Utilities.getRowStartFromLineOffset(doc, line);
                if (rowStartOffset < overallStartOffset) {
                    rowStartOffset = overallStartOffset;
                }

                // find first non-white character
                int firstNonWhite = Utilities.getRowFirstNonWhite(doc, rowStartOffset);

                // find line ending offset
                int rowEndOffset = Utilities.getRowEnd(doc, rowStartOffset);
                int nextLineStartOffset = rowEndOffset+1;

                boolean indentThisLine = true;
                boolean emptyLine = false;

                if (firstNonWhite != -1) {
                    // line contains some characters:
                    // move rowStartOffset to beginning of language
                    int newRowStartOffset = findLanguageOffset(joinedTS, rowStartOffset, rowEndOffset, true);
                    if (newRowStartOffset > overallEndOffset) {
                        continue;
                    }
                    // move rowEndOffset to end of language
                    rowEndOffset = findLanguageOffset(joinedTS, rowEndOffset, rowStartOffset, false);
                    rowStartOffset = newRowStartOffset;
                    // if this line does not contain any our "language" to format 
                    // then skip this line completely
                    if (rowStartOffset == -1 || rowEndOffset == -1) {
                        assert rowStartOffset == -1 && rowEndOffset == -1 : "if language start " +
                                "was not found them language end canot be found neither. " +
                                "firstNonWhite="+firstNonWhite+" joinedTS="+joinedTS;
                        continue;
                    }
                    if (rowEndOffset > overallEndOffset) {
                        rowEndOffset = overallEndOffset;
                    }
                    
                    // set indentThisLine to false if line does not start with language
                    // but process tokens from the line
                    indentThisLine = firstNonWhite == rowStartOffset;
                } else {
                    // line is empty:
                    emptyLine = true;

                    Language l = LexUtilities.getLanguage(getDocument(), rowStartOffset);
                    if (l == null || !l.equals(language)) {
                        // line is empty and first token on line is not from out language
                        continue;
                    }
                }

                // firstNonWhite must e within our language:
                if (firstNonWhite < rowStartOffset) {
                    firstNonWhite = rowStartOffset;
                }

                // is this line beginning of a new code block:
                boolean newBlock = lastLineIndex == -1 || line-lastLineIndex > 1;

                // if we are not formatting this line then force
                // start of new code block (which will also close previous block):
                if (!indentThisLine) {
                    newBlock = true;
                }

                // ask formatter for line indentation:
                IndenterContextData<T1> cd = new IndenterContextData(joinedTS, rowStartOffset, rowEndOffset, firstNonWhite, nextLineStartOffset);
                cd.setLanguageBlockStart(newBlock);
                List<IndentCommand> preliminaryNextLineIndent = new ArrayList<IndentCommand>();
                List<IndentCommand> iis = getLineIndent(cd, preliminaryNextLineIndent);
                if (iis.isEmpty()) {
                    throw new IllegalStateException("getLineIndent must always return at least IndentInstance.Type.NO_CHANGE");
                }

                // record line indentation:
                Line ln = new Line();
                ln.lineIndent = iis;
                ln.preliminaryNextLineIndent = preliminaryNextLineIndent;
                ln.offset = Utilities.getRowStartFromLineOffset(doc, line);
                ln.lineStartOffset = rowStartOffset;
                ln.lineEndOffset = rowEndOffset;
                ln.index = line;
                ln.indentThisLine = indentThisLine;
                ln.emptyLine = emptyLine;
                if (newBlock) {
                    ln.blockStart = true;
                    if (lineIndents.size() > 0) {
                        lineIndents.get(lineIndents.size()-1).blockEnd = true;
                    }
                }
                lineIndents.add(ln);

                // store last line index
                lastLineIndex = line;

                // debug line:
                if (DEBUG) {
                    debugIndentation(joinedTS, cd, iis, getDocument().getText(rowStartOffset, rowEndOffset-rowStartOffset+1).
                            replace("\n", "").replace("\r", "").trim());
                }
            }
        }
        // close last block:
        if (lineIndents.size() > 0) {
            lineIndents.get(lineIndents.size()-1).blockEnd = true;
        }
    }

    private int findLanguageOffset(JoinedTokenSequence<T1> joinedTS, int rowStartOffset, int rowEndOffset, boolean forward) {
        if (!joinedTS.move(rowStartOffset, forward)) {
            return -1;
        }
        if (!joinedTS.moveNext()) {
            if (!forward) {
                if (!joinedTS.movePrevious()) {
                    return -1;
                }
            } else {
                return -1;
            }
        }

        while ((forward ? joinedTS.offset() <= rowEndOffset :
                        (joinedTS.offset()+joinedTS.token().text().toString().length()) >= rowEndOffset)) {
            if (joinedTS.embedded() == null && joinedTS.language() == language && 
                    !joinedTS.isCurrentTokenSequenceVirtual()) {
                boolean ws = isWhiteSpaceToken(joinedTS.token());
                if (!ws) {
                    int tokenStart = joinedTS.offset();
                    int tokenEnd = joinedTS.offset() + joinedTS.token().text().toString().length();
                    int offset;
                    if (rowStartOffset >= tokenStart && rowStartOffset <= tokenEnd) {
                        offset = rowStartOffset;
                    } else if (rowStartOffset < tokenStart) {
                        offset = tokenStart;
                    } else {
                        offset = tokenEnd;
                    }
                    offset = findNonWhiteSpaceCharacter(joinedTS, offset, forward);
                    return offset;
                }
            }
            if ((forward ? !joinedTS.moveNext() : !joinedTS.movePrevious())) {
                break;
            }
        }

        return -1;
    }

    private int findNonWhiteSpaceCharacter(JoinedTokenSequence<T1> joinedTS, int offset, boolean forward) {
        String tokenText = joinedTS.token().text().toString();
        int tokenStart = joinedTS.offset();
        int tokenEnd = joinedTS.offset() + tokenText.length();
        int index = offset - tokenStart;
        if (!forward && index == tokenText.length()) {
            index--;
        }
        while ((forward ? index < tokenText.length() : index > 0) &&
                tokenText.charAt(index) == ' ') {
            if (forward) {
                index++;
            } else {
                index--;
            }
        }
        return tokenStart+index;

    }

    private static int getCalulatedPreviousIndent(List<IndentCommand> indentations, int shift) {
        return indentations.get(getCalulatedIndexOfPreviousIndent(indentations, shift)).getCalculatedIndentation();
    }

    private static int getCalulatedIndexOfPreviousIndent(List<IndentCommand> indentations, int shift) {
        int balance = 1;
        int i = indentations.size();
        do {
            i--;
            if (indentations.get(i).getType() == IndentCommand.Type.RETURN) {
                balance++;
            }
            if (indentations.get(i).getType() == IndentCommand.Type.INDENT) {
                balance--;
            }
        } while (balance != 0 && i > 0);
        if (balance != 0 || indentations.get(i).getType() != IndentCommand.Type.INDENT) {
            if (DEBUG) {
                System.err.println("WARNING: cannot find INDENT command corresponding to RETURN " +
                        "command at index "+(indentations.size()-1)+". make sure RETURN and INDENT commands are always paired. " +
                        "this can be caused by wrong getFormatStableStart. commands:"+indentations);
            }
            if (i+shift < 0) {
                i = 0 - shift;
            }
        }
        assert i+shift >= 0 : "i="+i+" shift="+shift;
        return i+shift;
    }

    private boolean isPreviousLineEndsWithContinue(List<IndentCommand> allPreviousCommands) {
        if (allPreviousCommands.size() == 0) {
            return false;
        }
        return allPreviousCommands.get(allPreviousCommands.size()-1).getType() == IndentCommand.Type.CONTINUE;
    }

    private int calculateLineIndent(int indentation, List<Line> lines, Line line, Line previousLine,
            List<IndentCommand> currentLineIndents, List<IndentCommand> allPreviousCommands,
            boolean update, boolean beingFormatted, int lineStart) throws BadLocationException {
        int thisLineIndent = 0;
        int returnToLine = -1;
        List<IndentCommand> allCommands = new ArrayList<IndentCommand>(allPreviousCommands);
        boolean previousLineEndsWithContinue = isPreviousLineEndsWithContinue(allPreviousCommands);
        if (previousLineEndsWithContinue && currentLineIndents.size() > 0 &&
                currentLineIndents.get(0).getType() != IndentCommand.Type.CONTINUE) {
            indentation = getCalulatedPreviousIndent(allCommands, 0);
            thisLineIndent = 0;
        }
        for (IndentCommand ii : currentLineIndents) {
            switch (ii.getType()) {
                case NO_CHANGE:
                    break;
                case INDENT:
                case SINGLE_INDENT:
                    if (ii.getFixedIndentSize() != -1) {
                        thisLineIndent = ii.getFixedIndentSize();
                    } else {
                        thisLineIndent += indentationSize;
                    }
                    break;
                case CONTINUE:
                    // only first occurance of CONTINUE is indented:
                    if (previousLineEndsWithContinue) {
                        // do nothing
                    } else {
                        if (ii.getFixedIndentSize() != -1) {
                            thisLineIndent = ii.getFixedIndentSize();
                        } else {
                            thisLineIndent += indentationSize;
                        }
                    }
                    break;
                case RETURN:
                    int index = getCalulatedIndexOfPreviousIndent(allCommands, -1);
                    returnToLine = Utilities.getLineOffset(getDocument(), allCommands.get(index).getLineOffset());
                    if (returnToLine >= lineStart) {
                        returnToLine = -1;
                    }
                    indentation = allCommands.get(index).getCalculatedIndentation();
                    //int previousIndentIndex = findIndexOfPreviousIndent(allIndentComands, currentIndex);
                    //assert previousIndentIndex > 0;
                    //indentation = allIndentComands.get(previousIndentIndex-1).getCalculatedIndentation();
                    thisLineIndent = 0;
                    break;
                case DO_NOT_INDENT_THIS_LINE:
                    if (update) {
                        line.indentThisLine = false;
                    }
                    break;
                case PRESERVE_INDENTATION:
                    if (update) {
                        line.preserveThisLineIndent = true;
                    }
                    break;
                case BLOCK_START:
                    if (update) {
                        line.compoundStart = true;
                    }
                    break;
                case BLOCK_END:
                    if (update) {
                        line.compoundEnd = true;
                    }
                    break;
            }
            ii.setCalculatedIndentation(indentation+thisLineIndent);
            allCommands.add(ii);
        }
        if (update) {
            if (beingFormatted) {
                int diff = 0;
                if (returnToLine != -1) {
                    // get existing and calcualted indent of this line
                    Line l = findLineByLineIndex(lines, returnToLine);
                    if (l != null && !l.emptyLine) {
                        if (l.existingLineIndent != 0) {
                            diff = l.existingLineIndent - l.indentation;
                        }
                    }
                } else if (previousLine != null && previousLine.existingLineIndent != 0) {
                    // use diff from previous line
                    diff = previousLine.existingLineIndent - previousLine.indentation;
                }
                indentation += diff;
            } else {
                // compare calculated indentation to existing one and store difference:
                line.existingLineIndent = GsfUtilities.getLineIndent(getDocument(), line.offset);
            }

            line.indentation = indentation + thisLineIndent;
        }
        return indentation + thisLineIndent;
    }

    private Line findLineByLineIndex(List<Line> lines, int index) {
        for (Line l : lines) {
            if (l.index == index) {
                return l;
            } else if (l.index > index) {
                break;
            }
        }
        return null;
    }

    protected void applyIndents(final List<Line> indentedLines,
            final int lineStart, final int lineEnd, final boolean indentEmptyLines) {
        getDocument().runAtomic(new Runnable() {
            public void run() {
                try {
                    applyIndents0(indentedLines, lineStart, lineEnd, indentEmptyLines);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
        });
    }

    protected void applyIndents0(List<Line> indentedLines,
            int lineStart, int lineEnd, boolean indentEmptyLines) throws BadLocationException {

        if (DEBUG) {
            System.err.println(">> reindentation done by "+this.getClass()+":");
        }

        int indentation = 0;
        List<IndentCommand> commands = new ArrayList<IndentCommand>();
        Map<Integer, Integer> lineIndents = new TreeMap<Integer, Integer>();
        Line previousLine = null;
        int emptyLinesCount = 0;
        int index = 0;
        for (Line line : indentedLines) {

            if ((line.blockStart && !context.isPrimaryFormatter()) ||
                    (previousLine != null && !previousLine.indentThisLine && previousLine.blockStart && !context.isPrimaryFormatter())) {
                int ind = context.getLineInitialIndent(line.index);
                if (ind != -1) {
                    indentation = ind;
                } else {

                    // problem is that sometimes I may want to continue with current indent
                    // and sometimes I may want to ignore it and use indent from previous line;
                    // TODO: identify concrete cases and design solution for them

//                    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");


                    // for now commen this out to make indentantaion of JSP's '%>' work
                    /*if (line.index > 0) {
                        indentation = GsfUtilities.getLineIndent(getDocument(), line.index-1);
                    }*/
                }
            }
            if (previousLine != null && previousLine.index+1+emptyLinesCount != line.index) {
                previousLine = null;
            }
            indentation = calculateLineIndent(indentation, indentedLines, line, previousLine, line.lineIndent, commands, true, line.index >= lineStart, lineStart);
            if (line.emptyLine && !indentEmptyLines) {
                line.indentation = 0;
            }
            if (line.blockEnd || index+1 == indentedLines.size()) {
                if (context.getLineInitialIndent(line.index+1) == -1 && line.indentThisLine) {
                    List<IndentCommand> commands2 = new ArrayList<IndentCommand>(commands);
                    commands2.addAll(line.lineIndent);
                    int indent = calculateLineIndent(indentation, indentedLines, null, line, line.preliminaryNextLineIndent, commands2, false, line.index+1 >= lineStart, lineStart);
                    lineIndents.put(line.index+1, indent);
                }
            }
            commands.addAll(line.lineIndent);
            if (line.emptyLine) {
                emptyLinesCount++;
            } else {
                previousLine = line;
                emptyLinesCount = 0;
            }

            index++;
        }


        // go through compound blocks and generate lines for them:
        List<Line> indents = new ArrayList<Line>();
        int lastStart = -1;
        for (Line line : indentedLines) {
            if (line.compoundStart) {
                lastStart = line.index;
            }
            if (line.compoundEnd) {
                if (lastStart == -1) {
                    assert false : "found line.compoundEnd without start: "+indentedLines;
                }
                if (lastStart != line.index) {
                    int end = line.index;
                    if (!line.indentThisLine) {
                        // if end line is not indentable than treat it as part of
                        // BLOCK to shift (eg. JSP code "javaCall(); %>")
                        end++;
                    }
                    assert (indents.size() > 0 ? indents.get(indents.size()-1).index <= lastStart : true) :
                        "start="+lastStart+" end="+end+" indents="+indents+" indentedLines="+indentedLines;
                    for (int i = lastStart+1; i < end; i++) {
                        Line line2 = new Line();
                        line2.index = i;
                        line2.indentThisLine = true;
                        line2.preserveThisLineIndent = true;
                        line2.offset = Utilities.getRowStartFromLineOffset(getDocument(), i);
                        indents.add(line2);
                    }
                }
                lastStart = -1;
            }
            indents.add(line);
        }
        indentedLines = indents;

        // iterate through lines and ignore all lines with line.indentThisLine == false
        // search for line.preserveThisLineIndent and apply indent calculated using
        // last non-preserveThisLineIndent's line indent
        Line lineBeforePreserveIndent = null;
        for (Line line : indentedLines) {
            if (!line.indentThisLine) {
                continue;
            }
            if (line.preserveThisLineIndent) {
                if (lineBeforePreserveIndent != null) {
                    int originalFirstLineIndent = GsfUtilities.getLineIndent(getDocument(), lineBeforePreserveIndent.offset);
                    int originalCurrentLineIndent = GsfUtilities.getLineIndent(getDocument(), line.offset);
                    line.indentation = lineBeforePreserveIndent.indentation + (originalCurrentLineIndent-originalFirstLineIndent);
                } else {
                    assert false : "lineBeforePreserveIndent was not found: "+indentedLines;
                }
            } else {
                lineBeforePreserveIndent = line;
            }
        }

       // debug
        if (DEBUG) {
            System.err.println(">> line data:");
            for (Line line : indentedLines) {
                System.err.println(line.dump());
            }
        }

        if (DEBUG) {
            for (Line line : indentedLines) {
                if (line.indentThisLine) {
                    debugLineIndentation(line, line.index >= lineStart && line.index <= lineEnd);
                }
            }
        }

        if (DEBUG && lineIndents.keySet().size() > 0) {
            System.err.println(">> set line indents:");
        }
        for (Map.Entry<Integer, Integer> ent : lineIndents.entrySet()) {
            context.setLineInitialIndent(ent.getKey(), ent.getValue());
            if (DEBUG) {
                System.err.println(""+(ent.getKey().intValue()+1)+":"+ent.getValue());
            }
        }

        // iterate through lines backwards and ignore all lines with line.indentThisLine == false
        // modify line's indent using calculated indentation
        for (int i=indentedLines.size()-1; i>=0; i--) {
            Line line = indentedLines.get(i);
            if (!line.indentThisLine || line.index < lineStart || line.index > lineEnd) {
                continue;
            }
            int currentIndent = GsfUtilities.getLineIndent(getDocument(), line.offset);
            int newIndent = line.indentation;
            if (newIndent < 0) {
                newIndent = 0;
            }
            if (currentIndent != newIndent) {
                context.modifyIndent(line.offset, newIndent);
            }
        }


    }

    protected void debugIndentation(JoinedTokenSequence<T1> joinedTS, IndenterContextData cd, List<IndentCommand> iis, String text) {
        try {
            int index = Utilities.getLineOffset(getDocument(), cd.getLineStartOffset());
            System.err.println(String.format("[%4d]", index+1)+text);
            for (IndentCommand ii : iis) {
                System.err.println("      "+ii);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void debugLineIndentation(Line ln, boolean indentable) throws BadLocationException {
        String line = getDocument().getText(ln.lineStartOffset, ln.lineEndOffset-ln.lineStartOffset+1).replace("\n", "").replace("\r", "").trim();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%1c[%4d]", indentable ? '*' : ' ', ln.index+1));
        for (int i=0; i<ln.indentation; i++) {
            sb.append('.');
        }
        sb.append(line);
        if (sb.length() > 75) {
            sb.setLength(75);
        }

        System.err.println(sb.toString());
    }

    public static final class Line {
        private List<IndentCommand> lineIndent;
        private List<IndentCommand> preliminaryNextLineIndent;
        private boolean blockStart;
        private boolean blockEnd;
        private int offset;
        private int lineStartOffset;
        private int lineEndOffset;
        private int index;
        private boolean indentThisLine = true;
        private boolean preserveThisLineIndent;
        private int indentation;
        private boolean emptyLine;
        private boolean compoundStart;
        private boolean compoundEnd;
        private int existingLineIndent;

        public String dump() {
            return String.format("[%4d]", index+1)+
                    " offset="+offset+
                    " ("+lineStartOffset+
                    "-"+lineEndOffset+
                    ") indent=" +indentation+
                    ((existingLineIndent != 0) ? " existingIndent="+existingLineIndent : "") +
                    (blockStart? " blockStart" : "") +
                    (blockEnd? " blockEnd" : "") +
                    (compoundStart? " compStart" : "") +
                    (compoundEnd? " compEnd" : "") +
                    (preserveThisLineIndent? " preserve" : "") +
                    (emptyLine? " empty" : "") +
                    (!indentThisLine? " noIndent" : "");
        }

        @Override
        public String toString() {
            return "Line["+
                    "index="+index+
                    ",lineOffset="+offset+
                    ",startOffset="+lineStartOffset+
                    ",endOffset="+lineEndOffset+
                    ",indentation=" +indentation+
                    ((existingLineIndent != 0) ? " existingIndent="+existingLineIndent : "") +
                    (blockStart? ",blockStart" : "") +
                    (blockEnd? ",blockEnd" : "") +
                    (preserveThisLineIndent? ",preserveThisLineIndent" : "") +
                    (emptyLine? ",empty" : "") +
                    (!indentThisLine? ",doNotIndentThisLine" : "") +
                    ",lineIndent=" +lineIndent+
                    "]";
        }

    }

}
