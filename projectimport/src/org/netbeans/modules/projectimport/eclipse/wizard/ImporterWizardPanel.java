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

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Basic wizard panel for Eclipse Wizard importer.
 *
 * @author mkrauskopf
 */
abstract class ImporterWizardPanel implements WizardDescriptor.Panel {

    /** Registered ChangeListeners */
    private List changeListeners;
    
    /** Panel validity flag */
    private boolean valid;
    
    /** Error message displayed by wizard. */
    private String errorMessage;
    
    static final String WORKSPACE_LOCATION_STEP =
            ProjectImporterWizard.getMessage("CTL_WorkspaceLocationStep"); // NOI18N
    static final String PROJECT_SELECTION_STEP =
            ProjectImporterWizard.getMessage("CTL_ProjectSelectionStep"); // NOI18N
    static final String PROJECTS_SELECTION_STEP =
            ProjectImporterWizard.getMessage("CTL_ProjectsSelectionStep"); // NOI18N
    
    /* Init defaults for the given component. */
    void initPanel(JComponent comp, int wizardNumber) {
        comp.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        comp.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        comp.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
        comp.putClientProperty("WizardPanel_contentSelectedIndex",  // NOI18N
                new Integer(wizardNumber));
        comp.putClientProperty("WizardPanel_contentData", new String[] { // NOI18N
            WORKSPACE_LOCATION_STEP, PROJECTS_SELECTION_STEP
        });
        comp.setPreferredSize(new java.awt.Dimension(500, 380));
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
    
    /**
     * Sets error message used by importer wizard. Consequently sets validity of
     * this panel. If the given <code>newError</code> is null panel is
     * considered valid. Invalid otherwise.
     */
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
    
    public void storeSettings(Object settings) {;}
    
    public void readSettings(Object settings) {;}
}
