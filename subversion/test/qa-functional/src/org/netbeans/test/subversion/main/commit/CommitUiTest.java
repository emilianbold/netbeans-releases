/*
 * CommitUiTest.java
 *
 * Created on 15 May 2006, 16:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.commit;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.test.subversion.operators.CommitOperator;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;
import org.openide.filesystems.FileObject;

/**
 *
 * @author peter
 */
public class CommitUiTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;

    static Logger log;

    /** Creates a new instance of CheckoutUITest */
    public CommitUiTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
        if (log == null) {
            log = Logger.getLogger(TestKit.LOGGER_NAME);
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(CommitUiTest.class).addTest(
                    "testInvokeCloseCommit"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }

    public void testInvokeCloseCommit() throws Exception {
        try {
            MessageHandler mh = new MessageHandler("Committing");
            log.addHandler(mh);

            TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                new NewProjectWizardOperator().invoke().close();

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));

            System.out.println(TMP_PATH + File.separator + REPO_PATH +"  ,  "+ getDataDir().getCanonicalPath() + File.separator + "repo_dump");


            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            ImportWizardOperator iwo = ImportWizardOperator.invoke(ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME));
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
            cso.finish();

            TestKit.waitText(mh);

            TestKit.createNewElements(PROJECT_NAME, "xx", "NewClass");
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass2");
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass3");
            Node packNode = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            CommitOperator co = CommitOperator.invoke(packNode);

            co.selectCommitAction("NewClass.java", "Add As Text");
            co.selectCommitAction("NewClass.java", "Add As Binary");
            co.selectCommitAction("NewClass.java", "Exclude from Commit");
            co.selectCommitAction(2, "Add As Text");
//            co.selectCommitAction(2, "Add As Binary");
            co.selectCommitAction(2, "Exclude from Commit");

            JTableOperator table = co.tabFiles();
            TableModel model = table.getModel();
            String[] expected = {"xx", "NewClass.java", "NewClass2.java", "NewClass3.java"};
            String[] actual = new String[model.getRowCount()];
            for (int i = 0; i < model.getRowCount(); i++) {
                actual[i] = model.getValueAt(i, 0).toString();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Commit table doesn't contain all files!!!", expected.length, result);

            co.verify();
            co.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
