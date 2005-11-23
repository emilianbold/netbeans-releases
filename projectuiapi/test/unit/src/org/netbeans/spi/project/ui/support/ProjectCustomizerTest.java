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

package org.netbeans.spi.project.ui.support;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;


/**
 *
 * @author Jan Lahoda
 */
public class ProjectCustomizerTest extends NbTestCase {
    
    public ProjectCustomizerTest(String testName) {
        super(testName);
    }

    private WeakReference[] runTestCategoriesAreReclaimable() throws Exception {
        final WeakReference[] result = new WeakReference[2];
        final Category test = Category.create("test", "test", null, null);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Dialog d = ProjectCustomizer.createCustomizerDialog(new Category[] {test}, new CategoryComponentProviderImpl(), null, new ActionListener() {
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
                
                result[0] = new WeakReference(d);
                
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
                
        result[1] = new WeakReference(test);
        
        return result;
    }
    
    public void testCategoriesAreReclaimable() throws Exception {
        final WeakReference[] refs = runTestCategoriesAreReclaimable();
                
        assertGC("Category is reclaimable", refs[1]);
        assertGC("Dialog is reclaimable", refs[0]);
    }
    
    private static final class CategoryComponentProviderImpl implements CategoryComponentProvider {
        
        public JComponent create(Category category) {
            return new JPanel();
        }
        
    }
    
}
