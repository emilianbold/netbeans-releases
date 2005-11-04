/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class OpenProjectsTest extends NbTestCase {
    
    private FileObject scratch;
    private FileObject testProjectFolder;
    private Project testProject;
    
    public OpenProjectsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        scratch = TestUtil.makeScratchDir(this);
        TestUtil.setLookup(new Object[] {
            TestUtil.testProjectFactory(),
        });
        
        assertNotNull(testProjectFolder = scratch.createFolder("test"));
        assertNotNull(testProjectFolder.createFolder("testproject"));
        
        testProject = ProjectManager.getDefault().findProject(testProjectFolder);
        
        assertNotNull(testProject);
    }

    public void testListenersNotified() throws Exception {
        PropertyChangeListenerImpl l = new PropertyChangeListenerImpl();
        
        OpenProjects.getDefault().addPropertyChangeListener(l);
        
        assertEquals(0, l.events.size());
        
        OpenProjects.getDefault().open(new Project[] {testProject}, false);
        
        assertEquals(1, l.events.size());
        
        PropertyChangeEvent e = (PropertyChangeEvent) l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_OPEN_PROJECTS, e.getPropertyName());
        
        OpenProjects.getDefault().close(new Project[] {testProject});
        
        assertEquals(1, l.events.size());
        
        e = (PropertyChangeEvent) l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_OPEN_PROJECTS, e.getPropertyName());
    }
    
    private static final class PropertyChangeListenerImpl implements PropertyChangeListener {
        
        private List events = new ArrayList();
        
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt);
        }
        
    }
    
}
