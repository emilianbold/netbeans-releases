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

package org.netbeans.installer.event.dialog.console;


import com.installshield.event.ui.*;
import com.installshield.database.designtime.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.product.service.product.*;
import com.installshield.wizardx.conditions.*;
import com.installshield.util.*;

public class PanelInstalledProductCheckConsoleImpl {

    private static final String SUMMARY_KEY =
        InstallCheckWizardBeanCondition.PROPERTY_KEY
            + ProductService.SUMMARY_MSG.replace('.', '_');

    public void consoleInteractionInstalledProductCheck(ISDialogContext context) {

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        WizardServices wServices = context.getServices();

        //Display console interaction
        String summaryText = getSummary(wServices);
        tty.printLine();
        tty.printPage(new HtmlToTextConverter().convertText(summaryText));

        //Disable Next in Navigation options
        ConsoleWizardUI consoleUI = (ConsoleWizardUI)context.getWizardUI();

        consoleUI.showNext(false);

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
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
            return "";
        }
    }

}
