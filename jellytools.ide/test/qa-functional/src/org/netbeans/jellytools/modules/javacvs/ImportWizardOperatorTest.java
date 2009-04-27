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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.jellytools.modules.javacvs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.modules.javacvs.ImportWizardOperator,
 * BrowseRepositoryFolderOperator, FolderToImportStepOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */ 
public class ImportWizardOperatorTest extends JellyTestCase {

    public static final String[] tests = new String[] {
        "testInvoke", "testSetCVSRoot", "testFolderToImport", "testFinish"
    };

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        /*
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ImportWizardOperatorTest("testInvoke"));
        suite.addTest(new ImportWizardOperatorTest("testSetCVSRoot"));
        suite.addTest(new ImportWizardOperatorTest("testFolderToImport"));
        suite.addTest(new ImportWizardOperatorTest("testFinish"));
        return suite;
         */
        /*
        return createModuleTest(ImportWizardOperatorTest.class,
        "testInvoke",
        "testSetCVSRoot",
        "testFolderToImport",
        "testFinish");
         */
        //comment out for now
        //return createModuleTest(ImportWizardOperatorTest.class, tests);
        return new TestSuite();
    }

    /** Set up executed before each test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public ImportWizardOperatorTest(String testName) {
        super(testName);
    }
    
    /** Test of invoke method.*/
    public void testInvoke() throws IOException {
        openDataProjects("SampleProject");
        ImportWizardOperator.invoke(ProjectsTabOperator.invoke().getProjectRootNode("SampleProject")); //NOI18N
    }

    private static PseudoCvsServer cvss;
    
    /** Tests CVS root customizer. */
    public void testSetCVSRoot() throws Exception {
        CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
        InputStream in = new ByteArrayInputStream(
                new byte[] {'I', ' ', 'L', 'O', 'V', 'E', ' ', 'Y', 'O', 'U', '\n'});
        try {
            cvss = new PseudoCvsServer(in);
        } catch (IOException ioe) {
            throw new JemmyException("Error initializing PseudoCvsServer: "+ioe);
        }
        cvss.ignoreProbe();
        new Thread(cvss).start();
        cvsRootOper.setCVSRoot(cvss.getCvsRoot());
        OptionsOperator proxyOper = cvsRootOper.proxyConfiguration();
        proxyOper.close();
        cvsRootOper.next();
    }

    /** Test Folder to Import panel of wizard.  */
    public void testFolderToImport() throws Exception {
        FolderToImportStepOperator folderToImportOper = new FolderToImportStepOperator();
        folderToImportOper.setFolderToImport(getWorkDirPath());
        JFileChooserOperator browseFolder = folderToImportOper.browseFolderToImport();
        assertEquals("Directory set in wizard not propagated to file chooser:", getWorkDir().getAbsolutePath(), browseFolder.getCurrentDirectory().getAbsolutePath()); // NOI18N
        browseFolder.cancel();
        folderToImportOper.setImportMessage("Import message"); //NOI18N
        folderToImportOper.setRepositoryFolder("folder");
        BrowseRepositoryFolderOperator browseRepositoryOper =  folderToImportOper.browseRepositoryFolder();
        browseRepositoryOper.selectFolder(""); // NOI18N
        browseRepositoryOper.ok();
        folderToImportOper.checkCheckoutAfterImport(false);
    }
    
    /** Test finish wizard. */
    public void testFinish() throws Exception {
        cvss.stop();
        // Cancel wizard (finish is possible but causes an error because 
        // of use of pseudo pserver.
        new ImportWizardOperator().cancel();
    }
}
