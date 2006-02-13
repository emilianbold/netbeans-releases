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

package org.netbeans.installer.event.dialog.silent;


import com.installshield.event.*;
import com.installshield.util.*;
import com.installshield.wizard.*;
import com.installshield.wizard.service.*;
import com.installshield.util.runtime.*;

public class PanelPasswordSilentImpl {

    private static final String TYPED_PASSWORD_VARIABLE = "IS_TYPED_PASSWORD";
    private static final String PASSWORD_HASH_VARIABLE = "IS_PASSWORD_HASH";
    private String invalidPasswordMessage =
        "$L(com.installshield.wizardx.i18n.WizardXResources, PasswordPanel.invalidPassword)";

    private String typedPasswordHash = null;

    public void silentExecutePassword(ISSilentContext context) {

        try {
            String typedPassword =
                context.getServices().getISDatabase().getVariableValue(
                    TYPED_PASSWORD_VARIABLE);
            String passwdHash =
                context.getServices().getISDatabase().getVariableValue(
                    PASSWORD_HASH_VARIABLE);

            if (passwdHash != null
                && !(PasswordUtils.isValidPassword(typedPassword, passwdHash))) {
                LogUtils.getLog().logEvent(
                    this,
                    Log.ERROR,
                    context.getServices().resolveString(
                        invalidPasswordMessage));
                context.getWizard().exit(ExitCodes.INVALID_OR_UNSET_PASSWORD);
            }
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

}
