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
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;

/**
 *
 * @author Jan Lahoda
 */
public class OccurrencesFinderImplTest extends TestBase {
    
    public OccurrencesFinderImplTest(String testName) {
        super(testName);
    }            

    private String preaperTestFile(String filePath) throws IOException {
        String retval = TestUtilities.copyFileToString(new File(getDataDir(), filePath));
        return retval;
    }

    private String prepareTestFile(String filePath, String... texts) throws IOException {
        String retval = preaperTestFile(filePath);
        assert texts != null && texts.length%2 == 0;
        for (int i = 0; i+1 < texts.length; i++) {
            String originalText = texts[i];
            String replacement = texts[++i];
            retval = retval.replace(originalText, replacement);
        }
        return retval;
    }

    public void testMarkClsIface() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class ^clsDecla|ration^ implements ifaceDeclaration {}",
                "class clsDeclaration3 extends clsDeclaration {}",
                "class clsDeclaration3 extends ^clsDeclaration^ {}",
                "clsDeclaration  $clsDeclarationVar,",
                "^clsDeclaration^  $clsDeclarationVar,",
                "} catch (clsDeclaration $cex) {",
                "} catch (^clsDeclaration^ $cex) {",
                "if ($cex instanceof clsDeclaration) {",
                "if ($cex instanceof ^clsDeclaration^) {",
                "$cex = new clsDeclaration;",
                "$cex = new ^clsDeclaration^;"
                );
        performTestOccurrences(gotoTypeTest, true);
    }
    public void testMarkClsIface2() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class ^clsDeclaration^ implements ifaceDeclaration {}",
                "class clsDeclaration3 extends clsDeclaration {}",
                "class clsDeclaration3 extends ^clsDec|laration^ {}",
                "clsDeclaration  $clsDeclarationVar,",
                "^clsDeclaration^  $clsDeclarationVar,",
                "} catch (clsDeclaration $cex) {",
                "} catch (^clsDeclaration^ $cex) {",
                "if ($cex instanceof clsDeclaration) {",
                "if ($cex instanceof ^clsDeclaration^) {",
                "$cex = new clsDeclaration;",
                "$cex = new ^clsDeclaration^;"                
                );
        performTestOccurrences(gotoTypeTest, true);
    }
    public void testMarkClsIface3() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration {}",
                "interface ^ifaceDec|laration^ {}",
                "interface ifaceDeclaration2 extends ifaceDeclaration  {}",
                "interface ifaceDeclaration2 extends ^ifaceDeclaration^  {}",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class clsDeclaration implements ^ifaceDeclaration^ {}",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "class clsDeclaration2 implements ^ifaceDeclaration^, ifaceDeclaration2 {}",
                "ifaceDeclaration $ifaceDeclarationVar,",
                "^ifaceDeclaration^ $ifaceDeclarationVar,"
                );
        performTestOccurrences(gotoTypeTest, true);
    }
    public void testMarkClsIface4() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration {}",
                "interface ^ifaceDeclaration^ {}",
                "interface ifaceDeclaration2 extends ifaceDeclaration  {}",
                "interface ifaceDeclaration2 extends ^ifaceDecl|aration^  {}",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class clsDeclaration implements ^ifaceDeclaration^ {}",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "class clsDeclaration2 implements ^ifaceDeclaration^, ifaceDeclaration2 {}",
                "ifaceDeclaration $ifaceDeclarationVar,",
                "^ifaceDeclaration^ $ifaceDeclarationVar,"                
                );
        performTestOccurrences(gotoTypeTest, true);
    }
    public void testMarkClsIface5() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration {}",
                "interface ^ifaceDeclaration^ {}",
                "interface ifaceDeclaration2 extends ifaceDeclaration  {}",
                "interface ifaceDeclaration2 extends ^ifaceDeclaration^  {}",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class clsDeclaration implements ^ifaceDeclara|tion^ {}",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "class clsDeclaration2 implements ^ifaceDeclaration^, ifaceDeclaration2 {}",
                "ifaceDeclaration $ifaceDeclarationVar,",
                "^ifaceDeclaration^ $ifaceDeclarationVar,"                                
                );
        performTestOccurrences(gotoTypeTest, true);
    }
    public void testMarkClsIface6() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration {}",
                "interface ^ifaceDeclaration^ {}",
                "interface ifaceDeclaration2 extends ifaceDeclaration  {}",
                "interface ifaceDeclaration2 extends ^ifaceDeclaration^  {}",
                "class clsDeclaration implements ifaceDeclaration {}",
                "class clsDeclaration implements ^ifaceDeclaration^ {}",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "class clsDeclaration2 implements ^ifaceDecla|ration^, ifaceDeclaration2 {}",
                "ifaceDeclaration $ifaceDeclarationVar,",
                "^ifaceDeclaration^ $ifaceDeclarationVar,"                                
                );
        performTestOccurrences(gotoTypeTest, true);
    }
    public void testMarkClsIface7() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration2 extends ifaceDeclaration  {}",
                "interface ^ifaceDecl|aration2^ extends ifaceDeclaration  {}",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "class clsDeclaration2 implements ifaceDeclaration, ^ifaceDeclaration2^ {}",
                "ifaceDeclaration2 $ifaceDeclaration2Var,",
                "^ifaceDeclaration2^ $ifaceDeclaration2Var,"
                );
        performTestOccurrences(gotoTypeTest, true);
    }
    public void testMarkClsIface8() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "interface ifaceDeclaration2 extends ifaceDeclaration  {}",
                "interface ^ifaceDeclaration2^ extends ifaceDeclaration  {}",
                "class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}",
                "class clsDeclaration2 implements ifaceDeclaration, ^ifaceDecl|aration2^ {}",
                "ifaceDeclaration2 $ifaceDeclaration2Var,",
                "^ifaceDeclaration2^ $ifaceDeclaration2Var,"                
                );
        performTestOccurrences(gotoTypeTest, true);
    }
    //test no naming clashes for different kinds like fnc, var
    public void testMarkClsIface9() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "$ifaceDeclaration = 1;",
                "$^ifaceDec|laration^ = 1;"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface10() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "$ifaceDeclaration2 = 1;",
                "$^ifaceDeclarati|on2^ = 1;"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface11() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "$ifaceDeclaration4 = 1;",
                "$^iface|Declaration4^ = 1;"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface12() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "$clsDeclaration  = 1;",
                "$^clsDec|laration^  = 1;"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface13() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "$clsDeclaration2 = 1;",
                "$^clsDec|laration2^ = 1;"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface14() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "$clsDeclaration4 = 1;",
                "$^clsDec|laration4^ = 1;"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface15() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "$clsDeclaration3 = 1;",
                "$^clsDeclar|ation3^ = 1;"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface16() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "function ifaceDeclaration() {",
                "function ^ifaceDe|claration^() {"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface17() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "function ifaceDeclaration2() {}",
                "function ^ifaceDe|claration2^() {}"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface18() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "function ifaceDeclaration4() {}",
                "function ^ifaceDe|claration4^() {}"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface19() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "function clsDeclaration() {}",
                "function ^clsDecla|ration^() {}"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface20() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "function clsDeclaration2() {}",
                "function ^clsDecla|ration2^() {}"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface21() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "function clsDeclaration3() {}",
                "function ^clsDecla|ration3^() {}"
                );
        performTestOccurrences(gotoTypeTest, false);
    }
    public void testMarkClsIface22() throws Exception {
        String gotoTypeTest = prepareTestFile(
                "testfiles/gotoType.php",
                "function clsDeclaration4() {}",
                "function ^clsDecla|ration4^() {}"
                );
        performTestOccurrences(gotoTypeTest, false);
    }

    public void testOccurrences1() throws Exception {
        performTestOccurrences("<?php\n$^name^ = \"test\";\n echo \"$^na|me^\";\n?>", true);
    }
    
    public void testOccurrences2() throws Exception {
        performTestOccurrences("<?php\necho \"$^name^\";\n echo \"$^na|me^\";\n?>", true);
    }
    
    public void testOccurrences3() throws Exception {
        performTestOccurrences("<?php\n" +
                               "$name = \"test\";\n" +
                               "function foo() {\n" +
                               "    echo \"$^na|me^\";\n" +
                               "}\n" + 
                               "?>",
                               true);
    }
    
    public void testOccurrences4() throws Exception {
        performTestOccurrences("<?php\n" +
                               "$^name^ = \"test\";\n" +
                               "function foo() {\n" +
                               "    global $^name^;\n" +
                               "    echo \"$^na|me^\";\n" +
                               "}\n" + 
                               "?>",
                               true);
    }
    
    public void testOccurrences5() throws Exception {
        performTestOccurrences("<?php\n" +
                               "$^name^ = \"test\";\n" +
                               "function foo() {\n" +
                               "    echo $GLOBALS['^na|me^'];\n" +
                               "}\n" + 
                               "?>");
    }

    /* TODO: regression, fails, evaluate, fix 
    public void testOccurrencesDefines() throws Exception {
        performTestOccurrences("<?php\n" +
                               "echo \"fff\".test.\"dddd\";\n" +
                               "define('^test^', 'testttttt');\n" +
                               "echo \"fff\".^te|st^.\"dddd\";\n" +
                               "echo \"fff\".^test^.\"dddd\";\n" +
                               "?>",
                               true);
    }*/
    
    public void test132230() throws Exception {
        performTestOccurrences("<?php\n" +
                               "function a() {\n" +
                               "    global $^f^;\n" +
                               "    $^|f^['s']();\n" +
                               "}\n" +
                               "?>",
                               true);
    }
    
    public void testOccurrencesFunctionHeader() throws Exception {
        performTestOccurrences("<?php\n" +
                               "function ^fo|o^() {\n" +
                               "}\n" +
                               "^foo^();\n" +
                               "?>");
    }
    
    public void testOccurrencesClassHeader() throws Exception {
        performTestOccurrences("<?php\n" +
                               "class ^fo|o^ {\n" +
                               "}\n" +
                               "$r = new ^foo^();\n" +
                               "?>");
    }
    
    public void DISABLEDtestOccurrencesGlobalVariable() throws Exception {
        performTestOccurrences("<?php\n" +
                               "^$GLOBALS['na|me']^ = \"test\";\n" +
                               "function foo() {\n" +
                               "    echo ^$GLOBALS['name']^;\n" +
                               "}\n" + 
                               "?>", true);
    }
    
    public void testOccurrencesInstanceVarParam1() throws Exception {
        performTestOccurrences("<?php\n" +
                               "class test {\n" +
                               "    var $name;\n" +
                               "    function ftest($^name^) {\n" +
                               "        $this->name = $^na|me^;\n" +
                               "    }\n" +
                               "}\n" +
                               "?>",
                               true);
    }
    
    public void testOccurrencesInstanceVarParam2() throws Exception {
        performTestOccurrences("<?php\n" +
                               "class test {\n" +
                               "    var $^name^;\n" +
                               "    function ftest($name) {\n" +
                               "        $this->^na|me^ = $name;\n" +
                               "    }\n" +
                               "}\n" +
                               "?>",
                               true);
    }
    
    public void testOccurrencesInstanceMethod() throws Exception {
        performTestOccurrences("<?php\n" +
                               "class test {\n" +
                               "    function ^name^() {}\n" +
                               "    function ftest() {\n" +
                               "        $this->^na|me^();\n" +
                               "    }\n" +
                               "}\n" +
                               "?>",
                               true);
    }
    
    private void performTestOccurrences(String code) throws Exception {
        performTestOccurrences(code, false);
    }
    
    private void performTestOccurrences(String code, boolean symmetric) throws Exception {
        int caretOffset = code.replaceAll("\\^", "").indexOf('|');
        String[] split = code.replaceAll("\\|", "").split("\\^");
        
        assertTrue(split.length > 1);
        
        int[] goldenRanges = new int[split.length - 1];
        int offset = split[0].length();
        
        for (int cntr = 1; cntr < split.length; cntr++) {
            goldenRanges[cntr - 1] = offset;
            offset += split[cntr].length();
        }
        
        assertTrue(caretOffset != (-1));
        
        performTestOccurrences(code.replaceAll("\\^", "").replaceAll("\\|", ""), caretOffset, symmetric, goldenRanges);
    }

    private void performTestOccurrences(String code, final int caretOffset, final boolean symmetric, final int... goldenRanges) throws Exception {
        performTest(new String[] {code}, new CancellableTask<CompilationInfo>() {
            public void cancel() {}
            public void run(CompilationInfo parameter) throws Exception {
                Collection<OffsetRange> ranges = OccurrencesFinderImpl.compute(parameter, caretOffset);
                
                assertEquals(goldenRanges, ranges);
                
                if (symmetric) {
                    for (OffsetRange r : ranges) {
                        assertEquals(goldenRanges, OccurrencesFinderImpl.compute(parameter, (r.getStart() + r.getEnd()) / 2));
                    }
                }
            }
        });
    }
    
    private static void assertEquals(int[] goldenRanges, Collection<OffsetRange> ranges) {
        List<Integer> golden = new LinkedList<Integer>();
        List<Integer> out = new LinkedList<Integer>();

        for (OffsetRange r : ranges) {
            out.add(r.getStart());
            out.add(r.getEnd());
        }

        for (int i : goldenRanges) {
            golden.add(i);
        }

        assertEquals(golden, out);
    }
}
