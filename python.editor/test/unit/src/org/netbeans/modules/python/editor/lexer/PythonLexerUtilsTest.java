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

package org.netbeans.modules.python.editor.lexer;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.python.editor.PythonTestBase;

/**
 *
 * @author Tor Norbye
 */
public class PythonLexerUtilsTest extends PythonTestBase {

    public PythonLexerUtilsTest(String name) {
        super(name);
    }

    public void testNarrow() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/extract1.py"));
        String s = doc.getText(0, doc.getLength());

        int start = getCaretOffset(s, "^# Beginning of extraction segment");
        int end = getCaretOffset(s, "# End of extraction segment^");
        OffsetRange range = new OffsetRange(start, end);

        OffsetRange result = PythonLexerUtils.narrow(doc, range, true);

        OffsetRange expected = new OffsetRange(
                getCaretOffset(s, "^read_after_block = 5"),
                getCaretOffset(s, "print param_read_in_block^")
                );

        assertEquals(expected, result);
    }

    public void testNarrow2() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/extract1.py"));
        String s = doc.getText(0, doc.getLength());

        int start = getCaretOffset(s, "^read_after_block = 5");
        int end = getCaretOffset(s, "read_after_block = 5^");
        OffsetRange range = new OffsetRange(start, end);

        OffsetRange result = PythonLexerUtils.narrow(doc, range, true);

        assertEquals(range, result);
    }

    public void testNarrow3() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/extract1.py"));
        String s = doc.getText(0, doc.getLength());

        int start = getCaretOffset(s, "^  read_after_block = 5");
        int end = getCaretOffset(s, "read_after_block = 5^");
        OffsetRange range = new OffsetRange(start, end);

        OffsetRange result = PythonLexerUtils.narrow(doc, range, true);

        OffsetRange expected = new OffsetRange(
                getCaretOffset(s, "^read_after_block = 5"),
                getCaretOffset(s, "read_after_block = 5^")
                );

        assertEquals(expected, result);
    }


    public void testNarrow4() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/extract1.py"));
        String s = doc.getText(0, doc.getLength());

        int start = getCaretOffset(s, "= 4\n^");
        int end = getCaretOffset(s, "^ print local1");
        OffsetRange range = new OffsetRange(start, end);

        OffsetRange result = PythonLexerUtils.narrow(doc, range, false);

        OffsetRange expected = new OffsetRange(
                getCaretOffset(s, "^# Beginning of extraction segment"),
                getCaretOffset(s, "# End of extraction segment^")
                );

        assertEquals(expected, result);
    }

    public void testNarrow5() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/extract1.py"));
        String s = doc.getText(0, doc.getLength());

        int start = getCaretOffset(s, "r^ead_after_block = 5");
        int end = getCaretOffset(s, "# End of extraction segment^");
        OffsetRange range = new OffsetRange(start, end);

        OffsetRange result = PythonLexerUtils.narrow(doc, range, true);
        assertEquals(OffsetRange.NONE, result);
    }

    public void testNarrow6() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/extract1.py"));
        String s = doc.getText(0, doc.getLength());

        int start = getCaretOffset(s, "^# Beginning of extraction segment");
        int end = getCaretOffset(s, "print param_read_in_bloc^k");
        OffsetRange range = new OffsetRange(start, end);

        OffsetRange result = PythonLexerUtils.narrow(doc, range, true);
        assertEquals(OffsetRange.NONE, result);
    }

    public void testFindSpaceBegin() {
        // Spaces in strings don't count
        String s = "x =  'f oo'";
        BaseDocument doc = getDocument(s);
        assertEquals(0, PythonLexerUtils.findSpaceBegin(doc, 0));
        assertEquals(1, PythonLexerUtils.findSpaceBegin(doc, 1));
        assertEquals(1, PythonLexerUtils.findSpaceBegin(doc, 2));
        assertEquals(3, PythonLexerUtils.findSpaceBegin(doc, 3));
        assertEquals(3, PythonLexerUtils.findSpaceBegin(doc, 4));
        assertEquals(3, PythonLexerUtils.findSpaceBegin(doc, 5));
        assertEquals(6, PythonLexerUtils.findSpaceBegin(doc, 6));
        assertEquals(7, PythonLexerUtils.findSpaceBegin(doc, 7));
        assertEquals(8, PythonLexerUtils.findSpaceBegin(doc, 8));
        assertEquals(9, PythonLexerUtils.findSpaceBegin(doc, 9));
        assertEquals(10, PythonLexerUtils.findSpaceBegin(doc, 10));
    }

    public void testFindSpaceBeginNoNewlines() {
        // Spaces in strings don't count
        String s = "x = \n 'f oo'";
        BaseDocument doc = getDocument(s);
        assertEquals(0, PythonLexerUtils.findSpaceBegin(doc, 0));
        assertEquals(1, PythonLexerUtils.findSpaceBegin(doc, 1));
        assertEquals(1, PythonLexerUtils.findSpaceBegin(doc, 2));
        assertEquals(3, PythonLexerUtils.findSpaceBegin(doc, 3));
        assertEquals(3, PythonLexerUtils.findSpaceBegin(doc, 4));
        assertEquals(5, PythonLexerUtils.findSpaceBegin(doc, 5));
        assertEquals(6, PythonLexerUtils.findSpaceBegin(doc, 6));
        assertEquals(7, PythonLexerUtils.findSpaceBegin(doc, 7));
        assertEquals(8, PythonLexerUtils.findSpaceBegin(doc, 8));
        assertEquals(9, PythonLexerUtils.findSpaceBegin(doc, 9));
        assertEquals(10, PythonLexerUtils.findSpaceBegin(doc, 10));
    }

    public void testFindSpaceBeginNoNewlinesBlank() {
        String s = "x = \n   ";
        BaseDocument doc = getDocument(s);
        assertEquals(0, PythonLexerUtils.findSpaceBegin(doc, 0));
        assertEquals(1, PythonLexerUtils.findSpaceBegin(doc, 1));
        assertEquals(1, PythonLexerUtils.findSpaceBegin(doc, 2));
        assertEquals(3, PythonLexerUtils.findSpaceBegin(doc, 3));
        assertEquals(3, PythonLexerUtils.findSpaceBegin(doc, 4));
        assertEquals(5, PythonLexerUtils.findSpaceBegin(doc, 5));
        assertEquals(6, PythonLexerUtils.findSpaceBegin(doc, 6));
        assertEquals(7, PythonLexerUtils.findSpaceBegin(doc, 7));
        assertEquals(8, PythonLexerUtils.findSpaceBegin(doc, 8));
    }

    public void testFindSpaceBeginLineContinuation() {
        String s = "foo(a, \n   f";
        BaseDocument doc = getDocument(s);
        assertEquals(0, PythonLexerUtils.findSpaceBegin(doc, 0));
        assertEquals(1, PythonLexerUtils.findSpaceBegin(doc, 1));
        assertEquals(2, PythonLexerUtils.findSpaceBegin(doc, 2));
        assertEquals(3, PythonLexerUtils.findSpaceBegin(doc, 3));
        assertEquals(4, PythonLexerUtils.findSpaceBegin(doc, 4));
        assertEquals(5, PythonLexerUtils.findSpaceBegin(doc, 5));
        assertEquals(6, PythonLexerUtils.findSpaceBegin(doc, 6));
        assertEquals(6, PythonLexerUtils.findSpaceBegin(doc, 7));
        assertEquals(6, PythonLexerUtils.findSpaceBegin(doc, 8));
        assertEquals(6, PythonLexerUtils.findSpaceBegin(doc, 9));
        assertEquals(6, PythonLexerUtils.findSpaceBegin(doc, 10));
        assertEquals(6, PythonLexerUtils.findSpaceBegin(doc, 11));
        assertEquals(12, PythonLexerUtils.findSpaceBegin(doc, 12));
    }
    public void testFindSpaceBeginBlanks() {
        String s = "  \n ";
        BaseDocument doc = getDocument(s);
        assertEquals(0, PythonLexerUtils.findSpaceBegin(doc, 0));
        assertEquals(1, PythonLexerUtils.findSpaceBegin(doc, 1));
        assertEquals(2, PythonLexerUtils.findSpaceBegin(doc, 2));
        assertEquals(3, PythonLexerUtils.findSpaceBegin(doc, 3));
        assertEquals(4, PythonLexerUtils.findSpaceBegin(doc, 4));
    }
}
