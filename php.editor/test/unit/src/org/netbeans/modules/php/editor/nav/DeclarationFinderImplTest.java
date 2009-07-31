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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda, Radek Matous
 */
public class DeclarationFinderImplTest extends TestBase {

    public DeclarationFinderImplTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();        
    }

    public void testParamVarPropInPhpDocTest() throws Exception {
        String markTest = prepareTestFile(
                "testfiles/markphpdocTest.php",
                "function test($hello) {//function",
                "function test($^hello) {//function",
                "* @param Book $hello",
                "* @param Book $he|llo"
                );
        performTestSimpleFindDeclaration(-1, markTest);
    }

    public void testParamVarPropInPhpDocTest2() throws Exception {
        String markTest = prepareTestFile(
                "testfiles/markphpdocTest.php",
                "function test($hello) {//method",
                "function test($^hello) {//method",
                "$tmp = $hello;",
                "$tmp = $hel|lo;"
                );
        performTestSimpleFindDeclaration(-1, markTest);
    }

    public void testClsVarPropInPhpDocTest() throws Exception {
        String markTest = prepareTestFile(
                "testfiles/markphpdocTest.php",
                "class Author {",
                "class ^Author^ {",
                " * @property Author $author hello this is doc",
                " * @property Au|thor $author hello this is doc"
                );
        performTestSimpleFindDeclaration(-1, markTest);
    }

    public void testClsVarPropInPhpDocTest2() throws Exception {
        String markTest = prepareTestFile(
                "testfiles/markphpdocTest.php",
                " * @property Author $author hello this is doc",
                " * @property Author $^author hello this is doc",
                "$this->author;",
                "$this->auth|or;"
                );
        performTestSimpleFindDeclaration(-1, markTest);
    }

    public void testGotoConstructTest() throws Exception {
        String ifaceTest = prepareTestFile(
                "testfiles/gotoConstrTest.php",
                "public function __construct() {//MyClassConstr",
                "public function ^__construct() {//MyClassConstr",
                "$a = new MyClassConstr();",
                "$a = new MyCla|ssConstr();"
                );
        performTestSimpleFindDeclaration(-1, ifaceTest);
    }

    public void testGotoConstructTest2() throws Exception {
        String ifaceTest = prepareTestFile(
                "testfiles/gotoConstrTest.php",
                "class MyClassConstr2 extends MyClassConstr  {}//MyClassConstr2",
                "class ^MyClassConstr2 extends MyClassConstr  {}//MyClassConstr2",
                "$b = new MyClassConstr2();",
                "$b = new MyCla|ssConstr2();"
                );
        performTestSimpleFindDeclaration(-1, ifaceTest);
    }

    public void testIfaceTest() throws Exception {
        String ifaceTest = prepareTestFile(
                "testfiles/ifaceTest.php",
                "interface myface {",
                "interface ^myface {",
                "myface::RECOVER_ORIG;",
                "myf|ace::RECOVER_ORIG;"
                );
        performTestSimpleFindDeclaration(-1, ifaceTest);
    }

    public void testIfaceTest2() throws Exception {
        String ifaceTest = prepareTestFile(
                "testfiles/ifaceTest.php",
                "const RECOVER_ORIG = 2;",
                "const ^RECOVER_ORIG = 2;",
                "myface::RECOVER_ORIG;",
                "myface::REC|OVER_ORIG;"
                );
        performTestSimpleFindDeclaration(-1, ifaceTest);
    }

    public void testIfaceTest3() throws Exception {
        String ifaceTest = prepareTestFile(
                "testfiles/ifaceTest.php",
                "class mycls implements myface {",
                "class ^mycls implements myface {",
                "mycls::RECOVER_ORIG;",
                "myc|ls::RECOVER_ORIG;"
                );
        performTestSimpleFindDeclaration(-1, ifaceTest);
    }

    public void testIfaceTest4() throws Exception {
        String ifaceTest = prepareTestFile(
                "testfiles/ifaceTest.php",
                "const RECOVER_ORIG = 1;",
                "const ^RECOVER_ORIG = 1;",
                "mycls::RECOVER_ORIG;",
                "mycls::REC|OVER_ORIG;"
                );
        performTestSimpleFindDeclaration(-1, ifaceTest);
    }

    public void testIfaceTest5() throws Exception {
        String ifaceTest = prepareTestFile(
                "testfiles/ifaceTest.php",
                "function mfnc() {}//mycls",
                "function ^mfnc^() {}//mycls",
                "$a->mfnc();//mycls",
                "$a->mf|nc();//mycls"
                );
        performTestSimpleFindDeclaration(-1, ifaceTest);
    }

    public void testIfaceTest6() throws Exception {
        String ifaceTest = prepareTestFile(
                "testfiles/ifaceTest.php",
                "function mfnc();//myface",
                "function ^mfnc^();//myface",
                "$a->mfnc();//myface",
                "$a->mfn|c();//myface"
                );
        performTestSimpleFindDeclaration(-1, ifaceTest);
    }

     public void testImplementsInterface() throws Exception {
        String gotoTest2 = prepareTestFile(
                "testfiles/classMan.php",
                "implements Person {",
                "implements P|erson {"
                );
        String gotoTest = prepareTestFile(
                "testfiles/classPerson.php",
                "interface Person {",
                "interface ^Person {"
                );
        performTestSimpleFindDeclaration(-1, gotoTest2, gotoTest);

    }

    public void testGotoTypeClsIface() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration {}",
                "interface ^ifaceDeclaration {}",
                "interface ifaceDeclaration2 extends ifaceDeclaration  {}",
                "interface ifaceDeclaration2 extends ifaceDec|laration  {}"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIface2() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration {}",
                "interface ^ifaceDeclaration {}",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class clsDeclaration implements ifaceDecl|aration {}"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIface3() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration {}",
                "interface ^ifaceDeclaration {}",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "class clsDeclaration2 implements ifaceDec|laration, ifaceDeclaration2 {}"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIface4() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class ^clsDeclaration implements ifaceDeclaration {}",
                "class clsDeclaration3 extends clsDeclaration {}",
                "class clsDeclaration3 extends clsDeclarat|ion {}"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIface5() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration2 extends ifaceDeclaration  {}",
                "interface ^ifaceDeclaration2 extends ifaceDeclaration  {}",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDecla|ration2 {}"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIface6() throws Exception {
        String gotoTest = prepareTestFile(
                "testfiles/gotoType2.php",
                "interface ifaceDeclaration4 {}",
                "interface ^ifaceDeclaration4 {}"
                );
        String gotoTest2 = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}",
                "class clsDeclaration4 extends clsDeclaration3 implements ifaceDecla|ration4 {}"
                );
        performTestSimpleFindDeclaration(-1, gotoTest2, gotoTest);
    }

    public void testGotoTypeClsIfaceFromalParam() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration {}",
                "interface ^ifaceDeclaration {}",
                "ifaceDeclaration $ifaceDeclarationVar,",
                "ifaceD|eclaration $ifaceDeclarationVar,"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIfaceFromalParam2() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration2 extends ifaceDeclaration  {}",
                "interface ^ifaceDeclaration2 extends ifaceDeclaration  {}",
                "ifaceDeclaration2 $ifaceDeclaration2Var,",
                "ifaceD|eclaration2 $ifaceDeclaration2Var,"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIfaceFromalParam3() throws Exception {
        String gotoTest = prepareTestFile(
                "testfiles/gotoType2.php",
                "interface ifaceDeclaration4 {}",
                "interface ^ifaceDeclaration4 {}"
                );
        String gotoTest2 = prepareTestFile(
                "testfiles/gotoType.php",
                "ifaceDeclaration4 $ifaceDeclaration4Var,",
                "ifaceD|eclaration4 $ifaceDeclaration4Var,"
                );
        performTestSimpleFindDeclaration(-1, gotoTest2, gotoTest);
    }
    public void testGotoTypeClsIfaceFromalParam4() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class ^clsDeclaration implements ifaceDeclaration {}",
                "clsDeclaration  $clsDeclarationVar,",
                "clsD|eclaration  $clsDeclarationVar,"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIfaceFromalParam5() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "class ^clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "clsDeclaration2 $clsDeclaration2Var,",
                "clsDeclara|tion2 $clsDeclaration2Var,,"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIfaceFromalParam6() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration3 extends clsDeclaration {}",
                "class ^clsDeclaration3 extends clsDeclaration {}",
                "clsDeclaration3 $clsDeclaration3Var,",
                "clsDe|claration3 $clsDeclaration3Var,"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIfaceFromalParam7() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}",
                "class ^clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}",
                "clsDeclaration4 $clsDeclaration4Var",
                "clsDeclar|ation4 $clsDeclaration4Var"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIfaceCatch() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class ^clsDeclaration implements ifaceDeclaration {}",
                "} catch (clsDeclaration $cex) {",
                "} catch (clsDecla|ration $cex) {"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIfaceInstanceof() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class ^clsDeclaration implements ifaceDeclaration {}",
                "if ($cex instanceof clsDeclaration) {",
                "if ($cex instanceof clsDecl|aration) {"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeClsIfaceInstanceof2() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "} catch (clsDeclaration $cex) {",
                "} catch (clsDeclaration $^cex) {",
                "if ($cex instanceof clsDeclaration) {",
                "if ($c|ex instanceof clsDeclaration) {"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "private static $static_array = array('', 'thousand ', 'million ', 'billion ');",
                "private static $^static_array = array('', 'thousand ', 'million ', 'billion ');",
                "$result .= self::$static_array[$idx++];",
                "$result .= self::$static_a|rray[$idx++];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays2() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "private static $static_array = array('', 'thousand ', 'million ', 'billion ');",
                "private static $^static_array = array('', 'thousand ', 'million ', 'billion ');",
                "$result .= self::$static_array[$instance_array[$idx]];",
                "$result .= self::$static|_array[$instance_array[$idx]];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays3() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "private $field_array = array('', 'thousand ', 'million ', 'billion ');",
                "private $^field_array = array('', 'thousand ', 'million ', 'billion ');",
                "$result .= $this->field_array[$idx++];",
                "$result .= $this->field_a|rray[$idx++];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays4() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "private $field_array = array('', 'thousand ', 'million ', 'billion ');",
                "private $^field_array = array('', 'thousand ', 'million ', 'billion ');",
                "$result .= $this->field_array[$instance_array[$idx]];",
                "$result .= $this->field_|array[$instance_array[$idx]];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays5() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$instance_array = array('', 'thousand ', 'million ', 'billion ');",
                "$^instance_array = array('', 'thousand ', 'million ', 'billion ');",
                "$instance_array[$idx];",
                "$instan|ce_array[$idx];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays6() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$instance_array = array('', 'thousand ', 'million ', 'billion ');",
                "$^instance_array = array('', 'thousand ', 'million ', 'billion ');",
                "$result .= self::$static_array[$instance_array[$idx]];",
                "$result .= self::$static_array[$instance_|array[$idx]];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays7() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$instance_array = array('', 'thousand ', 'million ', 'billion ');",
                "$^instance_array = array('', 'thousand ', 'million ', 'billion ');",
                "$result .= $this->field_array[$instance_array[$idx]];",
                "$result .= $this->field_array[$instan|ce_array[$idx]];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays8() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$idx = 1;",
                "$^idx = 1;",
                "$result .= self::$static_array[$idx++];",
                "$result .= self::$static_array[$id|x++];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays9() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$idx = 1;",
                "$^idx = 1;",
                "$result .= $this->field_array[$idx++];",
                "$result .= $this->field_array[$i|dx++];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays10() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$idx = 1;",
                "$^idx = 1;",
                "$result .= self::$static_array[$instance_array[$idx]];",
                "$result .= self::$static_array[$instance_array[$id|x]];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays11() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$idx = 1;",
                "$^idx = 1;",
                "$result .= $this->field_array[$instance_array[$idx]];",
                "$result .= $this->field_array[$instance_array[$id|x]];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays12() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$idx2 = 1;",
                "$^idx2 = 1;",
                "$instance_array2[$idx2];",
                "$instance_array2[$idx|2];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays13() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$instance_array2 = array('', 'thousand ', 'million ', 'billion ');",
                "$^instance_array2 = array('', 'thousand ', 'million ', 'billion ');",
                "$instance_array2[$idx2];",
                "$instance_a|rray2[$idx2];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays14() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$idx3 = 1;",
                "$^idx3 = 1;",
                "$instance_array3[$idx3];",
                "$instance_array3[$id|x3];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }
    public void testGotoTypeArrays15() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoarray.php",
                "$instance_array3 = array('', 'thousand ', 'million ', 'billion ');",
                "$^instance_array3 = array('', 'thousand ', 'million ', 'billion ');",
                "$instance_array3[$idx3];",
                "$instance_ar|ray3[$idx3];"
                );
        performTestSimpleFindDeclaration(-1, gotoTypeTest);
    }

    /*TODO: in these animalTests is actually bug but not important I guess. Sometimes jumps:
     * 1/public static ^$count = 0, $animal;
     * 2/^public static $count = 0, $animal;
     */
    public void testFuncParamAsReference() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/basicTest.php",
                "function funcWithRefParam(&$param) {",
                "function funcWithRefParam(&$^param) {",
                "$param++;",
                "$par|am++;;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccess() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "Animal::$count;",
                "Animal::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessArrayIndex() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "static $animalSpecies = array();",
                "static $^animalSpecies = array();",
                "$species = self::$animalSpecies;",
                "$species = self::$animalSpec|ies;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessArrayIndex2() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "static $animalSpecies = array();",
                "static $^animalSpecies = array();",
                "$first = self::$animalSpecies[0];",
                "$first = self::$animalSpec|ies[0];"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "Animal::$count;",
                "&Animal::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccess2() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $cat;",
                "public static $^count = 0, $cat;",
                "Cat::$count;",
                "Cat::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccess2Ref() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $cat;",
                "public static $^count = 0, $cat;",
                "Cat::$count;",
                "&Cat::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessOutsideClass() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "Animal::$count--;",
                "Animal::$co|unt--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessOutsideClassRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "Animal::$count--;",
                "&Animal::$co|unt--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessOutsideClass2() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $cat;",
                "public static $^count = 0, $cat;",
                "Cat::$count--;",
                "Cat::$co|unt--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessOutsideClass2Ref() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $cat;",
                "public static $^count = 0, $cat;",
                "Cat::$count--;",
                "&Cat::$co|unt--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessOutsideClassDeclaredInSuperClass() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "Mammal::$count--;",
                "Mammal::$co|unt--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessOutsideClassDeclaredInSuperClassRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "Mammal::$count--;",
                "&Mammal::$co|unt--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessSelf() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $count = 0, $^animal;",
                "self::$animal = $this;",
                "self::$ani|mal = $this;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessSelfRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $count = 0, $^animal;",
                "self::$animal = $this;",
                "&self::$ani|mal = $this;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessSelfDeclaredInSuperClass() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "echo self::$count;",
                "echo self::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessSelfDeclaredInSuperClass2() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "echo self::$count;",
                "echo &self::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessParentDeclaredInSuperClass() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "echo parent::$count;",
                "echo parent::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessParentDeclaredInSuperClassRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "echo parent::$count;",
                "echo &parent::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    
    public void testStaticFieldAccessParent() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "echo parent::$count;",
                "echo parent::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessParentRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "echo parent::$count;",
                "echo &parent::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessInOtherFile() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "return Animal::$count;",
                "return Animal::$cou|nt;;"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    public void testStaticFieldAccessInOtherFileRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "return Animal::$count;",
                "return &Animal::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    public void testStaticFieldAccessParentInOtherFile() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;"
                /*maybe a bug but not important I guess. Sometimes jumps:
                 * 1/public static ^$count = 0, $animal;
                 * 2/^public static $count = 0, $animal;
                 */
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "parent::$count;",
                "parent::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    public void testStaticFieldAccessParentInOtherFileRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;"
                /*maybe a bug but not important I guess. Sometimes jumps:
                 * 1/public static ^$count = 0, $animal;
                 * 2/^public static $count = 0, $animal;
                 */
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "parent::$count;",
                "&parent::$cou|nt;"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }

    public void testStaticFieldAccess_ClassName() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {",
                "Animal::$count;",
                "Ani|mal::$count;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccess_ClassNameRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {",
                "Animal::$count;",
                "&Ani|mal::$count;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }

    public void testStaticFieldAccess2_ClassName() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Cat extends Mammal {",
                "class ^Cat extends Mammal {",
                "Cat::$count;",
                "Ca|t::$count;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccess2_ClassNameRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Cat extends Mammal {",
                "class ^Cat extends Mammal {",
                "Cat::$count;",
                "&Ca|t::$count;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }

    public void testStaticFieldAccessOutsideClass_ClassName() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {",
                "Animal::$count--;",
                "Ani|mal::$count--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessOutsideClass_ClassNameRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {",
                "Animal::$count--;",
                "&Ani|mal::$count--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }

    public void testStaticFieldAccessOutsideClass2_ClassName() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Cat extends Mammal {",
                "class ^Cat extends Mammal {",
                "Cat::$count--;",
                "Ca|t::$count--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessOutsideClass2_ClassNameRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Cat extends Mammal {",
                "class ^Cat extends Mammal {",
                "Cat::$count--;",
                "&Ca|t::$count--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }

    public void testStaticFieldAccessOutsideClassDeclaredInSuperClass_ClassName() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {",
                "Mammal::$count--;",
                "Mam|mal::$count--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticFieldAccessOutsideClassDeclaredInSuperClass_ClassNameRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {",
                "Mammal::$count--;",
                "&Mam|mal::$count--;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }

    public void testStaticFieldAccessInOtherFile_ClassName() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "return Animal::$count;",
                "return Ani|mal::$count;;"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    public void testStaticFieldAccessInOtherFile_ClassNameRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "return Animal::$count;",
                "return &Ani|mal::$count;;"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    //classes
    public void testClassInstantiation() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "function __construct() {//Mammal",
                "function ^__construct() {//Mammal",
                "$mammal = new Mammal;",
                "$mammal = new Mamm|al;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testSuperClasses() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {",
                "class Cat extends Mammal {",
                "class Cat extends Mamm|al {"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testSuperClassesOtherClass() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "class Fish extends Animal {",
                "class Fish extends Anim|al {"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }


    //method invocations
    public void testMethodInvocationParent() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function ^getCount($animalLogging) {",
                "echo parent::getCount(\"calling animal's getCount 1\");",
                "echo parent::getC|ount(\"calling animal's getCount 1\");"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testMethodInvocationParent2() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function ^getCount($animalLogging) {",
                "echo parent::getCount(\"calling animal's getCount 2\");",
                "echo parent::getC|ount(\"calling animal's getCount 2\");"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testMethodInvocation() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function ^getCount($animalLogging) {",
                "$mammal->getCount(\"calling animal's getCount 3\");",
                "$mammal->get|Count(\"calling animal's getCount 3\");"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testMethodInvocationConstructor() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest2.php",
                "function __construct($info) {",
                "function ^__construct($info) {",
                "parent::__construct(\"\");",
                "parent::__constr|uct(\"\");"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testMethodInvocationRef() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function &^getCount($animalLogging) {",
                "$mammal->getCount(\"calling animal's getCount 3\");",
                "$mammal->get|Count(\"calling animal's getCount 3\");"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testMethodInvocation2() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($catLogging) {",
                "public function ^getCount($catLogging) {",
                "$cat->getCount(\"calling cat's getCount 1\");",
                "$cat->getCo|unt(\"calling cat's getCount 1\");"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testMethodInvocationParentThis() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($catLogging) {",
                "public function ^getCount($catLogging) {",
                "echo $this->getCount(\"calling cat's getCount\");",
                "echo $this->getCou|nt(\"calling cat's getCount\");"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testMethodInvocationSelf() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function ^getCount($animalLogging) {",
                "self::getCount(\"calling animal's getCount 0\");",
                "self::get|Count(\"calling animal's getCount 0\");"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }

    public void testMethodInvocationFromOtherClassThis() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function ^getCount($animalLogging) {"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "$this->getCount(\"\")",
                "$this->getCo|unt(\"\")"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    public void testMethodInvocationFromOtherClassParent() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function ^getCount($animalLogging) {"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "parent::getCount(\"\")",
                "parent::getCo|unt(\"\")"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    public void testMethodInvocationFromOtherClassSelf() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function ^getCount($animalLogging) {"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "self::getCount(\"\")",
                "self::getCo|unt(\"\")"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    public void testMethodInvocationFromOther2() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function ^getCount($animalLogging) {"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "$mammal->getCount(\"\")",
                "$mammal->getCo|unt(\"\")"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    public void testMethodInvocationFromOther4() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($animalLogging) {",
                "public function ^getCount($animalLogging) {"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "$fish->getCount(\"\")",
                "$fish->getCo|unt(\"\")"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }
    public void testMethodInvocationFromOther5() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest2.php",
                "public function getCount($sharkLogging) {",
                "public function ^getCount($sharkLogging) {",
                "$shark->getCount(\"\");",
                "$$shark->getCou|nt(\"\");"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;",
                "echo self::KIND;",
                "echo self::KIN|D;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess2() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;",
                "$isMe = (self::KIND == $mammalKind);",
                "$isMe = (self::KI|ND == $mammalKind);"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess2_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;",
                "$isParentAnimal = (parent::KIND == $animalKind);",
                "$isParentAnimal = (parent::KI|ND == $animalKind);"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess3() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;",
                "$mammalKind = Mammal::KIND;",
                "$mammalKind = Mammal::KIN|D;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess3_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {",
                "$mammalKind = Mammal::KIND;",
                "$mammalKind = Mam|mal::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess4() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;",
                "$animalKind = Animal::KIND;",
                "$animalKind = Animal::KI|ND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess4_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {",
                "$animalKind = Animal::KIND;",
                "$animalKind = Ani|mal::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess5() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=3;",
                "const ^KIND=3;",
                "$catKind = self::KIND;",
                "$catKind = self::KIN|D;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }    
    public void testConstantAccess6() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;",
                "echo Animal::KIND;",
                "echo Animal::KIN|D;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }    
    public void testConstantAccess6_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {",
                "echo Animal::KIND;",
                "echo Ani|mal::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess7() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;",
                "echo Mammal::KIND;",
                "echo Mammal::KI|ND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess7_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {",
                "echo Mammal::KIND;",
                "echo Mamm|al::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess8() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=3;",
                "const ^KIND=3;",
                "echo Cat::KIND;",
                "echo Cat::KI|ND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess8_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Cat extends Mammal {",
                "class ^Cat extends Mammal {",
                "echo Cat::KIND;",
                "echo Ca|t::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess9() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;",
                "print Animal::KIND;",
                "print Animal::KI|ND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess9_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {",
                "print Animal::KIND;",
                "print Ani|mal::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess10() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;",
                "print Mammal::KIND;",
                "print Mammal::KIN|D;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess10_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {",
                "print Mammal::KIND;",
                "print Mam|mal::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess11() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=3;",
                "const ^KIND=3;",
                "print Cat::KIND;",
                "print Cat::KI|ND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess11_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Cat extends Mammal {",
                "class ^Cat extends Mammal {",
                "print Cat::KIND;",
                "print Ca|t::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testConstantAccess12() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Animal::KIND;",
                "echo Animal::KIN|D;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);        
    }
    public void testConstantAccess12_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Animal::KIND;",
                "echo Ani|mal::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testConstantAccess13() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Mammal::KIND;",
                "echo Mammal::KI|ND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testConstantAccess13_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Mammal::KIND;",
                "echo Mamm|al::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testConstantAccess14() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=3;",
                "const ^KIND=3;"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Cat::KIND;",
                "echo Cat::KI|ND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testConstantAccess15() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "print Animal::KIND;",
                "print Animal::KIN|D;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testConstantAccess15_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "print Animal::KIND;",
                "print Anim|al::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testConstantAccess16() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=1;",
                "const ^KIND=1;"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "print Mammal::KIND;",
                "print Mammal::KIN|D;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testConstantAccess16_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "print Mammal::KIND;",
                "print Ma|mmal::KIND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    /*TODO: fails, evaluate, fix
     public void testConstantAccess17() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "const KIND=3;",
                "^const KIND=3;"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "print Cat::KIND;",
                "print Cat::KI|ND;"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }*/

    /* TODO: check, fix
    public void testMethodInvocationFromOther3() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public function getCount($catLogging) {",
                "public ^function getCount($catLogging) {"
                );
        String animal2Test = prepareTestFile(
                "testfiles/animalTest2.php",
                "$cat->getCount(\"\")",
                "$cat->getCo|unt(\"\")"
                );
        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
    }*/

    public void testFirstStaticMethodInvocation() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static $count = 0, $animal;",
                "public static $^count = 0, $animal;",
                "echo Mammal::$count;",
                "echo Mammal::$co|unt;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }


    public void testStaticMethodInvocation() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"animal is ...\";}",
                "public static function ^kindInfo() {return \"animal is ...\";}",
                "echo Animal::kindInfo();",
                "echo Animal::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Animal::kindInfo();",
                "echo Ani|mal::kindInfo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testStaticMethodInvocation1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"animal is ...\";}",
                "public static function ^kindInfo() {return \"animal is ...\";}",
                "echo Mammal::kindInfo();",
                "echo Mammal::kindI|nfo();;"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation1_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {",
                "echo Mammal::kindInfo();",
                "echo Mam|mal::kindInfo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation2() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"cat is ...\";}",
                "public static function ^kindInfo() {return \"cat is ...\";}",
                "echo Cat::kindInfo();",
                "echo Cat::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation2_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Cat extends Mammal {",
                "class ^Cat extends Mammal {",
                "echo Cat::kindInfo();",
                "echo C|at::kindInfo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation3() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"cat is ...\";}",
                "public static function ^kindInfo() {return \"cat is ...\";}",
                "echo self::kindInfo();",
                "echo self::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation4() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"animal is ...\";}",
                "public static function ^kindInfo() {return \"animal is ...\";}",
                "echo parent::kindInfo();",
                "echo parent::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    /* TODO: fails, evaluate, fix
    public void testStaticMethodInvocation5() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"animal is ...\";}",
                "public static ^function kindInfo() {return \"animal is ...\";}",
                "print Animal::kindInfo();",
                "print Animal::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }*/
    public void testStaticMethodInvocation5_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {",
                "print Animal::kindInfo();",
                "print Anim|al::kindInfo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation6() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"animal is ...\";}",
                "public static function ^kindInfo() {return \"animal is ...\";}",
                "print Mammal::kindInfo();",
                "print Mammal::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation6_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {",
                "print Mammal::kindInfo();",
                "print Mam|mal::kindInfo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation7() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"cat is ...\";}",
                "public static function ^kindInfo() {return \"cat is ...\";}",
                "print Cat::kindInfo();",
                "print Cat::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }
    public void testStaticMethodInvocation7_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Cat extends Mammal {",
                "class ^Cat extends Mammal {",
                "print Cat::kindInfo();",
                "print Ca|t::kindInfo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest);
    }

    //////////////
    public void testStaticMethodInvocation8() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"animal is ...\";}",
                "public static function ^kindInfo() {return \"animal is ...\";}"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Animal::kindInfo();",
                "echo Animal::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testStaticMethodInvocation8_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "abstract class Animal {",
                "abstract class ^Animal {"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Animal::kindInfo();",
                "echo Ani|mal::kindInfo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testStaticMethodInvocation9() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"animal is ...\";}",
                "public static function ^kindInfo() {return \"animal is ...\";}"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Mammal::kindInfo();",
                "echo Mammal::kindI|nfo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    public void testStaticMethodInvocation9_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Mammal extends Animal {",
                "class ^Mammal extends Animal {"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Mammal::kindInfo();",
                "echo Mam|mal::kindInfo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }
    /* TODO: fails, evaluate, fix
     public void testStaticMethodInvocation10() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"cat is ...\";}",
                "^public static function kindInfo() {return \"cat is ...\";}"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Cat::kindInfo();",
                "echo Cat::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
    }*/
    public void testStaticMethodInvocation10_1() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "class Cat extends Mammal {",
                "class ^Cat extends Mammal {"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo Cat::kindInfo();",
                "echo C|at::kindInfo();"
                );
        performTestSimpleFindDeclaration(-1,animalTest2, animalTest);
    }
    public void testStaticMethodInvocation11() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"animal is ...\";}",
                "public static function ^kindInfo() {return \"animal is ...\";}"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo self::kindInfo();",
                "echo self::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1,animalTest2, animalTest);
    }
    public void testStaticMethodInvocation12() throws Exception {
        String animalTest = prepareTestFile(
                "testfiles/animalTest.php",
                "public static function kindInfo() {return \"animal is ...\";}",
                "public static function ^kindInfo() {return \"animal is ...\";}"
                );
        String animalTest2 = prepareTestFile(
                "testfiles/animalTest2.php",
                "echo parent::kindInfo();",
                "echo parent::kindIn|fo();"
                );
        performTestSimpleFindDeclaration(-1,animalTest2, animalTest);
    }

    
    public void testDefines2() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "echo \"a\".te|st.\"b\";\n" +
                                         "?>",
                                         "<?php\n" +
                                         "define('^test', 'test');\n" +
                                         "?>");
    }
    
    public void testSimpleFindDeclaration1() throws Exception {
        performTestSimpleFindDeclaration("<?php\n$^name = \"test\";\n echo \"$na|me\";\n?>");
    }

    public void testSimpleFindDeclaration2() throws Exception {
        performTestSimpleFindDeclaration("<?php\n$^name = \"test\";\n$name = \"test\";\n echo \"$na|me\";\n?>");
    }

    public void testSimpleFindDeclaration3() throws Exception {
        performTestSimpleFindDeclaration("<?php\n$^name = \"test\";\n$name = \"test\";\n echo \"$na|me\";\n$name = \"test\";\n?>");
    }

    public void testSimpleFindDeclaration4() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "$name = \"test\";\n" +
                                         "function foo($^name) {\n" +
                                         "    echo \"$na|me\";\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testSimpleFindDeclaration5() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "$^name = \"test\";\n" +
                                         "function foo($name) {\n" +
                                         "}\n" +
                                         "echo \"$na|me\";\n" +
                                         "?>");
    }

    public void testSimpleFindDeclaration6() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "$name = \"test\";\n" +
                                         "function ^foo($name) {\n" +
                                         "}\n" +
                                         "fo|o($name);\n" +
                                         "?>");
    }

    public void testSimpleFindDeclaration7() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "class ^name {\n" +
                                         "}\n" +
                                         "$r = new na|me();\n" +
                                         "?>");
    }

    public void testSimpleFindDeclaration8() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "class name {\n" +
                                         "    function ^test() {" +
                                         "    }" +
                                         "}\n" +
                                         "$r = new name();\n" +
                                         "$r->te|st();" +
                                         "?>");
    }

    public void testSimpleFindDeclaration9() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "$^name = \"test\";\n" +
                                         "function foo($name) {\n" +
                                         "}\n" +
                                         "foo($na|me);\n" +
                                         "?>");
    }

    public void testFindDeclarationInOtherFile1() throws Exception {
        performTestSimpleFindDeclaration(1,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "function ^foo() {}\n" +
                                         "?>");
    }

    public void testFindDeclarationInOtherFile2() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "include \"testb.php\";\n" +
                                         "?>",
                                         "<?php\n" +
                                         "function ^foo() {}\n" +
                                         "?>");
    }

    public void testFindDeclarationInOtherFile3() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "$r = new fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "include \"testb.php\";\n" +
                                         "?>",
                                         "<?php\n" +
                                         "class ^foo {}\n" +
                                         "?>",
                                         "<?php\n" +
                                         "class ^foo {}\n" +
                                         "?>");
    }

    public void testFunctionsInGlobalScope1() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "function ^foo() {}\n" +
                                         "function bar() {\n" +
                                         "    fo|o();\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testClassInGlobalScope1() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "function foo() {" +
                                         "    class ^bar {}\n" +
                                         "}\n" +
                                         "$r = new b|ar();\n" +
                                         "?>");
    }

    public void testArrayVariable() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "$^foo = array();\n" +
                                         "$f|oo['test'] = array();\n" +
                                         "?>");
    }

    public void testResolveUseBeforeDeclaration() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "fo|o();\n" +
                                         "function ^foo() {}\n" +
                                         "?>",
                                         "<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }

    public void testShowAllDeclarationsWhenUnknownForFunctions() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "function ^foo() {}\n" +
                                         "?>",
                                          "<?php\n" +
                                         "function ^foo() {}\n" +
                                         "?>");
    }

    public void testShowAllDeclarationsWhenUnknownForClasses() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "$r = new fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "class ^foo {}\n" +
                                         "?>",
                                          "<?php\n" +
                                         "class ^foo {}\n" +
                                         "?>");
    }

    public void testDefines1() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "define('^test', 'test');\n" +
                                         "echo \"a\".te|st.\"b\";\n" +
                                         "?>");
    }

    public void testGoToInherited() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "class foo {\n" +
                                         "    function ^test() {}\n" +
                                         "}\n" +
                                         "class bar extends foo {\n" +
                                         "}\n" +
                                         "$r = new bar();\n" +
                                         "$r->te|st();" +
                                         "?>");
    }

    public void testGoToInclude01() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "include \"te|sta.php\";\n" +
                                         "?>",
                                         "^<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }

    public void testGoToInclude02() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "include ('|testa.php');\n" +
                                         "?>",
                                         "^<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }

    public void testGoToInclude03() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "require 'testa.php|';\n" +
                                         "?>",
                                         "^<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }

    public void testGoToInclude04() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "include_once '|testa.php';\n" +
                                         "?>",
                                         "^<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }

    public void testGoToInclude05() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "include_once ('|testa.php');\n" +
                                         "?>",
                                         "^<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }

    public void testGoToInclude06() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "require_once '|testa.php';\n" +
                                         "?>",
                                         "^<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }

    public void testGoToInclude07() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "require_once (\"|testa.php\");\n" +
                                         "?>",
                                         "^<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }
    
    public void testGoToInstanceVar() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "class test {\n" +
                                         "    function ftest($name) {\n" +
                                         "        $this->na|me = $name;\n" +
                                         "    }\n" +
                                         "    var $^name;\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testGoToForward() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "class test {\n" +
                                         "    function ftest($name) {\n" +
                                         "        $this->na|me();\n" +
                                         "    }\n" +
                                         "    function ^name() {}\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testMethodInOtherFile() throws Exception {
        performTestSimpleFindDeclaration(-1,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "$r = new foo();\n" +
                                         "$r->ffo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "include \"testb.php\";\n" +
                                         "?>",
                                         "<?php\n" +
                                         "class foo {\n" +
                                         "    function ^ffoo() {\n" +
                                         "    }\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testMethodInOtherFileWithInheritance() throws Exception {
        performTestSimpleFindDeclaration(-1,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "$r = new foo2();\n" +
                                         "$r->ffo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "include \"testb.php\";\n" +
                                         "class foo2 extends foo3 {}\n" +
                                         "?>",
                                         "<?php\n" +
                                         "class foo3 {\n" +
                                         "    function ^ffoo() {\n" +
                                         "    }\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testExtendedClass() throws Exception {
        String userClass = prepareTestFile(
                "testfiles/classUser.php",
                "extends Man {",
                "extends M|an {");
        String manClass = prepareTestFile(
                "testfiles/classMan.php",
                "class Man implements Person {",
                "class ^Man implements Person {");
        performTestSimpleFindDeclaration(-1, userClass, manClass);
    }

    public void testPHPDocType01() throws Exception {
        performTestSimpleFindDeclaration(-1,
                                         "<?php\n" +
                                         "class Magazine {\n" +
                                         "    public $title;\n" +
                                         "}\n" +
                                         "class ^Book { \n" +
                                         "    public $author;\n" +
                                         "}\n" +
                                         "/**\n" +
                                         " * @param Bo|ok $hello\n" +
                                         " * @return Magazine test\n" +
                                         " */\n" +
                                         "function test($hello) {\n" +
                                         "}\n" +
                                         "?>\n");
    }

    public void testPHPDocType02() throws Exception {
        performTestSimpleFindDeclaration(-1,
                                         "<?php\n" +
                                         "class ^Magazine {\n" +
                                         "    public $title;\n" +
                                         "}\n" +
                                         "class Book { \n" +
                                         "    public $author;\n" +
                                         "}\n" +
                                         "/**\n" +
                                         " * @param Book $hello\n" +
                                         " * @return Mag|azine test\n" +
                                         " */\n" +
                                         "function test($hello) {\n" +
                                         "}\n" +
                                         "?>\n");
    }

    public void testPHPDocParamName() throws Exception {
        performTestSimpleFindDeclaration(-1,
                                         "<?php\n" +
                                         "/**\n" +
                                         " *\n" +
                                         " * @param  string $he|llo\n" +
                                         " */\n" +
                                        "function test($^hello) {\n" +
                                         "}\n" +
                                         "?> ");
    }

    private void performTestSimpleFindDeclaration(int declarationFile, String... code) throws Exception {
        assertTrue(code.length > 0);

        Set<Golden> golden = new HashSet<Golden>();

        for (int cntr = 0; cntr < code.length; cntr++) {
            int i = code[cntr].replaceAll("\\|", "").indexOf('^');

            if (i != (-1)) {
                golden.add(new Golden(cntr, i));

                code[cntr] = code[cntr].replaceAll("\\^", "");
            }
        }

        int caretOffset = code[0].indexOf('|');

        code[0] = code[0].replaceAll("\\|", "");

        assertTrue(caretOffset != (-1));
        assertFalse(golden.isEmpty());

        performTestSimpleFindDeclaration(code, caretOffset, golden);
    }

    private void performTestSimpleFindDeclaration(String code) throws Exception {
        int caretOffset = code.replaceAll("\\^", "").indexOf('|');
        int declOffset = code.replaceAll("\\|", "").indexOf('^');

        assertTrue(caretOffset != (-1));
        assertTrue(declOffset != (-1));

        performTestSimpleFindDeclaration(code.replaceAll("\\^", "").replaceAll("\\|", ""), caretOffset, declOffset);
    }

    private void performTestSimpleFindDeclaration(String code, final int caretOffset, final int declarationOffset) throws Exception {
        performTestSimpleFindDeclaration(new String[] {code}, caretOffset, 0, declarationOffset);
    }

    private void performTestSimpleFindDeclaration(String[] code, final int caretOffset, final int declarationFile, final int declarationOffset) throws Exception {
        performTestSimpleFindDeclaration(code, caretOffset, Collections.singleton(new Golden(declarationFile, declarationOffset)));
    }

    private void performTestSimpleFindDeclaration(String[] code, final int caretOffset, final Set<Golden> golden) throws Exception {
        final DeclarationLocation[] found = new DeclarationLocation[1];
        final ParserResult[] parserResult = new ParserResult[1];
        performTest(code, new UserTask() {

            public void cancel() {}

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                 parserResult[0] = (ParserResult) resultIterator.getParserResult();
            }
        });
        found[0] = DeclarationFinderImpl.findDeclarationImpl(parserResult[0], caretOffset);
        assertNotNull(found[0]);
        assertNotNull(found[0].getFileObject());
        Set<Golden> result = new HashSet<Golden>();

        result.add(new Golden(found[0].getFileObject().getNameExt(), found[0].getOffset()));

        for (AlternativeLocation l : found[0].getAlternativeLocations()) {
            result.add(new Golden(l.getLocation().getFileObject().getNameExt(), l.getLocation().getOffset()));
        }

        assertEquals(golden, result);
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        try {
            return new FileObject[]{toFileObject(workDirToFileObject(), "src", true)};//NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static final class Golden {
        private String declarationFile;
        private int declarationOffset;

        public Golden(int declarationFile, int declarationOffset) {
            this(computeFileName(declarationFile - 1), declarationOffset);
        }

        public Golden(String declarationFile, int declarationOffset) {
            this.declarationFile = declarationFile;
            this.declarationOffset = declarationOffset;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Golden other = (Golden) obj;
            if (this.declarationFile != other.declarationFile && (this.declarationFile == null || !this.declarationFile.equals(other.declarationFile))) {
                return false;
            }
            if (this.declarationOffset != other.declarationOffset) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.declarationFile != null ? this.declarationFile.hashCode() : 0);
            hash = 29 * hash + this.declarationOffset;
            return hash;
        }

        @Override
        public String toString() {
            return "[Golden: " + declarationFile + ":" + declarationOffset + "]";
        }

    }
}
