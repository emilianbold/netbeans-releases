/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.awt.Component;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 *
 * Class handling the standard Agent wizard panel
 *
 */
public abstract class GenericWizardPanel 
        implements WizardDescriptor.Panel {
    
    /**
     * Returns the panel component.
     * @return <CODE>Component</CODE> the panel component.
     */
    public abstract Component getComponent() ;
    
    /**
     * Returns the corresponding help context.
     * @return <CODE>HelpCtx</CODE> the corresponding help context.
     */
    public HelpCtx getHelp() {
        return null;
    }
    
    /**
     * Returns if the user is able to go to next step and to finish the wizard.
     * @return <CODE>boolean</CODE> true only if the user can go to next step 
     * and finish the wizard.
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * Called to read information from the wizard map in order to populate
     * the GUI correctly.
     * @param settings <CODE>Object</CODE> an object containing 
     * the wizard informations.
     */
    public void readSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
    }
    
    /**
     * Called to store information from the GUI into the wizard map.
     * @param settings <CODE>Object</CODE> an object containing 
     * the wizard informations.
     */
    public void storeSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
    }
    
    private final Set listeners = new HashSet(1); // Set<ChangeListener>
    
    /** Add a listener to changes of the panel's validity.
     * @param l <CODE>ChangeListener</CODE> the listener to add
     */
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
} 

