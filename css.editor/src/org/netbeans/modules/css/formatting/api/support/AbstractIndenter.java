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
import java.util.Collections;
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

    private static final boolean DEBUG = true;

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

    public void reindent() {
        final BaseDocument doc = getDocument();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

        if (DEBUG) {
            if (/*context.isPrimaryFormatter()*/true) {
                System.err.println(">> TokenHierarchy of file to be indented:");
                System.err.println(TokenHierarchy.get(doc));
            }
            System.err.println(">> AbstractIndenter based indenter: "+this.getClass().toString());
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

    protected void processLanguage(JoinedTokenSequence<T1> joinedTS, List<LinePair> lines,
            int overallStartOffset, int overallEndOffset,
            List<Line> lineIndents) throws BadLocationException {

        int lastLineIndex = -1;
        BaseDocument doc = getDocument();
        List<IndentCommand> indentations;

        joinedTS.moveStart();
        joinedTS.moveNext();

        for (LinePair lp : lines) {
            for (int line = lp.startingLine; line <= lp.endingLine; line++) {

                // find line starting offset
                int rowStartOffset = Utilities.getRowStartFromLineOffset(doc, line);
                if (rowStartOffset < overallStartOffset) {
                    rowStartOffset = overallStartOffset;
                }

                int firstNonWhite = Utilities.getRowFirstNonWhite(doc, rowStartOffset);

                // find line ending offset
                int rowEndOffset = Utilities.getRowEnd(doc, rowStartOffset);
                int nextLineStartOffset = rowEndOffset+1;
                if (rowEndOffset > overallEndOffset) {
                    rowEndOffset = overallEndOffset;
                }

                boolean indentThisLine = true;
                boolean emptyLine = false;
                if (firstNonWhite != -1) {

                    // move rowStartOffset to beginning of language
                    rowStartOffset = findLanguageOffset(joinedTS, rowStartOffset, rowEndOffset, true);
                    // move rowEndOffset to end of language
                    rowEndOffset = findLanguageOffset(joinedTS, rowEndOffset, rowStartOffset, false);
                    if (rowStartOffset == -1 || rowEndOffset == -1) {
                        continue;
                    }
                    
                    // set indentThisLine to false if line does not start with language
                    indentThisLine = firstNonWhite == rowStartOffset;
                } else {
                    emptyLine = true;

                    // attempt to resolve case when JSP is trying to indent empty lines which
                    // should be indented by HTML indenter
                    if (!context.isPrimaryFormatter()) {
                        continue;
                    }
                }

                if (firstNonWhite < rowStartOffset) {
                    firstNonWhite = rowStartOffset;
                }

                IndenterContextData<T1> cd = new IndenterContextData(joinedTS, rowStartOffset, rowEndOffset, firstNonWhite, nextLineStartOffset);
                boolean newBlock = lastLineIndex == -1 || line-lastLineIndex > 1;
                cd.setLanguageBlockStart(newBlock);


                // if one of the lines was ignored (ie. indentThisLine is false) then do
                // not update lastLineIndex so that newBlock flag is set consequently.
                // experimental: I'm not sure whether to treat empty line as new block sematic
                //   possibly to be revised.
                if (!indentThisLine) {
                    newBlock = true;
                }



                List<IndentCommand> preliminaryNextLineIndent = new ArrayList<IndentCommand>();
                List<IndentCommand> iis = getLineIndent(cd, preliminaryNextLineIndent);
                if (iis.isEmpty()) {
                    throw new IllegalStateException("getLineIndent must always return at least IndentInstance.Type.NO_CHANGE");
                }
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
                if (DEBUG) {
                    debugIndentation(joinedTS, cd, iis, getDocument().getText(rowStartOffset, rowEndOffset-rowStartOffset+1).
                            replace("\n", "").replace("\r", "").trim());
                }

                lastLineIndex = line;



                // if one of the lines was ignored (ie. indentThisLine is false) then do
                // not update lastLineIndex so that newBlock flag is set consequently.
                // experimental: I'm not sure whether to treat empty line as new block sematic
                //   possibly to be revised.
                if (!indentThisLine) {
                    lastLineIndex = -1;
                }


            }
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
            throw new IllegalStateException("cannot find INDENT command corresponding to RETURN " +
                    "command at index "+(indentations.size()-1)+". make sure RETURN and INDENT commands are always paired. " +
                    "this can be caused by wrong getFormatStableStart. commands:"+indentations);
        }
        assert i+shift >= 0 : "i="+i+" shift="+shift;
        return indentations.get(i+shift).getCalculatedIndentation();
    }

    private static int findIndexOfPreviousIndent(List<IndentCommand> indentations, int index) {
        assert index > 0;
        int balance = 1;
        int i = index;
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
            throw new IllegalStateException("cannot find INDENT command corresponding to RETURN " +
                    "command at index "+index+". make sure RETURN and INDENT commands are always paired. " +
                    "this can be caused by wrong getFormatStableStart. commands:"+indentations);
        }
        return i;
    }

    private boolean isPreviousLineEndsWithContinue(List<IndentCommand> allPreviousCommands) {
        if (allPreviousCommands.size() == 0) {
            return false;
        }
        return allPreviousCommands.get(allPreviousCommands.size()-1).getType() == IndentCommand.Type.CONTINUE;
    }

    protected int calculateLineIndent(int indentation, Line line, List<IndentCommand> currentLineIndents, List<IndentCommand> allPreviousCommands, boolean update) {
        int thisLineIndent = 0;
        List<IndentCommand> allCommands = new ArrayList<IndentCommand>(allPreviousCommands);
        for (IndentCommand ii : currentLineIndents) {
            //lineOffset = ii.getLineOffset();
            //blankLine = false; //ii.isBlankLine(); // TODO: XXX: detect whether line is blank or not
    //                    if (ii.getType() == IndentCommand.Type.BLOCK_INDENT) {
    //                        assert i == 0 : "BLOCK_INDENT should be first on line";
    //                        indentation = ii.getFixedIndentSize();
    //                        newBlock = true;
    //                        //index++;
    //                        continue;
    //                    }
            if (isPreviousLineEndsWithContinue(allPreviousCommands) && ii.getType() != IndentCommand.Type.CONTINUE) {
                indentation = getCalulatedIndexOfPreviousIndent(allCommands, 0);
                //indentation = allIndentComands.get(previousIndentIndex).getCalculatedIndentation();
                thisLineIndent = 0;
            }
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
                    if (isPreviousLineEndsWithContinue(allPreviousCommands)) {
                        // only first occurance of CONTINUE is indented
                    } else {
                        if (ii.getFixedIndentSize() != -1) {
                            thisLineIndent = ii.getFixedIndentSize();
                        } else {
                            thisLineIndent += indentationSize;
                        }
                    }
                    break;
                case RETURN:
                    indentation = getCalulatedIndexOfPreviousIndent(allCommands, -1);
                    //int previousIndentIndex = findIndexOfPreviousIndent(allIndentComands, currentIndex);
                    //assert previousIndentIndex > 0;
                    //indentation = allIndentComands.get(previousIndentIndex-1).getCalculatedIndentation();
                    thisLineIndent = 0;
                    break;
                case DO_NOT_INDENT_THIS_LINE:
                    line.indentThisLine = false;
                    break;
                case PRESERVE_INDENTATION:
                    line.preserveThisLineIndent = true;
                    break;
                case BLOCK_START:
                    line.compoundStart = true;
                    break;
                case BLOCK_END:
                    line.compoundEnd = true;
                    break;
            }
            ii.setCalculatedIndentation(indentation+thisLineIndent);
            allCommands.add(ii);
        }
        if (update) {
            line.indentation = indentation + thisLineIndent;
        }
        return indentation + thisLineIndent;
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
        for (Line line : indentedLines) {

            if (line.blockStart && !context.isPrimaryFormatter()) {
                int ind = context.getLineInitialIndent(line.index);
                if (ind != -1) {
                    indentation = ind;
                } else {

                    // problem is that sometimes I may want to continue with current indent
                    // and sometimes I may want to ignore it and use indent from previous line;
                    // TODO: identify concrete cases and design solution for them

                    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");


                    // for now commen this out to make indentantaion of JSP's '%>' work
                    /*if (line.index > 0) {
                        indentation = GsfUtilities.getLineIndent(getDocument(), line.index-1);
                    }*/
                }
            }
            indentation = calculateLineIndent(indentation, line, line.lineIndent, commands, true);
            if (line.emptyLine && !indentEmptyLines) {
                line.indentation = 0;
            }
            if (line.blockEnd) {
                if (context.getLineInitialIndent(line.index+1) == -1) {
                    int indent = calculateLineIndent(indentation, line, line.preliminaryNextLineIndent, commands, false);
                    lineIndents.put(line.index+1, indent);
                }
            }
            commands.addAll(line.lineIndent);
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

        // adjust indentation based on previous line
        Line previousLine = null;
        int difference = -1;
        boolean update = false;
        for (Line line : indentedLines) {
            if (line.index == lineStart && previousLine != null && previousLine.index+1 == line.index) {
                int realIndentOfPreviousLine = GsfUtilities.getLineIndent(getDocument(), previousLine.offset);
                difference = realIndentOfPreviousLine - previousLine.indentation;
                update = true;
            }
            if (update) {
                line.indentation += difference;
            }
            previousLine = line;
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

//    private static List<List<IndentCommand>> groupIndentationsByLine(List<IndentCommand> indentations) {
//        List<List<IndentCommand>> l = new ArrayList<List<IndentCommand>>();
//        List<IndentCommand> lineIndentations = null;
//        IndentCommand lastIndentCommand = null;
//        for (IndentCommand ic : indentations) {
//            if (lineIndentations == null) {
//                lineIndentations = new ArrayList<IndentCommand>();
//                l.add(lineIndentations);
//                lineIndentations.add(ic);
//            } else {
//                if (lastIndentCommand.getLineOffset() == ic.getLineOffset()) {
//                    lineIndentations.add(ic);
//                } else {
//                    lineIndentations = new ArrayList<IndentCommand>();
//                    l.add(lineIndentations);
//                    lineIndentations.add(ic);
//                }
//            }
//            lastIndentCommand = ic;
//        }
//        return l;
//    }
//
//    private static class IndentationPair {
//        public int offset;
//        public int indentation;
//        public int indentationBase;
//        /** do not indent this line */
//        public boolean noIndent;
//
//        public IndentationPair(int offset, int indentation, int indentationBase) {
//            this.offset = offset;
//            this.indentation = indentation;
//            this.indentationBase = indentationBase;
//            this.noIndent = false;
//        }
//
//        @Override
//        public String toString() {
//            return "IndentationPair[offset="+offset+",indentation="+indentation+", base="+indentationBase+"]";
//        }
//
//    }

    /**
     * Returns true if formatter operates on text embedded in a document; that is
     * it mime of document differs from mime this formatter is written for.
     */
    protected boolean isEmbedded(Document document) {
        String mimeType = (String)document.getProperty("mimeType"); // NOI18N
        return !language.mimeType().equals(mimeType);
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

        public String dump() {
            return String.format("[%4d]", index+1)+
                    " offset="+offset+
                    " ("+lineStartOffset+
                    "-"+lineEndOffset+
                    ") indent=" +indentation+
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
