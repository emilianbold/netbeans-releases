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
import org.netbeans.modules.cnd.editor.reformat.Reformatter.Diff;

/**
 *
 * @author Alexander Simon
 */
public class ReformatterImpl {
    /*package local*/ final ExtendedTokenSequence ts;
    /*package local*/ final CodeStyle codeStyle;
    /*package local*/ final DiffLinkedList diffs = new DiffLinkedList();
    private final int startOffset;
    private final int endOffset;
    private BracesStack braces = new BracesStack();
    private PreprocessorFormatter preprocessorFormatter;
    
    ReformatterImpl(TokenSequence<CppTokenId> ts, int startOffset, int endOffset, CodeStyle codeStyle){
        this.ts = new ExtendedTokenSequence(ts, diffs);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.codeStyle = codeStyle;
        preprocessorFormatter = new PreprocessorFormatter(this);
    }
    
    LinkedList<Diff> reformat(){
        ts.moveStart();
        Token<CppTokenId> previous = ts.lookPrevious();
        int parenDepth = 0;
        while(ts.moveNext()){
            if (ts.offset() > endOffset) {
                break;
            }
            Token<CppTokenId> current = ts.token();
            CppTokenId id = current.id();
            if (previous != null && previous.id() == PREPROCESSOR_DIRECTIVE && id != PREPROCESSOR_DIRECTIVE){
                // indent afre preprocessor directive
                if (braces.getStatementContinuation() == BracesStack.StatementContinuetion.START){
                    braces.setStatementContinuation(BracesStack.StatementContinuetion.CONTINUE);
                }
                if (doFormat()){
                    indentNewLine(current);
                }
            }
            switch(id){
                case PREPROCESSOR_DIRECTIVE: //(null, "preprocessor"),
                {
                    preprocessorFormatter.indentPreprocessor(previous);
                    break;
                }
                case NEW_LINE:
                {
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuetion.START){
                        braces.setStatementContinuation(BracesStack.StatementContinuetion.CONTINUE);
                    }
                    if (doFormat()) {
                        newLineFormat(previous, current, parenDepth);
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
                case BLOCK_COMMENT:
                {
                    if (doFormat()) {
                        reformatBlockComment(current);
                    }
                    break;
                }
                case LBRACE: //("{", "separator"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        braceFormat(previous, current);
                    }
                    break;
                }
                case LPAREN: //("(", "separator"),
                {
                    if (parenDepth == 0) {
                        if (braces.getStatementContinuation() == BracesStack.StatementContinuetion.STOP) {
                            braces.setStatementContinuation(BracesStack.StatementContinuetion.START);
                        }
                    }
                    parenDepth++;
                    if (doFormat()) {
                        formatLeftParen(previous, current);
                    }
                    break;
                }
                case RPAREN: //(")", "separator"),
                {
                    parenDepth--;
                    if (parenDepth == 0) {
                        braces.setStatementContinuation(BracesStack.StatementContinuetion.STOP);
                    }
                    if (doFormat()) {
                        formatRightParen(previous, current);
                    }
                    break;
                }
                case IDENTIFIER:
                {
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuetion.STOP) {
                        braces.setStatementContinuation(BracesStack.StatementContinuetion.START);
                    }
                    break;
                }
                case SEMICOLON: //(";", "separator"),
                {
                    if (parenDepth == 0) {
                        braces.pop(ts);
                    }
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceBeforeSemi());
                        if (parenDepth == 0) {
                            Token<CppTokenId> next = ts.lookNext();
                            if (next != null) {
                                Token<CppTokenId> n2 = ts.lookNext(2);
                                if (!(next.id() == NEW_LINE ||
                                      next.id() == LINE_COMMENT ||
                                      next.id() == WHITESPACE && n2 != null && n2.id() == LINE_COMMENT)){
                                    ts.addAfterCurrent(current, getIndent("\n")); // NOI18N
                                    break;
                                }    
                            }
                        }
                        spaceAfter(current, codeStyle.spaceAfterSemi());
                    }
                    if (parenDepth == 0) {
                        braces.setStatementContinuation(BracesStack.StatementContinuetion.STOP);
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
                                    newLineBefore();
                                    break;
                            }
                        }
                    }
                    break;
                }
                case COLON: //(":", "operator"),
                {
                    if (doFormat()) {
                        Token<CppTokenId> p = ts.lookPreviousImportant();
                        if (p != null && 
                           (p.id() == PRIVATE ||
                            p.id() == PROTECTED ||
                            p.id() == PUBLIC)) {
                            if(!ts.isLastLineToken()){
                                ts.addAfterCurrent(current, getIndent("\n")); // NOI18N
                            }
                            break;
                        }
                        if (p != null && p.id() == DEFAULT) {
                            braces.setStatementContinuation(BracesStack.StatementContinuetion.STOP);
                            break;
                        }
                        Token<CppTokenId> p2 = ts.lookPreviousImportant(2);
                        if (p2 != null && p2.id() == CASE) {
                            braces.setStatementContinuation(BracesStack.StatementContinuetion.STOP);
                            break;
                        }
                        spaceBefore(previous, codeStyle.spaceBeforeColon());
                        spaceAfter(current, codeStyle.spaceAfterColon());
                    }
                    break;
                }
                case RBRACE: //("}", "separator"),
                {
                    int indent = braces.pop(ts);
                    if (doFormat()) {
                        if (previous != null) {
                            Diff diff = diffs.getFirst();
                            if (diff != null && diff.getEndOffset() == ts.offset()){
                                diff.replaceSpaces(getIndent("", indent)); // NOI18N
                            } else {
                                if (previous.id()== WHITESPACE){
                                    ts.replacePrevious(previous, getIndent("\n", indent)); // NOI18N
                                } else if (previous.id() == NEW_LINE ||
                                           previous.id() == PREPROCESSOR_DIRECTIVE) {
                                    ts.addBeforeCurrent(getIndent("", indent)); // NOI18N
                                }
                            }
                        }
                        Token<CppTokenId> next = ts.lookNext();
                        if (next != null && 
                            !(next.id() == SEMICOLON ||
                              next.id() == NEW_LINE)){
                            ts.addAfterCurrent(current, getIndent("\n", indent)); // NOI18N
                        }
                    }
                    break;
                }
                case NOT: //("!", "operator"),
                case TILDE: //("~", "operator"),
                case PLUSPLUS: //("++", "operator"),
                case MINUSMINUS: //("--","operator"),
                {
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                        spaceAfter(current, codeStyle.spaceAroundUnaryOps());
                    }
                    break;
                }
                case PLUS: //("+", "operator"),
                case MINUS: //("-", "operator"),
                {
                    if (doFormat()) {
                        if (isBynaryOperator(current)){
                            spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                            spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                        } else{
                            spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                            spaceAfter(current, codeStyle.spaceAroundUnaryOps());
                        }
                    }
                    break;
                }
                case STAR: //("*", "operator"),
                case AMP: //("&", "operator"),
                {
                    if (doFormat()) {
                        if (isBynaryOperator(current)){
                            spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                            spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                        } else{
                            //TOTO style of type declaration
                            //spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                            //spaceAfter(current, codeStyle.spaceAroundUnaryOps());
                        }
                    }
                    break;
                }
                case GT: //(">", "operator"),
                case LT: //("<", "operator"),
                {
                    if (doFormat()) {
                        if (isBynaryOperator(current)){
                            spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                            spaceAfter(current, codeStyle.spaceAroundBinaryOps());
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
                        spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                        spaceAfter(current, codeStyle.spaceAroundBinaryOps());
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
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuetion.STOP) {
                        braces.setStatementContinuation(BracesStack.StatementContinuetion.START);
                    }
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceAroundAssignOps());
                        spaceAfter(current, codeStyle.spaceAroundAssignOps());
                    }
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuetion.START){
                        braces.setStatementContinuation(BracesStack.StatementContinuetion.CONTINUE);
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
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeIfParen(), LPAREN);
                    }
                    break;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    formatElse(previous);
                    break;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceBeforeWhile());
                        spaceAfterBefore(current, codeStyle.spaceBeforeWhileParen(), LPAREN);
                    }
                    break;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeForParen(), LPAREN);
                    }
                    break;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    braces.push(new StackEntry(ts));
                    break;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceBeforeCatch());
                        spaceAfterBefore(current, codeStyle.spaceBeforeCatchParen(), LPAREN);
                    }
                    break;
                }
                case ASM: //("asm", "keyword-directive"), // gcc and C++
                {
                    braces.push(new StackEntry(ts));
                    break;
                }
                case DO: //("do", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    break;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeSwitchParen(), LPAREN);
                    }
                    break;
                }
                case DEFAULT: //("default", "keyword-directive"),
                case CASE: //("case", "keyword-directive"),
                {
                    braces.setStatementContinuation(BracesStack.StatementContinuetion.STOP);
                    if (doFormat()) {
                        if (ts.isFirstLineToken()){
                            indentCase(previous, current);
                        }
                    }
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
            }
            previous = current;
        }
        return diffs.getStorage();
    }
    
    /*package local*/ String getParentIndent(String prefix) {
        int shift = (braces.getLength() - 1) * codeStyle.getGlobalIndentSize();
        if (shift > 0) {
            return spaces(prefix, shift);
        } else {
            return prefix;
        }
    }

    /*package local*/ String getIndent(String prefix) {
        return getIndent(prefix, braces.getLength());
    }

    /*package local*/ String getIndent(String prefix, int shift) {
        shift = shift * codeStyle.getGlobalIndentSize();
        StackEntry entry = braces.peek();
        if (entry != null) {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuetion.CONTINUE){
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
                    case CATCH: 
                        shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.getGlobalIndentSize();
                        break;
                    case SWITCH: 
                        shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.getGlobalIndentSize();
                        if (codeStyle.indentCasesFromSwitch()) {
                            shift += codeStyle.getGlobalIndentSize();
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
                        if (!entry.isLikeToArrayInitialization()){
                            shift += codeStyle.getFormatStatementContinuationIndent();
                        }
                        break;
                    }
                }
            } else if (entry.getImportantKind() == SWITCH) {
               if (codeStyle.indentCasesFromSwitch()) {
                   shift += codeStyle.getGlobalIndentSize();
               }
            }
        } else {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuetion.CONTINUE){
               shift += codeStyle.getFormatStatementContinuationIndent();
            }
        }
        if (shift > 0) {
            return spaces(prefix, shift);
        } else {
            return prefix;
        }
    }

    /*package local*/ String spaces(String prefix, int length) {
        StringBuilder spaces = new StringBuilder(prefix);
        for(int i = 0; i < length; i++){
            spaces.append(' ');
        }
        return spaces.toString();
    }

    private void braceFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        StackEntry entry = braces.peek();
        if (entry != null && entry.getImportantKind() != null) {
            switch (entry.getImportantKind()) {
                case NAMESPACE: //("namespace", "keyword"), //C++
                {
                    // TODO divide for neamespace
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceNamespace(),
                            codeStyle.spaceBeforeClassDeclLeftBrace());
                    return;
                }
                case CLASS: //("class", "keyword"), //C++
                case STRUCT: //("struct", "keyword"),
                case ENUM: //("enum", "keyword"),
                case UNION: //("union", "keyword"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceClass(),
                            codeStyle.spaceBeforeClassDeclLeftBrace());
                    return;
                }
                case IF: //("if", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeIfLeftBrace());
                    return;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeElseLeftBrace());
                    return;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeSwitchLeftBrace());
                    return;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeWhileLeftBrace());
                    return;
                }
                case DO: //("do", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeDoLeftBrace());
                    return;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeForLeftBrace());
                    return;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeTryLeftBrace());
                    return;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeCatchLeftBrace());
                    return;
                }
            }
        }
        if (entry.isLikeToFunction()) {
            newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceDeclaration(),
                    codeStyle.spaceBeforeMethodDeclLeftBrace());
        } else if (entry.isLikeToArrayInitialization()) {
            newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                    codeStyle.spaceBeforeArrayInitLeftBrace());
        } else {
            // TODO add options
            newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(), true);
        }
    }

    private void formatElse(Token<CppTokenId> previous) {
        if (doFormat()) {
            spaceBefore(previous, codeStyle.spaceBeforeElse());
            if (previous != null) {
                Diff diff = diffs.getFirst();
                if (diff != null && diff.getEndOffset() == ts.offset()) {
                    diff.replaceSpaces(getParentIndent("")); // NOI18N
                } else {
                    if (previous.id() == WHITESPACE) {
                        int len = previous.text().length();
                        diff = diffs.getBeforeFirst();
                        if (diff != null && diff.getEndOffset() == ts.offset()-len) {
                            diff.replaceSpaces(getParentIndent("")); // NOI18N
                        } else {
                            Token<CppTokenId> p2 = ts.lookPrevious(2);
                            if (p2 != null && p2.id()== NEW_LINE) {
                                ts.replacePrevious(previous, getParentIndent(""));
                            } else {
                                ts.replacePrevious(previous, "");
                            }
                        }
                    } else if (previous.id() == NEW_LINE || previous.id() == PREPROCESSOR_DIRECTIVE) {
                        String text = getParentIndent(""); // NOI18N
                        ts.addBeforeCurrent(text);
                    }
                }
            }
        }
    }

    private void newLineFormat(Token<CppTokenId> previous, Token<CppTokenId> current, int parenDepth) {
        if (parenDepth > 0) {
            // get indent from left paren indent
//            Token<CppTokenId> prev = ts.findOpenParenToken(parenDepth);
//            if (prev != null) {
//                switch (prev.id()){
//                    case IDENTIFIER:
//                }
//            }
        }
        if (previous != null) {
            Diff diff = diffs.getFirst();
            if (diff != null && diff.getEndOffset() == ts.offset()) {
                diff.replaceSpaces(""); // NOI18N
            } else if (previous.id() == WHITESPACE) {
                ts.replacePrevious(previous, ""); // NOI18N
            }
        }
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            Token<CppTokenId> n2 = ts.lookNext(2);
            if (n2 != null && n2.id() == NEW_LINE) {
                return;
            }
            String space = getIndent(""); // NOI18N
            if (next.id() == WHITESPACE) {
                ts.replaceNext(current, next, space);
            } else {
                if (space.length() > 0) {
                    ts.addAfterCurrent(current, space);
                }
            }
        }
    }

    // indent new line after preprocessor directive
    private void indentNewLine(Token<CppTokenId> current){
        if (current.id() == NEW_LINE) {
            return;
        }
        String space = getIndent(""); // NOI18N
        if (current.id() == WHITESPACE) {
            ts.replaceCurrent(current, space);
        } else {
            ts.addBeforeCurrent(space);
        }
    }

    private void indentCase(Token<CppTokenId> previous, Token<CppTokenId> current){
        String space = getIndent("",braces.getLength()-1); // NOI18N
        Diff diff = diffs.getFirst();
        if (diff != null && diff.getEndOffset() == ts.offset()) {
            diff.replaceSpaces(space);
            return;
        }
        if (previous.id() == WHITESPACE) {
            ts.replacePrevious(current, space);
        } else {
            if (space.length() > 0) {
                ts.addBeforeCurrent(space);
            }
       }
    }


    private void reformatBlockComment(Token<CppTokenId> current) {
        int start = -1;
        int end = -1;
        CharSequence s = current.text();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                start = i;
            } else if (s.charAt(i) == ' ' || s.charAt(i) == '\t') {
                end = i;
            } else {
                if (start >= 0 && end > start) {
                    String shift = "";
                    if (s.charAt(i) == '*') {
                        shift = " ";
                    }
                    diffs.addFirst(ts.offset() + start + 1, ts.offset() + end + 1, getIndent(shift)); // NOI18N
                }
                start = -1;
            }
        }
    }
    
    private void whiteSpaceFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null && current.length()>1) {
            Diff diff = diffs.getFirst();
            if (diff != null){
                if (diff.getEndOffset() == ts.offset()+current.length()) {
                    // already replaced
                    return;
                } else if (diff.getStartOffset() == ts.offset()){
                    // inserted before
                    ts.replaceCurrent(current, ""); // NOI18N
                    return;
                }
            }
            if (previous.id() == NEW_LINE ||
                previous.id() == PREPROCESSOR_DIRECTIVE) {
                // already formatted
                return;
            }
            Token<CppTokenId> next = ts.lookNext();
            if (next != null && next.id() == NEW_LINE) {
                // will be formatted on new line
                return;
            }
            ts.replaceCurrent(current, " "); // NOI18N
        }
    }

    private void newLine(Token<CppTokenId> previous, Token<CppTokenId> current,
            CodeStyle.BracePlacement where, boolean spaceBefore){
        if (where == CodeStyle.BracePlacement.NEW_LINE) {
            newLineBefore();
        } else if (where == CodeStyle.BracePlacement.SAME_LINE) {
            if (ts.isFirstLineToken()){
                tryRemoveLine(spaceBefore);
            } else {
                spaceBefore(previous, spaceBefore);
            }
        }
        if(!ts.isLastLineToken()){
            ts.addAfterCurrent(current, getParentIndent("\n")); // NOI18N
        }
    }

    private void newLineBefore() {
        if (!ts.isFirstLineToken()) {
           Token<CppTokenId> previous = ts.lookPrevious();
           if (previous != null && previous.id() == WHITESPACE) {
               ts.replacePrevious(previous, getParentIndent("\n")); // NOI18N\
           } else {
               ts.addBeforeCurrent(getParentIndent("\n")); // NOI18N\
           }
        } else {
            Diff diff = diffs.getFirst();
            if (diff != null && diff.getEndOffset() == ts.offset()) {
                diff.replaceSpaces(getParentIndent("")); // NOI18N
            } else {
                Token<CppTokenId> previous = ts.lookPrevious();
                if (previous != null) {
                    if (previous.id() == WHITESPACE) {
                        ts.replacePrevious(previous, getParentIndent(""));
                    } else if (previous.id() == NEW_LINE) {
                        String text = getParentIndent(""); // NOI18N
                        ts.addBeforeCurrent(text);
                    }
                }
            }
        }
    }

    private void spaceBefore(Token<CppTokenId> previous, boolean add){
        if (previous != null) {
            if (add) {
                Diff diff = diffs.getFirst();
                if (diff != null && diff.getEndOffset() == ts.offset() && !diff.hasNewLine()) {
                    if (!(previous.id() == WHITESPACE ||
                             previous.id() == NEW_LINE ||
                             previous.id() == PREPROCESSOR_DIRECTIVE)){
                        diff.replaceSpaces(" ");
                    }
                } else if (!(previous.id() == WHITESPACE ||
                             previous.id() == NEW_LINE ||
                             previous.id() == PREPROCESSOR_DIRECTIVE)) {
                    ts.addBeforeCurrent(" "); // NOI18N
                }
            } else {
                Diff diff = diffs.getFirst();
                if (diff != null && diff.getEndOffset() == ts.offset() && !diff.hasNewLine()) {
                    diff.replaceSpaces("");
                } else if (previous.id() == WHITESPACE && !ts.isFirstLineToken()) {
                    Token<CppTokenId> p2 = ts.lookPrevious(2);
                    if (p2 == null || !OPERATOR_CATEGORY.equals(p2.id().primaryCategory())){
                        ts.replacePrevious(previous, ""); // NOI18N
                    }
                }
            }
        }
    }

    private void spaceAfter(Token<CppTokenId> current, boolean add){
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (add) {
                if (!(next.id() == WHITESPACE ||
                      next.id() == NEW_LINE)) {
                    ts.addAfterCurrent(current, " "); // NOI18N
                }
            } else {
                if (next.id() == WHITESPACE) {
                    Token<CppTokenId> n2 = ts.lookNext(2);
                    if (n2 == null || !OPERATOR_CATEGORY.equals(n2.id().primaryCategory())){
                        ts.replaceNext(current, next, ""); // NOI18N
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
                        ts.replaceNext(current, next, ""); // NOI18N
                    }
                }
            } else if (next.id() == before) {
                if (add) {
                    ts.addAfterCurrent(current, " "); // NOI18N
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
            if (isTypeCast()){
                spaceAfter(current, codeStyle.spaceWithinTypeCastParens());
                return;
            }
            p = ts.lookPreviousImportant();
            if (p != null && p.id() == IDENTIFIER) {
                StackEntry entry = braces.peek();
                if (entry == null){
                    spaceBefore(previous, codeStyle.spaceBeforeMethodDeclParen());
                    return;
                }
                if (entry.getImportantKind() != null) {
                    switch (entry.getImportantKind()) {
                        case CLASS:
                        case NAMESPACE:
                            spaceBefore(previous, codeStyle.spaceBeforeMethodDeclParen());
                            return;
                    }
                }
                spaceBefore(previous, codeStyle.spaceBeforeMethodCallParen());
                return;
            }
        }
    }

    private void formatRightParen(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null){
            Token<CppTokenId> p = ts.lookPreviousStatement();
            if (p != null) {
                switch(p.id()) {
                    case IF:
                        spaceBefore(current, codeStyle.spaceWithinIfParens());
                        return;
                    case FOR:
                        spaceBefore(current, codeStyle.spaceWithinForParens());
                        return;
                    case WHILE:
                        spaceBefore(current, codeStyle.spaceWithinWhileParens());
                        return;
                    case SWITCH:
                        spaceBefore(current, codeStyle.spaceWithinSwitchParens());
                        return;
                    case CATCH:
                        spaceBefore(current, codeStyle.spaceWithinCatchParens());
                        return;
                }
            }
            if (isTypeCast()){
                spaceBefore(current, codeStyle.spaceWithinTypeCastParens());
                spaceAfter(current, codeStyle.spaceAfterTypeCast());
            }
        }
    }

    private boolean isTypeCast() {
        int index = ts.index();
        try {
            boolean findId = false;
            if (ts.token().id() == RPAREN) {
                while (ts.movePrevious()) {
                    switch (ts.token().id()) {
                        case LPAREN:
                        {
                            if (findId) {
                                ts.moveIndex(index);
                                ts.moveNext();
                                Token<CppTokenId> next = ts.lookNextImportant();
                                return next != null && next.id() == IDENTIFIER;
                            }
                            return false;
                        }
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            findId = true;
                            break;
                        case IDENTIFIER:
                            if (findId) {
                                return false;
                            }
                            findId = true;
                            break;
                        case AMP:
                        case STAR:
                        case LBRACKET:
                        case RBRACKET:
                        case WHITESPACE:
                        case NEW_LINE:
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case PREPROCESSOR_DIRECTIVE:
                            break;
                        default:
                            return false;
                    }
                }
            } else if (ts.token().id() == LPAREN) {
                while (ts.moveNext()) {
                    switch (ts.token().id()) {
                        case RPAREN:
                        {
                            if (findId) {
                                Token<CppTokenId> next = ts.lookNextImportant();
                                return next != null && next.id() == IDENTIFIER;
                            }
                            return false;
                        }
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            findId = true;
                            break;
                        case IDENTIFIER:
                            if (findId) {
                                return false;
                            }
                            findId = true;
                            break;
                        case AMP:
                        case STAR:
                        case LBRACKET:
                        case RBRACKET:
                        case WHITESPACE:
                        case NEW_LINE:
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case PREPROCESSOR_DIRECTIVE:
                            break;
                        default:
                            return false;
                    }
                }
            }
            return false;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    private void tryRemoveLine(boolean addSpace){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return;
                }
                if (ts.token().id() == NEW_LINE){
                    if (ts.movePrevious()) {
                        if (ts.token().id() != LINE_COMMENT) {
                            while (true){
                                ts.moveNext();
                                if (ts.index()>=index) {
                                    return;
                                }
                                if (ts.token().id() == NEW_LINE && addSpace){
                                    ts.replaceCurrent(ts.token(), " "); // NOI18N
                                } else {
                                    ts.replaceCurrent(ts.token(), ""); // NOI18N
                                }
                            }
                        }
                    }
                } else if (ts.token().id() != WHITESPACE){
                    return;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    private boolean isBynaryOperator(Token<CppTokenId> current){
        Token<CppTokenId> previous = ts.lookNextImportant();
        Token<CppTokenId> next = ts.lookNextImportant();
        if (previous != null && next != null) {
            String prevCategory = previous.id().primaryCategory();
            if (KEYWORD_CATEGORY.equals(prevCategory) ||
                SEPARATOR_CATEGORY.equals(prevCategory)){
                return false;
            }
            if (NUMBER_CATEGORY.equals(prevCategory) ||
                LITERAL_CATEGORY.equals(prevCategory) ||
                CHAR_CATEGORY.equals(prevCategory) ||
                STRING_CATEGORY.equals(prevCategory)){
                return true;
            }
            String nextCategory = next.id().primaryCategory();
            if (KEYWORD_CATEGORY.equals(nextCategory)){
                return false;
            }
            if (previous.id() == IDENTIFIER){
                if (NUMBER_CATEGORY.equals(nextCategory) ||
                    LITERAL_CATEGORY.equals(nextCategory) ||
                    CHAR_CATEGORY.equals(nextCategory) ||
                    STRING_CATEGORY.equals(nextCategory)){
                    return true;
                }
                if (next.id() == RPAREN){
                    // TODO need detect that previous ID is not type
                    return true;
                }
                if (OPERATOR_CATEGORY.equals(nextCategory) ||
                    SEPARATOR_CATEGORY.equals(nextCategory)){
                    return false;
                }
                if (next.id() == IDENTIFIER) {
                    // TODO need detect that previous ID is not type
                    return !isLikeDeclarationOrLvalue();
                }
            }
        }
        return false;
    }
    
    private boolean isLikeDeclarationOrLvalue(){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    case COMMA:
                    case SEMICOLON:
                    case EQ:
                        return true;
                }
            }
            return false;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    /*package local*/ boolean doFormat(){
        return ts.offset() >= this.startOffset;
    }
}
