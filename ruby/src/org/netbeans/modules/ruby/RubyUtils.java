/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformProvider;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tor Norbye
 */
public class RubyUtils {
    
    public static final String RUBY_MIME_TYPE = RubyInstallation.RUBY_MIME_TYPE; // NOI18N
    private static final String CONTROLLER_SUFFIX = "Controller";
    private static final String HELPER_SUFFIX = "Helper";
    private static final String CONTROLLER_FILE_SUFFIX = "_controller.rb"; // NOI18N


    private static final Logger LOGGER = Logger.getLogger(RubyUtils.class.getName());

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
        int nameLength = name.length();

        if ("".equals(name.substring(fromIndex).trim())) {
            return false;
        }

        for (; i < nameLength; i++) {
            char c = name.charAt(i);
            if (!(c == '$' || c == '@' || c == ':')) {
                break;
            }
            if (i > 0 && c != '@') {
                return false;
            }
            if (i > 1) {
                return false;
            }
            if (i + 1 == nameLength) {
                return false;
            }
        }
        // digits are not allowed as the first char, except in
        // pre-defined variables which this method does not handle
        if (Character.isDigit(name.charAt(i))) {
            return false;
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

    /**
     * Whether this is valid fully qualified constant name, i.e. similar to
     * {@link #isValidConstantName}, but allows a number of double-colons
     * (<em>::</em>) to join scopes (module or class) names. E.g.:
     *
     * <pre>
     *   module Colors
     *   
     *     module Converter
     *       # module definition
     *     end
     *
     *     RED   = "#FF0000"
     *     GREEN = "#00FF00"
     *     BLUE  = "#0000FF"
     *
     *   end
     * </pre>
     *
     * Colors::Converter is a constant name for module, Color::Constant is
     * an "common" constant.
     * 
     * @param name to check
     * @return <code>true</code> or <code>false</code>
     * @see #isValidConstantName
     */
    public static boolean isValidConstantFQN(final String name) {
        if (name.trim().length() == 0) {
            return false;
        }
        
        String[] mods = name.split("::"); // NOI18N
        for (String mod : mods) {
            if (!isValidConstantName(mod)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Whether this is a valid constant name, i.e. also class or module name.
     * 
     * @param name to check
     * @return <code>true</code> or <code>false</code>
     * @see #isValidConstantFQN
     */
    public static boolean isValidConstantName(final String name) {
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

    /**
     * Parse out module/class name and constant name out of possibly fully
     * qualified constant name. When non-FQN is given, returns
     * <code>Kernel</code> as the receiver name. E.g:
     *
     * <pre>
     *   "Color::HTML::RED" =&gt; {"Color::HTML", "RED"}
     *   "Color::RED"       =&gt; {"Color"      , "RED"}
     *   "RED"              =&gt; {"Kernel"     , "RED"}
     * </pre>
     *
     * @param mayBeFqn constant name, might or might not be FQN
     * @return two-member array, containing module/class name and constant name
     */
    static String[] parseConstantName(final String mayBeFqn) {
        int lastColon2 = mayBeFqn.lastIndexOf("::"); // NOI18N
        String module;
        String constant;
        if (lastColon2 != -1) {
            constant = mayBeFqn.substring(lastColon2 + 2);
            module = mayBeFqn.substring(0, lastColon2);
        } else {
            constant = mayBeFqn;
            module = "Kernel";
        }
        return new String[]{module, constant};
    }

    /**
     * Gets the "parent" modules of the given <code>fqn</code>. Does
     * not include the fqn itself. E.g. for <code>"Foo::Bar::Baz::Qux"</code>
     * this will return <code>"Foo::Bar::Baz"</code>, <code>"Foo::Bar"</code> 
     * and <code>"Foo"</code>.
     * 
     * @param fqn
     * @return
     */
    static List<String> getParentModules(String fqn) {
        int lastColon2 = fqn.lastIndexOf("::");
        if (lastColon2 == -1) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        while(lastColon2 > 0) {
            String parent = fqn.substring(0, lastColon2);
            result.add(parent);
            lastColon2 = parent.lastIndexOf("::");
        }
        return result;
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
        return Arrays.binarySearch(RUBY_KEYWORDS, name) >= 0;
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

    static { // so we can use Arrays#binarySearch
        Arrays.sort(RUBY_KEYWORDS);
    }

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

    private static void addControllerNames(FileObject file, List<String> names, boolean lowercase) {
        final String filename = file.getNameExt();
        if (filename.endsWith(CONTROLLER_FILE_SUFFIX)) {
            String name = filename.substring(0, filename.length()-CONTROLLER_FILE_SUFFIX.length());
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
        "haml", "rxml", "dryml", "html.erb"}; // NOI18N

    /*
     * Possible extensions for action mailer views.
     */
    private static final String[] ACTIONMAILER_VIEW_EXTS = {
        "text.rhtml", "html.erb", "html.rhtml", "text.html.rhtml", "text.html.erb" //NOI18N
    };
    /**
     * Move from something like app/controllers/credit_card_controller.rb#debit()
     * to app/views/credit_card/debit.rhtml
     * 
     * @param strict If true, limit view searches to the given method name, don't find other views
     * @param isHelper If false, it's a controller, else it's a helper
     * 
     */
    public static FileObject getRailsViewFor(FileObject file, String methodName, 
            String fileSuffix, String parentAppDir, boolean strict) {
        
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
                List<String> viewExts = new ArrayList<String>();
                viewExts.addAll(Arrays.asList(RUBY_VIEW_EXTS));
                viewExts.addAll(Arrays.asList(ACTIONMAILER_VIEW_EXTS));
                for (String ext : viewExts) {
                    viewFile = viewsFolder.getFileObject(methodName, ext);
                    if (viewFile != null) {
                        break;
                    }
                }
                
                if (viewFile == null && strict) {
                    return null;
                }
            }

            if (viewFile == null && fileSuffix.length() > 0) {
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
    
    /**
     * Gets the Rails controller or ActionMailer model class for the given view.
     * 
     * @param file the view to get the controller/model for.
     * @return the controller/model or <code>null</code>.
     *
     */
    public static FileObject getRailsControllerFor(FileObject file) {

        if (file.getParent() == null) {
            return null;
        }
        // TODO - instead of relying on Path manipulation here, should I just
        // use the RubyIndex to locate the class and method?
        FileObject result = null;
        
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

        result = app.getFileObject("controllers/" + path + "_controller.rb"); // NOI18N
        if (result == null) {
            // possibly a view for an action mailer model
            result = app.getFileObject("models/" + path + ".rb"); // NOI18N
        }

        return result;
    }

    static String join(final String[] arr, final String separator) {
        return join(Arrays.asList(arr), separator);
    }

    static String join(final Iterable<? extends String> iterable, final String separator) {
        return join(iterable, separator, separator);
    }

    static String join(final Iterable<? extends String> iterable, final String separator, final String lastSeparator) {
        Iterator<? extends String> it = iterable.iterator();
        if (!it.hasNext()) {
            return "";
        }
        StringBuffer buf = new StringBuffer(60);
        buf.append(it.next());
        while (it.hasNext()) {
            String next = it.next();
            if (it.hasNext()) {
                buf.append(separator);
            } else {
                buf.append(lastSeparator);
            }
            buf.append(next);
        }

        return buf.toString();
    }

    public static BaseDocument getDocument(Result result) {
        return getDocument(result, false);
    }

    public static BaseDocument getDocument(Result result, boolean forceOpen) {
        return (BaseDocument) result.getSnapshot().getSource().getDocument(forceOpen);
    }

    public static FileObject getFileObject(Result result) {
        return result.getSnapshot().getSource().getFileObject();
    }

    static boolean isRubyStubsURL(String url) {
        return url != null && url.indexOf(RubyPlatform.RUBYSTUBS) != -1; // NOI18N
    }

    /**
     * Checks whether the given file is part of the platform (incl. gems)
     * rather than a source file in a project.
     * @param fileObject
     * @return
     */
    static boolean isPlatformFile(FileObject fileObject) {
        Project owner = FileOwnerQuery.getOwner(fileObject);
        if (owner == null) {
            return true;
        }
        // needed for the dev ide since the bundled jruby is under nbbuild (a free form project)
        RubyPlatformProvider platformProvider = owner.getLookup().lookup(RubyPlatformProvider.class);
        return platformProvider == null;
    }

    private static final Pattern RAILS_VERSION_PATTERN = Pattern.compile(".*/(action|active){1}.+-(\\d+\\.\\d+\\.?\\d*).+"); //NOI18N

    /**
     *@return true if the given path represents a file that is in a rails gem
     * that is 2.3 or newer. Note that this does not work when rails is in
     * the vendor directory as the gem paths there don't contain version
     * numbers.
     */
    static boolean isRails23OrHigher(String path) {
        Matcher m = RAILS_VERSION_PATTERN.matcher(path);
        if (!m.matches()) {
            return false;
        }
        String[] version = m.group(2).split("\\.");
        int major = Integer.parseInt(version[0]);
        int minor = Integer.parseInt(version[1]);
        if (major == 2) {
            return minor >= 3;
        }
        return major > 2 ? true : false;
    }

    // copied from org.netbeans.modules.parsing.impl.indexing.Util#getFileObject
    static FileObject getFileObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject) sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject) sdp).getPrimaryFile();
        }
        return null;
    }

    /**
     * Performs a simplistic check for determining whether the given file represents
     * a rails controller.
     * 
     * @param fo
     * @return
     */
    static boolean isRailsController(FileObject fo) {
        boolean endsWithController = fo.getName().endsWith("_controller");
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            // fallback 
            return endsWithController;
        }
        FileObject controllerDir = owner.getProjectDirectory().getFileObject("app/controllers"); //NOI18N
        if (controllerDir == null) {
            return endsWithController;
        }
        return FileUtil.isParentOf(controllerDir, fo) && endsWithController;
    }

    /**
     * @return true if the given file appears to belong to a Rails project.
     */
    static boolean isRailsProject(FileObject fo) {
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            return false;
        }
        // just a simple check, can't depend on ruby.railsprojects here.
        FileObject app = owner.getProjectDirectory().getFileObject("app");
        FileObject config = owner.getProjectDirectory().getFileObject("config");
        FileObject db = owner.getProjectDirectory().getFileObject("db");
        return app != null && config != null && db != null;
        
        
    }

    /**
     * Gets the "app" dir of the project the given file belongs to.
     * @param fo
     * @return
     */
    static FileObject getAppDir(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }

        return project.getProjectDirectory().getFileObject("app/"); //NOI18N
    }

    static String helperName(String controllerName) {
        return baseName(controllerName) + HELPER_SUFFIX; //NOI18N
    }

    static String controllerName(String baseName) {
        return baseName(baseName) + CONTROLLER_SUFFIX;
    }

    static String baseName(String controllerName) {
        if (controllerName.endsWith(CONTROLLER_SUFFIX)) {
            return controllerName.substring(0, controllerName.length() - CONTROLLER_SUFFIX.length());
        }
        return controllerName;

    }

    /**
     * Adds the given {@code toAdd} to the end of the given {@code array}.
     * 
     * @param array
     * @param toAdd
     * @return
     */
    static String[] addToArray(String[] array, String... toAdd) {
        if (toAdd == null || toAdd.length == 0) {
            return array;
        }
        String[] result = new String[array.length + toAdd.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        for (int i = array.length; i < array.length + toAdd.length; i++) {
            result[i] = toAdd[i - array.length];
        }
        return result;
    }

}
