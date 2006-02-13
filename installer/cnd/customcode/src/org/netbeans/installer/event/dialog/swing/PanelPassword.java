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


import com.installshield.event.*;
import com.installshield.event.ui.*;
import com.installshield.wizard.*;
import com.installshield.wizard.service.*;
import com.installshield.util.*;
import com.installshield.util.runtime.*;
import com.installshield.ui.controls.*;

public class PanelPassword {

    private static final String TYPED_PASSWORD_VARIABLE = "IS_TYPED_PASSWORD";
    private static final String PASSWORD_HASH_VARIABLE = "IS_PASSWORD_HASH";
    private static final String PASSWORD_CTRL_ID = "IS_PASSWORD_CONTROL";
    private static final String IS_VALIDATED_VAR = "IS_VALIDATED_PASSWORD";

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
                doc += LocalizedStringResolver.resolve(
                        "com.installshield.wizardx.i18n.WizardXResources",
                        "RuntimePasswordPanel.ote1HashRecorded");
            }

            option = "-V " + TYPED_PASSWORD_VARIABLE + "=\"" + value + "\"";
            context.getOptionEntries().addElement(
                new OptionsTemplateEntry(
                    "Custom Dialog: " + context.getPanel().getName(),
                    doc,
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

        ISPasswordControl pwdCtrl =
            (ISPasswordControl)context.getISPanel().getControl(
                PASSWORD_CTRL_ID);

        if (!pwdCtrl.isPasswordValid()) {
            context.getWizardUI().displayUserMessage(
                context.getServices().resolveString(
                    context.getISFrame().getTitle()),
                context.getServices().resolveString(
                    "$L(com.installshield.wizardx.i18n.WizardXResources,PasswordPanel.invalidPassword)"),
                UserInputRequest.MESSAGE);

            pwdCtrl.setText("");
            context.setReturnValue(false);
        }
        else {
            try {
                //set bean inactive, so it will cannot revisited once validated
                context.getServices().getISDatabase().setVariableValue(
                    IS_VALIDATED_VAR,
                    "true");
                context.getWizard().getCurrentBean().setActive(false);
                context.setReturnValue(true);
            }
            catch (Exception e) {
                context.getServices().logEvent(this, Log.ERROR, e);
            }
        }
    }
}