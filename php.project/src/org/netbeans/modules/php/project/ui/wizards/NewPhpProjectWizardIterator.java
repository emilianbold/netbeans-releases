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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class NewPhpProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    static final String PROJECT_DIR = "projdir"; // NOI18N
    static final String SET_AS_MAIN = "setAsMain"; // NOI18N
    static final String USE_PROJECT_FOLDER = "useProjectFolder"; // NOI18N
    static final String USE_WWW_FOLDER = "useWwwFolder"; // NOI18N
    static final String WWW_FOLDER = "wwwFolder"; // NOI18N
    static final String CREATE_INDEX_FILE = "createIndexFile"; // NOI18N
    static final String INDEX_FILE = "indexFile"; // NOI18N
    static final String ENCODING = "encoding"; // NOI18N

    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel[] panels;
    private int index;

    public void initialize(WizardDescriptor wizard) {
        descriptor = wizard;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }

    public void uninitialize(WizardDescriptor wizard) {
        wizard.putProperty(USE_PROJECT_FOLDER, null);
        wizard.putProperty(USE_WWW_FOLDER, null);
        wizard.putProperty(WWW_FOLDER, null);
        wizard.putProperty(INDEX_FILE, null);
        wizard.putProperty(ENCODING, null);

        panels = null;
        descriptor = null;
    }

    public Set instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    public Set instantiate(ProgressHandle handle) throws IOException {
        final Set<Object> resultSet = new HashSet<Object>(2);

        File indexFile = (File) descriptor.getProperty(INDEX_FILE);
        handle.start(indexFile == null ? 2 : 3);
        int progres = 1;

        File dirF = (File) descriptor.getProperty("projdir"); // NOI18N
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }

        String msg = NbBundle.getMessage(
                NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingProject");
        handle.progress(msg, progres++);

        if (indexFile != null) {
            msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingIndexFile");
            handle.progress(msg, progres++);
        }

        msg = NbBundle.getMessage(
                NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_PreparingToOpen");
        handle.progress(msg, progres++);

        return resultSet;
    }

    public String name() {
        return NbBundle.getMessage(NewPhpProjectWizardIterator.class, "LBL_IteratorName", index + 1, panels.length);
    }

    public boolean hasNext() {
        return index < panels.length - 1;
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

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[]{
            new ConfigureProjectPanel(),
        };
    }

    private String[] createSteps() {
        String[] steps = new String[]{
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString("LBL_ProjectTitleName"),
            //NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString(STEP_WEB_SERVER)
        };
        return steps;
    }
}
