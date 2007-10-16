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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.insync.models;

import junit.framework.*;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.visualweb.insync.InsyncTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sc32560
 */
public class FacesModelSetTest extends InsyncTestBase {
    public FacesModelSetTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(FacesModelSetTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }   
    
    //    /**
//     * Test of startModeling method, of class FacesModelSet.
//     */
//    public void testStartModeling() {
//        System.out.println("startModeling");
//        FileObject file = null;
//        FacesModelSet expResult = null;
//        FacesModelSet result = FacesModelSet.startModeling(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of getInstance method, of class FacesModelSet.
     */

    public void testGetInstance() {
        System.out.println("getInstance");
        FileObject file = getProject().getProjectDirectory();
        FacesModelSet expResult = null;
        FacesModelSet result = FacesModelSet.getInstance(file);
        assertNotNull(result);
        assert (result.getModels().length == 4);
    }
    
    //
//    /**
//     * Test of getFacesModelIfAvailable method, of class FacesModelSet.
//     */
//    public void testGetFacesModelIfAvailable() {
//        System.out.println("getFacesModelIfAvailable");
//        FileObject fileObject = null;
//        FacesModel expResult = null;
//        FacesModel result = FacesModelSet.getFacesModelIfAvailable(fileObject);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of destroy method, of class FacesModelSet.
//     */
//    public void testDestroy() {
//        System.out.println("destroy");
//        FacesModelSet instance = null;
//        instance.destroy();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getFacesContainer method, of class FacesModelSet.
//     */
//    public void testGetFacesContainer() {
//        System.out.println("getFacesContainer");
//        FacesModelSet instance = null;
//        FacesContainer expResult = null;
//        FacesContainer result = instance.getFacesContainer();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getFacesConfigModel method, of class FacesModelSet.
//     */
//    public void testGetFacesConfigModel() {
//        System.out.println("getFacesConfigModel");
//        FacesModelSet instance = null;
//        FacesConfigModel expResult = null;
//        FacesConfigModel result = instance.getFacesConfigModel();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getFacesModels method, of class FacesModelSet.
//     */
//    public void testGetFacesModels() {
//        System.out.println("getFacesModels");
//        FacesModelSet instance = null;
//        FacesModel[] expResult = null;
//        FacesModel[] result = instance.getFacesModels();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getModel method, of class FacesModelSet.
//     */
//    public void testGetModel() {
//        System.out.println("getModel");
//        FileObject file = null;
//        FacesModelSet instance = null;
//        Model expResult = null;
//        Model result = instance.getModel(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getFacesModel method, of class FacesModelSet.
//     */
//    public void testGetFacesModel() {
//        System.out.println("getFacesModel");
//        FileObject file = null;
//        FacesModelSet instance = null;
//        FacesModel expResult = null;
//        FacesModel result = instance.getFacesModel(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateBeanElReferences method, of class FacesModelSet.
//     */
//    public void testUpdateBeanElReferences() {
//        System.out.println("updateBeanElReferences");
//        String oldname = "";
//        String newname = "";
//        FacesModelSet instance = null;
//        instance.updateBeanElReferences(oldname, newname);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeBeanElReferences method, of class FacesModelSet.
//     */
//    public void testRemoveBeanElReferences() {
//        System.out.println("removeBeanElReferences");
//        String oldname = "";
//        FacesModelSet instance = null;
//        instance.removeBeanElReferences(oldname);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBeanNamesToXRef method, of class FacesModelSet.
//     */
//    public void testGetBeanNamesToXRef() {
//        System.out.println("getBeanNamesToXRef");
//        Scope scope = null;
//        FacesModel facesModel = null;
//        FacesModelSet instance = null;
//        Collection expResult = null;
//        Collection result = instance.getBeanNamesToXRef(scope, facesModel);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addPropertyChangeListener method, of class FacesModelSet.
//     */
//    public void testAddPropertyChangeListener() {
//        System.out.println("addPropertyChangeListener");
//        PropertyChangeListener propChangeListener = null;
//        FacesModelSet instance = null;
//        instance.addPropertyChangeListener(propChangeListener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removePropertyChangeListener method, of class FacesModelSet.
//     */
//    public void testRemovePropertyChangeListener() {
//        System.out.println("removePropertyChangeListener");
//        PropertyChangeListener propChangeListener = null;
//        FacesModelSet instance = null;
//        instance.removePropertyChangeListener(propChangeListener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getContextClassLoader method, of class FacesModelSet.
//     */
//    public void testGetContextClassLoader() {
//        System.out.println("getContextClassLoader");
//        FacesModelSet instance = null;
//        ClassLoader expResult = null;
//        ClassLoader result = instance.getContextClassLoader();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDesignContexts method, of class FacesModelSet.
//     */
//    public void testGetDesignContexts() {
//        System.out.println("getDesignContexts");
//        FacesModelSet instance = null;
//        DesignContext[] expResult = null;
//        DesignContext[] result = instance.getDesignContexts();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findDesignContext method, of class FacesModelSet.
//     */
//    public void testFindDesignContext() {
//        System.out.println("findDesignContext");
//        String beanName = "";
//        FacesModelSet instance = null;
//        DesignContext expResult = null;
//        DesignContext result = instance.findDesignContext(beanName);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findDesignContexts method, of class FacesModelSet.
//     */
//    public void testFindDesignContexts() {
//        System.out.println("findDesignContexts");
//        String[] scopes = null;
//        FacesModelSet instance = null;
//        DesignContext[] expResult = null;
//        DesignContext[] result = instance.findDesignContexts(scopes);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createDesignContext method, of class FacesModelSet.
//     */
//    public void testCreateDesignContext() {
//        System.out.println("createDesignContext");
//        String className = "";
//        Class baseClass = null;
//        Map contextData = null;
//        FacesModelSet instance = null;
//        DesignContext expResult = null;
//        DesignContext result = instance.createDesignContext(className, baseClass, contextData);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeUnits method, of class FacesModelSet.
//     */
//    public void testRemoveUnits() {
//        System.out.println("removeUnits");
//        SourceUnit[] units = null;
//        FacesModelSet instance = null;
//        boolean expResult = false;
//        boolean result = instance.removeUnits(units);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeDesignContext method, of class FacesModelSet.
//     */
//    public void testRemoveDesignContext() {
//        System.out.println("removeDesignContext");
//        DesignContext context = null;
//        FacesModelSet instance = null;
//        boolean expResult = false;
//        boolean result = instance.removeDesignContext(context);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getResources method, of class FacesModelSet.
//     */
//    public void testGetResources() {
//        System.out.println("getResources");
//        URI folderUri = null;
//        boolean recurseFolders = false;
//        FacesModelSet instance = null;
//        URI[] expResult = null;
//        URI[] result = instance.getResources(folderUri, recurseFolders);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getResourceFile method, of class FacesModelSet.
//     */
//    public void testGetResourceFile() {
//        System.out.println("getResourceFile");
//        URI resourceUri = null;
//        FacesModelSet instance = null;
//        File expResult = null;
//        File result = instance.getResourceFile(resourceUri);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addResource method, of class FacesModelSet.
//     */
//    public void testAddResource() throws Exception {
//        System.out.println("addResource");
//        URL sourceUrl = null;
//        URI targetUri = null;
//        FacesModelSet instance = null;
//        URI expResult = null;
//        URI result = instance.addResource(sourceUrl, targetUri);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getProjectDirectory method, of class FacesModelSet.
//     */
//    public void testGetProjectDirectory() {
//        System.out.println("getProjectDirectory");
//        FacesModelSet instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.getProjectDirectory();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDocumentDirectory method, of class FacesModelSet.
//     */
//    public void testGetDocumentDirectory() {
//        System.out.println("getDocumentDirectory");
//        FacesModelSet instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.getDocumentDirectory();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getProjectDirectoryUri method, of class FacesModelSet.
//     */
//    public void testGetProjectDirectoryUri() {
//        System.out.println("getProjectDirectoryUri");
//        FacesModelSet instance = null;
//        URI expResult = null;
//        URI result = instance.getProjectDirectoryUri();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of relativize method, of class FacesModelSet.
//     */
//    public void testRelativize() {
//        System.out.println("relativize");
//        FileObject file = null;
//        FacesModelSet instance = null;
//        URI expResult = null;
//        URI result = instance.relativize(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of resolveToFileObject method, of class FacesModelSet.
//     */
//    public void testResolveToFileObject() {
//        System.out.println("resolveToFileObject");
//        URI uri = null;
//        FacesModelSet instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.resolveToFileObject(uri);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of resolveToUri method, of class FacesModelSet.
//     */
//    public void testResolveToUri() {
//        System.out.println("resolveToUri");
//        URI uri = null;
//        FacesModelSet instance = null;
//        URI expResult = null;
//        URI result = instance.resolveToUri(uri);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of resolveToUrl method, of class FacesModelSet.
//     */
//    public void testResolveToUrl() {
//        System.out.println("resolveToUrl");
//        URI uri = null;
//        FacesModelSet instance = null;
//        URL expResult = null;
//        URL result = instance.resolveToUrl(uri);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeResource method, of class FacesModelSet.
//     */
//    public void testRemoveResource() {
//        System.out.println("removeResource");
//        URI resourceUri = null;
//        FacesModelSet instance = null;
//        boolean expResult = false;
//        boolean result = instance.removeResource(resourceUri);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setProjectData method, of class FacesModelSet.
//     */
//    public void testSetProjectData() {
//        System.out.println("setProjectData");
//        String key = "";
//        Object data = null;
//        FacesModelSet instance = null;
//        instance.setProjectData(key, data);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getProjectData method, of class FacesModelSet.
//     */
//    public void testGetProjectData() {
//        System.out.println("getProjectData");
//        String key = "";
//        FacesModelSet instance = null;
//        Object expResult = null;
//        Object result = instance.getProjectData(key);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of flushProjectData method, of class FacesModelSet.
//     */
//    public void testFlushProjectData() {
//        System.out.println("flushProjectData");
//        FacesModelSet instance = null;
//        instance.flushProjectData();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setGlobalData method, of class FacesModelSet.
//     */
//    public void testSetGlobalData() {
//        System.out.println("setGlobalData");
//        String key = "";
//        Object data = null;
//        FacesModelSet instance = null;
//        instance.setGlobalData(key, data);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getGlobalData method, of class FacesModelSet.
//     */
//    public void testGetGlobalData() {
//        System.out.println("getGlobalData");
//        String key = "";
//        FacesModelSet instance = null;
//        Object expResult = null;
//        Object result = instance.getGlobalData(key);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDisplayName method, of class FacesModelSet.
//     */
//    public void testGetDisplayName() {
//        System.out.println("getDisplayName");
//        FacesModelSet instance = null;
//        String expResult = "";
//        String result = instance.getDisplayName();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDescription method, of class FacesModelSet.
//     */
//    public void testGetDescription() {
//        System.out.println("getDescription");
//        FacesModelSet instance = null;
//        String expResult = "";
//        String result = instance.getDescription();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLargeIcon method, of class FacesModelSet.
//     */
//    public void testGetLargeIcon() {
//        System.out.println("getLargeIcon");
//        FacesModelSet instance = null;
//        Image expResult = null;
//        Image result = instance.getLargeIcon();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSmallIcon method, of class FacesModelSet.
//     */
//    public void testGetSmallIcon() {
//        System.out.println("getSmallIcon");
//        FacesModelSet instance = null;
//        Image expResult = null;
//        Image result = instance.getSmallIcon();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getHelpKey method, of class FacesModelSet.
//     */
//    public void testGetHelpKey() {
//        System.out.println("getHelpKey");
//        FacesModelSet instance = null;
//        String expResult = "";
//        String result = instance.getHelpKey();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of classPathChanged method, of class FacesModelSet.
//     */
//    public void testClassPathChanged() {
//        System.out.println("classPathChanged");
//        FacesModelSet instance = null;
//        instance.classPathChanged();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addDesignProjectListener method, of class FacesModelSet.
//     */
//    public void testAddDesignProjectListener() {
//        System.out.println("addDesignProjectListener");
//        DesignProjectListener listener = null;
//        FacesModelSet instance = null;
//        instance.addDesignProjectListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeDesignProjectListener method, of class FacesModelSet.
//     */
//    public void testRemoveDesignProjectListener() {
//        System.out.println("removeDesignProjectListener");
//        DesignProjectListener listener = null;
//        FacesModelSet instance = null;
//        instance.removeDesignProjectListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDesignProjectListeners method, of class FacesModelSet.
//     */
//    public void testGetDesignProjectListeners() {
//        System.out.println("getDesignProjectListeners");
//        FacesModelSet instance = null;
//        DesignProjectListener[] expResult = null;
//        DesignProjectListener[] result = instance.getDesignProjectListeners();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasDesignProjectListeners method, of class FacesModelSet.
//     */
//    public void testHasDesignProjectListeners() {
//        System.out.println("hasDesignProjectListeners");
//        FacesModelSet instance = null;
//        boolean expResult = false;
//        boolean result = instance.hasDesignProjectListeners();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of fireContextOpened method, of class FacesModelSet.
//     */
//    public void testFireContextOpened() {
//        System.out.println("fireContextOpened");
//        DesignContext context = null;
//        FacesModelSet instance = null;
//        instance.fireContextOpened(context);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of fireContextClosed method, of class FacesModelSet.
//     */
//    public void testFireContextClosed() {
//        System.out.println("fireContextClosed");
//        DesignContext context = null;
//        FacesModelSet instance = null;
//        instance.fireContextClosed(context);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of evalOrderModels method, of class FacesModelSet.
//     */
//    public void testEvalOrderModels() {
//        System.out.println("evalOrderModels");
//        Collection modelsToOrder = null;
//        FacesModelSet instance = null;
//        Collection expResult = null;
//        Collection result = instance.evalOrderModels(modelsToOrder);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeModel method, of class FacesModelSet.
//     */
//    public void testRemoveModel() {
//        System.out.println("removeModel");
//        Model model = null;
//        FacesModelSet instance = null;
//        instance.removeModel(model);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getJavaRootFolder method, of class FacesModelSet.
//     */
//    public void testGetJavaRootFolder() {
//        System.out.println("getJavaRootFolder");
//        FacesModelSet instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.getJavaRootFolder();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageJavaRootFolder method, of class FacesModelSet.
//     */
//    public void testGetPageJavaRootFolder() {
//        System.out.println("getPageJavaRootFolder");
//        FacesModelSet instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.getPageJavaRootFolder();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageJspRootFolder method, of class FacesModelSet.
//     */
//    public void testGetPageJspRootFolder() {
//        System.out.println("getPageJspRootFolder");
//        FacesModelSet instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.getPageJspRootFolder();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getJavaFolderFor method, of class FacesModelSet.
//     */
//    public void testGetJavaFolderFor() {
//        System.out.println("getJavaFolderFor");
//        FileObject other = null;
//        boolean tryHard = false;
//        FacesModelSet instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.getJavaFolderFor(other, tryHard);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of syncAll method, of class FacesModelSet.
//     */
//    public void testSyncAll() {
//        System.out.println("syncAll");
//        FacesModelSet instance = null;
//        instance.syncAll();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
