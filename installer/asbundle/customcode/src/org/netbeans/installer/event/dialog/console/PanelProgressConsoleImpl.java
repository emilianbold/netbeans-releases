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
import com.installshield.wizard.*;
import com.installshield.wizard.console.*;
import com.installshield.wizardx.progress.*;

public class PanelProgressConsoleImpl {

    public void consoleInteractionProgress(ISDialogContext context) {
        ConsoleWizardUI wizUI = (ConsoleWizardUI)context.getWizardUI();

        ProgressRenderer pr = new StandardProgressRenderer();
        ConsoleProgressRendererImpl prImpl =
            new StandardProgressRendererConsoleImpl();
        pr.setProgressRendererImpl(prImpl);
        prImpl.setConsoleWizardUI(wizUI);
        prImpl.setProgressRenderer(pr);

        wizUI.setCurrentConsoleProgressRenderer(pr);
    }

}
