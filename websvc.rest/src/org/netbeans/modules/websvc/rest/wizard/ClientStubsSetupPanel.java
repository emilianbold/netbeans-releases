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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.wizard;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Nam Nguyen
 */
public class ClientStubsSetupPanel extends AbstractPanel implements WizardDescriptor.Panel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ClientStubsSetupPanelVisual component;

    public ClientStubsSetupPanel(String name, WizardDescriptor wizardDescriptor) {
        super(name, wizardDescriptor);
    }
    
    public ClientStubsSetupPanelVisual getComponent() {
        if (component == null) {
            component = new ClientStubsSetupPanelVisual(getName());
            component.addChangeListener(this);
        }
        return component;
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
}

