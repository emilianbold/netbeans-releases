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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.editing;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tor Norbye
 */
public class JsUtils {

    private JsUtils() {
    }

    public static boolean isJsFile(FileObject f) {
        return JsMimeResolver.JAVASCRIPT_MIME_TYPE.equals(f.getMIMEType());
    }

    public static boolean isJsDocument(Document doc) {
        String mimeType = (String)doc.getProperty("mimeType");

        return JsMimeResolver.JAVASCRIPT_MIME_TYPE.equals(mimeType);
    }

    public static final String RHTML_MIME_TYPE = "application/x-httpd-eruby"; // NOI18N
    
    public static boolean isRhtmlDocument(Document doc) {
        String mimeType = (String)doc.getProperty("mimeType");

        return RHTML_MIME_TYPE.equals(mimeType);
    }
    
    /** Is this name a valid operator name? */
    public static boolean isOperator(String name) {
        // TODO - this must be rewritten for Ruby; see ECMA-262 section 7.7
        if (name.length() == 0) {
            return false;
        }
        // Pieced together from various sources (JsYaccLexer, DefaultJsParser, ...)
        switch (name.charAt(0)) {
        case '+':
            return name.equals("+") || name.equals("+@");
        case '-':
            return name.equals("-") || name.equals("-@");
        case '*':
            return name.equals("*") || name.equals("**");
        case '<':
            return name.equals("<") || name.equals("<<") || name.equals("<=") || name.equals("<=>");
        case '>':
            return name.equals(">") || name.equals(">>") || name.equals(">=");
        case '=':
            return name.equals("=") || name.equals("==") || name.equals("===") || name.equals("=~");
        case '!':
            return name.equals("!=") || name.equals("!~");
        case '&':
            return name.equals("&") || name.equals("&&");
        case '|':
            return name.equals("|") || name.equals("||");
        case '[':
            return name.equals("[]") || name.equals("[]=");
        case '%':
            return name.equals("%");
        case '/':
            return name.equals("/");
        case '~':
            return name.equals("~");
        case '^':
            return name.equals("^");
        case '`':
            return name.equals("`");
        default:
            return false;
        }
    }

    // There are lots of valid method names...   %, *, +, -, <=>, ...
    /**
     * Js identifiers should consist of [a-zA-Z0-9_]
     * http://www.headius.com/rubyspec/index.php/Variables
     * <p>
     * This method also accepts the field/global chars
     * since it's unlikely 
     */
    public static boolean isSafeIdentifierName(String name, int fromIndex) {
        int i = fromIndex;
        for (; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c == '$' || c == '@' || c == ':')) {
                break;
            }
        }
        for (; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c == '_') ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') ||
                    (c == '?') || (c == '=') || (c == '!'))) { // Method suffixes; only allowed on the last line

                if (isOperator(name)) {
                    return true;
                }

                return false;
            }
        }

        return true;
    }

    /** 
     * Return null if the given identifier name is valid, otherwise a localized
     * error message explaining the problem.
     */
    public static String getIdentifierWarning(String name, int fromIndex) {
        if (isSafeIdentifierName(name, fromIndex)) {
            return null;
        } else {
            return NbBundle.getMessage(JsUtils.class, "UnsafeIdentifierName");
        }
    }

    /** Similar to isValidJsClassName, but allows a number of ::'s to join class names */
    public static boolean isValidJsModuleName(String name) {
        if (name.trim().length() == 0) {
            return false;
        }

        String[] mods = name.split("::"); // NOI18N
        for (String mod : mods) {
            if (!isValidJsClassName(mod)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidJsClassName(String name) {
        if (isJsKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        if (!Character.isUpperCase(name.charAt(0))) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!isStrictIdentifierChar(c)) {
                return false;
            }

//            if (c == '!' || c == '=' || c == '?') {
//                // Not allowed in constant names
//                return false;
//            }

        }

        return true;
    }

    public static boolean isValidJsLocalVarName(String name) {
        if (isJsKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        if (Character.isUpperCase(name.charAt(0)) || Character.isWhitespace(name.charAt(0))) {
            return false;
        }

        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                return false;
            }
            // Identifier char isn't really accurate - I can have a function named "[]" etc.
            // so just look for -obvious- mistakes
            if (Character.isWhitespace(name.charAt(i))) {
                return false;
            }

        }

        return true;
    }

    public static boolean isValidJsMethodName(String name) {
        if (isJsKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        if (isOperator(name)) {
            return true;
        }

        if (Character.isUpperCase(name.charAt(0)) || Character.isWhitespace(name.charAt(0))) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                // !, = and ? can only be in the last position
                if (i == name.length() - 1 && ((c == '!') || (c == '=') || (c == '?'))) {
                    return true;
                }
                return false;
            }

        }

        return true;
    }

    public static boolean isValidJsIdentifier(String name) {
        if (isJsKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {
            // Identifier char isn't really accurate - I can have a function named "[]" etc.
            // so just look for -obvios- mistakes
            if (Character.isWhitespace(name.charAt(i))) {
                return false;
            }

        // TODO - make this more accurate, like the method validifier
        }

        return true;
    }

    public static boolean isJsKeyword(String name) {
        for (String s : JAVASCRIPT_KEYWORDS) {
            if (s.equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static String getLineCommentPrefix() {
        return "//"; // NOI18N
    }

    /** Includes things you'd want selected as a unit when double clicking in the editor */
    public static boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) || (// Globals, fields and parameter prefixes (for blocks and symbols)
                c == '$') || (c == '@') || (c == '&') || (c == ':') || (// Function name suffixes
                c == '!') || (c == '?') || (c == '=');
    }

    /** Includes things you'd want selected as a unit when double clicking in the editor */
    public static boolean isStrictIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) ||
                (c == '!') || (c == '?') || (c == '=');
    }

    // Section 7.5.2 in ECMAScript Language Specification, ECMA-262
    public static final String[] JAVASCRIPT_KEYWORDS =
            new String[]{
        // Uhm... what about "true" and "false" ? And "nil" ?
        "break",
        "case",
        "catch",
        "continue",
        "default",
        "delete",
        "do",
        "else",

        // Not included in the ECMAScript list of keywords - really a datatype
        "false", // NOI18N
        
        "finally",
        "for",
        "function",
        "if",
        "in",
        "instanceof",
        "new",

        // Not included in the ECMAScript list of keywords - really a datatype
        "null", // NOI18N
        
        "return",
        "switch",
        "this",
        "throw",
        
        // Not included in the ECMAScript list of keywords - really a datatype
        "true", // NOI18N
        
        "try",
        "typeof",

        // Not included in the ECMAScript list of keywords - really a datatype
        "undefined", // NOI18N
        
        "var",
        "void",
        "while",
        "with"
    };

    // Section 7.5.3 in ECMAScript Language Specification, ECMA-262
    public static final String[] JAVASCRIPT_RESERVED_WORDS =
            new String[]{
        "abstract",
        "boolean",
        "byte",
        "char",
        "class",
        "const",
        "debugger",
        "double",
        "enum",
        "export",
        "extends",
        "final",
        "float",
        "goto",
        "implements",
        "import",
        "int",
        "interface",
        "long",
        "native",
        "package",
        "private",
        "protected",
        "public",
        "short",
        "static",
        "super",
        "synchronized",
        "throws",
        "transient",
        "volatile",
    };

    public static boolean isRowWhite(String text, int offset) throws BadLocationException {
        try {
            // Search forwards
            for (int i = offset; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            // Search backwards
            for (int i = offset - 1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(),
                    offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static boolean isRowEmpty(String text, int offset) throws BadLocationException {
        try {
            if (offset < text.length()) {
                char c = text.charAt(offset);
                if (!(c == '\n' || (c == '\r' && (offset == text.length() - 1 || text.charAt(offset + 1) == '\n')))) {
                    return false;
                }
            }

            if (!(offset == 0 || text.charAt(offset - 1) == '\n')) {
                // There's previous stuff on this line
                return false;
            }

            return true;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(),
                    offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowLastNonWhite(String text, int offset) throws BadLocationException {
        try {
            // Find end of line
            int i = offset;
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n' || (c == '\r' && (i == text.length() - 1 || text.charAt(i + 1) == '\n'))) {
                    break;
                }
            }
            // Search backwards to find last nonspace char from offset
            for (i--; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(),
                    offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowFirstNonWhite(String text, int offset) throws BadLocationException {
        try {
            // Find start of line
            int i = offset - 1;
            if (i < text.length()) {
                for (; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        break;
                    }
                }
                i++;
            }
            // Search forwards to find first nonspace char from offset
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(),
                    offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowStart(String text, int offset) throws BadLocationException {
        try {
            // Search backwards
            for (int i = offset - 1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return i + 1;
                }
            }

            return 0;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(),
                    offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static boolean endsWith(StringBuilder sb, String s) {
        int len = s.length();

        if (sb.length() < len) {
            return false;
        }

        for (int i = sb.length() - len,  j = 0; j < len; i++, j++) {
            if (sb.charAt(i) != s.charAt(j)) {
                return false;
            }
        }

        return true;
    }

    public static String truncate(String s, int length) {
        assert length > 3; // Not for short strings
        if (s.length() <= length) {
            return s;
        } else {
            return s.substring(0, length - 3) + "...";
        }
    }
}
