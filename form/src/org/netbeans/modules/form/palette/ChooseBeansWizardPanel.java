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

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.File;
import java.util.*;
import javax.swing.event.*;

import org.openide.WizardDescriptor;
import org.openide.ErrorManager;

/**
 * The second panel in the wizard for adding new components to the palette.
 * Lets the user choose components from a list of all available components
 * in selected source.
 *
 * @author Tomas Pavek
 */

class ChooseBeansWizardPanel implements WizardDescriptor.Panel {

    private File[] currentFiles;
    private java.util.List allBeans;
    private BeanSelector beanSelector;

    private EventListenerList listenerList;

    // ----------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (beanSelector == null) { // create the UI component for the wizard step
            beanSelector = new BeanSelector();

            // wizard API: set the caption and index of this panel
            beanSelector.setName(PaletteUtils.getBundleString("CTL_SelectBeans_Caption")); // NOI18N
            beanSelector.putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                                           new Integer(1));
            if (allBeans != null)
                beanSelector.setBeans(allBeans);

            beanSelector.list.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        fireStateChanged();
                    }
                });
        }

        return beanSelector;
    }

    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    public boolean isValid() {
        return beanSelector != null && beanSelector.getSelectedBeans().size() > 0;
    }

    public void readSettings(Object settings) {
        AddToPaletteWizard wizard = (AddToPaletteWizard) settings;
        File[] jarFiles = wizard.getJARFiles();

        if (currentFiles != null && currentFiles.length == jarFiles.length)
            for (int i=0; i < jarFiles.length; i++)
                if (jarFiles[i].equals(currentFiles[i])) {
                    if (i+1 == jarFiles.length)
                        return;  // no change from the last time
                }
                else break;

        currentFiles = jarFiles;
        allBeans = BeanInstaller.findJavaBeans(jarFiles);

        Map libraryMap = wizard.libraryMap;
        wizard.libraryMap = null;
        if (libraryMap != null) { // need to change file names to library names
            for (int i=0, n=allBeans.size(); i < n; i++) {
                BeanInstaller.ItemInfo ii = (BeanInstaller.ItemInfo) allBeans.get(i);
                ii.source = (String) libraryMap.get(ii.source);
            }
        }

        Collections.sort(allBeans);

        if (beanSelector != null)
            beanSelector.setBeans(allBeans);
    }

    public void storeSettings(Object settings) {
        if (beanSelector != null) {
            java.util.List itemList = beanSelector.getSelectedBeans();
            BeanInstaller.ItemInfo[] itemArray =
                new BeanInstaller.ItemInfo[itemList.size()];
            itemList.toArray(itemArray);
            ((AddToPaletteWizard)settings).setSelectedBeans(itemArray);
        }
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
