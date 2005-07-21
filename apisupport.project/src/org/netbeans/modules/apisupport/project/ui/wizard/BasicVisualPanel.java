/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
    
    private WizardDescriptor setting;
    private Boolean valid = Boolean.FALSE;
    
    protected BasicVisualPanel(WizardDescriptor setting) {
        this.setting = setting;
    }
    
    protected WizardDescriptor getSetting() {
        return setting;
    }
    
    /** Set error message and always update panel validity. */
    protected void setErrorMessage(String errorMessage) {
        setErrorMessage(errorMessage, true);
    }
    
    /** Set error message and eventually update panel validity. */
    protected void setErrorMessage(String errorMessage, boolean fireChange) {
        setting.putProperty("WizardPanel_errorMessage", errorMessage); // NOI18N
        if (fireChange) {
            setValid(Boolean.valueOf(errorMessage == null));
        }
    }
    
    /**
     * Sets this panel's validity and fires event to it's wrapper wizard panel.
     * See {@link BasicWizardPanel#propertyChange} for what happens further.
     */
    protected void setValid(Boolean newValid) {
        this.valid = newValid;
        firePropertyChange("valid", Boolean.valueOf(!newValid.booleanValue()), newValid); // NOI18N
    }
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    String getMessage(String key) {
        return NbBundle.getMessage(BasicVisualPanel.class, key);
    }
}
