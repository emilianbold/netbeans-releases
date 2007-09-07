/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.refactoring.java.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.ui.WhereUsedPanel.Scope;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import javax.lang.model.element.Modifier;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.Project;


/**
 * @author  Jan Becicka
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {

    private final transient TreePathHandle element;
    private  TreePathHandle newElement;
    private final transient ChangeListener parent;
    private static final int MAX_NAME = 50;
    
    /** Creates new form WhereUsedPanel */
    public WhereUsedPanel(String name, TreePathHandle e, ChangeListener parent) {
        setName(NbBundle.getMessage(WhereUsedPanel.class,"LBL_WhereUsed")); // NOI18N
        this.element = e;
        this.parent = parent;
        initComponents();
        //parent.setPreviewEnabled(false);
    }
    
    public enum Scope {
        ALL,
        CURRENT
    }
    
    public Scope getScope() {
        if (scope.getSelectedIndex()==1)
            return Scope.CURRENT;
        return Scope.ALL;
    }

    private boolean initialized = false;
    private String methodDeclaringSuperClass = null;
    private String methodDeclaringClass = null;
    
    String getMethodDeclaringClass() {
        return isMethodFromBaseClass() ? methodDeclaringSuperClass : methodDeclaringClass;
    }
    
    private Collection getOverriddenMethods(ExecutableElement m, CompilationInfo info) {
        return RetoucheUtils.getOverridenMethods(m, info);
    }
    
    public void initialize() {
        if (initialized) return;
        JavaSource source = JavaSource.forFileObject(element.getFileObject());
        Project p = FileOwnerQuery.getOwner(element.getFileObject());
        final JLabel currentProject;
        final JLabel allProjects;
        if (p!=null) {
            ProjectInformation pi = ProjectUtils.getInformation(FileOwnerQuery.getOwner(element.getFileObject()));
            currentProject = new JLabel(pi.getDisplayName(), pi.getIcon(), SwingConstants.LEFT);
            allProjects = new JLabel(NbBundle.getMessage(WhereUsedPanel.class,"LBL_AllProjects"), pi.getIcon(), SwingConstants.LEFT);
        } else {
            currentProject = null;
            allProjects = null;
        }
        CancellableTask<CompilationController> task =new CancellableTask<CompilationController>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                String m_isBaseClassText = null;
                final String labelText;
                Set<Modifier> modif = new HashSet<Modifier>();
                
                final Element element = WhereUsedPanel.this.element.resolveElement(info);
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) element;
                    modif = method.getModifiers();
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_MethodUsages", getHeader(method, info), getSimpleName(method.getEnclosingElement())); // NOI18N
                    
                    methodDeclaringClass = getSimpleName(method.getEnclosingElement());
                    Collection overridens = getOverriddenMethods(method, info);
                    if (!overridens.isEmpty()) {
                        ExecutableElement el = (ExecutableElement) overridens.iterator().next();                        
                        assert el!=null;
                        m_isBaseClassText =
                                new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_UsagesOfBaseClass")).format(
                                new Object[] {
                            methodDeclaringSuperClass = getSimpleName((el).getEnclosingElement())
                        }
                        );
                        newElement = TreePathHandle.create(el, info);

                    }
                } else if (element.getKind().isClass() || element.getKind().isInterface()) {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ClassUsages", element.getSimpleName()); // NOI18N
                } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ConstructorUsages", getHeader(element), getSimpleName(element.getEnclosingElement())); // NOI18N
                } else if (element.getKind().isField()) {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_FieldUsages", element.getSimpleName(), getSimpleName(element.getEnclosingElement())); // NOI18N
                } else if (element.getKind() == ElementKind.PACKAGE) {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_PackageUsages", element.getSimpleName()); // NOI18N
                } else {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_VariableUsages", element.getSimpleName()); // NOI18N
                }
                
                final Set<Modifier> modifiers = modif;
                final String isBaseClassText = m_isBaseClassText;
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        remove(classesPanel);
                        remove(methodsPanel);
                        label.setText(labelText);
                        if (element instanceof ExecutableElement) {
                            add(methodsPanel, BorderLayout.CENTER);
                            methodsPanel.setVisible(true);
                            m_usages.setVisible(!modifiers.contains(Modifier.STATIC));
                            m_overriders.setVisible(! (modifiers.contains(Modifier.FINAL) || modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE)));
                            if (methodDeclaringSuperClass != null ) {
                                m_isBaseClass.setVisible(true);
                                m_isBaseClass.setSelected(true);
                                Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
                            } else {
                                m_isBaseClass.setVisible(false);
                                m_isBaseClass.setSelected(false);
                            }
                        } else if ((element.getKind() == ElementKind.CLASS) || (element.getKind() == ElementKind.INTERFACE)) {
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
                        if (currentProject!=null) {
                            scope.setModel(new DefaultComboBoxModel(new Object[]{allProjects, currentProject }));
                            int defaultItem = (Integer) RefactoringModule.getOption("whereUsed.scope", 0);
                            scope.setSelectedIndex(defaultItem);
                            scope.setRenderer(new JLabelRenderer());
                        } else {
                            scopePanel.setVisible(false);
                        }
                        validate();
                    }
                });
            }};
            try {
                source.runUserActionTask(task, true);
            } catch (IOException ioe) {
                throw (RuntimeException) new RuntimeException().initCause(ioe);
            }
            initialized = true;
    }
    private static class JLabelRenderer extends JLabel implements ListCellRenderer {
        public JLabelRenderer () {
            setOpaque(true);
        }
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if ( value != null ) {
                setText(((JLabel)value).getText());
                setIcon(((JLabel)value).getIcon());
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    
    private String getSimpleName(Element clazz) {
        return clazz.getSimpleName().toString();
        //return NbBundle.getMessage(WhereUsedPanel.class, "LBL_AnonymousClass"); // NOI18N
    }
    
    private String getHeader(ExecutableElement call, CompilationInfo info) {
        String result = UiUtils.getHeader(call,info,UiUtils.PrintPart.NAME + UiUtils.PrintPart.PARAMETERS);
        if (result.length() > MAX_NAME) {
            result = result.substring(0,MAX_NAME-1) + "..."; // NOI18N
        }
        return RetoucheUtils.htmlize(result);
    }
    
    public TreePathHandle getBaseMethod() {
        return newElement;
    }
    
    public void requestFocus() {
        super.requestFocus();
    }
    
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
        commentsPanel = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        searchInComments = new javax.swing.JCheckBox();
        scopePanel = new javax.swing.JPanel();
        scopeLabel = new javax.swing.JLabel();
        scope = new javax.swing.JComboBox();

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
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle"); // NOI18N
        m_isBaseClass.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_isBaseClass")); // NOI18N

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
        m_overriders.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_overriders")); // NOI18N

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
        m_usages.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_usages")); // NOI18N

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
        c_subclasses.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_subclasses")); // NOI18N

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
        c_usages.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_usages")); // NOI18N

        buttonGroup.add(c_directOnly);
        org.openide.awt.Mnemonics.setLocalizedText(c_directOnly, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindDirectSubtypesOnly")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_directOnly, gridBagConstraints);
        c_directOnly.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_directOnly")); // NOI18N

        add(classesPanel, java.awt.BorderLayout.CENTER);

        commentsPanel.setLayout(new java.awt.BorderLayout());
        commentsPanel.add(label, java.awt.BorderLayout.NORTH);

        searchInComments.setSelected(((Boolean) RefactoringModule.getOption("searchInComments.whereUsed", Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("LBL_SearchInComents")); // NOI18N
        searchInComments.setMargin(new java.awt.Insets(10, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });
        commentsPanel.add(searchInComments, java.awt.BorderLayout.SOUTH);
        searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

        add(commentsPanel, java.awt.BorderLayout.NORTH);

        scopeLabel.setText(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_Scope")); // NOI18N

        scope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scopeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout scopePanelLayout = new org.jdesktop.layout.GroupLayout(scopePanel);
        scopePanel.setLayout(scopePanelLayout);
        scopePanelLayout.setHorizontalGroup(
            scopePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scopePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(scopeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scope, 0, 288, Short.MAX_VALUE)
                .addContainerGap())
        );
        scopePanelLayout.setVerticalGroup(
            scopePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(scopeLabel)
            .add(scope, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
        );

        add(scopePanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

private void scopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scopeActionPerformed
    RefactoringModule.setOption("whereUsed.scope", scope.getSelectedIndex());
}//GEN-LAST:event_scopeActionPerformed

    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for searchInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption("searchInComments.whereUsed", b);
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
    private javax.swing.JPanel commentsPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel label;
    private javax.swing.JCheckBox m_isBaseClass;
    private javax.swing.JCheckBox m_overriders;
    private javax.swing.JCheckBox m_usages;
    private javax.swing.JPanel methodsPanel;
    private javax.swing.JComboBox scope;
    private javax.swing.JLabel scopeLabel;
    private javax.swing.JPanel scopePanel;
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

    private Object getHeader(javax.lang.model.element.Element element) {
        return element.toString();
    }

    public Component getComponent() {
        return this;
    }
}

