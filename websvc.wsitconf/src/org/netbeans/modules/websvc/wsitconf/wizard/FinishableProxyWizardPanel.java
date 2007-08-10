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

package org.netbeans.modules.websvc.wsitconf.wizard;

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
