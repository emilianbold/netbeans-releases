/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.modules.masterfs.MasterFileSystem;
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
        assertNotNull(MasterFileSystem.settingsFactory(null));
        
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
    
    void waitFinished()
    {
        while (true)
        {
            try   {
                syncObj.wait();
                break;
            }
            catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File workDir = getWorkDir();
        File proj = new File(workDir, "testProject");
        
        System.setProperty("netbeans.user","test/tiredTester");
        
        synchronized(syncObj) {
            aph = J2MEProjectGenerator.createNewProject(proj, "testProject", null, null,null);
            waitFinished();
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
