/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.structure.formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;

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
public abstract class TagBasedLexerFormatter {

    private static final Logger logger = Logger.getLogger(TagBasedLexerFormatter.class.getName());

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

    public void process(Context context) throws BadLocationException{
        if (context.isIndent()){
            enterPressed(context);
        } else {
            reformat(context);
        }
    }
    
    public void reformat(Context context) throws BadLocationException{
        reformat(context, context.startOffset(), context.endOffset());
    }

    public void reformat(Context context, int startOffset, int endOffset) throws BadLocationException {
        LinkedList<TagIndentationData> unprocessedOpeningTags = new LinkedList<TagIndentationData>();
        List<TagIndentationData> matchedOpeningTags = new ArrayList<TagIndentationData>();
        BaseDocument doc = (BaseDocument) context.document();
        doc.atomicLock();
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);

        if (tokenHierarchy == null) {
            logger.severe("Could not retrieve TokenHierarchy for document " + doc);
            return;
        }

        try {
            TransferData transferData = null;
            
            if (isTopLevelLanguage(doc)){
                transferData = new TransferData();
                transferData.init(doc);
            } else {
                transferData = TransferData.readFromDocument(doc);
                assert transferData != null;
            }
            
            int firstRefBlockLine = Utilities.getLineOffset(doc, startOffset);
            int lastRefBlockLine = Utilities.getLineOffset(doc, endOffset);
            int firstUnformattableLine = -1;

            boolean currentLanguage[] = new boolean[transferData.getNumberOfLines()];
            int[] indentsWithinTags = new int[transferData.getNumberOfLines()];

            TokenSequence[] tokenSequences = (TokenSequence[]) tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
            TextBounds[] tokenSequenceBounds = new TextBounds[tokenSequences.length];

            for (int i = 0; i < tokenSequenceBounds.length; i++) {
                tokenSequenceBounds[i] = findTokenSequenceBounds(doc, tokenSequences[i]);

                if (tokenSequenceBounds[i].getStartLine() > -1) {
                    // skip white-space blocks
                    markCurrentLanguageLinesAsFormattable(doc, tokenSequenceBounds[i], currentLanguage);
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
                            calcIndents_processClosingTag(tagName, tagLine, transferData, unprocessedOpeningTags, matchedOpeningTags);
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

                        int lastUnformattableLine = thereAreMoreTokens ? Utilities.getLineOffset(doc, tokenSequence.offset() - 1) : transferData.getNumberOfLines() - 1;

                        for (int i = firstUnformattableLine + 1; i < lastUnformattableLine; i++) {
                            transferData.setNonFormattable(i);
                        }

                        firstUnformattableLine = -1;
                    }
                    
                    // Mark blocks of embedded language for relative formatting
                    if (tokenSequence.embedded() != null) {
                        int firstLineOfEmbeddedBlock = Utilities.getLineOffset(doc, tokenSequence.offset());
                        int lastLineOfEmbeddedBlock = Utilities.getLineOffset(doc, tokenSequence.offset() + getTxtLengthWithoutWhitespaceSuffix(tokenSequence.token().text()));

                        if (Utilities.getFirstNonWhiteFwd(doc, Utilities.getRowStartFromLineOffset(doc, firstLineOfEmbeddedBlock)) < Utilities.getFirstNonWhiteFwd(doc, tokenSequence.offset())) {

                            firstLineOfEmbeddedBlock++;
                        }

                        for (int i = firstLineOfEmbeddedBlock; i <= lastLineOfEmbeddedBlock; i++) {
                            currentLanguage[i] = false;
                        }
                    }

                } while (thereAreMoreTokens);
            }
            
            // pass 2 - calculate block shifts for each block of different language code
            
            
            //****************
            // calc line indents - pass 3
            // TODO: optimize it
            int[] indentLevels = new int[transferData.getNumberOfLines()];
            Arrays.fill(indentLevels, 0);

            for (TagIndentationData td : matchedOpeningTags) {
                // increase indent from one line after the opening tag
                // up to one line before the closing tag
                for (int i = td.getLine() + 1; i <= td.getClosedOnLine() - 1; i++) {
                    indentLevels[i]++;
                }
            }
            
            int[] previousIndents = transferData.getTransformedOffsets();
            int[] absoluteIndents = new int[transferData.getNumberOfLines()];
            
            for (int i = 0; i < transferData.getNumberOfLines(); i++) {
                absoluteIndents[i] = indentLevels[i] * doc.getShiftWidth() + indentsWithinTags[i];
            }
            
            boolean lastLineIsCurrentLanguage = false;
            int lastCrossPoint = 0;
            int[] newIndents = new int[transferData.getNumberOfLines()];

            for (int i = 0; i < transferData.getNumberOfLines(); i++) {
                if (lastLineIsCurrentLanguage != currentLanguage[i]) {
                    // crosspoint
                    lastLineIsCurrentLanguage = currentLanguage[i];
                    lastCrossPoint = i;
                }

                if (!transferData.isFormattable(i)) {
                    newIndents[i] = transferData.getOriginalIndent(i);
                } else {
                    if (currentLanguage[i]) {
                        newIndents[i] = previousIndents[i] + absoluteIndents[i];
                    } else {
                        newIndents[i] = previousIndents[lastCrossPoint] + absoluteIndents[i];
                    }
                }
            }
            
            int lineBeforeSelectionBias = 0;
            
            if (firstRefBlockLine > 0){
                lineBeforeSelectionBias = transferData.getOriginalIndent(firstRefBlockLine - 1) - newIndents[firstRefBlockLine - 1];
            }
                        
            for (int line = firstRefBlockLine; line <= lastRefBlockLine; line++) {
                int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
                context.modifyIndent(lineStart, newIndents[line] + lineBeforeSelectionBias);
            }
            
            transferData.setTransformedOffsets(newIndents);

            if (logger.isLoggable(Level.FINE)) {
                StringBuilder buff = new StringBuilder();

                for (int i = 0; i < transferData.getNumberOfLines(); i++) {
                    int lineStart = Utilities.getRowStartFromLineOffset(doc, i);

                    char formattingTypeSymbol = 0;
                    
                    if (!transferData.isFormattable(i)){
                        formattingTypeSymbol = '-';
                    } else if (currentLanguage[i]){
                        formattingTypeSymbol = 'N';
                    } else {
                        formattingTypeSymbol = 'R';
                    }
                    
                    char formattingRange = (i >= firstRefBlockLine && i <= lastRefBlockLine) 
                            ? '*' : ' ';

                    buff.append(i + ":" + formattingRange + ":" + indentLevels[i] + ":" + formattingTypeSymbol + ":" + doc.getText(lineStart, Utilities.getRowEnd(doc, lineStart) - lineStart) + ".\n"); //NOI18N
                }

                buff.append("\n-------------\n"); //NOI18N
                logger.fine(getClass().getName() + ":\n" + buff);
            }

        } finally {
            doc.atomicUnlock();
        }
    }
    
    private static int getTxtLengthWithoutWhitespaceSuffix(CharSequence txt){
        for (int i = txt.length(); i > 0; i --){
            if (!Character.isWhitespace(txt.charAt(i - 1))){
                return i;
            }
        }
        
        return 0;
    }

    protected int getMatchingOpeningTagStart(JoinedTokenSequence tokenSequence, int closingTagOffset) {
        int originalOffset = tokenSequence.offset();
        tokenSequence.move(closingTagOffset);
        tokenSequence.moveNext();

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
        protected boolean isWSToken(Token token) {
        return isOnlyWhiteSpaces(token.text());
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
        }

        return thereAreMoreTokens;
    }

    private void calcIndents_processClosingTag(final String tagName, final int tagClosedOnLine, final TransferData transferData, final LinkedList<TagIndentationData> unprocessedOpeningTags, final Collection<TagIndentationData> matchedOpeningTags) throws BadLocationException {
        LinkedList<TagIndentationData> tagsToBeRemoved = new LinkedList<TagIndentationData>();

        while (!unprocessedOpeningTags.isEmpty()) {
            TagIndentationData processedTD = unprocessedOpeningTags.removeLast();

            if (areTagNamesEqual(tagName, processedTD.getTagName())) {
                processedTD.setClosedOnLine(tagClosedOnLine);
                matchedOpeningTags.add(processedTD);

                // mark all the stuff between unformattable tag as unformattable
                if (isUnformattableTag(tagName)) {
                    for (int i = tagClosedOnLine - 1; i > processedTD.getLine(); i--) {
                        transferData.setNonFormattable(i);
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

    protected static int getNumberOfLines(BaseDocument doc) throws BadLocationException {
        return Utilities.getLineOffset(doc, doc.getLength()) + 1;
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

    private void markCurrentLanguageLinesAsFormattable(BaseDocument doc, TextBounds languageBounds, boolean[] currentLanguage) throws BadLocationException {
        if (languageBounds.getStartPos() == -1){
            return; // only white spaces
        }
        
        int firstLineOfTheLanguageBlock = languageBounds.getStartLine();

        int lineStart = Utilities.getRowStartFromLineOffset(doc, firstLineOfTheLanguageBlock);

        if (Utilities.getFirstNonWhiteFwd(doc, lineStart) < languageBounds.getStartPos()) {
            firstLineOfTheLanguageBlock++;
        }

        for (int i = firstLineOfTheLanguageBlock; i <= languageBounds.getEndLine(); i++) {
            currentLanguage[i] = true;
        }
    }
    
    protected boolean isTopLevelLanguage(BaseDocument doc) {
        return supportedLanguagePath().size() == 1;
    }
    
    protected static int getExistingIndent(BaseDocument doc, int line) throws BadLocationException{
        int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
        int eol = Utilities.getRowEnd(doc, lineStart);
        int nextNonWS = Utilities.getFirstNonWhiteFwd(doc, lineStart);
        
        if (nextNonWS == -1){
            nextNonWS = Integer.MAX_VALUE;
        }
        
        return Math.min(eol - lineStart, nextNonWS - lineStart);
    }
    
    public static class TransferData{
        private static final String DOC_PROPERTY = "TagBasedFormatterData"; //NOI18N
        private boolean formattableLines[];
        private int originalIndents[];
        private int transformedOffsets[];
        private int numberOfLines;
        
        public void init(BaseDocument doc) throws BadLocationException {
            numberOfLines = TagBasedLexerFormatter.getNumberOfLines(doc);
            formattableLines = new boolean[numberOfLines];
            Arrays.fill(formattableLines, true);
            originalIndents = new int[numberOfLines];
            transformedOffsets = new int[numberOfLines];
            
            for (int i = 0; i < numberOfLines; i++) {
                originalIndents[i] = getExistingIndent(doc, i);
                //transformedOffsets[i] = originalIndents[i];
            }
            
            doc.putProperty(DOC_PROPERTY, this);
        }
        
        public static TransferData readFromDocument(BaseDocument doc){
            return (TransferData) doc.getProperty(DOC_PROPERTY);
        }
        
        public int getNumberOfLines(){
            return numberOfLines;
        }
        
        public boolean isFormattable(int line){
            return formattableLines[line];
        }
        
        public void setNonFormattable(int line){
            formattableLines[line] = false;
        }

        public int[] getTransformedOffsets() {
            return transformedOffsets;
        }
       
        public void setTransformedOffsets(int[] transformedOffsets) {
            this.transformedOffsets = transformedOffsets;
        }
        
        private int getOriginalIndent(int i) {
            return originalIndents[i];
        }
    }
    
    private static final String ORG_CARET_OFFSET_DOCPROPERTY = "TagBasedFormatter.org_caret_offset";
    
    public void enterPressed(Context context) {
        BaseDocument doc = (BaseDocument)context.document();
        
        doc.atomicLock();
        
        try {
            
            if (isTopLevelLanguage(doc)) {
                doc.putProperty(ORG_CARET_OFFSET_DOCPROPERTY, new Integer(context.caretOffset()));
            }

            Integer dotPos = (Integer) doc.getProperty(ORG_CARET_OFFSET_DOCPROPERTY);
            assert dotPos != null;
            
            if (indexWithinCurrentLanguage(doc, dotPos - 1)) {
                if (isSmartEnter(doc, dotPos)) {
                    handleSmartEnter(context);
                } else {
                    int newIndent = 0;
                    int lineNumber = Utilities.getLineOffset(doc, dotPos);
                    boolean firstRow = false;
                    
                    if (Utilities.getRowStart(doc, dotPos - 1) == dotPos - 1){
                        newIndent = getExistingIndent(doc, lineNumber);
                        firstRow = true;
                    } else if (lineNumber > 0){
                        newIndent = getExistingIndent(doc, lineNumber - 1);
                    }
                    
                    TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
                    TokenSequence[] tokenSequences = (TokenSequence[]) tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
                    TextBounds[] tokenSequenceBounds = new TextBounds[tokenSequences.length];
                    
                    for (int i = 0; i < tokenSequenceBounds.length; i++) {
                        tokenSequenceBounds[i] = findTokenSequenceBounds(doc, tokenSequences[i]);
                    }

                    JoinedTokenSequence tokenSequence = new JoinedTokenSequence(tokenSequences, tokenSequenceBounds);
                    tokenSequence.moveStart(); tokenSequence.moveNext();

                    int openingTagOffset = getTagEndingAtPosition(tokenSequence, dotPos - 2);
                    
                    if (isOpeningTag(tokenSequence, openingTagOffset)){
                        newIndent += doc.getShiftWidth();
                    }

                    context.modifyIndent(Utilities.getRowStart(doc, dotPos), newIndent);
                    
                    if (firstRow){
                        context.setCaretOffset(context.caretOffset() - newIndent);
                    }
                }
            }
        } catch (BadLocationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            doc.atomicUnlock();
        }
    }
    
    private boolean indexWithinCurrentLanguage(BaseDocument doc, int index) throws BadLocationException{
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence[] tokenSequences = (TokenSequence[]) tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
        
        for (TokenSequence tokenSequence: tokenSequences){
            TextBounds languageBounds = findTokenSequenceBounds(doc, tokenSequence);
            
            if (languageBounds.getAbsoluteStart() <= index && languageBounds.getAbsoluteEnd() >= index){
                tokenSequence.move(index);
                
                if (tokenSequence.moveNext()){
                    // the newly entered \n character may or may not
                    // form a separate token - work it around
                    if (isWSToken(tokenSequence.token())){
                        tokenSequence.movePrevious(); 
                    }
                    
                    return tokenSequence.embedded() == null && !isWSToken(tokenSequence.token());
                }
            }
        }
        
        return false;
    }
    
    public boolean handleSmartEnter(Context context) throws BadLocationException {
        boolean wasSmartEnter = false;
        BaseDocument doc = (BaseDocument)context.document();
        int dotPos = context.caretOffset();

        wasSmartEnter = isSmartEnter(doc, dotPos);

        if (wasSmartEnter) {
            int line = Utilities.getLineOffset(doc, dotPos);
            assert line > 0;
            int baseIndent = getExistingIndent(doc, line - 1);
            doc.insertString(dotPos, "\n", null); //NOI18N
            Position position = doc.createPosition(dotPos);
            context.modifyIndent(Utilities.getRowStartFromLineOffset(doc, line), baseIndent + doc.getShiftWidth());
            context.modifyIndent(Utilities.getRowStartFromLineOffset(doc, line + 1), baseIndent);
            context.setCaretOffset(position.getOffset());
        }

        return wasSmartEnter;
    }
    
    public boolean isSmartEnter(BaseDocument doc, int dotPos) {
        
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence[] tokenSequences = (TokenSequence[]) tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
        TextBounds[] tokenSequenceBounds = new TextBounds[tokenSequences.length];
        try {

            for (int i = 0; i < tokenSequenceBounds.length; i++) {
                tokenSequenceBounds[i] = findTokenSequenceBounds(doc, tokenSequences[i]);
            }
            
            JoinedTokenSequence tokenSequence = new JoinedTokenSequence(tokenSequences, tokenSequenceBounds);
            
            if (tokenSequence.move(dotPos) != Integer.MIN_VALUE) { // ignore if dotPos not within current language
                tokenSequence.moveNext();

                if (isJustBeforeClosingTag(tokenSequence, dotPos)) {
                    int closingTagOffset = getNextClosingTagOffset(tokenSequence, dotPos);
                    int matchingOpeningTagOffset = getMatchingOpeningTagStart(tokenSequence, closingTagOffset);
                    int openingTagEnd = getTagEndOffset(tokenSequence, matchingOpeningTagOffset);

                    return openingTagEnd + 1 == dotPos;
                }
            }
            
        } catch (BadLocationException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        

        return false;
    }
    
    protected boolean isOnlyWhiteSpaces(CharSequence txt){
        for (int i = 0; i < txt.length(); i ++){
            if (!Character.isWhitespace(txt.charAt(i))){
                return false;
            }
        }
        
        return true;
    }

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

            return Integer.MIN_VALUE;
        }

        public int offset() {
            return currentTokenSequence().offset();
        }

        private TokenSequence embedded() {
            return currentTokenSequence().embedded();
        }
    }
}
