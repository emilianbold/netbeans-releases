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

package org.netbeans.spi.project.ui.support;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
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

    private Reference<?>[] runTestCategoriesAreReclaimable() throws Exception {
        final Reference<?>[] result = new Reference<?>[4];
              Category test1 = Category.create("test1", "test1", null);
        final Category test2 = Category.create("test2", "test3", null, test1);
        final Category test3 = Category.create("test3", "test3", null);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Dialog d = ProjectCustomizer.createCustomizerDialog(new Category[] {test2, test3}, new CategoryComponentProviderImpl(), null, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //ignore
                    }
                }, HelpCtx.DEFAULT_HELP);
                
                d.setVisible(true);
                
                try {
                    Thread.currentThread().sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                
                d.setVisible(false);
                d.dispose();
                
                result[0] = new WeakReference<Object>(d);
                
                d = null;
                
            }
        });
        
        //the dialog may be still strongly hold by the Swing/AWT, make it disappear:
        Thread.sleep(1000);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JFrame f = new JFrame("test");
                
                f.setVisible(true);
                
                JDialog d = new JDialog(f, false);
                
                d.setVisible(true);
                
                try {
                    Thread.currentThread().sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                
                d.setVisible(false);
                d.dispose();
                
                f.setVisible(false);
                f.dispose();
                
                d = null;
            }
        });
                
        result[1] = new WeakReference<Object>(test1);
        result[2] = new WeakReference<Object>(test2);
        result[3] = new WeakReference<Object>(test3);
        
        return result;
    }
    
    public void testCategoriesAreReclaimable() throws Exception {
        for (Reference<?> ref : runTestCategoriesAreReclaimable()) {
            assertGC("Is reclaimable", ref);
        }
    }
    
    private static final class CategoryComponentProviderImpl implements CategoryComponentProvider {
        
        public JComponent create(Category category) {
            return new JPanel();
        }
        
    }
    
    public void testReadCategories() {
        FileSystem sysFS = Repository.getDefault().getDefaultFileSystem();
        // creating direcotry structure on System FileSystem
        FileObject projectFO = sysFS.findResource("Projects");
        if (projectFO == null) {
            FileObject rootFO = sysFS.getRoot();
            try {
                projectFO = rootFO.createFolder("Projects");
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
            return ProjectCustomizer.Category.create("testCategory", "TestCategory", null, null);
        }
        public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
            return new JButton();
        }
    }
    
}
