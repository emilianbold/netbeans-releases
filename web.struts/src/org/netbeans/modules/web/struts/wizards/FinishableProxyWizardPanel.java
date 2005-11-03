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

package org.netbeans.modules.web.struts.wizards;

import java.awt.Component;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;

/**
 * FinishableProxyWizardPanel.java - used decorator pattern to enable to finish 
 * the original wizard panel, that is not finishable
 * 
 *
 * @author mkuchtiak
 */
public class FinishableProxyWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor.Panel original;
    /** Creates a new instance of ProxyWizardPanel */
    public FinishableProxyWizardPanel(WizardDescriptor.Panel original) {
        this.original=original;
        
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
        original.addChangeListener(l);
    }

    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        original.removeChangeListener(l);
    }

    public void storeSettings(Object settings) {
        original.storeSettings(settings);
    }

    public void readSettings(Object settings) {
        original.readSettings(settings);
    }

    public boolean isValid() {
        return original.isValid();
    }

    public boolean isFinishPanel() {
        return true;
    }

    public java.awt.Component getComponent() {
        return original.getComponent();
    }

    public org.openide.util.HelpCtx getHelp() {
        return original.getHelp();
    }
    
}
