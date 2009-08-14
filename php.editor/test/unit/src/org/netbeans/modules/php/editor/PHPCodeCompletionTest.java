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

package org.netbeans.modules.php.editor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;

/**
 *
 * @author tomslot
 */
public class PHPCodeCompletionTest extends PHPTestBase {

    public PHPCodeCompletionTest(String testName) {
        super(testName);
    }

    public void testPhpContextWithPrefix() throws Exception {
        checkCompletion("testfiles/completion/lib/tst.php", "^GL", false);
    }

    public void testClassMemberVisibility1() throws Exception {
        checkCompletion("testfiles/completion/lib/class_member_visibility.php", "self::^", false);
    }

    public void testClassMemberVisibility2() throws Exception {
        checkCompletion("testfiles/completion/lib/class_member_visibility.php", "parent::^", false);
    }

    public void testClassMemberVisibility3() throws Exception {
        checkCompletion("testfiles/completion/lib/class_member_visibility.php", "$tst->^", false);
    }

    public void testInterfaces1() throws Exception {
        checkCompletion("testfiles/completion/lib/interfaces.php", "$arg->^", false);
    }

    public void testVarScope1() throws Exception {
        checkCompletion("testfiles/completion/lib/var_scope.php", "echo $^", false);
    }

    public void testVarScope2() throws Exception {
        checkCompletion("testfiles/completion/lib/var_scope.php", "print $^", false);
    }

    public void testCCOnMethods0() throws Exception {
        checkCompletion("testfiles/completion/lib/test_cc_on_methods.php", "TestCCOnMethods::crea^", false);
    }

    public void testCCOnMethods1() throws Exception {
        checkCompletion("testfiles/completion/lib/test_cc_on_methods.php", "$tst1->newInstance()->^", false);
    }

    public void testCCOnMethods2() throws Exception {
        checkCompletion("testfiles/completion/lib/test_cc_on_methods.php", "TestCCOnMethods::create()->^", false);
    }

    public void testCCOnMethods3() throws Exception {
        checkCompletion("testfiles/completion/lib/test_cc_on_methods.php", "self::create()->^", false);
    }

    public void testCCOnMethods4() throws Exception {
        checkCompletion("testfiles/completion/lib/test_cc_on_methods.php", "parent::parentInstance()->pa^", false);
    }

    public void testCCOnMethods5() throws Exception {
        checkCompletion("testfiles/completion/lib/test_cc_on_methods.php", "foo_TestCCOnMethods()->^", false);
    }

    public void testCCOnMethods6() throws Exception {
        checkCompletion("testfiles/completion/lib/test_cc_on_methods.php", "foo_TestCCOnMethods()->newInstance()->n^", false);
    }

    public void testComments1() throws Exception {
        checkCompletion("testfiles/completion/lib/comments.php", "one line ^", false);
    }

    public void testComments2() throws Exception {
        checkCompletion("testfiles/completion/lib/comments.php", "multiline comment ^", false);
    }

    public void testComments3() throws Exception {
        checkCompletion("testfiles/completion/lib/comments.php", "PHPDoc comment @^", false);
    }

    public void testFunctionReturnType1() throws Exception {
        checkCompletion("testfiles/completion/lib/function_return_type.php", "$result_from_self->^", false);
    }

    public void testFunctionReturnType2() throws Exception {
        checkCompletion("testfiles/completion/lib/function_return_type.php", "$result_from_standalone_function->^", false);
    }

    public void testFunctionReturnType3() throws Exception {
        checkCompletion("testfiles/completion/lib/function_return_type.php", "$result_from_static_method->^", false);
    }

    public void testTypesInPHPDOC1() throws Exception {
        checkCompletion("testfiles/completion/lib/types_in_phpdoc.php", "* @var TypesinPHPDo^", false);
    }

    public void testTypesInPHPDOC2() throws Exception {
        checkCompletion("testfiles/completion/lib/types_in_phpdoc.php", "* @return TypesinPHPD^", false);
    }

    public void testTypesInPHPDOC3() throws Exception {
        checkCompletion("testfiles/completion/lib/types_in_phpdoc.php", "* @return TypesinPHPDoc des^", false);
    }

    public void testTypesInPHPDOC4() throws Exception {
        checkCompletion("testfiles/completion/lib/typeInPHPDoc01.php", "* @return PhpDoc01News|PhpDoc01^", false);
    }

    public void testTypesInPHPDOC5() throws Exception {
        checkCompletion("testfiles/completion/lib/typeInPHPDoc01.php", "* @return PhpDoc01News|PhpDoc01   text|@^", false);
    }

//    public void testNamespaces1() throws Exception {
//        checkCompletion("testfiles/completion/lib/namespaces1.php", "use ^", false);
//    }
//
//    public void testNamespaces2() throws Exception {
//        checkCompletion("testfiles/completion/lib/namespaces1.php", "use C^", false);
//    }
//
//    public void testNamespaces3() throws Exception {
//        checkCompletion("testfiles/completion/lib/namespaces1.php", "use ANS\\^", false);
//    }
//
//    public void testNamespaces4() throws Exception {
//        checkCompletion("testfiles/completion/lib/namespaces1.php", "use ANS\\B^", false);
//    }
//
//    public void testNamespaces5() throws Exception {
//        checkCompletion("testfiles/completion/lib/namespaces1.php", "use \\^", false);
//    }
//
//    public void testNamespaces6() throws Exception {
//        checkCompletion("testfiles/completion/lib/namespaces1.php", "use \\C^", false);
//    }
//
//    public void testNamespaces7() throws Exception {
//        checkCompletion("testfiles/completion/lib/namespaces1.php", "use \\ANS\\^", false);
//    }
//
//    public void testNamespaces8() throws Exception {
//        checkCompletion("testfiles/completion/lib/namespaces1.php", "use \\ANS\\B^", false);
//    }

    public void testPhpContext9() throws Exception {
        checkCompletion("testfiles/completion/lib/tst.php", "$GL^", false);
    }

    public void test145138_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue145138.php", "echo $param^", false);
    }

    public void test145138_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue145138.php", "print $param^", false);
    }

    public void test146176() throws Exception {
        checkCompletion("testfiles/completion/lib/issue146176.php", "echo oddlyNamedMetho^", false);
    }

    public void test145692() throws Exception {
        checkCompletion("testfiles/completion/lib/issue145692.php", "echo $test145692Instance->tst->t^", false);
    }
    
    public void test147055() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147055.php", "$test147055->^", false);
    }

    public void test145206_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue145206.php", "echo TestIssue145206 :: ^", false);
    }

    public void test145206_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue145206.php", "echo TestIssue145206 :: c^", false);
    }

    public void test145206_3() throws Exception {
        checkCompletion("testfiles/completion/lib/issue145206.php",
                "echo TestIssue145206 :: createStatic()->create() ->create() ->^", false);
    }

    public void test145206_4() throws Exception {
        checkCompletion("testfiles/completion/lib/issue145206.php",
                "echo TestIssue145206 :: createStatic()->create() ->create() -> c^", false);
    }
    
    public void test146187() throws Exception {
        checkCompletion("testfiles/completion/lib/issue146187.php",
                "echo $tst->^", false);
    }
    public void test146648() throws Exception {
        checkCompletion("testfiles/completion/lib/issue146648.php",
                "$v146648::^", false);
    }
    public void test146648_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue146648.php",
                "echo Hello146648::^", false);
    }
    public void test147191() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147191.php",
                "case self::V^", false);
    }
    public void test147575_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "else $this->^", false);
    }
    public void test147575_3() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "else $this->factory()->^", false);
    }
    public void test147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "if (1) $this->^", false);
    }
    public void test147575_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "if (1) $this->factory()->^", false);
    }
    public void test147575_4() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "for ($i = 0 ; $i < 10 ; $i++) $this->^", false);
    }
    public void test147575_5() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "for ($i = 0 ; $i < 10 ; $i++) $this->factory()->^", false);
    }
    public void test147575_6() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "while (1) $this->^", false);
    }
    public void test147575_7() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "while (1) $this->factory()->^", false);
    }
    
    ///
    public void test147575_8() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "if(1) $this->^", false);
    }
    public void test147575_9() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "if(1) $this->factory()->^", false);
    }
    
    public void test147179_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147179.php",
                "<? Test147179::^", false);
    }
    
    public void test147179_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147179.php",
                "<?= Test147179::^", false);
    }
    
    public void test2i147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "if(1) $this->factory()->^", false);
    }
    public void test3i147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "for($i = 0 ; $i < 10 ; $i++) $this->^", false);
    }
    public void test4i147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "for($i = 0 ; $i < 10 ; $i++) $this->factory()->^", false);
    }
    public void test5i147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "while(1) $this->^", false);
    }
    public void test6i147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "while(1) $this->factory()->^", false);
    }

    ///
    public void test7i147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "if(1)$this->^", false);
    }
    public void test8i147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "if(1)$this->factory()->^", false);
    }
    public void test9i147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "for($i = 0 ; $i < 10 ; $i++)$this->^", false);
    }
    public void test1o147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "for($i = 0 ; $i < 10 ; $i++)$this->factory()->^", false);
    }
    public void test2o147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "while(1)$this->^", false);
    }
    public void test3o147575() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147575.php",
                "while(1)$this->factory()->^", false);
    }
    public void test140784() throws Exception {
        checkCompletion("testfiles/completion/lib/issue140784.php",
                "return $this->teacher->^", false);
    }

    /* TEMPORARILY DISABLED
    public void test136744_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136744.php", "print $test1^", false);
    }*/

    public void test136744_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136744.php", "print $test2^", false);
    }

    public void test136744_3() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136744.php", "print $test3^", false);
    }

    public void test144830() throws Exception {
        checkCompletion("testfiles/completion/lib/issue144830.php", "$this->^", false);
    }
    
    public void test147883() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147883.php", "echo $a->^", false);
    }
    
    public void test148856() throws Exception {
        checkCompletion("testfiles/completion/lib/issue148856.php", "test148856Func(1))->^", false);
    }
    
    public void test148219() throws Exception {
        checkCompletion("testfiles/completion/lib/issue148219.php", "$newBook->^", false);
    }
    
    public void test142919() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142919.php", "echo $param^", false);
    }

    public void test136744_4() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136744.php", "print $test4^", false);
    }
    // #142024 Code completion + references
    public void test142024() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142024.php", "$t->^", false);
    }
    // #142051 CC doesn't work when an object is a refence
    public void test142051() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142051.php", "echo \"Name1: \".$user1->^", false);
    }

    public void test142051_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142051.php", "echo \"Name2: \".$user2->^", false);
    }

    public void test140633() throws Exception {
        checkCompletion("testfiles/completion/lib/issue140633.php", "echo $_COOKI^", false);
    }

    public void test141999() throws Exception {
        checkCompletion("testfiles/completion/lib/issue141999.php", "echo $test141999->^", false);
    }

    // #136092 Code completion doesn't show reference parameters
    public void test136092_withoutReference() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136092.php", "$source1 = $reques^", false);
    }
    public void test136092_withReference() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136092.php", "$source2 = $reques^", false);
    }
    // #132294 [cc] cc for variables in strings not working if there are non-ws chars preceding the variablle
    public void test132294() throws Exception {
        checkCompletion("testfiles/completion/lib/issue132294.php", "echo \"Hello $tst13229^", false);
    }
    public void test132294_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue132294.php", "echo \"Hello$tst13229^", false);
    }
    // #142234 $t->| shouldn't propose __construct()
    public void test142234() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142234.php", "$t->^", false);
    }
    public void test142234_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142234.php", "parent::^", false);
    }
    // #135618 [CC] Missing static members from parent classes after "self::"
    public void test135618() throws Exception {
        checkCompletion("testfiles/completion/lib/issue135618.php", "self::^", false);
    }
    public void test135618_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue135618.php", "B135618::^", false);
    }
    public void test135618_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue135618.php", "A135618::^", false);
    }
    public void test142091() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142091.php", "function fi142091(iface14209^", false);
    }
    public void test142091_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142091.php", "function fc142091(cls14209^", false);
    }
    // #136188 [cc] issues related to class name case-sensitivity
    public void test136188() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v1->^", false);
    }
    public void test136188_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v2->^", false);
    }

    public void test144409() throws Exception {
        checkCompletion("testfiles/completion/lib/issue144409.php", "$tmp->^", false);
    }

    public void testOptionalArgs_1() throws Exception {
        checkCompletion("testfiles/completion/lib/optional_args.php", "TestOptionalArgsClass::test^", false);
    }

    public void testOptionalArgs_2() throws Exception {
        checkCompletion("testfiles/completion/lib/optional_args.php", "$foo = testOptionalArgsFunc^", false);
    }

    //#137033: Code completion for class identifier
    public void test137033_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue137033.php", "class a^", false);
    }

    public void test137033_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue137033.php", "interface a^", false);
    }

    public void test136188_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v3->^", false);
    }
    public void test136188_3() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v1 = new Cls136188^", false);
    }
    public void test136188_4() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v2 = new cls136188^", false);
    }
    public void test136188_5() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v3 = new CLS136188^", false);
    }
    public void test149519() throws Exception {
        checkCompletion("testfiles/completion/lib/issue149519.php", "$this->^", false);
    }
    public void test154055() throws Exception {
        checkCompletion("testfiles/completion/lib/issue154055.php", "$book->^", false);
    }
    // tests for class declaration until '{' like "class name extends MyClass  "
    public void testClsDeclaration() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class^", false);
    }
    public void testClsDeclaration_1() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ^", false);
    }
    public void testClsDeclaration_2() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTes^", false);
    }
    public void testClsDeclaration_3() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest ^", false);
    }
    public void testClsDeclaration_4() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest e^", false);
    }
    public void testClsDeclaration_5() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest extends^", false);
    }
    public void testClsDeclaration_6() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest extends Cls2DeclarationTes^", false);
    }
    public void testClsDeclaration_7() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest extends Cls2DeclarationTest^", false);
    }
    public void testClsDeclaration_8() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest extends Cls2DeclarationTest ^", false);
    }
    public void testInsideClass() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "cons^", false);
    }
    public void testInsideClass_1() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "priv^", false);
    }
    public void testInsideClass_2() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "function ^", false);
    }
    public void testInsideClass_3() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "$this->setFl^", false);
    }
    public void testInsideClass_4() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "function __cons^", false);
    }
    public void testInsideClass_5() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "function __dest^", false);
    }
    public void testInsideClass_6() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "$v = new InsideCl^", false);
    }
    public void testInsideClass_7() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "public stat^", false);
    }
    public void testInsideClass_8() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "InsideClass::^", false);
    }
    public void testInsideClass_9() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass3.php", "func^", false);
    }
    public void testInsideClass_10() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass3.php", "privat^", false);
    }
    public void testInsideClass_11() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass3.php", "cons^", false);
    }
    public void testInsideClassAdv() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass2.php", "$this->^", false);
    }
    public void testInsideClassAdv_1() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass2.php", "public f^", false);
    }
    public void testInsideClassAdv_2() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass2.php", "protected f^", false);
    }
    public void testInsideClassAdv_3() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass2.php", "$bVa^", false);
    }
    public void testInsideClassAdv_4() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass2.php", "$aVar->^", false);
    }
    /* doesn't work
    public void testInsideClassAdv_5() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass2.php", "$this->meth_b^", false);
    }
     */
    public void test140758() throws Exception {
        checkCompletion("testfiles/completion/lib/issue140758.php", "echo $_SERVER['^", false);
    }
    public void test140758_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue140758.php", "echo $_SERVER[\"^", false);
    }
    public void test148213() throws Exception {
        checkCompletion("testfiles/completion/lib/issue148213.php", "$oldguy148213 = $newguy^", false);
    }

    public void testSanitizedCode() throws Exception {
        checkCompletion("testfiles/sanitize/curly04.php", "$baba = $param^", false);
    }

    public void testVarAssignment_1() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$clsVarA1=$clsVarA->^", false);
    }
    public void testVarAssignment_2() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$clsVarC1=$clsVarC->^", false);
    }
    public void testVarAssignment_3() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$clsVarA1->^", false);
    }
    public void testVarAssignment_4() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$clsVarC1->^", false);
    }
    public void testVarAssignment_5() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$clsVarA2->^", false);
    }
    public void testVarAssignment_6() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$clsVarC2->^", false);
    }
    public void testVarAssignment_7() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$stVarAA1=$stVarAA->^", false);
    }
    public void testVarAssignment_8() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$stVarAC1 = $stVarAC->^", false);
    }
    public void testVarAssignment_9() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$stVarAA1->^", false);
    }
    public void testVarAssignment_10() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$stVarAC1->^", false);
    }
    public void testVarAssignment_11() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$stVarAA2->^", false);
    }
    public void testVarAssignment_12() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$fncVarA1=$fncVarA->^", false);
    }
    public void testVarAssignment_13() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$fncVarC1=$fncVarC->^", false);
    }
    public void testVarAssignment_14() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$fncVarA1->^", false);
    }
    public void testVarAssignment_15() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$fncVarC1->^", false);
    }
    public void testVarAssignment_16() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$fncVarA2->^", false);
    }
    /*public void testVarAssignment_17() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$clsVarCErr->^", false);
    }*/
    public void testVarAssignment_18() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$aParam2->^", false);
    }
    public void testVarAssignment_19() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$cParam2->^", false);
    }
    public void testVarAssignment_20() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$this1->^", false);
    }
    public void testVarAssignment_21() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$parent1->^", false);
    }
    public void testVarAssignment_22() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$self1->^", false);
    }
    public void testVarAssignment_23() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$aParam4->^", false);
    }
    public void testVarAssignment_24() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$cParam5->^", false);
    }
    //TODO: should be evaluated later whether this way of CC should be supported
    /*public void testUnknown() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment.php", "$unknown->aCreate^", false);
    }*/
    public void test145835() throws Exception {
        checkCompletion("testfiles/completion/lib/varAssignment2.php", "$cVarAdvancedTest->^", false);
    }
    public void test148109() throws Exception {
        checkCompletion("testfiles/completion/lib/issue148109.php", "$v1480109->^", false);
    }
    public void test147427() throws Exception {
        checkCompletion("testfiles/completion/lib/issue147427.php", "$currentUser = System147427::$userInfo->^", false);
    }
    public void testTypeInCatch() throws Exception {
        checkCompletion("testfiles/completion/lib/catchinstanceof.php", "$vCatch->^", false);
    }

    public void testMixedType01() throws Exception {
        checkCompletion("testfiles/completion/lib/mixedtypes.php", "getBookMagazine()->^", false);
    }
    public void testMixedType02() throws Exception {
        checkCompletion("testfiles/completion/lib/mixedtypes.php", "$bm->^", false);
    }
    public void testMixedType03() throws Exception {
        checkCompletion("testfiles/completion/lib/mixedtypes.php", "getBook()->^", false);
    }
    public void testMixedType01_1() throws Exception {
        checkCompletion("testfiles/completion/lib/mixedtypes_1.php", "getBookMagazine()->^", false);
    }
    public void testMixedType02_1() throws Exception {
        checkCompletion("testfiles/completion/lib/mixedtypes_1.php", "$bm->^", false);
    }
    public void testMixedType03_1() throws Exception {
        checkCompletion("testfiles/completion/lib/mixedtypes_1.php", "getBook()->^", false);
    }

    public void testVarTypeCommentVariable02() throws Exception {
        checkCompletion("testfiles/completion/lib/varTypeComment.php", "/* @var $hell^", false);
    }

    public void testVarTypeCommentType02() throws Exception {
        checkCompletion("testfiles/completion/lib/varTypeComment.php", "/* @var $hello VarTypeComment^", false);
    }

    public void testIssue157534() throws Exception {
        checkCompletion("testfiles/completion/lib/issue157534.php", "issue157534^;", false);
    }
    public void testIssue157534_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue157534.php", "/**/add^", false);
    }
    public void testIssue157534_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue157534.php", "$v157534->add^", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        //just test them as standalone files (just PHP Platform in index)
        List<String> asList = Arrays.asList("testPhpContextWithPrefix", "testVarScope2", "testSanitizedCode");//NOI18N
        if (asList.contains(getName())) {
            return null;
        }
        return super.createClassPathsForTest();
    }

    /* doesn't work properly yet
    public void testTypeInInstanceof() throws Exception {
        checkCompletion("testfiles/completion/lib/catchinstanceof.php", "$vInstanceof->^", false);
    }*/
}
