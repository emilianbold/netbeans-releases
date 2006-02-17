/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import javax.swing.JPanel;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Basic visual panel for APISupport wizard panels.
 *
 * @author Martin Krauskopf
 */
public abstract class BasicVisualPanel extends JPanel {
    
    private WizardDescriptor settings;
    
    protected BasicVisualPanel(final WizardDescriptor setting) {
        this.settings = setting;
    }
    
    public final WizardDescriptor getSettings() {
        return settings;
    }
    
    /**
     * Set an error message and mark the panel as invalid.
     */
    protected final void setError(String message) {
        if (message == null) {
            throw new NullPointerException();
        }
        setMessage(message);
        setValid(false);
    }
    
    /**
     * Set an warning message but mark the panel as valid.
     */
    protected final void setWarning(String message) {
        if (message == null) {
            throw new NullPointerException();
        }
        setMessage(message);
        setValid(true);
    }
    
    /**
     * Mark the panel as invalid without any message.
     * Use with restraint; generally {@link #setError} is better.
     */
    protected final void markInvalid() {
        setMessage(null);
        setValid(false);
    }
    
    /**
     * Mark the panel as valid and clear any error or warning message.
     */
    protected final void markValid() {
        setMessage(null);
        setValid(true);
    }
    
    private final void setMessage(String message) {
        settings.putProperty("WizardPanel_errorMessage", message); // NOI18N
    }
    
    /**
     * Sets this panel's validity and fires event to it's wrapper wizard panel.
     * See {@link BasicWizardPanel#propertyChange} for what happens further.
     */
    private final void setValid(boolean valid) {
        firePropertyChange("valid", null, Boolean.valueOf(valid)); // NOI18N
    }
    
    protected abstract static class NewTemplatePanel extends BasicVisualPanel {
        
        private final NewModuleProjectData data;
        
        NewTemplatePanel(final NewModuleProjectData data) {
            super(data.getSettings());
            this.data = data;
            String resource;
            int wizardType = data.getWizardType();
            switch (data.getWizardType()) {
                case NewNbModuleWizardIterator.TYPE_SUITE:
                    resource = "emptySuite"; // NOI18N
                    break;
                case NewNbModuleWizardIterator.TYPE_MODULE:
                case NewNbModuleWizardIterator.TYPE_SUITE_COMPONENT:
                    resource = "emptyModule"; // NOI18N
                    break;
                case NewNbModuleWizardIterator.TYPE_LIBRARY_MODULE:
                    resource = "libraryModule"; // NOI18N
                    break;
                default:
                    assert false : "Unknown wizard type = " + wizardType;
                    resource = "";
            }
            data.getSettings().putProperty("NewProjectWizard_Title", // NOI18N
                    NbBundle.getMessage(BasicVisualPanel.class, "Templates/Project/APISupport/" + resource));
        }
        
        protected NewModuleProjectData getData() {
            return data;
        }
        
        protected boolean isSuiteWizard() {
            return getData().getWizardType() == NewNbModuleWizardIterator.TYPE_SUITE;
        }
        
        protected boolean isSuiteComponentWizard() {
            return getData().getWizardType() == NewNbModuleWizardIterator.TYPE_SUITE_COMPONENT;
        }

        protected boolean isLibraryWizard() {
            return getData().getWizardType() == NewNbModuleWizardIterator.TYPE_LIBRARY_MODULE;
        }
        
    }
    
}
