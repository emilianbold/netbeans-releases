/*
 * CreateFromEar.java
 *
 * Created on June 5, 2006, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.archive.xtests;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.archive.project.ArchiveProjectProperties;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author vbk
 */
public class CreateProjectsTest extends NbTestCase {
    
    /** Creates a new instance of CreateFromEar */
    public CreateProjectsTest(String testName) {
        super(testName);
    }
    
    public void createFromOne4Ear() {
        TestUtil.createProjectFromArchive("jbearwebbean.ear",
                ArchiveProjectProperties.PROJECT_TYPE_VALUE_EAR,15000);
        File f = new File(System.getProperty("xtest.tmpdir") + File.separator + "jbearwebbean.ear");
        if (!f.exists())
            fail("directory missing: "+f.getAbsolutePath());
        Project p = null;
        try {
            p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        CustomizerProvider cp  = 
                (CustomizerProvider) p.getLookup().lookup(CustomizerProvider.class);
        //cp.
    }
    
    public void createFromBadOne4Ear() {
        TestUtil.createProjectFromArchive("BADcmpcustomer.ear",
                ArchiveProjectProperties.PROJECT_TYPE_VALUE_EAR,20000);
        File f = new File(System.getProperty("xtest.tmpdir") + File.separator + "BADcmpcustomer.ear");
        if (f.exists())
            fail("directory wasn't deleted: "+f.getAbsolutePath());
    }

    public void createFromFiveEar() {
        TestUtil.createProjectFromArchive("noddjavaee5.ear",
                ArchiveProjectProperties.PROJECT_TYPE_VALUE_EAR,15000);
        File f = new File(System.getProperty("xtest.tmpdir") + File.separator + "noddjavaee5.ear");
        if (!f.exists())
            fail("directory missing: "+f.getAbsolutePath());
    }

    public void createFromOne4EjbJar() {
        TestUtil.createProjectFromArchive("jbrejb14.jar",
                ArchiveProjectProperties.PROJECT_TYPE_VALUE_JAR,20000);
        File f = new File(System.getProperty("xtest.tmpdir") + File.separator + "jbrejb14.jar");
        if (!f.exists())
            fail("directory missing: "+f.getAbsolutePath());
        f =  new File(System.getProperty("xtest.tmpdir") + File.separator + "jbrejb14.jar" +
                File.separator + "tmpproj" + File.separator + "src" + File.separator +
                "conf" + File.separator + "sun-ejb-jar.xml");
        if (!f.exists())
            fail("descriptor missing: "+f.getAbsolutePath());
        if (f.length() < 300)
            fail("descriptor is too short: "+f.length());
            
    }

    public void createFromOne4War() {
        TestUtil.createProjectFromArchive("tcwa14.war",
                ArchiveProjectProperties.PROJECT_TYPE_VALUE_WAR,15000);
        File f = new File(System.getProperty("xtest.tmpdir") + File.separator + "tcwa14.war");
        if (!f.exists())
            fail("directory missing: "+f.getAbsolutePath());
        f =  new File(System.getProperty("xtest.tmpdir") + File.separator + "tcwa14.war" +
                File.separator + "tmpproj" + File.separator + "web" + File.separator +
                "WEB-INF" + File.separator + "sun-web.xml");
        if (!f.exists())
            fail("descriptor missing: "+f.getAbsolutePath());
        if (f.length() < 300)
            fail("descriptor is too short: "+f.length());            
    }

    public void createFromRar() {
        TestUtil.createProjectFromArchive("mailconnector.rar",
                ArchiveProjectProperties.PROJECT_TYPE_VALUE_RAR,15000);
        File f = new File(System.getProperty("xtest.tmpdir") + File.separator + "mailconnector.rar");
        if (!f.exists())
            fail("directory missing: "+f.getAbsolutePath());
    }    

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("CreateProjectsTest");
//XXX:  suite.addTest(new AddRemoveSjsasInstance4Test("addSjsasInstance"));
        suite.addTest(new CreateProjectsTest("createFromOne4Ear"));
        suite.addTest(new CreateProjectsTest("createFromBadOne4Ear"));
        suite.addTest(new CreateProjectsTest("createFromFiveEar"));
        suite.addTest(new CreateProjectsTest("createFromOne4EjbJar"));
        suite.addTest(new CreateProjectsTest("createFromOne4War"));
        suite.addTest(new CreateProjectsTest("createFromRar"));
        return suite;
    }
}
