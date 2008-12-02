/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby;

import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.ruby.elements.IndexedMethod;

/**
 *
 * @author Tor Norbye
 */
public class RubyCodeCompleterTest extends RubyTestBase {
    
    public RubyCodeCompleterTest(String testName) {
        super(testName);
    }

//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//
//        List<FileObject> files = new ArrayList<FileObject>();
//        RubyLanguage.libraries = files;
//        files.add(RubyPlatform.getRubyStubs());
//        GemManager gemManager = RubyPlatformManager.getDefaultPlatform().getGemManager();
//
//        Map<String, URL> gemUrls = gemManager.getGemUrls();
//        List<URL> urls = new ArrayList<URL>(gemUrls.values());
//        for (URL url : gemManager.getNonGemLoadPath()) {
//            urls.add(url);
//        }
//
//        for (URL url : urls) {
//            FileObject fo = URLMapper.findFileObject(url);
//            assertNotNull(url.toExternalForm(), fo);
//            files.add(fo);
//        }
//    }


    @Override
    protected void checkCall(GsfTestCompilationInfo info, int caretOffset, String expectedParameter, boolean expectSuccess) {
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int lexOffset = caretOffset;
        int astOffset = caretOffset;
        boolean ok = RubyMethodCompleter.computeMethodCall(info, lexOffset, astOffset, methodHolder, paramIndexHolder, anchorOffsetHolder, null, NameKind.PREFIX);

        if (expectSuccess) {
            assertTrue(ok);
        } else if (!ok) {
            return;
        }
        IndexedMethod method = methodHolder[0];
        assertNotNull(method);
        int index = paramIndexHolder[0];
        assertTrue(index >= 0);
        String parameter = method.getParameters().get(index);
        // The index doesn't work right at test time - not sure why
        // it doesn't have all of the gems...
        //assertEquals(fqn, method.getFqn());
        assertEquals(expectedParameter, parameter);
    }
    
    public void checkCompletion(String file, String caretLine) throws Exception {
        checkCompletion(file, caretLine, false);
    }

    public void testPrefix1() throws Exception {
        checkPrefix("testfiles/cc-prefix1.rb");
    }

    public void testPrefix2() throws Exception {
        checkPrefix("testfiles/cc-prefix2.rb");
    }

    public void testPrefix3() throws Exception {
        checkPrefix("testfiles/cc-prefix3.rb");
    }

    public void testPrefix4() throws Exception {
        checkPrefix("testfiles/cc-prefix4.rb");
    }

    public void testPrefix5() throws Exception {
        checkPrefix("testfiles/cc-prefix5.rb");
    }

    public void testPrefix6() throws Exception {
        checkPrefix("testfiles/cc-prefix6.rb");
    }

    public void testPrefix7() throws Exception {
        checkPrefix("testfiles/cc-prefix7.rb");
    }

    public void testPrefix8() throws Exception {
        checkPrefix("testfiles/cc-prefix8.rb");
    }

    public void testAutoQuery1() throws Exception {
        assertAutoQuery(QueryType.NONE, "foo^", "o");
        assertAutoQuery(QueryType.NONE, "foo^", " ");
        assertAutoQuery(QueryType.NONE, "foo^", "c");
        assertAutoQuery(QueryType.NONE, "foo^", "d");
        assertAutoQuery(QueryType.NONE, "foo^", ";");
        assertAutoQuery(QueryType.NONE, "foo^", "f");
        assertAutoQuery(QueryType.NONE, "Foo:^", ":");
        assertAutoQuery(QueryType.NONE, "Foo^ ", ":");
        assertAutoQuery(QueryType.NONE, "Foo^bar", ":");
        assertAutoQuery(QueryType.NONE, "Foo:^bar", ":");
    }

    public void testAutoQuery2() throws Exception {
        assertAutoQuery(QueryType.STOP, "foo^", "[");
        assertAutoQuery(QueryType.STOP, "foo^", "(");
        assertAutoQuery(QueryType.STOP, "foo^", "{");
        assertAutoQuery(QueryType.STOP, "foo^", "\n");
    }

    public void testAutoQuery3() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo.^", ".");
        assertAutoQuery(QueryType.COMPLETION, "Foo::^", ":");
        assertAutoQuery(QueryType.COMPLETION, "foo^ ", ".");
        assertAutoQuery(QueryType.COMPLETION, "foo^bar", ".");
        assertAutoQuery(QueryType.COMPLETION, "Foo::^bar", ":");
    }

    public void testAutoQueryComments() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo^ # bar", ".");
        assertAutoQuery(QueryType.NONE, "#^foo", ".");
        assertAutoQuery(QueryType.NONE, "# foo^", ".");
        assertAutoQuery(QueryType.NONE, "# foo^", ":");
    }

    public void testAutoQueryStrings() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo^ 'foo'", ".");
        assertAutoQuery(QueryType.NONE, "'^foo'", ".");
        assertAutoQuery(QueryType.NONE, "/f^oo/", ".");
        assertAutoQuery(QueryType.NONE, "\"^\"", ".");
        assertAutoQuery(QueryType.NONE, "\" foo^ \"", ":");
    }

    public void testAutoQueryRanges() throws Exception {
        assertAutoQuery(QueryType.NONE, "x..^", ".");
        assertAutoQuery(QueryType.NONE, "x..^5", ".");
    }

    // This test is unstable for some reason
//    public void testCompletion1() throws Exception {
//        checkCompletion("testfiles/completion/lib/test1.rb", "f.e^");
//    }

    public void testCompletion2() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "Result is #{@^myfield} and #@another.");
    }

    public void testCompletion3() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "Result is #{@myfield} and #@a^nother.");
    }

    public void testCompletion4() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "Hell^o World");
    }

    public void testCompletion5() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "/re^g/");
    }

    public void testCompletion6() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "class My^Test");
    }

    public void testCompletion7() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "puts Module.class_variable^s");
    }

    public void testCompletion8() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "puts 'Hello'.class^");
    }

//    
//    // TODO: Test open classes, class inheritance, relative symbols, finding classes, superclasses, def completion, ...

    public void checkComputeMethodCall(String file, String caretLine, String fqn, String param, boolean expectSuccess) throws Exception {
        checkComputeMethodCall(file, caretLine, param, expectSuccess);
    }

    public void testCall1() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table(^firstarg,  :id => true)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "table_name", true);
    }

    public void testCall2() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table(firstarg^,  :id => true)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "table_name", true);
    }
    public void testCall3() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table(firstarg,^  :id => true)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall4() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table(firstarg,  ^:id => true)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCallSpace1() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table firstarg,  ^:id => true",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCallSpace2() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table ^firstarg,  :id => true",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "table_name", true);
    }
    public void testCall5() throws Exception {
        checkComputeMethodCall("testfiles/calls/call2.rb", "create_table(^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "table_name", true);
    }
    public void testCall6() throws Exception {
        checkComputeMethodCall("testfiles/calls/call3.rb", "create_table^",
                null, null, false);
    }
    public void testCall7() throws Exception {
        checkComputeMethodCall("testfiles/calls/call3.rb", "create_table ^",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "table_name", true);
    }
    public void testCall8() throws Exception {
        checkComputeMethodCall("testfiles/calls/call4.rb", "create_table foo,^",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall9() throws Exception {
        checkComputeMethodCall("testfiles/calls/call4.rb", "create_table foo, ^",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall10() throws Exception {
        checkComputeMethodCall("testfiles/calls/call5.rb", " create_table(foo, ^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall11() throws Exception {
        checkComputeMethodCall("testfiles/calls/call6.rb", " create_table(foo, :key => ^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }

    public void testCall12() throws Exception {
        checkComputeMethodCall("testfiles/calls/call7.rb", " create_table(foo, :key => :^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }

    public void testCall13() throws Exception {
        checkComputeMethodCall("testfiles/calls/call8.rb", " create_table(foo, :key => :a^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall14() throws Exception {
        checkComputeMethodCall("testfiles/calls/call9.rb", " create_table(foo, :^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }

//    public void testCall15() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call10.rb", "File.exists?(^)",
//                "File#exists", "file", true);
//    }

    public void testCall16() throws Exception {
        checkComputeMethodCall("testfiles/calls/call11.rb", " ^#",
                null, null, false);
    }

    public void testCall17() throws Exception {
        checkComputeMethodCall("testfiles/calls/call12.rb", " ^#",
                null, null, false);
    }

    public void testEmpty1() throws Exception {
        checkCompletion("testfiles/empty.rb", "^", false);
    }

    public void testGlobals() throws Exception {
        checkCompletion("testfiles/completion/lib/globals.rb", "$^m # input");
    }

    public void testAttributes() throws Exception {
        checkCompletion("testfiles/completion/lib/song.rb", "ss.^");
    }

    public void testIfWithFailingInferenceInBranchType() throws Exception {
        checkCompletion("testfiles/if_with_failing_inference_in_branch_type.rb", "var.to_i^");
    }

    public void testPredefConstantsComletionARGV() throws Exception {
        checkCompletion("testfiles/predef_constants.rb", "ARGV.del^");
    }

    public void testPredefConstantsComletion__FILE__() throws Exception {
        checkCompletion("testfiles/predef_constants.rb", "__FILE__.cho^");
    }

    // #threw NPE from RubyDeclarationFinder
    public void testUnkownInTheBlock() throws Exception {
        // TODO: it actually tries to infer wrongly upon the 'arr', but should
        // upon the 'Huh'. Cf. unknown_in_the_block.rb
        checkCompletion("testfiles/unknown_in_the_block.rb", "Huh.err^");
    }

    public void testConstantMethods() throws Exception {
        checkCompletion("testfiles/constants.rb", "Colors::RED.byte^");
    }

    public void testConstants() throws Exception {
        checkCompletion("testfiles/constants1.rb", "Fcntl::O_A^");
    }

    public void testConstantsNonPrefixed() throws Exception {
        checkCompletion("testfiles/constants1.rb", "Fcntl::^O_A");
    }

    public void testConstantsFromParentsAreNotOffered() throws Exception {
        // must not offer FALSE from Object
        checkCompletion("testfiles/constants1.rb", "Fcntl::F^");
    }
    // TODO - test more non-fc calls (e.g. x.foo)
    // TODO test with splat args (more args than are in def list)
    // TODO test with long arg lists
}
