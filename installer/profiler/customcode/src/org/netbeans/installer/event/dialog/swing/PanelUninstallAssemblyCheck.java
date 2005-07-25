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

package org.netbeans.installer.event.dialog.swing;


import com.installshield.event.ui.*;
import com.installshield.util.*;
import com.installshield.ui.controls.*;

public class PanelUninstallAssemblyCheck{

     private static final String HTML_CONTROL =
        "UNINSTALL_ASSEMBLY_CHECK_SUMMARY";

    public void initializeUIUninstallAssemblyCheck(ISDialogContext context) {
    	
        ISHtmlControl htmlCtrl =
            context.getISPanel().getISHtmlControl(HTML_CONTROL);
        htmlCtrl.setContentType(ISHtmlControl.HTML_CONTENT_TYPE);

        String summary = LocalizedStringResolver.resolve(
                "com.installshield.wizardx.i18n.WizardXResources",
        		"UninstallAssemblyCheckPanel.errorMessage",
        		new String[]{"$P(displayName)","$P(displayName)"});

        htmlCtrl.setText(summary);
        
        ISFrame frame = context.getISFrame();
        ISButton nextButton = frame.getButton("next");
        nextButton.setEnabled(false);

    }
}