/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.xml.refactoring.ui;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;

/**
 * @author  Jan Becicka
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {
    public static final long serialVersionUID = 1L;

//    private final transient RefObject element;
    //private final transient ParametersPanel parent;
    private final transient ChangeListener parent;
    private transient Referenceable ref;

    /** Creates new form RenamePanelName */
//    public WhereUsedPanel(String name, RefObject e, ParametersPanel parent) {
//        setName(NbBundle.getMessage(WhereUsedPanel.class,"LBL_WhereUsed")); // NOI18N
//        this.element = e;
//        this.parent = parent;
//        initComponents();
//        parent.setPreviewEnabled(false);
//    }
    
    public WhereUsedPanel(Referenceable ref, ChangeListener parent) {
        this.parent = parent;
        this.ref = ref;
    }

    private boolean initialized = false;
    private String methodDeclaringSuperClass = null;
    private String methodDeclaringClass = null;
    
    String getMethodDeclaringClass() {
        return isMethodFromBaseClass() ? methodDeclaringSuperClass : methodDeclaringClass;
    }
    
    public void initialize() {
//        String m_isBaseClassText = null;
//        if (initialized) return;
//        final String labelText;
//        int modif = 0;
//        
//        JavaModel.getJavaRepository().beginTrans(true); 
//        try {
//            if (element instanceof Method) {
//                Method method = (Method) element;
//                JavaModel.setClassPath(method.getResource());
//                modif = method.getModifiers();
//                labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_MethodUsages", getHeader(method), getSimpleName(method.getDeclaringClass())); // NOI18N
//
//                methodDeclaringClass = getSimpleName(method.getDeclaringClass());
//                Collection overridens = JavaModelUtil.getOverriddenMethods(method);
//                if (!overridens.isEmpty()) {
//                    m_isBaseClassText =
//                        new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_UsagesOfBaseClass")).format(
//                            new Object[] {
//                                methodDeclaringSuperClass = getSimpleName(((Method) overridens.iterator().next()).getDeclaringClass())
//                            }
//                        );
//                } 
//            } else if (element instanceof JavaClass) {
//                labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ClassUsages", ((JavaClass) element).getSimpleName()); // NOI18N
//            } else if (element instanceof Constructor) {
//                labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ConstructorUsages", getHeader((CallableFeature)element), getSimpleName(((Constructor) element).getDeclaringClass())); // NOI18N
//            } else if (element instanceof Field) {
//                labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_FieldUsages", ((NamedElement) element).getName(), getSimpleName(((Field) element).getDeclaringClass())); // NOI18N
//            } else if (element instanceof JavaPackage) {
//                labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_PackageUsages", ((NamedElement) element).getName()); // NOI18N
//            } else if (element instanceof NamedElement) {
//                labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_VariableUsages", ((NamedElement) element).getName()); // NOI18N
//            } else {
//                labelText = null;
//            }
//        } finally {
//            JavaModel.getJavaRepository().endTrans();
//        }
//        
//        final int modifiers = modif;
//        final String isBaseClassText = m_isBaseClassText;
//        
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                remove(classesPanel);
//                remove(methodsPanel);
//                label.setText(labelText);
//                if (element instanceof Method) {
//                    add(methodsPanel, BorderLayout.CENTER);
//                    methodsPanel.setVisible(true);
//                    m_usages.setVisible(!Modifier.isStatic(modifiers));
//                    m_overriders.setVisible(! (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers)));
//                    if (methodDeclaringSuperClass != null ) {
//                        m_isBaseClass.setVisible(true);
//                        m_isBaseClass.setSelected(true);
//                        Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
//                    } else {
//                        m_isBaseClass.setVisible(false);
//                        m_isBaseClass.setSelected(false);
//                    }
//                } else if (element instanceof JavaClass) {
//                    add(classesPanel, BorderLayout.CENTER);
//                    classesPanel.setVisible(true);
//                } else {
//                    remove(classesPanel);
//                    remove(methodsPanel);
//                    c_subclasses.setVisible(false);
//                    m_usages.setVisible(false);
//                    c_usages.setVisible(false);
//                    c_directOnly.setVisible(false);
//                }
//                validate();
//            }
//        });
//        initialized = true;
    }
    
//    private String getSimpleName(ClassDefinition clazz) {
//        if (clazz instanceof JavaClass) {
//            return ((JavaClass) clazz).getSimpleName();
//        } else {
//            return NbBundle.getMessage(WhereUsedPanel.class, "LBL_AnonymousClass"); // NOI18N
//        }
//    }
//    
//    private String getHeader(CallableFeature call) {
//        if (((CallableFeatureImpl) call).getParser() == null) {
//            if (call instanceof Method) {
//                return ((Method) call).getName();
//            } else if (call instanceof Constructor) {
//                return getSimpleName(call.getDeclaringClass()); 
//            }
//            return "";
//        }
//        int s = ((MetadataElement) call).getPartStartOffset(ElementPartKindEnum.HEADER);
//        int element = ((MetadataElement) call).getPartEndOffset(ElementPartKindEnum.HEADER);
//        String result =  call.getResource().getSourceText().substring(s,element);
//        if (result.length() > 50) {
//            result = result.substring(0,49) + "..."; // NOI18N
//        }
//        return CheckUtils.htmlize(result);
//    }
//    
//    public void requestFocus() {
//        super.requestFocus();
//    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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
        jPanel3 = new javax.swing.JPanel();
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
        m_isBaseClass.getAccessibleContext().setAccessibleDescription(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        methodsPanel.add(jPanel1, gridBagConstraints);

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
        m_overriders.getAccessibleContext().setAccessibleDescription(null);

        m_usages.setSelected(true);
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
        m_usages.getAccessibleContext().setAccessibleDescription(null);

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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_subclasses, gridBagConstraints);
        c_subclasses.getAccessibleContext().setAccessibleDescription(null);

        buttonGroup.add(c_usages);
        c_usages.setSelected(true);
        c_usages.setMargin(new java.awt.Insets(4, 2, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_usages, gridBagConstraints);
        c_usages.getAccessibleContext().setAccessibleDescription(null);

        buttonGroup.add(c_directOnly);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_directOnly, gridBagConstraints);
        c_directOnly.getAccessibleContext().setAccessibleDescription(null);

        add(classesPanel, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel3.add(label, java.awt.BorderLayout.NORTH);

        searchInComments.setMargin(new java.awt.Insets(10, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });

        jPanel3.add(searchInComments, java.awt.BorderLayout.SOUTH);
        searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

        add(jPanel3, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents

    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for searchInComments check-box.
        // The value is persisted and then used as default in next IDE run.
//        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
//        RefactoringModule.setOption("searchInComments.whereUsed", b);
    }//GEN-LAST:event_searchInCommentsItemStateChanged

    private void m_isBaseClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_isBaseClassActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_isBaseClassActionPerformed

    private void m_overridersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_overridersActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_overridersActionPerformed

    private void m_usagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_usagesActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_usagesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton c_directOnly;
    private javax.swing.JRadioButton c_subclasses;
    private javax.swing.JRadioButton c_usages;
    private javax.swing.JPanel classesPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
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
    
    public Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(orig.width + 30 , orig.height + 30);
    }
    
    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }
    
    public Component getComponent(){
        return this;
    }
                
   
}

