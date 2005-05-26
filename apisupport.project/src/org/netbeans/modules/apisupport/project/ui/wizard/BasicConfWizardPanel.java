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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * Second panel of <code>NewNbModuleWizardIterator</code>. Allow user to enter
 * basic configuration:
 *
 * <ul>
 *  <li>Code Name Base</li>
 *  <li>Module Display Name</li>
 *  <li>Localizing Bundle</li>
 *  <li>XML Layer</li>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 *
 * @author mkrauskopf
 */
final class BasicConfWizardPanel implements PropertyChangeListener,
        WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel {
    
    /** Representing visual component for this step. */
    private BasicConfVisualPanel visualPanel;
    
    private WizardDescriptor settings;
    
    private EventListenerList listeners = new EventListenerList();
    
    private boolean valid;
    
    /** Creates a new instance of BasicConfWizardPanel */
    public BasicConfWizardPanel(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    public void readSettings(Object settings) {
        visualPanel.refreshData();
    }
    
    public void storeSettings(Object settings) {
        visualPanel.storeData();
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(ChangeListener.class, l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(ChangeListener.class, l);
    }
    
    protected void fireChange() {
        ChangeListener[] chListeners = (ChangeListener[]) listeners.
                getListeners(ChangeListener.class);
        ChangeEvent e = new ChangeEvent(this);
        for (int i = 0; i < chListeners.length; i++) {
            chListeners[i].stateChanged(e);
        }
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicConfVisualPanel(settings);
            visualPanel.addPropertyChangeListener(this);
        }
        return visualPanel;
    }
    
    public HelpCtx getHelp() {
        return null;
    }
    
    public boolean isValid() {
        return true; // TODO
    }
    
    public void validate() throws WizardValidationException {
        // TODO (if needed, don't implement ValidatingPanel otherwise)
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ("valid".equals(evt.getPropertyName())) {
            this.valid = ((Boolean) evt.getNewValue()).booleanValue();
            fireChange();
        }
    }
}
