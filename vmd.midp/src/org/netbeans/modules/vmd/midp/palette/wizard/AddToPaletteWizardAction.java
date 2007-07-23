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
 *
 */
package org.netbeans.modules.vmd.midp.palette.wizard;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.Map;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class AddToPaletteWizardAction extends CallableSystemAction {

    static final String PROPERTY_PROJECT = "project"; // NOI18N
    static final String PROPERTY_ITEMS = "items"; // NOI18N
    static final String PROPERTY_TO_INSTALL = "toInstall"; // NOI18N

    private WizardDescriptor.Panel[] panels;

    public void performAction() {
        DesignDocument document = ActiveDocumentSupport.getDefault ().getActiveDocument ();
        if (! MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (document.getDocumentInterface ().getProjectType ()))
            return;

        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(NbBundle.getMessage (AddToPaletteWizardAction.class, "TITLE_AddToPaletteWizard")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;

        if (!cancelled) {
            ComponentInstaller.install (
                (Map<String, ComponentInstaller.Item>) wizardDescriptor.getProperty (PROPERTY_ITEMS),
                (List<ComponentInstaller.Item>) wizardDescriptor.getProperty (PROPERTY_TO_INSTALL)
            );
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new AddToPaletteWizardPanel1(),
                new AddToPaletteWizardPanel2()
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
                }
            }
        }
        return panels;
    }

    public String getName() {
        return NbBundle.getMessage (AddToPaletteWizardAction.class, "DISP_AddToPaletteWizard"); // NOI18N
    }

    public String iconResource() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

}

