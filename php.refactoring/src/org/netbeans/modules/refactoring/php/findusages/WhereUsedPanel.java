/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.refactoring.php.findusages;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;
import javax.swing.JPanel;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.openide.util.NbPreferences;

/**
 * Based on the WhereUsedPanel in Java refactoring by Jan Becicka.
 * @author Jan Becicka, Tor Norbye, Radek Matous
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {

    private final transient WhereUsedSupport usage;
    private WhereUsedSupport newElement;
    private final transient ChangeListener parent;

    /** Creates new form WhereUsedPanel */
    public WhereUsedPanel(String name, WhereUsedSupport e, ChangeListener parent) {
        setName(NbBundle.getMessage(WhereUsedPanel.class, "LBL_WhereUsed")); // NOI18N

        this.usage = e;
        this.parent = parent;
        initComponents();
        searchInComments.setEnabled(false);
        searchInComments.setVisible(false);
    }
    private boolean initialized = false;
    private String methodDeclaringSuperClass = null;
    private String methodDeclaringClass = null;

    String getMethodDeclaringClass() {
        return isMethodFromBaseClass() ? methodDeclaringSuperClass : methodDeclaringClass;
    }

    public void initialize() {
        if (initialized) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setupPanels();
            }
        });
        
        initialized = true;
    }

    
    private void setupPanels() {
        ModelElement elem = usage.getModelElement();
        assert elem != null;
        String name = usage.getName();
        String clsName = elem.getInScope() instanceof TypeScope ? elem.getInScope().getName() : null;
        String bKey = bundleKeyForLabel();
        final Set<Modifier> modifiers = usage.getModifiers();
        final String lblText;
        if (clsName != null) {
            lblText = NbBundle.getMessage(WhereUsedPanel.class, bKey, name, clsName);
        } else {
            lblText = NbBundle.getMessage(WhereUsedPanel.class, bKey, name);
        }

        remove(classesPanel);
        remove(methodsPanel);
        c_subclasses.setVisible(false);
        m_overriders.setVisible(false);
        label.setText(lblText);
        if (usage.getKind() == PhpElementKind.METHOD) {
            add(methodsPanel, BorderLayout.CENTER);
            methodsPanel.setVisible(true);
            m_usages.setVisible(!modifiers.contains(Modifier.STATIC));
            //m_overriders.setVisible(modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE));
            if (methodDeclaringSuperClass != null) {
                m_isBaseClass.setVisible(true);
                m_isBaseClass.setSelected(true);
            //Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
            } else {
                m_isBaseClass.setVisible(false);
                m_isBaseClass.setSelected(false);
            }
        } else if (usage.getKind() == PhpElementKind.CLASS) {
            add(classesPanel, BorderLayout.CENTER);            
            classesPanel.setVisible(true);
        } else {
            remove(classesPanel);
            remove(methodsPanel);
            c_subclasses.setVisible(false);
            m_usages.setVisible(false);
            c_usages.setVisible(false);
            c_directOnly.setVisible(false);
        }
        validate();
    }

    private String bundleKeyForLabel() {
        String bundleKey = null;
        switch (usage.getKind()) {
            case IFACE:
                bundleKey = "DSC_IfaceUsages";//NOI18N
                break;
            case CLASS:
                bundleKey = "DSC_ClassUsages";//NOI18N
                break;
            case VARIABLE:
                bundleKey = "DSC_VariableUsages"; // NOI18N

                break;
            case FUNCTION:
                bundleKey = "DSC_FuncUsages"; // NOI18N

                break;
            case FIELD:
                bundleKey = "DSC_FieldUsages"; //NOI18N

                break;
            case METHOD:
                bundleKey = "DSC_MethodUsages"; //NOI18N

                break;
            case CONSTANT:
                bundleKey = "DSC_ConstantUsages"; //NOI18N

                break;
            case TYPE_CONSTANT:
                bundleKey = "DSC_ClassConstantUsages"; //NOI18N

                break;
        }

        return bundleKey;
    }

    public WhereUsedSupport getBaseMethod() {
        return usage;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        methodsPanel = new javax.swing.JPanel();
        m_isBaseClass = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        m_overriders = new javax.swing.JCheckBox();
        m_usages = new javax.swing.JCheckBox();
        classesPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        c_subclasses = new javax.swing.JRadioButton();
        c_usages = new javax.swing.JRadioButton();
        c_directOnly = new javax.swing.JRadioButton();
        commentsPanel = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        searchInComments = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        methodsPanel.setLayout(new java.awt.GridBagLayout());

        m_isBaseClass.setSelected(true);
        m_isBaseClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_isBaseClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_isBaseClass, gridBagConstraints);
        m_isBaseClass.getAccessibleContext().setAccessibleDescription("null");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        methodsPanel.add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(m_overriders, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindOverridingMethods")); // NOI18N
        m_overriders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_overridersActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_overriders, gridBagConstraints);
        m_overriders.getAccessibleContext().setAccessibleDescription("null");

        m_usages.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(m_usages, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindUsages")); // NOI18N
        m_usages.setMargin(new java.awt.Insets(10, 2, 2, 2));
        m_usages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_usagesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_usages, gridBagConstraints);
        m_usages.getAccessibleContext().setAccessibleDescription("null");

        add(methodsPanel, java.awt.BorderLayout.CENTER);

        classesPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        classesPanel.add(jPanel2, gridBagConstraints);

        buttonGroup.add(c_subclasses);
        org.openide.awt.Mnemonics.setLocalizedText(c_subclasses, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindAllSubtypes")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_subclasses, gridBagConstraints);
        c_subclasses.getAccessibleContext().setAccessibleDescription("null");

        buttonGroup.add(c_usages);
        c_usages.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(c_usages, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindUsages")); // NOI18N
        c_usages.setMargin(new java.awt.Insets(4, 2, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_usages, gridBagConstraints);
        c_usages.getAccessibleContext().setAccessibleDescription("null");

        buttonGroup.add(c_directOnly);
        org.openide.awt.Mnemonics.setLocalizedText(c_directOnly, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindDirectSubtypesOnly")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_directOnly, gridBagConstraints);
        c_directOnly.getAccessibleContext().setAccessibleDescription("null");

        add(classesPanel, java.awt.BorderLayout.CENTER);

        commentsPanel.setLayout(new java.awt.BorderLayout());
        commentsPanel.add(label, java.awt.BorderLayout.NORTH);

        searchInComments.setSelected(((Boolean) NbPreferences.forModule(WhereUsedPanel.class).getBoolean("searchInComments.whereUsed", Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("LBL_SearchInComents")); // NOI18N
        searchInComments.setMargin(new java.awt.Insets(10, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });
        searchInComments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchInCommentsActionPerformed(evt);
            }
        });
        commentsPanel.add(searchInComments, java.awt.BorderLayout.CENTER);
        searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

        add(commentsPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for searchInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        NbPreferences.forModule(WhereUsedPanel.class).putBoolean("searchInComments.whereUsed", b);//GEN-LAST:event_searchInCommentsItemStateChanged
    }                                                 

    private void m_isBaseClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_isBaseClassActionPerformed
        parent.stateChanged(null);//GEN-LAST:event_m_isBaseClassActionPerformed
    }                                             

    private void m_overridersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_overridersActionPerformed
        parent.stateChanged(null);//GEN-LAST:event_m_overridersActionPerformed
    }                                            

    private void m_usagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_usagesActionPerformed
        parent.stateChanged(null);//GEN-LAST:event_m_usagesActionPerformed
    }                                        

private void searchInCommentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchInCommentsActionPerformed
// TODO add your handling code here://GEN-LAST:event_searchInCommentsActionPerformed
}                                                

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton c_directOnly;
    private javax.swing.JRadioButton c_subclasses;
    private javax.swing.JRadioButton c_usages;
    private javax.swing.JPanel classesPanel;
    private javax.swing.JPanel commentsPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel label;
    private javax.swing.JCheckBox m_isBaseClass;
    private javax.swing.JCheckBox m_overriders;
    private javax.swing.JCheckBox m_usages;
    private javax.swing.JPanel methodsPanel;
    private javax.swing.JCheckBox searchInComments;
    // End of variables declaration//GEN-END:variables

    public boolean isMethodFromBaseClass() {
        return m_isBaseClass.isSelected();
    }

    public boolean isMethodOverriders() {
        return m_overriders.isSelected();
    }

    public boolean isClassSubTypes() {
        return c_subclasses.isSelected();
    }

    public boolean isClassSubTypesDirectOnly() {
        return c_directOnly.isSelected();
    }

    public boolean isMethodFindUsages() {
        return m_usages.isSelected();
    }

    public boolean isClassFindUsages() {
        return c_usages.isSelected();
    }

    public 
    @Override
    Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(orig.width + 30, orig.height + 80);
    }

    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }

    public Component getComponent() {
        return this;
    }
}

