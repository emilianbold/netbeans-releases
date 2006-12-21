/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.ui;

import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelConfigureProject;
import org.openide.WizardDescriptor;

public class ConfigureProjectPanel {
    PanelConfigureProject panel = null;

    public ConfigureProjectPanel(String name, String wizardTitle, String wizardACSD, boolean showMakefileTextField) {
	panel = new PanelConfigureProject(name, -1, wizardTitle, wizardACSD, showMakefileTextField);
    }

    public String getName() {
	return panel.getName();
    }

    public WizardDescriptor.Panel getPanel() {
        return (WizardDescriptor.Panel)panel;
    }
}
