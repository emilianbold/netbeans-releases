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
import com.installshield.wizard.console.*;
import com.installshield.util.*;

public class PanelUninstallAssemblyCheckConsoleImpl {


    private final String ERROR_MSG =
        "$L("
            + "com.installshield.wizardx.i18n.WizardXResources, "
            + "UninstallAssemblyCheckPanel.errorMessage, "
            + "$P(displayName), $P(displayName)"
            + ")";

    public void consoleInteractionUninstallAssemblyCheck(ISDialogContext context) {

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();

        //Display console interaction
        String summaryText = context.getServices().resolveString(
                ERROR_MSG);
        tty.printLine();
        tty.printPage(new HtmlToTextConverter().convertText(summaryText));

        //Disable Next in Navigation options
        ConsoleWizardUI consoleUI = (ConsoleWizardUI)context.getWizardUI();

        consoleUI.showNext(false);
    }
}
