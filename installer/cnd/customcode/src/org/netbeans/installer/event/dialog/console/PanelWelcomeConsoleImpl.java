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
