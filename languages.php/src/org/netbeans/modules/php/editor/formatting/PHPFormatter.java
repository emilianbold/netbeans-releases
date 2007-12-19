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

package org.netbeans.modules.php.editor.formatting;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence;
import org.netbeans.modules.editor.structure.formatting.TextBounds;
import org.netbeans.modules.editor.structure.formatting.TransferData;
import org.netbeans.modules.php.editor.TokenUtils;
import org.netbeans.modules.php.lexer.PhpTokenId;
import org.netbeans.modules.php.model.Block;
import org.netbeans.modules.php.model.ClassBody;
import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.ClassStatement;
import org.netbeans.modules.php.model.Else;
import org.netbeans.modules.php.model.ElseIf;
import org.netbeans.modules.php.model.Expression;
import org.netbeans.modules.php.model.ExpressionStatement;
import org.netbeans.modules.php.model.For;
import org.netbeans.modules.php.model.ForEach;
import org.netbeans.modules.php.model.ForEachStatement;
import org.netbeans.modules.php.model.ForStatement;
import org.netbeans.modules.php.model.If;
import org.netbeans.modules.php.model.IfStatement;
import org.netbeans.modules.php.model.ModelAccess;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.Statement;
import org.netbeans.modules.php.model.StatementContainer;
import org.netbeans.modules.php.model.While;
import org.netbeans.modules.php.model.WhileStatement;
/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
//TODO: Remove code duplication with the TagBasedLexerFormatter class
public class PHPFormatter {    
    private static final Logger logger = Logger.getLogger(PHPFormatter.class.getName());

    private LanguagePath supportedLanguagePath(){
        LanguagePath phpLanguagePath = LanguagePath.get(Language.find("text/x-php5"));
        return LanguagePath.get(phpLanguagePath, Language.find("text/x-pure-php5"));
    }
    
    public enum EmbeddingType {
        /**
         * The line belongs to the language being currently formatted
         */
        CURRENT_LANG,
        /**
         * The line belongs to a language embedded <em>inside</em> currently formatted one
         */
        INNER,
        /**
         * The line belongs to a languge in which currently formatted languaged is embedded
         */
        OUTER}

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
        BaseDocument doc = (BaseDocument) context.document();
        doc.atomicLock();
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);

        if (tokenHierarchy == null) {
            logger.severe("Could not retrieve TokenHierarchy for document " + doc);
            return;
        }
        
        try {
            TransferData transferData = null;
      
            // read data stored by previously called compatible formatters
            transferData = TransferData.readFromDocument(doc);
            assert transferData != null;

            
            // PASS 1: Calculate EmbeddingType and AbsoluteIndentLevel 
            // (determined by the tags of the current language) for each line
            
            int firstRefBlockLine = Utilities.getLineOffset(doc, startOffset);
            int lastRefBlockLine = Utilities.getLineOffset(doc, endOffset);

            EmbeddingType embeddingType[] = new EmbeddingType[transferData.getNumberOfLines()];
            Arrays.fill(embeddingType, EmbeddingType.OUTER);

            TokenSequence[] tokenSequences = (TokenSequence[]) tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
            TextBounds[] tokenSequenceBounds = new TextBounds[tokenSequences.length];

            for (int i = 0; i < tokenSequenceBounds.length; i++) {
                tokenSequenceBounds[i] = findTokenSequenceBounds(doc, tokenSequences[i]);
                if (tokenSequenceBounds[i].getStartLine() > -1) {
                    // skip white-space blocks
                    markCurrentLanguageLines(doc, tokenSequenceBounds[i], embeddingType);
                }
            }

            if (tokenSequences.length > 0) {
                JoinedTokenSequence tokenSequence = new JoinedTokenSequence(tokenSequences, tokenSequenceBounds);
                tokenSequence.moveStart();
                boolean thereAreMoreTokens = tokenSequence.moveNext();
                
                do {
                    // Mark blocks of embedded language
                    if (tokenSequence.embedded() != null) {
                        int firstLineOfEmbeddedBlock = Utilities.getLineOffset(doc, tokenSequence.offset());
                        int lastLineOfEmbeddedBlock = Utilities.getLineOffset(doc, tokenSequence.offset() + getTxtLengthWithoutWhitespaceSuffix(tokenSequence.token().text()));

                        if (Utilities.getFirstNonWhiteFwd(doc, Utilities.getRowStartFromLineOffset(doc, firstLineOfEmbeddedBlock)) < Utilities.getFirstNonWhiteFwd(doc, tokenSequence.offset())) {

                            firstLineOfEmbeddedBlock++;
                        }

                        for (int i = firstLineOfEmbeddedBlock; i <= lastLineOfEmbeddedBlock; i++) {
                            embeddingType[i] = EmbeddingType.INNER;
                        }
                    }
                    
                    thereAreMoreTokens &= tokenSequence.moveNext();

                } while (thereAreMoreTokens);
            }
            
            // PASS 2: handle formatting order for languages on the same level of mime-hierarchy
            
            for (int line = 0; line < transferData.getNumberOfLines(); line ++){
                if (embeddingType[line] == EmbeddingType.CURRENT_LANG){
                    transferData.setProcessedByNativeFormatter(line);
                } else if (embeddingType[line] == EmbeddingType.OUTER){
                    // play master
                    if (!transferData.wasProcessedByNativeFormatter(line)){
                        embeddingType[line] = EmbeddingType.INNER; 
                    }
                }
            }
            
            int[] previousIndents = transferData.getTransformedOffsets();
            int[] absoluteIndents = new int[transferData.getNumberOfLines()];
            
            for (int line = 0; line < transferData.getNumberOfLines(); line ++){
                if (embeddingType[line] == EmbeddingType.CURRENT_LANG){
                    transferData.setProcessedByNativeFormatter(line);
                }
            }
            
           
            //***********************
            PhpModel model = ModelAccess.getAccess().getModel(doc);
            model.writeLock();
            try {
                model.sync();
                processStatementList(doc, model.getStatements(), absoluteIndents);
            } finally {
                model.writeUnlock();
            }


            //***********************
            
            int lastCrossPoint = 0;
            int lastOuterCrossPoint = 0;
            EmbeddingType lastEmbeddingType = null;
            
            int[] newIndents = new int[transferData.getNumberOfLines()];

            for (int i = 0; i < transferData.getNumberOfLines(); i++) {
                if (lastEmbeddingType != embeddingType[i]){
                    lastCrossPoint = i;
                    
                    if (lastEmbeddingType == EmbeddingType.OUTER){
                        lastOuterCrossPoint = i;
                    }
                    
                    lastEmbeddingType = embeddingType[i];
                }

                if (!transferData.isFormattable(i)) {
                    newIndents[i] = transferData.getOriginalIndent(i);
                } else {
                    if (embeddingType[i] == EmbeddingType.OUTER) {
                        newIndents[i] = previousIndents[i] + absoluteIndents[i];
                    } else if (embeddingType[i] == EmbeddingType.INNER) { // INNER
                        if (lastCrossPoint == i){ // first line of inner embedding
                            int previousLineIndent = i > 0 ? newIndents[lastCrossPoint - 1] : 0;
                            int absDiff = absoluteIndents[i] - (i > 0 ? absoluteIndents[i - 1] : 0);
                            newIndents[i] = previousLineIndent + absDiff;
                        } else {
                            int diff = previousIndents[i] - previousIndents[lastCrossPoint];
                            
                            newIndents[i] = newIndents[lastCrossPoint] + diff;
                        }
                    } else { // embeddingType[i] == EmbeddingType.CURRENT_LANG
                        newIndents[i] = previousIndents[lastOuterCrossPoint] + absoluteIndents[i];
                    }
                }
            }
            
            int lineBeforeSelectionBias = 0;
            
            if (firstRefBlockLine > 0){
                lineBeforeSelectionBias = transferData.getOriginalIndent(firstRefBlockLine - 1) - newIndents[firstRefBlockLine - 1];
            }
            
            // PASS 4: apply line indents
            
            for (int line = firstRefBlockLine; line <= lastRefBlockLine; line++) {
                int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
                int newIndent = newIndents[line] + lineBeforeSelectionBias;
                context.modifyIndent(lineStart, newIndent > 0 ? newIndent : 0);
            }
            
            transferData.setTransformedOffsets(newIndents);

            if (logger.isLoggable(Level.FINE)) {
                StringBuilder buff = new StringBuilder();

                for (int i = 0; i < transferData.getNumberOfLines(); i++) {
                    int lineStart = Utilities.getRowStartFromLineOffset(doc, i);

                    char formattingTypeSymbol = 0;
                    
                    if (!transferData.isFormattable(i)){
                        formattingTypeSymbol = '-';
                    } else if (embeddingType[i] == EmbeddingType.INNER){
                        formattingTypeSymbol = 'I';
                    } else if (embeddingType[i] == EmbeddingType.OUTER) {
                        formattingTypeSymbol = 'O';
                    } else {
                        formattingTypeSymbol = 'C';
                    }
                    
                    char formattingRange = (i >= firstRefBlockLine && i <= lastRefBlockLine) 
                            ? '*' : ' ';

                    buff.append(i + ":" + formattingRange + ":" + absoluteIndents[i] + ":" + formattingTypeSymbol + ":" + doc.getText(lineStart, Utilities.getRowEnd(doc, lineStart) - lineStart) + ".\n"); //NOI18N
                }

                buff.append("\n-------------\n"); //NOI18N
                logger.fine(getClass().getName() + ":\n" + buff);
            }

        } finally {
            doc.atomicUnlock();
        }
    }
    
    private void processStatementList(BaseDocument doc, Collection<Statement> statements, int absoluteIndents[]) throws BadLocationException{
        for (Statement statement : statements){
            processSourceElement(doc, absoluteIndents, statement);
        }
    }
    
    private void processSourceElement(BaseDocument doc, int[] absoluteIndents, SourceElement sourceElement) throws BadLocationException{
        if (sourceElement instanceof StatementContainer){
            for (Statement statement : ((StatementContainer) sourceElement).getStatements()){
                processSubStatement(doc, statement, absoluteIndents);
            }
        } else if (sourceElement instanceof ClassDefinition){
            for (ClassStatement classStatement : ((ClassDefinition)sourceElement).getBody().getStatements()){
                processSubStatement(doc, classStatement, absoluteIndents);
            }
        }
        
        for (SourceElement child : sourceElement.getChildren()){
            processSourceElement(doc, absoluteIndents, child);
        }
    }
    
    private void processSubStatement(BaseDocument doc, SourceElement statement, int[] absoluteIndents) throws BadLocationException {
        
        if (!(statement instanceof Block)) {
            TextBounds textBounds = findTokenSequenceBounds(doc, statement.getTokenSequence());

            int startLine = textBounds.getStartLine();
            int endLine = textBounds.getEndLine();
            for (int line = startLine; line <= endLine; line++) {
                absoluteIndents[line] += doc.getShiftWidth();
                
                if (line > startLine){
                    // increase indent for the all but the first line
                    // in a multi-line statement
                    absoluteIndents[line] += doc.getShiftWidth();
                }
            }
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

    protected boolean isWSToken(Token token) {
        return isOnlyWhiteSpaces(token.text());
    }

    protected static int getNumberOfLines(BaseDocument doc) throws BadLocationException {
        return Utilities.getLineOffset(doc, doc.getLength()) + 1;
    }

    protected Token getTokenAtOffset(JoinedTokenSequence tokenSequence, int tagTokenOffset) {
        if (tokenSequence != null && tagTokenOffset >= 0) {
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
    
    protected static int getExistingIndent(BaseDocument doc, int line) throws BadLocationException{
        int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
        return IndentUtils.lineIndent(doc, lineStart);
    }
    
    public void enterPressed(Context context) {
        BaseDocument doc = (BaseDocument)context.document();   
        doc.atomicLock();
        
        try {
            // WORKAROUND for the fact that the GLFIndentTask is called first instead of GenericTopLevelLanguageFormatter
            doc.putProperty(TransferData.ORG_CARET_OFFSET_DOCPROPERTY, new Integer(context.caretOffset()));
            // end of workaround
            Integer dotPos = (Integer) doc.getProperty(TransferData.ORG_CARET_OFFSET_DOCPROPERTY);
            assert dotPos != null;
            int origDotPos = dotPos.intValue() - 1; // dotPos - "\n".length()
            
            // a workaround for the fact that now there is no way to add indent task for the top level lang
            // (greedy Schlieman)
            if (indexWithinTopLevelLanguage(doc, origDotPos - 1)){
                int newIndent = 0;
                int baseIndent = 0;
                int lineNumber = Utilities.getLineOffset(doc, dotPos);

                if (Utilities.getRowStart(doc, origDotPos) == origDotPos){
                    baseIndent = getExistingIndent(doc, lineNumber);
                } else if (lineNumber > 0){
                    baseIndent = getExistingIndent(doc, lineNumber - 1);
                }

                newIndent = baseIndent;
                
                context.modifyIndent(Utilities.getRowStart(doc, dotPos), newIndent);
            } else if (indexWithinCurrentLanguage(doc, origDotPos - 1)) {
                int newIndent = 0;
                int baseIndent = 0;
                int lineNumber = Utilities.getLineOffset(doc, dotPos);
                boolean firstRow = false;

                if (Utilities.getRowStart(doc, origDotPos) == origDotPos){
                    baseIndent = getExistingIndent(doc, lineNumber);
                    firstRow = true;
                } else if (lineNumber > 0){
                    baseIndent = getExistingIndent(doc, lineNumber - 1);
                }

                newIndent = baseIndent;

                PhpModel model = ModelAccess.getAccess().getModel(doc);
                model.writeLock();
                try {
                    model.sync();
                    int elemIndent = 0;
                    SourceElement elem = model.findSourceElement(origDotPos);
                    
                    if (elem != null){
                        int elemOffset = getElementOffset(elem);
                        elemIndent = context.lineIndent(Utilities.getRowStart(doc, elemOffset));
                    }

                    logger.fine("element before caret: " + elem);

                    if (isElementThatIncreasesIndent(elem)) {
                        newIndent = elemIndent + doc.getShiftWidth();
                    } else if (elem instanceof Expression){
                        newIndent = elemIndent + doc.getShiftWidth();
                    } 
                    else if (elem instanceof Block || elem instanceof ClassBody) {
                        int firstNonWhiteBckw = Utilities.getFirstNonWhiteBwd(doc, origDotPos);
                        
                        Token tokenBefore = TokenUtils.getEmbeddedToken(doc, firstNonWhiteBckw);

                        if ("{".equals(tokenBefore.text().toString())) {
                            newIndent += doc.getShiftWidth();
                        }

                        int nextNonWhiteChar = Utilities.getFirstNonWhiteFwd(doc, origDotPos);
                        
                        nextNonWhiteChar = (nextNonWhiteChar > Utilities.getRowEnd(doc, origDotPos)) ? origDotPos + 1 : nextNonWhiteChar;

                        if (nextNonWhiteChar != -1) {

                            Token nextToken = TokenUtils.getPhpToken(doc, nextNonWhiteChar);

                            if (nextToken != null && "}".equals(nextToken.text().toString())) {
                                // smart enter (caret was just between bracets - {|})
                                doc.insertString(nextNonWhiteChar, "\n", null);
                                int nextLineStart = Utilities.getRowStart(doc, nextNonWhiteChar + 1);
                                context.modifyIndent(nextLineStart, baseIndent);
                                context.setCaretOffset(nextNonWhiteChar);
                            }
                        }
                    } else if (elem != null && isStatementThatIncreasesIndent(elem.getParent())) {
                        // decrease indent
                        SourceElement elemBehindCaret = model.findSourceElement(origDotPos + 1);
                        
                        // find the youngest common ancestor
                        
                        
                        
                        int parentStart = getElementOffset(elem.getParent());
                        int intentOfParentElement = context.lineIndent(Utilities.getRowStart(doc, parentStart));
                        newIndent = intentOfParentElement;
                    }

                } finally {
                    model.writeUnlock();
                }

                context.modifyIndent(Utilities.getRowStart(doc, dotPos), newIndent);

                if (firstRow){
                    context.setCaretOffset(context.caretOffset() - newIndent);
                }
            }

        } catch (BadLocationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            doc.atomicUnlock();
        }
    }
    
    private static int getElementOffset(SourceElement element) {
        TokenSequence tokenSequence = element.getTokenSequence();
        tokenSequence.moveStart();
        tokenSequence.moveNext();
        return tokenSequence.offset();
    }
    
    private static boolean isElementThatIncreasesIndent(SourceElement element) {
        Class[] indentIncreasingElements = new Class[]{For.class, While.class, If.class, ForEach.class, Else.class, ElseIf.class};

        for (Class clazz : indentIncreasingElements) {
            if (clazz.isInstance(element)) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean isStatementThatIncreasesIndent(SourceElement element) {
        Class[] indentIncreasingElements = new Class[]{ForStatement.class, WhileStatement.class,
            IfStatement.class, ForEachStatement.class, Else.class, ElseIf.class};

        for (Class clazz : indentIncreasingElements) {
            if (clazz.isInstance(element)) {
                return true;
            }
        }
        
        return false;
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
    
    private boolean indexWithinTopLevelLanguage(BaseDocument doc, int index) throws BadLocationException{
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence[] tokenSequences = (TokenSequence[]) tokenHierarchy.tokenSequenceList(LanguagePath.get(Language.find("text/x-php5")), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
        
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
    
    protected boolean isOnlyWhiteSpaces(CharSequence txt){
        for (int i = 0; i < txt.length(); i ++){
            if (!Character.isWhitespace(txt.charAt(i))){
                return false;
            }
        }
        
        return true;
    }
    
    private void markCurrentLanguageLines(BaseDocument doc, TextBounds languageBounds, EmbeddingType[] embeddingType) throws BadLocationException {
        if (languageBounds.getStartPos() == -1){
            return; // only white spaces
        }
        
        int firstLineOfTheLanguageBlock = languageBounds.getStartLine();

        int lineStart = Utilities.getRowStartFromLineOffset(doc, firstLineOfTheLanguageBlock);

        if (Utilities.getFirstNonWhiteFwd(doc, lineStart) < languageBounds.getStartPos()) {
            firstLineOfTheLanguageBlock++;
        }

        for (int i = firstLineOfTheLanguageBlock; i <= languageBounds.getEndLine(); i++) {
            embeddingType[i] = EmbeddingType.CURRENT_LANG;
        }
    }
}
