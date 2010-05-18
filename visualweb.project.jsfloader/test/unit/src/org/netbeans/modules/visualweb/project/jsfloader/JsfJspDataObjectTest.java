/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.visualweb.project.jsfloader;

import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.visualweb.project.jsfloader.test.SetupUtils;
import org.netbeans.modules.web.core.jsploader.api.TagLibParseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author quynguyen
 */
public class JsfJspDataObjectTest extends NbTestCase {
    private Project project;
    private FileObject projectRoot;
    
    public JsfJspDataObjectTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        project = SetupUtils.setup(getWorkDir());
        projectRoot = FileUtil.toFileObject(getWorkDir()).getFileObject("VWJavaEE5");
        
        assertNotNull("Project should not be null", project);
        assertNotNull("Project root folder should not be null", projectRoot);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        project = null;
        projectRoot = null;
        
        MockLookup.setInstances();
    }

    public void testFindPrimaryFile() throws Exception {
        System.out.println("JsfJspDataLoader.findPrimaryFile");
        FileObject webFolder = projectRoot.getFileObject("web");
        FileObject pageBean = projectRoot.getFileObject("web/Page1.jsp");
        FileObject plainJsp = projectRoot.getFileObject("web/index.jsp");
        
        assertNotNull("webFolder FileObject should not be null", webFolder);
        assertNotNull("pageBean FileObject should not be null", pageBean);
        assertNotNull("plainJsp FileObject should not be null", plainJsp);
        
        JsfJspDataLoader loader = SetupUtils.getJspLoader();
        
        FileObject result1 = loader.findPrimaryFile(pageBean);
        assertEquals("findPrimaryFile() on web/Page1.jsp did not work", result1, pageBean);
        
        FileObject result2 = loader.findPrimaryFile(webFolder);
        assertNull("findPrimaryFile() should not accept folder objects", result2);
        
        FileObject result3 = loader.findPrimaryFile(plainJsp);
        assertNull("FindPrimaryFile() should not accept non-visual web JSP files", result3);
    }
    
    /**
     * Test of getLookup method, of class JsfJspDataObject.
     */
    public void testGetLookup() throws Exception {
        System.out.println("getLookup");
        FileObject jspPage = projectRoot.getFileObject("web/Page1.jsp");
        JsfJspDataObject instance = (JsfJspDataObject)DataObject.find(jspPage);
        
        // test validity of lookup results
        Lookup result = instance.getLookup();
        JsfJspEditorSupport support = result.lookup(JsfJspEditorSupport.class);
        OpenCookie openCookie = result.lookup(OpenCookie.class);
        EditCookie editCookie = result.lookup(EditCookie.class);
        EditorCookie editorCookie = result.lookup(EditorCookie.class);
        TagLibParseCookie tagLibParseCookie = result.lookup(TagLibParseCookie.class);
        
        assertNotNull("OpenCookie is null", openCookie);
        assertNotNull("EditCookie is null", editCookie);
        assertNotNull("EditorCookie is null", editorCookie);
        assertNotNull("TagLibParseCookie is null", tagLibParseCookie);
        assertNotNull("JsfJspEditorSupport is null", support);
        
        assertEquals("getLookup and getCookie should return the same OpenCookie", 
                openCookie, instance.getCookie(OpenCookie.class));
        assertEquals("getLookup and getCookie should return the same EditCookie", 
                editCookie, instance.getCookie(EditCookie.class));
        assertEquals("getLookup and getCookie should return the same EditorCookie", 
                editorCookie, instance.getCookie(EditorCookie.class));
        assertEquals("getLookup and getCookie should return the same TagLibParseCookie", 
                tagLibParseCookie, instance.getCookie(TagLibParseCookie.class));
        assertEquals("getLookup and getCookie should return the same JsfJspEditorSupport", 
                support, instance.getCookie(JsfJspEditorSupport.class));
    }
    
    public void testGetCookie() throws Exception {
        System.out.println("getCookie");
        FileObject jspPage = projectRoot.getFileObject("web/Page1.jsp");
        JsfJspDataObject instance = (JsfJspDataObject)DataObject.find(jspPage);
        
        // test getCookie
        EditorCookie editorCookie = instance.getCookie(EditorCookie.class);
        OpenCookie openCookie = instance.getCookie(OpenCookie.class);
        EditCookie editCookie = instance.getCookie(EditCookie.class);
        
        assertNotNull("OpenCookie is null", openCookie);
        assertNotNull("EditCookie is null", editCookie);
        assertNotNull("EditorCookie is null", editorCookie);
        
        Lookup lookup = instance.getLookup();
        
        assertEquals("getLookup and getCookie should return the same OpenCookie", 
                openCookie, lookup.lookup(OpenCookie.class));
        assertEquals("getLookup and getCookie should return the same EditCookie", 
                editCookie, lookup.lookup(EditCookie.class));
        assertEquals("getLookup and getCookie should return the same EditorCookie", 
                editorCookie, lookup.lookup(EditorCookie.class));
    }
    
    
    public void testCorrespondingJsfJava() throws Exception {
        System.out.println("correspondingJsfJavaFile");
        FileObject jspPage = projectRoot.getFileObject("web/Page1.jsp");
        JsfJspDataObject instance = (JsfJspDataObject)DataObject.find(jspPage);
        FileObject javaSrc = Utils.findJavaForJsp(instance.getPrimaryFile());
        DataObject dobj = DataObject.find(javaSrc);
        assertTrue("Corresponding java file needs to be JSF", dobj instanceof JsfJavaDataObject);
    }
    
//    /**
//     * Test of createNodeDelegate method, of class JsfJspDataObject.
//     */
//    public void testCreateNodeDelegate() {
//        System.out.println("createNodeDelegate");
//        JsfJspDataObject instance = null;
//        Node expResult = null;
//        Node result = instance.createNodeDelegate();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getHelpCtx method, of class JsfJspDataObject.
//     */
//    public void testGetHelpCtx() {
//        System.out.println("getHelpCtx");
//        JsfJspDataObject instance = null;
//        HelpCtx expResult = null;
//        HelpCtx result = instance.getHelpCtx();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createCookie method, of class JsfJspDataObject.
//     */
//    public void testCreateCookie() {
//        System.out.println("createCookie");
//        Class klass = null;
//        JsfJspDataObject instance = null;
//        Cookie expResult = null;
//        Cookie result = instance.createCookie(klass);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCookieSet0 method, of class JsfJspDataObject.
//     */
//    public void testGetCookieSet0() {
//        System.out.println("getCookieSet0");
//        JsfJspDataObject instance = null;
//        CookieSet expResult = null;
//        CookieSet result = instance.getCookieSet0();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//
//    /**
//     * Test of getPureCookie method, of class JsfJspDataObject.
//     */
//    public void testGetPureCookie() {
//        System.out.println("getPureCookie");
//        Class clazz = null;
//        JsfJspDataObject instance = null;
//        Cookie expResult = null;
//        Cookie result = instance.getPureCookie(clazz);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCookie method, of class JsfJspDataObject.
//     */
//    public void testGetCookie() {
//        System.out.println("getCookie");
//        Class clazz = null;
//        JsfJspDataObject instance = null;
//        Cookie expResult = null;
//        Cookie result = instance.getCookie(clazz);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getFileEncoding method, of class JsfJspDataObject.
//     */
//    public void testGetFileEncoding() {
//        System.out.println("getFileEncoding");
//        JsfJspDataObject instance = null;
//        String expResult = "";
//        String result = instance.getFileEncoding();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateFileEncoding method, of class JsfJspDataObject.
//     */
//    public void testUpdateFileEncoding() {
//        System.out.println("updateFileEncoding");
//        boolean fromEditor = false;
//        JsfJspDataObject instance = null;
//        instance.updateFileEncoding(fromEditor);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of pureCopy method, of class JsfJspDataObject.
//     */
//    public void testPureCopy() throws Exception {
//        System.out.println("pureCopy");
//        DataFolder folder = null;
//        JsfJspDataObject instance = null;
//        instance.pureCopy(folder);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//
//    /**
//     * Test of handleDelete method, of class JsfJspDataObject.
//     */
//    public void testHandleDelete() throws Exception {
//        System.out.println("handleDelete");
//        JsfJspDataObject instance = null;
//        instance.handleDelete();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of handleCreateFromTemplate method, of class JsfJspDataObject.
//     */
//    public void testHandleCreateFromTemplate() throws Exception {
//        System.out.println("handleCreateFromTemplate");
//        DataFolder df = null;
//        String name = "";
//        JsfJspDataObject instance = null;
//        DataObject expResult = null;
//        DataObject result = instance.handleCreateFromTemplate(df, name);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of handleRename method, of class JsfJspDataObject.
//     */
//    public void testHandleRename() throws Exception {
//        System.out.println("handleRename");
//        String name = "";
//        JsfJspDataObject instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.handleRename(name);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }


}
