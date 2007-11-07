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

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.filesystems.FileObject;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author joelle
 */
public class PageFlowControllerTest extends NbTestCase implements TestServices {

    final String zipPath;
    private PageFlowTestUtility tu;
    PageFlowView view;
    PageFlowScene scene;
    PageFlowController controller;

    public PageFlowControllerTest(String testName) {
        super(testName);
        zipPath = PageFlowControllerTest.class.getResource("TestJSFApp.zip").getPath();
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(PageFlowControllerTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tu = new PageFlowTestUtility(this);

        tu.setUp(zipPath, "TestJSFApp");

        importantValuesNotNull();
    }

    public void importantValuesNotNull() throws InterruptedException {

        assertNotNull(tu.getProject());
        assertNotNull(tu.getJsfDO());
        assertNotNull(tu.getFacesConfig());
        assertNotNull(view = tu.getPageFlowView());
        assertNotNull(controller = tu.getController());
        assertNotNull(scene = tu.getScene());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        tu.tearDown();
        tu = null;
        view = null;
        scene = null;
        controller = null;
    }

    public void setupServices() {

        ClassLoader l = this.getClass().getClassLoader();
        MockLookup.setLookup(Lookups.fixed(l), Lookups.metaInfServices(l));
    }

    /**
     * Test of setShowNoWebFolderDialog method, of class PageFlowController.
     */
    public void testNoWebFolderDialog() {
        System.out.println("setShowNoWebFolderDialog");
        boolean show = false;
        controller.setShowNoWebFolderDialog(false);
        assertFalse(controller.isShowNoWebFolderDialog());
        controller.setShowNoWebFolderDialog(true);
        assertTrue(controller.isShowNoWebFolderDialog());
//
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//
//            public void run() {
//                controller.ifNecessaryShowNoWebFolderDialog();
//            }
//        });
//
//        Frame frame = WindowManager.getDefault().getMainWindow();
//        assertNotNull(frame);
    }

    private void waitEQ() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
    }
    private static final String JSP_EXT = "jsp";

    /**
     * Test of unregisterListeners method, of class PageFlowController.
     * 
     * This test is sort of tempermental in that if I were to test 
     * register listeners in this same way, it would only work 50% of the time.
     * Regardless, this test should always pass because page2 is always null.
     */
    public void testUnregisterListeners() throws IOException {
        System.out.println("unregisterListeners");
        final String strNewPage2 = "newPage2";
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());

        controller.unregisterListeners();
        webFolder.createData(strNewPage2, JSP_EXT);
        Page page2 = controller.getPageName2Page(strNewPage2 + "." + JSP_EXT);
        assertNull(page2);

    }

    /**
     * Test of isCurrentScope method, of class PageFlowController.
     */
    public void testIsCurrentScope() {
        System.out.println("isCurrentScope");
        boolean result = controller.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT);
        assertTrue(result);
        result = controller.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG);
        assertFalse(result);
        PageFlowToolbarUtilities.getInstance(view).setCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG);
        result = controller.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT);
        assertFalse(result);
        result = controller.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG);
        assertTrue(result);

    }

    /**
     * Test of createPage method, of class PageFlowController.
     */
    public void testCreatePage() {
        System.out.println("createPage");
        final String pageName = "pageJSP";
        Page result = controller.createPage(pageName + "." + JSP_EXT);
        assertEquals(pageName + "." + JSP_EXT, result.getName());


    }

    /**
     * Test of createPage method, of class PageFlowController.
     */
    public void testCreatePageEmptyString() {
        System.out.println("createPageEmptyString");
        boolean aeFound = false;
        try {
            Page result = controller.createPage("");
        } catch (AssertionError ae) {
            aeFound = true;
        }
        assertTrue(aeFound);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    /**
     * Test of createPage method, of class PageFlowController.
     */
    public void testCreatePageNull() {
        System.out.println("createPageNull");
        boolean npeCaught = false;
        try {
            Page result = controller.createPage(null);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
    }

    /**
     * Test of createLink method, of class PageFlowController.
     */
    public void testCreateLink() {
        System.out.println("createLink");
        String page1 = "page1.jsp";
        String page2 = "page2.jsp";
        Page source = controller.createPage(page1);
        Page target = controller.createPage(page2);
        /* this create an NPE from being thrown in FacesModelListener 
           In order to create the link , the pages need to exist in the node.*/
        view.createNode(source, null, null);
        view.createNode(target, null, null);
        view.validateGraph();
        
        Pin pinNode = null;
        NavigationCase result = controller.createLink(source, target, pinNode);

        assertEquals(result.getToViewId(), "/" + target.getName());
        assert (result.getParent() instanceof NavigationRule);
        NavigationRule resultRule = (NavigationRule) result.getParent();
        assertEquals(resultRule.getFromViewId(), "/" + source.getName());
        assertEquals(result.getFromOutcome(), "case1");
    }

    /**
     * Test of createLink method, of class PageFlowController.
     */
    public void testCreateLinkWithNullValues() {
        System.out.println("createLink with null values");
        boolean npeCaught = false;
        String page1 = "page1.jsp";
        String page2 = "page2.jsp";
        Page source = controller.createPage(page1);
        Page target = controller.createPage(page2);
        Pin pinNode = null;

        try {
            NavigationCase result = controller.createLink(null, target, pinNode);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
        npeCaught = false;

        try {
            NavigationCase result = controller.createLink(source, null, pinNode);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
        npeCaught = false;

        try {
            NavigationCase result = controller.createLink(source, null, pinNode);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);

    }
//
//    /**
//     * Test of updatePageItems method, of class PageFlowController.
//     */
//    public void testUpdatePageItems() {
//        System.out.println("updatePageItems");
//        Page pageNode = null;
//        PageFlowController instance = null;
//        instance.updatePageItems(pageNode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of isKnownFile method, of class PageFlowController.
     */
    public void testIsKnownFile() throws IOException {
        System.out.println("isKnownFile");
        
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());
        String strNewPage = "newPage";
        FileObject newFO = webFolder.createData(strNewPage, "jsp");
        boolean result = controller.isKnownFile(newFO);
        assertTrue(result);
    }
    
    public void testIsKnownFileXML() throws IOException {
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());
        FileObject webINFFile = webFolder.getFileObject("WEB-INF");
        String strNewPage = "newPage";
        FileObject newFO = webINFFile.createData(strNewPage, "xml");
        boolean result = controller.isKnownFile(newFO);
        assertFalse(result);
    }
    
     public void testIsKnownFileNull()  {
         boolean npeFound = false;
         try { 
            boolean result = controller.isKnownFile(null);
         } catch (NullPointerException npe ){
             npeFound = true;
         }
         assertTrue(npeFound);
    }
     
//     /**
//      * Test if getting the webFolder works properly
//      */
//     public void testGetWebFolder() {
//         System.out.println("CONTROLLER: getWebFolder() ->" + controller.getWebFolder());
//         System.out.println("PageFlowView: getWebFolder() ->" + PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile()));
//         assertEquals(controller.getWebFolder(),PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile()));
//     }
     
     
    /**
     * Test of isKnownFolder method, of class PageFlowController.
     */
    public void testIsKnownFolder() throws IOException {
        System.out.println("isKnownFolder");
        
        
        FileObject webFolder = controller.getWebFolder();
        FileObject testFolder = webFolder.createFolder("tesFolder");
        boolean result1 = controller.isKnownFolder(webFolder);
        assertTrue( result1);
        boolean result2 = controller.isKnownFolder(testFolder);
        assertTrue(result2);
    }

    /**
     * Test of isKnownFolder method, of class PageFlowController.
     */
    public void testIsKnownFolderWeBINF() {
        System.out.println("isKnownFolder");
        
        
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());
        FileObject webINFFolder = webFolder.getFileObject("WEB-INF");
        boolean result = controller.isKnownFolder(webINFFolder);
        assertFalse(result);
    }

//    /**
//     * Test of setupGraph method, of class PageFlowController.
//     */
//    public void testSetupGraph() {
//        System.out.println("setupGraph");
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.setupGraph();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setupGraphNoSaveData method, of class PageFlowController.
//     */
//    public void testSetupGraphNoSaveData() {
//        System.out.println("setupGraphNoSaveData");
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.setupGraphNoSaveData();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createEdge method, of class PageFlowController.
//     */
//    public void testCreateEdge() {
//        System.out.println("createEdge");
//        NavigationCaseEdge caseNode = null;
//        PageFlowController instance = null;
//        instance.createEdge(caseNode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createPageFlowNode method, of class PageFlowController.
//     */
//    public void testCreatePageFlowNode() {
//        System.out.println("createPageFlowNode");
//        Node node = null;
//        PageFlowController instance = null;
//        Page expResult = null;
//        Page result = instance.createPageFlowNode(node);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//

//
//    /**
//     * Test of destroyPageFlowNode method, of class PageFlowController.
//     */
//    public void testDestroyPageFlowNode() {
//        System.out.println("destroyPageFlowNode");
//        Page pageNode = null;
//        PageFlowController instance = null;
//        instance.destroyPageFlowNode(pageNode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removePageName2Page method, of class PageFlowController.
//     */
//    public void testRemovePageName2Page() {
//        System.out.println("removePageName2Page");
//        Page pageNode = null;
//        boolean destroy = false;
//        PageFlowController instance = null;
//        Page expResult = null;
//        Page result = instance.removePageName2Page(pageNode, destroy);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of replacePageName2Page method, of class PageFlowController.
//     */
//    public void testReplacePageName2Page() {
//        System.out.println("replacePageName2Page");
//        Page node = null;
//        String newName = "";
//        String oldName = "";
//        PageFlowController instance = null;
//        instance.replacePageName2Page(node, newName, oldName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of clearPageName2Page method, of class PageFlowController.
//     */
//    public void testClearPageName2Page() {
//        System.out.println("clearPageName2Page");
//        PageFlowController instance = null;
//        instance.clearPageName2Page();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of putPageName2Page method, of class PageFlowController.
//     */
//    public void testPutPageName2Page() {
//        System.out.println("putPageName2Page");
//        String displayName = "";
//        Page pageNode = null;
//        PageFlowController instance = null;
//        instance.putPageName2Page(displayName, pageNode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageName2Page method, of class PageFlowController.
//     */
//    public void testGetPageName2Page() {
//        System.out.println("getPageName2Page");
//        String displayName = "";
//        PageFlowController instance = null;
//        Page expResult = null;
//        Page result = instance.getPageName2Page(displayName);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of printThreadInfo method, of class PageFlowController.
//     */
//    public void testPrintThreadInfo() {
//        System.out.println("printThreadInfo");
//        PageFlowController instance = null;
//        instance.printThreadInfo();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of renamePageInModel method, of class PageFlowController.
//     */
//    public void testRenamePageInModel() {
//        System.out.println("renamePageInModel");
//        String oldDisplayName = "";
//        String newDisplayName = "";
//        PageFlowController instance = null;
//        instance.renamePageInModel(oldDisplayName, newDisplayName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeSceneNodeEdges method, of class PageFlowController.
//     */
//    public void testRemoveSceneNodeEdges() {
//        System.out.println("removeSceneNodeEdges");
//        Page pageNode = null;
//        PageFlowController instance = null;
//        instance.removeSceneNodeEdges(pageNode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removePageInModel method, of class PageFlowController.
//     */
//    public void testRemovePageInModel() {
//        System.out.println("removePageInModel");
//        String displayName = "";
//        PageFlowController instance = null;
//        instance.removePageInModel(displayName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getWebFolder method, of class PageFlowController.
//     */
//    public void testGetWebFolder() {
//        System.out.println("getWebFolder");
//        PageFlowController instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.getWebFolder();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isPageInAnyFacesConfig method, of class PageFlowController.
//     */
//    public void testIsPageInAnyFacesConfig() {
//        System.out.println("isPageInAnyFacesConfig");
//        String name = "";
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.isPageInAnyFacesConfig(name);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isNavCaseInFacesConfig method, of class PageFlowController.
//     */
//    public void testIsNavCaseInFacesConfig() {
//        System.out.println("isNavCaseInFacesConfig");
//        NavigationCaseEdge navEdge = null;
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.isNavCaseInFacesConfig(navEdge);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of changeToAbstractNode method, of class PageFlowController.
//     */
//    public void testChangeToAbstractNode() {
//        System.out.println("changeToAbstractNode");
//        Page oldNode = null;
//        String displayName = "";
//        PageFlowController instance = null;
//        instance.changeToAbstractNode(oldNode, displayName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getConfigDataObject method, of class PageFlowController.
//     */
//    public void testGetConfigDataObject() {
//        System.out.println("getConfigDataObject");
//        PageFlowController instance = null;
//        DataObject expResult = null;
//        DataObject result = instance.getConfigDataObject();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of saveLocation method, of class PageFlowController.
//     */
//    public void testSaveLocation() {
//        System.out.println("saveLocation");
//        String oldDisplayName = "";
//        String newDisplayName = "";
//        PageFlowController instance = null;
//        instance.saveLocation(oldDisplayName, newDisplayName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeWebFile method, of class PageFlowController.
//     */
//    public void testRemoveWebFile() {
//        System.out.println("removeWebFile");
//        FileObject fileObj = null;
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.removeWebFile(fileObj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addWebFile method, of class PageFlowController.
//     */
//    public void testAddWebFile() {
//        System.out.println("addWebFile");
//        FileObject fileObj = null;
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.addWebFile(fileObj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of containsWebFile method, of class PageFlowController.
//     */
//    public void testContainsWebFile() {
//        System.out.println("containsWebFile");
//        FileObject fileObj = null;
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.containsWebFile(fileObj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of putNavCase2NavCaseEdge method, of class PageFlowController.
//     */
//    public void testPutNavCase2NavCaseEdge() {
//        System.out.println("putNavCase2NavCaseEdge");
//        NavigationCase navCase = null;
//        NavigationCaseEdge navCaseEdge = null;
//        PageFlowController instance = null;
//        instance.putNavCase2NavCaseEdge(navCase, navCaseEdge);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getNavCase2NavCaseEdge method, of class PageFlowController.
//     */
//    public void testGetNavCase2NavCaseEdge() {
//        System.out.println("getNavCase2NavCaseEdge");
//        NavigationCase navCase = null;
//        PageFlowController instance = null;
//        NavigationCaseEdge expResult = null;
//        NavigationCaseEdge result = instance.getNavCase2NavCaseEdge(navCase);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeNavCase2NavCaseEdge method, of class PageFlowController.
//     */
//    public void testRemoveNavCase2NavCaseEdge() {
//        System.out.println("removeNavCase2NavCaseEdge");
//        NavigationCase navCase = null;
//        PageFlowController instance = null;
//        NavigationCaseEdge expResult = null;
//        NavigationCaseEdge result = instance.removeNavCase2NavCaseEdge(navCase);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeNavRule2String method, of class PageFlowController.
//     */
//    public void testRemoveNavRule2String() {
//        System.out.println("removeNavRule2String");
//        NavigationRule navRule = null;
//        PageFlowController instance = null;
//        String expResult = "";
//        String result = instance.removeNavRule2String(navRule);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of putNavRule2String method, of class PageFlowController.
//     */
//    public void testPutNavRule2String() {
//        System.out.println("putNavRule2String");
//        NavigationRule navRule = null;
//        String navRuleName = "";
//        PageFlowController instance = null;
//        String expResult = "";
//        String result = instance.putNavRule2String(navRule, navRuleName);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getView method, of class PageFlowController.
//     */
//    public void testGetView() {
//        System.out.println("getView");
//        PageFlowController instance = null;
//        PageFlowView expResult = null;
//        PageFlowView result = instance.getView();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setModelNavigationCaseName method, of class PageFlowController.
//     */
//    public void testSetModelNavigationCaseName() {
//        System.out.println("setModelNavigationCaseName");
//        NavigationCase navCase = null;
//        String newName = "";
//        PageFlowController instance = null;
//        instance.setModelNavigationCaseName(navCase, newName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeModelNavigationCase method, of class PageFlowController.
//     */
//    public void testRemoveModelNavigationCase() throws Exception {
//        System.out.println("removeModelNavigationCase");
//        NavigationCase navCase = null;
//        PageFlowController instance = null;
//        instance.removeModelNavigationCase(navCase);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of serializeNodeLocations method, of class PageFlowController.
//     */
//    public void testSerializeNodeLocations() {
//        System.out.println("serializeNodeLocations");
//        PageFlowController instance = null;
//        instance.serializeNodeLocations();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of openNavigationCase method, of class PageFlowController.
//     */
//    public void testOpenNavigationCase() {
//        System.out.println("openNavigationCase");
//        NavigationCaseEdge navCaseEdge = null;
//        PageFlowController instance = null;
//        instance.openNavigationCase(navCaseEdge);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageContentModelProviders method, of class PageFlowController.
//     */
//    public void testGetPageContentModelProviders() {
//        System.out.println("getPageContentModelProviders");
//        Collection<? extends PageContentModelProvider> expResult = null;
//        Collection<? extends PageContentModelProvider> result = PageFlowController.getPageContentModelProviders();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
