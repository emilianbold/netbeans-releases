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

package org.netbeans.modules.kenai.ui;

import java.awt.Component;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.kenai.ui.NewKenaiProjectWizardIterator.SharedItem;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 *
 * @author Milan Kubec
 */
public class SourceAndIssuesWizardPanel implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    private SourceAndIssuesWizardPanelGUI component;
    private WizardDescriptor settings;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final List<SharedItem> initialItems;

    public SourceAndIssuesWizardPanel(List<SharedItem> items) {
        this.initialItems = items;
    }

    List<SharedItem> getInitialItems() {
        return initialItems;
    }

    public Component getComponent() {
        if (component == null) {
            component = new SourceAndIssuesWizardPanelGUI(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(SourceAndIssuesWizardPanel.class.getName());
    }

    public void readSettings(Object settings) {
        this.settings = (WizardDescriptor) settings;
        component.read((WizardDescriptor) settings);
    }

    public void storeSettings(Object settings) {
        component.store((WizardDescriptor) settings);
    }

    public boolean isValid() {
        return component.valid();
    }

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public void validate() throws WizardValidationException {
        component.validateWizard();
    }

    public boolean isFinishPanel() {
        return true;
    }

}
