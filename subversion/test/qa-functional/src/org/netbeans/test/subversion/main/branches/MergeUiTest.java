/*
 * MergeUiTest.java
 *
 * Created on 16 May 2006, 16:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.branches;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.MergeOneRepoOperator;
import org.netbeans.test.subversion.operators.MergeOperator;
import org.netbeans.test.subversion.operators.MergeOriginOperator;
import org.netbeans.test.subversion.operators.MergeTwoRepoOperator;
import org.netbeans.test.subversion.operators.RepositoryBrowserOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class MergeUiTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    String os_name;

    /** Creates a new instance of MergeUiTest */
    public MergeUiTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        System.out.println("### " + getName() + " ###");
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
                 NbModuleSuite.createConfiguration(MergeUiTest.class).addTest(
                    "testInvokeCloseMerge"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }

    public void testInvokeCloseMerge() throws Exception {
        try {
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

            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            MergeOperator mo = MergeOperator.invoke(projNode);

            Thread.sleep(3000);
            //0. one repository operator
            mo.cboMergeFrom().selectItem(0);
            MergeOneRepoOperator moro = new MergeOneRepoOperator();
            //moro.verify();
            RepositoryBrowserOperator rbo = moro.browseRepository();
            //moro.selectRepositoryFolder("ok");
            rbo.selectFolder("trunk");
            rbo.selectFolder("branches");
            rbo.selectFolder("tags");
            rbo.ok();
            assertEquals("Wrong folder selection!!!", "tags", moro.getRepositoryFolder());
            moro.setRepositoryFolder("");
            //1. two repository operator
            moro.cboMergeFrom().selectItem(2);
            MergeTwoRepoOperator mtro = new MergeTwoRepoOperator();
            //mtro.verify();
            rbo = mtro.browseRepositoryFolder1();
            rbo.selectFolder("trunk");
            rbo.selectFolder("branches");
            rbo.selectFolder("tags");
            rbo.ok();
            assertEquals("Wrong folder selection!!!", "tags", mtro.getRepositoryFolder1());
            mtro.setRepositoryFolder1("");
            rbo = mtro.browseRepositoryFolder2();
            rbo.selectFolder("tags");
            rbo.selectFolder("branches");
            rbo.selectFolder("trunk");
            rbo.ok();
            assertEquals("Wrong folder selection!!!", "trunk", mtro.getRepositoryFolder2());
            mtro.setRepositoryFolder2("");

            //2. two repository operator
            moro.cboMergeFrom().selectItem(1);
            MergeOriginOperator moo = new MergeOriginOperator();
            //moo.verify();
            rbo = moo.browseRepositoryFolder();
            rbo.selectFolder("trunk");
            rbo.selectFolder("branches");
            rbo.selectFolder("tags");
            rbo.ok();
            assertEquals("Wrong folder selection!!!", "tags", moo.getRepositoryFolder());
            moo.cancel();
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}