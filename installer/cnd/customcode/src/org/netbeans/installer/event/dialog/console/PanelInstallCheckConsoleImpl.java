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

package org.netbeans.installer.event.dialog.console;


import com.installshield.event.ui.*;
import com.installshield.database.designtime.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.product.service.product.*;
import com.installshield.wizardx.conditions.*;
import com.installshield.util.*;

public class PanelInstallCheckConsoleImpl {

    private static final String SUMMARY_KEY =
        InstallCheckWizardBeanCondition.PROPERTY_KEY
            + ProductService.SUMMARY_MSG.replace('.', '_');

    public void consoleInteractionInstallCheck(ISDialogContext context) {

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
