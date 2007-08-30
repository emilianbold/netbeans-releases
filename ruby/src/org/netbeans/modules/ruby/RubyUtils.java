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
        String mimeType = (String)doc.getProperty("mimeType");

        return RubyInstallation.RHTML_MIME_TYPE.equals(mimeType);
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
    
    /** Is this name a valid operator name? */
    public static boolean isOperator(String name) {
        if (name.length() == 0) {
            return false;
        }
        // Pieced together from various sources (RubyYaccLexer, DefaultRubyParser, ...)
        switch (name.charAt(0)) {
        case '+':
            return name.equals("+") || name.equals("+@");
        case '-':
            return name.equals("-@");
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

    /** Similar to isValidRubyClassName, but allows a number of ::'s to join class names */
    public static boolean isValidRubyModuleName(String name) {
        if (name.trim().length() == 0) {
            return false;
        }
        
        String[] mods = name.split("::"); // NOI18N
        for (String mod : mods) {
            if (!isValidRubyClassName(mod)) {
                return false;
            }
        }
        
        return true;
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
                if (i == name.length()-1 && ((c == '!') || (c == '=') || (c == '?'))) {
                    return true;
                }
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
            
            // TODO - make this more accurate, like the method validifier
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
    
    public static String getControllerName(FileObject file) {
        String fileSuffix = "_controller"; // NOI18N
        String parentAppDir = "controllers"; // NOI18N
        String controllerName =
            file.getName().substring(0, file.getName().length() - fileSuffix.length());

        String path = controllerName;

        // Find app dir, and build up a relative path to the view file in the process
        FileObject app = file.getParent();

        while (app != null) {
            if (app.getName().equals(parentAppDir) && // NOI18N
                    ((app.getParent() == null) || app.getParent().getName().equals("app"))) { // NOI18N
                app = app.getParent();

                break;
            }

            path = app.getNameExt() + "/" + path; // NOI18N
            app = app.getParent();
        }

        return path;
    }
    
    // This does not include the various RHTML extensions: .rhtml, .erb, .html.erb, ... See isRhtmlFile
    public static final String[] RUBY_VIEW_EXTS = { 
        "rhtml", "erb", "dryml", "mab", "rjs", // NOI18N
        "haml", "rxml", "dryml", "html.erb" }; // NOI18N

    /**
     * Move from something like app/controllers/credit_card_controller.rb#debit()
     * to app/views/credit_card/debit.rhtml
     * 
     * @param strict If true, limit view searches to the given method name, don't find other views
     * @param isHelper If false, it's a controller, else it's a helper
     * 
     */
    public static FileObject getRailsViewFor(FileObject file, String methodName, boolean isHelper, boolean strict) {
        String fileSuffix = isHelper ? "_helper" : "_controller"; // NOI18N
        String parentAppDir = isHelper ? "helpers" : "controllers"; // NOI18N
        
        FileObject viewFile = null;

        try {
            String controllerName =
                file.getName().substring(0, file.getName().length() - fileSuffix.length());

            String path = controllerName;

            // Find app dir, and build up a relative path to the view file in the process
            FileObject app = file.getParent();

            while (app != null) {
                if (app.getName().equals(parentAppDir) && // NOI18N
                        ((app.getParent() == null) || app.getParent().getName().equals("app"))) { // NOI18N
                    app = app.getParent();

                    break;
                }

                path = app.getNameExt() + "/" + path; // NOI18N
                app = app.getParent();
            }

            if (app == null) {
                return null;
            }

            FileObject viewsFolder = app.getFileObject("views/" + path); // NOI18N

            if (viewsFolder == null) {
                return null;
            }

            if (methodName != null) {
                for (String ext : RUBY_VIEW_EXTS) {
                    viewFile = viewsFolder.getFileObject(methodName, ext);
                    if (viewFile != null) {
                        break;
                    }
                }
                
                if (viewFile == null && strict) {
                    return null;
                }
            }

            if (viewFile == null) {
                // The caret was likely not inside any of the methods, or in a method that
                // isn't directly tied to a view
                // Just pick one of the views. Try index first.
                viewFile = viewsFolder.getFileObject("index.rhtml"); // NOI18N
                if (viewFile == null) {
                    for (FileObject child : viewsFolder.getChildren()) {
                        String ext = child.getExt();
                        for (String e : RUBY_VIEW_EXTS) {
                            if (ext.equalsIgnoreCase(e)) {
                                viewFile = child;
                                
                                break;
                            }
                        }
                    }
                }
            }

            if (viewFile == null) {
                return null;
            }

        } catch (Exception e) {
            return null;
        }

        if (viewFile == null) {
            return null;
        }
        
        return viewFile;
    }
}
