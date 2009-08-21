/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.ArgsNode;
import org.jrubyparser.ast.ArgumentNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.INameNode;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.RubyCompletionItem.CallItem;
import org.netbeans.modules.ruby.RubyCompletionItem.ClassItem;
import org.netbeans.modules.ruby.RubyCompletionItem.FieldItem;
import org.netbeans.modules.ruby.RubyCompletionItem.MethodItem;
import org.netbeans.modules.ruby.RubyCompletionItem.ParameterItem;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.AstFieldElement;
import org.netbeans.modules.ruby.elements.AstNameElement;
import org.netbeans.modules.ruby.elements.CommentElement;
import org.netbeans.modules.ruby.elements.Element;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedField;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.IndexedVariable;
import org.netbeans.modules.ruby.elements.KeywordElement;
import org.netbeans.modules.ruby.elements.RubyElement;
import org.netbeans.modules.ruby.lexer.Call;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyStringTokenId;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Code completion handler for Ruby.
 * 
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
public class RubyCodeCompleter implements CodeCompletionHandler {

    // Another good logical parameter would be SINGLE_WHITESPACE which would
    // insert a whitespace separator IF NEEDED

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

    public RubyCodeCompleter() {
    }

    static boolean startsWith(String theString, String prefix, boolean caseSensitive) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                             : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private boolean startsWith(String theString, String prefix) {
        return RubyCodeCompleter.startsWith(theString, prefix, caseSensitive);
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
    public String getPrefix(ParserResult info, int lexOffset, boolean upToOffset) {
        try {
            BaseDocument doc = RubyUtils.getDocument(info);
            if (doc == null) {
                return null;
            }

            TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
            doc.readLock(); // Read-lock due to token hierarchy use
            try {
            int requireStart = LexUtilities.getRequireStringOffset(lexOffset, th);

            if (requireStart != -1) {
                // XXX todo - do upToOffset
                return doc.getText(requireStart, lexOffset - requireStart);
            }

            TokenSequence<?extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

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

            Token<?extends RubyTokenId> token = ts.token();

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
            } finally {
                doc.readUnlock();
            }
            // Else: normal identifier: just return null and let the machinery do the rest
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        // Default behavior
        return null;
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
        QuerySupport.Kind kind = request.kind;
        
        TokenSequence<?extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

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

            Token<?extends RubyTokenId> token = ts.token();

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
    
    private void completeGlobals(List<CompletionProposal> proposals, CompletionRequest request, boolean showSymbols) {
        RubyIndex index = request.index;
        String prefix = request.prefix;
        QuerySupport.Kind kind = request.kind;
        
        Set<IndexedVariable> globals = index.getGlobals(prefix, kind);
        for (IndexedVariable global : globals) {
            RubyCompletionItem item = new RubyCompletionItem(global, anchor, request);
            item.setSmart(true);

            if (showSymbols) {
                item.setSymbol(true);
            }
            
            proposals.add(item);
        }
    }

    private boolean addParameters(List<CompletionProposal> proposals, CompletionRequest request) {
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        @SuppressWarnings("unchecked")
        Set<IndexedMethod>[] alternatesHolder = new Set[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        ParserResult info = request.parserResult;
        int lexOffset = request.lexOffset;
        int astOffset = request.astOffset;
        if (!RubyMethodCompleter.computeMethodCall(info, lexOffset, astOffset,
                methodHolder, paramIndexHolder, anchorOffsetHolder, alternatesHolder, request.kind)) {

            return false;
        }

        IndexedMethod targetMethod = methodHolder[0];
        int index = paramIndexHolder[0];
        
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
        
        List<String> params = targetMethod.getParameters();
        if (params == null || params.isEmpty()) {
            return false;
        }

        if  (params.size() <= index) {
            // Just use the last parameter in these cases
            // See for example the TableDefinition.binary dynamic method where
            // you can add a number of parameter names and the options parameter
            // is always the last one
            index = params.size()-1;
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
        Set<IndexedClass> clz = request.index.getSubClasses(request.prefix, RubyIndex.ACTIVE_RECORD_BASE, request.kind);
        
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
//        Set<String> tables = request.index.getDatabaseTables(request.prefix, request.kind);
        
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
    public CodeCompletionResult complete(final CodeCompletionContext context) {
        ParserResult ir = context.getParserResult();
        int lexOffset = context.getCaretOffset();
        String prefix = context.getPrefix();
        QuerySupport.Kind kind = context.isPrefixMatch() ? QuerySupport.Kind.PREFIX : QuerySupport.Kind.EXACT;
        QueryType queryType = context.getQueryType();
        this.caseSensitive = context.isCaseSensitive();

        final int astOffset = AstUtilities.getAstOffset(ir, lexOffset);
        if (astOffset == -1) {
            return null;
        }
        
        // Avoid all those annoying null checks
        if (prefix == null) {
            prefix = "";
        }

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        DefaultCompletionResult completionResult = new DefaultCompletionResult(proposals, false);

        anchor = lexOffset - prefix.length();

        final RubyIndex index = RubyIndex.get(ir);

        final Document document = RubyUtils.getDocument(ir);
        if (document == null) {
            return CodeCompletionResult.NONE;
        }

        // TODO - move to LexUtilities now that this applies to the lexing offset?
        lexOffset = AstUtilities.boundCaretOffset(ir, lexOffset);

        // Discover whether we're in a require statement, and if so, use special completion
        final TokenHierarchy<Document> th = TokenHierarchy.get(document);
        final BaseDocument doc = (BaseDocument)document;
        final FileObject fileObject = RubyUtils.getFileObject(ir);
        
        boolean showLower = true;
        boolean showUpper = true;
        boolean showSymbols = false;
        char first = 0;

        doc.readLock(); // Read-lock due to Token hierarchy use
        try {
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
        // a request context with supporting parserResult needed by the various completion helpers.
        CompletionRequest request = new CompletionRequest(
                completionResult, th, ir, lexOffset, astOffset,
                doc, prefix, index, kind, queryType, fileObject);
        
        // See if we're inside a string or regular expression and if so,
        // do completions applicable to strings - require-completion,
        // escape codes for quoted strings and regular expressions, etc.
        if (RubyStringCompleter.complete(proposals, request, anchor, caseSensitive)) {
            completionResult.setFilterable(false);
            return completionResult;
        }
        
        Call call = Call.getCallType(doc, th, lexOffset);

        // Fields
        // This is a bit stupid at the moment, not looking at the current typing context etc.
        Node root = AstUtilities.getRoot(ir);

        if (root == null) {
            RubyKeywordCompleter.complete(proposals, request, anchor, caseSensitive, showSymbols);
            return completionResult;
        }

        // Compute the bounds of the line that the caret is on, and suppress nodes overlapping the line.
        // This will hide not only paritally typed identifiers, but surrounding contents like the current class and module
        final int astLineBegin;
        final int astLineEnd;

        try {
            astLineBegin = AstUtilities.getAstOffset(ir, Utilities.getRowStart(doc, lexOffset));
            astLineEnd = AstUtilities.getAstOffset(ir, Utilities.getRowEnd(doc, lexOffset));
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return CodeCompletionResult.NONE;
        }

        final AstPath path = new AstPath(root, astOffset);
        request.path = path;

        Map<String, Node> variables = new HashMap<String, Node>();
        Map<String, Node> fields = new HashMap<String, Node>();
        Map<String, Node> globals = new HashMap<String, Node>();
        Map<String, Node> constants = new HashMap<String, Node>();

        final Node closest = path.leaf();
        request.target = closest;

        // Don't try to add local vars, globals etc. as part of calls or class fqns
        if (call.getLhs() == null) {
            if (showLower && (closest != null)) {

                List<Node> applicableBlocks = AstUtilities.getApplicableBlocks(path, false);
                for (Node block : applicableBlocks) {
                    addDynamic(block, variables);
                }
                
                Node method = AstUtilities.findLocalScope(closest, path);

                List<Node> list2 = method.childNodes();

                for (Node child : list2) {
                    if (child.isInvisible()) {
                        continue;
                    }
                    addLocals(child, variables);
                }
            }

            boolean inAttrCall = isInAttr(closest, path);

            if (prefix.length() == 0 || first == '@' || showSymbols || inAttrCall) {
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
                     //strip out ':' when querying fields for cases like 'attr_reader :^'
                    if (inAttrCall && first == ':' && prefix.length() == 1) {
                        f = index.getInheritedFields(fqn, "", kind, false);
                    } else {
                        f = index.getInheritedFields(fqn, prefix, kind, false);
                    }
                }

                for (IndexedField field : f) {
                    String insertPrefix = inAttrCall ? ":" : null;
                    FieldItem item = new FieldItem(field, anchor, request, insertPrefix);

                    item.setSmart(field.isSmart());

                    if (showSymbols) {
                        item.setSymbol(true);
                    }

                    proposals.add(item);
                }

                // return just the fields for attr_
                if (inAttrCall) {
                    return completionResult;
                }
            }

            // $ is neither upper nor lower 
            if ((prefix.length() == 0) || (first == '$') || showSymbols) {
                if (prefix.startsWith("$") || showSymbols) {
                    completeGlobals(proposals, request, showSymbols);
                    // Dollar variables too
                    RubyKeywordCompleter.complete(proposals, request, anchor, caseSensitive, showSymbols);
                    if (!showSymbols) {
                        return completionResult;
                    }
                }
            }
        }

        // TODO: should only include fields etc. down to caret location??? Decide. (Depends on language semantics. Can I have forward referemces?
        if (call.isConstantExpected()) {
            addConstants(root, constants);
            RubyConstantCompleter.complete(proposals, request, anchor, caseSensitive, call);
        }
        
        // If we're in a call, add in some parserResult and help for the code completion call
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
                    return completionResult;
                }

                if ((fqn != null) &&
                        RubyMethodCompleter.complete(proposals, request, fqn, call, anchor, caseSensitive)) {
                    return completionResult;
                }

                // Only call local and inherited methods if we don't have an LHS, such as Foo::
                if (call.getLhs() == null) {
                    // TODO - pull this into a completeInheritedMethod call
                    // Complete inherited methods or local methods only (plus keywords) since there
                    // is no receiver so it must be a local or inherited method call
                    Set<IndexedMethod> inheritedMethods =
                        index.getInheritedMethods(fqn, prefix, kind);

                    inheritedMethods = RubyDynamicFindersCompleter.proposeDynamicMethods(inheritedMethods, proposals, request, anchor);
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
                    return completionResult;
                }
            }
            if ((showUpper && ((prefix != null && prefix.length() > 0) ||
                    (!call.isMethodExpected() && call.getLhs() != null && call.getLhs().length() > 0)))
                    || (showSymbols && !inCall)) {
                // TODO - allow method calls if you're already entered the first char!
                RubyClassCompleter.complete(proposals, request, anchor, caseSensitive, call, showSymbols);
            }
        }
        assert (kind == QuerySupport.Kind.PREFIX) || (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX) ||
        (kind == QuerySupport.Kind.EXACT);

        // TODO
        // Remove fields and variables whose names are already taken, e.g. do a fields.removeAll(variables) etc.
        for (String variable : variables.keySet()) {
            if (((kind == QuerySupport.Kind.EXACT) && prefix.equals(variable)) ||
                    ((kind != QuerySupport.Kind.EXACT) && startsWith(variable, prefix))) {
                Node node = variables.get(variable);

                if (!overlapsLine(node, astLineBegin, astLineEnd)) {
                    AstElement co = new AstNameElement(ir, node, variable,
                            ElementKind.VARIABLE);
                    RubyCompletionItem item = new RubyCompletionItem(co, anchor, request);
                    item.setSmart(true);

                    if (showSymbols) {
                        item.setSymbol(true);
                    }

                    proposals.add(item);
                }
            }
        }

        for (String field : fields.keySet()) {
            if (((kind == QuerySupport.Kind.EXACT) && prefix.equals(field)) ||
                    ((kind != QuerySupport.Kind.EXACT) && startsWith(field, prefix))) {
                Node node = fields.get(field);

                if (overlapsLine(node, astLineBegin, astLineEnd)) {
                    continue;
                }

                Element co = new AstFieldElement(ir, node);
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
            // TODO - kind.EXACT
            if (startsWith(variable, prefix) ||
                    (showSymbols && startsWith(variable.substring(1), prefix))) {
                Node node = globals.get(variable);

                if (overlapsLine(node, astLineBegin, astLineEnd)) {
                    continue;
                }

                AstElement co = new AstNameElement(ir, node, variable,
                        ElementKind.VARIABLE);
                RubyCompletionItem item = new RubyCompletionItem(co, anchor, request);
                item.setSmart(true);

                if (showSymbols) {
                    item.setSymbol(true);
                }

                proposals.add(item);
            }
        }
        
        // TODO - model globals and constants using different icons / etc.
        for (String variable : constants.keySet()) {
            if (((kind == QuerySupport.Kind.EXACT) && prefix.equals(variable)) ||
                    ((kind != QuerySupport.Kind.EXACT) && startsWith(variable, prefix))) {
                // Skip constants that are known to be classes
                Node node = constants.get(variable);

                if (overlapsLine(node, astLineBegin, astLineEnd)) {
                    continue;
                }

                //                ComObject co;
                //                if (isClassName(variable)) {
                //                    co = JRubyNode.create(target, null);
                //                    if (co == null) {
                //                        continue;
                //                    }
                //                } else {
                //                    co = new DefaultComVariable(variable, false, -1, -1);
                //                    ((DefaultComVariable)co).setNode(target);
                AstElement co = new AstNameElement(ir, node, variable,
                        ElementKind.VARIABLE);

                RubyCompletionItem item = new RubyCompletionItem(co, anchor, request);
                item.setSmart(true);

                if (showSymbols) {
                    item.setSymbol(true);
                }

                proposals.add(item);
            }
        }

        if (RubyKeywordCompleter.complete(proposals, request, anchor, caseSensitive, showSymbols)) {
            return completionResult;
        }

        if (queryType == QueryType.DOCUMENTATION) {
            proposals = filterDocumentation(proposals, root, doc, ir, astOffset, lexOffset, prefix, path,
                    index);
        }
        } finally {
            doc.readUnlock();
        }

        return completionResult;
    }
        

    private boolean isInAttr(Node closest, AstPath path) {
        if (closest != null) {
            // first argument in attr_*
            for (Node child : closest.childNodes()) {
                if (AstUtilities.isAttr(child)) {
                    return true;
                }
            }
            // others, e.g. attr_reader :foo, :ba^r
            if (AstUtilities.isAttr(path.leafParent()) || AstUtilities.isAttr(path.leafGrandParent())) {
                return true;
            }
        }
        return false;
    }

    private void addActionViewMethods(Set<IndexedMethod> inheritedMethods, FileObject fileObject, RubyIndex index, String prefix,
            QuerySupport.Kind kind) { 
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
            QuerySupport.Kind kind) { 
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
    // TODO - pass in request object here!
    private List<CompletionProposal> filterDocumentation(List<CompletionProposal> proposals,
        Node root, BaseDocument doc, ParserResult parserResult, int astOffset, int lexOffset, String name,
        AstPath path, RubyIndex index) {
        // Look to see if this symbol is either a "class Foo" or a "def foo", and if we invoke
        // completion on it, prefer this element provided it has documentation
        List<CompletionProposal> candidates = new ArrayList<CompletionProposal>();
        FileObject fo = RubyUtils.getFileObject(parserResult);
        Map<IndexedElement, CompletionProposal> elementMap =
            new HashMap<IndexedElement, CompletionProposal>();
        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();
        Set<IndexedClass> classes = new HashSet<IndexedClass>();

        for (CompletionProposal proposal : proposals) {
            RubyElement e = (RubyElement) proposal.getElement();

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
            RubyElement re = (RubyElement) candidate.getElement();
            if (!(re instanceof IndexedElement)) {
                continue;
            }
            IndexedElement e = (IndexedElement)re;
            String signature = e.getSignature();
            Node node = AstUtilities.findBySignature(root, signature);

            if (node != null) {
                SourcePosition pos = node.getPosition();
                int startPos = LexUtilities.getLexerOffset(parserResult, pos.getStartOffset());

                try {
                    int lineBegin = AstUtilities.getAstOffset(parserResult, Utilities.getRowFirstNonWhite(doc, startPos));
                    int lineEnd = AstUtilities.getAstOffset(parserResult, Utilities.getRowEnd(doc, startPos));

                    if ((astOffset >= lineBegin) && (astOffset <= lineEnd)) {
                        // Look for documentation
                        List<String> rdoc = AstUtilities.gatherDocumentation(parserResult.getSnapshot(), node);

                        if (rdoc != null && !rdoc.isEmpty()) {
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
        IndexedElement candidate = null;

        if (!classes.isEmpty()) {
            RubyClassDeclarationFinder cdf = new RubyClassDeclarationFinder(parserResult, null, path, index, path.leaf());
            candidate = cdf.findBestElementMatch(classes);
        } else if (!methods.isEmpty()) {
            RubyDeclarationFinder finder = new RubyDeclarationFinder();
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
        SourcePosition pos = node.getPosition();

        //return (((pos.getStartOffset() <= lineEnd) && (pos.getEndOffset() >= lineBegin)));
        // Don't look to see if the line is within the target. See if the target is started on this line (where
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

    static void addLocals(Node node, Map<String, Node> variables) {
        switch (node.getNodeType()) {
        case LOCALASGNNODE: {
            String name = ((INameNode)node).getName();

            if (!variables.containsKey(name)) {
                variables.put(name, node);
            }
            break;
        }
        case ARGSNODE: {
            // TODO - use AstUtilities.getDefArgs here - but avoid hitting them twice!
            //List<String> parameters = AstUtilities.getDefArgs(def, true);
            // However, I've gotta find the parameter nodes themselves too!
            ArgsNode an = (ArgsNode)node;

            if (an.getRequiredCount() > 0) {
                List<Node> args = an.childNodes();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        List<Node> args2 = arg.childNodes();

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
            if (an.getRest() != null) {
                String name = an.getRest().getName();
                variables.put(name, an.getRest());
            }

            // Block args
            if (an.getBlock() != null) {
                String name = an.getBlock().getName();
                variables.put(name, an.getBlock());
            }
            
            break;
        }

        //        } else if (target instanceof AliasNode) {
        //            AliasNode an = (AliasNode)target;
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
            if (child.isInvisible()) {
                continue;
            }
            switch (child.getNodeType()) {
            case DEFNNODE:
            case DEFSNODE:
            case CLASSNODE:
            case SCLASSNODE:
            case MODULENODE:
                // Don't look in nested context for local vars
                continue;
            }

            addLocals(child, variables);
        }
    }

    static void addDynamic(Node node, Map<String, Node> variables) {
        if (node.getNodeType() == NodeType.DASGNNODE) {
            String name = ((INameNode)node).getName();

            if (!variables.containsKey(name)) {
                variables.put(name, node);
            }

            //} else if (target instanceof ArgsNode) {
            //    ArgsNode an = (ArgsNode)target;
            //
            //    if (an.getArgsCount() > 0) {
            //        List<Node> args = an.childNodes();
            //        List<String> parameters = null;
            //
            //        for (Node arg : args) {
            //            if (arg instanceof ListNode) {
            //                List<Node> args2 = arg.childNodes();
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
            //        } else if (!ignoreAlias && target instanceof AliasNode) {
            //            AliasNode an = (AliasNode)target;
            //
            //            if (an.getNewName().equals(name)) {
            //                OffsetRange range = AstUtilities.getAliasNewRange(an);
            //                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            //            } else if (an.getOldName().equals(name)) {
            //                OffsetRange range = AstUtilities.getAliasOldRange(an);
            //                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            //            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            switch (child.getNodeType()) {
            case ITERNODE:
            //case BLOCKNODE:
            case DEFNNODE:
            case DEFSNODE:
            case CLASSNODE:
            case SCLASSNODE:
            case MODULENODE:
                continue;
            }

            addDynamic(child, variables);
        }
    }

    private void addConstants(Node node, Map<String, Node> constants) {
        if (node.getNodeType() == NodeType.CONSTDECLNODE) {
            constants.put(((INameNode)node).getName(), node);
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            addConstants(child, constants);
        }
    }

    private String loadResource(String basename) {
        // TODO: I18N
        InputStream is = null;
        StringBuilder sb = new StringBuilder();

        try {
            is = new BufferedInputStream(RubyCodeCompleter.class.getResourceAsStream("resources/" +
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
    static IndexedElement findDocumentationEntry(Node root, IndexedElement obj) {
        // 1. Find entries known to have documentation
        String fqn = obj.getSignature();
        Set<?extends IndexedElement> result = obj.getIndex().getDocumented(fqn);

        if ((result == null) || (result.isEmpty())) {
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

            if (RubyUtils.isRubyStubsURL(url)) {
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
     * @param parserResult The (optional) compilation parserResult for a document referencing the element.
     *   This is used to consult require-statements in the given compilation context etc.
     *   to choose among many alternatives. May be null, in which case the element had
     *   better be an IndexedElement.
     */
    static List<String> getComments(ParserResult info, Element element) {
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

            node = AstUtilities.getForeignNode(com);

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
        Snapshot snapshot;
        if (element instanceof IndexedElement) {
            FileObject f = ((IndexedElement) element).getFileObject();
            snapshot = Source.create(f).createSnapshot();
        } else if (info != null) {
            snapshot = info.getSnapshot();
        } else {
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
                    comments = AstUtilities.gatherDocumentation(snapshot, clz);

                    if ((comments != null) && (!comments.isEmpty())) {
                        break;
                    }
                }
            }
        } else {
            comments = AstUtilities.gatherDocumentation(snapshot, node);
        }

        if ((comments == null) || (comments.isEmpty())) {
            return null;
        }
        
        return comments;
    }
    
    public String document(ParserResult info, ElementHandle handle) {
        Element element = null;
        if (handle instanceof ElementHandle.UrlHandle) {
            String url = ((ElementHandle.UrlHandle)handle).getUrl();
            DeclarationLocation loc = new RubyDeclarationFinder().findLinkedMethod(info, url);
            if (loc != DeclarationLocation.NONE) {
                //element = loc.getElement();
                ElementHandle h = loc.getElement();
                if (handle != null) {
                    element = RubyParser.resolveHandle(info, h);
                    if (element == null) {
                        return null;
                    }
                }
            }
        } else {
            element = RubyParser.resolveHandle(info, handle);
        }
        if (element == null) {
            return null;
        }
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
            return formatter.toHtml();
        }
        
        List<String> comments = getComments(info, element);
        if (comments == null) {
            if (element.getName().startsWith("find_by_") ||
                element.getName().startsWith("find_all_by_")) {
                return new RDocFormatter().getSignature(element) + NbBundle.getMessage(RubyCodeCompleter.class, "DynamicMethod");
            }
            String html = new RDocFormatter().getSignature(element) + "\n<hr>\n<i>" + NbBundle.getMessage(RubyCodeCompleter.class, "NoCommentFound") +"</i>";

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

    public ElementHandle resolveLink(String link, ElementHandle elementHandle) {
        if (link.indexOf('#') != -1 && elementHandle.getMimeType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
            if (link.startsWith("#")) {
                // Put the current class etc. in front of the method call if necessary
                Element surrounding = RubyParser.resolveHandle(null, elementHandle);
                if (surrounding != null && surrounding.getKind() != ElementKind.KEYWORD) {
                    String name = surrounding.getName();
                    ElementKind kind = surrounding.getKind();
                    if (!(kind == ElementKind.CLASS || kind == ElementKind.MODULE)) {
                        String in = surrounding.getIn();
                        if (in != null && in.length() > 0) {
                            name = in;
                        } else if (name != null) {
                            int index = name.indexOf('#');
                            if (index > 0) {
                                name = name.substring(0, index);
                            }
                        }
                    }
                    if (name != null) {
                        link = name + link;
                    }
                }
            }
            return new ElementHandle.UrlHandle(link);
        }
        
        return null;
    }
    
    public Set<String> getApplicableTemplates(ParserResult info, int selectionBegin,
        int selectionEnd) {

        // TODO - check the code at the AST path and determine whether it makes sense to
        // wrap it in a begin block etc.
        // TODO - I'd like to be able to pass any selection-based templates I'm not familiar with
        
        boolean valid = false;

        if (selectionEnd != -1) {
            BaseDocument doc = RubyUtils.getDocument(info);
            if (doc == null) {
                return Collections.emptySet();
            }

            try {
                doc.readLock();
                if (selectionBegin == selectionEnd) {
                    return Collections.emptySet();
                } else if (selectionEnd < selectionBegin) {
                    int temp = selectionBegin;
                    selectionBegin = selectionEnd;
                    selectionEnd = temp;
                }
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
                            Token<?> token = LexUtilities.getToken(doc, selectionBegin);
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
            } finally {
                doc.readUnlock();
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

    private String suggestName(ParserResult info, int caretOffset, String prefix, Map params) {
        // Look at the given context, compute fields and see if I can find a free name
        caretOffset = AstUtilities.boundCaretOffset(info, caretOffset);

        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return null;
        }

        AstPath path = new AstPath(root, caretOffset);
        Node closest = path.leaf();
        if (closest == null) {
            return null;
        }

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

    public String resolveTemplateVariable(String variable, ParserResult result, int caretOffset,
        String name, Map params) {
        if (variable.equals(KEY_PIPE)) {
            return "||";
        }

        // Old-style format - support temporarily
        if (variable.equals(ATTR_UNUSEDLOCAL)) { // TODO REMOVEME
            return suggestName(result, caretOffset, name, params);
        }

        if (params != null && params.containsKey(ATTR_UNUSEDLOCAL)) {
            return suggestName(result, caretOffset, name, params);
        }

        if ((!(variable.equals(KEY_METHOD) || variable.equals(KEY_METHOD_FQN) ||
                variable.equals(KEY_CLASS) || variable.equals(KEY_CLASS_FQN) ||
                variable.equals(KEY_SUPERCLASS) || variable.equals(KEY_PATH) ||
                variable.equals(KEY_FILE)))) {
            return null;
        }

        caretOffset = AstUtilities.boundCaretOffset(result, caretOffset);

        Node root = AstUtilities.getRoot(result);

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
                RubyIndex index = RubyIndex.get(result);
                if (index != null) {
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
            return FileUtil.toFile(result.getSnapshot().getSource().getFileObject()).getName();
        } else if (variable.equals(KEY_PATH)) {
            return FileUtil.toFile(RubyUtils.getFileObject(result)).getPath();
        }

        return null;
    }

    public ParameterInfo parameters(ParserResult info, int lexOffset, CompletionProposal proposal) {
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int astOffset = AstUtilities.getAstOffset(info, lexOffset);
        if (!RubyMethodCompleter.computeMethodCall(info, lexOffset, astOffset,
                methodHolder, paramIndexHolder, anchorOffsetHolder, null, QuerySupport.Kind.PREFIX)) {

            return ParameterInfo.NONE;
        }

        IndexedMethod method = methodHolder[0];
        if (method == null) {
            return ParameterInfo.NONE;
        }
        int index = paramIndexHolder[0];
        int astAnchorOffset = anchorOffsetHolder[0];
        int anchorOffset = LexUtilities.getLexerOffset(info, astAnchorOffset);


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

        if ((params != null) && (!params.isEmpty())) {
            return new ParameterInfo(params, index, anchorOffset);
        }

        return ParameterInfo.NONE;
    }
    
    /** Return true if we always want to use parentheses
     * @todo Make into a user-configurable option
     * @todo Avoid doing this if there's possible ambiguity (e.g. nested method calls
     *   without spaces
     */
    
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);
        
        if (c == '\n' || c == '(' || c == '[' || c == '{') {
            return QueryType.STOP;
        }
        
        if (c != '.' && c != ':') {
            return QueryType.NONE;
        }

        int offset = component.getCaretPosition();
        BaseDocument doc = (BaseDocument) component.getDocument();

        if (".".equals(typedText)) { // NOI18N
            // See if we're in Ruby context
            TokenSequence<? extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);
            if (ts == null) {
                return QueryType.NONE;
            }
            ts.move(offset);
            if (!ts.moveNext() && !ts.movePrevious()) {
                return QueryType.NONE;
            }
            if (ts.offset() == offset && !ts.movePrevious()) {
                return QueryType.NONE;
            }
            Token<? extends RubyTokenId> token = ts.token();
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
        TokenSequence<? extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);

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
