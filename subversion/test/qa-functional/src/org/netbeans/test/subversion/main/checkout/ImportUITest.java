/*
 * ImportUITest.java
 *
 * Created on 10 May 2006, 15:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.checkout;

import java.io.File;
import javax.swing.table.TableModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.CreateNewFolderOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryBrowserImpOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class ImportUITest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    String os_name;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    long timeout_c;
    long timeout_d;

    /** Creates a new instance of ImportUITest */
    public ImportUITest(String name) {
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
        suite.addTest(new ImportUITest("testInvoke"));
        suite.addTest(new ImportUITest("testWarningMessage"));
        suite.addTest(new ImportUITest("testRepositoryFolderLoad"));
        suite.addTest(new ImportUITest("testCommitStep"));
        return suite;
    }

    public void testInvoke() throws Exception {

        timeout_c = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        } catch (TimeoutExpiredException e) {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout_c);
        }

        timeout_d = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        } catch (TimeoutExpiredException e) {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeout_d);
        }

        try {
            TestKit.closeProject(PROJECT_NAME);

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
            Operator.setDefaultStringComparator(comOperator);
            ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
            Operator.setDefaultStringComparator(oldOperator);
            iwo.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }

    public void testWarningMessage() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        try {
            TestKit.closeProject(PROJECT_NAME);

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
            Operator.setDefaultStringComparator(comOperator);
            ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();
            //rso.verify();
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
            rso.next();
            Thread.sleep(2000);

            FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
            ftiso.verify();

            //Warning message for empty REPOSITORY FOLDER
            ftiso.setRepositoryFolder("");
            assertEquals("Repository Folder must be specified", "Repository Folder must be specified", ftiso.lblImportMessageRequired().getText());
            assertFalse("Next button should be disabled", ftiso.btNext().isEnabled());
            assertFalse("Finish button should be disabled", ftiso.btFinish().isEnabled());

            //Warning message for empty import message
            ftiso.setRepositoryFolder(PROJECT_NAME);
            ftiso.setImportMessage("");
            assertEquals("Import message required", "Import Message required", ftiso.lblImportMessageRequired().getText());
            assertFalse("Next button should be disabled", ftiso.btNext().isEnabled());
            assertFalse("Finish button should be disabled", ftiso.btFinish().isEnabled());

            //NO Warning message if both are setup correctly.
            ftiso.setRepositoryFolder(PROJECT_NAME);
            ftiso.setImportMessage("initial import");
            assertEquals("No Warning message", " ", ftiso.lblImportMessageRequired().getText());
            assertTrue("Next button should be enabled", ftiso.btNext().isEnabled());
            //Finish button should be enabled.
            //System.out.println("Issue should be fixed: http://www.netbeans.org/issues/show_bug.cgi?id=76165!!!");
            assertTrue("Finish button should be enabled", ftiso.btFinish().isEnabled());
            iwo.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }

    public void testRepositoryFolderLoad() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        TestKit.closeProject(PROJECT_NAME);
        try {

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
            Operator.setDefaultStringComparator(comOperator);
            ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();
            //rso.verify();
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
            rso.next();
            Thread.sleep(2000);

            FolderToImportStepOperator ftiso = new FolderToImportStepOperator();

            //only required nodes are expended - want to see all in browser
            ftiso.setRepositoryFolder("");
            RepositoryBrowserImpOperator rbo = ftiso.browseRepository();
            rbo.selectFolder("branches");
            rbo.selectFolder("tags");
            rbo.selectFolder("trunk");
            rbo.selectFolder("trunk|JavaApp|src|javaapp");
            rbo.ok();
            assertEquals("Wrong folder selection!!!", "trunk/JavaApp/src/javaapp", ftiso.getRepositoryFolder());

            /*
            ftiso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            rbo = ftiso.browseRepository();
            rbo.selectFolder("trunk|" + PROJECT_NAME);
            rbo.selectFolder("branches|release01|" + PROJECT_NAME);
            rbo.ok();
            assertEquals("Wrong folder selection!!!", "branches/release01/" + PROJECT_NAME, ftiso.getRepositoryFolder());*/

            //
            ftiso.setRepositoryFolder("trunk");
            rbo = ftiso.browseRepository();
            rbo.selectFolder("trunk");
            CreateNewFolderOperator cnfo = rbo.createNewFolder();
            cnfo.setFolderName(PROJECT_NAME);
            cnfo.ok();
            rbo.selectFolder("trunk|" + PROJECT_NAME);
            rbo.ok();
            assertEquals("Wrong folder selection!!!", "trunk/" + PROJECT_NAME, ftiso.getRepositoryFolder());

            //
            ftiso.setRepositoryFolder("");
            rbo = ftiso.browseRepository();
            rbo.selectFolder("branches");
            cnfo = rbo.createNewFolder();
            cnfo.setFolderName("release_01");
            cnfo.ok();
            rbo.selectFolder("branches|release_01");
            cnfo = rbo.createNewFolder();
            cnfo.setFolderName(PROJECT_NAME);
            cnfo.ok();
            rbo.selectFolder("branches|release_01|" + PROJECT_NAME);
            rbo.ok();
            assertEquals("Wrong folder selection!!!", "branches/release_01/" + PROJECT_NAME, ftiso.getRepositoryFolder());

            iwo.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }

    public void testCommitStep() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        try {
            TestKit.closeProject(PROJECT_NAME);

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
            Operator.setDefaultStringComparator(comOperator);
            ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();
            //rso.verify();
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
            rso.next();
            Thread.sleep(1000);

            FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
            ftiso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            ftiso.setImportMessage("initial import");
            ftiso.next();
            Thread.sleep(1000);
            CommitStepOperator cso = new CommitStepOperator();
            cso.verify();

            JTableOperator table = cso.tabFiles();
            TableModel model = table.getModel();
            String[] expected = {"genfiles.properties", "build-impl.xml", "Main.java", "manifest.mf", "src", "project.xml", PROJECT_NAME.toLowerCase(), "nbproject", "project.properties", "test", "build.xml"};
            String[] actual = new String[model.getRowCount()];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = model.getValueAt(i, 0).toString();
            }
            assertEquals("Incorrect count of records for addition!!!", 11, model.getRowCount());
            assertEquals("Some records were omitted from addition", 11, TestKit.compareThem(expected, actual, false));
            //try to change commit actions
            cso.selectCommitAction("project.xml", "Add As Text");
            cso.selectCommitAction("project.xml", "Add As Binary");
            cso.selectCommitAction("project.xml", "Exclude from Commit");
            cso.selectCommitAction("test", "Add Directory");
            cso.selectCommitAction("test", "Exclude from Commit");
            iwo.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }

    public void testStopProcess() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        try {
            TestKit.closeProject(PROJECT_NAME);

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
            Operator.setDefaultStringComparator(comOperator);
            ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();
            //rso.verify();
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
            rso.next();
            //Stop process in 1st step of Import wizard
            rso.btStop().push();
            assertEquals("Warning message - process was cancelled by user", "Action canceled by user", rso.lblWarning().getText());
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
            rso = new RepositoryStepOperator();
            //rso.verify();
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
            rso.next();

            FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
            ftiso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            ftiso.setImportMessage("initial import");
            ftiso.next();
            //Stop process in 2st step of Import wizard
            ftiso.btStop().push();

            ftiso = new FolderToImportStepOperator();
            //ftiso.verify();
            ftiso.back();

            rso = new RepositoryStepOperator();
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
            rso = new RepositoryStepOperator();
            rso.verify();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout_c);
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeout_d);
        }
    }
}
