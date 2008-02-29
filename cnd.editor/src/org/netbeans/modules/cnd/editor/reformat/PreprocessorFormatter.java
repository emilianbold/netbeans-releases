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
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.reformat.DiffLinkedList.DiffResult;

/**
 *
 * @author Alexander Simon
 */
public class PreprocessorFormatter {
    private ReformatterImpl context;
    private final ExtendedTokenSequence ts;
    private final CodeStyle codeStyle;
    private final DiffLinkedList diffs;
    private int prepocessorDepth = 0;
    private Stack<BracesStack> stateStack = new Stack<BracesStack>();
    private BracesStack braces;

    
    /*package local*/ PreprocessorFormatter(ReformatterImpl context){
        this.context = context;
        this.ts = context.ts;
        this.codeStyle = context.codeStyle;
        this.diffs = context.diffs;
        this.braces = context.braces;
    }
    
    /*package local*/ void indentPreprocessor(Token<CppTokenId> previous) {
        TokenSequence<CppTokenId> prep = ts.embedded(CppTokenId.languagePreproc());
        if (prep == null){
            return;
        }
        prep.moveStart();
        while (prep.moveNext()) {
            if (!(prep.token().id() == WHITESPACE ||
                    prep.token().id() == PREPROCESSOR_START)) {
                break;
            }
        }
        Token<CppTokenId> directive = null;
        boolean atSharp = false;
        if (prep.token() != null) {
            directive = prep.token();
        }
        if (directive != null) {
            switch (directive.id()) {
                case PREPROCESSOR_ELSE: //("else", "preprocessor-keyword-directive"),
                case PREPROCESSOR_ELIF: //("elif", "preprocessor-keyword-directive"),
                    prepocessorDepth--;
                    if (!stateStack.empty()){
                        braces.reset(stateStack.pop());
                    }
                    break;
                case PREPROCESSOR_ENDIF: //("endif", "preprocessor-keyword-directive"),
                    prepocessorDepth--;
                    if (!stateStack.empty()){
                        stateStack.pop();
                    }
                    break;
            }
            if (context.doFormat()) {
                while(prep.movePrevious()) {
                    if (prep.token().id() == PREPROCESSOR_START) {
                        atSharp = true;
                        break;
                    }
                }
            }
        }
        if (atSharp) {
            selectPreprocessorIndent(previous, prep);
        }
        if (directive != null) {
            switch (directive.id()) {
                case PREPROCESSOR_IF: //("if", "preprocessor-keyword-directive"),
                case PREPROCESSOR_IFDEF: //("ifdef", "preprocessor-keyword-directive"),
                case PREPROCESSOR_IFNDEF: //("ifndef", "preprocessor-keyword-directive"),
                    prepocessorDepth++;
                    stateStack.push(braces.clone());
                    break;
                case PREPROCESSOR_ELSE: //("else", "preprocessor-keyword-directive"),
                case PREPROCESSOR_ELIF: //("elif", "preprocessor-keyword-directive"),
                    prepocessorDepth++;
                    stateStack.push(braces.clone());
                    break;
            }
        }
    }
    
    private void selectPreprocessorIndent(Token<CppTokenId> previous, TokenSequence<CppTokenId> prep) {
        Token<CppTokenId> next = null;
        if (prep.moveNext()) {
            next = prep.token();
            prep.movePrevious();
        }
        switch(codeStyle.indentPreprocessorDirectives()) {
            case CODE_INDENT:
                 indentByCode(previous, prep, next);
                 break;
            case START_LINE:
                 noIndent(previous, prep, next);
                 break;
            case PREPROCESSOR_INDENT:
                 indentByPreprocessor(previous, prep, next);
                 break;
        }
    }

    private void noIndent(Token<CppTokenId> previous, TokenSequence<CppTokenId> prep, Token<CppTokenId> next) {
        indentBefore(previous, ""); // NOI18N
        indentAfter(prep, next, ""); // NOI18N
    }

    private void indentBefore(Token<CppTokenId> previous, String spaces) {
        DiffResult diff = diffs.getDiffs(ts, -1);
        if (diff != null) {
            if (diff.after != null) {
                diff.after.replaceSpaces(spaces); // NOI18N
                if (diff.replace != null && !diff.after.hasNewLine()){
                    diff.replace.replaceSpaces(""); // NOI18N
                }
                return;
            } else if (diff.replace != null) {
                diff.replace.replaceSpaces(spaces); // NOI18N
                return;
            }
        }
        if (previous != null && previous.id() == WHITESPACE) {
            if (!spaces.equals(previous.text().toString())){
                ts.replacePrevious(previous, spaces);
            }
        } else {
            if (spaces.length()>0){
                ts.addBeforeCurrent(spaces);
            }
        }
    }

    private void indentAfter(TokenSequence<CppTokenId> prep, Token<CppTokenId> next, String spaces) {
        if (next.id() == WHITESPACE) {
            if (!spaces.equals(next.text().toString())){
                diffs.addFirst(prep.offset() + prep.token().length(),
                               prep.offset() + prep.token().length() + next.length(), spaces);
            }
        } else {
            if (spaces.length() > 0) {
                diffs.addFirst(prep.offset()+ prep.token().length(),
                               prep.offset()+ prep.token().length(), spaces);
            }
        }
    }


    private void indentByCode(Token<CppTokenId> previous, TokenSequence<CppTokenId> prep, Token<CppTokenId> next) {
        if (codeStyle.sharpAtStartLine()) {
            indentBefore(previous, ""); // NOI18N
            indentAfter(prep, next, context.getIndent("")); // NOI18N
        } else {
            indentBefore(previous, context.getIndent("")); // NOI18N
            indentAfter(prep, next, ""); // NOI18N
        }
    }

    private void indentByPreprocessor(Token<CppTokenId> previous, TokenSequence<CppTokenId> prep, Token<CppTokenId> next) {
        if (codeStyle.sharpAtStartLine()) {
            indentBefore(previous, ""); // NOI18N
            indentAfter(prep, next, getPreprocessorIndent("", prepocessorDepth)); // NOI18N
        } else {
            indentBefore(previous, getPreprocessorIndent("", prepocessorDepth)); // NOI18N
            indentAfter(prep, next, ""); // NOI18N
        }
    }

    private String getPreprocessorIndent(String prefix, int shift) {
        if (shift > 0) {
            return context.spaces(prefix, shift * codeStyle.getGlobalIndentSize());
        } else {
            return prefix;
        }
    }
}
