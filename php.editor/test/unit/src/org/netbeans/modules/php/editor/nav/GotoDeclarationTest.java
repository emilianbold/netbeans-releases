/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.nav;

import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous
 */
public class GotoDeclarationTest extends TestBase {

    public GotoDeclarationTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testConstAccesInFldDecl() throws Exception {
//testfiles/gotodeclaration/testConstAccesInFldDecl/testConstAccesInFldDecl.php
        checkDeclaration(getTestPath(), "public $fetchMode = self::FETCH_E^AGER;", "const ^FETCH_EAGER = 3;");
    }

    public void testParamVarPropInPhpDocTest_2() throws Exception {
//testfiles/gotodeclaration/testParamVarPropInPhpDocTest/testParamVarPropInPhpDocTest.php
        checkDeclaration(getTestPath(), "$tmp = $hel^lo;", "function test($^hello) {//method");
    }

    public void testClsVarPropInPhpDocTest() throws Exception {
//testfiles/gotodeclaration/testClsVarPropInPhpDocTest/testClsVarPropInPhpDocTest.php
        checkDeclaration(getTestPath(), " * @property Au^thor $author hello this is doc", "class ^Author {");
    }

    public void testClsVarPropInPhpDocTest_2() throws Exception {
//testfiles/gotodeclaration/testClsVarPropInPhpDocTest/testClsVarPropInPhpDocTest.php
        checkDeclaration(getTestPath(), "$this->auth^or;", " * @property Author $^author hello this is doc");
    }

    public void testGotoConstructTest() throws Exception {
//testfiles/gotodeclaration/testGotoConstructTest/testGotoConstructTest.php
        checkDeclaration(getTestPath(), "$a = new MyCla^ssConstr();", "public function ^__construct() {//MyClassConstr");
    }

    public void testGotoConstructTest_2() throws Exception {
//testfiles/gotodeclaration/testGotoConstructTest/testGotoConstructTest.php
        checkDeclaration(getTestPath(), "$b = new MyCla^ssConstr2();", "class ^MyClassConstr2 extends MyClassConstr  {}//MyClassConstr2");
    }

    public void testIfaceTest() throws Exception {
//testfiles/gotodeclaration/testIfaceTest/testIfaceTest.php
        checkDeclaration(getTestPath(), "myf^ace::RECOVER_ORIG;", "interface ^myface {");
    }

    public void testIfaceTest_2() throws Exception {
//testfiles/gotodeclaration/testIfaceTest/testIfaceTest.php
        checkDeclaration(getTestPath(), "myface::REC^OVER_ORIG;", "const ^RECOVER_ORIG = 2;");
    }

    public void testIfaceTest_3() throws Exception {
//testfiles/gotodeclaration/testIfaceTest/testIfaceTest.php
        checkDeclaration(getTestPath(), "myc^ls::RECOVER_ORIG;", "class ^mycls implements myface {");
    }

    public void testIfaceTest_4() throws Exception {
//testfiles/gotodeclaration/testIfaceTest/testIfaceTest.php
        checkDeclaration(getTestPath(), "mycls::REC^OVER_ORIG;", "const ^RECOVER_ORIG = 1;");
    }

    public void testIfaceTest_5() throws Exception {
//testfiles/gotodeclaration/testIfaceTest/testIfaceTest.php
        checkDeclaration(getTestPath(), "$a->mf^nc();//mycls", "function ^mfnc() {}//mycls");
    }

    public void testIfaceTest_6() throws Exception {
//testfiles/gotodeclaration/testIfaceTest/testIfaceTest.php
        checkDeclaration(getTestPath(), "$a->mfn^c();//myface", "function ^mfnc();//myface");
    }

    public void testGotoTypeClsIface() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIface/testGotoTypeClsIface.php
        checkDeclaration(getTestPath(), "interface ifaceDeclaration2 extends ifaceDec^laration  {}", "interface ^ifaceDeclaration {}");
    }

    public void testGotoTypeClsIface_2() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIface/testGotoTypeClsIface.php
        checkDeclaration(getTestPath(), "class clsDeclaration implements ifaceDecl^aration {}", "interface ^ifaceDeclaration {}");
    }

    public void testGotoTypeClsIface_3() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIface/testGotoTypeClsIface.php
        checkDeclaration(getTestPath(), "class clsDeclaration2 implements ifaceDec^laration, ifaceDeclaration2 {}", "interface ^ifaceDeclaration {}");
    }

    public void testGotoTypeClsIface_4() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIface/testGotoTypeClsIface.php
        checkDeclaration(getTestPath(), "class clsDeclaration3 extends clsDeclarat^ion {}", "class ^clsDeclaration implements ifaceDeclaration {}");
    }

    public void testGotoTypeClsIface_5() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIface/testGotoTypeClsIface.php
        checkDeclaration(getTestPath(), "class clsDeclaration2 implements ifaceDeclaration, ifaceDecla^ration2 {}", "interface ^ifaceDeclaration2 extends ifaceDeclaration  {}");
    }

    public void testGotoTypeClsIfaceFromalParam() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIfaceFromalParam/testGotoTypeClsIfaceFromalParam.php
        checkDeclaration(getTestPath(), "ifaceD^eclaration $ifaceDeclarationVar,", "interface ^ifaceDeclaration {}");
    }

    public void testGotoTypeClsIfaceFromalParam_2() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIfaceFromalParam/testGotoTypeClsIfaceFromalParam.php
        checkDeclaration(getTestPath(), "ifaceD^eclaration2 $ifaceDeclaration2Var,", "interface ^ifaceDeclaration2 extends ifaceDeclaration  {}");
    }

    public void testGotoTypeClsIfaceFromalParam_4() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIfaceFromalParam/testGotoTypeClsIfaceFromalParam.php
        checkDeclaration(getTestPath(), "clsD^eclaration  $clsDeclarationVar,", "class ^clsDeclaration implements ifaceDeclaration {}");
    }

    public void testGotoTypeClsIfaceFromalParam_5() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIfaceFromalParam/testGotoTypeClsIfaceFromalParam.php
        checkDeclaration(getTestPath(), "clsDeclara^tion2 $clsDeclaration2Var,", "class ^clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}");
    }

    public void testGotoTypeClsIfaceFromalParam_6() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIfaceFromalParam/testGotoTypeClsIfaceFromalParam.php
        checkDeclaration(getTestPath(), "clsDe^claration3 $clsDeclaration3Var,", "class ^clsDeclaration3 extends clsDeclaration {}");
    }

    public void testGotoTypeClsIfaceFromalParam_7() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIfaceFromalParam/testGotoTypeClsIfaceFromalParam.php
        checkDeclaration(getTestPath(), "clsDeclar^ation4 $clsDeclaration4Var", "class ^clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}");
    }

    public void testGotoTypeClsIfaceCatch() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIfaceCatch/testGotoTypeClsIfaceCatch.php
        checkDeclaration(getTestPath(), "} catch (clsDecla^ration $cex) {", "class ^clsDeclaration implements ifaceDeclaration {}");
    }

    public void testGotoTypeClsIfaceInstanceof() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIfaceInstanceof/testGotoTypeClsIfaceInstanceof.php
        checkDeclaration(getTestPath(), "if ($cex instanceof clsDecl^aration) {", "class ^clsDeclaration implements ifaceDeclaration {}");
    }

    public void testGotoTypeClsIfaceInstanceof_2() throws Exception {
//testfiles/gotodeclaration/testGotoTypeClsIfaceInstanceof/testGotoTypeClsIfaceInstanceof.php
        checkDeclaration(getTestPath(), "if ($c^ex instanceof clsDeclaration) {", "} catch (clsDeclaration $^cex) {");
    }

    public void testGotoTypeArrays() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= self::$static_a^rray[$idx++];", "private static $^static_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_2() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= self::$static^_array[$instance_array[$idx]];", "private static $^static_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_3() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= $this->field_a^rray[$idx++];", "private $^field_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_4() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= $this->field_^array[$instance_array[$idx]];", "private $^field_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_5() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$instan^ce_array[$idx];", "$^instance_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_6() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= self::$static_array[$instance_^array[$idx]];", "$^instance_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_7() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= $this->field_array[$instan^ce_array[$idx]];", "$^instance_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_8() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= self::$static_array[$id^x++];", "$^idx = 1;");
    }

    public void testGotoTypeArrays_9() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= $this->field_array[$i^dx++];", "$^idx = 1;");
    }

    public void testGotoTypeArrays_10() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= self::$static_array[$instance_array[$id^x]];", "$^idx = 1;");
    }

    public void testGotoTypeArrays_11() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$result .= $this->field_array[$instance_array[$id^x]];", "$^idx = 1;");
    }

    public void testGotoTypeArrays_12() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$instance_array2[$idx^2];", "$^idx2 = 1;");
    }

    public void testGotoTypeArrays_13() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$instance_a^rray2[$idx2];", "$^instance_array2 = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_14() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$instance_array3[$id^x3];", "$^idx3 = 1;");
    }

    public void testGotoTypeArrays_15() throws Exception {
//testfiles/gotodeclaration/testGotoTypeArrays/testGotoTypeArrays.php
        checkDeclaration(getTestPath(), "$instance_ar^ray3[$idx3];", "$^instance_array3 = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testFuncParamAsReference() throws Exception {
//testfiles/gotodeclaration/testFuncParamAsReference/testFuncParamAsReference.php
        checkDeclaration(getTestPath(), "$par^am++;", "function funcWithRefParam(&$^param) {");
    }

    public void testStaticFieldAccess() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Animal::$cou^nt;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_ArrayIndex() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "$species = self::$animalSpec^ies;", "static $^animalSpecies = array();");
    }

    public void testStaticFieldAccess_ArrayIndex2() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "$first = self::$animalSpec^ies[0];", "static $^animalSpecies = array();");
    }

    public void testStaticFieldAccess_2() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Cat::$cou^nt;", "public static $^count = 0, $cat;");
    }

    public void testStaticFieldAccess_OutsideClass() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Animal::$co^unt--;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_OutsideClass2() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Cat::$co^unt--;", "public static $^count = 0, $cat;");
    }

    public void testStaticFieldAccess_OutsideClassDeclaredInSuperClass() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Mammal::$co^unt--;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_Self() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "self::$ani^mal = $this;", "public static $count = 0, $^animal;");
    }

    public void testStaticFieldAccess_SelfDeclaredInSuperClass() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "echo self::$cou^nt;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_ParentDeclaredInSuperClass() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "echo parent::$cou^nt;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_Parent() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "echo parent::$cou^nt;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_ClassName() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Ani^mal::$count;", "abstract class ^Animal {");
    }

    public void testStaticFieldAccess_2ClassName() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Ca^t::$count;", "class ^Cat extends Mammal {");
    }

    public void testStaticFieldAccess_OutsideClass_ClassName() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Ani^mal::$count--;", "abstract class ^Animal {");
    }

    public void testStaticFieldAccess_OutsideClass2_ClassName() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Ca^t::$count--;", "class ^Cat extends Mammal {");
    }

    public void testStaticFieldAccess_OutsideClassDeclaredInSuperClass_ClassName() throws Exception {
//testfiles/gotodeclaration/testStaticFieldAccess/testStaticFieldAccess.php
        checkDeclaration(getTestPath(), "Mam^mal::$count--;", "class ^Mammal extends Animal {");
    }

    public void testClassInstantiation() throws Exception {
//testfiles/gotodeclaration/testClassInstantiation/testClassInstantiation.php
        checkDeclaration(getTestPath(), "$mammal = new Mamm^al;", "function ^__construct() {//Mammal");
    }

    public void testSuperClasses() throws Exception {
//testfiles/gotodeclaration/testSuperClasses/testSuperClasses.php
        checkDeclaration(getTestPath(), "class Cat extends Mamm^al {", "class ^Mammal extends Animal {");
    }

    public void testMethodInvocation_Parent() throws Exception {
//testfiles/gotodeclaration/testMethodInvocation/testMethodInvocation.php
        checkDeclaration(getTestPath(), "echo parent::getC^ount(\"calling animal's getCount 1\");", "public function ^getCount($animalLogging) {");
    }

    public void testMethodInvocation_Parent2() throws Exception {
//testfiles/gotodeclaration/testMethodInvocation/testMethodInvocation.php
        checkDeclaration(getTestPath(), "echo parent::getC^ount(\"calling animal's getCount 2\");", "public function ^getCount($animalLogging) {");
    }

    public void testMethodInvocation() throws Exception {
//testfiles/gotodeclaration/testMethodInvocation/testMethodInvocation.php
        checkDeclaration(getTestPath(), "$mammal->get^Count(\"calling animal's getCount 3\");", "public function ^getCount($animalLogging) {");
    }

    public void testMethodInvocation_Constructor() throws Exception {
//testfiles/gotodeclaration/testMethodInvocation/testMethodInvocation.php
        checkDeclaration(getTestPath(), "parent::__constr^uct", "function ^__construct() {");
    }

    public void testMethodInvocation_2() throws Exception {
//testfiles/gotodeclaration/testMethodInvocation/testMethodInvocation.php
        checkDeclaration(getTestPath(), "$cat->getCo^unt(\"calling cat's getCount 1\");", "public function ^getCount($catLogging) {");
    }

    public void testMethodInvocation_ParentThis() throws Exception {
//testfiles/gotodeclaration/testMethodInvocation/testMethodInvocation.php
        checkDeclaration(getTestPath(), "echo $this->getCou^nt(\"calling cat's getCount\");", "public function ^getCount($catLogging) {");
    }

    public void testMethodInvocation_Self() throws Exception {
//testfiles/gotodeclaration/testMethodInvocation/testMethodInvocation.php
        checkDeclaration(getTestPath(), "self::get^Count(\"calling animal's getCount 0\");", "public function ^getCount($animalLogging) {");
    }

    public void testConstantAccess_2() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "$isMe = (self::KI^ND == $mammalKind);", "const ^KIND=1;");
    }

    public void testConstantAccess_2_1() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "$isParentAnimal = (parent::KI^ND == $animalKind);", "const ^KIND=1;");
    }

    public void testConstantAccess_3() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "$mammalKind = Mammal::KIN^D;", "const ^KIND=1;");
    }

    public void testConstantAccess_3_1() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "$mammalKind = Mam^mal::KIND;", "class ^Mammal extends Animal {");
    }

    public void testConstantAccess_4() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "$animalKind = Animal::KI^ND;", "const ^KIND=1;");
    }

    public void testConstantAccess_4_1() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "$animalKind = Ani^mal::KIND;", "abstract class ^Animal {");
    }

    public void testConstantAccess_5() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "$catKind = self::KIN^D;", "const ^KIND=3;");
    }

    public void testConstantAccess_6() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "echo Animal::KIN^D;", "const ^KIND=1;");
    }

    public void testConstantAccess_6_1() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "echo Ani^mal::KIND;", "abstract class ^Animal {");
    }

    public void testConstantAccess_7() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "echo Mammal::KI^ND;", "const ^KIND=1;");
    }

    public void testConstantAccess_7_1() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "echo Mamm^al::KIND;", "class ^Mammal extends Animal {");
    }

    public void testConstantAccess_8() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "echo Cat::KI^ND;", "const ^KIND=3;");
    }

    public void testConstantAccess_8_1() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "echo Ca^t::KIND;", "class ^Cat extends Mammal {");
    }

    public void testConstantAccess_9() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "print Animal::KI^ND;", "const ^KIND=1;");
    }

    public void testConstantAccess_9_1() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "print Ani^mal::KIND;", "abstract class ^Animal {");
    }

    public void testConstantAccess_10() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "print Mammal::KIN^D;", "const ^KIND=1;");
    }

    public void testConstantAccess_10_1() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "print Mam^mal::KIND;", "class ^Mammal extends Animal {");
    }

    public void testConstantAccess_11() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "print Cat::KI^ND;", "const ^KIND=3;");
    }

    public void testConstantAccess_11_1() throws Exception {
//testfiles/gotodeclaration/testConstantAccess/testConstantAccess.php
        checkDeclaration(getTestPath(), "print Ca^t::KIND;", "class ^Cat extends Mammal {");
    }

    public void testStaticMethodInvocation_First() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "echo Mammal::$co^unt;", "public static $^count = 0, $animal;");
    }

    public void testStaticMethodInvocation() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "echo Animal::kindIn^fo();", "public static function ^kindInfo() {return \"animal is ...\";}");
    }

    public void testStaticMethodInvocation_1_2() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "echo Mammal::kindI^nfo();", "public static function ^kindInfo() {return \"animal is ...\";}");
    }

    public void testStaticMethodInvocation_1_1() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "echo Mam^mal::kindInfo();", "class ^Mammal extends Animal {");
    }

    public void testStaticMethodInvocation_2() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "echo Cat::kindIn^fo();", "public static function ^kindInfo() {return \"cat is ...\";}");
    }

    public void testStaticMethodInvocation_2_1() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "echo C^at::kindInfo();", "class ^Cat extends Mammal {");
    }

    public void testStaticMethodInvocation_3() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "echo self::kindIn^fo();", "public static function ^kindInfo() {return \"cat is ...\";}");
    }

    public void testStaticMethodInvocation_4() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "echo parent::kindIn^fo();", "public static function ^kindInfo() {return \"animal is ...\";}");
    }

    public void testStaticMethodInvocation_5_1() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "print Anim^al::kindInfo();", "abstract class ^Animal {");
    }

    public void testStaticMethodInvocation_6() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "print Mammal::kindIn^fo();", "public static function ^kindInfo() {return \"animal is ...\";}");
    }

    public void testStaticMethodInvocation_6_1() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "print Mam^mal::kindInfo();", "class ^Mammal extends Animal {");
    }

    public void testStaticMethodInvocation_7() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "print Cat::kindIn^fo();", "public static function ^kindInfo() {return \"cat is ...\";}");
    }

    public void testStaticMethodInvocation_7_1() throws Exception {
//testfiles/gotodeclaration/testStaticMethodInvocation/testStaticMethodInvocation.php
        checkDeclaration(getTestPath(), "print Ca^t::kindInfo();", "class ^Cat extends Mammal {");
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        return new FileObject[]{FileUtil.toFileObject(new File(getDataDir(), getTestFolderPath()))};
    }

    private String getTestFolderPath() {
        return "testfiles/gotodeclaration/" + getTestName();
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestName() + ".php";
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }
}
