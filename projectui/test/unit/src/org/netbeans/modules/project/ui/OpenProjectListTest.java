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

package org.netbeans.modules.project.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.lookup.Lookups;

/** Tests fix of issue 56454.
 *
 * @author Jiri Rechtacek
 */
public class OpenProjectListTest extends NbTestCase {
    FileObject f1_1_open, f1_2_open, f1_3_close;
    FileObject f2_1_open;

    Project project1, project2;
    TestOpenCloseProjectDocument handler = new OpenProjectListTest.TestOpenCloseProjectDocument ();

    public OpenProjectListTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        super.setUp ();
        TestUtil.setLookup(new Object[] { 
            TestSupport.testProjectFactory(),
            TestSupport.createAuxiliaryConfiguration(),
        });
        clearWorkDir ();
        
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL = handler;
        
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
    
        FileObject p1 = TestSupport.createTestProject (workDir, "project1");
        f1_1_open = p1.createData("f1_1.java");
        f1_2_open = p1.createData("f1_2.java");
        f1_3_close = p1.createData("f1_3.java");

        project1 = ProjectManager.getDefault ().findProject (p1);
        ((TestSupport.TestProject) project1).setLookup (Lookups.singleton (TestSupport.createAuxiliaryConfiguration ()));
        
        FileObject p2 = TestSupport.createTestProject (workDir, "project2");
        f2_1_open = p2.createData ("f2_1.java");

        // project2 depends on projects1
        project2 = ProjectManager.getDefault ().findProject (p2);
        ((TestSupport.TestProject) project2).setLookup (Lookups.fixed (new Object[] { TestSupport.createAuxiliaryConfiguration (), new MySubprojectProvider (project1) } ));
        
        // prepare set of open documents for both projects
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f1_1_open);
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f1_2_open);
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f2_1_open);
        
        // close both projects with own open files
        ProjectUtilities.closeAllDocuments (new Project [] { project1, project2 });
        OpenProjectList.getDefault ().close (new Project [] { project1, project2 });
    }
    
    protected void tearDown () {
        OpenProjectList.getDefault ().close (new Project [] { project1, project2 });
    }

    public void testOpen () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0);        
        OpenProjectList.getDefault ().open (project1, true);        
        assertTrue ("Project1 is opened.", OpenProjectList.getDefault ().isOpen (project1));
        
        assertTrue ("Document f1_1_open is loaded.", handler.openFiles.contains (f1_1_open.getURL ().toExternalForm ()));
        assertTrue ("Document f1_2_open is loaded.", handler.openFiles.contains (f1_2_open.getURL ().toExternalForm ()));
        assertFalse ("Document f2_1_open isn't loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
    }
    
    public void testClose () throws Exception {
        testOpen ();
        
        ProjectUtilities.closeAllDocuments (new Project [] { project1 });
        OpenProjectList.getDefault ().close (new Project [] { project1 });
        assertFalse ("Document f1_1_open isn't loaded.", handler.openFiles.contains (f1_1_open.getURL ().toExternalForm ()));
        assertFalse ("Document f1_2_open isn't loaded.", handler.openFiles.contains (f1_2_open.getURL ().toExternalForm ()));
        assertFalse ("Document f2_1_open isn't loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
        
        OpenProjectList.getDefault ().open (project1);
        OpenProjectList.getDefault ().open (project2);
        
        // close all project1's documents
        handler.openFiles.remove (f1_1_open.getURL ().toExternalForm ());
        handler.openFiles.remove (f1_2_open.getURL ().toExternalForm ());
        
        ProjectUtilities.closeAllDocuments (new Project [] {project1});
        OpenProjectList.getDefault ().close (new Project [] {project1});

        OpenProjectList.getDefault ().open (project1);
        assertFalse ("Document f1_1_open isn't loaded.", handler.openFiles.contains (f1_1_open.getURL ().toExternalForm ()));
        assertFalse ("Document f1_2_open isn't loaded.", handler.openFiles.contains (f1_2_open.getURL ().toExternalForm ()));
        assertTrue ("Document f2_1_open is still loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
    }
    
    public void testOpenDependingProject () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0);        
        OpenProjectList.getDefault ().open (project2, true);        
        assertTrue ("Project1 is opened.", OpenProjectList.getDefault ().isOpen (project1));
        assertTrue ("Project2 is opened.", OpenProjectList.getDefault ().isOpen (project2));
        
        assertTrue ("Document f1_1_open is loaded.", handler.openFiles.contains (f1_1_open.getURL ().toExternalForm ()));
        assertTrue ("Document f1_2_open is loaded.", handler.openFiles.contains (f1_2_open.getURL ().toExternalForm ()));
        assertTrue ("Document f2_1_open is loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
    }

    private static class MySubprojectProvider implements SubprojectProvider {
        Project p;
        public MySubprojectProvider (final Project project) {
            p = project;
        }
        public Set/*<Project>*/ getSubprojects () {
            return Collections.singleton (p);
        }
        
        public void removeChangeListener (javax.swing.event.ChangeListener changeListener) {}
        public void addChangeListener (javax.swing.event.ChangeListener changeListener) {}

    }
    
    private static class TestOpenCloseProjectDocument implements ProjectUtilities.OpenCloseProjectDocument {
        public Set/*<String>*/ openFiles = new HashSet ();
        public Map/*<Project, SortedSet<String>>*/ urls4project = new HashMap ();
        
        public boolean open (FileObject fo) {
            Project owner = FileOwnerQuery.getOwner (fo);
            if (!urls4project.containsKey (owner)) {
              // add project
              urls4project.put (owner, new TreeSet ());
            }
            URL url = null;
            DataObject dobj = null;
            try {
                dobj = DataObject.find (fo);
                url = dobj.getPrimaryFile ().getURL ();
                ((SortedSet)urls4project.get (owner)).add (url.toExternalForm ());
                openFiles.add (fo.getURL ().toExternalForm ());
            } catch (FileStateInvalidException fsie) {
                fail ("FileStateInvalidException in " + dobj.getPrimaryFile ());
            } catch (DataObjectNotFoundException donfe) {
                fail ("DataObjectNotFoundException on " + fo);
            }
            return true;
        }
        
        public Map/*<Project, SortedSet<String>>*/ close (Project[] projects) {
            
            for (int i = 0; i < projects.length; i++) {
                Set projectOpenFiles = (Set) urls4project.get (projects [i]);
                if (projectOpenFiles != null) {
                    projectOpenFiles.retainAll (openFiles);
                    urls4project.put (projects [i], projectOpenFiles);
                    Iterator loop = projectOpenFiles.iterator ();
                    while (loop.hasNext ()) {
                        String url = (String) loop.next ();
                        FileObject fo = null;
                        try {
                            fo = URLMapper.findFileObject (new URL (url));
                            openFiles.remove (fo.getURL ().toExternalForm ());
                        } catch (MalformedURLException mue) {
                            fail ("MalformedURLException in " + url);
                        } catch (FileStateInvalidException fsie) {
                            fail ("FileStateInvalidException in " + fo);
                        }
                    }
                }
            }
            
            return urls4project;
        }
    }
    
}
