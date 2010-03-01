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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import org.netbeans.modules.ruby.RubyType;

/**
 *
 * @author Tor Norbye
 */
public class CallTest extends RubyTestBase {

    public CallTest(String testName) {
        super(testName);
    }

    private void assertType(String expected, RubyType actualType) {
        RubyType expectedType = expected == null ? RubyType.unknown() : RubyType.create(expected);
        assertEquals(expectedType, actualType);
    }

    private Call getCall(String source) {
        int caretPos = source.indexOf('^');
        assertTrue("No ^ marker for the caret in the text", caretPos != -1);

        source = source.substring(0, caretPos) + source.substring(caretPos + 1);

        BaseDocument doc = getDocument(source);

        TokenHierarchy<Document> th = TokenHierarchy.get((Document) doc);
        Call call = Call.getCallType(doc, th, caretPos);

        return call;
    }

    public void testCall1() throws Exception {
        Call call = getCall("File.ex^");
        assertEquals("File", call.getLhs());
        assertType("File", call.getType());
        assertTrue(call.isSimpleIdentifier());
        assertTrue(call.isStatic());
    }

    public void testCall1b() throws Exception {
        Call call = getCall("File::ex^");
        assertEquals("File", call.getLhs());
        assertType("File", call.getType());
        assertTrue(call.isSimpleIdentifier());
        assertTrue(call.isStatic());
    }

    public void testCall1c() throws Exception {
        Call call = getCall("File.ex^ ");
        assertEquals("File", call.getLhs());
        assertType("File", call.getType());
        assertTrue(call.isSimpleIdentifier());
        assertTrue(call.isStatic());
    }

    public void testCall2() throws Exception {
        Call call = getCall("xy.ex^");
        assertEquals("xy", call.getLhs());
        assertType(null, call.getType());
    }

    public void testCall2b() throws Exception {
        Call call = getCall("xy.^");
        assertEquals("xy", call.getLhs());
        assertType(null, call.getType());
    }

    public void testCall2c() throws Exception {
        Call call = getCall("xy.ex^ ");
        assertEquals("xy", call.getLhs());
        assertType(null, call.getType());
    }

    public void testCall2d() throws Exception {
        Call call = getCall("xy.^ ");
        assertEquals("xy", call.getLhs());
        assertType(null, call.getType());
    }

    public void testCall3() throws Exception {
        Call call = getCall("\"foo\".gsu^");
        assertType("String", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall3b() throws Exception {
        Call call = getCall("\"foo\".^");
        assertType("String", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall4() throws Exception {
        Call call = getCall("/foo/.gsu^");
        assertType("Regexp", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall4b() throws Exception {
        Call call = getCall("/foo/.^");
        assertType("Regexp", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall5() throws Exception {
        Call call = getCall("[1,2,3].each^");
        assertType("Array", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall5b() throws Exception {
        Call call = getCall("[1,2,3].^");
        assertType("Array", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall6() throws Exception {
        Call call = getCall("{:x=>:y}.foo^");
        assertType("Hash", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall6b() throws Exception {
        Call call = getCall("{:x=>:y}.^");
        assertType("Hash", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall7() throws Exception {
        Call call = getCall("50.ea^");
        assertType("Fixnum", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall7b() throws Exception {
        Call call = getCall("50.^");
        assertType("Fixnum", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall8() throws Exception {
        Call call = getCall("3.14.ea^");
        assertType("Float", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall8b() throws Exception {
        Call call = getCall("3.14.^");
        assertType("Float", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall9() throws Exception {
        Call call = getCall(":mysym.foo^");
        assertType("Symbol", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall9b() throws Exception {
        Call call = getCall(":mysym.^");
        assertType("Symbol", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }
    // This test doesn't work; the lexer believes this (erroneous input) is a number
    //public void testCall10() throws Exception {
    //    Call call = getCall("1..10.each^");
    //    assertType("Range", call.getType());
    //    assertFalse(call.isSimpleIdentifier());
    //    assertFalse(call.isStatic());
    //}

    // This test doesn't work; the lexer believes this (erroneous input) is a number
    //public void testCall10b() throws Exception {
    //    Call call = getCall("1..10.^");
    //    assertType("Range", call.getType());
    //    assertFalse(call.isSimpleIdentifier());
    //    assertFalse(call.isStatic());
    //}
    public void testCa11() throws Exception {
        Call call = getCall("nil.foo^");
        assertType("NilClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCal1b() throws Exception {
        Call call = getCall("nil.^");
        assertType("NilClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll2() throws Exception {
        Call call = getCall("true.foo^");
        assertType("TrueClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll2b() throws Exception {
        Call call = getCall("true.^");
        assertType("TrueClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll3() throws Exception {
        Call call = getCall("false.foo^");
        assertType("FalseClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll3b() throws Exception {
        Call call = getCall("false.^");
        assertType("FalseClass", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall14() throws Exception {
        Call call = getCall("self.foo^");
        assertType("self", call.getType());
        assertEquals("self", call.getLhs());
    }

    public void testCall14b() throws Exception {
        Call call = getCall("self.^");
        assertType("self", call.getType());
        assertEquals("self", call.getLhs());
    }

    public void testCalll5() throws Exception {
        Call call = getCall("super.foo^");
        assertType("super", call.getType());
        assertEquals("super", call.getLhs());
    }

    public void testCalll5b() throws Exception {
        Call call = getCall("super.^");
        assertType("super", call.getType());
        assertEquals("super", call.getLhs());
    }

    public void testCal16() throws Exception {
        Call call = getCall("Test::Unit::TestCase.ex^");
        assertEquals("Test::Unit::TestCase", call.getLhs());
        assertType("Test::Unit::TestCase", call.getType());
        assertTrue(call.isStatic());
    }

    public void testCalll7() throws Exception {
        Call call = getCall("@xy.ex^");
        assertEquals("@xy", call.getLhs());
        assertType(null, call.getType());
        assertTrue(!call.isStatic());
    }

    public void testCalll7b() throws Exception {
        Call call = getCall("@xy.^");
        assertEquals("@xy", call.getLhs());
        assertType(null, call.getType());
        assertTrue(!call.isStatic());
    }

    public void testCalll8() throws Exception {
        Call call = getCall("@@xy.ex^");
        assertEquals("@@xy", call.getLhs());
        assertType(null, call.getType());
    }

    public void testCalll9() throws Exception {
        Call call = getCall("$xy.ex^");
        assertEquals("$xy", call.getLhs());
        assertType(null, call.getType());
    }

    public void testCall20() throws Exception {
        Call call = getCall("foo.bar.ex^");
        assertEquals("foo.bar", call.getLhs());
        assertType(null, call.getType());
    }

    public void testCall21() throws Exception {
        Call call = getCall("10.between?(0, 100).^");
        assertEquals("10.between?(0, 100)", call.getLhs());
        assertFalse(call.getType().isKnown());
    }

    public void testCallUnknown() throws Exception {
        Call call = getCall("getFoo().x^");
        assertEquals("getFoo()", call.getLhs());
        assertFalse(call.getType().isKnown());
    }

    public void testCallLocal() throws Exception {
        Call call = getCall("foo^");
        assertSame(Call.LOCAL, call);
    }

    public void testCallNested() throws Exception {
        Call call = getCall("x=\"#{ File.ex^}\"");
        assertType("File", call.getType());
        assertTrue(call.isStatic());
    }

    // THIS IS BROKEN:
    //public void testCallNested2() throws Exception {
    //    Call call = getCall("x=\"#{ File.ex^ }\"");
    //    assertType("File", call.getType());
    //    assertTrue(call.isStatic());
    //}
    
    public void testConstructorCall() throws Exception {
        Call call = getCall("String.new.^");
        assertType("String", call.getType());
        assertFalse(call.isStatic());
    }

    public void testConstructorCall2() throws Exception {
        Call call = getCall("String.new.ex^");
        assertType("String", call.getType());
        assertFalse(call.isStatic());
    }
    
    public void testConstructorCall3() throws Exception {
        Call call = getCall("String.new.ex^ ");
        assertType("String", call.getType());
        assertFalse(call.isStatic());
    }

    public void testConstructorCall4() throws Exception {
        Call call = getCall("Test::Unit::TestCase.new.ex^ ");
        assertType("Test::Unit::TestCase", call.getType());
        assertFalse(call.isStatic());
    }

    public void testConstructorCall5() throws Exception {
        Call call = getCall("String.new(\"foo\").^");
        assertType("String", call.getType());
        assertFalse(call.isStatic());
    }

    public void testConstructorCall6() throws Exception {
        Call call = getCall("Something.new(a_method_call()).^");
        assertType("Something", call.getType());
        assertFalse(call.isStatic());
    }

    public void testConstructorCall7() throws Exception {
        Call call = getCall("Something.new(some_var).^");
        assertType("Something", call.getType());
        assertFalse(call.isStatic());
    }

    public void testNotConstructorCall() throws Exception {
        Call call = getCall("String.neww.^");
        assertFalse(call.getType().isKnown());
    }

    public void testNotConstructorCall2() throws Exception {
        Call call = getCall("new.^");
        assertFalse(call.getType().isKnown());
    }

    public void testNotConstructorCall3() throws Exception {
        Call call = getCall("@foo.new.^");
        assertFalse(call.getType().isKnown());
    }

    public void testNotConstructorCall4() throws Exception {
        Call call = getCall("foo.new.^");
        assertFalse(call.getType().isKnown());
    }

    public void testNotConstructorCall5() throws Exception {
        Call call = getCall("1.even?.^");
        assertFalse(call.getType().isKnown());
    }

    public void testNotConstructorCall6() throws Exception {
        Call call = getCall("String.neww().^");
        assertFalse(call.getType().isKnown());
    }

    public void testARGV() {
        Call call = getCall("ARGV.cho^");
        assertType("Array", call.getType());
        assertFalse(call.isStatic());
    }

    public void test__FILE_() {
        Call call = getCall("__FILE__.cho^");
        assertType("String", call.getType());
        assertFalse(call.isStatic());
    }

    public void testNotStaticCall() throws Exception {
        Call call = getCall("Some.new.thing.^");
        assertFalse(call.isStatic());
    }

    public void testNotStaticCall2() throws Exception {
        Call call = getCall("Some.new(x).thing(y).^");
        assertFalse(call.isStatic());
    }

    public void testNotStaticCall3() throws Exception {
        Call call = getCall("Some.new(x).thing.^");
        assertFalse(call.isStatic());
    }

    public void testNotStaticCall4() throws Exception {
        Call call = getCall("Some.new.thing(y).^");
        assertFalse(call.isStatic());
    }

    public void testNotStaticCall5() throws Exception {
        // assume that even static methods return instances
        Call call = getCall("Some.thing.^");
        assertFalse(call.isStatic());
    }

    public void testNotStaticCall6() throws Exception {
        // assume that even static methods return instances
        Call call = getCall("Some.thing.else.^");
        assertFalse(call.isStatic());
    }
}
