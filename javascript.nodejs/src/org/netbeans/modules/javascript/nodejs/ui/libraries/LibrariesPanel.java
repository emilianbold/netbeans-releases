/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.nodejs.ui.libraries;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for customization of npm dependencies/library.
 *
 * @author Jan Stola
 */
public class LibrariesPanel extends javax.swing.JPanel {
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(LibrariesPanel.class);
    /** Project whose npm libraries are being customized. */
    private final Project project;
    /** Installed npm libraries (maps the name to the installed version). */
    private Map<String,String> installedLibraries;

    /**
     * Creates a new {@code LibrariesPanel}.
     * 
     * @param project project whose libraries should be customized.
     */
    public LibrariesPanel(Project project) {
        this.project = project;
        initComponents();        
        PackageJson packagejson = getPackageJson();
        if (packagejson.exists()) {
            PackageJson.NpmDependencies dependencies = packagejson.getDependencies();
            regularPanel.setProject(project);
            developmentPanel.setProject(project);
            optionalPanel.setProject(project);
            regularPanel.setDependencies(new HashMap<>(dependencies.dependencies));
            developmentPanel.setDependencies(new HashMap<>(dependencies.devDependencies));
            optionalPanel.setDependencies(new HashMap<>(dependencies.optionalDependencies));
            loadInstalledLibraries();
        } else {
            show(packageJsonProblemLabel);
        }
    }

    /**
     * Shows the given component in the main area of the customizer.
     * 
     * @param component component to show.
     */
    private void show(Component component) {
        assert EventQueue.isDispatchThread();
        GroupLayout layout = (GroupLayout)getLayout();
        Component currentComponent = getComponent(0);
        layout.replace(currentComponent, component);
    }

    /**
     * Creates a store listener (the listener that is invoked when
     * the changes in the project customizer are confirmed).
     * 
     * @return store listener.
     */
    ActionListener createStoreListener() {
        return new StoreListener();
    }

    /**
     * Returns {@code package.json} for the project.
     * 
     * @return {@code package.json} for the project.
     */
    private PackageJson getPackageJson() {
        NodeJsSupport nodeJsSupport = project.getLookup().lookup(NodeJsSupport.class);
        if (nodeJsSupport != null) {
            return nodeJsSupport.getPackageJson();
        }
        return new PackageJson(project.getProjectDirectory());
    }

    /**
     * Converts the library-to-version map to the list of {@code Library.Version}s.
     * 
     * @param map maps library name to library version.
     * @return list of {@code Library.Version}s that corresponds to the given map.
     */
    static List<Library.Version> toLibraries(Map<String,String> map) {
        List<Library.Version> libraries = new ArrayList<>(map.size());
        for (Map.Entry<String,String> entry : map.entrySet()) {
            Library library = new Library(entry.getKey());
            Library.Version version = new Library.Version(library, entry.getValue());
            libraries.add(version);
        }
        return libraries;
    }

    /**
     * Loads the libraries installed in the project. Updates
     * the view once the installed libraries are determined.
     */
    private void loadInstalledLibraries() {
        show(loadingLabel);
        RP.post(new Runnable() {
            @Override
            public void run() {
                LibraryProvider provider = LibraryProvider.forProject(project);
                installedLibraries = provider.installedLibraries();

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (installedLibraries == null) {
                            show(npmProblemPanel);
                        } else {
                            regularPanel.setInstalledLibraries(installedLibraries);
                            developmentPanel.setInstalledLibraries(installedLibraries);
                            optionalPanel.setInstalledLibraries(installedLibraries);
                            show(tabbedPane);
                        }
                    }
                });
            }
        });
    }

    /** Progress handle used when storing changes. */
    private ProgressHandle progressHandle;

    /**
     * Performs/stores the changes requested by the user in the customizer.
     */
    @NbBundle.Messages("LibrariesPanel.updatingPackages=Updating npm packages...")
    void storeChanges() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                progressHandle = ProgressHandle.createHandle(Bundle.LibrariesPanel_updatingPackages());
                progressHandle.start();
                try {
                    PackageJson packagejson = getPackageJson();
                    if (packagejson.exists()) {
                        PackageJson.NpmDependencies dependencies = packagejson.getDependencies();
                        List<String> errors = new ArrayList<>();
                        storeChanges(dependencies.dependencies,
                                regularPanel.getSelectedDependencies(),
                                PackageJson.FIELD_DEPENDENCIES, errors);
                        storeChanges(dependencies.devDependencies,
                                developmentPanel.getSelectedDependencies(),
                                PackageJson.FIELD_DEV_DEPENDENCIES, errors);
                        storeChanges(dependencies.optionalDependencies,
                                optionalPanel.getSelectedDependencies(),
                                PackageJson.FIELD_OPTIONAL_DEPENDENCIES, errors);
                        reportErrors(errors);
                    }
                } finally {
                    progressHandle.finish();
                    progressHandle = null;
                }
            }
        });
    }

    /**
     * Notifies the user about errors that occurred while storing changes.
     * 
     * @param errors list of error messages (possibly empty).
     */
    private void reportErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (String error : errors) {
                if (message.length() != 0) {
                    message.append('\n');
                }
                message.append(error);
            }
            NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                    message.toString(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(descriptor);
        }
    }

    /**
     * Performs/stores the changes requested by the user for the specified
     * type of dependencies.
     * 
     * @param originalDependencies original dependencies.
     * @param selectedDependencies requested list of dependencies.
     * @param dependencyType dependency type (name of the corresponding field
     * in {@code package.json}).
     * @param errors collection that should be populated with errors that occurred.
     */
    @NbBundle.Messages({
        "# {0} - library name",
        "# {1} - library version",
        "LibrariesPanel.dependencyNotSet=Unable to set {0}@{1} dependency in package.json!",
        "# {0} - library name",
        "# {1} - library version",
        "LibrariesPanel.installationFailed=Installation of version {1} of package {0} failed!"
    })
    private void storeChanges(Map<String,String> originalDependencies,
            List<Dependency> selectedDependencies,
            String dependencyType, List<String> errors) {
        // Update package.json
        PackageJson packagejson = getPackageJson();
        if (packagejson.exists()) {
            // Remove obsolete dependencies
            Set<String> selectedSet = new HashSet<>();
            for (Dependency dependency : selectedDependencies) {
                selectedSet.add(dependency.getName());
            }
            for (String name : originalDependencies.keySet()) {
                if (!selectedSet.contains(name)) {
                    // PENDING PackageJson.removeContent(Arrays.asList(dependencyType, name));
                    errors.add("Cannot remove "+name+" - removal of dependencies not implemented yet!");
                }
            }
            
            // Add new/update existing dependencies
            for (Dependency dependency : selectedDependencies) {
                String name = dependency.getName();
                String oldRequiredVersion = originalDependencies.get(name);
                String newRequiredVersion = dependency.getRequiredVersion();
                if (!newRequiredVersion.equals(oldRequiredVersion)) {
                    try {
                        packagejson.setContent(Arrays.asList(dependencyType, name), newRequiredVersion);
                    } catch (IOException ioex) {
                        Logger.getLogger(LibrariesPanel.class.getName()).log(Level.INFO, null, ioex);
                        errors.add(Bundle.LibrariesPanel_dependencyNotSet(name, newRequiredVersion));
                    }
                }
            }
        }

        // Install missing packages
        NpmExecutable executable = NpmExecutable.getDefault(project, false);
        if (executable != null) {
            for (Dependency dependency : selectedDependencies) {
                String name = dependency.getName();
                String versionToInstall = dependency.getInstalledVersion();
                String installedVersion = installedLibraries.get(name);
                if (versionToInstall != null && !versionToInstall.equals(installedVersion)) {
                    Integer result = null;
                    try {
                        // npm install name@versionToInstall
                        Future<Integer> future = executable.install(name + "@" + versionToInstall); // NOI18N
                        if (future != null) {
                            result = future.get();
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(LibrariesPanel.class.getName()).log(Level.INFO, null, ex);
                    }
                    if (result == null || result != 0) {
                        errors.add(Bundle.LibrariesPanel_installationFailed(name, versionToInstall));
                    }
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        packageJsonProblemLabel = new javax.swing.JLabel();
        npmProblemPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        configureButton = new javax.swing.JButton();
        retryButton = new javax.swing.JButton();
        npmProblemLabel = new javax.swing.JLabel();
        loadingLabel = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        regularPanel = new org.netbeans.modules.javascript.nodejs.ui.libraries.DependenciesPanel();
        developmentPanel = new org.netbeans.modules.javascript.nodejs.ui.libraries.DependenciesPanel();
        optionalPanel = new org.netbeans.modules.javascript.nodejs.ui.libraries.DependenciesPanel();

        packageJsonProblemLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(packageJsonProblemLabel, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.packageJsonProblemLabel.text")); // NOI18N
        packageJsonProblemLabel.setEnabled(false);
        packageJsonProblemLabel.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        org.openide.awt.Mnemonics.setLocalizedText(configureButton, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.configureButton.text")); // NOI18N
        configureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(retryButton, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.retryButton.text")); // NOI18N
        retryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addComponent(configureButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(retryButton))
        );

        buttonPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {configureButton, retryButton});

        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(configureButton)
                .addComponent(retryButton))
        );

        org.openide.awt.Mnemonics.setLocalizedText(npmProblemLabel, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.npmProblemLabel.text")); // NOI18N

        javax.swing.GroupLayout npmProblemPanelLayout = new javax.swing.GroupLayout(npmProblemPanel);
        npmProblemPanel.setLayout(npmProblemPanelLayout);
        npmProblemPanelLayout.setHorizontalGroup(
            npmProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(npmProblemPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(npmProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(npmProblemLabel)
                    .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        npmProblemPanelLayout.setVerticalGroup(
            npmProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(npmProblemPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(npmProblemLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        loadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(loadingLabel, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.loadingLabel.text")); // NOI18N
        loadingLabel.setEnabled(false);
        loadingLabel.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.regularPanel.TabConstraints.tabTitle"), regularPanel); // NOI18N
        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.developmentPanel.TabConstraints.tabTitle"), developmentPanel); // NOI18N
        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.optionalPanel.TabConstraints.tabTitle"), optionalPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        OptionsDisplayer.getDefault().open(NodeJsOptionsPanelController.OPTIONS_PATH);
    }//GEN-LAST:event_configureButtonActionPerformed

    private void retryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retryButtonActionPerformed
        loadInstalledLibraries();
    }//GEN-LAST:event_retryButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton configureButton;
    private org.netbeans.modules.javascript.nodejs.ui.libraries.DependenciesPanel developmentPanel;
    private javax.swing.JLabel loadingLabel;
    private javax.swing.JLabel npmProblemLabel;
    private javax.swing.JPanel npmProblemPanel;
    private org.netbeans.modules.javascript.nodejs.ui.libraries.DependenciesPanel optionalPanel;
    private javax.swing.JLabel packageJsonProblemLabel;
    private org.netbeans.modules.javascript.nodejs.ui.libraries.DependenciesPanel regularPanel;
    private javax.swing.JButton retryButton;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Listener invoked when the changes in the project customizer are confirmed.
     */
    private class StoreListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            storeChanges();
        }
        
    }

}
