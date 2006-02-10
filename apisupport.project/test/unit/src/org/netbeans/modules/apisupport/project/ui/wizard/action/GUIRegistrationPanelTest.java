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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.ref.WeakReference;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.WizardDescriptor;

/**
 * @author Martin Krauskopf
 */
public class GUIRegistrationPanelTest extends LayerTestBase {
    
    public GUIRegistrationPanelTest(String testName) {
        super(testName);
    }
    
    public void testMemoryLeak_70032() throws Exception {
        TestBase.initializeBuildProperties(getWorkDir());
        WizardDescriptor wd = new WizardDescriptor(new WizardDescriptor.Panel[] {});
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT,
                TestBase.generateStandaloneModule(getWorkDir(), "module"));
        GUIRegistrationPanel regPane = new GUIRegistrationPanel(wd, new DataModel(wd));
        // let's force checkValidity failed until all data are loaded
        regPane.editorContext.setSelected(true);
        regPane.fileTypeContext.setSelected(true);
        regPane.globalMenuItem.setSelected(true);
        regPane.globalToolbarButton.setSelected(true);
        
        // workaround for inability to GC JFrame/JDialog itself
        JPanel outerPane = new JPanel();
        outerPane.add(regPane);
        
        final WeakReference compWr = new WeakReference(regPane);
        final WeakReference outerWr = new WeakReference(outerPane);
        
        final JFrame frame = new JFrame("testMemoryLeak_70032");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JLabel("<html><font color='blue'>I will close my self" +
                " as soos as I load all data from the SystemFileSystem</font></html>"), BorderLayout.NORTH);
        frame.getContentPane().add(outerPane, BorderLayout.CENTER);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame.pack();
                // NOTE: we have to show the frame to take combobox renderers into the game
                frame.setVisible(true);
            }
        });
        while (!regPane.checkValidity()) {
            Thread.sleep(200);
        }
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame.getContentPane().remove((Component) outerWr.get());
                
                // prevents KeyboardFocusManager.permanentFocusOwner to hold us
                JLabel dummyFocusEater = new JLabel();
                frame.getContentPane().add(dummyFocusEater);
                dummyFocusEater.requestFocus();
                
                frame.setVisible(false);
                frame.dispose();
            }
        });
        outerPane = null;
        regPane = null;
        
        assertGC("GCing comp", compWr);
        assertNull("sanity check", compWr.get());
    }
    
}
