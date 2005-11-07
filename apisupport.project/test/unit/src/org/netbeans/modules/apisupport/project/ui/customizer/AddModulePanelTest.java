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

package org.netbeans.modules.apisupport.project.ui.customizer;

import javax.swing.JFrame;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;

/**
 * Tests {@link AddModulePanel}.
 *
 * @author Martin Krauskopf
 */
public class AddModulePanelTest extends TestBase {
    
    private AddModulePanel amp;
    
    public AddModulePanelTest(String name) {
        super(name);
    }
    
    public void testDependenciesFiltering() throws Exception {
        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        final SingleModuleProperties props = SingleModulePropertiesTest.loadProperties(p);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                amp = new AddModulePanel(props);
                JFrame f = new JFrame();
                f.getContentPane().add(amp);
                f.pack();
                f.setVisible(true);
            }
        });
        while (amp == null || !amp.filterValue.isEnabled()) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                fail();
            }
        }
        int all = amp.moduleList.getModel().getSize();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                // fire multiple events to EQ
                amp.filterValue.setText("o");
                amp.filterValue.setText("or");
                amp.filterValue.setText("org");
                amp.filterValue.setText("org.");
                amp.filterValue.setText("org.o");
                amp.filterValue.setText("org.op");
            }
        });
        int sleepingTime = 0;
        // wait until filter is applied
        while (amp.moduleList.getModel() == CustomizerComponentFactory.LIST_WAIT_MODEL) {
            Thread.sleep(200);
            sleepingTime += 200;
            if (sleepingTime > 60000) {
                fail("Filter wasn't applied in 60s.");
            }
        }
        ListModel model = amp.moduleList.getModel();
        int filtered = model.getSize();
        final int EXPECTED_MAX = 50; // XXX really should be computed
        assertTrue("filter was successfull (" + all + " > " + filtered + ")", all > filtered);
        assertTrue("filter was successfull (" + filtered + " > " + EXPECTED_MAX + ")", filtered < EXPECTED_MAX);
        assertTrue("non-wait model", model != CustomizerComponentFactory.LIST_WAIT_MODEL);
        assertTrue("non-empty model", model != CustomizerComponentFactory.EMPTY_MODEL);
    }
    
}
