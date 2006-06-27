/*
 * CheckoutContentTest.java
 *
 * Created on 26 May 2006, 20:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.checkout;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class CheckoutContentTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    String os_name;
    
    /**
     * Creates a new instance of CheckoutContentTest
     */
    public CheckoutContentTest(String name) {
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
        suite.addTest(new CheckoutContentTest("testCheckoutProject"));      
        suite.addTest(new CheckoutContentTest("testCheckoutContent"));      
        return suite;
    }
    
    public void testCheckoutProject() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator rso = new RepositoryStepOperator();       
        
        //create repository... 
        new File(TMP_PATH).mkdirs();
        new File(TMP_PATH + File.separator + WORK_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        rso.next();
        OutputTabOperator oto = new OutputTabOperator("file:///tmp");
        oto.clear();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/JavaApp");
        wdso.setLocalFolder(TMP_PATH + File.separator + WORK_PATH);
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();
        //open project
        oto.waitText("Checking out... finished.");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        ProjectSupport.waitScanFinished();
        Node projNode = new Node(new ProjectsTabOperator().tree(), "JavaApp");
        TestKit.removeAllData("JavaApp"); 
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
    }
    
    public void testCheckoutContent() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator rso = new RepositoryStepOperator();
        
        //create repository... 
        new File(TMP_PATH).mkdirs();
        new File(TMP_PATH + File.separator + WORK_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        rso.next();
        OutputTabOperator oto = new OutputTabOperator("file:///tmp");
        oto.clear();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/JavaApp/src");
        wdso.checkCheckoutContentOnly(true);
        wdso.setLocalFolder(TMP_PATH + File.separator + WORK_PATH);
        wdso.finish();
        //open project
        oto.waitText("Checking out... finished.");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator create = new JButtonOperator(nbdialog, "Create Project...");
        JButtonOperator close = new JButtonOperator(nbdialog, "Close");
        close.push();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
        //ProjectSupport.waitScanFinished();
        //Node projNode = new Node(new ProjectsTabOperator().tree(), "JavaApp");
    }
}
