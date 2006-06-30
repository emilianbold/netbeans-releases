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

package org.netbeans.modules.form.palette;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;

import org.openide.WizardDescriptor;

/**
 * The first panel in the wizard for adding new components to the palette from
 * a JAR. In this panel (as the first step) the user chooses the JAR file.
 * The alternative first steps are ChooseLibraryWizardPanel and
 * ChooseProjectWizardPanel.
 *
 * @author Tomas Pavek
 */

class ChooseJARWizardPanel implements WizardDescriptor.Panel {

    private JFileChooser fileChooser;
    private static String lastDirectoryUsed;

    private AddToPaletteWizard wizard;

    private EventListenerList listenerList;

    // ----------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (fileChooser == null) { // create the UI component for the wizard step
            fileChooser = new JFileChooser(lastDirectoryUsed);
            fileChooser.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            // wizard API: set the caption and index of this panel
            fileChooser.setName(PaletteUtils.getBundleString("CTL_SelectJAR_Caption")); // NOI18N
            fileChooser.putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                                          new Integer(0));

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(true);
            fileChooser.setControlButtonsAreShown(false);
            fileChooser.setMultiSelectionEnabled(true);

            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory()
                           || f.getName().toLowerCase().endsWith(".jar"); // NOI18N
                }
                public String getDescription() {
                    return PaletteUtils.getBundleString("CTL_JarArchivesMask"); // NOI18N
                }
            });

            fileChooser.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    wizard.stepToNext();
                }
            });

            fileChooser.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY
                                        .equals(ev.getPropertyName()))
                        fireStateChanged();
                }
            });
        }

        return fileChooser;
    }

    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    public boolean isValid() {
        if (fileChooser != null && fileChooser.getSelectedFiles().length > 0) {
            lastDirectoryUsed = fileChooser.getCurrentDirectory().getAbsolutePath();
            return true;
        }
        return false;
    }

    public void readSettings(Object settings) {
        wizard = (AddToPaletteWizard) settings;
    }

    public void storeSettings(Object settings) {
        if (fileChooser != null)
            ((AddToPaletteWizard)settings).setJARFiles(fileChooser.getSelectedFiles());
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
