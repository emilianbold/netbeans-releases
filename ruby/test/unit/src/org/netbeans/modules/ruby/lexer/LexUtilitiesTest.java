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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.ruby.lexer;

import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.RubyTestBase;

/**
 *
 * @author Tor Norbye
 */
public class LexUtilitiesTest extends RubyTestBase {
    
    public LexUtilitiesTest(String testName) {
        super(testName);
    }

    public void testIsInsideQuotedString() {
        String s = "x = \"foo\" ";
        BaseDocument doc = getDocument(s);
        for (int i = 0; i < 4; i++) {
            assertFalse(LexUtilities.isInsideQuotedString(doc, i));
        }
        for (int i = 5; i <= 8; i++) {
            assertTrue(LexUtilities.isInsideQuotedString(doc, i));
        }
        for (int i = 9; i < s.length(); i++) {
            assertFalse(LexUtilities.isInsideQuotedString(doc, i));
        }
    }
    
    public void testIsInsideEmptyQuotedString() {
        String s = "x = \"\"";
        BaseDocument doc = getDocument(s);
        for (int i = 0; i < 4; i++) {
            assertFalse(LexUtilities.isInsideQuotedString(doc, i));
        }
        assertTrue(LexUtilities.isInsideQuotedString(doc, 5));
        assertFalse(LexUtilities.isInsideQuotedString(doc, 6));
    }

    public void testIsInsideQuotedStringNegative() {
        String s = "x = 'foo'";
        BaseDocument doc = getDocument(s);
        for (int i = 0; i < s.length(); i++) {
            assertFalse(LexUtilities.isInsideQuotedString(doc, i));
        }
    }

    public void testIsInsideRegexp() {
        String s = "x = /foo/ ";
        BaseDocument doc = getDocument(s);
        for (int i = 0; i < 4; i++) {
            assertFalse(LexUtilities.isInsideRegexp(doc, i));
        }
        for (int i = 5; i <= 8; i++) {
            assertTrue(LexUtilities.isInsideRegexp(doc, i));
        }
        for (int i = 9; i < s.length(); i++) {
            assertFalse(LexUtilities.isInsideRegexp(doc, i));
        }
    }
    
    public void testIsInsideEmptyRegexp() {
        String s = "x = //";
        BaseDocument doc = getDocument(s);
        for (int i = 0; i < 4; i++) {
            assertFalse(LexUtilities.isInsideRegexp(doc, i));
        }
        assertTrue(LexUtilities.isInsideRegexp(doc, 5));
        assertFalse(LexUtilities.isInsideRegexp(doc, 6));
    }

    public void testIsInsideRegexpNegative() {
        String s = "x = 'foo'";
        BaseDocument doc = getDocument(s);
        for (int i = 0; i < s.length(); i++) {
            assertFalse(LexUtilities.isInsideRegexp(doc, i));
        }
    }
    
    public void testFindSpaceBegin() {
        // Spaces in strings don't count
        String s = "x =  'f oo'";
        BaseDocument doc = getDocument(s);
        assertEquals(0, LexUtilities.findSpaceBegin(doc, 0));
        assertEquals(1, LexUtilities.findSpaceBegin(doc, 1));
        assertEquals(1, LexUtilities.findSpaceBegin(doc, 2));
        assertEquals(3, LexUtilities.findSpaceBegin(doc, 3));
        assertEquals(3, LexUtilities.findSpaceBegin(doc, 4));
        assertEquals(3, LexUtilities.findSpaceBegin(doc, 5));
        assertEquals(6, LexUtilities.findSpaceBegin(doc, 6));
        assertEquals(7, LexUtilities.findSpaceBegin(doc, 7));
        assertEquals(8, LexUtilities.findSpaceBegin(doc, 8));
        assertEquals(9, LexUtilities.findSpaceBegin(doc, 9));
        assertEquals(10, LexUtilities.findSpaceBegin(doc, 10));
    }

    public void testFindSpaceBeginNoNewlines() {
        // Spaces in strings don't count
        String s = "x = \n 'f oo'";
        BaseDocument doc = getDocument(s);
        assertEquals(0, LexUtilities.findSpaceBegin(doc, 0));
        assertEquals(1, LexUtilities.findSpaceBegin(doc, 1));
        assertEquals(1, LexUtilities.findSpaceBegin(doc, 2));
        assertEquals(3, LexUtilities.findSpaceBegin(doc, 3));
        assertEquals(3, LexUtilities.findSpaceBegin(doc, 4));
        assertEquals(5, LexUtilities.findSpaceBegin(doc, 5));
        assertEquals(6, LexUtilities.findSpaceBegin(doc, 6));
        assertEquals(7, LexUtilities.findSpaceBegin(doc, 7));
        assertEquals(8, LexUtilities.findSpaceBegin(doc, 8));
        assertEquals(9, LexUtilities.findSpaceBegin(doc, 9));
        assertEquals(10, LexUtilities.findSpaceBegin(doc, 10));
    }

    public void testFindSpaceBeginNoNewlinesBlank() {
        String s = "x = \n   ";
        BaseDocument doc = getDocument(s);
        assertEquals(0, LexUtilities.findSpaceBegin(doc, 0));
        assertEquals(1, LexUtilities.findSpaceBegin(doc, 1));
        assertEquals(1, LexUtilities.findSpaceBegin(doc, 2));
        assertEquals(3, LexUtilities.findSpaceBegin(doc, 3));
        assertEquals(3, LexUtilities.findSpaceBegin(doc, 4));
        assertEquals(5, LexUtilities.findSpaceBegin(doc, 5));
        assertEquals(6, LexUtilities.findSpaceBegin(doc, 6));
        assertEquals(7, LexUtilities.findSpaceBegin(doc, 7));
        assertEquals(8, LexUtilities.findSpaceBegin(doc, 8));
    }

    public void testFindSpaceBeginLineContinuation() {
        String s = "foo(a, \n   f";
        BaseDocument doc = getDocument(s);
        assertEquals(0, LexUtilities.findSpaceBegin(doc, 0));
        assertEquals(1, LexUtilities.findSpaceBegin(doc, 1));
        assertEquals(2, LexUtilities.findSpaceBegin(doc, 2));
        assertEquals(3, LexUtilities.findSpaceBegin(doc, 3));
        assertEquals(4, LexUtilities.findSpaceBegin(doc, 4));
        assertEquals(5, LexUtilities.findSpaceBegin(doc, 5));
        assertEquals(6, LexUtilities.findSpaceBegin(doc, 6));
        assertEquals(6, LexUtilities.findSpaceBegin(doc, 7));
        assertEquals(6, LexUtilities.findSpaceBegin(doc, 8));
        assertEquals(6, LexUtilities.findSpaceBegin(doc, 9));
        assertEquals(6, LexUtilities.findSpaceBegin(doc, 10));
        assertEquals(6, LexUtilities.findSpaceBegin(doc, 11));
        assertEquals(12, LexUtilities.findSpaceBegin(doc, 12));
    }
    public void testFindSpaceBeginBlanks() {
        String s = "  \n ";
        BaseDocument doc = getDocument(s);
        assertEquals(0, LexUtilities.findSpaceBegin(doc, 0));
        assertEquals(1, LexUtilities.findSpaceBegin(doc, 1));
        assertEquals(2, LexUtilities.findSpaceBegin(doc, 2));
        assertEquals(3, LexUtilities.findSpaceBegin(doc, 3));
        assertEquals(4, LexUtilities.findSpaceBegin(doc, 4));
    }

    public void testGetStringAt() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/ape.rb"));
        String text = doc.getText(0, doc.getLength());
        int lineOffset = text.indexOf("require 'service'");
        assertTrue(lineOffset != -1);
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        assertNotNull(th);
        int caretOffset = text.indexOf("ervic", lineOffset);
        String stringAt = LexUtilities.getStringAt(caretOffset, th);
        assertEquals("service", stringAt);
        int offset = LexUtilities.getRequireStringOffset(caretOffset, th);
        assertEquals(lineOffset + "require '".length(), offset);

        caretOffset = text.indexOf("service", lineOffset);
        stringAt = LexUtilities.getStringAt(caretOffset, th);
        assertEquals("service", stringAt);
        offset = LexUtilities.getRequireStringOffset(caretOffset, th);
        assertEquals(lineOffset + "require '".length(), offset);

        caretOffset = text.indexOf("service", lineOffset) + "service".length();
        stringAt = LexUtilities.getStringAt(caretOffset, th);
        assertEquals("service", stringAt);
        offset = LexUtilities.getRequireStringOffset(caretOffset, th);
        assertEquals(lineOffset + "require '".length(), offset);

        // Look for trouble - spot sample
        for (int i = 0, length = doc.getLength(); i < length; i += length/100) {
            LexUtilities.getStringAt(i, th);
            LexUtilities.getRequireStringOffset(i, th);
        }
    }

    public void testGetStringAt2() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/active_record.rb"));
        String text = doc.getText(0, doc.getLength());
        int lineOffset = text.indexOf("require \"active_record/connection_adapters");
        assertTrue(lineOffset != -1);
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        assertNotNull(th);
        int caretOffset = text.indexOf("ctive", lineOffset);
        String stringAt = LexUtilities.getStringAt(caretOffset, th);
        assertEquals("active_record/connection_adapters/", stringAt);
        int offset = LexUtilities.getRequireStringOffset(caretOffset, th);
        assertEquals(lineOffset + "require \"".length(), offset);
    }

    public void testQuotedLiteralString() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/ape.rb"));
        String text = doc.getText(0, doc.getLength());
        int lineOffset = text.indexOf("good \"Will use collection");
        assertTrue(lineOffset != -1);
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        assertNotNull(th);
        int caretOffset = text.indexOf("use", lineOffset);
        int offset = LexUtilities.getDoubleQuotedStringOffset(caretOffset, th);
        assertEquals(text.indexOf('"', lineOffset) + 1, offset);

        caretOffset = text.indexOf("Will", lineOffset);
        offset = LexUtilities.getDoubleQuotedStringOffset(caretOffset, th);
        assertEquals(text.indexOf('"', lineOffset) + 1, offset);

        caretOffset = text.indexOf("collection", lineOffset + "collection".length());
        offset = LexUtilities.getDoubleQuotedStringOffset(caretOffset, th);
        assertEquals(text.indexOf('"', lineOffset) + 1, offset);

        caretOffset = text.indexOf("good", lineOffset);
        offset = LexUtilities.getDoubleQuotedStringOffset(caretOffset, th);
        assertEquals(-1, offset);

        // Look for trouble - spot sample
        for (int i = 0, length = doc.getLength(); i < length; i += length/100) {
            LexUtilities.getDoubleQuotedStringOffset(i, th);
        }
    }

    public void testSingleLiteralString() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/ape.rb"));
        String text = doc.getText(0, doc.getLength());
        int lineOffset = text.indexOf("Feed.read(coll.href, 'Pictures from multi-post'");
        assertTrue(lineOffset != -1);
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        assertNotNull(th);
        int caretOffset = text.indexOf("from", lineOffset);
        int offset = LexUtilities.getSingleQuotedStringOffset(caretOffset, th);
        assertEquals(text.indexOf('\'', lineOffset) + 1, offset);

        caretOffset = text.indexOf("Pictures", lineOffset);
        offset = LexUtilities.getSingleQuotedStringOffset(caretOffset, th);
        assertEquals(text.indexOf('\'', lineOffset) + 1, offset);

        caretOffset = text.indexOf("post", lineOffset + "post".length());
        offset = LexUtilities.getSingleQuotedStringOffset(caretOffset, th);
        assertEquals(text.indexOf('\'', lineOffset) + 1, offset);

        caretOffset = text.indexOf("good", lineOffset);
        offset = LexUtilities.getSingleQuotedStringOffset(caretOffset, th);
        assertEquals(-1, offset);

        // Look for trouble - spot sample
        for (int i = 0, length = doc.getLength(); i < length; i += length/100) {
            LexUtilities.getSingleQuotedStringOffset(i, th);
        }
    }

    public void testRegexpLiteralString() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/ape.rb"));
        String text = doc.getText(0, doc.getLength());
        int lineOffset = text.indexOf("$!.to_s.gsub(/\\n/,");
        assertTrue(lineOffset != -1);
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        assertNotNull(th);
        int caretOffset = text.indexOf("\\n/", lineOffset);
        int offset = LexUtilities.getRegexpOffset(caretOffset, th);
        assertEquals(text.indexOf('/', lineOffset) + 1, offset);

        caretOffset = text.indexOf("n/", lineOffset);
        offset = LexUtilities.getRegexpOffset(caretOffset, th);
        assertEquals(text.indexOf('/', lineOffset) + 1, offset);

        caretOffset = text.indexOf("\\n/", lineOffset + "\\n".length());
        offset = LexUtilities.getRegexpOffset(caretOffset, th);
        assertEquals(text.indexOf('/', lineOffset) + 1, offset);

        caretOffset = text.indexOf("good", lineOffset);
        offset = LexUtilities.getRegexpOffset(caretOffset, th);
        assertEquals(-1, offset);

        // Look for trouble - spot sample
        for (int i = 0, length = doc.getLength(); i < length; i += length/100) {
            LexUtilities.getRegexpOffset(i, th);
        }
    }

    public void testGetClassNameAt() throws Exception {
        BaseDocument doc = getDocument(getTestFile("testfiles/ar-associations/user.rb"));
        String text = doc.getText(0, doc.getLength());
        int lineOffset = text.indexOf("class_name => \"UserDetail\"");
        assertTrue(lineOffset != -1);
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        assertNotNull(th);
        int caretOffset = text.indexOf("rDetail", lineOffset);
        String stringAt = LexUtilities.getStringAt(caretOffset, th);
        assertEquals("UserDetail", stringAt);
        int offset = LexUtilities.getClassNameStringOffset(caretOffset, th);
        assertEquals(lineOffset + "class_name => \"".length(), offset);
    }

}
