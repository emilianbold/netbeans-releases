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
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.CopyToOperator;
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
public class CopyUiTest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    
    String os_name;
    
    /** Creates a new instance of CopyUiTest */
    public CopyUiTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
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
        suite.addTest(new CopyUiTest("testInvokeCloseCopy")); 
        return suite;
    }
    
    public void testInvokeCloseCopy() throws Exception{
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        try {
            TestKit.closeProject(PROJECT_NAME);

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
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
            
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.waitText("Committed revision 7");
            
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
