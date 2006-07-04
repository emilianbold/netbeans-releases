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


import com.installshield.database.designtime.*;
import com.installshield.product.service.product.ProductService;
import com.installshield.wizardx.conditions.InstallCheckWizardBeanCondition;
import com.installshield.event.ui.*;
import com.installshield.wizard.service.*;
import com.installshield.util.*;
import com.installshield.ui.controls.*;

public class PanelInstallCheck {

    private static final String SUMMARY_KEY =
        InstallCheckWizardBeanCondition.PROPERTY_KEY
            + ProductService.SUMMARY_MSG.replace('.', '_');
    private static final String INSTALL_CHECK_HTML_CONTROL =
        "INSTALL_CHECK_SUMMARY";

    public void initializeUIInstallCheck(ISDialogContext context) {
    	
        ISHtmlControl htmlCtrl =
            context.getISPanel().getISHtmlControl(INSTALL_CHECK_HTML_CONTROL);
        htmlCtrl.setContentType(ISHtmlControl.HTML_CONTENT_TYPE);

        String summary = getSummary(context.getServices());
        htmlCtrl.setText(summary);
        
        ISFrame frame = context.getISFrame();
        ISButton nextButton = frame.getButton("next");
        nextButton.setEnabled(false);

    }

    private String getSummary(WizardServices wServices) {
        try {
            ISDatabaseDef db = wServices.getISDatabase().getDatabaseDef();
            ISVariableDef var = db.getVariable(SUMMARY_KEY);
            if (var == null) {
                return "";
            }
            return var.getValue();
        }
        catch (ServiceException e) {
            wServices.logEvent(this, Log.ERROR, e);
            return null;
        }
    }
}
