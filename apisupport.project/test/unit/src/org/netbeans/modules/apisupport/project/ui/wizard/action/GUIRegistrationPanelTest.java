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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardPanel;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.WizardDescriptor;

/**
 * @author Martin Krauskopf
 */
public class GUIRegistrationPanelTest extends LayerTestBase {
    
    private JFrame frame;
    private JPanel outerPane;
    private GUIRegistrationPanel regPane;
    private WeakReference regPaneWR;
    private boolean valid;
    
    public GUIRegistrationPanelTest(String testName) {
        super(testName);
    }
    
    public void testMemoryLeak_70032() throws Exception {
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        final WizardDescriptor wd = new WizardDescriptor(new WizardDescriptor.ArrayIterator() {
            public WizardDescriptor.Panel current() { // satisfying WizardDescriptor 1.32 (#76318)
                return new BasicWizardPanel(null) {
                    public Component getComponent() {
                        return new JPanel();
                    }
                };
            }
        });
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT,
                TestBase.generateStandaloneModule(getWorkDir(), "module"));
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                regPane = new GUIRegistrationPanel(wd, new DataModel(wd));
                // let's force checkValidity failed until all data are loaded
                regPane.editorContext.setSelected(true);
                regPane.fileTypeContext.setSelected(true);
                regPane.globalMenuItem.setSelected(true);
                regPane.globalToolbarButton.setSelected(true);
                
                // workaround for inability to GC JFrame/JDialog itself
                outerPane = new JPanel();
                outerPane.add(regPane);
                
                regPaneWR = new WeakReference(regPane);
                
                frame = new JFrame("testMemoryLeak_70032");
                frame.getContentPane().setLayout(new BorderLayout());
                frame.getContentPane().add(new JLabel("<html><font color='blue'>I will close myself" +
                        " as soon as I load all data from the SystemFileSystem</font></html>"), BorderLayout.NORTH);
                frame.getContentPane().add(outerPane, BorderLayout.CENTER);
                frame.pack();
                // NOTE: we have to show the frame to take combobox renderers into the game
                frame.setVisible(true);
            }
        });
        while (!isRegPaneValid()) {
            Thread.sleep(200);
        }
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                frame.getContentPane().remove(outerPane);
                
                // prevents KeyboardFocusManager.permanentFocusOwner and
                // JTextComponent.focusedComponent to hold us
                JTextField dummyFocusEater = new JTextField();
                frame.getContentPane().add(dummyFocusEater);
                dummyFocusEater.requestFocus();
                frame.setVisible(false);
                frame.dispose();
            }
        });
        
        outerPane = null;
        regPane = null;
        
        assertGC("GCing comp", regPaneWR);
    }
    
    private boolean isRegPaneValid() throws InterruptedException, InvocationTargetException {
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                valid = regPane.checkValidity();
            }
        });
        return valid;
    }
    
}
