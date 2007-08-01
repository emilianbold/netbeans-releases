/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import javax.swing.text.Document;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tor Norbye
 */
public class RubyUtils {

    private RubyUtils() {
    }

    public static boolean isRubyFile(FileObject f) {
        return RubyMimeResolver.RUBY_MIME_TYPE.equals(f.getMIMEType());
    }
    
    public static boolean isMarkabyFile(FileObject fileObject) {
        return "mab".equals(fileObject.getExt()); // NOI18N
    }

    public static boolean isRhtmlFile(FileObject f) {
        return RubyInstallation.RHTML_MIME_TYPE.equals(f.getMIMEType());
    }
    
    public static boolean isRhtmlDocument(Document doc) {
        DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);
        if (dobj != null) {
            return isRhtmlFile(dobj.getPrimaryFile());
        }
        
        return false;
    }
    
    public static boolean isRubyOrRhtmlFile(FileObject f) {
        return isRubyFile(f) || isRhtmlFile(f);
    }
    
    public static String camelToUnderlinedName(String name) {
        // TODO - convert :: to /
        StringBuilder sb = new StringBuilder();
        boolean lastWasUnderline = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean isCaps = Character.isUpperCase(c);
            if (isCaps) {
                if (i > 0 && !lastWasUnderline) {
                    sb.append('_');
                    lastWasUnderline = true;
                }
                c = Character.toLowerCase(c);
            }
            sb.append(c);
            
            lastWasUnderline = (c == '_');
        }
        return sb.toString();
    }
    
    /**
     * Ruby identifiers should consist of [a-zA-Z0-9_]
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
            if (!((c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') ||
                   (c == '_'))) { 
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
           return NbBundle.getMessage(RubyUtils.class, "UnsafeIdentifierName");
       }
    }

    /** Camelize the given word -- see Rails activesupport's camelize method */
    public static String underlinedNameToCamel(String name) {
        StringBuilder sb = new StringBuilder();
        boolean upperCaseNext = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                upperCaseNext = true;
                continue;
            } else if (c == '/') {
                upperCaseNext = true;
                sb.append("::");
                continue;
            } else if (upperCaseNext) {
                c = Character.toUpperCase(c);
                upperCaseNext = false;
            }
            sb.append(c);
        }
        return sb.toString();
    }
    
    public static boolean isValidRubyClassName(String name) {
        if (isRubyKeyword(name)) {
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
            
            if (c == '!' || c == '=' || c == '?') {
                // Not allowed in constant names
                return false;
            }
            
        }

        return true;
    }
    
    public static boolean isValidRubyLocalVarName(String name) {
        if (isRubyKeyword(name)) {
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
    
    public static boolean isValidRubyMethodName(String name) {
        if (isRubyKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        // TODO - find out what the exact rules are
        if (name.equals("[]")) {
            return true;
        }

        if (Character.isUpperCase(name.charAt(0)) || Character.isWhitespace(name.charAt(0))) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            // Identifier char isn't really accurate - I can have a function named "[]" etc.
            // so just look for -obvious- mistakes
            if (Character.isWhitespace(name.charAt(i))) {
                return false;
            }
            
        }
        
        // !, = and ? can only be in the last position
        for (int i = 0; i < name.length()-1; i++) {
            char c = name.charAt(i);
            
            if (c == '!' || c == '=' || c == '?') {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidRubyIdentifier(String name) {
        if (isRubyKeyword(name)) {
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
            
        }
        
        return true;
    }
    
    public static boolean isRubyKeyword(String name) {
        for (String s : RUBY_KEYWORDS) {
            if (s.equals(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static String getLineCommentPrefix() {
        return "#"; // NOI18N
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
    
    public static final String[] RUBY_KEYWORDS =
        new String[] {
            // Keywords
            "alias", "and", "BEGIN", "begin", "break", "case", "class", "def", "defined?", "do",
            "else", "elsif", "END", "end", "ensure", "false", "for", "if", "in", "module", "next",
            "nil", "not", "or", "redo", "rescue", "retry", "return", "self", "super", "then", "true",
            "undef", "unless", "until", "when", "while", "yield"
        };
}
