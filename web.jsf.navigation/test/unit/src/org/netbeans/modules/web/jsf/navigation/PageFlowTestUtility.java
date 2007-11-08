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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.navigation;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JEditorPane;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.text.EditorKit;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigEditorSupport;
import org.netbeans.modules.web.jsf.JSFConfigLoader;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author joelle
 */
public class PageFlowTestUtility {

    private Project project;
    private JSFConfigDataObject jsfDO;
    private FacesConfig facesConfig;
    private PageFlowView pageFlowView;
    private PageFlowScene scene;
    private PageFlowController controller;
    NbTestCase nbTestCase;

    public PageFlowTestUtility(NbTestCase nbTestCase) {
        this.nbTestCase = nbTestCase;
    }

    public void setUp(final String zipPath, final String projectNameZip) throws Exception {
        nbTestCase.clearWorkDir();
        setupServices();
        setProject(openProject(zipPath, projectNameZip));
        setJsfDO(initDOFacesConfig());
        setFacesConfig(initFacesConfig());
        setPageFlowView(initPageFlowView());
        setScene(getPageFlowView().getScene());
        setController(getPageFlowView().getPageFlowController());
    }

    public void tearDown() throws Exception {
        destroyProject();
        
    }

    private void destroyProject() throws IOException {
        saveFacesModel();
        closeFacesModel(); //This gets rid of all the views.. Not sure though why closing the project does not call componentClosing.
        closeProject(project);
    }

    public File getWorkDir() throws IOException {
        return nbTestCase.getWorkDir();
    }

    public String getWorkDirPath() {
        return nbTestCase.getWorkDirPath();
    }

    private Project openProject(String zipPath, String projectName) throws IOException {
        assertNotNull(zipPath);
        File archiveFile = new File(zipPath);

        FileObject destFileObj = FileUtil.toFileObject(getWorkDir());
        unZipFile(archiveFile, destFileObj);
        assertTrue(destFileObj.isValid());
        FileObject testApp = destFileObj.getFileObject(projectName);

        assertNotNull(testApp);
        //System.out.println("Children of " + projectName + ":" + Arrays.toString(testApp.getChildren()));
        //        assertTrue( ProjectManager.getDefault().isProject(testApp));
        Project myProject = ProjectManager.getDefault().findProject(testApp);
        assertNotNull(myProject);
        setProject(myProject);
        OpenProjects.getDefault().open(new Project[]{myProject}, false);
        return myProject;
    }

    private void closeProject(Project myProject) throws IOException {
        //        assertNotNull(jsfDO);
//        ((CloseCookie)jsfDO.getCookie(CloseCookie.class)).close();
        assertNotNull(myProject);
        OpenProjects.getDefault().close(new Project[]{myProject});
    }
    JSFConfigEditorSupport editorSupport;

    protected void closeFacesModel() throws IOException {
        if (editorSupport != null) {
            editorSupport.close();
        }
    }
    
    protected void saveFacesModel() throws IOException {
        if (editorSupport != null) {
            editorSupport.saveDocument();
        }
    }

    protected void setupServices() {

        if (nbTestCase instanceof TestServices) {
            ((TestServices) nbTestCase).setupServices();

        } else {
            ClassLoader l = this.getClass().getClassLoader();
            MockLookup.setLookup(Lookups.fixed(l), Lookups.metaInfServices(l));
        }
    }
    //    JSFConfigLoader loader;

    private static JSFConfigLoader loader = new JSFConfigLoader();

    private JSFConfigDataObject initDOFacesConfig() throws IOException {
        assertTrue(project instanceof WebProject);
        WebModule webModule = ((WebProject) project).getAPIWebModule();
        assertNotNull(webModule);
        FileObject[] facesConfigFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
        assertTrue(facesConfigFiles.length == 1);

        FileObject facesConfigFile = facesConfigFiles[0];
        assertNotNull(facesConfigFile);


        DataLoader dl = DataLoaderPool.getPreferredLoader(facesConfigFile);

        if (dl == null || !(dl instanceof JSFConfigLoader)) {

            if (loader != null) {
                DataLoaderPool.setPreferredLoader(facesConfigFile, loader);
            }
        }

        DataObject dataObj = DataObject.find(facesConfigFile);
        assertNotNull(dataObj);
        assertTrue(dataObj instanceof JSFConfigDataObject);
        jsfDO = (JSFConfigDataObject) dataObj;
        assertNotNull(getJsfDO());
        return getJsfDO();
    }

    private FacesConfig initFacesConfig() throws IOException, InterruptedException {
        assertNotNull(getJsfDO());
        editorSupport = (JSFConfigEditorSupport) getJsfDO().createCookie(JSFConfigEditorSupport.class);
        assertNotNull(editorSupport);
        Lookup lookup = getJsfDO().getLookup();
        assertNotNull(lookup);
        Util.registerXMLKit();
        EditorKit kit = JSFConfigEditorSupport.getEditorKit("text/x-jsf+xml");
        assert (kit instanceof XMLKit);
        editorSupport.edit();
       
        JSFConfigModel model = ConfigurationUtils.getConfigModel(getJsfDO().getPrimaryFile(), true);
        assertNotNull(model);
        FacesConfig myFacesConfig = model.getRootComponent();
        assertNotNull(myFacesConfig);
        return myFacesConfig;
    }

    private PageFlowView initPageFlowView() {
        Set<PageFlowView> views = PageFlowToolbarUtilities.getViews();
        assertTrue(views.size() >0);
        
        PageFlowView myPageFlowView = null;
        for (PageFlowView view : views) {
            myPageFlowView = view;
            break;
        }
        assertNotNull(myPageFlowView);
        return myPageFlowView;
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

    public JSFConfigDataObject getJsfDO() {
        return jsfDO;
    }

    public void setJsfDO(JSFConfigDataObject jsfDO) {
        this.jsfDO = jsfDO;
    }

    public PageFlowView getPageFlowView() {
        return pageFlowView;
    }

    public void setPageFlowView(PageFlowView pfv) {
        pageFlowView = pfv;
    }

    public FacesConfig getFacesConfig() {
        return facesConfig;
    }

    public void setFacesConfig(FacesConfig fc) {
        facesConfig = fc;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project p) {
        project = p;
    }

    public void assertNotNull(Object obj) {
        NbTestCase.assertNotNull(obj);
    }

    public void assertTrue(boolean b) {
        NbTestCase.assertTrue(b);
    }

    public PageFlowScene getScene() {
        return scene;
    }

    public void setScene(PageFlowScene scene) {
        this.scene = scene;
    }

    public PageFlowController getController() {
        return controller;
    }

    public void setController(PageFlowController controller) {
        this.controller = controller;
    }
}
