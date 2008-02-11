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
    private TokenSequence<CppTokenId> ts;
    private int startOffset;
    private int endOffset;
    private CodeStyle codeStyle;
    private LinkedList<Diff> diffs;
    private BracesStack braces = new BracesStack();
    
    ReformatterImpl(TokenSequence<CppTokenId> ts, int startOffset, int endOffset, CodeStyle codeStyle){
        this.ts = ts;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.codeStyle = codeStyle;
    }
    
    LinkedList<Diff> reformat(){
        diffs = new  LinkedList<Diff>();
        ts.move(startOffset);
        Token<CppTokenId> previous = lookPrevious();
        int prepocessorDepth = 0;
        while(ts.moveNext()){
            if (ts.offset() > endOffset) {
                break;
            }
            Token<CppTokenId> current = ts.token();
            CppTokenId id = current.id();
            switch(id){
                case PREPROCESSOR_DIRECTIVE: //(null, "preprocessor"),
                {
                    if (codeStyle.indentPreprocessorDirectives() == CodeStyle.PreprocessorIndent.CODE_INDENT) {
                        if (previous != null) {
                            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                            if (diff != null && diff.getEndOffset() == ts.offset()){
                                if (diff.getText().startsWith("\n")){
                                    diff.setText(getIndent("\n", 0));
                                } else {
                                    diff.setText(getIndent("", 0));
                                }
                            } else {
                                if (previous.id()==WHITESPACE){
                                    diffs.addFirst(new Diff(ts.offset()-previous.length(),
                                                            ts.offset(), getIndent("", 0)));
                                } else {
                                    diffs.addFirst(new Diff(ts.offset(),
                                                            ts.offset(), getIndent("", 0)));
                                }
                            }
                        }
                    } else if (codeStyle.indentPreprocessorDirectives() == CodeStyle.PreprocessorIndent.START_LINE) {
                        if (previous != null) {
                            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                            if (diff != null && diff.getEndOffset() == ts.offset()){
                                if (diff.getText().startsWith("\n")){
                                    diff.setText("\n");
                                } else {
                                    diff.setText("");
                                }
                            } else {
                                if (previous.id()==WHITESPACE){
                                    diffs.addFirst(new Diff(ts.offset()-previous.length(),
                                                            ts.offset(), ""));
                                }
                            }
                        }
                    } else if (codeStyle.indentPreprocessorDirectives() == CodeStyle.PreprocessorIndent.PREPROCESSOR_INDENT) {
                        TokenSequence<CppTokenId> prep = ts.embedded(CppTokenId.languagePreproc());
                        if (prep != null) {
                            prep.moveStart();
                            while(prep.moveNext()) {
                                if (!(prep.token().id() == WHITESPACE ||
                                    prep.token().id() == PREPROCESSOR_START)) {
                                    break;
                                }
                            }
                        }
                        if (prep != null && prep.token() != null) {
                            switch(prep.token().id()){
                                case PREPROCESSOR_ELSE: //("else", "preprocessor-keyword-directive"),
                                case PREPROCESSOR_ELIF: //("elif", "preprocessor-keyword-directive"),
                                case PREPROCESSOR_ENDIF: //("endif", "preprocessor-keyword-directive"),
                                    prepocessorDepth--;
                                    break;
                            }
                        }
                        if (previous != null) {
                            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                            if (diff != null && diff.getEndOffset() == ts.offset()){
                                if (diff.getText().startsWith("\n")){
                                    diff.setText(getPreprocessorIndent("\n",prepocessorDepth));
                                } else {
                                    diff.setText(getPreprocessorIndent("",prepocessorDepth));
                                }
                            } else {
                                if (previous.id()==WHITESPACE){
                                    diffs.addFirst(new Diff(ts.offset()-previous.length(),
                                                            ts.offset(), getPreprocessorIndent("",prepocessorDepth)));
                                } else {
                                    diffs.addFirst(new Diff(ts.offset(),
                                                            ts.offset(), getPreprocessorIndent("",prepocessorDepth)));
                                }
                            }
                        }
                        if (prep != null && prep.token() != null) {
                            switch(prep.token().id()){
                                case PREPROCESSOR_IF: //("if", "preprocessor-keyword-directive"),
                                case PREPROCESSOR_IFDEF: //("ifdef", "preprocessor-keyword-directive"),
                                case PREPROCESSOR_IFNDEF: //("ifndef", "preprocessor-keyword-directive"),
                                    prepocessorDepth++;
                                    break;
                                case PREPROCESSOR_ELSE: //("else", "preprocessor-keyword-directive"),
                                case PREPROCESSOR_ELIF: //("elif", "preprocessor-keyword-directive"),
                                    prepocessorDepth++;
                                    break;
                            }
                        }
                    }
                    break;
                }
                case NEW_LINE:
                {
                    Token<CppTokenId> next = lookNext();
                    if (next != null) {
                        String space = getIndent("", 0);
                        if (next.id() == WHITESPACE) {
                            diffs.addFirst(new Diff(ts.offset()+current.length(),
                                                    ts.offset()+current.length()+next.length(), space));
                        } else {
                            diffs.addFirst(new Diff(ts.offset()+current.length(),
                                                    ts.offset()+current.length(), space));
                        }
                    }
                    break;
                }
                case LBRACE: //("{", "separator"),
                {
                    braces.push(new StackEntry(ts));
                    StackEntry entry = braces.peek();
                    if (entry != null && entry.getImportantKind() != null){
                        switch (entry.getImportantKind()) {
                            case NAMESPACE: //("namespace", "keyword"), //C++
                                newLine(current, codeStyle.getFormatNewlineBeforeBraceNamespace());
                                break;
                            case CLASS: //("class", "keyword"), //C++
                            case STRUCT: //("struct", "keyword"),
                            case ENUM: //("enum", "keyword"),
                            case UNION: //("union", "keyword"),
                                newLine(current, codeStyle.getFormatNewlineBeforeBraceClass());
                                break;
                            case IF: //("if", "keyword-directive"),
                            case ELSE: //("else", "keyword-directive"),
                            case SWITCH: //("switch", "keyword-directive"),
                            case CASE: //("case", "keyword-directive"),
                            case WHILE: //("while", "keyword-directive"),
                            case DO: //("do", "keyword-directive"),
                            case FOR: //("for", "keyword-directive"),
                            case TRY: //("try", "keyword-directive"), // C++
                            case CATCH: //("catch", "keyword-directive"), //C++
                                newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                                break;
                            default:
                                if (entry.isLikeToFunction()) {
                                    newLine(current, codeStyle.getFormatNewlineBeforeBraceDeclaration());
                                } else {
                                    newLine(current, codeStyle.getFormatNewlineBeforeBrace());
                                }
                                break;
                        }
                    } else {
                        newLine(current, codeStyle.getFormatNewlineBeforeBraceDeclaration());
                    }
                    break;
                }
                case SEMICOLON: //(";", "separator"),
                {
                    Token<CppTokenId> next = lookNext();
                    if (next != null) {
                        if (!(next.id() == NEW_LINE ||
                            next.id() == LINE_COMMENT)){
                            diffs.addFirst(new Diff(ts.offset()+current.length(),
                                                    ts.offset()+current.length(), getIndent("\n", 0)));
                        }    
                    }
                    break;
                }
                case COMMA: //(",", "separator"),
                {
                    spaceBefore(previous, codeStyle.spaceBeforeComma());
                    spaceAfter(current, codeStyle.spaceAfterComma());
                    break;
                }
                case COLON: //(":", "operator"),
                {
                    if (previous != null && 
                       (previous.id() == PRIVATE ||
                        previous.id() == PROTECTED ||
                        previous.id() == PUBLIC)) {
                        diffs.addFirst(new Diff(ts.offset()+current.length(),
                                                ts.offset()+current.length(), getIndent("\n", -1)));
                        break;
                    } 
                    spaceBefore(previous, codeStyle.spaceBeforeColon());
                    spaceAfter(current, codeStyle.spaceAfterColon());
                    break;
                }
                case RBRACE: //("}", "separator"),
                {
                    braces.pop();
                    if (previous != null) {
                        Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                        if (diff != null && diff.getEndOffset() == ts.offset()){
                            if (diff.getText().startsWith("\n")){
                                diff.setText(getIndent("\n", 0));
                            }
                        } else {
                            if (previous.id()== WHITESPACE){
                                diffs.addFirst(new Diff(ts.offset()-previous.length(),
                                                        ts.offset(), getIndent("\n", 0)));
                            } else if (previous.id() == PREPROCESSOR_DIRECTIVE) {
                                diffs.addFirst(new Diff(ts.offset(),
                                                        ts.offset(), getIndent("", 0)));
                            }
                        }
                    }
                    Token<CppTokenId> next = lookNext();
                    if (next != null && 
                       !(next.id() == SEMICOLON ||
                         next.id() == NEW_LINE)){
                        diffs.addFirst(new Diff(ts.offset()+current.length(),
                                                ts.offset()+current.length(), getIndent("\n", 0)));
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
                    spaceAfterBefore(current, codeStyle.spaceBeforeIfParen(), LPAREN);
                    break;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    spaceBefore(previous, codeStyle.spaceBeforeElse());
                    break;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
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
                case WHILE: //("while", "keyword-directive"),
                {
                    spaceBefore(previous, codeStyle.spaceBeforeWhile());
                    spaceAfterBefore(current, codeStyle.spaceBeforeWhileParen(), LPAREN);
                    break;
                }
                case DO: //("do", "keyword-directive"),
                {
                    break;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    spaceAfterBefore(current, codeStyle.spaceBeforeForParen(), LPAREN);
                    break;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    break;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    spaceBefore(previous, codeStyle.spaceBeforeCatch());
                    spaceAfterBefore(current, codeStyle.spaceBeforeCatchParen(), LPAREN);
                    break;
                }
            }
            previous = current;
        }
        return diffs;
    }
    
    private String getIndent(String prefix, int body) {
        int shift = braces.getLength() + body;
        if (shift >= 0) {
            return spaces(prefix, shift * codeStyle.getGlobalIndentSize());
        } else {
            return prefix;
        }
    }

    private String getPreprocessorIndent(String prefix, int shift) {
        if (shift >= 0) {
            return spaces(prefix, shift * codeStyle.getGlobalIndentSize());
        } else {
            return prefix;
        }
    }

    private String spaces(String prefix, int length) {
        StringBuilder spaces = new StringBuilder(prefix);
        for(int i = 0; i < length; i++){
            spaces.append(' ');
        }
        return spaces.toString();
    }

    private void spaceBefore(Token<CppTokenId> previous, boolean add){
        if (previous != null) {
            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
            if (diff != null && diff.getEndOffset() == ts.offset() && diff.getText().length() > 0) {
                return;
            }
            if (add) {
                if (!(previous.id() == WHITESPACE ||
                      previous.id() == NEW_LINE)) {
                    diffs.addFirst(new Diff(ts.offset(), ts.offset(), " "));
                }
            } else {
                if (previous.id() == WHITESPACE) {
                    String text = previous.text().toString();
                    if (text.endsWith(" ")) {
                        Token<CppTokenId> p = lookPrevious(2);
                        if (!(p.id() == WHITESPACE ||
                              p.id() == NEW_LINE)) {
                            diffs.addFirst(new Diff(ts.offset() - text.length(),
                                                    ts.offset(), text.substring(0, text.length() - 1)));
                        }
                    }
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
                    diffs.addFirst(new Diff(ts.offset()+current.length(),
                                            ts.offset()+current.length(), " "));
                }
            } else {
                if (next.id() == WHITESPACE) {
                    String text = next.text().toString();
                    if (text.startsWith(" ")) {
                        diffs.addFirst(new Diff(ts.offset()+current.length(),
                                                ts.offset()+current.length()+text.length(), text.substring(1)));
                    }
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
                        String text = next.text().toString();
                        diffs.addFirst(new Diff(ts.offset()+current.length(),
                                                ts.offset()+current.length()+text.length(), ""));
                    }
                }
            } else if (next.id() == before) {
                if (add) {
                    diffs.addFirst(new Diff(ts.offset()+current.length(),
                                            ts.offset()+current.length(), " "));
                }
            }
        }
    }


    private void newLine(Token<CppTokenId> current, CodeStyle.BracePlacement where){
        if (where == CodeStyle.BracePlacement.NEW_LINE) {
            if (!isFirstLineToken()){
                diffs.addFirst(new Diff(ts.offset(),
                                        ts.offset(), getIndent("\n", -1)));
            }
        } else if (where == CodeStyle.BracePlacement.SAME_LINE) {
            if (isFirstLineToken()){
                tryRemoveLine();
            }
        }
        if(!isLastLineToken()){
            diffs.addFirst(new Diff(ts.offset()+current.length(),
                                    ts.offset()+current.length(), getIndent("\n", -1)));
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
                                diffs.addFirst(new Diff(ts.offset(),
                                                        ts.offset()+ts.token().length(), ""));
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
