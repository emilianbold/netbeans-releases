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

/*
 * CompiledSourceForBinaryQueryTest.java
 * JUnit based test
 *
 * Created on 14 February 2006, 10:43
 */
package org.netbeans.modules.mobility.project.queries;

import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.*;
import java.net.URL;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lukas
 */
public class CompiledSourceForBinaryQueryTest extends NbTestCase {
    static AntProjectHelper aph=null;
    static CompiledSourceForBinaryQuery instance = null;
    static FileObject projDir = null;
    
    static
    {
        TestUtil.setLookup( new Object[] {            
        }, CompiledSourceForBinaryQueryTest.class.getClassLoader());
    }
    
    public CompiledSourceForBinaryQueryTest(String testName) {
        super(testName);
        
        TestUtil.setEnv();
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File workDir = getWorkDir();
        File proj = new File(workDir, "testProject");
        
        System.setProperty("netbeans.user","test/tiredTester");
        
        
        aph = J2MEProjectGenerator.createNewProject(proj, "testProject", null, null,null);
        projDir=FileUtil.toFileObject(proj);
        Project p=ProjectManager.getDefault().findProject(projDir);
        assertNotNull(p);
        projDir.getParent().createFolder("src2");
        instance = p.getLookup().lookup(CompiledSourceForBinaryQuery.class);
        assertNotNull(instance);
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("libs.classpath","../src2");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
        
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CompiledSourceForBinaryQueryTest.class);
        
        return suite;
    }
    
    /**
     * Test of findSourceRoots method, of class org.netbeans.modules.mobility.project.queries.CompiledSourceForBinaryQuery.
     */
    /*
    public void testFindSourceRoots() throws Exception {
        System.out.println("findSourceRoots");
        URL projRoot=null;
        URL binRoot=null;
        try {
            projRoot = projDir.getURL();
            binRoot=aph.resolveFile("dist").toURL();
        } catch (FileStateInvalidException ex) {
            ex.printStackTrace();
            fail("Can't get URL");
        }
        
        SourceForBinaryQuery.Result result = instance.findSourceRoots(projRoot);
        assertNotNull(result);        
        FileObject obj[]=result.getRoots();
        assertNotNull(obj);       
        assertTrue(obj.length==0);
        
        
        result = instance.findSourceRoots(binRoot);
        assertNotNull(result);        
        obj=result.getRoots();
        assertNotNull(obj);       
        assertTrue(obj.length>0);
        
        
        assertEquals(obj[0],projDir.getFileObject("src"));
        
        //Improve test coverage
        ChangeListener list=new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            }
        };
        result.addChangeListener(list);
        result.removeChangeListener(null);
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("src.dir","../src2");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
        //result.removeChangeListener(list);
    }
     */
    
    public void testSourceLevelQueryImpl() throws Exception {
        J2MEProject p=(J2MEProject)ProjectManager.getDefault().findProject(projDir);
        SourceLevelQueryImpl simpl=p.getLookup().lookup(SourceLevelQueryImpl.class);
        String ver=simpl.getSourceLevel(null);
        assertEquals(ver,"1.3");
    }
    
}
