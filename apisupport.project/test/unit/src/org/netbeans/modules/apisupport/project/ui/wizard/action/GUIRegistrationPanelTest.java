/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.lang.ref.Reference;
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
    private Reference<?> regPaneWR;
    private boolean valid;
    
    public GUIRegistrationPanelTest(String testName) {
        super(testName);
    }
    
    public void testMemoryLeak_70032() throws Exception {
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        final WizardDescriptor wd = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>() {
            public WizardDescriptor.Panel<WizardDescriptor> current() { // satisfying WizardDescriptor 1.32 (#76318)
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
                
                regPaneWR = new WeakReference<Object>(regPane);
                
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
