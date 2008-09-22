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

package org.netbeans.modules.php.editor.parser;

import java.util.List;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.StructureItem;

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

    public void testPHPDocTagProperty() throws Exception {
        performTest("propertyTag");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        StringBuffer sb = new StringBuffer();
        CompilationInfo info = getInfo("testfiles/" + filename +".php");
        PhpStructureScanner instance = new PhpStructureScanner();
        List<? extends StructureItem> result = instance.scan(info);
        for (StructureItem structureItem : result) {
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