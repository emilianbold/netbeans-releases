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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import junit.framework.TestCase;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class CloseProjectTest extends NbTestCase implements PropertyChangeListener {
    private int change;
    
    public CloseProjectTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of actionPerformed method, of class CloseProject.
     */
    public void testNoNeedToRefreshWhenNotVisible() throws IOException {
        InstanceContent ic = new InstanceContent();
        Lookup context = new AbstractLookup(ic);
        CloseProject instance = new CloseProject(context);
        
        CharSequence log1 = Log.enable("org.netbeans.modules.project.ui.actions", Level.FINER);
        assertFalse("Disabled", instance.isEnabled());
        if (!log1.toString().contains("Refreshing")) {
            fail("Should be refreshing: " + log1);
        }
        
        JMenuItem item = instance.getPopupPresenter();
        item.addPropertyChangeListener("enabled", this);
        /*
        item.addNotify();
        JFrame f = new JFrame();
        JMenuBar b = new JMenuBar();
        JMenu m = new JMenu();
        b.add(m);
        m.add(item);
        f.setJMenuBar(b);
        f.pack();
        f.setVisible(true);
         */
        
        assertFalse("Not enabled", item.isEnabled());
        FileObject pfo = TestSupport.createTestProject(FileUtil.createMemoryFileSystem().getRoot(), "yaya");
        FileObject pf2 = TestSupport.createTestProject(FileUtil.createMemoryFileSystem().getRoot(), "blabla");
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        Project p = ProjectManager.getDefault().findProject(pfo);
        Project p2 = ProjectManager.getDefault().findProject(pf2);
        assertNotNull("Project found", p);
        assertNotNull("Project2 found", p2);
        OpenProjects.getDefault().open(new Project[] { p }, false);
        ic.add(p);
        assertTrue("enabled", item.isEnabled());
        assertEquals("One change", 1, change);
        
        item.removeNotify();
        
        CharSequence log2 = Log.enable("org.netbeans.modules.project.ui.actions", Level.FINER);
        ic.remove(p);
        ic.add(p2);
        
        if (log2.length() > 0) {
            fail("Nothing shall happen:\n" + log2);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        change++;
    }

}
