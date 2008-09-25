/*
 * CopyUiTest.java
 *
 * Created on 16 May 2006, 10:32
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
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.CopyToOperator;
import org.netbeans.test.subversion.operators.CreateNewFolderOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryBrowserImpOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class CopyUiTest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    static Logger log;
    
    String os_name;
    
    /** Creates a new instance of CopyUiTest */
    public CopyUiTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {        
        System.out.println("### "+getName()+" ###");
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
                 NbModuleSuite.createConfiguration(CopyUiTest.class).addTest(
                    "testInvokeCloseCopy"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void testInvokeCloseCopy() throws Exception{
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
            CopyToOperator cto = CopyToOperator.invoke(projNode);
            cto.verify();
            //only required nodes are expended - want to see all in browser
            cto.setRepositoryFolder("");
            RepositoryBrowserImpOperator rbio = cto.browseRepository();
            rbio.verify();
            rbio.selectFolder("tags");
            rbio.selectFolder("trunk");
            rbio.selectFolder("branches");
            CreateNewFolderOperator cnfo = rbio.createNewFolder();
            cnfo.setFolderName("release01-" + PROJECT_NAME);
            cnfo.cancel();
            //Creation of new folder was canceled - no new folder can't be created
            TimeoutExpiredException tee = null;
            try {
                rbio.selectFolder("branches|release01-" + PROJECT_NAME);
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull(tee);

            rbio.selectFolder("branches");
            cnfo = rbio.createNewFolder();
            cnfo.setFolderName("release01-" + PROJECT_NAME);
            cnfo.ok();
            rbio.selectFolder("branches|release01-" + PROJECT_NAME);
            rbio.ok();
            assertEquals("New folder for copy purpose wasn't created", "branches/release01-" + PROJECT_NAME, cto.getRepositoryFolder());
            cto.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME); 
        }    
    }
}
