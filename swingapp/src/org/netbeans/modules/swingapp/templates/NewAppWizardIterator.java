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

package org.netbeans.modules.swingapp.templates;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

// TODO: use WizardDescriptor.ProgressInstantiatingIterator
public class NewAppWizardIterator implements WizardDescriptor.InstantiatingIterator, ChangeListener {

    private WizardDescriptor wizard;
    private int panelIndex;
    private WizardDescriptor.Panel[] panels;

    /** Names of wizard steps. */
    private String[] steps;

    /** Delegated app shell's iterator - if the selected app shell has one. */
    private WizardDescriptor.InstantiatingIterator appShellIterator;

    private EventListenerList listenerList;

    /** Key for the description of the wizard content. */
    private static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    /** Key for the description of the wizard panel's position. */
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; // NOI18N

    public NewAppWizardIterator() {
    }

    void setAppShellIterator(WizardDescriptor.InstantiatingIterator iterator) {
        if (appShellIterator != null) {
            appShellIterator.removeChangeListener(this);
            appShellIterator.uninitialize(wizard);
        }
        appShellIterator = iterator;
        if (appShellIterator != null) {
            appShellIterator.initialize(wizard);
            appShellIterator.addChangeListener(this);
        }
        if (panelIndex >= panels.length) {
            panelIndex = panels.length - 1; // issue 102728
        }
        initSteps();
        updateSteps();
        fireStateChanged();
    }

    WizardDescriptor.InstantiatingIterator getAppShellIterator() {
        return appShellIterator;
    }

    private void updateSteps() {
        JComponent comp = (JComponent) current().getComponent();
        comp.putClientProperty(WIZARD_PANEL_CONTENT_DATA, steps);
        comp.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, panelIndex);
    }

    private void initSteps() {
        String[] thisSteps = new String[] {
            NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.name") }; // NOI18N
        if (appShellIterator != null) {
            Object data = ((JComponent)appShellIterator.current().getComponent())
                    .getClientProperty(WIZARD_PANEL_CONTENT_DATA);
            if (data instanceof String[]) {
                String[] appShellSteps = (String[]) data;
                for (String s : appShellSteps) {
                    if (s == thisSteps[0]) {
                        steps = appShellSteps; // already set
                        return;
                    }
                }
                steps = new String[thisSteps.length + appShellSteps.length];
                System.arraycopy(thisSteps, 0, steps, 0, thisSteps.length);
                System.arraycopy(appShellSteps, 0, steps, thisSteps.length, appShellSteps.length);
                return;
            }
        }
        steps = thisSteps;
    }

    public void initialize(WizardDescriptor wiz) {
        wizard = wiz;
        panelIndex = 0;
        panels = new WizardDescriptor.Panel[] { new ConfigureProjectPanel(this) };
        initSteps();
        updateSteps();
    }

    public String name() {
        return current().getComponent().getName();
    }

    public WizardDescriptor.Panel current() {
         return panelIndex < panels.length ? panels[panelIndex] : appShellIterator.current();
    }

    public boolean hasNext() {
        if (panelIndex+1 < panels.length)
            return true;
        if (appShellIterator != null) {
            return panelIndex+1 == panels.length ?
                true : // we assume the app shell iterator has at least one panel
                appShellIterator.hasNext();
        }
        return false;
    }

    public boolean hasPrevious() {
        return panelIndex > 0;
    }

    public void nextPanel() {
        panelIndex++;
        if (panelIndex > panels.length) {
            appShellIterator.nextPanel();
            // don't call nextPanel on appShellIterator when we switch to its first panel
        }
        updateSteps();
    }

    public void previousPanel() {
        panelIndex--;
        if (panelIndex >= panels.length) {
            appShellIterator.previousPanel();
            // don't call previousPanel on appShellIterator when we switch from its first panel
        }
        updateSteps();
    }

    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null)
            listenerList.remove(ChangeListener.class, listener);
    }

    void fireStateChanged() {
        if (listenerList == null)
            return;

        ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i=listeners.length-2; i >= 0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }

    // called from appShellIterator - refire
    public void stateChanged(ChangeEvent e) {
        fireStateChanged();
    }

    public Set instantiate(/*TemplateWizard wiz*/) throws IOException {
        File projectDirectory = (File) wizard.getProperty("projdir"); // NOI18N
//        String projectName = (String) wiz.getProperty("projname"); // NOI18N
        if (projectDirectory == null) // || projectName == null)
            return null;

        projectDirectory = FileUtil.normalizeFile(projectDirectory);

        FileObject template = (FileObject) wizard.getProperty("appshell"); // NOI18N
        String[] templateNames = getTemplateNames(template);

        String projectName = projectDirectory.getName();
        String appClassName = (String) wizard.getProperty("appname"); // NOI18N
        String[] substNames = getSubstituteNames(projectName, appClassName);

        // create the project from template
        FileObject projectFolderFO = AppProjectGenerator.createProjectFromTemplate(
                template, projectDirectory, templateNames, substNames);

        // remember project location in ProjectChooser
        ProjectChooser.setProjectsFolder(projectDirectory.getParentFile());

        FileObject appFO = AppProjectGenerator.getGeneratedFile(
                projectFolderFO, "src/applicationpackage/ShellApp.java", // NOI18N
                templateNames, substNames);
        FileObject mainFormFO = AppProjectGenerator.getGeneratedFile(
                projectFolderFO, "src/applicationpackage/ShellView.java", // NOI18N
                templateNames, substNames);
        wizard.putProperty("mainForm", mainFormFO); // NOI18N

        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        Set<FileObject> subResults = null;

        // let the app shell wizard do the additional configuration
        if (appShellIterator != null) {
            subResults = appShellIterator.instantiate();
        }

        resultSet.add(projectFolderFO);
        if (appFO != null) {
            resultSet.add(appFO);
        }
        if (subResults != null) {
            resultSet.addAll(subResults);
        }
        // we need all forms to be opened and regenerated
        collectForms(projectFolderFO.getFileObject("src"), resultSet); // NOI18N
        // the main form should open as last
        if (mainFormFO != null) {
            resultSet.remove(mainFormFO);
            resultSet.add(mainFormFO);
        }

        return resultSet;
    }

    private static void collectForms(FileObject folder, Set<FileObject> set) {
        if (folder == null || !folder.isFolder()) {
            return;
        }
        for (FileObject file : folder.getChildren()) {
            if (file.isFolder()) {
                collectForms(file, set);
            }
            if (file.getExt().equalsIgnoreCase("java") // NOI18N
                    && file.existsExt("form")) { // NOI18N
                try {
                    file.setAttribute("justCreatedByNewWizard", Boolean.TRUE); // NOI18N
                    set.add(file);
                } catch (IOException ex) {
                    Logger.getLogger(NewAppWizardIterator.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
    }

    // -----

    private static String[] getTemplateNames(FileObject template) {
        // assuming template's name corresponds to the name of the template project
        return new String[] {
            template.getName(), // project name (dir)
            "applicationpackage", // NOI18N
            "ShellApp", // NOI18N
            "ShellView", // NOI18N
            "ShellAboutBox", // NOI18N
            "UTF-\\u0038" }; // hack - replace UTF-8 only in project.properties (#107083) // NOI18N
    }

    private static String[] getSubstituteNames(String projectName, String appClassName) {
        int i = appClassName.lastIndexOf('.');
        String packageName = appClassName.substring(0, i);

        appClassName = appClassName.substring(i+1); // short name

        String appSuffix = "Application"; // NOI18N
        if (!appClassName.endsWith(appSuffix)) {
            appSuffix = "App"; // NOI18N
            if (!appClassName.endsWith(appSuffix))
                appSuffix = null;
        }
        String appPrefix = appSuffix != null ?
            appClassName.substring(0, appClassName.length() - appSuffix.length()) :
            appClassName;

        return new String[] {
            projectName,
            packageName,
            appClassName,
            appPrefix + "View", // NOI18N
            appPrefix + "AboutBox", // NOI18N
            FileEncodingQuery.getDefaultEncoding().name() // for source.encoding in project.properties
        };
    }
}
