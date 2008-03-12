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

package org.netbeans.modules.cnd.editor.reformat;

import java.util.LinkedList;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.CodeStyle.BracePlacement;
import org.netbeans.modules.cnd.editor.reformat.BracesStack.StatementKind;
import org.netbeans.modules.cnd.editor.reformat.ContextDetector.OperatorKind;
import org.netbeans.modules.cnd.editor.reformat.DiffLinkedList.DiffResult;
import org.netbeans.modules.cnd.editor.reformat.Reformatter.Diff;

/**
 *
 * @author Alexander Simon
 */
public class ReformatterImpl {
    /*package local*/ final ContextDetector ts;
    /*package local*/ final CodeStyle codeStyle;
    /*package local*/ final DiffLinkedList diffs = new DiffLinkedList();
    /*package local*/ final BracesStack braces;
    private final int startOffset;
    private final int endOffset;
    private PreprocessorFormatter preprocessorFormatter;
    
    ReformatterImpl(TokenSequence<CppTokenId> ts, int startOffset, int endOffset, CodeStyle codeStyle){
        braces = new BracesStack(codeStyle);
        this.ts = new ContextDetector(ts, diffs, braces);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.codeStyle = codeStyle;
        preprocessorFormatter = new PreprocessorFormatter(this);
    }
    
    LinkedList<Diff> reformat(){
        ts.moveStart();
        Token<CppTokenId> previous = ts.lookPrevious();
        while(ts.moveNext()){
            if (ts.offset() > endOffset) {
                break;
            }
            Token<CppTokenId> current = ts.token();
            CppTokenId id = current.id();
            if (previous != null && previous.id() == PREPROCESSOR_DIRECTIVE && id != PREPROCESSOR_DIRECTIVE){
                // indent afre preprocessor directive
                if (braces.getStatementContinuation() == BracesStack.StatementContinuation.START){
                    braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE);
                }
                if (doFormat()){
                    indentNewLine(current);
                }
            }
            switch(id){
                case PREPROCESSOR_DIRECTIVE: //(null, "preprocessor"),
                case NEW_LINE:
                case ESCAPED_WHITESPACE:
                case WHITESPACE:
                case BLOCK_COMMENT:
                case DOXYGEN_COMMENT:
                case LINE_COMMENT:
                case PRIVATE:
                case PROTECTED:
                case PUBLIC:
                case COLON:
                case SEMICOLON:
                case LBRACE:
                case RBRACE:
                case LPAREN:
                case RPAREN:
                    break;
                default:
                    braces.setLastStatementStart(ts);
            }
            switch(id){
                case PREPROCESSOR_DIRECTIVE: //(null, "preprocessor"),
                {
                    preprocessorFormatter.indentPreprocessor(previous);
                    break;
                }
                case NEW_LINE:
                {
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.START){
                        braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE);
                    }
                    if (doFormat()) {
                        newLineFormat(previous, current, braces.parenDepth);
                    }
                    break;
                }
                case WHITESPACE:
                {
                    if (doFormat()) {
                        whiteSpaceFormat(previous, current);
                    }
                    break;
                }
                case DOXYGEN_COMMENT:
                case BLOCK_COMMENT:
                {
                    if (doFormat()) {
                        reformatBlockComment(previous, current);
                    }
                    break;
                }
                case LBRACE: //("{", "separator"),
                {
                    int start = braces.lastStatementStart;
                    braces.push(ts);
                    if (doFormat()) {
                        braceFormat(previous, current);

                        StackEntry entry = braces.peek();
                        if (entry.getImportantKind() == CLASS ||
                            entry.getImportantKind() == STRUCT ||    
                            entry.getImportantKind() == UNION ||    
                            entry.getImportantKind() == ENUM) {
                            // add new lines before class declaration
                            newLinesBeforeDeclaration(codeStyle.blankLinesBeforeClass(), start);
                        } else if (entry.getImportantKind() == NAMESPACE){
                            // TODO blank lines before namespace
                        } else if (entry.isLikeToFunction()) {
                            // add new lines before method declaration
                            newLinesBeforeDeclaration(codeStyle.blankLinesBeforeMethods(), start);
                        } else if (entry.isLikeToArrayInitialization()) {
                            // no action
                        } else {
                            Token<CppTokenId> prevImportant = ts.lookPreviousImportant();
                            if (prevImportant != null &&
                                prevImportant.id() == SEMICOLON &&
                                braces.getLength() == 1) {
                                // TODO detect K&R style.
                                entry.setLikeToFunction(true);
                                newLinesBeforeDeclaration(codeStyle.blankLinesBeforeMethods(), braces.lastKRstart);
                            }
                        }
                    }
                    braces.lastKRstart = -1;
                    break;
                }
                case LPAREN: //("(", "separator"),
                {
                    if (braces.parenDepth == 0) {
                        if (braces.getStatementContinuation() == BracesStack.StatementContinuation.STOP) {
                            braces.setStatementContinuation(BracesStack.StatementContinuation.START);
                        }
                        if (braces.getLength() == 0){
                            // save K&R start
                            braces.lastKRstart = braces.lastStatementStart;
                        }
                    }
                    braces.parenDepth++;
                    if (doFormat()) {
                        formatLeftParen(previous, current);
                    }
                    break;
                }
                case RPAREN: //(")", "separator"),
                {
                    braces.parenDepth--;
                    if (braces.parenDepth < 0){
                        // unbalanced paren
                        braces.parenDepth = 0;
                    }
                    if (braces.parenDepth == 0) {
                        StackEntry entry = braces.peek();
                        if (entry == null || entry.getKind() != LBRACE ||
                            entry.getImportantKind() == CLASS || entry.getImportantKind() == NAMESPACE){
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                        }
                    }
                    if (doFormat()) {
                        formatRightParen(previous, current);
                    }
                    break;
                }
                case IDENTIFIER:
                {
                    boolean isStart = false;
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.STOP) {
                        braces.setStatementContinuation(BracesStack.StatementContinuation.START);
                        isStart = ts.index() == braces.lastStatementStart;
                    }
                    if (isStart) {
                        Token<CppTokenId> next = ts.lookNextImportant();
                        if (next != null && next.id() == COLON) {
                            braces.isLabel = true;
                            if (doFormat()) {
                                if (!ts.isFirstLineToken()) {
                                    ts.addBeforeCurrent(1, 0);
                                } else {
                                    DiffResult diff = diffs.getDiffs(ts, -1);
                                    if (diff == null){
                                        if (previous != null && previous.id() == WHITESPACE) {
                                            ts.replacePrevious(previous, 0, 0);
                                        }
                                    } else {
                                        if (diff.after != null) {
                                            diff.after.replaceSpaces(0);
                                        }
                                        if (diff.replace != null) {
                                            diff.replace.replaceSpaces(0);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                case SEMICOLON: //(";", "separator"),
                {
                    StackEntry entry = braces.peek();
                    if (braces.parenDepth == 0) {
                        braces.pop(ts);
                    }
                    if (entry != null && 
                       (entry.getKind() == DO || entry.getImportantKind() == DO)) {
                        Token<CppTokenId> next = ts.lookNextImportant();
                        if (next != null && next.id() == WHILE) {
                            braces.isDoWhile = true;
                        }
                    }
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceBeforeSemi());
                        if (true) {
                            // TODO should be controlled
                            // add new line after ;
                            if (braces.parenDepth == 0) {
                                Token<CppTokenId> next = ts.lookNext();
                                if (next != null) {
                                    Token<CppTokenId> n2 = ts.lookNext(2);
                                    if (!(next.id() == NEW_LINE ||
                                          next.id() == LINE_COMMENT ||
                                          next.id() == WHITESPACE && n2 != null && n2.id() == LINE_COMMENT)){
                                        ts.addAfterCurrent(current, 1, getIndent());
                                        break;
                                    }    
                                }
                            }
                        }
                        spaceAfter(current, codeStyle.spaceAfterSemi());
                    }
                    if (braces.parenDepth == 0) {
                        braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                    }
                    break;
                }
                case COMMA: //(",", "separator"),
                {
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceBeforeComma());
                        spaceAfter(current, codeStyle.spaceAfterComma());
                    }
                    break;
                }
                case PRIVATE:
                case PROTECTED:
                case PUBLIC:
                {
                    StackEntry entry = braces.peek();
                    if (doFormat()) {
                        if (entry != null && entry.getImportantKind() != null){
                            switch (entry.getImportantKind()) {
                                case CLASS: //("class", "keyword"), //C++
                                case STRUCT: //("struct", "keyword"),
                                    Token<CppTokenId> next = ts.lookNextImportant();
                                    if (next != null && next.id() == COLON) {
                                        newLineBefore();
                                    }
                                    break;
                            }
                        }
                    }
                    break;
                }
                case COLON: //(":", "operator"),
                {
                    boolean isLabel = braces.isLabel;
                    braces.isLabel = false;
                    if (doFormat()) {
                        if (isLabel) {
                            spaceBefore(previous, false);
                            if(!ts.isLastLineToken()){
                                ts.addAfterCurrent(current, 1, getIndent());
                            }
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            break;
                        }
                        Token<CppTokenId> p = ts.lookPreviousImportant();
                        if (p != null && 
                           (p.id() == PRIVATE ||
                            p.id() == PROTECTED ||
                            p.id() == PUBLIC)) {
                            spaceBefore(previous, false);
                            if(!ts.isLastLineToken()){
                                // TODO use flase?
                                ts.addAfterCurrent(current, 1, getIndent());
                            }
                            break;
                        }
                        if (p != null && p.id() == DEFAULT) {
                            // TODO use flase?
                            spaceBefore(previous, false);
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            break;
                        }
                        Token<CppTokenId> p2 = ts.lookPreviousImportant(2);
                        if (p2 != null && p2.id() == CASE) {
                            // TODO use flase?
                            spaceBefore(previous, false);
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            break;
                        }
                        spaceBefore(previous, codeStyle.spaceBeforeColon());
                        spaceAfter(current, codeStyle.spaceAfterColon());
                    }
                    break;
                }
                case RBRACE: //("}", "separator"),
                {
                    StackEntry entry = braces.peek();
                    braces.pop(ts);
                    if (entry != null && 
                       (entry.getKind() == DO || entry.getImportantKind() == DO)) {
                        Token<CppTokenId> next = ts.lookNextImportant();
                        if (next != null && next.id() == WHILE) {
                            braces.isDoWhile = true;
                        }
                    }
                    if (doFormat()) {
                        indentRbrace(entry, previous, current);
                    }
                    break;
                }
                case NOT: //("!", "operator"),
                case TILDE: //("~", "operator"),
                case PLUSPLUS: //("++", "operator"),
                case MINUSMINUS: //("--","operator"),
                {
                    if (doFormat()) {
                        if (!isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                            spaceAfter(current, codeStyle.spaceAroundUnaryOps());
                        } else {
                            spaceBefore(previous, false);
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen());
                        }
                    }
                    break;
                }
                case PLUS: //("+", "operator"),
                case MINUS: //("-", "operator"),
                {
                    if (doFormat()) {
                        if (!isOperator()) {
                            OperatorKind kind = ts.getOperatorKind(current);
                            if (kind == OperatorKind.BINARY){
                                spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                                spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                            } else if (kind == OperatorKind.UNARY){
                                spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                                spaceAfter(current, codeStyle.spaceAroundUnaryOps());
                            }
                        } else {
                            spaceBefore(previous, false);
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen());
                        }
                    }
                    break;
                }
                case STAR: //("*", "operator"),
                case AMP: //("&", "operator"),
                {
                    if (doFormat()) {
                        if (!isOperator()) {
                            OperatorKind kind = ts.getOperatorKind(current);
                            if (kind == OperatorKind.BINARY){
                                spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                                spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                            } else if (kind == OperatorKind.TYPE_MODIFIER){
                                //TODO style of type declaration
                            }
                        } else {
                            spaceBefore(previous, false);
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen());
                        }
                    }
                    break;
                }
                case GT: //(">", "operator"),
                case LT: //("<", "operator"),
                {
                    if (doFormat()) {
                        if (!isOperator()) {
                            OperatorKind kind = ts.getOperatorKind(current);
                            if (kind == OperatorKind.BINARY){
                                spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                                spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                            } else if (kind == OperatorKind.SEPARATOR){
                                //TODO style of template declaration
                            }
                        } else {
                            spaceBefore(previous, false);
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen());
                        }
                    }
                    break;
                }
                case EQEQ: //("==", "operator"),
                case LTEQ: //("<=", "operator"),
                case GTEQ: //(">=", "operator"),
                case NOTEQ: //("!=","operator"),
                case AMPAMP: //("&&", "operator"),
                case BARBAR: //("||", "operator"),
                case SLASH: //("/", "operator"),
                case BAR: //("|", "operator"),
                case PERCENT: //("%", "operator"),
                case LTLT: //("<<", "operator"),
                case GTGT: //(">>", "operator"),
                {
                    if (doFormat()) {
                        if (!isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                            spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                        } else {
                            spaceBefore(previous, false);
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen());
                        }
                    }
                    break;
                }
                case EQ: //("=", "operator"),
                case PLUSEQ: //("+=", "operator"),
                case MINUSEQ: //("-=", "operator"),
                case STAREQ: //("*=", "operator"),
                case SLASHEQ: //("/=", "operator"),
                case AMPEQ: //("&=", "operator"),
                case BAREQ: //("|=", "operator"),
                case CARETEQ: //("^=", "operator"),
                case PERCENTEQ: //("%=", "operator"),
                case LTLTEQ: //("<<=", "operator"),
                case GTGTEQ: //(">>=", "operator"),
                {
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.STOP) {
                        braces.setStatementContinuation(BracesStack.StatementContinuation.START);
                    }
                    if (doFormat()) {
                        if (!isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAroundAssignOps());
                            spaceAfter(current, codeStyle.spaceAroundAssignOps());
                        } else {
                            spaceBefore(previous, false);
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen());
                        }
                    }
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.START){
                        braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE);
                    }
                    break;
                }
                case NAMESPACE: //("namespace", "keyword"), //C++
                case CLASS: //("class", "keyword"), //C++
                case STRUCT: //("struct", "keyword"),
                case ENUM: //("enum", "keyword"),
                case UNION: //("union", "keyword"),
                {
                    break;
                }
                case IF: //("if", "keyword-directive"),
                {
                    braces.push(ts);
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeIfParen(), LPAREN);
                    }
                    break;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    braces.push(ts);
                    if (doFormat()) {
                       formatElse(previous);
                    }
                    break;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    braces.push(ts);
                    if (doFormat()) {
                        boolean doSpaceBefore = true;
                        if (braces.isDoWhile) {
                            if (ts.isFirstLineToken()) {
                                if (!codeStyle.newLineWhile()) {
                                    // try to remove new line
                                    newLine(previous, current, CodeStyle.BracePlacement.SAME_LINE,
                                            codeStyle.spaceBeforeWhile(), 0);
                                    doSpaceBefore = false;
                                }
                            } else {
                                if (codeStyle.newLineWhile()) {
                                    // add new line
                                    newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE,
                                            codeStyle.spaceBeforeWhile(), 0);
                                    doSpaceBefore = false;
                                }
                            }
                        }
                        if (doSpaceBefore){
                            spaceBefore(previous, codeStyle.spaceBeforeWhile());
                        }
                        spaceAfterBefore(current, codeStyle.spaceBeforeWhileParen(), LPAREN);
                    }
                    braces.isDoWhile = false;
                    break;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    braces.push(ts);
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeForParen(), LPAREN);
                    }
                    break;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    braces.push(ts);
                    break;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    braces.push(ts);
                    if (doFormat()) {
                        boolean doSpaceBefore = true;
                        if (ts.isFirstLineToken()) {
                            if (!codeStyle.newLineCatch()){
                                // try to remove new line
                                newLine(previous, current, CodeStyle.BracePlacement.SAME_LINE,
                                        codeStyle.spaceBeforeCatch(), 0);
                                doSpaceBefore = false;
                            }
                        } else {
                             if (codeStyle.newLineCatch()){
                                // add new line
                                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE,
                                        codeStyle.spaceBeforeCatch(), 0);
                                doSpaceBefore = false;
                            }
                       }
                       if (doSpaceBefore){
                          spaceBefore(previous, codeStyle.spaceBeforeCatch());
                       }
                       spaceAfterBefore(current, codeStyle.spaceBeforeCatchParen(), LPAREN);
                    }
                    break;
                }
                case ASM: //("asm", "keyword-directive"), // gcc and C++
                {
                    braces.push(ts);
                    break;
                }
                case DO: //("do", "keyword-directive"),
                {
                    braces.push(ts);
                    break;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
                    braces.push(ts);
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeSwitchParen(), LPAREN);
                    }
                    break;
                }
                case DEFAULT: //("default", "keyword-directive"),
                case CASE: //("case", "keyword-directive"),
                {
                    braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                    break;
                }
                case BREAK: //("break", "keyword-directive"),
                {
                    break;
                }
                case CONTINUE: //("continue", "keyword-directive"),
                {
                    break;
                }
                case SCOPE:
                {
                    if (doFormat()) {
                        Token<CppTokenId> p = ts.lookPreviousImportant(1);
                        if (p != null && p.id() == IDENTIFIER) {
                            spaceBefore(previous, false);
                        }
                        spaceAfter(current, false);
                    }
                }
            }
            previous = current;
        }
        return diffs.getStorage();
    }
    
    /*package local*/ int getParentIndent() {
        return continuationIndent(braces.getSelfIndent());
    }

    /*package local*/ int getCaseIndent() {
        if (codeStyle.getFormatNewlineBeforeBrace() == BracePlacement.NEW_LINE_HALF_INDENTED){
            if (codeStyle.indentCasesFromSwitch()) {
                return getParentIndent() + codeStyle.indentSize()/2;
            } else {
                return getParentIndent();
            }
        } else {
            if (codeStyle.indentCasesFromSwitch()) {
                return getParentIndent() + codeStyle.indentSize();
            } else {
                return getParentIndent();
            }
        }
    }

    /*package local*/ int getIndent() {
        return continuationIndent(braces.getIndent());
    }

    /*package local*/ int continuationIndent(int shift){
        StackEntry entry = braces.peek();
        if (entry != null) {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE){
                switch (entry.getKind()){
                    case NAMESPACE: //("namespace", "keyword"), //C++
                    case CLASS: //("class", "keyword"), //C++
                    case STRUCT: //("struct", "keyword"),
                    case ENUM: //("enum", "keyword"),
                    case UNION: //("union", "keyword"),
                        break;
                    case IF: 
                    case ELSE: 
                    case FOR: 
                    case DO: 
                    case WHILE: 
                    case SWITCH: 
                    case CATCH: 
                        if (codeStyle.getFormatNewlineBeforeBrace() == BracePlacement.NEW_LINE_HALF_INDENTED){
                            shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.indentSize()/2;
                        } else {
                            shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.indentSize();
                        }
                        break;
                    default:
                    {
                        if (entry.getKind() == LBRACE) {
                            if (entry.getImportantKind() != null &&
                                entry.getImportantKind() == ENUM) {
                                break;
                            }
                        }
                        if (entry.isLikeToArrayInitialization()){
                            break;
                        }
                        StatementKind kind = braces.getLastStatementKind(ts);
                        if (kind == null || 
                            !(kind == StatementKind.CLASS ||
                              kind == StatementKind.FUNCTION && braces.parenDepth == 0)) {
                            shift += codeStyle.getFormatStatementContinuationIndent();
                        }
                        break;
                    }
                }
            }
        } else {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE){
                StatementKind kind = braces.getLastStatementKind(ts);
                
                if (kind == null || 
                    !(kind == StatementKind.CLASS ||
                      kind == StatementKind.FUNCTION && braces.parenDepth == 0)) {
                    shift += codeStyle.getFormatStatementContinuationIndent();
                }
            }
        }
        if (shift > 0) {
            return shift;
        } else {
            return 0;
        }
    }
    
    
    /*package local*/ int getIndent(int shift) {
        shift = shift * codeStyle.indentSize();
        if (codeStyle.indentCasesFromSwitch()) {
            shift += codeStyle.indentSize() * braces.switchDepth();
        }
        StackEntry entry = braces.peek();
        if (entry != null) {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE){
                switch (entry.getKind()){
                    case NAMESPACE: //("namespace", "keyword"), //C++
                    case CLASS: //("class", "keyword"), //C++
                    case STRUCT: //("struct", "keyword"),
                    case ENUM: //("enum", "keyword"),
                    case UNION: //("union", "keyword"),
                        break;
                    case IF: 
                    case ELSE: 
                    case FOR: 
                    case DO: 
                    case WHILE: 
                    case SWITCH: 
                    case CATCH: 
                        shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.indentSize();
                        break;
                    default:
                    {
                        if (entry.getKind() == LBRACE) {
                            if (entry.getImportantKind() != null &&
                                entry.getImportantKind() == ENUM) {
                                break;
                            }
                        }
                        if (entry.isLikeToArrayInitialization()){
                            break;
                        }
                        StatementKind kind = braces.getLastStatementKind(ts);
                        if (kind == null || 
                            !(kind == StatementKind.CLASS ||
                              kind == StatementKind.FUNCTION && braces.parenDepth == 0)) {
                            shift += codeStyle.getFormatStatementContinuationIndent();
                        }
                        break;
                    }
                }
            }
        } else {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE){
                StatementKind kind = braces.getLastStatementKind(ts);
                
                if (kind == null || 
                    !(kind == StatementKind.CLASS ||
                      kind == StatementKind.FUNCTION && braces.parenDepth == 0)) {
                    shift += codeStyle.getFormatStatementContinuationIndent();
                }
            }
        }
        //Token<CppTokenId> next = ts.lookNextImportant();
        //if (next != null) {
        //    System.out.println("Indent:"+next.text().toString()+"="+shift);
        //}
        if (shift > 0) {
            return shift;
        } else {
            return 0;
        }
    }

    private void braceFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        StackEntry entry = braces.peek();
        if (entry != null && entry.getImportantKind() != null) {
            switch (entry.getImportantKind()) {
                case NAMESPACE: //("namespace", "keyword"), //C++
                {
                    // TODO divide for neamespace
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceNamespace(),
                            codeStyle.spaceBeforeClassDeclLeftBrace(), 1);
                    return;
                }
                case CLASS: //("class", "keyword"), //C++
                case STRUCT: //("struct", "keyword"),
                case ENUM: //("enum", "keyword"),
                case UNION: //("union", "keyword"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceClass(),
                            codeStyle.spaceBeforeClassDeclLeftBrace(), codeStyle.blankLinesAfterClassHeader()+1);
                    return;
                }
                case IF: //("if", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeIfLeftBrace(), 1);
                    return;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeElseLeftBrace(), 1);
                    return;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeSwitchLeftBrace(), 1);
                    return;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeWhileLeftBrace(), 1);
                    return;
                }
                case DO: //("do", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeDoLeftBrace(), 1);
                    return;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeForLeftBrace(), 1);
                    return;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeTryLeftBrace(), 1);
                    return;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeCatchLeftBrace(), 1);
                    return;
                }
            }
        }
        if (entry != null && entry.isLikeToFunction()) {
            newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceDeclaration(),
                    codeStyle.spaceBeforeMethodDeclLeftBrace(), 1);
        } else if (entry != null && entry.isLikeToArrayInitialization()) {
            StackEntry prevEntry = braces.lookPerevious();
            if (prevEntry != null && prevEntry.isLikeToArrayInitialization()) {
                // it a situation int a[][]={{
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE,
                        codeStyle.spaceBeforeArrayInitLeftBrace(), 0);
            } else {
                Token<CppTokenId> p1 = ts.lookPreviousLineImportant();
                boolean concurent = false;
                if (p1 != null) {
                    if (p1.id() == EQ){
                        concurent |= codeStyle.spaceAroundAssignOps();
                    }
                }
                newLine(previous, current, CodeStyle.BracePlacement.SAME_LINE,
                        concurent || codeStyle.spaceBeforeArrayInitLeftBrace(), 0);
            }
        } else {
            // TODO add options for block spaces 
            Token<CppTokenId> p1 = ts.lookPreviousImportant();
            if (p1 != null && p1.id() == LBRACE) {
                // it a situation while(true){{
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, 1);
                return;
            }
            StackEntry prevEntry = braces.lookPerevious();
            if (prevEntry != null &&
                prevEntry.getImportantKind() != null && prevEntry.getImportantKind() == SWITCH){
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, 1);
                return;
            }
            if (prevEntry == null ||
                prevEntry != null && prevEntry.getImportantKind() != null && prevEntry.getImportantKind() == NAMESPACE){
                // It is a K&R stryle of function definition
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, 1);
                if (entry != null) {
                    //entry.setLikeToFunction(true);
                }
                return;
            }
            newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, 1);
        }
    }

    private void formatElse(Token<CppTokenId> previous) {
        //spaceBefore(previous, codeStyle.spaceBeforeElse());
        if (ts.isFirstLineToken()) {
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                boolean done = false;
                if (diff.after != null) {
                    diff.after.replaceSpaces(getParentIndent());
                    done = true;
                }
                if (diff.replace != null && previous.id() == WHITESPACE) {
                    if (!done) {
                        diff.replace.replaceSpaces(getParentIndent());
                        done = true;
                    } else {
                        diff.replace.replaceSpaces(0);
                    }
                }
                if (diff.before != null && previous.id() == WHITESPACE){
                    if (!done) {
                        diff.before.replaceSpaces(getParentIndent());
                        done = true;
                    } else {
                        diff.before.replaceSpaces(0);
                    }
                }
                if (done) {
                    return;
                }
            }
            if (previous.id() == WHITESPACE) {
                Token<CppTokenId> p2 = ts.lookPrevious(2);
                if (p2 != null && p2.id()== NEW_LINE) {
                    ts.replacePrevious(previous, 0, getParentIndent());
                } else {
                    ts.replacePrevious(previous, 0, 0);
                }
            } else if (previous.id() == NEW_LINE || previous.id() == PREPROCESSOR_DIRECTIVE) {
                ts.addBeforeCurrent(0, getParentIndent());
            }
        } else if (previous != null) {
            makeSpaceBefore(codeStyle.spaceBeforeElse());
        }
    }

    private void indentRbrace(StackEntry entry, Token<CppTokenId> previous,
                              Token<CppTokenId> current) {
        
        int indent = continuationIndent(entry.getSelfIndent());
        if (previous != null) {
            boolean done = false;
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                if (diff.before != null && previous.id() == WHITESPACE) {
                    diff.before.replaceSpaces(indent); // NOI18N
                    done = true;
                }
                if (diff.replace != null) {
                    if (!done) {
                        diff.replace.replaceSpaces(indent); // NOI18N
                    } else {
                        diff.replace.replaceSpaces(0); // NOI18N
                    }
                    done = true;
                }
                if (diff.after != null) {
                    if (!done) {
                        if (diff.after.hasNewLine() || ts.isFirstLineToken()) {
                            diff.after.replaceSpaces(indent); // NOI18N
                        } else {
                            if (!entry.isLikeToArrayInitialization()) {
                                ts.addBeforeCurrent(1, indent);
                            }
                        }
                    }
                    done = true;
                }
            }
            if (!done) {
                if (previous.id() == WHITESPACE) {
                    if (ts.isFirstLineToken()) {
                        ts.replacePrevious(previous, 0, indent);
                    } else {
                        if (braces.parenDepth <= 0) {
                            ts.replacePrevious(previous, 1, indent);
                        } else {
                            //it a array?
                            // do nothing
                        }
                    }
                } else if (previous.id() == NEW_LINE ||
                           previous.id() == PREPROCESSOR_DIRECTIVE ||
                           previous.id() == ESCAPED_WHITESPACE) {
                    ts.addBeforeCurrent(0, indent);
                } else {
                    if (!entry.isLikeToArrayInitialization()) {
                        ts.addBeforeCurrent(1, indent);
                    }
                }
            }
        }
        boolean isClassDeclaration = entry.getImportantKind() != null &&
                                    (entry.getImportantKind() == CLASS ||
                                     entry.getImportantKind() == STRUCT ||
                                     entry.getImportantKind() == UNION ||
                                     entry.getImportantKind() == ENUM);

        Token<CppTokenId> next = ts.lookNext();
        if (isClassDeclaration) {
            if (next != null && !(next.id() == WHITESPACE || next.id() == NEW_LINE)) {
                ts.addAfterCurrent(current, 0, 1);
            }
            return;
        }
        StackEntry top = braces.peek();
        Token<CppTokenId> nextImportant = ts.lookNextImportant();
        if (nextImportant != null) {
            switch (nextImportant.id()) {
                case WHILE:
                {
                    if (top != null && top.getKind() == DO) {
                        if (!codeStyle.newLineWhile()) {
                            if (ts.isLastLineToken()) {
                                Token<CppTokenId> n2 = ts.lookNext(2);
                                if (n2 == null || n2.id() != PREPROCESSOR_DIRECTIVE) {
                                    ts.replaceNext(current, next, 0, 0);
                                }
                            }
                        } else {
                            if (!ts.isLastLineToken()) {
                                ts.addAfterCurrent(current, 1, indent);
                            }
                        }
                        return;
                    }
                    break;
                }
                case CATCH:
                {
                    if (top != null &&
                        (top.getKind() == TRY || top.getKind() == CATCH)) {
                        if (!codeStyle.newLineCatch()) {
                            if (ts.isLastLineToken()) {
                                Token<CppTokenId> n2 = ts.lookNext(2);
                                if (n2 == null || n2.id() != PREPROCESSOR_DIRECTIVE) {
                                    ts.replaceNext(current, next, 0, 0);
                                }
                            }
                        } else {
                            if (!ts.isLastLineToken()) {
                                ts.addAfterCurrent(current, 1, indent);
                            }
                        }
                        return;
                    }
                    break;
                }
                case ELSE:
                {
                    if (!codeStyle.newLineElse()) {
                        if (ts.isLastLineToken()) {
                            Token<CppTokenId> n2 = ts.lookNext(2);
                            if (n2 == null || n2.id() != PREPROCESSOR_DIRECTIVE) {
                                ts.replaceNext(current, next, 0, 0);
                            }
                        }
                    } else {
                        if (!ts.isLastLineToken()) {
                            ts.addAfterCurrent(current, 1, indent);
                        }
                    }
                    return;
                }
            }
        }
        next = ts.lookNextLineImportant();
        if (next != null && !(next.id() == RPAREN || next.id() == COMMA || next.id() == SEMICOLON || next.id() == NEW_LINE)) {
            ts.addAfterCurrent(current, 1, indent);
        }
    }

    private void newLineFormat(Token<CppTokenId> previous, Token<CppTokenId> current, int parenDepth) {
        if (previous != null) {
            boolean done = false;
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                if (diff.after != null) {
                    diff.after.replaceSpaces(0); // NOI18N
                    if (diff.replace != null){
                        diff.replace.replaceSpaces(0); // NOI18N
                    }
                    done = true;
                } else if (diff.replace != null) {
                    diff.replace.replaceSpaces(0); // NOI18N
                    done = true;
                }
            }
            if (!done && previous.id() == WHITESPACE) {
                ts.replacePrevious(previous, 0, 0);
            }
        }
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (next.id() == NEW_LINE) {
                return;
            }
            int space = -1;
            if (parenDepth > 0) {
                // get indent from left paren indent
                Token<CppTokenId> prev = ts.findOpenParenToken(parenDepth);
                if (prev != null) {
                    switch (prev.id()){
                        case IDENTIFIER:
                        {
                            if (braces.isDeclarationLevel()){
                                if (codeStyle.alignMultilineMethodParams()){
                                    int i = ts.openParenIndent(parenDepth);
                                    if (i >=0) {
                                        space = i;
                                    }
                                }
                            } else {
                                if (codeStyle.alignMultilineCallArgs()){
                                    int i = ts.openParenIndent(parenDepth);
                                    if (i >=0) {
                                        space = i;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if (space == -1) {
                Token<CppTokenId> first = ts.lookNextLineImportant();
                if (first != null && braces.getStatementContinuation()!=BracesStack.StatementContinuation.STOP) {
                    switch (first.id()) {
                        case CASE:
                        case DEFAULT:
                        case FOR:
                        case IF:
                        case ELSE:
                        case DO:
                        case WHILE:
                        case SWITCH:
                        case TRY:
                        case CATCH:
                        case BREAK:
                        case RETURN:
                        case CONTINUE:
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            braces.lastStatementStart = -1;
                    }
                }
                if (first != null && (first.id() == CASE ||first.id() == DEFAULT)){
                    space = getCaseIndent();
                }
                if (braces.isDoWhile && first != null && first.id() == WHILE){
                    space = getParentIndent();
                }
            }
            if (space == -1) {
                space = getIndent();
            }
            if (next.id() == WHITESPACE) {
                ts.replaceNext(current, next, 0, space);
            } else {
                if (space > 0) {
                    ts.addAfterCurrent(current, 0, space);
                }
            }
        }
    }

    // indent new line after preprocessor directive
    private void indentNewLine(Token<CppTokenId> current){
        if (current.id() == NEW_LINE) {
            return;
        }
        int space;
        Token<CppTokenId> first = ts.lookNextLineImportant();
        if (first != null && (first.id() == CASE ||first.id() == DEFAULT)){
            space = getCaseIndent();
        } else {
            space = getIndent();
        }
        if (current.id() == WHITESPACE) {
            ts.replaceCurrent(current, 0, space);
        } else {
            ts.addBeforeCurrent(0, space);
        }
    }

    private void reformatBlockComment(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (!ts.isFirstLineToken()){
            // do not format block comments inside cole line
            return;
        }
        int tab = codeStyle.getGlobalTabSize();
        if (tab <= 1) {
            tab = 4;
        }
        int originalIndent = 0;
        if (previous == null || previous.id() == NEW_LINE || previous.id() == PREPROCESSOR_DIRECTIVE){
            originalIndent = 0;
        } else if (previous.id()==WHITESPACE) {
            CharSequence s = previous.text();
            for (int i = 0; i < previous.length(); i++) {
                if (s.charAt(i) == ' '){ // NOI18N
                    originalIndent++;
                } else if (s.charAt(i) == '\t'){ // NOI18N
                    originalIndent = (originalIndent/tab+1)*tab;
                }
            }
        }
        int requiredIndent = getIndent();
        int start = -1;
        int end = -1;
        int currentIndent = 0;
        CharSequence s = current.text();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') { // NOI18N
                start = i;
                end = i;
                currentIndent = 0;
            } else if (s.charAt(i) == ' ' || s.charAt(i) == '\t') { // NOI18N
                end = i;
                if (s.charAt(i) == ' '){ // NOI18N
                    currentIndent++;
                } else if (s.charAt(i) == '\t'){ // NOI18N
                    currentIndent = (currentIndent/tab+1)*tab;
                }
            } else {
                if (start >= 0) {
                    addCommentIndent(start, end, s.charAt(i), requiredIndent, originalIndent, currentIndent);
                }
                start = -1;
            }
        }
        addCommentIndent(start, end, '*', requiredIndent, originalIndent, currentIndent); // NOI18N
    }
    
    private void addCommentIndent(int start, int end, char c, int requiredIndent, int originalIndent, int currentIndent) {
        if (start >= 0 && end >= start) {
            if (c == '*') { // NOI18N
                diffs.addFirst(ts.offset() + start + 1, ts.offset() + end + 1, 0, 1 + requiredIndent);
            } else {
                int indent = requiredIndent + currentIndent - originalIndent;
                if (indent < 0) {
                    indent = requiredIndent;
                }
                diffs.addFirst(ts.offset() + start + 1, ts.offset() + end + 1, 0, indent); 
            }
        }
    }

    private void whiteSpaceFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null) {
            DiffResult diff = diffs.getDiffs(ts, 0);
            if (diff != null) {
                if (diff.replace != null) {
                    return;
                }
                if (diff.before != null){
                    ts.replaceCurrent(current, 0, 0);
                    return;
                }
            }
            if (previous.id() == NEW_LINE ||
                previous.id() == PREPROCESSOR_DIRECTIVE) {
                // already formatted
                return;
            }
        }
        Token<CppTokenId> next = ts.lookNext();
        if (next != null && next.id() == NEW_LINE) {
            // will be formatted on new line
            return;
        }
        if (previous == null) {
            ts.replaceCurrent(current, 0, 0);
        } else {
            ts.replaceCurrent(current, 0, 1);
        }
    }

    private void newLine(Token<CppTokenId> previous, Token<CppTokenId> current,
            CodeStyle.BracePlacement where, boolean spaceBefore, int newLineAfter){
        if (where == CodeStyle.BracePlacement.NEW_LINE) {
            newLineBefore();
        } else if (where == CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED) {
            newLineBefore();
        } else if (where == CodeStyle.BracePlacement.SAME_LINE) {
            if (ts.isFirstLineToken()){
                if (!removeLineBefore(spaceBefore)){
                    newLineBefore();
                }
            } else {
                spaceBefore(previous, spaceBefore);
            }
        }
        if (newLineAfter>0){
            if (ts.isLastLineToken()) {
                if (newLineAfter>1) {
                    ts.addAfterCurrent(current, newLineAfter-1, getIndent());
                }
            } else {
                ts.addAfterCurrent(current, newLineAfter, getIndent());
            }
        }
    }

    private void newLinesBeforeDeclaration(int lines, int start) {
        int index = ts.index();
        int[] segment = ts.getNewLinesBeforeDeclaration(start);
        try {
            if (segment[0] == -1) {
                if (start <= 0) {
                    return;
                }
                ts.moveIndex(start);
                ts.moveNext();
                ts.addBeforeCurrent(lines, 0); // NOI18N
            } else {
                if (segment[0] == 0) {
                    return;
                }
                ts.moveIndex(start);
                ts.moveNext();
                int indent = ts.getTokenPosition();
                ts.moveIndex(segment[0]);
                Diff toReplace = null;
                while (ts.moveNext()) {
                    if (ts.index() > segment[1]) {
                        break;
                    }
                    DiffResult diff = diffs.getDiffs(ts, 0);
                    if (diff != null) {
                        if (diff.replace != null) {
                            diff.replace.setText(0,0); // NOI18N

                            if (toReplace == null) {
                                toReplace = diff.replace;
                            }
                        } else {
                            //if (!(ts.token().id() == WHITESPACE ||
                            //    ts.token().id() == NEW_LINE)) {
                            //    System.out.println("Replace token "+ts.token().text());
                            //    ts.getNewLinesBeforeDeclaration(start);
                            //}
                            if (toReplace == null) {
                                toReplace = ts.replaceCurrent(ts.token(), 0, 0);
                            } else {
                                ts.replaceCurrent(ts.token(), 0, 0);
                            }
                        }
                        if (diff.before != null) {
                            diff.before.setText(0, 0); // NOI18N

                            if (toReplace == null) {
                                toReplace = diff.replace;
                            }
                        }
                    } else {
                        //if (!(ts.token().id() == WHITESPACE ||
                        //    ts.token().id() == NEW_LINE)) {
                        //    System.out.println("Replace token "+ts.token().text());
                        //    ts.getNewLinesBeforeDeclaration(start);
                        //}
                        if (toReplace == null) {
                            toReplace = ts.replaceCurrent(ts.token(), 0, 0);
                        } else {
                            ts.replaceCurrent(ts.token(), 0, 0);
                        }
                    }
                }
                if (toReplace != null) {
                    toReplace.setText(lines+segment[2], indent);
                } else {
                    ts.moveIndex(segment[0]);
                    ts.moveNext();
                    if (ts.token().id() == WHITESPACE ||
                        ts.token().id() == NEW_LINE) {
                        ts.replaceCurrent(ts.token(), lines+segment[2], indent);
                    } else {
                        ts.addBeforeCurrent(lines+segment[2], indent);
                    }
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    private void newLineBefore() {
        if (!ts.isFirstLineToken()) {
           Token<CppTokenId> previous = ts.lookPrevious();
           if (previous != null && previous.id() == WHITESPACE) {
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (diff != null) {
                    if (diff.after != null) {
                        diff.after.setText(1, getParentIndent());
                        if (diff.replace != null){
                            diff.replace.setText(0, 0);
                        }
                        return;
                    } else if (diff.replace != null) {
                        diff.replace.setText(1, getParentIndent());
                        return;
                    }
                }
               ts.replacePrevious(previous, 1, getParentIndent());
           } else {
               ts.addBeforeCurrent(1, getParentIndent());
           }
        } else {
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                if (diff.after != null) {
                    diff.after.replaceSpaces(getParentIndent());
                    if (diff.replace != null){
                        diff.replace.replaceSpaces(0);
                    }
                    return;
                } else if (diff.replace != null) {
                    diff.replace.replaceSpaces(getParentIndent());
                    if (diff.before != null) {
                        diff.before.replaceSpaces(0);
                    }
                    return;
                }
            }
            Token<CppTokenId> previous = ts.lookPrevious();
            if (previous != null) {
                if (previous.id() == WHITESPACE) {
                    ts.replacePrevious(previous, 0, getParentIndent());
                } else if (previous.id() == NEW_LINE) {
                    ts.addBeforeCurrent(0, getParentIndent());
                }
            }
        }
    }

    private void spaceBefore(Token<CppTokenId> previous, boolean add){
        if (previous != null && !ts.isFirstLineToken()) {
            if (add) {
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (diff != null) {
                    if (diff.after != null && !diff.after.hasNewLine()) {
                        diff.after.replaceSpaces(1);
                        if (diff.replace != null && !diff.replace.hasNewLine()){
                            diff.replace.replaceSpaces(0);
                        }
                        return;
                    } else if (diff.replace != null && !diff.replace.hasNewLine()) {
                        diff.replace.replaceSpaces(1);
                        return;
                    }
                }
                if (!(previous.id() == WHITESPACE ||
                      previous.id() == NEW_LINE ||
                      previous.id() == PREPROCESSOR_DIRECTIVE)) {
                    ts.addBeforeCurrent(0, 1);
                }
            } else if (canRemoveSpaceBefore(previous)){
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (diff != null) {
                    if (diff.after != null && !diff.after.hasNewLine()) {
                        diff.after.replaceSpaces(0);
                        if (diff.replace != null && !diff.replace.hasNewLine()){
                            diff.replace.replaceSpaces(0);
                        }
                        return;
                    } else if (diff.replace != null && !diff.replace.hasNewLine()) {
                        diff.replace.replaceSpaces(0);
                        return;
                    }
                }
                if (previous.id() == WHITESPACE) {
                    ts.replacePrevious(previous, 0, 0);
                }
            }
        }
    }

    private boolean canRemoveSpaceBefore(Token<CppTokenId> previous){
        if (previous == null) {
            return false;
        }
        if (previous.id() == WHITESPACE) {
            Token<CppTokenId> p2 = ts.lookPrevious(2);
            if (p2 == null) {
                return true;
            }
            previous = p2;
        }
        CppTokenId prev = previous.id();
        CppTokenId curr = ts.token().id();
        return canRemoveSpace(prev,curr);
    }

    private boolean canRemoveSpace(CppTokenId prev, CppTokenId curr){
        if (prev == IDENTIFIER && curr == IDENTIFIER) {
            return false;
        }
        String currCategory = curr.primaryCategory();
        String prevCategory = prev.primaryCategory();
        if (KEYWORD_CATEGORY.equals(prevCategory) ||
            KEYWORD_DIRECTIVE_CATEGORY.equals(prevCategory)) {
            if (SEPARATOR_CATEGORY.equals(currCategory)) {
                return true;
            } else if (curr == COLON) {
                return true;
            } else if (prev == OPERATOR) {
                return true;
            }
            return false;
        } else if (OPERATOR_CATEGORY.equals(prevCategory)) {
            if (OPERATOR_CATEGORY.equals(currCategory)) {
                return false;
            }
            return true;
        } else if (prev == IDENTIFIER) {
            if (NUMBER_CATEGORY.equals(currCategory) ||
                LITERAL_CATEGORY.equals(currCategory) ||
                CHAR_CATEGORY.equals(currCategory) ||
                STRING_CATEGORY.equals(currCategory)) {
                return false;
            }
        }
        return true;
    }

    private boolean canRemoveSpaceAfter(Token<CppTokenId> current){
        Token<CppTokenId> next = ts.lookPrevious();
        if (next == null) {
            return false;
        }
        if (next.id() == WHITESPACE) {
            Token<CppTokenId> n2 = ts.lookNext(2);
            if (n2 == null) {
                return true;
            }
            next = n2;
        }
        CppTokenId curr = next.id();
        CppTokenId prev = current.id();
        return canRemoveSpace(prev,curr);
    }
    
    private void spaceAfter(Token<CppTokenId> current, boolean add){
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (add) {
                if (!(next.id() == WHITESPACE ||
                      next.id() == NEW_LINE)) {
                    ts.addAfterCurrent(current, 0, 1);
                }
            } else if (canRemoveSpaceAfter(current)){
                if (next.id() == WHITESPACE) {
                    Token<CppTokenId> n2 = ts.lookNext(2);
                    if (n2 == null || !OPERATOR_CATEGORY.equals(n2.id().primaryCategory())){
                        ts.replaceNext(current, next, 0, 0);
                    }
                }
            }
        }
    }

    private void spaceAfterBefore(Token<CppTokenId> current, boolean add, CppTokenId before){
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (next.id() == WHITESPACE) {
                Token<CppTokenId> p = ts.lookNext(2);
                if (p!=null && p.id()==before) {
                    if (!add) {
                        ts.replaceNext(current, next, 0, 0); // NOI18N
                    }
                }
            } else if (next.id() == before) {
                if (add) {
                    ts.addAfterCurrent(current, 0, 1);
                }
            }
        }
    }

    private void formatLeftParen(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null){
            Token<CppTokenId> p = ts.lookPreviousStatement();
            if (p != null) {
                switch(p.id()) {
                    case IF:
                        spaceAfter(current, codeStyle.spaceWithinIfParens());
                        return;
                    case FOR:
                        spaceAfter(current, codeStyle.spaceWithinForParens());
                        return;
                    case WHILE:
                        spaceAfter(current, codeStyle.spaceWithinWhileParens());
                        return;
                    case SWITCH:
                        spaceAfter(current, codeStyle.spaceWithinSwitchParens());
                        return;
                    case CATCH:
                        spaceAfter(current, codeStyle.spaceWithinCatchParens());
                        return;
                }
            }
            p = ts.lookPreviousImportant();
            if (p != null && p.id() == IDENTIFIER) {
                StackEntry entry = braces.peek();
                if (entry == null){
                    spaceBefore(previous, codeStyle.spaceBeforeMethodDeclParen());
                    spaceAfter(current, codeStyle.spaceWithinMethodDeclParens());
                    return;
                }
                if (entry.getImportantKind() != null) {
                    switch (entry.getImportantKind()) {
                        case CLASS:
                        case NAMESPACE:
                            spaceBefore(previous, codeStyle.spaceBeforeMethodDeclParen());
                            spaceAfter(current, codeStyle.spaceWithinMethodDeclParens());
                            return;
                    }
                }
                spaceBefore(previous, codeStyle.spaceBeforeMethodCallParen());
                spaceAfter(current, codeStyle.spaceWithinMethodCallParens());
                return;
            } else if (ts.isTypeCast()){
                spaceAfter(current, codeStyle.spaceWithinTypeCastParens());
                return;
            } else {
                spaceAfter(current, codeStyle.spaceWithinParens());
            }
        }
    }

    private void formatRightParen(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null){
            Token<CppTokenId> p = ts.lookPreviousStatement();
            if (p != null) {
                switch(p.id()) {
                    case IF:
                        spaceBefore(previous, codeStyle.spaceWithinIfParens());
                        return;
                    case FOR:
                        spaceBefore(previous, codeStyle.spaceWithinForParens());
                        return;
                    case WHILE:
                        spaceBefore(previous, codeStyle.spaceWithinWhileParens());
                        return;
                    case SWITCH:
                        spaceBefore(previous, codeStyle.spaceWithinSwitchParens());
                        return;
                    case CATCH:
                        spaceBefore(previous, codeStyle.spaceWithinCatchParens());
                        return;
                }
            }
            p = getImportantBeforeBrace();
            if (p != null && p.id() == IDENTIFIER) {
                StackEntry entry = braces.peek();
                if (entry == null){
                    spaceBefore(previous, codeStyle.spaceWithinMethodDeclParens());
                    return;
                }
                if (entry.getImportantKind() != null) {
                    switch (entry.getImportantKind()) {
                        case CLASS:
                        case NAMESPACE:
                            spaceBefore(previous, codeStyle.spaceWithinMethodDeclParens());
                            return;
                    }
                }
                spaceBefore(previous, codeStyle.spaceWithinMethodCallParens());
                return;
            } else if (ts.isTypeCast()){
                spaceBefore(previous, codeStyle.spaceWithinTypeCastParens());
                spaceAfter(current, codeStyle.spaceAfterTypeCast());
                return;
            } else {
                spaceBefore(previous, codeStyle.spaceWithinParens());
            }
        }
    }

    private Token<CppTokenId> getImportantBeforeBrace(){
        int index = ts.index();
        try {
            if (ts.token().id() == RPAREN) {
                int depth = 1;
                while (ts.movePrevious()) {
                    switch (ts.token().id()) {
                        case RPAREN:
                            depth++;
                            break;
                        case LPAREN:
                        {
                            depth--;
                            if (depth <=0) {
                                return ts.lookPreviousImportant();
                            }
                            break;
                        }
                    }
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    private boolean isOperator(){
        Token<CppTokenId> p = ts.lookPreviousImportant(1);
        return p != null && p.id() == OPERATOR;
    }

    
    // <importantFrom><WS><NL><WS><importantTo>
    // where <NL> replaced on nonNL
    // indexTo point to importantTo
    // importantTo shoul be not first line token
    // method removes chain <WS><NL><WS> or replaces it to on space
    private boolean makeSpaceBefore(boolean addSpace){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return false;
                }
                if (ts.token().id() == NEW_LINE){
                    DiffResult diff = diffs.getDiffs(ts, 0);
                    if (diff.replace == null || diff.replace.hasNewLine()){
                        return false;
                    }
                } else if (ts.token().id() == PREPROCESSOR_DIRECTIVE){
                    return false;
                } else if (ts.token().id() != WHITESPACE){
                    replaceSegment(addSpace, index);
                    return true;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    private boolean removeLineBefore(boolean addSpace){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return false;
                }
                if (ts.token().id() == NEW_LINE){
                    if (ts.movePrevious()) {
                        if (ts.token().id() == WHITESPACE) {
                            ts.movePrevious();
                            replaceSegment(addSpace, index);
                            return true;
                        } else if (ts.token().id() != LINE_COMMENT) {
                            replaceSegment(addSpace, index);
                            return true;
                        }
                    }
                    return false;
                } else if (ts.token().id() == PREPROCESSOR_DIRECTIVE){
                    return false;
                } else if (ts.token().id() != WHITESPACE){
                    replaceSegment(addSpace, index);
                    return true;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    // <importantFrom><WS><NL><WS><importantTo>
    // current ts point to importantFrom
    // indexTo point to importantTo
    // method removes chain <WS><NL><WS> or replaces it to on space
    private void replaceSegment(boolean addSpace, int indexTo) {
        boolean first = true;
        Diff diffToSpace = null;
        while (ts.index() < indexTo) {
            DiffResult diff = diffs.getDiffs(ts, 0);
            if (diff != null) {
                if (!first) {
                    if (diff.replace != null) {
                        if (diffToSpace == null) {
                            diffToSpace = diff.replace;
                        }
                        diff.replace.setText(0, 0);
                    } else {
                        Diff added = diffs.addFirst(ts.offset(), ts.offset()+ts.token().length(), 0, 0);
                        if (diffToSpace == null) {
                            diffToSpace = added;
                        }
                    }
                }
                if (diff.after != null) {
                    if (diffToSpace == null) {
                        diffToSpace = diff.after;
                    }
                    diff.after.setText(0, 0);
                }
            }
            if (!first && diff == null) {
                Diff added = diffs.addFirst(ts.offset(), ts.offset() + ts.token().length(), 0, 0);
                if (diffToSpace == null) {
                    diffToSpace = added;
                }
            }
            first = false;
            ts.moveNext();
        }
        if (diffToSpace != null && addSpace){
            diffToSpace.setText(0, 1);
        }
    }
    
    /*package local*/ boolean doFormat(){
        return ts.offset() >= this.startOffset;
    }
}
