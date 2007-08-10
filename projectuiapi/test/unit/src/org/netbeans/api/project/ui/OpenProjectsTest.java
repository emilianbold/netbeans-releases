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

package org.netbeans.api.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
        
        PropertyChangeEvent e = l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_OPEN_PROJECTS, e.getPropertyName());
        assertFalse(Arrays.asList((Project[])e.getOldValue()).contains(testProject));
        assertTrue(Arrays.asList((Project[])e.getNewValue()).contains(testProject));
        
        OpenProjects.getDefault().setMainProject(testProject);
        
        assertEquals(1, l.events.size());
        
        e = l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_MAIN_PROJECT, e.getPropertyName());
        
        OpenProjects.getDefault().setMainProject(null);
        
        assertEquals(1, l.events.size());
        
        e = l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_MAIN_PROJECT, e.getPropertyName());
        
        OpenProjects.getDefault().close(new Project[] {testProject});
        
        assertEquals(1, l.events.size());
        
        e = l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_OPEN_PROJECTS, e.getPropertyName());
        assertTrue(Arrays.asList((Project[])e.getOldValue()).contains(testProject));
        assertFalse(Arrays.asList((Project[])e.getNewValue()).contains(testProject));
    }
    
    
    private static final class PropertyChangeListenerImpl implements PropertyChangeListener {
        
        private List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt);
        }
        
    }
    
}
