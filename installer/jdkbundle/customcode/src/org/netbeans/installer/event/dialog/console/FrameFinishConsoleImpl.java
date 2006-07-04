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

public class FrameFinishConsoleImpl {

    public void preConsoleInteractionFinish(ISDialogFrameContext context) {

        ConsoleWizardUI consoleUI = (ConsoleWizardUI)context.getWizardUI();
        TTYDisplay tty = consoleUI.getTTY();

        //Display header information
        tty.printLine();
        tty.printHRule();
        tty.printLine(context.getISFrame().getTitle());
        tty.printLine();

        //set navigation options
        consoleUI.setCancelType(ConsoleWizardUI.CLOSE);
        consoleUI.showNext(false);
        consoleUI.showBack(false);
        consoleUI.showHelp(false);
        consoleUI.showCancel(true);

    }

    public void postConsoleInteractionFinish(ISDialogFrameContext context) {

        ConsoleWizardUI consoleUI = (ConsoleWizardUI)context.getWizardUI();

        //show navigation
        consoleUI.displayOptions();
    }

}
