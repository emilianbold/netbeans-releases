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
import com.installshield.event.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.database.designtime.*;
import com.installshield.util.*;
import com.installshield.util.runtime.*;
import com.installshield.wizard.*;

public class PanelPasswordConsoleImpl {

    private static final String TYPED_PASSWORD_VARIABLE = "IS_TYPED_PASSWORD";
    private static final String PASSWORD_HASH_VARIABLE = "IS_PASSWORD_HASH";
    private static final String IS_VALIDATED_VAR = "IS_VALIDATED_PASSWORD";

    private static final String DESCRIPTION =
        "$L(com.installshield.wizardx.i18n.WizardXResources,PasswordPanel.description)";
    private static final String CAPTION =
        "$L(com.installshield.wizardx.i18n.WizardXResources,PasswordPanel.label)";

    public void consoleInteractionPassword(ISDialogContext context) {

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        WizardServices wServices = context.getServices();

        tty.printLine(wServices.resolveString(DESCRIPTION));
        tty.printLine();

        String pwd =
            tty.promptPassword(
                MnemonicString.stripMn(wServices.resolveString(CAPTION)));
        setTypedPassword(wServices, pwd);

    }

    public void generateOptionsEntriesPassword(ISOptionsContext context) {

        String value;
        String option;

        try {
            if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
                value =
                    LocalizedStringResolver.resolve(
                        "com.installshield.wizard.i18n.WizardResources",
                        "WizardBean.valueStr");
            }
            else {
                value =
                    context
                        .getWizard()
                        .getServices()
                        .getISDatabase()
                        .getVariableValue(
                        TYPED_PASSWORD_VARIABLE);
            }

            String doc =
                LocalizedStringResolver.resolve(
                    "com.installshield.wizardx.i18n.WizardXResources",
                    "RuntimePasswordPanel.ote1Doc");
            if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
                doc += PasswordUtils.PASSWORD_OPTIONS_TEMPLATE_STRING;
            }
            else {
                doc
                    += LocalizedStringResolver.resolve(
                        "com.installshield.wizardx.i18n.WizardXResources",
                        "RuntimePasswordPanel.ote1HashRecorded");
            }

            option = "-V " + TYPED_PASSWORD_VARIABLE + "=\"" + value + "\"";
            context.getOptionEntries().addElement(
                new OptionsTemplateEntry(
                    "Custom Dialog: " + context.getPanel().getName(),
                    "",
                    option));
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

    public void queryEnterPassword(ISQueryContext context) {

        try {

            String isValidated =
                context.getServices().getISDatabase().getVariableValue(
                    IS_VALIDATED_VAR);
            String typedPassword =
                context.getServices().getISDatabase().getVariableValue(
                    TYPED_PASSWORD_VARIABLE);
            String passwdHash =
                context.getServices().getISDatabase().getVariableValue(
                    PASSWORD_HASH_VARIABLE);

            // if the panel has already validated the password, or its correct to begin with, skip
            // we do not want to skip if the passwordHash has not been specified. This
            // takes care of the case where the required password is an empty string. We
            // still want to visit this panel in this case, but any password is accepted.
            if ((isValidated != null)
                || (passwdHash != null)
                && PasswordUtils.isValidPassword(typedPassword, passwdHash)) {
                context.setReturnValue(false);
            }
            else {
                context.setReturnValue(true);
            }

        }
        catch (ServiceException e) {
            context.getServices().logEvent(this, Log.ERROR, e);
        }
    }

    public void queryExitPassword(ISDialogQueryContext context) {

        try {
            String typedPassword =
                context.getServices().getISDatabase().getVariableValue(
                    TYPED_PASSWORD_VARIABLE);
            String passwdHash =
                context.getServices().getISDatabase().getVariableValue(
                    PASSWORD_HASH_VARIABLE);

            if (!PasswordUtils.isValidPassword(typedPassword, passwdHash)) {
                context.getWizardUI().displayUserMessage(
                    context.getServices().resolveString(
                        context.getISFrame().getTitle()),
                    context.getServices().resolveString(
                        "$L(com.installshield.wizardx.i18n.WizardXResources,PasswordPanel.invalidPassword)"),
                    UserInputRequest.MESSAGE);

                context.setReturnValue(false);
            }
            else {
                //set bean inactive, so it will cannot revisited once validated
                context.getServices().getISDatabase().setVariableValue(
                    IS_VALIDATED_VAR,
                    "true");
                context.getWizard().getCurrentBean().setActive(false);
                context.setReturnValue(true);
            }
        }
        catch (Exception e) {
            context.getServices().logEvent(this, Log.ERROR, e);
        }
    }

    private void setTypedPassword(WizardServices wServices, String password) {
        String passwordHash =
            PasswordUtils.encryptPassword(
                PasswordUtils.encryptPassword(password));
        try {
            ISDatabaseDef isDb = wServices.getISDatabase().getDatabaseDef();
            ISVariableDef passwordVar =
                isDb.getVariable(TYPED_PASSWORD_VARIABLE);
            if (passwordVar != null) {
                passwordVar.setValue(passwordHash);
            }
        }
        catch (Exception e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

    }

}
