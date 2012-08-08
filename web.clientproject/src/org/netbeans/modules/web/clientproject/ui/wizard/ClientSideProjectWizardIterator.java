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

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectUtilities;
import org.netbeans.modules.web.clientproject.api.MissingLibResourceException;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.web.clientproject.libraries.JavaScriptLibraryTypeProvider;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.ui.wizard.JavaScriptLibrarySelection.LibraryVersion;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(folder = "Project/ClientSide", displayName = "#ClientSideProject_displayName",
        description = "ClientSideProjectDescription.html",
        iconBase = ClientSideProject.PROJECT_ICON )
@Messages({"ClientSideProject_displayName=HTML Application",
            "MSG_Progress1=Creating project"})
public class ClientSideProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectWizardIterator.class.getName());

    static final String SITE_TEMPLATE = "SITE_TEMPLATE"; // NOI18N
    static final String LIBRARIES_FOLDER = "LIBRARIES_FOLDER"; // NOI18N
    static final String SELECTED_LIBRARIES = "SELECTED_LIBRARIES"; // NOI18N

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor wiz;

    private SiteTemplateWizardPanel sitesPanel;
    private JavaScriptLibrarySelectionPanel librariesPanel;

    public ClientSideProjectWizardIterator() {
    }

    public static ClientSideProjectWizardIterator createIterator() {
        return new ClientSideProjectWizardIterator();
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        sitesPanel = new SiteTemplateWizardPanel();
        librariesPanel = new JavaScriptLibrarySelectionPanel();
        @SuppressWarnings("unchecked")
        WizardDescriptor.Panel<WizardDescriptor>[] pnls = new WizardDescriptor.Panel[] {
                    new ClientSideProjectWizardPanel(),
                    sitesPanel,
                    librariesPanel,
        };
        return pnls;
    }

    private String[] createSteps() {
        return new String[]{
                    NbBundle.getMessage(ClientSideProjectWizardIterator.class, "LBL_CreateProjectStep"),
                    NbBundle.getMessage(ClientSideProjectWizardIterator.class, "LBL_ChooseSiteStep"),
                    NbBundle.getMessage(ClientSideProjectWizardIterator.class, "LBL_JavaScriptLibrarySelectionStep"),
                };
    }

    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.MSG_Progress1());
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir"));
        String name = (String) wiz.getProperty("name");
        dirF.mkdirs();
        FileObject dir = FileUtil.toFileObject(dirF);
        ClientSideProjectUtilities.setupProject(dir, name);
        // Always open top dir as a project:
        resultSet.add(dir);

        // site template
        SiteTemplateImplementation siteTemplate = (SiteTemplateImplementation) wiz.getProperty(SITE_TEMPLATE);
        if (siteTemplate != null) {
            // any site template selected
            applySiteTemplate(siteTemplate, dir, handle);
        }

        // js libs
        @SuppressWarnings("unchecked")
        List<JavaScriptLibrarySelection.SelectedLibrary> selectedLibraries = (List<JavaScriptLibrarySelection.SelectedLibrary>) wiz.getProperty(SELECTED_LIBRARIES);
        if (selectedLibraries != null) {
            // any libraries selected
            applyJsLibraries(selectedLibraries, (String) wiz.getProperty(LIBRARIES_FOLDER), dir, handle);
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        handle.finish();
        return resultSet;
    }

    @NbBundle.Messages({
        "# {0} - template name",
        "ClientSideProjectWizardIterator.error.applyingSiteTemplate=Cannot apply template \"{0}\"."
    })
    private void applySiteTemplate(SiteTemplateImplementation siteTemplate, final FileObject p, final ProgressHandle handle) {
        assert !EventQueue.isDispatchThread();
        final String templateName = siteTemplate.getName();
        try {
            siteTemplate.apply(p, handle);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            errorOccured(Bundle.ClientSideProjectWizardIterator_error_applyingSiteTemplate(templateName));
        }
    }

    @NbBundle.Messages({
        "ClientSideProjectWizardIterator.error.copyingJsLib=Some of the library files could not be retrieved.",
        "# {0} - library name",
        "ClientSideProjectWizardIterator.msg.downloadingJsLib=Downloading {0}"
    })
    private void applyJsLibraries(List<JavaScriptLibrarySelection.SelectedLibrary> selectedLibraries, String jsLibFolder, FileObject projectDir,
            ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        FileObject librariesRoot = null;
        boolean someFilesAreMissing = false;
        for (JavaScriptLibrarySelection.SelectedLibrary selectedLibrary : selectedLibraries) {
            if (selectedLibrary.isFromTemplate()) {
                // ignore files from site template (they are already applied)
                continue;
            }
            if (librariesRoot == null) {
                librariesRoot = FileUtil.createFolder(projectDir, jsLibFolder);
            }
            LibraryVersion libraryVersion = selectedLibrary.getLibraryVersion();
            Library library = libraryVersion.getLibrary();
            handle.progress(Bundle.ClientSideProjectWizardIterator_msg_downloadingJsLib(library.getProperties().get(JavaScriptLibraryTypeProvider.PROPERTY_REAL_DISPLAY_NAME)));
            try {
                WebClientLibraryManager.addLibraries(new Library[]{library}, librariesRoot, libraryVersion.getType());
            } catch (MissingLibResourceException e) {
                someFilesAreMissing = true;
            }
        }
        if (someFilesAreMissing) {
            errorOccured(Bundle.ClientSideProjectWizardIterator_error_copyingJsLib());
        }
    }

    private void errorOccured(String message) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        throw new UnsupportedOperationException("never implemented - use progress one");
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null);
        this.wiz.putProperty("name", null);
        this.wiz = null;
        panels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[]{new Integer(index + 1), new Integer(panels.length)});
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

}
