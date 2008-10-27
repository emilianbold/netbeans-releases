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

/*
 * HibernateMappingWizardPanel.java
 *
 * Created on February 5, 2008, 12:23 PM
 */
package org.netbeans.modules.hibernate.wizards;

import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import javax.swing.SwingUtilities;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  gowri
 */
public class HibernateMappingWizardPanel extends javax.swing.JPanel {

    private Project project;
    List<FileObject> configFileObjects;
    List<String> databaseTables;
    private HibernateEnvironment env;

    /** Creates new form HibernateMappingWizardPanel */
    public HibernateMappingWizardPanel(Project project) {
        this.project = project;
        initComponents();
        env = project.getLookup().lookup(HibernateEnvironment.class);
        String[] configFiles = getConfigFilesFromProject(project);
        this.cmbResource.setModel(new DefaultComboBoxModel(configFiles));
        fillDatabaseTable();
        this.browseButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
    }

    // This fills the databaseTable drop down with a list of tables
    public void fillDatabaseTable() {
        if (cmbResource.getItemCount() == 0) {
            this.cmbDatabaseTable.setModel(new DefaultComboBoxModel(new String[]{}));
        } else {
            if (cmbResource.getSelectedIndex() != -1) {
                try {
                    ////  HibernateConfiguration hibConf = ((HibernateCfgDataObject) DataObject.find(configFileObjects.get(cmbResource.getSelectedIndex()))).getHibernateConfiguration();
                    //  databaseTables = env.getAllDatabaseTables(hibConf);
                    databaseTables = env.getAllDatabaseTablesOnEventThread(configFileObjects.get(cmbResource.getSelectedIndex()));
                    // adding an empty element to the list
                    databaseTables.add(0, "");
                    this.cmbDatabaseTable.setModel(new DefaultComboBoxModel(databaseTables.toArray()));
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (DatabaseException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (SQLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private void browseButtonActionPerformed(ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                final ElementHandle<TypeElement> handle = TypeElementFinder.find(null, new TypeElementFinder.Customizer() {

                    public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {
                        return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
                    }

                    public boolean accept(ElementHandle<TypeElement> typeHandle) {
                        return true;
                    }
                });

                if (handle != null) {
                    txtClassName.setText(handle.getQualifiedName());
                }
            }
        });

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "LBL_HibernateMappingPanel_Name"); // NOI18N
    }

    public String getClassName() {
        if (txtClassName.getText() != null) {
            return txtClassName.getText().trim();
        }
        return null;
    }

    public FileObject getConfigurationFile() {
        if (cmbResource.getSelectedIndex() != -1) {
            return configFileObjects.get(cmbResource.getSelectedIndex());
        }
        return null;
    }

    public String getDatabaseTable() {
        if (cmbDatabaseTable.getSelectedIndex() != -1) {
            return cmbDatabaseTable.getSelectedItem().toString().trim();
        }
        return null;
    }

    // Gets the list of Config files from HibernateEnvironment.
    public String[] getConfigFilesFromProject(Project project) {
        List<String> configFiles = new ArrayList<String>();
        configFileObjects = env.getAllHibernateConfigFileObjects();
        for (FileObject fo : configFileObjects) {
            configFiles.add(fo.getNameExt());
        }
        return configFiles.toArray(new String[]{});
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtClassName = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        cmbResource = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        cmbDatabaseTable = new javax.swing.JComboBox();

        setName(org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "LBL_HibernateMappingPanel_Name")); // NOI18N

        jLabel1.setLabelFor(txtClassName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "HibernateMappingWizardPanel.jLabel1.text")); // NOI18N

        txtClassName.setColumns(30);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "HibernateMappingWizardPanel.browseButton.text")); // NOI18N

        jLabel2.setLabelFor(cmbResource);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "HibernateMappingWizardPanel.jLabel2.text")); // NOI18N

        cmbResource.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbResource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbResourceActionPerformed(evt);
            }
        });

        jLabel3.setLabelFor(cmbDatabaseTable);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "HibernateMappingWizardPanel.jLabel3.text")); // NOI18N

        cmbDatabaseTable.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(27, 27, 27)
                        .add(txtClassName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cmbDatabaseTable, 0, 431, Short.MAX_VALUE)
                            .add(cmbResource, 0, 431, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(browseButton)
                    .add(txtClassName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(cmbResource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cmbDatabaseTable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    private void cmbResourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbResourceActionPerformed
        // TODO add your handling code here:
        fillDatabaseTable();
    }//GEN-LAST:event_cmbResourceActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox cmbDatabaseTable;
    private javax.swing.JComboBox cmbResource;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField txtClassName;
    // End of variables declaration//GEN-END:variables
}
