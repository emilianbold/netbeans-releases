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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
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
    
    /** Panel validity flag */
    private boolean valid;
    
    /** Error message displayed by wizard. */
    private String errorMessage;
    
    static final String WORKSPACE_LOCATION_STEP =
            NbBundle.getMessage(ImporterWizardPanel.class, "CTL_WorkspaceLocationStep"); // NOI18N
    static final String PROJECT_SELECTION_STEP =
            NbBundle.getMessage(ImporterWizardPanel.class, "CTL_ProjectSelectionStep"); // NOI18N
    
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
    
    /** Sets error message used by importer wizard. */
    protected void setErrorMessage(String newError) {
        boolean changed =
                (errorMessage == null && newError != null) ||
                (errorMessage != null && !errorMessage.equals(newError));
        if (changed) errorMessage = newError;
        setValid(newError == null, changed);
    }
    
    
    /** Sets if the current state of panel is valid or not. */
    protected void setValid(boolean valid, boolean forceFiring) {
        boolean changed = this.valid != valid;
        if (changed) this.valid = valid;
        if (changed || forceFiring) {
            fireChange();
        }
    }
    
    /** Returns error message used by importer wizard. */
    String getErrorMessage() {
        return errorMessage;
    }
    
    
    public boolean isValid() {
        return valid;
    }
    
    public HelpCtx getHelp() {
        return null;
    }
    
    public Component getComponent() {
        return this;
    }
    
    public void storeSettings(Object settings) {;}
    
    public void readSettings(Object settings) {;}
}
