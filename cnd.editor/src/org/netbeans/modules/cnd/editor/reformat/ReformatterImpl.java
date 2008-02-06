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
        while(ts.moveNext()){
            if (ts.offset() > endOffset) {
                break;
            }
            Token<CppTokenId> current = ts.token();
            CppTokenId id = current.id();
            switch(id){
                case LBRACE:
                case SEMICOLON:
                    diffs.addFirst(new Diff(ts.offset()+current.length(), ts.offset()+current.length(), "\n"));
                    break;
                case COLON:
                {
                    if (previous != null && 
                       (previous.id() == PRIVATE ||
                        previous.id() == PROTECTED ||
                        previous.id() == PUBLIC)) {
                        diffs.addFirst(new Diff(ts.offset()+current.length(), ts.offset()+current.length(), "\n"));
                    }
                    break;
                }
                case RBRACE:
                {
                    Token<CppTokenId> next = lookNext();
                    if (next != null && 
                       !(next.id() == SEMICOLON ||
                         next.id() == NEW_LINE)){
                        diffs.addFirst(new Diff(ts.offset()+current.length(), ts.offset()+current.length(), "\n"));
                    }
                    break;
                }
                case NOT: //("!", "operator"),
                case TILDE: //("~", "operator"),
                case PLUSPLUS: //("++", "operator"),
                case MINUSMINUS: //("--","operator"),
                {
                    spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                    spaceAfter(current, lookNext(), codeStyle.spaceAroundUnaryOps());
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
                            spaceAfter(current, lookNext(), codeStyle.spaceAroundBinaryOps());
                        } else{
                            spaceBefore(previous, codeStyle.spaceAroundUnaryOps());
                            spaceAfter(current, lookNext(), codeStyle.spaceAroundUnaryOps());
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
                    spaceAfter(current, lookNext(), codeStyle.spaceAroundBinaryOps());
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
                    spaceAfter(current, lookNext(), codeStyle.spaceAroundAssignOps());
                    break;
                }
            }
            previous = current;
        }
        return diffs;
    }
    
    private void spaceBefore(Token<CppTokenId> previous, boolean add){
        if (previous != null) {
            if (add) {
                if (!(previous.id() == WHITESPACE ||
                        previous.id() == NEW_LINE)) {
                    diffs.addFirst(new Diff(ts.offset(), ts.offset(), " "));
                }
            } else {
                if (previous.id() == WHITESPACE) {
                    String text = previous.text().toString();
                    if (text.endsWith(" ")) {
                        diffs.addFirst(new Diff(ts.offset() - text.length(),
                                                ts.offset(), text.substring(0, text.length() - 1)));
                    }
                }
            }
        }
    }

    private void spaceAfter(Token<CppTokenId> current, Token<CppTokenId> next, boolean add){
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
    
    private Token<CppTokenId> lookNext(){
        if (ts.moveNext()) {
            Token<CppTokenId> next = ts.token();
            ts.movePrevious();
            return next;
        }
        return null;
    }

    private Token<CppTokenId> lookPrevious(){
        if (ts.movePrevious()) {
            Token<CppTokenId> previous = ts.token();
            ts.moveNext();
            return previous;
        }
        return null;
    }
}
