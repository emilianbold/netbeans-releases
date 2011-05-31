/*
 * RevertUiTest.java
 *
 * Created on 18 May 2006, 17:22
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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.RevertModificationsOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class RevertUiTest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    static Logger log;
    
    /** Creates a new instance of RevertUiTest */
    public RevertUiTest(String name) {
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
                 NbModuleSuite.createConfiguration(RevertUiTest.class).addTest(
                    "testInvokeCloseRevert"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void testInvokeCloseRevert() throws Exception {
        try {
            MessageHandler mh = new MessageHandler("Checking out");
            log.addHandler(mh);
            TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            CheckoutWizardOperator.invoke();
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();

            //create repository...
            File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
            new File(TMP_PATH).mkdirs();
            work.mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

            rso.next();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);

            wdso.finish();
            TestKit.waitText(mh);

            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            TestKit.waitForScanFinishedSimple();

            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            RevertModificationsOperator rmo = RevertModificationsOperator.invoke(projNode);
            rmo.verify();
            TimeoutExpiredException tee = null;
            try {
                rmo.setStartRevision("1");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);
            tee = null;
            try {
                rmo.setEndRevision("1");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);
            tee = null;
            try {
                rmo.setEndRevision("1");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);

            rmo.rbPreviousCommits().push();
            rmo.setStartRevision("1");
            rmo.setEndRevision("2");

            tee = null;
            try {
                rmo.setRevision("3");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);

            rmo.rbSingleCommit().push();
            rmo.setRevision("3");

            tee = null;
            try {
                rmo.setStartRevision("1");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);

            tee = null;
            try {
                rmo.setEndRevision("2");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);

            rmo.cancel();
            TestKit.TIME_OUT = 15;
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        } 
        
    }
}
