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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.mozilla.nb.javascript.Node;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.javascript.editing.JsParser.Sanitize;
import org.netbeans.modules.javascript.editing.lexer.Call;
import org.netbeans.modules.javascript.editing.lexer.JsCommentLexer;
import org.netbeans.modules.javascript.editing.lexer.JsCommentTokenId;
import org.netbeans.modules.javascript.editing.lexer.JsLexer;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Code completion handler for JavaScript
 * 
 * @todo Do completion on element id's inside $() calls (prototype.js) and $$() calls for CSS rules.
 *   See http://www.sitepoint.com/article/painless-javascript-prototype
 * @todo Track logical classes and inheritance ("extend")
 * @todo Track global variables (these are vars which aren't local). Somehow cooperate work between
 *    semantic highlighter and structure analyzer. I need to only store a single instance of each
 *    global var in the index. The variable visitor should probably be part of the structure analyzer,
 *    since global variables also need to be tracked there. Another possibility is having the
 *    parser track variables - but that's trickier. Perhaps a second pass over the parse tree
 *    (where I set parent pointers) is where I can do this? I can even change node types to be
 *    more obvious...
 * @todo I should NOT include in queries functions that are known to be methods if you're not doing
 *    "unnown type" completion!
 * @todo Today's feature work:
 *    - this.-completion should do something useful
 *    - I need to model prototype inheritance, and then use it in code completion queries
 *    - Skip no-doc'ed methods
 *    - Improve type analysis:
 *        - known types (element, document, ...)
 *        - variable-name guessing (el, doc, etc ...)
 *        - return value tracking
 *    - Improve indexing:
 *        - store @-private, etc.
 *        - more efficient browser-compat flags
 *    - Fix case-sensitivity on index queries such that open type and other forms of completion
 *      work better!
 *  @todo Distinguish properties and globals and functions? Perhaps with attributes in the flags!
 *  @todo Display more information in parameter tooltips, such as type hints (perhaps do smart
 *    filtering Java-style?), and explanations for each parameter
 *  @todo Need preindexing support for unit tests - and separate files
 * @todo Insert semicolon too when you insert methods, in custom templates (unless you're in a call), a var block, etc.
 * 
 * @author Tor Norbye
 */
public class JsCodeCompletion implements CodeCompletionHandler {

    private static final Logger LOG = Logger.getLogger(JsCodeCompletion.class.getName());
    static int MAX_COMPLETION_ITEMS = JsIndex.MAX_SEARCH_ITEMS;
    private static ImageIcon keywordIcon;
    private boolean caseSensitive;
    private static final String[] REGEXP_WORDS =
            new String[]{
        // Literals
        "\\0", "The NUL character (\\u0000)",
        "\\t", "Tab (\\u0009)",
        "\\n", "Newline (\\u000A)",
        "\\v", "Vertical tab (\\u000B)",
        "\\f", "Form feed (\\u000C)",
        "\\r", "Carriage return (\\u000D)",
        "\\x", "\\x<i>nn</i>: The latin character in hex <i>nn</i>",
        "\\u", "\\u<i>xxxx</i>: The Unicode character in hex <i>xxxx</i>",
        "\\c", "\\c<i>X</i>: The control character ^<i>X</i>",
        // Character classes
        "[]", "Any one character between the brackets",
        "[^]", "Any one character not between the brackets",
        "\\w", "Any ASCII word character; same as [0-9A-Za-z_]",
        "\\W", "Not a word character; same as [^0-9A-Za-z_]",
        "\\s", "Unicode space character",
        "\\S", "Non-space character",
        "\\d", "Digit character; same as [0-9]",
        "\\D", "Non-digit character; same as [^0-9]",
        "[\\b]", "Literal backspace",
        // Match positions
        "^", "Start of line",
        "$", "End of line",
        "\\b", "Word boundary (if not in a range specification)",
        "\\B", "Non-word boundary",
        // According to JavaScript The Definitive Guide, the following are not supported
        // in JavaScript:
        // \\a, \\e, \\l, \\u, \\L, \\U, \\E, \\Q, \\A, \\Z, \\z, and \\G
        //
        //"\\A", "Beginning of string",
        //"\\z", "End of string",
        //"\\Z", "End of string (except \\n)",

        "*", "Zero or more repetitions of the preceding",
        "+", "One or more repetitions of the preceding",
        "{m,n}", "At least m and at most n repetitions of the preceding",
        "?", "At most one repetition of the preceding; same as {0,1}",
        "|", "Either preceding or next expression may match",
        "()", "Grouping", //"[:alnum:]", "Alphanumeric character class",
    //"[:alpha:]", "Uppercase or lowercase letter",
    //"[:blank:]", "Blank and tab",
    //"[:cntrl:]", "Control characters (at least 0x00-0x1f,0x7f)",
    //"[:digit:]", "Digit",
    //"[:graph:]", "Printable character excluding space",
    //"[:lower:]", "Lowecase letter",
    //"[:print:]", "Any printable letter (including space)",
    //"[:punct:]", "Printable character excluding space and alphanumeric",
    //"[:space:]", "Whitespace (same as \\s)",
    //"[:upper:]", "Uppercase letter",
    //"[:xdigit:]", "Hex digit (0-9, a-f, A-F)",
    };
    // Strings section 7.8
    private static final String[] STRING_ESCAPES =
            new String[]{
        "\\0", "The NUL character (\\u0000)",
        "\\b", "Backspace (0x08)",
        "\\t", "Tab (\\u0009)",
        "\\n", "Newline (\\u000A)",
        "\\v", "Vertical tab (\\u000B)",
        "\\f", "Form feed (\\u000C)",
        "\\r", "Carriage return (\\u000D)",
        "\\\"", "Double Quote (\\u0022)",
        "\\'", "Single Quote (\\u0027)",
        "\\\\", "Backslash (\\u005C)",
        "\\x", "\\x<i>nn</i>: The latin character in hex <i>nn</i>",
        "\\u", "\\u<i>xxxx</i>: The Unicode character in hex <i>xxxx</i>",
        "\\", "\\<i>ooo</i>: The latin character in octal <i>ooo</i>",
        // PENDING: Is this supported?
        "\\c", "\\c<i>X</i>: The control character ^<i>X</i>",};
    private static final String[] CSS_WORDS =
            new String[]{
        // Dbl-space lines to keep formatter from collapsing pairs into a block

        // Source: http://docs.jquery.com/DOM/Traversing/Selectors
        "nth-child()", "The n-th child of its parent",
        "first-child", "First child of its parent",
        "last-child", "Last child of its parent",
        "only-child", "Only child of its parent",
        "empty", "Has no children (including text nodes)",
        "enabled", "Element which is not disabled",
        "disabled", "Element which is disabled",
        "checked", "Element which is checked (checkbox, ...)",
        "selected", "Element which is selected (e.g. in a select)",
        "link", "Not yet visited hyperlink",
        "visited", "Already visited hyperlink",
        "active", "",
        "hover", "",
        "focus", "Element during user actions",
        "target", "Target of the referring URI",
        "lang()", "Element in given language",
        ":first-line", "The first formatted line",
        ":first-letter", "The first formatted letter",
        ":selection", "Portion currently highlighted by the user",
        ":before", "Generated content before an element",
        ":after", "Generated content after an element",
        // Custom Selectors
        "even", "Selects every other (even) element",
        "odd", "Selects every other (odd) element",
        "eq()", "Selects the Nth element",
        "nth()", "Selects the Nth element",
        "gt()", "Selects elements whose index is greater than N",
        "lt()", "Selects elements whose index is less than N",
        "first", "Equivalent to :eq(0)",
        "last", "Selects the last matched element",
        "parent", "Elements that have children (including text)",
        "contains('", "Elements which contain the specified text",
        "visible", "Selects all visible elements",
        "hidden", "Selects all hidden elements",
        // Form Selectors
        "input", "All form elements",
        "text", "All text fields (type=\"text\")",
        "password", "All password fields (type=\"password\")",
        "radio", "All radio fields (type=\"radio\")",
        "checkbox", "All checkbox fields (type=\"checkbox\")",
        "submit", "All submit buttons (type=\"submit\")",
        "image", "All form images (type=\"image\")",
        "reset", "All reset buttons (type=\"reset\")",
        "button", "All other buttons (type=\"button\")",
        "file", "All file uploads (type=\"file\")",};
    // From http://code.google.com/p/jsdoc-toolkit/wiki/TagReference
    private static final String[] JSDOC_WORDS =
            new String[]{
        "@augments",
        "@class",
        "@config",
        "@constructor",
        "@deprecated",
        "@description",
        "@event",
        "@example",
        "@exception",
        "@fileOverview",
        "@function",
        "@ignore",
        "@inherits",
        "@memberOf",
        "@name",
        "@namespace",
        "@param",
        "@private",
        "@property",
        "@return",
        "@scope",
        "@static",
        "@type",};

    public JsCodeCompletion() {
    }

    public CodeCompletionResult complete(CodeCompletionContext context) {
        ParserResult info = context.getParserResult();
        int lexOffset = context.getCaretOffset();
        String prefix = context.getPrefix();
        QuerySupport.Kind kind = context.isPrefixMatch() ? QuerySupport.Kind.PREFIX : QuerySupport.Kind.EXACT;
        QueryType queryType = context.getQueryType();
        this.caseSensitive = context.isCaseSensitive();

        if (prefix == null) {
            prefix = ""; //NOI18N
        }

        final Document document = info.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            return CodeCompletionResult.NONE;
        }
        final BaseDocument doc = (BaseDocument) document;

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        DefaultCompletionResult completionResult = new DefaultCompletionResult(proposals, false);

        JsParseResult parseResult = AstUtilities.getParseResult(info);
        doc.readLock(); // Read-lock due to Token hierarchy use
        try {
            Node root = parseResult != null ? parseResult.getRootNode() : null;
            int astOffset = AstUtilities.getAstOffset(info, lexOffset);
            if (astOffset == -1) {
                try {
                    if (lexOffset < doc.getLength() && lexOffset > 0 && "\"\"".equals(doc.getText(lexOffset - 1, 2))) {
                        // Completion in HTML in something like an empty attribute
                        astOffset = 0;
                    } else {
                        return CodeCompletionResult.NONE;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                    return CodeCompletionResult.NONE;
                }
            }
            final TokenHierarchy<Document> th = TokenHierarchy.get(document);
            final FileObject fileObject = info.getSnapshot().getSource().getFileObject();
            Call call = Call.getCallType(doc, th, lexOffset);

            // Carry completion context around since this logic is split across lots of methods
            // and I don't want to pass dozens of parameters from method to method; just pass
            // a request context with supporting info needed by the various completion helpers i
            CompletionRequest request = new CompletionRequest();
            request.completionResult = completionResult;
            request.result = parseResult;
            request.lexOffset = lexOffset;
            request.astOffset = astOffset;
            request.index = JsIndex.get(QuerySupport.findRoots(fileObject, null, Collections.singleton(JsClassPathProvider.BOOT_CP), Collections.<String>emptySet()));
            request.doc = doc;
            request.info = AstUtilities.getParseResult(info);
            request.prefix = prefix;
            request.th = th;
            request.kind = kind;
            request.queryType = queryType;
            request.fileObject = fileObject;
            request.anchor = lexOffset - prefix.length();
            request.call = call;

            Token<? extends TokenId> token = LexUtilities.getToken(doc, lexOffset);
            if (token == null) {
                if (JsUtils.isJsFile(fileObject) || JsUtils.isJsonFile(fileObject)) {
                    if (doc.getLength() == 0) {
                        // Special case: empty document - no token, but completion is valid
                        completeKeywords(proposals, request);

                        // We already know that searching with an empty prefix will
                        // give too many matches. Rather than having completely random
                        // stuff show up, show up things starting with "a" to give
                        // impression that we're showing top of the list and mark truncated
                        request.prefix = "a"; // NOI18N
                        completionResult.setTruncated(true);

                        completeFunctions(proposals, request);
                    }
                    return completionResult;
                }

                // Embedding? Possibly in something like an EMPTY JAvaScript attribute,
                // e.g.   <input onclick="|"> - there's no token here. We have to
                // instead assume an empty prefix.
                request.prefix = prefix = "";
            } else {
                TokenId id = token.id();
                if (id == JsTokenId.LINE_COMMENT) {
                    // TODO - Complete symbols in comments?
                    return completionResult;
                } else if (id == JsTokenId.BLOCK_COMMENT) {
                    try {
                        completeComments(proposals, request);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return completionResult;
                } else if (id == JsTokenId.STRING_LITERAL || id == JsTokenId.STRING_END) {
                    completeStrings(proposals, request);
                    completionResult.setFilterable(false);
                    return completionResult;
                } else if (id == JsTokenId.REGEXP_LITERAL || id == JsTokenId.REGEXP_END) {
                    completeRegexps(proposals, request);
                    completionResult.setFilterable(false);
                    return completionResult;
                }
            }

            if (root != null) {
                int offset = astOffset;

                OffsetRange sanitizedRange = parseResult.getSanitizedRange();
                if (sanitizedRange != OffsetRange.NONE && sanitizedRange.containsInclusive(offset)) {
                    offset = sanitizedRange.getStart();
                }

                final AstPath path = new AstPath(root, offset);
                request.path = path;
                request.fqn = AstUtilities.getFqn(path, null, null);

                final Node closest = path.leaf();

                //check for regexp ast node. Under some circumstances (see issue #158890)
                //the text is not lexed as JsTokenId.REGEXP_LITERAL but still represents
                //a regular expression so we want the right completion there.
                if (closest.getType() == org.mozilla.nb.javascript.Token.REGEXP) {
                    completeRegexps(proposals, request);
                    completionResult.setFilterable(false);
                    return completionResult;
                }

                request.root = root;
                request.node = closest;
            }

            // If we're in a call, add in some info and help for the code completion call
            if (completeParameters(proposals, request)) {
                return completionResult;
            }

            // Don't do empty-completion for parameters
            // Can't do this yet... requires canFilter() improvement in GSF such that
            // I don't just filter this empty result on the next iteration
            //if (inCall && proposals.size() > 0 && prefix.length() == 0) {
            //    return proposals;
            //}

            if (root == null) {
                completeKeywords(proposals, request);
                return completionResult;
            }

            // Try to complete "new" RHS
            if (completeNew(proposals, request)) {
                return completionResult;
            }

            if (call.getLhs() != null || request.call.getPrevCallParenPos() != -1) {
                completeObjectMethod(proposals, request);
                return completionResult;
            }

            completeKeywords(proposals, request);

            addLocals(proposals, request);

            if (completeObjectMethod(proposals, request)) {
                return completionResult;
            }

            // Try to complete methods
            if (prefix.length() == 0 && !request.inCall) {
                // We already know that searching with an empty prefix will
                // give too many matches. Rather than having completely random
                // stuff show up, show up things starting with "a" to give
                // impression that we're showing top of the list and mark truncated
                request.prefix = "a"; // NOI18N
                completionResult.setTruncated(true);
            }
            if (completeFunctions(proposals, request)) {
                return completionResult;
            }

        } finally {
            doc.readUnlock();
        }

        return completionResult;
    }

    private void addLocals(List<CompletionProposal> proposals, CompletionRequest request) {
        Node node = request.node;
        String prefix = request.prefix;
        QuerySupport.Kind kind = request.kind;
        JsParseResult result = request.result;

        // TODO - find the scope!!!
        VariableVisitor v = result.getVariableVisitor();

        Map<String, List<Node>> localVars = v.getLocalVars(node);
        for (String name : localVars.keySet()) {
            if (((kind == QuerySupport.Kind.EXACT) && prefix.equals(name)) ||
                    ((kind != QuerySupport.Kind.EXACT) && startsWith(name, prefix))) {
                List<Node> nodeList = localVars.get(name);
                if (nodeList != null && nodeList.size() > 0) {
                    AstElement element = AstElement.getElement(request.info, nodeList.get(0));
                    proposals.add(new JsCompletionItem(element, request));
                }
            }
        }


        // Add in "arguments" local variable which is available to all functions
        String ARGUMENTS = "arguments"; // NOI18N
        if (startsWith(ARGUMENTS, prefix)) {
            // Make sure we're in a function before adding the arguments property
            for (Node n = node; n != null; n = n.getParentNode()) {
                if (n.getType() == org.mozilla.nb.javascript.Token.FUNCTION) {
                    KeywordElement element = new KeywordElement(ARGUMENTS, ElementKind.VARIABLE);
                    proposals.add(new JsCompletionItem(element, request));
                    break;
                }
            }
        }
    }

    private void completeKeywords(List<CompletionProposal> proposals, CompletionRequest request) {
        // No keywords possible in the RHS of a call (except for "this"?)
        if (request.call.getLhs() != null) {
            return;
        }

        String prefix = request.prefix;

//        // Keywords
//        if (prefix.equals("$")) {
//            // Show dollar variable matches (global vars from the user's
//            // code will also be shown
//            for (int i = 0, n = Js_DOLLAR_VARIABLES.length; i < n; i += 2) {
//                String word = Js_DOLLAR_VARIABLES[i];
//                String desc = Js_DOLLAR_VARIABLES[i + 1];
//
//                KeywordItem item = new KeywordItem(word, desc, anchor, request);
//
//                if (isSymbol) {
//                    item.setSymbol(true);
//                }
//
//                proposals.add(item);
//            }
//        }
//
//        for (String keyword : Js_BUILTIN_VARS) {
//            if (startsWith(keyword, prefix)) {
//                KeywordItem item = new KeywordItem(keyword, null, anchor, request);
//
//                if (isSymbol) {
//                    item.setSymbol(true);
//                }
//
//                proposals.add(item);
//            }
//        }

        String[] keywords = request.inCall ? JsUtils.CALL_KEYWORDS : JsUtils.JAVASCRIPT_KEYWORDS;

        for (String keyword : keywords) {
            if (startsWith(keyword, prefix)) {
                KeywordItem item = new KeywordItem(keyword, null, request);

                proposals.add(item);
            }
        }

        if (!request.inCall) {
            for (String keyword : JsUtils.JAVASCRIPT_RESERVED_WORDS) {
                if (startsWith(keyword, prefix)) {
                    KeywordItem item = new KeywordItem(keyword, null, request);

                    proposals.add(item);
                }
            }
        }
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private boolean completeRegexps(List<CompletionProposal> proposals, CompletionRequest request) {
        String prefix = request.prefix;

        // Regular expression matching.  {
        for (int i = 0, n = REGEXP_WORDS.length; i < n; i += 2) {
            String word = REGEXP_WORDS[i];
            String desc = REGEXP_WORDS[i + 1];

            if (startsWith(word, prefix)) {
                KeywordItem item = new KeywordItem(word, desc, request);
                proposals.add(item);
            }
        }

        return true;
    }

    private boolean completeComments(List<CompletionProposal> proposals, CompletionRequest request) throws BadLocationException {
        String prefix = request.prefix;

        BaseDocument doc = request.doc;
        int rowStart = Utilities.getRowFirstNonWhite(doc, request.lexOffset);
        if (rowStart == -1) {
            return false;
        }
        String line = doc.getText(rowStart, Utilities.getRowEnd(doc, request.lexOffset) - rowStart);
        int delta = request.lexOffset - rowStart;

        int i = delta - 1;
        for (; i >= 0; i--) {
            char c = line.charAt(i);
            if (Character.isWhitespace(c) || (!Character.isLetterOrDigit(c) && c != '@' && c != '.' && c != '_')) {
                break;
            }
        }
        i++;
        prefix = line.substring(i, delta);
        request.anchor = rowStart + i;

        // Regular expression matching.  {
        for (int j = 0, n = JSDOC_WORDS.length; j < n; j++) {
            String word = JSDOC_WORDS[j];
            if (startsWith(word, prefix)) {
                //KeywordItem item = new KeywordItem(word, desc, request);
                KeywordItem item = new KeywordItem(word, null, request);
                proposals.add(item);
            }
        }

        return true;
    }

    private boolean completeStrings(List<CompletionProposal> proposals, CompletionRequest request) {
        String prefix = request.prefix;

        // See if we're in prototype js functions, $() and $F(), and if so,
        // offer to complete the function ids
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(request.doc, request.lexOffset);
        assert ts != null; // or we wouldn't have been called in the first place
        //Token<? extends JsTokenId> stringToken = ts.token();
        int stringOffset = ts.offset();

        tokenLoop:
        while (ts.movePrevious()) {
            Token<? extends JsTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == JsTokenId.IDENTIFIER) {
                String text = token.text().toString();

                if (text.startsWith("$") || text.equals("getElementById") || // NOI18N
                        text.startsWith("getElementsByTagName") || text.equals("getElementsByName") || // NOI18N
                        "addClass".equals(text) || "toggleClass".equals(text)) { // NOI18N

                    // Compute a custom prefix
                    int lexOffset = request.lexOffset;
                    if (lexOffset > stringOffset) {
                        try {
                            prefix = request.doc.getText(stringOffset, lexOffset - stringOffset);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        prefix = "";
                    }
                    // Update anchor
                    request.anchor = stringOffset;

                    boolean jQuery = false;
                    if (text.equals("$")) {
                        for (String imp : request.result.getStructure().getImports()) {
                            if (imp.indexOf("jquery") != -1) { // NOI18N
                                jQuery = true;
                            }
                        }
                        if (!jQuery) {
                            jQuery = request.index.getType("jQuery") != null;
                        }
                    }

                    if ("getElementById".equals(text) || (!jQuery && ("$".equals(text) || "$F".equals(text)))) { // NOI18N
                        addElementIds(proposals, request, prefix);

                    } else if ("getElementsByName".equals(text)) { // NOI18N
                        addElementClasses(proposals, request, prefix);
                    } else if ("addClass".equals(text) || "toggleClass".equals(text)) { // NOI18N
                        // From jQuery
                        addElementClasses(proposals, request, prefix);
                    } else if (text.startsWith("getElementsByTagName")) { // NOI18N
                        addTagNames(proposals, request, prefix);
                    } else if ("$$".equals(text) || (jQuery && "$".equals(text) && jQuery)) { // NOI18N
                        // Selectors
                        // Determine whether we want to include elements or classes
                        // Classes after [ and .

                        int showClasses = 1;
                        int showElements = 2;
                        int showIds = 3;
                        int showSpecial = 4;
                        int expect = showElements;
                        int i = prefix.length() - 1;
                        findEnd:
                        for (; i >= 0; i--) {
                            char c = prefix.charAt(i);
                            switch (c) {
                                case '.':
                                case '[':
                                    expect = showClasses;
                                    break findEnd;
                                case '#':
                                    expect = showIds;
                                    break findEnd;
                                case ':':
                                    expect = showSpecial;
                                    if (i > 0 && prefix.charAt(i - 1) == ':') {
                                        // Handle ::'s
                                        i--;
                                    }
                                    break findEnd;
                                case ' ':
                                case '/':
                                case '>':
                                case '+':
                                case '~':
                                case ',':
                                    expect = showElements;
                                    break findEnd;
                                default:
                                    if (!Character.isLetter(c)) {
                                        expect = showElements;
                                        break findEnd;
                                    }
                            }
                        }
                        if (i >= 0) {
                            prefix = prefix.substring(i + 1);
                        }
                        // Update anchor
                        request.anchor = stringOffset + i + 1;

                        if (expect == showElements) {
                            addTagNames(proposals, request, prefix);
                        } else if (expect == showIds) {
                            addElementIds(proposals, request, prefix);
                        } else if (expect == showSpecial) {
                            // Regular expression matching.  {
                            for (int j = 0, n = CSS_WORDS.length; j < n; j += 2) {
                                String word = CSS_WORDS[j];
                                String desc = CSS_WORDS[j + 1];
                                if (word.startsWith(":") && prefix.length() == 0) {
                                    // Filter out the double words
                                    continue;
                                }
                                if (startsWith(word, prefix)) {
                                    if (word.startsWith(":")) { // NOI18N
                                        word = word.substring(1);
                                    }
                                    //KeywordItem item = new KeywordItem(word, desc, request);
                                    GenericItem item = new GenericItem(word, desc, request, ElementKind.RULE);
                                    proposals.add(item);
                                }
                            }
                        } else {
                            assert expect == showClasses;
                            addElementClasses(proposals, request, prefix);
                        }
                    }

                    return true;
                }

                break tokenLoop;
            } else if (id == JsTokenId.STRING_BEGIN) {
                stringOffset = ts.offset() + token.length();
            } else if (!(id == JsTokenId.WHITESPACE ||
                    id == JsTokenId.STRING_LITERAL || id == JsTokenId.LPAREN)) {
                break tokenLoop;
            }
        }

        for (int i = 0, n = STRING_ESCAPES.length; i < n; i += 2) {
            String word = STRING_ESCAPES[i];
            String desc = STRING_ESCAPES[i + 1];

            if (startsWith(word, prefix)) {
                KeywordItem item = new KeywordItem(word, desc, request);
                proposals.add(item);
            }
        }

        return true;
    }

    private void addElementClasses(final List<CompletionProposal> proposals, final CompletionRequest request, final String prefix) {
        Source source = request.info.getSnapshot().getSource();
        if (source.getMimeType().equals(JsUtils.HTML_MIME_TYPE)) {
            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {

                    public
                    @Override
                    void run(ResultIterator resultIterator) throws Exception {
                        HtmlParserResult htmlResult = (HtmlParserResult) resultIterator.getParserResult();
                        List<SyntaxElement> elementsList = htmlResult.elementsList();
                        Set<String> classes = new HashSet<String>();
                        for (SyntaxElement s : elementsList) {
                            if (s.type() == SyntaxElement.TYPE_TAG) {
                                String element = s.text().toString();
                                int classIdx = element.indexOf("class=\""); // NOI18N
                                if (classIdx != -1) {
                                    int classIdxEnd = element.indexOf('"', classIdx + 7);
                                    if (classIdxEnd != -1 && classIdxEnd > classIdx + 1) {
                                        String clz = element.substring(classIdx + 7, classIdxEnd);
                                        classes.add(clz);
                                    }
                                }
                            }
                        }

                        String filename = request.fileObject.getNameExt();
                        for (String tag : classes) {
                            if (startsWith(tag, prefix)) {
                                GenericItem item = new GenericItem(tag, filename, request, ElementKind.TAG);
                                proposals.add(item);
                            }
                        }
                    }
                });
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
    }

    private void addTagNames(final List<CompletionProposal> proposals, final CompletionRequest request, final String prefix) {
        Source source = request.info.getSnapshot().getSource();
        if (source.getMimeType().equals(JsUtils.HTML_MIME_TYPE)) {
            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {

                    public
                    @Override
                    void run(ResultIterator resultIterator) throws Exception {
                        HtmlParserResult htmlResult = (HtmlParserResult) resultIterator.getParserResult();
                        List<SyntaxElement> elementsList = htmlResult.elementsList();
                        Set<String> tagNames = new HashSet<String>();
                        for (SyntaxElement s : elementsList) {
                            if (s.type() == SyntaxElement.TYPE_TAG) {
                                String element = s.text().toString();
                                int start = 1;
                                int end = element.indexOf(' ');
                                if (end == -1) {
                                    end = element.length() - 1;
                                }
                                String tag = element.substring(start, end);
                                tagNames.add(tag);
                            }
                        }

                        String filename = request.fileObject.getNameExt();

                        for (String tag : tagNames) {
                            if (startsWith(tag, prefix)) {
                                GenericItem item = new GenericItem(tag, filename, request, ElementKind.TAG);
                                proposals.add(item);
                            }
                        }
                    }
                });
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
    }

    private void addElementIds(final List<CompletionProposal> proposals, final CompletionRequest request, final String prefix) {
        Source source = request.info.getSnapshot().getSource();
        if (source.getMimeType().equals(JsUtils.HTML_MIME_TYPE)) {
            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {

                    public
                    @Override
                    void run(ResultIterator resultIterator) throws Exception {
                        HtmlParserResult htmlResult = (HtmlParserResult) resultIterator.getParserResult();
                        Set<SyntaxElement.TagAttribute> elementIds = new HashSet<SyntaxElement.TagAttribute>(htmlResult.elementsList().size() / 10);
                        for (SyntaxElement element : htmlResult.elementsList()) {
                            if (element.type() == SyntaxElement.TYPE_TAG) {
                                SyntaxElement.TagAttribute attr = ((SyntaxElement.Tag) element).getAttribute("id"); //NOI18N
                                if (attr != null) {
                                    elementIds.add(attr);
                                }
                            }
                        }
                        String filename = request.fileObject.getNameExt();
                        for (SyntaxElement.TagAttribute tag : elementIds) {
                            String elementId = tag.getValue();
                            // Strip "'s surrounding value, if any
                            if (elementId.length() > 2 && elementId.startsWith("\"") && // NOI18N
                                    elementId.endsWith("\"")) { // NOI18N
                                elementId = elementId.substring(1, elementId.length() - 1);
                            }

                            System.out.println("~~~ elementId: '" + elementId + "'");
                            if (startsWith(elementId, prefix)) {
                                GenericItem item = new GenericItem(elementId, filename, request, ElementKind.TAG);
                                proposals.add(item);
                            }
                        }
                    }
                });
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
    }

    /**
     * Compute an appropriate prefix to use for code completion.
     * In Strings, we want to return the -whole- string if you're in a
     * require-statement string, otherwise we want to return simply "" or the previous "\"
     * for quoted strings, and ditto for regular expressions.
     * For non-string contexts, just return null to let the default identifier-computation
     * kick in.
     */
    public String getPrefix(ParserResult info, int lexOffset, boolean upToOffset) {
        try {
            BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return null;
            }


            TokenHierarchy<Document> th = TokenHierarchy.get((Document) doc);
            doc.readLock(); // Read-lock due to token hierarchy use
            try {
//            int requireStart = LexUtilities.getRequireStringOffset(lexOffset, th);
//
//            if (requireStart != -1) {
//                // XXX todo - do upToOffset
//                return doc.getText(requireStart, lexOffset - requireStart);
//            }

                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, lexOffset);

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

                Token<? extends JsTokenId> token = ts.token();

                if (token != null) {
                    TokenId id = token.id();


                    if (id == JsTokenId.STRING_BEGIN || id == JsTokenId.STRING_END ||
                            id == JsTokenId.STRING_LITERAL || id == JsTokenId.REGEXP_LITERAL ||
                            id == JsTokenId.REGEXP_BEGIN || id == JsTokenId.REGEXP_END) {
                        if (lexOffset > 0) {
                            char prevChar = doc.getText(lexOffset - 1, 1).charAt(0);
                            if (prevChar == '\\') {
                                return "\\";
                            }
                            return "";
                        }
                    }
//                        
//                // We're within a String that has embedded Js. Drop into the
//                // embedded language and see if we're within a literal string there.
//                if (id == JsTokenId.EMBEDDED_RUBY) {
//                    ts = (TokenSequence)ts.embedded();
//                    assert ts != null;
//                    ts.move(lexOffset);
//
//                    if (!ts.moveNext() && !ts.movePrevious()) {
//                        return null;
//                    }
//
//                    token = ts.token();
//                    id = token.id();
//                }
//
//                String tokenText = token.text().toString();
//
//                if ((id == JsTokenId.STRING_BEGIN) || (id == JsTokenId.QUOTED_STRING_BEGIN) ||
//                        ((id == JsTokenId.ERROR) && tokenText.equals("%"))) {
//                    int currOffset = ts.offset();
//
//                    // Percent completion
//                    if ((currOffset == (lexOffset - 1)) && (tokenText.length() > 0) &&
//                            (tokenText.charAt(0) == '%')) {
//                        return "%";
//                    }
//                }
//            }
//
//            int doubleQuotedOffset = LexUtilities.getDoubleQuotedStringOffset(lexOffset, th);
//
//            if (doubleQuotedOffset != -1) {
//                // Tokenize the string and offer the current token portion as the text
//                if (doubleQuotedOffset == lexOffset) {
//                    return "";
//                } else if (doubleQuotedOffset < lexOffset) {
//                    String text = doc.getText(doubleQuotedOffset, lexOffset - doubleQuotedOffset);
//                    TokenHierarchy hi =
//                        TokenHierarchy.create(text, JsStringTokenId.languageDouble());
//
//                    TokenSequence seq = hi.tokenSequence();
//
//                    seq.move(lexOffset - doubleQuotedOffset);
//
//                    if (!seq.moveNext() && !seq.movePrevious()) {
//                        return "";
//                    }
//
//                    TokenId id = seq.token().id();
//                    String s = seq.token().text().toString();
//
//                    if ((id == JsStringTokenId.STRING_ESCAPE) ||
//                            (id == JsStringTokenId.STRING_INVALID)) {
//                        return s;
//                    } else if (s.startsWith("\\")) {
//                        return s;
//                    } else {
//                        return "";
//                    }
//                } else {
//                    // The String offset is greater than the caret position.
//                    // This means that we're inside the string-begin section,
//                    // for example here: %q|(
//                    // In this case, report no prefix
//                    return "";
//                }
//            }
//
//            int singleQuotedOffset = LexUtilities.getSingleQuotedStringOffset(lexOffset, th);
//
//            if (singleQuotedOffset != -1) {
//                if (singleQuotedOffset == lexOffset) {
//                    return "";
//                } else if (singleQuotedOffset < lexOffset) {
//                    String text = doc.getText(singleQuotedOffset, lexOffset - singleQuotedOffset);
//                    TokenHierarchy hi =
//                        TokenHierarchy.create(text, JsStringTokenId.languageSingle());
//
//                    TokenSequence seq = hi.tokenSequence();
//
//                    seq.move(lexOffset - singleQuotedOffset);
//
//                    if (!seq.moveNext() && !seq.movePrevious()) {
//                        return "";
//                    }
//
//                    TokenId id = seq.token().id();
//                    String s = seq.token().text().toString();
//
//                    if ((id == JsStringTokenId.STRING_ESCAPE) ||
//                            (id == JsStringTokenId.STRING_INVALID)) {
//                        return s;
//                    } else if (s.startsWith("\\")) {
//                        return s;
//                    } else {
//                        return "";
//                    }
//                } else {
//                    // The String offset is greater than the caret position.
//                    // This means that we're inside the string-begin section,
//                    // for example here: %q|(
//                    // In this case, report no prefix
//                    return "";
//                }
//            }
//
//            // Regular expression
//            int regexpOffset = LexUtilities.getRegexpOffset(lexOffset, th);
//
//            if ((regexpOffset != -1) && (regexpOffset <= lexOffset)) {
//                // This is not right... I need to actually parse the regexp
//                // (I should use my Regexp lexer tokens which will be embedded here)
//                // such that escaping sequences (/\\\\\/) will work right, or
//                // character classes (/[foo\]). In both cases the \ may not mean escape.
//                String tokenText = token.text().toString();
//                int index = lexOffset - ts.offset();
//
//                if ((index > 0) && (index <= tokenText.length()) &&
//                        (tokenText.charAt(index - 1) == '\\')) {
//                    return "\\";
//                } else {
//                    // No prefix for regexps unless it's \
//                    return "";
//                }
//
//                //return doc.getText(regexpOffset, offset-regexpOffset);
//            }
                }

                int lineBegin = Utilities.getRowStart(doc, lexOffset);
                if (lineBegin != -1) {
                    int lineEnd = Utilities.getRowEnd(doc, lexOffset);
                    String line = doc.getText(lineBegin, lineEnd - lineBegin);
                    int lineOffset = lexOffset - lineBegin;
                    int start = lineOffset;
                    if (lineOffset > 0) {
                        for (int i = lineOffset - 1; i >= 0; i--) {
                            char c = line.charAt(i);
                            if (!JsUtils.isIdentifierChar(c)) {
                                break;
                            } else {
                                start = i;
                            }
                        }
                    }

                    // Find identifier end
                    String prefix;
                    if (upToOffset) {
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
                                if (!JsUtils.isStrictIdentifierChar(d)) {
                                    break;
                                } else {
                                    end = j + 1;
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

                        // The identifier chars identified by JsLanguage are a bit too permissive;
                        // they include things like "=", "!" and even "&" such that double-clicks will
                        // pick up the whole "token" the user is after. But "=" is only allowed at the
                        // end of identifiers for example.
                        if (prefix.length() == 1) {
                            char c = prefix.charAt(0);
                            if (!(Character.isJavaIdentifierPart(c) || c == '@' || c == '$' || c == ':')) {
                                return null;
                            }
                        } else {
                            for (int i = prefix.length() - 2; i >= 0; i--) { // -2: the last position (-1) can legally be =, ! or ?
                                char c = prefix.charAt(i);
                                if (i == 0 && c == ':') {
                                    // : is okay at the begining of prefixes
                                } else if (!(Character.isJavaIdentifierPart(c) || c == '@' || c == '$')) {
                                    prefix = prefix.substring(i + 1);
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

    private boolean completeFunctions(List<CompletionProposal> proposals, CompletionRequest request) {
        JsIndex index = request.index;
        String prefix = request.prefix;
        TokenHierarchy<Document> th = request.th;
        QuerySupport.Kind kind = request.kind;
        String fqn = request.fqn;
        JsParseResult result = request.result;

        boolean includeNonFqn = !request.inCall;

        Set<IndexedElement> matches;
        if (fqn != null) {
            matches = index.getElements(prefix, fqn, kind, result);
        } else {
            Pair<Set<IndexedElement>, Boolean> names = index.getAllNamesTruncated(prefix, kind, result);
            matches = names.getA();
            boolean isTruncated = names.getB();
            if (isTruncated) {
                request.completionResult.setTruncated(true);
                includeNonFqn = false;
            }
        }
        // Also add in non-fqn-prefixed elements
        if (includeNonFqn) {
            Set<IndexedElement> top = index.getElements(prefix, null, kind, result);
            if (top.size() > 0) {
                matches.addAll(top);
            }
        }

        for (IndexedElement element : matches) {
            if (element.isNoDoc()) {
                continue;
            }

            JsCompletionItem item;
            if (element instanceof IndexedFunction) {
                item = new FunctionItem((IndexedFunction) element, request);
            } else {
                item = new JsCompletionItem(request, element);
            }
            proposals.add(item);

        }

        return true;
    }

    /** Determine if we're trying to complete the name of a method on another object rather
     * than an inherited or local one. These should list ALL known methods, unless of course
     * we know the type of the method we're operating on (such as strings or regexps),
     * or types inferred through data flow analysis
     *
     * @todo Look for self or this or super; these should be limited to inherited.
     */
    private boolean completeObjectMethod(List<CompletionProposal> proposals, CompletionRequest request) {

        JsIndex index = request.index;
        String prefix = request.prefix;
        int astOffset = request.astOffset;
        int lexOffset = request.lexOffset;
        Node root = request.root;
        TokenHierarchy<Document> th = request.th;
        AstPath path = request.path;
        QuerySupport.Kind kind = request.kind;
        Node node = request.node;
        JsParseResult result = request.result;
        JsParseResult info = request.info;

        String fqn = request.fqn;
        Call call = request.call;

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, lexOffset);

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

//            boolean skipInstanceMethods = call.isStatic();

            Set<IndexedElement> elements = Collections.emptySet();

            String type = call.getType();
            String lhs = call.getLhs();

            if (type == null) {
                Node method = AstUtilities.findLocalScope(node, path);
                if (method != null) {
                    List<Node> nodes = new ArrayList<Node>();
                    AstUtilities.addNodesByType(method, new int[]{org.mozilla.nb.javascript.Token.MISSING_DOT}, nodes);
                    if (nodes.size() > 0) {
                        Node exprNode = nodes.get(0);
                        JsTypeAnalyzer analyzer = new JsTypeAnalyzer(info, /*request.info.getParserResult(),*/ index, method, node, astOffset, lexOffset);
                        type = analyzer.getType(exprNode.getParentNode());
                    }
                }
            }

            if (type == null && call.getPrevCallParenPos() != -1) {
                // It's some sort of call
                assert call.getType() == null;
                assert call.getLhs() == null;

                // Try to figure out the call in question
                int callEndAstOffset = AstUtilities.getAstOffset(info, call.getPrevCallParenPos());
                if (callEndAstOffset != -1) {
                    AstPath callPath = new AstPath(root, callEndAstOffset);
                    Iterator<Node> it = callPath.leafToRoot();
                    while (it.hasNext()) {
                        Node callNode = it.next();
                        if (callNode.getType() == org.mozilla.nb.javascript.Token.FUNCTION) {
                            break;
                        } else if (callNode.getType() == org.mozilla.nb.javascript.Token.CALL) {
                            Node method = AstUtilities.findLocalScope(node, path);

                            if (method != null) {
                                JsTypeAnalyzer analyzer = new JsTypeAnalyzer(info, /*request.info.getParserResult(),*/ index, method, node, astOffset, lexOffset);
                                type = analyzer.getType(callNode);
                            }
                            break;
                        } else if (callNode.getType() == org.mozilla.nb.javascript.Token.GETELEM) {
                            Node method = AstUtilities.findLocalScope(node, path);

                            if (method != null) {
                                JsTypeAnalyzer analyzer = new JsTypeAnalyzer(info, /*request.info.getParserResult(),*/ index, method, node, astOffset, lexOffset);
                                type = analyzer.getType(callNode);
                            }
                            break;
                        }
                    }
                }
            } else if (type == null && lhs != null && node != null) {
                Node method = AstUtilities.findLocalScope(node, path);

                if (method != null) {
                    JsTypeAnalyzer analyzer = new JsTypeAnalyzer(info, /*request.info.getParserResult(),*/ index, method, node, astOffset, lexOffset);
                    type = analyzer.getType(node);
                }
            }

            if ((type == null) && (lhs != null) && (node != null) && call.isSimpleIdentifier()) {
                Node method = AstUtilities.findLocalScope(node, path);

                if (method != null) {
                    // TODO - if the lhs is "foo.bar." I need to split this
                    // up and do it a bit more cleverly
                    JsTypeAnalyzer analyzer = new JsTypeAnalyzer(info, /*request.info.getParserResult(),*/ index, method, node, astOffset, lexOffset);
                    type = analyzer.getType(lhs);
                }
            }

            // E4X
            if (type != null && type.startsWith("XML<") && type.endsWith(">")) { // NOI18N
                // Extract XML
                String xml = type.substring(4, type.length() - 1);
                addXmlItems(xml, proposals, request);
                return true;
            }

            // I'm not doing any data flow analysis at this point, so
            // I can't do anything with a LHS like "foo.". Only actual types.
            if ((type != null) && (type.length() > 0)) {
                if ("this".equals(lhs)) {
                    type = fqn;
                    skipPrivate = false;
//                } else if ("super".equals(lhs)) {
//                    skipPrivate = false;
//
//                    IndexedClass sc = index.getSuperclass(fqn);
//
//                    if (sc != null) {
//                        type = sc.getFqn();
//                    } else {
//                        ClassNode cls = AstUtilities.findClass(path);
//
//                        if (cls != null) {
//                            type = AstUtilities.getSuperclass(cls);
//                        }
//                    }
//
//                    if (type == null) {
//                        type = "Object"; // NOI18N
//                    }
                }

                if ((type != null) && (type.length() > 0)) {
                    // Possibly a class on the left hand side: try searching with the class as a qualifier.
                    // Try with the LHS + current FQN recursively. E.g. if we're in
                    // Test::Unit when there's a call to Foo.x, we'll try
                    // Test::Unit::Foo, and Test::Foo
                    while (elements.size() == 0 && fqn != null && !fqn.equals(type)) {
                        elements = index.getElements(prefix, fqn + "." + type, kind, result);

                        int f = fqn.lastIndexOf("::");

                        if (f == -1) {
                            break;
                        } else {
                            fqn = fqn.substring(0, f);
                        }
                    }

                    // Add methods in the class (without an FQN)
                    Set<IndexedElement> m = index.getElements(prefix, type, kind, result);

                    if (m.size() > 0) {
                        elements = m;
                    }
                }
            } else if (lhs != null && lhs.length() > 0) {
                // No type but an LHS - perhaps it's a type?
                Set<IndexedElement> m = index.getElements(prefix, lhs, kind, result);

                if (m.size() > 0) {
                    elements = m;
                }
            }

            // Try just the method call (e.g. across all classes). This is ignoring the 
            // left hand side because we can't resolve it.
            if ((elements.size() == 0) && (prefix.length() > 0 || type == null)) {
//                if (prefix.length() == 0) {
//                    proposals.clear();
//                    proposals.add(new KeywordItem("", "Type more characters to see matches", request));
//                    return true;
//                } else {
                Pair<Set<IndexedElement>, Boolean> names = index.getAllNamesTruncated(prefix, kind, result);
                elements = names.getA();
                boolean isTruncated = names.getB();
                if (isTruncated) {
                    request.completionResult.setTruncated(true);
                }
//                }
            }

            for (IndexedElement element : elements) {
                if (proposals.size() >= MAX_COMPLETION_ITEMS) {
                    request.completionResult.setTruncated(true);
                    return true;
                }

                // Skip constructors - you don't want to call
                //   x.Foo !
//                if (element.getKind() == ElementKind.CONSTRUCTOR) {
//                    continue;
//                }

                // Don't include private or protected methods on other objects
                if (skipPrivate && element.isPrivate()) {
                    continue;
                }
//
//                // We can only call static methods
//                if (skipInstanceMethods && !method.isStatic()) {
//                    continue;
//                }
//
                if (element.isNoDoc()) {
                    continue;
                }

                if (element instanceof IndexedFunction) {
                    FunctionItem item = new FunctionItem((IndexedFunction) element, request);
                    proposals.add(item);
                } else {
                    JsCompletionItem item = new JsCompletionItem(request, element);
                    proposals.add(item);
                }
            }

            return done;
        }

        return false;
    }

    private void addXmlItems(String xml, List<CompletionProposal> proposals, CompletionRequest request) {
        // Parse
        JsAnalyzer.XmlStructureItem xmlItem = JsAnalyzer.XmlStructureItem.get(xml, 0);

        // Add items for all the XML items
        Set<String> names = new HashSet<String>();
        addXmlItems(xmlItem, names);

        QuerySupport.Kind kind = request.kind;
        String prefix = request.prefix;
        for (String tag : names) {
            if (((kind == QuerySupport.Kind.EXACT) && prefix.equals(tag)) ||
                    ((kind != QuerySupport.Kind.EXACT) && startsWith(tag, prefix))) {
                KeywordItem item = new KeywordItem(tag, null, request);
                item.setKind(ElementKind.TAG);
                proposals.add(item);
            }
        }
    }

    private void addXmlItems(StructureItem item, Set<String> names) {
        names.add(item.getName());
        for (StructureItem child : item.getNestedItems()) {
            addXmlItems(child, names);
        }
    }

    /** Determine if we're trying to complete the name for a "new" (in which case
     * we show available constructors.
     */
    private boolean completeNew(List<CompletionProposal> proposals, CompletionRequest request) {
        JsIndex index = request.index;
        String prefix = request.prefix;
        int lexOffset = request.lexOffset;
        TokenHierarchy<Document> th = request.th;
        QuerySupport.Kind kind = request.kind;

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, lexOffset);

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

            Token<? extends JsTokenId> token = ts.token();

            if (token != null) {
                TokenId id = token.id();

                // See if we're in the identifier - "foo" in "def foo"
                // I could also be a keyword in case the prefix happens to currently
                // match a keyword, such as "next"
                if ((id == JsTokenId.IDENTIFIER) || id.primaryCategory().equals(JsLexer.KEYWORD_CAT)) {
                    if (!ts.movePrevious()) {
                        return false;
                    }

                    token = ts.token();
                    id = token.id();
                }

                // If we're not in the identifier we need to be in the whitespace after "def"
                if (id != JsTokenId.WHITESPACE && id != JsTokenId.EOL) {
                    // Do something about http://www.netbeans.org/issues/show_bug.cgi?id=100452 here
                    // In addition to checking for whitespace I should look for "Foo." here
                    return false;
                }

                // There may be more than one whitespace; skip them
                while (ts.movePrevious()) {
                    token = ts.token();

                    if (token.id() != JsTokenId.WHITESPACE) {
                        break;
                    }
                }

                if (token.id() == JsTokenId.NEW) {
                    Pair<Set<IndexedElement>, Boolean> constructors = index.getConstructors(prefix, kind);
                    Set<IndexedElement> elements = constructors.getA();
                    if (constructors.getB()) {
                        request.completionResult.setTruncated(true);
                    }
                    String lhs = request.call.getLhs();
                    if (lhs != null && lhs.length() > 0) {
                        Set<IndexedElement> m = index.getElements(prefix, lhs, kind, null);
                        if (m.size() > 0) {
                            if (elements.size() == 0) {
                                elements = new HashSet<IndexedElement>();
                            }
                            for (IndexedElement f : m) {
                                if (f.getKind() == ElementKind.CONSTRUCTOR || f.getKind() == ElementKind.PACKAGE) {
                                    elements.add(f);
                                }
                            }
                        }
                    } else if (prefix.length() > 0) {
                        Set<IndexedElement> m = index.getElements(prefix, null, kind, null);
                        if (m.size() > 0) {
                            if (elements.size() == 0) {
                                elements = new HashSet<IndexedElement>();
                            }
                            for (IndexedElement f : m) {
                                if (f.getKind() == ElementKind.CONSTRUCTOR || f.getKind() == ElementKind.PACKAGE) {
                                    elements.add(f);
                                }
                            }
                        }
                    }

                    for (IndexedElement element : elements) {
                        if (proposals.size() >= MAX_COMPLETION_ITEMS) {
                            request.completionResult.setTruncated(true);
                            return true;
                        }

                        // Hmmm, is this necessary? Filtering should happen in the getInheritedMEthods call
                        if ((prefix.length() > 0) && !element.getName().startsWith(prefix)) {
                            continue;
                        }

                        if (element.isNoDoc()) {
                            continue;
                        }

//                        // For def completion, skip local methods, only include superclass and included
//                        if ((fqn != null) && fqn.equals(method.getClz())) {
//                            continue;
//                        }

                        // If a method is an "initialize" method I should do something special so that
                        // it shows up as a "constructor" (in a new() statement) but not as a directly
                        // callable initialize method (it should already be culled because it's private)
                        JsCompletionItem item;
                        if (element instanceof IndexedFunction) {
                            item = new FunctionItem((IndexedFunction) element, request);
                        } else {
                            item = new JsCompletionItem(request, element);
                        }
                        // Exact matches
//                        item.setSmart(method.isSmart());
                        proposals.add(item);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private boolean completeParameters(List<CompletionProposal> proposals, CompletionRequest request) {
        IndexedFunction[] methodHolder = new IndexedFunction[1];
        @SuppressWarnings("unchecked")
        Set<IndexedFunction>[] alternatesHolder = new Set[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        JsParseResult info = request.info;
        int lexOffset = request.lexOffset;
        int astOffset = request.astOffset;

        if (!computeMethodCall(info, lexOffset, astOffset,
                methodHolder, paramIndexHolder, anchorOffsetHolder, alternatesHolder)) {
            request.inCall = false;

            return false;
        }

        request.inCall = true;

        IndexedFunction targetMethod = methodHolder[0];
        int index = paramIndexHolder[0];

        CallItem callItem = new CallItem(targetMethod, index, request);
        proposals.add(callItem);
        // Also show other documented, not nodoc'ed items (except for those
        // with identical signatures, such as overrides of the same method)
        if (alternatesHolder[0] != null) {
            Set<String> signatures = new HashSet<String>();
            signatures.add(targetMethod.getSignature().substring(targetMethod.getSignature().indexOf('#') + 1));
            for (IndexedFunction m : alternatesHolder[0]) {
                if (m != targetMethod && m.isDocumented() && !m.isNoDoc()) {
                    String sig = m.getSignature().substring(m.getSignature().indexOf('#') + 1);
                    if (!signatures.contains(sig)) {
                        CallItem item = new CallItem(m, index, request);
                        proposals.add(item);
                        signatures.add(sig);
                    }
                }
            }
        }

        List<String> params = targetMethod.getParameters();
        if (params == null || params.size() == 0) {
            return false;
        }

        if (params.size() <= index) {
            // Just use the last parameter in these cases
            // See for example the TableDefinition.binary dynamic method where
            // you can add a number of parameter names and the options parameter
            // is always the last one
            index = params.size() - 1;
        }

        // Add in inherited properties, if any...
        // Look for properties on the object - as well as inherited properties. NOT methods!!!
        // Also look for @cfg and @config properties. This is a bit tricky. In the case of Ext,
        // we have these guys on the class itself, not associated with a method parameter.
        // Shall I take this to be a set of constructor properties?
        // In YUI it's different; many of the properties we want to inherit are NOT marked as @config,
        // such as "animate" in the Editor. 
        String fqn = null;
        AstPath path = request.path;
        Node leaf = path.leaf();
        int leafType = leaf.getType();
        if (leafType == org.mozilla.nb.javascript.Token.OBJECTLIT || leafType == org.mozilla.nb.javascript.Token.OBJLITNAME) {
            if (leafType == org.mozilla.nb.javascript.Token.OBJLITNAME) {
                leaf = leaf.getParentNode(); // leaf still won't be null, OBJLITNAME is always below an OBJECTLIT
            }
            // We're trying to complete object literal names. These should be properties we're
            // expecting.

            // (1) See if we're in a constructor argument, and if so, look for configuration objects
            // on the function and the class, and if not:
            // (2) Assume that we're customizing the class we're surrounding so use that as the type.

            Node parent = leaf.getParentNode();
            int parentType = parent.getType();
            if (parentType == org.mozilla.nb.javascript.Token.CALL ||
                    parentType == org.mozilla.nb.javascript.Token.NEW) {

                int last = params.size() - 1;
                if (index > last) {
                    index = last;
                }

                if (index >= 0) {
                    String param = params.get(index);
                    int typeIdx = param.indexOf(':');
                    if (typeIdx != -1) {
                        String type = param.substring(typeIdx + 1);
                        param = param.substring(0, typeIdx);
                        fqn = type;
                    }

                    // See if we have @config options for this in its documentation?
                    if ((fqn == null || "Object".equals(fqn)) && targetMethod.isDocumented()) { // NOI18N
                        String prefix = request.prefix;
                        boolean foundConfig = false;
                        List<String> comments = ElementUtilities.getComments(info, targetMethod);
                        if (comments != null && comments.size() > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (String line : comments) {
                                sb.append(line);
                                sb.append("\n"); // NOI18N
                            }
                            sb.setLength(sb.length() - 1);
                            TokenHierarchy<?> hi = TokenHierarchy.create(sb.toString(), JsCommentTokenId.language());
                            TokenSequence<JsCommentTokenId> ts = hi.tokenSequence(JsCommentTokenId.language());
                            String currentParameter = null;
                            // Look for @config tags
                            while (ts != null && ts.moveNext()) {
                                Token<? extends JsCommentTokenId> token = ts.token();
                                TokenId id = token.id();
                                if (id == JsCommentTokenId.COMMENT_TAG) {
                                    CharSequence text = token.text();
                                    if (TokenUtilities.textEquals("@param", text) || // NOI18N
                                            TokenUtilities.textEquals("@argument", text)) { // NOI18N
                                        int tsidx = ts.index();
                                        String paramType = JsCommentLexer.nextType(ts);
                                        if (paramType == null) {
                                            ts.moveIndex(tsidx);
                                            ts.moveNext();
                                        }
                                        String paramName = JsCommentLexer.nextIdent(ts);
                                        if (paramName != null) {
                                            currentParameter = paramName;
                                        } else {
                                            ts.moveIndex(tsidx);
                                            ts.moveNext();
                                        }
                                    } else if (TokenUtilities.textEquals("@config", text) || // NOI18N
                                            TokenUtilities.textEquals("@cfg", text)) { // NOI18N
                                        int tsidx = ts.index();
                                        String configType = JsCommentLexer.nextType(ts);
                                        if (configType == null) {
                                            ts.moveIndex(tsidx);
                                            ts.moveNext();
                                        }
                                        String configName = JsCommentLexer.nextIdent(ts);
                                        if (configName != null && (currentParameter == null || currentParameter.equals(param))) {
                                            // Compute the rest of the description of the config, if applicable
                                            int i2 = ts.index();
                                            //StringBuilder longDesc = new StringBuilder();
                                            StringBuilder shortDesc = new StringBuilder();
                                            boolean truncated = false;
                                            while (ts.moveNext()) {
                                                text = ts.token().text();
                                                if (text.length() > 0 && text.charAt(0) == '@' && ts.token().id() == JsCommentTokenId.COMMENT_TAG) {
                                                    break;
                                                } else {
                                                    if (!truncated) {
                                                        shortDesc.append(text);
                                                        int MAX = 40;
                                                        if (shortDesc.length() > MAX) {
                                                            shortDesc.setLength(MAX - 3);
                                                            shortDesc.append("...");
                                                            truncated = true;
                                                            break;
                                                        }
                                                    }
                                                    //longDesc.append(text);
                                                }
                                            }
                                            String rhs = shortDesc.toString().trim();
                                            if (configType != null) {
                                                if (rhs.length() > 0) {
                                                    rhs = "{" + configType + "} " + rhs;
                                                } else {
                                                    rhs = configType;
                                                }
                                            }

                                            ts.moveIndex(i2);
                                            if (startsWith(configName, prefix)) {
                                                GenericItem item = new GenericItem(configName, rhs, request, ElementKind.PARAMETER);
                                                item.element = targetMethod;
                                                //String desc = longDesc.toString().trim();
                                                //if (desc.length() > 0) {
                                                //    item.setLongDescription(desc);
                                                //}
                                                proposals.add(item);
                                                foundConfig = true;
                                            }
                                        } else {
                                            ts.moveIndex(tsidx);
                                            ts.moveNext();
                                        }
                                    }
                                }
                            }
                        }

                        if (foundConfig) {
                            return true;
                        }
                    }
                }

                if (targetMethod.getKind() == ElementKind.CONSTRUCTOR && (fqn == null || "Object".equals(fqn))) { // NOI18N
                    if (!Character.isUpperCase(targetMethod.getName().charAt(0)) && targetMethod.getIn().length() > 0) {
                        fqn = targetMethod.getIn();
                    } else {
                        fqn = targetMethod.getFqn();
                    }
                }

                String prefix = request.prefix;
                QuerySupport.Kind kind = request.kind;
                JsParseResult result = request.result;
                Set<IndexedElement> matches = request.index.getElements(prefix, fqn, kind, result);
                boolean found = false;

                for (IndexedElement element : matches) {
                    if (element.isNoDoc()) {
                        continue;
                    }

                    if (element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.CONSTRUCTOR) {
                        continue;
                    }

                    // Skip constants
                    String name = element.getName();
                    if (Character.isUpperCase(name.charAt(0))) {
                        continue;
                    }

                    // Skip private fields
                    // (Not sure about this)
                    if (element.isPrivate()) {
                        continue;
                    }

                    JsCompletionItem item;
                    if (element instanceof IndexedFunction) {
                        item = new FunctionItem((IndexedFunction) element, request);
                    } else {
                        item = new JsCompletionItem(request, element);
                    }
                    found = true;
                    proposals.add(item);
                }

                if (found) {
                    return true;
                }
            }
        }

        return false;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);

        // TODO - auto query on ' and " when you're in $() or $F()

        if (c == '\n' || c == '(' || c == '[' || c == '{' || c == ';') {
            return QueryType.STOP;
        }

        if (c != '.'/* && c != ':'*/) {
            return QueryType.NONE;
        }

        int offset = component.getCaretPosition();
        BaseDocument doc = (BaseDocument) component.getDocument();

        if (".".equals(typedText)) { // NOI18N
            // See if we're in Js context
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);
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
            Token<? extends JsTokenId> token = ts.token();
            TokenId id = token.id();

//            // ".." is a range, not dot completion
//            if (id == JsTokenId.RANGE) {
//                return QueryType.NONE;
//            }

            // TODO - handle embedded JavaScript
            if (JsLexer.COMMENT_CAT.equals(id.primaryCategory()) || // NOI18N
                    JsLexer.STRING_CAT.equals(id.primaryCategory()) || // NOI18N
                    JsLexer.REGEXP_CAT.equals(id.primaryCategory())) { // NOI18N
                return QueryType.NONE;
            }

            return QueryType.COMPLETION;
        }

//        if (":".equals(typedText)) { // NOI18N
//            // See if it was "::" and we're in ruby context
//            int dot = component.getSelectionStart();
//            try {
//                if ((dot > 1 && component.getText(dot-2, 1).charAt(0) == ':') && // NOI18N
//                        isJsContext(doc, dot-1)) {
//                    return QueryType.COMPLETION;
//                }
//            } catch (BadLocationException ble) {
//                Exceptions.printStackTrace(ble);
//            }
//        }
//        
        return QueryType.NONE;
    }

    public static boolean isJsContext(BaseDocument doc, int offset) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);

        if (ts == null) {
            return false;
        }

        ts.move(offset);

        if (!ts.movePrevious() && !ts.moveNext()) {
            return true;
        }

        TokenId id = ts.token().id();
        if (JsLexer.COMMENT_CAT.equals(id.primaryCategory()) || JsLexer.STRING_CAT.equals(id.primaryCategory()) || // NOI18N
                JsLexer.REGEXP_CAT.equals(id.primaryCategory())) { // NOI18N
            return false;
        }

        return true;
    }

    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset,
            String name, Map parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String document(ParserResult info, ElementHandle handle) {
        JsParseResult jspr = AstUtilities.getParseResult(info);
        if (jspr == null) {
            return null;
        }
        Element element = ElementUtilities.getElement(jspr, handle);
        if (element == null) {
            return null;
        }
        if (element instanceof IndexedPackage) {
            return null;
        }

        if (element instanceof CommentElement) {
            // Text is packaged as the name
            String comment = element.getName();
            String[] comments = comment.split("\n");
            StringBuilder sb = new StringBuilder();
            for (int i = 0, n = comments.length; i < n; i++) {
                String line = comments[i];
                if (line.startsWith("/**")) {
                    sb.append(line.substring(3));
                } else if (i == n - 1 && line.trim().endsWith("*/")) {
                    sb.append(line.substring(0, line.length() - 2));
                    continue;
                } else if (line.startsWith("//")) {
                    sb.append(line.substring(2));
                } else if (line.startsWith("/*")) {
                    sb.append(line.substring(2));
                } else if (line.startsWith("*")) {
                    sb.append(line.substring(1));
                } else {
                    sb.append(line);
                }
            }
            String html = sb.toString();
            return html;
        } else if (element instanceof KeywordElement) {
            return null; //getKeywordHelp(((KeywordElement)element).getName());
        } else if (element instanceof IndexedElement) {
            IndexedElement ie = (IndexedElement) element;
            if (!ie.isDocumented()) {
                IndexedElement e = ie.findDocumentedSibling();
                if (e != null) {
                    element = e;
                }
            }
        }

        List<String> comments = ElementUtilities.getComments(jspr, element);
        if (comments == null) {
            String html = ElementUtilities.getSignature(element) + "\n<hr>\n<i>" + NbBundle.getMessage(JsCodeCompletion.class, "NoCommentFound") + "</i>";

            return html;
        }

        JsCommentFormatter formatter = new JsCommentFormatter(comments);
        String name = element.getName();
        if (name != null && name.length() > 0) {
            formatter.setSeqName(name);
        }

        String html = formatter.toHtml();
        html = ElementUtilities.getSignature(element) + "\n<hr>\n" + html;
        return html;
    }

    public Set<String> getApplicableTemplates(ParserResult info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    public ParameterInfo parameters(ParserResult info, int lexOffset, CompletionProposal proposal) {
        JsParseResult jspr = AstUtilities.getParseResult(info);
        if (jspr == null) {
            return ParameterInfo.NONE;
        }
        IndexedFunction[] methodHolder = new IndexedFunction[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int astOffset = AstUtilities.getAstOffset(info, lexOffset);
        if (!computeMethodCall(jspr, lexOffset, astOffset,
                methodHolder, paramIndexHolder, anchorOffsetHolder, null)) {

            return ParameterInfo.NONE;
        }

        IndexedFunction method = methodHolder[0];
        if (method == null) {
            return ParameterInfo.NONE;
        }
        int index = paramIndexHolder[0];
        int astAnchorOffset = anchorOffsetHolder[0];
        int anchorOffset = LexUtilities.getLexerOffset(jspr, astAnchorOffset);

        // TODO: Make sure the caret offset is inside the arguments portion
        // (parameter hints shouldn't work on the method call name itself
        // See if we can find the method corresponding to this call
        //        if (proposal != null) {
        //            Element element = proposal.getElement();
        //            if (element instanceof IndexedFunction) {
        //                method = ((IndexedFunction)element);
        //            }
        //        }

        List<String> params = method.getParameters();

        if ((params != null) && (params.size() > 0)) {
            return new ParameterInfo(params, index, anchorOffset);
        }

        return ParameterInfo.NONE;
    }
    private static int callLineStart = -1;
    private static IndexedFunction callMethod;

    /** Compute the current method call at the given offset. Returns false if we're not in a method call. 
     * The argument index is returned in parameterIndexHolder[0] and the method being
     * called in methodHolder[0].
     */
    static boolean computeMethodCall(JsParseResult info, int lexOffset, int astOffset,
            IndexedFunction[] methodHolder, int[] parameterIndexHolder, int[] anchorOffsetHolder,
            Set<IndexedFunction>[] alternativesHolder) {
        try {
            Node root = info.getRootNode();

            if (root == null) {
                return false;
            }

            IndexedFunction targetMethod = null;
            int index = -1;

            AstPath path = null;
            // Account for input sanitation
            // TODO - also back up over whitespace, and if I hit the method
            // I'm parameter number 0
            int originalAstOffset = astOffset;

            // Adjust offset to the left
            BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return false;
            }
            int newLexOffset = LexUtilities.findSpaceBegin(doc, lexOffset);
            if (newLexOffset < lexOffset) {
                astOffset -= (lexOffset - newLexOffset);
            }

            JsParseResult rpr = AstUtilities.getParseResult(info);
            OffsetRange range = rpr.getSanitizedRange();
            if (range != OffsetRange.NONE && range.containsInclusive(astOffset)) {
                if (astOffset != range.getStart()) {
                    astOffset = range.getStart() - 1;
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
//                    if (AstUtilities.isCall(node) &&
//                            name.equals(AstUtilities.getCallName(node))) {
//                        if (node.nodeId == NodeTypes.CALLNODE) {
//                            Node argsNode = ((CallNode)node).getArgsNode();
//
//                            if (argsNode != null) {
//                                index = AstUtilities.findArgumentIndex(argsNode, astOffset);
//
//                                if (index == -1 && astOffset < originalAstOffset) {
//                                    index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
//                                }
//
//                                if (index != -1) {
//                                    call = node;
//                                    anchorOffset = argsNode.getPosition().getStartOffset();
//                                }
//                            }
//                        } else if (node.nodeId == NodeTypes.FCALLNODE) {
//                            Node argsNode = ((FCallNode)node).getArgsNode();
//
//                            if (argsNode != null) {
//                                index = AstUtilities.findArgumentIndex(argsNode, astOffset);
//
//                                if (index == -1 && astOffset < originalAstOffset) {
//                                    index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
//                                }
//
//                                if (index != -1) {
//                                    call = node;
//                                    anchorOffset = argsNode.getPosition().getStartOffset();
//                                }
//                            }
//                        } else if (node.nodeId == NodeTypes.VCALLNODE) {
//                            // We might be completing at the end of a method call
//                            // and we don't have parameters yet so it just looks like
//                            // a vcall, e.g.
//                            //   create_table |
//                            // This is okay as long as the caret is outside and to
//                            // the right of this call. However
//                            final OffsetRange callRange = AstUtilities.getCallRange(node);
//                            AstUtilities.getCallName(node);
//                            if (originalAstOffset > callRange.getEnd()) {
//                                index = 0;
//                                call = node;
//                                anchorOffset = callRange.getEnd()+1;
//                            }
//                        }
//                        
//                        break;
//                    }
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

                    int nodeType = node.getType();
                    if (nodeType == org.mozilla.nb.javascript.Token.FUNCTION) {
                        // See for example issue 149001
                        // If the call is outside the current function scope,
                        // we don't want to include it!
                        break;
                    }

                    if (nodeType == org.mozilla.nb.javascript.Token.CALL ||
                            nodeType == org.mozilla.nb.javascript.Token.NEW) {
                        call = node;
                        index = AstUtilities.findArgumentIndex(call, astOffset, path);
                        break;
                    }
//                    if (node.nodeId == NodeTypes.CALLNODE) {
//                        final OffsetRange callRange = AstUtilities.getCallRange(node);
//                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
//                            for (int i = 0; i < 3; i++) {
//                                // It's not really a peek in the sense
//                                // that there's no reason to retry these
//                                // nodes later
//                                Node peek = it.next();
//                                if (AstUtilities.isCall(peek) &&
//                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(info, peek.getPosition().getStartOffset())) ==
//                                        Utilities.getRowStart(doc, lexOffset)) {
//                                    // Use the outer method call instead
//                                    it.previous();
//                                    continue nodesearch;
//                                }
//                            }
//                        }
//                        
//                        Node argsNode = ((CallNode)node).getArgsNode();
//
//                        if (argsNode != null) {
//                            index = AstUtilities.findArgumentIndex(argsNode, astOffset);
//
//                            if (index == -1 && astOffset < originalAstOffset) {
//                                index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
//                            }
//
//                            if (index != -1) {
//                                call = node;
//                                anchorOffset = argsNode.getPosition().getStartOffset();
//
//                                break;
//                            }
//                        } else {
//                            if (originalAstOffset > callRange.getEnd()) {
//                                index = 0;
//                                call = node;
//                                anchorOffset = callRange.getEnd()+1;
//                                break;
//                            }
//                        }
//                    } else if (node.nodeId == NodeTypes.FCALLNODE) {
//                        final OffsetRange callRange = AstUtilities.getCallRange(node);
//                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
//                            for (int i = 0; i < 3; i++) {
//                                // It's not really a peek in the sense
//                                // that there's no reason to retry these
//                                // nodes later
//                                Node peek = it.next();
//                                if (AstUtilities.isCall(peek) &&
//                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(info, peek.getPosition().getStartOffset())) ==
//                                        Utilities.getRowStart(doc, lexOffset)) {
//                                    // Use the outer method call instead
//                                    it.previous();
//                                    continue nodesearch;
//                                }
//                            }
//                        }
//                        
//                        Node argsNode = ((FCallNode)node).getArgsNode();
//
//                        if (argsNode != null) {
//                            index = AstUtilities.findArgumentIndex(argsNode, astOffset);
//
//                            if (index == -1 && astOffset < originalAstOffset) {
//                                index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
//                            }
//
//                            if (index != -1) {
//                                call = node;
//                                anchorOffset = argsNode.getPosition().getStartOffset();
//
//                                break;
//                            }
//                        }
//                    } else if (node.nodeId == NodeTypes.VCALLNODE) {
//                        // We might be completing at the end of a method call
//                        // and we don't have parameters yet so it just looks like
//                        // a vcall, e.g.
//                        //   create_table |
//                        // This is okay as long as the caret is outside and to
//                        // the right of this call.
//                        
//                        final OffsetRange callRange = AstUtilities.getCallRange(node);
//                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
//                            for (int i = 0; i < 3; i++) {
//                                // It's not really a peek in the sense
//                                // that there's no reason to retry these
//                                // nodes later
//                                Node peek = it.next();
//                                if (AstUtilities.isCall(peek) &&
//                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(info, peek.getPosition().getStartOffset())) ==
//                                        Utilities.getRowStart(doc, lexOffset)) {
//                                    // Use the outer method call instead
//                                    it.previous();
//                                    continue nodesearch;
//                                }
//                            }
//                        }
//                        
//                        if (originalAstOffset > callRange.getEnd()) {
//                            index = 0;
//                            call = node;
//                            anchorOffset = callRange.getEnd()+1;
//                            break;
//                        }
//                    }
                }
            }

            if (index != -1 && haveSanitizedComma && call != null) {
                Node an = null;
//                if (call.nodeId == NodeTypes.FCALLNODE) {
//                    an = ((FCallNode)call).getArgsNode();
//                } else if (call.nodeId == NodeTypes.CALLNODE) {
//                    an = ((CallNode)call).getArgsNode();
//                }
//                if (an != null && index < an.childNodes().size() &&
//                        ((Node)an.childNodes().get(index)).nodeId == NodeTypes.HASHNODE) {
//                    // We should stay within the hashnode, so counteract the
//                    // index++ which follows this if-block
//                    index--;
//                }

                // Adjust the index to account for our removed
                // comma
                index++;
            }

//            String fqn = null;
            if ((call == null) || (index == -1)) {
                callLineStart = -1;
                callMethod = null;
                return false;
            } else if (targetMethod == null) {
                // Look up the
                // See if we can find the method corresponding to this call
//                fqn = JsTypeAnalyzer.getCallFqn(info, call, true);
//                if (fqn != null) {
//                    JsIndex jsIndex = JsIndex.get(info.getIndex(JsTokenId.JAVASCRIPT_MIME_TYPE));
//                    JsParseResult parseResult = AstUtilities.getParseResult(info);
//                    Set<IndexedElement> elements = jsIndex.getElementsByFqn(fqn, NameKind.EXACT_NAME, JsIndex.ALL_SCOPE, parseResult);
//                    // How do I choose one?
//                }
                targetMethod = new JsDeclarationFinder().findMethodDeclaration(info, call, path,
                        alternativesHolder);
                if (targetMethod == null) {
                    return false;
                }
            }

            callLineStart = currentLineStart;
            callMethod = targetMethod;

            // TODO - make dedicated result object?
            methodHolder[0] = callMethod;
            parameterIndexHolder[0] = index;
            // TODO - store the fqn too?

            if (anchorOffset == -1) {
                anchorOffset = call.getSourceStart(); // TODO - compute
            }
            anchorOffsetHolder[0] = anchorOffset;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return false;
        }

        return true;
    }

    private static class CompletionRequest {

        private DefaultCompletionResult completionResult;
        private TokenHierarchy<Document> th;
        private JsParseResult info;
        private AstPath path;
        private Node node;
        private Node root;
        private int anchor;
        private int lexOffset;
        private int astOffset;
        private BaseDocument doc;
        private String prefix;
        private JsIndex index;
        private QuerySupport.Kind kind;
        private JsParseResult result;
        private QueryType queryType;
        private FileObject fileObject;
        private Call call;
        private boolean inCall;
        private String fqn;
    }

    private class JsCompletionItem implements CompletionProposal {

        protected CompletionRequest request;
        protected Element element;
        protected IndexedElement indexedElement;

        private JsCompletionItem(Element element, CompletionRequest request) {
            this.element = element;
            this.request = request;
        }

        private JsCompletionItem(CompletionRequest request, IndexedElement element) {
            this(element, request);
            this.indexedElement = element;
        }

        public int getAnchorOffset() {
            return request.anchor;
        }

        public String getName() {
            return element.getName();
        }

        public String getInsertPrefix() {
            if (getKind() == ElementKind.PACKAGE) {
                return getName() + ".";
            } else {
                return getName();
            }
        }

        public String getSortText() {
            return getName();
        }

        public ElementHandle getElement() {
            // XXX Is this called a lot? I shouldn't need it most of the time
            return element;
        }

        public ElementKind getKind() {
            return element.getKind();
        }

        public ImageIcon getIcon() {
            return null;
        }

        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();
            boolean emphasize = (kind != ElementKind.PACKAGE && indexedElement != null) ? !indexedElement.isInherited() : false;
            if (emphasize) {
                formatter.emphasis(true);
            }

            boolean strike = false;
            if (indexedElement != null) {
                if (indexedElement.isDeprecated() || !SupportedBrowsers.getInstance().isSupported(indexedElement.getCompatibility())) {
                    strike = true;
                }
            }
            if (strike) {
                formatter.deprecated(true);
            }

            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);
            if (strike) {
                formatter.deprecated(false);
            }
            if (emphasize) {
                formatter.emphasis(false);
            }

            if (indexedElement != null) {
                String type = indexedElement.getType();
                if (type != null && type != Node.UNKNOWN_TYPE) {
                    formatter.appendHtml(" : "); // NOI18N
                    formatter.appendText(JsUtils.normalizeTypeString(type));
                }
            }

            return formatter.getText();
        }

        public String getRhsHtml(HtmlFormatter formatter) {
            if (element.getKind() == ElementKind.PACKAGE || element.getKind() == ElementKind.CLASS) {
                if (element instanceof IndexedElement) {
                    String origin = ((IndexedElement) element).getOrigin();
                    if (origin != null) {
                        formatter.appendText(origin);
                        return formatter.getText();
                    }
                }

                return null;
            }

            String in = element.getIn();

            if (in != null) {
                formatter.appendText(in);
                return formatter.getText();
            } else if (element instanceof IndexedElement) {
                IndexedElement ie = (IndexedElement) element;
                String filename = ie.getFilenameUrl();
                if (filename != null) {
                    if (filename.indexOf("jsstubs") == -1) { // NOI18N
                        int index = filename.lastIndexOf('/');
                        if (index != -1) {
                            filename = filename.substring(index + 1);
                        }
                        formatter.appendText(filename);
                        return formatter.getText();
                    } else {
                        String origin = ie.getOrigin();
                        if (origin != null) {
                            formatter.appendText(origin);
                            return formatter.getText();
                        }
                    }
                }

                return null;
            }

            return null;
        }

        public Set<Modifier> getModifiers() {
            return element.getModifiers();
        }

        @Override
        public String toString() {
            String cls = this.getClass().getName();
            cls = cls.substring(cls.lastIndexOf('.') + 1);

            return cls + "(" + getKind() + "): " + getName();
        }

        public boolean isSmart() {
            return indexedElement != null ? indexedElement.isSmart() : true;
        }

        public String getCustomInsertTemplate() {
            return null;
        }

        public int getSortPrioOverride() {
            return 0;
        }
    }

    private class FunctionItem extends JsCompletionItem {

        protected IndexedFunction function;

        FunctionItem(IndexedFunction element, CompletionRequest request) {
            super(request, element);
            this.function = element;
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();
            boolean strike = !SupportedBrowsers.getInstance().isSupported(function.getCompatibility());
            if (!strike && function.isDeprecated()) {
                strike = true;
            }
            if (strike) {
                formatter.deprecated(true);
            }
            boolean emphasize = !function.isInherited();
            if (emphasize) {
                formatter.emphasis(true);
            }
            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);
            if (emphasize) {
                formatter.emphasis(false);
            }
            if (strike) {
                formatter.deprecated(false);
            }

            Collection<String> parameters = function.getParameters();

            formatter.appendHtml("("); // NOI18N
            if ((parameters != null) && (parameters.size() > 0)) {

                Iterator<String> it = parameters.iterator();

                while (it.hasNext()) { // && tIt.hasNext()) {
                    formatter.parameters(true);
                    String param = it.next();
                    int typeIndex = param.indexOf(':');
                    if (typeIndex != -1) {
                        formatter.type(true);
                        // TODO - call JsUtils.normalizeTypeString() on this string?
                        formatter.appendText(param, typeIndex + 1, param.length());
                        formatter.type(false);
                        formatter.appendHtml(" ");

                        formatter.appendText(param, 0, typeIndex);
                    } else {
                        formatter.appendText(param);
                    }
                    formatter.parameters(false);

                    if (it.hasNext()) {
                        formatter.appendText(", "); // NOI18N
                    }
                }

            }
            formatter.appendHtml(")"); // NOI18N

            if (indexedElement != null && indexedElement.getType() != null &&
                    indexedElement.getType() != Node.UNKNOWN_TYPE &&
                    indexedElement.getKind() != ElementKind.CONSTRUCTOR) {
                formatter.appendHtml(" : ");
                formatter.appendText(JsUtils.normalizeTypeString(indexedElement.getType()));
            }

            return formatter.getText();
        }

        @Override
        public String getCustomInsertTemplate() {
            final String insertPrefix = getInsertPrefix();
            List<String> params = function.getParameters();
            String startDelimiter = "(";
            String endDelimiter = ")";
            int paramCount = params.size();

            StringBuilder sb = new StringBuilder();
            sb.append(insertPrefix);
            sb.append(startDelimiter);

            int id = 1;
            for (int i = 0; i < paramCount; i++) {
                String paramDesc = params.get(i);
                sb.append("${"); //NOI18N
                // Ensure that we don't use one of the "known" logical parameters
                // such that a parameter like "path" gets replaced with the source file
                // path!
                sb.append("js-cc-"); // NOI18N
                sb.append(Integer.toString(id++));
                sb.append(" default=\""); // NOI18N
                int typeIndex = paramDesc.indexOf(':');
                if (typeIndex != -1) {
                    sb.append(paramDesc, 0, typeIndex);
                } else {
                    sb.append(paramDesc);
                }
                sb.append("\""); // NOI18N
                sb.append("}"); //NOI18N
                if (i < paramCount - 1) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(endDelimiter);

            sb.append("${cursor}"); // NOI18N

            // Facilitate method parameter completion on this item
            try {
                callLineStart = Utilities.getRowStart(request.doc, request.anchor);
                callMethod = function;
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }

            return sb.toString();
        }
    }

    private class KeywordItem extends JsCompletionItem {

        private static final String Js_KEYWORD = "org/netbeans/modules/javascript/editing/javascript.png"; //NOI18N
        private final String keyword;
        private final String description;
        private ElementKind kind = ElementKind.KEYWORD;

        KeywordItem(String keyword, String description, CompletionRequest request) {
            super(null, request);
            this.keyword = keyword;
            this.description = description;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return kind;
        }

        @Override
        public String getRhsHtml(final HtmlFormatter formatter) {
            return null;
        }

        @Override
        public String getLhsHtml(final HtmlFormatter formatter) {
            ElementKind kind = getKind();
            formatter.name(kind, true);
            formatter.appendText(keyword);
            formatter.appendText(" "); // NOI18N
            formatter.name(kind, false);
            if (description != null) {
                formatter.appendHtml(description);
            }
            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (kind != ElementKind.KEYWORD) {
                // Use GSF default
                return null;
            }

            if (keywordIcon == null) {
                keywordIcon = ImageUtilities.loadImageIcon(Js_KEYWORD, false);
            }

            return keywordIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return new KeywordElement(keyword);
        }

        @Override
        public boolean isSmart() {
            return false;
        }

        private void setKind(ElementKind kind) {
            this.kind = kind;
        }
    }

    private class GenericItem extends JsCompletionItem {

        private final String tag;
        private final String description;
        private String longDescription;
        private final ElementKind kind;

        GenericItem(String keyword, String description, CompletionRequest request, ElementKind kind) {
            super(null, request);
            this.tag = keyword;
            this.description = description;
            this.kind = kind;
        }

        void setLongDescription(String longDescription) {
            this.longDescription = longDescription;
        }

        @Override
        public String getName() {
            return tag;
        }

        @Override
        public ElementKind getKind() {
            return kind;
        }

        //@Override
        //public String getLhsHtml(HtmlFormatter formatter) {
        //    // Override so we can put HTML contents in
        //    ElementKind kind = getKind();
        //    formatter.name(kind, true);
        //    //formatter.appendText(getName());
        //    formatter.appendHtml(getName());
        //    formatter.name(kind, false);
        //
        //    return formatter.getText();
        //}
        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (description != null) {
                //formatter.appendText(description);
                formatter.appendHtml("<i>"); // NOI18N
                formatter.appendHtml(description);
                formatter.appendHtml("</i>"); // NOI18N

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            if (element == null) {
                if (longDescription != null && longDescription.length() > 0) {
                    element = new CommentElement(longDescription);
                } else {
                    // For completion documentation
                    element = new KeywordElement(tag);
                }
            }

            return element;
        }

        @Override
        public boolean isSmart() {
            return true;
        }
    }

    public ElementHandle resolveLink(String link, ElementHandle elementHandle) {
        if (link.indexOf(':') != -1) {
            link = link.replace(':', '.');
            return new ElementHandle.UrlHandle(link);
        }
        return null;
    }

    private class CallItem extends FunctionItem {

        private int index;

        CallItem(IndexedFunction method, int parameterIndex, CompletionRequest request) {
            super(method, request);
            this.index = parameterIndex;
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
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();
            formatter.name(kind, true);
            formatter.appendText(getName());

            List<String> parameters = function.getParameters();

            if ((parameters != null) && (parameters.size() > 0)) {
                formatter.appendHtml("("); // NOI18N

                if (index > 0 && index < parameters.size()) {
                    formatter.appendText("... , ");
                }

                formatter.active(true);
                formatter.appendText(parameters.get(Math.min(parameters.size() - 1, index)));
                formatter.active(false);

                if (index < parameters.size() - 1) {
                    formatter.appendText(", ...");
                }

                formatter.appendHtml(")"); // NOI18N
            }

            formatter.name(kind, false);

            return formatter.getText();
        }

        @Override
        public boolean isSmart() {
            return true;
        }

        @Override
        public String getCustomInsertTemplate() {
            return null;
        }
    }
}
