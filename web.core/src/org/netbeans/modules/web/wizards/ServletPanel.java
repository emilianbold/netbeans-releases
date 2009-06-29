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
 */
package org.netbeans.modules.web.wizards;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

/** Wizard panel that collects data for the Servlet and Filter
 * wizards. 
 *
 * @author Ana von Klopp, Milan Kuchtiak
 */
public class ServletPanel implements WizardDescriptor.FinishablePanel {

    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private transient BaseWizardPanel wizardPanel;
    private transient TemplateWizard wizard;
    /** listener to changes in the wizard */
    private ChangeListener listener;
    private DeployData deployData;
    private transient TargetEvaluator evaluator;

    /** Create the wizard panel descriptor. */
    private ServletPanel(TargetEvaluator evaluator, TemplateWizard wizard,
            boolean first) {
        this.evaluator = evaluator;
        this.wizard = wizard;
        this.deployData = evaluator.getDeployData();
        if (first) {
            this.wizardPanel = new DeployDataPanel(evaluator, wizard);
        } else {
            this.wizardPanel = new DeployDataExtraPanel(evaluator, wizard);
        }
    }

    public boolean isFinishPanel() {
        return true;
    }

    /** Create the wizard panel descriptor. */
    public static ServletPanel createServletPanel(TargetEvaluator evaluator,
            TemplateWizard wizard) {
        return new ServletPanel(evaluator, wizard, true);
    }

    /** Create the wizard panel descriptor. */
    public static ServletPanel createFilterPanel(TargetEvaluator evaluator,
            TemplateWizard wizard) {
        return new ServletPanel(evaluator, wizard, false);
    }

    public Component getComponent() {
        return wizardPanel;
    }

    public boolean isValid() {
        if (deployData.isValid()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
            return true;
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, //NOI18N
                deployData.getErrorMessage());
        return false;
    }

    public HelpCtx getHelp() {
        // #114487
        if (evaluator.getFileType() == FileType.SERVLET) {
            return wizardPanel.getHelp();
        }
        return null;
    }

    /** Add a listener to changes of the panel's validity.
     * @param l the listener to add
     * @see #isValid
     */
    public void addChangeListener(ChangeListener l) {
        if (listener != null) {
            throw new IllegalStateException();
        }
        if (wizardPanel != null) {
            wizardPanel.addChangeListener(l);
        }
        listener = l;
    }

    /** Remove a listener to changes of the panel's validity.
     * @param l the listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        listener = null;
        if (wizardPanel != null) {
            wizardPanel.removeChangeListener(l);
        }
    }

    public void readSettings(Object settings) {
        if (settings instanceof TemplateWizard) {
            TemplateWizard w = (TemplateWizard) settings;
            //Project project = Templates.getProject(w);
            String targetName = w.getTargetName();
            org.openide.filesystems.FileObject targetFolder = Templates.getTargetFolder(w);
            Project project = Templates.getProject(w);
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String packageName = null;
            for (int i = 0; i < groups.length && packageName == null; i++) {
                if (WebModule.getWebModule(groups[i].getRootFolder()) != null) {
                    packageName = org.openide.filesystems.FileUtil.getRelativePath(groups[i].getRootFolder(), targetFolder);
                }
            }
            if (packageName != null) {
                packageName = packageName.replace('/', '.');
            } else {
                packageName = "";
            }
            if (targetName == null) {
                evaluator.setClassName(w.getTemplate().getName(), packageName);
            } else {
                evaluator.setClassName(targetName, packageName);
            }
        }
        wizardPanel.setData();
    }

    public void storeSettings(Object settings) {
        // do nothing
    }
}
