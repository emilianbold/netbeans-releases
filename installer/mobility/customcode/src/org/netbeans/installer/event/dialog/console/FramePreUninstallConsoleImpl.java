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

public class FramePreUninstallConsoleImpl {

    public void preConsoleInteractionPreUninstall(ISDialogFrameContext context) {

        ConsoleWizardUI consoleUI = (ConsoleWizardUI)context.getWizardUI();
        TTYDisplay tty = consoleUI.getTTY();

        //Display header information
        tty.printLine();
        tty.printHRule();
        tty.printLine(context.getISFrame().getTitle());
        tty.printLine();

        //set navigation options
        consoleUI.setCancelType(ConsoleWizardUI.CANCEL);
        consoleUI.showNext(true);
        consoleUI.showBack(true);
        consoleUI.showHelp(false);
        consoleUI.showCancel(true);

    }

    public void postConsoleInteractionPreUninstall(ISDialogFrameContext context) {

        ConsoleWizardUI consoleUI = (ConsoleWizardUI)context.getWizardUI();

        //show navigation
        consoleUI.displayOptions();
    }

}
