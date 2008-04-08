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

package org.netbeans.modules.editor.options;

import java.awt.Dimension;
import org.openide.util.NbBundle;

/**
 * Input panel for pair of strings, one inline and one in editor
 *
 * @author  Petr Nejedly
 * @deprecated Without any replacement.
 */
public class AbbrevInputPanel extends javax.swing.JPanel {

    /** Creates new form AbbrevsInputPanel */
    public AbbrevInputPanel() {
        initComponents ();

        getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_AIP")); // NOI18N
        abbrevField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_AIP_Abbrev")); // NOI18N
        expandTextArea.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_AIP_Expand")); // NOI18N
        
        Dimension dim = getPreferredSize();
        dim.width = 4*dim.width;
        dim.height = 4*dim.height;
        setPreferredSize( dim );
    }
    
    private String getBundleString(String s) {
        return NbBundle.getMessage(AbbrevInputPanel.class, s);
    }        
    
    public void requestFocus(){
        abbrevField.requestFocus();
    }

    public void setAbbrev( String[] abbrev ) {
        abbrevField.setText( abbrev[0] );
        expandTextArea.setText( abbrev[1] );
    }

    public String[] getAbbrev() {
        String[] retVal = { abbrevField.getText(), expandTextArea.getText() };
        return retVal;
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        abbrevLabel = new javax.swing.JLabel();
        abbrevField = new javax.swing.JTextField();
        expandLabel = new javax.swing.JLabel();
        expandScrollPane = new javax.swing.JScrollPane();
        expandTextArea = new javax.swing.JTextArea();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.GridBagLayout());

        abbrevLabel.setLabelFor(abbrevField);
        org.openide.awt.Mnemonics.setLocalizedText(abbrevLabel, getBundleString( "AIP_Abbrev" ));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(abbrevLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(abbrevField, gridBagConstraints);

        expandLabel.setLabelFor(expandTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(expandLabel, getBundleString( "AIP_Expand" ));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 12);
        add(expandLabel, gridBagConstraints);

        expandScrollPane.setViewportView(expandTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(expandScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField abbrevField;
    private javax.swing.JLabel abbrevLabel;
    private javax.swing.JLabel expandLabel;
    private javax.swing.JScrollPane expandScrollPane;
    private javax.swing.JTextArea expandTextArea;
    // End of variables declaration//GEN-END:variables

}
