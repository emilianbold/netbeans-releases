/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * CustomizerCommon.java
 *
 * Created on Jul 27, 2011, 11:55:54 AM
 */
package org.netbeans.modules.coherence.server.ui;

import java.awt.Dimension;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.modules.coherence.library.LibraryUtils;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.netbeans.modules.coherence.server.util.ClasspathPropertyUtils;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Panel for setup base (common) Coherence instance properties.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CustomizerCommon extends javax.swing.JPanel implements ChangeListener {

    private DefaultListModel listModel;
    private CoherenceProperties coherenceProperties;
    private JFileChooser fileChooser = new JFileChooser();

    private ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new {@code CustomizerCommon} panel.
     *
     * @param coherenceProperties properties for which will be form initialized
     */
    public CustomizerCommon(CoherenceProperties coherenceProperties) {
        initComponents();
        this.coherenceProperties = coherenceProperties;

        init();
    }

    /**
     * Initialization of the panel values.
     */
    private void init() {
        coherenceLocationTextField.setText(coherenceProperties.getServerRoot());
        javaFlagsTextField.setText(coherenceProperties.getJavaFlags());
        customPropertiesTextField.setText(coherenceProperties.getCustomJavaProps());

        listModel = new DefaultListModel();
        for (String cp : ClasspathPropertyUtils.classpathFromStringToArray(coherenceProperties.getClasspath())) {
            if (!ClasspathPropertyUtils.isCoherenceServerJar(cp, true)) {
                listModel.addElement(cp);
            }
        }
        classpathList.setModel(listModel);

        changeSupport.addChangeListener(this);
        coherenceLocationTextField.getDocument().addDocumentListener(new SaveDocumentListener());
        javaFlagsTextField.getDocument().addDocumentListener(new SaveDocumentListener());
        customPropertiesTextField.getDocument().addDocumentListener(new SaveDocumentListener());
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        savePanel();
    }

    @NbBundle.Messages({
        "project.chooser.title=Add Project",
        "project.chooser.add.project.jars=Add Project JAR Files",
        "project.chooser.msg.no.jar.output=This project cannot be added because it does not produce a JAR file using an Ant script."
    })
    private void showProjectChooser() {
        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.setDialogTitle(Bundle.project_chooser_title());
        chooser.setApproveButtonText(Bundle.project_chooser_add_project_jars());
        chooser.setPreferredSize(new Dimension(650, 380));
        chooser.setCurrentDirectory(getNearestDirectory());
        int option = chooser.showOpenDialog(this); //Show the chooser
        if (option == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            Project selectedProject = FileOwnerQuery.getOwner(FileUtil.toFileObject(dir));

            if (selectedProject != null) {
                AntArtifact[] artifacts = AntArtifactQuery.findArtifactsByType(selectedProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
                if (artifacts.length == 0) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.project_chooser_msg_no_jar_output()));
                    return;
                }

                for (AntArtifact antArtifact : artifacts) {
                    for (URI uri : antArtifact.getArtifactLocations()) {
                        File jar = new File(FileUtil.toFile(selectedProject.getProjectDirectory()), uri.getPath());
                        addElementToClasspathList(jar.getAbsolutePath());
                    }
                }
            }
        }
    }

    private static File getNearestDirectory() {
        File folder = ProjectChooser.getProjectsFolder();
        if (folder == null) {
            folder = new File(System.getProperty("user.home")); //NOI18N
        }
        return FileUtil.normalizeFile(folder);
    }

    private void showLibraryChooser() {
        Set<Library> libraries = LibraryChooser.showDialog(
                LibraryManager.getDefault(),
                createLibraryFilter(),
                null);
        if (libraries != null) {
            for (Library library : libraries) {
                List<URL> cpContent = library.getContent("classpath"); //NOI18H
                for (URL jarUrl : cpContent) {
                    String libraryJarPath = getLibraryJarPath(jarUrl);
                    if (libraryJarPath != null) {
                        addElementToClasspathList(libraryJarPath);
                    }
                }
            }
        }
    }

    private static String getLibraryJarPath(URL pathUrl) {
        String jarPath = null;
        URI uri;
        try {
            uri = ((URL) pathUrl).toURI();
        } catch (URISyntaxException ex) {
            Logger.getLogger(CustomizerCommon.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }
        if (uri != null && uri.toString().startsWith("http")) { //NOI18N
            return null;
        } else if (uri != null) {
            if (uri.toString().contains("!/")) { //NOI18N
                uri = LibrariesSupport.getArchiveFile(uri);
            }
            FileObject fo = LibrariesSupport.resolveLibraryEntryFileObject(null, uri);
            if (fo == null) {
                jarPath = uri.toString();
            } else {
                if (uri.isAbsolute()) {
                    jarPath = FileUtil.getFileDisplayName(fo);
                } else {
                    jarPath = LibrariesSupport.convertURIToFilePath(uri);
                }
            }
        }
        return jarPath;
    }

    private static LibraryChooser.Filter createLibraryFilter() {
        return  new LibraryChooser.Filter() {

            @Override
            public boolean accept(Library library) {
                if ("javascript".equals(library.getType())) { //NOI18N
                    return false;
                }
                try {
                    library.getContent("classpath"); //NOI18N
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }
        };
    }

    private class SaveDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            fireChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            fireChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            fireChange();
        }

        private void fireChange() {
            changeSupport.fireChange();
        }
    }

    /**
     * Storing values from this panel into {@link InstanceProperties}.
     */
    private void savePanel() {
        coherenceProperties.setJavaFlags(javaFlagsTextField.getText());
        coherenceProperties.setCustomJavaProps(customPropertiesTextField.getText());

        // update classpath property
        List<String> cpEntries = new ArrayList<String>();
        for (int i = 0; i < classpathList.getModel().getSize(); i++) {
            cpEntries.add((String) classpathList.getModel().getElementAt(i));
        }
        String cp = ClasspathPropertyUtils.getUpdatedClasspath(
                coherenceProperties.getClasspath(),
                cpEntries.toArray(new String[cpEntries.size()]),
                null);
        coherenceProperties.setClasspath(cp);
    }

    private void addElementToClasspathList(String element) {
        listModel.addElement(element);
        changeSupport.fireChange();
    }

    /**
     * Shows the fileChooser.
     */
    private void showFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        // set the chooser's properties
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                String fileName = f.getName();
                String fileExt = fileName.substring(
                        fileName.lastIndexOf(".") + 1, //NOI18N
                        fileName.length());
                if (f.isDirectory()) {
                    return true;
                } else if (f.isFile() && "jar".equalsIgnoreCase(fileExt)) { //NOI18N
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(CustomizerCommon.class, "DESC_AddJarToClasspath"); //NOI18N
            }
        });
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // wait for the user to choose the file and if he clicked OK button add
        // the selected JAR into the classpath list
        if (fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            addElementToClasspathList(fileChooser.getSelectedFile().getPath());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        classpathLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        classpathList = new javax.swing.JList();
        addJarButton = new javax.swing.JButton();
        removeClasspathButton = new javax.swing.JButton();
        javaFlagsLabel = new javax.swing.JLabel();
        javaFlagsTextField = new javax.swing.JTextField();
        customPropertiesLabel = new javax.swing.JLabel();
        customPropertiesTextField = new javax.swing.JTextField();
        coherenceLocationTextField = new javax.swing.JTextField();
        coherenceLocationLabel = new javax.swing.JLabel();
        createLibraryButton = new javax.swing.JButton();
        addProjectButton = new javax.swing.JButton();
        addLibraryButton = new javax.swing.JButton();

        setName(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "TITLE_Common")); // NOI18N

        classpathLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.classpathLabel.text")); // NOI18N
        classpathLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.additionalClasspathLabel.desc")); // NOI18N

        classpathList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        classpathList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                classpathListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(classpathList);

        addJarButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.addJarButton.text")); // NOI18N
        addJarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJarButtonActionPerformed(evt);
            }
        });

        removeClasspathButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.removeClasspathButton.text")); // NOI18N
        removeClasspathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClasspathButtonActionPerformed(evt);
            }
        });

        javaFlagsLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.javaFlagsLabel.text")); // NOI18N
        javaFlagsLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.javaFlagsLabel.desc")); // NOI18N

        javaFlagsTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.javaFlagsTextField.text")); // NOI18N

        customPropertiesLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.customPropertiesLabel.text")); // NOI18N
        customPropertiesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.customPropertiesLabel.desc")); // NOI18N

        customPropertiesTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.customPropertiesTextField.text")); // NOI18N

        coherenceLocationTextField.setEditable(false);

        coherenceLocationLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.coherenceLocationLabel.text")); // NOI18N

        createLibraryButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.createLibraryButton.text")); // NOI18N
        createLibraryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLibraryButtonActionPerformed(evt);
            }
        });

        addProjectButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.addProjectButton.text")); // NOI18N
        addProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProjectButtonActionPerformed(evt);
            }
        });

        addLibraryButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.addLibraryButton.text")); // NOI18N
        addLibraryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLibraryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaFlagsLabel)
                            .addComponent(customPropertiesLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaFlagsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addComponent(customPropertiesTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(coherenceLocationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(coherenceLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))
                    .addComponent(classpathLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addJarButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeClasspathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addProjectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addLibraryButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(createLibraryButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(coherenceLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(coherenceLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(classpathLabel)
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addJarButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addProjectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addLibraryButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeClasspathButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaFlagsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(javaFlagsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customPropertiesLabel)
                    .addComponent(customPropertiesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(createLibraryButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addJarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJarButtonActionPerformed
        showFileChooser();
    }//GEN-LAST:event_addJarButtonActionPerformed

    private void removeClasspathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClasspathButtonActionPerformed
        if (classpathList.getSelectedIndex() == -1) {
            return;
        }
        listModel.remove(classpathList.getSelectedIndex());
        changeSupport.fireChange();
    }//GEN-LAST:event_removeClasspathButtonActionPerformed

private void classpathListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_classpathListValueChanged
    if (classpathList.getSelectedValue() == null) {
        return;
    }
}//GEN-LAST:event_classpathListValueChanged

    private void createLibraryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLibraryButtonActionPerformed
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(CustomizerCommon.class, "MSG_ConfirmationForLibraryCreation", coherenceProperties.getDisplayName()), //NOI18N
                NbBundle.getMessage(CustomizerCommon.class, "TIT_LibraryCreationDialog"), //NOI18N
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE);
        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
            File location = new File(coherenceProperties.getServerRoot());
            LibraryUtils.createCoherenceLibrary(location);
        }
    }//GEN-LAST:event_createLibraryButtonActionPerformed

    private void addProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProjectButtonActionPerformed
        showProjectChooser();
    }//GEN-LAST:event_addProjectButtonActionPerformed

    private void addLibraryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLibraryButtonActionPerformed
        showLibraryChooser();
    }//GEN-LAST:event_addLibraryButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addJarButton;
    private javax.swing.JButton addLibraryButton;
    private javax.swing.JButton addProjectButton;
    private javax.swing.JLabel classpathLabel;
    private javax.swing.JList classpathList;
    private javax.swing.JLabel coherenceLocationLabel;
    private javax.swing.JTextField coherenceLocationTextField;
    private javax.swing.JButton createLibraryButton;
    private javax.swing.JLabel customPropertiesLabel;
    private javax.swing.JTextField customPropertiesTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel javaFlagsLabel;
    private javax.swing.JTextField javaFlagsTextField;
    private javax.swing.JButton removeClasspathButton;
    // End of variables declaration//GEN-END:variables
}
