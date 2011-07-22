/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
    
    public void testClassInstantiation_2() throws Exception {
        checkDeclaration(getTestPath(), "class Mammal extends Animal^ {", "abstract class ^Animal");
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

    public void testVardoc166660() throws Exception {
        //testfiles/gotodeclaration/testVardoc166660/testVardoc166660.php
        checkDeclaration(getTestPath(), "@var $testClass Test^Class", "class ^TestClass {}");
    }
    public void testVardoc166660_1() throws Exception {
        //testfiles/gotodeclaration/testVardoc166660/testVardoc166660.php
        checkDeclaration(getTestPath(), "@var $test^Class TestClass", "$^testClass = new TestClass();");
    }
    
    public void testStaticConstant197239_01() throws Exception {
        //testfiles/gotodeclaration/testStaticConstant197239/testStaticConstant197239.php
        checkDeclaration(getTestPath(), "echo static::LET^TER22;", "const ^LETTER22 = 'a';");
    }

    public void testStaticConstant197239_02() throws Exception {
        //testfiles/gotodeclaration/testStaticConstant197239/testStaticConstant197239.php
        checkDeclaration(getTestPath(), "echo self::LETT^ER22;", "const ^LETTER22 = 'a';");
    }
    
    public void testStaticConstant197239_03() throws Exception {
        //testfiles/gotodeclaration/testStaticConstant197239/testStaticConstant197239.php
        checkDeclaration(getTestPath(), "echo AA::LETT^ER22;", "const ^LETTER22 = 'a';");
    }
    
    public void testMixedTypes200156_01() throws Exception {
        checkDeclaration(getTestPath(), "* @property F^oo|Bar $property", "class ^Foo {");
    }
    
    public void testMixedTypes200156_02() throws Exception {
        checkDeclaration(getTestPath(), "* @property Foo|B^ar $property", "class ^Bar {");
    }
    
    public void testMixedTypes200156_03() throws Exception {
        checkDeclaration(getTestPath(), "     * @var Fo^o|Bar", "class ^Foo {");
    }
    
    public void testMixedTypes200156_04() throws Exception {
        checkDeclaration(getTestPath(), "     * @var Foo|Ba^r", "class ^Bar {");
    }
    
    // uncomment when issue #200161 will be fixed
//    public void testMixedTypes200156_05() throws Exception {
//        checkDeclaration(getTestPath(), "* @method Fo^o|Bar m1() m1(Foo|Bar $param) a magic method declaration", "class ^Foo {");
//    }
//    
//    public void testMixedTypes200156_06() throws Exception {
//        checkDeclaration(getTestPath(), "* @method Foo|B^ar m1() m1(Foo|Bar $param) a magic method declaration", "class ^Bar {");
//    }
    
    public void testMixedTypes200156_07() throws Exception {
        checkDeclaration(getTestPath(), "* @method Foo|Bar m1() m1(F^oo|Bar $param) a magic method declaration", "class ^Foo {");
    }
    
    public void testMixedTypes200156_08() throws Exception {
        checkDeclaration(getTestPath(), "* @method Foo|Bar m1() m1(Foo|B^ar $param) a magic method declaration", "class ^Bar {");
    }
     
    //TODO: these tests need to be checked, filtered , rewritten , enabled
//         public void testImplementsInterface() throws Exception {
//        String gotoTest2 = prepareTestFile(
//                "testfiles/classMan.php",
//                "implements Person {",
//                "implements P|erson {"
//                );
//        String gotoTest = prepareTestFile(
//                "testfiles/classPerson.php",
//                "interface Person {",
//                "interface ^Person {"
//                );
//        performTestSimpleFindDeclaration(-1, gotoTest2, gotoTest);
//
//    }
//
//    public void testGotoTypeClsIface6() throws Exception {
//        String gotoTest = prepareTestFile(
//                "testfiles/gotoType2.php",
//                "interface ifaceDeclaration4 {}",
//                "interface ^ifaceDeclaration4 {}"
//                );
//        String gotoTest2 = prepareTestFile(
//                "testfiles/gotoType.php",
//                "class clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}",
//                "class clsDeclaration4 extends clsDeclaration3 implements ifaceDecla|ration4 {}"
//                );
//        performTestSimpleFindDeclaration(-1, gotoTest2, gotoTest);
//    }
//
//    public void testGotoTypeClsIfaceFromalParam3() throws Exception {
//        String gotoTest = prepareTestFile(
//                "testfiles/gotoType2.php",
//                "interface ifaceDeclaration4 {}",
//                "interface ^ifaceDeclaration4 {}"
//                );
//        String gotoTest2 = prepareTestFile(
//                "testfiles/gotoType.php",
//                "ifaceDeclaration4 $ifaceDeclaration4Var,",
//                "ifaceD|eclaration4 $ifaceDeclaration4Var,"
//                );
//        performTestSimpleFindDeclaration(-1, gotoTest2, gotoTest);
//    }
//    public void testStaticFieldAccessInOtherFile() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static $count = 0, $animal;",
//                "public static $^count = 0, $animal;"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "return Animal::$count;",
//                "return Animal::$cou|nt;;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testStaticFieldAccessInOtherFileRef() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static $count = 0, $animal;",
//                "public static $^count = 0, $animal;"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "return Animal::$count;",
//                "return &Animal::$cou|nt;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testStaticFieldAccessParentInOtherFile() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static $count = 0, $animal;",
//                "public static $^count = 0, $animal;"
//                /*maybe a bug but not important I guess. Sometimes jumps:
//                 * 1/public static ^$count = 0, $animal;
//                 * 2/^public static $count = 0, $animal;
//                 */
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "parent::$count;",
//                "parent::$cou|nt;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testStaticFieldAccessParentInOtherFileRef() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static $count = 0, $animal;",
//                "public static $^count = 0, $animal;"
//                /*maybe a bug but not important I guess. Sometimes jumps:
//                 * 1/public static ^$count = 0, $animal;
//                 * 2/^public static $count = 0, $animal;
//                 */
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "parent::$count;",
//                "&parent::$cou|nt;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//
//    public void testStaticFieldAccessInOtherFile_ClassName() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "return Animal::$count;",
//                "return Ani|mal::$count;;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testStaticFieldAccessInOtherFile_ClassNameRef() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "return Animal::$count;",
//                "return &Ani|mal::$count;;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testSuperClassesOtherClass() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "class Fish extends Animal {",
//                "class Fish extends Anim|al {"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOtherClassThis() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "$this->getCount(\"\")",
//                "$this->getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOtherClassParent() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "parent::getCount(\"\")",
//                "parent::getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOtherClassSelf() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "self::getCount(\"\")",
//                "self::getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOther2() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "$mammal->getCount(\"\")",
//                "$mammal->getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOther4() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "$fish->getCount(\"\")",
//                "$fish->getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOther5() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "public function getCount($sharkLogging) {",
//                "public function ^getCount($sharkLogging) {",
//                "$shark->getCount(\"\");",
//                "$$shark->getCou|nt(\"\");"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest);
//    }
//    public void testConstantAccess() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;",
//                "echo self::KIND;",
//                "echo self::KIN|D;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest);
//    }
//    public void testConstantAccess12() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Animal::KIND;",
//                "echo Animal::KIN|D;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess12_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Animal::KIND;",
//                "echo Ani|mal::KIND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess13() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Mammal::KIND;",
//                "echo Mammal::KI|ND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess13_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "class Mammal extends Animal {",
//                "class ^Mammal extends Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Mammal::KIND;",
//                "echo Mamm|al::KIND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess14() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=3;",
//                "const ^KIND=3;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Cat::KIND;",
//                "echo Cat::KI|ND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess15() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "print Animal::KIND;",
//                "print Animal::KIN|D;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess15_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "print Animal::KIND;",
//                "print Anim|al::KIND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess16() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "print Mammal::KIND;",
//                "print Mammal::KIN|D;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess16_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "class Mammal extends Animal {",
//                "class ^Mammal extends Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "print Mammal::KIND;",
//                "print Ma|mmal::KIND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//
//    public void testStaticMethodInvocation8() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"animal is ...\";}",
//                "public static function ^kindInfo() {return \"animal is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Animal::kindInfo();",
//                "echo Animal::kindIn|fo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation8_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Animal::kindInfo();",
//                "echo Ani|mal::kindInfo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation9() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"animal is ...\";}",
//                "public static function ^kindInfo() {return \"animal is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Mammal::kindInfo();",
//                "echo Mammal::kindI|nfo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation9_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "class Mammal extends Animal {",
//                "class ^Mammal extends Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Mammal::kindInfo();",
//                "echo Mam|mal::kindInfo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    /* TODO: fails, evaluate, fix
//     public void testStaticMethodInvocation10() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"cat is ...\";}",
//                "^public static function kindInfo() {return \"cat is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Cat::kindInfo();",
//                "echo Cat::kindIn|fo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }*/
//    public void testStaticMethodInvocation10_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "class Cat extends Mammal {",
//                "class ^Cat extends Mammal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Cat::kindInfo();",
//                "echo C|at::kindInfo();"
//                );
//        performTestSimpleFindDeclaration(-1,animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation11() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"animal is ...\";}",
//                "public static function ^kindInfo() {return \"animal is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo self::kindInfo();",
//                "echo self::kindIn|fo();"
//                );
//        performTestSimpleFindDeclaration(-1,animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation12() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"animal is ...\";}",
//                "public static function ^kindInfo() {return \"animal is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo parent::kindInfo();",
//                "echo parent::kindIn|fo();"
//                );
//        performTestSimpleFindDeclaration(-1,animalTest2, animalTest);
//    }
//
//
//    public void testDefines2() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "echo \"a\".te|st.\"b\";\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "define('^test', 'test');\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration1() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n$^name = \"test\";\n echo \"$na|me\";\n?>");
//    }
//
//    public void testSimpleFindDeclaration2() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n$^name = \"test\";\n$name = \"test\";\n echo \"$na|me\";\n?>");
//    }
//
//    public void testSimpleFindDeclaration3() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n$^name = \"test\";\n$name = \"test\";\n echo \"$na|me\";\n$name = \"test\";\n?>");
//    }
//
//    public void testSimpleFindDeclaration4() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "$name = \"test\";\n" +
//                                         "function foo($^name) {\n" +
//                                         "    echo \"$na|me\";\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration5() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "$^name = \"test\";\n" +
//                                         "function foo($name) {\n" +
//                                         "}\n" +
//                                         "echo \"$na|me\";\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration6() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "$name = \"test\";\n" +
//                                         "function ^foo($name) {\n" +
//                                         "}\n" +
//                                         "fo|o($name);\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration7() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "class ^name {\n" +
//                                         "}\n" +
//                                         "$r = new na|me();\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration8() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "class name {\n" +
//                                         "    function ^test() {" +
//                                         "    }" +
//                                         "}\n" +
//                                         "$r = new name();\n" +
//                                         "$r->te|st();" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration9() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "$^name = \"test\";\n" +
//                                         "function foo($name) {\n" +
//                                         "}\n" +
//                                         "foo($na|me);\n" +
//                                         "?>");
//    }
//
//    public void testFindDeclarationInOtherFile1() throws Exception {
//        performTestSimpleFindDeclaration(1,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testFindDeclarationInOtherFile2() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "include \"testb.php\";\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testFindDeclarationInOtherFile3() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "$r = new fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "include \"testb.php\";\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class ^foo {}\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class ^foo {}\n" +
//                                         "?>");
//    }
//
//    public void testFunctionsInGlobalScope1() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "function bar() {\n" +
//                                         "    fo|o();\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testClassInGlobalScope1() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "function foo() {" +
//                                         "    class ^bar {}\n" +
//                                         "}\n" +
//                                         "$r = new b|ar();\n" +
//                                         "?>");
//    }
//
//    public void testArrayVariable() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "$^foo = array();\n" +
//                                         "$f|oo['test'] = array();\n" +
//                                         "?>");
//    }
//
//    public void testResolveUseBeforeDeclaration() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "fo|o();\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testShowAllDeclarationsWhenUnknownForFunctions() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>",
//                                          "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testShowAllDeclarationsWhenUnknownForClasses() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "$r = new fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class ^foo {}\n" +
//                                         "?>",
//                                          "<?php\n" +
//                                         "class ^foo {}\n" +
//                                         "?>");
//    }
//
//    public void testDefines1() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "define('^test', 'test');\n" +
//                                         "echo \"a\".te|st.\"b\";\n" +
//                                         "?>");
//    }
//
//    public void testGoToInherited() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "class foo {\n" +
//                                         "    function ^test() {}\n" +
//                                         "}\n" +
//                                         "class bar extends foo {\n" +
//                                         "}\n" +
//                                         "$r = new bar();\n" +
//                                         "$r->te|st();" +
//                                         "?>");
//    }
//
//    public void testGoToInclude01() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include \"te|sta.php\";\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude02() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include ('|testa.php');\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude03() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "require 'testa.php|';\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude04() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include_once '|testa.php';\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude05() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include_once ('|testa.php');\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude06() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "require_once '|testa.php';\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude07() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "require_once (\"|testa.php\");\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInstanceVar() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "class test {\n" +
//                                         "    function ftest($name) {\n" +
//                                         "        $this->na|me = $name;\n" +
//                                         "    }\n" +
//                                         "    var $^name;\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testGoToForward() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "class test {\n" +
//                                         "    function ftest($name) {\n" +
//                                         "        $this->na|me();\n" +
//                                         "    }\n" +
//                                         "    function ^name() {}\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testMethodInOtherFile() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "$r = new foo();\n" +
//                                         "$r->ffo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "include \"testb.php\";\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class foo {\n" +
//                                         "    function ^ffoo() {\n" +
//                                         "    }\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testMethodInOtherFileWithInheritance() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "$r = new foo2();\n" +
//                                         "$r->ffo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "include \"testb.php\";\n" +
//                                         "class foo2 extends foo3 {}\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class foo3 {\n" +
//                                         "    function ^ffoo() {\n" +
//                                         "    }\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testExtendedClass() throws Exception {
//        String userClass = prepareTestFile(
//                "testfiles/classUser.php",
//                "extends Man {",
//                "extends M|an {");
//        String manClass = prepareTestFile(
//                "testfiles/classMan.php",
//                "class Man implements Person {",
//                "class ^Man implements Person {");
//        performTestSimpleFindDeclaration(-1, userClass, manClass);
//    }
//
//    public void testPHPDocType01() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "class Magazine {\n" +
//                                         "    public $title;\n" +
//                                         "}\n" +
//                                         "class ^Book { \n" +
//                                         "    public $author;\n" +
//                                         "}\n" +
//                                         "/**\n" +
//                                         " * @param Bo|ok $hello\n" +
//                                         " * @return Magazine test\n" +
//                                         " */\n" +
//                                         "function test($hello) {\n" +
//                                         "}\n" +
//                                         "?>\n");
//    }
//
//    public void testPHPDocType02() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "class ^Magazine {\n" +
//                                         "    public $title;\n" +
//                                         "}\n" +
//                                         "class Book { \n" +
//                                         "    public $author;\n" +
//                                         "}\n" +
//                                         "/**\n" +
//                                         " * @param Book $hello\n" +
//                                         " * @return Mag|azine test\n" +
//                                         " */\n" +
//                                         "function test($hello) {\n" +
//                                         "}\n" +
//                                         "?>\n");
//    }
//
//    public void testPHPDocParamName() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "/**\n" +
//                                         " *\n" +
//                                         " * @param  string $he|llo\n" +
//                                         " */\n" +
//                                        "function test($^hello) {\n" +
//                                         "}\n" +
//                                         "?> ");

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
