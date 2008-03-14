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

package org.netbeans.modules.java.editor.javadoc;

import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jan Pokorsky
 */
public class DocPositionsTest extends JavadocTestSupport {

    public DocPositionsTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(DocPositionsTest.class);
//        suite.addTest(new DocPositionsTest("testGetTag"));
//        suite.addTest(new DocPositionsTest("getTagSpan"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DocPositions.isTestMode = true;
    }
    
    public void testGetTag() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * HUH {@link String} GUG. {@code null}Second  sentence. <code>true {@link St</code>\n" +
                "  inside indent   * Second line  sentence.\n" +
                "     * @param m1 m1 {@#{link String ugly but valid} description\n" +
                "     * \n" +
                "     * @ no-name tag description\n" +
                "     * @@ at tag description\n" +
                "     * @return return description {@code unclosed in return\n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "/**";
        int offset = code.indexOf(what) + what.length();
        Doc javadoc = JavadocCompletionUtils.findJavadoc(info, doc, offset);
        assertNotNull(insertPointer(code, offset), javadoc);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(doc, offset);
        assertTrue(jdts.moveNext());
        assertNotNull(insertPointer(code, offset), jdts);
        
        DocPositions positions = DocPositions.get(info, javadoc, jdts);
        Tag[] inlineTags = javadoc.inlineTags();
        Tag[] tags = javadoc.tags();
        
        what = "{@link String} GUG";
        offset = code.indexOf(what);
        int offsetEnd = offset + what.length() - 4;
        Tag tag = positions.getTag(offset);
        assertEquals(logPositions(code, offset, offsetEnd, inlineTags[1], tag), inlineTags[1], tag);
        
        what = "{@code null}";
        offset = code.indexOf(what);
        offsetEnd = offset + what.length();
        tag = positions.getTag(offset);
        assertEquals(logPositions(code, offset, offsetEnd, inlineTags[3], tag), inlineTags[3], tag);
        
        what = "{@link St</code>\n";
        offset = code.indexOf(what);
        offsetEnd = code.indexOf("@param m1");
        tag = positions.getTag(offset);
        assertNotNull(logPositions(code, offset, positions), tag);
        assertEquals(logPositions(code, offset, positions), DocPositions.UNCLOSED_INLINE_TAG, tag.kind());
        int[] span = positions.getTagSpan(tag);
        assertNotNull(logPositions(code, offset, positions), span);
        assertTrue(logPositions(code, offset, offsetEnd, span), offset == span[0] && offsetEnd == span[1]);
        
        what = "@param m1";
        offset = code.indexOf(what);
        offsetEnd = code.indexOf("@ no-name");
        tag = positions.getTag(offset);
        assertEquals(logPositions(code, offset, offsetEnd, tags[0], tag), tags[0], tag);
        
        inlineTags = tag.inlineTags();
        what = "{@#{link String ugly but valid}";
        offset = code.indexOf(what);
        offsetEnd = offset + what.length();
        tag = positions.getTag(offset);
        assertEquals(logPositions(code, offset, offsetEnd, inlineTags[1], tag), inlineTags[1], tag);
        
        what = "@ no-name";
        offset = code.indexOf(what);
        offsetEnd = code.indexOf("@@ at");
        tag = positions.getTag(offset);
        assertEquals(logPositions(code, offset, offsetEnd, tags[1], tag), tags[1], tag);
        
        what = "@@ at";
        offset = code.indexOf(what);
        offsetEnd = code.indexOf("@return");
        tag = positions.getTag(offset);
        assertEquals(logPositions(code, offset, offsetEnd, tags[2], tag), tags[2], tag);
        
        what = "@return";
        offset = code.indexOf(what);
        offsetEnd = code.indexOf("*/\n");
        tag = positions.getTag(offset);
        assertEquals(logPositions(code, offset, offsetEnd, tags[3], tag), tags[3], tag);
        
        inlineTags = tag.inlineTags();
        what = "{@code unclosed in return";
        offset = code.indexOf(what);
        offsetEnd = offset + what.length();
        tag = positions.getTag(offset + 2);
//        assertEquals(logPositions(code, offset, offsetEnd, inlineTags[1], tag), inlineTags[1], tag);
        assertNotNull(logPositions(code, offset, positions), tag);
        assertEquals(logPositions(code, offset, positions), DocPositions.UNCLOSED_INLINE_TAG, tag.kind());
    }
    
    private static String logPositions(String code, int begin, int end, Tag exp, Tag found) {
        String expSnipped;
        try {
            expSnipped = code.substring(begin, end);
        } catch (IndexOutOfBoundsException ioobe) {
            expSnipped = "<IndexOutOfBoundsException>";
        }
        return "expected: [" + begin + ", " + end + "], expected: '" + expSnipped
                + "'\nexpec tag: '" + exp
                + "'\nfound tag: '" + found + "'";
    }
    
    private static String logPositions(String code, int begin, int end, int[] span) {
        String realBegin = span != null && span.length > 0? String.valueOf(span[0]): "null";
        String realEnd = span != null && span.length > 1? String.valueOf(span[1]): "null";
        String expSnipped;
        try {
            expSnipped = code.substring(begin, end);
        } catch (IndexOutOfBoundsException ioobe) {
            expSnipped = "<IndexOutOfBoundsException>";
        }
        String resSnipped;
        if (span != null && span.length == 2 && span[0] < span[1]) {
            resSnipped = code.substring(span[0], span[1]);
        } else {
            resSnipped = "<invalid>";
        }
        return "expected: [" + begin + ", " + end + "], result: [" + realBegin + ", " + realEnd + "]" +
                "\nexpected: '" + expSnipped + "', result: '" + resSnipped + "'";
    }

    private static String logPositions(String code, int offset, DocPositions dp) {
        return insertPointer(code, offset) + '\n' + dp;
    }
}
