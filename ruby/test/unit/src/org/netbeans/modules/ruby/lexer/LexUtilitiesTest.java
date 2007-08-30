/*
 * LexUtilitiesTest.java
 * JUnit based test
 *
 * Created on July 26, 2007, 9:46 PM
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
}
