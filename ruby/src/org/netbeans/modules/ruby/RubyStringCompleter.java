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
package org.netbeans.modules.ruby;

import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.RubyCompletionItem.ClassItem;
import org.netbeans.modules.ruby.RubyCompletionItem.KeywordItem;
import org.netbeans.modules.ruby.elements.CommentElement;
import org.netbeans.modules.ruby.elements.Element;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.modules.ruby.platform.gems.Gem;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.util.NbBundle;

final class RubyStringCompleter extends RubyBaseCompleter {

    private static final String[] RUBY_PERCENT_WORDS = new String[]{
        "%q", "String (single-quoting rules)",
        "%Q", "String (double-quoting rules)",
        "%r", "Regular Expression",
        "%x", "Commands",
        "%W", "String Array (double quoting rules)",
        "%w", "String Array (single quoting rules)",
        "%s", "Symbol"
    };
    
    private static final String[] RUBY_REGEXP_WORDS = new String[]{
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
        "[:xdigit:]", "Hex digit (0-9, a-f, A-F)"
    };

    private static final String[] RUBY_STRING_PAIRS = new String[]{
        "(", "(delimiters)",
        "{", "{delimiters}",
        "[", "[delimiters]",
        "x", "<i>x</i>delimiters<i>x</i>"
    };

    private static final String[] RUBY_QUOTED_STRING_ESCAPES =
            new String[]{
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
        "\\v", "Vertical tab (0x0b)"
    };
    
    static boolean complete(
            final List<? super CompletionProposal> proposals,
            final CompletionRequest request,
            final int anchor,
            final boolean caseSensitive) {
        RubyStringCompleter rsc = new RubyStringCompleter(proposals, request, anchor, caseSensitive);
        return rsc.complete();
    }

    private RubyStringCompleter(
            final List<? super CompletionProposal> proposals,
            final CompletionRequest request,
            final int anchor,
            final boolean caseSensitive) {
        super(proposals, request, anchor, caseSensitive);
    }

    @SuppressWarnings("unchecked")
    private boolean complete() {
        String prefix = request.prefix;
        int lexOffset = request.lexOffset;
        TokenHierarchy<Document> th = request.th;

        TokenSequence<? extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

        if ((getIndex() != null) && (ts != null)) {
            ts.move(lexOffset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return false;
            }

            if (ts.offset() == lexOffset) {
                // We're looking at the offset to the RIGHT of the caret
                // and here I care about what's on the left
                ts.movePrevious();
            }

            Token<? extends RubyTokenId> token = ts.token();

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
                                if (text.startsWith("=begin\n") && text.endsWith("=end")) { // NOI18N
                                    text = text.substring("=begin\n".length(), text.length() - "=end".length()); // NOI18N
                                }
                                Element element = new CommentElement(text);
                                ClassItem item = new ClassItem(element, anchor, request);
                                propose(item);
                                return true;
                            } catch (BadLocationException ble) {
                                // do nothing - see #154991
                            }
                        }
                    }

                    // No other possible completions in comments
                    return true;
                }

                // We're within a String that has embedded Ruby. Drop into the
                // embedded language and see if we're within a literal string there.
                if (id == RubyTokenId.EMBEDDED_RUBY) {
                    ts = (TokenSequence) ts.embedded();
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
                        if (completePercentWords()) {
                            return true;
                        }
                    }
                }

                // Incomplete String/Regexp marker:  %x|{
                if (((id == RubyTokenId.STRING_BEGIN) || (id == RubyTokenId.QUOTED_STRING_BEGIN) ||
                        (id == RubyTokenId.REGEXP_BEGIN)) &&
                        ((token.length() == 3) && (lexOffset == (ts.offset() + 2)))) {
                    if (Character.isLetter(tokenText.charAt(1))) {
                        completeStringBegins();

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
                    if (completeRegexps()) {
                        request.completionResult.setFilterable(false);
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
                                        getIndex().getRequires(prefix,
                                        caseSensitive ? QuerySupport.Kind.PREFIX
                                        : QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);

                                for (String[] require : requires) {
                                    assert require.length == 2;

                                    // If a method is an "initialize" method I should do something special so that
                                    // it shows up as a "constructor" (in a new() statement) but not as a directly
                                    // callable initialize method (it should already be culled because it's private)
                                    KeywordItem item =
                                            new KeywordItem(require[0], require[1], anchor, request);
                                    propose(item);
                                }

                                return true;
                            } else if ("gem".equals(text)) {
                                proposeGems(prefix);
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
                            propose(item);
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

    private void proposeGems(String prefix) {
        Project owner = FileOwnerQuery.getOwner(request.fileObject);
        if (owner == null) {
            return;
        }
        GemManager gemManager = RubyPlatform.gemManagerFor(owner);
        if (gemManager == null) {
            return;
        }
        List<Gem> gems = gemManager.getLocalGems();
        for (Gem gem : gems) {
            if (gem.getName().startsWith(prefix)) {
                String versions = NbBundle.getMessage(RubyStringCompleter.class, "InstalledVersions", gem.getInstalledVersionsAsString());
                KeywordItem item =
                        new KeywordItem(gem.getName(), versions, anchor, request);
                propose(item);
            }
        }
    }

    private boolean completePercentWords() {
        String prefix = request.prefix;

        for (int i = 0, n = RUBY_PERCENT_WORDS.length; i < n; i += 2) {
            String word = RUBY_PERCENT_WORDS[i];
            String desc = RUBY_PERCENT_WORDS[i + 1];

            if (RubyCodeCompleter.startsWith(word, prefix, caseSensitive)) {
                KeywordItem item = new KeywordItem(word, desc, anchor, request);
                propose(item);
            }
        }

        return true;
    }

    private boolean completeRegexps() {
        String prefix = request.prefix;

        // Regular expression matching.  {
        for (int i = 0, n = RUBY_REGEXP_WORDS.length; i < n; i += 2) {
            String word = RUBY_REGEXP_WORDS[i];
            String desc = RUBY_REGEXP_WORDS[i + 1];

            if (RubyCodeCompleter.startsWith(word, prefix, caseSensitive)) {
                KeywordItem item = new KeywordItem(word, desc, anchor, request);
                propose(item);
            }
        }

        return true;
    }

    private boolean completeStringBegins() {
        for (int i = 0, n = RUBY_STRING_PAIRS.length; i < n; i += 2) {
            String word = RUBY_STRING_PAIRS[i];
            String desc = RUBY_STRING_PAIRS[i + 1];

            KeywordItem item = new KeywordItem(word, desc, anchor, request);
            propose(item);
        }

        return true;
    }

}
