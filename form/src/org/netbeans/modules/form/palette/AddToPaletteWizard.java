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
import java.util.Map;
import javax.swing.event.ChangeListener;

import org.openide.*;

import org.netbeans.modules.form.project.ClassSource;

/**
 * A wizard allowing the user to add components to palette from a JAR file,
 * library, or a project. This class manages the whole wizard depending on the
 * type of source the user wants to choose from. There are three steps in the
 * wizard - selecting source, selecting components, and selecting palette
 * category.
 *
 * @author Tomas Pavek
 */

class AddToPaletteWizard extends WizardDescriptor {

    ATPWizardIterator wizardIterator;

    private File[] selectedFiles;
    private BeanInstaller.ItemInfo[] selectedBeans;
    private String selectedCategory;
    private String sourceType;

    Map libraryNameMap; // map from root file (JAR) names to libraries they belong to
                        // created by ChooseLibraryWizardPanel.storeSettings

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
        String firstStep_key;
        this.sourceType = sourceType;
        if (ClassSource.JAR_SOURCE.equals(sourceType))
            firstStep_key = "CTL_SelectJAR_Step"; // NOI18N
        else if (ClassSource.LIBRARY_SOURCE.equals(sourceType))
            firstStep_key = "CTL_SelectLibrary_Step"; // NOI18N
        else if (ClassSource.PROJECT_SOURCE.equals(sourceType))
            firstStep_key = "CTL_SelectProject_Step"; // NOI18N
        else
            throw new IllegalArgumentException();

        putProperty("WizardPanel_contentData",  // NOI18N
                    new String[] { PaletteUtils.getBundleString(firstStep_key),
                                   PaletteUtils.getBundleString("CTL_SelectBeans_Step"), // NOI18N
                                   PaletteUtils.getBundleString("CTL_SelectCategory_Step") }); // NOI18N

        libraryNameMap = null;
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

    /** @return the JAR files representing the selected source in the first
     * step of the wizard (i.e. a JAR file directly, library, or project) */
    File[] getJARFiles() {
        return selectedFiles;
    }

    void setSelectedBeans(BeanInstaller.ItemInfo[] beans) {
        selectedBeans = beans;
    }

    BeanInstaller.ItemInfo[] getSelectedBeans() {
        return selectedBeans;
    }

    void setSelectedCategory(String name) {
        selectedCategory = name;
    }

    String getSelectedCategory() {
        return selectedCategory;
    }
    
    String getSourceType() {
        return sourceType;
    }

    // -------

    /** Wizard iterator implementation for Add to Palette wizard */
    static class ATPWizardIterator implements WizardDescriptor.Iterator {

        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[getPanelsCount()];
        int stage;

        void setSourceType(String sourceType) {
            if (ClassSource.JAR_SOURCE.equals(sourceType))
                panels[0] = new ChooseJARWizardPanel();
            else if (ClassSource.LIBRARY_SOURCE.equals(sourceType))
                panels[0] = new ChooseLibraryWizardPanel();
            else if (ClassSource.PROJECT_SOURCE.equals(sourceType))
                panels[0] = new ChooseProjectWizardPanel();
            else
                throw new IllegalArgumentException();

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
        }

        public void removeChangeListener(ChangeListener listener) {
        }
    }
}
