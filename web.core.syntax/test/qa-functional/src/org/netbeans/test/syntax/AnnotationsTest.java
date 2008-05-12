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
package org.netbeans.test.syntax;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.lib.BasicOpenFileTest;
import org.netbeans.test.web.RecurrentSuiteFactory;

/**
 *
 * @author Jindrich Sedek
 */
public class AnnotationsTest extends BasicOpenFileTest {

    private boolean GENERATE_GOLDEN_FILES = false;
    private String projectName = "AnnTestProject";
    private static boolean firstTest = true;
    public AnnotationsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (firstTest){
            openProject(projectName);
            RecurrentSuiteFactory.resolveServer(projectName);
            Thread.sleep(10000);
            firstTest = false;
        }
    }


    public void testIssue101861() throws Exception {
        runTest("issue101861.jspx");
    }

    public void testIssue121046() throws Exception {
        runTest("issue121046.jsp");
    }

    public void testIssue121768() throws Exception {
        runTest("issue121768.jsp");
    }

    public void testIssue131519() throws Exception {
        runTest("issue121768.jsp");
    }

    public void testIssue131871() throws Exception {
        runTest("issue131871.jsp");
    }

    public void testIssue133173() throws Exception {
        runTest("issue133173.jsp");
    }

    public void testIssue133173_() throws Exception {
        runTest("issue133173_.jsp");
    }

    public void testIssue99526() throws Exception {
        runTest("issue99526.html");
    }

    public void testIssue130745() throws Exception {
        runTest("issue130745.jsp");
    }

    public void testIssue133760() throws Exception {
        runTest("issue133760.jsp");
    }

    public void testIssue133841() throws Exception {
        runTest("issue133841.html");
    }

    public void testIssue134518() throws Exception {
        runTest("issue1334518.jsp");
    }

    public void testIssue134877() throws Exception {
        runTest("issue13344877.jsp");
    }

    public void testIssue134879() throws Exception {
        runTest("issue13344879.jspf");
    }
    
    public void testMissingEndTag() throws Exception {
        runTest("missingEndTag.html", 1);
    }

    public void testMissingStartTag() throws Exception {
        runTest("missingStartTag.html", 1);
    }

    public void testUnknownCSSProperty() throws Exception {
        runTest("unknownCSSProperty.html", 1);
    }
  
    private void runTest(String fileName) throws Exception {
        runTest(fileName, 0);
    }

    private void runTest(String fileName, int annotationsCount) throws Exception {
        EditorOperator eOp = openFile(fileName);
        Object[] anns = eOp.getAnnotations();
        assertEquals(annotationsCount, anns.length);
        for (Object object : anns) {
            String desc = EditorOperator.getAnnotationShortDescription(object);
            ref(desc);
        }
        eOp.closeDiscard();
        if (anns.length > 0) {
            if (!GENERATE_GOLDEN_FILES) {
                compareReferenceFiles();
            } else {
                CompletionTest.generateGoldenFiles(this);
            }
        }
    }

    @Override
    protected EditorOperator openFile(String fileName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Web Pages|" + fileName);
        node.select();
        node.performPopupAction("Open");
        EditorOperator operator = new EditorOperator(fileName);
        assertNotNull(operator.getText());
        assertTrue(operator.getText().length() > 0);
        return operator;
    }

    public static void main(java.lang.String[] args) {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(AnnotationsTest.class);
        junit.textui.TestRunner.run(suite);
    }
}
