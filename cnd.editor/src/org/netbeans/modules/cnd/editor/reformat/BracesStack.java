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

import java.util.Stack;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;

/**
 *
 * @author Alexander Simon
 */
class BracesStack {
    
    private static final boolean TRACE_STACK = true;
    private static final boolean TRACE_STATEMENT = true;
    
    private Stack<StackEntry> stack = new Stack<StackEntry>();
    private StatementContinuation statementContinuation = StatementContinuation.STOP;
    private int lastStatementStart = -1;
    int parenDepth = 0;
    boolean isDoWhile = false;

    BracesStack() {
        super();
    }

    @Override
    public BracesStack clone(){
        BracesStack clone = new BracesStack();
        clone.statementContinuation = statementContinuation;
        clone.lastStatementStart = lastStatementStart;
        clone.parenDepth = parenDepth;
        clone.isDoWhile = isDoWhile;
        for(int i = 0; i < stack.size(); i++){
            clone.stack.add(stack.get(i));
        }
        return clone;
    }
    
    public void reset(BracesStack clone){
        statementContinuation = clone.statementContinuation;
        lastStatementStart = clone.lastStatementStart;
        parenDepth = clone.parenDepth;
        isDoWhile = clone.isDoWhile;
        stack.clear();
        for(int i = 0; i < clone.stack.size(); i++){
            stack.add(clone.stack.get(i));
        }
    }
    
    public void push(StackEntry entry) {
        statementContinuation = StatementContinuation.STOP;
        if (entry.getKind() == ELSE){
            if (stack.size() > 0 && 
                (stack.peek().getKind() == IF || stack.peek().getKind() == ELSE)) {
                stack.pop();
            }
        }
        if (!(entry.getImportantKind() != null ||
              entry.isLikeToArrayInitialization())) {
            if (peek() != null && peek().isLikeToArrayInitialization()){
                // this is two dimensiomal arry initialization
                entry.setLikeToArrayInitialization(true);
            }
        }
        if (entry.getKind() == LBRACE){
            if(!entry.isLikeToArrayInitialization()) {
                clearLastStatementStart();
            }
        } else if (lastStatementStart != entry.getIndex()) {
            lastStatementStart = entry.getIndex();
            if (TRACE_STATEMENT) System.out.println("start of Statement/Declaration:"+entry.getText());
        }
        stack.push(entry);
        if (TRACE_STACK) System.out.println("push: "+toString()); // NOI18N
    }

    public int pop(ExtendedTokenSequence ts) {
        clearLastStatementStart();
        statementContinuation = StatementContinuation.STOP;
        int res = popImpl(ts);
        if (TRACE_STACK) System.out.println("pop "+ts.token().id().name()+": "+toString()); // NOI18N
        return res;
    }

    public int popImpl(ExtendedTokenSequence ts) {
        if (stack.empty()) {
            return 0;
        }
        CppTokenId id = ts.token().id();
        if (id == RBRACE) {
            return popBrace(ts);
        }
        return popStatement(ts);
    }

    public int popBrace(ExtendedTokenSequence ts) {
        int res = 0;
        int brace = 0;
        for (int i = stack.size() - 1; i >= 0; i--) {
            StackEntry top = stack.get(i);
            if (top.getKind() == LBRACE) {
                brace = i - 1;
                stack.setSize(i);
                res = getLength();
                if (isStatement(peek())){
                    res--;
                }
                break;
            }
        }
        if (brace < 0) {
            stack.setSize(0);
            return res;
        }
        popStatement(ts);
        return res;
    }

    public int popStatement(ExtendedTokenSequence ts) {
        Token<CppTokenId> next = getNextImportant(ts);
        for (int i = stack.size() - 1; i >= 0; i--) {
            StackEntry top = stack.get(i);
            switch (top.getKind()) {
                case LBRACE: {
                    stack.setSize(i + 1);
                    return getLength();
                }
                case IF: //("if", "keyword-directive"),
                {
                    if (next != null && next.id() == ELSE) {
                        if (i > 0 && stack.get(i-1).getKind() == ELSE) {
                            stack.setSize(i);
                            return getLength();
                        } else {
                            stack.setSize(i + 1);
                            return getLength();
                        }
                    }
                    break;
                }
                case ELSE: //("else", "keyword-directive"),
                case TRY: //("try", "keyword-directive"), // C++
                case CATCH: //("catch", "keyword-directive"), //C++
                case SWITCH: //("switch", "keyword-directive"),
                case FOR: //("for", "keyword-directive"),
                case ASM: //("asm", "keyword-directive"), // gcc and C++
                case DO: //("do", "keyword-directive"),
                case WHILE: //("while", "keyword-directive"),
                    break;
            }
        }
        stack.setSize(0);
        return 0;
    }
    
    private boolean isStatement(StackEntry top){
        if (top != null) {
            switch (top.getKind()) {
                case IF: //("if", "keyword-directive"),
                case ELSE: //("else", "keyword-directive"),
                case TRY: //("try", "keyword-directive"), // C++
                case CATCH: //("catch", "keyword-directive"), //C++
                case SWITCH: //("switch", "keyword-directive"),
                case FOR: //("for", "keyword-directive"),
                case ASM: //("asm", "keyword-directive"), // gcc and C++
                case DO: //("do", "keyword-directive"),
                case WHILE: //("while", "keyword-directive"),
                    return true;
            }
        }
        return false;
    }
    
    public boolean isDeclarationLevel(){
        StackEntry top = peek();
        if (top == null) {
            return true;
        }
        if (top.getKind() == CATCH){
            return true;
        }
        if (isStatement(top)){
            return false;
        }
        CppTokenId id = top.getImportantKind();
        if (id == null){
            return false;
        }
        return id == CppTokenId.NAMESPACE || id == CppTokenId.CLASS;
    }
    
    public StackEntry peek() {
        if (stack.empty()) {
            return null;
        }
        return stack.peek();
    }

    public int getLength() {
        StackEntry prev = null;
        int res = 0;
        for(int i = 0; i < stack.size(); i++){
            StackEntry entry = stack.get(i);
            if (entry.getKind() == LBRACE) {
                if (prev == null) {
                    res++;
                } else {
                    if (prev.getKind()==LBRACE){
                        CppTokenId kind = prev.getImportantKind();
                        if (kind != SWITCH) {
                            res++;
                        }
                    }
                }
            } else if (entry.getKind() == IF){
                if (prev == null || prev.getKind()!=ELSE) {
                    res++;
                }
            } else {
                res++;
            }
            prev = entry;
        }
        return res;
    }
    
    public int switchDepth(){
        int res = 0;
        StackEntry prev = null;
        for(int i = 0; i < stack.size(); i++){
            StackEntry entry = stack.get(i);
            if (entry.getKind() == LBRACE) {
                if (prev != null && prev.getKind() == SWITCH) {
                    res++;
                }
            }
            prev = entry;
        }
        return res;
    }

    public StackEntry lookPerevious(){
        if (stack.size() < 2) {
            return null;
        }
        return stack.get(stack.size()-2);
        
    }
    
    private Token<CppTokenId> getNextImportant(ExtendedTokenSequence ts) {
        int i = ts.index();
        try {
            while (true) {
                if (!ts.moveNext()) {
                    return null;
                }
                Token<CppTokenId> current = ts.token();
                switch (current.id()) {
                    case WHITESPACE:
                    case NEW_LINE:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    case IF: //("if", "keyword-directive"),
                    case ELSE: //("else", "keyword-directive"),
                    case SWITCH: //("switch", "keyword-directive"),
                    case ASM: //("asm", "keyword-directive"), // gcc and C++
                    case WHILE: //("while", "keyword-directive"),
                    case DO: //("do", "keyword-directive"),
                    case FOR: //("for", "keyword-directive"),
                    case TRY: //("try", "keyword-directive"), // C++
                    case CATCH: //("catch", "keyword-directive"), //C++
                        return current;
                    default:
                        return null;
                }
            }
        } finally {
            ts.moveIndex(i);
            ts.moveNext();
        }
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < stack.size(); i++){
            StackEntry entry = stack.get(i);
            if (i > 0) {
                buf.append(", "); // NOI18N
            }
            buf.append(entry.toString());
        }
        buf.append("+"+getLength()); // NOI18N
        return buf.toString();
    }

    public StatementContinuation getStatementContinuation() {
        return statementContinuation;
    }

    public void setStatementContinuation(StatementContinuation statementContinuation) {
        this.statementContinuation = statementContinuation;
    }

    public StatementKind getLastStatementKind(ExtendedTokenSequence ts) {
        if (lastStatementStart < 0) {
            return null;
        }
        int i = ts.index();
        try {
            int paren = 0;
            int curly = 0;
            int triangle = 0;
            ts.moveIndex(lastStatementStart);
            StatementKind res = null;
            while (true) {
                if (!ts.moveNext()) {
                    return null;
                }
                Token<CppTokenId> current = ts.token();
                switch (current.id()) {
                    case RPAREN: //(")", "separator"),
                    {
                        paren--;
                        break;
                    }
                    case LPAREN: //("(", "separator"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            if (isDeclarationLevel()){
                                return StatementKind.FUNCTION;
                            } else {
                                return StatementKind.EXPRESSION_STATEMENT;
                            }
                        }
                        paren++;
                        break;
                    }
                    case RBRACE: //("}", "separator"),
                    case LBRACE: //("{", "separator"),
                    case SEMICOLON: //(";", "separator"),
                    {
                       if (isDeclarationLevel()){
                           if (res != null){
                               return res;
                           }
                           return StatementKind.FUNCTION;
                        } else {
                            return StatementKind.DECLARATION_STATEMENT;
                        }
                    }
                    case EQ: //("=", "operator"),
                    {
                       if (isDeclarationLevel()){
                            return StatementKind.DECLARATION_STATEMENT;
                        } else {
                            return StatementKind.EXPRESSION_STATEMENT;
                        }
                    }
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
                        if (paren == 0) {
                            return StatementKind.EXPRESSION_STATEMENT;
                        }
                        break;
                    }
                    case GT: //(">", "operator"),
                    {
                        if (paren == 0 && curly == 0) {
                            triangle--;
                        }
                        break;
                    }
                    case LT: //("<", "operator"),
                    {
                        if (paren == 0 && curly == 0) {
                            triangle++;
                        }
                        break;
                    }
                    case NAMESPACE: //("namespace", "keyword"), //C++
                        return StatementKind.NAMESPACE;
                    case CLASS: //("class", "keyword"), //C++
                        return StatementKind.CLASS;
                    case STRUCT: //("struct", "keyword"),
                    case ENUM: //("enum", "keyword"),
                    case UNION: //("union", "keyword"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            res = StatementKind.CLASS;
                        }
                        break;
                    }
                    case EXTERN: //EXTERN("extern", "keyword"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            res = StatementKind.NAMESPACE;
                        }
                        break;
                    }
                    case ASM: //("if", "keyword-directive"),
                    case IF: //("if", "keyword-directive"),
                    case ELSE: //("else", "keyword-directive"),
                    case SWITCH: //("switch", "keyword-directive"),
                    case WHILE: //("while", "keyword-directive"),
                    case DO: //("do", "keyword-directive"),
                    case FOR: //("for", "keyword-directive"),
                    case TRY: //("try", "keyword-directive"), // C++
                    case CATCH: //("catch", "keyword-directive"), //C++
                       return StatementKind.COMPAUND_STATEMENT;
                }
            }
        } finally {
            ts.moveIndex(i);
            ts.moveNext();
        }
    }
    
    public void clearLastStatementStart() {
        lastStatementStart = -1;
    }
    
    public void setLastStatementStart(ExtendedTokenSequence ts) {
        if (lastStatementStart == -1) {
            lastStatementStart = ts.index();
            if (TRACE_STATEMENT) System.out.println("start of Statement/Declaration:"+ts.token().text());
        }
    }
    
    public static enum StatementContinuation {
        START,
        CONTINUE,
        STOP;
    }

    public static enum StatementKind {
        NAMESPACE,
        CLASS,
        FUNCTION,
        DECLARATION_STATEMENT,
        COMPAUND_STATEMENT,
        EXPRESSION_STATEMENT;
    }
}
