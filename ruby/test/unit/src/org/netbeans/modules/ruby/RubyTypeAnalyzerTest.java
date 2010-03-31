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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

import java.util.Map;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.ruby.options.TypeInferenceSettings;
import org.openide.filesystems.FileObject;

/**
 * @todo Test compound assignment:  x = File::Stat.new
 *
 * @author Tor Norbye
 */
public class RubyTypeAnalyzerTest extends RubyTestBase {

    public RubyTypeAnalyzerTest(String testName) {
        super(testName);
        RubyIndexer.userSourcesTest = true;
        TypeInferenceSettings.getDefault().setMethodTypeInference(true);
        TypeInferenceSettings.getDefault().setRdocTypeInference(true);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return rubyTestsClassPath();
    }

    private RubyTypeInferencer getInferencer(String file, String caretLine, boolean findMethod) throws Exception {
        FileObject fo = getTestFile("testfiles/" + file);

        int caretOffset = -1;
        if (caretLine != null) {
            Source source = Source.create(fo);
            caretOffset = getCaretOffset(source.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(source, caretOffset);
        }

        BaseDocument doc = getDocument(fo);
        ParserResult parserResult = getParserResult(fo);
        Node root = AstUtilities.getRoot(parserResult);
        initializeRegistry();
        RubyIndex index = RubyIndex.get(parserResult);
        AstPath path = new AstPath(root, caretOffset);
        Node node = path.leaf();

        if (findMethod) {
            MethodDefNode method = AstUtilities.findMethodAtOffset(root, caretOffset);
            assertNotNull(method);
            root = method;
        }

        ContextKnowledge knowledge = new ContextKnowledge(index, root, node, caretOffset, caretOffset, parserResult);
        return RubyTypeInferencer.create(knowledge);
    }

    private void assertTypes(final RubyType actualTypes, final String... expectedTypes) {
        assertTypes(null, actualTypes, expectedTypes);
    }

    private void assertTypes(final String message, final RubyType actualTypes, final String... expectedTypes) {
        assertTypes(message, actualTypes, false, expectedTypes);
    }

    private void assertTypes(final String message, final RubyType actualTypes,
            final boolean hasUnknownMember, final String... expectedTypes) {
        RubyType expected = new RubyType(expectedTypes);
        if (hasUnknownMember) {
            expected.append(RubyType.unknown());
        }
        assertTrue(message + ":" +
                "\n  actualTypes:   " + actualTypes +
                "\n  expectedTypes: " + expected, actualTypes.equals(expected));
    }

    private void assertTypes(String relFilePath, String matchingLine,
            String exprToInfer, String... expectedTypes) throws Exception {
        assertTypes(relFilePath, matchingLine, exprToInfer, false, expectedTypes);
    }
    private void assertTypes(String relFilePath, String matchingLine,
            String exprToInfer, boolean hasUnknownMember, String... expectedTypes) throws Exception {
        RubyTypeInferencer instance = getInferencer(relFilePath, matchingLine, false);
        assertTypes("Types correctly inferred", instance.inferType(exprToInfer), hasUnknownMember, expectedTypes);
    }

    public void testGetType() throws Exception {
        RubyTypeInferencer instance = getInferencer("types.rb", " l^oc = {", false);

        assertTypes(instance.inferType("x"), "Integer");
        // y is reassigned later in the file - make sure that at this
        // point in scope we have the right type
        assertTypes(instance.inferType("y"), "File");
        assertTypes(instance.inferType("$baz"), "Hash");
        assertTypes(instance.inferType("@bar"), "Fixnum");
        assertTypes(instance.inferType("@foo"), "Array");
    }

    public void testGetType2() throws Exception {
        RubyTypeInferencer instance = getInferencer("types.rb", " # d^one", false);

        // Y is assigned different types - make sure that at this position, it's a number
        assertTypes(instance.inferType("y"), "Fixnum");
        // Lots of reassignments - track types through vars, statics, fields, classvars
        assertTypes(instance.inferType("loc"), "Hash");
        assertTypes(instance.inferType("$glob"), "Hash");
        assertTypes(instance.inferType("@field"), "Hash");
        assertTypes(instance.inferType("@@clsvar"), "Hash");
        assertTypes(instance.inferType("loc2"), "Hash");
    }

    public void testMultipleAssigments() throws Exception {
        RubyTypeInferencer instance = getInferencer("types.rb", " # d^one", false);
        assertTypes(instance.inferType("q"), "Fixnum");
        assertTypes(instance.inferType("w"), "Fixnum");
        assertTypes(instance.inferType("e"), "String");
        assertTypes(instance.inferType("@r"), "String");
        assertTypes(instance.inferType("@t"), "Fixnum");
    }

    public void testNestedMultipleAssigments() throws Exception {
        RubyTypeInferencer instance = getInferencer("types.rb", " # d^one", false);
        assertTypes(instance.inferType("u"), "String");
        assertTypes(instance.inferType("i"), "Fixnum");
        assertTypes(instance.inferType("o"), "Float");
        assertTypes(instance.inferType("p"), "File");
    }

    public void testTypeAssertions() throws Exception {
        RubyTypeInferencer instance = getInferencer("types.rb", " l^oc = {", true);
        assertTypes(instance.inferType("param1"), "String");
        assertTypes(instance.inferType("param2"), "Hash");
    }

    public void testBegin() throws Exception {
        RubyTypeInferencer instance = getInferencer("types2.rb", " @f^iles = ARGV.dup", false);
        assertTypes(instance.inferType("go"), "GetoptLong");
    }

    public void testRailsController() throws Exception {
        assertTypes("type_controller.rb", "^end", "request", "ActionController::CgiRequest");
        RubyTypeInferencer instance = getInferencer("type_controller.rb", "^end", false);
        assertTypes(instance.inferType("request"), "ActionController::CgiRequest");
    }

// This test doesn't work; the behavior works in the IDE but the
// Lucene index isn't returning local symbols in the testing framework yet
//    public void testComplex1() throws Exception {
//        RubyTypeInferencer instance = getAnalyzer("types3.rb", "^caret", false);
//        assertEquals("Product", instance.getType("@product"));
//    }

//    public void testComplex2() throws Exception {
//        RubyTypeInferencer instance = getAnalyzer("types3.rb", "^caret", true);
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
        RubyTypeInferencer instance = getInferencer("migrate/20080726182725_create_posts.rb", " t.^time", true);
        assertTypes(instance.inferType("t"), "ActiveRecord::ConnectionAdapters::TableDefinition");
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

    public void testEqualsType() throws Exception {
        assertTypes("equals_type.rb", "var.t^", "var", "TrueClass", "FalseClass");
        assertTypes("equals_type.rb", "var2.t^", "var2", "TrueClass", "FalseClass");
    }

    public void testIfWithFailingInferenceInBranchType() throws Exception {
        assertTypes("if_with_failing_inference_in_branch_type.rb", "var.to_^", "var", true, "NilClass");
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
        assertTypes("constants.rb", "Colors::RED.byte^", "Colors::RED", "String");
        assertTypes("constants.rb", "puts b.down^case", "b", "String");
        // TODO fix and uncomment when reindexed
        // assertTypes("indexed constants type inference", "constants.rb", "REXML::COPY^RIGHT", "COPYRIGHT", "String");
    }

    public void testMethodType() throws Exception {
        assertTypes("method_type_inference.rb", "puts n^um.abs", "num", "Fixnum");
        assertTypes("method_type_inference.rb", "puts cons^t", "const", "Fixnum");
    }

//    public void testCoreMethodType() throws Exception {
//        assertTypes("core_methods.rb", "ance^stors.delete(String)", "ancestors", "Array");
//        assertTypes("core_methods.rb", "puts has_^one.t", "has_one", "TrueClass", "FalseClass");
//        assertTypes("core_methods.rb", "huh = a.eq^l?(123)", "a", "Fixnum", "Numeric");
//    }

    public void testMethodsChaining() throws Exception {
        assertTypes("methods_chaining.rb", "puts gree^ting", "greeting", "String");
    }
}
