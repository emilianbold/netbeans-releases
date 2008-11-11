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

package org.netbeans.modules.php.editor.parser;

import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.parser.astnodes.Program;

/**
 *
 * @author Petr Pisl
 */
public class SanitizeSourceTest extends ParserTestBase {
    
    public SanitizeSourceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSanitizeClass01() throws Exception {
        performTest("sanitize/sanitize001");
    }

    public void testSanitizeClass02() throws Exception {
        performTest("sanitize/sanitize002");
    }

    public void testSanitizeClass03() throws Exception {
        performTest("sanitize/sanitize004");
    }
    public void testSanitizeTopContext() throws Exception {
        performTest("sanitize/sanitize003");
    }

    public void testMissingEndCurly() throws Exception {
        // one } at the end
        performTest("sanitize/curly01");
    }

    public void testMissingEndCurly2() throws Exception {
        // more } at the end
        performTest("sanitize/curly02");
    }
    
    public void testMissingEndCurly3() throws Exception {
        // more } at the end without end php ?> 
        performTest("sanitize/curly03");
    }
    
    public void testMissingEndCurly4() throws Exception {
        // inner functions
        performTest("sanitize/curly04");
    }

    public void testMissingEndCurly5() throws Exception {
        // non finished class and method and blog
        performTest("sanitize/curly05");
    }
    
    public void testMissingEndCurly6() throws Exception {
        // non finished class and method and blog 2
        performTest("sanitize/curly06");
    }
    
    public void testMissingEndCurly7() throws Exception {
        // non finieshed class and method
        performTest("sanitize/curly07");
    }
    
    public void testMissingEndCurly8() throws Exception {
        // non finished class
        performTest("sanitize/curly08");
    }

    public void testMissingEndCurly9() throws Exception {
        // non finished class
        performTest("sanitize/sanitize005");
    }
    
    public void testUnfinishedVar() throws Exception {
        // non finished class
        performTest("sanitize/sanitize006");
    }

    // testing when class declaration is in class declaration
    public void testCDInCD01() throws Exception {
        performTest("sanitize/sanitize007");
    }
    public void testCDInCD02() throws Exception {
        performTest("sanitize/sanitize008");
    }
    public void testCDInCD03() throws Exception {
        performTest("sanitize/sanitize009");
    }
    public void testCDInCD04() throws Exception {
        performTest("sanitize/sanitize010");
    }
    public void testCDInCD05() throws Exception {
        performTest("sanitize/sanitize011");
    }
    public void testCDInCD06() throws Exception {
        performTest("sanitize/sanitize012");
    }
    public void testCDInCD07() throws Exception {
        performTest("sanitize/sanitize013");
    }
    public void testCDInCD08() throws Exception {
        performTest("sanitize/sanitize014");
    }
    public void testCDInCD09() throws Exception {
        performTest("sanitize/sanitize015");
    }
    public void testCDInCD10() throws Exception {
        performTest("sanitize/sanitize016");
    }

    // disabling the test, unitl I find out what is wrong
//    public void test145494() throws Exception {
//        performTest("sanitize/sanitize145494");
//    }

    public void testDoNotDeleteCurly01() throws Exception {
        performTest("sanitize/sanitize017");
    }

    public void testCase01() throws Exception {
        performTest("sanitize/case01");
    }

    public void test149424() throws Exception {
        performTest("sanitize/issue149424");
    }

    protected String getTestResult(String filename) throws Exception {
        GsfTestCompilationInfo info = getInfo("testfiles/" + filename + ".php");
        StringBuffer textresult = new StringBuffer();
        int offset = info.getText().indexOf('^');
        if (offset > -1) {
            String content = info.getText();
            content = content.substring(0, offset) + content.substring(offset+1, content.length()-1);
            info = getInfoForText(content, "testFile.php");
            info.setCaretOffset(offset);
        }

        ParserResult result = info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);

        if (result == null) {
            textresult.append("Not possible to parse");
        } else {
            PHPParseResult phpResult = (PHPParseResult)result;
            Program program = phpResult.getProgram();

            if (program != null){
                textresult.append((new PrintASTVisitor()).printTree(program, 0));
            }
            else {
                textresult.append("Program is null");
            }
        }
        return textresult.toString();
    }
}
