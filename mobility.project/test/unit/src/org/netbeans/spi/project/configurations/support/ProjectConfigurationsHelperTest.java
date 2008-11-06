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
 * ProjectConfigurationsHelperTest.java
 * JUnit based test
 *
 * Created on 08 February 2006, 18:21
 */
package org.netbeans.spi.project.configurations.support;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import junit.framework.*;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author lukas
 */
public class ProjectConfigurationsHelperTest extends NbTestCase {
    
    static ProjectConfigurationsHelper instance = null;
    static AntProjectHelper aph=null;
    static final Object syncObj=new Object();
    static
    {
        TestUtil.setLookup( new Object[] {            
        }, ProjectConfigurationsHelperTest.class.getClassLoader());
        
        Logger.getLogger("org.openide.util.RequestProcessor").addHandler(new Handler() {
                public void publish(LogRecord record) {
                    String s=record.getMessage();
                    if (s==null)
                        return;
                    if (s.startsWith("Work finished") &&
                            s.indexOf("J2MEProject$6")!=-1 &&
                            s.indexOf("RequestProcessor")!=-1) {
                        synchronized (syncObj) {
                            syncObj.notify();
                        }
                    }
                }
                public void flush() {}
                public void close() throws SecurityException {}
            });
    }
    
    public ProjectConfigurationsHelperTest(String testName) {
        super(testName);
        TestUtil.setEnv();
    }
    
    void waitFinished(File proj) {
        File f = new File (new File (proj, "nbproject"), "project.properties");
        int ct=0;
        try {
            while (!f.exists() && ct++ < 200) {
                Thread.sleep(100);
            }
            Thread.sleep(500);
        } catch (InterruptedException interruptedException) {
            throw new RuntimeException (interruptedException);
        }
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File workDir = getWorkDir();
        File proj = new File(workDir, "testProject");
        
        System.setProperty("netbeans.user","test/tiredTester");
        
        synchronized(syncObj) {
            aph = J2MEProjectGenerator.createNewProject(proj, "testProject", null, null,null);
            waitFinished(proj);
        }
        Project p=ProjectManager.getDefault().findProject(FileUtil.toFileObject(proj));
        assertNotNull(p);
        instance = p.getLookup().lookup(ProjectConfigurationsHelper.class);
        assertNotNull(instance);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ProjectConfigurationsHelperTest.class);
        
        return suite;
    }
    
    /**
     * Test of getDefaultConfiguration method, of class org.netbeans.spi.project.configurations.support.ProjectConfigurationsHelper.
     */
    public void testGetDefaultConfiguration() {
        System.out.println("getDefaultConfiguration");
        
        ProjectConfiguration result = instance.getDefaultConfiguration();
        assertEquals(result.getDisplayName(), "DefaultConfiguration");
    }
    
    /**
     * Test of addConfiguration method, of class org.netbeans.spi.project.configurations.support.ProjectConfigurationsHelper.
     */
    public void testAddConfiguration() {
        System.out.println("addConfiguration");
        
        boolean result = instance.addConfiguration("MyConfig");
        assertTrue(result);
    }
    
    /**
     * Test of getActiveAbilities method, of class org.netbeans.spi.project.configurations.support.ProjectConfigurationsHelper.
     */
    public void testGetActiveAbilities() {
        System.out.println("getActiveAbilities");
        
        
        Map result = instance.getActiveAbilities();
        assertTrue(result.size()==1);
        assertEquals(result.get("DebugLevel"), "debug");
    }
    
    /**
     * Test of getAllIdentifiers method, of class org.netbeans.spi.project.configurations.support.ProjectConfigurationsHelper.
     */
    public void testGetAllIdentifiers() {
        System.out.println("getAllIdentifiers");
        
        boolean includeConfigNames = true;
        
        instance.addConfiguration("MyCFG");
        
        Set result = instance.getAllIdentifiers(includeConfigNames);
        assertTrue(result.size()==2);
        assertTrue(result.remove("MyCFG"));
        assertTrue(result.remove("DefaultConfiguration"));
    }
    
    /**
     * Test of removeConfiguration method, of class org.netbeans.spi.project.configurations.support.ProjectConfigurationsHelper.
     */
    public void testRemoveConfiguration() {
        System.out.println("removeConfiguration");
        
        assertTrue(instance.addConfiguration("MyCFG"));
        Collection<ProjectConfiguration> result = instance.getConfigurations();
        assertTrue(result.size()==2);
        assertTrue(instance.removeConfiguration(new ProjectConfiguration() {
            public String getDisplayName() {
                return "MyCFG";
            }
        }));
        result = instance.getConfigurations();
        assertTrue(result.size()==1);
        assertEquals(result.iterator().next().getDisplayName(),"DefaultConfiguration");
    }
    
    
    /**
     * Test of getConfigurationByName method, of class org.netbeans.spi.project.configurations.support.ProjectConfigurationsHelper.
     */
    public void testGetConfigurationByName() {
        System.out.println("getConfigurationByName");
        
        ProjectConfiguration result = instance.getConfigurationByName("MyCFG");
        assertNull(result);
        assertTrue(instance.addConfiguration("MyCFG"));
        result = instance.getConfigurationByName("MyCFG");
        assertNotNull(result);
        assertEquals(result.getDisplayName(),"MyCFG");
    }
    
    
    /**
     * Test of setActiveConfiguration method, of class org.netbeans.spi.project.configurations.support.ProjectConfigurationsHelper.
     */
    public void testSetActiveConfiguration() {
        System.out.println("setActiveConfiguration");
        assertTrue(instance.addConfiguration("MyCFG_1"));
        assertTrue(instance.addConfiguration("MyCFG_2"));
        assertTrue(instance.addConfiguration("MyCFG_3"));
        try {
            instance.setActiveConfiguration(new ProjectConfiguration() {
                public String getDisplayName() {
                    return "MyCFG_2";
                }
            });
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        assertEquals(instance.getActiveConfiguration().getDisplayName(),"MyCFG_2");
    }
}
