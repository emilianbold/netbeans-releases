/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;

/**
 *
 * @author Petr Pisl
 *
 */

public class JsKeyWords {

    protected static enum CompletionType {
        SIMPLE,
        CURSOR_INSIDE_BRACKETS,
        ENDS_WITH_CURLY_BRACKETS,
        ENDS_WITH_SPACE,
        ENDS_WITH_SEMICOLON,
        ENDS_WITH_COLON,
        ENDS_WITH_DOT
    };

    protected final static Map<String, CompletionType> KEYWORDS = new HashMap<String, CompletionType>();
    static {
        KEYWORDS.put(JsTokenId.KEYWORD_BREAK.fixedText(), CompletionType.ENDS_WITH_SEMICOLON);
        KEYWORDS.put(JsTokenId.KEYWORD_CASE.fixedText(), CompletionType.ENDS_WITH_COLON);
        KEYWORDS.put(JsTokenId.KEYWORD_CATCH.fixedText(), CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_CONTINUE.fixedText(), CompletionType.ENDS_WITH_SEMICOLON);
        KEYWORDS.put(JsTokenId.KEYWORD_DEBUGGER.fixedText(), CompletionType.ENDS_WITH_SEMICOLON);
        KEYWORDS.put(JsTokenId.KEYWORD_DEFAULT.fixedText(), CompletionType.ENDS_WITH_COLON);
        KEYWORDS.put(JsTokenId.KEYWORD_DELETE.fixedText(), CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_DO.fixedText(), CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_ELSE.fixedText(), CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_FALSE.fixedText(), CompletionType.SIMPLE);
        KEYWORDS.put(JsTokenId.KEYWORD_FINALLY.fixedText(), CompletionType.ENDS_WITH_CURLY_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_FOR.fixedText(), CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_FUNCTION.fixedText(), CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put(JsTokenId.KEYWORD_IF.fixedText(), CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_IN.fixedText(), CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put(JsTokenId.KEYWORD_INSTANCEOF.fixedText(), CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put(JsTokenId.KEYWORD_NEW.fixedText(), CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put(JsTokenId.KEYWORD_RETURN.fixedText(), CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put(JsTokenId.KEYWORD_SWITCH.fixedText(), CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_THIS.fixedText(), CompletionType.ENDS_WITH_DOT);
        KEYWORDS.put(JsTokenId.KEYWORD_THROW.fixedText(), CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put(JsTokenId.KEYWORD_TRUE.fixedText(), CompletionType.SIMPLE);
        KEYWORDS.put(JsTokenId.KEYWORD_TRY.fixedText(), CompletionType.ENDS_WITH_CURLY_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_TYPEOF.fixedText(), CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put(JsTokenId.KEYWORD_VAR.fixedText(), CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put(JsTokenId.KEYWORD_VOID.fixedText(), CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put(JsTokenId.KEYWORD_WHILE.fixedText(), CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put(JsTokenId.KEYWORD_WITH.fixedText(), CompletionType.ENDS_WITH_SPACE);
    }
}
