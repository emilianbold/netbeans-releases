/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.navigation;

/*
 * VWPContentModelProviderTest.java
 * JUnit 4.x based test
 *
 * Created on July 30, 2007, 6:55 PM
 */



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.text.BadLocationException;
import junit.framework.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModel;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.visualweb.j2ee15classloaderprovider.J2EE15CommonClassloaderProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;



/**
 *
 * @author joelle
 */
public class VWPContentModelProviderTest extends NbTestCase {

    public VWPContentModelProviderTest() {
        super("VWPContentModelProviderTest");
    }

    //test methods -----------

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        //ClassLoader l = this.getClass().getClassLoader();
        MockServices.setServices(VWPContentModelProvider.class, MockOpenProjectsTrampoline.class, J2EE15CommonClassloaderProvider.class);
        //Lookup defaultLookup = Lookup.getDefault();
        //MockLookup.setLookup(Lookups.fixed(l), Lookups.metaInfServices(l));
        

        openProject();
    }

    Project project;
    private static final String appName = "VWPApplication";

    public Project openProject() throws IOException {
        String zipResource = appName + ".zip";
        String zipPath = VWPContentModelProviderTest.class.getResource(zipResource).getPath();
        assertNotNull(zipPath);
        File archiveFile = new File(zipPath);

        //        FileObject destFileObj = TestUtil.makeScratchDir(this);
        FileObject destFileObj = FileUtil.toFileObject(getWorkDir());
        unZipFile(archiveFile, destFileObj);
        assertTrue(destFileObj.isValid());
        FileObject testApp = destFileObj.getFileObject(appName);
        System.out.println("Children of " + appName + ":" + Arrays.toString(testApp.getChildren()));
        //        assertTrue( ProjectManager.getDefault().isProject(testApp));
        project = ProjectManager.getDefault().findProject(testApp);
        assertNotNull(project);
        OpenProjects.getDefault().open(new Project[]{project}, false);
        return project;
    }


    @Override
    protected void tearDown() throws Exception {
        System.out.println("Tearing Down The Project");
        destroyProject();
    }

    public void destroyProject() throws IOException {
        System.out.println("Deleting Project");
        project.getProjectDirectory().delete();
        System.out.println("Clearing Work Directory");
        clearWorkDir();
    }

    //test methods -----------
    public void testPageContentModelProviderExists() throws BadLocationException, IOException {
        System.out.println("Testing: getPageContentModel");
        FileObject fileObject = null;
        //VWPContentModelProvider instance = new VWPContentModelProvider();
        //PageContentModel expResult = null;
        //PageContentModel result = instance.getPageContentModel(fileObject);
        //assertEquals(expResult, result);
        //FileObject fileObject = ((DataNode)original).getDataObject().getPrimaryFile();
        Lookup.Template<PageContentModelProvider> templ = new Lookup.Template<PageContentModelProvider>(PageContentModelProvider.class);
        final Lookup.Result<PageContentModelProvider> result = Lookup.getDefault().lookup(templ);
        Collection<? extends PageContentModelProvider> impls = result.allInstances();
        assertTrue(impls.size() > 0);
        assertTrue(impls.toArray()[0] instanceof VWPContentModelProvider);

        assertNotNull(project);
        FileObject page1 = project.getProjectDirectory().getFileObject("web/Page1.jsp");
        assertNotNull(page1);
        fileObject = page1;

        try {
            FacesModelSet modelSet = new FacesModelSet(project);
            assertNotNull(modelSet);
            for (PageContentModelProvider provider : impls) {
                PageContentModel pageContentModel = provider.getPageContentModel(fileObject);
                //exit when you find one.
                if (pageContentModel != null) {
                    return;
                }
            }
        } catch (ExceptionInInitializerError eie) {
            Exception e = (Exception) eie.getException();
            e.printStackTrace();
        }
    }




    private static void unZipFile(File archiveFile, FileObject destDir) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFile);
        try {
            ZipInputStream str = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(destDir, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(destDir, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            fis.close();
        }
    }
}
