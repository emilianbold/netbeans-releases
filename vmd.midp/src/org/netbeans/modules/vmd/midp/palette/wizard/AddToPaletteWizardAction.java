/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */package org.netbeans.modules.vmd.midp.palette.wizard;
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


    public AddToPaletteWizardAction () {
        putValue (Action.NAME, NbBundle.getMessage (AddToPaletteWizardAction.class, "NAME_AddToPaletteWizard")); // NOI18N
        putValue (Action.LONG_DESCRIPTION, NbBundle.getMessage (AddToPaletteWizardAction.class, "DESC_AddToPaletteWizard")); // NOI18N
    }

    public void performAction() {
        DesignDocument document = ActiveDocumentSupport.getDefault ().getActiveDocument ();
        if (document == null) {
            return;
        }
        
        if (! MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (document.getDocumentInterface ().getProjectType ()))
            return;

        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(NbBundle.getMessage (AddToPaletteWizardAction.class, "TITLE_AddToPaletteWizard")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage (AddToPaletteWizardAction.class, "TITLE_AddToPaletteWizard")); //NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (AddToPaletteWizardAction.class, "TITLE_AddToPaletteWizard")); //NOI18N
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
        return new HelpCtx (AddToPaletteWizardAction.class);
    }

    protected boolean asynchronous() {
        return false;
    }

}

