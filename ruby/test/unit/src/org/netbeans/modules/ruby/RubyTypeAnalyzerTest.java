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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jruby.nb.ast.MethodDefNode;
import org.jruby.nb.ast.Node;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.openide.filesystems.FileObject;

/**
 * @todo Test compound assignment:  x = File::Stat.new
 *
 * @author Tor Norbye
 */
public class RubyTypeAnalyzerTest extends RubyTestBase {

    public RubyTypeAnalyzerTest(String testName) {
        super(testName);
    }

    private RubyTypeAnalyzer getAnalyzer(String file, String caretLine, boolean findMethod) throws Exception {
        FileObject fo = getTestFile("testfiles/" + file);
        BaseDocument doc = getDocument(fo);
        GsfTestCompilationInfo info = getInfo(fo);
        Node root = AstUtilities.getRoot(info);
        initializeRegistry();
        RubyIndex index = RubyIndex.get(info.getIndex(RubyInstallation.RUBY_MIME_TYPE), info.getFileObject());

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = info.getText().indexOf(caretLine);
            assertTrue("unable to find offset for give carretLine: " + caretLine, lineOffset != -1);
            caretOffset = lineOffset + caretDelta;
        }

        AstPath path = new AstPath(root, caretOffset);
        Node node = path.leaf();

        if (findMethod) {
            MethodDefNode method = AstUtilities.findMethodAtOffset(root, caretOffset);
            assertNotNull(method);

            root = method;
        }

        RubyTypeAnalyzer instance = new RubyTypeAnalyzer(index, root, node, caretOffset, caretOffset, doc, fo);

        return instance;
    }

    private void assertTypes(final Set<? extends String> actualTypes, final String... expectedTypes) {
        assertTypes(null, actualTypes, expectedTypes);
    }

    private void assertTypes(final String message, final Set<? extends String> actualTypes, final String... expectedTypes) {
        Set<String> expectedTypesHash = new HashSet<String>(Arrays.asList(expectedTypes));
        assertTrue(message + ":" +
                "\n  actualTypes:   " + actualTypes +
                "\n  expectedTypes: " + expectedTypesHash, actualTypes.equals(expectedTypesHash));
    }

    private void assertTypes(String relFilePath, String matchingLine,
            String exprToInfer, String... expectedTypes) throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer(relFilePath, matchingLine, false);
        assertTypes("Types correctly inferred", instance.getTypes(exprToInfer), expectedTypes);
    }

    public void testGetType() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("types.rb", " l^oc = {", false);

        assertTypes(instance.getTypes("x"), "Integer");
        // y is reassigned later in the file - make sure that at this
        // point in scope we have the right type
        assertTypes(instance.getTypes("y"), "File");
        assertTypes(instance.getTypes("$baz"), "Hash");
        assertTypes(instance.getTypes("@bar"), "Fixnum");
        assertTypes(instance.getTypes("@foo"), "Array");
    }

    public void testGetType2() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("types.rb", " # d^one", false);

        // Y is assigned different types - make sure that at this position, it's a number
        assertTypes(instance.getTypes("y"), "Fixnum");
        // Lots of reassignments - track types through vars, statics, fields, classvars
        assertTypes(instance.getTypes("loc"), "Hash");
        assertTypes(instance.getTypes("$glob"), "Hash");
        assertTypes(instance.getTypes("@field"), "Hash");
        assertTypes(instance.getTypes("@@clsvar"), "Hash");
        assertTypes(instance.getTypes("loc2"), "Hash");
    }

    public void testTypeAssertions() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("types.rb", " l^oc = {", true);
        assertTypes(instance.getTypes("param1"), "String");
        assertTypes(instance.getTypes("param2"), "Hash");
    }

    public void testBegin() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("types2.rb", " @f^iles = ARGV.dup", true);
        assertTypes(instance.getTypes("go"), "GetoptLong");
    }

    public void testRailsController() throws Exception {
        assertTypes("type_controller.rb", "^end", "request", "ActionController::CgiRequest");
        RubyTypeAnalyzer instance = getAnalyzer("type_controller.rb", "^end", false);
        assertTypes(instance.getTypes("request"), "ActionController::CgiRequest");
    }

// This test doesn't work; the behavior works in the IDE but the
// Lucene index isn't returning local symbols in the testing framework yet    
//    public void testComplex1() throws Exception {
//        RubyTypeAnalyzer instance = getAnalyzer("types3.rb", "^caret", false);
//        assertEquals("Product", instance.getType("@product"));
//    }

//    public void testComplex2() throws Exception {
//        RubyTypeAnalyzer instance = getAnalyzer("types3.rb", "^caret", true);
//        assertEquals("ActiveRecord::ConnectionAdapters::TableDefinition", instance.getType("t"));
//    }

    //public void testComplex3() throws Exception {
    //    // XXX TODO 
    //    assertFalse("Check that I do closures for each, collect, map, etc.", true);
    //    // also check to_s
    //}
    // TODO: Make sure I can handle compound expressions like this one:
    //  Product.find(params[:id]).destroy
    public void testMigrationType() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("migrate/20080726182725_create_posts.rb", " t.^time", true);
        assertTypes(instance.getTypes("t"), "ActiveRecord::ConnectionAdapters::TableDefinition");
    }

    public void testIfType() throws Exception {
        assertTypes("if_type.rb", "p va^r.in", "var", "String", "NilClass");
    }

    public void testIfElseType() throws Exception {
        assertTypes("if_else_type.rb", "p va^r.in", "var", "String", "Array");
    }

    public void testIfElseType2() throws Exception {
        assertTypes("if_else_type_2.rb", "p va^r.in", "var", "String", "NilClass");
    }

    public void testIfElseWithInBlockReassignmentType() throws Exception {
        assertTypes("if_else_with_block_reassignment_type.rb", "p va^r.in", "var", "Hash", "Array");
    }

    public void testIfElseIfElseType() throws Exception {
        assertTypes("if_elsif_else_type.rb", "p va^r.in", "var", "String", "Array", "Hash");
    }

    public void testUnlessType() throws Exception {
        assertTypes("unless_type.rb", "var.i^", "var", "Array", "Hash");
    }

    public void testIfWithFailingInferenceInBranchType() throws Exception {
        assertTypes("if_with_failing_inference_in_branch_type.rb", "var.to_^", "var", "NilClass", null);
    }

    // TODO inference is still not able to do the below
    public void FIXME_testIfElseNestedSimpleType() throws Exception {
        assertTypes("if_else_nested_simple_type.rb", "var.^ifcond1b", "var", "Float");
        assertTypes("if_else_nested_simple_type.rb", "var.^aa", "var", "NilClass", "Float");
    }

    // TODO inference is still not able to do the below
    public void FIXME_testIfElseNestedType() throws Exception {
        assertTypes("if_else_nested_type.rb", "va^r.ifcond2", "var", "Hash");
        // XXX more, see the if_else_nested_type.rb
    }

    public void testConstant() throws Exception {
        assertTypes("constants.rb", "Colors::RED.byte^", "RED", "String");
        // TODO fix and uncomment when reindexed
        // assertTypes("indexed constants type inference", "constants.rb", "REXML::COPY^RIGHT", "COPYRIGHT", "String");
    }

    public void testCoreMethodType() throws Exception {
        assertTypes("core_methods.rb", "ance^stors.delete(String)", "ancestors", "Array");
    }
}
