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

package org.netbeans.spi.project.ui.support;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author Jan Lahoda
 */
public class ProjectCustomizerTest extends NbTestCase {
    
    public ProjectCustomizerTest(String testName) {
        super(testName);
    }

    public void testCategoriesAreReclaimable() throws Exception {
        final Reference<?>[] refs = new Reference<?>[4];
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Category test1 = Category.create("test1", "test1", null);
                Category test2 = Category.create("test2", "test3", null, test1);
                Category test3 = Category.create("test3", "test3", null);
                refs[1] = new WeakReference<Object>(test1);
                refs[2] = new WeakReference<Object>(test2);
                refs[3] = new WeakReference<Object>(test3);
                Dialog d = ProjectCustomizer.createCustomizerDialog(new Category[] {test2, test3}, new CategoryComponentProvider() {
                    public JComponent create(Category category) {
                        return new JPanel();
                    }
                }, null, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //ignore
                    }
                }, HelpCtx.DEFAULT_HELP);
                d.dispose();
                refs[0] = new WeakReference<Object>(d);
            }
        });
        
        for (Reference<?> ref : refs) {
            assertGC("Is reclaimable", ref);
        }
    }
    
    public void testReadCategories() {
        // creating direcotry structure on System FileSystem
        FileObject projectFO = FileUtil.getConfigFile("Projects");
        if (projectFO == null) {
            try {
                projectFO = FileUtil.getConfigRoot().createFolder("Projects");
            } catch (IOException ex) {
                fail("Cannot create 'Projects' folder.");
            }
        }
        FileObject j2seprojectFO = projectFO.getFileObject("org-netbeans-modules-java-j2seproject");
        if (j2seprojectFO == null) {
            try {
                j2seprojectFO = projectFO.createFolder("org-netbeans-modules-java-j2seproject");
            } catch (IOException ex) {
                fail("Cannot create 'org-netbeans-modules-java-j2seproject' folder.");
            }
        }
        FileObject customizerFO = j2seprojectFO.getFileObject("Customizer");
        if (customizerFO == null) {
            try {
                customizerFO = j2seprojectFO.createFolder("Customizer");
            } catch (IOException ex) {
                fail("Cannot create 'Customizer' folder.");
            }
        }
        try {
            // category folder
            FileObject testFO = customizerFO.createFolder("TestCategory");
            DataFolder testDataFolder = DataFolder.findFolder(testFO);
            // category instance file
            DataObject instance = InstanceDataObject.create(testDataFolder, "Self", 
                    "org.netbeans.spi.project.ui.support.ProjectCustomizerTest$TestCompositeCategoryProvider");
        } catch (IOException ex) {
            fail("Cannot create category folder.");
        }
        ProjectCustomizer.DelegateCategoryProvider dcp = new ProjectCustomizer.DelegateCategoryProvider(DataFolder.findFolder(customizerFO), null);
        ProjectCustomizer.Category categories[] = null;
        try {
            categories = dcp.readCategories(DataFolder.findFolder(customizerFO));
        } catch (Exception ex) {
            fail("Reading of categories failed.");
        }
        assertNotNull(categories);
        assertEquals(1, categories.length);
        assertEquals("TestCategory", categories[0].getDisplayName());
        JComponent jc = dcp.create(categories[0]);
        assertTrue(jc instanceof JButton);
    }
    
    public static class TestCompositeCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
        public ProjectCustomizer.Category createCategory(Lookup context) {
            return ProjectCustomizer.Category.create("testCategory", "TestCategory", null);
        }
        public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
            return new JButton();
        }
    }
    
}
