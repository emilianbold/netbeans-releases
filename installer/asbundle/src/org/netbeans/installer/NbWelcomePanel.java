/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;

public class NbWelcomePanel extends TextDisplayPanel {

    public NbWelcomePanel() {
        setTextSource(TEXT_PROPERTY);
        setContentType(HTML_CONTENT_TYPE);
        setDescription("");
    }
    
    public boolean queryEnter(WizardBeanEvent evt) {
        boolean okay = super.queryEnter(evt);
        setText(resolveString("$L(org.netbeans.installer.Bundle,InstallWelcomePanel.text,"
        + "$L(org.netbeans.installer.Bundle,Product.displayName),"
        + "$L(org.netbeans.installer.Bundle,AS.name))"));
        return okay;
    }
    
}
