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

package org.netbeans.modules.javascript.editing;

import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 *
 * @author Tor Norbye
 */
public class JsCodeCompletionTest extends JsTestBase {
    
    public JsCodeCompletionTest(String testName) {
        super(testName);
        
        
        // Don't truncate in unit tests; it's non-deterministic which items we end up
        // with coming out of the index so golden file diffing doesn't work
        JsIndex.MAX_SEARCH_ITEMS = Integer.MAX_VALUE;
        JsCodeCompletion.MAX_COMPLETION_ITEMS = Integer.MAX_VALUE;
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("CslJar", "true");
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("CslJar", "false");
    }


    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(JsClassPathProvider.BOOT_CP, JsClassPathProvider.getBootClassPath());
    }

    @Override
    protected void checkCall(ParserResult info, int caretOffset, String expectedParameter, boolean expectSuccess) {
        IndexedFunction[] methodHolder = new IndexedFunction[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int lexOffset = caretOffset;
        int astOffset = caretOffset;
        JsParseResult jspr = AstUtilities.getParseResult(info);
        assertNotNull("Expecting JsParseResult, but got " + info, jspr);
        boolean ok = JsCodeCompletion.computeMethodCall(jspr, lexOffset, astOffset, methodHolder, paramIndexHolder, anchorOffsetHolder, null);

        if (expectSuccess) {
            assertTrue(ok);
        } else if (!ok) {
            return;
        }
        IndexedFunction method = methodHolder[0];
        assertNotNull(method);
        int index = paramIndexHolder[0];
        assertTrue(index >= 0);
        if (expectedParameter != null) {
            assert method.getParameters().size() > 0;
            String parameter = method.getParameters().get(index);
            int typeDef = parameter.indexOf(':');
            if (typeDef != -1) {
                parameter = parameter.substring(0,typeDef);
            }

            // The index doesn't work right at test time - not sure why
            // it doesn't have all of the gems...
            //assertEquals(fqn, method.getFqn());
            assertEquals(expectedParameter, parameter);
        }
    }
    
    public void testPrefix1() throws Exception {
        checkPrefix("testfiles/cc-prefix1.js");
    }
    
    public void testPrefix2() throws Exception {
        checkPrefix("testfiles/cc-prefix2.js");
    }

    public void testPrefix3() throws Exception {
        checkPrefix("testfiles/cc-prefix3.js");
    }

    public void testPrefix4() throws Exception {
        checkPrefix("testfiles/cc-prefix4.js");
    }

    public void testPrefix5() throws Exception {
        checkPrefix("testfiles/cc-prefix5.js");
    }

    public void testPrefix6() throws Exception {
        checkPrefix("testfiles/cc-prefix6.js");
    }

    public void testPrefix7() throws Exception {
        checkPrefix("testfiles/cc-prefix7.js");
    }

    public void testPrefix8() throws Exception {
        checkPrefix("testfiles/cc-prefix8.js");
    }
    
    public void testAutoQuery1() throws Exception {
        assertAutoQuery(QueryType.NONE, "foo^", "o");
        assertAutoQuery(QueryType.NONE, "foo^", " ");
        assertAutoQuery(QueryType.NONE, "foo^", "c");
        assertAutoQuery(QueryType.NONE, "foo^", "d");
        assertAutoQuery(QueryType.NONE, "foo^", "f");
        assertAutoQuery(QueryType.NONE, "Foo:^", ":");
        assertAutoQuery(QueryType.NONE, "Foo::^", ":");
        assertAutoQuery(QueryType.NONE, "Foo^ ", ":");
        assertAutoQuery(QueryType.NONE, "Foo^bar", ":");
        assertAutoQuery(QueryType.NONE, "Foo:^bar", ":");
        assertAutoQuery(QueryType.NONE, "Foo::^bar", ":");
    }

    public void testAutoQuery2() throws Exception {
        assertAutoQuery(QueryType.STOP, "foo^", ";");
        assertAutoQuery(QueryType.STOP, "foo^", "[");
        assertAutoQuery(QueryType.STOP, "foo^", "(");
        assertAutoQuery(QueryType.STOP, "foo^", "{");
        assertAutoQuery(QueryType.STOP, "foo^", "\n");
    }

    public void testAutoQuery3() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo.^", ".");
        assertAutoQuery(QueryType.COMPLETION, "foo^ ", ".");
        assertAutoQuery(QueryType.COMPLETION, "foo^bar", ".");
    }

    public void testAutoQueryComments() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo^ # bar", ".");
        assertAutoQuery(QueryType.NONE, "//^foo", ".");
        assertAutoQuery(QueryType.NONE, "/* foo^*/", ".");
        assertAutoQuery(QueryType.NONE, "// foo^", ".");
    }

    public void testAutoQueryStrings() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo^ 'foo'", ".");
        assertAutoQuery(QueryType.NONE, "'^foo'", ".");
        assertAutoQuery(QueryType.NONE, "/f^oo/", ".");
        assertAutoQuery(QueryType.NONE, "\"^\"", ".");
        assertAutoQuery(QueryType.NONE, "\" foo^ \"", ".");
    }

//    public void testAutoQueryRanges() throws Exception {
//        assertAutoQuery(QueryType.NONE, "x..^", ".");
//        assertAutoQuery(QueryType.NONE, "x..^5", ".");
//    }

//    public void testCompletion1() throws Exception {
//        checkCompletion("testfiles/completion/lib/test1.js", "f.e^");
//    }
//    
//    public void testCompletion2() throws Exception {
//        // This test doesn't pass yet because we need to index the -current- file
//        // before resuming
//        checkCompletion("testfiles/completion/lib/test2.js", "Result is #{@^myfield} and #@another.");
//    }
//    
//    public void testCompletion3() throws Exception {
//        checkCompletion("testfiles/completion/lib/test2.js", "Result is #{@myfield} and #@a^nother.");
//    }
//    

    public void testLocalCompletion1() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.js", "^alert('foo1", false);
    }

    public void testLocalCompletion2() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.js", "^alert('foo2", false);
    }

    public void testLocalCompletion3() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.js", "^var declaredglobal;", false);
    }

    public void testCompletionStringCompletion1() throws Exception {
        checkCompletion("testfiles/completion/lib/test1.js", "Hell^o World", false);
    }

    public void testCompletionStringCompletion2() throws Exception {
        checkCompletion("testfiles/completion/lib/test1.js", "\"f\\^oo\"", false);
    }
    
    public void testCompletionStringCompletion3() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.js", "alert('^foo1", false);
    }

    public void testCompletionRegexpCompletion1() throws Exception {
        checkCompletion("testfiles/completion/lib/test1.js", "/re^g/", false);
    }

    public void testCompletionRegexpCompletion2() throws Exception {
        checkCompletion("testfiles/completion/lib/test1.js", "/b\\^ar/", false);
    }

    public void testComments1() throws Exception {
        checkCompletion("testfiles/completion/lib/comments.js", "@^param", false);
    }

    public void testComments2() throws Exception {
        checkCompletion("testfiles/completion/lib/comments.js", "@p^aram", false);
    }

    public void testComments3() throws Exception {
        checkCompletion("testfiles/completion/lib/comments.js", "^@param", false);
    }

    public void testComments4() throws Exception {
        checkCompletion("testfiles/completion/lib/comments.js", "T^his", false);
    }

    public void testE4X1() throws Exception {
        checkCompletion("testfiles/completion/lib/e4x.js", "order.^", true);
    }

    public void testE4X2() throws Exception {
        checkCompletion("testfiles/completion/lib/e4x.js", "order.item.^qty", true);
    }

    public void testE4X3() throws Exception {
        checkCompletion("testfiles/completion/lib/e4x.js", "order.i^tem.qty", true);
    }

    public void testCall7() throws Exception {
        checkComputeMethodCall("testfiles/calls/call3.js", "^jQ // 2",
                null, false);
    }

    public void testCall8() throws Exception {
        checkComputeMethodCall("testfiles/calls/call3.js", "^jQ // 3",
                null, false);
    }

    // TODO: Test open classes, class inheritance, relative symbols, finding classes, superclasses, def completion, ...

//////////////////////////////////////////////////////////////////////////////
//    // Unstable
//////////////////////////////////////////////////////////////////////////////
//
//    public void testCall1() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call1.js", "x.addEventListener(type, ^listener, useCapture)", "listener", true);
//    }
//
//    public void testCall2() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call1.js", "foo2(^x);", "a", true);
//    }
//
//    public void testCall3() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call1.js", "foo3(x^,y)",
//                "a", true);
//    }
//    public void testCall4() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call2.js", "foo3(x,^)",
//                "b", true);
//    }
//
//    public void testCall5() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call1.js", "foo4(x,y,^z);",
//                "c", true);
//    }
//
//    public void testCall6() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call3.js", "^bar: {'foo': 1, 'bar': 2},",
//                null, true);
//    }
//
//    public void testEmpty() throws Exception {
//        checkCompletion("testfiles/completion/lib/empty.js", "^", false);
//    }
//
//    public void test129036() throws Exception {
//        checkCompletion("testfiles/completion/lib/test129036.js", "my^ //Foo", false);
//    }
//
//    public void testExpression1() throws Exception {
//        checkCompletion("testfiles/completion/lib/expressions.js", "^escape", false);
//    }
//
//    public void testExpressions2() throws Exception {
//        checkCompletion("testfiles/completion/lib/expressions.js", "^toE", false);
//    }
//
//    public void testExpressions2b() throws Exception {
//        checkCompletion("testfiles/completion/lib/expressions2.js", "ownerDocument.^", false);
//    }
//
//    public void testExpressions3() throws Exception {
//        checkCompletion("testfiles/completion/lib/expressions3.js", "specified.^", false);
//    }
//
//    public void testExpressions4() throws Exception {
//        checkCompletion("testfiles/completion/lib/expressions4.js", "document.b^", false);
//    }
//
//    public void testExpressions5() throws Exception {
//        checkCompletion("testfiles/completion/lib/expressions5.js", "dur.^t", false);
//    }
//
//    public void testNewCompletionEol() throws Exception {
//        checkCompletion("testfiles/completion/lib/newcompletion.js", "new ^", false);
//    }
//
//    public void testYahoo() throws Exception {
//        checkCompletion("testfiles/completion/lib/yahoo.js", "e^ // complete on editor members etc", true);
//    }
//
//    public void testParameterCompletion1() throws Exception {
//        checkCompletion("testfiles/completion/lib/configcalls.js", "somefunction({}, {^}, {});", false);
//    }
//
//    public void testParameterCompletion2() throws Exception {
//        checkCompletion("testfiles/completion/lib/configcalls.js", "somefunction({}, {}, {^});", false);
//    }
//
//    // This test is unstable for some reason
//    //    public void testParameterCompletion3() throws Exception {
//    //        checkCompletion("testfiles/completion/lib/configcalls.js", "somefunction({^}, {}, {});", false);
//    //    }
//
//    public void testParameterCompletion4() throws Exception {
//        checkCompletion("testfiles/completion/lib/configcalls.js", "somefunctio^n({}, {}, {});", false);
//    }
//
//    public void testParameterCompletion5() throws Exception {
//        checkCompletion("testfiles/completion/lib/configcalls.js", "somefunction({}, {f^:1}, {});", false);
//    }
//
//    public void testDeprecatedProperties() throws Exception {
//        checkCompletion("testfiles/completion/lib/domproperties.js", ".s^", true);
//    }


}
