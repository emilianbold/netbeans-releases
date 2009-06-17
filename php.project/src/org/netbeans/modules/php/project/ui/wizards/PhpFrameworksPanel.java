/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * @author Tomas Mysik
 */
public class PhpFrameworksPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {

    static final String VALID = "PhpFrameworksPanel.valid"; // NOI18N // used in the previous steps while validating
    static final String FRAMEWORKS = "frameworks";

    private final String[] steps;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private PhpFrameworksPanelVisual frameworksPanel = null;
    private WizardDescriptor descriptor = null;

    public PhpFrameworksPanel(String[] steps) {
        this.steps = steps;
    }

    String[] getSteps() {
        return steps;
    }

    public Component getComponent() {
        if (frameworksPanel == null) {
            frameworksPanel = new PhpFrameworksPanelVisual(this);
        }
        return frameworksPanel;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(PhpFrameworksPanel.class);
    }

    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;

        frameworksPanel.addPhpFrameworksListener(this);
    }

    public void storeSettings(WizardDescriptor settings) {
        getComponent();
        frameworksPanel.removePhpFrameworksListener(this);

        descriptor.putProperty(FRAMEWORKS, frameworksPanel.getSelectedFrameworks());
    }

    public boolean isValid() {
        getComponent();
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N

        // validate frameworks?
        //descriptor.putProperty(VALID, false);

        descriptor.putProperty(VALID, true);
        return true;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public boolean isFinishPanel() {
        return true;
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }
}
