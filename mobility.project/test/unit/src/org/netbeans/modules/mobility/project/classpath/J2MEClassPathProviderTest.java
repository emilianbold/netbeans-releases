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
 * J2MEClassPathProviderTest.java
 * JUnit based test
 *
 * Created on 09 February 2006, 16:58
 */
package org.netbeans.modules.mobility.project.classpath;

import java.io.File;
import java.util.List;
import junit.framework.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lukas
 */
public class J2MEClassPathProviderTest extends NbTestCase {
    static AntProjectHelper aph=null;
    static J2MEClassPathProvider instance = null;
    static FileObject projDir = null;
    
    static {
        TestUtil.setLookup(new Object[]{}, J2MEClassPathProviderTest.class.getClassLoader());
    }
    
    public J2MEClassPathProviderTest(String testName) {
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
        instance = p.getLookup().lookup(J2MEClassPathProvider.class);
        assertNotNull(instance);
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("libs.classpath","build");
        ep.setProperty("platform.active","test");
        ep.setProperty("platforms.test.home","home");
        ep.setProperty("platform.bootclasspath","${platform.home}/bin");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(J2MEClassPathProviderTest.class);
        
        return suite;
    }
    
    /**
     * Test of findClassPath method, of class org.netbeans.modules.mobility.project.classpath.J2MEClassPathProvider.
     */
    public void testFindClassPath() throws Exception {
        List entries=null;
        System.out.println("findClassPath");
        FileObject file=projDir.getFileObject("src").createData("Test.java");
        
        ClassPath result = instance.findClassPath(file, ClassPath.BOOT);
        entries=result.entries();
        assertTrue(entries.size()==1);
        ClassPath.Entry entry=(ClassPath.Entry)entries.get(0);
        String s1=(FileUtil.normalizeFile(new File(System.getProperty("platform.home"))).toURL().toString()+"bin/").replaceAll(" ","%20");
        String s2=entry.getURL().toString();
        assertEquals(s1,s2);
        
        result = instance.findClassPath(file, ClassPath.COMPILE);
        entries=result.entries();
        assertTrue(entries.size()==1);
        entry=(ClassPath.Entry)entries.get(0);
        s1=(projDir.getURL().toString()+"build/").replaceAll(" ","%20");
        s2=entry.getURL().toString();
        assertEquals(s1,s2);
        
        result = instance.findClassPath(file, ClassPath.EXECUTE);
        entries=result.entries();
        assertTrue(entries.size()==1);
        entry=(ClassPath.Entry)entries.get(0);
        s1=(projDir.getURL().toString()+"build/compiled/").replaceAll(" ","%20");
        s2=entry.getURL().toString();
        assertEquals(s1,s2);
        
        result = instance.findClassPath(file, ClassPath.SOURCE);
        entries=result.entries();
        assertTrue(entries.size()==1);
        entry=(ClassPath.Entry)entries.get(0);
        s1=(projDir.getURL().toString()+"src/").replaceAll(" ","%20");
        s2=entry.getURL().toString();
        assertTrue(s1.startsWith(s2));
    }
}
