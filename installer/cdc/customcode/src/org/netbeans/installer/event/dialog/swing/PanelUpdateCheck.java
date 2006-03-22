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


import com.installshield.database.designtime.*;
import com.installshield.product.service.product.ProductService;
import com.installshield.wizardx.conditions.InstallCheckWizardBeanCondition;
import com.installshield.event.ui.*;
import com.installshield.wizard.service.*;
import com.installshield.util.*;
import com.installshield.ui.controls.*;

public class PanelUpdateCheck {

    private static final String SUMMARY_KEY =
        InstallCheckWizardBeanCondition.PROPERTY_KEY
            + ProductService.SUMMARY_MSG.replace('.', '_');
    private static final String INSTALL_CHECK_HTML_CONTROL =
        "UPDATE_CHECK_SUMMARY";

    public void initializeUIUpdateCheck(ISDialogContext context) {

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
