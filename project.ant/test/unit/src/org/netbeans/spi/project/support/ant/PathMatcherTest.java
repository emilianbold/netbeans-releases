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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;

/**
 * Test of {@link PathMatcher}.
 * @author Jesse Glick
 */
public class PathMatcherTest extends NbTestCase {

    public PathMatcherTest(String n) {
        super(n);
    }

    private void assertMatches(String includes, String excludes, String path) {
        if (!new PathMatcher(includes, excludes, null).matches(path, false)) {
            fail("includes=" + includes + " excludes=" + excludes + " should have matched " + path);
        }
    }

    private void assertDoesNotMatch(String includes, String excludes, String path) {
        if (new PathMatcher(includes, excludes, null).matches(path, false)) {
            fail("includes=" + includes + " excludes=" + excludes + " should not have matched " + path);
        }
    }

    public void testPlainPaths() throws Exception {
        assertMatches("foo/", null, "foo/");
        assertDoesNotMatch("foo/", null, "foo");
        assertMatches("foo/", null, "foo/bar");
        assertMatches("foo/", null, "foo/bar/");
        assertDoesNotMatch("foo", null, "foo/");
        assertMatches("foo,bar", null, "foo");
        assertDoesNotMatch("foo,bar", "foo", "foo");
        assertDoesNotMatch("", null, "");
        try {
            new PathMatcher(null, null, null).matches(null, false);
            fail();
        } catch (Exception x) {}
    }

    public void testWildcards() throws Exception {
        assertMatches("foo/**", null, "foo/");
        assertDoesNotMatch("foo/**", null, "foo");
        assertMatches("foo/**", null, "foo/bar");
        assertMatches("foo/**", null, "foo/bar/");
        assertMatches("foo/**/bar", null, "foo/bar");
        assertMatches("**/foo", null, "foo");
        assertMatches("foo*bar", null, "foobar");
        assertMatches("foo*bar", null, "foo_bar");
        assertDoesNotMatch("foo*bar", null, "foo/bar");
        assertMatches("**/*.foo", null, "x/y/z.foo");
        assertMatches("**/*.foo", null, "z.foo");
        assertMatches("**", null, "");
        assertMatches("**", null, "a");
        assertMatches("**", null, "a/");
        assertMatches("**", null, "a/b");
        assertMatches("**", null, "a/b/");
    }

    public void testOddChars() throws Exception {
        assertMatches("foo$bar", null, "foo$bar");
        assertMatches("foo.bar", null, "foo.bar");
        assertDoesNotMatch("foo.bar", null, "foo_bar");
        assertMatches("\u011E", null, "\u011E");
    }

    public void testSeparators() throws Exception {
        assertMatches("foo bar", null, "foo");
        assertMatches("foo bar", null, "bar");
        assertDoesNotMatch("foo bar", null, "foo bar");
        assertMatches("foo*bar", null, "foo bar");
        assertMatches("  foo  bar  ", null, "foo");
        assertMatches("  foo  bar  ", null, "bar");
        assertMatches(",,foo,bar,,", null, "foo");
        assertMatches(",,foo,bar,,", null, "bar");
        assertMatches(" foo , bar ", null, "foo");
        assertMatches(" foo , bar ", null, "bar");
        assertMatches("foo\\bar", null, "foo/bar");
        assertDoesNotMatch("foo/bar", null, "foo\\bar");
        assertMatches("foo\\", null, "foo/");
        assertMatches("foo\\**", null, "foo/");
        assertMatches("foo\\**\\bar", null, "foo/bar");
    }

    private PathMatcher assertIncludedRoots(String includes, String excludes, String files, String... roots) throws Exception {
        clearWorkDir();
        File d = getWorkDir();
        for (String f : files.split(",")) {
            File create = new File(d, f);
            if (f.endsWith("/")) {
                create.mkdirs();
            } else {
                create.getParentFile().mkdirs();
                create.createNewFile();
            }
        }
        PathMatcher m = new PathMatcher(includes, excludes, d);
        SortedSet<File> actual = new TreeSet<File>(m.findIncludedRoots());
        SortedSet<File> expected = new TreeSet<File>();
        for (String root : roots) {
            expected.add(new File(d, root.replace('/', File.separatorChar)));
        }
        assertEquals("includes=" + includes + " excludes=" + excludes + " gave wrong roots with actual files " + files, setToS(expected), setToS(actual));
        return m;
    }
    private String setToS(Set<?> s) {
        return s.isEmpty() ? "nil" : s.toString();
    }

    public void testIncludedRoots() throws Exception {
        assertIncludedRoots("foo/,bar/", null, "foo/x,bar/x", "foo/", "bar/");
        assertIncludedRoots("foo/**,bar/**", null, "foo/x,bar/x", "foo/", "bar/");
        PathMatcher m = assertIncludedRoots("**/bar/", null, "foo/bar/baz", "foo/bar/");
        assertTrue(m.matches("", true));
        assertFalse(m.matches("", false));
        assertTrue(m.matches("foo/", true));
        assertFalse(m.matches("foo/", false));
        assertFalse(m.matches("foo/bar", true));
        assertFalse(m.matches("foo/bar", false));
        assertTrue(m.matches("foo/bar/", true));
        assertTrue(m.matches("foo/bar/", false));
        assertIncludedRoots("foo/,**/bar/", null, "foo/bar/baz", "foo/");
        assertIncludedRoots("foo/bar/baz", null, "foo/bar/baz", "foo/bar/");
        assertIncludedRoots(null, null, "", "");
        assertIncludedRoots("f,foo/", null, "f,foo/", "", "foo/");
        assertIncludedRoots("foo/", "foo/", "foo/"/*, nothing*/);
        assertIncludedRoots("foo/bar", null, ""/*, nothing*/);
        assertIncludedRoots("foo/bar", null, "foo/bar", "foo/");
        assertIncludedRoots("foo/,bar/,baz", null, "foo/,bar/,baz", "foo/", "bar/", "");
        assertIncludedRoots("**", null, "", "");
        new PathMatcher(null, null, new File("nonexistent")).findIncludedRoots(); // should not fail
        assertIncludedRoots("java/awt/ sun/awt/", null, "java/lang/Object.java,sun/awt/Mutex.java", "sun/awt/");
    }

}
