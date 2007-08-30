/*
 * CallTest.java
 * JUnit based test
 *
 * Created on August 30, 2007, 2:58 PM
 */

package org.netbeans.modules.ruby.lexer;

import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.RubyTestBase;

/**
 *
 * @author Tor Norbye
 */
public class CallTest extends RubyTestBase {

    public CallTest(String testName) {
        super(testName);
    }

    private Call getCall(String source) {
        int caretPos = source.indexOf('^');

        source = source.substring(0, caretPos) + source.substring(caretPos + 1);

        BaseDocument doc = getDocument(source);

        TokenHierarchy<Document> th = TokenHierarchy.get((Document) doc);
        Call call = Call.getCallType(doc, th, caretPos);

        return call;
    }

    public void testCall1() throws Exception {
        Call call = getCall("File.ex^");
        assertEquals("File", call.getLhs());
        assertEquals("File", call.getType());
        assertTrue(call.isSimpleIdentifier());
        assertTrue(call.isStatic());
    }

    public void testCall1b() throws Exception {
        Call call = getCall("File::ex^");
        assertEquals("File", call.getLhs());
        assertEquals("File", call.getType());
        assertTrue(call.isSimpleIdentifier());
        assertTrue(call.isStatic());
    }

    public void testCall1c() throws Exception {
        Call call = getCall("File.ex^ ");
        assertEquals("File", call.getLhs());
        assertEquals("File", call.getType());
        assertTrue(call.isSimpleIdentifier());
        assertTrue(call.isStatic());
    }

    public void testCall2() throws Exception {
        Call call = getCall("xy.ex^");
        assertEquals("xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCall2b() throws Exception {
        Call call = getCall("xy.^");
        assertEquals("xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCall2c() throws Exception {
        Call call = getCall("xy.ex^ ");
        assertEquals("xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCall2d() throws Exception {
        Call call = getCall("xy.^ ");
        assertEquals("xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCall3() throws Exception {
        Call call = getCall("\"foo\".gsu^");
        assertEquals("String", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall3b() throws Exception {
        Call call = getCall("\"foo\".^");
        assertEquals("String", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall4() throws Exception {
        Call call = getCall("/foo/.gsu^");
        assertEquals("Regexp", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall4b() throws Exception {
        Call call = getCall("/foo/.^");
        assertEquals("Regexp", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall5() throws Exception {
        Call call = getCall("[1,2,3].each^");
        assertEquals("Array", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall5b() throws Exception {
        Call call = getCall("[1,2,3].^");
        assertEquals("Array", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall6() throws Exception {
        Call call = getCall("{:x=>:y}.foo^");
        assertEquals("Hash", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall6b() throws Exception {
        Call call = getCall("{:x=>:y}.^");
        assertEquals("Hash", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall7() throws Exception {
        Call call = getCall("50.ea^");
        assertEquals("Fixnum", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall7b() throws Exception {
        Call call = getCall("50.^");
        assertEquals("Fixnum", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall8() throws Exception {
        Call call = getCall("3.14.ea^");
        assertEquals("Float", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall8b() throws Exception {
        Call call = getCall("3.14.^");
        assertEquals("Float", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall9() throws Exception {
        Call call = getCall(":mysym.foo^");
        assertEquals("Symbol", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall9b() throws Exception {
        Call call = getCall(":mysym.^");
        assertEquals("Symbol", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }
    // This test doesn't work; the lexer believes this (erroneous input) is a number
    //public void testCall10() throws Exception {
    //    Call call = getCall("1..10.each^");
    //    assertEquals("Range", call.getType());
    //    assertFalse(call.isSimpleIdentifier());
    //    assertFalse(call.isStatic());
    //}

    // This test doesn't work; the lexer believes this (erroneous input) is a number
    //public void testCall10b() throws Exception {
    //    Call call = getCall("1..10.^");
    //    assertEquals("Range", call.getType());
    //    assertFalse(call.isSimpleIdentifier());
    //    assertFalse(call.isStatic());
    //}
    public void testCa11() throws Exception {
        Call call = getCall("nil.foo^");
        assertEquals("NilClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCal1b() throws Exception {
        Call call = getCall("nil.^");
        assertEquals("NilClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll2() throws Exception {
        Call call = getCall("true.foo^");
        assertEquals("TrueClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll2b() throws Exception {
        Call call = getCall("true.^");
        assertEquals("TrueClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll3() throws Exception {
        Call call = getCall("false.foo^");
        assertEquals("FalseClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll3b() throws Exception {
        Call call = getCall("false.^");
        assertEquals("FalseClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall14() throws Exception {
        Call call = getCall("self.foo^");
        assertEquals("self", call.getType());
        assertEquals("self", call.getLhs());
    }

    public void testCall14b() throws Exception {
        Call call = getCall("self.^");
        assertEquals("self", call.getType());
        assertEquals("self", call.getLhs());
    }

    public void testCalll5() throws Exception {
        Call call = getCall("super.foo^");
        assertEquals("super", call.getType());
        assertEquals("super", call.getLhs());
    }

    public void testCalll5b() throws Exception {
        Call call = getCall("super.^");
        assertEquals("super", call.getType());
        assertEquals("super", call.getLhs());
    }

    public void testCal16() throws Exception {
        Call call = getCall("Test::Unit::TestCase.ex^");
        assertEquals("Test::Unit::TestCase", call.getLhs());
        assertEquals("Test::Unit::TestCase", call.getType());
        assertTrue(call.isStatic());
    }

    public void testCalll7() throws Exception {
        Call call = getCall("@xy.ex^");
        assertEquals("@xy", call.getLhs());
        assertEquals(null, call.getType());
        assertTrue(!call.isStatic());
    }

    public void testCalll7b() throws Exception {
        Call call = getCall("@xy.^");
        assertEquals("@xy", call.getLhs());
        assertEquals(null, call.getType());
        assertTrue(!call.isStatic());
    }

    public void testCalll8() throws Exception {
        Call call = getCall("@@xy.ex^");
        assertEquals("@@xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCalll9() throws Exception {
        Call call = getCall("$xy.ex^");
        assertEquals("$xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCall20() throws Exception {
        Call call = getCall("foo.bar.ex^");
        assertEquals("foo.bar", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCallUnknown() throws Exception {
        Call call = getCall("getFoo().x^");
        assertSame(Call.UNKNOWN, call);
    }

    public void testCallLocal() throws Exception {
        Call call = getCall("foo^");
        assertSame(Call.LOCAL, call);
    }

    public void testCallNested() throws Exception {
        Call call = getCall("x=\"#{ File.ex^}\"");
        assertEquals("File", call.getType());
        assertTrue(call.isStatic());
    }

    // THIS IS BROKEN:
    //public void testCallNested2() throws Exception {
    //    Call call = getCall("x=\"#{ File.ex^ }\"");
    //    assertEquals("File", call.getType());
    //    assertTrue(call.isStatic());
    //}
}