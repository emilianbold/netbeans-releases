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
import org.openide.WizardDescriptor.FinishablePanel;

/**
 *
 * Class used to become non finishable wizard panel in finishable wizard panel
 *
 */
public class FinishableDelegatedWizardPanel 
        implements WizardDescriptor.Panel, FinishablePanel {
    
    /**
     * The wrapped panel
     */
    protected final WizardDescriptor.Panel delegate;
    /**
     * The panel which allows Finish button enabled.
     */
    protected final WizardDescriptor.Panel finishDelegate;
    
    /**
     * Construct a new instance.
     * @param delegate <CODE>WizardDescriptor.Panel</CODE> Panel which wants to be finishable.
     * @param finishDelegate <CODE>WizardDescriptor.Panel</CODE> Panel which realize the finish actions.
     */
    public FinishableDelegatedWizardPanel(WizardDescriptor.Panel delegate,
            GenericWizardPanel finishDelegate) {
        super();
        this.delegate = delegate;
        this.finishDelegate = finishDelegate;
    }
    
    /**
     * Returns if this panel is finishable.
     * @return <CODE>boolean</CODE> true only if this panel is finishable.
     */
    public boolean isFinishPanel() {
        return ((FinishablePanel) finishDelegate).isFinishPanel();
    }
    
    /**
     * Returns the panel component.
     * @return <CODE>Component</CODE> the panel component.
     */
    public Component getComponent() {
        return delegate.getComponent();
    };
    
    /**
     * Returns the corresponding help context.
     * @return <CODE>HelpCtx</CODE> the corresponding help context.
     */
    public HelpCtx getHelp() {
        return delegate.getHelp();
    }
    
    /**
     * Returns if the user is able to go to next step and to finish the wizard.
     * @return <CODE>boolean</CODE> true only if the user can go to next step 
     * and finish the wizard.
     */
    public boolean isValid() {
        return delegate.isValid();
    }
    
    /**
     * This method is called when a step is loaded.
     * @param settings <CODE>Object</CODE> an object containing the wizard informations.
     */
    public void readSettings(Object settings) {
        delegate.readSettings(settings);
        finishDelegate.storeSettings(settings);
        finishDelegate.readSettings(settings);
    }
    
    /**
     * This method is used to force the delegate panel to load wizard informations.
     * @param settings <CODE>Object</CODE> an object containing the wizard informations.
     */
    public void readAllSettings(Object settings) {
        delegate.readSettings(settings);
    }
    
    /**
     * This method is called when the user quit a step.
     * @param settings <CODE>Object</CODE> an object containing the wizard informations.
     */
    public void storeSettings(Object settings) {
        finishDelegate.storeSettings(settings);
    }
    
    /**
     * This method is used to force the delegate panel to store user informations 
     * into the wizard informations.
     * @param settings <CODE>Object</CODE> an object containing the wizard informations.
     */
    public void storeAllSettings(Object settings) {
        delegate.storeSettings(settings);
    }
    
    /** Add a listener to changes of the panel's validity.
      * @param l <CODE>ChangeListener</CODE> the listener to add
      */
    public final void addChangeListener(ChangeListener l) {
        delegate.addChangeListener(l);
    }
    
    /** Remove a listener to changes of the panel's validity.
      * @param l <CODE>ChangeListener</CODE> the listener to remove
      */
    public final void removeChangeListener(ChangeListener l) {
        delegate.removeChangeListener(l);
    }
} 

