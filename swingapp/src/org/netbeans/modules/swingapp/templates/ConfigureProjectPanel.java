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

package org.netbeans.modules.swingapp.templates;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class ConfigureProjectPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizard;
    private NewAppWizardIterator wizardIterator;
    private ConfigureProjectVisualPanel visualPanel;

    private EventListenerList listenerList;

    public ConfigureProjectPanel(NewAppWizardIterator iterator) {
        wizardIterator = iterator;
    }

    public Component getComponent() {
        if (visualPanel == null)
            visualPanel = new ConfigureProjectVisualPanel(this);
        return visualPanel;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void readSettings(Object settings) {
        WizardDescriptor wd = (WizardDescriptor) settings;
        wizard = wd;
        getComponent();

        // need to set the title everytime when switching from the first panel
        wd.putProperty("NewProjectWizard_Title", // NOI18N
                NbBundle.getMessage(ConfigureProjectPanel.class, "TITLE_NewDesktopApp")); // NOI18N

        // (project location is parent of project directory)
        File projectLocation = (File) wd.getProperty("projdir"); // NOI18N
        if (projectLocation == null || projectLocation.getParentFile() == null
                || !projectLocation.getParentFile().isDirectory ()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        }
        else projectLocation = projectLocation.getParentFile();

        String projectName = (String) wd.getProperty("projname"); // NOI18N
        if (projectName == null) {
            String baseName = "DesktopApplication"; // NOI18N
            int index = 0;
            do {
                projectName = baseName + (++index);
            }
            while(new File(projectLocation, projectName).exists());
        }

        String appClassName = (String) wd.getProperty("appname"); // NOI18N

        visualPanel.setConfig(projectLocation, projectName, appClassName);
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wd = (WizardDescriptor) settings;
        wd.putProperty("projdir", visualPanel.getProjectDirectory()); // NOI18N
        wd.putProperty("projname", visualPanel.getProjectName()); // NOI18N
        wd.putProperty("appname", visualPanel.getApplicationClassName()); // NOI18N
        wd.putProperty("appshell", visualPanel.getSelectedTemplate()); // NOI18N
        wd.putProperty("setAsMain", visualPanel.isSetMainProject()); // NOI18N
    }

    public boolean isValid() {
        String projName = visualPanel.getProjectName();
        if (projName == null || projName.length() == 0
                || projName.indexOf('/') > 0 || projName.indexOf('\\') > 0
                || projName.indexOf(':') > 0 || projName.indexOf('\"') > 0) {
            wizard.putProperty("WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectName")); // NOI18N
            return false;
        }
        File projDir = visualPanel.getProjectDirectory();
        // check if the path is valid
        File cProjDir;
        try {
            cProjDir = projDir.getCanonicalFile();
        } catch (IOException e) {
            wizard.putProperty("WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectLocation")); // NOI18N
            return false;
        }
        // not allow to create project on unix root folder, see #82339
        if (Utilities.isUnix() && cProjDir.getParentFile().getParent() == null) {
            wizard.putProperty("WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectInRootNotSupported")); // NOI18N
            return false;
        }
        // check for read only
        File nProjDir = FileUtil.normalizeFile(projDir);
        while (nProjDir != null && !nProjDir.exists()) {
            nProjDir = nProjDir.getParentFile();
        }
        if (nProjDir == null || !nProjDir.canWrite()) {
            wizard.putProperty("WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectFolderReadOnly")); // NOI18N
            return false;
        }
        // check if the existing root lies on a usable filesystem
        if (FileUtil.toFileObject(nProjDir) == null) {
            wizard.putProperty("WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectLocation")); // NOI18N
            return false;
        }
        // check for existing content
        File[] kids = projDir.listFiles();
        if (projDir.exists() && kids != null && kids.length > 0) {
            wizard.putProperty("WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectFolderExists")); // NOI18N
            return false;
        }

        // check valid app class name
        String appClassName = visualPanel.getApplicationClassName();
        for (String s : appClassName.split("\\.", -1)) { // NOI18N
            if (!Utilities.isJavaIdentifier(s)) {
                wizard.putProperty("WizardPanel_errorMessage", // NOI18N
                        NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_InvalidAppClassName")); // NOI18N
                return false;
            }
        }

        wizard.putProperty("WizardPanel_errorMessage", null); // NOI18N

        return visualPanel.getSelectedTemplate() != null;
    }

    public boolean isFinishPanel() {
        return wizardIterator.getAppShellIterator() == null;
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

    private void fireStateChanged() {
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

    void visualPanelChanged(boolean appShellChanged) {
        if (appShellChanged) {
            wizardIterator.setAppShellIterator(getAppShellWizardIterator(visualPanel.getSelectedTemplate()));
        }
        fireStateChanged();
    }

    private WizardDescriptor.InstantiatingIterator getAppShellWizardIterator(FileObject appShellTemplate) {
        if (appShellTemplate != null) {
            Object iteratorObj = appShellTemplate.getAttribute("instantiatingIterator"); // NOI18N
            if (iteratorObj instanceof WizardDescriptor.InstantiatingIterator)
                return (WizardDescriptor.InstantiatingIterator) iteratorObj;
        }
        return null;
    }
}
