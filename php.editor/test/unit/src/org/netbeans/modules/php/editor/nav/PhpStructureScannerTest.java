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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.ParserTestBase;
import org.netbeans.modules.php.editor.parser.TestHtmlFormatter;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class PhpStructureScannerTest extends ParserTestBase{

    public PhpStructureScannerTest(String testName) {
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

    /**
     * Test of scan method, of class PhpStructureScanner.
     */

    public void testNamespace() throws Exception {
        performTest("structure/php53/namespace");

    }
    public void testMultiple_namespaces() throws Exception {
        performTest("structure/php53/multiple_namespaces");

    }
    public void testBracketedMultipleNamespaces() throws Exception {
        performTest("structure/php53/bracketed_multiple_namespaces");

    }
    public void testBracketedMultipleNamespacesWithDefaultOne() throws Exception {
        performTest("structure/php53/bracketed_multiple_namespaces_with_default_one");

    }
    public void testScan() throws Exception {
        performTest("interface_001");

    }

    public void test133484() throws Exception {
        performTest("referenceParameter_001");
    }

    public void testClass() throws Exception {
        performTest("class005");
    }

    public void testIssue142644() throws Exception {
        performTest("issue142644");
    }

    public void testIssue148558() throws Exception {
        performTest("issue148558");
    }

    public void testPHPDocTagProperty() throws Exception {
        performTest("propertyTag");
    }

    public void testIssue205886_01() throws Exception {
        performTest("issue205886_01");
    }

    public void testTraits_01() throws Exception {
        performTest("traitsStructure_01");
    }

    public void testTraits_02() throws Exception {
        performTest("traitsStructure_02");
    }

    public void testIssue170712() throws Exception {
        performTest("issue170712");
    }

    public void testFoldingMethod() throws Exception {
        checkFolds("testfiles/parser/foldingMethod.php");
    }

    public void testFoldingConditionalStatements() throws Exception {
        checkFolds("testfiles/parser/foldingConditionalStatements.php");
    }

    public void testFoldingCycles() throws Exception {
        checkFolds("testfiles/parser/foldingCycles.php");
    }

    public void testFoldingMethod_1() throws Exception {
        checkFolds("testfiles/parser/foldingMethod_1.php");
    }

    public void testFoldingConditionalStatements_1() throws Exception {
        checkFolds("testfiles/parser/foldingConditionalStatements_1.php");
    }

    public void testFoldingCycles_1() throws Exception {
        checkFolds("testfiles/parser/foldingCycles_1.php");
    }

    public void testIssue213616() throws Exception {
        checkFolds("testfiles/parser/issue213616.php");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        StringBuffer sb = new StringBuffer();
        FileObject testFile = getTestFile("testfiles/" + filename + ".php");

        Source testSource = getTestSource(testFile);

        final PhpStructureScanner instance = new PhpStructureScanner();
        final List<StructureItem> result = new ArrayList<StructureItem>();
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult info = (PHPParseResult)resultIterator.getParserResult();
                if (info != null) {
                    result.addAll(instance.scan(info));
                }
            }
        });
        Comparator<StructureItem> comparator = new Comparator<StructureItem>() {
            public int compare(StructureItem o1, StructureItem o2) {
                long position1 = o1.getPosition();
                long position2 = o2.getPosition();
                return (int) (position1 - position2);
            }
        };
        Collections.sort(result,comparator);

        for (StructureItem structureItem : result) {
            Collections.sort(structureItem.getNestedItems(),comparator);
            sb.append(printStructureItem(structureItem, 0));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String printStructureItem(StructureItem structureItem, int indent) {
        StringBuffer sb = new StringBuffer();
        sb.append(indent(indent));
        sb.append(structureItem.getName());
        sb.append(" [");
        sb.append(structureItem.getPosition());
        sb.append(", ");
        sb.append(structureItem.getEndPosition());
        sb.append("] : ");
        HtmlFormatter formatter = new TestHtmlFormatter() ;
        sb.append(structureItem.getHtml(formatter));
        for (StructureItem item : structureItem.getNestedItems()) {
            sb.append("\n");
            sb.append(printStructureItem(item, indent+1));
        }
        return sb.toString();
    }

    private String indent(int indent) {
        String text = "|-";
        for (int i = 0; i < indent; i++  ) {
            text = text + "-";
        }
        return text;
    }


}
