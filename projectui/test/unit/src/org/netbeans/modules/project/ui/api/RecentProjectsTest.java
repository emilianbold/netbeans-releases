/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.api;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import junit.framework.*;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.junit.*;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.actions.TestSupport;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 * Tests for RecentProjects class
 * @author Milan Kubec
 */
public class RecentProjectsTest extends NbTestCase {
    
    Project[] testProjects = new Project[15];
    String[] tpDisplayNames = new String[15];
    URL[] tpURLs = new URL[15];
    
    public static final ImageIcon icon = new ImageIcon(RecentProjectsTest.class.getResource("testimage.png"));
    public static final String PRJ_NAME_PREFIX = "Project";
    
    public RecentProjectsTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            TestSupport.testProjectFactory(),
        });
        clearWorkDir();
        FileObject workDirFO = FileUtil.toFileObject(getWorkDir());
        for (int i = 0; i < testProjects.length; i++) {
            String prjName = PRJ_NAME_PREFIX + (i + 1);
            FileObject p = TestSupport.createTestProject(workDirFO, prjName);
            TestSupport.TestProject tp = (TestSupport.TestProject) ProjectManager.getDefault().findProject(p);
            tp.setLookup(Lookups.fixed(new Object[] { new TestProjectInfo(prjName) }));
            testProjects[i] = tp;
            tpDisplayNames[i] = ((ProjectInformation) ProjectUtils.getInformation(tp)).getDisplayName();
            tpURLs[i] = tp.getProjectDirectory().getURL();
        }
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testGetRecentProjectsInformation() {
        
        List pil;
        
        for (int i = 0; i < testProjects.length; i++) {
            OpenProjectList.getDefault().open(testProjects[i], false);
        }
        
        // Close all projects in the list one by one
        for (int j = 0; j < testProjects.length; j++) {
            OpenProjectList.getDefault().close(new Project[] { testProjects[j] });
            pil = RecentProjects.getDefault().getRecentProjectInformation();
            assertEquals(1, RecentProjects.getDefault().getRecentProjectInformation().size());
            assertEquals(tpDisplayNames[j], ((UnloadedProjectInformation) pil.get(0)).getDisplayName());
            assertEquals(tpURLs[j], ((UnloadedProjectInformation) pil.get(0)).getURL());
            assertEquals(icon, ((UnloadedProjectInformation) pil.get(0)).getIcon());
            OpenProjectList.getDefault().open(testProjects[j], false);
        }
        
        assertEquals(0, RecentProjects.getDefault().getRecentProjectInformation().size());
        
        // Close rand number of rand modules
        OpenProjectList.getDefault().close(new Project[] {testProjects[3]});
        OpenProjectList.getDefault().close(new Project[] {testProjects[4]});
        OpenProjectList.getDefault().close(new Project[] {testProjects[6]});
        OpenProjectList.getDefault().close(new Project[] {testProjects[10]});
        OpenProjectList.getDefault().close(new Project[] {testProjects[12]});
        
        pil = RecentProjects.getDefault().getRecentProjectInformation();
        assertEquals(5, RecentProjects.getDefault().getRecentProjectInformation().size());
        
        assertEquals(tpDisplayNames[12], ((UnloadedProjectInformation) pil.get(0)).getDisplayName());
        assertEquals(tpURLs[12], ((UnloadedProjectInformation) pil.get(0)).getURL());
        assertEquals(icon, ((UnloadedProjectInformation) pil.get(0)).getIcon());
        
        assertEquals(tpDisplayNames[10], ((UnloadedProjectInformation) pil.get(1)).getDisplayName());
        assertEquals(tpURLs[10], ((UnloadedProjectInformation) pil.get(1)).getURL());
        assertEquals(icon, ((UnloadedProjectInformation) pil.get(1)).getIcon());
        
        assertEquals(tpDisplayNames[6], ((UnloadedProjectInformation) pil.get(2)).getDisplayName());
        assertEquals(tpURLs[6], ((UnloadedProjectInformation) pil.get(2)).getURL());
        assertEquals(icon, ((UnloadedProjectInformation) pil.get(2)).getIcon());
        
        assertEquals(tpDisplayNames[4], ((UnloadedProjectInformation) pil.get(3)).getDisplayName());
        assertEquals(tpURLs[4], ((UnloadedProjectInformation) pil.get(3)).getURL());
        assertEquals(icon, ((UnloadedProjectInformation) pil.get(3)).getIcon());
        
        assertEquals(tpDisplayNames[3], ((UnloadedProjectInformation) pil.get(4)).getDisplayName());
        assertEquals(tpURLs[3], ((UnloadedProjectInformation) pil.get(4)).getURL());
        assertEquals(icon, ((UnloadedProjectInformation) pil.get(4)).getIcon());
        
        OpenProjectList.getDefault().open(testProjects[3], false);
        OpenProjectList.getDefault().open(testProjects[4], false);
        OpenProjectList.getDefault().open(testProjects[6], false);
        OpenProjectList.getDefault().open(testProjects[10], false);
        OpenProjectList.getDefault().open(testProjects[12], false);
        
        assertEquals(0, RecentProjects.getDefault().getRecentProjectInformation().size());
        
        // Close ten projects
        for (int k = 3; k < 13; k++) {
            OpenProjectList.getDefault().close(new Project[] { testProjects[k] });
        }
        pil = RecentProjects.getDefault().getRecentProjectInformation();
        assertEquals(10, RecentProjects.getDefault().getRecentProjectInformation().size());
        for (int l = 0; l > 10; l++) {
            assertEquals(tpDisplayNames[12 - l], ((UnloadedProjectInformation) pil.get(l)).getDisplayName());
            assertEquals(tpURLs[12 - l], ((UnloadedProjectInformation) pil.get(l)).getURL());
            assertEquals(icon, ((UnloadedProjectInformation) pil.get(l)).getIcon());
        }
        for (int m = 3; m < 13; m++) {
            OpenProjectList.getDefault().open(testProjects[m], false);
        }
        
        assertEquals(0, RecentProjects.getDefault().getRecentProjectInformation().size());
        
        // Open and close more than ten projects
        for (int n = 0; n < testProjects.length; n++) {
            OpenProjectList.getDefault().close(new Project[] { testProjects[n] });
        }
        pil = RecentProjects.getDefault().getRecentProjectInformation();
        assertEquals(10, RecentProjects.getDefault().getRecentProjectInformation().size());
        for (int p = 0; p > 10; p++) {
            assertEquals(tpDisplayNames[testProjects.length - p], ((UnloadedProjectInformation) pil.get(p)).getDisplayName());
            assertEquals(tpURLs[testProjects.length - p], ((UnloadedProjectInformation) pil.get(p)).getURL());
            assertEquals(icon, ((UnloadedProjectInformation) pil.get(p)).getIcon());
        }
        for (int q = 0; q < testProjects.length; q++) {
            OpenProjectList.getDefault().open(testProjects[q], false);
        }
        
        assertEquals(0, RecentProjects.getDefault().getRecentProjectInformation().size());
        
        // close array of projects
        OpenProjectList.getDefault().close(new Project[] { testProjects[2], testProjects[5], testProjects[9], testProjects[11] });
        pil = RecentProjects.getDefault().getRecentProjectInformation();
        assertEquals(4, RecentProjects.getDefault().getRecentProjectInformation().size());
        
        assertEquals(tpDisplayNames[11], ((UnloadedProjectInformation) pil.get(0)).getDisplayName());
        assertEquals(tpURLs[11], ((UnloadedProjectInformation) pil.get(0)).getURL());
        assertEquals(icon, ((UnloadedProjectInformation) pil.get(0)).getIcon());
        
        assertEquals(tpDisplayNames[9], ((UnloadedProjectInformation) pil.get(1)).getDisplayName());
        assertEquals(tpURLs[9], ((UnloadedProjectInformation) pil.get(1)).getURL());
        assertEquals(icon, ((UnloadedProjectInformation) pil.get(1)).getIcon());
        
        assertEquals(tpDisplayNames[5], ((UnloadedProjectInformation) pil.get(2)).getDisplayName());
        assertEquals(tpURLs[5], ((UnloadedProjectInformation) pil.get(2)).getURL());
        assertEquals(icon, ((UnloadedProjectInformation) pil.get(2)).getIcon());
        
        assertEquals(tpDisplayNames[2], ((UnloadedProjectInformation) pil.get(3)).getDisplayName());
        assertEquals(tpURLs[2], ((UnloadedProjectInformation) pil.get(3)).getURL());
        assertEquals(icon, ((UnloadedProjectInformation) pil.get(3)).getIcon());
        
    }
    
    // -------------------------------------------------------------------------
    
    private static class TestProjectInfo implements ProjectInformation {
        
        private String displayName;
        
        public TestProjectInfo(String dname) {
            displayName = dname;
        }
        public String getName() {
            return displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
        public Icon getIcon() {
            return icon;
        }
        public Project getProject() {
            return null;
        }
        public void addPropertyChangeListener(PropertyChangeListener listener) {}
        public void removePropertyChangeListener(PropertyChangeListener listener) {}
        
    }
    
}
