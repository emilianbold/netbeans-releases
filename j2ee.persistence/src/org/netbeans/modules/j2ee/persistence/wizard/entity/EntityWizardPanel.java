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

package org.netbeans.modules.j2ee.persistence.wizard.entity;

import java.awt.Dimension;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Adamek
 */
public class EntityWizardPanel extends javax.swing.JPanel {
    
    private Project project;
    private ChangeListener listener;
    private PersistenceUnit persistenceUnit;
    
    static final String IS_VALID = "EntityWizardPanel_isValid"; //NOI18N
    
    public EntityWizardPanel(ChangeListener changeListener) {
        this.setProject(project);
        this.listener = changeListener;
        initComponents();
        
        setPersistenceUnitButtonVisibility(false, null);
        
        primaryKeyTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }
            public void insertUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }
            public void removeUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }
        });
        
    }
    
    public String getPrimaryKeyClassName() {
        return primaryKeyTextField.getText();
    }
    
    void setPersistenceUnitButtonVisibility(boolean visible, String warning) {
        createPUButton.setVisible(visible);
        Icon icon = null;
        if (warning != null) {
            icon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/j2ee/persistence/ui/resources/warning.gif"));
        } else {
            warning = " ";
        }
        createPUWarningLabel.setIcon(icon);
        createPUWarningLabel.setText(warning);
        createPUWarningLabel.setToolTipText(warning);
    }
    
    void setProject(Project project) {
        this.project = project;
    }

    public PersistenceUnit getPersistenceUnit() {
        return persistenceUnit;
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        persistenceGroup = new javax.swing.ButtonGroup();
        accessTypeGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        primaryKeyTextField = new javax.swing.JTextField();
        createPUButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        createPUWarningLabel = new ShyLabel();

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/entity/Bundle").getString("MN_PrimaryKeyType").charAt(0));
        jLabel1.setLabelFor(primaryKeyTextField);
        jLabel1.setText(org.openide.util.NbBundle.getBundle(EntityWizardPanel.class).getString("LBL_PrimaryKeyClass")); // NOI18N

        primaryKeyTextField.setText("Long");

        createPUButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/entity/Bundle").getString("MN_CreatePersistenceUnit").charAt(0));
        createPUButton.setText(org.openide.util.NbBundle.getMessage(EntityWizardPanel.class, "LBL_CreatePersistenceUnit")); // NOI18N
        createPUButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPUButtonActionPerformed(evt);
            }
        });

        searchButton.setText("...");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        createPUWarningLabel.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(primaryKeyTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchButton))
            .add(createPUButton)
            .add(createPUWarningLabel)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(searchButton)
                    .add(primaryKeyTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                .add(createPUWarningLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(createPUButton))
        );

        primaryKeyTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityWizardPanel.class, "LBL_PrimaryKeyClass")); // NOI18N
        createPUButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityWizardPanel.class, "LBL_CreatePersistenceUnit")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final ElementHandle<TypeElement> handle = TypeElementFinder.find(null, new TypeElementFinder.Customizer() {

                            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {
                                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
                            }

                            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                                //XXX not all types are supported as identifiers by the jpa spec, but 
                                // leaving unrestricted for now since different persistence providers 
                                // might support more types
                                return true;
                            }
                        });

                if (handle != null) {
                    primaryKeyTextField.setText(handle.getQualifiedName());
                }
            }
        });
    }//GEN-LAST:event_searchButtonActionPerformed
    
    private void createPUButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPUButtonActionPerformed
        persistenceUnit = Util.buildPersistenceUnitUsingWizard(project, null, TableGeneration.CREATE);
        if (persistenceUnit != null){
            firePropertyChange(IS_VALID, false, true);
            setPersistenceUnitButtonVisibility(false, null);
        }
    }//GEN-LAST:event_createPUButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessTypeGroup;
    private javax.swing.JButton createPUButton;
    private javax.swing.JLabel createPUWarningLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.ButtonGroup persistenceGroup;
    private javax.swing.JTextField primaryKeyTextField;
    private javax.swing.JButton searchButton;
    // End of variables declaration//GEN-END:variables
    
    /**
     * A crude attempt at a label which doesn't expand its parent.
     */
    private static final class ShyLabel extends JLabel {

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.width = 0;
            return size;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension size = super.getMinimumSize();
            size.width = 0;
            return size;
        }
    }
}
