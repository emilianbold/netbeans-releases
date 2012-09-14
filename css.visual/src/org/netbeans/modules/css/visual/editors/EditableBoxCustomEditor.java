/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual.editors;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.modules.css.model.api.semantic.box.BoxElement;
import org.netbeans.modules.css.model.api.semantic.box.BoxType;
import org.netbeans.modules.css.model.api.semantic.Edge;
import org.netbeans.modules.css.model.api.semantic.box.EditableBox;

/**
 *
 * @author marekfukala
 */
public class EditableBoxCustomEditor extends javax.swing.JPanel {

    private EditableBoxPropertyEditor editor;
    
    /**
     * Creates new form EditableBoxCustomEditor
     */
    public EditableBoxCustomEditor(EditableBoxPropertyEditor editor) {
        this.editor = editor;
        initComponents();
        
        setFocusTraversalPolicyProvider(true);
        setFocusTraversalPolicy(new FocusTraversalPolicy() {

            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt) {
                cmpnt = cmpnt.getParent(); //XXX this is likely not correct, using parent of some platform spefific classes is dangerous
                if(cmpnt == top) {
                    return right;
                } else if(cmpnt == right) {
                    return bottom;
                } else if(cmpnt == bottom) {
                    return left;
                } else if(cmpnt == left) {
                    return top;
                }
                return null;
            }

            @Override
            public Component getComponentBefore(Container cntnr, Component cmpnt) {
                cmpnt = cmpnt.getParent(); //XXX this is likely not correct, using parent of some platform spefific classes is dangerous
                if(cmpnt == top) {
                    return left;
                } else if(cmpnt == right) {
                    return top;
                } else if(cmpnt == bottom) {
                    return right;
                } else if(cmpnt == left) {
                    return bottom;
                }
                return null;
            }

            @Override
            public Component getFirstComponent(Container cntnr) {
                return top;
            }

            @Override
            public Component getLastComponent(Container cntnr) {
                return left;
            }

            @Override
            public Component getDefaultComponent(Container cntnr) {
                return top;
            }
        });
        
    }
    
    private ComboBoxModel modelFor(Edge e) {
        BoxElement mw = editor.editableBox.getEdge(e);
        String value = mw != null ? mw.asText() : "";
        
        Set<String> set = new TreeSet<String>();
        set.add(value);
//        set.add("auto");
        
        return new DefaultComboBoxModel(new Vector(set));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        top = new javax.swing.JComboBox();
        right = new javax.swing.JComboBox();
        bottom = new javax.swing.JComboBox();
        left = new javax.swing.JComboBox();

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 20, 0, 20, 0};
        layout.rowHeights = new int[] {0, 20, 0, 20, 0};
        setLayout(layout);

        top.setEditable(true);
        top.setModel(modelFor(Edge.TOP));
        top.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(top, gridBagConstraints);

        right.setEditable(true);
        right.setModel(modelFor(Edge.RIGHT));
        right.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(right, gridBagConstraints);

        bottom.setEditable(true);
        bottom.setModel(modelFor(Edge.BOTTOM));
        bottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottomActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(bottom, gridBagConstraints);

        left.setEditable(true);
        left.setModel(modelFor(Edge.LEFT));
        left.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(left, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void setEdge(Edge e) {
        JComboBox cb = getJComboBoxForEdge(e);
        String value = cb.getModel().getSelectedItem().toString();
        if(value.length() > 0) {
            EditableBox editableBox = editor.editableBox;
            BoxElement boxElement = editableBox.createElement(value);
            editor.editableBox.setEdge(e, boxElement);
        }
    }
    
    private void topActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topActionPerformed
        setEdge(Edge.TOP);
    }//GEN-LAST:event_topActionPerformed

    private void rightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightActionPerformed
        setEdge(Edge.RIGHT);
    }//GEN-LAST:event_rightActionPerformed

    private void bottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottomActionPerformed
        setEdge(Edge.BOTTOM);
    }//GEN-LAST:event_bottomActionPerformed

    private void leftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftActionPerformed
        setEdge(Edge.LEFT);
    }//GEN-LAST:event_leftActionPerformed

    private JComboBox getJComboBoxForEdge(Edge e) {
        switch(e) {
            case BOTTOM: 
                return bottom;
            case LEFT:
                return left;
            case TOP:
                return top;
            case RIGHT:
                return right;
        }
        return null;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox bottom;
    private javax.swing.JComboBox left;
    private javax.swing.JComboBox right;
    private javax.swing.JComboBox top;
    // End of variables declaration//GEN-END:variables
}
