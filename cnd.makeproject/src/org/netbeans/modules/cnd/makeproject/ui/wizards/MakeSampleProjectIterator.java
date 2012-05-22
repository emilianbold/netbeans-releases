/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectWizardPanels.MakeSamplePanel;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectWizardPanels.NamedPanel;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public class MakeSampleProjectIterator implements TemplateWizard.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final long serialVersionUID = 4L;
    private transient int index = 0;
    private transient WizardDescriptor.Panel<WizardDescriptor> panel;
    private transient TemplateWizard wiz;

    static Object create() {
        return new MakeSampleProjectIterator();
    }

    public MakeSampleProjectIterator() {
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panel;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        int i = 0;
        this.wiz = (TemplateWizard)wizard;
        String name = wiz.getTemplate().getNodeDelegate().getName();
        if (name != null) {
            name = name.replaceAll(" ", ""); // NOI18N
        }
        wiz.putProperty(WizardConstants.PROPERTY_NAME, name);
        String wizardTitle = getString("SAMPLE_PROJECT") + name; // NOI18N
        String wizardTitleACSD = getString("SAMPLE_PROJECT_ACSD"); // NOI18N

        panel = getPanel(-1, name, wizardTitle, wizardTitleACSD, false);
        String[] steps = new String[1];
            JComponent jc = (JComponent) panel.getComponent();
            steps[i] = ((NamedPanel) panel).getName();
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
    }
    
    public static MakeSamplePanel<WizardDescriptor> getPanel(int wizardtype, String name, String wizardTitle, String wizardACSD, boolean fullRemote) {
        return new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, fullRemote);
    }

    @Override
    public void uninitialize(WizardDescriptor templateWizard) {
        panel = null;
        index = -1;
        this.wiz.putProperty(WizardConstants.PROPERTY_PROJECT_FOLDER, null);
        this.wiz.putProperty(WizardConstants.PROPERTY_NAME, null);
    }

    @Override
    public Set<?> instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start();
            return instantiate();
        } finally {
            handle.finish();
        }
    }
    
    @Override
    public Set<DataObject> instantiate() throws IOException {
        FSPath projectLocation = (FSPath) wiz.getProperty(WizardConstants.PROPERTY_PROJECT_FOLDER);
        String name = (String) wiz.getProperty(WizardConstants.PROPERTY_NAME);
        String hostUID = (String) wiz.getProperty(WizardConstants.PROPERTY_HOST_UID);
        if (wiz.getProperty(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV) != null) {
            hostUID = ExecutionEnvironmentFactory.toUniqueID(ExecutionEnvironmentFactory.getLocal());
        }
        CompilerSet toolchain = (CompilerSet) wiz.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        boolean defaultToolchain = Boolean.TRUE.equals(wiz.getProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(name, projectLocation);
        prjParams.setHostToolchain(hostUID, toolchain, defaultToolchain);
        return ProjectGenerator.createProjectFromTemplate(wiz.getTemplate().getPrimaryFile(), prjParams);
    }

    @Override
    public String name() {
        return current().getComponent().getName();
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(NewMakeProjectWizardIterator.class);
        }
        return bundle.getString(s);
    }
}
