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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * @author Pavel Buzek
 */
final class EntityResourcesSetupPanel extends AbstractPanel {
    
    private EntityResourcesSetupPanelVisual component;
    
    /** Create the wizard panel descriptor. */
    public EntityResourcesSetupPanel(String name, WizardDescriptor wizardDescriptor) {
        super(name, wizardDescriptor);
    }
    
    public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        if (component == null) {
            component = new EntityResourcesSetupPanelVisual(panelName);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
}
