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
