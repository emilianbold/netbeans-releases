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
import org.netbeans.modules.javacard.project.deps.DeploymentStrategy;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

final class ChooseDeploymentStrategyWizardPanel implements /* WizardDescriptor.AsynchronousValidatingPanel<Map<String,Object>>, */ WizardDescriptor.FinishablePanel<Map<String,Object>>, ChangeListener {
    private ChooseDeploymentStrategyPanelVisual component;
    private final WizardDescriptor wiz;

    public ChooseDeploymentStrategyWizardPanel (WizardDescriptor wiz) {
        this.wiz = wiz;
        Parameters.notNull("wiz", wiz);
    }

    public Component getComponent() {
        if (component == null) {
            component = new ChooseDeploymentStrategyPanelVisual(wiz);
            component.setDependencyKind(kind);
            component.setDeploymentStrategy(strat);
            component.setSigFile(sigFile);
            component.addChangeListener (WeakListeners.change(this, component));
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        boolean result = strat != null;
        if (!result) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(ChooseDeploymentStrategyWizardPanel.class,
                    "ERR_CHOOSE_DEPLOYMENT")); //NOI18N
        } else {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }
        return result;
    }

    private final ChangeSupport supp = new ChangeSupport(this);
    public final void addChangeListener(ChangeListener l) {
        supp.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        supp.removeChangeListener(l);
    }

    public boolean isFinishPanel() {
        return isValid();
    }

    static final String PROP_DEPLOYMENT_STRATEGY = "_deploymentStrategy"; //NOI18N
    private DependencyKind kind;
    private DeploymentStrategy strat;
    private File sigFile;
    private Map<String, Object> settings;
    public void readSettings(Map<String, Object> settings) {
        this.settings = settings;
        kind = (DependencyKind) settings.get(ChooseOriginWizardPanel.PROP_ACTUAL_DEP_KIND);
        strat = (DeploymentStrategy) settings.get(PROP_DEPLOYMENT_STRATEGY);
        sigFile = (File) settings.get(ChooseOriginWizardPanel.PROP_SIG_FILE);
        if (component != null) {
            component.setDependencyKind(kind);
            component.setDeploymentStrategy(strat);
            component.setSigFile (sigFile);
        }
    }

    public void storeSettings(Map<String, Object> settings) {
        if (strat != null) {
            settings.put (PROP_DEPLOYMENT_STRATEGY, strat);
            if (component != null) {
                File f = component.getSignatureFile();
                if (f != null) {
                    settings.put(ChooseOriginWizardPanel.PROP_SIG_FILE, f);
                } else {
                    settings.remove (ChooseOriginWizardPanel.PROP_SIG_FILE);
                }
            }
        }
    }

    public void stateChanged(ChangeEvent e) {
        strat = component.getDeploymentStrategy();
        supp.fireChange();
    }

//    @Override
//    public void prepareValidation() {
//        //do nothing
//    }
//
//    @Override
//    public void validate() throws WizardValidationException {
//        if (DeploymentStrategy.INCLUDE_IN_PROJECT_CLASSES.equals(strat) && !kind.isOriginAFolder()) {
//            File origin = (File) settings.get(ChooseOriginWizardPanel.PROP_ORIGIN_FILE);
//            try {
//                JarFile jf = new JarFile(origin);
//                try {
//                    for (JarEntry je : NbCollections.iterable(jf.entries())) {
//                        String name = je.getName();
//                        if (name.endsWith(".class")) {
//
//                        }
//                    }
//                } finally {
//                    jf.close();
//                }
//            } catch (IOException ioe) {
//                Logger.getLogger (ChooseDeploymentStrategyWizardPanel.class.getName()).log(Level.INFO, null, ioe);
//                throw new WizardValidationException (component, ioe.getLocalizedMessage(), ioe.getLocalizedMessage());
//            }
//        }
//    }


}

