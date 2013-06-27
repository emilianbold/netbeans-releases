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
package org.netbeans.modules.web.clientproject.api.jslibs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.web.clientproject.api.util.JsLibUtilities;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.clientproject.api.validation.FolderValidator;
import org.netbeans.modules.web.clientproject.api.validation.ValidationResult;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Manager for project JS library files.
 * <p>
 * This panel is ready to be used as a component by {@link ProjectCustomizer.CompositeCategoryProvider}.
 * @see #getCategoryDisplayName()
 * @see ProjectCustomizer.Category#create(String, String, java.awt.Image, ProjectCustomizer.Category)
 * @since 1.20
 */
final class JavaScriptLibraryCustomizerPanel extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = 8973245611032L;

    static final Logger LOGGER = Logger.getLogger(JavaScriptLibraryCustomizerPanel.class.getName());

    static final String JS_MIME_TYPE = "text/javascript"; // NOI18N

    private static final RequestProcessor RP = new RequestProcessor(JavaScriptLibraryCustomizerPanel.class);

    private final ProjectCustomizer.Category category;
    private final Project project;
    final JavaScriptLibraries.CustomizerSupport customizerSupport;
    // @GuardedBy("EDT")
    private final JavaScriptLibrarySelectionPanel javaScriptLibrarySelection;

    private Lookup context;

    /**
     * Create new manager for project JS library files.
     * @param category customizer category
     * @param customizerSupport customizer support
     */
    public JavaScriptLibraryCustomizerPanel(@NonNull ProjectCustomizer.Category category, @NonNull JavaScriptLibraries.CustomizerSupport customizerSupport,
            @NonNull Lookup context) {
        Parameters.notNull("category", category);
        Parameters.notNull("customizerSupport", customizerSupport);
        Parameters.notNull("context", context);
        checkUiThread();

        this.category = category;
        this.customizerSupport = customizerSupport;
        this.context = context;
        project = context.lookup(Project.class);
        assert project != null : "No project found in lookup: " + context;
        javaScriptLibrarySelection = new JavaScriptLibrarySelectionPanel(new LibraryValidator(customizerSupport, context));

        initComponents();
        init();
    }

    /**
     * Get display name of customizer category. It can be used
     * when {@link ProjectCustomizer.Category#create(String, String, java.awt.Image, ProjectCustomizer.Category) creating} a new category.
     * @return display name of customizer category
     */
    @NbBundle.Messages("JavaScriptLibraryCustomizerPanel.category.displayName=JavaScript Files")
    public static String getCategoryDisplayName() {
        return Bundle.JavaScriptLibraryCustomizerPanel_category_displayName();
    }

    private void checkUiThread() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Must be run in UI thread");
        }
    }

    private void init() {
        // add listener
        javaScriptLibrarySelection.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                validateAndSetData();
            }
        });
        // set initial data
        String storedJsLibsFolder = JavaScriptLibraries.getJsLibFolder(project);
        if (storedJsLibsFolder != null) {
            javaScriptLibrarySelection.setLibrariesFolder(storedJsLibsFolder);
        }
        // set store listener
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                storeData();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        initPanel();
        validateData();
    }

    private void initPanel() {
        initPanelContent(loadingLabel);
        File webRoot = getValidWebRoot();
        setBrowseButtonVisible(webRoot);
        setJsFiles(webRoot);
    }

    private void initPanelContent(Component component) {
        assert EventQueue.isDispatchThread();
        placeholderPanel.removeAll();
        placeholderPanel.add(component, BorderLayout.CENTER);
        placeholderPanel.revalidate();
        placeholderPanel.repaint();
    }

    @CheckForNull
    private File getValidWebRoot() {
        File webRoot = customizerSupport.getWebRoot(context);
        ValidationResult result = validateWebRoot(webRoot);
        if (result.hasErrors()) {
            return null;
        }
        return webRoot;
    }

    private void setBrowseButtonVisible(File webRoot) {
        assert EventQueue.isDispatchThread();
        javaScriptLibrarySelection.setBrowseButtonVisible(webRoot);
    }

    private void setJsFiles(final File webRoot) {
        assert EventQueue.isDispatchThread();
        RP.post(new Runnable() {
            @Override
            public void run() {
                // set js files
                final Collection<String> jsFiles;
                if (webRoot == null) {
                    jsFiles = Collections.<String>emptyList();
                } else {
                    jsFiles = Collections.synchronizedCollection(findProjectJsFiles(FileUtil.toFileObject(webRoot)));
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        javaScriptLibrarySelection.updateDefaultLibraries(jsFiles);
                        initPanelContent(javaScriptLibrarySelection);
                    }
                });
            }
        });
    }

    void validateAndSetData() {
        if (validateData()) {
            customizerSupport.setLibrariesFolder(context, javaScriptLibrarySelection.getLibrariesFolder());
            customizerSupport.setSelectedLibraries(context, javaScriptLibrarySelection.getSelectedLibraries());
        }
    }

    @NbBundle.Messages("JavaScriptLibraryCustomizerPanel.error.webRoot.invalid=Invalid web/site root provided.")
    boolean validateData() {
        assert EventQueue.isDispatchThread();
        ValidationResult result = validateWebRoot(customizerSupport.getWebRoot(context));
        if (result.hasErrors()) {
            category.setErrorMessage(Bundle.JavaScriptLibraryCustomizerPanel_error_webRoot_invalid());
            category.setValid(false);
            return false;
        }
        String errorMessage = javaScriptLibrarySelection.getErrorMessage();
        if (errorMessage != null) {
            category.setErrorMessage(errorMessage);
            category.setValid(false);
            return false;
        }
        String warningMessage = javaScriptLibrarySelection.getWarningMessage();
        if (warningMessage != null) {
            category.setErrorMessage(warningMessage);
            category.setValid(true);
            return true;
        }
        // ok
        category.setErrorMessage(null);
        category.setValid(true);
        return true;
    }

    private ValidationResult validateWebRoot(File webRoot) {
        return new FolderValidator()
                .validateFolder(webRoot)
                .getResult();
    }

    void storeData() {
        assert !EventQueue.isDispatchThread();
        final String librariesFolder = javaScriptLibrarySelection.getLibrariesFolder();
        JavaScriptLibraries.setJsLibFolder(project, librariesFolder);
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    addNewJsLibraries(librariesFolder, javaScriptLibrarySelection.getSelectedLibraries());
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        });
    }

    @NbBundle.Messages({
        "JavaScriptLibraryCustomizerPanel.jsLibs.downloading=Downloading selected JavaScript libraries...",
        "# {0} - names of JS libraries",
        "JavaScriptLibraryCustomizerPanel.error.jsLibs=<html><b>These JavaScript libraries failed to download:</b><br><br>{0}<br><br>"
            + "<i>More information can be found in IDE log.</i>"
    })
    void addNewJsLibraries(String jsLibFolder, List<JavaScriptLibrarySelectionPanel.SelectedLibrary> newJsLibraries) throws IOException {
        if (newJsLibraries.isEmpty()) {
            return;
        }
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(Bundle.JavaScriptLibraryCustomizerPanel_jsLibs_downloading());
        progressHandle.start();
        try {
            File webRoot = customizerSupport.getWebRoot(context);
            assert webRoot != null;
            List<JavaScriptLibrarySelectionPanel.SelectedLibrary> failedLibs = JsLibUtilities.applyJsLibraries(newJsLibraries, jsLibFolder,
                    FileUtil.toFileObject(webRoot), progressHandle);
            if (!failedLibs.isEmpty()) {
                LOGGER.log(Level.INFO, "Failed download of JS libraries: {0}", failedLibs);
                errorOccured(Bundle.JavaScriptLibraryCustomizerPanel_error_jsLibs(StringUtilities.implode(getLibraryNames(failedLibs), "<br>"))); // NOI18N
            }
        } finally {
            progressHandle.finish();
        }
    }

    @NbBundle.Messages("JavaScriptLibraryCustomizerPanel.errorDialog.configureProxy=Configure Proxy...")
    private static void errorOccured(String message) {
        NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        JButton configureProxyButton = new JButton(Bundle.JavaScriptLibraryCustomizerPanel_errorDialog_configureProxy());
        configureProxyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDisplayer.getDefault().open(OptionsDisplayer.GENERAL);
            }
        });
        descriptor.setAdditionalOptions(new Object[] {configureProxyButton});
        DialogDisplayer.getDefault().notifyLater(descriptor);
    }

    private List<String> getLibraryNames(List<JavaScriptLibrarySelectionPanel.SelectedLibrary> libraries) {
        List<String> names = new ArrayList<String>(libraries.size());
        for (JavaScriptLibrarySelectionPanel.SelectedLibrary selectedLibrary : libraries) {
            JavaScriptLibrarySelectionPanel.LibraryVersion libraryVersion = selectedLibrary.getLibraryVersion();
            Library library = libraryVersion.getLibrary();
            assert library != null : "Library must be found for " + libraryVersion;
            String name = library.getProperties().get(WebClientLibraryManager.PROPERTY_REAL_DISPLAY_NAME);
            names.add(name);
        }
        return names;
    }

    private Collection<String> findProjectJsFiles(final FileObject siteRoot) {
        assert !EventQueue.isDispatchThread();
        final Set<String> jsFiles = new TreeSet<>();
        Enumeration<? extends FileObject> children = siteRoot.getChildren(true);
        while (children.hasMoreElements()) {
            FileObject child = children.nextElement();
            if (JS_MIME_TYPE.equals(FileUtil.getMIMEType(child, JS_MIME_TYPE))) {
                jsFiles.add(FileUtil.getRelativePath(siteRoot, child));
            }
        }
        return jsFiles;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.customizer.JavaScriptFilesPanel"); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        placeholderPanel = new JPanel();
        loadingLabel = new JLabel();

        placeholderPanel.setLayout(new BorderLayout());

        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Mnemonics.setLocalizedText(loadingLabel, NbBundle.getMessage(JavaScriptLibraryCustomizerPanel.class, "JavaScriptLibraryCustomizerPanel.loadingLabel.text")); // NOI18N
        placeholderPanel.add(loadingLabel, BorderLayout.CENTER);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(placeholderPanel, GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(placeholderPanel, GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel loadingLabel;
    private JPanel placeholderPanel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class LibraryValidator implements JavaScriptLibrarySelectionPanel.JavaScriptLibrariesValidator {

        private final JavaScriptLibraries.CustomizerSupport customizerSupport;
        private Lookup context;

        private LibraryValidator(JavaScriptLibraries.CustomizerSupport customizerSupport, Lookup context) {
            assert customizerSupport != null;
            this.customizerSupport = customizerSupport;
            this.context = context;
        }

        @NbBundle.Messages("JavaScriptLibraryCustomizerPanel.error.jsLibsAlreadyExist=Some of the selected libraries already exist.")
        @Override
        public Pair<Set<JavaScriptLibrarySelectionPanel.SelectedLibrary>, String> validate(String librariesFolder, Set<JavaScriptLibrarySelectionPanel.SelectedLibrary> newLibraries) {
            if (newLibraries.isEmpty()) {
                // nothing to validate
                return VALID_RESULT;
            }
            FileObject libsFolder = getLibsFolder(librariesFolder);
            if (libsFolder == null) {
                // non-existing or invalid js libs folder
                return VALID_RESULT;
            }
            Set<JavaScriptLibrarySelectionPanel.SelectedLibrary> existing = new HashSet<JavaScriptLibrarySelectionPanel.SelectedLibrary>();
            for (JavaScriptLibrarySelectionPanel.SelectedLibrary selectedLibrary : newLibraries) {
                for (String filePath : selectedLibrary.getFilePaths()) {
                    if (libsFolder.getFileObject(filePath) != null) {
                        existing.add(selectedLibrary);
                    }
                }
            }
            if (!existing.isEmpty()) {
                // validation failed
                return Pair.of(existing, Bundle.JavaScriptLibraryCustomizerPanel_error_jsLibsAlreadyExist());
            }
            // all ok
            return VALID_RESULT;
        }

        private FileObject getLibsFolder(String librariesFolder) {
            File webRoot = customizerSupport.getWebRoot(context);
            if (webRoot == null) {
                // no folder
                return null;
            }
            FileObject webRootFo = FileUtil.toFileObject(webRoot);
            if (webRootFo == null) {
                // non-existing folder
                return null;
            }
            return webRootFo.getFileObject(librariesFolder);
        }

    }

}
