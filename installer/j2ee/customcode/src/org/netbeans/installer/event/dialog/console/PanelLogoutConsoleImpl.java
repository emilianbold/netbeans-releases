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


import com.installshield.event.*;
import com.installshield.event.ui.*;
import com.installshield.wizard.console.*;
import com.installshield.wizard.service.system.*;
import com.installshield.util.*;

public class PanelLogoutConsoleImpl {

    private static final String LOGOUT_TEXT =
        "$L(com.installshield.wizardx.i18n.WizardXResources, LogoutPanel.text)";

    public void queryEnterLogout(ISQueryContext context) {

        SystemUtilService service = null;
        try {
            service =
                (SystemUtilService)context.getService(SystemUtilService.NAME);
            context.setReturnValue(service.isLogoutRequired());
        }
        catch (Throwable e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

    public void consoleInteractionLogout(ISDialogContext context) {

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        tty.printLine();
        tty.printLine(
            context.getServices().resolveString(LOGOUT_TEXT));
    }

}
