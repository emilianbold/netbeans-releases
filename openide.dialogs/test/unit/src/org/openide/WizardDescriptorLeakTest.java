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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.openide;

import java.awt.Component;
import java.awt.Dialog;
import java.lang.ref.WeakReference;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

public class WizardDescriptorLeakTest extends NbTestCase {

    public WizardDescriptorLeakTest (String testName) {
        super (testName);
    }

    @Override
    protected boolean runInEQ () {
        return false;
    }

    private static Object HOLDER;

    /** Preventing following memory leak by making sure dispose() clear all
     * references to DialogDescriptor.
     * 
     * <pre>
private static java.awt.Component java.awt.KeyboardFocusManager.focusOwner->
javax.swing.JFrame@1e89fdb-focusTraversalPolicy->
javax.swing.LegacyGlueFocusTraversalPolicy@5cc8e7-delegateManager->
javax.swing.DelegatingDefaultFocusManager@1bf5121-delegate->
java.awt.DefaultKeyboardFocusManager@ee3f7c-realOppositeWindow->
org.netbeans.core.windows.services.NbDialog@b6e92f-descriptor->
org.openide.DialogDescriptor@53e657-message->
org.netbeans.modules.refactoring.spi.impl.ParametersPanel@13f1403-rui->
org.netbeans.modules.refactoring.java.ui.SafeDeleteUI@1aa6496-refactoring->
org.netbeans.modules.refactoring.api.SafeDeleteRefactoring@509746-scope->
org.netbeans.modules.refactoring.api.Context@164bd30-delegate->
org.openide.util.lookup.AbstractLookup@a39c01-tree->
org.openide.util.lookup.ArrayStorage@a56580-content->
[Ljava.lang.Object;@153cc48-[0]->
org.openide.util.lookup.InstanceContent$SimpleItem@af4bd9-obj->
org.netbeans.api.java.source.ClasspathInfo@110bab1-bootClassPath->
org.netbeans.api.java.classpath.ClassPath@142871f-impl->
org.netbeans.spi.java.project.support.ClassPathProviderMerger$ProxyClassPathImplementation@1ff103e-mainProvider->
org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl@1a405d9-helper->
org.netbeans.spi.project.support.ant.AntProjectHelper@1ec9e59-state->
org.netbeans.api.project.ProjectManager$ProjectStateImpl@593990-p->
org.netbeans.modules.java.j2seproject.J2SEProject@161761f
 </pre>
     */
    public void testDisposeClearsDescriptor () throws Exception {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels(), null);
        wizardDescriptor.setModal (false);
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        dialog.setVisible(true);
        while (!dialog.isShowing()) {
            waitAWT();
        }
        dialog.setVisible(false);
        while (dialog.isShowing()) {
            waitAWT();
        }
        WeakReference<WizardDescriptor> w = new WeakReference<WizardDescriptor> (wizardDescriptor);
        WeakReference<Object> m = new WeakReference<Object> (wizardDescriptor.getMessage());
        wizardDescriptor = null;
        dialog.dispose();
        HOLDER = dialog;

        assertGC("After dispose the descriptor can disappear", w);
        assertGC("After dispose the message can disappear", m);
    }

    private static void waitAWT() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
    }

    
    private WizardDescriptor.Panel<?>[] getPanels () {
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
