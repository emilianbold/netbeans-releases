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
public class OccurrencesFinderImplTest extends TestBase {

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
//testfiles/markoccurences/testGotoConstructTest/testGotoConstructTest.php
        checkOccurrences(getTestPath(), "$a = new MyCla^ssConstr(", true);
    }

    public void testGotoConstructTest_2() throws Exception {
//testfiles/markoccurences/testGotoConstructTest/testGotoConstructTest.php
        checkOccurrences(getTestPath(), "$b = new MyClass^Constr2(", true);
    }

    public void testParamVarPropInPhpDocTest() throws Exception {
//testfiles/markoccurences/testParamVarPropInPhpDocTest/testParamVarPropInPhpDocTest.php
        checkOccurrences(getTestPath(), "* @param Book $he^llo", true);
    }

    public void testMarkReturnsOnConstructorTest() throws Exception {
//testfiles/markoccurences/testMarkReturnsOnConstructorTest/testMarkReturnsOnConstructorTest.php
        checkOccurrences(getTestPath(), "funct^ion __construct() {}//Auth", true);
    }

    public void testMarkReturnsOnConstructorTest_2() throws Exception {
//testfiles/markoccurences/testMarkReturnsOnConstructorTest/testMarkReturnsOnConstructorTest.php
        checkOccurrences(getTestPath(), "funct^ion __construct() {}//Bo", true);
    }

    public void testClsVarPropInPhpDocTest() throws Exception {
//testfiles/markoccurences/testClsVarPropInPhpDocTest/testClsVarPropInPhpDocTest.php
        checkOccurrences(getTestPath(), "* @return Aut^hor", true);
    }

    public void testIfaceTest() throws Exception {
//testfiles/markoccurences/testIfaceTest/testIfaceTest.php
        checkOccurrences(getTestPath(), "class mycls implements my^face", true);
    }

    public void testIfaceTest_2() throws Exception {
//testfiles/markoccurences/testIfaceTest/testIfaceTest.php
        checkOccurrences(getTestPath(), "const REC^OVER_ORIG = ", true);
    }

    public void testIfaceTest_3() throws Exception {
//testfiles/markoccurences/testIfaceTest/testIfaceTest.php
        checkOccurrences(getTestPath(), "class my^cls implements myface", true);
    }

    public void testIfaceTest_4() throws Exception {
//testfiles/markoccurences/testIfaceTest/testIfaceTest.php
        checkOccurrences(getTestPath(), "const RECOV^ER_ORIG = ", true);
    }

    public void testMarkClsIface() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "class clsDecla^ration implements ifaceDeclaration ", true);
    }

    public void testMarkClsIface_2() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "class clsDeclaration3 extends clsDec^laration ", true);
    }

    public void testMarkClsIface_3() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "interface ifaceDec^laration ", true);
    }

    public void testMarkClsIface_4() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "interface ifaceDeclaration2 extends ifaceDecl^aration  ", true);
    }

    public void testMarkClsIface_5() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "class clsDeclaration implements ifaceDeclara^tion ", true);
    }

    public void testMarkClsIface_6() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "class clsDeclaration2 implements ifaceDecla^ration, ifaceDeclaration2 ", true);
    }

    public void testMarkClsIface_7() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "interface ifaceDecl^aration2 extends ifaceDeclaration  ", true);
    }

    public void testMarkClsIface_8() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "class clsDeclaration2 implements ifaceDeclaration, ifaceDecl^aration2 ", true);
    }

    public void testMarkClsIface_9() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "$ifaceDec^laration = ", true);
    }

    public void testMarkClsIface_10() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "$ifaceDeclarati^on2 = ", true);
    }

    public void testMarkClsIface_11() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "$iface^Declaration4 = ", true);
    }

    public void testMarkClsIface_12() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "$clsDec^laration  = ", true);
    }

    public void testMarkClsIface_13() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "$clsDec^laration2 = ", true);
    }

    public void testMarkClsIface_14() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "$clsDec^laration4 = ", true);
    }

    public void testMarkClsIface_15() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "$clsDeclar^ation3 = ", true);
    }

    public void testMarkClsIface_16() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "function ifaceDe^claration()", true);
    }

    public void testMarkClsIface_17() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "function ifaceDe^claration2() ", true);
    }

    public void testMarkClsIface_18() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "function ifaceDe^claration4() ", true);
    }

    public void testMarkClsIface_19() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "function clsDecla^ration() ", true);
    }

    public void testMarkClsIface_20() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "function clsDecla^ration2() ", true);
    }

    public void testMarkClsIface_21() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "function clsDecla^ration3() ", true);
    }

    public void testMarkClsIface_22() throws Exception {
//testfiles/markoccurences/testMarkClsIface/testMarkClsIface.php
        checkOccurrences(getTestPath(), "function clsDecla^ration4() ", true);
    }

    public void testMarkArray() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "private static $stat^ic_array = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_2() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$result .= self::$st^atic_array[$instance_array[$idx]", true);
    }

    public void testMarkArray_3() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "private $fi^eld_array = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_4() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$result .= $this->fiel^d_array[$instance_array[$idx]", true);
    }

    public void testMarkArray_5() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$instance_a^rray = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_6() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$result .= self::$static_array[$instanc^e_array[$idx]", true);
    }

    public void testMarkArray_7() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$i^dx = ", true);
    }

    public void testMarkArray_8() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$instance_array[$i^dx", true);
    }

    public void testMarkArray_9() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$i^dx2 = ", true);
    }

    public void testMarkArray_10() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$instance_array2[$id^x2", true);
    }

    public void testMarkArray_11() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$i^dx3 = ", true);
    }

    public void testMarkArray_12() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$instance_array3[$id^x3", true);
    }

    public void testMarkArray_13() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$instan^ce_array2 = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_14() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$instan^ce_array2[$idx2", true);
    }

    public void testMarkArray_15() throws Exception {
//testfiles/markoccurences/testMarkArray/testMarkArray.php
        checkOccurrences(getTestPath(), "$instan^ce_array3 = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testVardoc166660() throws Exception {
        //testfiles/markoccurences/testVardoc166660/testVardoc166660.php
        checkOccurrences(getTestPath(), "@var $testClass Test^Class", true);
    }
    public void testVardoc166660_1() throws Exception {
        //testfiles/markoccurences/testVardoc166660/testVardoc166660.php
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
        checkOccurrences(getTestPath(), "function functionName(Character\\Ma^nager", true);
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

    //TODO; these 2 tests are temporary disabled not to fail, needs to be evaluated
    // and maybe fixed (NOT URGENT)
    //caused by got to declaration, mark occurences rewrite
    /*public void testOccurrences5() throws Exception {
        performTestOccurrences("<?php\n" +
                               "$^name^ = \"test\";\n" +
                               "function foo() {\n" +
                               "    echo $GLOBALS['^na|me^'];\n" +
                               "}\n" +
                               "?>");
    }

    public void test132230() throws Exception {
        performTestOccurrences("<?php\n" +
                               "function a() {\n" +
                               "    global $^f^;\n" +
                               "    $^|f^['s']();\n" +
                               "}\n" +
                               "?>",
                               true);
    }*/

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
