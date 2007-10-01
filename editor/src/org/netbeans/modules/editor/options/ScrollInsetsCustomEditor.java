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

import java.awt.Insets;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/** Custom editor for java.awt.Insets allowing to set per cent values
 *  as negative numbers.
 *
 * @author   Petr Nejedly
 * @author   Ian Formanek
 */
public class ScrollInsetsCustomEditor extends javax.swing.JPanel implements DocumentListener {


    static final long serialVersionUID =-1472891501739636852L;

    private ScrollInsetsEditor editor;
    private PropertyEnv env;

    /** Initializes the Form */
    public ScrollInsetsCustomEditor(ScrollInsetsEditor editor, PropertyEnv env) {
        initComponents ();
        
        this.editor = editor;
        this.env = env;
        
        Insets insets = (Insets)editor.getValue();

        if (insets == null) insets = new Insets( 0, 0, 0, 0 );

        getAccessibleContext ().setAccessibleDescription (getBundleString("ACSD_SICE")); // NOI18N
        topLabel.setDisplayedMnemonic (getBundleString("SICE_Top_Mnemonic").charAt(0)); // NOI18N
        bottomLabel.setDisplayedMnemonic (getBundleString("SICE_Bottom_Mnemonic").charAt(0)); // NOI18N
        leftLabel.setDisplayedMnemonic (getBundleString("SICE_Left_Mnemonic").charAt(0)); // NOI18N
        rightLabel.setDisplayedMnemonic (getBundleString("SICE_Right_Mnemonic").charAt(0)); // NOI18N
        topField.setText (int2percent (insets.top ));
        leftField.setText (int2percent (insets.left ));
        bottomField.setText (int2percent (insets.bottom));
        rightField.setText (int2percent (insets.right));
        topField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SICE_Top")); // NOI18N
        leftField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SICE_Left")); // NOI18N
        bottomField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SICE_Bottom")); // NOI18N
        rightField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SICE_Right")); // NOI18N
        
        /*
        jPanel2.setBorder (new javax.swing.border.CompoundBorder (
                               new javax.swing.border.TitledBorder (
                                   new javax.swing.border.EtchedBorder (),
                                   getBundleString().getString ("SICE_Insets")), // NOI18N
                               new javax.swing.border.EmptyBorder (new java.awt.Insets(12, 12, 11, 11))));
                               */

        setPreferredSize(new java.awt.Dimension(320, getPreferredSize().height));
        this.env.setState(PropertyEnv.STATE_VALID);
        topField.getDocument().addDocumentListener(this);
        leftField.getDocument().addDocumentListener(this);
        bottomField.getDocument().addDocumentListener(this);
        rightField.getDocument().addDocumentListener(this);
    }

    private String getBundleString(String s) {
        return NbBundle.getMessage(ScrollInsetsCustomEditor.class, s);
    }        
    
    
//    public Object getPropertyValue () throws IllegalStateException {
//        try {
//            return getValue();
//        } catch (NumberFormatException e) {
//            org.openide.DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message(
//                                                getBundleString("SIC_InvalidValue"), // NOI18N
//                                                NotifyDescriptor.ERROR_MESSAGE
//                                            ) );
//            throw new IllegalStateException();
//        }
//    }



    public static String int2percent( int i ) {
        if( i < 0 ) return( "" + (-i) + "%" ); // NOI18N
        else return( "" + i );
    }

    private int percent2int( String val ) throws NumberFormatException {
        val = val.trim();
        if( val.endsWith( "%" ) ) { // NOI18N
            return -Math.abs( Integer.parseInt( val.substring( 0, val.length() - 1 ) ) );
        } else {
            return Integer.parseInt( val );
        }
    }

    Insets getValue() throws NumberFormatException {
        int top = percent2int( topField.getText() );
        int left = percent2int( leftField.getText() );
        int bottom = percent2int( bottomField.getText() );
        int right = percent2int( rightField.getText() );
        return new Insets( top, left, bottom, right );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        topLabel = new javax.swing.JLabel();
        topField = new javax.swing.JTextField();
        leftLabel = new javax.swing.JLabel();
        leftField = new javax.swing.JTextField();
        bottomLabel = new javax.swing.JLabel();
        bottomField = new javax.swing.JTextField();
        rightLabel = new javax.swing.JLabel();
        rightField = new javax.swing.JTextField();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));

        setBorder(new javax.swing.border.EmptyBorder( new java.awt.Insets( 12, 12, 11, 11) ) );
        jPanel2.setLayout(new java.awt.GridBagLayout());

        topLabel.setLabelFor(topField);
        topLabel.setText(getBundleString("SICE_Top"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        jPanel2.add(topLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(topField, gridBagConstraints);

        leftLabel.setLabelFor(leftField);
        leftLabel.setText(getBundleString("SICE_Left"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        jPanel2.add(leftLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(leftField, gridBagConstraints);

        bottomLabel.setLabelFor(bottomField);
        bottomLabel.setText(getBundleString("SICE_Bottom"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        jPanel2.add(bottomLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(bottomField, gridBagConstraints);

        rightLabel.setLabelFor(rightField);
        rightLabel.setText(getBundleString("SICE_Right"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 12);
        jPanel2.add(rightLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(rightField, gridBagConstraints);

        add(jPanel2);

    }// </editor-fold>//GEN-END:initComponents

    public void insertUpdate(DocumentEvent e) {
        updateInsets();
    }

    public void removeUpdate(DocumentEvent e) {
        updateInsets();
    }

    public void changedUpdate(DocumentEvent e) {
        updateInsets();
    }

    private void updateInsets() {
        try {
            editor.setValue(getValue());
            env.setState(PropertyEnv.STATE_VALID);
        } catch (NumberFormatException e) {
            env.setState(PropertyEnv.STATE_INVALID);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bottomField;
    private javax.swing.JLabel bottomLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField leftField;
    private javax.swing.JLabel leftLabel;
    private javax.swing.JTextField rightField;
    private javax.swing.JLabel rightLabel;
    private javax.swing.JTextField topField;
    private javax.swing.JLabel topLabel;
    // End of variables declaration//GEN-END:variables

}
