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
import org.openide.util.NbBundle;

/**
 * FinishableProxyWizardPanel.java - used decorator pattern to enable to finish 
 * the original wizard panel, that is not finishable
 * 
 *
 * @author mkuchtiak
 */
public class FinishableProxyWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private boolean enableFinish = false;        
    private WizardDescriptor.Panel original;
    private WizardDescriptor wizard;
    
    /** Creates a new instance of ProxyWizardPanel */
    public FinishableProxyWizardPanel(WizardDescriptor.Panel original, boolean enableFinish) {
        this.original=original;
        this.enableFinish = enableFinish;
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
        wizard = (WizardDescriptor)settings;
        original.readSettings(settings);
    }

    public boolean isValid() {
        setErrorMessage();
        return original.isValid() && enableFinish;
    }

    public boolean isFinishPanel() {
        setErrorMessage();
        return enableFinish;
    }

    public java.awt.Component getComponent() {
        setErrorMessage();
        return original.getComponent();
    }

    public org.openide.util.HelpCtx getHelp() {
        return original.getHelp();
    }
    
    private void setErrorMessage() {
        if (!enableFinish) {
            if (wizard != null) {
                wizard.putProperty ("WizardPanel_errorMessage", NbBundle.getMessage(FinishableProxyWizardPanel.class, "ERR_NotSupportedInbJavaEE4")); // NOI18N
            }
        }
    }
}
