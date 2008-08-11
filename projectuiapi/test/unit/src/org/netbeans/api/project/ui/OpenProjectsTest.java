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

package org.netbeans.api.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Override
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
        OpenProjects.getDefault().openProjects().get();

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
