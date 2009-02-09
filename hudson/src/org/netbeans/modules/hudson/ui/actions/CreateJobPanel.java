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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory.ProjectHudsonJobCreator;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Visual configuration of {@link CreateJob}.
 */
public class CreateJobPanel extends JPanel implements ChangeListener {

    private Set<String> takenNames;
    private NotificationLineSupport notifications;
    private NotifyDescriptor descriptor;
    private Set<Project> manuallyAddedProjects = new HashSet<Project>();
    ProjectHudsonJobCreator creator;

    CreateJobPanel() {}

    void init(Set<String> takenNames, NotifyDescriptor descriptor) {
        this.takenNames = takenNames;
        this.descriptor = descriptor;
        this.notifications = descriptor.createNotificationLineSupport();
        initComponents();
        updateProjectModel();
        project.setSelectedItem(null);
        project.setRenderer(new ProjectRenderer());
        name.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                nameChange();
            }
            public void removeUpdate(DocumentEvent e) {
                nameChange();
            }
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    public @Override void addNotify() {
        super.addNotify();
        project.requestFocusInWindow();
        descriptor.setValid(false);
        // Doing this in init does not work (message is not displayed):
        notifications.setInformationMessage("Pick a project to create a job for."); // XXX I18N
    }

    private void ok() {
        descriptor.setValid(true);
        notifications.clearMessages();
    }

    private void error(String msg) {
        descriptor.setValid(false);
        notifications.setErrorMessage(msg);
    }

    String name() {
        return name.getText();
    }

    Project selectedProject() {
        return (Project) project.getSelectedItem();
    }

    private void updateProjectModel() {
        SortedSet<Project> projects = new TreeSet<Project>(new Comparator<Project>() {
            Collator COLL = Collator.getInstance();
            public int compare(Project o1, Project o2) {
                return COLL.compare(ProjectUtils.getInformation(o1).getDisplayName(),
                                    ProjectUtils.getInformation(o2).getDisplayName());
            }
        });
        projects.addAll(Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
        projects.addAll(manuallyAddedProjects);
        project.setModel(new DefaultComboBoxModel(projects.toArray(new Project[projects.size()])));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        project = new javax.swing.JComboBox();
        browse = new javax.swing.JButton();
        custom = new javax.swing.JPanel();

        nameLabel.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.nameLabel.text")); // NOI18N

        projectLabel.setLabelFor(project);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.projectLabel.text")); // NOI18N

        project.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.browse.text")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        custom.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(custom, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameLabel)
                            .add(projectLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, project, 0, 171, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browse)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLabel)
                    .add(project, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browse))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(custom, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.showOpenDialog(this);
        File dir = chooser.getSelectedFile();
        if (dir != null) {
            FileObject d = FileUtil.toFileObject(dir);
            if (d != null) {
                try {
                    Project p = ProjectManager.getDefault().findProject(d);
                    if (p != null) {
                        manuallyAddedProjects.add(p);
                        updateProjectModel();
                        project.setSelectedItem(p);
                    }
                } catch (IOException x) {
                    Exceptions.printStackTrace(x);
                }
            }
        }
    }//GEN-LAST:event_browseActionPerformed

    private void projectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectActionPerformed
        if (creator != null) {
            creator.removeChangeListener(this);
        }
        creator = null;
        ok();
        Project p = selectedProject();
        if (p == null) {
            error("You must pick a project."); // XXX I18N
            return;
        }
        if (p.getClass().getName().equals("org.netbeans.modules.project.ui.LazyProject")) { // NOI18N
            // XXX ugly but not obvious how better to handle this...
            updateProjectModel();
            project.setSelectedItem(null);
            return;
        }
        if (ProjectHudsonProvider.getDefault().findAssociation(p) != null) {
            // XXX check whether the association is still valid; job might have been deleted since then
            error("This project already seems to be associated with a Hudson job."); // XXX I18N
            return;
        }
        for (ProjectHudsonJobCreatorFactory factory : Lookup.getDefault().lookupAll(ProjectHudsonJobCreatorFactory.class)) {
            creator = factory.forProject(p);
            if (creator != null) {
                break;
            }
        }
        if (creator == null) {
            error("The IDE does not know how to set up a job for this project."); // XXX I18N
            return;
        }
        name.setText(creator.jobName());
        custom.removeAll();
        custom.add(creator.customizer());
        String problem = creator.error();
        if (problem != null) {
            error(problem);
        }
        creator.addChangeListener(this);
    }//GEN-LAST:event_projectActionPerformed

    public void stateChanged(ChangeEvent event) {
        if (creator != null) {
            String problem = creator.error();
            if (problem != null) {
                error(problem);
            } else {
                // Name might still be broken.
                nameChange();
            }
        }
    }

    private void nameChange() {
        if (creator == null) {
            return;
        }
        if (takenNames.contains(name())) {
            error("This name is taken. Pick another."); // XXX I18N
        } else {
            ok();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browse;
    private javax.swing.JPanel custom;
    private javax.swing.JTextField name;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox project;
    private javax.swing.JLabel projectLabel;
    // End of variables declaration//GEN-END:variables

    private static class ProjectRenderer extends DefaultListCellRenderer {
        public @Override Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            }
            ProjectInformation info = ProjectUtils.getInformation((Project) value);
            JLabel label = (JLabel) super.getListCellRendererComponent(list, info.getDisplayName(), index, isSelected, cellHasFocus);
            label.setIcon(info.getIcon());
            return label;
        }
    }

}
