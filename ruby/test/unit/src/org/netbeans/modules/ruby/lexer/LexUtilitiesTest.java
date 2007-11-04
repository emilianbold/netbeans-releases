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
import junit.framework.TestCase;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.lexer.Call;

/**
 *
 * @author Tor Norbye
 */
public class LexUtilitiesTest extends RubyTestBase {
    
    public LexUtilitiesTest(String testName) {
        super(testName);
    }

//    public void testGetLexerOffset() {
//        System.out.println("getLexerOffset");
//        CompilationInfo info = null;
//        int astOffset = 0;
//        int expResult = 0;
//        int result = LexUtilities.getLexerOffset(info, astOffset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getLexerOffset method, of class LexUtilities. */
//
//    public void testGetRubyTokenSequence() {
//        System.out.println("getRubyTokenSequence");
//        BaseDocument doc = null;
//        int offset = 0;
//        TokenSequence expResult = null;
//        TokenSequence result = LexUtilities.getRubyTokenSequence(doc, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getRubyTokenSequence method, of class LexUtilities. */
//
//    public void testGetToken() {
//        System.out.println("getToken");
//        BaseDocument doc = null;
//        int offset = 0;
//        Token expResult = null;
//        Token result = LexUtilities.getToken(doc, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getToken method, of class LexUtilities. */
//
//    public void testGetTokenChar() {
//        System.out.println("getTokenChar");
//        BaseDocument doc = null;
//        int offset = 0;
//        char expResult = ' ';
//        char result = LexUtilities.getTokenChar(doc, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getTokenChar method, of class LexUtilities. */
//
//    public void testFindHeredocEnd() {
//        System.out.println("findHeredocEnd");
//        TokenSequence<? extends GsfTokenId> ts = null;
//        Token<? extends GsfTokenId> startToken = null;
//        OffsetRange expResult = null;
//        OffsetRange result = LexUtilities.findHeredocEnd(ts, startToken);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of findHeredocEnd method, of class LexUtilities. */
//
//    public void testFindHeredocBegin() {
//        System.out.println("findHeredocBegin");
//        TokenSequence<? extends GsfTokenId> ts = null;
//        Token<? extends GsfTokenId> endToken = null;
//        OffsetRange expResult = null;
//        OffsetRange result = LexUtilities.findHeredocBegin(ts, endToken);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of findHeredocBegin method, of class LexUtilities. */
//
//    public void testFindFwd() {
//        System.out.println("findFwd");
//        BaseDocument doc = null;
//        TokenSequence<? extends GsfTokenId> ts = null;
//        TokenId up = null;
//        TokenId down = null;
//        OffsetRange expResult = null;
//        OffsetRange result = LexUtilities.findFwd(doc, ts, up, down);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of findFwd method, of class LexUtilities. */
//
//    public void testFindBwd() {
//        System.out.println("findBwd");
//        BaseDocument doc = null;
//        TokenSequence<? extends GsfTokenId> ts = null;
//        TokenId up = null;
//        TokenId down = null;
//        OffsetRange expResult = null;
//        OffsetRange result = LexUtilities.findBwd(doc, ts, up, down);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of findBwd method, of class LexUtilities. */
//
//    public void testFindBegin() {
//        System.out.println("findBegin");
//        BaseDocument doc = null;
//        TokenSequence<? extends GsfTokenId> ts = null;
//        OffsetRange expResult = null;
//        OffsetRange result = LexUtilities.findBegin(doc, ts);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of findBegin method, of class LexUtilities. */
//
//    public void testFindEnd() {
//        System.out.println("findEnd");
//        BaseDocument doc = null;
//        TokenSequence<? extends GsfTokenId> ts = null;
//        OffsetRange expResult = null;
//        OffsetRange result = LexUtilities.findEnd(doc, ts);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of findEnd method, of class LexUtilities. */
//
//    public void testIsEndmatchingDo() {
//        System.out.println("isEndmatchingDo");
//        BaseDocument doc = null;
//        int offset = 0;
//        boolean expResult = false;
//        boolean result = LexUtilities.isEndmatchingDo(doc, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of isEndmatchingDo method, of class LexUtilities. */
//
//    public void testIsBeginToken() {
//        System.out.println("isBeginToken");
//        TokenId id = null;
//        BaseDocument doc = null;
//        int offset = 0;
//        boolean expResult = false;
//        boolean result = LexUtilities.isBeginToken(id, doc, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of isBeginToken method, of class LexUtilities. */
//
//    public void testIsIndentToken() {
//        System.out.println("isIndentToken");
//        TokenId id = null;
//        boolean expResult = false;
//        boolean result = LexUtilities.isIndentToken(id);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of isIndentToken method, of class LexUtilities. */
//
//    public void testGetBeginEndLineBalance() {
//        System.out.println("getBeginEndLineBalance");
//        BaseDocument doc = null;
//        int offset = 0;
//        int expResult = 0;
//        int result = LexUtilities.getBeginEndLineBalance(doc, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getBeginEndLineBalance method, of class LexUtilities. */
//
//    public void testGetLineBalance() {
//        System.out.println("getLineBalance");
//        BaseDocument doc = null;
//        int offset = 0;
//        TokenId up = null;
//        TokenId down = null;
//        int expResult = 0;
//        int result = LexUtilities.getLineBalance(doc, offset, up, down);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getLineBalance method, of class LexUtilities. */
//
//    public void testGetTokenBalance() throws Exception {
//        System.out.println("getTokenBalance");
//        BaseDocument doc = null;
//        TokenId open = null;
//        TokenId close = null;
//        int offset = 0;
//        int expResult = 0;
//        int result = LexUtilities.getTokenBalance(doc, open, close, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getTokenBalance method, of class LexUtilities. */
//
//    public void testGetLineIndent() {
//        System.out.println("getLineIndent");
//        BaseDocument doc = null;
//        int offset = 0;
//        int expResult = 0;
//        int result = LexUtilities.getLineIndent(doc, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getLineIndent method, of class LexUtilities. */
//
//    public void testIndent() {
//        System.out.println("indent");
//        StringBuilder sb = null;
//        int indent = 0;
//        LexUtilities.indent(sb, indent);
//        fail("The test case is a prototype.");
//    } /* Test of indent method, of class LexUtilities. */
//
//    public void testGetIndentString() {
//        System.out.println("getIndentString");
//        int indent = 0;
//        String expResult = "";
//        String result = LexUtilities.getIndentString(indent);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getIndentString method, of class LexUtilities. */
//
//    public void testIsCommentOnlyLine() throws Exception {
//        System.out.println("isCommentOnlyLine");
//        BaseDocument doc = null;
//        int offset = 0;
//        boolean expResult = false;
//        boolean result = LexUtilities.isCommentOnlyLine(doc, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of isCommentOnlyLine method, of class LexUtilities. */
//
//    public void testAdjustLineIndentation() {
//        System.out.println("adjustLineIndentation");
//        BaseDocument doc = null;
//        int offset = 0;
//        int adjustment = 0;
//        LexUtilities.adjustLineIndentation(doc, offset, adjustment);
//        fail("The test case is a prototype.");
//    } /* Test of adjustLineIndentation method, of class LexUtilities. */
//
//    public void testSetLineIndentation() {
//        System.out.println("setLineIndentation");
//        BaseDocument doc = null;
//        int offset = 0;
//        int indent = 0;
//        int expResult = 0;
//        int result = LexUtilities.setLineIndentation(doc, offset, indent);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of setLineIndentation method, of class LexUtilities. */
//
//    public void testGetStringAt() {
//        System.out.println("getStringAt");
//        int caretOffset = 0;
//        TokenHierarchy<Document> th = null;
//        String expResult = "";
//        String result = LexUtilities.getStringAt(caretOffset, th);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getStringAt method, of class LexUtilities. */
//
//    public void testGetRequireStringOffset() {
//        System.out.println("getRequireStringOffset");
//        int caretOffset = 0;
//        TokenHierarchy<Document> th = null;
//        int expResult = 0;
//        int result = LexUtilities.getRequireStringOffset(caretOffset, th);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getRequireStringOffset method, of class LexUtilities. */
//
//    public void testGetSingleQuotedStringOffset() {
//        System.out.println("getSingleQuotedStringOffset");
//        int caretOffset = 0;
//        TokenHierarchy<Document> th = null;
//        int expResult = 0;
//        int result = LexUtilities.getSingleQuotedStringOffset(caretOffset, th);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getSingleQuotedStringOffset method, of class LexUtilities. */
//
//    public void testGetDoubleQuotedStringOffset() {
//        System.out.println("getDoubleQuotedStringOffset");
//        int caretOffset = 0;
//        TokenHierarchy<Document> th = null;
//        int expResult = 0;
//        int result = LexUtilities.getDoubleQuotedStringOffset(caretOffset, th);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getDoubleQuotedStringOffset method, of class LexUtilities. */
//
//    public void testGetRegexpOffset() {
//        System.out.println("getRegexpOffset");
//        int caretOffset = 0;
//        TokenHierarchy<Document> th = null;
//        int expResult = 0;
//        int result = LexUtilities.getRegexpOffset(caretOffset, th);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getRegexpOffset method, of class LexUtilities. */
//
//    public void testGetCallType() {
//        System.out.println("getCallType");
//        BaseDocument doc = null;
//        TokenHierarchy<Document> th = null;
//        int offset = 0;
//        Call expResult = null;
//        Call result = LexUtilities.getCallType(doc, th, offset);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getCallType method, of class LexUtilities. */
//
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
}
