/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.palette;

import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.event.*;
import java.beans.*;
import java.util.*;
import java.net.URI;

import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.netbeans.api.project.ant.*;
import org.netbeans.api.project.*;
import org.netbeans.spi.project.ui.support.ProjectChooser;

/**
 * The first panel in the wizard for adding new components to the palette from
 * a project. In this panel the user chooses a project (as project folder
 * using the project chooser UI).
 *
 * @author Tomas Pavek
 */

class ChooseProjectWizardPanel implements WizardDescriptor.Panel {

    private JFileChooser projectChooser;
    private static String lastDirectoryUsed;

    private EventListenerList listenerList;

    // ----------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (projectChooser == null) { // create the UI component for the wizard step
            projectChooser = ProjectChooser.projectChooser();
            projectChooser.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            // wizard API: set the caption and index of this panel
            projectChooser.setName(PaletteUtils.getBundleString("CTL_SelectProject_Caption")); // NOI18N
            projectChooser.putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                                             new Integer(0));

            if (lastDirectoryUsed != null)
                projectChooser.setCurrentDirectory(new File(lastDirectoryUsed));
            projectChooser.setControlButtonsAreShown(false);

            projectChooser.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    String propName = ev.getPropertyName();
                    if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(propName)
                         || JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(propName))
                        fireStateChanged();
                }
            });
        }

        return projectChooser;
    }

    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    public boolean isValid() {
        if (projectChooser != null) {
            File file = projectChooser.getSelectedFile();
            if (file != null) {
                file = FileUtil.normalizeFile(file);
                FileObject projectDir = FileUtil.toFileObject(file);
                try {
                    Project project = ProjectManager.getDefault()
                                               .findProject(projectDir);
                    if (project != null) { // it is a project directory
                        lastDirectoryUsed = projectChooser.getCurrentDirectory()
                                                           .getAbsolutePath();
                        return true;
                    }
                }
                catch (IOException ex) {} // ignore
            }
        }
        return false;
    }

    public void readSettings(Object settings) {
    }

    public void storeSettings(Object settings) {
        if (projectChooser == null)
            return;

        File file = projectChooser.getSelectedFile();
        if (file == null)
            return;
        file = FileUtil.normalizeFile(file);

        Project project = null;
        try {
            project = ProjectManager.getDefault().findProject(
                                    FileUtil.toFileObject(file));
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        if (project == null)
            return;

        List fileList = new ArrayList();
        AntArtifact[] artifacts =
            AntArtifactQuery.findArtifactsByType(project, "jar"); // NOI18N

        for (int i=0; i < artifacts.length; i++) {
            URI scriptLocation = artifacts[i].getScriptLocation().toURI();
            URI[] artifactLocations = artifacts[i].getArtifactLocations();
            for (int j=0; j < artifactLocations.length; j++) {
                File outputFile = new File(scriptLocation.resolve(artifactLocations[j]).normalize());
                fileList.add(outputFile);
            }
        }

        File[] outputFiles = new File[fileList.size()];
        fileList.toArray(outputFiles);
        ((AddToPaletteWizard)settings).setJARFiles(outputFiles);
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

    // -----

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
}
