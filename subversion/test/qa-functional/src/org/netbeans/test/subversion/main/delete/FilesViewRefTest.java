/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.subversion.main.delete;

import java.io.File;
import java.io.PrintStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.CommitOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author novakm
 */
public class FilesViewRefTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;

    /** Creates a new instance of FilesViewRefTest */
    public FilesViewRefTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### " + getName() + " ###");
    }

    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }

    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new FilesViewRefTest("testFilesViewRefactoring"));
        return suite;
    }

    public void testFilesViewRefactoring() throws Exception {
        try {
            TestKit.closeProject(PROJECT_NAME);
            OutputOperator.invoke();
            JTableOperator table;
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            VersioningOperator vo = VersioningOperator.invoke();
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();

            //create repository...
            File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
            new File(TMP_PATH).mkdirs();
            work.mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            //RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

            rso.next();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);
            wdso.finish();
            //open project
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
//            oto.clear();
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();

            TestKit.waitForScanFinishedAndQueueEmpty();

            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            TestKit.createNewPackage(PROJECT_NAME, "a.b.c");
            TestKit.createNewElement(PROJECT_NAME, "a", "AClass");
            TestKit.createNewElement(PROJECT_NAME, "a.b", "BClass");
            TestKit.createNewElement(PROJECT_NAME, "a.b.c", "CClass");
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "");
            node = new Node(new FilesTabOperator().tree(), PROJECT_NAME);
            node.performPopupActionNoBlock("Subversion|Show Changes");
            CommitOperator cmo = CommitOperator.invoke(node);
            cmo.commit();
            node = new Node(new FilesTabOperator().tree(), PROJECT_NAME + "|src|a");
            node.performPopupActionNoBlock("Cut");
            node = new Node(new FilesTabOperator().tree(), PROJECT_NAME + "|src|javaapp");
//            node.performPopupActionNoBlock("Paste");
            node.performPopupActionNoBlock("Paste|Refactor Move");

            nbdialog = new NbDialogOperator("Move Classes");
            JButtonOperator refBut = new JButtonOperator(nbdialog, "Refactor");
            refBut.push();
            nbdialog.waitClosed();
            Thread.sleep(1000);
            node = new Node(new FilesTabOperator().tree(), PROJECT_NAME);
            node.performPopupActionNoBlock("Subversion|Show Changes");
            vo = VersioningOperator.invoke();
            String[] expected = new String[]{"AClass.java", "BClass.java", "CClass.java", "a", "AClass.java", "b", "BClass.java", "c", "CClass.java"};
            String[] actual = new String[vo.tabFiles().getRowCount()];
            for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
                actual[i] = vo.tabFiles().getValueAt(i, 0).toString().trim();
            }
//            for (int idx = 0; idx < vo.tabFiles().getRowCount(); idx++) {
//                System.out.println(actual[idx]);
//                System.out.println(expected[idx]);
//            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Wrong files in Versioning View", expected.length, result);
            expected = new String[]{"Locally Deleted", "Locally Deleted", "Locally Deleted", "Locally New", "Locally Copied", "Locally New", "Locally Copied", "Locally New", "Locally Copied"};
            actual = new String[vo.tabFiles().getRowCount()];
            for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
                actual[i] = vo.tabFiles().getValueAt(i, 1).toString().trim();
            }
            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Wrong status in Versioning View", expected.length, result);
            Exception e = null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "a|b|BClass.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("Unexpected behavior - File shouldn't be in explorer!!!", e);
            e = null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp.a.b|BClass.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("Unexpected behavior - File should be in explorer!!!", e);
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}