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

import java.io.File;
import javax.swing.event.ChangeListener;

import org.openide.*;

class AddToPaletteWizard extends WizardDescriptor {

    ATPWizardIterator wizardIterator;

    private File[] selectedFiles;
    private String[] selectedClasses;
    private String selectedCategory;

    private java.awt.Dialog dialog;

    // ---------

    public AddToPaletteWizard() {
        this(new ATPWizardIterator());
    }

    private AddToPaletteWizard(ATPWizardIterator iterator) {
        super(iterator);
        wizardIterator = iterator;

        putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N

        setTitle(PaletteUtils.getBundleString("CTL_AddToPaletteWizard_Title")); // NOI18N
        setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
    }

    public boolean show(String sourceType) {
        putProperty("WizardPanel_contentData",  // NOI18N
                    new String[] { PaletteUtils.getBundleString("CTL_SelectJAR_Step"), // NOI18N
                                   PaletteUtils.getBundleString("CTL_SelectBeans_Step"), // NOI18N
                                   PaletteUtils.getBundleString("CTL_SelectCategory_Step") }); // NOI18N

        wizardIterator.setSourceType(sourceType);
        updateState();

        if (dialog == null)
            dialog = DialogDisplayer.getDefault().createDialog(this);
        dialog.setVisible(true);
        dialog.dispose();

        return getValue() == FINISH_OPTION;
    }

    // -------

    void stepToNext() {
        if (wizardIterator.hasNext()) {
            wizardIterator.nextPanel();
            updateState();
        }
    }

    void setJARFiles(File[] files) {
        selectedFiles = files;
    }

    File[] getJARFiles() {
        return selectedFiles;
    }

    String[] getClassPathSource(String sourceType) {
        assert selectedFiles != null;

        String[] strings = new String[selectedFiles.length];
        for (int i=0; i < selectedFiles.length; i++)
            strings[i] = selectedFiles[i].getAbsolutePath();

        return strings;
    }

//    void setSelectedBeans(List beans) {
//        selectedBeans = beans;
//    }
//
//    List getSelectedBeans() {
//        return selectedBeans;
//    }

    void setSelectedClasses(String[] classes) {
        selectedClasses = classes;
    }

    String[] getSelectedClasses() {
        return selectedClasses;
    }

    void setSelectedCategory(String name) {
        selectedCategory = name;
    }

    String getSelectedCategory() {
        return selectedCategory;
    }

    // -------

    /** Wizard iterator implementation for Add to Palette wizard */
    static class ATPWizardIterator implements WizardDescriptor.Iterator/*, ChangeListener*/ {

        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[getPanelsCount()];
        int stage;

        void setSourceType(String sourceType) {
            if (PaletteItem.JAR_SOURCE == sourceType)
                panels[0] = new ChooseJARWizardPanel();;
            panels[1] = new ChooseBeansWizardPanel();
            panels[2] = new ChooseCategoryWizardPanel();

            stage = 1;
        }

        static int getPanelsCount() {
            return 3;
        }

        // ------
        // WizardDescriptor.Iterator implementation

        public WizardDescriptor.Panel current() {
            return panels[stage-1];
        }

        public boolean hasNext() {
            return stage < getPanelsCount();
        }

        public boolean hasPrevious() {
            return stage > 1;
        }

        public java.lang.String name() {
            return ""; // NOI18N
        }

        public void nextPanel() {
            if (stage < getPanelsCount())
                stage++;
        }

        public void previousPanel() {
            if (stage > 1)
                stage--;
        }

        public void addChangeListener(ChangeListener listener) {
//            if (listenerList == null)
//                listenerList = new EventListenerList();
//            listenerList.add(ChangeListener.class, listener);
        }

        public void removeChangeListener(ChangeListener listener) {
//            if (listenerList != null)
//                listenerList.remove(ChangeListener.class, listener);
        }

    }
}
