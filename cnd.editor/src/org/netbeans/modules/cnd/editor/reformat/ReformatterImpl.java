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
    /*package local*/ final TokenSequence<CppTokenId> ts;
    /*package local*/ final CodeStyle codeStyle;
    /*package local*/ final DiffLinkedList diffs = new DiffLinkedList();
    private final int startOffset;
    private final int endOffset;
    private BracesStack braces = new BracesStack();
    private PreprocessorFormatter preprocessorFormatter;
    
    ReformatterImpl(TokenSequence<CppTokenId> ts, int startOffset, int endOffset, CodeStyle codeStyle){
        this.ts = ts;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.codeStyle = codeStyle;
        preprocessorFormatter = new PreprocessorFormatter(this);
    }
    
    LinkedList<Diff> reformat(){
        ts.move(startOffset);
        Token<CppTokenId> previous = lookPrevious();
        int parenDepth = 0;
        while(ts.moveNext()){
            if (ts.offset() > endOffset) {
                break;
            }
            Token<CppTokenId> current = ts.token();
            CppTokenId id = current.id();
            switch(id){
                case PREPROCESSOR_DIRECTIVE: //(null, "preprocessor"),
                {
                    preprocessorFormatter.indentPreprocessor(previous);
                    break;
                }
                case NEW_LINE:
                {
                    newLineFormat(previous, current);
                    break;
                }
                case LBRACE: //("{", "separator"),
                {
                    braceFormat(previous, current);
                    break;
                }
                case LPAREN: //("(", "separator"),
                {
                    parenDepth++;
                    formatLeftParen(previous, current);
                    break;
                }
                case RPAREN: //(")", "separator"),
                {
                    parenDepth--;
                    formatRightParen(previous, current);
                    break;
                }
                case SEMICOLON: //(";", "separator"),
                {
                    spaceBefore(previous, codeStyle.spaceBeforeSemi());
                    if (parenDepth == 0) {
                        braces.pop(ts);
                        Token<CppTokenId> next = lookNext();
                        if (next != null) {
                            if (!(next.id() == NEW_LINE ||
                                next.id() == LINE_COMMENT)){
                                diffs.addFirst(ts.offset()+current.length(),
                                               ts.offset()+current.length(), getIndent("\n"));
                                break;
                            }    
                        }
                    }
                    spaceAfter(current, codeStyle.spaceAfterSemi());
                    break;
                }
                case COMMA: //(",", "separator"),
                {
                    spaceBefore(previous, codeStyle.spaceBeforeComma());
                    spaceAfter(current, codeStyle.spaceAfterComma());
                    break;
                }
                case PRIVATE:
                case PROTECTED:
                case PUBLIC:
                {
                    StackEntry entry = braces.peek();
                    if (entry != null && entry.getImportantKind() != null){
                        switch (entry.getImportantKind()) {
                            case CLASS: //("class", "keyword"), //C++
                            case STRUCT: //("struct", "keyword"),
                                newLineBefore();
                                break;
                        }
                    }
                    break;
                }
                case COLON: //(":", "operator"),
                {
                    if (previous != null && 
                       (previous.id() == PRIVATE ||
                        previous.id() == PROTECTED ||
                        previous.id() == PUBLIC)) {
                        if(!isLastLineToken()){
                            diffs.addFirst(ts.offset()+current.length(),
                                           ts.offset()+current.length(), getIndent("\n"));
                            break;
                        }
                    } 
                    spaceBefore(previous, codeStyle.spaceBeforeColon());
                    spaceAfter(current, codeStyle.spaceAfterColon());
                    break;
                }
                case RBRACE: //("}", "separator"),
                {
                    int indent = braces.pop(ts);
                    if (previous != null) {
                        Diff diff = diffs.getFirst();
                        if (diff != null && diff.getEndOffset() == ts.offset() && diff.getText() != null){
                            String text = diff.getText();
                            int i =text.lastIndexOf('\n');
                            if (i >= 0){
                                diff.setText(getIndent(text.substring(0, i+1), indent));
                            } else {
                                diff.setText(getIndent("", indent));
                            }
                        } else {
                            if (previous.id()== WHITESPACE){
                                diffs.addFirst(ts.offset()-previous.length(),
                                               ts.offset(), getIndent("\n", indent));
                            } else if (previous.id() == PREPROCESSOR_DIRECTIVE) {
                                diffs.addFirst(ts.offset(),
                                               ts.offset(), getIndent("", indent));
                            }
                        }
                    }
                    Token<CppTokenId> next = lookNext();
                    if (next != null && 
                       !(next.id() == SEMICOLON ||
                         next.id() == NEW_LINE)){
                        diffs.addFirst(ts.offset()+current.length(),
                                       ts.offset()+current.length(), getIndent("\n"));
                    }
                    break;
                }
                case NOT: //("!", "operator"),
                case TILDE: //("~", "operator"),
                case PLUSPLUS: //("++", "operator"),
                case MINUSMINUS: //("--","operator"),
                {
                    spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                    spaceAfter(current, codeStyle.spaceAroundUnaryOps());
                    break;
                }
                case PLUS: //("+", "operator"),
                case MINUS: //("-", "operator"),
                case STAR: //("*", "operator"),
                case AMP: //("&", "operator"),
                {
                    if (previous != null) {
                        if (previous.id() == IDENTIFIER ||
                            previous.id() == RPAREN ||
                            NUMBER_CATEGORY.equals(previous.id().primaryCategory()) ||
                            CHAR_CATEGORY.equals(previous.id().primaryCategory()) ||
                            STRING_CATEGORY.equals(previous.id().primaryCategory()) ){
                            spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                            spaceAfter(current, codeStyle.spaceAroundBinaryOps());
                        } else{
                            spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                            spaceAfter(current, codeStyle.spaceAroundUnaryOps());
                        }
                    }
                    break;
                }
                case GT: //(">", "operator"),
                case LT: //("<", "operator"),
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
                    spaceBefore(previous, codeStyle.spaceAroundBinaryOps());
                    spaceAfter(current, codeStyle.spaceAroundBinaryOps());
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
                    spaceBefore(previous, codeStyle.spaceAroundAssignOps());
                    spaceAfter(current, codeStyle.spaceAroundAssignOps());
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
                    spaceAfterBefore(current, codeStyle.spaceBeforeIfParen(), LPAREN);
                    break;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    spaceBefore(previous, codeStyle.spaceBeforeElse());
                    if (previous != null) {
                        Diff diff = diffs.getFirst();
                        if (diff != null && diff.getEndOffset() == ts.offset() && diff.getText() != null) {
                            String text = diff.getText();
                            int i =text.lastIndexOf('\n');
                            if (i >= 0){
                                diff.setText(getIndent(text.substring(0, i+1)));
                            } else {
                                diff.setText(getParentIndent(""));
                            }
                        } else {
                            if (previous.id() == WHITESPACE) {
                                int len = previous.text().length();
                                String text = getParentIndent("");
                                if (len != text.length()) {
                                    diffs.addFirst(ts.offset()-len,
                                                   ts.offset(), text);
                                }
                            } else if (previous.id() == NEW_LINE){
                                String text = getParentIndent("");
                                diffs.addFirst(ts.offset(),
                                               ts.offset(), text);
                            }
                        }
                    }
                    break;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    spaceBefore(previous, codeStyle.spaceBeforeWhile());
                    spaceAfterBefore(current, codeStyle.spaceBeforeWhileParen(), LPAREN);
                    break;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    braces.push(new StackEntry(ts));
                    spaceAfterBefore(current, codeStyle.spaceBeforeForParen(), LPAREN);
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
                    spaceBefore(previous, codeStyle.spaceBeforeCatch());
                    spaceAfterBefore(current, codeStyle.spaceBeforeCatchParen(), LPAREN);
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
                    spaceAfterBefore(current, codeStyle.spaceBeforeSwitchParen(), LPAREN);
                    break;
                }
                case CASE: //("case", "keyword-directive"),
                {
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
                case DEFAULT: //("default", "keyword-directive"),
                {
                    break;
                }
            }
            previous = current;
        }
        return diffs.getStorage();
    }
    
    /*package local*/ String getParentIndent(String prefix) {
        return getIndent(prefix, braces.getLength() - 1);
    }

    /*package local*/ String getIndent(String prefix) {
        return getIndent(prefix, braces.getLength());
    }

    /*package local*/ String getIndent(String prefix, int shift) {
        if (shift >= 0) {
            return spaces(prefix, shift * codeStyle.getGlobalIndentSize());
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
        braces.push(new StackEntry(ts));
        StackEntry entry = braces.peek();
        if (entry != null && entry.getImportantKind() != null) {
            switch (entry.getImportantKind()) {
                case NAMESPACE: //("namespace", "keyword"), //C++
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBraceNamespace());
                    break;
                }
                case CLASS: //("class", "keyword"), //C++
                case STRUCT: //("struct", "keyword"),
                case ENUM: //("enum", "keyword"),
                case UNION: //("union", "keyword"),
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBraceClass());
                    if (codeStyle.getFormatNewlineBeforeBraceClass() == CodeStyle.BracePlacement.SAME_LINE) {
                        spaceBefore(previous, codeStyle.spaceBeforeClassDeclLeftBrace());
                    }
                    break;
                }
                case IF: //("if", "keyword-directive"),
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                    if (codeStyle.getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.SAME_LINE) {
                        spaceBefore(previous, codeStyle.spaceBeforeIfLeftBrace());
                    }
                    break;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                    if (codeStyle.getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.SAME_LINE) {
                        spaceBefore(previous, codeStyle.spaceBeforeElseLeftBrace());
                    }
                    break;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                    if (codeStyle.getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.SAME_LINE) {
                        spaceBefore(previous, codeStyle.spaceBeforeSwitchLeftBrace());
                    }
                    break;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                    if (codeStyle.getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.SAME_LINE) {
                        spaceBefore(previous, codeStyle.spaceBeforeWhileLeftBrace());
                    }
                    break;
                }
                case DO: //("do", "keyword-directive"),
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                    if (codeStyle.getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.SAME_LINE) {
                        spaceBefore(previous, codeStyle.spaceBeforeDoLeftBrace());
                    }
                    break;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                    if (codeStyle.getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.SAME_LINE) {
                        spaceBefore(previous, codeStyle.spaceBeforeForLeftBrace());
                    }
                    break;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                    if (codeStyle.getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.SAME_LINE) {
                        spaceBefore(previous, codeStyle.spaceBeforeTryLeftBrace());
                    }
                    break;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                    if (codeStyle.getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.SAME_LINE) {
                        spaceBefore(previous, codeStyle.spaceBeforeCatchLeftBrace());
                    }
                    break;
                }
                default:
                    if (entry.isLikeToFunction()) {
                        newLine(current, codeStyle.getFormatNewlineBeforeBraceDeclaration());
                        if (codeStyle.getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.SAME_LINE) {
                            spaceBefore(previous, codeStyle.spaceBeforeMethodDeclLeftBrace());
                        }
                    } else {
                        newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                    }
                    break;
            }
        } else {
            newLine(current, codeStyle.getFormatNewlineBeforeBraceDeclaration());
        }
        return;
    }

    private void newLineFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null) {
            Diff diff = diffs.getFirst();
            if (diff != null && diff.getEndOffset() == ts.offset()) {
                if (diff.getText().endsWith(" ")){
                    int i = diff.getText().lastIndexOf('\n');
                    if (i >= 0){
                        diff.setText(diff.getText().substring(0,i+1));
                    } else {
                        diff.setText("");
                    }
                }
            } else if (previous.id() == WHITESPACE) {
                String text = previous.text().toString();
                if (text.endsWith(" ")) {
                    diffs.addFirst(ts.offset() - text.length(),
                                   ts.offset(), "");
                }
            }
        }
        Token<CppTokenId> next = lookNext();
        if (next != null) {
            Token<CppTokenId> p = lookPrevious(2);
            if (p != null && p.id() == NEW_LINE) {
                return;
            }
            String space = getIndent("");
            if (next.id() == WHITESPACE) {
                if (next.length() != space.length()) {
                    diffs.addFirst(ts.offset() + current.length(),
                                   ts.offset() + current.length() + next.length(), space);
                }
            } else {
                if (space.length() > 0) {
                    diffs.addFirst(ts.offset() + current.length(),
                                   ts.offset() + current.length(), space);
                }
            }
        }
    }

    private void newLine(Token<CppTokenId> current, CodeStyle.BracePlacement where){
        if (where == CodeStyle.BracePlacement.NEW_LINE) {
            newLineBefore();
        } else if (where == CodeStyle.BracePlacement.SAME_LINE) {
            if (isFirstLineToken()){
                tryRemoveLine();
            }
        }
        if(!isLastLineToken()){
            diffs.addFirst(ts.offset()+current.length(),
                           ts.offset()+current.length(), getParentIndent("\n"));
        }
    }

    private void newLineBefore() {
        if (!isFirstLineToken()) {
            diffs.addFirst(ts.offset(), ts.offset(), getParentIndent("\n"));
        } else {
            Diff diff = diffs.getFirst();
            if (diff != null && diff.getEndOffset() == ts.offset() && diff.getText() != null) {
                String text = diff.getText();
                int i = text.lastIndexOf('\n');
                if (i >= 0) {
                    diff.setText(getParentIndent(text.substring(0, i + 1)));
                } else {
                    diff.setText(getParentIndent(""));
                }
            } else {
                Token<CppTokenId> previous = lookPrevious();
                if (previous != null) {
                    if (previous.id() == WHITESPACE) {
                        int len = previous.text().length();
                        String text = getParentIndent("");
                        if (len != text.length()) {
                            diffs.addFirst(ts.offset() - len, ts.offset(), text);
                        }
                    } else if (previous.id() == NEW_LINE) {
                        String text = getParentIndent("");
                        diffs.addFirst(ts.offset(), ts.offset(), text);
                    }
                }
            }
        }
    }

    private void spaceBefore(Token<CppTokenId> previous, boolean add){
        if (previous != null) {
            Diff diff = diffs.getFirst();
            if (diff != null && diff.getEndOffset() == ts.offset()) {
                if (diff.getText().length() == 0) {
                    diff.setText(" ");
                }
                return;
            }
            if (add) {
                if (!(previous.id() == WHITESPACE ||
                      previous.id() == NEW_LINE)) {
                    diffs.addFirst(ts.offset(), ts.offset(), " ");
                }
            } else {
                if (previous.id() == WHITESPACE && !isFirstLineToken()) {
                    diffs.addFirst(ts.offset() - previous.length(),
                                   ts.offset(), "");
                }
            }
        }
    }

    private void spaceAfter(Token<CppTokenId> current, boolean add){
        Token<CppTokenId> next = lookNext();
        if (next != null) {
            if (add) {
                if (!(next.id() == WHITESPACE ||
                      next.id() == NEW_LINE)) {
                    diffs.addFirst(ts.offset()+current.length(),
                                   ts.offset()+current.length(), " ");
                }
            } else {
                if (next.id() == WHITESPACE) {
                    diffs.addFirst(ts.offset()+current.length(),
                                   ts.offset()+current.length()+next.length(), "");
                }
            }
        }
    }

    private void spaceAfterBefore(Token<CppTokenId> current, boolean add, CppTokenId before){
        Token<CppTokenId> next = lookNext();
        if (next != null) {
            if (next.id() == WHITESPACE) {
                Token<CppTokenId> p = lookNext(2);
                if (p!=null && p.id()==before) {
                    if (!add) {
                        diffs.addFirst(ts.offset()+current.length(),
                                       ts.offset()+current.length()+next.length(), "");
                    }
                }
            } else if (next.id() == before) {
                if (add) {
                    diffs.addFirst(ts.offset()+current.length(),
                                   ts.offset()+current.length(), " ");
                }
            }
        }
    }

    private void formatLeftParen(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null){
            Token<CppTokenId> p = lookPreviousStatement();
            if (p != null) {
                switch(p.id()) {
                    case IF:
                    {
                        spaceAfter(current, codeStyle.spaceWithinIfParens());
                        break;
                    }
                    case FOR:
                    {
                        spaceAfter(current, codeStyle.spaceWithinForParens());
                        break;
                    }
                    case WHILE:
                    {
                        spaceAfter(current, codeStyle.spaceWithinWhileParens());
                        break;
                    }
                    case SWITCH:
                    {
                        spaceAfter(current, codeStyle.spaceWithinSwitchParens());
                        break;
                    }
                    case CATCH:
                    {
                        spaceAfter(current, codeStyle.spaceWithinCatchParens());
                        break;
                    }
                }
            }
        }
    }

    private void formatRightParen(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null){
            Token<CppTokenId> p = lookPreviousStatement();
            if (p != null) {
                switch(p.id()) {
                    case IF:
                    {
                        spaceBefore(current, codeStyle.spaceWithinIfParens());
                        break;
                    }
                    case FOR:
                    {
                        spaceBefore(current, codeStyle.spaceWithinForParens());
                        break;
                    }
                    case WHILE:
                    {
                        spaceBefore(current, codeStyle.spaceWithinWhileParens());
                        break;
                    }
                    case SWITCH:
                    {
                        spaceBefore(current, codeStyle.spaceWithinSwitchParens());
                        break;
                    }
                    case CATCH:
                    {
                        spaceBefore(current, codeStyle.spaceWithinCatchParens());
                        break;
                    }
                }
            }
        }
    }

    private Token<CppTokenId> lookPreviousStatement(){
        int index = ts.index();
        int balance = 0;
        if (ts.token().id() == RPAREN){
            balance = 1;
        }
        try {
            while(ts.movePrevious()){
                switch(ts.token().id()) {
                    case LPAREN:
                        if (balance == 0) {
                            return null;
                        }
                        balance--;
                        break;
                    case RPAREN:
                        balance++;
                        break;
                    case WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        if (balance == 0) {
                            return ts.token();
                        }
                        break;
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    private Token<CppTokenId> lookNext(){
        if (ts.moveNext()) {
            Token<CppTokenId> next = ts.token();
            ts.movePrevious();
            return next;
        }
        return null;
    }

    private Token<CppTokenId> lookNext(int i){
        int index = ts.index();
        try {
            while(i-- > 0) {
                if (!ts.moveNext()){
                    return null;
                }
            }
            return ts.token();
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    private Token<CppTokenId> lookPrevious(){
        if (ts.movePrevious()) {
            Token<CppTokenId> previous = ts.token();
            ts.moveNext();
            return previous;
        }
        return null;
    }

    private Token<CppTokenId> lookPrevious(int i){
        int index = ts.index();
        try {
            while(i-- > 0) {
                if (!ts.movePrevious()){
                    return null;
                }
            }
            return ts.token();
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    private boolean isFirstLineToken(){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return true;
                }
                if (ts.token().id() == NEW_LINE){
                    return true;
                } else if (ts.token().id() != WHITESPACE){
                    return false;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    private void tryRemoveLine(){
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
                                diffs.addFirst(ts.offset(),
                                               ts.offset()+ts.token().length(), "");
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
    
    private boolean isLastLineToken(){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.moveNext()){
                    return true;
                }
                CppTokenId id = ts.token().id();
                if (id == NEW_LINE){
                    return true;
                } else if ( id == LINE_COMMENT){
                    // skip
                } else if (ts.token().id() != WHITESPACE){
                    return false;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
}
