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

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection.SelectedLibrary;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.clientproject.util.FileUtilities;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class JavaScriptLibrarySelectionPanel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor> {

    static final Logger LOGGER = Logger.getLogger(JavaScriptLibrarySelectionPanel.class.getName());

    private final FileObject librariesFolder;

    // @GuardedBy("EDT") - not possible, wizard support calls store() method in EDT as well as in a background thread
    private volatile JavaScriptLibrarySelection javaScriptLibrarySelection;
    private volatile WizardDescriptor wizardDescriptor;
    // #202796
    volatile boolean asynchError = false;


    public JavaScriptLibrarySelectionPanel() {
        librariesFolder = createLibrariesFolder();
    }

    @NbBundle.Messages("JavaScriptLibrarySelectionPanel.jsLibs.info=Libraries added by your template are already selected.")
    @Override
    public JavaScriptLibrarySelection getComponent() {
        if (javaScriptLibrarySelection == null) {
            javaScriptLibrarySelection = new JavaScriptLibrarySelection();
            javaScriptLibrarySelection.setAdditionalInfo(Bundle.JavaScriptLibrarySelectionPanel_jsLibs_info());
            javaScriptLibrarySelection.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    asynchError = false;
                }
            });
        }
        return javaScriptLibrarySelection;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.wizard.JavaScriptLibrarySelectionPanel"); // NOI18N
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        asynchError = false;
        SiteTemplateImplementation siteTemplate = (SiteTemplateImplementation) wizardDescriptor.getProperty(ClientSideProjectWizardIterator.NewProjectWizard.SITE_TEMPLATE);
        getComponent().updateDefaults(siteTemplate.supportedLibraries());
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.NewProjectWizard.LIBRARIES_FOLDER, librariesFolder);
    }

    @Override
    public void prepareValidation() {
        getComponent().lockPanel();
    }

    @NbBundle.Messages({
        "JavaScriptLibrarySelectionPanel.jsLibs.downloading=Downloading selected JavaScript libraries...",
        "# {0} - number of failed libraries",
        "JavaScriptLibrarySelectionPanel.error.downloading=Not all libraries can be downloaded ({0} failed)."
    })
    @Override
    public void validate() throws WizardValidationException {
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(Bundle.JavaScriptLibrarySelectionPanel_jsLibs_downloading());
        progressHandle.start();
        try {
            FileUtilities.cleanupFolder(librariesFolder);
            List<SelectedLibrary> selectedLibraries = getComponent().getSelectedLibraries();
            if (selectedLibraries.isEmpty()) {
                return;
            }
            List<SelectedLibrary> failedLibs = ClientSideProjectUtilities.applyJsLibraries(selectedLibraries, getComponent().getLibrariesFolder(), librariesFolder, progressHandle);
            if (!failedLibs.isEmpty()) {
                LOGGER.log(Level.INFO, "Failed download of JS libraries: {0}", failedLibs);
                getComponent().updateFailed(failedLibs);
                asynchError = true;
                throw new WizardValidationException(getComponent(), "ERROR_DOWNLOAD", Bundle.JavaScriptLibrarySelectionPanel_error_downloading(failedLibs.size())); // NOI18N
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            asynchError = true;
            throw new WizardValidationException(getComponent(), "ERROR_DOWNLOAD", ex.getLocalizedMessage()); // NOI18N
        } finally {
            progressHandle.finish();
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getComponent().unlockPanel();
                }
            });
        }
    }

    @Override
    public boolean isValid() {
        // grrr
        if (asynchError) {
            return true;
        }
        // error
        String error = getComponent().getErrorMessage();
        if (error != null && !error.isEmpty()) {
            setErrorMessage(error);
            return false;
        }
        // warning
        String warning = getComponent().getWarningMessage();
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
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    private FileObject createLibrariesFolder() {
        int i = 0;
        for (;;) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "netbeans-jslibs-" + i++); // NOI18N
            if (!tmpDir.isDirectory() && tmpDir.mkdirs()) {
                FileObject fo = FileUtil.toFileObject(tmpDir);
                if (fo != null && fo.isValid()) {
                    return fo;
                }
            }
        }
    }

}
