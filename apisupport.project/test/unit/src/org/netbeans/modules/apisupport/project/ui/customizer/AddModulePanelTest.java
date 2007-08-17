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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.util.concurrent.atomic.AtomicBoolean;
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
        NbModuleProject p = generateStandaloneModule("module1");
        final SingleModuleProperties props = SingleModulePropertiesTest.loadProperties(p);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                amp = new AddModulePanel(props);
                JFrame f = new JFrame();
                f.getContentPane().add(amp);
                f.pack();
                //f.setVisible(true);
            }
        });
        while (amp == null || !amp.filterValue.isEnabled()) {
            Thread.sleep(400);
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
        // wait until filter is applied
        final AtomicBoolean done = new AtomicBoolean();
        while (!done.get()) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    done.set(!CustomizerComponentFactory.isWaitModel(amp.moduleList.getModel()));
                }
            });
            Thread.sleep(200);
        }
        ListModel model = amp.moduleList.getModel();
        int filtered = model.getSize();
        final int EXPECTED_MAX = 50; // XXX really should be computed
        assertTrue("filter was successfull (" + all + " > " + filtered + ")", all > filtered);
        assertTrue("filter was successfull (" + filtered + " > " + EXPECTED_MAX + ")", filtered < EXPECTED_MAX);
        assertTrue("non-wait model", !CustomizerComponentFactory.isWaitModel(amp.moduleList.getModel()));
        assertTrue("non-empty model", !CustomizerComponentFactory.hasOnlyValue(amp.moduleList.getModel(), CustomizerComponentFactory.EMPTY_VALUE));
    }
    
}
