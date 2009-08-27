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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.DelegateCategoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
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
    
    public void testReadCategories() throws Exception {
        FileObject customizerFO = FileUtil.createFolder(FileUtil.getConfigRoot(), "Projects/test/Customizer");
        FileObject testFO = customizerFO.createFolder("TestCategory");
        testFO.setAttribute("displayName", "Test Category");
        InstanceDataObject.create(DataFolder.findFolder(testFO), "Self", TestCompositeCategoryProvider.class);
        DelegateCategoryProvider dcp = new DelegateCategoryProvider(DataFolder.findFolder(customizerFO), null);
        Category categories[] = dcp.readCategories(DataFolder.findFolder(customizerFO));
        assertNotNull(categories);
        assertEquals(1, categories.length);
        assertEquals("TestCategory", categories[0].getDisplayName());
        /* XXX does not work yet because ExternalUtil.MainFS does not provide a FS.Status:
        assertEquals("Test Category", categories[0].getDisplayName());
         */
        JComponent jc = dcp.create(categories[0]);
        assertTrue(jc instanceof JButton);
    }

    public static class TestCompositeCategoryProvider implements CompositeCategoryProvider {
        public Category createCategory(Lookup context) {
            throw new AssertionError();
        }
        public JComponent createComponent(Category category, Lookup context) {
            return new JButton();
        }
    }
    
}
