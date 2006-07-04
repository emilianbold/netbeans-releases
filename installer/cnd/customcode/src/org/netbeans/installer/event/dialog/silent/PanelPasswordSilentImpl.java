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
