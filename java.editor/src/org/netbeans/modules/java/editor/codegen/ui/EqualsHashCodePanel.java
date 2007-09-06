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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.editor.codegen.EqualsHashCodeGenerator;
import org.openide.util.NbBundle;

/**
 *
 * @author  Dusan Balek
 */
public class EqualsHashCodePanel extends JPanel {

    private ElementSelectorPanel equalsSelector;
    private ElementSelectorPanel hashCodeSelector;
    
    private JLabel equalsLabel;
    private JLabel hashCodeLabel;

    /** Creates new form EqualsHashCodePanel */
    public EqualsHashCodePanel(ElementNode.Description description, boolean generateEquals, boolean generateHashCode) {
        assert generateEquals || generateHashCode;
        
        initComponents();
        
        GridBagConstraints gridBagConstraints;

        if( generateEquals ) {
            equalsLabel = new JLabel(NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_equals_select")); //NOI18N

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
            add(equalsLabel, gridBagConstraints);

            equalsSelector = new ElementSelectorPanel(description, false);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
            add(equalsSelector, gridBagConstraints);
        
            equalsLabel.setLabelFor(equalsSelector);
        }

        if( generateHashCode ) {
            hashCodeLabel = new JLabel(NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_hashcode_select")); //NOI18N

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.insets = new java.awt.Insets(12, generateEquals ? 0 : 12, 6, 12);
            add(hashCodeLabel, gridBagConstraints);

            hashCodeSelector = new ElementSelectorPanel( ElementNode.Description.deepCopy(description), false);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, generateEquals ? 0 : 12, 0, 12);
            add(hashCodeSelector, gridBagConstraints);
        
            hashCodeLabel.setLabelFor(hashCodeSelector);
        }
	
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EqualsHashCodeGenerator.class, "A11Y_Generate_EqualsHashCode"));
    }
    
    
    public List<ElementHandle<? extends Element>> getEqualsVariables() {
        if( null == equalsSelector )
            return null;
        return ((ElementSelectorPanel)equalsSelector).getSelectedElements();
    }

    public List<ElementHandle<? extends Element>> getHashCodeVariables() {
        if( null == hashCodeSelector )
            return null;
        return ((ElementSelectorPanel)hashCodeSelector).getSelectedElements();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
