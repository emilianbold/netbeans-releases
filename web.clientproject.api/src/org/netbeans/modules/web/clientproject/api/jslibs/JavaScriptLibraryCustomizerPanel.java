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
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.web.clientproject.api.validation.FolderValidator;
import org.netbeans.modules.web.clientproject.api.validation.ValidationResult;
import org.netbeans.modules.web.common.api.Pair;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Manager for project JS library files.
 * @since 1.20
 */
public final class JavaScriptLibraryCustomizerPanel extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = 8973245611032L;

    static final String JS_MIME_TYPE = "text/javascript"; // NOI18N

    private final ProjectCustomizer.Category category;
    final LibrariesFolderRootProvider librariesFolderRootProvider;
    // @GuardedBy("EDT")
    private final JavaScriptLibrarySelectionPanel javaScriptLibrarySelection;


    public JavaScriptLibraryCustomizerPanel(@NonNull ProjectCustomizer.Category category, @NonNull LibrariesFolderRootProvider librariesFolderRootProvider) {
        Parameters.notNull("category", category);
        Parameters.notNull("librariesFolderRootProvider", librariesFolderRootProvider);
        checkUiThread();

        this.category = category;
        this.librariesFolderRootProvider = librariesFolderRootProvider;
        javaScriptLibrarySelection = new JavaScriptLibrarySelectionPanel(new LibraryValidator(librariesFolderRootProvider));

        initComponents();
        init();
    }

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
                validateData();
            }
        });
        // add to placeholder
        placeholderPanel.add(javaScriptLibrarySelection, BorderLayout.CENTER);
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
        setJsFiles();
        validateData();
    }

    private void setJsFiles() {
        assert EventQueue.isDispatchThread();
        // set js files
        File siteRootFolder = librariesFolderRootProvider.getLibrariesFolderRoot();
        ValidationResult result = new FolderValidator()
                .validateFolder(siteRootFolder)
                .getResult();
        Collection<String> jsFiles;
        if (result.hasErrors()) {
            jsFiles = Collections.<String>emptyList();
        } else {
            jsFiles = findProjectJsFiles(FileUtil.toFileObject(siteRootFolder));
        }
        javaScriptLibrarySelection.updateDefaultLibraries(jsFiles);
    }

    boolean validateData() {
        assert EventQueue.isDispatchThread();
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

    private void storeData() {
        assert !EventQueue.isDispatchThread();
        // XXX
//        uiProperties.setJsLibFolder(javaScriptLibrarySelection.getLibrariesFolder());
//        uiProperties.setNewJsLibraries(javaScriptLibrarySelection.getSelectedLibraries());
    }

    @NbBundle.Messages("JavaScriptLibraryCustomizerPanel.progress.detectingJsFiles=Detecting JavaScript files...")
    private Collection<String> findProjectJsFiles(final FileObject siteRoot) {
        final Set<String> jsFiles = Collections.synchronizedSortedSet(new TreeSet<String>());
        ProgressUtils.showProgressDialogAndRun(new Runnable() {
            @Override
            public void run() {
                Enumeration<? extends FileObject> children = siteRoot.getChildren(true);
                while (children.hasMoreElements()) {
                    FileObject child = children.nextElement();
                    if (JS_MIME_TYPE.equals(FileUtil.getMIMEType(child, JS_MIME_TYPE))) {
                        jsFiles.add(FileUtil.getRelativePath(siteRoot, child));
                    }
                }
            }
        }, Bundle.JavaScriptLibraryCustomizerPanel_progress_detectingJsFiles());
        return jsFiles;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.customizer.JavaScriptFilesPanel");
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        placeholderPanel = new JPanel();

        placeholderPanel.setLayout(new BorderLayout());

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
    private JPanel placeholderPanel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    /**
     * Provider for root (parent folder) of {@link JavaScriptLibrarySelectionPanel#getLibrariesFolder() libraries folder}.
     * <p>
     * Implementations must be thread-safe.
     */
    public interface LibrariesFolderRootProvider {

        /**
         * Get root (parent folder) of {@link JavaScriptLibrarySelectionPanel#getLibrariesFolder() libraries folder}.
         * The root is typically web root folder or site root folder.
         * The root is also searched for existing JS libraries/files.
         * @return root (parent folder) of {@link JavaScriptLibrarySelectionPanel#getLibrariesFolder() libraries folder}, can be {@code null}
         */
        @CheckForNull
        File getLibrariesFolderRoot();
    }

    private static final class LibraryValidator implements JavaScriptLibrarySelectionPanel.JavaScriptLibrariesValidator {

        private final LibrariesFolderRootProvider librariesFolderRootProvider;


        private LibraryValidator(LibrariesFolderRootProvider librariesFolderRootProvider) {
            assert librariesFolderRootProvider != null;
            this.librariesFolderRootProvider = librariesFolderRootProvider;
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
            File librariesFolderRoot = librariesFolderRootProvider.getLibrariesFolderRoot();
            if (librariesFolderRoot == null) {
                // no folder
                return null;
            }
            FileObject siteRootFolder = FileUtil.toFileObject(librariesFolderRoot);
            if (siteRootFolder == null) {
                // non-existing folder
                return null;
            }
            return siteRootFolder.getFileObject(librariesFolder);
        }

    }

}
