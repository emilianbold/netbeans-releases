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

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Selection wizard panel for Eclipse Wizard importer.
 *
 * @author mkrauskopf
 */
final class SelectionWizardPanel extends ImporterWizardPanel implements
        PropertyChangeListener {
    
    private SelectionPanel panel;
    
    /** Creates a new instance of WorkspaceWizardPanel */
    SelectionWizardPanel() {
        panel = new SelectionPanel();
        panel.addPropertyChangeListener(this);
        initPanel(panel, 0);
    }
    
    public Component getComponent() {
        return panel;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ("errorMessage".equals(evt.getPropertyName())) { //NOI18N
            setErrorMessage((String) evt.getNewValue());
        }
    }
    
    // ==== delegate methods ==== //
    boolean isWorkspaceChosen() {
        return panel.isWorkspaceChosen();
    }
    
    String getProjectDir() {
        return panel.getProjectDir();
    }
    
    public String getProjectDestinationDir() {
        return panel.getProjectDestinationDir();
    }

    public String getWorkspaceDir() {
        return panel.getWorkspaceDir();
    }
}
