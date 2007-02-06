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
package org.netbeans.modules.java.editor.codegen.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.editor.codegen.DelegateMethodGenerator;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class DelegatePanel extends javax.swing.JPanel implements PropertyChangeListener {

    private JTextComponent component;
    private ElementSelectorPanel delegateSelector;
    private ElementSelectorPanel methodSelector;

    /** Creates new form DelegatePanel */
    public DelegatePanel(JTextComponent component, ElementNode.Description description) {
        this.component = component;
        initComponents();
        delegateSelector = new ElementSelectorPanel(description);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(delegateSelector, gridBagConstraints);
        delegateSelector.getExplorerManager().addPropertyChangeListener(this);
        
        methodSelector = new ElementSelectorPanel(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(methodSelector, gridBagConstraints);
        
        delegateLabel.setText(NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_field_select")); //NOI18N
        delegateLabel.setLabelFor(delegateSelector);
        methodLabel.setText(NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_method_select")); //NOI18N
        methodLabel.setLabelFor(methodSelector);
    }

    public ElementHandle<? extends Element> getDelegateField() {
        List<ElementHandle<? extends Element>> handles = delegateSelector.getSelectedElements();
        return handles.size() == 1 ? handles.get(0) : null;
    }

    public List<ElementHandle<? extends Element>> getDelegateMethods() {
        return ((ElementSelectorPanel)methodSelector).getSelectedElements();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        delegateLabel = new javax.swing.JLabel();
        methodLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(delegateLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 6, 12);
        add(methodLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    public void propertyChange(PropertyChangeEvent evt) {
        ElementHandle<? extends Element> handle = getDelegateField();
        methodSelector.setRootElement(handle == null ? null : DelegateMethodGenerator.getAvailableMethods(component, handle));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel delegateLabel;
    public javax.swing.JLabel methodLabel;
    // End of variables declaration//GEN-END:variables
}
