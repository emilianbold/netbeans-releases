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

package org.netbeans.modules.editor.structure.formatting;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtFormatter;

/**
 * Handling embedded languages:
 *
 * When formatting a language that is split into several blocks:
 *
 * - for each line in block: line indent = indent calculated by formatter + block offset, taken from the first line, e.g.:
 *
 * XXXX
 *   <p>
 *   Hello, world!
 *    	</p>
 * XXXX
 *
 * will be formatted to:
 *
 * XXXX
 *   <p>
 *   	Hello, world!
 *    </p>
 * XXXX
 *
 * Note that indent of the first line doesn't change, the rest is formatted accordingly. The containing language formatter should always indent at least the first line of the embedded language block  to desired position.
 *
 * - if 2 blocks of the language being formatted surround a block of a different language the whole block will be shifted right by the indent level calculated for the current language, e.g:
 *
 * XXXXX
 *   <p>
 * XXXXX
 *   </p>
 * XXXXX
 *
 * will be formatted to:
 *
 * XXXXX
 *   <p>
 *   	XXXXX
 *   </p>
 * XXXXX
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class TagBasedLexerFormatter extends ExtFormatter {

    private static final Logger logger = Logger.getLogger(TagBasedLexerFormatter.class.getName());

    private enum LineFormattingType {NORMAL, RELATIVE, NONE}

    /** Creates a new instance of TagBases */
    public TagBasedLexerFormatter(Class kitClass) {
        super(kitClass);
    }

    protected abstract boolean isClosingTag(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract boolean isUnformattableToken(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract boolean isUnformattableTag(String tag);

    protected abstract boolean isOpeningTag(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract String extractTagName(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract boolean areTagNamesEqual(String tagName1, String tagName2);

    protected abstract boolean isClosingTagRequired(BaseDocument doc, String tagName);

    protected abstract int getOpeningSymbolOffset(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract int getTagEndingAtPosition(JoinedTokenSequence tokenSequence, int tagOffset) throws BadLocationException;

    protected abstract int getTagEndOffset(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract LanguagePath supportedLanguagePath();

    protected boolean isWSToken(Token token) {
        char[] chars = token.text().toString().toCharArray();

        for (char c : chars) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }

        return true;
    }

    protected int getIndentForTagParameter(BaseDocument doc, JoinedTokenSequence tokenSequence, int tagOffset) throws BadLocationException {
        int originalOffset = tokenSequence.offset();
        int tagStartLine = Utilities.getLineOffset(doc, tagOffset);
        tokenSequence.move(tagOffset);
        Token<? extends TokenId> token;
        int tokenOffset;
        boolean thereWasWS = false;
        int shift = doc.getShiftWidth(); // default;

        /*
         * Find the offset of the first attribute if it is specified on the same line as the opening of the tag
         * e.g. <tag   |attr=
         *
         */
        while (tokenSequence.moveNext()) {
            token = tokenSequence.token();
            tokenOffset = tokenSequence.offset();
            boolean isWSToken = isWSToken(token);
            
            if (thereWasWS && (!isWSToken || tagStartLine != Utilities.getLineOffset(doc, tokenOffset))) {
                if (!isWSToken && tagStartLine == Utilities.getLineOffset(doc, tokenOffset)) {
                    
                    shift = tokenOffset - Utilities.getRowIndent(doc, tokenOffset)
                            - Utilities.getRowStart(doc, tokenOffset);
                }
                break;
            } else if (isWSToken){
                thereWasWS = true;
            }
        }

        tokenSequence.move(originalOffset);
        tokenSequence.moveNext();

        return shift;
    }

    private boolean calcIndents_processOpeningTag(final BaseDocument doc, final JoinedTokenSequence tokenSequence, final String tagName, final Collection<TagIndentationData> unprocessedOpeningTags, final int[] indentsWithinTags) throws BadLocationException {

        boolean thereAreMoreTokens = true;
        // format content of a tag that spans across multiple lines
        int firstTagLine = Utilities.getLineOffset(doc, tokenSequence.offset());
        int tagEndOffset = getTagEndOffset(tokenSequence, tokenSequence.offset());
        int lastTagLine = Utilities.getLineOffset(doc, tagEndOffset);

        TagIndentationData tagData = new TagIndentationData(tagName, lastTagLine);
        unprocessedOpeningTags.add(tagData);

        if (firstTagLine < lastTagLine) {
            // performance!
            int indentWithinTag = getIndentForTagParameter(doc, tokenSequence, tokenSequence.offset());

            for (int i = firstTagLine + 1; i <= lastTagLine; i++) {
                indentsWithinTags[i] = indentWithinTag;
            }

            // if there is only the closing symbol on the last line of tag do not indent it
            thereAreMoreTokens &= tokenSequence.moveNext();

            while (Utilities.getLineOffset(doc, tokenSequence.offset()) < lastTagLine || isWSToken(tokenSequence.token())) {

                tokenSequence.moveNext();
            }

            if (tokenSequence.offset() == tagEndOffset) {
                indentsWithinTags[lastTagLine] = 0;
            }
        }

        return thereAreMoreTokens;
    }

    private void calcIndents_processClosingTag(final String tagName, final int tagClosedOnLine, final LineFormattingType[] lineFormatting, final LinkedList<TagIndentationData> unprocessedOpeningTags, final Collection<TagIndentationData> matchedOpeningTags) throws BadLocationException {
        LinkedList<TagIndentationData> tagsToBeRemoved = new LinkedList<TagIndentationData>();

        while (!unprocessedOpeningTags.isEmpty()) {
            TagIndentationData processedTD = unprocessedOpeningTags.removeLast();

            if (areTagNamesEqual(tagName, processedTD.getTagName())) {
                processedTD.setClosedOnLine(tagClosedOnLine);
                matchedOpeningTags.add(processedTD);

                // mark all the stuff between unformattable tag as unformattable
                if (isUnformattableTag(tagName)) {
                    for (int i = tagClosedOnLine - 1; i > processedTD.getLine(); i--) {
                        lineFormatting[i] = LineFormattingType.NONE;
                    }
                }

                // forgetting preceding tags permanently
                tagsToBeRemoved.clear();
                break;
            } else {
                tagsToBeRemoved.add(processedTD);
            }
        }

        // if matching opening tag was not found on the stack put all the tags back
        unprocessedOpeningTags.addAll(tagsToBeRemoved);
    }

    @Override
    public Writer reformat(BaseDocument doc, int startOffset, int endOffset, boolean indentOnly) throws BadLocationException {
        LinkedList<TagIndentationData> unprocessedOpeningTags = new LinkedList<TagIndentationData>();
        List<TagIndentationData> matchedOpeningTags = new ArrayList<TagIndentationData>();
        doc.atomicLock();
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);

        if (tokenHierarchy == null) {
            logger.severe("Could not retrieve TokenHierarchy for document " + doc);
            return null;
        }


        try {
            int lastLine = Utilities.getLineOffset(doc, doc.getLength());
            int firstRefBlockLine = Utilities.getLineOffset(doc, startOffset);
            int lastRefBlockLine = Utilities.getLineOffset(doc, endOffset);
            int firstUnformattableLine = -1;

            LineFormattingType[] lineFormatting = new LineFormattingType[lastLine + 1];
            int[] blockShiftOffsets = new int[lastLine + 1];
            // Only reindent lines that start with tokens of current language
            Arrays.fill(lineFormatting, LineFormattingType.RELATIVE);
            int[] indentsWithinTags = new int[lastLine + 1];

            TokenSequence[] tokenSequences = (TokenSequence[]) tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
            TextBounds[] tokenSequenceBounds = new TextBounds[tokenSequences.length];

            for (int i = 0; i < tokenSequenceBounds.length; i++) {
                tokenSequenceBounds[i] = findTokenSequenceBounds(doc, tokenSequences[i]);
                
                if (tokenSequenceBounds[i].getStartLine() > -1) { // skip white-space blocks
                    markCurrentLanguageLinesAsFormattable(doc, tokenSequenceBounds[i], lineFormatting);
                    int blockShiftOffset = findLanguageBlockShiftOffset(doc, tokenSequenceBounds[i]);

                    for (int j = tokenSequenceBounds[i].getStartLine(); j <= tokenSequenceBounds[i].getEndLine(); j++) {
                        blockShiftOffsets[j] = blockShiftOffset;
                    }
                }
            }

            if (tokenSequences.length > 0) {
                // calc line indents - pass 1
                JoinedTokenSequence tokenSequence = new JoinedTokenSequence(tokenSequences, tokenSequenceBounds);
                tokenSequence.moveStart();
                boolean thereAreMoreTokens = tokenSequence.moveNext();
                
                do {
                    boolean isOpenTag = isOpeningTag(tokenSequence, tokenSequence.offset());
                    boolean isCloseTag = isClosingTag(tokenSequence, tokenSequence.offset());

                    if (isOpenTag || isCloseTag) {

                        String tagName = extractTagName(tokenSequence, tokenSequence.offset());

                        if (isOpenTag) {

                            thereAreMoreTokens &= calcIndents_processOpeningTag(doc, tokenSequence, tagName, unprocessedOpeningTags, indentsWithinTags);
                        } else {
                            int tagLine = Utilities.getLineOffset(doc, tokenSequence.offset());
                            calcIndents_processClosingTag(tagName, tagLine, lineFormatting, unprocessedOpeningTags, matchedOpeningTags);
                        }
                    }

                    // process a block of unformattable tokens
                    boolean wasPreviousTokenUnformattable = isUnformattableToken(tokenSequence, tokenSequence.offset());

                    if (wasPreviousTokenUnformattable && firstUnformattableLine == -1) {
                        firstUnformattableLine = Utilities.getLineOffset(doc, tokenSequence.offset());
                    }

                    thereAreMoreTokens &= tokenSequence.moveNext();

                    // detect the end of an unformattable block; mark it
                    if (firstUnformattableLine > -1 && (!wasPreviousTokenUnformattable || !thereAreMoreTokens)) {

                        int lastUnformattableLine = thereAreMoreTokens ? Utilities.getLineOffset(doc, tokenSequence.offset() - 1) : lastLine;

                        for (int i = firstUnformattableLine + 1; i < lastUnformattableLine; i++) {
                            lineFormatting[i] = LineFormattingType.NONE;
                        }

                        firstUnformattableLine = -1;
                    }
                } while (thereAreMoreTokens);
            }

            //****************
            // calc line indents - pass 2
            // TODO: optimize it
            int[] indentLevels = new int[lastLine + 1];
            Arrays.fill(indentLevels, 0);

            for (TagIndentationData td : matchedOpeningTags) {
                // increase indent from one line after the opening tag
                // up to one line before the closing tag
                for (int i = td.getLine() + 1; i <= td.getClosedOnLine() - 1; i++) {
                    indentLevels[i]++;
                }
            }

            // when reformatting only a part of file
            // we need to take into account the local bias
            InitialIndentData initialIndentData = new InitialIndentData(doc, indentLevels, indentsWithinTags, firstRefBlockLine, lastRefBlockLine);

            // apply line indents
            int blockOfRelativeLinesOffset = -1;

            for (int line = firstRefBlockLine; line <= lastRefBlockLine; line++) {
                int lineStart = Utilities.getRowStartFromLineOffset(doc, line);

                if (lineFormatting[line] == LineFormattingType.RELATIVE) {
                    if (blockOfRelativeLinesOffset == -1) {
                        // the first line of a relative formatting block, remember the offset
                        blockOfRelativeLinesOffset = Utilities.getFirstNonWhiteFwd(doc, lineStart) - lineStart;
                    }
                } else {
                    // the end of a relative formatting block
                    blockOfRelativeLinesOffset = -1;
                }

                if (lineFormatting[line] != LineFormattingType.NONE && initialIndentData.isEligibleToIndent(line)) {

                    int relativeShift = 0;

                    if (lineFormatting[line] == LineFormattingType.RELATIVE) {
                        int existingIndent = Utilities.getFirstNonWhiteFwd(doc, lineStart) - lineStart;

                        relativeShift = existingIndent > blockOfRelativeLinesOffset ? existingIndent - blockOfRelativeLinesOffset : 0;
                    }

                    changeRowIndent(doc, lineStart, initialIndentData.getIndent(line) + blockShiftOffsets[line] + relativeShift + blockOfRelativeLinesOffset);
                }
            }
        } finally {
            doc.atomicUnlock();
        }

        return null;
    }

    protected int getMatchingOpeningTagStart(JoinedTokenSequence tokenSequence, int closingTagOffset) {
        int originalOffset = tokenSequence.offset();
        tokenSequence.move(closingTagOffset);

        String searchedTagName = extractTagName(tokenSequence, closingTagOffset);
        int balance = 0;

        while (tokenSequence.movePrevious()) {
            int currentTokenOffset = tokenSequence.offset();
            if (areTagNamesEqual(searchedTagName, extractTagName(tokenSequence, currentTokenOffset))) {
                if (isOpeningTag(tokenSequence, currentTokenOffset)) {
                    if (balance == 0) {
                        tokenSequence.move(originalOffset);
                        tokenSequence.moveNext();
                        return currentTokenOffset;
                    }

                    balance--;
                } else if (isClosingTag(tokenSequence, currentTokenOffset)) {
                    balance++;
                }
            }
        }

        tokenSequence.move(originalOffset);
        tokenSequence.moveNext();
        return -1;
    }

    protected int getInitialIndentFromPreviousLine(final BaseDocument doc, final int line) throws BadLocationException {

        // get initial indent from the previous line
        int initialIndent = 0;

        if (line > 0) {
            int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
            int previousNonWhiteLineEnd = Utilities.getFirstNonWhiteBwd(doc, lineStart);

            if (previousNonWhiteLineEnd > 0) {
                initialIndent = Utilities.getRowIndent(doc, previousNonWhiteLineEnd);
            }
        }

        return initialIndent;
    }

    private int findLanguageBlockShiftOffset(BaseDocument doc, TextBounds languageBounds) throws BadLocationException {
        // basic impl: simply return the offset from the first line
        int lineStart = Utilities.getRowStartFromLineOffset(doc, languageBounds.getStartLine());
        return Utilities.getFirstNonWhiteFwd(doc, lineStart) - lineStart;
    }

    private int getInitialIndentFromNextLine(final BaseDocument doc, final int line) throws BadLocationException {

        // get initial indent from the next line
        int initialIndent = 0;

        int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
        int lineEnd = Utilities.getRowEnd(doc, lineStart);
        int nextNonWhiteLineStart = Utilities.getFirstNonWhiteFwd(doc, lineEnd);

        if (nextNonWhiteLineStart > 0) {
            initialIndent = Utilities.getRowIndent(doc, nextNonWhiteLineStart, true);
        }

        return initialIndent;
    }

    protected static int getNumberOfLines(BaseDocument doc) throws BadLocationException {
        return Utilities.getLineOffset(doc, doc.getLength() - 1) + 1;
    }

    protected int getNextClosingTagOffset(JoinedTokenSequence tokenSequence, int offset) throws BadLocationException {
        int originalOffset = tokenSequence.offset();
        tokenSequence.move(offset);
        int currentOffset = -1;

        while (tokenSequence.moveNext()) {
            currentOffset = tokenSequence.offset();

            if (isClosingTag(tokenSequence, currentOffset)) {
                tokenSequence.move(originalOffset);
                tokenSequence.moveNext();
                return currentOffset;
            }
        }

        tokenSequence.move(originalOffset);
        tokenSequence.moveNext();
        return -1;
    }

    protected boolean isJustBeforeClosingTag(JoinedTokenSequence tokenSequence, int pos) throws BadLocationException {
        // default, trivial implementation
        if (isClosingTag(tokenSequence, pos)) {
            return true;
        }

        return false;
    }

    protected Token getTokenAtOffset(JoinedTokenSequence tokenSequence, int tagTokenOffset) {
        if (tokenSequence != null) {
            int originalOffset = tokenSequence.offset();
            tokenSequence.move(tagTokenOffset);

            if (tokenSequence.moveNext()) {
                Token r = tokenSequence.token();
                tokenSequence.move(originalOffset);
                tokenSequence.moveNext();
                return r;
            }
        }

        return null;
    }

    private TextBounds findTokenSequenceBounds(BaseDocument doc, TokenSequence tokenSequence) throws BadLocationException {
        tokenSequence.moveStart();
        tokenSequence.moveNext();
        int absoluteStart = tokenSequence.offset();
        tokenSequence.moveEnd();
        tokenSequence.movePrevious();
        int absoluteEnd = tokenSequence.offset() + tokenSequence.token().length();

//         trim whitespaces from both ends
        while (isWSToken(tokenSequence.token())) {
            if (!tokenSequence.movePrevious()) {
                return new TextBounds(absoluteStart, absoluteEnd); // a block of empty text
            }
        }

        int whiteSpaceSuffixLen = 0;

        while (Character.isWhitespace(tokenSequence.token().text().charAt(tokenSequence.token().length() - 1 - whiteSpaceSuffixLen))) {
            whiteSpaceSuffixLen++;
        }

        int languageBlockEnd = tokenSequence.offset() + tokenSequence.token().length() - whiteSpaceSuffixLen;

        tokenSequence.moveStart();

        do {
            tokenSequence.moveNext();
        } while (isWSToken(tokenSequence.token()));

        int whiteSpacePrefixLen = 0;

        while (Character.isWhitespace(tokenSequence.token().text().charAt(whiteSpacePrefixLen))) {
            whiteSpacePrefixLen++;
        }

        int languageBlockStart = tokenSequence.offset() + whiteSpacePrefixLen;
        int firstLineOfTheLanguageBlock = Utilities.getLineOffset(doc, languageBlockStart);
        int lastLineOfTheLanguageBlock = Utilities.getLineOffset(doc, languageBlockEnd);
        return new TextBounds(absoluteStart, absoluteEnd, languageBlockStart, languageBlockEnd, firstLineOfTheLanguageBlock, lastLineOfTheLanguageBlock);
    }

    private void markCurrentLanguageLinesAsFormattable(BaseDocument doc, TextBounds languageBounds, LineFormattingType[] lineFormatting) throws BadLocationException {
        if (languageBounds.getStartPos() == -1){
            return; // only white spaces
        }
        
        int firstLineOfTheLanguageBlock = languageBounds.getStartLine();

        int lineStart = Utilities.getRowStartFromLineOffset(doc, firstLineOfTheLanguageBlock);

        if (Utilities.getFirstNonWhiteFwd(doc, lineStart) < languageBounds.getStartPos()) {
            firstLineOfTheLanguageBlock++;
        }

        for (int i = firstLineOfTheLanguageBlock; i <= languageBounds.getEndLine(); i++) {
            lineFormatting[i] = LineFormattingType.NORMAL;
        }
    }
    
    //    protected void enterPressed(JTextComponent txtComponent, int dotPos) throws BadLocationException {
//        BaseDocument doc = Utilities.getDocument(txtComponent);
//        int lineNumber = Utilities.getLineOffset(doc, dotPos);
//        int initialIndent = getInitialIndentFromPreviousLine(doc, lineNumber);
//        int endOfPreviousLine = Utilities.getFirstNonWhiteBwd(doc, dotPos);
//        endOfPreviousLine = endOfPreviousLine == -1 ? 0 : endOfPreviousLine;
//        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
//
//        // workaround for \n passed from code completion to reformatter
//        if (lineNumber == Utilities.getLineOffset(doc, endOfPreviousLine)) {
//            return;
//        }
//
//        int openingTagOffset = getTagEndingAtPosition(tokenSequence, endOfPreviousLine);
//
//        if (isOpeningTag(tokenHierarchy, openingTagOffset)) {
//            int closingTagOffset = getNextClosingTagOffset(tokenHierarchy, dotPos + 1);
//
//            if (closingTagOffset != -1) {
//                int matchingOpeningTagOffset = getMatchingOpeningTagStart(tokenHierarchy, closingTagOffset);
//
//                if (openingTagOffset == matchingOpeningTagOffset) {
//
//                    int openingTagLine = Utilities.getLineOffset(doc, openingTagOffset);
//                    int closingTagLine = Utilities.getLineOffset(doc, closingTagOffset);
//
//                    if (closingTagLine == Utilities.getLineOffset(doc, dotPos)) {
//
//                        if (openingTagLine == closingTagLine - 1) {
//                            /* "smart enter"
//                             * <t>|optional text</t>
//                             */
//                            Position closingTagPos = doc.createPosition(getOpeningSymbolOffset(tokenHierarchy, closingTagOffset));
//                            changeRowIndent(doc, dotPos, initialIndent + doc.getShiftWidth());
//                            doc.insertString(closingTagPos.getOffset(), "\n", null); //NOI18N
//                            int newCaretPos = closingTagPos.getOffset() - 1;
//                            changeRowIndent(doc, closingTagPos.getOffset() + 1, initialIndent);
//                            newCaretPos = Utilities.getRowEnd(doc, newCaretPos);
//                            txtComponent.setCaretPosition(newCaretPos);
//                        } else {
//                            /*  <t>
//                             *
//                             *  |</t>
//                             */
//                            changeRowIndent(doc, dotPos, initialIndent);
//                        }
//                    }
//                }
//
//                int indent = initialIndent;
//
//                if (isClosingTagRequired(doc, extractTagName(tokenHierarchy, openingTagOffset))) {
//                    indent += doc.getShiftWidth();
//                }
//
//                changeRowIndent(doc, dotPos, indent);
//            }
//        } else {
//            int indent = initialIndent;
//
//            if (isJustBeforeClosingTag(tokenHierarchy, dotPos)) {
//                indent -= doc.getShiftWidth();
//                indent = indent < 0 ? 0 : indent;
//            }
//
//            // preceeding token is not opening tag, keep same indentation
//            changeRowIndent(doc, dotPos, indent);
//        }
//    }
//
//    @Override
//    public int[] getReformatBlock(JTextComponent target, String typedText) {
//        TokenHierarchy tokenHierarchy = TokenHierarchy.get(target.getDocument());
//        if (tokenHierarchy == null) {
//            logger.severe("Could not retrieve TokenHierarchy for document " + target.getDocument());
//            return null;
//        }
//        char lastChar = typedText.charAt(typedText.length() - 1);
//
//        try {
//            int dotPos = target.getCaret().getDot();
//
//            if (lastChar == '>') {
//                int precedingTokenOffset = getTagEndingAtPosition(tokenHierarchy, dotPos - 1);
//
//                if (isClosingTag(tokenHierarchy, precedingTokenOffset)) {
//                    // the user has just entered a closing tag
//                    // - reformat it unless matching opening tag is on the same line
//                    int openingTagOffset = getMatchingOpeningTagStart(tokenHierarchy, precedingTokenOffset);
//
//                    if (openingTagOffset != -1) {
//                        BaseDocument doc = Utilities.getDocument(target);
//                        int openingTagLine = Utilities.getLineOffset(doc, openingTagOffset);
//                        int closingTagSymbolLine = Utilities.getLineOffset(doc, dotPos);
//
//                        if (openingTagLine != closingTagSymbolLine) {
//                            return new int[]{precedingTokenOffset, dotPos};
//                        }
//                    }
//                }
//            } else if (lastChar == '\n') {
//                // just pressed enter
//                enterPressed(target, dotPos);
//            }
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Exception during code formatting", e); //NOI18N
//        }
//
//        return null;
//    }

//TODO: replace TextBounds with some generic class
    protected static class TextBounds {

        private int absoluteStart; // start offset regardless of white spaces
        private int absoluteEnd; // end --
        private int startPos = -1;
        private int endPos = -1;
        private int startLine = -1;
        private int endLine = -1;

        public TextBounds(int absoluteStart, int absoluteEnd) {
            this.absoluteStart = absoluteStart;
            this.absoluteEnd = absoluteEnd;
        }
        
        public TextBounds(int absoluteStart, int absoluteEnd, int startPos, int endPos, int startLine, int endLine) {
            this.absoluteStart = absoluteStart;
            this.absoluteEnd = absoluteEnd;
            this.startPos = startPos;
            this.endPos = endPos;
            this.startLine = startLine;
            this.endLine = endLine;
        }        

        public int getEndPos() {
            return endPos;
        }

        public int getStartPos() {
            return startPos;
        }

        public int getEndLine() {
            return endLine;
        }

        public int getStartLine() {
            return startLine;
        }
        
        public int getAbsoluteEnd() {
            return absoluteEnd;
        }

        public int getAbsoluteStart() {
            return absoluteStart;
        }

        @Override
        public String toString() {
            return "pos " + startPos + "-" + endPos + ", lines " + startLine + "-" + endLine; //NOI18N
        }
    }

    protected class InitialIndentData {

        private final int indentLevelBias;
        private final int indentBias;
        private final int[] indentLevels;
        private final int[] indentsWithinTags;
        private BaseDocument doc;

        public InitialIndentData(BaseDocument doc, int[] indentLevels, int[] indentsWithinTags, int firstRefBlockLine, int lastRefBlockLine) throws BadLocationException {

            int initialIndent = getInitialIndentFromPreviousLine(doc, firstRefBlockLine);
            int indentLevelBiasFromTheTop = initialIndent / doc.getShiftWidth() - (firstRefBlockLine > 0 ? indentLevels[firstRefBlockLine - 1] : 0);

            int initialIndentFromTheBottom = getInitialIndentFromNextLine(doc, lastRefBlockLine);
            int indentLevelBiasFromTheBottom = initialIndentFromTheBottom / doc.getShiftWidth() - (lastRefBlockLine < getNumberOfLines(doc) - 1 ? indentLevels[lastRefBlockLine + 1] : 0);

            if (indentLevelBiasFromTheBottom > indentLevelBiasFromTheTop) {
                indentLevelBias = indentLevelBiasFromTheBottom;
                initialIndent = initialIndentFromTheBottom;
            } else {
                indentLevelBias = indentLevelBiasFromTheTop;
            }

            indentBias = initialIndent % doc.getShiftWidth();
            this.indentLevels = indentLevels;
            this.indentsWithinTags = indentsWithinTags;
            this.doc = doc;
        }

        public boolean isEligibleToIndent(int line) {
            return getActualIndentLevel(line) >= 0;
        }

        public int getIndent(int line) {
            return indentBias + indentsWithinTags[line] + getActualIndentLevel(line) * doc.getShiftWidth();
        }

        private int getActualIndentLevel(int line) {
            return indentLevels[line] + indentLevelBias;
        }
    }

    protected static class TagIndentationData {

        private final String tagName;
        private final int line;
        private int closedOnLine;

        public TagIndentationData(String tagName, int line) {
            this.tagName = tagName;
            this.line = line;
        }

        public String getTagName() {
            return tagName;
        }

        public int getLine() {
            return line;
        }

        public int getClosedOnLine() {
            return closedOnLine;
        }

        public void setClosedOnLine(int closedOnLine) {
            this.closedOnLine = closedOnLine;
        }
    }

// TODO replace this class with a generic one, provided by Lexer API, when implemented
    public static class JoinedTokenSequence {

        private TokenSequence[] tokenSequences;
        private TextBounds[] tokenSequenceBounds;
        private int currentTokenSequence = -1;

        public JoinedTokenSequence(TokenSequence[] tokenSequences, TextBounds[] tokenSequenceBounds) {
            this.tokenSequences = tokenSequences;
            this.tokenSequenceBounds = tokenSequenceBounds;
        }

        public Token token() {
            return currentTokenSequence().token();
        }

        public TokenSequence currentTokenSequence() {
            return tokenSequences[currentTokenSequence];
        }

        public void moveStart() {
            currentTokenSequence = 0;
            currentTokenSequence().moveStart();
        }

        public boolean moveNext() {
            boolean moreTokens = currentTokenSequence().moveNext();

            if (!moreTokens) {
                if (currentTokenSequence + 1 < tokenSequences.length) {
                    currentTokenSequence++;
                    moveNext();
                } else {
                    return false;
                }
            }

            return true;
        }

        public boolean movePrevious() {
            boolean moreTokens = currentTokenSequence().movePrevious();

            if (!moreTokens) {
                if (currentTokenSequence > 0) {
                    currentTokenSequence--;
                    movePrevious();
                } else {
                    return false;
                }
            }

            return true;
        }

        public int move(int offset) {
            for (int i = 0; i < tokenSequences.length; i++) {
                if (tokenSequenceBounds[i].getAbsoluteStart() <= offset && tokenSequenceBounds[i].getAbsoluteEnd() > offset) {

                    currentTokenSequence = i;
                    return currentTokenSequence().move(offset);
                }
            }

            return -1;
        }

        public int offset() {
            return currentTokenSequence().offset();
        }
    }
}