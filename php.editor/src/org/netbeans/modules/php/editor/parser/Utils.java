/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.editor.parser;

import javax.swing.text.BadLocationException;

/**
 *
 * @author Petr Pisl
 */
public class Utils {

    /**
     * This method is mainly used for debugging purpose.
     * 
     * @param id token id
     * @return text representation for the token
     */
    public static String getASTScannerTokenName(int id) {
        String name;
        switch (id) {
            case ASTPHP5Symbols.EOF:
                name = "EOF";
                break;
            case ASTPHP5Symbols.T_ABSTRACT:
                name = "T_ABSTRACT";
                break;
            case ASTPHP5Symbols.T_AND_EQUAL:
                name = "T_AND_EQUAL";
                break;
            case ASTPHP5Symbols.T_ARRAY:
                name = "T_ARRAY";
                break;
            case ASTPHP5Symbols.T_ARRAY_CAST:
                name = "T_ARRAY_CAST";
                break;
            case ASTPHP5Symbols.T_AS:
                name = "T_AS";
                break;
            case ASTPHP5Symbols.T_AT:
                name = "T_AT";
                break;
            case ASTPHP5Symbols.T_BACKQUATE:
                name = "T_BACKQUATE";
                break;
            case ASTPHP5Symbols.T_BOOLEAN_AND:
                name = "T_BOOLEAN_AND";
                break;
            case ASTPHP5Symbols.T_BOOLEAN_OR:
                name = "T_BOOLEAN_OR";
                break;
            case ASTPHP5Symbols.T_BOOL_CAST:
                name = "T_BOOL_CAST";
                break;
            case ASTPHP5Symbols.T_BREAK:
                name = "T_BREAK";
                break;
            case ASTPHP5Symbols.T_CASE:
                name = "T_CASE";
                break;
            case ASTPHP5Symbols.T_CATCH:
                name = "T_CATCH";
                break;
            case ASTPHP5Symbols.T_CLASS:
                name = "T_CLASS";
                break;
            case ASTPHP5Symbols.T_CLASS_C:
                name = "T_CLASS_C";
                break;
            case ASTPHP5Symbols.T_CLONE:
                name = "T_CLONE";
                break;
            case ASTPHP5Symbols.T_CLOSE_PARENTHESE:
                name = "T_CLOSE_PARENTHESE";
                break;
            case ASTPHP5Symbols.T_CLOSE_RECT:
                name = "T_CLOSE_RECT";
                break;
            case ASTPHP5Symbols.T_COMMA:
                name = "T_COMMA";
                break;
            case ASTPHP5Symbols.T_CONCAT_EQUAL:
                name = "T_CONCAT_EQUAL";
                break;
            case ASTPHP5Symbols.T_CONST:
                name = "T_CONST";
                break;
            case ASTPHP5Symbols.T_CONSTANT_ENCAPSED_STRING:
                name = "T_CONSTANT_ENCAPSED_STRING";
                break;
            case ASTPHP5Symbols.T_CONTINUE:
                name = "T_CONTINUE";
                break;
            case ASTPHP5Symbols.T_CURLY_CLOSE:
                name = "T_CURLY_CLOSE";
                break;
            case ASTPHP5Symbols.T_CURLY_OPEN:
                name = "T_CURLY_OPEN";
                break;
            case ASTPHP5Symbols.T_CURLY_OPEN_WITH_DOLAR:
                name = "T_CURLY_OPEN_WITH_DOLAR";
                break;
            case ASTPHP5Symbols.T_DEC:
                name = "T_DEC";
                break;
            case ASTPHP5Symbols.T_DECLARE:
                name = "T_DECLARE";
                break;
            case ASTPHP5Symbols.T_DEFAULT:
                name = "T_DEFAULT";
                break;
            case ASTPHP5Symbols.T_DEFINE:
                name = "T_DEFINE";
                break;
            case ASTPHP5Symbols.T_DIV:
                name = "T_DIV";
                break;
            case ASTPHP5Symbols.T_DIV_EQUAL:
                name = "T_DIV_EQUAL";
                break;
            case ASTPHP5Symbols.T_DNUMBER:
                name = "T_DNUMBER";
                break;
            case ASTPHP5Symbols.T_DO:
                name = "T_DO";
                break;
            case ASTPHP5Symbols.T_DOLLAR:
                name = "T_DOLLAR";
                break;
            case ASTPHP5Symbols.T_DOLLAR_OPEN_CURLY_BRACES:
                name = "T_DOLLAR_OPEN_CURLY_BRACES";
                break;
            case ASTPHP5Symbols.T_DOUBLE_ARROW:
                name = "T_DOUBLE_ARROW";
                break;
            case ASTPHP5Symbols.T_DOUBLE_CAST:
                name = "T_DOUBLE_CAST";
                break;
            case ASTPHP5Symbols.T_ECHO:
                name = "T_ECHO";
                break;
            case ASTPHP5Symbols.T_ELSE:
                name = "T_ELSE";
                break;
            case ASTPHP5Symbols.T_ELSEIF:
                name = "T_ELSEIF";
                break;
            case ASTPHP5Symbols.T_EMPTY:
                name = "T_EMPTY";
                break;
            case ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE:
                name = "T_ENCAPSED_AND_WHITESPACE";
                break;
            case ASTPHP5Symbols.T_ENDDECLARE:
                name = "T_ENDDECLARE";
                break;
            case ASTPHP5Symbols.T_ENDFOR:
                name = "T_ENDFOR";
                break;
            case ASTPHP5Symbols.T_ENDFOREACH:
                name = "T_ENDFOREACH";
                break;
            case ASTPHP5Symbols.T_ENDIF:
                name = "T_ENDIF";
                break;
            case ASTPHP5Symbols.T_ENDSWITCH:
                name = "T_ENDSWITCH";
                break;
            case ASTPHP5Symbols.T_ENDWHILE:
                name = "T_ENDWHILEnejdu.";
                break;
            case ASTPHP5Symbols.T_END_HEREDOC:
                name = "T_END_HEREDOC";
                break;
            case ASTPHP5Symbols.T_END_NOWDOC:
                name = "T_END_NOWDOC";
                break;
            case ASTPHP5Symbols.T_EQUAL:
                name = "T_EQUAL";
                break;
            case ASTPHP5Symbols.T_EVAL:
                name = "T_EVAL";
                break;
            case ASTPHP5Symbols.T_EXIT:
                name = "T_EXIT";
                break;
            case ASTPHP5Symbols.T_EXTENDS:
                name = "T_EXTENDS";
                break;
            case ASTPHP5Symbols.T_FILE:
                name = "T_FILE";
                break;
            case ASTPHP5Symbols.T_FINAL:
                name = "T_FINAL";
                break;
            case ASTPHP5Symbols.T_FOR:
                name = "T_FOR";
                break;
            case ASTPHP5Symbols.T_FOREACH:
                name = "T_FOREACH";
                break;
            case ASTPHP5Symbols.T_FUNCTION:
                name = "T_FUNCTION";
                break;
            case ASTPHP5Symbols.T_FUNC_C:
                name = "T_FUNC_C";
                break;
            case ASTPHP5Symbols.T_GLOBAL:
                name = "T_GLOBAL";
                break;
            case ASTPHP5Symbols.T_HALT_COMPILER:
                name = "T_HALT_COMPILER";
                break;
            case ASTPHP5Symbols.T_IF:
                name = "T_IF";
                break;
            case ASTPHP5Symbols.T_IMPLEMENTS:
                name = "T_IMPLEMENTS";
                break;
            case ASTPHP5Symbols.T_INC:
                name = "T_INC";
                break;
            case ASTPHP5Symbols.T_INCLUDE:
                name = "T_INCLUDE";
                break;
            case ASTPHP5Symbols.T_INCLUDE_ONCE:
                name = "T_INCLUDE_ONCE";
                break;
            case ASTPHP5Symbols.T_INLINE_HTML:
                name = "T_INLINE_HTML";
                break;
            case ASTPHP5Symbols.T_INSTANCEOF:
                name = "T_INSTANCEOF";
                break;
            case ASTPHP5Symbols.T_INTERFACE:
                name = "T_INTERFACE";
                break;
            case ASTPHP5Symbols.T_INT_CAST:
                name = "T_INT_CAST";
                break;
            case ASTPHP5Symbols.T_ISSET:
                name = "T_ISSET";
                break;
            case ASTPHP5Symbols.T_IS_EQUAL:
                name = "T_IS_EQUAL";
                break;
            case ASTPHP5Symbols.T_IS_GREATER_OR_EQUAL:
                name = "T_IS_GREATER_OR_EQUAL";
                break;
            case ASTPHP5Symbols.T_IS_IDENTICAL:
                name = "T_IS_IDENTICAL";
                break;
            case ASTPHP5Symbols.T_IS_NOT_EQUAL:
                name = "T_IS_NOT_EQUAL";
                break;
            case ASTPHP5Symbols.T_IS_NOT_IDENTICAL:
                name = "T_IS_NOT_IDENTICAL";
                break;
            case ASTPHP5Symbols.T_IS_SMALLER_OR_EQUAL:
                name = "T_IS_SMALLER_OR_EQUAL";
                break;
            case ASTPHP5Symbols.T_KOVA:
                name = "T_KOVA";
                break;
            case ASTPHP5Symbols.T_LGREATER:
                name = "T_LGREATER";
                break;
            case ASTPHP5Symbols.T_LINE:
                name = "T_LINE";
                break;
            case ASTPHP5Symbols.T_LIST:
                name = "T_LIST";
                break;
            case ASTPHP5Symbols.T_LNUMBER:
                name = "T_LNUMBER";
                break;
            case ASTPHP5Symbols.T_LOGICAL_AND:
                name = "T_LOGICAL_AND";
                break;
            case ASTPHP5Symbols.T_LOGICAL_OR:
                name = "T_LOGICAL_OR";
                break;
            case ASTPHP5Symbols.T_LOGICAL_XOR:
                name = "T_LOGICAL_XOR";
                break;
            case ASTPHP5Symbols.T_METHOD_C:
                name = "T_METHOD_C";
                break;
            case ASTPHP5Symbols.T_MINUS:
                name = "T_MINUS";
                break;
            case ASTPHP5Symbols.T_MINUS_EQUAL:
                name = "T_MINUS_EQUAL";
                break;
            case ASTPHP5Symbols.T_MOD_EQUAL:
                name = "T_MOD_EQUAL";
                break;
            case ASTPHP5Symbols.T_MUL_EQUAL:
                name = "T_MUL_EQUAL";
                break;
            case ASTPHP5Symbols.T_NEKUDA:
                name = "T_NEKUDA";
                break;
            case ASTPHP5Symbols.T_NEKUDOTAIM:
                name = "T_NEKUDOTAIM";
                break;
            case ASTPHP5Symbols.T_NEW:
                name = "T_NEW";
                break;
            case ASTPHP5Symbols.T_NOT:
                name = "T_NOT";
                break;
            case ASTPHP5Symbols.T_NUM_STRING:
                name = "T_NUM_STRING";
                break;
            case ASTPHP5Symbols.T_OBJECT_CAST:
                name = "T_OBJECT_CAST";
                break;
            case ASTPHP5Symbols.T_OBJECT_OPERATOR:
                name = "T_OBJECT_OPERATOR";
                break;
            case ASTPHP5Symbols.T_OPEN_PARENTHESE:
                name = "T_OPEN_PARENTHESE";
                break;
            case ASTPHP5Symbols.T_OPEN_RECT:
                name = "T_OPEN_RECT";
                break;
            case ASTPHP5Symbols.T_OPEN_TAG:
                name = "T_OPEN_TAG";
                break;
            case ASTPHP5Symbols.T_OR:
                name = "T_OR";
                break;
            case ASTPHP5Symbols.T_OR_EQUAL:
                name = "T_OR_EQUAL";
                break;
            case ASTPHP5Symbols.T_PAAMAYIM_NEKUDOTAYIM:
                name = "T_PAAMAYIM_NEKUDOTAYIM";
                break;
            case ASTPHP5Symbols.T_PLUS:
                name = "T_PLUS";
                break;
            case ASTPHP5Symbols.T_PLUS_EQUAL:
                name = "T_PLUS_EQUAL";
                break;
            case ASTPHP5Symbols.T_PRECENT:
                name = "T_PRECENT";
                break;
            case ASTPHP5Symbols.T_PRINT:
                name = "T_PRINT";
                break;
            case ASTPHP5Symbols.T_PRIVATE:
                name = "T_PRIVATE";
                break;
            case ASTPHP5Symbols.T_PROTECTED:
                name = "T_PROTECTED";
                break;
            case ASTPHP5Symbols.T_PUBLIC:
                name = "T_PUBLIC";
                break;
            case ASTPHP5Symbols.T_QUATE:
                name = "T_QUATE";
                break;
            case ASTPHP5Symbols.T_QUESTION_MARK:
                name = "T_QUESTION_MARK";
                break;
            case ASTPHP5Symbols.T_REFERENCE:
                name = "T_REFERENCE";
                break;
            case ASTPHP5Symbols.T_REQUIRE:
                name = "T_REQUIRE";
                break;
            case ASTPHP5Symbols.T_REQUIRE_ONCE:
                name = "T_REQUIRE_ONCE";
                break;
            case ASTPHP5Symbols.T_RETURN:
                name = "T_RETURN";
                break;
            case ASTPHP5Symbols.T_RGREATER:
                name = "T_RGREATER";
                break;
            case ASTPHP5Symbols.T_SEMICOLON:
                name = "T_SEMICOLON";
                break;
            case ASTPHP5Symbols.T_SL:
                name = "T_SL";
                break;
            case ASTPHP5Symbols.T_SL_EQUAL:
                name = "T_SL_EQUAL";
                break;
            case ASTPHP5Symbols.T_SR:
                name = "T_SR";
                break;
            case ASTPHP5Symbols.T_SR_EQUAL:
                name = "T_SR_EQUAL";
                break;
            case ASTPHP5Symbols.T_START_HEREDOC:
                name = "T_START_HEREDOC";
                break;
            case ASTPHP5Symbols.T_START_NOWDOC:
                name = "T_START_NOWDOC";
                break;
            case ASTPHP5Symbols.T_STATIC:
                name = "T_STATIC";
                break;
            case ASTPHP5Symbols.T_STRING:
                name = "T_STRING";
                break;
            case ASTPHP5Symbols.T_STRING_CAST:
                name = "T_STRING_CAST";
                break;
            case ASTPHP5Symbols.T_STRING_VARNAME:
                name = "T_STRING_VARNAME";
                break;
            case ASTPHP5Symbols.T_SWITCH:
                name = "T_SWITCH";
                break;
            case ASTPHP5Symbols.T_THROW:
                name = "T_THROW";
                break;
            case ASTPHP5Symbols.T_TILDA:
                name = "T_TILDA";
                break;
            case ASTPHP5Symbols.T_TIMES:
                name = "T_TIMES";
                break;
            case ASTPHP5Symbols.T_TRY:
                name = "T_TRY";
                break;
            case ASTPHP5Symbols.T_UNSET:
                name = "T_UNSET";
                break;
            case ASTPHP5Symbols.T_UNSET_CAST:
                name = "T_UNSET_CAST";
                break;
            case ASTPHP5Symbols.T_USE:
                name = "T_USE";
                break;
            case ASTPHP5Symbols.T_VAR:
                name = "T_VAR";
                break;
            case ASTPHP5Symbols.T_VARIABLE:
                name = "T_VARIABLE";
                break;
            case ASTPHP5Symbols.T_VAR_COMMENT:
                name = "T_VAR_COMMENT";
                break;
            case ASTPHP5Symbols.T_WHILE:
                name = "T_WHILE";
                break;
            case ASTPHP5Symbols.T_XOR_EQUAL:
                name = "T_XOR_EQUAL";
                break;
            default:
                name = "unknown";
        }
        return name;
    }

    public static String getSpaces(int length) {
        StringBuffer sb = new StringBuffer(length);
        for (int index = 0; index < length; index++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String getRepeatingChars(char c, int length) {
        StringBuffer sb = new StringBuffer(length);
        for (int index = 0; index < length; index++) {
            sb.append(c);
        }
        return sb.toString();
    }
    
    public static int getRowStart(String text, int offset) {
        // Search backwards
        for (int i = offset - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '\n') {
                return i + 1;
            }
        }
        return 0;
    }

    public static int getRowEnd(String text, int offset) {
        int i = offset - 1;
        if (i < 0 ) {
            return 0;
        }
        for (; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                return i;
            }
        }
        return i;
    }
}
