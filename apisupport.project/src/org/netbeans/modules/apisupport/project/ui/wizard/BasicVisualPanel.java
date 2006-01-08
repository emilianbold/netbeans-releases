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
     * Set an error message and always update panel's validity. See {@link
     * #setErrorMessage(String, boolean)} for more details.
     */
    protected final void setErrorMessage(String errorMessage) {
        setErrorMessage(errorMessage, true);
    }
    
    /**
     * Set an error message and eventually update panel's validity. If an
     * <em>updateValidity</em> is <code>true</code> also set a validity of this
     * panel. i.e. if the given error message is equal to <code>null</code>
     * panel is treat as valid; invalid otherwise.
     */
    protected final void setErrorMessage(String errorMessage, boolean updateValidity) {
        settings.putProperty("WizardPanel_errorMessage", errorMessage); // NOI18N
        if (updateValidity) {
            setValid(Boolean.valueOf(errorMessage == null));
        }
    }
    
    /**
     * Sets this panel's validity and fires event to it's wrapper wizard panel.
     * See {@link BasicWizardPanel#propertyChange} for what happens further.
     */
    protected final void setValid(Boolean newValid) {
        firePropertyChange("valid", Boolean.valueOf(!newValid.booleanValue()), newValid); // NOI18N
    }
    
    abstract static class NewTemplatePanel extends BasicVisualPanel {
        
        NewTemplatePanel(final WizardDescriptor settings, final int wizardType) {
            super(settings);
            String resource = null;
            if (wizardType == NewNbModuleWizardIterator.TYPE_SUITE) {
                resource = "emptySuite"; // NOI18N
            } else if (wizardType == NewNbModuleWizardIterator.TYPE_MODULE ||
                    wizardType == NewNbModuleWizardIterator.TYPE_SUITE_COMPONENT) {
                resource = "emptyModule"; // NOI18N
            } else if (wizardType == NewNbModuleWizardIterator.TYPE_LIBRARY_MODULE) {
                resource = "libraryModule"; // NOI18N
            } else {
                assert false : "Unknown wizard type = " + wizardType; // NOI18N
            }
            settings.putProperty("NewProjectWizard_Title", // NOI18N
                    NbBundle.getMessage(BasicVisualPanel.class, "Templates/Project/APISupport/" + resource));
        }
        
    }
    
}
