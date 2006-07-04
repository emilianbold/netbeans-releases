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

package org.netbeans.installer.event.dialog.swing;


import java.util.Vector;

import com.installshield.event.*;
import com.installshield.event.ui.*;
import com.installshield.wizard.*;
import com.installshield.wizard.service.*;
import com.installshield.util.*;
import com.installshield.ui.controls.*;

public class PanelLicense {

    private static final String ACCEPT_BUTTON_VARIABLE =
        "LICENSE_ACCEPT_BUTTON";
    private static final String REJECT_BUTTON_VARIABLE =
        "LICENSE_REJECT_BUTTON";

    private static final String ACCEPT_BUTTON = "accept";
    private static final String NEXT_BUTTON = "next";

    public void initializeUILicense(ISDialogContext context) {
        ISFrame frame = context.getISFrame();
        ISButton nextButton = frame.getButton(NEXT_BUTTON);
        ISRadioButton accept =
            context.getISPanel().getRadioButton(ACCEPT_BUTTON);
        if ((accept != null) && (accept.isSelected()))
            nextButton.setEnabled(true);
        else
            nextButton.setEnabled(false);
    }

    public void checkedaccept(ISControlContext context) {
        ISPanel panel = (ISPanel)context.getISContainer();
        ISFrame frame = panel.getISFrame();
        ISButton nextButton = frame.getButton(NEXT_BUTTON);
        nextButton.setEnabled(true);
    }

    public void checkedreject(ISControlContext context) {
        ISPanel panel = (ISPanel)context.getISContainer();
        ISFrame frame = panel.getISFrame();
        ISButton nextButton = frame.getButton(NEXT_BUTTON);
        nextButton.setEnabled(false);
    }

    public void generateOptionsEntriesLicense(ISOptionsContext context) {

        String acceptValue = null;
        String rejectValue = null;
        String option = null;
        String panelId = context.getPanel().getName();
        Vector optionEntries = context.getOptionEntries();

        try {

            if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
                acceptValue =
                    rejectValue =
                        LocalizedStringResolver.resolve(
                            "com.installshield.wizard.i18n.WizardResources",
                            "WizardBean.valueStr");
            }
            else {
                acceptValue =
                    context
                        .getWizard()
                        .getServices()
                        .getISDatabase()
                        .getVariableValue(
                        ACCEPT_BUTTON_VARIABLE);
                rejectValue =
                    context
                        .getWizard()
                        .getServices()
                        .getISDatabase()
                        .getVariableValue(
                        REJECT_BUTTON_VARIABLE);
            }

            String doc =
                "The initial state of the License panel.  The accept and reject option states are stored as Variables and must be set with -V";

            option =
                "-V " + ACCEPT_BUTTON_VARIABLE + "=\"" + acceptValue + "\"";
            optionEntries.addElement(
                new OptionsTemplateEntry(
                    "Custom Dialog: " + panelId,
                    doc,
                    option));

            option =
                "-V " + REJECT_BUTTON_VARIABLE + "=\"" + rejectValue + "\"";
            optionEntries.addElement(
                new OptionsTemplateEntry(
                    "Custom Dialog: " + panelId,
                    doc,
                    option));

        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

}
