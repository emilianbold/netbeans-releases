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
package org.netbeans.modules.coherence.editor.pof;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.BindingListener;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Property;
import org.jdesktop.beansbinding.PropertyStateEvent;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.coherence.xml.pof.PofConfig;
import org.netbeans.modules.coherence.xml.pof.PofConfigComponentFactory;
import org.netbeans.modules.coherence.xml.pof.PofConfigModel;
import org.netbeans.modules.coherence.xml.pof.PofConfigModelFactory;
import org.netbeans.modules.coherence.xml.pof.Serializer;
import org.netbeans.modules.coherence.xml.pof.UserType;
import org.netbeans.modules.coherence.xml.pof.UserTypeList;
import org.netbeans.modules.coherence.xml.pof.ValueNotPermittedException;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@MultiViewElement.Registration(displayName = "#LBL_PofConfig_VISUAL",
iconBase = "org/netbeans/modules/coherence/resources/icons/pof.png",
mimeType = "text/coh-pof+xml",
persistenceType = TopComponent.PERSISTENCE_NEVER,
preferredID = "PofConfigVisual",
position = 1000)
@Messages({
    "LBL_PofConfig_VISUAL=General"
})
/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public final class PofConfigVisualElement extends JPanel implements MultiViewElement, TableModelListener, ListSelectionListener, BindingListener {

    private PofConfigDataObject obj;
    private JToolBar toolbar = new JToolBar();
    private transient MultiViewElementCallback callback;

    public PofConfigVisualElement(Lookup lkp) {
        obj = lkp.lookup(PofConfigDataObject.class);
        assert obj != null;
        initComponents();
        initialise();
    }

    @Override
    public String getName() {
        return "PofConfigVisualElement";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        topPanel = new javax.swing.JPanel();
        generalPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        cbAllowInterfaces = new javax.swing.JCheckBox();
        cbAllowSubclesses = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfClassName = new javax.swing.JTextField();
        standardPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jToggleButton2 = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabStandardUT = new javax.swing.JTable();
        customPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jToggleButton3 = new javax.swing.JToggleButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabCustomUT = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btnEditImage = new javax.swing.JButton();
        btnAddImage = new javax.swing.JButton();
        btnDeleteImage = new javax.swing.JButton();

        jToggleButton1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jToggleButton1, org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.jToggleButton1.text")); // NOI18N
        jToggleButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbAllowInterfaces, org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.cbAllowInterfaces.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbAllowSubclesses, org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.cbAllowSubclesses.text")); // NOI18N

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.jPanel7.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tfClassName, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfClassName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbAllowInterfaces)
                    .addComponent(cbAllowSubclesses)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(cbAllowInterfaces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbAllowSubclesses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(75, Short.MAX_VALUE))
            .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jToggleButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(jToggleButton2, org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.jToggleButton2.text")); // NOI18N
        jToggleButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToggleButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToggleButton2)
        );

        tabStandardUT.setModel(getStandardUserTypes());
        tabStandardUT.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(tabStandardUT);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout standardPanelLayout = new javax.swing.GroupLayout(standardPanel);
        standardPanel.setLayout(standardPanelLayout);
        standardPanelLayout.setHorizontalGroup(
            standardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        standardPanelLayout.setVerticalGroup(
            standardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(standardPanelLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jToggleButton3, org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.jToggleButton3.text")); // NOI18N
        jToggleButton3.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToggleButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jToggleButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabCustomUT.setModel(getCustomUserTypes());
        tabCustomUT.setFillsViewportHeight(true);
        jScrollPane2.setViewportView(tabCustomUT);

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnEdit, org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.btnEdit.text")); // NOI18N
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(PofConfigVisualElement.class, "PofConfigVisualElement.btnRemove.text")); // NOI18N
        btnRemove.setEnabled(false);
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEdit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemove)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnAdd)
                .addComponent(btnEdit)
                .addComponent(btnRemove))
        );

        btnEditImage.setBorderPainted(false);
        btnEditImage.setContentAreaFilled(false);
        btnEditImage.setEnabled(false);
        btnEditImage.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnEditImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditImageActionPerformed(evt);
            }
        });

        btnAddImage.setBorderPainted(false);
        btnAddImage.setContentAreaFilled(false);
        btnAddImage.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnAddImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddImageActionPerformed(evt);
            }
        });

        btnDeleteImage.setBorderPainted(false);
        btnDeleteImage.setContentAreaFilled(false);
        btnDeleteImage.setEnabled(false);
        btnDeleteImage.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnDeleteImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteImageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnEditImage)
            .addComponent(btnAddImage)
            .addComponent(btnDeleteImage)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnAddImage)
                .addGap(6, 6, 6)
                .addComponent(btnEditImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteImage)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout customPanelLayout = new javax.swing.GroupLayout(customPanel);
        customPanel.setLayout(customPanelLayout);
        customPanelLayout.setHorizontalGroup(
            customPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        customPanelLayout.setVerticalGroup(
            customPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customPanelLayout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(standardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(generalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(customPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(standardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(customPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane3.setViewportView(topPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
    jPanel2.setVisible(jToggleButton1.isSelected());
}//GEN-LAST:event_jToggleButton1ActionPerformed

private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
    jPanel4.setVisible(jToggleButton2.isSelected());
}//GEN-LAST:event_jToggleButton2ActionPerformed

private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
    jPanel6.setVisible(jToggleButton3.isSelected());
}//GEN-LAST:event_jToggleButton3ActionPerformed

private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
    java.awt.EventQueue.invokeLater(new Runnable() {

        public void run() {
            EditUserTypeDialog dialog = new EditUserTypeDialog(null, true, null);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            if (dialog.getReturnStatus() == dialog.RET_OK) {
                getModel().startTransaction();
                try {
                    UserType ut = getFactory().createUserType();
                    ut.setClassName(getFactory().createClassName());
                    ut.getClassName().setValue(dialog.getClassname());
                    ut.setTypeId(getFactory().createTypeId());
                    ut.getTypeId().setValue(Integer.parseInt(dialog.getTypeId()));
                    String serializer = dialog.getSerializerClassname();
                    if (serializer != null & serializer.trim().length() > 0) {
                        Serializer utSerializer = getFactory().createSerializer();
                        utSerializer.setClassName(getFactory().createClassName());
                        utSerializer.getClassName().setValue(serializer);
                        ut.setSerializer(utSerializer);
                    }
                    PofConfig pofConfig = getPofConfig();
                    pofConfig.getUserTypeList().addElement(ut);
                    customUserTypes.addRow(ut.getTypeId().getValue(), ut.getClassName().getValue(), serializer, ut);
                    setModified();
                    //                    serialize();
                } catch (ValueNotPermittedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                getModel().endTransaction();
            }
        }
    });
}//GEN-LAST:event_btnAddActionPerformed

private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
    java.awt.EventQueue.invokeLater(new Runnable() {

        public void run() {
            int rowNum = tabCustomUT.getSelectedRow();
            UserType ut = (UserType) customUserTypes.getUserType(rowNum);
            EditUserTypeDialog dialog = new EditUserTypeDialog(null, true, ut);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            if (dialog.getReturnStatus() == dialog.RET_OK) {
                getModel().startTransaction();
                try {
                    ut.getClassName().setValue(dialog.getClassname());
                    ut.getTypeId().setValue(Integer.parseInt(dialog.getTypeId()));
                    String serializer = dialog.getSerializerClassname();
                    if (serializer != null & serializer.trim().length() > 0) {
                        Serializer utSerializer = getFactory().createSerializer();
                        if (utSerializer.getClassName() == null) {
                            utSerializer.setClassName(getFactory().createClassName());
                        }
                        utSerializer.getClassName().setValue(serializer);
                        ut.setSerializer(utSerializer);
                    }
                    //                    PofConfig pofConfig = getPofConfig();
                    //                    pofConfig.getUserTypeList().getUserTypeOrInclude().add(ut);
                    customUserTypes.updateRow(rowNum, ut.getTypeId().getValue(), ut.getClassName().getValue(), serializer);
                    setModified();
                    //                    serialize();
                } catch (ValueNotPermittedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                getModel().endTransaction();
            }
        }
    });
}//GEN-LAST:event_btnEditActionPerformed

private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
    java.awt.EventQueue.invokeLater(new Runnable() {

        public void run() {
            int option = JOptionPane.showConfirmDialog(customPanel, "Please Confirm User Type Removal", "", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                int rowNum = tabCustomUT.getSelectedRow();
                UserType ut = (UserType) customUserTypes.getUserType(rowNum);
                customUserTypes.removeRow(rowNum);

                getModel().startTransaction();
                for (UserType xmlUT : getPofConfig().getUserTypeList().getUserTypes()) {
                    if (xmlUT.getTypeId().equals(ut.getTypeId()) && xmlUT.getClassName().equals(ut.getClassName())) {
                        getPofConfig().getUserTypeList().removeElement(xmlUT);
                        break;
                    }
                }
                setModified();
                getModel().endTransaction();
//                    serialize();
            }
        }
    });
}//GEN-LAST:event_btnRemoveActionPerformed

private void btnEditImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditImageActionPerformed
    btnEditActionPerformed(evt);
}//GEN-LAST:event_btnEditImageActionPerformed

private void btnAddImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddImageActionPerformed
    btnAddActionPerformed(evt);
}//GEN-LAST:event_btnAddImageActionPerformed

private void btnDeleteImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteImageActionPerformed
    btnRemoveActionPerformed(evt);
}//GEN-LAST:event_btnDeleteImageActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAddImage;
    private javax.swing.JButton btnDeleteImage;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnEditImage;
    private javax.swing.JButton btnRemove;
    private javax.swing.JCheckBox cbAllowInterfaces;
    private javax.swing.JCheckBox cbAllowSubclesses;
    private javax.swing.JPanel customPanel;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JPanel standardPanel;
    private javax.swing.JTable tabCustomUT;
    private javax.swing.JTable tabStandardUT;
    private javax.swing.JTextField tfClassName;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return obj.getLookup();
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
        refreshBindings();
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        btnEdit.setEnabled(true);
        btnRemove.setEnabled(true);
        btnEditImage.setEnabled(true);
        btnDeleteImage.setEnabled(true);
    }

    @Override
    public void bindingBecameBound(Binding binding) {
    }

    @Override
    public void bindingBecameUnbound(Binding binding) {
    }

    @Override
    public void syncFailed(Binding binding, SyncFailure failure) {
    }

    @Override
    public void synced(Binding binding) {
    }

    @Override
    public void sourceChanged(Binding binding, PropertyStateEvent event) {
    }

    @Override
    public void targetChanged(Binding binding, PropertyStateEvent event) {
    }

    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    private PofConfigModel myModel = null;
    private PofConfig myRoot = null;
    private ModelSource myModelSource = null;
    private PofConfigComponentFactory myFactory = null;
    private UserTypesTableModel standardUserTypes = new UserTypesTableModel();
    private UserTypesTableModel customUserTypes = new UserTypesTableModel();
    private BindingGroup bindingGroup = new BindingGroup();

    private PofConfig getPofConfig() {
        return getRoot();
    }

    private PofConfig getRoot() {
        if (myRoot == null) {
            myRoot = getModel().getPofConfig();
        }
        return myRoot;
    }

    private PofConfigModel getModel() {
        if (myModel == null) {
            // Get Model
            myModel = PofConfigModelFactory.getInstance().getModel(getModelSource());
        }
        return myModel;
    }

    private ModelSource getModelSource() {
        if (myModelSource == null) {
            myModelSource = Utilities.getModelSource(obj.getPrimaryFile(), true);
        }
        return myModelSource;
    }

    private PofConfigComponentFactory getFactory() {
        if (myFactory == null) {
            myFactory = getModel().getFactory();
        }
        return myFactory;
    }
    /*
     * Custom Inner Classes
     */

    public class UserTypesTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Type Id", "Class Name", "Serializer"};
        private List<Object[]> data = new ArrayList<Object[]>();
        private boolean[] edittable = {false, false, false};

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object value = null;

            if (rowIndex < getRowCount() && columnIndex < getColumnCount()) {
                value = data.get(rowIndex)[columnIndex];
            }
            return value;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex < getRowCount() && columnIndex < getColumnCount()) {
                data.get(rowIndex)[columnIndex] = aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (getEdittable() == null || columnIndex > getEdittable().length) {
                return false;
            } else {
                return getEdittable()[columnIndex];
            }
        }

        public boolean[] getEdittable() {
            return edittable;
        }

        public void setEdittable(boolean[] edittable) {
            this.edittable = edittable;
        }

        public void addRow(int id, String name, String serializer, Object node) {
            Object[] row = {Integer.valueOf(id), name, serializer, node};
            data.add(row);
            fireTableDataChanged();
        }

        public void clear() {
            data.clear();
            fireTableDataChanged();
        }

        public void updateRow(int rowNum, int id, String name, String serializer) {
            try {
                data.get(rowNum)[0] = id;
                data.get(rowNum)[1] = name;
                data.get(rowNum)[2] = serializer;
                UserType ut = (UserType) data.get(rowNum)[3];
                ut.getClassName().setValue(name);
                ut.getTypeId().setValue(id);
                ut.getSerializer().getClassName().setValue(serializer);
                fireTableDataChanged();
            } catch (ValueNotPermittedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public Object getUserType(int rowIndex) {
            Object value = null;

            if (rowIndex < getRowCount()) {
                value = data.get(rowIndex)[3];
            }
            return value;
        }

        public void removeRow(int rowIndex) {
            if (rowIndex < getRowCount()) {
                data.remove(rowIndex);
            }
            fireTableDataChanged();
        }

        public List<Object[]> getData() {
            return data;
        }

        public void setData(List<Object[]> data) {
            this.data = data;
            fireTableDataChanged();
        }
    }

    public UserTypesTableModel getCustomUserTypes() {
        return customUserTypes;
    }

    public UserTypesTableModel getStandardUserTypes() {
        return standardUserTypes;
    }

    private void setModified() {
    }

    private void initialise() {
        // Set Display State of Panels based on Toggle Button Status
        jToggleButton1ActionPerformed(null);
        jToggleButton2ActionPerformed(null);
        jToggleButton3ActionPerformed(null);
        // Assign Change listener to Table
        tabCustomUT.getSelectionModel().addListSelectionListener(this);
        // Add Listeners
    }

    private void refreshBindings() {
        for (Binding b : bindingGroup.getBindings()) {
            bindingGroup.removeBinding(b);
        }
        setupBindings();
    }

    private void setupBindings() {
        PofConfig pofConfig = getPofConfig();
        // Set Bindings
        Property propertyTextValue = BeanProperty.create("text");
        Property propertySelected = BeanProperty.create("selected");
        Property propertySelectedItem = BeanProperty.create("selectedItem");
        Property propertyData = BeanProperty.create("data");
        // EditionName
        Property propertyDefaultSerializer = BeanProperty.create("defaultSerializer");
        Binding bindingDefaultSerializer = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyDefaultSerializer, tfClassName, propertyTextValue);
        bindingGroup.addBinding(bindingDefaultSerializer);
        // Allow SubClasses
        Property propertyAllowSubclasses = BeanProperty.create("allowSubclasses");
        Binding bindingAllowSubclasses = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyAllowSubclasses, cbAllowSubclesses, propertySelected);
        bindingGroup.addBinding(bindingAllowSubclasses);
        // Allow Interfaces
        Property propertyAllowInterfaces = BeanProperty.create("allowInterfaces");
        Binding bindingAllowInterfaces = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyAllowInterfaces, cbAllowInterfaces, propertySelected);
        bindingGroup.addBinding(bindingAllowInterfaces);
        // Standard Types
        Property propertyStandardTypes = BeanProperty.create("standardUserTypeRows");
        Binding bindingStandardTypes = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyStandardTypes, standardUserTypes, propertyData);
        bindingGroup.addBinding(bindingStandardTypes);
        // Standard Types
        Property propertyCustomTypes = BeanProperty.create("customUserTypeRows");
        Binding bindingCustomTypes = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyCustomTypes, customUserTypes, propertyData);
        bindingGroup.addBinding(bindingCustomTypes);
        // Bind All
        bindingGroup.addBindingListener(this);
        bindingGroup.bind();
    }
    /*
     * Binding Methods
     */

    public void setAllowInterfaces(boolean b) {
        getModel().startTransaction();
        if (getPofConfig().getAllowInterfaces() == null) {
            getPofConfig().setAllowInterfaces(getFactory().createAllowInterfaces());
        }
        getPofConfig().getAllowInterfaces().setValue(b);
        getModel().endTransaction();
    }

    public boolean isAllowInterfaces() {
        if (getPofConfig().getAllowInterfaces() == null) {
            getPofConfig().setAllowInterfaces(getFactory().createAllowInterfaces());
        }
        return getPofConfig().getAllowInterfaces().getValue();
    }

    public boolean getAllowInterfaces() {
        return isAllowInterfaces();
    }

    public void setAllowSubclasses(boolean b) {
        getModel().startTransaction();
        if (getPofConfig().getAllowSubclasses() == null) {
            getPofConfig().setAllowSubclasses(getFactory().createAllowSubclasses());
        }
        getPofConfig().getAllowSubclasses().setValue(b);
        getModel().endTransaction();
    }

    public boolean isAllowSubclasses() {
        if (getPofConfig().getAllowSubclasses() == null) {
            getPofConfig().setAllowSubclasses(getFactory().createAllowSubclasses());
        }
        return getPofConfig().getAllowSubclasses().getValue();
    }

    public boolean getAllSubclasses() {
        return isAllowSubclasses();
    }

    public void setDefaultSerializer(String s) {
        getModel().startTransaction();
        if (s == null || s.trim().length() == 0) {
            getPofConfig().setDefaultSerializer(null);
        } else {
            if (getPofConfig().getDefaultSerializer() == null) {
                getPofConfig().setDefaultSerializer(getFactory().createDefaultSerializer());
            }
            if (getPofConfig().getDefaultSerializer().getClassName() == null) {
                getPofConfig().getDefaultSerializer().setClassName(getFactory().createClassName());
            }
            getPofConfig().getDefaultSerializer().getClassName().setValue(s);
        }
        getModel().endTransaction();
    }

    public String getDefaultSerializer() {
        String s = null;
        if (getPofConfig().getDefaultSerializer() != null) {
            if (getPofConfig().getDefaultSerializer().getClassName() != null) {
                s = getPofConfig().getDefaultSerializer().getClassName().getValue();
            }
        }
        return s;
    }

    public void setStandardUserTypeRows(List<Object[]> data) {
        PofConfig pofConfig = getPofConfig();
        if (data != null) {
//            for (Object[] oArray : data) {
//            }
        }
    }

    public List<Object[]> getStandardUserTypeRows() {
        PofConfig pofConfig = getPofConfig();
        List<Object[]> objArrayList = new ArrayList<Object[]>();
        Object[] row = null;
        // Process User Type
        UserTypeList utl = pofConfig.getUserTypeList();
        if (utl != null) {
            List<UserType> utoiList = utl.getUserTypes();
            String serializer = null;
            if (utoiList != null) {
                for (UserType ut : utoiList) {
                    try {
                        serializer = null;
                        if (ut.getTypeId().getValue() < 1000) {
                            if (ut.getSerializer() != null) {
                                serializer = ut.getSerializer().getClassName().getValue();
                            }
                            row = new Object[4];
                            row[0] = ut.getTypeId().getValue();
                            row[1] = ut.getClassName();
                            row[2] = serializer;
                            row[3] = ut;
                            objArrayList.add(row);
                        }
                    } catch (Exception e) {
                        StatusDisplayer.getDefault().setStatusText("Failed to parse type");
                    }
                }
            }
        }
        return objArrayList;
    }

    public void setCustomUserTypeRows(List<Object[]> data) {
        PofConfig pofConfig = getPofConfig();
        if (data != null) {
//            for (Object[] oArray : data) {
//            }
        }
    }

    public List<Object[]> getCustomUserTypeRows() {
        PofConfig pofConfig = getPofConfig();
        List<Object[]> objArrayList = new ArrayList<Object[]>();
        Object[] row = null;
        // Process User Type
        UserTypeList utl = pofConfig.getUserTypeList();
        if (utl != null) {
            List<UserType> utoiList = utl.getUserTypes();
            String serializer = null;
            if (utoiList != null) {
                for (UserType ut : utoiList) {
                    try {
                        serializer = null;
                        if (ut.getTypeId().getValue() >= 1000) {
                            if (ut.getSerializer() != null) {
                                serializer = ut.getSerializer().getClassName().getValue();
                            }
                            row = new Object[4];
                            row[0] = ut.getTypeId().getValue();
                            row[1] = ut.getClassName();
                            row[2] = serializer;
                            row[3] = ut;
                            objArrayList.add(row);
                        }
                    } catch (Exception e) {
                        StatusDisplayer.getDefault().setStatusText("Failed to parse type");
                    }
                }
            }
        }
        return objArrayList;
    }
    /*
     * =========================================================================
     * END: Custom Code
     * =========================================================================
     */
}
