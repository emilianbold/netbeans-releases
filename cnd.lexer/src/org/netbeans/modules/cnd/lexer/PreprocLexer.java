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

package org.netbeans.modules.cnd.lexer;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class PreprocLexer extends CndLexer {
    private static final int INIT               = 0;
    private static final int DIRECTIVE_NAME     = INIT + 1;
    private static final int MACRO_NAME         = DIRECTIVE_NAME + 1;
    private static final int MACRO_BODY         = MACRO_NAME + 1;
    private static final int EXPRESSION         = MACRO_BODY + 1;
    private static final int INCLUDE_DIRECTIVE  = EXPRESSION + 1;
    private static final int OTHER              = INCLUDE_DIRECTIVE + 1;
    private static final int ERROR              = OTHER + 1;
    
    private static final String WHITESPACE_CATEGORY = CppTokenId.WHITESPACE.primaryCategory();
    private static final String COMMENT_CATEGORY = CppTokenId.LINE_COMMENT.primaryCategory();
    private static final String PREPROC_KEYWORD_CATEGORY = CppTokenId.PREPROCESSOR_PRAGMA.primaryCategory();
    
    private int state = INIT;
    private final Filter<CppTokenId> preprocFilter;

    public PreprocLexer(LexerRestartInfo<CppTokenId> info) {
        super(info);
        this.preprocFilter = CndLexerUtilities.getPreprocFilter();
        fromState(info.state()); // last line in contstructor
    }

    @Override
    public Object state() {
        return Integer.valueOf(state);
    }
    
    private void fromState(Object state) {
        state = state == null ? INIT : ((Integer)state).intValue();
    }

    @Override
    protected Token<CppTokenId> finishSharp() {
        if (state == INIT) { 
            // the first sharp in preprocessor directive has own id            
            return token(CppTokenId.PREPROCESSOR_START);
        }
        return super.finishSharp();
    }

    @SuppressWarnings("fallthrough")
    @Override
    protected Token<CppTokenId> finishDblQuote() {
        if (state == INCLUDE_DIRECTIVE) {
            while (true) { // user include literal
                switch (read(true)) {
                    case '"': // NOI18N
                        return token(CppTokenId.PREPROCESSOR_USER_INCLUDE);
                    case '\r':
                        consumeNewline();
                    case '\n':
                    case EOF:
                        return tokenPart(CppTokenId.PREPROCESSOR_USER_INCLUDE, PartType.START);
                }
            }              
        }
        return super.finishDblQuote();
    }

    @SuppressWarnings("fallthrough")
    @Override
    protected Token<CppTokenId> finishLT() {
        if (state == INCLUDE_DIRECTIVE) {
            while (true) { // system include literal
                switch (read(true)) {
                    case '>': // NOI18N
                        return token(CppTokenId.PREPROCESSOR_SYS_INCLUDE);
                    case '\r':
                        consumeNewline();
                    case '\n':
                    case EOF:
                        return tokenPart(CppTokenId.PREPROCESSOR_SYS_INCLUDE, PartType.START);
                }
            }              
        }        
        return super.finishLT();
    }
    
    @Override
    protected CppTokenId getKeywordOrIdentifierID(CharSequence text) {
        CppTokenId id = null;
        if (state == DIRECTIVE_NAME) {
            id = preprocFilter.check(text);
        } else if (state == EXPRESSION) {
            if (CharSequenceUtilities.textEquals(CppTokenId.PREPROCESSOR_DEFINED.fixedText(), text)) {
                id = CppTokenId.PREPROCESSOR_DEFINED;
            }
        }
        return id != null ? id : CppTokenId.PREPROCESSOR_IDENTIFIER;
    }

    @Override
    protected void postTokenCreate(CppTokenId id) {
        assert id != null;
        switch (state) { // change state of lexer
            case INIT:
                assert id == CppTokenId.PREPROCESSOR_START : 
                    "in INIT state only CppTokenId.PREPROCESSOR_START is possible: " + id; //NOI18N
                state = DIRECTIVE_NAME;
                break;
            case DIRECTIVE_NAME:
                if (PREPROC_KEYWORD_CATEGORY.equals(id.primaryCategory())) {
                    if (id == CppTokenId.PREPROCESSOR_DEFINE ||
                        id == CppTokenId.PREPROCESSOR_UNDEF) {
                        state = MACRO_NAME;
                    }
                } else if (!WHITESPACE_CATEGORY.equals(id.primaryCategory()) &&
                           !COMMENT_CATEGORY.equals(id.primaryCategory())) {
                    state = OTHER;
                }
                break;
            case MACRO_NAME:
                if (!WHITESPACE_CATEGORY.equals(id.primaryCategory()) &&
                        !COMMENT_CATEGORY.equals(id.primaryCategory())) {
                    state = MACRO_BODY;
                }
                break;
            case MACRO_BODY:                
            case INCLUDE_DIRECTIVE:                
            case EXPRESSION:                
            case OTHER:                
            case ERROR:
                // do not change state
        }
    }
}
