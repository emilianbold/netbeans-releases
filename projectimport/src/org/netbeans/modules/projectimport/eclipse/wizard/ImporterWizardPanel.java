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

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Basic panel for Eclipse Wizard importer.
 *
 * @author mkrauskopf
 */
abstract class ImporterWizardPanel extends JPanel
        implements WizardDescriptor.Panel {
    
    /** Registered ChangeListeners */
    private List changeListeners;
    
    static final String WORKSPACE_LOCATION_STEP =
            NbBundle.getMessage(ImporterWizardPanel.class, "LBL_WorkspaceLocationStep"); // NOI18N
    static final String PROJECT_SELECTION_STEP =
            NbBundle.getMessage(ImporterWizardPanel.class, "LBL_ProjectSelectionStep"); // NOI18N
    
    /** Creates a new instance of ImporterPanel */
    ImporterWizardPanel(int wizardNumber) {
        super();
        putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
        putClientProperty("WizardPanel_contentSelectedIndex",  // NOI18N
                new Integer(wizardNumber));
        putClientProperty("WizardPanel_contentData", new String[] {
            WORKSPACE_LOCATION_STEP, PROJECT_SELECTION_STEP
        });
    }
    
    
    /**
     * Return message to be displayed as ErrorMessage by Eclipse importer
     * wizard. Default implementation returns null (no error message will be
     * displayed)
     */
    String getErrorMessage() {
        return null;
    }
    
    public void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList(2);
        }
        changeListeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        if (changeListeners != null) {
            if (changeListeners.remove(l) && changeListeners.isEmpty()) {
                changeListeners = null;
            }
        }
    }
    
    protected void fireChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            for (Iterator i = changeListeners.iterator(); i.hasNext(); ) {
                ((ChangeListener) i.next()).stateChanged(e);
            }
        }
    }
    
    public void storeSettings(Object settings) {;}
    
    public void readSettings(Object settings) {;}
}
