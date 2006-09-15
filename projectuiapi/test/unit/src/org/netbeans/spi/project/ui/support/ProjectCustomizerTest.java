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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;
import org.openide.util.HelpCtx;

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
    
}
