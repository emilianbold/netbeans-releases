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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.formatting.api.LexUtilities;
import org.netbeans.modules.css.formatting.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.css.formatting.api.embedding.JoinedTokenSequence.TokenSequenceWrapper;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.openide.util.Exceptions;

/**
 *
 */
abstract public class AbstractIndenter<T1 extends TokenId> {

    private Language<T1> language;
    private Context context;
    private int indentationSize;

    protected static final boolean DEBUG = true;

    public static boolean inUnitTestRun = false;

    private IndenterFormattingContext formattingContext;

    public AbstractIndenter(Language<T1> language, Context context) {
        this.language = language;
        this.context = context;
        indentationSize = IndentUtils.indentLevelSize(getDocument());
        formattingContext = new IndenterFormattingContext(getDocument());
    }

    public final IndenterFormattingContext createFormattingContext() {
        return formattingContext;
    }

    public final void beforeReindent(List<IndenterFormattingContext> contexts) {
        IndenterFormattingContext first = null;
        IndenterFormattingContext last = null;
        for (IndenterFormattingContext ifc : contexts) {
            if (ifc.isInitialized()) {
                return;
            }
            if (first == null) {
                first = ifc;
                first.initFirstIndenter();
            } else {
                ifc.setDelegate(first);
            }
            last = ifc;
        }
        assert first != null;
        assert last != null;
        last.setLastIndenter();
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
     * Iterate backwards from given start offset and return offset in document which
     * is good start to base formatting of document between given offsets. Returned
     * formatting start should ideally be a begining of language block which
     * encompasses everything between startOffset and endOffset.
     *
     * @return offset representing stable start for formatting
     */
    abstract protected int getFormatStableStart(JoinedTokenSequence<T1> ts, int startOffset, int endOffset,
            AbstractIndenter.OffsetRanges rangesToIgnore);

    /**
     * Calculate and return list of indentation commands for the given line.
     *
     * @param context descriptor of line to be formatted
     * @param preliminaryNextLineIndent list of indentation commands to apply
     *  for next lines based on available data; correctness is limited as nothing
     *  is known about next line yet
     * @return list of indentation commands to apply when formatting this line
     */
    abstract protected List<IndentCommand> getLineIndent(IndenterContextData<T1> context, List<IndentCommand> preliminaryNextLineIndent);

    /**
     * Is this whitespace token?
     */
    abstract protected boolean isWhiteSpaceToken(Token<T1> token);


    private static final class OffsetRange {
        private int start;
        private int end;

        public OffsetRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getEnd() {
            return end;
        }

        public int getStart() {
            return start;
        }

        @Override
        public String toString() {
            return "OffsetRange["+start+"-"+end+"]";
        }

    }

    /**
     * Descriptor of range wihtin documente defined by (inclusive) document offsets.
     */
    public static final class OffsetRanges {
        List<OffsetRange> ranges;

        public OffsetRanges() {
            this.ranges = new ArrayList<OffsetRange>();
        }

        /**
         * Add new range. Bordering ranges are automatically merged.
         */
        public void add(int start, int end) {
            for (OffsetRange range : ranges) {
                if (range.end == start) {
                    range.end = end;
                    return;
                } else if (range.start == end) {
                    range.start = start;
                    return;
                }
            }
            ranges.add(new OffsetRange(start, end));
        }

        /**
         * Is area given by start and end offset within this range?
         */
        private boolean contains(int start, int end) {
            for (OffsetRange or : ranges) {
                if (start >= or.getStart() && end <= or.getEnd()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Adjusts given start and end offset to describe area not covered
         * by a range. Exceptions are: #1) if given start-end region lies within
         * a range then -1 values will be returned; and #2) if there is a range
         * which lies within start-end region it will be ignored.
         * @param result array of two integers which will be populated by this 
         *  method; first one will be new start and second one will be new end
         * @return true if something was calculated
         */
        private boolean calculateUncoveredArea(int start, int end, int[] result) {
            boolean changed = false;
            for (OffsetRange or : ranges) {
                if (start < or.getStart() && end > or.getEnd()) {
                    // ignore-area lies within the line -> just process whole line
                    continue;
                }
                if (start >= or.getStart() && end <= or.getEnd()) {
                    // skip this line
                    result[0] = -1;
                    result[1] = -1;
                    return true;
                }
                if (start >= or.getStart() && start <= or.getEnd()) {
                    start = or.getEnd()+1;
                    changed = true;
                    assert start < end;
                }
                if (end >= or.getStart() && end <= or.getEnd()) {
                    end = or.getStart()-1;
                    changed = true;
                    assert start < end;
                }
            }
            if (changed) {
                result[0] = start;
                result[1] = end;
            }
            return changed;
        }

        public boolean isEmpty() {
            return ranges.size() == 0;
        }

        public String dump() {
            StringBuilder sb = new StringBuilder();
            for (OffsetRange or : ranges) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append("["+or.start+"-"+or.end+"]");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "OffsetRanges["+dump()+"]";
        }

    }

//    private boolean isWithinLanguage(int startOffset, int endOffset) {
//        for (Context.Region r : context.indentRegions()) {
//            if ( (startOffset >= r.getStartOffset() && startOffset <= r.getEndOffset()) ||
//                    (endOffset >= r.getStartOffset() && endOffset <= r.getEndOffset()) ||
//                    (startOffset <= r.getStartOffset() && endOffset >= r.getEndOffset()) ) {
//                    return true;
//            }
//        }
//        return false;
//    }

    public final void reindent() {
        formattingContext.disableListener();
        try {
            if (!formattingContext.isFirstIndenter()) {
                // if there were document changes done by some other formatter
                // then update offsets of all lines we are keeping in memory:
                List<IndenterFormattingContext.Change> l = formattingContext.getAndClearChanges();
                if (l.size() > 0) {
                    updateLineOffsets(l);
                }
            }
            calculateIndentation();
            applyIndentation();
        } catch (BadLocationException ble) {
            if (inUnitTestRun) {
                throw new RuntimeException(ble);
            }
            Exceptions.printStackTrace(ble);
        } finally {
            if (formattingContext.isLastIndenter()) {
                formattingContext.removeListener();
            } else {
                formattingContext.enableListener();
            }
        }
    }

    private void calculateIndentation() throws BadLocationException {
        final BaseDocument doc = getDocument();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

        if (DEBUG) {
            System.err.println(">> AbstractIndenter based indenter: "+this.getClass().toString());
        }

// TODO: this needs to be revisited. the problem is that different formatters
//       may have influence on each other and therefore has to be run even if
//       they are not covered by current indentation region. for example
//       closing a JSP tag and pressing Enter may need to be indented accoring to
//       previous HTML tag which may be dozens line above current line
//
//        boolean withinLanguage = isWithinLanguage(startOffset, endOffset);
//        boolean justAfterOurLanguage = isJustAfterOurLanguage();
//        if (DEBUG && !withinLanguage && justAfterOurLanguage) {
//            System.err.println("enabling formatter because it is justAfterOurLanguage case");
//        }
//
//        // abort if formatting is not within our language
//        if (!withinLanguage && !justAfterOurLanguage) {
//            if (DEBUG) {
//                System.err.println("Nothing to be done by "+this.getClass().toString());
//            }
//            return;
//        }
//        // quick check whether this is new line indent and if it is within our language
//        if (endOffset-startOffset < 4 && !justAfterOurLanguage) { // for line reindents etc.
//            boolean found = false;
//            for (int offset = startOffset; offset <= endOffset; offset++) {
//                Language l = LexUtilities.getLanguage(doc, offset);
//                if (l != null && l.equals(language)) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                if (DEBUG) {
//                    System.err.println("Nothing to be done by "+this.getClass().toString());
//                }
//                return;
//            }
//        }

        if (DEBUG) {
            //System.err.println(">> TokenHierarchy of file to be indented:");
            //System.err.println(TokenHierarchy.get(doc));
        }

        // create chunks of our language from the document:
        List<JoinedTokenSequence.CodeBlock<T1>> blocks = LexUtilities.createCodeBlocks(doc, language);
        if (blocks == null) {
            // nothing to do:
            return;
        }
        if (DEBUG) {
            //System.err.println(">> Code blocks:\n"+blocks);
        }

        // create joined TokenSequence for our language
        JoinedTokenSequence joinedTS = JoinedTokenSequence.createFromCodeBlocks(blocks);

        // start on the beginning of line:
        int start = Utilities.getRowStart(doc, startOffset);
        // end after the last line:
        int end = Utilities.getRowEnd(doc, endOffset)+1;
        if (end > doc.getLength()) {
            end = doc.getLength();
        }

        int initialOffset = 0;
        OffsetRanges rangesToIgnore = new OffsetRanges();
        if (start > 0) {
            // find point of stable formatting start if start position not zero
            TokenSequence<T1> ts = (TokenSequence<T1>)LexUtilities.getTokenSequence(doc, start, language);
            if (ts == null) {
                initialOffset = start;
            } else {
                initialOffset = getFormatStableStart(joinedTS, start, end, rangesToIgnore);
                if (DEBUG && !rangesToIgnore.isEmpty()) {
                    System.err.println("Ignored ranges: "+rangesToIgnore.dump());
                }
            }
        }

        // list of lines with their indentation
        final List<Line> indentedLines = new ArrayList<Line>();

        // get list of code blocks of our language in form of [line start number, line end number]
        List<LinePair> linePairs = calculateLinePairs(blocks, initialOffset, end);
        if (DEBUG) {
            System.err.println("line pairs to process="+linePairs);
        }

        // process blocks of our language and record data for each line:
        processLanguage(joinedTS, linePairs, initialOffset, end, indentedLines, rangesToIgnore);

        assert formattingContext.getIndentationData() != null;
        List<List<Line>> indentationData = formattingContext.getIndentationData();
        indentationData.add(indentedLines);
    }

    private void applyIndentation() throws BadLocationException {
        if (!formattingContext.isLastIndenter()) {
            // last formatter will apply changes
            return;
        }

        // recalcualte line numbers according to new offsets
        recalculateLineIndexes();

        // apply line data into concrete indentation:
        int lineStart = Utilities.getLineOffset(getDocument(), context.startOffset());
        int lineEnd = Utilities.getLineOffset(getDocument(), context.endOffset());
        assert formattingContext.getIndentationData() != null;
        List<List<Line>> indentationData = formattingContext.getIndentationData();

        List<Line> indentedLines = mergeIndentedLines(indentationData);
        if (DEBUG) {
            System.err.println("Merged line data:");
            for (Line l : indentedLines) {
                debugIndentation(l.lineStartOffset, l.lineIndent, getDocument().getText(l.lineStartOffset, l.lineEndOffset-l.lineStartOffset+1).
                        replace("\n", "").replace("\r", "").trim(), l.indentThisLine);
            }
        }

        applyIndents(indentedLines, lineStart, lineEnd, false);
    }

    private static class LineCommandsPair {
        private int line;
        private List<IndentCommand> commands;

        public LineCommandsPair(int line, List<IndentCommand> commands) {
            this.line = line;
            this.commands = commands;
        }

        @Override
        public String toString() {
            return "LineCommandsPair["+line+":"+commands+"]";
        }


    }

    private List<Line> mergeIndentedLines(List<List<Line>> indentationData) throws BadLocationException {

        // iterate over individual List<Line> and translate CONTINUE 
        // to simple INDENT/RETURN commands
        for (List<Line> l : indentationData) {
            addLanguageEndLine(l);
        }

        // iterate over individual List<Line> and move indents after GAP
        // to special list of pairs [line number, commands]
        // + get rid of non-formattable lines; if non-formatable line contains
        // INDENT follow above procedure but set line number to removed line;
        List<LineCommandsPair> pairs = new ArrayList<LineCommandsPair>();
        for (List<Line> l : indentationData) {
            simplifyIndentationCommands(pairs, l);
            handleLanguageGaps(pairs, l);
            extractCommandsFromNonIndentableLines(pairs, l);
        }

        // merge all the lines
        List<Line> all = new ArrayList<Line>();
        for (List<Line> l : indentationData) {
            all = mergeIndentedLines(all, l);
        }

        // apply stored commands per lines
        applyStoredCommads(all, pairs);

        return all;
    }

    private void handleLanguageGaps(List<LineCommandsPair> pairs, List<Line> lines) {
        List<Line> newLines = new ArrayList<Line>();
        Line prevLine = null;
        for (Line l : lines) {
            if (prevLine != null && prevLine.index+1 != l.index) {
                // there was a gap; move all INDENT commands from beginning of line:
                List<IndentCommand> removed = new ArrayList<IndentCommand>();
                List<IndentCommand> kept = new ArrayList<IndentCommand>();
                boolean keepRemoving = true;
                for (IndentCommand ic : l.lineIndent) {
                    if (keepRemoving && ic.getType() == IndentCommand.Type.INDENT) {
                        removed.add(ic);
                    } else {
                        kept.add(ic);
                        keepRemoving = false;
                    }
                }
                l.lineIndent = kept;
                if (removed.size() > 0) {
                    // should go to beginning of line:
                    pairs.add(new LineCommandsPair(prevLine.index+1, removed));
                }
            }
            newLines.add(l);
            prevLine = l;
        }
        lines.clear();
        lines.addAll(newLines);
    }

    private void extractCommandsFromNonIndentableLines(List<LineCommandsPair> pairs, List<Line> lines) {
        List<Line> newLines = new ArrayList<Line>();
        Line previousLine = null;
        for (Line l : lines) {
            if (!l.indentThisLine) {
                List<IndentCommand> accepted = new ArrayList<IndentCommand>();
                List<IndentCommand> nextLine = new ArrayList<IndentCommand>();
                for (IndentCommand ic : l.lineIndent) {
                    if (ic.getType() == IndentCommand.Type.INDENT) {
                        // should go to beginning of line:
                        accepted.add(ic);
                    } else if (ic.getType() == IndentCommand.Type.BLOCK_END ||
                            ic.getType() == IndentCommand.Type.BLOCK_START) {
                        // should go to end of line but does not really matter:
                        accepted.add(ic);
                    } else if (ic.getType() == IndentCommand.Type.RETURN) {
                        if ((previousLine == null || previousLine.index+1 != l.index) && !l.emptyLine) {
                            // if this is first line of language block then put commands
                            // on next line rather than current, for example
                            //
                            // 05:      color:red;
                            // 06:   } </style>
                            //
                            // line 6 is end of CSS within HTML's <style> tag and
                            // if RETURN command corresponding to </style> was
                            // applied on line 06 it would be wrong - it needs to
                            // be applied to next line and line 6 should be indented
                            // fully by CSS formatter

                            // should go to beginning of line:
                            nextLine.add(ic);
                        } else {
                            // should go to beginning of line:
                            accepted.add(ic);
                        }
                    }
                }
                if (accepted.size() > 0) {
                    pairs.add(new LineCommandsPair(l.index, accepted));
                }
                if (nextLine.size() > 0) {
                    pairs.add(new LineCommandsPair(l.index+1, nextLine));
                }
                l.lineIndent = new ArrayList<IndentCommand>();
                newLines.add(l);
            } else {
                newLines.add(l);
            }
            previousLine = l;
        }
        lines.clear();
        lines.addAll(newLines);
    }

    private void addLanguageEndLine(List<Line> lines) throws BadLocationException {
        // check what last line of language suggests about next line:
        if (lines.size() == 0) {
            return;
        }
        Line lastLine = lines.get(lines.size()-1);
        if (lastLine.preliminaryNextLineIndent.size() == 0) {
            return;
        }
        int lineIndex = lastLine.index+1;
        int offset = Utilities.getRowStartFromLineOffset(getDocument(), lineIndex);
        if (offset == -1) {
            return;
        }
        Line l = generateBasicLine(lineIndex);
        l.indentThisLine = false;
        l.lineIndent = new ArrayList<IndentCommand>(lastLine.preliminaryNextLineIndent);
        l.preliminaryNextLineIndent = new ArrayList<IndentCommand>();
        lines.add(l);
    }

    /**
     * Merge lines and if there are two lines then accept one which is indentable.
     */
    private static List<Line> mergeIndentedLines(List<Line> originalLines, List<Line> newLines) {
        List<Line> merged = new ArrayList<Line>();
        Iterator<Line> it1 = originalLines.iterator();
        Iterator<Line> it2 = newLines.iterator();
        Line l1 = null;
        Line l2 = null;
        if (it1.hasNext()) {
            l1 = it1.next();
        }
        if (it2.hasNext()) {
            l2 = it2.next();
        }
        boolean move1;
        boolean move2;
        while (l1 != null && l2 != null) {

            if (l1.index < l2.index) {
                merged.add(l1);
                move1 = true;
                move2 = false;
            } else if (l1.index > l2.index) {
                merged.add(l2);
                move1 = false;
                move2 = true;
            } else {
                if (l1.indentThisLine) {
                    assert !l2.indentThisLine :
                        "two lines which claim to be indentable: l1="+l1+" l2="+l2;
                    merged.add(l1);
                } else {
                    assert !l1.indentThisLine;
                    merged.add(l2);
                }
                move1 = true;
                move2 = true;
            }
            if (move1) {
                if (it1.hasNext()) {
                    l1 = it1.next();
                } else {
                    l1 = null;
                }
            }
            if (move2) {
                if (it2.hasNext()) {
                    l2 = it2.next();
                } else {
                    l2 = null;
                }
            }
        }

        if (l1 != null) {
            merged.add(l1);
        } else if (l2 != null) {
            merged.add(l2);
        }
        while (it1.hasNext()) {
            merged.add(it1.next());
        }
        while (it2.hasNext()) {
            merged.add(it2.next());
        }

        return merged;
    }

    /**
     * Apply indent commands which we collected previously. These might be for
     * example commands from non-indentable lines, etc.
     */
    private void applyStoredCommads(List<Line> all, List<LineCommandsPair> pairs) throws BadLocationException {
        Comparator<LineCommandsPair> c = new Comparator<LineCommandsPair>() {
            public int compare(LineCommandsPair o1, LineCommandsPair o2) {
                return o1.line - o2.line;
            }
        };
        //
        // sort pairs and then merge them; there can be multiple pairs
        // for one line and their order should be preserved, eg.
        //
        // 01: <table    id=smth
        // 02:           class=smth>
        // 03:    <p>
        //
        // line 3 will have RETURN and INDENT commands and they have to stay
        // in that order. If swapped the resulting indent would be
        //
        // 03:           <p>
        //
        // line was first indented and then returned back to previous position
        // which alligns with "class" attribute
        //
        List<LineCommandsPair> s1 = new ArrayList<LineCommandsPair>(pairs);
        Collections.sort(s1, c);
        List<LineCommandsPair> s2 = new ArrayList<LineCommandsPair>();
        LineCommandsPair previous = null;
        // merge commands on one line:
        for (LineCommandsPair pair : s1) {
            if (previous != null && previous.line == pair.line) {
                previous.commands.addAll(pair.commands);
            } else {
                s2.add(pair);
                previous = pair;
            }
        }

        Iterator<Line> it = all.iterator();
        assert all.size() > 0;
        Line l = null;
        Line lastLine = null;
        for (LineCommandsPair pair : s2) {
            while (it.hasNext() && (l == null || l.index < pair.line)) {
                l = it.next();
            }
            assert l != null;
            if (l.index >= pair.line) {
                List<IndentCommand> commands = new ArrayList();
                for (IndentCommand ic : pair.commands) {
                    if (ic.getType() == IndentCommand.Type.BLOCK_START ||
                            ic.getType() == IndentCommand.Type.BLOCK_END) {
                        assert l.index == pair.line;
                    }
                    commands.add(ic);
                }
                for (IndentCommand ic : l.lineIndent) {
                    if (ic.getType() != IndentCommand.Type.NO_CHANGE ||
                            (ic.getType() == IndentCommand.Type.NO_CHANGE && commands.size() == 0)) {
                        commands.add(ic);
                    }
                }
                l.lineIndent = commands;
            } else {
                assert !it.hasNext();
                // put all commands on the last line;
                // that should do the trick
                if (lastLine == null) {
                    int offset = Utilities.getRowStartFromLineOffset(getDocument(), pair.line);
                    if (offset == -1) {
                        // lines does not exist so ignore:
                        break;
                    }
                    lastLine = generateBasicLine(pair.line);
                    lastLine.lineIndent = new ArrayList<IndentCommand>(pair.commands);
                    lastLine.preliminaryNextLineIndent = new ArrayList<IndentCommand>();
                } else {
                    lastLine.lineIndent.addAll(pair.commands);
                }
            }
        }
        if (lastLine != null) {
            all.add(lastLine);
        }

    }

    /**
     * Replace CONTINUE with simple INDENT and RETURN commands.
     */
    private void simplifyIndentationCommands(List<LineCommandsPair> pairs, List<Line> lines) {
        boolean firstContinue = true;
        boolean inContinue = false;
        boolean fixedIndentContinue = false;
        Line lastLineWithContinue = null;
        for (Line l : lines) {
            List<IndentCommand> commands = new ArrayList<IndentCommand>();
            for (IndentCommand ic : l.lineIndent) {
                if (ic.getType() == IndentCommand.Type.CONTINUE) {
                    if (firstContinue) {
                        if (ic.getFixedIndentSize() != -1) {
                            IndentCommand ic2 = new IndentCommand(IndentCommand.Type.INDENT, ic.getLineOffset());
                            ic2.setFixedIndentSize(ic.getFixedIndentSize());
                            commands.add(ic2);
                            fixedIndentContinue = true;
                        } else {
                            commands.add(new IndentCommand(IndentCommand.Type.INDENT, ic.getLineOffset()));
                            //commands.add(new IndentCommand(IndentCommand.Type.INDENT, ic.getLineOffset()));
                            fixedIndentContinue = false;
                        }
                        firstContinue = false;
                        inContinue = true;
                    }
                    lastLineWithContinue = l;
                } else {
                    if (inContinue) {
                        List<IndentCommand> listToAddTo = commands;
                        assert lastLineWithContinue != null;
                        if (l.index - lastLineWithContinue.index > 1) {
                            List<IndentCommand> list = new ArrayList<IndentCommand>();
                            // should go to beginning of line:
                            pairs.add(new LineCommandsPair(lastLineWithContinue.index+1, list));
                            listToAddTo = list;
                        }
                        if (fixedIndentContinue) {
                            listToAddTo.add(new IndentCommand(IndentCommand.Type.RETURN, ic.getLineOffset()));
                        } else {
                            listToAddTo.add(new IndentCommand(IndentCommand.Type.RETURN, ic.getLineOffset()));
                            //listToAddTo.add(new IndentCommand(IndentCommand.Type.RETURN, ic.getLineOffset()));
                        }
                        inContinue = false;
                        firstContinue = true;
                    }
                    if (ic.getType() != IndentCommand.Type.NO_CHANGE || 
                            (ic.getType() == IndentCommand.Type.NO_CHANGE && commands.size() == 0)) {
                        commands.add(ic);
                    }
                }
            }
            if (commands.size() == 0) {
                commands.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, l.lineStartOffset));
            }
            l.lineIndent = commands;
        }
    }

    private void updateLineOffsets(List<IndenterFormattingContext.Change> l) {
        if (DEBUG) {
            System.err.println("update line offset with following deltas:"+l);
        }
        for (List<Line> lines : formattingContext.getIndentationData()) {
            for (Line line : lines) {
                for (IndenterFormattingContext.Change ch : l) {
                    if (ch.offset <= line.offset) {
                        line.updateOffset(ch.change);
                    }
                }
            }
        }
    }

    private void recalculateLineIndexes() throws BadLocationException {
        for (List<Line> lines : formattingContext.getIndentationData()) {
            for (Line line : lines) {
                line.recalculateLineIndex(getDocument());
            }
        }
    }

//    private boolean isJustAfterOurLanguage() {
//        // get start of formatted area
//        int start = context.startOffset();
//        if (start > 0) {
//            start--;
//        }
//
//        while (start > 0) {
//            try {
//                String text = getDocument().getText(start, 1).trim();
//                if (text.length() > 0) {
//                    System.err.println("isJustAfterOurLanguage found: "+text+" at "+start);
//                    break;
//                }
//                start--;
//            } catch (BadLocationException ex) {
//                Exceptions.printStackTrace(ex);
//                return false;
//            }
//        }
//        if (start == 0) {
//            return false;
//        }
//        Language l = LexUtilities.getLanguage(getDocument(), start);
//        System.err.println("isJustAfterOurLanguage lang:"+l);
//        return (l != null && l.equals(language));
//    }

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
                if (lp.startingLine > endLine) {
                    break;
                }
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

    private final class LinePair {
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

//    protected int getBlockIndent(int offset, int indentSize) throws BadLocationException {
//        BaseDocument doc = getDocument();
//        int start = Utilities.getRowStart(doc, offset);
//        start = Utilities.getFirstNonWhiteRow(doc, start-1, false);
//        TokenSequence<? extends TokenId> ts = TokenHierarchy.get((Document)doc).tokenSequence();
//        ts.move(start);
//        ts.movePrevious();
//        ts.moveNext();
//        if (ts.language().equals(language) && ts.embedded() == null) {
//            return 0;
//        } else {
//            return indentSize;
//        }
//    }

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
    private void processLanguage(JoinedTokenSequence<T1> joinedTS, List<LinePair> lines,
            int overallStartOffset, int overallEndOffset,
            List<Line> lineIndents,
            OffsetRanges rangesToIgnore) throws BadLocationException {

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

                // check whether line can be skip completely:
                if (rangesToIgnore.contains(rowStartOffset, rowEndOffset)) {
                    continue;
                }

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
                        continue;
                    }

                    if (rowStartOffset > rowEndOffset) {
                        // trying to handle properly following CSS case:
                        //
                        // 03:       }/*CC */
                        // 04:   </style>
                        //
                        // when line 4 is being processed the CSS token text
                        // is "/*CC */\n        " which results in rowEndOffset
                        // pointing just after '\n' and rowStartOffset just to
                        // the end of token text. This happens because token is
                        // not whitespace. For now I will ignore these lines and see.
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
                        // line is empty and first token on line is not from our language
                        continue;
                    }
                }

                int newOffset[] = new int[2];

                if (rangesToIgnore.calculateUncoveredArea(rowStartOffset, rowEndOffset, newOffset)) {
                    rowStartOffset = newOffset[0];
                    rowEndOffset = newOffset[1];
                    if (rowStartOffset == -1 && rowEndOffset == -1) {
                        continue;
                    }
                }

                // firstNonWhite must e within our language:
                if (firstNonWhite < rowStartOffset) {
                    firstNonWhite = rowStartOffset;
                }

                // ask formatter for line indentation:
                IndenterContextData<T1> cd = new IndenterContextData(joinedTS, rowStartOffset, rowEndOffset, firstNonWhite, nextLineStartOffset);
                cd.setLanguageBlockStart(line == lp.startingLine);
                List<IndentCommand> preliminaryNextLineIndent = new ArrayList<IndentCommand>();
                List<IndentCommand> iis = getLineIndent(cd, preliminaryNextLineIndent);
                if (iis.isEmpty()) {
                    throw new IllegalStateException("getLineIndent must always return at least IndentInstance.Type.NO_CHANGE");
                }
                if (preliminaryNextLineIndent.isEmpty()) {
                    throw new IllegalStateException("preliminaryNextLineIndent from getLineIndent must always return at least IndentInstance.Type.NO_CHANGE");
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
                lineIndents.add(ln);

                // debug line:
                if (DEBUG) {
                    debugIndentation(cd.getLineStartOffset(), iis, getDocument().getText(rowStartOffset, rowEndOffset-rowStartOffset+1).
                            replace("\n", "").replace("\r", "").trim(), ln.indentThisLine);
                }
            }
        }
        if (DEBUG && lineIndents.size() > 0) {
            List<IndentCommand> l = lineIndents.get(lineIndents.size()-1).preliminaryNextLineIndent;
            if (l.size() > 0) {
                System.err.println("Preliminary indent commands for next line:"+l);
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

    /**
     * This method is called when calculateLineIndent is processing RETURN command -
     * it iterates backward over list of all commands and tries to find corresponding
     * INDENT command which is being closed by the RETURN command.
     */
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
                        "this can be caused by wrong getFormatStableStart but also by user typing code which is not " +
                        "syntactically correct. commands:"+(indentations.size() < 30 ? indentations : "[too many commands]"));
            }
            if (i+shift < 0) {
                i = 0 - shift;
            }
        }
        assert i+shift >= 0 : "i="+i+" shift="+shift;
        return i+shift;
    }

    /**
     * Calculate indenation of this line for given base indentation and given
     * line indentation commands. This method can be called with update set to
     * false and with preliminary line commands given in currentLineIndents to
     * calculated indent of next line.
     */
    private int calculateLineIndent(int indentation, List<Line> lines, Line line,
            List<IndentCommand> currentLineIndents, List<IndentCommand> allPreviousCommands,
            boolean update, int lineStart) throws BadLocationException {

        boolean beingFormatted = line != null ? line.index >= lineStart : false;
        int thisLineIndent = 0;
        int returnToLine = -1;
        List<IndentCommand> allCommands = new ArrayList<IndentCommand>(allPreviousCommands);

        // iterate over indent commands for the given line and calculate line's indentation
        for (IndentCommand ii : currentLineIndents) {

            switch (ii.getType()) {

                case NO_CHANGE:
                    break;

                case INDENT:
                    if (ii.getFixedIndentSize() != -1) {
                        thisLineIndent = ii.getFixedIndentSize();
                    } else {
                        thisLineIndent += indentationSize;
                    }
                    break;

                case RETURN:
                    // find index of INDENT command which is being closed by this
                    // RETURN command and move to previous indent command (shift is -1)
                    // which indent will be used as a base, eg:
                    //
                    // 01: if (a) {          NO_CHANGE
                    // 02:  something1();    INDENT
                    // 03:  something2();    NO_CHANGE
                    // 04: }                 RETURN
                    //
                    // when RETURN command is found on line 04 it finds opening INDENT
                    // command on line 02 and moves one command prior to that:
                    // command NO_CHANGE from line 01. That one is going to be
                    // used as base for indentation of line 04
                    int index = getCalulatedIndexOfPreviousIndent(allCommands, -1);

                    // calculate line index of base indentation line and check whether
                    // that line lies within area being formatted or not; if the line
                    // is not being formatted than we have to respect whatever (possibly
                    // incorrect) formatting it has and in such a case we store line number
                    // in returnToLine variable for later evaluation:
                    returnToLine = Utilities.getLineOffset(getDocument(), allCommands.get(index).getLineOffset());
                    if (returnToLine >= lineStart) {
                        returnToLine = -1;
                    }

                    // use indentation of found command and override any indent
                    // calculated so far
                    indentation = allCommands.get(index).getCalculatedIndentation();
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
                        line.foreignLanguageBlockStart = true;
                    }
                    break;

                case BLOCK_END:
                    if (update) {
                        line.foreignLanguageBlockEnd = true;
                    }
                    break;

            }
            ii.setCalculatedIndentation(indentation+thisLineIndent);
            allCommands.add(ii);
        }

        if (update) {

            // is this line being formatted:
            if (beingFormatted) {
                int diff = 0;
                if (returnToLine != -1) {
                    // this is a special case when indent of this line should be
                    // based on the line which we were not formatting (and which
                    // is possibly misformatted):
                    Line l = findLineByLineIndex(lines, returnToLine);
                    if (l.emptyLine && returnToLine > 0) {
                        // this is attempt to fix following scenario:
                        //
                        // 01:          <table><tr><td>
                        // 02:
                        // 03:                  </td>
                        //
                        // when only line 3 is being indented then returnToLine
                        // will be pointing to line 2 which is empty because
                        // it is line 1 which generates INDENT commands
                        // for line 3. I'm not sure this fix is the best one:
                        Line l2 = findLineByLineIndex(lines, returnToLine-1);
                        if (!l2.emptyLine) {
                            l = l2;
                        }
                    }
                    if (l != null && !l.emptyLine) {
                        // get existing line indent and indent which we calculated
                        // and use difference to adjust this line's indent, eg:
                        //
                        //                     CURRENT   CALCULATED
                        // 01:if (a) {         0         0
                        // 02: if (b) {        1         4
                        // 03:  if (c)         2         8
                        // 04:   smth();       3         12
                        //
                        // in above example if Enter was pressed at the end of line 4 then:
                        // * indentation of line 05 should be based according to line 03
                        // * only line 05 is being indented
                        // * calculated indent of line 03 is 8 and that what indent of line 05
                        //   would be if we formatted whole block
                        // * because line 03 lies outside of formatted area we have to adjust
                        //   calculated indentation 8 by 03.current-03.calculated which is -6

                        assert l.existingLineIndent != -1 : "line is missing existingLineIndent "+l;
                        diff = l.existingLineIndent - l.indentation;
                    }
                } else if (line.index == lineStart) {
                    Line previousNonEmptyLine = findPreviousNonEmptyLine(lines, line.index);
                    if (previousNonEmptyLine != null) {
                        // variation on previous case (see above for more details):
                        // in this case previousLine was not formattable and so current line
                        // needs to be adjusted as well
                        diff = previousNonEmptyLine.existingLineIndent - previousNonEmptyLine.indentation;
                    }
                }
                indentation += diff;
                line.indentationAdjustment = diff;
            }

            // store existing line indent:
            line.existingLineIndent = GsfUtilities.getLineIndent(getDocument(), line.offset);

            // set calculated indentation:
            line.indentation = indentation + thisLineIndent;
        }
        return indentation + thisLineIndent;
    }

    private Line findLineByLineIndex(List<Line> lines, int index) {
        for (Line l : lines) {
            if (l == null) {
                continue;
            }
            if (l.index == index) {
                return l;
            } else if (l.index > index) {
                break;
            }
        }
        return null;
    }

    private Line findPreviousNonEmptyLine(List<Line> lines, int index) {
        for (int i = lines.size()-1; i>=0; i--) {
            Line l = lines.get(i);
            if (l.index < index && !l.emptyLine) {
                return l;
            }
        }
        return null;
    }

    private void applyIndents(final List<Line> indentedLines,
            final int lineStart, final int lineEnd, final boolean justAfterOurLanguage) throws BadLocationException {
        final BadLocationException ex[] = new BadLocationException[1];
        getDocument().runAtomic(new Runnable() {
            public void run() {
                try {
                    applyIndents0(indentedLines, lineStart, lineEnd, justAfterOurLanguage);
                } catch (BadLocationException ble) {
                    ex[0] = ble;
                }
            }
        });
        if (ex[0] != null) {
            throw ex[0];
        }
    }

    private void applyIndents0(List<Line> indentedLines,
            int lineStart, int lineEnd, boolean justAfterOurLanguage) throws BadLocationException {

        if (DEBUG) {
            System.err.println(">> reindentation done by "+this.getClass()+":");
        }

        // indentation should indent empty lines; format should not
        boolean indentEmptyLines = context.isIndent();

        int indentation = 0;
        List<IndentCommand> commands = new ArrayList<IndentCommand>();
        int index = 0;

        // iterate over lines indentation commands and calculate real indentation
        for (Line line : indentedLines) {

            // calculate indentation:
            indentation = calculateLineIndent(indentation, indentedLines, line,
                    line.lineIndent, commands, true, lineStart);

            // force zero indent if line is empty and empty lines should not be indented
            if (line.emptyLine && !indentEmptyLines) {
                line.indentation = 0;
            }

            commands.addAll(line.lineIndent);
            index++;
        }

        // generate line indents for lines within a block:
        indentedLines = generateBlockIndentsForForeignLanguage(indentedLines);

        // set line indent for preserved lines:
        updateIndentationForPreservedLines(indentedLines);

       // DEBUG info:
        if (DEBUG) {
            System.err.println(">> line data:");
            for (Line line : indentedLines) {
                System.err.println(" "+line.dump());
            }
            System.err.println(">> line indentations:");
            for (Line line : indentedLines) {
                if (line.indentThisLine) {
                    debugLineIndentation(line, line.index >= lineStart && line.index <= lineEnd);
                }
            }
        }

        // physically modify document's indentation
        applyIndentations(indentedLines, lineStart, lineEnd);
    }

    private List<Line> generateBlockIndentsForForeignLanguage(List<Line> indentedLines) throws BadLocationException {
        // go through compound blocks and generate lines for them:
        List<Line> indents = new ArrayList<Line>();
        int lastStart = -1;
        for (Line line : indentedLines) {
            if (line.foreignLanguageBlockStart) {
                lastStart = line.index;
            }
            if (line.foreignLanguageBlockEnd) {
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
                        Line line2 = generateBasicLine(i);//new Line();
                        //line2.index = i;
                        line2.indentThisLine = true;
                        line2.preserveThisLineIndent = true;
                        //line2.offset = Utilities.getRowStartFromLineOffset(getDocument(), i);
                        //line2.existingLineIndent = GsfUtilities.getLineIndent(getDocument(), line2.offset);
                        if (!line2.emptyLine) {
                            indents.add(line2);
                        }
                    }
                }
                lastStart = -1;
            }
            indents.add(line);
        }
        return indents;
    }

    private Line generateBasicLine(int index) throws BadLocationException {
        Line line = new Line();
        line.index = index;
        line.offset = Utilities.getRowStartFromLineOffset(getDocument(), index);
        line.existingLineIndent = GsfUtilities.getLineIndent(getDocument(), line.offset);
        line.emptyLine = Utilities.getRowFirstNonWhite(getDocument(), line.offset) == -1;
        line.lineStartOffset = line.offset;
        line.lineEndOffset = Utilities.getRowEnd(getDocument(), line.offset);

        return line;
    }

    private void updateIndentationForPreservedLines(List<Line> indentedLines) {
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
    }

    private void applyIndentations(List<Line> indentedLines, int lineStart, int lineEnd) throws BadLocationException {
        // iterate through lines backwards and ignore all lines with line.indentThisLine == false
        // modify line's indent using calculated indentation
        for (int i=indentedLines.size()-1; i>=0; i--) {
            Line line = indentedLines.get(i);
            if (!line.indentThisLine || line.index < lineStart || line.index > lineEnd) {
                continue;
            }
            //int currentIndent = GsfUtilities.getLineIndent(getDocument(), line.offset);
            int newIndent = line.indentation;
            if (newIndent < 0) {
                newIndent = 0;
            }
            assert line.existingLineIndent != -1 : "line is missing existingLineIndent "+line;
            if (line.existingLineIndent != newIndent) {
                context.modifyIndent(line.offset, newIndent);
            }
        }
    }

    private void debugIndentation(int lineOffset, List<IndentCommand> iis, String text, boolean indentable) throws BadLocationException {
        int index = Utilities.getLineOffset(getDocument(), lineOffset);
        char ch = ' ';
        if (indentable) {
            ch = '*';
        }
        System.err.println(String.format("%1c[%4d]", ch, index+1)+text);
        for (IndentCommand ii : iis) {
            System.err.println("      "+ii);
        }
    }

    private void debugLineIndentation(Line ln, boolean indentable) throws BadLocationException {
        String line = "";
        if (ln.lineStartOffset != ln.lineEndOffset) {
            line = getDocument().getText(ln.lineStartOffset, ln.lineEndOffset-ln.lineStartOffset+1).replace("\n", "").replace("\r", "").trim();
        }
        StringBuilder sb = new StringBuilder();
        char ch = ' ';
        if (indentable) {
            ch = '*';
        } else if (ln.preserveThisLineIndent) {
            ch = 'P';
        }
        sb.append(String.format("%1c[%4d]", ch, ln.index+1));
        for (int i=0; i<ln.indentation; i++) {
            sb.append('.');
        }
        sb.append(line);
        if (sb.length() > 75) {
            sb.setLength(75);
        }

        System.err.println(sb.toString());
    }

    static final class Line {
        private List<IndentCommand> lineIndent;
        private List<IndentCommand> preliminaryNextLineIndent;
        private int offset;
        private int lineStartOffset;
        private int lineEndOffset;
        private int index;
        private boolean indentThisLine = true;
        private boolean preserveThisLineIndent;
        private int indentation;
        private boolean emptyLine;
        private boolean foreignLanguageBlockStart;
        private boolean foreignLanguageBlockEnd;
        private int existingLineIndent = -1;

        // just for diagnostics:
        private int indentationAdjustment = -1;

        private void updateOffset(int diff) {
            offset += diff;
            lineStartOffset += diff;
            lineEndOffset += diff;
            for (IndentCommand ic : lineIndent) {
                ic.updateOffset(diff);
            }
            for (IndentCommand ic : preliminaryNextLineIndent) {
                ic.updateOffset(diff);
            }
        }

        private void recalculateLineIndex(BaseDocument doc) throws BadLocationException {
            index = Utilities.getLineOffset(doc, offset);
        }

        public String dump() {
            return String.format("[%4d]", index+1)+
                    " offset="+offset+
                    " ("+lineStartOffset+
                    "-"+lineEndOffset+
                    ") indent=" +indentation+
                    (indentationAdjustment > 0 ? "("+indentationAdjustment+")" : "") +
                    ((existingLineIndent != -1) ? " existingIndent="+existingLineIndent : "") +
                    (foreignLanguageBlockStart? " foreignLangBlockStart" : "") +
                    (foreignLanguageBlockEnd? " foreignLangBlockEnd" : "") +
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
                    (indentationAdjustment > 0 ? "("+indentationAdjustment+")" : "") +
                    ((existingLineIndent != -1) ? ",existingIndent="+existingLineIndent : "") +
                    (preserveThisLineIndent? ",preserveThisLineIndent" : "") +
                    (emptyLine? ",empty" : "") +
                    (!indentThisLine? ",doNotIndentThisLine" : "") +
                    ",lineIndent=" +lineIndent+
                    "]";
        }

    }

}
