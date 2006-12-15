/*
 * CreateProjectVersionedDirTest.java
 *
 * Created on Pondelok, 2006, september 25, 10:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.checkout;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class CreateProjectVersionedDirTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    String os_name;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of CreateProjectVersionedDirTest */
    public CreateProjectVersionedDirTest(String name) {
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
        suite.addTest(new CreateProjectVersionedDirTest("testCreateNewProject"));
        return suite;
    }
    
    public void testCreateNewProject() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        TestKit.closeProject(PROJECT_NAME);
        OutputTabOperator oto;
        OutputOperator oo = OutputOperator.invoke();
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
        oto = new OutputTabOperator("file:///tmp/repo");
        oto.clear();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
        wdso.setLocalFolder(work.getCanonicalPath());
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();
        //open project
        oto.waitText("Checking out... finished.");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        ProjectSupport.waitScanFinished();
        
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("General");
        npwo.selectProject("Java Application");
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        new JTextFieldOperator(npnlso, 1).setText(work.getAbsolutePath() + File.separator + PROJECT_NAME + File.separator+ "src"); // NOI18N
        new JTextFieldOperator(npnlso, 0).setText(PROJECT_NAME); // NOI18N
        //new JTextFieldOperator(npnlso, 2).setText(folder); // NOI18N
        new NewProjectWizardOperator().finish();
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
        
        TestKit.closeProject(PROJECT_NAME);
    }
}
