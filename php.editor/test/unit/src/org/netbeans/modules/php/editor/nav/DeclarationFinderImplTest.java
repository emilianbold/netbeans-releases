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
