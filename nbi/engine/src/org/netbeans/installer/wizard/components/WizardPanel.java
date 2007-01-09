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
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components;

import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class WizardPanel extends WizardComponent {
    protected WizardUi wizardUi;
    
    protected WizardPanel() {
        // does nothing
    }
    
    public final void executeForward() {
        // does nothing
    }
    
    public final void executeBackward() {
        // does nothing
    }
    
    public void initialize() {
        // does nothing
    }
    
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new WizardPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class WizardPanelUi extends WizardComponentUi {
        protected WizardPanel        component;
        
        public WizardPanelUi(WizardPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WizardPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class WizardPanelSwingUi extends WizardComponentSwingUi {
        protected WizardPanel component;
        
        public WizardPanelSwingUi(
                final WizardPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
        }
    }
}