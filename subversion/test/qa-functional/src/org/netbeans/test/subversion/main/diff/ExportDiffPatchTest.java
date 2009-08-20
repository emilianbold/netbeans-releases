/*
 * ExportDiffPatchTest.java
 *
 * Created on Piatok, 2006, september 15, 10:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class ExportDiffPatchTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    static Logger log;
    
    /** Creates a new instance of ExportDiffPatchTest */
    public ExportDiffPatchTest(String name) {
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
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(ExportDiffPatchTest.class).addTest(
                    "invokeExportDiffPatch"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void invokeExportDiffPatch() throws Exception {
        try {
            MessageHandler mh = new MessageHandler("Checking out");
            log.addHandler(mh);

            TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                new NewProjectWizardOperator().invoke().close();
            
            TestKit.TIME_OUT = 25;
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            VersioningOperator vo = VersioningOperator.invoke();
            TestKit.showStatusLabels();
            CheckoutWizardOperator.invoke();
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
            //open project

            TestKit.waitText(mh);

            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            TestKit.waitForScanFinishedAndQueueEmpty();
            
            //modify, save file and invoke Diff

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Open");
            EditorOperator eo = new EditorOperator("Main.java");
            eo.deleteLine(2);
            eo.insert(" insert", 5, 1);
            eo.insert("\tSystem.out.println(\"\");\n", 19, 1);
            eo.save();

            mh = new MessageHandler("Refreshing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node.performPopupAction("Subversion|Show Changes");
            TestKit.waitText(mh);
            Thread.sleep(1000);
            vo = VersioningOperator.invoke();
            //Save action should change the file annotations
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be new!!!", TestKit.MODIFIED_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.MODIFIED_STATUS, status);
            assertEquals("Wrong number of records in Versioning view!!!", 1, vo.tabFiles().getRowCount());
            
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            //comOperator = new Operator.DefaultStringComparator(true, true);
            //oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            //Operator.setDefaultStringComparator(comOperator);
            new EventTool().waitNoEvent(3000);

            mh = new MessageHandler("Exporting");
            TestKit.removeHandlers(log);
            log.addHandler(mh);
            node.performMenuActionNoBlock("Team|Subversion|Export Diff Patch...");
            //Operator.setDefaultStringComparator(oldOperator);
            
            nbdialog = new NbDialogOperator("Export Diff Patch");
            JButtonOperator btn = new JButtonOperator(nbdialog, "OK");


            JRadioButtonOperator rbtno = new JRadioButtonOperator(nbdialog, "Save as File");
            rbtno.push();

            JTextFieldOperator tf = new JTextFieldOperator(nbdialog, 2);

            String patchFile = "/tmp/patch" + System.currentTimeMillis() + ".patch";
            File file = new File(patchFile);
            tf.setText(file.getCanonicalFile().toString()); 

            btn.push();

            TestKit.waitText(mh);
            new EventTool().waitNoEvent(3000);


            System.out.println("After refresh");

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            boolean generated = false;
            if (line != null) {
                generated = line.indexOf("# This patch file was generated by NetBeans IDE") != -1 ? true : false;
            }
            
            br.close();
            assertTrue("Diff Patch file is empty!", generated);
            System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
            TestKit.TIME_OUT = 15;
            stream.flush();
            stream.close();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
    
}
