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
package org.netbeans.modules.javascript2.editor.formatter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.editor.model.JsElement;

/**
 *
 * @author Petr Hejl
 */
public final class FormatToken {

    private static final Set<Kind> BEFORE_MARKERS = EnumSet.noneOf(Kind.class);

    static {
        Collections.addAll(BEFORE_MARKERS, Kind.BEFORE_BINARY_OPERATOR,
                Kind.BEFORE_ASSIGNMENT_OPERATOR, Kind.BEFORE_COMMA,
                Kind.BEFORE_WHILE_KEYWORD, Kind.BEFORE_ELSE_KEYWORD,
                Kind.BEFORE_CATCH_KEYWORD, Kind.BEFORE_FINALLY_KEYWORD,
                Kind.BEFORE_SEMICOLON, Kind.BEFORE_UNARY_OPERATOR,
                Kind.BEFORE_TERNARY_OPERATOR, Kind.BEFORE_FUNCTION_DECLARATION,
                Kind.BEFORE_FUNCTION_CALL, Kind.BEFORE_FUNCTION_DECLARATION_PARENTHESIS,
                Kind.BEFORE_IF_PARENTHESIS, Kind.BEFORE_WHILE_PARENTHESIS,
                Kind.BEFORE_FOR_PARENTHESIS, Kind.BEFORE_WITH_PARENTHESIS,
                Kind.BEFORE_SWITCH_PARENTHESIS, Kind.BEFORE_CATCH_PARENTHESIS,
                Kind.BEFORE_RIGHT_PARENTHESIS, Kind.BEFORE_FUNCTION_DECLARATION_BRACE,
                Kind.BEFORE_IF_BRACE, Kind.BEFORE_ELSE_BRACE,
                Kind.BEFORE_WHILE_BRACE, Kind.BEFORE_FOR_BRACE,
                Kind.BEFORE_DO_BRACE, Kind.BEFORE_SWITCH_BRACE,
                Kind.BEFORE_TRY_BRACE, Kind.BEFORE_CATCH_BRACE,
                Kind.BEFORE_FINALLY_BRACE, Kind.BEFORE_ARRAY_LITERAL_BRACKET);
    }

    private final Kind kind;

    private final int offset;

    private final CharSequence text;

    private FormatToken next;

    private FormatToken previous;

    private FormatToken(Kind kind, int offset, CharSequence text) {
        this.kind = kind;
        this.offset = offset;
        this.text = text;
    }

    public static FormatToken forText(int offset, CharSequence text) {
        return new FormatToken(Kind.TEXT, offset, text);
    }

    public static FormatToken forFormat(Kind kind) {
        return new FormatToken(kind, -1, null);
    }

    public static FormatToken forAny(Kind kind, int offset, CharSequence text) {
        return new FormatToken(kind, offset, text);
    }

    @NonNull
    public Kind getKind() {
        return kind;
    }

    @CheckForNull
    public CharSequence getText() {
        return text;
    }

    public int getOffset() {
        return offset;
    }

    @CheckForNull
    public FormatToken next() {
        return next;
    }

    @CheckForNull
    public FormatToken previous() {
        return previous;
    }

    public boolean isVirtual() {
        return offset < 0;
    }

    public boolean isBeforeMarker() {
        return BEFORE_MARKERS.contains(kind);
    }

    public boolean isIndentationMarker() {
        return Kind.INDENTATION_INC == kind || Kind.INDENTATION_DEC == kind
                || Kind.ELSE_IF_INDENTATION_INC == kind || Kind.ELSE_IF_INDENTATION_DEC == kind;
    }

    @Override
    public String toString() {
        return "FormattingToken{" + "kind=" + kind + ", offset=" + offset + ", text=" + text + '}';
    }

    void setNext(FormatToken next) {
        this.next = next;
    }

    void setPrevious(FormatToken previous) {
        this.previous = previous;
    }
    
    public static enum Kind {
        SOURCE_START,
        TEXT,
        WHITESPACE,
        EOL,

        LINE_COMMENT,
        DOC_COMMENT,
        BLOCK_COMMENT,

        INDENTATION_INC,
        ELSE_IF_INDENTATION_INC,
        INDENTATION_DEC,
        ELSE_IF_INDENTATION_DEC,

        AFTER_STATEMENT,
        AFTER_PROPERTY,
        AFTER_CASE,
        
        AFTER_BLOCK_START,
        ELSE_IF_AFTER_BLOCK_START,
        
        BEFORE_OBJECT,

        // around binary operator
        BEFORE_BINARY_OPERATOR,
        AFTER_BINARY_OPERATOR,

        // around assignment operator
        BEFORE_ASSIGNMENT_OPERATOR,
        AFTER_ASSIGNMENT_OPERATOR,

        // around comma
        BEFORE_COMMA,
        AFTER_COMMA,

        // keywords with possible space before parentheses
        AFTER_IF_KEYWORD,
        AFTER_WHILE_KEYWORD,
        AFTER_FOR_KEYWORD,
        AFTER_WITH_KEYWORD,
        AFTER_SWITCH_KEYWORD,
        AFTER_CATCH_KEYWORD,

        // keywords with possible space before
        BEFORE_WHILE_KEYWORD,
        BEFORE_ELSE_KEYWORD,
        BEFORE_CATCH_KEYWORD,
        BEFORE_FINALLY_KEYWORD,

        BEFORE_SEMICOLON,
        AFTER_SEMICOLON,

        BEFORE_UNARY_OPERATOR,
        AFTER_UNARY_OPERATOR,

        BEFORE_TERNARY_OPERATOR,
        AFTER_TERNARY_OPERATOR,

        BEFORE_FUNCTION_DECLARATION,
        AFTER_FUNCTION_DECLARATION,

        BEFORE_FUNCTION_CALL,

        // within parentheses
        AFTER_FUNCTION_DECLARATION_PARENTHESIS,
        BEFORE_FUNCTION_DECLARATION_PARENTHESIS,

        AFTER_FUNCTION_CALL_PARENTHESIS,
        BEFORE_FUNCTION_CALL_PARENTHESIS,

        BEFORE_IF_PARENTHESIS,
        AFTER_IF_PARENTHESIS,

        BEFORE_WHILE_PARENTHESIS,
        AFTER_WHILE_PARENTHESIS,

        BEFORE_FOR_PARENTHESIS,
        AFTER_FOR_PARENTHESIS,

        BEFORE_WITH_PARENTHESIS,
        AFTER_WITH_PARENTHESIS,

        BEFORE_SWITCH_PARENTHESIS,
        AFTER_SWITCH_PARENTHESIS,

        BEFORE_CATCH_PARENTHESIS,
        AFTER_CATCH_PARENTHESIS,

        BEFORE_RIGHT_PARENTHESIS,
        AFTER_LEFT_PARENTHESIS,

        // before braces
        BEFORE_FUNCTION_DECLARATION_BRACE,
        BEFORE_IF_BRACE,
        BEFORE_ELSE_BRACE,
        BEFORE_WHILE_BRACE,
        BEFORE_FOR_BRACE,
        BEFORE_DO_BRACE,
        BEFORE_SWITCH_BRACE,
        BEFORE_TRY_BRACE,
        BEFORE_CATCH_BRACE,
        BEFORE_FINALLY_BRACE,

        // array literal brackets
        AFTER_ARRAY_LITERAL_BRACKET,
        BEFORE_ARRAY_LITERAL_BRACKET
    }

}
