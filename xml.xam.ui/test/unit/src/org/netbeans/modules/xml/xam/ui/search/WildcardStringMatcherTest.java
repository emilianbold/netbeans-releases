/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.xml.xam.ui.search;

import junit.framework.TestCase;

/**
 * Unit test for WildcardStringMatcher class.
 *
 * @author Nathan Fiedler
 */
public class WildcardStringMatcherTest extends TestCase {
    
    public WildcardStringMatcherTest(String testName) {
        super(testName);
    }

    /**
     * Test of containsWildcards method, of class match.WildcardStringMatcher.
     */
    public void testContainsWildcards() {
        assertTrue(WildcardStringMatcher.containsWildcards("a*"));
        assertTrue(WildcardStringMatcher.containsWildcards("a?"));
        assertTrue(WildcardStringMatcher.containsWildcards("a*b"));
        assertTrue(WildcardStringMatcher.containsWildcards("a?b"));
        assertTrue(WildcardStringMatcher.containsWildcards("*b"));
        assertTrue(WildcardStringMatcher.containsWildcards("?b"));
        assertTrue(WildcardStringMatcher.containsWildcards("*"));
        assertTrue(WildcardStringMatcher.containsWildcards("?"));
        assertFalse(WildcardStringMatcher.containsWildcards("abc"));
    }

    /**
     * Test of match method, of class match.WildcardStringMatcher.
     */
    public void testMatch() {
        TestData[] data;
        data = new TestData[] {
            new TestData("foo", true),
            new TestData("ffoo", false),
            new TestData("fofoo", false),
            new TestData("foobar", true),
            new TestData("fofoobar", false),
            new TestData("barfoo", false),
            new TestData("barfoofo", false),
            new TestData("barfoobaz", false),
            new TestData("foofoofoo", true),
            new TestData("fofofoo", false),
            new TestData("", false),
        };
        performTest(data, "foo*");

        data = new TestData[] {
            new TestData("foo", true),
            new TestData("foobar", false),
            new TestData("barfoo", true),
            new TestData("barfoobaz", false),
            new TestData("foofoofoo", true),
            new TestData("fofofoo", true),
            new TestData("", false),
        };
        performTest(data, "*foo");

        data = new TestData[] {
            new TestData("foo", true),
            new TestData("foobar", false),
            new TestData("barfoo", true),
            new TestData("barfoobaz", false),
            new TestData("foofoofoo", true),
            new TestData("fofofoo", true),
            new TestData("", false),
        };
        performTest(data, "**foo");

        data = new TestData[] {
            new TestData("foo", false),
            new TestData("foobar", false),
            new TestData("barfoo", true),
            new TestData("barfoobaz", true),
            new TestData("foofoofoo", false),
            new TestData("", false),
        };
        performTest(data, "bar*");

        data = new TestData[] {
            new TestData("foo", false),
            new TestData("foobar", false),
            new TestData("barfoo", true),
            new TestData("barfoobaz", true),
            new TestData("foofoofoo", false),
            new TestData("", false),
        };
        performTest(data, "bar**");

        data = new TestData[] {
            new TestData("bar", false),
            new TestData("bar1", true),
            new TestData("barZ", true),
            new TestData("1bar", false),
            new TestData("foofoofoo", false),
            new TestData("", false),
        };
        performTest(data, "bar?");

        data = new TestData[] {
            new TestData("foo", true),
            new TestData("bar", true),
            new TestData("baz", true),
            new TestData("", true),
        };
        performTest(data, "*");

        data = new TestData[] {
            new TestData("foobarbaz", true),
            new TestData("foobar", true),
            new TestData("foo", true),
            new TestData("fo", true),
            new TestData("f", true),
            new TestData("", true),
        };
        performTest(data, "***");

        data = new TestData[] {
            new TestData("foobarbaz", true),
            new TestData("foo", true),
            new TestData("ba", true),
            new TestData("b", false),
            new TestData("", false),
        };
        performTest(data, "*?*?*");

        data = new TestData[] {
            new TestData("f", true),
            new TestData("b", true),
            new TestData("foo", false),
            new TestData("", false),
        };
        performTest(data, "?");

        data = new TestData[] {
            new TestData("a", true),
            new TestData("1a", true),
            new TestData("aaa", true),
            new TestData("cba", true),
            new TestData("abc", false),
            new TestData("c", false),
            new TestData("", false),
        };
        performTest(data, "*a");

        data = new TestData[] {
            new TestData("foo", true),
            new TestData("bar", true),
            new TestData("foobar", false),
            new TestData("", false),
        };
        performTest(data, "???");

        data = new TestData[] {
            new TestData("fooabcbar1baz", true),
            new TestData("foobarbarbar!baz", true),
            new TestData("foobarXbaz", true),
            new TestData("foobarbaz", false),
            new TestData("bar", false),
            new TestData("baz", false),
            new TestData("", false),
        };
        performTest(data, "foo*bar?baz");

        data = new TestData[] {
            new TestData("fooabc123baz", true),
            new TestData("foobarbarbar!baz", true),
            new TestData("foobarXbaz", true),
            new TestData("foobarbaz", true),
            new TestData("foobaz", false),
            new TestData("foobar", false),
            new TestData("foo123bar", false),
            new TestData("", false),
        };
        performTest(data, "foo*???baz");

        data = new TestData[] {
            new TestData("hereheroherr", true),
            new TestData("herheroher", true),
            new TestData("abcherodef", true),
            new TestData("hero", true),
            new TestData("heroherohero", true),
            new TestData("herehero", true),
            new TestData("heroherr", true),
            new TestData("he1ro", false),
            new TestData("", false),
        };
        performTest(data, "*hero*");
    }

    /**
     * Perform tests on the given data.
     *
     * @param  data   set of test data (text and expected result).
     * @param  query  the wildcard query string.
     */
    private void performTest(TestData[] data, String query) {
        for (TestData datum : data) {
            String text = datum.getText();
            boolean matches = WildcardStringMatcher.match(text, query);
            if (matches != datum.match) {
                if (matches) {
                    fail("Mistakenly found " + query + " in \"" + text + '"');
                } else {
                    fail("Did not find " + query + " in \"" + text + '"');
                }
            }
        }
    }

    /**
     * Holds the test data for testing the string matcher.
     */
    private static class TestData {
        private String text;
        private boolean match;

        public TestData(String text, boolean match) {
            this.text = text;
            this.match = match;
        }

        public String getText() {
            return text;
        }

        public boolean shouldMatch() {
            return match;
        }
    }
}
