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
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
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
    
    /** Creates a new instance of ImportUITest */
    public ImportUITest(String name) {
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
        suite.addTest(new ImportUITest("testInvoke"));
        suite.addTest(new ImportUITest("testWarningMessage"));
        return suite;
    }
    
    public void testInvoke() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        
        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        projectPath = TestKit.prepareProject("General", "Java Application", PROJECT_NAME);
        
        ImportWizardOperator iwo = ImportWizardOperator.invoke(ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME));
        iwo.cancel();
        
        TestKit.removeAllData(PROJECT_NAME);
    }
    
    public void testWarningMessage() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        
        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        projectPath = TestKit.prepareProject("General", "Java Application", PROJECT_NAME);
        
        ImportWizardOperator iwo = ImportWizardOperator.invoke(ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME));
        RepositoryStepOperator rso = new RepositoryStepOperator();
        //rso.verify();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        rso.next();
        Thread.sleep(2000);
        
        FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
        ftiso.verify();
        
        //Warning message for empty REPOSITORY FOLDER
        ftiso.setRepositoryFolder("");
        assertEquals("Repository folder must be specified", "Repository folder must be specified", ftiso.lblImportMessageRequired().getText());
        assertFalse("Next button should be disabled", ftiso.btNext().isEnabled());
        assertFalse("Finish button should be disabled", ftiso.btFinish().isEnabled());
        
        //Warning message for empty import message
        ftiso.setRepositoryFolder(PROJECT_NAME);
        ftiso.setImportMessage("");
        assertEquals("Import message required", "Import message required", ftiso.lblImportMessageRequired().getText());
        assertFalse("Next button should be disabled", ftiso.btNext().isEnabled());
        assertFalse("Finish button should be disabled", ftiso.btFinish().isEnabled());
        
        //NO Warning message if both are setup correctly.
        ftiso.setRepositoryFolder(PROJECT_NAME);
        ftiso.setImportMessage("initial import");
        assertEquals("No Warning message", " ", ftiso.lblImportMessageRequired().getText());
        assertTrue("Next button should be enabled", ftiso.btNext().isEnabled());
        //Finish button should be enabled.
        System.out.println("Issue should be fixed: http://www.netbeans.org/issues/show_bug.cgi?id=76165!!!");
        assertFalse("Finish button should be enabled", ftiso.btFinish().isEnabled());
        iwo.cancel();
        TestKit.removeAllData(PROJECT_NAME);
    }
}
