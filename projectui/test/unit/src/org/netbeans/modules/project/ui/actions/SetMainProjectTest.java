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

package org.netbeans.modules.project.ui.actions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.actions.ProjectActionTest.ActionCreator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class SetMainProjectTest extends NbTestCase {
    
    public SetMainProjectTest(String name) {
        super( name );
    }

    public void setUp() throws Exception {
        super.setUp();

        MockServices.setServices(TestSupport.TestProjectFactory.class);
        clearWorkDir ();
    }
    
    public boolean runInEQ () {
        return true;
    }
    
    public void testAcceleratorsPropagated() {
        ProjectActionTest.doTestAcceleratorsPropagated(new ActionCreator() {
            public ProjectAction create(Lookup l) {
                return new SetMainProject(l);
            }
        }, false);
    }
    
    public void test70368() {
        SetMainProject a = new SetMainProject();
        WeakReference<?> ref = new WeakReference<Object>(a);
        
        a = null;
        
        assertGC("SetMainProject action's instance can be freed:", ref);
    }
    
    public void test70835() throws IOException {
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        
        assertNotNull(workDir);
        
        FileObject f1 = TestSupport.createTestProject (workDir, "project1");
        FileObject f2 = TestSupport.createTestProject (workDir, "project2");
        
        assertNotNull(f1);
        assertNotNull(f2);
        
        Project p1 = ProjectManager.getDefault().findProject(f1);
        Project p2 = ProjectManager.getDefault().findProject(f2);
        
        assertNotNull(p1);
        assertNotNull(p2);
        
        OpenProjectList.getDefault().open(new Project[] {p1, p2}, false);
        
        SetMainProject a = new SetMainProject();
        
        JMenuItem item = a.getMenuPresenter();
        
        assertTrue(item instanceof JMenu);
        
        JMenu menu = (JMenu) item;
        
        item = null;
        
        assertEquals(2, menu.getItemCount());
        assertTrue(menu.isEnabled());
        
        WeakReference<?> menuRef = new WeakReference<Object>(menu);
        WeakReference<?> actionRef = new WeakReference<Object>(a);
        
        a = null;
        
        try {
            assertGC("", actionRef);
        } catch (Error e) {
            //ignore....
        }
        
        OpenProjectList.getDefault().close(new Project[] {p1});
        
        assertEquals(1, menu.getItemCount());
        assertTrue(menu.isEnabled());

        OpenProjectList.getDefault().close(new Project[] {p2});
        
        assertEquals(0, menu.getItemCount());
        assertFalse(menu.isEnabled());

        OpenProjectList.getDefault().open(new Project[] {p1}, false);
        
        assertEquals(1, menu.getItemCount());
        assertTrue(menu.isEnabled());
        
        menu = null;
        
        assertGC("", menuRef);
        assertGC("", actionRef);
    }
}
