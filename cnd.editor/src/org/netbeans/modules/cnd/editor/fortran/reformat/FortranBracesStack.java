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

package org.netbeans.modules.cnd.editor.fortran.reformat;

import java.util.Stack;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;
import static org.netbeans.cnd.api.lexer.FortranTokenId.*;

/**
 *
 * @author Alexander Simon
 */
class FortranBracesStack {
    private static final boolean TRACE_STACK = false;
    
    private Stack<FortranStackEntry> stack = new Stack<FortranStackEntry>();
    private FortranCodeStyle codeStyle;
    int parenDepth = 0;

    FortranBracesStack(FortranCodeStyle codeStyle) {
        this.codeStyle = codeStyle;
    }

    @Override
    public FortranBracesStack clone(){
        FortranBracesStack clone = new FortranBracesStack(codeStyle);
        for(int i = 0; i < stack.size(); i++){
            clone.stack.add(stack.get(i));
        }
        clone.parenDepth = parenDepth;
        return clone;
    }
    
    public void reset(FortranBracesStack clone){
        stack.clear();
        for(int i = 0; i < clone.stack.size(); i++){
            stack.add(clone.stack.get(i));
        }
        parenDepth = clone.parenDepth;
    }

    public void push(Token<FortranTokenId> token) {
        FortranStackEntry newEntry = new FortranStackEntry(token);
        pushImpl(newEntry);
    }

    public void push(FortranExtendedTokenSequence ts) {
        FortranStackEntry newEntry = new FortranStackEntry(ts);
        pushImpl(newEntry);
    }

    private void pushImpl(FortranStackEntry newEntry) {
        FortranStackEntry prevEntry = peek();
        int prevIndent = 0;
        int prevSelfIndent = 0;
        int statementIndent = codeStyle.indentSize();
        int switchIndent = codeStyle.indentSize();
        if (prevEntry != null){
            prevIndent = prevEntry.getIndent();
            prevSelfIndent = prevEntry.getSelfIndent();
        }
        switch (newEntry.getKind()) {
            case KW_ELSEIF:
            case KW_ELSE:
                if (prevEntry != null && 
                   (prevEntry.getKind() == KW_IF || prevEntry.getKind() == KW_ELSE || prevEntry.getKind() == KW_ELSEIF)) {
                    newEntry.setIndent(prevIndent);
                    newEntry.setSelfIndent(prevSelfIndent);
                    break;
                }
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case KW_IF:
                if (prevEntry != null && prevEntry.getKind() == KW_ELSE) {
                    newEntry.setIndent(prevIndent);
                    newEntry.setSelfIndent(prevSelfIndent);
                    break;
                }
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case KW_WHILE:
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case KW_DO:
            case KW_FORALL:
            case KW_SELECT:
                newEntry.setIndent(prevIndent + switchIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case KW_MODULE:
            case KW_PROGRAM:
            case KW_PROCEDURE:
            case KW_SUBROUTINE:
            case KW_FUNCTION:
            case KW_BLOCK:
                statementIndent = codeStyle.indentSize();
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case KW_INTERFACE:
            case KW_STRUCTURE:
            case KW_UNION:
            case KW_TYPE:
            case KW_BLOCKDATA:
            case KW_MAP:
//            case KW_FILE:
                statementIndent = codeStyle.indentSize();
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
        }
        push(newEntry);
    }
        
    public int getIndent(){
        int shift = 0;
        if (!codeStyle.isFreeFormatFortran()){
            shift = 5;
        }
        FortranStackEntry top = peek();
        if (top != null) {
            return shift + top.getIndent();
        }
        return shift;
    }

    public int getSelfIndent(){
        int shift = 0;
        if (!codeStyle.isFreeFormatFortran()){
            shift = 5;
        }
        FortranStackEntry top = peek();
        if (top != null) {
            return shift + top.getSelfIndent();
        }
        return shift;
    }

    private void push(FortranStackEntry entry) {
        if (entry.getKind() == KW_ELSE || entry.getKind() == KW_ELSEIF){
            if (stack.size() > 0 && 
                (stack.peek().getKind() == KW_IF || stack.peek().getKind() == KW_ELSE || stack.peek().getKind() == KW_ELSEIF)) {
                stack.pop();
            }
        }
        stack.push(entry);
        if (TRACE_STACK) {System.out.println("push: "+toString());} // NOI18N
    }

    public void pop(FortranExtendedTokenSequence ts) {
        popImpl(ts);
        if (TRACE_STACK) {System.out.println("pop "+ts.token().id().name()+": "+toString());} // NOI18N
    }

    public void popImpl(FortranExtendedTokenSequence ts) {
        if (stack.empty()) {
            return;
        }
        stack.pop();
    }
    
    public FortranStackEntry peek() {
        if (stack.empty()) {
            return null;
        }
        return stack.peek();
    }

    public int getLength() {
        return stack.size();
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < stack.size(); i++){
            FortranStackEntry entry = stack.get(i);
            if (i > 0) {
                buf.append(", "); // NOI18N
            }
            buf.append(entry.toString());
        }
        buf.append("+"+getIndent()+"-"+getSelfIndent()); // NOI18N
        return buf.toString();
    }

}
