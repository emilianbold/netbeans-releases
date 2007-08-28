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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.services;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/** Tests issue 96282 - Memory leak in org.netbeans.core.windows.services.NbPresenter
 *
 * @author Jiri Rechtacek
 */
public class NbPresenterLeakTest extends NbTestCase {

    public NbPresenterLeakTest (String testName) {
        super (testName);
    }

    protected boolean runInEQ () {
        return false;
    }

    public void testLeakingNbPresenterDescriptor () throws InterruptedException, InvocationTargetException {
        WizardDescriptor wizardDescriptor = new WizardDescriptor (getPanels ());
        wizardDescriptor.setModal (false);
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        WeakReference<WizardDescriptor> w = new WeakReference<WizardDescriptor> (wizardDescriptor);
        
        SwingUtilities.invokeAndWait (new EDTJob(dialog, true));
        SwingUtilities.invokeAndWait (new EDTJob(dialog, false));
        boolean cancelled = wizardDescriptor.getValue() !=
            WizardDescriptor.FINISH_OPTION;
        Dialog d = new JDialog();
        
        // workaround for JDK bug 6575402
        JPanel p = new JPanel();
        d.setLayout(new BorderLayout());
        d.add(p, BorderLayout.CENTER);
        JButton btn = new JButton("Button");
        p.add(btn, BorderLayout.NORTH);
        
        SwingUtilities.invokeAndWait (new EDTJob(d, true));
        SwingUtilities.invokeAndWait (new EDTJob(d, false));

        //assertNull ("BufferStrategy was disposed.", dialog.getBufferStrategy ());
        
        dialog = null;
        wizardDescriptor = null;
        
        assertGC ("Dialog disappears.", w);
    }
    
    private static class EDTJob implements Runnable {
        private Dialog d;
        private boolean visibility;
        
        EDTJob (Dialog d, boolean vis) {
            this.d = d;
            visibility = vis;
        }
        public void run() {
            d.setVisible(visibility);
            if (!visibility) {
                d.dispose();
            }
        }
    }
    
    private WizardDescriptor.Panel [] getPanels () {
        WizardDescriptor.Panel p1 = new WizardDescriptor.Panel () {
            public Component getComponent() {
                return new JLabel ("test");
            }

            public HelpCtx getHelp() {
                return null;
            }

            public void readSettings(Object settings) {
            }

            public void storeSettings(Object settings) {
            }

            public boolean isValid() {
                return true;
            }

            public void addChangeListener(ChangeListener l) {
            }

            public void removeChangeListener(ChangeListener l) {
            }
        };
        
        return new WizardDescriptor.Panel [] {p1};
    }
    
    
    
}
