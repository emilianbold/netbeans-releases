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
import com.installshield.wizard.*;
import com.installshield.database.designtime.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.util.*;
import com.installshield.event.*;

import java.util.Vector;

public class PanelLicenseConsoleImpl {

    private static final String LICENSE_AGREEMENT = "EULA_FILE";

    private static final String DESCRIPTION =
        "$L(com.installshield.product.i18n.ProductResources, LicensePanel.description)";
    private static final String APPROVE_CAPTION =
        "$L(com.installshield.product.i18n.ProductResources, LicensePanel.approval)";
    private static final String DISAPPROVE_CAPTION =
        "$L(com.installshield.product.i18n.ProductResources, LicensePanel.disapproval)";

    private static final String CHOICE_CAPTION =
        "$L(com.installshield.wizardx.i18n.WizardXResources, ApprovalPanel.consoleChoiceCaption)";
    public static final String WARNING_MSG =
        "$L(com.installshield.wizardx.i18n.WizardXResources, ApprovalPanel.cantContinueMsg)";

    private static final String ACCEPT_BUTTON_VARIABLE =
        "LICENSE_ACCEPT_BUTTON";
    private static final String REJECT_BUTTON_VARIABLE =
        "LICENSE_REJECT_BUTTON";
    public static final int HTML = 1;
    public static final int PLAIN_TEXT = 2;

    /* The CONTENT_TYPE should set to PLAIN_TEXT if the licence file text is plain text.
     * It  should be set to HTML if licence file text is HTML type.
     */ 
    public static int CONTENT_TYPE = PLAIN_TEXT;	

    public void consoleInteractionLicense(ISDialogContext context) {

        boolean disapprove = true;
        String licenseText = "";

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        WizardServices wServices = context.getServices();

        tty.printLine(wServices.resolveString(DESCRIPTION));
        tty.printLine();

        ConsoleChoice choice = new ConsoleChoice();
        String approveCaption =
            MnemonicString.stripMn(wServices.resolveString(APPROVE_CAPTION));
        String disapproveCaption =
            MnemonicString.stripMn(wServices.resolveString(DISAPPROVE_CAPTION));

        choice.setOptions(new String[] { approveCaption, disapproveCaption });
        choice.setMultiSelect(false);
        String caption = wServices.resolveString(CHOICE_CAPTION);

        //determine default choice
        try {
            String accept =
                context.getServices().getISDatabase().getVariableValue(
                    ACCEPT_BUTTON_VARIABLE);
            if (accept.equalsIgnoreCase("true")) {
                choice.setSelected(0);
            }
            else {
                choice.setSelected(1);
            }
        }
        catch (ServiceException e) {
            context.getServices().logEvent(this, Log.ERROR, e);
            choice.setSelected(1);
        }

        //resolve EULA from setup file
        try {
            ISSetupFileDef eulaFile =
                wServices.getISDatabase().getDatabaseDef().getSetupFile(
                    LICENSE_AGREEMENT);
            if (eulaFile != null) {
                String filePath =
                    context.getWizard().getRuntimeFileResourceLocation(
                        eulaFile.getStorageKey(),
                        true,
                        wServices);
                licenseText = FileUtils.readTextFromFile(filePath, null);
            }
        }
        catch (ServiceException e) {
            context.getServices().logEvent(this, Log.ERROR, e);
        }

        tty.printLine();
        tty.printLine();
        if(CONTENT_TYPE == PLAIN_TEXT) {
            tty.printPage(licenseText);
        }
        else {
            tty.printPage(new HtmlToTextConverter().convertText(licenseText));
        }
        tty.printLine();
        tty.printLine();
        tty.printLine(wServices.resolveString(caption));

        while (disapprove) {
            choice.consoleInteraction(tty);
            if (choice.getSelectedIndex() == 0) {
                disapprove = false;
                try {
                    context.getServices().getISDatabase().setVariableValue(
                        ACCEPT_BUTTON_VARIABLE,
                        "true");
                    context.getServices().getISDatabase().setVariableValue(
                        REJECT_BUTTON_VARIABLE,
                        "false");
                }
                catch (Exception e) {
                    context.getServices().logEvent(this, Log.ERROR, e);
                }
            }
            else {
                tty.printLine();
                tty.printLine(wServices.resolveString(WARNING_MSG));
                tty.printLine();
            }
        }

        tty.printLine();

    }

    public void generateOptionsEntriesLicense(ISOptionsContext context) {

        String acceptValue = null;
        String rejectValue = null;
        String option = null;
        String panelId = context.getPanel().getName();
        Vector optionEntries = context.getOptionEntries();

        try {

            if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
                acceptValue =
                    rejectValue =
                        LocalizedStringResolver.resolve(
                            "com.installshield.wizard.i18n.WizardResources",
                            "WizardBean.valueStr");
            }
            else {
                acceptValue =
                    context.getServices().getISDatabase().getVariableValue(
                        ACCEPT_BUTTON_VARIABLE);
                rejectValue =
                    context.getServices().getISDatabase().getVariableValue(
                        REJECT_BUTTON_VARIABLE);
            }

            String doc =
                "The initial state of the License panel.  The accept and reject option states are stored as Variables and must be set with -V";

            option =
                "-V " + ACCEPT_BUTTON_VARIABLE + "=\"" + acceptValue + "\"";
            optionEntries.addElement(
                new OptionsTemplateEntry(
                    "Custom Dialog: " + panelId,
                    doc,
                    option));

            option =
                "-V " + REJECT_BUTTON_VARIABLE + "=\"" + rejectValue + "\"";
            optionEntries.addElement(
                new OptionsTemplateEntry(
                    "Custom Dialog: " + panelId,
                    doc,
                    option));
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

}
