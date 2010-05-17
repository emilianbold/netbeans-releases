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
public class JsfJavaDataObjectTest extends NbTestCase {
    private Project project;
    private FileObject projectRoot;
    
    public JsfJavaDataObjectTest(String testName) {
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
        System.out.println("JsfJavaDataLoader.findPrimaryFile");
        
        FileObject srcFolder = projectRoot.getFileObject("src/java/vwjavaee5");
        FileObject pageJava = projectRoot.getFileObject("src/java/vwjavaee5/Page1.java");
        FileObject normalJava = projectRoot.getFileObject("src/java/vwjavaee5/RequestBean1.java");
        
        assertNotNull("srcFolder FileObject should not be null", srcFolder);
        assertNotNull("pageJava FileObject should not be null", pageJava);
        assertNotNull("normalJava FileObject should not be null", normalJava);
        
        JsfJavaDataLoader loader = SetupUtils.getJavaLoader();
        
        FileObject result1 = loader.findPrimaryFile(pageJava);
        FileObject result2 = loader.findPrimaryFile(srcFolder);
        FileObject result3 = loader.findPrimaryFile(normalJava);
        
        assertEquals("findPrimaryFile did not recognize Page1.java", result1, pageJava);
        assertNull("findPrimaryFile should not recognize folders", result2);
        assertNull("findPrimaryFile should not recognize plain java files", result3);
    }
    
    /**
     * Test of getLookup method, of class JsfJspDataObject.
     */
    public void testGetLookup() throws Exception {
        System.out.println("getLookup");
        FileObject javaPage = projectRoot.getFileObject("src/java/vwjavaee5/Page1.java");
        JsfJavaDataObject instance = (JsfJavaDataObject)DataObject.find(javaPage);
        
        // test validity of lookup results
        Lookup result = instance.getLookup();
        JsfJavaEditorSupport support = result.lookup(JsfJavaEditorSupport.class);
        OpenCookie openCookie = result.lookup(OpenCookie.class);
        EditCookie editCookie = result.lookup(EditCookie.class);
        EditorCookie editorCookie = result.lookup(EditorCookie.class);
        
        assertNotNull("OpenCookie is null", openCookie);
        assertNotNull("EditCookie is null", editCookie);
        assertNotNull("EditorCookie is null", editorCookie);
        assertNotNull("JsfJavaEditorSupport is null", support);
        
        assertEquals("getLookup and getCookie should return the same OpenCookie", 
                openCookie, instance.getCookie(OpenCookie.class));
        assertEquals("getLookup and getCookie should return the same EditCookie", 
                editCookie, instance.getCookie(EditCookie.class));
        assertEquals("getLookup and getCookie should return the same EditorCookie", 
                editorCookie, instance.getCookie(EditorCookie.class));
        assertEquals("getLookup and getCookie should return the same JsfJavaEditorSupport", 
                support, instance.getCookie(JsfJavaEditorSupport.class));
    }
    
    public void testGetCookie() throws Exception {
        System.out.println("getCookie");
        FileObject javaPage = projectRoot.getFileObject("src/java/vwjavaee5/Page1.java");
        JsfJavaDataObject instance = (JsfJavaDataObject)DataObject.find(javaPage);
        
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
    
    

}
