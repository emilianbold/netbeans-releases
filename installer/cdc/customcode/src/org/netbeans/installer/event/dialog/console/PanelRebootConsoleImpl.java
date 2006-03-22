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
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.wizard.service.system.*;
import com.installshield.util.*;
import java.util.Vector;
import java.io.*;
import com.installshield.wizard.*;

public class PanelRebootConsoleImpl {

    private static final String CAPTION =
        "$L(com.installshield.wizardx.i18n.WizardXResources, RebootPanel.mustRestart)";

    private static final String REBOOT_NOW =
        "$L(com.installshield.wizardx.i18n.WizardXResources, RebootPanel.restartNow)";
    private static final String REBOOT_LATER =
        "$L(com.installshield.wizardx.i18n.WizardXResources, RebootPanel.restartLater)";
    private static final String WIZARD_RESTART_VARIABLE = "IS_WIZARD_RESTART";

    private static final String REBOOTNOW_BUTTON_VARIABLE = "IS_REBOOT_NOW";
    private static final String REBOOTLATER_BUTTON_VARIABLE = "IS_REBOOT_LATER";
	Boolean restartWizard;
    public void consoleInteractionReboot(ISDialogContext context) {

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        WizardServices wServices = context.getServices();

        boolean rebootSelected = false;

        tty.printLine();
        tty.printLine(wServices.resolveString(CAPTION));

        String[] options =
            new String[] {
                MnemonicString.stripMn(wServices.resolveString(REBOOT_NOW)),
                MnemonicString.stripMn(wServices.resolveString(REBOOT_LATER))};
        ConsoleChoice choice = new ConsoleChoice(options, false);

        choice.setSelected(1);

        tty.printLine();
        choice.setOptionsIndent(TTYDisplay.DEFAULT_INDENT);
        choice.consoleInteraction(tty);

        if (choice.getSelectedIndex() == 0) {
            rebootSelected = true;
        }

        //set reboot on exit
        try {
            SystemUtilService service =
                (SystemUtilService)context.getService(SystemUtilService.NAME);
            service.setRebootOnExit(rebootSelected);
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

    }

    public void generateOptionsEntriesReboot(ISOptionsContext context) {

        String rebootNowValue = null;
        String rebootLaterValue = null;
        String option = null;
        String panelId = context.getPanel().getName();
        Vector optionEntries = context.getOptionEntries();

        try {

            if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
                rebootNowValue =
                    rebootLaterValue =
                        LocalizedStringResolver.resolve(
                            "com.installshield.wizard.i18n.WizardResources",
                            "WizardBean.valueStr");
            }
            else {
                rebootNowValue =
                    context
                        .getWizard()
                        .getServices()
                        .getISDatabase()
                        .getVariableValue(
                        REBOOTNOW_BUTTON_VARIABLE);
                rebootLaterValue =
                    context
                        .getWizard()
                        .getServices()
                        .getISDatabase()
                        .getVariableValue(
                        REBOOTLATER_BUTTON_VARIABLE);
            }
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

    public void queryEnterReboot(ISQueryContext context) {
	
		/**Set restartWizard to true if you wish to have the 
		* wizard restart after reboot with the next bean 
		* in the sequence.
		*/
		restartWizard = new Boolean(true);
				try {
					context.getServices().getISDatabase().setVariableValue(WIZARD_RESTART_VARIABLE,restartWizard.toString());
				}
				catch (Exception e) {
					context.getServices().logEvent(this, Log.ERROR, e);
				}

        SystemUtilService service = null;
        try {
            service =
                (SystemUtilService)context.getService(SystemUtilService.NAME);
            if (!service.isRebootRequired()) {
                context.setReturnValue(false);
            }

        }
        catch (Throwable e) {
            context.setReturnValue(false);
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

    public void queryExitReboot(ISDialogQueryContext context) {
        boolean rebootSelected = false;
		Boolean restartWizard = null;
		try {
			restartWizard = new Boolean(	context.getServices().getISDatabase().getVariableValue(WIZARD_RESTART_VARIABLE));
		}
		catch (Exception e) {
			context.getServices().logEvent(this, Log.ERROR, e);
		}

		if (!restartWizard.booleanValue()) {

        try {
            SystemUtilService service =
                (SystemUtilService)context.getService(SystemUtilService.NAME);
            service.setRebootOnExit(rebootSelected);
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }

    
	} else {
		try {
			SystemUtilService service = (SystemUtilService)context.getService(SystemUtilService.NAME);
			service.setRebootOnExit(true);
			WizardBean nextBean = context.getWizard().getIterator().getNext(context.getWizard().getCurrentBean());
			// if the current bean is the last wizard bean, then just reboot
			if (nextBean != context.getWizard().getIterator().end()) {
				String launcherFileName = System.getProperty("is.launcher.file");
				if (launcherFileName != null) {
					File launchFile = new File(launcherFileName);
					if (!launchFile.exists()) {
					} else {
						String restartCommand = launcherFileName +
												" -goto " +
												nextBean.getBeanId();
						service.addSystemStartupCommand(restartCommand);
					}
				} else {
					LogUtils.getLog().logEvent(this, 
							 Log.ERROR, 
							 "Restarting the wizard after a reboot is only " +
							 "supported through the use of a native launcher.");
				}
				context.getWizard().exit(ExitCodes.WIZARD_CALLBACK_REQUIRED);
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}	
	}
    }
}
