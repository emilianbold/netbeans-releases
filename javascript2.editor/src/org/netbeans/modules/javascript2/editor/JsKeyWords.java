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

/**
 *
 * @author Petr Pisl
 *
 */

public class JsKeyWords {

    protected static enum CompletionType {
        CURSOR_INSIDE_BRACKETS,
        ENDS_WITH_CURLY_BRACKETS,
        ENDS_WITH_SPACE,
        ENDS_WITH_SEMICOLON,
        ENDS_WITH_COLON
    };

    protected final static Map<String,CompletionType> KEYWORDS = new HashMap<String, CompletionType>();
    static {
        KEYWORDS.put("break", CompletionType.ENDS_WITH_SEMICOLON);
        KEYWORDS.put("case", CompletionType.ENDS_WITH_COLON);
        KEYWORDS.put("catch", CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put("continue",  CompletionType.ENDS_WITH_SEMICOLON);
        KEYWORDS.put("default", CompletionType.ENDS_WITH_COLON);
        KEYWORDS.put("delete",  CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put("do", CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put("else", CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put("finally", CompletionType.ENDS_WITH_CURLY_BRACKETS);
        KEYWORDS.put("for", CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put("function", CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put("if", CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put("in", CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put("instanceof",  CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put("new", CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put("return",  CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put("switch", CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put("throw", CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put("try",  CompletionType.ENDS_WITH_CURLY_BRACKETS);
        KEYWORDS.put("typeof", CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put("var", CompletionType.ENDS_WITH_SPACE);
        KEYWORDS.put("while", CompletionType.CURSOR_INSIDE_BRACKETS);
        KEYWORDS.put("with",  CompletionType.ENDS_WITH_SPACE);
    }
}
