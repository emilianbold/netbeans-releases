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
 * @author Radek Matous
 */
public class OccurrencesFinderImplTest extends PHPNavTestBase {

    public OccurrencesFinderImplTest(String testName) {
        super(testName);
    }

    public void testGotoLabel() throws Exception {
        checkOccurrences(getTestPath(), "goto en^d;", true);
    }

    public void testOccurrencesInstanceMethod() throws Exception {
        checkOccurrences(getTestPath(), "$this->na^me();", true);
    }

    public void testOccurrencesDefines() throws Exception {
        checkOccurrences(getTestPath(), "echo \"fff\".t^est.\"ddddd\";", true);
    }

    public void testOccurrencesInstanceVarParam() throws Exception {
        checkOccurrences(getTestPath(), "$this->name = $na^me;", true);
    }
    public void testOccurrencesInstanceVarParam_1() throws Exception {
        checkOccurrences(getTestPath(), "$this->na^me = $name;", true);
    }

    public void testOccurrencesClassHeader() throws Exception {
        checkOccurrences(getTestPath(), "class fo^o", true);
    }

    public void testOccurrences1() throws Exception {
        checkOccurrences(getTestPath(), "function fo^o", true);
    }

    public void testOccurrences2() throws Exception {
        checkOccurrences(getTestPath(), "echo $na^me;", true);
    }

    public void testOccurrences3() throws Exception {
        checkOccurrences(getTestPath(), "echo $na^me;", true);
    }
    public void testOccurrences4() throws Exception {
        checkOccurrences(getTestPath(), "echo \"$na^me\";", true);
    }

    public void testGotoConstructTest() throws Exception {
        checkOccurrences(getTestPath(), "$a = new MyCla^ssConstr(", true);
    }

    public void testGotoConstructTest_2() throws Exception {
        checkOccurrences(getTestPath(), "$b = new MyClass^Constr2(", true);
    }

    public void testParamVarPropInPhpDocTest() throws Exception {
        checkOccurrences(getTestPath(), "* @param Book $he^llo", true);
    }

    public void testMarkReturnsOnConstructorTest() throws Exception {
        checkOccurrences(getTestPath(), "funct^ion __construct() {}//Auth", true);
    }

    public void testMarkReturnsOnConstructorTest_2() throws Exception {
        checkOccurrences(getTestPath(), "funct^ion __construct() {}//Bo", true);
    }

    public void testClsVarPropInPhpDocTest() throws Exception {
        checkOccurrences(getTestPath(), "* @return Aut^hor", true);
    }

    public void testIfaceTest() throws Exception {
        checkOccurrences(getTestPath(), "class mycls implements my^face", true);
    }

    public void testIfaceTest_2() throws Exception {
        checkOccurrences(getTestPath(), "const REC^OVER_ORIG = ", true);
    }

    public void testIfaceTest_3() throws Exception {
        checkOccurrences(getTestPath(), "class my^cls implements myface", true);
    }

    public void testIfaceTest_4() throws Exception {
        checkOccurrences(getTestPath(), "const RECOV^ER_ORIG = ", true);
    }

    public void testMarkClsIface() throws Exception {
        checkOccurrences(getTestPath(), "class clsDecla^ration implements ifaceDeclaration ", true);
    }

    public void testMarkClsIface_2() throws Exception {
        checkOccurrences(getTestPath(), "class clsDeclaration3 extends clsDec^laration ", true);
    }

    public void testMarkClsIface_3() throws Exception {
        checkOccurrences(getTestPath(), "interface ifaceDec^laration ", true);
    }

    public void testMarkClsIface_4() throws Exception {
        checkOccurrences(getTestPath(), "interface ifaceDeclaration2 extends ifaceDecl^aration  ", true);
    }

    public void testMarkClsIface_5() throws Exception {
        checkOccurrences(getTestPath(), "class clsDeclaration implements ifaceDeclara^tion ", true);
    }

    public void testMarkClsIface_6() throws Exception {
        checkOccurrences(getTestPath(), "class clsDeclaration2 implements ifaceDecla^ration, ifaceDeclaration2 ", true);
    }

    public void testMarkClsIface_7() throws Exception {
        checkOccurrences(getTestPath(), "interface ifaceDecl^aration2 extends ifaceDeclaration  ", true);
    }

    public void testMarkClsIface_8() throws Exception {
        checkOccurrences(getTestPath(), "class clsDeclaration2 implements ifaceDeclaration, ifaceDecl^aration2 ", true);
    }

    public void testMarkClsIface_9() throws Exception {
        checkOccurrences(getTestPath(), "$ifaceDec^laration = ", true);
    }

    public void testMarkClsIface_10() throws Exception {
        checkOccurrences(getTestPath(), "$ifaceDeclarati^on2 = ", true);
    }

    public void testMarkClsIface_11() throws Exception {
        checkOccurrences(getTestPath(), "$iface^Declaration4 = ", true);
    }

    public void testMarkClsIface_12() throws Exception {
        checkOccurrences(getTestPath(), "$clsDec^laration  = ", true);
    }

    public void testMarkClsIface_13() throws Exception {
        checkOccurrences(getTestPath(), "$clsDec^laration2 = ", true);
    }

    public void testMarkClsIface_14() throws Exception {
        checkOccurrences(getTestPath(), "$clsDec^laration4 = ", true);
    }

    public void testMarkClsIface_15() throws Exception {
        checkOccurrences(getTestPath(), "$clsDeclar^ation3 = ", true);
    }

    public void testMarkClsIface_16() throws Exception {
        checkOccurrences(getTestPath(), "function ifaceDe^claration()", true);
    }

    public void testMarkClsIface_17() throws Exception {
        checkOccurrences(getTestPath(), "function ifaceDe^claration2() ", true);
    }

    public void testMarkClsIface_18() throws Exception {
        checkOccurrences(getTestPath(), "function ifaceDe^claration4() ", true);
    }

    public void testMarkClsIface_19() throws Exception {
        checkOccurrences(getTestPath(), "function clsDecla^ration() ", true);
    }

    public void testMarkClsIface_20() throws Exception {
        checkOccurrences(getTestPath(), "function clsDecla^ration2() ", true);
    }

    public void testMarkClsIface_21() throws Exception {
        checkOccurrences(getTestPath(), "function clsDecla^ration3() ", true);
    }

    public void testMarkClsIface_22() throws Exception {
        checkOccurrences(getTestPath(), "function clsDecla^ration4() ", true);
    }

    public void testMarkArray() throws Exception {
        checkOccurrences(getTestPath(), "private static $stat^ic_array = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_2() throws Exception {
        checkOccurrences(getTestPath(), "$result .= self::$st^atic_array[$instance_array[$idx]", true);
    }

    public void testMarkArray_3() throws Exception {
        checkOccurrences(getTestPath(), "private $fi^eld_array = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_4() throws Exception {
        checkOccurrences(getTestPath(), "$result .= $this->fiel^d_array[$instance_array[$idx]", true);
    }

    public void testMarkArray_5() throws Exception {
        checkOccurrences(getTestPath(), "$instance_a^rray = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_6() throws Exception {
        checkOccurrences(getTestPath(), "$result .= self::$static_array[$instanc^e_array[$idx]", true);
    }

    public void testMarkArray_7() throws Exception {
        checkOccurrences(getTestPath(), "$i^dx = ", true);
    }

    public void testMarkArray_8() throws Exception {
        checkOccurrences(getTestPath(), "$instance_array[$i^dx", true);
    }

    public void testMarkArray_9() throws Exception {
        checkOccurrences(getTestPath(), "$i^dx2 = ", true);
    }

    public void testMarkArray_10() throws Exception {
        checkOccurrences(getTestPath(), "$instance_array2[$id^x2", true);
    }

    public void testMarkArray_11() throws Exception {
        checkOccurrences(getTestPath(), "$i^dx3 = ", true);
    }

    public void testMarkArray_12() throws Exception {
        checkOccurrences(getTestPath(), "$instance_array3[$id^x3", true);
    }

    public void testMarkArray_13() throws Exception {
        checkOccurrences(getTestPath(), "$instan^ce_array2 = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_14() throws Exception {
        checkOccurrences(getTestPath(), "$instan^ce_array2[$idx2", true);
    }

    public void testMarkArray_15() throws Exception {
        checkOccurrences(getTestPath(), "$instan^ce_array3 = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testVardoc166660() throws Exception {
        checkOccurrences(getTestPath(), "@var $testClass Test^Class", true);
    }
    public void testVardoc166660_1() throws Exception {
        checkOccurrences(getTestPath(), "@var $test^Class TestClass", true);
    }

    public void testMagicMethod171249() throws Exception {
        checkOccurrences(getTestPath(), "class OldC^lass {", true);
    }

    public void testInstanceof198909_01() throws Exception {
        checkOccurrences(getTestPath(), "$mExpectedE^xception = null", true);
    }

    public void testInstanceof198909_02() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Exception $^e) {", true);
    }

    public void testInstanceof198909_03() throws Exception {
        checkOccurrences(getTestPath(), "$e instanceof $mExpect^edException", true);
    }

    public void testIssue198449_01() throws Exception {
        checkOccurrences(getTestPath(), "$cl^ass = 'StdClass';", true);
    }

    public void testIssue201429_01() throws Exception {
        checkOccurrences(getTestPath(), "protected static function test($keyC^losure)", true);
    }

    public void testIssue200399_01() throws Exception {
        checkOccurrences(getTestPath(), "function functionName(\\Character\\Ma^nager", true);
    }

    public void testIssue201671() throws Exception {
        checkOccurrences(getTestPath(), "$array as $my^Key", true);
    }

    public void testIssue133465_01() throws Exception {
        checkOccurrences(getTestPath(), "private $U^RL;", true);
    }

    public void testIssue133465_02() throws Exception {
        checkOccurrences(getTestPath(), "st $this->$U^RL", true);
    }

    public void testIssue133465_03() throws Exception {
        checkOccurrences(getTestPath(), "return $this->$U^RL;", true);
    }

    public void testIssue197283_01() throws Exception {
        checkOccurrences(getTestPath(), "$fu^nc = 'someFunc';", true);
    }

    public void testIssue197283_02() throws Exception {
        checkOccurrences(getTestPath(), "$fu^nc();", true);
    }

    public void testIssue197283_03() throws Exception {
        checkOccurrences(getTestPath(), "$o^bj = 'MyObj';", true);
    }

    public void testIssue197283_04() throws Exception {
        checkOccurrences(getTestPath(), "$x = new $o^bj;", true);
    }

    public void testIssue197283_05() throws Exception {
        checkOccurrences(getTestPath(), "$another^Obj = 'AnotherObj';", true);
    }

    public void testIssue197283_06() throws Exception {
        checkOccurrences(getTestPath(), "$y = new $another^Obj();", true);
    }

    public void testIssue203419_01() throws Exception {
        checkOccurrences(getTestPath(), "class MyClass20^3419", true);
    }

    public void testIssue203419_02() throws Exception {
        checkOccurrences(getTestPath(), "* @var \\test\\sub\\MyClass203^419", true);
    }

    public void testIssue203419_03() throws Exception {
        checkOccurrences(getTestPath(), "public function test2(MyClass^203419 $param) {", true);
    }

    public void testIssue203419_04() throws Exception {
        checkOccurrences(getTestPath(), "$v1 = new \\test\\sub\\MyClass20^3419();", true);
    }

    public void testIssue203419_05() throws Exception {
        checkOccurrences(getTestPath(), "$v2 = new MyClass203^419();", true);
    }

    public void testIssue203419_06() throws Exception {
        checkOccurrences(getTestPath(), "$v3 = new sub\\MyClass20^3419();", true);
    }

    public void testIssue203419_07() throws Exception {
        checkOccurrences(getTestPath(), "$v4 = new baf\\MyClass203^419();", true);
    }

    public void testIssue204433_01() throws Exception {
        checkOccurrences(getTestPath(), "$form = new Edit^Form();", true);
    }

    public void testIssue204433_02() throws Exception {
        checkOccurrences(getTestPath(), "$form = new E^F()", true);
    }

    public void testIssue204433_03() throws Exception {
        checkOccurrences(getTestPath(), "$fr = new Edit^Form();", true);
    }

    public void testArrayDereferencing_01() throws Exception {
        checkOccurrences(getTestPath(), "$myCl^ass->field[0]->getArray()[][]->foo();", true);
    }

    public void testArrayDereferencing_02() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->fie^ld[0]->getArray()[][]->foo();", true);
    }

    public void testArrayDereferencing_03() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->field[0]->getA^rray()[][]->foo();", true);
    }

    public void testArrayDereferencing_04() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->field[0]->getArray()[][]->fo^o();", true);
    }

    public void testArrayDereferencing_05() throws Exception {
        checkOccurrences(getTestPath(), "$myC^lass->getArray()[0][]->foo();", true);
    }

    public void testArrayDereferencing_06() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->getA^rray()[0][]->foo();", true);
    }

    public void testArrayDereferencing_07() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->getArray()[0][]->fo^o();", true);
    }

    public void testArrayDereferencing_08() throws Exception {
        checkOccurrences(getTestPath(), "function^Name()[0]->foo();", true);
    }

    public void testArrayDereferencing_09() throws Exception {
        checkOccurrences(getTestPath(), "functionName()[0]->fo^o();", true);
    }

    public void testVariableAsAClassName() throws Exception {
        checkOccurrences(getTestPath(), "$static_clas^sname::$static_property;", true);
    }

    public void testStaticMethodCall() throws Exception {
        checkOccurrences(getTestPath(), "Presenter::staticFun^ctionName($param);", true);
    }

    public void testIssue209187_01() throws Exception {
        checkOccurrences(getTestPath(), "class Class^Name {", true);
    }

    public void testIssue209187_02() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Class^Name;", true);
    }

    public void testIssue209187_03() throws Exception {
        checkOccurrences(getTestPath(), "new Class^Name();", true);
    }

    public void testIssue208826_01() throws Exception {
        checkOccurrences(getTestPath(), "class Mo^del {}", true);
    }

    public void testIssue208826_02() throws Exception {
        checkOccurrences(getTestPath(), "* @var \\Mo^del", true);
    }

    public void testIssue208826_03() throws Exception {
        checkOccurrences(getTestPath(), "* @return \\Mo^del", true);
    }

    public void testIssue208826_04() throws Exception {
        checkOccurrences(getTestPath(), "class B^ag {}", true);
    }

    public void testIssue208826_05() throws Exception {
        checkOccurrences(getTestPath(), "* @param B\\B^ag $param", true);
    }

    public void testIssue208826_06() throws Exception {
        checkOccurrences(getTestPath(), "function functionName1(B\\B^ag $param) {", true);
    }

    public void testIssue200596_01() throws Exception {
        checkOccurrences(getTestPath(), "class Class^Name {", true);
    }

    public void testIssue200596_02() throws Exception {
        checkOccurrences(getTestPath(), "class Aliased^ClassName {", true);
    }

    public void testIssue200596_03() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar as O^mg;", true);
    }

    public void testIssue200596_04() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Aliased^ClassName as Cls;", true);
    }

    public void testIssue200596_05() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\AliasedClassName as C^ls;", true);
    }

    public void testIssue200596_06() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Class^Name;", true);
    }

    public void testIssue200596_07() throws Exception {
        checkOccurrences(getTestPath(), "(new O^mg\\AliasedClassName())->bar();", true);
    }

    public void testIssue200596_08() throws Exception {
        checkOccurrences(getTestPath(), "(new Omg\\Aliased^ClassName())->bar();", true);
    }

    public void testIssue200596_09() throws Exception {
        checkOccurrences(getTestPath(), "(new C^ls())->bar();", true);
    }

    public void testIssue200596_10() throws Exception {
        checkOccurrences(getTestPath(), "(new Class^Name())->bar();", true);
    }

    public void testIssue200596_11() throws Exception {
        checkOccurrences(getTestPath(), "new O^mg\\AliasedClassName();", true);
    }

    public void testIssue200596_12() throws Exception {
        checkOccurrences(getTestPath(), "new Omg\\Aliased^ClassName();", true);
    }

    public void testIssue200596_13() throws Exception {
        checkOccurrences(getTestPath(), "new C^ls();", true);
    }

    public void testIssue200596_14() throws Exception {
        checkOccurrences(getTestPath(), "new Class^Name();", true);
    }

    public void testIssue200596_15() throws Exception {
        checkOccurrences(getTestPath(), "O^mg\\AliasedClassName::foo();", true);
    }

    public void testIssue200596_16() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\Aliased^ClassName::foo();", true);
    }

    public void testIssue200596_17() throws Exception {
        checkOccurrences(getTestPath(), "C^ls::foo();", true);
    }

    public void testIssue200596_18() throws Exception {
        checkOccurrences(getTestPath(), "Class^Name::bar();", true);
    }

    public void testIssue200596_19() throws Exception {
        checkOccurrences(getTestPath(), "O^mg\\AliasedClassName::FOO;", true);
    }

    public void testIssue200596_20() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\Aliased^ClassName::FOO;", true);
    }

    public void testIssue200596_21() throws Exception {
        checkOccurrences(getTestPath(), "C^ls::FOO;", true);
    }

    public void testIssue200596_22() throws Exception {
        checkOccurrences(getTestPath(), "Class^Name::BAR;", true);
    }

    public void testIssue200596_23() throws Exception {
        checkOccurrences(getTestPath(), "O^mg\\AliasedClassName::$foo;", true);
    }

    public void testIssue200596_24() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\Aliased^ClassName::$foo;", true);
    }

    public void testIssue200596_25() throws Exception {
        checkOccurrences(getTestPath(), "C^ls::$foo;", true);
    }

    public void testIssue200596_26() throws Exception {
        checkOccurrences(getTestPath(), "Class^Name::$bar;", true);
    }

    public void testIssue200596_27() throws Exception {
        checkOccurrences(getTestPath(), "if ($x instanceof O^mg\\AliasedClassName) {}", true);
    }

    public void testIssue200596_28() throws Exception {
        checkOccurrences(getTestPath(), "if ($x instanceof Omg\\Aliased^ClassName) {}", true);
    }

    public void testIssue200596_29() throws Exception {
        checkOccurrences(getTestPath(), "if ($x instanceof C^ls) {}", true);
    }

    public void testIssue200596_30() throws Exception {
        checkOccurrences(getTestPath(), "if ($x instanceof Class^Name) {}", true);
    }

    public void testFieldAccessInInstanceOf_01() throws Exception {
        checkOccurrences(getTestPath(), "if ($a instanceof $this->bb^bbb) {}", true);
    }

    public void testFieldAccessInInstanceOf_02() throws Exception {
        checkOccurrences(getTestPath(), "public $bb^bbb;", true);
    }

    public void testIssue209309_01() throws Exception {
        checkOccurrences(getTestPath(), "class Aliased^ClassName {}", true);
    }

    public void testIssue209309_02() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar as O^mg;", true);
    }

    public void testIssue209309_03() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Aliased^ClassName as Cls;", true);
    }

    public void testIssue209309_04() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\AliasedClassName as C^ls;", true);
    }

    public void testIssue209309_05() throws Exception {
        checkOccurrences(getTestPath(), "function bar(O^mg\\AliasedClassName $p, Cls $a) {}", true);
    }

    public void testIssue209309_06() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\Aliased^ClassName $p, Cls $a) {}", true);
    }

    public void testIssue209309_07() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\AliasedClassName $p, C^ls $a) {}", true);
    }

    public void testIssue209308_01() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar as Om^g;", true);
    }

    public void testIssue209308_02() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\AliasedClassName as Cl^s;", true);
    }

    public void testIssue209308_03() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Cl^s */", true);
    }

    public void testIssue209308_04() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Om^g\\AliasedClassName */", true);
    }

    public void testIssue209308_05() throws Exception {
        checkOccurrences(getTestPath(), "* @return Om^g\\AliasedClassName", true);
    }

    public void testIssue209308_08() throws Exception {
        checkOccurrences(getTestPath(), "* @param Om^g\\AliasedClassName $p", true);
    }

    public void testIssue209308_09() throws Exception {
        checkOccurrences(getTestPath(), "* @param Cl^s $a", true);
    }

    public void testIssue209308_010() throws Exception {
        checkOccurrences(getTestPath(), "* @return Cl^s", true);
    }

    public void testIssue209308_011() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Om^g\\AliasedClassName $p, Cls $a, \\Foo\\Bar\\AliasedClassName $name) {}", true);
    }

    public void testIssue209308_012() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\AliasedClassName $p, Cl^s $a, \\Foo\\Bar\\AliasedClassName $name) {}", true);
    }

    public void testIssue209308_013() throws Exception {
        checkOccurrences(getTestPath(), "class Aliased^ClassName {}", true);
    }

    public void testIssue209308_014() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Aliased^ClassName as Cls;", true);
    }

    public void testIssue209308_015() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Omg\\Aliased^ClassName */", true);
    }

    public void testIssue209308_016() throws Exception {
        checkOccurrences(getTestPath(), "* @return Omg\\Aliased^ClassName", true);
    }

    public void testIssue209308_018() throws Exception {
        checkOccurrences(getTestPath(), "* @param Omg\\Aliased^ClassName $p", true);
    }

    public void testIssue209308_019() throws Exception {
        checkOccurrences(getTestPath(), "* @param \\Foo\\Bar\\Aliased^ClassName $name Description", true);
    }

    public void testIssue209308_020() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\Aliased^ClassName $p, Cls $a, \\Foo\\Bar\\AliasedClassName $name) {}", true);
    }

    public void testIssue209308_021() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\AliasedClassName $p, Cls $a, \\Foo\\Bar\\Aliased^ClassName $name) {}", true);
    }

    public void testStaticAccessWithNs_01() throws Exception {
        checkOccurrences(getTestPath(), "const B^AR = 2;", true);
    }

    public void testStaticAccessWithNs_02() throws Exception {
        checkOccurrences(getTestPath(), "public static $b^ar;", true);
    }

    public void testStaticAccessWithNs_03() throws Exception {
        checkOccurrences(getTestPath(), "static function b^ar() {}", true);
    }

    public void testStaticAccessWithNs_04() throws Exception {
        checkOccurrences(getTestPath(), "const F^OO = 1;", true);
    }

    public void testStaticAccessWithNs_05() throws Exception {
        checkOccurrences(getTestPath(), "public static $f^oo;", true);
    }

    public void testStaticAccessWithNs_06() throws Exception {
        checkOccurrences(getTestPath(), "static function f^oo() {}", true);
    }

    public void testStaticAccessWithNs_07() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\AliasedClassName::f^oo();", true);
    }

    public void testStaticAccessWithNs_08() throws Exception {
        checkOccurrences(getTestPath(), "Cls::f^oo();", true);
    }

    public void testStaticAccessWithNs_09() throws Exception {
        checkOccurrences(getTestPath(), "ClassName::b^ar();", true);
    }

    public void testStaticAccessWithNs_10() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\AliasedClassName::F^OO;", true);
    }

    public void testStaticAccessWithNs_11() throws Exception {
        checkOccurrences(getTestPath(), "Cls::F^OO;", true);
    }

    public void testStaticAccessWithNs_12() throws Exception {
        checkOccurrences(getTestPath(), "ClassName::B^AR;", true);
    }

    public void testStaticAccessWithNs_13() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\AliasedClassName::$f^oo;", true);
    }

    public void testStaticAccessWithNs_14() throws Exception {
        checkOccurrences(getTestPath(), "Cls::$f^oo;", true);
    }

    public void testStaticAccessWithNs_15() throws Exception {
        checkOccurrences(getTestPath(), "ClassName::$b^ar;", true);
    }

    public void testStaticAccessWithNs_16() throws Exception {
        checkOccurrences(getTestPath(), "\\Foo\\Bar\\ClassName::$b^ar;", true);
    }

    public void testStaticAccessWithNs_17() throws Exception {
        checkOccurrences(getTestPath(), "\\Foo\\Bar\\ClassName::b^ar();", true);
    }

    public void testStaticAccessWithNs_18() throws Exception {
        checkOccurrences(getTestPath(), "\\Foo\\Bar\\ClassName::B^AR;", true);
    }

    public void testIssue207971_01() throws Exception {
        checkOccurrences(getTestPath(), "private $fie^ld1;", true);
    }

    public void testIssue207971_02() throws Exception {
        checkOccurrences(getTestPath(), "private $fie^ld3;", true);
    }

    public void testIssue207971_03() throws Exception {
        checkOccurrences(getTestPath(), "private $obj^ect2;", true);
    }

    public void testIssue207971_04() throws Exception {
        checkOccurrences(getTestPath(), "$sql = \" {$this->fie^ld1} {$this->object2->xxx} {$this->field3['array1']} \";", true);
    }

    public void testIssue207971_05() throws Exception {
        checkOccurrences(getTestPath(), "$sql = \" {$this->field1} {$this->obj^ect2->xxx} {$this->field3['array1']} \";", true);
    }

    public void testIssue207971_06() throws Exception {
        checkOccurrences(getTestPath(), "$sql = \" {$this->field1} {$this->object2->xxx} {$this->fie^ld3['array1']} \";", true);
    }

    public void testQualifiedUseStatement_01() throws Exception {
        checkOccurrences(getTestPath(), "class Kit^chen {", true);
    }

    public void testQualifiedUseStatement_02() throws Exception {
        checkOccurrences(getTestPath(), "use pl\\dagguh\\someproject\\rooms\\Kit^chen;", true);
    }

    public void testQualifiedUseStatement_03() throws Exception {
        checkOccurrences(getTestPath(), "Kit^chen::DEFAULT_SIZE;", true);
    }

    public void testQualifiedUseStatement_04() throws Exception {
        checkOccurrences(getTestPath(), "use pl\\dagguh\\someproject\\rooms\\Kit^chen as Alias;", true);
    }

    public void testIssue208245_01() throws Exception {
        checkOccurrences(getTestPath(), "$glob^Var = \"\";", true);
    }

    public void testIssue208245_02() throws Exception {
        checkOccurrences(getTestPath(), "function() use($glob^Var) {", true);
    }

    public void testIssue208245_03() throws Exception {
        checkOccurrences(getTestPath(), "echo $glob^Var;", true);
    }

    public void testIssue208245_04() throws Exception {
        checkOccurrences(getTestPath(), "$v^ar = \"\";", true);
    }

    public void testIssue208245_05() throws Exception {
        checkOccurrences(getTestPath(), "function() use($v^ar) {", true);
    }

    public void testIssue208245_06() throws Exception {
        checkOccurrences(getTestPath(), "echo $v^ar;", true);
    }

    public void testIssue203073_01() throws Exception {
        checkOccurrences(getTestPath(), "class First^Parent {", true);
    }

    public void testIssue203073_02() throws Exception {
        checkOccurrences(getTestPath(), "use Full\\Name\\Space\\First^Parent as SecondParent;", true);
    }

    public void testIssue203073_03() throws Exception {
        checkOccurrences(getTestPath(), "use Full\\Name\\Space\\First^Parent;", true);
    }

    public void testIssue203073_04() throws Exception {
        checkOccurrences(getTestPath(), "class Yours1 extends First^Parent {", true);
    }

    public void testIssue203073_05() throws Exception {
        checkOccurrences(getTestPath(), "use Full\\Name\\Space\\FirstParent as Second^Parent;", true);
    }

    public void testIssue203073_06() throws Exception {
        checkOccurrences(getTestPath(), "class Yours extends Second^Parent {", true);
    }

    public void testIssue203814_01() throws Exception {
        checkOccurrences(getTestPath(), "public function fMe^thod()", true);
    }

    public void testIssue203814_02() throws Exception {
        checkOccurrences(getTestPath(), "self::$first->fMe^thod();", true);
    }

    public void testIssue203814_03() throws Exception {
        checkOccurrences(getTestPath(), "static::$first->fMe^thod();", true);
    }

    public void testIssue203814_04() throws Exception {
        checkOccurrences(getTestPath(), "Second::$first->fMe^thod();", true);
    }

    public void testIssue207346_01() throws Exception {
        checkOccurrences(getTestPath(), "public $invalid^LinkMode;", true);
    }

    public void testIssue207346_02() throws Exception {
        checkOccurrences(getTestPath(), "$this->invalid^LinkMode = 10;", true);
    }

    public void testIssue207346_03() throws Exception {
        checkOccurrences(getTestPath(), "$this->invalid^LinkMode;", true);
    }

    public void testIssue207615_01() throws Exception {
        checkOccurrences(getTestPath(), "protected static $_v^ar = true;", true);
    }

    public void testIssue207615_02() throws Exception {
        checkOccurrences(getTestPath(), "self::$_v^ar;", true);
    }

    public void testIssue207615_03() throws Exception {
        checkOccurrences(getTestPath(), "return static::$_v^ar;", true);
    }

    public void testConstants_01() throws Exception {
        checkOccurrences(getTestPath(), "const C^ON = 1;", true);
    }

    public void testConstants_02() throws Exception {
        checkOccurrences(getTestPath(), "parent::C^ON;", true);
    }

    public void testConstants_03() throws Exception {
        checkOccurrences(getTestPath(), "self::C^ON;", true);
    }

    public void testConstants_04() throws Exception {
        checkOccurrences(getTestPath(), "static::C^ON;", true);
    }

    public void testStaticAccessWithNsAlias_01() throws Exception {
        checkOccurrences(getTestPath(), "const O^MG = 1;", true);
    }

    public void testStaticAccessWithNsAlias_02() throws Exception {
        checkOccurrences(getTestPath(), "parent::O^MG;", true);
    }

    public void testStaticAccessWithNsAlias_03() throws Exception {
        checkOccurrences(getTestPath(), "self::O^MG;", true);
    }

    public void testStaticAccessWithNsAlias_04() throws Exception {
        checkOccurrences(getTestPath(), "static::O^MG;", true);
    }

    public void testStaticAccessWithNsAlias_05() throws Exception {
        checkOccurrences(getTestPath(), "public static $static^Field = 2;", true);
    }

    public void testStaticAccessWithNsAlias_06() throws Exception {
        checkOccurrences(getTestPath(), "parent::$static^Field;", true);
    }

    public void testStaticAccessWithNsAlias_07() throws Exception {
        checkOccurrences(getTestPath(), "self::$static^Field;", true);
    }

    public void testStaticAccessWithNsAlias_08() throws Exception {
        checkOccurrences(getTestPath(), "static::$static^Field;", true);
    }

    public void testStaticAccessWithNsAlias_09() throws Exception {
        checkOccurrences(getTestPath(), "static function some^Func() {", true);
    }

    public void testStaticAccessWithNsAlias_10() throws Exception {
        checkOccurrences(getTestPath(), "parent::some^Func();", true);
    }

    public void testIssue211230_01() throws Exception {
        checkOccurrences(getTestPath(), "class F^oo {", true);
    }

    public void testIssue211230_02() throws Exception {
        checkOccurrences(getTestPath(), " * @method F^oo|Bar method() This is my cool magic method description.", true);
    }

    public void testIssue211230_03() throws Exception {
        checkOccurrences(getTestPath(), " * @method Foo|B^ar method() This is my cool magic method description.", true);
    }

    public void testIssue211230_04() throws Exception {
        checkOccurrences(getTestPath(), "class B^ar {", true);
    }

    public void testIssue211230_05() throws Exception {
        checkOccurrences(getTestPath(), "$b = new B^ar();", true);
    }

    public void testMagicMethod_01() throws Exception {
        checkOccurrences(getTestPath(), " * @method Foo|Bar met^hod() This is my cool magic method description.", true);
    }

    public void testMagicMethod_02() throws Exception {
        checkOccurrences(getTestPath(), "$b->met^hod()->fooMethod();", true);
    }

    public void testIssue211015_01() throws Exception {
        checkOccurrences(getTestPath(), "$f^oo = \"omg\";", true);
    }

    public void testIssue211015_02() throws Exception {
        checkOccurrences(getTestPath(), "$this->$f^oo();", true);
    }

    public void testIssue211015_03() throws Exception {
        checkOccurrences(getTestPath(), "self::$f^oo();", true);
    }

    public void testIssue211015_04() throws Exception {
        checkOccurrences(getTestPath(), "static::$f^oo();", true);
    }

    public void testIssue211015_05() throws Exception {
        checkOccurrences(getTestPath(), "parent::$f^oo();", true);
    }

    public void testIssue186553_01() throws Exception {
        checkOccurrences(getTestPath(), "public function do^Something()", true);
    }

    public void testIssue186553_02() throws Exception {
        checkOccurrences(getTestPath(), "$object1->do^Something();", true);
    }

    public void testIssue186553_03() throws Exception {
        checkOccurrences(getTestPath(), "$this->do^Something();", true);
    }

    public void testIssue213133_01() throws Exception {
        checkOccurrences(getTestPath(), "class Te^st {", true);
    }

    public void testIssue213133_02() throws Exception {
        checkOccurrences(getTestPath(), "echo $test->{Te^st::$CHECK};", true);
    }

    public void testIssue213133_03() throws Exception {
        checkOccurrences(getTestPath(), "echo Te^st::$CHECK;", true);
    }

    public void testIssue213133_04() throws Exception {
        checkOccurrences(getTestPath(), "public static $CH^ECK = \"check\";", true);
    }

    public void testIssue213133_05() throws Exception {
        checkOccurrences(getTestPath(), "echo $test->{Test::$CH^ECK};", true);
    }

    public void testIssue213133_06() throws Exception {
        checkOccurrences(getTestPath(), "echo Test::$CH^ECK;", true);
    }

    public void testIssue213584_01() throws Exception {
        checkOccurrences(getTestPath(), "trait A^A {", true);
    }

    public void testIssue213584_02() throws Exception {
        checkOccurrences(getTestPath(), "trait B^B {", true);
    }

    public void testIssue213584_03() throws Exception {
        checkOccurrences(getTestPath(), "trait C^C {", true);
    }

    public void testIssue213584_04() throws Exception {
        checkOccurrences(getTestPath(), "trait D^D {", true);
    }

    public void testIssue213584_05() throws Exception {
        checkOccurrences(getTestPath(), "use A^A, BB, CC, DD {", true);
    }

    public void testIssue213584_06() throws Exception {
        checkOccurrences(getTestPath(), "use AA, B^B, CC, DD {", true);
    }

    public void testIssue213584_07() throws Exception {
        checkOccurrences(getTestPath(), "use AA, BB, C^C, DD {", true);
    }

    public void testIssue213584_08() throws Exception {
        checkOccurrences(getTestPath(), "use AA, BB, CC, D^D {", true);
    }

    public void testIssue213584_09() throws Exception {
        checkOccurrences(getTestPath(), "C^C::bar insteadof AA, BB;", true);
    }

    public void testIssue213584_10() throws Exception {
        checkOccurrences(getTestPath(), "CC::bar insteadof A^A, BB;", true);
    }

    public void testIssue213584_11() throws Exception {
        checkOccurrences(getTestPath(), "CC::bar insteadof AA, B^B;", true);
    }

    public void testIssue213584_12() throws Exception {
        checkOccurrences(getTestPath(), "D^D::bar as foo;", true);
    }

    public void testIssue217357_01() throws Exception {
        checkOccurrences(getTestPath(), "class Str^ing {", true);
    }

    public void testIssue217357_02() throws Exception {
        checkOccurrences(getTestPath(), "use Abc\\Str^ing;", true);
    }

    public void testIssue217357_03() throws Exception {
        checkOccurrences(getTestPath(), "$s = new Str^ing();", true);
    }

    public void testCatchWithAlias_01() throws Exception {
        checkOccurrences(getTestPath(), "use Blah\\Sec as B^S;", true);
    }

    public void testCatchWithAlias_02() throws Exception {
        checkOccurrences(getTestPath(), "new B^S\\MyException();", true);
    }

    public void testCatchWithAlias_03() throws Exception {
        checkOccurrences(getTestPath(), "} catch (B^S\\MyException $ex) {", true);
    }

    public void testIssue216876_01() throws Exception {
        checkOccurrences(getTestPath(), "class MyNewCl^ass123 {", true);
    }

    public void testIssue216876_02() throws Exception {
        checkOccurrences(getTestPath(), "public function MyNewCl^ass123($foo) {", true);
    }

    public void testIssue216876_03() throws Exception {
        checkOccurrences(getTestPath(), "$c = new \\Foo\\MyNewCl^ass123();", true);
    }

    public void testIssue218487_01() throws Exception {
        checkOccurrences(getTestPath(), "use Zend\\Stdlib2\\DispatchableInterface2 as Dispatch^able2;", true);
    }

    public void testIssue218487_02() throws Exception {
        checkOccurrences(getTestPath(), "class AbstractController implements Dispatch^able2 {", true);
    }

    public void testIssue223076_01() throws Exception {
        checkOccurrences(getTestPath(), "func^tion functionName($param) {", true);
    }

    public void testIssue223076_02() throws Exception {
        checkOccurrences(getTestPath(), "retur^n 5;", true);
    }

    public void testIssue223076_03() throws Exception {
        checkOccurrences(getTestPath(), "retur^n 10;", true);
    }

    public void testReflectionVariableInMethodInvocation_01() throws Exception {
        checkOccurrences(getTestPath(), "private $cont^ext;", true);
    }

    public void testReflectionVariableInMethodInvocation_02() throws Exception {
        checkOccurrences(getTestPath(), "$this->cont^ext[0]", true);
    }

    public void testIssue217360_01() throws Exception {
        checkOccurrences(getTestPath(), "private function get^Two()", true);
    }

    public void testIssue217360_02() throws Exception {
        checkOccurrences(getTestPath(), "$two = $this->get^Two();", true);
    }

    public void testIssue217360_03() throws Exception {
        checkOccurrences(getTestPath(), "return $two->get^Two();", true);
    }

    public void testIssue217360_04() throws Exception {
        checkOccurrences(getTestPath(), "(new Two)->get^Two();", true);
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        return new FileObject[]{FileUtil.toFileObject(new File(getDataDir(), getTestFolderPath()))};
    }

    private String getTestFolderPath() {
        return "testfiles/markoccurences/" + getTestName();//NOI18N
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestName() + ".php";//NOI18N
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
