/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;

import org.netbeans.api.project.TestUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class OpenProjectsTrampolineImplTest extends NbTestCase {
    
    public OpenProjectsTrampolineImplTest(String name) {
        super( name );
    }
    
    
    private FileObject scratch;
    private FileObject goodproject;
    private FileObject goodproject2;
    // private FileObject badproject;
    // private FileObject mysteryproject;
    private ProjectManager pm;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        goodproject = scratch.createFolder("good");
        goodproject.createFolder("testproject");
        goodproject2 = scratch.createFolder("good2");
        goodproject2.createFolder("testproject");
//        badproject = scratch.createFolder("bad");
//        badproject.createFolder("testproject").createData("broken");
//        mysteryproject = scratch.createFolder("mystery");
        TestUtil.setLookup(Lookups.singleton(TestUtil.testProjectFactory()));
        pm = ProjectManager.getDefault();
    }
    
    protected void tearDown() throws Exception {
        scratch = null;
        goodproject = null;
//        badproject = null;
//        mysteryproject = null;
        pm = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }
    
    public void testOpenProjects() throws Exception {
    
        OpenProjectsTrampolineImpl trampoline = new OpenProjectsTrampolineImpl();
        TestPropertyChangeListener tpchl = new TestPropertyChangeListener();
        trampoline.addPropertyChangeListenerAPI( tpchl );
        
        Project[] projects = trampoline.getOpenProjectsAPI();
        
        assertEquals( "No project should be open.", 0, projects.length );
        assertEquals( "No events.", 0, tpchl.getEvents().size() );
        
        Project p1 = null;
        
        try {
            p1 = pm.findProject( goodproject );
        } catch ( IOException e ) {
            fail("Should not fail to load goodproject: " + e);
        }
        
        assertNotNull( "Project should not be null", p1 );
        
        OpenProjectList.getDefault().open( p1 );
        projects = trampoline.getOpenProjectsAPI();
        
        assertEquals( "One project should be open.", 1, projects.length );
        assertEquals( "Obe event.", 1, tpchl.getEvents().size() );
        
        Project p2 = null;
        
        try {
            p2 = pm.findProject( goodproject2 );
        } catch ( IOException e ) {
            fail("Should not fail to load goodproject: " + e);
        }
        
        assertNotNull( "Project should not be null", p2 );
        
        OpenProjectList.getDefault().open( p2 );
        projects = trampoline.getOpenProjectsAPI();
        assertEquals( "Two projects should be open.", 2, projects.length );
        assertEquals( "Two events.", 2, tpchl.getEvents().size() );
        
        OpenProjectList.getDefault().close(new Project[]{p1});
        projects = trampoline.getOpenProjectsAPI();
        assertEquals( "Two projects should be open.", 1, projects.length );
        assertEquals( "Two events.", 3, tpchl.getEvents().size() );
        
        
        OpenProjectList.getDefault().close(new Project[]{p2});
        projects = trampoline.getOpenProjectsAPI();
        assertEquals( "Two projects should be open.", 0, projects.length );
        assertEquals( "Two events.", 4, tpchl.getEvents().size() );
                
    }

    
    private static class TestPropertyChangeListener implements PropertyChangeListener {
        
        List events = new ArrayList();
        
        public void propertyChange( PropertyChangeEvent e ) {
            events.add( e );
        }
        
        void clear() {
            events.clear();
        }
        
        List getEvents() {
            return events;
        }
                
    }
    
}
