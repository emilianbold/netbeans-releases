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
package org.netbeans.modules.ruby;

import org.netbeans.api.gsf.ElementHandle;
import org.netbeans.modules.ruby.elements.CommentElement;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Completable;
import org.netbeans.api.gsf.CompletionProposal;
import org.netbeans.api.gsf.DeclarationFinder.DeclarationLocation;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.HtmlFormatter;
import org.netbeans.modules.ruby.elements.IndexedField;
import static org.netbeans.api.gsf.Index.*;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.ParameterInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.RubyParser.Sanitize;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.AstFieldElement;
import org.netbeans.modules.ruby.elements.AstVariableElement;
import org.netbeans.modules.ruby.elements.ClassElement;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.KeywordElement;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.Call;
import org.netbeans.modules.ruby.lexer.RubyStringTokenId;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Code completion handler for Ruby.
 * Bug: I add lists of fields etc. But if these -overlap- the current line,
 *  I throw them away. The problem is that there may be other references
 *  to the field that I should -not- throw away, elsewhere!
 * @todo Ensure that I prefer assignment over reference such that javadoc is
 *   more likely to be there!
 *   
 * 
 * @todo Handle this case:  {@code class HTTPBadResponse &lt; StandardError; end}
 * @todo Code completion should automatically suggest "initialize()" for def completion! (if I'm in a class)
 * @todo It would be nice if you select a method that takes a block, such as Array.each, if we could
 *   insert a { ^ } suffix
 * @todo Use lexical tokens to avoid attempting code completion within comments,
 *   literal strings and regexps
 * @todo Percent-completion doesn't work if you at this to the end of the
 *   document:  x = %    and try to complete.
 * @todo Handle more completion scenarios: Classes (no keywords) after "class Foo &lt;",
 *   classes after "::", parameter completion (!!!), .new() completion (initialize), etc.
 * @todo Make sure completion works during a "::"
 * @todo I need to move the smart-determination from just checking in=Object/Class/Module
 *   to the code which computes matches, since we have for example ObjectMixin in pretty printer
 *   which adds mixin methods to Object.
 * @todo Handle Rails methods that deal with hashes:
 *    - Try figuring out whether the method should take parameters by looking for examples;
 *      lines that start with the method name and looks like it might have arguments
 *    - Try to figure out what the different parameters are if there are hashes
 *    - &lt;tt&gt;: looks like a parameter, e.g. "<tt>:filename</tt>"
 *      and to see which parameter it might correspond to, see the
 *      label; see if any of the parameter names are listed there (possibly in the args list)
 *      A fallback is to look for args that look like they may be hashes, e.g.  
 *      def(foo1, foo2, foo3={}) - the third one is obviously a hash
 * @todo Make code completion when we're in a parameter list include the parameters as well!
 * @todo For .rjs files, insert an object named "page" of type 
 *    ActionView::Helpers::PrototypeHelper::JavaScriptGenerator::GeneratorMethods
 *    (#105088)
 * @todo For .builder files, insert an object named "xml" of type
 *    Builder::XmlMarkup
 * @todo For .rhtml/.html.erb files, insert fields etc. as documented in actionpack's lib/action_view/base.rb
 *    (#105095)
 * @todo For test files in Rails, get testing context (#105043). In particular, actionpack's
 *    ActionController::Assertions needs to be pulled in. This happens in action_controller/assertions.rb.
 * @todo Require-completion should handle ruby gems; it should provide the "preferred" (entry-point) files for
 *    all the ruby gems, and it should hide all the files that are inside the gem
 * @todo Rakefiles files should inherit Rakefile context
 * @todo See http://blog.diegodoval.com/2007/09/ruby_on_os_x_some_useful_links.html
 * @todo Documentation completion in a rdoc should preview that rdoc section
 * @todo Make a dedicated completion item which I return on documentation completion if I want  to
 *    complete the CURRENT element; it basically just wraps the desired comment so we can pull it
 *    out in the document() method
 * @todo Provide code completion for "3|" or "3 |" - show available overloaded operators! This
 *    shouldn't just apply to numbers - any class you've overridden
 * @todo Digest http://blogs.sun.com/coolstuff/entry/using_java_classes_in_jruby
 *    to fix require'java' etc.
 * @todo http://www.innovationontherun.com/scraping-dynamic-websites-using-jruby-and-htmlunit/
 *    Idea: Use a quicktip to require all the jars in the project?
 * @todo The "h" method in <%= %> doesn't show up in RHTML files... where is it?
 * @todo Completion AFTER a method which takes a block (optional or required) should offer
 *    { } and do/end !!
 * @author Tor Norbye
 */
public class CodeCompleter implements Completable {
    /** Include call items in code completion? */
    private static final boolean INCLUDE_CALL_ITEMS = true;
    
    /** Another good logical parameter would be SINGLE_WHITESPACE which would insert a whitespace separator IF NEEDED */
    /** Live code template parameter: require the given file, if not already done so */
    private static final String KEY_REQUIRE = "require"; // NOI18N

    /** Live code template parameter: find a name in scope that is known to be of the given type */
    private static final String KEY_INSTANCEOF = "instanceof"; // NOI18N

    /** Live code template parameter: compute an unused local variable name */
    private static final String ATTR_UNUSEDLOCAL = "unusedlocal"; // NOI18N

    /** Live code template parameter: pipe variable, since | is a bit mishandled in the UI for editing abbrevs */
    private static final String KEY_PIPE = "pipe"; // NOI18N

    /** Live code template parameter: compute the method name */
    private static final String KEY_METHOD = "method"; // NOI18N

    /** Live code template parameter: compute the method signature */
    private static final String KEY_METHOD_FQN = "methodfqn"; // NOI18N

    /** Live code template parameter: compute the class name (not including the module prefix) */
    private static final String KEY_CLASS = "class"; // NOI18N

    /** Live code template parameter: compute the class fully qualified name */
    private static final String KEY_CLASS_FQN = "classfqn"; // NOI18N

    /** Live code template parameter: compute the superclass of the current class */
    private static final String KEY_SUPERCLASS = "superclass"; // NOI18N

    /** Live code template parameter: compute the filename (not including the path) of the file */
    private static final String KEY_FILE = "file"; // NOI18N

    /** Live code template parameter: compute the full path of the source directory */
    private static final String KEY_PATH = "path"; // NOI18N

    /** Default name values for ATTR_UNUSEDLOCAL and friends */
    private static final String ATTR_DEFAULTS = "defaults"; // NOI18N
    private static final String[] RUBY_BUILTIN_VARS =
        new String[] {
            // Predefined variables
            "__FILE__", "__LINE__", "STDIN", "STDOUT", "STDERR", "ENV", "ARGF", "ARGV", "DATA",
            "RUBY_VERSION", "RUBY_RELEASE_DATE", "RUBY_PLATFORM", "$DEBUG", "$FILENAME",
            "$LOAD_PATH", "$stderr", "$stdin", "$stdout", "$VERBOSE",
        };
    private static final String[] RUBY_REGEXP_WORDS =
        new String[] {
            // Dbl-space lines to keep formatter from collapsing pairs into a block
            "^", "Start of line",
            
            "$", "End of line",
            
            "\\A", "Beginning of string",
            
            "\\z", "End of string",
            
            "\\Z", "End of string (except \\n)",
            
            "\\w", "Letter or digit; same as [0-9A-Za-z]",
            
            "\\W", "Neither letter or digit",
            
            "\\s", "Space character; same as [ \\t\\n\\r\\f]",
            
            "\\S", "Non-space character",
            
            "\\d", "Digit character; same as [0-9]",
            
            "\\D", "Non-digit character",
            
            "\\b", "Backspace (0x08) (only if in a range specification)",
            
            "\\b", "Word boundary (if not in a range specification)",
            
            "\\B", "Non-word boundary",
            
            "*", "Zero or more repetitions of the preceding",
            
            "+", "One or more repetitions of the preceding",
            
            "{m,n}", "At least m and at most n repetitions of the preceding",
            
            "?", "At most one repetition of the preceding; same as {0,1}",
            
            "|", "Either preceding or next expression may match",
            
            "()", "Grouping",
            
            "[:alnum:]", "Alphanumeric character class",
            
            "[:alpha:]", "Uppercase or lowercase letter",
            
            "[:blank:]", "Blank and tab",
            
            "[:cntrl:]", "Control characters (at least 0x00-0x1f,0x7f)",
            
            "[:digit:]", "Digit",
            
            "[:graph:]", "Printable character excluding space",
            
            "[:lower:]", "Lowecase letter",
            
            "[:print:]", "Any printable letter (including space)",
            
            "[:punct:]", "Printable character excluding space and alphanumeric",
            
            "[:space:]", "Whitespace (same as \\s)",
            
            "[:upper:]", "Uppercase letter",
            
            "[:xdigit:]", "Hex digit (0-9, a-f, A-F)",
        };
    private static final String[] RUBY_PERCENT_WORDS =
        new String[] {
            // Dbl-space lines to keep formatter from collapsing pairs into a block
            "%q", "String (single-quoting rules)",
            
            "%Q", "String (double-quoting rules)",
            
            "%r", "Regular Expression",
            
            "%x", "Commands",
            
            "%W", "String Array (double quoting rules)",
            
            "%w", "String Array (single quoting rules)",
            
            "%s", "Symbol",
        };
    private static final String[] RUBY_STRING_PAIRS =
        new String[] {
            // Dbl-space lines to keep formatter from collapsing pairs into a block
            "(", "(delimiters)",
            
            "{", "{delimiters}",
            
            "[", "[delimiters]",
            
            "x", "<i>x</i>delimiters<i>x</i>",
        };
    private static final String[] RUBY_DOLLAR_VARIABLES =
        new String[] {
            // From http://www.ruby-doc.org/docs/UsersGuide/rg/globalvars.html
            "$!", "Latest error message",
            
            "$@", "Location of error",
            
            "$_", "String last read by gets",
            
            "$.", "Line number last read by interpreter",
            
            "$&", "String last matched by regexp",
            
            "$~", "The last regexp match, as an array of subexpressions",
            
            "$n", "The nth subexpression in the last match (same as $~[n])",
            
            "$=", "Case-insensitivity flag",
            
            "$/", "Input record separator",
            
            "$\\", "Output record separator",
            
            "$0", "The name of the ruby script file",
            
            "$*", "The command line arguments",
            
            "$$", "Interpreter's process ID",
            
            "$?", "Exit status of last executed child process",
        };
    private static final String[] RUBY_QUOTED_STRING_ESCAPES =
        new String[] {
            "\\a", "Bell/alert (0x07)",
            
            "\\b", "Backspace (0x08)",
            
            "\\x", "\\x<i>nn</i>: Hex <i>nn</i>",
            
            "\\e", "Escape (0x1b)",
            
            "\\c", "Control-<i>x</i>",
            
            "\\C-", "Control-<i>x</i>",
            
            "\\f", "Formfeed (0x0c)",
            
            "\\n", "Newline (0x0a)",
            
            "\\M-", "\\M-<i>x</i>: Meta-<i>x</i>",
            
            "\\r", "Return (0x0d)",
            
            "\\M-\\C-", "Meta-control-<i>x</i>",
            
            "\\s", "Space (0x20)",
            
            "\\", "\\nnn Octal <i>nnn</i>",
            
            //"\\", "<i>x</i>",
            "\\t", "Tab (0x09)",
            
            "#{", "#{expr}: Value of expr",
            
            "\\v", "Vertical tab (0x0b)",
        };
    private static ImageIcon keywordIcon;
    private static ImageIcon symbolIcon;
    private static final Set<String> selectionTemplates = new HashSet<String>();

    static {
        selectionTemplates.add("begin"); // NOI18N
        selectionTemplates.add("do"); // NOI18N
        selectionTemplates.add("doc"); // NOI18N
        //selectionTemplates.add("dop"); // NOI18N
        selectionTemplates.add("if"); // NOI18N
        selectionTemplates.add("ife"); // NOI18N
    }

    private boolean caseSensitive;
    private int anchor;

    public CodeCompleter() {
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                             : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     * Compute an appropriate prefix to use for code completion.
     * In Strings, we want to return the -whole- string if you're in a
     * require-statement string, otherwise we want to return simply "" or the previous "\"
     * for quoted strings, and ditto for regular expressions.
     * For non-string contexts, just return null to let the default identifier-computation
     * kick in.
     */
    @SuppressWarnings("unchecked")
    public String getPrefix(CompilationInfo info, int lexOffset, boolean upToOffset) {
        try {
            BaseDocument doc = (BaseDocument)info.getDocument();

            TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);

            int requireStart = LexUtilities.getRequireStringOffset(lexOffset, th);

            if (requireStart != -1) {
                // XXX todo - do upToOffset
                return doc.getText(requireStart, lexOffset - requireStart);
            }

            TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

            if (ts == null) {
                return null;
            }

            ts.move(lexOffset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return null;
            }

            if (ts.offset() == lexOffset) {
                // We're looking at the offset to the RIGHT of the caret
                // and here I care about what's on the left
                ts.movePrevious();
            }

            Token<?extends GsfTokenId> token = ts.token();

            if (token != null) {
                TokenId id = token.id();

                // We're within a String that has embedded Ruby. Drop into the
                // embedded language and see if we're within a literal string there.
                if (id == RubyTokenId.EMBEDDED_RUBY) {
                    ts = (TokenSequence)ts.embedded();
                    assert ts != null;
                    ts.move(lexOffset);

                    if (!ts.moveNext() && !ts.movePrevious()) {
                        return null;
                    }

                    token = ts.token();
                    id = token.id();
                }

                String tokenText = token.text().toString();

                if ((id == RubyTokenId.STRING_BEGIN) || (id == RubyTokenId.QUOTED_STRING_BEGIN) ||
                        ((id == RubyTokenId.ERROR) && tokenText.equals("%"))) {
                    int currOffset = ts.offset();

                    // Percent completion
                    if ((currOffset == (lexOffset - 1)) && (tokenText.length() > 0) &&
                            (tokenText.charAt(0) == '%')) {
                        return "%";
                    }
                }
            }

            int doubleQuotedOffset = LexUtilities.getDoubleQuotedStringOffset(lexOffset, th);

            if (doubleQuotedOffset != -1) {
                // Tokenize the string and offer the current token portion as the text
                if (doubleQuotedOffset == lexOffset) {
                    return "";
                } else if (doubleQuotedOffset < lexOffset) {
                    String text = doc.getText(doubleQuotedOffset, lexOffset - doubleQuotedOffset);
                    TokenHierarchy hi =
                        TokenHierarchy.create(text, RubyStringTokenId.languageDouble());

                    TokenSequence seq = hi.tokenSequence();

                    seq.move(lexOffset - doubleQuotedOffset);

                    if (!seq.moveNext() && !seq.movePrevious()) {
                        return "";
                    }

                    TokenId id = seq.token().id();
                    String s = seq.token().text().toString();

                    if ((id == RubyStringTokenId.STRING_ESCAPE) ||
                            (id == RubyStringTokenId.STRING_INVALID)) {
                        return s;
                    } else if (s.startsWith("\\")) {
                        return s;
                    } else {
                        return "";
                    }
                } else {
                    // The String offset is greater than the caret position.
                    // This means that we're inside the string-begin section,
                    // for example here: %q|(
                    // In this case, report no prefix
                    return "";
                }
            }

            int singleQuotedOffset = LexUtilities.getSingleQuotedStringOffset(lexOffset, th);

            if (singleQuotedOffset != -1) {
                if (singleQuotedOffset == lexOffset) {
                    return "";
                } else if (singleQuotedOffset < lexOffset) {
                    String text = doc.getText(singleQuotedOffset, lexOffset - singleQuotedOffset);
                    TokenHierarchy hi =
                        TokenHierarchy.create(text, RubyStringTokenId.languageSingle());

                    TokenSequence seq = hi.tokenSequence();

                    seq.move(lexOffset - singleQuotedOffset);

                    if (!seq.moveNext() && !seq.movePrevious()) {
                        return "";
                    }

                    TokenId id = seq.token().id();
                    String s = seq.token().text().toString();

                    if ((id == RubyStringTokenId.STRING_ESCAPE) ||
                            (id == RubyStringTokenId.STRING_INVALID)) {
                        return s;
                    } else if (s.startsWith("\\")) {
                        return s;
                    } else {
                        return "";
                    }
                } else {
                    // The String offset is greater than the caret position.
                    // This means that we're inside the string-begin section,
                    // for example here: %q|(
                    // In this case, report no prefix
                    return "";
                }
            }

            // Regular expression
            int regexpOffset = LexUtilities.getRegexpOffset(lexOffset, th);

            if ((regexpOffset != -1) && (regexpOffset <= lexOffset)) {
                // This is not right... I need to actually parse the regexp
                // (I should use my Regexp lexer tokens which will be embedded here)
                // such that escaping sequences (/\\\\\/) will work right, or
                // character classes (/[foo\]). In both cases the \ may not mean escape.
                String tokenText = token.text().toString();
                int index = lexOffset - ts.offset();

                if ((index > 0) && (index <= tokenText.length()) &&
                        (tokenText.charAt(index - 1) == '\\')) {
                    return "\\";
                } else {
                    // No prefix for regexps unless it's \
                    return "";
                }

                //return doc.getText(regexpOffset, offset-regexpOffset);
            }

            int lineBegin = Utilities.getRowStart(doc, lexOffset);
            if (lineBegin != -1) {
                int lineEnd = Utilities.getRowEnd(doc, lexOffset);
                String line = doc.getText(lineBegin, lineEnd-lineBegin);
                int lineOffset = lexOffset-lineBegin;
                int start = lineOffset;
                if (lineOffset > 0) {
                    for (int i = lineOffset-1; i >= 0; i--) {
                        char c = line.charAt(i);
                        if (!RubyUtils.isIdentifierChar(c)) {
                            break;
                        } else {
                            start = i;
                        }
                    }
                }
                
                // Find identifier end
                String prefix;
                if (upToOffset ){
                    prefix = line.substring(start, lineOffset);
                } else {
                    if (lineOffset == line.length()) {
                        prefix = line.substring(start);
                    } else {
                        int n = line.length();
                        int end = lineOffset;
                        for (int j = lineOffset; j < n; j++) {
                            char d = line.charAt(j);
                            // Try to accept Foo::Bar as well
                            if (!RubyUtils.isStrictIdentifierChar(d)) {
                                break;
                            } else {
                                end = j+1;
                            }
                        }
                        prefix = line.substring(start, end);
                    }
                }
                
                if (prefix.length() > 0) {
                    if (prefix.endsWith("::")) {
                        return "";
                    }

                    if (prefix.endsWith(":") && prefix.length() > 1) {
                        return null;
                    }

                    // Strip out LHS if it's a qualified method, e.g.  Benchmark::measure -> measure
                    int q = prefix.lastIndexOf("::");

                    if (q != -1) {
                        prefix = prefix.substring(q + 2);
                    }
                    
                    // The identifier chars identified by RubyLanguage are a bit too permissive;
                    // they include things like "=", "!" and even "&" such that double-clicks will
                    // pick up the whole "token" the user is after. But "=" is only allowed at the
                    // end of identifiers for example.
                    if (prefix.length() == 1) {
                        char c = prefix.charAt(0);
                        if (!(Character.isJavaIdentifierPart(c) || c == '@' || c == '$' || c == ':')) {
                            return null;
                        }
                    } else {
                        for (int i = prefix.length()-2; i >= 0; i--) { // -2: the last position (-1) can legally be =, ! or ?
                            char c = prefix.charAt(i);
                            if (i ==0 && c == ':') {
                                // : is okay at the begining of prefixes
                            } else if (!(Character.isJavaIdentifierPart(c) || c == '@' || c == '$')) {
                                prefix = prefix.substring(i+1);
                                break;
                            }
                        }
                    }

                    return prefix;
                }
            }
            // Else: normal identifier: just return null and let the machinery do the rest
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        // Default behavior
        return null;
    }

    private boolean completeKeywords(List<CompletionProposal> proposals, CompletionRequest request,
        boolean isSymbol) {
        
        String prefix = request.prefix;
        
        // Keywords
        if (prefix.equals("$")) {
            // Show dollar variable matches (global vars from the user's
            // code will also be shown
            for (int i = 0, n = RUBY_DOLLAR_VARIABLES.length; i < n; i += 2) {
                String word = RUBY_DOLLAR_VARIABLES[i];
                String desc = RUBY_DOLLAR_VARIABLES[i + 1];

                KeywordItem item = new KeywordItem(word, desc, anchor, request);

                if (isSymbol) {
                    item.setSymbol(true);
                }

                proposals.add(item);
            }
        }

        for (String keyword : RUBY_BUILTIN_VARS) {
            if (startsWith(keyword, prefix)) {
                KeywordItem item = new KeywordItem(keyword, null, anchor, request);

                if (isSymbol) {
                    item.setSymbol(true);
                }

                proposals.add(item);
            }
        }

        for (String keyword : RubyUtils.RUBY_KEYWORDS) {
            if (startsWith(keyword, prefix)) {
                KeywordItem item = new KeywordItem(keyword, null, anchor, request);

                if (isSymbol) {
                    item.setSymbol(true);
                }

                proposals.add(item);
            }
        }

        return false;
    }

    private boolean completeRegexps(List<CompletionProposal> proposals, CompletionRequest request) {
        String prefix = request.prefix;

        // Regular expression matching.  {
        for (int i = 0, n = RUBY_REGEXP_WORDS.length; i < n; i += 2) {
            String word = RUBY_REGEXP_WORDS[i];
            String desc = RUBY_REGEXP_WORDS[i + 1];

            if (startsWith(word, prefix)) {
                KeywordItem item = new KeywordItem(word, desc, anchor, request);
                proposals.add(item);
            }
        }

        return true;
    }

    private boolean completePercentWords(List<CompletionProposal> proposals, CompletionRequest request) {
        String prefix = request.prefix;

        for (int i = 0, n = RUBY_PERCENT_WORDS.length; i < n; i += 2) {
            String word = RUBY_PERCENT_WORDS[i];
            String desc = RUBY_PERCENT_WORDS[i + 1];

            if (startsWith(word, prefix)) {
                KeywordItem item = new KeywordItem(word, desc, anchor, request);
                proposals.add(item);
            }
        }

        return true;
    }

    private boolean completeStringBegins(List<CompletionProposal> proposals, CompletionRequest request) {
        for (int i = 0, n = RUBY_STRING_PAIRS.length; i < n; i += 2) {
            String word = RUBY_STRING_PAIRS[i];
            String desc = RUBY_STRING_PAIRS[i + 1];

            KeywordItem item = new KeywordItem(word, desc, anchor, request);
            proposals.add(item);
        }

        return true;
    }

    /** Determine if we're trying to complete the name for a "def" (in which case
     * we'd show the inherited methods).
     * This needs to be enhanced to handle "Foo." prefixes, e.g. def self.foo
     */
    private boolean completeDefOrInclude(List<CompletionProposal> proposals, CompletionRequest request, String fqn) {
        RubyIndex index = request.index;
        String prefix = request.prefix;
        int lexOffset = request.lexOffset;
        TokenHierarchy<Document> th = request.th;
        NameKind kind = request.kind;
        
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

        if ((index != null) && (ts != null)) {
            ts.move(lexOffset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return false;
            }

            if (ts.offset() == lexOffset) {
                // We're looking at the offset to the RIGHT of the caret
                // position, which could be whitespace, e.g.
                //  "def fo| " <-- looking at the whitespace
                ts.movePrevious();
            }

            Token<?extends GsfTokenId> token = ts.token();

            if (token != null) {
                TokenId id = token.id();

                // See if we're in the identifier - "foo" in "def foo"
                // I could also be a keyword in case the prefix happens to currently
                // match a keyword, such as "next"
                if ((id == RubyTokenId.IDENTIFIER) || (id == RubyTokenId.CONSTANT) || id.primaryCategory().equals("keyword")) {
                    if (!ts.movePrevious()) {
                        return false;
                    }

                    token = ts.token();
                    id = token.id();
                }

                // If we're not in the identifier we need to be in the whitespace after "def"
                if (id != RubyTokenId.WHITESPACE) {
                    // Do something about http://www.netbeans.org/issues/show_bug.cgi?id=100452 here
                    // In addition to checking for whitespace I should look for "Foo." here
                    return false;
                }

                // There may be more than one whitespace; skip them
                while (ts.movePrevious()) {
                    token = ts.token();

                    if (token.id() != RubyTokenId.WHITESPACE) {
                        break;
                    }
                }

                if (token.id() == RubyTokenId.DEF) {
                    Set<IndexedMethod> methods = index.getInheritedMethods(fqn, prefix, kind);

                    for (IndexedMethod method : methods) {
                        // Hmmm, is this necessary? Filtering should happen in the getInheritedMEthods call
                        if ((prefix.length() > 0) && !method.getName().startsWith(prefix)) {
                            continue;
                        }

                        // For def completion, skip local methods, only include superclass and included
                        if ((fqn != null) && fqn.equals(method.getClz())) {
                            continue;
                        }
                        
                        if (method.isNoDoc()) {
                            continue;
                        }

                        // If a method is an "initialize" method I should do something special so that
                        // it shows up as a "constructor" (in a new() statement) but not as a directly
                        // callable initialize method (it should already be culled because it's private)
                        MethodItem item = new MethodItem(method, anchor, request);
                        // Exact matches
                        item.setSmart(method.isSmart());
                        proposals.add(item);
                    }

                    return true;
                } else if (token.id() == RubyTokenId.IDENTIFIER && "include".equals(token.text().toString())) {
                    // Module completion
                    Set<IndexedClass> classes = index.getClasses(prefix, kind, false, true, false);
                    for (IndexedClass clz : classes) {
                        if (clz.isNoDoc()) {
                            continue;
                        }
                        
                        ClassItem item = new ClassItem(clz, anchor, request);
                        item.setSmart(true);
                        proposals.add(item);
                    }     
                    
                    return true;
                }
            }
        }

        return false;
    }

    /** Determine if we're trying to complete the name of a method on another object rather
     * than an inherited or local one. These should list ALL known methods, unless of course
     * we know the type of the method we're operating on (such as strings or regexps),
     * or types inferred through data flow analysis
     *
     * @todo Look for self or this or super; these should be limited to inherited.
     */
    private boolean completeObjectMethod(List<CompletionProposal> proposals, CompletionRequest request, String fqn,
        Call call) {
        
        RubyIndex index = request.index;
        String prefix = request.prefix;
        int astOffset = request.astOffset;
        int lexOffset = request.lexOffset;
        TokenHierarchy<Document> th = request.th;
        BaseDocument doc = request.doc;
        AstPath path = request.path;
        NameKind kind = request.kind;
        FileObject fileObject = request.fileObject;
        Node node = request.node;

        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

        // Look in the token stream for constructs of the type
        //   foo.x^
        // or
        //   foo.^
        // and if found, add all methods
        // (no keywords etc. are possible matches)
        if ((index != null) && (ts != null)) {
            boolean skipPrivate = true;

            if ((call == Call.LOCAL) || (call == Call.NONE)) {
                return false;
            }

            // If we're not sure we're only looking for a method, don't abort after this
            boolean done = call.isMethodExpected();

            boolean skipInstanceMethods = call.isStatic();

            Set<IndexedMethod> methods = Collections.emptySet();

            String type = call.getType();
            String lhs = call.getLhs();

            if ((type == null) && (lhs != null) && (node != null) && call.isSimpleIdentifier()) {
                Node method = AstUtilities.findLocalScope(node, path);

                if (method != null) {
                    // TODO - if the lhs is "foo.bar." I need to split this
                    // up and do it a bit more cleverly
                    TypeAnalyzer analyzer = new TypeAnalyzer(/*request.info.getParserResult(),*/ index, method, node, astOffset, lexOffset, doc, fileObject);
                    type = analyzer.getType(lhs);
                }
            }

            // I'm not doing any data flow analysis at this point, so
            // I can't do anything with a LHS like "foo.". Only actual types.
            if ((type != null) && (type.length() > 0)) {
                if ("self".equals(lhs)) {
                    type = fqn;
                    skipPrivate = false;
                } else if ("super".equals(lhs)) {
                    skipPrivate = false;

                    IndexedClass sc = index.getSuperclass(fqn);

                    if (sc != null) {
                        type = sc.getFqn();
                    } else {
                        ClassNode cls = AstUtilities.findClass(path);

                        if (cls != null) {
                            type = AstUtilities.getSuperclass(cls);
                        }
                    }

                    if (type == null) {
                        type = "Object"; // NOI18N
                    }
                }

                if ((type != null) && (type.length() > 0)) {
                    // Possibly a class on the left hand side: try searching with the class as a qualifier.
                    // Try with the LHS + current FQN recursively. E.g. if we're in
                    // Test::Unit when there's a call to Foo.x, we'll try
                    // Test::Unit::Foo, and Test::Foo
                    while (methods.size() == 0) {
                        methods = index.getInheritedMethods(fqn + "::" + type, prefix, kind);

                        int f = fqn.lastIndexOf("::");

                        if (f == -1) {
                            break;
                        } else {
                            fqn = fqn.substring(0, f);
                        }
                    }

                    // Add methods in the class (without an FQN)
                    Set<IndexedMethod> m = index.getInheritedMethods(type, prefix, kind);

                    if (m.size() > 0) {
                        methods.addAll(m);
                    }
                }
            }

            // Try just the method call (e.g. across all classes). This is ignoring the 
            // left hand side because we can't resolve it.
            if ((methods.size() == 0)) {
                methods = index.getMethods(prefix, null, kind);
            }

            for (IndexedMethod method : methods) {
                // Don't include private or protected methods on other objects
                if (skipPrivate && (method.isPrivate() && !"new".equals(method.getName()))) {
                    // TODO - "initialize" removal here should not be necessary since they should
                    // be marked as private, but index doesn't contain that yet
                    continue;
                }

                // We can only call static methods
                if (skipInstanceMethods && !method.isStatic()) {
                    continue;
                }

                if (method.isNoDoc()) {
                    continue;
                }

                if (method.getMethodType() == IndexedMethod.MethodType.DBCOLUMN) {
                    DbItem item = new DbItem(method.getName(), method.getIn(), anchor, request);
                    proposals.add(item);
                    continue;
                }

                MethodItem methodItem = new MethodItem(method, anchor, request);
                // Exact matches
                methodItem.setSmart(method.isSmart());
                proposals.add(methodItem);
            }

            return done;
        }

        return false;
    }

    /** Determine if we're trying to complete the name for a "def" (in which case
     * we'd show the inherited methods) */
    private boolean completeClasses(List<CompletionProposal> proposals, CompletionRequest request,
        boolean showSymbols, Call call) {

        RubyIndex index = request.index;
        String prefix = request.prefix;
        NameKind kind = request.kind;
        
        int classAnchor = anchor;
        int fqnIndex = prefix.lastIndexOf("::");

        if (fqnIndex != -1) {
            classAnchor += (fqnIndex + 2);
        }

        String fullPrefix = prefix;

        // foo.| or foo.b|  -> we're expecting a method call. For Foo:: we don't know.
        if (call.isMethodExpected()) {
            return false;
        }

        String type = call.getType();
        String lhs = call.getLhs();

        if ((lhs != null) && lhs.equals(type)) {
            fullPrefix = type + "::" + prefix;
        }

        AstPath path = request.path;
        String ctx = AstUtilities.getFqnName(path);

        Set<String> uniqueClasses = new HashSet<String>();
        Set<IndexedClass> classes = index.getClasses(fullPrefix, kind, false, false, false, RubyIndex.ALL_SCOPE, uniqueClasses);

        // Also try looking or classes scoped by the current class
        if ((ctx != null) && (ctx.length() > 0)) {
            Set<IndexedClass> extraClasses = index.getClasses(ctx + "::" + fullPrefix, kind, false, false, false, RubyIndex.ALL_SCOPE, uniqueClasses);
            classes.addAll(extraClasses);
        }

        // Prefix the current class if necessary
        for (IndexedClass cls : classes) {
            if (cls.isNoDoc()) {
                continue;
            }

            ClassItem item = new ClassItem(cls, classAnchor, request);
            item.setSmart(true);

            if (showSymbols) {
                item.setSymbol(true);
            }

            proposals.add(item);
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean completeStrings(List<CompletionProposal> proposals, CompletionRequest request) {
        RubyIndex index = request.index;
        String prefix = request.prefix;
        int lexOffset = request.lexOffset;
        TokenHierarchy<Document> th = request.th;
        
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

        if ((index != null) && (ts != null)) {
            ts.move(lexOffset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return false;
            }

            if (ts.offset() == lexOffset) {
                // We're looking at the offset to the RIGHT of the caret
                // and here I care about what's on the left
                ts.movePrevious();
            }

            Token<?extends GsfTokenId> token = ts.token();

            if (token != null) {
                TokenId id = token.id();

                if (id == RubyTokenId.LINE_COMMENT || id == RubyTokenId.DOCUMENTATION) {
                    // Comment completion - rdoc tags and such
                    
                    if (request.queryType == QueryType.DOCUMENTATION) {
                        BaseDocument doc = request.doc;
                        OffsetRange commentBlock = LexUtilities.getCommentBlock(doc, lexOffset);
                        
                        if (commentBlock != OffsetRange.NONE) {
                            try {
                                String text = doc.getText(commentBlock.getStart(), commentBlock.getLength());
                                Element element = new CommentElement(text);
                                ClassItem item = new ClassItem(element, anchor, request);
                                proposals.add(item);
                                return true;
                            } catch (BadLocationException ble) {
                                Exceptions.printStackTrace(ble);
                            }
                        }
                    }
                    
                    // No other possible completions in comments
                    return true;
                }
                
                // We're within a String that has embedded Ruby. Drop into the
                // embedded language and see if we're within a literal string there.
                if (id == RubyTokenId.EMBEDDED_RUBY) {
                    ts = (TokenSequence)ts.embedded();
                    assert ts != null;
                    ts.move(lexOffset);

                    if (!ts.moveNext() && !ts.movePrevious()) {
                        return false;
                    }

                    token = ts.token();
                    id = token.id();
                }

                boolean inString = false;
                boolean isQuoted = false;
                boolean inRegexp = false;
                String tokenText = token.text().toString();

                // Percent completion
                if ((id == RubyTokenId.STRING_BEGIN) || (id == RubyTokenId.QUOTED_STRING_BEGIN) ||
                        ((id == RubyTokenId.ERROR) && tokenText.equals("%"))) {
                    int offset = ts.offset();

                    if ((offset == (lexOffset - 1)) && (tokenText.length() > 0) &&
                            (tokenText.charAt(0) == '%')) {
                        if (completePercentWords(proposals, request)) {
                            return true;
                        }
                    }
                }

                // Incomplete String/Regexp marker:  %x|{
                if (((id == RubyTokenId.STRING_BEGIN) || (id == RubyTokenId.QUOTED_STRING_BEGIN) ||
                        (id == RubyTokenId.REGEXP_BEGIN)) &&
                        ((token.length() == 3) && (lexOffset == (ts.offset() + 2)))) {
                    if (Character.isLetter(tokenText.charAt(1))) {
                        completeStringBegins(proposals, request);

                        return true;
                    }
                }

                // Skip back to the beginning of the String. I have to loop since I
                // may have embedded Ruby segments.
                while ((id == RubyTokenId.ERROR) || (id == RubyTokenId.STRING_LITERAL) ||
                        (id == RubyTokenId.QUOTED_STRING_LITERAL) ||
                        (id == RubyTokenId.REGEXP_LITERAL) || (id == RubyTokenId.EMBEDDED_RUBY)) {
                    if (id == RubyTokenId.QUOTED_STRING_LITERAL) {
                        isQuoted = true;
                    }
                    if (!ts.movePrevious()) {
                        return false;
                    }

                    token = ts.token();
                    id = token.id();
                }

                if (id == RubyTokenId.STRING_BEGIN) {
                    inString = true;
                } else if (id == RubyTokenId.QUOTED_STRING_BEGIN) {
                    inString = true;
                    isQuoted = true;
                } else if (id == RubyTokenId.REGEXP_BEGIN) {
                    inRegexp = true;
                }

                if (inRegexp) {
                    if (completeRegexps(proposals, request)) {
                        return true;
                    }
                } else if (inString) {
                    // Completion of literal strings within require calls
                    while (ts.movePrevious()) {
                        token = ts.token();

                        if ((token.id() == RubyTokenId.WHITESPACE) ||
                                (token.id() == RubyTokenId.LPAREN) ||
                                (token.id() == RubyTokenId.STRING_LITERAL) ||
                                (token.id() == RubyTokenId.QUOTED_STRING_LITERAL) ||
                                (token.id() == RubyTokenId.STRING_BEGIN) ||
                                (token.id() == RubyTokenId.QUOTED_STRING_BEGIN)) {
                            continue;
                        }

                        if (token.id() == RubyTokenId.IDENTIFIER) {
                            String text = token.text().toString();

                            if (text.equals("require") || text.equals("load")) {
                                // Do require-completion
                                Set<String[]> requires =
                                    index.getRequires(prefix,
                                        caseSensitive ? NameKind.PREFIX
                                                      : NameKind.CASE_INSENSITIVE_PREFIX);

                                for (String[] require : requires) {
                                    assert require.length == 2;

                                    // If a method is an "initialize" method I should do something special so that
                                    // it shows up as a "constructor" (in a new() statement) but not as a directly
                                    // callable initialize method (it should already be culled because it's private)
                                    KeywordItem item =
                                        new KeywordItem(require[0], require[1], anchor, request);
                                    proposals.add(item);
                                }

                                return true;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }

                    if (inString && isQuoted) {
                        for (int i = 0, n = RUBY_QUOTED_STRING_ESCAPES.length; i < n; i += 2) {
                            String word = RUBY_QUOTED_STRING_ESCAPES[i];
                            String desc = RUBY_QUOTED_STRING_ESCAPES[i + 1];

                            if (!word.startsWith(prefix)) {
                                continue;
                            }

                            KeywordItem item = new KeywordItem(word, desc, anchor, request);
                            proposals.add(item);
                        }

                        return true;
                    } else if (inString) {
                        // No completions for single quoted strings
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    private int callLineStart = -1;
    private IndexedMethod callMethod;

    /** Compute the current method call at the given offset. Returns false if we're not in a method call. 
     * The argument index is returned in parameterIndexHolder[0] and the method being
     * called in methodHolder[0].
     */
    boolean computeMethodCall(CompilationInfo info, int lexOffset, int astOffset,
            IndexedMethod[] methodHolder, int[] parameterIndexHolder, int[] anchorOffsetHolder,
            Set<IndexedMethod>[] alternativesHolder) {
        try {
            Node root = AstUtilities.getRoot(info);

            if (root == null) {
                return false;
            }

            IndexedMethod targetMethod = null;
            int index = -1;

            AstPath path = null;
            // Account for input sanitation
            // TODO - also back up over whitespace, and if I hit the method
            // I'm parameter number 0
            int originalAstOffset = astOffset;

            // Adjust offset to the left
            BaseDocument doc = (BaseDocument) info.getDocument();
            int newLexOffset = LexUtilities.findSpaceBegin(doc, lexOffset);
            if (newLexOffset < lexOffset) {
                astOffset -= (lexOffset-newLexOffset);
            }

            RubyParseResult rpr = (RubyParseResult)info.getParserResult();
            OffsetRange range = rpr.getSanitizedRange();
            if (range != OffsetRange.NONE && range.containsInclusive(astOffset)) {
                if (astOffset != range.getStart()) {
                    astOffset = range.getStart()-1;
                    if (astOffset < 0) {
                        astOffset = 0;
                    }
                    path = new AstPath(root, astOffset);
                }
            }

            if (path == null) {
                path = new AstPath(root, astOffset);
            }

            int currentLineStart = Utilities.getRowStart(doc, lexOffset);
            if (callLineStart != -1 && currentLineStart == callLineStart) {
                // We know the method call
                targetMethod = callMethod;
                if (targetMethod != null) {
                    // Somehow figure out the argument index
                    // Perhaps I can keep the node tree around and look in it
                    // (This is all trying to deal with temporarily broken
                    // or ambiguous calls.
                }
            }
            // Compute the argument index

            Node call = null;
            int anchorOffset = -1;

            if (targetMethod != null) {
                Iterator<Node> it = path.leafToRoot();
                String name = targetMethod.getName();
                while (it.hasNext()) {
                    Node node = it.next();
                    if (AstUtilities.isCall(node) &&
                            name.equals(AstUtilities.getCallName(node))) {
                        if (node.nodeId == NodeTypes.CALLNODE) {
                            Node argsNode = ((CallNode)node).getArgsNode();

                            if (argsNode != null) {
                                index = AstUtilities.findArgumentIndex(argsNode, astOffset);

                                if (index == -1 && astOffset < originalAstOffset) {
                                    index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
                                }

                                if (index != -1) {
                                    call = node;
                                    anchorOffset = argsNode.getPosition().getStartOffset();
                                }
                            }
                        } else if (node.nodeId == NodeTypes.FCALLNODE) {
                            Node argsNode = ((FCallNode)node).getArgsNode();

                            if (argsNode != null) {
                                index = AstUtilities.findArgumentIndex(argsNode, astOffset);

                                if (index == -1 && astOffset < originalAstOffset) {
                                    index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
                                }

                                if (index != -1) {
                                    call = node;
                                    anchorOffset = argsNode.getPosition().getStartOffset();
                                }
                            }
                        } else if (node.nodeId == NodeTypes.VCALLNODE) {
                            // We might be completing at the end of a method call
                            // and we don't have parameters yet so it just looks like
                            // a vcall, e.g.
                            //   create_table |
                            // This is okay as long as the caret is outside and to
                            // the right of this call. However
                            final OffsetRange callRange = AstUtilities.getCallRange(node);
                            AstUtilities.getCallName(node);
                            if (originalAstOffset > callRange.getEnd()) {
                                index = 0;
                                call = node;
                                anchorOffset = callRange.getEnd()+1;
                            }
                        }
                        
                        break;
                    }
                }
            }

            boolean haveSanitizedComma = rpr.getSanitized() == Sanitize.EDITED_DOT ||
                    rpr.getSanitized() == Sanitize.ERROR_DOT;
            if (haveSanitizedComma) {
                // We only care about removed commas since that
                // affects the parameter count
                if (rpr.getSanitizedContents().indexOf(',') == -1) {
                    haveSanitizedComma = false;
                }
            }

            if (call == null) {
                // Find the call in around the caret. Beware of 
                // input sanitization which could have completely
                // removed the current parameter (e.g. with just
                // a comma, or something like ", @" or ", :")
                // where we accidentally end up in the previous
                // parameter.
                ListIterator<Node> it = path.leafToRoot();
             nodesearch:
                while (it.hasNext()) {
                    Node node = it.next();

                    if (node.nodeId == NodeTypes.CALLNODE) {
                        final OffsetRange callRange = AstUtilities.getCallRange(node);
                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
                            for (int i = 0; i < 3; i++) {
                                // It's not really a peek in the sense
                                // that there's no reason to retry these
                                // nodes later
                                Node peek = it.next();
                                if (AstUtilities.isCall(peek) &&
                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(info, peek.getPosition().getStartOffset())) ==
                                        Utilities.getRowStart(doc, lexOffset)) {
                                    // Use the outer method call instead
                                    it.previous();
                                    continue nodesearch;
                                }
                            }
                        }
                        
                        Node argsNode = ((CallNode)node).getArgsNode();

                        if (argsNode != null) {
                            index = AstUtilities.findArgumentIndex(argsNode, astOffset);

                            if (index == -1 && astOffset < originalAstOffset) {
                                index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
                            }

                            if (index != -1) {
                                call = node;
                                anchorOffset = argsNode.getPosition().getStartOffset();

                                break;
                            }
                        } else {
                            if (originalAstOffset > callRange.getEnd()) {
                                index = 0;
                                call = node;
                                anchorOffset = callRange.getEnd()+1;
                                break;
                            }
                        }
                    } else if (node.nodeId == NodeTypes.FCALLNODE) {
                        final OffsetRange callRange = AstUtilities.getCallRange(node);
                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
                            for (int i = 0; i < 3; i++) {
                                // It's not really a peek in the sense
                                // that there's no reason to retry these
                                // nodes later
                                Node peek = it.next();
                                if (AstUtilities.isCall(peek) &&
                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(info, peek.getPosition().getStartOffset())) ==
                                        Utilities.getRowStart(doc, lexOffset)) {
                                    // Use the outer method call instead
                                    it.previous();
                                    continue nodesearch;
                                }
                            }
                        }
                        
                        Node argsNode = ((FCallNode)node).getArgsNode();

                        if (argsNode != null) {
                            index = AstUtilities.findArgumentIndex(argsNode, astOffset);

                            if (index == -1 && astOffset < originalAstOffset) {
                                index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
                            }

                            if (index != -1) {
                                call = node;
                                anchorOffset = argsNode.getPosition().getStartOffset();

                                break;
                            }
                        }
                    } else if (node.nodeId == NodeTypes.VCALLNODE) {
                        // We might be completing at the end of a method call
                        // and we don't have parameters yet so it just looks like
                        // a vcall, e.g.
                        //   create_table |
                        // This is okay as long as the caret is outside and to
                        // the right of this call.
                        
                        final OffsetRange callRange = AstUtilities.getCallRange(node);
                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
                            for (int i = 0; i < 3; i++) {
                                // It's not really a peek in the sense
                                // that there's no reason to retry these
                                // nodes later
                                Node peek = it.next();
                                if (AstUtilities.isCall(peek) &&
                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(info, peek.getPosition().getStartOffset())) ==
                                        Utilities.getRowStart(doc, lexOffset)) {
                                    // Use the outer method call instead
                                    it.previous();
                                    continue nodesearch;
                                }
                            }
                        }
                        
                        if (originalAstOffset > callRange.getEnd()) {
                            index = 0;
                            call = node;
                            anchorOffset = callRange.getEnd()+1;
                            break;
                        }
                    }
                }
            }

            if (index != -1 && haveSanitizedComma && call != null) {
                Node an = null;
                if (call.nodeId == NodeTypes.FCALLNODE) {
                    an = ((FCallNode)call).getArgsNode();
                } else if (call.nodeId == NodeTypes.CALLNODE) {
                    an = ((CallNode)call).getArgsNode();
                }
                if (an != null && index < an.childNodes().size() &&
                        ((Node)an.childNodes().get(index)).nodeId == NodeTypes.HASHNODE) {
                    // We should stay within the hashnode, so counteract the
                    // index++ which follows this if-block
                    index--;
                }

                // Adjust the index to account for our removed
                // comma
                index++;
            }
            
            if ((call == null) || (index == -1)) {
                callLineStart = -1;
                callMethod = null;
                return false;
            } else if (targetMethod == null) {
                // Look up the
                // See if we can find the method corresponding to this call
                targetMethod = new DeclarationFinder().findMethodDeclaration(info, call, path, 
                        alternativesHolder);
                if (targetMethod == null) {
                    return false;
                }
            }

            callLineStart = currentLineStart;
            callMethod = targetMethod;

            methodHolder[0] = callMethod;
            parameterIndexHolder[0] = index;

            // TODO - if you're in a splat node, I should be highlighting the splat node!!
            if (anchorOffset == -1) {
                anchorOffset = call.getPosition().getStartOffset(); // TODO - compute
            }
            anchorOffsetHolder[0] = anchorOffset;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return false;
        }

        return true;
    }
    
    private boolean addParameters(List<CompletionProposal> proposals, CompletionRequest request) {
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        @SuppressWarnings("unchecked")
        Set<IndexedMethod>[] alternatesHolder = new Set[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        CompilationInfo info = request.info;
        int lexOffset = request.lexOffset;
        int astOffset = request.astOffset;
        if (!computeMethodCall(info, lexOffset, astOffset,
                methodHolder, paramIndexHolder, anchorOffsetHolder, alternatesHolder)) {

            return false;
        }

        IndexedMethod targetMethod = methodHolder[0];
        int index = paramIndexHolder[0];
        
        if (INCLUDE_CALL_ITEMS) {
            CallItem callItem = new CallItem(targetMethod, index, anchor, request);
            proposals.add(callItem);
            // Also show other documented, not nodoc'ed items (except for those
            // with identical signatures, such as overrides of the same method)
            if (alternatesHolder[0] != null) {
                Set<String> signatures = new HashSet<String>();
                signatures.add(targetMethod.getSignature().substring(targetMethod.getSignature().indexOf('#')+1));
                for (IndexedMethod m : alternatesHolder[0]) {
                    if (m != targetMethod && m.isDocumented() && !m.isNoDoc()) {
                        String sig = m.getSignature().substring(m.getSignature().indexOf('#')+1);
                        if (!signatures.contains(sig)) {
                            CallItem item = new CallItem(m, index, anchor, request);
                            proposals.add(item);
                            signatures.add(sig);
                        }
                    }
                }
            }
        }
        
        List<String> params = targetMethod.getParameters();
        if (params == null || params.size() <= index) {
            return false;
        }

        boolean isLastArg = index < params.size()-1;
        
        String attrs = targetMethod.getEncodedAttributes();
        if (attrs != null && attrs.length() > 0) {
            int offset = -1;
            for (int i = 0; i < 3; i++) {
                offset = attrs.indexOf(';', offset+1);
                if (offset == -1) {
                    break;
                }
            }
            if (offset == -1) {
                Node root = null;
                if (info != null) {
                    root = AstUtilities.getRoot(info);
                }

                IndexedElement match = findDocumentationEntry(root, targetMethod);
                if (match == targetMethod || !(match instanceof IndexedMethod)) {
                    return false;
                }
                targetMethod = (IndexedMethod)match;
                attrs = targetMethod.getEncodedAttributes();
                if (attrs != null && attrs.length() > 0) {
                    offset = -1;
                    for (int i = 0; i < 3; i++) {
                        offset = attrs.indexOf(';', offset+1);
                        if (offset == -1) {
                            break;
                        }
                    }
                }
            }
            String currentName = params.get(index);
            if (currentName.startsWith("*")) {
                // * and & are part of the sig
                currentName = currentName.substring(1);
            } else if (currentName.startsWith("&")) {
                currentName = currentName.substring(1);
            }
            if (offset != -1) {
                // Pick apart
                attrs = attrs.substring(offset+1);
                if (attrs.length() == 0) {
                    return false;
                }
                String[] argEntries = attrs.split(",");
                for (String entry : argEntries) {
                    int parenIndex = entry.indexOf('(');
                    assert parenIndex != -1 : attrs;
                    String name = entry.substring(0, parenIndex);
                    if  (currentName.equals(name)) {
                        // Found a special parameter desc entry for this
                        // parameter - decode it and create completion items
                        // Decode
                        int endIndex = entry.indexOf(')', parenIndex);
                        assert endIndex != -1;
                        String data = entry.substring(parenIndex+1, endIndex);
                        if (data.length() > 0 && data.charAt(0) == '-') {
                            // It's a plain item (e.g. not a hash etc) where
                            // we have some logical types to complete
                            if ("-table".equals(data)) {
                                completeDbTables(proposals, targetMethod, request, isLastArg);
                                // Not exiting - I may have other entries here too
                            } else if ("-column".equals(data)) {
                                completeDbColumns(proposals, targetMethod, request, isLastArg);
                                // Not exiting - I may have other entries here too
                            } else if ("-model".equals(data)) {
                                completeModels(proposals, targetMethod, request, isLastArg);
                            }
                        } else if (data.startsWith("=>")) {
                            // It's a hash; show the given keys
                            // TODO: Determine if the caret is in the
                            // value part, and if so, show the values instead
                            // Uhm... what about fields and such?
                            completeHash(proposals, request, targetMethod, data, isLastArg);
                            // Not exiting - I may have a non-hash entry here too!
                        } else {
                            // Just show a fixed set of values
                            completeFixed(proposals, request, targetMethod, data, isLastArg);
                            // Not exiting - I may have other entries here too
                        }
                    }
                }
            }
        }
        
        return true;
    }

//    /** Handle insertion of :action, :controller, etc. even for methods without
//     * actual method signatures. Operate at the lexical level.
//     */
//    private void handleRailsKeys(List<CompletionProposal> proposals, CompletionRequest request, IndexedMethod target, String data, boolean isLastArg) {
//        TokenSequence ts = LexUtilities.getRubyTokenSequence(request.doc, anchor);
//        if (ts == null) {
//            return;
//        }
//        boolean inValue = false;
//        ts.move(anchor);
//        String line = null;
//        while (ts.movePrevious()) {
//            final Token token = ts.token();
//            if (token.id() == RubyTokenId.WHITESPACE) {
//                continue;
//            } else if (token.id() == RubyTokenId.NONUNARY_OP &&
//                    (token.text().toString().equals("=>"))) { // NOI18N
//                inValue = true;
//                // TODO - continue on to find out what the key is
//                try {
//                    BaseDocument doc = request.doc;
//                    int lineStart = Utilities.getRowStart(doc, ts.offset());
//                    line = doc.getText(lineStart, ts.offset()-lineStart).trim();
//                } catch (BadLocationException ble) {
//                    Exceptions.printStackTrace(ble);
//                    return;
//                }
//            } else {
//                break;
//            }
//        }
//
//        if (inValue) {
//            if (line.endsWith(":action")) {
//                // TODO
//            } else if (line.endsWith(":controller")) {
//                // Dynamically produce controllers
//                List<String> controllers = RubyUtils.getControllerNames(request.fileObject, true);
//                String prefix = request.prefix;
//                for (String n : controllers) {
//                    n = "'" + n + "'";
//                    if (startsWith(n, prefix)) {
//                        String insert = n;
//                        if (!isLastArg) {
//                            insert = insert + ", ";
//                        }
//                        ParameterItem item = new ParameterItem(target, n, null, insert,  anchor, request);
//                        item.setSymbol(true);
//                        item.setSmart(true);
//                        proposals.add(item);
//                    }
//                }
//            } else if (line.endsWith(":partial")) {
//                // TODO
//            }
//        }
//    }

    private boolean completeHash(List<CompletionProposal> proposals, CompletionRequest request, IndexedMethod target, String data, boolean isLastArg) {
        assert data.startsWith("=>");
        data = data.substring(2);
        String prefix = request.prefix;
        
        // Determine if we're in the key part or the value part when completing
        boolean inValue = false;
        TokenSequence ts = LexUtilities.getRubyTokenSequence(request.doc, anchor);
        if (ts == null) {
            return false;
        }
        ts.move(anchor);
        String line = null;
        
        while (ts.movePrevious()) {
            final Token token = ts.token();
            if (token.id() == RubyTokenId.WHITESPACE) {
                continue;
            } else if (token.id() == RubyTokenId.NONUNARY_OP &&
                    (token.text().toString().equals("=>"))) { // NOI18N
                inValue = true;
                // TODO - continue on to find out what the key is
                try {
                    BaseDocument doc = request.doc;
                    int lineStart = Utilities.getRowStart(doc, ts.offset());
                    line = doc.getText(lineStart, ts.offset()-lineStart).trim();
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                    return false;
                }
            } else {
                break;
            }
        }

        List<String> suggestions = new ArrayList<String>();
        
        String key = null;
        String[] values = data.split("\\|");
        if (inValue) {
            // Find the key and see if we have a type to offer for it
            for (String value : values) {
                int typeIndex = value.indexOf(':');
                if (typeIndex != -1) {
                    String name = value.substring(0, typeIndex);
                    if (line.endsWith(name)) {
                        key = name;
                        // Score - it appears we're using the
                        // key for this item
                        String type = value.substring(typeIndex+1);
                        if ("nil".equals(type)) { // NOI18N
                            suggestions.add("nil"); // NOI18N
                        } else if ("bool".equals(type)) { // NOI18N
                            suggestions.add("true"); // NOI18N
                            suggestions.add("false"); // NOI18N
                        } else if ("submitmethod".equals(type)) { // NOI18N
                            suggestions.add("post"); // NOI18N
                            suggestions.add("get"); // NOI18N
                        } else if ("validationactive".equals(type)) { // NOI18N
                            suggestions.add(":save"); // NOI18N
                            suggestions.add(":create"); // NOI18N
                            suggestions.add(":update"); // NOI18N
                        } else if ("string".equals(type)) { // NOI18N
                            suggestions.add("\""); // NOI18N
                        } else if ("hash".equals(type)) { // NOI18N
                            suggestions.add("{"); // NOI18N
                        } else if ("controller".equals(type)) {
                            // Dynamically produce controllers
                            List<String> controllers = RubyUtils.getControllerNames(request.fileObject, true);
                            for (String n : controllers) {
                                suggestions.add("'" + n + "'");
                            }
                        } else if ("action".equals(type)) {
                            // Dynamically produce actions
                            // This would need to be scoped by the current
                            // context - look at the hash, find the specified
                            // controller and limit it to that
                            List<String> actions = getActionNames(request);
                            for (String n : actions) {
                                suggestions.add("'" + n + "'");
                            }
                        }
                    }
                }
            }
        } else {
            for (String value : values) {
                int typeIndex = value.indexOf(':');
                if (typeIndex != -1) {
                    value = value.substring(0, typeIndex);
                }
                value = ":" + value + " => ";
                suggestions.add(value);
            }
        }

        // I've gotta clean up the colon handling in complete()
        // I originally stripped ":" to make direct (INameNode)getName()
        // comparisons on symbols work directly but it's becoming a liability now
        String colonPrefix = ":" + prefix;
        for (String suggestion : suggestions) {
            if (startsWith(suggestion, prefix) || startsWith(suggestion, colonPrefix)) {
                String insert = suggestion;
                String desc = null;
                if (inValue) {
                    if (!isLastArg) {
                        insert = insert + ", ";
                    }
                    if (key != null) {
                        desc = ":" + key + " = " + suggestion;
                    }
                }
                ParameterItem item = new ParameterItem(target, suggestion, desc, insert,  anchor, request);
                item.setSymbol(true);
                item.setSmart(true);
                proposals.add(item);
            }
        }
        
        return true;
    }

    /** Get the actions for the given file. If the file is a controller, list the actions within it,
     * otherwise, if the file is a view, list the actions for the corresponding controller.
     * 
     * @param fileInProject the file we're looking up
     * @return A List of action names
     */
    private List<String> getActionNames(CompletionRequest request) {
        FileObject file = request.fileObject;
        FileObject controllerFile = null;
        if (file.getNameExt().endsWith("_controller.rb")) {
            controllerFile = file;
        } else {
            controllerFile = RubyUtils.getRailsControllerFor(file);
        }
        // TODO - check for other :controller-> settings in the hashmap and if present, use it
        if (controllerFile == null) {
            return Collections.emptyList();
        }
        
        String controllerClass = RubyUtils.getControllerClass(controllerFile);
        if (controllerClass != null) {
            String prefix = request.prefix;
            Set<IndexedMethod> methods = request.index.getMethods(prefix, controllerClass, request.kind);
            List<String> actions = new ArrayList<String>();
            for (IndexedMethod method : methods) {
                if (method.isPublic() && method.getArgs() == null || method.getArgs().length == 0) {
                    actions.add(method.getName());
                }
            }
            
            return actions;
        }
        
        // TODO - pull out the methods or this class
        
        return Collections.emptyList();
    }
    
    private void completeFixed(List<CompletionProposal> proposals, CompletionRequest request, IndexedMethod target, String data, boolean isLastArg) {
        String[] values = data.split("\\|");
        String prefix = request.prefix;
        // I originally stripped ":" to make direct (INameNode)getName()
        // comparisons on symbols work directly but it's becoming a liability now
        String colonPrefix = ":" + prefix;
        for (String value : values) {
            if (startsWith(value, prefix) || startsWith(value, colonPrefix)) {
                String insert = isLastArg ? value : (value + ", ");
                ParameterItem item = new ParameterItem(target, value, null, insert, anchor, request);
                item.setSymbol(true);
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }
    
    private void completeDbTables(List<CompletionProposal> proposals, IndexedMethod target, CompletionRequest request, boolean isLastArg) {
        // Add in the eligible database tables found in this project
        // Assumes this is a Rails project
        String p = request.prefix;
        String colonPrefix = p;
        if (":".equals(p)) { // NOI18N
            p = "";
        } else {
            colonPrefix = ":" + p; // NOI18N
        }
        Set<String> tables = request.index.getDatabaseTables(p, request.kind);
        
        // I originally stripped ":" to make direct (INameNode)getName()
        // comparisons on symbols work directly but it's becoming a liability now
        String prefix = request.prefix;
        for (String table : tables) {
            // PENDING: Should I insert :tablename or 'tablename' or "tablename" ?
            String tableName = ":" + table;
            if (startsWith(tableName, prefix) || startsWith(tableName, colonPrefix)) {
                String insert = isLastArg ? tableName : (tableName + ", ");
                ParameterItem item = new ParameterItem(target, tableName, null, insert, anchor, request);
                item.setSymbol(true);
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }

    private void completeModels(List<CompletionProposal> proposals, IndexedMethod target, CompletionRequest request, boolean isLastArg) {
        Set<IndexedClass> clz = request.index.getSubClasses(request.prefix, "ActiveRecord::Base", request.kind, RubyIndex.SOURCE_SCOPE);
        
        String prefix = request.prefix;
        // I originally stripped ":" to make direct (INameNode)getName()
        // comparisons on symbols work directly but it's becoming a liability now
        String colonPrefix = ":" + prefix;
        for (IndexedClass c : clz) {
            String name = c.getName();
            String symbol = ":"+RubyUtils.camelToUnderlinedName(name);
            if (startsWith(symbol, prefix) || startsWith(symbol, colonPrefix)) {
                String insert = isLastArg ? symbol : (symbol + ", ");
                ParameterItem item = new ParameterItem(target, symbol, name, insert, anchor, request);
                item.setSymbol(true);
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }
    
    private void completeDbColumns(List<CompletionProposal> proposals, IndexedMethod target, CompletionRequest request, boolean isLastArg) {
        // Add in the eligible database tables found in this project
        // Assumes this is a Rails project
        Set<String> tables = request.index.getDatabaseTables(request.prefix, request.kind);
        
        // TODO
//        for (String table : tables) {
//            if (startsWith(table, prefix)) {
//                SymbolHashItem item = new SymbolHashItem(target, ":" + table, null, anchor, request);
//                item.setSymbol(true);
//                proposals.add(item);
//            }
//        }
    }
    

    // TODO: Move to the top
    public List<CompletionProposal> complete(final CompilationInfo info, int lexOffset, String prefix,
        final NameKind kind, final QueryType queryType, final boolean caseSensitive, final HtmlFormatter formatter) {
        this.caseSensitive = caseSensitive;

        final int astOffset = AstUtilities.getAstOffset(info, lexOffset);
        if (astOffset == -1) {
            return null;
        }
        
        // Avoid all those annoying null checks
        if (prefix == null) {
            prefix = "";
        }

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        anchor = lexOffset - prefix.length();

        final RubyIndex index = RubyIndex.get(info.getIndex());

        final Document document;
        try {
            document = info.getDocument();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return null;
        }

        // TODO - move to LexUtilities now that this applies to the lexing offset?
        lexOffset = AstUtilities.boundCaretOffset(info, lexOffset);

        // Discover whether we're in a require statement, and if so, use special completion
        final TokenHierarchy<Document> th = TokenHierarchy.get(document);
        final BaseDocument doc = (BaseDocument)document;
        final FileObject fileObject = info.getFileObject();

        boolean showLower = true;
        boolean showUpper = true;
        boolean showSymbols = false;
        char first = 0;

        if (prefix.length() > 0) {
            first = prefix.charAt(0);

            // Foo::bar --> first char is "b" - we're looking for a method
            int qualifier = prefix.lastIndexOf("::");

            if ((qualifier != -1) && (qualifier < (prefix.length() - 2))) {
                first = prefix.charAt(qualifier + 2);
            }

            showLower = Character.isLowerCase(first);
            // showLower is not necessarily !showUpper - prefix can be ":foo" for example
            showUpper = Character.isUpperCase(first);

            if (first == ':') {
                showSymbols = true;

                if (prefix.length() > 1) {
                    char second = prefix.charAt(1);
                    prefix = prefix.substring(1);
                    showLower = Character.isLowerCase(second);
                    showUpper = Character.isUpperCase(second);
                }
            }
        }
        
        // Carry completion context around since this logic is split across lots of methods
        // and I don't want to pass dozens of parameters from method to method; just pass
        // a request context with supporting info needed by the various completion helpers i
        CompletionRequest request = new CompletionRequest();
        request.formatter = formatter;
        request.lexOffset = lexOffset;
        request.astOffset = astOffset;
        request.index = index;
        request.doc = doc;
        request.info = info;
        request.prefix = prefix;
        request.th = th;
        request.kind = kind;
        request.queryType = queryType;
        request.fileObject = fileObject;
        
        // See if we're inside a string or regular expression and if so,
        // do completions applicable to strings - require-completion,
        // escape codes for quoted strings and regular expressions, etc.
        if (completeStrings(proposals, request)) {
            return proposals;
        }
        
        Call call = Call.getCallType(doc, th, lexOffset);

        // Fields
        // This is a bit stupid at the moment, not looking at the current typing context etc.
        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            completeKeywords(proposals, request, showSymbols);

            return proposals;
        }

        // Compute the bounds of the line that the caret is on, and suppress nodes overlapping the line.
        // This will hide not only paritally typed identifiers, but surrounding contents like the current class and module
        final int astLineBegin;
        final int astLineEnd;

        try {
            astLineBegin = AstUtilities.getAstOffset(info, Utilities.getRowStart(doc, lexOffset));
            astLineEnd = AstUtilities.getAstOffset(info, Utilities.getRowEnd(doc, lexOffset));
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return null;
        }

        final AstPath path = new AstPath(root, astOffset);
        request.path = path;

        Map<String, Node> variables = new HashMap<String, Node>();
        Map<String, Node> fields = new HashMap<String, Node>();
        Map<String, Node> globals = new HashMap<String, Node>();
        Map<String, Node> constants = new HashMap<String, Node>();

        final Node closest = path.leaf();
        request.node = closest;

        // Don't try to add local vars, globals etc. as part of calls or class fqns
        if (call.getLhs() == null) {
            if (showLower && (closest != null)) {
                
                List<Node> applicableBlocks = AstUtilities.getApplicableBlocks(path, false);
                for (Node block : applicableBlocks) {
                    addDynamic(block, variables);
                }
                
                Node method = AstUtilities.findLocalScope(closest, path);

                @SuppressWarnings("unchecked")
                List<Node> list2 = method.childNodes();

                for (Node child : list2) {
                    addLocals(child, variables);
                }
            }

            if ((prefix.length() == 0) || (first == '@') || showSymbols) {
                String fqn = AstUtilities.getFqnName(path);

                if ((fqn == null) || (fqn.length() == 0)) {
                    fqn = "Object"; // NOI18N
                }

                // TODO - if fqn has multiple ::'s, try various combinations? or is 
                // add inherited already doing that?
                
                Set<IndexedField> f;
                if (RubyUtils.isRhtmlFile(fileObject) || RubyUtils.isMarkabyFile(fileObject)) {
                    f = new HashSet<IndexedField>();
                    addActionViewFields(f, fileObject, index, prefix, kind);
                } else {
                    f = index.getInheritedFields(fqn, prefix, kind, false);
                }

                for (IndexedField field : f) {
                    FieldItem item = new FieldItem(field, anchor, request);

                    item.setSmart(field.isSmart());

                    if (showSymbols) {
                        item.setSymbol(true);
                    }

                    proposals.add(item);
                }
            }

            // $ is neither upper nor lower 
            if ((prefix.length() == 0) || (first == '$') || showSymbols) {
                @SuppressWarnings("unchecked")
                List<Node> list = root.childNodes();

                for (Node child : list) {
                    addGlobals(child, globals);
                }
            }
        }

        // TODO: should only include fields etc. down to caret location??? Decide. (Depends on language semantics. Can I have forward referemces?
        if (showUpper || showSymbols) {
            addConstants(root, constants);
        }
        
        // If we're in a call, add in some info and help for the code completion call
        boolean inCall = addParameters(proposals, request);

        // Code completion from the index.
        if (index != null) {
            if (showLower || showSymbols) {
                String fqn = AstUtilities.getFqnName(path);

                if ((fqn == null) || (fqn.length() == 0)) {
                    fqn = "Object"; // NOI18N
                }

                if ((fqn != null) && queryType == QueryType.COMPLETION && // doesn't apply to (or work with) documentation/tooltip help
                        completeDefOrInclude(proposals, request, fqn)) {
                    return proposals;
                }

                if ((fqn != null) &&
                        completeObjectMethod(proposals, request, fqn, call)) {
                    return proposals;
                }

                // Only call local and inherited methods if we don't have an LHS, such as Foo::
                if (call.getLhs() == null) {
                    // TODO - pull this into a completeInheritedMethod call
                    // Complete inherited methods or local methods only (plus keywords) since there
                    // is no receiver so it must be a local or inherited method call
                    Set<IndexedMethod> inheritedMethods =
                        index.getInheritedMethods(fqn, prefix, kind);

                    // Handle action view completion for RHTML and Markaby files
                    if (RubyUtils.isRhtmlFile(fileObject) || RubyUtils.isMarkabyFile(fileObject)) {
                        addActionViewMethods(inheritedMethods, fileObject, index, prefix, kind);
                    } else if (fileObject.getName().endsWith("_spec")) { // NOI18N
                        // RSpec
                        
                        /* My spec object had the following extras methods over a plain Object:
                                x = self.class.methods
                                x.each {|c|
                                      puts c
                                }
                            > args_and_options
                            > context
                            > copy_instance_variables_from
                            > describe
                            > gem
                            > metaclass
                            > require
                            > require_gem
                            > respond_to
                            > should
                            > should_not
                         */
                        String includes[] = { 
                            // "describe" should be in Kernel already, from spec/runner/extensions/kernel.rb
                            "Spec::Matchers",
                            // This one shouldn't be necessary since there's a
                            // "class Object; include xxx::ObjectExpectations; end" in rspec's object.rb
                            "Spec::Expectations::ObjectExpectations", 
                            "Spec::DSL::BehaviourEval::InstanceMethods" }; // NOI18N
                        for (String fqns : includes) {
                            Set<IndexedMethod> helper = index.getInheritedMethods(fqns, prefix, kind);
                            inheritedMethods.addAll(helper);
                        }
                    }

                    for (IndexedMethod method : inheritedMethods) {
                        // This should not be necessary - filtering happens in getInheritedMethods right?
                        if ((prefix.length() > 0) && !method.getName().startsWith(prefix)) {
                            continue;
                        }

                        if (method.isNoDoc()) {
                            continue;
                        }
                        
                        // If a method is an "initialize" method I should do something special so that
                        // it shows up as a "constructor" (in a new() statement) but not as a directly
                        // callable initialize method (it should already be culled because it's private)
                        MethodItem item = new MethodItem(method, anchor, request);

                        item.setSmart(method.isSmart());

                        if (showSymbols) {
                            item.setSymbol(true);
                        }

                        proposals.add(item);
                    }
                }
            }

            if (showUpper) {
                if (queryType == QueryType.COMPLETION && // doesn't apply to (or work with) documentation/tooltip help
                        completeDefOrInclude(proposals, request, "")) {
                    return proposals;
                }
            }
            if ((showUpper && ((prefix != null && prefix.length() > 0) ||
                    (!call.isMethodExpected() && call.getLhs() != null && call.getLhs().length() > 0)))
                    || (showSymbols && !inCall)) {
                // TODO - allow method calls if you're already entered the first char!
                completeClasses(proposals, request, showSymbols, call);
            }
        }
        assert (kind == NameKind.PREFIX) || (kind == NameKind.CASE_INSENSITIVE_PREFIX) ||
        (kind == NameKind.EXACT_NAME);

        // TODO
        // Remove fields and variables whose names are already taken, e.g. do a fields.removeAll(variables) etc.
        for (String variable : variables.keySet()) {
            if (((kind == NameKind.EXACT_NAME) && prefix.equals(variable)) ||
                    ((kind != NameKind.EXACT_NAME) && startsWith(variable, prefix))) {
                Node node = variables.get(variable);

                if (!overlapsLine(node, astLineBegin, astLineEnd)) {
                    AstVariableElement co = new AstVariableElement(node, variable);
                    PlainItem item = new PlainItem(co, anchor, request);
                    item.setSmart(true);

                    if (showSymbols) {
                        item.setSymbol(true);
                    }

                    proposals.add(item);
                }
            }
        }

        for (String field : fields.keySet()) {
            if (((kind == NameKind.EXACT_NAME) && prefix.equals(field)) ||
                    ((kind != NameKind.EXACT_NAME) && startsWith(field, prefix))) {
                Node node = fields.get(field);

                if (overlapsLine(node, astLineBegin, astLineEnd)) {
                    continue;
                }

                Element co = new AstFieldElement(node);
                FieldItem item = new FieldItem(co, anchor, request);
                item.setSmart(true);

                if (showSymbols) {
                    item.setSymbol(true);
                }

                proposals.add(item);
            }
        }

        // TODO - model globals and constants using different icons / etc.
        for (String variable : globals.keySet()) {
            // TODO - kind.EXACT_NAME
            if (startsWith(variable, prefix) ||
                    (showSymbols && startsWith(variable.substring(1), prefix))) {
                Node node = globals.get(variable);

                if (overlapsLine(node, astLineBegin, astLineEnd)) {
                    continue;
                }

                AstElement co = new AstVariableElement(node, variable);
                PlainItem item = new PlainItem(co, anchor, request);
                item.setSmart(true);

                if (showSymbols) {
                    item.setSymbol(true);
                }

                proposals.add(item);
            }
        }

        // TODO - model globals and constants using different icons / etc.
        for (String variable : constants.keySet()) {
            if (((kind == NameKind.EXACT_NAME) && prefix.equals(variable)) ||
                    ((kind != NameKind.EXACT_NAME) && startsWith(variable, prefix))) {
                // Skip constants that are known to be classes
                Node node = constants.get(variable);

                if (overlapsLine(node, astLineBegin, astLineEnd)) {
                    continue;
                }

                //                ComObject co;
                //                if (isClassName(variable)) {
                //                    co = JRubyNode.create(node, null);  
                //                    if (co == null) {
                //                        continue;
                //                    }
                //                } else {
                //                    co = new DefaultComVariable(variable, false, -1, -1);
                //                    ((DefaultComVariable)co).setNode(node);
                AstElement co = new AstVariableElement(node, variable);
                PlainItem item = new PlainItem(co, anchor, request);
                item.setSmart(true);

                if (showSymbols) {
                    item.setSymbol(true);
                }

                proposals.add(item);
            }
        }

        if (completeKeywords(proposals, request, showSymbols)) {
            return proposals;
        }

        if (queryType == QueryType.DOCUMENTATION) {
            proposals = filterDocumentation(proposals, root, doc, info, astOffset, lexOffset, prefix, path,
                    index);
        }

        return proposals;
    }
        
    private void addActionViewMethods(Set<IndexedMethod> inheritedMethods, FileObject fileObject, RubyIndex index, String prefix, 
            NameKind kind) { 
        // RHTML and Markaby: Add in the helper methods etc. from the associated files
        boolean isMarkaby = RubyUtils.isMarkabyFile(fileObject);
        if (isMarkaby) {
            Set<IndexedMethod> actionView = index.getInheritedMethods("ActionView::Base", prefix, kind); // NOI18N
            inheritedMethods.addAll(actionView);
        }

        if (RubyUtils.isRhtmlFile(fileObject) || isMarkaby) {
            // Hack - include controller and helper files as well
            FileObject f = fileObject.getParent();
            String controllerName = null;
            // XXX Will this work for .mab files? Where do they go?
            while (f != null && !f.getName().equals("views")) { // todo - make sure grandparent is app
                String n = RubyUtils.underlinedNameToCamel(f.getName());
                if (controllerName == null) {
                    controllerName = n;
                } else {
                    controllerName = n + "::" + controllerName;
                }
                f = f.getParent();
            }
            Set<IndexedMethod> helper = index.getInheritedMethods(controllerName+"Helper", prefix, kind); // NOI18N
            inheritedMethods.addAll(helper);
            // TODO - pull in the fields (NOT THE METHODS) from the controller
            //Set<IndexedMethod> controller = index.getInheritedMethods(controllerName+"Controller", prefix, kind);
            //inheritedMethods.addAll(controller);
        }
    }       

    private void addActionViewFields(Set<IndexedField> inheritedFields, FileObject fileObject, RubyIndex index, String prefix, 
            NameKind kind) { 
        // RHTML and Markaby: Add in the helper methods etc. from the associated files
        boolean isMarkaby = RubyUtils.isMarkabyFile(fileObject);
        if (isMarkaby) {
            Set<IndexedField> actionView = index.getInheritedFields("ActionView::Base", prefix, kind, true); // NOI18N
            inheritedFields.addAll(actionView);
        }

        if (RubyUtils.isRhtmlFile(fileObject) || isMarkaby) {
            // Hack - include controller and helper files as well
            FileObject f = fileObject.getParent();
            String controllerName = null;
            // XXX Will this work for .mab files? Where do they go?
            while (f != null && !f.getName().equals("views")) { // NOI18N // todo - make sure grandparent is app
                String n = RubyUtils.underlinedNameToCamel(f.getName());
                if (controllerName == null) {
                    controllerName = n;
                } else {
                    controllerName = n + "::" + controllerName; // NOI18N
                }
                f = f.getParent();
            }

            String fqn = controllerName+"Controller"; // NOI18N
            Set<IndexedField> controllerFields = index.getInheritedFields(fqn, prefix, kind, true);
            for (IndexedField field : controllerFields) {
                if ("ActionController::Base".equals(field.getIn())) { // NOI18N
                    continue;
                }
                inheritedFields.add(field);
            }
        }
    }       
    
    /** If we're doing documentation completion, try to drop the list down to a single alternative
     * (since the framework will just use the first produced result), and in particular, the -best-
     * alternative
     */
    private List<CompletionProposal> filterDocumentation(List<CompletionProposal> proposals,
        Node root, BaseDocument doc, CompilationInfo info, int astOffset, int lexOffset, String name,
        AstPath path, RubyIndex index) {
        // Look to see if this symbol is either a "class Foo" or a "def foo", and if we invoke
        // completion on it, prefer this element provided it has documentation
        List<CompletionProposal> candidates = new ArrayList<CompletionProposal>();
        FileObject fo = info.getFileObject();
        Map<IndexedElement, CompletionProposal> elementMap =
            new HashMap<IndexedElement, CompletionProposal>();
        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();
        Set<IndexedClass> classes = new HashSet<IndexedClass>();

        for (CompletionProposal proposal : proposals) {
            Element e = proposal.getElement();

            if (e instanceof IndexedElement) {
                IndexedElement ie = (IndexedElement)e;

                if (ie instanceof IndexedClass) {
                    classes.add((IndexedClass)ie);
                    elementMap.put(ie, proposal);
                } else if (ie instanceof IndexedMethod) {
                    methods.add((IndexedMethod)ie);
                    elementMap.put(ie, proposal);
                }

                if (ie.getFileObject() == fo) {
                    // The class is in this file - if it has documentation, prefer it
                    candidates.add(proposal);
                }
            }
        }

        // Check the candidates to see if one of them is actually -defined-
        // under the caret; e.g. if you have "class File" with documentation,
        // and you ctrl-space on it, you always want to show THIS documentation
        // for File, not the standard one defined elsewhere.
        for (CompletionProposal candidate : candidates) {
            // See if the candidate corresponds to the caret position
            IndexedElement e = (IndexedElement)candidate.getElement();
            String signature = e.getSignature();
            Node node = AstUtilities.findBySignature(root, signature);

            if (node != null) {
                ISourcePosition pos = node.getPosition();
                int startPos = LexUtilities.getLexerOffset(info, pos.getStartOffset());

                try {
                    int lineBegin = AstUtilities.getAstOffset(info, Utilities.getRowFirstNonWhite(doc, startPos));
                    int lineEnd = AstUtilities.getAstOffset(info, Utilities.getRowEnd(doc, startPos));

                    if ((astOffset >= lineBegin) && (astOffset <= lineEnd)) {
                        // Look for documentation
                        List<String> rdoc = AstUtilities.gatherDocumentation(info, doc, node);

                        if ((rdoc != null) && (rdoc.size() > 0)) {
                            return Collections.singletonList(candidate);
                        }
                    }
                } catch (BadLocationException ble) {
                    // The parse information is too old - the document has shrunk. Do nothing, the
                    // AST nodes are pointing into the old contents.
                }
            }
        }

        // Try to pick the best match among many documentation entries: Heuristic time.
        // Similar to heuristics used for Go To Declaration: Prefer long documentation,
        // prefer documentation related to the require-statements in this file, etc.
        DeclarationFinder finder = new DeclarationFinder();
        IndexedElement candidate = null;

        if (classes.size() > 0) {
            candidate = finder.findBestClassMatch(classes, doc, path, path.leaf(), index);
        } else if (methods.size() > 0) {
            candidate = finder.findBestMethodMatch(name, methods, doc, astOffset, lexOffset, path,
                    path.leaf(), index);
        }

        if (candidate != null) {
            CompletionProposal proposal = elementMap.get(candidate);

            if (proposal != null) {
                return Collections.singletonList(proposal);
            }
        }

        return proposals;
    }

    //    private boolean isClassName(String s) {
    //        // Initial capital letter, second letter is not
    //        if (s.length() == 1) {
    //            return Character.isUpperCase(s.charAt(0));
    //        }
    //        
    //        if (Character.isLowerCase(s.charAt(0))) {
    //            return false;
    //        }
    //        
    //        return Character.isLowerCase(s.charAt(1));
    //    }
    private boolean overlapsLine(Node node, int lineBegin, int lineEnd) {
        ISourcePosition pos = node.getPosition();

        //return (((pos.getStartOffset() <= lineEnd) && (pos.getEndOffset() >= lineBegin)));
        // Don't look to see if the line is within the node. See if the node is started on this line (where
        // the declaration is, e.g. it might be an incomplete line.
        return ((pos.getStartOffset() >= lineBegin) && (pos.getStartOffset() <= lineEnd));
    }

    //    /** Return true iff the name looks like an operator name */
    //    private boolean isOperator(String name) {
    //        // If a name contains not a single letter, it is probably an operator - especially
    //        // if it is a short name
    //        int n = name.length();
    //
    //        if (n > 2) {
    //            return false;
    //        }
    //
    //        for (int i = 0; i < n; i++) {
    //            if (Character.isLetter(name.charAt(i))) {
    //                return false;
    //            }
    //        }
    //
    //        return true;
    //    }

    @SuppressWarnings("unchecked")
    private void addLocals(Node node, Map<String, Node> variables) {
        switch (node.nodeId) {
        case NodeTypes.LOCALASGNNODE: {
            String name = ((INameNode)node).getName();

            if (!variables.containsKey(name)) {
                variables.put(name, node);
            }
            break;
        }
        case NodeTypes.ARGSNODE: {
            // TODO - use AstUtilities.getDefArgs here - but avoid hitting them twice!
            //List<String> parameters = AstUtilities.getDefArgs(def, true);
            // However, I've gotta find the parameter nodes themselves too!
            ArgsNode an = (ArgsNode)node;

            if (an.getArgsCount() > 0) {
                List<Node> args = (List<Node>)an.childNodes();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        List<Node> args2 = (List<Node>)arg.childNodes();

                        for (Node arg2 : args2) {
                            if (arg2 instanceof ArgumentNode) {
                                variables.put(((ArgumentNode)arg2).getName(), arg2);
                            } else if (arg2 instanceof LocalAsgnNode) {
                                variables.put(((INameNode)arg2).getName(), arg2);
                            }
                        }
                    }
                }
            }

            // Rest args
            if (an.getRestArgNode() != null) {
                String name = an.getRestArgNode().getName();
                variables.put(name, an.getRestArgNode());
            }

            // Block args
            if (an.getBlockArgNode() != null) {
                String name = an.getBlockArgNode().getName();
                variables.put(name, an.getBlockArgNode());
            }
            
            break;
        }

        //        } else if (node instanceof AliasNode) {
        //            AliasNode an = (AliasNode)node;
        // Tricky -- which NODE do we add here? Completion creator needs to be aware of new name etc. Do later.
        // Besides, do we show it as a field or a method or what?

        //            variab
        //            if (an.getNewName().equals(name)) {
        //                OffsetRange range = AstUtilities.getAliasNewRange(an);
        //                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
        //            } else if (an.getOldName().equals(name)) {
        //                OffsetRange range = AstUtilities.getAliasOldRange(an);
        //                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
        //            }
        //          break;
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            switch (child.nodeId) {
            case NodeTypes.DEFNNODE:
            case NodeTypes.DEFSNODE:
            case NodeTypes.CLASSNODE:
            case NodeTypes.SCLASSNODE:
            case NodeTypes.MODULENODE:
                // Don't look in nested context for local vars
                continue;
            }

            addLocals(child, variables);
        }
    }

    private void addDynamic(Node node, Map<String, Node> variables) {
        if (node.nodeId == NodeTypes.DASGNNODE) {
            String name = ((INameNode)node).getName();

            if (!variables.containsKey(name)) {
                variables.put(name, node);
            }

            //} else if (node instanceof ArgsNode) {
            //    ArgsNode an = (ArgsNode)node;
            //
            //    if (an.getArgsCount() > 0) {
            //        List<Node> args = (List<Node>)an.childNodes();
            //        List<String> parameters = null;
            //
            //        for (Node arg : args) {
            //            if (arg instanceof ListNode) {
            //                List<Node> args2 = (List<Node>)arg.childNodes();
            //                parameters = new ArrayList<String>(args2.size());
            //
            //                for (Node arg2 : args2) {
            //                    if (arg2 instanceof ArgumentNode) {
            //                        OffsetRange range = AstUtilities.getRange(arg2);
            //                        highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            //                    } else if (arg2 instanceof LocalAsgnNode) {
            //                        OffsetRange range = AstUtilities.getRange(arg2);
            //                        highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            //                    }
            //                }
            //            }
            //        }
            //    }
            //        } else if (!ignoreAlias && node instanceof AliasNode) {
            //            AliasNode an = (AliasNode)node;
            //
            //            if (an.getNewName().equals(name)) {
            //                OffsetRange range = AstUtilities.getAliasNewRange(an);
            //                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            //            } else if (an.getOldName().equals(name)) {
            //                OffsetRange range = AstUtilities.getAliasOldRange(an);
            //                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            //            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            switch (child.nodeId) {
            case NodeTypes.ITERNODE:
            //case NodeTypes.BLOCKNODE:
            case NodeTypes.DEFNNODE:
            case NodeTypes.DEFSNODE:
            case NodeTypes.CLASSNODE:
            case NodeTypes.SCLASSNODE:
            case NodeTypes.MODULENODE:
                continue;
            }

            addDynamic(child, variables);
        }
    }

    private void addGlobals(Node node, Map<String, Node> globals) {
        if (node.nodeId == NodeTypes.GLOBALASGNNODE) {
            String name = ((INameNode)node).getName();

            if (!globals.containsKey(name)) {
                globals.put(name, node);
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            addGlobals(child, globals);
        }
    }

    private void addConstants(Node node, Map<String, Node> constants) {
        if (node.nodeId == NodeTypes.CONSTDECLNODE) {
            constants.put(((INameNode)node).getName(), node);
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            addConstants(child, constants);
        }
    }

    private String loadResource(String basename) {
        // TODO: I18N
        InputStream is = null;
        StringBuilder sb = new StringBuilder();

        try {
            is = new BufferedInputStream(CodeCompleter.class.getResourceAsStream("resources/" +
                    basename));
            //while (is)
            while (true) {
                int c = is.read();

                if (c == -1) {
                    break;
                }

                sb.append((char)c);
            }

            if (sb.length() > 0) {
                return sb.toString();
            }
        } catch (IOException ie) {
            Exceptions.printStackTrace(ie);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ie) {
                Exceptions.printStackTrace(ie);
            }
        }

        return null;
    }

    private String getKeywordHelp(String keyword) {
        // Difficulty here with context; "else" is used for both the ifelse.html and case.html both define it.
        // End is even more used.
        if (keyword.equals("if") || keyword.equals("elsif") || keyword.equals("else") ||
                keyword.equals("then") || keyword.equals("unless")) { // NOI18N

            return loadResource("ifelse.html"); // NOI18N
        } else if (keyword.equals("case") || keyword.equals("when") || keyword.equals("else")) { // NOI18N

            return loadResource("case.html"); // NOI18N
        } else if (keyword.equals("rescue") || keyword.equals("ensure")) { // NOI18N

            return loadResource("rescue.html"); // NOI18N
        } else if (keyword.equals("yield")) { // NOI18N

            return loadResource("yield.html"); // NOI18N
        }

        return null;
    }

    /**
     * Find the best possible documentation match for the given IndexedClass or IndexedMethod.
     * This involves looking at index to see which instances of this class or method
     * definition have associated rdoc, as well as choosing between them based on the
     * require statements in the file.
     */
    private IndexedElement findDocumentationEntry(Node root, IndexedElement obj) {
        // 1. Find entries known to have documentation
        String fqn = obj.getSignature();
        Set<?extends IndexedElement> result = obj.getIndex().getDocumented(fqn);

        if ((result == null) || (result.size() == 0)) {
            return null;
        } else if (result.size() == 1) {
            return result.iterator().next();
        }

        // 2. There are multiple matches so try to disambiguate them by the imports in this file.
        // For example, for "File" we usually show the standard (builtin) documentation,
        // unless you have required "ftools", which redefines File with new docs.
        Set<IndexedElement> candidates;
        if (root != null) {
            candidates = new HashSet<IndexedElement>();
            Set<String> requires = AstUtilities.getRequires(root);

            for (IndexedElement o : result) {
                String require = o.getRequire();

                if (requires.contains(require)) {
                    candidates.add(o);
                }
            }

            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (!candidates.isEmpty()) {
                result = candidates;
            }
        }

        // 3. Prefer builtin (kernel) docs over other docs.
        candidates = new HashSet<IndexedElement>();

        for (IndexedElement o : result) {
            String url = o.getFileUrl();

            if (url.indexOf("rubystubs") != -1) {
                candidates.add(o);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            result = candidates;
        }

        // 4. Consider other heuristics, like picking the "larger" documentation
        // (more lines)

        // 5. Just pick an arbitrary one.
        return result.iterator().next();
    }
    
    /**
     * @todo If you invoke this on top of a symbol, I should really just show
     *   the documentation for that symbol!
     * 
     * @param element The element we want to look up comments for
     * @param info The (optional) compilation info for a document referencing the element.
     *   This is used to consult require-statements in the given compilation context etc.
     *   to choose among many alternatives. May be null, in which case the element had
     *   better be an IndexedElement.
     */
    private List<String> getComments(CompilationInfo info, Element element) {
        assert info != null || element instanceof IndexedElement;
        
        if (element == null) {
            return null;
        }

        Node node = null;

        if (element instanceof AstElement) {
            node = ((AstElement)element).getNode();
        } else if (element instanceof IndexedElement) {
            IndexedElement com = (IndexedElement)element;
            Node root = null;
            if (info != null) {
                root = AstUtilities.getRoot(info);
            }

            IndexedElement match = findDocumentationEntry(root, com);

            if (match != null) {
                com = match;
                element = com;
            }

            node = AstUtilities.getForeignNode(com, null);

            if (node == null) {
                return null;
            }
        } else {
            assert false : element;

            return null;
        }

        // Initially, I implemented this by using RubyParserResult.getCommentNodes.
        // However, I -still- had to rely on looking in the Document itself, since
        // the CommentNodes are not attached to the AST, and to do things the way
        // RDoc does, I have to (for example) look to see if a comment is at the
        // beginning of a line or on the same line as something else, or if two
        // comments have any empty lines between them, and so on.
        // When I started looking in the document itself, I realized I might as well
        // do all the manipulation on the document, since having the Comment nodes
        // don't particularly help.
        Document doc = null;
        BaseDocument baseDoc = null;

        try {
            if (element instanceof IndexedElement) {
                doc = ((IndexedElement)element).getDocument();
                info = null;
            } else if (info != null) {
                doc = info.getDocument();
            }

            if (doc instanceof BaseDocument) {
                baseDoc = (BaseDocument)doc;
            } else {
                return null;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);

            return null;
        }

        List<String> comments = null;

        // Check for RubyComObject: These are external files (like Ruby lib) where I need to check many files
        if (node instanceof ClassNode && !(element instanceof IndexedElement)) {
            String className = AstUtilities.getClassOrModuleName((ClassNode)node);
            List<ClassNode> classes = AstUtilities.getClasses(AstUtilities.getRoot(info));

            // Iterate backwards through the list because the most recent documentation
            // should be chosen, if any
            for (int i = classes.size() - 1; i >= 0; i--) {
                ClassNode clz = classes.get(i);
                String name = AstUtilities.getClassOrModuleName(clz);

                if (name.equals(className)) {
                    comments = AstUtilities.gatherDocumentation(info, baseDoc, clz);

                    if ((comments != null) && (comments.size() > 0)) {
                        break;
                    }
                }
            }
        } else {
            comments = AstUtilities.gatherDocumentation(info, baseDoc, node);
        }

        if ((comments == null) || (comments.size() == 0)) {
            return null;
        }
        
        return comments;
    }
    
    public String document(CompilationInfo info, Element element) {
        if (element instanceof KeywordElement) {
            return getKeywordHelp(((KeywordElement)element).getName());
        } else if (element instanceof CommentElement) {
            // Text is packaged as the name
            String comment = element.getName();
            RDocFormatter formatter = new RDocFormatter();
            String[] comments = comment.split("\n");
            for (String text : comments) {
                // Truncate off leading whitespace before # on comment lines
                for (int i = 0, n = text.length(); i < n; i++) {
                    char c = text.charAt(i);
                    if (c == '#') {
                        if (i > 0) {
                            text = text.substring(i);
                            break;
                        }
                    } else if (!Character.isWhitespace(c)) {
                        break;
                    }
                }
                formatter.appendLine(text);
            }
            String html = formatter.toHtml();
            return html;
        } else if (element instanceof ElementHandle.UrlHandle) {
            String url = ((ElementHandle.UrlHandle)element).getUrl();
            DeclarationLocation loc = new DeclarationFinder().findLinkedMethod(info, url);
            if (loc != DeclarationLocation.NONE) {
                element = loc.getElement();
                if (element == null) {
                    return null;
                }
            } else {
                return null;
            }
        }
        
        List<String> comments = getComments(info, element);
        if (comments == null) {
            if (element.getName().startsWith("find_by_") ||
                element.getName().startsWith("find_all_by_")) {
                return new RDocFormatter().getSignature(element) + NbBundle.getMessage(CodeCompleter.class, "DynamicMethod");
            }
            String html = new RDocFormatter().getSignature(element) + "\n<hr>\n<i>" + NbBundle.getMessage(CodeCompleter.class, "NoCommentFound") +"</i>";

            return html;
        }
        
        RDocFormatter formatter = new RDocFormatter();
        String name = element.getName();
        if (name != null && name.length() > 0) {
            formatter.setSeqName(name);
        }

        for (String text : comments) {
            formatter.appendLine(text);
        }

        String html = formatter.toHtml();
        if (!formatter.wroteSignature()) {
            html = formatter.getSignature(element) + "\n<hr>\n" + html;
        }
        
        return html;
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin,
        int selectionEnd) {

        // TODO - check the code at the AST path and determine whether it makes sense to
        // wrap it in a begin block etc.
        // TODO - I'd like to be able to pass any selection-based templates I'm not familiar with
        
        boolean valid = false;

        if (selectionEnd != -1) {
            try {
                if (selectionBegin == selectionEnd) {
                    return Collections.emptySet();
                } else if (selectionEnd < selectionBegin) {
                    int temp = selectionBegin;
                    selectionBegin = selectionEnd;
                    selectionEnd = temp;
                }
                BaseDocument doc = (BaseDocument) info.getDocument();

                boolean startLineIsEmpty = Utilities.isRowEmpty(doc, selectionBegin);
                boolean endLineIsEmpty = Utilities.isRowEmpty(doc, selectionEnd);

                if ((startLineIsEmpty || selectionBegin <= Utilities.getRowFirstNonWhite(doc, selectionBegin)) &&
                        (endLineIsEmpty || selectionEnd > Utilities.getRowLastNonWhite(doc, selectionEnd))) {
                    // I have no text to the left of the beginning or text to the right of the end, but I might
                    // have just selected whitespace - check that
                    String text = doc.getText(selectionBegin, selectionEnd-selectionBegin);
                    for (int i = 0; i < text.length(); i++) {
                        if (!Character.isWhitespace(text.charAt(i))) {

                            // Make sure that we're not in a string etc
                            Token<? extends TokenId> token = LexUtilities.getToken(doc, selectionBegin);
                            if (token != null) {
                                TokenId id = token.id();
                                if (id != RubyTokenId.STRING_LITERAL && id != RubyTokenId.LINE_COMMENT &&
                                        id != RubyTokenId.QUOTED_STRING_LITERAL && id != RubyTokenId.REGEXP_LITERAL &&
                                        id != RubyTokenId.DOCUMENTATION) {
                                    // Yes - allow surround with here

                                    // TODO - make this smarter by looking at the AST and see if
                                    // we have a complete set of nodes
                                    valid = true;
                                }
                            }

                            break;
                        }
                    }
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            valid = true;
        }
        
        if (valid) {
            return selectionTemplates;
        } else {
            return Collections.emptySet();
        }
    }

    private String suggestName(CompilationInfo info, int caretOffset, String prefix, Map params) {
        // Look at the given context, compute fields and see if I can find a free name
        caretOffset = AstUtilities.boundCaretOffset(info, caretOffset);

        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return null;
        }

        AstPath path = new AstPath(root, caretOffset);
        Node closest = path.leaf();

        if (prefix.startsWith("$")) {
            // Look for a unique global variable -- this requires looking at the index
            // XXX TODO
            return null;
        } else if (prefix.startsWith("@@")) {
            // Look for a unique class variable -- this requires looking at superclasses and other class parts
            // XXX TODO
            return null;
        } else if (prefix.startsWith("@")) {
            // Look for a unique instance variable -- this requires looking at superclasses and other class parts
            // XXX TODO
            return null;
        } else {
            // Look for a local variable in the given scope
            if (closest != null) {
                Node method = AstUtilities.findLocalScope(closest, path);
                Map<String, Node> variables = new HashMap<String, Node>();
                addLocals(method, variables);

                List<Node> applicableBlocks = AstUtilities.getApplicableBlocks(path, false);
                for (Node block : applicableBlocks) {
                    addDynamic(block, variables);
                }
                
                // See if we have any name suggestions
                String suggestions = (String)params.get(ATTR_DEFAULTS);

                // Check the suggestions
                if ((suggestions != null) && (suggestions.length() > 0)) {
                    String[] names = suggestions.split(",");

                    for (String suggestion : names) {
                        if (!variables.containsKey(suggestion)) {
                            return suggestion;
                        }
                    }

                    // Try some variations of the name
                    for (String suggestion : names) {
                        for (int number = 2; number < 5; number++) {
                            String name = suggestion + number;

                            if ((name.length() > 0) && !variables.containsKey(name)) {
                                return name;
                            }
                        }
                    }
                }

                // Try the prefix
                if ((prefix.length() > 0) && !variables.containsKey(prefix)) {
                    return prefix;
                }

                // TODO: What's the right algorithm for uniqueifying a variable
                // name in Ruby?
                // For now, will just append a number
                if (prefix.length() == 0) {
                    prefix = "var";
                }

                for (int number = 1; number < 15; number++) {
                    String name = (number == 1) ? prefix : (prefix + number);

                    if ((name.length() > 0) && !variables.containsKey(name)) {
                        return name;
                    }
                }
            }

            return null;
        }
    }

    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset,
        String name, Map params) {
        if (variable.equals(KEY_PIPE)) {
            return "||";
        }

        // Old-style format - support temporarily
        if (variable.equals(ATTR_UNUSEDLOCAL)) { // TODO REMOVEME
            return suggestName(info, caretOffset, name, params);            
        }

        if (params != null && params.containsKey(ATTR_UNUSEDLOCAL)) {
            return suggestName(info, caretOffset, name, params);
        }

        if ((!(variable.equals(KEY_METHOD) || variable.equals(KEY_METHOD_FQN) ||
                variable.equals(KEY_CLASS) || variable.equals(KEY_CLASS_FQN) ||
                variable.equals(KEY_SUPERCLASS) || variable.equals(KEY_PATH) ||
                variable.equals(KEY_FILE)))) {
            return null;
        }

        caretOffset = AstUtilities.boundCaretOffset(info, caretOffset);

        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return null;
        }

        AstPath path = new AstPath(root, caretOffset);

        if (variable.equals(KEY_METHOD)) {
            Node node = AstUtilities.findMethod(path);

            if (node != null) {
                return AstUtilities.getDefName(node);
            }
        } else if (variable.equals(KEY_METHOD_FQN)) {
            MethodDefNode node = AstUtilities.findMethod(path);

            if (node != null) {
                String ctx = AstUtilities.getFqnName(path);
                String methodName = AstUtilities.getDefName(node);

                if ((ctx != null) && (ctx.length() > 0)) {
                    return ctx + "#" + methodName;
                } else {
                    return methodName;
                }
            }
        } else if (variable.equals(KEY_CLASS)) {
            ClassNode node = AstUtilities.findClass(path);

            if (node != null) {
                return node.getCPath().getName();
            }
        } else if (variable.equals(KEY_SUPERCLASS)) {
            ClassNode node = AstUtilities.findClass(path);

            if (node != null) {
                if (info.getIndex() != null) {
                    RubyIndex index = RubyIndex.get(info.getIndex());
                    IndexedClass cls = index.getSuperclass(AstUtilities.getFqnName(path));

                    if (cls != null) {
                        return cls.getFqn();
                    }
                }

                String superCls = AstUtilities.getSuperclass(node);

                if (superCls != null) {
                    return superCls;
                } else {
                    return "Object";
                }
            }
        } else if (variable.equals(KEY_CLASS_FQN)) {
            return AstUtilities.getFqnName(path);
        } else if (variable.equals(KEY_FILE)) {
            return FileUtil.toFile(info.getFileObject()).getName();
        } else if (variable.equals(KEY_PATH)) {
            return FileUtil.toFile(info.getFileObject()).getPath();
        }

        return null;
    }

    public ParameterInfo parameters(CompilationInfo info, int lexOffset, CompletionProposal proposal) {
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int astOffset = AstUtilities.getAstOffset(info, lexOffset);
        if (!computeMethodCall(info, lexOffset, astOffset,
                methodHolder, paramIndexHolder, anchorOffsetHolder, null)) {

            return ParameterInfo.NONE;
        }

        IndexedMethod method = methodHolder[0];
        if (method == null) {
            return ParameterInfo.NONE;
        }
        int index = paramIndexHolder[0];
        int anchorOffset = anchorOffsetHolder[0];


        // TODO: Make sure the caret offset is inside the arguments portion
        // (parameter hints shouldn't work on the method call name itself
        
                // See if we can find the method corresponding to this call
        //        if (proposal != null) {
        //            Element element = proposal.getElement();
        //            if (element instanceof IndexedMethod) {
        //                method = ((IndexedMethod)element);
        //            }
        //        }

        List<String> params = method.getParameters();

        if ((params != null) && (params.size() > 0)) {
            return new ParameterInfo(params, index, anchorOffset);
        }

        return ParameterInfo.NONE;
    }
    
    private static class CompletionRequest {
        private TokenHierarchy<Document> th;
        private CompilationInfo info;
        private AstPath path;
        private Node node;
        private int lexOffset;
        private int astOffset;
        private BaseDocument doc;
        private String prefix;
        private RubyIndex index;
        private NameKind kind;
        private QueryType queryType;
        private FileObject fileObject;
        private HtmlFormatter formatter;
    }

    private abstract class RubyCompletionItem implements CompletionProposal {
        protected CompletionRequest request;
        protected Element element;
        protected int anchorOffset;
        protected boolean symbol;
        protected boolean smart;

        private RubyCompletionItem(Element element, int anchorOffset, CompletionRequest request) {
            this.element = element;
            this.anchorOffset = anchorOffset;
            this.request = request;
        }

        public int getAnchorOffset() {
            return anchorOffset;
        }

        public String getName() {
            return element.getName();
        }

        public void setSymbol(boolean symbol) {
            this.symbol = symbol;
        }

        public String getInsertPrefix() {
            if (symbol) {
                return ":" + getName();
            } else {
                return getName();
            }
        }

        public String getSortText() {
            return getName();
        }

        public Element getElement() {
            return element;
        }

        public ElementKind getKind() {
            return element.getKind();
        }

        public ImageIcon getIcon() {
            return null;
        }

        public String getLhsHtml() {
            ElementKind kind = getKind();
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);

            return formatter.getText();
        }

        public String getRhsHtml() {
            return null;
        }

        public Set<Modifier> getModifiers() {
            return element.getModifiers();
        }

        @Override
        public String toString() {
            String cls = getClass().getName();
            cls = cls.substring(cls.lastIndexOf('.') + 1);

            return cls + "(" + getKind() + "): " + getName();
        }

        void setSmart(boolean smart) {
            this.smart = smart;
        }

        public boolean isSmart() {
            return smart;
        }

        public List<String> getInsertParams() {
            return null;
        }
        
        public String[] getParamListDelimiters() {
            return new String[] { "(", ")" }; // NOI18N
        }

        public String getCustomInsertTemplate() {
            return null;
        }
    }

    private class MethodItem extends RubyCompletionItem {
        private IndexedMethod method;
        MethodItem(IndexedMethod element, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
            this.method = element;
        }

        @Override
        public String getLhsHtml() {
            ElementKind kind = getKind();
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            boolean emphasize = !method.isInherited();
            if (emphasize) {
                formatter.emphasis(true);
            }
            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);
            if (emphasize) {
                formatter.emphasis(false);
            }

            Collection<String> parameters = method.getParameters();

            if ((parameters != null) && (parameters.size() > 0)) {
                formatter.appendHtml("("); // NOI18N

                Iterator<String> it = parameters.iterator();

                while (it.hasNext()) { // && tIt.hasNext()) {
                    formatter.parameters(true);
                    formatter.appendText(it.next());
                    formatter.parameters(false);

                    if (it.hasNext()) {
                        formatter.appendText(", "); // NOI18N
                    }
                }

                formatter.appendHtml(")"); // NOI18N
            }
            
            if (method.hasBlock() && !method.isBlockOptional()) {
                formatter.appendText(" { }");
            }

            return formatter.getText();
        }

        @Override
        public String getRhsHtml() {
            HtmlFormatter formatter = request.formatter;
            formatter.reset();

            // Top level methods (defined on Object) : print
            // the defining file instead
            if (method.isTopLevel() && method.getRequire() != null) {
                formatter.appendText(method.getRequire());

                return formatter.getText();
            }

            String in = method.getIn();

            if (in != null) {
                formatter.appendText(in);
                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public List<String> getInsertParams() {
            return method.getParameters();
        }

        @Override
        public String getCustomInsertTemplate() {
            final String insertPrefix = getInsertPrefix();
            List<String> params = getInsertParams();
            
            String startDelimiter;
            String endDelimiter;
            boolean hasBlock = false;
            int paramCount = params.size();
            int printArgs = paramCount;

            boolean hasHashArgs = method.getEncodedAttributes() != null &&
                    method.getEncodedAttributes().indexOf("=>") != -1; // NOI18N

            if (paramCount > 0 && params.get(paramCount-1).startsWith("&")) { // NOI18N
                hasBlock = true;
                printArgs--;

                // Force parentheses around the call when using { } blocks
                // to avoid presedence problems
                startDelimiter = "("; // NOI18N
                endDelimiter = ")"; // NOI18N
            } else if (method.hasBlock()) {
                hasBlock = true;
                if (paramCount > 0) {
                    // Force parentheses around the call when using { } blocks
                    // to avoid presedence problems
                    startDelimiter = "("; // NOI18N
                    endDelimiter = ")"; // NOI18N
                } else {
                    startDelimiter = "";
                    endDelimiter = "";
                }
            } else {
                String[] delimiters = getParamListDelimiters();
                assert delimiters.length == 2;
                startDelimiter = delimiters[0];
                endDelimiter = delimiters[1];
                
                // When there are no args, don't use parentheses - and no spaces
                // Don't add two blank spaces for the case where there are no args
                if (printArgs == 0 /*&& startDelimiter.length() > 0 && startDelimiter.charAt(0) == ' '*/) {
                    startDelimiter = "";
                    endDelimiter = "";
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(insertPrefix);

            if (hasHashArgs) {
                // Uhm, no don't do this until we get to the first arg that takes a hash
                // For methods with hashes, rely on code completion to insert args
                sb.append(" ");
                return sb.toString();
            }

            sb.append(startDelimiter);
            
            int id = 1;
            for (int i = 0; i < printArgs; i++) {
                String paramDesc = params.get(i);
                sb.append("${"); //NOI18N
                // Ensure that we don't use one of the "known" logical parameters
                // such that a parameter like "path" gets replaced with the source file
                // path!
                sb.append("ruby-cc-"); // NOI18N
                sb.append(Integer.toString(id++));
                sb.append(" default=\""); // NOI18N
                sb.append(paramDesc);
                sb.append("\""); // NOI18N
                sb.append("}"); //NOI18N
                if (i < printArgs-1) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(endDelimiter);
            
            if (hasBlock) {
                String[] blockArgs = null;
                String attrs = method.getEncodedAttributes();
                int yieldNameBegin = attrs.indexOf(';');
                if (yieldNameBegin != -1) {
                    int yieldNameEnd = attrs.indexOf(';', yieldNameBegin+1);
                    if (yieldNameEnd != -1) {
                        blockArgs = attrs.substring(yieldNameBegin+1,
                                yieldNameEnd).split(",");
                    }
                }
                // TODO - if it's not an indexed class, pull this from the
                // method comments instead!
                
                sb.append(" { |"); // NOI18N
                if (blockArgs != null && blockArgs.length > 0) {
                    for (int i = 0; i < blockArgs.length; i++) {
                        if (i > 0) {
                            sb.append(","); // NOI18N
                        }
                        String arg = blockArgs[i];
                        sb.append("${unusedlocal defaults=\""); // NOI18N
                        sb.append(arg);
                        sb.append("\"}"); // NOI18N
                    }
                } else {
                    sb.append("${unusedlocal defaults=\"i,e\"}"); // NOI18N
                }
                sb.append("| ${"); // NOI18N
                sb.append("ruby-cc-"); // NOI18N
                sb.append(Integer.toString(id++));
                sb.append(" default=\"\"} }${cursor}"); // NOI18N
                
            } else {
                sb.append("${cursor}"); // NOI18N
            }
            
            // Facility method parameter completion on this item
            try {
                callLineStart = Utilities.getRowStart(request.doc, anchorOffset);
                callMethod = method;
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
            
            return sb.toString();
        }
        
        @Override
        public String[] getParamListDelimiters() {
            // TODO - convert methods with NO parameters that take a block to insert { <here> }
            String n = getName();
            String in = element.getIn();
            if ("Module".equals(in)) {
                // Module.attr_ methods typically shouldn't use parentheses
                if (n.startsWith("attr_"))  {
                    return new String[] { " :", " " };
                } else if (n.equals("include") || n.equals("import")) { // NOI18N
                    return new String[] { " ", " " };
                } else if (n.equals("include_package")) { // NOI18N
                    return new String[] { " '", "'" }; // NOI18N
                }
            } else if ("Kernel".equals(in)) {
                // Module.require: insert quotes!
                if (n.equals("require")) { // NOI18N
                    return new String[] { " '", "'" }; // NOI18N
                } else if (n.equals("p")) {
                    return new String[] { " ", " " }; // NOI18N
                }
            } else if ("Object".equals(in)) {
                if (n.equals("include_class")) { // NOI18N
                    return new String[] { " '", "'" }; // NOI18N
                }
            }
            
            if (forceCompletionSpaces()) {
                // Can't have "" as the second arg because a bug causes pressing
                // return to complete editing the last field (at he end of a buffer)
                // such that the caret ends up BEFORE the last char instead of at the
                // end of it
                boolean ambiguous = false;
                
                AstPath path = request.path;
                if (path != null) {
                    Iterator<Node> it = path.leafToRoot();

                    while (it.hasNext()) {
                        Node node = it.next();

                        if (AstUtilities.isCall(node)) {
                            // We're in a call; see if it has parens
                            // TODO - no problem with ambiguity if it's on a separate line, correct?
                            
                            // Is this the method we're trying to complete?
                            if (node != request.node) {
                                // See if the outer call has parentheses!
                                ambiguous = true;
                                break;
                            }
                        }
                    }
                }
                
                if (ambiguous) {
                    return new String[] { "(", ")" }; // NOI18N
                } else {
                    return new String[] { " ", " " }; // NOI18N
                }
            }

            if (element instanceof IndexedElement) {
                List<String> comments = getComments(null, element);
                if (comments != null && comments.size() > 0) {
                    // Look through the comment, attempting to identify
                    // a usage of the current method and determine whether it
                    // is using parentheses or not.
                    // We only look for comments that look like code; e.g. they
                    // are indented according to rdoc conventions.
                    String name = getName();
                    boolean spaces = false;
                    boolean parens = false;
                    for (String line : comments) {
                        if (line.startsWith("#  ")) { // NOI18N
                            // Look for usages - there could be many
                            int i = 0;
                            int length = line.length();
                            while (true) {
                                int index = line.indexOf(name, i);
                                if (index == -1) {
                                    break;
                                }
                                index += name.length();
                                i = index;
                                if (index < length) {
                                    char c = line.charAt(index);
                                    if (c == ' ') {
                                        spaces = true;
                                    } else if (c == '(') {
                                        parens = true;
                                    }
                                }
                            }
                        }
                    }
                    
                    // Only use spaces if no parens were seen and we saw spaces
                    if (!parens && spaces) {
                        //return new String[] { " ", "" }; // NOI18N
                        // HACK because live code template editing doesn't seem to work - it places the caret at theront of the word when the last param is in the text!                        
                        return new String[] { " ", " " }; // NOI18N
                    }
                }
                
                // Take a look at the method definition itself and look for parens there
                
            }

            // Default - (,)
            return super.getParamListDelimiters();
        }

        @Override
        public ElementKind getKind() {
            if (method.getMethodType() == IndexedMethod.MethodType.ATTRIBUTE) {
                return ElementKind.ATTRIBUTE;
            }

            return element.getKind();
        }
    }

    private class KeywordItem extends RubyCompletionItem {
        private static final String RUBY_KEYWORD = "org/netbeans/modules/ruby/jruby.png"; //NOI18N
        private final String keyword;
        private final String description;

        KeywordItem(String keyword, String description, int anchorOffset, CompletionRequest request) {
            super(null, anchorOffset, request);
            this.keyword = keyword;
            this.description = description;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml() {
            if (description != null) {
                HtmlFormatter formatter = request.formatter;
                formatter.reset();
                formatter.appendText(description);

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = new ImageIcon(org.openide.util.Utilities.loadImage(RUBY_KEYWORD));
            }

            return keywordIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
        
        @Override
        public Element getElement() {
            // For completion documentation
            return new KeywordElement(keyword);
        }
    }

    private class ClassItem extends RubyCompletionItem {
        ClassItem(Element element, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
        }

        @Override
        public String getRhsHtml() {
            HtmlFormatter formatter = request.formatter;
            formatter.reset();

            String in = ((ClassElement)element).getIn();

            if (in != null) {
                formatter.appendText(in);
            } else {
                return null;
            }

            return formatter.getText();
        }
    }

    private class PlainItem extends RubyCompletionItem {
        PlainItem(Element element, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
        }
    }

    private class FieldItem extends RubyCompletionItem {
        FieldItem(Element element, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
        }

        @Override
        public String getLhsHtml() {
            if (element instanceof IndexedField) {
                HtmlFormatter formatter = request.formatter;
                formatter.reset();
                IndexedField field = (IndexedField)element;
                boolean emphasize = !field.isInherited();
                if (emphasize) {
                    formatter.emphasis(true);
                }
                formatter.name(ElementKind.FIELD, true);
                formatter.appendText(getName());
                formatter.name(ElementKind.FIELD, false);
                if (emphasize) {
                    formatter.emphasis(false);
                }
                
                return formatter.getText();
            }
            return super.getLhsHtml();
        }
        
        @Override
        public String getInsertPrefix() {
            String name;
            if (element.getModifiers().contains(Modifier.STATIC)) {
                name = "@@" + getName();
            } else {
                name = "@" + getName();
            }
            if (symbol) {
                name = ":" + name;
            }
            
            return name;
        }
        
        @Override
        public String getRhsHtml() {
            HtmlFormatter formatter = request.formatter;
            formatter.reset();

            // Top level methods (defined on Object) : print
            // the defining file instead
            if (element instanceof IndexedField) {
                IndexedField idx = (IndexedField)element;

                // TODO - check if top level?
                //if (me.isTopLevel() && me instanceof IndexedMethod) {
                //IndexedMethod im = (IndexedMethod)element;
                //if (im.isTopLevel() && im.getRequire() != null) {
                //    formatter.appendText(im.getRequire());
                //
                //    return formatter.getText();
                //}
                //}

                String in = idx.getIn();
                if (in != null) {
                    formatter.appendText(in);
                    return formatter.getText();
                }
            }

            return null;
        }
    }
    
    private class ParameterItem extends RubyCompletionItem {   
        private static final String CONSTANT_ICON = "org/netbeans/modules/ruby/symbol.png"; //NOI18N
        private final String name;
        private final String desc;
        private final String insert;
        
        ParameterItem(IndexedMethod element, String name, String symbol, String insert, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
            this.name = name;
            this.desc = symbol;
            this.insert = insert;
        }

        @Override
        public String getRhsHtml() {
            if (desc != null) {
                HtmlFormatter formatter = request.formatter;
                formatter.reset();
                formatter.appendText(desc);
                return formatter.getText();
            } else {
                return null;
            }
        }
        
        @Override
        public ElementKind getKind() {
            return ElementKind.PARAMETER;
        }

        @Override
        public ImageIcon getIcon() {
            if (symbolIcon == null) {
                symbolIcon = new ImageIcon(org.openide.util.Utilities.loadImage(CONSTANT_ICON));
            }

            return symbolIcon;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getInsertPrefix() {
            return insert;
        }
    }
    
    private class CallItem extends MethodItem {   
        private IndexedMethod method;
        private int index;
        
        CallItem(IndexedMethod method, int parameterIndex, int anchorOffset, CompletionRequest request) {
            super(method, anchorOffset, request);
            this.method = method;
            this.index = parameterIndex;
        }

        @Override
        public String getRhsHtml() {
            return super.getRhsHtml();//null;
        }
        
        @Override
        public ElementKind getKind() {
            return ElementKind.CALL;
        }

        @Override
        public String getInsertPrefix() {
            return "";
        }

        @Override
        public String getLhsHtml() {
            ElementKind kind = getKind();
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            formatter.name(kind, true);
            formatter.appendText(getName());

            List<String> parameters = method.getParameters();

            if ((parameters != null) && (parameters.size() > 0)) {
                formatter.appendHtml("("); // NOI18N

                if (index > 0 && index < parameters.size()) {
                    formatter.appendText("... , ");
                }
                
                formatter.active(true);
                formatter.appendText(parameters.get(Math.min(parameters.size()-1, index)));
                formatter.active(false);
                
                if (index < parameters.size()-1) {
                    formatter.appendText(", ...");
                }

                formatter.appendHtml(")"); // NOI18N
            }
            
            if (method.hasBlock() && !method.isBlockOptional()) {
                formatter.appendText(" { }");
            }

            formatter.name(kind, false);

            return formatter.getText();
        }

        @Override
        public boolean isSmart() {
            return true;
        }

        @Override
        public List<String> getInsertParams() {
            return null;
        }
        
        @Override
        public String getCustomInsertTemplate() {
            return null;
        }
    }

    /** Methods/attributes inferred from ActiveRecord migrations */
    private class DbItem extends RubyCompletionItem {
        private String name;
        private String type;
        
        DbItem(String name, String type, int anchorOffset, CompletionRequest request) {
            super(null, anchorOffset, request);
            this.name = name;
            this.type = type;
        }
        
        @Override
        public String getLhsHtml() {
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            formatter.emphasis(true);
            formatter.name(ElementKind.DB, true);
            formatter.appendText(getName());
            formatter.name(ElementKind.DB, false);
            formatter.emphasis(false);

            return formatter.getText();
        }

        @Override
        public String getInsertPrefix() {
            return name;
        }
        
        @Override
        public String getRhsHtml() {
            HtmlFormatter formatter = request.formatter;
            formatter.reset();

            // TODO - include table name somewhere?
            formatter.appendText(type);
            return formatter.getText();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.DB;
        }

        @Override
        public ImageIcon getIcon() {
            return null;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean isSmart() {
            // All database attributes are considered smart matches
            return true;
        }
    }
    
    /** Return true if we always want to use parentheses
     * @todo Make into a user-configurable option
     * @todo Avoid doing this if there's possible ambiguity (e.g. nested method calls
     *   without spaces
     */
    
    private static boolean forceCompletionSpaces() {
        return FORCE_COMPLETION_SPACES;
    }
    
    private static final boolean FORCE_COMPLETION_SPACES = Boolean.getBoolean("ruby.complete.spaces"); // NOI18N
    
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);
        
        if (c == '\n' || c == '(' || c == '[' || c == '{') {
            return QueryType.STOP;
        }
        
        if (c != '.' && c != ':') {
            return QueryType.NONE;
        }

        int offset = component.getCaretPosition();
        BaseDocument doc = (BaseDocument)component.getDocument();

        if (".".equals(typedText)) { // NOI18N
            // See if we're in Ruby context
            TokenSequence<? extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);
            if (ts == null) {
                return QueryType.NONE;
            }
            ts.move(offset);
            if (!ts.moveNext()) {
                if (!ts.movePrevious()) {
                    return QueryType.NONE;
                }
            }
            if (ts.offset() == offset && !ts.movePrevious()) {
                return QueryType.NONE;
            }
            Token<? extends GsfTokenId> token = ts.token();
            TokenId id = token.id();
            
            // ".." is a range, not dot completion
            if (id == RubyTokenId.RANGE) {
                return QueryType.NONE;
            }

            // TODO - handle embedded ruby
            if ("comment".equals(id.primaryCategory()) || // NOI18N
                    "string".equals(id.primaryCategory()) ||  // NOI18N
                    "regexp".equals(id.primaryCategory())) { // NOI18N
                return QueryType.NONE;
            }
            
            return QueryType.COMPLETION;
        }
        
        if (":".equals(typedText)) { // NOI18N
            // See if it was "::" and we're in ruby context
            int dot = component.getSelectionStart();
            try {
                if ((dot > 1 && component.getText(dot-2, 1).charAt(0) == ':') && // NOI18N
                        isRubyContext(doc, dot-1)) {
                    return QueryType.COMPLETION;
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
        }
        
        return QueryType.NONE;
    }
    
    public static boolean isRubyContext(BaseDocument doc, int offset) {
        TokenSequence<? extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);

        if (ts == null) {
            return false;
        }
        
        ts.move(offset);
        
        if (!ts.movePrevious() && !ts.moveNext()) {
            return true;
        }
        
        TokenId id = ts.token().id();
        if ("comment".equals(id.primaryCategory()) || "string".equals(id.primaryCategory()) || // NOI18N
                "regexp".equals(id.primaryCategory())) { // NOI18N
            return false;
        }
        
        return true;
    }
}
