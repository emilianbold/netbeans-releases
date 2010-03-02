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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author mp
 */
public class RegexpMakerTest extends NbTestCase {

    public RegexpMakerTest() {
        super("SimpleRegexpParserTest");
    }

    private static String getClassField(String name) throws Exception {
        Field field = RegexpMaker.class.getDeclaredField(name);
        field.setAccessible(true);
        return (String) field.get(null);
    }

    public void testMakeRegexp() throws Exception {

        /* basics: */
        assertEquals("", RegexpMaker.makeRegexp(""));
        assertEquals("a", RegexpMaker.makeRegexp("a"));
        assertEquals("ab", RegexpMaker.makeRegexp("ab"));
        assertEquals("abc", RegexpMaker.makeRegexp("abc"));

        /* special chars in the middle: */
        assertEquals("a.*?b.c", RegexpMaker.makeRegexp("a*b?c"));
        assertEquals("a..+?b", RegexpMaker.makeRegexp("a?*?b"));
        assertEquals("a.+?b", RegexpMaker.makeRegexp("a*?*b"));

        /* ignore stars in the begining: */
        assertEquals("a", RegexpMaker.makeRegexp("*a"));
        assertEquals(".a", RegexpMaker.makeRegexp("?a"));
        assertEquals("a", RegexpMaker.makeRegexp("**a"));
        assertEquals(".a", RegexpMaker.makeRegexp("*?a"));
        assertEquals(".a", RegexpMaker.makeRegexp("?*a"));
        assertEquals("..a", RegexpMaker.makeRegexp("??a"));

        /* ignore stars at the end: */
        assertEquals("a", RegexpMaker.makeRegexp("a*"));
        assertEquals("a.", RegexpMaker.makeRegexp("a?"));
        assertEquals("a", RegexpMaker.makeRegexp("a**"));
        assertEquals("a.", RegexpMaker.makeRegexp("a*?"));
        assertEquals("a.", RegexpMaker.makeRegexp("a?*"));
        assertEquals("a..", RegexpMaker.makeRegexp("a??"));

        /* other usage of '*' and '?': */
        assertEquals(" .*?a", RegexpMaker.makeRegexp(" *a"));
        assertEquals(" .a", RegexpMaker.makeRegexp(" ?a"));
        assertEquals(" a", RegexpMaker.makeRegexp("* a"));
        assertEquals(". a", RegexpMaker.makeRegexp("? a"));
        assertEquals("\\,a", RegexpMaker.makeRegexp("*,a"));
        assertEquals(".\\,a", RegexpMaker.makeRegexp("?,a"));
        assertEquals("a.*? ", RegexpMaker.makeRegexp("a* "));
        assertEquals("a. ", RegexpMaker.makeRegexp("a? "));
        assertEquals("a ", RegexpMaker.makeRegexp("a *"));
        assertEquals("a .", RegexpMaker.makeRegexp("a ?"));
        assertEquals("a\\,", RegexpMaker.makeRegexp("a,*"));
        assertEquals("a\\,.", RegexpMaker.makeRegexp("a,?"));

        /* whole words: */

        final String wordCharsExpr = getClassField("wordCharsExpr");
        final String checkNotAfterWordChar = getClassField("checkNotAfterWordChar");
        final String checkNotBeforeWordChar = getClassField("checkNotBeforeWordChar");

        assertEquals("", RegexpMaker.makeRegexp("", true));
        assertEquals(checkNotAfterWordChar + "a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("a", true));
        assertEquals(checkNotAfterWordChar
                     + "a" + wordCharsExpr + "*?b" + wordCharsExpr + "c"
                     + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("a*b?c", true));
        assertEquals(checkNotAfterWordChar
                     + "a" + wordCharsExpr + "{2,}?b"
                     + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("a?*?b", true));
        assertEquals(checkNotAfterWordChar
                     + "a" + wordCharsExpr + "+?b"
                     + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("a*?*b", true));

        assertEquals(wordCharsExpr + "*a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("*a", true));
        assertEquals(checkNotAfterWordChar + wordCharsExpr + "a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("?a", true));
        assertEquals(wordCharsExpr + "*a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("**a", true));
        assertEquals(wordCharsExpr + "+a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("*?a", true));
        assertEquals(wordCharsExpr + "+a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("?*a", true));
        assertEquals(checkNotAfterWordChar + wordCharsExpr + "{2}a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("??a", true));

        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "*",
                     RegexpMaker.makeRegexp("a*", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("a?", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "*",
                     RegexpMaker.makeRegexp("a**", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "+",
                     RegexpMaker.makeRegexp("a*?", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "+",
                     RegexpMaker.makeRegexp("a?*", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "{2}" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("a??", true));

        assertEquals(" " + wordCharsExpr + "*?a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp(" *a", true));
        assertEquals(" " + wordCharsExpr + "a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp(" ?a", true));
        assertEquals(wordCharsExpr + "* a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("* a", true));
        assertEquals(checkNotAfterWordChar + wordCharsExpr + " a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("? a", true));
        assertEquals(wordCharsExpr + "*\\,a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("*,a", true));
        assertEquals(checkNotAfterWordChar + wordCharsExpr + "\\,a" + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("?,a", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "*? ",
                     RegexpMaker.makeRegexp("a* ", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + " ",
                     RegexpMaker.makeRegexp("a? ", true));
        assertEquals(checkNotAfterWordChar + "a " + wordCharsExpr + "*",
                     RegexpMaker.makeRegexp("a *", true));
        assertEquals(checkNotAfterWordChar + "a " + wordCharsExpr + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("a ?", true));
        assertEquals(checkNotAfterWordChar + "a\\," + wordCharsExpr + "*",
                     RegexpMaker.makeRegexp("a,*", true));
        assertEquals(checkNotAfterWordChar + "a\\," + wordCharsExpr + checkNotBeforeWordChar,
                     RegexpMaker.makeRegexp("a,?", true));

        assertEquals("a b", RegexpMaker.makeRegexp("a b"));
        assertEquals("a\\!b", RegexpMaker.makeRegexp("a!b"));
        assertEquals("a\\\"b", RegexpMaker.makeRegexp("a\"b"));
        assertEquals("a\\#b", RegexpMaker.makeRegexp("a#b"));
        assertEquals("a\\$b", RegexpMaker.makeRegexp("a$b"));
        assertEquals("a\\%b", RegexpMaker.makeRegexp("a%b"));
        assertEquals("a\\&b", RegexpMaker.makeRegexp("a&b"));
        assertEquals("a\\'b", RegexpMaker.makeRegexp("a'b"));
        assertEquals("a\\(b", RegexpMaker.makeRegexp("a(b"));
        assertEquals("a\\)b", RegexpMaker.makeRegexp("a)b"));
        assertEquals("a\\+b", RegexpMaker.makeRegexp("a+b"));
        assertEquals("a\\,b", RegexpMaker.makeRegexp("a,b"));
        assertEquals("a\\-b", RegexpMaker.makeRegexp("a-b"));
        assertEquals("a\\.b", RegexpMaker.makeRegexp("a.b"));
        assertEquals("a\\/b", RegexpMaker.makeRegexp("a/b"));
        
        assertEquals("a0b", RegexpMaker.makeRegexp("a0b"));
        assertEquals("a1b", RegexpMaker.makeRegexp("a1b"));
        assertEquals("a2b", RegexpMaker.makeRegexp("a2b"));
        assertEquals("a3b", RegexpMaker.makeRegexp("a3b"));
        assertEquals("a4b", RegexpMaker.makeRegexp("a4b"));
        assertEquals("a5b", RegexpMaker.makeRegexp("a5b"));
        assertEquals("a6b", RegexpMaker.makeRegexp("a6b"));
        assertEquals("a7b", RegexpMaker.makeRegexp("a7b"));
        assertEquals("a8b", RegexpMaker.makeRegexp("a8b"));
        assertEquals("a9b", RegexpMaker.makeRegexp("a9b"));
        
        assertEquals("a\\:b", RegexpMaker.makeRegexp("a:b"));
        assertEquals("a\\;b", RegexpMaker.makeRegexp("a;b"));
        assertEquals("a\\<b", RegexpMaker.makeRegexp("a<b"));
        assertEquals("a\\=b", RegexpMaker.makeRegexp("a=b"));
        assertEquals("a\\>b", RegexpMaker.makeRegexp("a>b"));
        assertEquals("a\\@b", RegexpMaker.makeRegexp("a@b"));
        assertEquals("a\\[b", RegexpMaker.makeRegexp("a[b"));
        assertEquals("aa", RegexpMaker.makeRegexp("a\\a"));
        assertEquals("ab", RegexpMaker.makeRegexp("a\\b"));
        assertEquals("ac", RegexpMaker.makeRegexp("a\\c"));
        assertEquals("ad", RegexpMaker.makeRegexp("a\\d"));
        assertEquals("ae", RegexpMaker.makeRegexp("a\\e"));
        assertEquals("af", RegexpMaker.makeRegexp("a\\f"));
        assertEquals("ag", RegexpMaker.makeRegexp("a\\g"));
        assertEquals("ah", RegexpMaker.makeRegexp("a\\h"));
        assertEquals("ai", RegexpMaker.makeRegexp("a\\i"));
        assertEquals("aj", RegexpMaker.makeRegexp("a\\j"));
        assertEquals("ak", RegexpMaker.makeRegexp("a\\k"));
        assertEquals("al", RegexpMaker.makeRegexp("a\\l"));
        assertEquals("am", RegexpMaker.makeRegexp("a\\m"));
        assertEquals("a\\n", RegexpMaker.makeRegexp("a\\n"));
        assertEquals("ao", RegexpMaker.makeRegexp("a\\o"));
        assertEquals("ap", RegexpMaker.makeRegexp("a\\p"));
        assertEquals("aq", RegexpMaker.makeRegexp("a\\q"));
        assertEquals("ar", RegexpMaker.makeRegexp("a\\r"));
        assertEquals("as", RegexpMaker.makeRegexp("a\\s"));
        assertEquals("at", RegexpMaker.makeRegexp("a\\t"));
        assertEquals("au", RegexpMaker.makeRegexp("a\\u"));
        assertEquals("av", RegexpMaker.makeRegexp("a\\v"));
        assertEquals("aw", RegexpMaker.makeRegexp("a\\w"));
        assertEquals("ax", RegexpMaker.makeRegexp("a\\x"));
        assertEquals("ay", RegexpMaker.makeRegexp("a\\y"));
        assertEquals("az", RegexpMaker.makeRegexp("a\\z"));
        assertEquals("a\\]b", RegexpMaker.makeRegexp("a]b"));
        assertEquals("a\\^b", RegexpMaker.makeRegexp("a^b"));
        assertEquals("a\\_b", RegexpMaker.makeRegexp("a_b"));
        assertEquals("a\\`b", RegexpMaker.makeRegexp("a`b"));
        assertEquals("a\\{b", RegexpMaker.makeRegexp("a{b"));
        assertEquals("a\\|b", RegexpMaker.makeRegexp("a|b"));
        assertEquals("a\\}b", RegexpMaker.makeRegexp("a}b"));
        assertEquals("a\\~b", RegexpMaker.makeRegexp("a~b"));
        assertEquals("a\\\u007fb", RegexpMaker.makeRegexp("a\u007fb"));
        
        assertEquals("a\u0080b", RegexpMaker.makeRegexp("a\u0080b"));
        assertEquals("a\u00c1b", RegexpMaker.makeRegexp("a\u00c1b"));
        
        assertEquals("abc\\\\", RegexpMaker.makeRegexp("abc\\"));
    }

    public void testRegexpMatches() {
        checkMatch("public", "x", null);
        checkMatch("public", "li", "li");
        checkMatch("public", "*li", "li");
        checkMatch("public", "li*", "li");
        checkMatch("public", "*li*", "li");

        checkMatchWW("public", "x", null);
        checkMatchWW("public", "li", null);
        checkMatchWW("public", "*li", null);
        checkMatchWW("public", "li*", null);
        checkMatchWW("public", "*li*", "public");
        checkMatchWW("public poklice", "*li*", "public");
        checkMatchWW("public poklice", "*lic", "public");
        checkMatchWW("poklice public", "*lic", "public");
        checkMatchWW("public", "??lic", null);
        checkMatchWW("public", "pub??", null);
        checkMatchWW("", "???", null);
        checkMatchWW("p", "???", null);
        checkMatchWW("pub", "???", "pub");
        checkMatchWW("public", "???", null);
    }

    private void checkMatch(String testString,
                            String simpleExpr,
                            String expectedMatch) {
        checkMatch(testString, simpleExpr, expectedMatch, false);
    }

    private void checkMatchWW(String testString,
                              String simpleExpr,
                              String expectedMatch) {
        checkMatch(testString, simpleExpr, expectedMatch, true);
    }

    /**
     * Checks whether the given simple expression matches the expected substring
     * of the given string.
     * 
     * @param  simpleExpr  simple search expression to be tested
     * @param  expectedMatch  substring that should be matched by the expression
     * @param  testString  test string to be searched
     * @param  wholeWords  whether to search with the <i>Whole Words</i> option
     */
    private void checkMatch(String testString,
                            String simpleExpr,
                            String expectedMatch,
                            boolean wholeWords) {
        String regexp = RegexpMaker.makeRegexp(simpleExpr, wholeWords);
        Matcher matcher = Pattern.compile(regexp).matcher(testString);

        if (expectedMatch == null) {
            assertFalse(matcher.find());
        } else {
            assertTrue(matcher.find());
            assertEquals(expectedMatch, matcher.group());
        }
    }
    
    public void testMakeMultiRegexp() {
        assertEquals("", RegexpMaker.makeMultiRegexp(""));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a"));
        assertEquals("ab", RegexpMaker.makeMultiRegexp("ab"));
        assertEquals("abc", RegexpMaker.makeMultiRegexp("abc"));
        assertEquals("a.", RegexpMaker.makeMultiRegexp("a?"));
        assertEquals("a.*", RegexpMaker.makeMultiRegexp("a*"));
        assertEquals(".a", RegexpMaker.makeMultiRegexp("?a"));
        assertEquals(".*a", RegexpMaker.makeMultiRegexp("*a"));
        assertEquals("a.b", RegexpMaker.makeMultiRegexp("a?b"));
        assertEquals(".a.", RegexpMaker.makeMultiRegexp("?a?"));
        assertEquals("a.*b.c", RegexpMaker.makeMultiRegexp("a*b?c"));
        assertEquals("a...*b", RegexpMaker.makeMultiRegexp("a?*?b"));
        assertEquals("a..*b", RegexpMaker.makeMultiRegexp("a*?*b"));
        
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a b"));
        assertEquals("a\\!b", RegexpMaker.makeMultiRegexp("a!b"));
        assertEquals("a\\\"b", RegexpMaker.makeMultiRegexp("a\"b"));
        assertEquals("a\\#b", RegexpMaker.makeMultiRegexp("a#b"));
        assertEquals("a\\$b", RegexpMaker.makeMultiRegexp("a$b"));
        assertEquals("a\\%b", RegexpMaker.makeMultiRegexp("a%b"));
        assertEquals("a\\&b", RegexpMaker.makeMultiRegexp("a&b"));
        assertEquals("a\\'b", RegexpMaker.makeMultiRegexp("a'b"));
        assertEquals("a\\(b", RegexpMaker.makeMultiRegexp("a(b"));
        assertEquals("a\\)b", RegexpMaker.makeMultiRegexp("a)b"));
        assertEquals("a\\+b", RegexpMaker.makeMultiRegexp("a+b"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a,b"));
        assertEquals("a\\-b", RegexpMaker.makeMultiRegexp("a-b"));
        assertEquals("a\\.b", RegexpMaker.makeMultiRegexp("a.b"));
        assertEquals("a\\/b", RegexpMaker.makeMultiRegexp("a/b"));
        
        assertEquals("a0b", RegexpMaker.makeMultiRegexp("a0b"));
        assertEquals("a1b", RegexpMaker.makeMultiRegexp("a1b"));
        assertEquals("a2b", RegexpMaker.makeMultiRegexp("a2b"));
        assertEquals("a3b", RegexpMaker.makeMultiRegexp("a3b"));
        assertEquals("a4b", RegexpMaker.makeMultiRegexp("a4b"));
        assertEquals("a5b", RegexpMaker.makeMultiRegexp("a5b"));
        assertEquals("a6b", RegexpMaker.makeMultiRegexp("a6b"));
        assertEquals("a7b", RegexpMaker.makeMultiRegexp("a7b"));
        assertEquals("a8b", RegexpMaker.makeMultiRegexp("a8b"));
        assertEquals("a9b", RegexpMaker.makeMultiRegexp("a9b"));
        
        assertEquals("a\\:b", RegexpMaker.makeMultiRegexp("a:b"));
        assertEquals("a\\;b", RegexpMaker.makeMultiRegexp("a;b"));
        assertEquals("a\\<b", RegexpMaker.makeMultiRegexp("a<b"));
        assertEquals("a\\=b", RegexpMaker.makeMultiRegexp("a=b"));
        assertEquals("a\\>b", RegexpMaker.makeMultiRegexp("a>b"));
        assertEquals("a\\@b", RegexpMaker.makeMultiRegexp("a@b"));
        assertEquals("a\\[b", RegexpMaker.makeMultiRegexp("a[b"));
        assertEquals("ab", RegexpMaker.makeMultiRegexp("a\\b"));
        assertEquals("a\\]b", RegexpMaker.makeMultiRegexp("a]b"));
        assertEquals("a\\^b", RegexpMaker.makeMultiRegexp("a^b"));
        assertEquals("a\\_b", RegexpMaker.makeMultiRegexp("a_b"));
        assertEquals("a\\`b", RegexpMaker.makeMultiRegexp("a`b"));
        assertEquals("a\\{b", RegexpMaker.makeMultiRegexp("a{b"));
        assertEquals("a\\|b", RegexpMaker.makeMultiRegexp("a|b"));
        assertEquals("a\\}b", RegexpMaker.makeMultiRegexp("a}b"));
        assertEquals("a\\~b", RegexpMaker.makeMultiRegexp("a~b"));
        assertEquals("a\\\u007fb", RegexpMaker.makeMultiRegexp("a\u007fb"));
        
        assertEquals("a\u0080b", RegexpMaker.makeMultiRegexp("a\u0080b"));
        assertEquals("a\u00c1b", RegexpMaker.makeMultiRegexp("a\u00c1b"));
        
        assertEquals("abc\\\\", RegexpMaker.makeRegexp("abc\\"));
        
        assertEquals("", RegexpMaker.makeMultiRegexp(""));
        assertEquals("", RegexpMaker.makeMultiRegexp(" "));
        assertEquals("", RegexpMaker.makeMultiRegexp(","));
        assertEquals("", RegexpMaker.makeMultiRegexp(", "));
        assertEquals("", RegexpMaker.makeMultiRegexp(" ,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a "));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a, "));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a ,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp(",a"));
        assertEquals("a", RegexpMaker.makeMultiRegexp(" a"));
        assertEquals("a", RegexpMaker.makeMultiRegexp(", a"));
        assertEquals("a", RegexpMaker.makeMultiRegexp(" ,a"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a b"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a,b"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a, b"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a ,b"));
        assertEquals(" ", RegexpMaker.makeMultiRegexp("\\ "));
        assertEquals("\\,", RegexpMaker.makeMultiRegexp("\\,"));
        assertEquals("\\,", RegexpMaker.makeMultiRegexp("\\, "));
        assertEquals(" ", RegexpMaker.makeMultiRegexp(",\\ "));
        assertEquals("\\, ", RegexpMaker.makeMultiRegexp("\\,\\ "));
        assertEquals(" ", RegexpMaker.makeMultiRegexp("\\ ,"));
        assertEquals("\\,", RegexpMaker.makeMultiRegexp(" \\,"));
        assertEquals(" \\,", RegexpMaker.makeMultiRegexp("\\ \\,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("\\a,"));
        assertEquals("a\\,", RegexpMaker.makeMultiRegexp("a\\,"));
        assertEquals("a\\,", RegexpMaker.makeMultiRegexp("\\a\\,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("\\a "));
        assertEquals("a ", RegexpMaker.makeMultiRegexp("a\\ "));
        assertEquals("a ", RegexpMaker.makeMultiRegexp("\\a\\ "));
        assertEquals("a|\\\\", RegexpMaker.makeMultiRegexp("a, \\"));
        assertEquals("a| ", RegexpMaker.makeMultiRegexp("a,\\ "));
        assertEquals("a| \\\\", RegexpMaker.makeMultiRegexp("a,\\ \\"));
        assertEquals("a\\,", RegexpMaker.makeMultiRegexp("a\\, "));
        assertEquals("a\\,|\\\\", RegexpMaker.makeMultiRegexp("a\\, \\"));
        assertEquals("a\\, ", RegexpMaker.makeMultiRegexp("a\\,\\ "));
        assertEquals("a\\, \\\\", RegexpMaker.makeMultiRegexp("a\\,\\ \\"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("\\a, "));
        assertEquals("a|\\\\", RegexpMaker.makeMultiRegexp("\\a, \\"));
        assertEquals("a| ", RegexpMaker.makeMultiRegexp("\\a,\\ "));
        assertEquals("a| \\\\", RegexpMaker.makeMultiRegexp("\\a,\\ \\"));
        assertEquals("a\\,", RegexpMaker.makeMultiRegexp("\\a\\, "));
        assertEquals("a\\,|\\\\", RegexpMaker.makeMultiRegexp("\\a\\, \\"));
        assertEquals("a\\, ", RegexpMaker.makeMultiRegexp("\\a\\,\\ "));
        assertEquals("a\\, \\\\", RegexpMaker.makeMultiRegexp("\\a\\,\\ \\"));
        assertEquals("a|\\\\", RegexpMaker.makeMultiRegexp("a ,\\"));
        assertEquals("a|\\,", RegexpMaker.makeMultiRegexp("a \\,"));
        assertEquals("a|\\,\\\\", RegexpMaker.makeMultiRegexp("a \\,\\"));
        assertEquals("a ", RegexpMaker.makeMultiRegexp("a\\ ,"));
        assertEquals("a |\\\\", RegexpMaker.makeMultiRegexp("a\\ ,\\"));
        assertEquals("a \\,", RegexpMaker.makeMultiRegexp("a\\ \\,"));
        assertEquals("a \\,\\\\", RegexpMaker.makeMultiRegexp("a\\ \\,\\"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("\\a ,"));
        assertEquals("a|\\\\", RegexpMaker.makeMultiRegexp("\\a ,\\"));
        assertEquals("a|\\,", RegexpMaker.makeMultiRegexp("\\a \\,"));
        assertEquals("a|\\,\\\\", RegexpMaker.makeMultiRegexp("\\a \\,\\"));
        assertEquals("a ", RegexpMaker.makeMultiRegexp("\\a\\ ,"));
        assertEquals("a |\\\\", RegexpMaker.makeMultiRegexp("\\a\\ ,\\"));
        assertEquals("a \\,", RegexpMaker.makeMultiRegexp("\\a\\ \\,"));
        assertEquals("a \\,\\\\", RegexpMaker.makeMultiRegexp("\\a\\ \\,\\"));
        
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a, b"));
        assertEquals("a|.*b.", RegexpMaker.makeMultiRegexp("a,*b?"));
        assertEquals("a|\\*b.", RegexpMaker.makeMultiRegexp("a,\\*b?"));
        assertEquals("a|.*b\\?", RegexpMaker.makeMultiRegexp("a,*b\\?"));
    }
    
}
