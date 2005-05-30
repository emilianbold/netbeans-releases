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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Basic wizard panel for APISupport projects.
 *
 * @author mkrauskopf
 */
abstract class BasicWizardPanel implements WizardDescriptor.Panel, PropertyChangeListener {
    
    private boolean valid;
    private WizardDescriptor settings;
    
    private EventListenerList listeners = new EventListenerList();
    
    BasicWizardPanel(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    WizardDescriptor getSettings() {
        return settings;
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
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    protected String getMessage(String key) {
        return NbBundle.getMessage(BasicWizardPanel.class, key);
    }
    
    public HelpCtx getHelp() {
        return null;
    }
    
    public void storeSettings(Object settings) {;}
    
    public void readSettings(Object settings) {;}
    
    void setValid(boolean valid) {
        this.valid = valid;
        fireChange();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Mainly for receiving events from wrapped component about its validity.
     * Firing events further to Wizard descriptor so it will reread this panel's
     * state and reenable/redisable its next/prev/finish/... buttons.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("valid".equals(evt.getPropertyName())) { // NOI18N
            this.valid = ((Boolean) evt.getNewValue()).booleanValue();
            fireChange();
        }
    }
}
