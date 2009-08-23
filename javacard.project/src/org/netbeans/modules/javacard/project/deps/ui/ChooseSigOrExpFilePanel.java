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
package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.Component;
import java.io.File;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javacard.project.deps.DependencyKind;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tim Boudreau
 */
final class ChooseSigOrExpFilePanel implements WizardDescriptor.Panel<Map<String, Object>>, ChangeListener {
    private final WizardDescriptor wiz;
    ChooseSigOrExpFilePanelVisual component;
    private final ChangeSupport supp = new ChangeSupport(this);
    ChooseSigOrExpFilePanel(WizardDescriptor wiz) {
        this.wiz = wiz;
    }

    public Component getComponent() {
        if (component == null) {
            component = new ChooseSigOrExpFilePanelVisual(wiz);
            component.addChangeListener(WeakListeners.change(this, component));
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void readSettings(Map<String, Object> settings) {
        getComponent();
        IntermediatePanelKind kind = (IntermediatePanelKind) settings.get(ChooseOriginWizardPanel.PROP_INTERMEDIATE_PANEL_KIND);
        if (kind != null) {
            component.setKind(kind);
            File f = (File) settings.get(kind == IntermediatePanelKind.EXP_FILE ?
                ChooseOriginWizardPanel.PROP_EXP_FILE : ChooseOriginWizardPanel.PROP_SIG_FILE);
            component.setFile(f);
        }
    }

    public void storeSettings(Map<String, Object> settings) {
        IntermediatePanelKind kind = (IntermediatePanelKind) settings.get(ChooseOriginWizardPanel.PROP_INTERMEDIATE_PANEL_KIND);
        if (kind != null) {
            File f = component.getFile();
            switch (kind) {
                case EXP_FILE :
                    if (f != null) {
                        settings.put (ChooseOriginWizardPanel.PROP_EXP_FILE, f);
                        settings.put (ChooseOriginWizardPanel.PROP_ACTUAL_DEP_KIND, DependencyKind.JAR_WITH_EXP_FILE);
                    } else {
                        settings.remove (ChooseOriginWizardPanel.PROP_EXP_FILE);
                        settings.put (ChooseOriginWizardPanel.PROP_ACTUAL_DEP_KIND, DependencyKind.RAW_JAR);
                    }
                    break;
                case SIG_FILE :
                    settings.put (ChooseOriginWizardPanel.PROP_SIG_FILE, f);
                    break;
                default :
                    throw new AssertionError();
            }
        } else {
            settings.remove (ChooseOriginWizardPanel.PROP_EXP_FILE);
            settings.remove (ChooseOriginWizardPanel.PROP_SIG_FILE);
        }
    }

    public boolean isValid() {
        return component == null ? false : component.valid();
    }

    public void addChangeListener(ChangeListener l) {
        supp.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        supp.removeChangeListener(l);
    }

    public void stateChanged(ChangeEvent e) {
        supp.fireChange();
    }
}
