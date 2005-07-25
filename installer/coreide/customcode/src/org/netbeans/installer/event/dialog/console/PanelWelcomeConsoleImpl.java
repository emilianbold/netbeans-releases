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

public class PanelWelcomeConsoleImpl {

    private static final String WELCOME_MESSAGE =
        "$L("
            + "com.installshield.product.i18n.ProductResources, "
            + "WelcomePanel.message, "
            + "$P(displayName), $P(displayName), "
            + "$P(displayName), $P(vendor), $P(vendorWebsite)"
            + ")";

    public void consoleInteractionWelcome(ISDialogContext context) {

    	ConsoleWizardUI consoleUI = (ConsoleWizardUI)context.getWizardUI();
        TTYDisplay tty = consoleUI.getTTY();
	
		//Display console interaction
        String welcomeText =
            new HtmlToTextConverter().convertText(
                context.getServices().resolveString(
                    WELCOME_MESSAGE));

        tty.printPage(welcomeText);
        tty.printLine();

    }

}
