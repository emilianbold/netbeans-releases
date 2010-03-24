/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.indent;

import java.util.Arrays;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPNewLineIndenter {
    private Context context;

    private static final Collection<PHPTokenId> CONTROL_STATEMENT_TOKENS = Arrays.asList(
            PHPTokenId.PHP_DO, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_FOR,
            PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE);
    
    private Collection<ScopeDelimiter> scopeDelimiters = null;
    private int indentSize;
    private int continuationSize;
    private int itemsArrayDeclararionSize;

    public PHPNewLineIndenter(Context context) {
        this.context = context;
        indentSize = CodeStyle.get(context.document()).getIndentSize();
        continuationSize = CodeStyle.get(context.document()).getContinuationIndentSize();
        itemsArrayDeclararionSize = CodeStyle.get(context.document()).getItemsInArrayDeclarationIndentSize();
        int initialIndentSize = CodeStyle.get(context.document()).getInitialIndent();

        scopeDelimiters = Arrays.asList(
            new ScopeDelimiter(PHPTokenId.PHP_SEMICOLON, 0),
            new ScopeDelimiter(PHPTokenId.PHP_OPENTAG, initialIndentSize),
            new ScopeDelimiter(PHPTokenId.PHP_CURLY_CLOSE, 0),
            new ScopeDelimiter(PHPTokenId.PHP_CURLY_OPEN, indentSize),
            new ScopeDelimiter(PHPTokenId.PHP_CASE, indentSize),
            new ScopeDelimiter(PHPTokenId.PHP_IF, indentSize),
            new ScopeDelimiter(PHPTokenId.PHP_ELSE, indentSize),
            new ScopeDelimiter(PHPTokenId.PHP_ELSEIF, indentSize),
            new ScopeDelimiter(PHPTokenId.PHP_WHILE, indentSize),
            new ScopeDelimiter(PHPTokenId.PHP_DO, indentSize),
            new ScopeDelimiter(PHPTokenId.PHP_FOR, indentSize),
            new ScopeDelimiter(PHPTokenId.PHP_FOREACH, indentSize),
            new ScopeDelimiter(PHPTokenId.PHP_DEFAULT, indentSize)
        );
    }

    public void process() {
        final BaseDocument doc = (BaseDocument) context.document();
        final int offset = context.caretOffset();

        doc.runAtomic(new Runnable() {

            @Override
            public void run() {
                try {
                    int newIndent = 0;
                    boolean insideString = false;
                    TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
                    int caretLineStart = Utilities.getRowStart(doc, Utilities.getRowStart(doc, offset) - 1);
                    ts.move(offset);
                    ts.moveNext();

                    boolean indentStartComment = false;
                    

                   boolean movePrevious = false;
                   if (ts.token() == null) {
                        return;
                    }
                   if (ts.token().id() == PHPTokenId.WHITESPACE && ts.moveNext()) {
                        movePrevious = true;
                   }
                   if (ts.token().id() == PHPTokenId.PHP_COMMENT
                            || ts.token().id() == PHPTokenId.PHP_LINE_COMMENT
                            || ts.token().id() == PHPTokenId.PHP_COMMENT_START
                            || ts.token().id() == PHPTokenId.PHP_COMMENT_END) {

                       if (ts.token().id() == PHPTokenId.PHP_COMMENT_START && ts.offset() >= offset) {
                           indentStartComment = true;
                       }
                       else {
                           if (!movePrevious) {
                               // don't indent comment - issue #173979
                               return;
                           }
                           else {
                               if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
                                   ts.movePrevious();
                                   CharSequence whitespace = ts.token().text();
                                   if (ts.movePrevious() && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
                                       int index = 0;
                                       while (index < whitespace.length() && whitespace.charAt(index) != '\n') {
                                           index++;
                                       }
                                       if (index == whitespace.length()) {
                                           // don't indent if the line commnet continue
                                           // the last new line belongs to the line comment
                                           return;
                                       }
                                   }
                                   ts.moveNext();
                                   movePrevious = false;
                               }
                           }
                       }
                    }
                    if (movePrevious){
                        ts.movePrevious();
                    }
                    if (ts.token().id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING && offset > ts.offset()) {

                        int stringLineStart = Utilities.getRowStart(doc, ts.offset());

                        if (stringLineStart >= caretLineStart){
                            // string starts on the same line:
                            // current line indent + continuation size
                            newIndent = Utilities.getRowIndent(doc, stringLineStart) + indentSize;
                        } else {
                            // string starts before:
                            // repeat indent from the previous line
                            newIndent = Utilities.getRowIndent(doc, caretLineStart);
                        }

                        insideString = true;
                    }
                    
                    int bracketBalance = 0;

                    while (!insideString && ts.movePrevious()) {
                        Token token = ts.token();
                        ScopeDelimiter delimiter = getScopeDelimiter(token);
                        int anchor = ts.offset();
                        int shiftAtAncor = 0;

                        if (delimiter != null) {
                            if (delimiter.tokenId == PHPTokenId.PHP_SEMICOLON) {
                                if (breakProceededByCase(ts)){
                                    newIndent = Utilities.getRowIndent(doc, anchor) - indentSize;
                                    break;
                                }

                                CodeB4BreakData codeB4BreakData = processCodeBeforeBreak(ts, indentStartComment);
                                anchor = codeB4BreakData.expressionStartOffset;
                                shiftAtAncor = codeB4BreakData.indentDelta;

                                if (codeB4BreakData.processedByControlStmt){
                                    newIndent = Utilities.getRowIndent(doc, anchor) - indentSize;
                                }
                                else {
                                    newIndent = Utilities.getRowIndent(doc, anchor) + delimiter.indentDelta + shiftAtAncor;
                                }
                                break;
                            }
                            else if (delimiter.tokenId == PHPTokenId.PHP_CURLY_OPEN && ts.movePrevious()) {
                                int startExpression = findStartTokenOfExpression(ts);
                                newIndent = Utilities.getRowIndent(doc, startExpression) + indentSize;
                                break;
                            }

                            newIndent = Utilities.getRowIndent(doc, anchor) + delimiter.indentDelta + shiftAtAncor;
                            break;
                        } else {
                            if (ts.token().id() == PHPTokenId.PHP_TOKEN){
                                char ch = ts.token().text().charAt(0);
                                boolean indent = false;
                                switch (ch) {
                                    case ')' :
                                        bracketBalance++; break;
                                    case '(':
                                        if (bracketBalance == 0) {
                                            indent = true;
                                        }
                                        bracketBalance--;
                                        break;
                                    case ',':
                                        indent = true;
                                }
                                if (indent) {
                                    ts.move(offset);
                                    ts.moveNext();
                                    int startExpression = findStartTokenOfExpression(ts);
                                    if (startExpression != -1) {
                                        int offsetArrayDeclaration = offsetArrayDeclaration(startExpression, ts);
                                        if (offsetArrayDeclaration > -1) {
                                            newIndent = Utilities.getRowIndent(doc, offsetArrayDeclaration) + itemsArrayDeclararionSize;
                                        }
                                        else {
                                            newIndent = Utilities.getRowIndent(doc, startExpression) + continuationSize;
                                        }
                                    }
                                    break;
                                }
                            }
                            else if (ts.token().id() == PHPTokenId.PHP_OBJECT_OPERATOR) {
                                int startExpression = findStartTokenOfExpression(ts);
                                if (startExpression != -1) {
                                    newIndent = Utilities.getRowIndent(doc, startExpression) + continuationSize;
                                    break;
                                }
                            }
                        }
                    }

                    if (newIndent < 0){
                        newIndent = 0;
                    }

                    context.modifyIndent(Utilities.getRowStart(doc, offset), newIndent);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private CodeB4BreakData processCodeBeforeBreak(TokenSequence ts, boolean indentComment){
        CodeB4BreakData retunValue = new CodeB4BreakData();
        int origOffset = ts.offset();
        Token token = ts.token();

        if (token.id() == PHPTokenId.PHP_SEMICOLON && ts.movePrevious()) {
            retunValue.expressionStartOffset = findStartTokenOfExpression(ts);
            retunValue.indentDelta = 0;
            retunValue.processedByControlStmt = false;
            return retunValue;
        }
        while (ts.movePrevious()) {
            token = ts.token();
            ScopeDelimiter delimiter = getScopeDelimiter(token);
            if (delimiter != null){
                retunValue.expressionStartOffset = ts.offset();
                retunValue.indentDelta = delimiter.indentDelta;
                if (CONTROL_STATEMENT_TOKENS.contains(delimiter.tokenId)) {
                    retunValue.indentDelta = 0;
                }
                break;
            }
            else {
                if (indentComment && token.id() == PHPTokenId.WHITESPACE
                        && token.text().toString().indexOf('\n') != -1
                        && ts.moveNext()) {
                    retunValue.expressionStartOffset = ts.offset();
                    retunValue.indentDelta = 0;
                    break;
                }
            }
        }

        if (token.id() == PHPTokenId.PHP_OPENTAG && ts.moveNext()) {
            // we are at the begining of the php blog
            token = LexUtilities.findNext(ts, Arrays.asList(
                        PHPTokenId.WHITESPACE,
                        PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                        PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                        PHPTokenId.PHP_LINE_COMMENT));
            retunValue.expressionStartOffset = ts.offset();
            retunValue.indentDelta = 0;
        }
        ts.move(origOffset);
        ts.moveNext();
        return retunValue;
    }

    /**
     * Returns of set of the array declaration, where is the exexpression.
     * @param startExpression
     * @param ts
     * @return
     */
    private int  offsetArrayDeclaration(int startExpression, TokenSequence ts) {
        int result = -1;
        int origOffset = ts.offset();
        Token token;
        int balance = 0;

        do {
            token = ts.token();
            if (token.id() == PHPTokenId.PHP_TOKEN) {
                switch (token.text().charAt(0)) {
                    case ')' :
                        balance --;
                        break;
                    case '(':
                        balance ++;
                        break;
                }
            }
        } while (ts.offset() > startExpression
                && !(token.id() == PHPTokenId.PHP_ARRAY && balance ==1)
                && ts.movePrevious());

        if (token.id() == PHPTokenId.PHP_ARRAY && balance == 1) {
            result = ts.offset();
        }
        ts.move(origOffset);
        ts.moveNext();
        return result;
    }

    protected static int findStartTokenOfExpression(TokenSequence ts) {
        int start = -1;
        int origOffset = ts.offset();

        Token token;
        int balance = 0;
        int curlyBalance = 0;
        do {
            token = ts.token();
            if (token.id() == PHPTokenId.PHP_TOKEN) {
                switch (token.text().charAt(0)) {
                    case ')' :
                        balance --;
                        break;
                    case '(':
                        balance ++;
                        break;
                }
            }
            else if ((token.id() == PHPTokenId.PHP_SEMICOLON || token.id() == PHPTokenId.PHP_OPENTAG)
                    && ts.moveNext()) {
                // we found previous end of expression => find begin of the current.
                token = LexUtilities.findNext(ts, Arrays.asList(
                        PHPTokenId.WHITESPACE,
                        PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                        PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                        PHPTokenId.PHP_LINE_COMMENT));
                start = ts.offset();
                break;
            }
            else if (token.id() == PHPTokenId.PHP_IF) {
                // we are at a beginning of if .... withouth curly?
                // need to find end of the condition.
                int offsetIf = ts.offset(); // remember the if offset
                token = LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN));
                if (ts.offset() < origOffset && token.text().charAt(0) == '(') {
                    // we have the start of the condition and now find the end
                    int parentBalance = 1;
                    while (start == -1 && parentBalance > 0 && ts.offset() < origOffset && ts.moveNext()) {
                        token = LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN));
                        if (token.text().charAt(0) == '(') {
                            parentBalance++;
                        }
                        else if (token.text().charAt(0) == ')') {
                            parentBalance--;
                        }
                    }
                    if (parentBalance == 0 && ts.moveNext() && ts.offset() < origOffset) {
                        // we should have the end of condtion and we need to find next token.
//                        token = LexUtilities.findNext(ts, Arrays.asList(
//                                PHPTokenId.WHITESPACE,
//                                PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
//                                PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
//                                PHPTokenId.PHP_LINE_COMMENT));
//                        if (ts.offset() < origOffset) {
                            start = offsetIf;
                            break;
                        //}
                    }
                    else if (parentBalance > 0) {
                        // probably we are in a function in the condition
                        // and we need to find a line where is the function invocation
                        parentBalance = 0;
                        while (parentBalance < 1 && ts.offset() > offsetIf && ts.movePrevious()) {
                            token = LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN));
                            if (token.text().charAt(0) == '(') {
                                parentBalance++;
                            }
                            else if (token.text().charAt(0) == ')') {
                                parentBalance--;
                            }
                        }
                        if (parentBalance == 1 && ts.movePrevious()) {
                            token = LexUtilities.findPrevious(ts, Arrays.asList(
                                PHPTokenId.WHITESPACE,
                                PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                                PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                                PHPTokenId.PHP_LINE_COMMENT));
                            start = ts.offset();
                        }
                        break;
                    }
                    else if (parentBalance == 0) {
                        // before the end of condition
                        start = offsetIf;
                        break;
                    }
                }
                else {
                    ts.move(offsetIf);
                    ts.movePrevious();
                }
            }
            else if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                curlyBalance --;
                if (curlyBalance == -1 && ts.moveNext()) {
                    // we are after previous blog close
                    token = LexUtilities.findNext(ts, Arrays.asList(
                                PHPTokenId.WHITESPACE,
                                PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                                PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                                PHPTokenId.PHP_LINE_COMMENT));
                    if (ts.offset() <= origOffset) {
                        start = ts.offset();
                    }
                    else {
                        start = origOffset;
                    }
                    break;
                }
            }
            else if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                curlyBalance ++;
                if (curlyBalance == 1 && ts.moveNext()) {
                    // we are at the begining of a blog
                    token = LexUtilities.findNext(ts, Arrays.asList(
                                PHPTokenId.WHITESPACE,
                                PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                                PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                                PHPTokenId.PHP_LINE_COMMENT));
                    if (ts.offset() <= origOffset) {
                        start = ts.offset();
                    }
                    else {
                        start = origOffset;
                    }
                    break;
                }
            }
            else if (balance == 1 && token.id() == PHPTokenId.PHP_STRING) {
                // probably there is a function call insede the expression
                start = ts.offset();
                break;
            }
        } while (ts.movePrevious());

        if (!ts.movePrevious()) {
            // we are at the first php line
            token = LexUtilities.findNext(ts, Arrays.asList(
                        PHPTokenId.WHITESPACE,
                        PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                        PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                        PHPTokenId.PHP_LINE_COMMENT, PHPTokenId.PHP_OPENTAG));
            start = ts.offset();
        }

        ts.move(origOffset);
        ts.moveNext();

        return start;
    }


    private boolean breakProceededByCase(TokenSequence ts){
        boolean retunValue = false;
        int origOffset = ts.offset();

        if (ts.movePrevious()) {
            if (semicolonProceededByBreak(ts)) {
                while (ts.movePrevious()) {
                    TokenId tid = ts.token().id();

                    if (tid == PHPTokenId.PHP_CASE) {
                        retunValue = true;
                        break;
                    } else if (CONTROL_STATEMENT_TOKENS.contains(tid)) {
                        break;
                    }
                }
            }
        }

        ts.move(origOffset);
        ts.moveNext();
        
        return retunValue;
    }

    private boolean semicolonProceededByBreak(TokenSequence ts){
                boolean retunValue = false;
                
        if (ts.token().id() == PHPTokenId.PHP_BREAK){
            retunValue = true;
        } else if (ts.token().id() == PHPTokenId.PHP_NUMBER){
            int origOffset = ts.offset();
            
            if (ts.movePrevious()){
                if (ts.token().id() == PHPTokenId.WHITESPACE){
                    if (ts.movePrevious()){
                        if (ts.token().id() == PHPTokenId.PHP_BREAK){
                            retunValue = true;
                        }
                    }
                }
            }

            ts.move(origOffset);
            ts.moveNext();
        }
        
        return retunValue;
    }

    private ScopeDelimiter getScopeDelimiter(Token token){
        // TODO: more efficient impl

        for (ScopeDelimiter scopeDelimiter : scopeDelimiters){
            if (scopeDelimiter.matches(token)){
                return scopeDelimiter;
            }
        }

        return null;
    }

    private static class CodeB4BreakData{
        int expressionStartOffset;
        boolean processedByControlStmt;
        int indentDelta;
    }

    private static class ScopeDelimiter{
        private TokenId tokenId;
        private String tokenContent;
        private int indentDelta;

        public ScopeDelimiter(TokenId tokenId, int indentDelta) {
            this(tokenId, null, indentDelta);
        }

        public ScopeDelimiter(TokenId tokenId, String tokenContent, int indentDelta) {
            this.tokenId = tokenId;
            this.tokenContent = tokenContent;
            this.indentDelta = indentDelta;
        }

        public boolean matches(Token token){
            if (tokenId != token.id()){
                return false;
            }

            if (tokenContent != null 
                    && TokenUtilities.equals(token.text(), tokenContent)){
                
                return false;
            }

            return true;
        }
    }
}
