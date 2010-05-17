/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
 */
package org.netbeans.modules.javacard.wizard;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public final class ClassicPackageWizardPanel implements
        WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.ValidatingPanel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>,
        ChangeListener {
    private PackageAIDPanel component;
    private final ChangeSupport supp = new ChangeSupport(this);
    private final ProjectKind kind;
    public ClassicPackageWizardPanel(ProjectKind kind) {
        this.kind = kind;
    }

    public Component getComponent() {
        if (component == null) {
            component = new PackageAIDPanel();
            component.setName(NbBundle.getMessage(
                    ProjectDefinitionWizardPanel.class,
                    "LBL_PkgAidStep")); //NOI18N
            String stepName = NbBundle.getMessage(ProjectDefinitionWizardPanel.class,
                "WIZARD_STEP_ENTER_PKG_AID"); //NOI18N
            component.addChangeListener(this);
            // Sets step number of a component
            component.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(1)); //NOI18N
            // Sets steps names for a panel
            if (kind.isClassic()) {
                String prevStepName = NbBundle.getMessage(ProjectDefinitionWizardPanel.class, "WIZARD_STEP_CREATE_PROJECT"); //NOI18N
                component.putClientProperty("WizardPanel_contentData", new String[] { prevStepName, stepName}); //NOI18N
            } else {
                component.putClientProperty("WizardPanel_contentData", new String[] { stepName }); //NOI18N
            }
            // Turn off subtitle creation on each step
            component.putClientProperty("WizardPanel_autoWizardStyle", Boolean.FALSE); //NOI18N
            // Show steps on the left side with the image on the background
            component.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); //NOI18N
            // Turn on numbering of all steps
            component.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); //NOI18N

            component.addChangeListener(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void readSettings(WizardDescriptor settings) {
        ((PackageAIDPanel)getComponent()).read(settings);
    }

    public void storeSettings(WizardDescriptor settings) {
        ((PackageAIDPanel)getComponent()).write(settings);
    }

    public boolean isValid() {
        return component != null && !component.isProblem();
    }

    public void addChangeListener(ChangeListener l) {
        supp.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        supp.removeChangeListener(l);
    }

    public void validate() throws WizardValidationException {
        ((PackageAIDPanel)getComponent()).isProblem();
    }

    public boolean isFinishPanel() {
        return true;
    }

    public void stateChanged(ChangeEvent e) {
        supp.fireChange();
    }

}
