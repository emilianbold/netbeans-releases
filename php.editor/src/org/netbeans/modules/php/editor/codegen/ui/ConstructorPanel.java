/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * ConstructorPanel.java
 *
 * Created on Jul 20, 2008, 10:34:25 PM
 */

package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.Dimension;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.php.editor.codegen.CGSGenerator;
import org.netbeans.modules.php.editor.codegen.CGSInfo;
import org.netbeans.modules.php.editor.codegen.Property;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class ConstructorPanel extends javax.swing.JPanel {

    private final String className;
    private final List<Property> properties;
    private final CGSInfo cgsInfo;

    /** Creates new form ConstructorPanel */
    public ConstructorPanel(CGSGenerator.GenType genType, CGSInfo cgsInfo) {
        initComponents();
        this.className = cgsInfo.getClassName();
        switch (genType) {
            case CONSTRUCTOR: properties = cgsInfo.getProperties(); break;
            case GETTER: properties = cgsInfo.getPossibleGetters(); break;
            case SETTER: properties = cgsInfo.getPossibleSetters(); break;
            case METHODS: properties = cgsInfo.getPossibleMethods(); break;
            default: properties = cgsInfo.getPossibleGettersSetters(); break;
        }
        this.cgsInfo = cgsInfo;
        initPanel(genType);
        initTree();
    }

    private void initPanel(CGSGenerator.GenType genType) {
        String panelTitle = "";                     //NOI18N
        boolean customizeMethodGeneration = true;
        String name = "";
        if (properties.size() > 0) {
            name = properties.get(0).getName();
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        switch (genType) {
            case CONSTRUCTOR:
                panelTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_CONSTRUCTOR");    //NOI18N
                customizeMethodGeneration = false;
                break;
            case GETTER:
                panelTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_GETTERS");    //NOI18N
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    model.addElement(way.getGetterExample(name));
                }
                break;
            case SETTER:
                panelTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_SETTERS");    //NOI18N
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    model.addElement(way.getSetterExample(name));
                }
                break;
            case GETTER_AND_SETTER:
                panelTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_GETTERS_AND_SETTERS");    //NOI18N
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    model.addElement(way.getGetterExample(name) + ", " + way.getSetterExample(name));
                }
                break;
            case METHODS:
                panelTitle = NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_METHODS");    //NOI18N
                customizeMethodGeneration = false;
                Dimension preferredSize = getPreferredSize();
                setPreferredSize(new Dimension((int)(preferredSize.getWidth()*1.3), (int)(preferredSize.getHeight()*1.3)));
        }
        this.label.setText(panelTitle);
        this.pGSCustomize.setVisible(customizeMethodGeneration);
        if (customizeMethodGeneration) {
            cbMethodGeneration.setModel(model);
            int index = 0;
            if (cgsInfo.getHowToGenerate() != null) {
                for (CGSGenerator.GenWay genWay : CGSGenerator.GenWay.values()) {
                    if (genWay.equals(cgsInfo.getHowToGenerate())) {
                        break;
                    }
                    index++;
                }
            }
            cbMethodGeneration.setSelectedIndex(index);
        }
        cbGenerateDoc.setSelected(cgsInfo.isGenerateDoc());
        cbGenerateDoc.setVisible(false);
    }

    private void initTree(){
        CheckNode root;
        root = new CheckNode.CGSClassNode(className);
        JTree tree = new JTree(root);

        for (Property property : properties) {
            root.add(new CheckNode.CGSPropertyNode(property));
        }
        tree.setCellRenderer(new CheckBoxTreeRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.putClientProperty("JTree.lineStyle", "Angled");  //NOI18N
        NodeSelectionListener listener = new NodeSelectionListener(tree);
        tree.addMouseListener(listener);
        tree.addKeyListener(listener);
        tree.expandRow(0);
        tree.setShowsRootHandles(true);
        tree.setSelectionRow(0);
        scrollPane.add(tree);
        scrollPane.setViewportView(tree);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")  //NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        pGSCustomize = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbMethodGeneration = new javax.swing.JComboBox();
        cbGenerateDoc = new javax.swing.JCheckBox();

        label.setText(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.label.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.jLabel1.text")); // NOI18N

        cbMethodGeneration.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbMethodGeneration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMethodGenerationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pGSCustomizeLayout = new javax.swing.GroupLayout(pGSCustomize);
        pGSCustomize.setLayout(pGSCustomizeLayout);
        pGSCustomizeLayout.setHorizontalGroup(
            pGSCustomizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pGSCustomizeLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbMethodGeneration, 0, 247, Short.MAX_VALUE)
                .addContainerGap())
        );
        pGSCustomizeLayout.setVerticalGroup(
            pGSCustomizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pGSCustomizeLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pGSCustomizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbMethodGeneration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(65, 65, 65))
        );

        cbGenerateDoc.setText(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.cbGenerateDoc.text")); // NOI18N
        cbGenerateDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbGenerateDocActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(label, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbGenerateDoc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(pGSCustomize, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pGSCustomize, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbGenerateDoc)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbMethodGenerationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMethodGenerationActionPerformed
        int selectedIndex = cbMethodGeneration.getSelectedIndex();
        cgsInfo.setHowToGenerate(CGSGenerator.GenWay.values()[selectedIndex]);
    }//GEN-LAST:event_cbMethodGenerationActionPerformed

    private void cbGenerateDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbGenerateDocActionPerformed
        cgsInfo.setGenerateDoc(cbGenerateDoc.isSelected());
    }//GEN-LAST:event_cbGenerateDocActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbGenerateDoc;
    private javax.swing.JComboBox cbMethodGeneration;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel label;
    private javax.swing.JPanel pGSCustomize;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}
