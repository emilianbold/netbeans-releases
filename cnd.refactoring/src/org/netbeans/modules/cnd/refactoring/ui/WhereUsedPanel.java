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
package org.netbeans.modules.cnd.refactoring.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;


/**
 * Based on the WhereUsedPanel in Java refactoring by Jan Becicka.
 * @author Vladimir Voskresensky
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {

    private final transient CsmObject origObject;
    private transient CsmObject refObject;

    private enum ElementKind {
        UNKNOWN, FUNCTION, CLASSIFIER, METHOD, VARIABLE, MACRO, FIELD, NAMESPACE
    
    }
    private ElementKind kind = ElementKind.UNKNOWN;
    
    private CsmObject newElement;
    private final transient ChangeListener parent;
    private String name;
    /** Creates new form WhereUsedPanel */
    public WhereUsedPanel(String name, CsmObject csmObject,ChangeListener parent) {
        setName(NbBundle.getMessage(WhereUsedPanel.class, "LBL_WhereUsed")); // NOI18N
        this.origObject = csmObject;
        this.parent = parent;
        this.name = name;
        initComponents();
    }
    
    private boolean initialized = false;
    private String methodDeclaringSuperClass = null;
    private String methodDeclaringClass = null;

    String getMethodDeclaringClass() {
        return isMethodFromBaseClass() ? methodDeclaringSuperClass : methodDeclaringClass;
    }

    public void initialize() {
        // method is called to make initialization of components out of AWT
        if (initialized) {
            return;
        }
        initFields();

        final String labelText = this.name;
//        String _isBaseClassText = null;
//        if (isFunLike()) {
//            if (element.getElement() != null) {
//                modif = element.getElement().getModifiers();
//            }
//            String methodName = element.getName();
//            String className = getClassName(element);
//            String displayClassName = RubyIndex.UNKNOWN_CLASS.equals(className) ? "&lt;Unknown&gt;" : className;
//            labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_MethodUsages", methodName, displayClassName);
//
//            methodDeclaringClass = className;
//
//            IndexedMethod method = RetoucheUtils.getOverridingMethod(element, info);
//            if (method != null) {
//                _isBaseClassText = new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_UsagesOfBaseClass")).format(new Object[]{methodDeclaringSuperClass = method.getIn()});
//                newElement = new RubyElementCtx(method, info);
//            }
//        } else if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.MODULE) {
//            labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ClassUsages", element.getName()); // NOI18N
//        } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
//            String methodName = element.getName();
//            String className = getClassName(element);
//            labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ConstructorUsages", methodName, className); // NOI18N
//        } else if (element.getKind() == ElementKind.FIELD) {
//            String fieldName = element.getName();
//            String className = getClassName(element);
//            labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_FieldUsages", fieldName, className); // NOI18N
//        } else {
//            labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_VariableUsages", element.getName()); // NOI18N
//        }
//
//        final Set<Modifier> modifiers = modif;
//        final String isBaseClassText = _isBaseClassText;

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                remove(classesPanel);
                remove(methodsPanel);
                // WARNING for now since this feature is not ready yet
                //label.setText(labelText);
                String combinedLabelText = "<html><b><font style=\"color: red\">WARNING: This feature is in development and inaccurate!</font><br><br>" + labelText + "</html>";
                label.setText(combinedLabelText);
//                if (element.getKind() == ElementKind.METHOD) {
//                    add(methodsPanel, BorderLayout.CENTER);
//                    methodsPanel.setVisible(true);
//                    m_usages.setVisible(!modifiers.contains(Modifier.STATIC));
//                    // TODO - worry about frozen?
//                    m_overriders.setVisible(modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE));
//                    if (methodDeclaringSuperClass != null) {
//                        m_isBaseClass.setVisible(true);
//                        m_isBaseClass.setSelected(true);
//                        Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
//                    } else {
//                        m_isBaseClass.setVisible(false);
//                        m_isBaseClass.setSelected(false);
//                    }
//                } else if ((element.getKind() == ElementKind.CLASS) || (element.getKind() == ElementKind.MODULE)) {
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
                validate();
            }
        });

        initialized = true;
    }

//    public RubyElementCtx getBaseMethod() {
//        return newElement;
//    }

    public CsmObject getBaseMethod() {
        return newElement == null ? origObject : newElement;
    }
    
    public void requestFocus() {
        super.requestFocus();
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
        m_isBaseClass.getAccessibleContext().setAccessibleDescription("null");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        methodsPanel.add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(m_overriders, "null");
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
        org.openide.awt.Mnemonics.setLocalizedText(m_usages, "null");
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

        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(label, java.awt.BorderLayout.NORTH);

        searchInComments.setSelected(false/*((Boolean) RefactoringModule.getOption("searchInComments.whereUsed", Boolean.FALSE)).booleanValue()*/);
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("LBL_SearchInComents")); // NOI18N
        searchInComments.setEnabled(false);
        searchInComments.setMargin(new java.awt.Insets(10, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });
        jPanel3.add(searchInComments, java.awt.BorderLayout.CENTER);
        searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

        add(jPanel3, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for searchInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
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

    @Override
    public Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(orig.width + 30, orig.height + 80);
    }

    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }

    public Component getComponent() {
        return this;
    }
    
    /*package*/ boolean isFunction() {
        return kind == ElementKind.FUNCTION;
    }
    
    /*package*/ boolean isClass() {
        return kind == ElementKind.CLASSIFIER;
    }
    
    /*package*/ boolean isMethod() {
        return kind == ElementKind.METHOD;
    }
    
    /*package*/ boolean isMacro() {
        return kind == ElementKind.MACRO;
    }
    
    /*package*/ boolean isVariable() {
        return kind == ElementKind.VARIABLE;
    }    

    /*package*/ boolean isField() {
        return kind == ElementKind.FIELD;
    }    

    /*package*/ boolean isNamespace() {
        return kind == ElementKind.NAMESPACE;
    }    

    /*package*/ boolean isFunLike() {
        return kind == ElementKind.FUNCTION || kind == ElementKind.METHOD;
    }
    
    private void initFields() {
        this.refObject = getReferencedElement(origObject);
        this.name = getSearchElementName(refObject, this.name);
        this.kind = getKind(refObject);
    }
    
    private CsmObject getReferencedElement(CsmObject csmObject) {
        if (csmObject instanceof CsmReference) {
            return getReferencedElement(((CsmReference)csmObject).getReferencedObject());
        } else {
            return csmObject;
        }
    }
    
    private String getSearchElementName(CsmObject csmObj, String defaultName) {
        String objName;
        if (CsmKindUtilities.isNamedElement(csmObj)) {
            objName = ((CsmNamedElement)csmObj).getName();
        } else {
            System.err.println("Unhandled name for object " + csmObj);
            objName = defaultName;
        }
        return objName;
    }   
    
    private ElementKind getKind(CsmObject csmObj) {
        if (CsmKindUtilities.isClassifier(csmObj)) {
            return ElementKind.CLASSIFIER;
        } else if (CsmKindUtilities.isMacro(csmObj)) {
            return ElementKind.MACRO;
        } else if (CsmKindUtilities.isMethod(csmObj)) {
            return ElementKind.METHOD;
        } else if (CsmKindUtilities.isFunction(csmObj)) {
            return ElementKind.FUNCTION;
        } else if (CsmKindUtilities.isNamespace(csmObj)) {
            return ElementKind.NAMESPACE;
        } else if (CsmKindUtilities.isField(csmObj)) {
            return ElementKind.FIELD;
        } else if (CsmKindUtilities.isVariable(csmObj)) {
            return ElementKind.VARIABLE;
        } else {
            System.err.println("Unhandled name for object " + csmObj);
            return ElementKind.UNKNOWN;
        }
    }
    
    private String getString(String key) {
        return NbBundle.getBundle(WhereUsedPanel.class).getString(key);
    }
    
    private String getFormattedString(String key, String value) {
        return NbBundle.getMessage(WhereUsedPanel.class, key, value);
    }    
}
