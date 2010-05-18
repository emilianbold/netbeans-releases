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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.projectimport.j2seimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Radek Matous
 */
public class AbstractProjectDefinitionTest extends NbTestCase {
    protected AbstractProject testProject;

    static {
        System.setProperty("projectimport.logging.level", "FINEST");
    }

    public AbstractProjectDefinitionTest(String testName) {
        super(testName);
    }

    protected final void setUp() throws Exception {
        clearWorkDir();
        try {
            testProject = new AbstractProject(getName(), FileUtil.toFileObject(getWorkDir()));
        } catch(IOException iex) {
            assert false : iex.getLocalizedMessage();
            throw new IllegalStateException(iex.getLocalizedMessage());
        }
        
        specificSetUp(testProject);
    }


    protected void specificSetUp(AbstractProject projectDefinition) {}
    
    private AbstractProject generalSetUp(String testName) {
        AbstractProject retVal = null;
        try {
            retVal = new AbstractProject(testName, FileUtil.toFileObject(getWorkDir()));
        } catch(IOException iex) {
            assert false : iex.getLocalizedMessage();
            throw new IllegalStateException(iex.getLocalizedMessage());
        }
     
        return retVal;
    }
    
    protected void tearDown() throws Exception {
        clearWorkDir();
    }


    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractProjectDefinitionTest.class);
        
        return suite;
    }

    /**
     * Test of getName method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.AbstractProjectDefinition.
     */
    public void testGetName() {
        assertEquals(testProject.getName(), this.getName());
    }

    /**
     * Test of getProjectDir method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.AbstractProjectDefinition.
     */
    public void testGetProjectDir() throws Exception {
        assertEquals(testProject.getProjectDir(), FileUtil.toFileObject(getWorkDir()));        
    }

    /**
     * Test of isAlreadyImported method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.AbstractProjectDefinition.
     */
    public void testIsAlreadyImported() {
        assertFalse(testProject.isAlreadyImported());
    }

    /**
     * Test of setAsImported method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.AbstractProjectDefinition.
     */
    public void testSetAsImported() {        
        testProject.setAsImported();
        assertTrue(testProject.isAlreadyImported());        
    }

    /**
     * Test of getLibraryEntries method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.AbstractProjectDefinition.
     */
    public void testGetLibraryEntries() {
        assertNotNull(testProject.getLibraries());
        assertEquals(testProject.getLibraries().size(),0);
    }

    /**
     * Test of addLibraryEntry method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.AbstractProjectDefinition.
     */
    public void testAddLibraryEntry() throws Exception {
        File archiv = createArchivFile(getWorkDir(),"lib.jar");        
        
        assertEquals(testProject.getLibraries().size(),0);        
        assertEquals(testProject.getWarnings().size(),0);                
        
        AbstractProject.Library library = new AbstractProject.Library(archiv);
        assertTrue(library.isValid());
        testProject.addLibrary(library);        
        assertEquals(testProject.getLibraries().size(),1);        
        assertEquals(testProject.getWarnings().size(),0);                
        assertTrue(testProject.getLibraries().contains(library));
        
        

        library = new AbstractProject.Library(getWorkDir());
        assertFalse(library.isValid());
        testProject.addLibrary(library);        
        assertEquals(testProject.getLibraries().size(),2);        
        assertEquals(testProject.getWarnings().size(),1);                
        assertTrue(testProject.getLibraries().contains(library));        
    }

    /**
     * Test of getUserLibraries method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.AbstractProjectDefinition.
     */
    public void testGetUserLibraries() {
        assertNotNull(testProject.getUserLibraries());
        assertEquals(testProject.getUserLibraries().size(),0);
    }

    /**
     * Test of addLUserLibrary method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.AbstractProjectDefinition.
     */
    public void testAddLUserLibrary() throws Exception{
        AbstractProject.UserLibrary uLibrary = new AbstractProject.UserLibrary(this.getName());
        assertFalse("UserLibrary is valid if isn't empty",uLibrary.isValid());

        
        
        AbstractProject.Library library1 = 
                new AbstractProject.Library(createArchivFile(getWorkDir(),"lib1.jar"));//NOI18N
        
        
        uLibrary.addLibrary(library1);        
        assertTrue(uLibrary.isValid());
        assertEquals(uLibrary.getLibraries().size(),1);        
        assertTrue(uLibrary.getLibraries().contains(library1));        
        
        
        testProject.addUserLibrary(uLibrary);
        assertEquals(testProject.getUserLibraries().size(),1);        
        assertEquals(testProject.getWarnings().size(),0);                
        assertTrue(testProject.getUserLibraries().contains(uLibrary));        
        assertFalse("the same can be added just once",testProject.addUserLibrary(uLibrary));
        assertEquals("the same library isn't added again",testProject.getUserLibraries().size(),1);        
        assertEquals(testProject.getWarnings().size(),1);                
        

        AbstractProject.UserLibrary uLibrary2 = new AbstractProject.UserLibrary(this.getName()+"2");
        testProject.addUserLibrary(uLibrary2);
        
        //!!!!!
        assertEquals("UserLibrary must be added in non empy state else warning is fired",testProject.getWarnings().size(),2);                        
    }


    public static File createArchivFile(File workDir,String name) throws Exception {
        File archiv = new File(workDir, name);        
        assertTrue(archiv.createNewFile());
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(archiv));
        jos.setComment("Just for testing");//NOI18N
        JarEntry je = new JarEntry("oneEntry");//NOI18N        
        jos.putNextEntry(je);
        jos.close();
        
        return archiv;
    }
    
}
