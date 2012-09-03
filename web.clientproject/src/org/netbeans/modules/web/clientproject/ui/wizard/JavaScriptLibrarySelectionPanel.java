/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ui.wizard;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class JavaScriptLibrarySelectionPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private final Object javaScriptLibrarySelectionLock = new Object();

    // @GuardedBy("javaScriptLibrarySelectionLock")
    private JavaScriptLibrarySelection javaScriptLibrarySelection;
    private volatile WizardDescriptor wizardDescriptor;


    @NbBundle.Messages("JavaScriptLibrarySelectionPanel.jsLibs.info=Libraries added by your template are already selected.")
    @Override
    public JavaScriptLibrarySelection getComponent() {
        synchronized (javaScriptLibrarySelectionLock) {
            if (javaScriptLibrarySelection == null) {
                javaScriptLibrarySelection = new JavaScriptLibrarySelection();
                javaScriptLibrarySelection.setAdditionalInfo(Bundle.JavaScriptLibrarySelectionPanel_jsLibs_info());
            }
            return javaScriptLibrarySelection;
        }
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.wizard.JavaScriptLibrarySelectionPanel"); // NOI18N
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        SiteTemplateImplementation siteTemplate = (SiteTemplateImplementation) wizardDescriptor.getProperty(ClientSideProjectWizardIterator.NewProjectWizard.SITE_TEMPLATE);
        synchronized (javaScriptLibrarySelectionLock) {
            getComponent().updateDefaults(siteTemplate.supportedLibraries());
        }
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        synchronized (javaScriptLibrarySelectionLock) {
            wizardDescriptor.putProperty(ClientSideProjectWizardIterator.NewProjectWizard.LIBRARIES_FOLDER, getComponent().getLibrariesFolder());
            wizardDescriptor.putProperty(ClientSideProjectWizardIterator.NewProjectWizard.SELECTED_LIBRARIES, getComponent().getSelectedLibraries());
        }
    }

    @Override
    public boolean isValid() {
        // error
        String error;
        synchronized (javaScriptLibrarySelectionLock) {
            error = getComponent().getErrorMessage();
        }
        if (error != null && !error.isEmpty()) {
            setErrorMessage(error);
            return false;
        }
        // warning
        String warning;
        synchronized (javaScriptLibrarySelectionLock) {
            warning = getComponent().getWarningMessage();
        }
        if (warning != null && !warning.isEmpty()) {
            setErrorMessage(warning);
            return true;
        }
        // everything ok
        setErrorMessage(""); // NOI18N
        return true;
    }

    private void setErrorMessage(String message) {
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        synchronized (javaScriptLibrarySelectionLock) {
            getComponent().addChangeListener(listener);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        synchronized (javaScriptLibrarySelectionLock) {
            getComponent().removeChangeListener(listener);
        }
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
