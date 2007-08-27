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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtFormatter;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class TagBasedLexerFormatter extends ExtFormatter  {
    private static final Logger logger = Logger.getLogger(TagBasedLexerFormatter.class.getName());
            
    /** Creates a new instance of TagBases */
    public TagBasedLexerFormatter(Class kitClass) {
        super(kitClass);
    }
    
    protected abstract boolean isClosingTag(TokenHierarchy tokenHierarchy, int tagTokenOffset);
    protected abstract boolean isUnformattableToken(TokenHierarchy tokenHierarchy, int tagTokenOffset);
    protected abstract boolean isUnformattableTag(String tag);
    protected abstract boolean isOpeningTag(TokenHierarchy tokenHierarchy, int tagTokenOffset);
    protected abstract String extractTagName(TokenHierarchy tokenHierarchy, int tokenOffset);
    protected abstract boolean areTagNamesEqual(String tagName1, String tagName2);
    protected abstract boolean isClosingTagRequired(BaseDocument doc, String tagName);
    protected abstract int getOpeningSymbolOffset(TokenHierarchy tokenHierarchy, int tagTokenOffset);
    protected abstract int getTagEndingAtPosition(TokenHierarchy tokenHierarchy, int position) throws BadLocationException;
    protected abstract int getTagEndOffset(TokenHierarchy tokenHierarchy, int tagStartOffset);
    
    protected boolean isWSTag(Token tag){
        char chars[] = tag.text().toString().toCharArray();
        
        for (char c : chars){
            if (!Character.isWhitespace(c)){
                return false;
            }
        }
        
        return true;
    }
    
    protected int getIndentForTagParameter(TokenHierarchy<?> tokenHierarchy, int tagOffset) throws BadLocationException{
        BaseDocument doc = (BaseDocument) tokenHierarchy.mutableInputSource();
        int tagStartLine = Utilities.getLineOffset(doc, tagOffset);
        TokenSequence<? extends TokenId> tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.move(tagOffset);
        Token<? extends TokenId> token;
        int tokenOffset;
        
        /*
         * Find the offset of the first attribute if it is specified on the same line as the opening of the tag
         * e.g. <tag   |attr=
         * 
         */
        while (tokenSequence.moveNext()) {
            token = tokenSequence.token();
            tokenOffset = tokenSequence.offset();
            if (!isWSTag(token) || tagStartLine != Utilities.getLineOffset(doc, tokenOffset)) {
                if (!isWSTag(token) && tagStartLine == Utilities.getLineOffset(doc, tokenOffset)){
                    return tokenOffset - Utilities.getRowIndent(doc, tokenOffset) - Utilities.getRowStart(doc, tokenOffset);
                }
                break;
            }
        }
        
        
        return doc.getShiftWidth(); // default;
    }
    
    @Override public Writer reformat(BaseDocument doc, int startOffset, int endOffset,
            boolean indentOnly) throws BadLocationException {
        LinkedList<TagIndentationData>unprocessedOpeningTags = new LinkedList<TagIndentationData>();
        List<TagIndentationData>matchedOpeningTags = new ArrayList<TagIndentationData>();
        doc.atomicLock();
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        
        if (tokenHierarchy == null){
            logger.severe("Could not retrieve TokenHierarchy for document " + doc);
            return null;
        }
        
        try{
            int lastLine = Utilities.getLineOffset(doc, doc.getLength());
            int firstRefBlockLine = Utilities.getLineOffset(doc, startOffset);
            int lastRefBlockLine = Utilities.getLineOffset(doc, endOffset);
            int firstUnformattableLine = -1;
            
            boolean unformattableLines[] = new boolean[lastLine + 1];
            int indentsWithinTags[] = new int[lastLine + 1];
            
            TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
            tokenSequence.moveStart();
            boolean thereAreMoreTokens = tokenSequence.moveNext();
            
            
            if (tokenSequence != null){
                // calc line indents - pass 1
                do{
                    boolean isOpenTag = isOpeningTag(tokenHierarchy, tokenSequence.offset());
                    boolean isCloseTag = isClosingTag(tokenHierarchy, tokenSequence.offset());
                    
                    if (isOpenTag || isCloseTag){
                        
                        String tagName = extractTagName(tokenHierarchy, tokenSequence.offset());
                        int tagEndOffset = getTagEndOffset(tokenHierarchy, tokenSequence.offset());
                        int lastTagLine = Utilities.getLineOffset(doc, tagEndOffset);
                        
                        if (isOpenTag){
                            
                            TagIndentationData tagData = new TagIndentationData(tagName, lastTagLine);
                            unprocessedOpeningTags.add(tagData);
                            
                            // format content of a tag that spans across multiple lines
                            int firstTagLine = Utilities.getLineOffset(doc, tokenSequence.offset());
                            
                            if (firstTagLine < lastTagLine){ // performance!
                                int indentWithinTag = getIndentForTagParameter(tokenHierarchy, tokenSequence.offset());
                                
                                for (int i = firstTagLine + 1; i <= lastTagLine; i ++){
                                    indentsWithinTags[i] = indentWithinTag;
                                }
                                
                                // if there is only the closing symbol on the last line of tag do not indent it
                                thereAreMoreTokens &= tokenSequence.moveNext();
                                
                                while (Utilities.getLineOffset(doc, tokenSequence.offset()) < lastTagLine
                                        || isWSTag(tokenSequence.token())){
                                    
                                    tokenSequence.moveNext();
                                }
                                
                                if (tokenSequence.offset() == tagEndOffset){
                                    indentsWithinTags[lastTagLine] = 0;
                                }
                            }
                        } else {
                            // isCloseTag - find matching opening tag record
                            LinkedList<TagIndentationData>tagsToBeRemoved = new LinkedList<TagIndentationData>();
                            
                            while (!unprocessedOpeningTags.isEmpty()){
                                TagIndentationData processedTD = unprocessedOpeningTags.removeLast();
                                
                                if (areTagNamesEqual(tagName, processedTD.getTagName())){
                                    processedTD.setClosedOnLine(lastTagLine);
                                    matchedOpeningTags.add(processedTD);
                                    
                                    // mark all the stuff between unformattable tag as unformattable
                                    if (isUnformattableTag(tagName)){
                                        for (int i = lastTagLine - 1; i > processedTD.getLine(); i --){
                                            unformattableLines[i] = true;
                                        }
                                    }
                                    
                                    // forgetting preceding tags permanently
                                    tagsToBeRemoved.clear();
                                    break;
                                } else{
                                    tagsToBeRemoved.add(processedTD);
                                }
                            }
                            
                            // if matching opening tag was not found on the stack put all the tags back
                            unprocessedOpeningTags.addAll(tagsToBeRemoved);
                        }
                    }
                    
                    boolean wasPreviousTokenUnformattable = isUnformattableToken(tokenHierarchy, tokenSequence.offset());
                    
                    if (wasPreviousTokenUnformattable && firstUnformattableLine == -1){
                        firstUnformattableLine = Utilities.getLineOffset(doc, tokenSequence.offset());
                    }
                    
                    thereAreMoreTokens &= tokenSequence.moveNext();
                    
                    // detect an end of unformattable block; mark it
                    if (firstUnformattableLine > -1
                            && (!wasPreviousTokenUnformattable || !thereAreMoreTokens)){
                        
                        int lastUnformattableLine = thereAreMoreTokens ? 
                            Utilities.getLineOffset(doc, tokenSequence.offset() - 1) : lastLine;
                        
                        for (int i = firstUnformattableLine + 1; i < lastUnformattableLine; i ++){
                            unformattableLines[i] = true;
                        }
                        
                        firstUnformattableLine = -1;
                    }
                }
                while (thereAreMoreTokens);
            }
            
            // calc line indents - pass 2
            // TODO: optimize it
            int indentLevels[] = new int[lastLine + 1];
            Arrays.fill(indentLevels, 0);
            
            for (TagIndentationData td : matchedOpeningTags){
                // increase indent from one line after the opening tag
                // up to one line before the closing tag
                
                for (int i = td.getLine() + 1; i <= td.getClosedOnLine() - 1; i ++){
                    indentLevels[i] ++;
                }
            }
            
            // when reformatting only a part of file
            // we need to take into account the local bias
            InitialIndentData initialIndentData = new InitialIndentData(doc, indentLevels,
                    indentsWithinTags, firstRefBlockLine, lastRefBlockLine);
            
            // apply line indents
            for (int line = firstRefBlockLine; line <= lastRefBlockLine; line ++){
                int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
                
                if (!unformattableLines[line] && initialIndentData.isEligibleToIndent(line)){
                    changeRowIndent(doc, lineStart, initialIndentData.getIndent(line));
                }
            }
        } finally{
            doc.atomicUnlock();
        }
        
        return null;
    }
    
    protected void enterPressed(JTextComponent txtComponent, int dotPos) throws BadLocationException {
        BaseDocument doc = Utilities.getDocument(txtComponent);
        int lineNumber = Utilities.getLineOffset(doc, dotPos);
        int initialIndent = getInitialIndentFromPreviousLine(doc, lineNumber);
        int endOfPreviousLine = Utilities.getFirstNonWhiteBwd(doc, dotPos);
        endOfPreviousLine = endOfPreviousLine == -1 ? 0 : endOfPreviousLine;
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        
        // workaround for \n passed from code completion to reformatter
        if (lineNumber == Utilities.getLineOffset(doc, endOfPreviousLine)){
            return;
        }
        
        int openingTagOffset = getTagEndingAtPosition(tokenHierarchy, endOfPreviousLine);
        
        if (isOpeningTag(tokenHierarchy, openingTagOffset)){
            int closingTagOffset = getNextClosingTagOffset(tokenHierarchy, dotPos + 1);
            
            if (closingTagOffset != -1){
                int matchingOpeningTagOffset = getMatchingOpeningTagStart(tokenHierarchy, closingTagOffset);
                
                if (openingTagOffset == matchingOpeningTagOffset){
                    
                    int openingTagLine = Utilities.getLineOffset(doc, openingTagOffset);
                    int closingTagLine = Utilities.getLineOffset(doc, closingTagOffset);
                    
                    if (closingTagLine == Utilities.getLineOffset(doc, dotPos)){
                        
                        if (openingTagLine == closingTagLine - 1){
                            /* "smart enter"
                             * <t>|optional text</t>
                             */
                            Position closingTagPos = doc.createPosition(getOpeningSymbolOffset(tokenHierarchy, closingTagOffset));
                            changeRowIndent(doc, dotPos, initialIndent + doc.getShiftWidth());
                            doc.insertString(closingTagPos.getOffset(), "\n", null); //NOI18N
                            int newCaretPos = closingTagPos.getOffset() - 1;
                            changeRowIndent(doc, closingTagPos.getOffset() + 1, initialIndent);
                            newCaretPos = Utilities.getRowEnd(doc, newCaretPos);
                            txtComponent.setCaretPosition(newCaretPos);
                        } else{
                            /*  <t>
                             *
                             *  |</t>
                             */
                            changeRowIndent(doc, dotPos, initialIndent);
                        }
                    }
                }
                
                int indent = initialIndent;
                
                if (isClosingTagRequired(doc, extractTagName(tokenHierarchy, openingTagOffset))){
                    indent += doc.getShiftWidth();
                }
                
                changeRowIndent(doc, dotPos, indent);
            }
        } else{
            int indent = initialIndent;
            
            if (isJustBeforeClosingTag(tokenHierarchy, dotPos)){
                indent -= doc.getShiftWidth();
                indent = indent < 0 ? 0 : indent;
            }
            
            // preceeding token is not opening tag, keep same indentation
            changeRowIndent(doc, dotPos, indent);
        }
    }
    
    @Override public int[] getReformatBlock(JTextComponent target, String typedText) {
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(target.getDocument());
        if (tokenHierarchy == null){
            logger.severe("Could not retrieve TokenHierarchy for document " + target.getDocument());
            return null;
        }
        char lastChar = typedText.charAt(typedText.length() - 1);
        
        try{
            int dotPos = target.getCaret().getDot();
            
            if (lastChar == '>') {
                int precedingTokenOffset = getTagEndingAtPosition(tokenHierarchy, dotPos - 1);
                
                if (isClosingTag(tokenHierarchy, precedingTokenOffset)){
                    // the user has just entered a closing tag
                    // - reformat it unless matching opening tag is on the same line 
                    
                    int openingTagOffset = getMatchingOpeningTagStart(tokenHierarchy, precedingTokenOffset);
                    
                    if (openingTagOffset != -1){
                        BaseDocument doc = Utilities.getDocument(target);
                        int openingTagLine = Utilities.getLineOffset(doc, openingTagOffset);
                        int closingTagSymbolLine = Utilities.getLineOffset(doc, dotPos);
                        
                        if(openingTagLine != closingTagSymbolLine){
                            return new int[]{precedingTokenOffset, dotPos};
                        }
                    }
                }
            }
            
            else if(lastChar == '\n') {
                // just pressed enter
                enterPressed(target, dotPos);
            }
            
        } catch (Exception e){
            logger.log(Level.SEVERE, "Exception during code formatting", e); //NOI18N
        }
        
        return null;
    }
    
    protected int getMatchingOpeningTagStart(TokenHierarchy tokenHierarchy, int closingTagOffset){
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.move(closingTagOffset);
        
        String searchedTagName = extractTagName(tokenHierarchy, closingTagOffset);
        int balance = 0;
        
        while (tokenSequence.movePrevious()){
            int currentTokenOffset = tokenSequence.offset();
            if (areTagNamesEqual(searchedTagName, extractTagName(tokenHierarchy, currentTokenOffset))){
                if (isOpeningTag(tokenHierarchy, currentTokenOffset)){
                    if (balance == 0){
                        return currentTokenOffset;
                    }
                    
                    balance --;
                } else if (isClosingTag(tokenHierarchy, currentTokenOffset)){
                    balance ++;
                }
            }
        }
        
        return -1;
    }
    
    protected int getInitialIndentFromPreviousLine(final BaseDocument doc, final int line) throws BadLocationException {
        
        // get initial indent from the previous line
        int initialIndent = 0;
        
        if (line > 0){
            int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
            int previousNonWhiteLineEnd = Utilities.getFirstNonWhiteBwd(doc, lineStart);
            
            if (previousNonWhiteLineEnd > 0){
                initialIndent = Utilities.getRowIndent(doc, previousNonWhiteLineEnd);
            }
        }
        
        return initialIndent;
    }
    
    private int getInitialIndentFromNextLine(final BaseDocument doc, final int line) throws BadLocationException {
        
        // get initial indent from the next line
        int initialIndent = 0;
        
        int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
        int lineEnd = Utilities.getRowEnd(doc, lineStart);
        int nextNonWhiteLineStart = Utilities.getFirstNonWhiteFwd(doc, lineEnd);
        
        if (nextNonWhiteLineStart > 0){
            initialIndent = Utilities.getRowIndent(doc, nextNonWhiteLineStart, true);
        }
        
        return initialIndent;
    }

    protected static int getNumberOfLines(BaseDocument doc) throws BadLocationException{
        return Utilities.getLineOffset(doc, doc.getLength() - 1) + 1;
    }
    
    protected int getNextClosingTagOffset(TokenHierarchy tokenHierarchy, int offset) throws BadLocationException{
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.move(offset);
        int currentOffset;
        
        while (tokenSequence.moveNext()) {
            currentOffset = tokenSequence.offset();
            
            if (isClosingTag(tokenHierarchy, currentOffset)){
                return currentOffset;
            }
            
        }

        return -1;
    }

    protected boolean isJustBeforeClosingTag(TokenHierarchy tokenHierarchy, int pos) throws BadLocationException {
        // default, trivial implementation
        if (isClosingTag(tokenHierarchy, pos)){
            return true;
        }
        
        return false;
    }
    
    protected Token getTokenAtOffset(TokenHierarchy tokenHierarchy, int tagTokenOffset){
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        
        if (tokenSequence != null) {
            tokenSequence.move(tagTokenOffset);
            if (tokenSequence.moveNext())
                return tokenSequence.token();
        }
        
        return null;
    }
    
    protected class InitialIndentData{
        private final int indentLevelBias;
        private final int indentBias;
        private final int indentLevels[];
        private final int indentsWithinTags[];
        private BaseDocument doc;
        
        public InitialIndentData(BaseDocument doc, int indentLevels[], int indentsWithinTags[],
                int firstRefBlockLine, int lastRefBlockLine) throws BadLocationException{
            
            int initialIndent = getInitialIndentFromPreviousLine(doc, firstRefBlockLine);
            int indentLevelBiasFromTheTop = initialIndent / doc.getShiftWidth() - (firstRefBlockLine > 0 ? indentLevels[firstRefBlockLine - 1] : 0);
            
            int initialIndentFromTheBottom = getInitialIndentFromNextLine(doc, lastRefBlockLine);
            int indentLevelBiasFromTheBottom = initialIndentFromTheBottom / doc.getShiftWidth() - (lastRefBlockLine < getNumberOfLines(doc) - 1 ? indentLevels[lastRefBlockLine + 1] : 0);
            
            if (indentLevelBiasFromTheBottom > indentLevelBiasFromTheTop){
                indentLevelBias = indentLevelBiasFromTheBottom;
                initialIndent = initialIndentFromTheBottom;
            }
            else{
                indentLevelBias = indentLevelBiasFromTheTop;
            }
            
            indentBias = initialIndent % doc.getShiftWidth();
            this.indentLevels = indentLevels;
            this.indentsWithinTags = indentsWithinTags;
            this.doc = doc;
        }
        
        public boolean isEligibleToIndent(int line){
            return getActualIndentLevel(line) >= 0;
        }
        
        public int getIndent(int line){
            return indentBias + indentsWithinTags[line] + getActualIndentLevel(line) * doc.getShiftWidth();
        }
        
        private int getActualIndentLevel(int line){
            return indentLevels[line] + indentLevelBias;
        }
    }
    
    protected static class TagIndentationData{
        private final String tagName;
        private final int line;
        private int closedOnLine;
        
        public TagIndentationData(String tagName, int line){
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
}
