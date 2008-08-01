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

package org.netbeans.modules.javascript.editing.lexer;

import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.javascript.editing.JsTestBase;

/**
 *
 * @author Tor Norbye
 */
public class CallTest extends JsTestBase {

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
        Call call = getCall("File.ex^");
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
        assertEquals("RegExp", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall4b() throws Exception {
        Call call = getCall("/foo/.^");
        assertEquals("RegExp", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCallComma() throws Exception {
        Call call = getCall("foo(x,this.currentW^idth");
        assertEquals("this", call.getLhs());
    }

    public void testCallNegate() throws Exception {
        Call call = getCall("!this.currentW^idth");
        assertEquals("this", call.getLhs());
    }

// This test no longer applies; it's not common to call methods on
// array literals in JavaScript and it's a lot more likely
// you're trying to access an array -element-    
//    public void testCall5() throws Exception {
//        Call call = getCall("[1,2,3].each^");
//        assertEquals("Array", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }
//
// This test no longer applies; it's not common to call methods on
// array literals in JavaScript and it's a lot more likely
// you're trying to access an array -element-    
//    public void testCall5b() throws Exception {
//        Call call = getCall("[1,2,3].^");
//        assertEquals("Array", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }

//    public void testCall6() throws Exception {
//        Call call = getCall("{:x=>:y}.foo^");
//        assertEquals("Hash", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }
//
//    public void testCall6b() throws Exception {
//        Call call = getCall("{:x=>:y}.^");
//        assertEquals("Hash", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }

//    public void testCall7() throws Exception {
//        Call call = getCall("50.ea^");
//        assertEquals("Number", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }
//
//    public void testCall7b() throws Exception {
//        Call call = getCall("50.^");
//        assertEquals("Number", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }

    public void testCall8() throws Exception {
        Call call = getCall("3.14.ea^");
        assertEquals("Number", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall8b() throws Exception {
        Call call = getCall("3.14.^");
        assertEquals("Number", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

//    public void testCall9() throws Exception {
//        Call call = getCall(":mysym.foo^");
//        assertEquals("Symbol", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }
//
//    public void testCall9b() throws Exception {
//        Call call = getCall(":mysym.^");
//        assertEquals("Symbol", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }
//
//    public void testCa11() throws Exception {
//        Call call = getCall("nil.foo^");
//        assertEquals("NilClass", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }
//
//    public void testCal1b() throws Exception {
//        Call call = getCall("nil.^");
//        assertEquals("NilClass", call.getType());
//        assertFalse(call.isSimpleIdentifier());
//        assertFalse(call.isStatic());
//    }

    public void testCalll2() throws Exception {
        Call call = getCall("true.foo^");
        assertEquals("Boolean", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll2b() throws Exception {
        Call call = getCall("true.^");
        assertEquals("Boolean", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll3() throws Exception {
        Call call = getCall("false.foo^");
        assertEquals("Boolean", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll3b() throws Exception {
        Call call = getCall("false.^");
        assertEquals("Boolean", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall14() throws Exception {
        Call call = getCall("this.foo^");
        assertEquals("this", call.getType());
        assertEquals("this", call.getLhs());
    }

    public void testCall14b() throws Exception {
        Call call = getCall("this.^");
        assertEquals("this", call.getType());
        assertEquals("this", call.getLhs());
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
        Call call = getCall("Test.Unit.TestCase.ex^");
        assertEquals("Test.Unit.TestCase", call.getLhs());
        assertEquals("Test.Unit.TestCase", call.getType());
        assertTrue(call.isStatic());
    }

    public void testCal17() throws Exception {
        Call call = getCall("Spry.Data.Region.prototype.process^");
        assertEquals("Spry.Data.Region.prototype", call.getLhs());
        assertEquals("Spry.Data.Region", call.getType());
        assertTrue(call.isStatic());
    }

    public void testCal18() throws Exception {
        Call call = getCall("Spry.Data.Region.prototype^");
        assertEquals("Spry.Data.Region", call.getLhs());
        assertEquals("Spry.Data.Region", call.getType());
        assertTrue(call.isStatic());
    }

    public void testCallExpression1() throws Exception {
        String code = "var someOtherVariable = this.someProperty;";
        int caretPos = code.indexOf("someProp") + 4; // inside someProperty

        BaseDocument doc = getDocument(code);
        
        String callExpr = Call.getCallExpression(doc, caretPos);
        assertEquals("this.someProperty", callExpr);

        callExpr = Call.getCallExpression(doc, code.indexOf("this") + 2);
        assertEquals("this", callExpr);
    }

    public void testCallExpression2() throws Exception {
        String code = "  aaaaa.bbbbb.ccccc.ddddd";
        BaseDocument doc = getDocument(code);
        
        String callExpr;
        callExpr = Call.getCallExpression(doc, code.indexOf("aaa") + 2);
        assertEquals("aaaaa", callExpr);
        
        callExpr = Call.getCallExpression(doc, code.indexOf("bbb") + 2);
        assertEquals("aaaaa.bbbbb", callExpr);

        callExpr = Call.getCallExpression(doc, code.indexOf("ccc") + 2);
        assertEquals("aaaaa.bbbbb.ccccc", callExpr);

        callExpr = Call.getCallExpression(doc, code.indexOf("ddd") + 2);
        assertEquals("aaaaa.bbbbb.ccccc.ddddd", callExpr);
    }

// Not yet working, requires parse tree or fancier lexical analysis
//    public void testCallExpression3() throws Exception {
//        String code = "   addresses[3].city;  ";
//        BaseDocument doc = getDocument(code);
//        String callExpr = Call.getCallExpression(doc, code.indexOf("city") + 2);
//        assertEquals("addresses[3].city", callExpr);
//    }

    public void testCallExpression4() throws Exception {
        String code = "   this.city;  ";
        BaseDocument doc = getDocument(code);
        String callExpr = Call.getCallExpression(doc, code.indexOf("city") + 2);
        assertEquals("this.city", callExpr);
    }

// Not yet working, requires parse tree or fancier lexical analysis
//    public void testCallExpression5() throws Exception {
//        String code = "   getAddress().city;  ";
//        BaseDocument doc = getDocument(code);
//        String callExpr = Call.getCallExpression(doc, code.indexOf("city") + 2);
//        assertEquals("getAddress().city", callExpr);
//    }
    
    public void testCallExpressionNegative() throws Exception {
        String code = "  //aaaaa.bbbbb.ccccc.ddddd";
        BaseDocument doc = getDocument(code);
        String callExpr = Call.getCallExpression(doc, code.indexOf("aaa") + 2);
        assertEquals("aaaaa", callExpr);
        
        code = "  //  ";
        doc = getDocument(code);
        callExpr = Call.getCallExpression(doc, 4);
        assertEquals(null, callExpr);

        code = "x()";
        doc = getDocument(code);
        callExpr = Call.getCallExpression(doc, 2);
        assertEquals(null, callExpr);
    }
    
    public void testCallExpressionComma() throws Exception {
        String code = "  foo(x,this.currentWidth";
        BaseDocument doc = getDocument(code);
        String callExpr = Call.getCallExpression(doc, code.indexOf("Width"));
        assertEquals("this.currentWidth", callExpr);
    }

    public void testCallExpressionUnary() throws Exception {
        String code = "!this.currentWidth";
        BaseDocument doc = getDocument(code);
        String callExpr = Call.getCallExpression(doc, code.indexOf("Width"));
        assertEquals("this.currentWidth", callExpr);
    }
    
//    public void testCalll7() throws Exception {
//        Call call = getCall("@xy.ex^");
//        assertEquals("@xy", call.getLhs());
//        assertEquals(null, call.getType());
//        assertTrue(!call.isStatic());
//    }
//
//    public void testCalll7b() throws Exception {
//        Call call = getCall("@xy.^");
//        assertEquals("@xy", call.getLhs());
//        assertEquals(null, call.getType());
//        assertTrue(!call.isStatic());
//    }
//
//    public void testCalll8() throws Exception {
//        Call call = getCall("@@xy.ex^");
//        assertEquals("@@xy", call.getLhs());
//        assertEquals(null, call.getType());
//    }
//
//    public void testCalll9() throws Exception {
//        Call call = getCall("$xy.ex^");
//        assertEquals("$xy", call.getLhs());
//        assertEquals(null, call.getType());
//    }

    public void testCall20() throws Exception {
        Call call = getCall("foo.bar.ex^");
        assertEquals("foo.bar", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCallUnknown() throws Exception {
        Call call = getCall("getFoo().x^");
        assertNull(call.getType());
        assertNull(call.getLhs());
        assertEquals(6, call.getPrevCallParenPos());
    }

    public void testCallLocal() throws Exception {
        Call call = getCall("foo^");
        assertSame(Call.LOCAL, call);
    }

//    public void testCallNested() throws Exception {
//        Call call = getCall("x=\"#{ File.ex^}\"");
//        assertEquals("File", call.getType());
//        assertTrue(call.isStatic());
//    }

    
//    public void testConstructorCall() throws Exception {
//        Call call = getCall("String.new.^");
//        assertEquals("String", call.getType());
//        assertFalse(call.isStatic());
//    }
//
//    public void testConstructorCall2() throws Exception {
//        Call call = getCall("String.new.ex^");
//        assertEquals("String", call.getType());
//        assertFalse(call.isStatic());
//    }
//    
//    public void testConstructorCall3() throws Exception {
//        Call call = getCall("String.new.ex^ ");
//        assertEquals("String", call.getType());
//        assertFalse(call.isStatic());
//    }
//
//    public void testConstructorCall4() throws Exception {
//        Call call = getCall("Test::Unit::TestCase.new.ex^ ");
//        assertEquals("Test::Unit::TestCase", call.getType());
//        assertFalse(call.isStatic());
//    }
//
//    public void testNotConstructorCall() throws Exception {
//        Call call = getCall("String.neww.^");
//        assertNull(call.getType());
//    }
//
//    public void testNotConstructorCal2() throws Exception {
//        Call call = getCall("new.^");
//        assertNull(call.getType());
//    }
//
//    public void testNotConstructorCal3() throws Exception {
//        Call call = getCall("@foo.new.^");
//        assertNull(call.getType());
//    }
//
//    public void testNotConstructorCal4() throws Exception {
//        Call call = getCall("foo.new.^");
//        assertNull(call.getType());
//    }

//    public void testDom1() throws Exception {
//        Call call = getCall("window.cr^");
//        assertEquals("window.cr", call.getLhs());
//        assertEquals("window", call.getType());
//    }
//

}
