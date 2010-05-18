/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.python.editor;

/**
 *
 * @author Tor Norbye
 */
public class PythonFormatterTest extends PythonTestBase {

    public PythonFormatterTest(String testName) {
        super(testName);
    }

    public void testFormatFile1() throws Exception {
        reformatFileContents("testfiles/ConfigParser.py", new IndentPrefs(4,4));
    }

    public void testFormatFile2() throws Exception {
        reformatFileContents("testfiles/imports/organize1.py", new IndentPrefs(4,4));
    }

    public void testFormatFile3() throws Exception {
        reformatFileContents("testfiles/datetime.py", new IndentPrefs(4,4));
    }

    public void testFormatFile4() throws Exception {
        reformatFileContents("testfiles/formatting.py", new IndentPrefs(4,4));
    }

    public void testFormatFile5() throws Exception {
        reformatFileContents("testfiles/hanging_indent.py", new IndentPrefs(2,2));
    }

    public void testFormatFile6() throws Exception {
        reformatFileContents("testfiles/hanging_indent2.py", new IndentPrefs(2,2));
    }

    public void testStarArg() throws Exception {
        reformatFileContents("testfiles/star_arg.py", new IndentPrefs(4,4));
    }

    public void testSimple1() throws Exception {
        format("if true\n foo\nelse\n bar\n",
               "if true\n    foo\nelse\n    bar\n", null);
    }

    public void testSimple2() throws Exception {
        format("   if true\n    foo\n   else\n    bar\n",
               "   if true\n       foo\n   else\n       bar\n", null);
    }

    public void testSimple3() throws Exception {
        format("if true\n foo\nelse\n  bar\n",
               "if true\n    foo\nelse\n    bar\n", null);
    }

    public void testSimple4() throws Exception {
        format("if true\n %<%foo\nelse\n  bar\n%>%",
               "if true\n    foo\nelse\n    bar\n", null);
    }

    public void testSimple5() throws Exception {
        format("if \"%\" in v:\n    self._interpolate_some(option, accum, v,\n       section, map, depth + 1)\nelse:\n     accum.append(v)\n",
               "if \"%\" in v:\n    self._interpolate_some(option, accum, v,\n                           section, map, depth + 1)\nelse:\n    accum.append(v)\n",
               null);
    }

    public void testSimple6() throws Exception {
        format("class Foo\n  def bar\n    baz\nclass Bar\n",
               "class Foo\n    def bar\n        baz\nclass Bar\n", null);
    }

    public void testInconsistent() throws Exception {
        format("if true\n  bar\n  if false\n    hello\nif false\n    baz",
               "if true\n    bar\n    if false\n        hello\nif false\n    baz",
                null);
    }

    public void testInconsistent2() throws Exception {
        format("if true\n  bar\n  %<%if false\n    hello\nif false\n    baz%>%",
               "if true\n  bar\n    if false\n        hello\nif false\n    baz",
                null);
    }

    public void testSimple7() throws Exception {
        format("if true\n  if bar\n    baz\nhello",
                "if true\n    if bar\n        baz\nhello", null);
    }

    public void testSimple8() throws Exception {
        format(
                "'''This is my string\n  which\n should\n   not be changed'''",
                "'''This is my string\n  which\n should\n   not be changed'''", null);
    }
    
    public void testSimple9() throws Exception {
        format(
                "'''This is my string\n  which\n should\n   not be changed'''\nhello\nworld\n",
                "'''This is my string\n  which\n should\n   not be changed'''\nhello\nworld\n", null);
    }

    // Test: Try removing all WHITESPACE in the document (except for line prefixes) and then reformat and compare with master!
    // Also try doubling all space!
}
