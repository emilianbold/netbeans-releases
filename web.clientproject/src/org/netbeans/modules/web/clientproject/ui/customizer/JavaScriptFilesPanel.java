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
package org.netbeans.modules.web.clientproject.ui.customizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
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
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection.SelectedLibrary;
import org.netbeans.modules.web.clientproject.validation.ProjectFoldersValidator;
import org.netbeans.modules.web.clientproject.validation.ValidationResult;
import org.netbeans.modules.web.common.api.Pair;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Manager for project JavaScript files.
 */
public final class JavaScriptFilesPanel extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = 8973245611032L;

    static final String JS_MIME_TYPE = "text/javascript"; // NOI18N

    private final ProjectCustomizer.Category category;
    final ClientSideProjectProperties uiProperties;
    // @GuardedBy("EDT")
    private final JavaScriptLibrarySelection javaScriptLibrarySelection;


    JavaScriptFilesPanel(ProjectCustomizer.Category category, ClientSideProjectProperties uiProperties) {
        assert EventQueue.isDispatchThread();
        assert category != null;
        assert uiProperties != null;

        this.category = category;
        this.uiProperties = uiProperties;
        javaScriptLibrarySelection = new JavaScriptLibrarySelection(new LibraryValidator(uiProperties));

        initComponents();
        init();
    }

    private void init() {
        // add listener
        javaScriptLibrarySelection.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                validateAndStore();
            }
        });
        // add to placeholder
        placeholderPanel.add(javaScriptLibrarySelection, BorderLayout.CENTER);
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
        File siteRootFolder = uiProperties.getResolvedSiteRootFolder();
        ValidationResult result = new ProjectFoldersValidator()
                .validateSiteRootFolder(siteRootFolder)
                .getResult();
        Collection<String> jsFiles;
        if (result.hasErrors()) {
            jsFiles = Collections.<String>emptyList();
        } else {
            jsFiles = findProjectJsFiles(FileUtil.toFileObject(siteRootFolder));
        }
        javaScriptLibrarySelection.updateDefaultLibraries(jsFiles);
    }

    void validateAndStore() {
        if (validateData()) {
            storeData();
        }
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
        assert EventQueue.isDispatchThread();
        uiProperties.setJsLibFolder(javaScriptLibrarySelection.getLibrariesFolder());
        uiProperties.setNewJsLibraries(javaScriptLibrarySelection.getSelectedLibraries());
    }

    @NbBundle.Messages("JavaScriptFilesPanel.progress.detectingJsFiles=Detecting JavaScript files...")
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
        }, Bundle.JavaScriptFilesPanel_progress_detectingJsFiles());
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

    private static final class LibraryValidator implements JavaScriptLibrarySelection.JavaScriptLibrariesValidator {

        private final ClientSideProjectProperties uiProperties;


        private LibraryValidator(ClientSideProjectProperties uiProperties) {
            assert uiProperties != null;
            this.uiProperties = uiProperties;
        }

        @NbBundle.Messages("JavaScriptFilesPanel.error.jsLibsAlreadyExist=Some of the selected libraries already exist.")
        @Override
        public Pair<Set<SelectedLibrary>, String> validate(String librariesFolder, Set<SelectedLibrary> newLibraries) {
            if (newLibraries.isEmpty()) {
                // nothing to validate
                return VALID_RESULT;
            }
            FileObject libsFolder = getLibsFolder(librariesFolder);
            if (libsFolder == null) {
                // non-existing or invalid js libs folder
                return VALID_RESULT;
            }
            Set<SelectedLibrary> existing = new HashSet<SelectedLibrary>();
            for (SelectedLibrary selectedLibrary : newLibraries) {
                for (String filePath : selectedLibrary.getFilePaths()) {
                    if (libsFolder.getFileObject(filePath) != null) {
                        existing.add(selectedLibrary);
                    }
                }
            }
            if (!existing.isEmpty()) {
                // validation failed
                return Pair.of(existing, Bundle.JavaScriptFilesPanel_error_jsLibsAlreadyExist());
            }
            // all ok
            return VALID_RESULT;
        }

        private FileObject getLibsFolder(String librariesFolder) {
            FileObject siteRootFolder = FileUtil.toFileObject(uiProperties.getResolvedSiteRootFolder());
            if (siteRootFolder == null) {
                // non-existing folder
                return null;
            }
            return siteRootFolder.getFileObject(librariesFolder);
        }

    }

}
