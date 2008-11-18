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
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tor Norbye
 */
public class RubyUtils {
    
    public static final String RUBY_MIME_TYPE = RubyInstallation.RUBY_MIME_TYPE; // NOI18N

    private RubyUtils() {
    }

    public static boolean isRubyFile(FileObject f) {
        return RubyInstallation.RUBY_MIME_TYPE.equals(f.getMIMEType());
    }
    
    public static boolean isMarkabyFile(FileObject fileObject) {
        return "mab".equals(fileObject.getExt()); // NOI18N
    }

    public static boolean isRhtmlFile(FileObject f) {
        return RubyInstallation.RHTML_MIME_TYPE.equals(f.getMIMEType());
    }

    public static boolean isRubyDocument(Document doc) {
        String mimeType = (String)doc.getProperty("mimeType"); // NOI18N

        return RubyInstallation.RUBY_MIME_TYPE.equals(mimeType);
    }
    
    public static boolean isRhtmlDocument(Document doc) {
        String mimeType = (String)doc.getProperty("mimeType"); // NOI18N

        return RubyInstallation.RHTML_MIME_TYPE.equals(mimeType);
    }

    public static boolean isYamlDocument(Document doc) {
        String mimeType = (String)doc.getProperty("mimeType"); // NOI18N

        return "text/x-yaml".equals(mimeType); // NOI18N
    }

    public static boolean isYamlFile(FileObject f) {
        return "text/x-yaml".equals(f.getMIMEType()); // NOI18N
    }
    
    public static boolean isRhtmlOrYamlFile(FileObject f) {
        String mimeType = f.getMIMEType();
        return "text/x-yaml".equals(mimeType) || RubyInstallation.RHTML_MIME_TYPE.equals(mimeType); // NOI18N
    }

    public static boolean canContainRuby(FileObject f) {
        String mimeType = f.getMIMEType();
        return  RubyInstallation.RUBY_MIME_TYPE.equals(mimeType) ||
                "text/x-yaml".equals(mimeType) ||  // NOI18N
                RubyInstallation.RHTML_MIME_TYPE.equals(mimeType);
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
    
    private static final String S = "s";
    private static final String ES = "es";

    /** Pluralize, Rails style. This needs to roll in more magic from
     * Inflector.pluralize
     * 
     * @see activesupport/lib/active_support/inflections.rb
     * @param word The word to be pluralized
     * @return The pluralized word
     */
    public static String pluralize(String word) {
        // Apply inflector rules - see inflections.rb in activesupport
        char c = word.charAt(word.length()-1);
        switch (c) {
        case 't':
        case 'T':
            if (endsWithIgnoreCase(word, "equipment")) { // Uncountable
                return word;
            }
            break;
        case 'p':
        case 'P':
            if (endsWithIgnoreCase(word, "sheep")) { // Uncountable
                return word;
            }
            break;
        case 'd':
        case 'D':
            if (endsWithIgnoreCase(word, "child")) { // Irregular
                return word + "ren";
            }
            break;
        case 'n':
        case 'N':
            if (endsWithIgnoreCase(word, "information")) { // Uncountable
                return word;
            } else if (endsWithIgnoreCase(word, "man")) { // Irregular
                return word.substring(0, word.length()-2) + "en";
            } else if (endsWithIgnoreCase(word, "person")) { // Irregular
                return word.substring(0, word.length()-5) + "eople";
            }
            break;
        case 's':
        case 'S':
            if (endsWithIgnoreCase(word, "species") || endsWithIgnoreCase(word, "series")) { // Uncountable
                return word;
            } else if (endsWithIgnoreCase(word,"axis") || endsWithIgnoreCase(word, "testis")) {
                  // "(ax|test)is$", "\\1es",
                return word.substring(0, word.length()-2) + ES;
            } else if (endsWithIgnoreCase(word, "ss")) {
                // Part of "(x|ch|ss|sh)$", "\\1es",
                return word + ES;
            } else if (endsWithIgnoreCase(word,"alias") || endsWithIgnoreCase(word,"status")) {
                // "(alias|status)$", "\\1es",
                return word + ES;
            } else if (endsWithIgnoreCase(word, "us")) {
                //"(octop|vir)(us)$", "\\1i",
                return word.substring(0, word.length()-2) + "i";
            } else if (endsWithIgnoreCase(word, "bus")) {
                // "(bu)s$", "\\1ses",
                return word.substring(0, word.length()-1) + "ses";
            } else if (endsWithIgnoreCase(word, "sis")) {
                // "sis$", "ses",
                return word.substring(0, word.length()-2) + ES;
            }
            
            // Ends with just s -- fall back to just the word itself
            //   inflect.plural(/s$/i, 's')
            return word;
        case 'o':
        case 'O':
            if (endsWithIgnoreCase(word, "buffalo") || endsWithIgnoreCase(word, "tomato")) {
                // "(buffal|tomat)o$", "\\1oes",
                return word + ES;
            }
            break;
        case 'm':
        case 'M':
            if (endsWithIgnoreCase(word, "tum") || endsWithIgnoreCase(word, "ium")) {
                // "([ti])um$", "\\1a",
                return word.substring(0, word.length()-2) + "a";
            }
            break;
        case 'f':
        case 'F':
            if (endsWithIgnoreCase(word, "lf") || endsWithIgnoreCase(word, "rf")) {
                // First half of inflect.plural(/(?:([^f])fe|([lr])f)$/i, '\1\2ves')
                return word.substring(0, word.length()-1) + "ves";
            }
            break;
        case 'e':
        case 'E':
            if (endsWithIgnoreCase(word, "rice")) { // Uncountable
                return word;
            } else if (endsWithIgnoreCase(word, "move")) { // Irregular
                return word + S;
            } else if (endsWithIgnoreCase(word, "mouse") ||
                    endsWithIgnoreCase(word, "louse")) {
                //  inflect.plural(/([m|l])ouse$/i, '\1ice')
                return word.substring(0, word.length()-4) + "ice";
            } else if (endsWithIgnoreCase(word, "fe") && !endsWithIgnoreCase(word, "ffe")) {
                // Second half of inflect.plural(/(?:([^f])fe|([lr])f)$/i, '\1\2ves')
                return word.substring(0, word.length()-2) + "ves";
            } else if (endsWithIgnoreCase(word, "hive")) {
                // inflect.plural(/(hive)$/i, '\1s')
                return word+S;
            }
            break;
        case 'y':
        case 'Y':
            if (endsWithIgnoreCase(word, "money")) { // Uncountable
                return word;
            }
            //  inflect.plural(/([^aeiouy]|qu)y$/i, '\1ies')
            if (word.matches(".*([^aeiouy]|qu)y$")) {
                return word.substring(0, word.length()-1) + "ies";
            }
            break;
        case 'x':
        case 'X':
            if (endsWithIgnoreCase(word, "sex")) { // Irregular
                return word + ES;
            } else if (endsWithIgnoreCase(word, "matrix") || endsWithIgnoreCase(word, "vertex") ||
                    endsWithIgnoreCase(word, "index")) {
                // inflect.plural(/(matr|vert|ind)ix|ex$/i, '\1ices')
                return word.substring(0, word.length()-2) + "ices";
            } else if (word.equalsIgnoreCase("ox")) {
                // inflect.plural(/^(ox)$/i, '\1en')
                return "oxen";
            }
            
            // "(x|ch|ss|sh)$", "\\1es",
            return word + ES;
        case 'h':
        case 'H':
            if (endsWithIgnoreCase(word, "fish")) { // Uncountable
                return word;
            } else if (endsWithIgnoreCase(word, "ch") || endsWithIgnoreCase(word, "sh")) {
                //  Half of  inflect.plural(/(x|ch|ss|sh)$/i, '\1es')
                return word + ES;
            }
            break;
        case 'z':
        case 'Z':
            if (endsWithIgnoreCase(word, "quiz")) {
                // inflect.plural(/(quiz)$/i, '\1zes')
                return word + "zes";
            }
        }

        // Fallback
        return word+S;
    }
    
    private static boolean endsWithIgnoreCase(String word, String ending) {
        if (ending.length() > word.length()) {
            return false;
        }

        return word.regionMatches(true, word.length()-ending.length(), ending, 0, ending.length());
    }
    
    /**
     * Similar to Rails' Inflector tableize method: converts a name
     * to a corresponding table name:
     * 
     * @param word
     * @return
     */
    public static String tableize(String word) {
        return RubyUtils.pluralize(RubyUtils.camelToUnderlinedName(word));
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

    /** Return the class name corresponding to the given controller file */
    public static String getControllerClass(FileObject controllerFile) {
        Project p = FileOwnerQuery.getOwner(controllerFile);
        FileObject controllers = p.getProjectDirectory().getFileObject("app/controllers"); // NOI18N
        if (controllers != null) {
            String relative = controllerFile.getName();
            FileObject f = controllerFile.getParent();
            while (f != controllers && f != null) {
                relative = f.getName() + "/" + relative;
                f = f.getParent();
            }
            
            return underlinedNameToCamel(relative);
        }
        
        return null;
    }

    public static List<String> getControllerNames(FileObject fileInProject, boolean lowercase) {
        Project p = FileOwnerQuery.getOwner(fileInProject);
        FileObject controllers = p.getProjectDirectory().getFileObject("app/controllers"); // NOI18N
        if (controllers != null) {
            List<String> names = new ArrayList<String>();
            addControllerNames(controllers, names, lowercase);
            return names;
        }
        
        return Collections.emptyList();
    }

    private static final String CONTROLLER_SUFFIX = "_controller.rb"; // NOI18N
    
    private static void addControllerNames(FileObject file, List<String> names, boolean lowercase) {
        final String filename = file.getNameExt();
        if (filename.endsWith(CONTROLLER_SUFFIX)) {
            String name = filename.substring(0, filename.length()-CONTROLLER_SUFFIX.length());
            if (!lowercase) {
                name = underlinedNameToCamel(name);
            }
            names.add(name);
        }

        for (FileObject child : file.getChildren()) {
            addControllerNames(child, names, lowercase);
        }
    }
    
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
    
    public static FileObject getRailsControllerFor(FileObject file) {
        // TODO - instead of relying on Path manipulation here, should I just
        // use the RubyIndex to locate the class and method?
        FileObject controllerFile = null;

        try {
            file = file.getParent();

            String fileName = file.getName();
            String path = "";

            if (!fileName.startsWith("_")) { // NOI18N
                                             // For partials like "_foo", just use the surrounding view
                path = fileName;
            }

            // Find app dir, and build up a relative path to the view file in the process
            FileObject app = file.getParent();

            while (app != null) {
                if (app.getName().equals("views") && // NOI18N
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

            controllerFile = app.getFileObject("controllers/" + path + "_controller.rb"); // NOI18N
        } catch (Exception e) {
            return null;
        }

        return controllerFile;
    }
}
