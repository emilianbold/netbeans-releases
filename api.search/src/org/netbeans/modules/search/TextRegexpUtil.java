/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.search.SearchPattern;

/**
 * Helper for creating regular expression patterns for search patterns and
 * searcher options.
 *
 * @since org.openidex.util/3 3.34
 * @author jhavlin
 */
public final class TextRegexpUtil {

    /**
     * regular expression representing a set of word characters
     */
    private static final String wordCharsExpr =
            "[\\p{javaLetterOrDigit}_]";                                //NOI18N
    /**
     * regular expression representing negative lookbehind for a {@linkplain #wordCharsExpr word character}.
     */
    private static final String checkNotAfterWordChar =
            "(?<!" + wordCharsExpr + ")";                               //NOI18N
    /**
     * regular expression representing negative lookahead for a {@linkplain #wordCharsExpr word character}.
     */
    private static final String checkNotBeforeWordChar =
            "(?!" + wordCharsExpr + ")";                                //NOI18N
    private static String MULTILINE_REGEXP_PATTERN =
            ".*(\\\\n|\\\\r|\\\\f|\\\\u|\\\\0|\\\\x|\\(\\?[idmux]*s).*";//NOI18N
    private static final Logger LOG = Logger.getLogger(
            TextRegexpUtil.class.getName());

    private TextRegexpUtil() {
    }

    /**
     * Translates the given simple pattern to a regular expression.
     *
     * @param simplePattern pattern to be translated
     * @param wholeWords whether the <i>Whole Words</i> option is selected
     * @return regular expression corresponding to the simple pattern
     */
    private static String makeRegexp(String simplePattern, boolean wholeWords) {

        if (simplePattern.length() == 0) {              //trivial case
            return simplePattern;
        }

        if (!wholeWords
                && Pattern.matches("[a-zA-Z0-9 ]*", simplePattern)) {   //NOI18N
            return simplePattern;                       //trivial case
        }

        StringBuilder buf = new StringBuilder(simplePattern.length() + 16);
        boolean quoted = false;
        boolean starPresent = false;
        int minCount = 0;

        boolean bufIsEmpty = true;
        char lastInputChar = '*';       //might be any other non-word character
        for (char c : simplePattern.toCharArray()) {
            if (quoted && (c == '?' || c == '*')) {
                assert !starPresent && (minCount == 0);
                if (wholeWords && bufIsEmpty) {
                    buf.append(checkNotAfterWordChar);
                }
                buf.append('\\');
                buf.append(c);
                lastInputChar = c;
                bufIsEmpty = false;
                quoted = false;
            } else if (c == '?') {
                assert !quoted;
                minCount++;
            } else if (c == '*') {
                assert !quoted;
                starPresent = true;
            } else {
                if (starPresent || (minCount != 0)) {
                    if (wholeWords && bufIsEmpty && !starPresent) {
                        buf.append(checkNotAfterWordChar);
                    }
                    bufIsEmpty &= !addMetachars(buf, starPresent, minCount,
                            wholeWords, !bufIsEmpty);
                    starPresent = false;
                    minCount = 0;
                }
                if (quoted) { // backslash was not used for escaping
                    buf.append("\\\\");
                    quoted = false;
                }
                if (c == '\\') {
                    quoted = true;
                } else {
                    if (wholeWords && bufIsEmpty && isWordChar(c)) {
                        buf.append(checkNotAfterWordChar);
                    }
                    if (isSpecialCharacter(c)) {
                        buf.append('\\');
                    }
                    buf.append(c);
                    lastInputChar = c;
                    bufIsEmpty = false;
                }
            }
        }
        if (quoted) {
            assert !starPresent && (minCount == 0);
            buf.append('\\').append('\\');
            lastInputChar = '\\';
        } else if (starPresent || (minCount != 0)) {
            if (wholeWords && !starPresent && bufIsEmpty) {
                buf.append(checkNotAfterWordChar);
            }
            bufIsEmpty &= !addMetachars(buf, starPresent, minCount, wholeWords,
                    false);
            if (wholeWords && !starPresent) {
                buf.append(checkNotBeforeWordChar);
            }
            lastInputChar = '*';    //might be any other non-word character
        }
        if (wholeWords && isWordChar(lastInputChar)) {
            buf.append(checkNotBeforeWordChar);
        }
        return buf.toString();
    }

    /**
     * Translates the simple text pattern to a regular expression pattern and
     * compiles it. The compiled pattern is stored to field {@link #textPattern}.
     *
     * @param sp Search pattern, cannot be nul, and its search expression cannot
     * be null as well.
     */
    private static Pattern compileSimpleTextPattern(SearchPattern sp)
            throws PatternSyntaxException {

        assert sp != null;
        assert sp.getSearchExpression() != null;
        assert !sp.isRegExp();

        int flags = 0;
        if (!sp.isMatchCase()) {
            flags |= Pattern.CASE_INSENSITIVE;
            flags |= Pattern.UNICODE_CASE;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, " - textPatternExpr = \"{0}{1}",
                    new Object[]{sp.getSearchExpression(), '"'});       //NOI18N
        }
        String searchRegexp = makeRegexp(sp.getSearchExpression(),
                sp.isWholeWords());

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, " - regexp = \"{0}{1}",
                    new Object[]{searchRegexp, '"'});                   //NOI18N
        }
        return Pattern.compile(searchRegexp, flags);
    }

    /**
     * Tries to compile the regular expression pattern, thus checking its
     * validity. In case of success, the compiled pattern is stored to {@link #textPattern},
     * otherwise the field is set to {@code null}.
     *
     * <p>Actually, this method defines a pattern used in searching, i.e. it
     * defines behaviour of the searching. It should be the same as behavior of
     * the Find action (Ctrl+F) in the Editor to avoid any confusions (see Bug
     * #175101). Hence, this implementation should specify default flags in the
     * call of the method {@link Pattern#compile(java.lang.String, int)
     * java.util.regex.Pattern.compile(String regex, int flags)} that are the
     * same as in the implementation of the Find action (i.e in the method {@code getFinder}
     * of the class {@code org.netbeans.modules.editor.lib2.search.DocumentFinder}).
     * </p>
     *
     * @return {@code true} if the regexp pattern expression was valid; {@code false}
     * otherwise
     */
    private static Pattern compileRegexpPattern(SearchPattern sp)
            throws PatternSyntaxException {

        assert sp != null;
        assert sp.getSearchExpression() != null;
        assert sp.isRegExp();

        boolean multiline = canBeMultilinePattern(sp.getSearchExpression());

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, " - textPatternExpr = \"{0}{1}",
                    new Object[]{sp.getSearchExpression(), '"'});   //NOI18N
        }
        int flags = 0;
        if (!sp.isMatchCase()) {
            flags |= Pattern.CASE_INSENSITIVE;
            flags |= Pattern.UNICODE_CASE;
        }
        if (multiline) {
            flags |= Pattern.MULTILINE; // #175101
        }
        return Pattern.compile(sp.getSearchExpression(), flags);


    }

    public static Pattern makeTextPattern(SearchPattern pattern)
            throws PatternSyntaxException, NullPointerException {

        if (pattern == null) {
            throw new NullPointerException("search pattern is null");   //NOI18N
        } else if (pattern.getSearchExpression() == null) {
            throw new NullPointerException("expression is null");       //NOI18N
        }

        if (pattern.isRegExp()) {
            return compileRegexpPattern(pattern);
        } else {
            return compileSimpleTextPattern(pattern);
        }
    }

    /**
     * Checks whether the given character is a word character.
     *
     * @param c character to be checked
     * @return {@code true} if the character is a word character, {@code false}
     * otherwise
     * @see #wordCharsExpr
     */
    private static boolean isWordChar(char c) {
        /*
         * not necessary - just for performance
         */
        if ((c == '*') || (c == '\\')) {
            return false;
        }

        assert "[\\p{javaLetterOrDigit}_]".equals(wordCharsExpr) //NOI18N
                : "update implementation of method isWordChar(char)";    //NOI18N
        return (c == '_') || Character.isLetterOrDigit(c);
    }

    /**
     * Generates the part of a regular expression, that represents a sequence of
     * simple expression's metacharacters {@code '*'} and {@code '?'}, and adds
     * it to the given string buffer.
     *
     * @param buf string buffer to which the new part is to be added
     * @param starPresent whether the sequence contained at least one {@code '*'}
     * character
     * @param minCount number of {@code '?'} characters in the sequence
     * @param wholeWords whether the <i>Whole Words</i> option is selected
     * @param middle whether the metachars are to be placed in the middle (i.e.
     * not in the beginning or at the end) of the search expression
     * @return {@code true} if something was added to the string buffer, {@code false}
     * if the buffer was not modified
     */
    private static boolean addMetachars(final StringBuilder buf,
            boolean starPresent,
            final int minCount,
            final boolean wholeWords,
            final boolean middle) {
        assert starPresent || (minCount != 0);

        /*
         * If 'Whole Words' is not activated, ignore stars in the beginning and
         * at the end of the expression:
         */
        if (starPresent && !wholeWords && !middle) {
            starPresent = false;
        }

        if ((minCount == 0) && !starPresent) {
            return false;
        }

        if (wholeWords) {
            buf.append(wordCharsExpr);
        } else {
            buf.append('.');
        }
        switch (minCount) {
            case 0:
                assert starPresent;
                buf.append('*');
                break;
            case 1:
                if (starPresent) {
                    buf.append('+');
                }
                break;
            default:
                if (wholeWords) {
                    buf.append('{').append(minCount);
                    if (starPresent) {
                        buf.append(',');
                    }
                    buf.append('}');
                } else {
                    for (int i = 1; i < minCount; i++) {
                        buf.append('.');
                    }
                    if (starPresent) {
                        buf.append('+');
                    }
                }
        }
        if (starPresent && middle) {
            buf.append('?');    //use reluctant variant of the quantifier
        }
        return true;
    }
    
    private static boolean isSpecialCharacter(char c) {
        return (c > 0x20) && (c < 0x80) && !isAlnum(c);
    }

    private static boolean isAlnum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private static boolean isAlpha(char c) {
        c |= 0x20;  //to lower case
        return (c >= 'a') && (c <= 'z');
    }

    private static boolean isDigit(char c) {
        return (c >= '0') && (c <= '9');
    }

    /**
     * Check if multi-line matching should be used for passed regular
     * expression.
     */
    public static boolean canBeMultilinePattern(String expr) {
        if (expr == null) {
            return false;
        }
        return expr.matches(MULTILINE_REGEXP_PATTERN);
    }
}
