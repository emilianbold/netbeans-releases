/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.ui.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.ui.options.ToolsCacheManager;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public final class CreateHostWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;

    @SuppressWarnings( "unchecked" )
    private static WizardDescriptor.Panel<WizardDescriptor>[] callUncheckedNewForPanels() {
        return new WizardDescriptor.Panel[]{
                        new CreateHostWizardPanel1(),
                        new CreateHostWizardPanel2(),
                        new CreateHostWizardPanel3()
                    };
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = callUncheckedNewForPanels();
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); //NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); //NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); //NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); //NOI18N
                }
            }
        }
        return panels;
    }

    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels()[index];
    }

    public String name() {
        return "";//index + 1 + ". from " + getPanels().length;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    public static ServerRecord invokeMe(ToolsCacheManager cacheManager) {
        WizardDescriptor.Iterator<WizardDescriptor> iterator = new CreateHostWizardIterator();
        WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); //NOI18N
        wizardDescriptor.setTitle(getString("CreateNewHostWizardTitle"));
        wizardDescriptor.putProperty(CreateHostWizardConstants.PROP_CACHE_MANAGER, cacheManager);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            Runnable r = (Runnable) wizardDescriptor.getProperty(CreateHostWizardConstants.PROP_RUN_ON_FINISH);
            CndUtils.assertFalse(r == null);
            if (r != null) {
                r.run();
            }
            ExecutionEnvironment execEnv = (ExecutionEnvironment)wizardDescriptor.getProperty(CreateHostWizardConstants.PROP_HOST);
            String displayName = (String) wizardDescriptor.getProperty(CreateHostWizardConstants.PROP_DISPLAY_NAME);
            if (displayName == null) {
                displayName = execEnv.getDisplayName();
            }
            final RemoteSyncFactory syncFactory = (RemoteSyncFactory) wizardDescriptor.getProperty(CreateHostWizardConstants.PROP_SYNC);
            final ServerRecord record = ServerList.addServer(execEnv, displayName, syncFactory, false, false);
            return record;
        } else {
            return null;
        }
    }

    static String getString(String key) {
        return NbBundle.getBundle(CreateHostWizardIterator.class).getString(key);
    }
}
