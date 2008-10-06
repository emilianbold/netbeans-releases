/*
 * SwitchUiTest.java
 *
 * Created on 17 May 2006, 22:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.branches;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryBrowserOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.SwitchOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class SwitchUiTest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    static Logger log;
    
    String os_name;
    
    /** Creates a new instance of SwitchUiTest */
    public SwitchUiTest(String name) {
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
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(SwitchUiTest.class).addTest(
                    "testInvokeCloseSwitch"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void testInvokeCloseSwitch() throws Exception {
        try {
            MessageHandler mh = new MessageHandler("Committing");
            log.addHandler(mh);

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);
            
            ImportWizardOperator.invoke(ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME));
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
            
            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            Thread.sleep(2000);
            SwitchOperator so = SwitchOperator.invoke(projNode);
            Thread.sleep(2000);
            //only required nodes are expended - want to see all in browser
            so.setRepositoryFolder("");
            RepositoryBrowserOperator rbo = so.browseRepositoryFolder();
            so.verify();
            rbo.selectFolder("tags");
            rbo.selectFolder("trunk");
            rbo.selectFolder("branches");
            rbo.ok();
            assertEquals("Folder wasn't created", "branches", so.getRepositoryFolder());
            so.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
}
