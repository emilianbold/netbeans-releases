/*
 * SearchHistoryUITest.java
 *
 * Created on 14 June 2006, 15:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.archeology;

import java.io.File;
import java.io.PrintStream;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.SearchHistoryOperator;
import org.netbeans.test.subversion.operators.SearchRevisionsOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;
import org.netbeans.junit.ide.ProjectSupport;
import junit.textui.TestRunner;

/**
 *
 * @author peter
 */
public class SearchHistoryUITest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    
    /** Creates a new instance of SearchHistoryUITest */
    public SearchHistoryUITest(String name) {
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
        suite.addTest(new SearchHistoryUITest("testInvokeSearch"));
        
        return suite;
    }
    
    public void testInvokeSearch() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
        VersioningOperator vo = VersioningOperator.invoke();
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
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/JavaApp");
        wdso.setLocalFolder(TMP_PATH + File.separator + WORK_PATH);
        wdso.checkCheckoutContentOnly(false);
        OutputTabOperator oto = new OutputTabOperator("file:///tmp");
        oto.clear();
        wdso.finish();
        //open project
        oto.waitText("Checking out... finished.");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        ProjectSupport.waitScanFinished();
        
        oto = new OutputTabOperator("file:///tmp");
        oto.clear();
        Node node = new Node(new SourcePackagesNode("JavaApp"), "javaapp|Main.java");
        SearchHistoryOperator sho = SearchHistoryOperator.invoke(node);
        oto.waitText("Searching History... finished.");
        sho.verify();
        oto = new OutputTabOperator("file:///tmp");
        oto.clear();
        SearchRevisionsOperator sro = sho.getRevisionFrom();
        oto.waitText("Searching revisions finished.");
        sro.verify();
        sro.cancel();
        
        oto = new OutputTabOperator("file:///tmp");
        oto.clear();
        sro = sho.getRevisionTo();
        oto.waitText("Searching revisions finished.");
        sro.verify();
        sro.cancel();
        
        sho.setUsername("test");
        sho.setFrom("1");
        sho.setTo("2");
        //sho.btSearch().push();
        
        TestKit.removeAllData("JavaApp");
        stream.flush();
        stream.close();
    }
}
