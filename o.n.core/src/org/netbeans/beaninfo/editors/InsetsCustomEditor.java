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

package org.netbeans.beaninfo.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.netbeans.core.UIExceptions;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
*
* @author   Ian Formanek
* @version  1.00, 01 Sep 1998
*/
public class InsetsCustomEditor extends javax.swing.JPanel implements PropertyChangeListener, KeyListener {
    static final long serialVersionUID =-1472891501739636852L;
   
    //XXX this is just a copy of RectangleEditor with the fields and value 
    //type changed.  A proper solution would be have an equivalent of 
    //ArrayOfIntSupport for custom editors, which would just produce the
    //required number of fields
    
    private HashMap<JTextField,JLabel> labelMap = new HashMap<JTextField,JLabel>();
    private PropertyEnv env;
    /** Initializes the Form */
    public InsetsCustomEditor(InsetsEditor editor, PropertyEnv env) {
        this.env=env;
        this.env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        this.env.addPropertyChangeListener(this);

        initComponents ();
        this.editor = editor;
        Insets insets = (Insets)editor.getValue ();
        if (insets == null) insets = new Insets (0, 0, 0, 0);
        xField.setText (Integer.toString(insets.top)); // NOI18N
        yField.setText (Integer.toString(insets.left)); // NOI18N
        widthField.setText (Integer.toString(insets.bottom)); // NOI18N
        heightField.setText (Integer.toString(insets.right)); // NOI18N

        setBorder (new javax.swing.border.EmptyBorder (new Insets(5, 5, 5, 5)));
        jPanel2.setBorder (new javax.swing.border.CompoundBorder (
                               new javax.swing.border.TitledBorder (
                                   new javax.swing.border.EtchedBorder (),
                                   " " + NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Insets") + " "),
                               new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));

        Mnemonics.setLocalizedText(xLabel, NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Top"));
        Mnemonics.setLocalizedText(yLabel, NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Left"));
        Mnemonics.setLocalizedText(widthLabel, NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Bottom"));
        Mnemonics.setLocalizedText(heightLabel, NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Right"));

        xLabel.setLabelFor(xField);
        yLabel.setLabelFor(yField);
        widthLabel.setLabelFor(widthField);
        heightLabel.setLabelFor(heightField);


        xField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CTL_Top"));
        yField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CTL_Left"));
        widthField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CTL_Bottom"));
        heightField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CTL_Right"));
        
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CustomRectangleEditor"));

        labelMap.put(widthField,widthLabel);
        labelMap.put(xField,xLabel);
        labelMap.put(yField,yLabel);
        labelMap.put(heightField,heightLabel);
    }

    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (280, 160);
    }

    private Object getPropertyValue () throws IllegalStateException {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            int width = Integer.parseInt (widthField.getText ());
            int height = Integer.parseInt (heightField.getText ());
//            if ((x < 0) || (y < 0) || (width < 0) || (height < 0)) {
//                IllegalStateException ise = new IllegalStateException();
//                UIExceptions.annotateUser(ise, null,
//                                         NbBundle.getMessage(InsetsCustomEditor.class,
//                                                             "CTL_NegativeSize"),
//                                         null, null);
//                throw ise;
//            }
            return new Insets (x, y, width, height);
        } catch (NumberFormatException e) {
            IllegalStateException ise = new IllegalStateException();
            UIExceptions.annotateUser(ise, null,
                                     NbBundle.getMessage(InsetsCustomEditor.class,
                                                         "CTL_InvalidValue"),
                                     null, null);
            throw ise;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }
    

    private void initComponents () {
        setLayout (new java.awt.BorderLayout ());

        jPanel2 = new javax.swing.JPanel ();
        jPanel2.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        xLabel = new javax.swing.JLabel ();
        xLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (xLabel, gridBagConstraints1);

        xField = new javax.swing.JTextField ();
        xField.addKeyListener (this);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (xField, gridBagConstraints1);

        yLabel = new javax.swing.JLabel ();
        yLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (yLabel, gridBagConstraints1);

        yField = new javax.swing.JTextField ();
        yField.addKeyListener(this);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (yField, gridBagConstraints1);

        widthLabel = new javax.swing.JLabel ();
        widthLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (widthLabel, gridBagConstraints1);

        widthField = new javax.swing.JTextField ();
        widthField.addKeyListener(this);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (widthField, gridBagConstraints1);

        heightLabel = new javax.swing.JLabel ();
        heightLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (heightLabel, gridBagConstraints1);

        heightField = new javax.swing.JTextField ();
        heightField.addKeyListener(this);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (heightField, gridBagConstraints1);


        add (jPanel2, "Center"); // NOI18N

    }


    private void updateRectangle () {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            int width = Integer.parseInt (widthField.getText ());
            int height = Integer.parseInt (heightField.getText ());
            editor.setValue (new Insets (x, y, width, height));
        } catch (NumberFormatException e) {
            // [PENDING beep]
        }
    }

    public void keyPressed(java.awt.event.KeyEvent e) {
    }    

    public void keyReleased(java.awt.event.KeyEvent e) {
        if (checkValues()) {
            updateRectangle();
        }
    }
    
    public void keyTyped(java.awt.event.KeyEvent e) {
    }
    
    private boolean checkValues() {
        Component[] c = jPanel2.getComponents();
        boolean valid=true;
        for (int i=0; i < c.length; i++) {
            if (c[i] instanceof JTextField) {
                valid &= validFor((JTextField) c[i]);
            }
        }
        if (env != null) {
           env.setState(valid ? env.STATE_VALID : env.STATE_INVALID);
        }
        return valid;
    }
    
    private boolean validFor(JTextField c) {
        String s = c.getText().trim();
        try {
            Integer.parseInt(s);
            handleValid(c);
            return true;
        } catch (NumberFormatException e) {
            handleInvalid(c);
            return false;
        }
    }
    
    private void handleInvalid(JTextField c) {
        c.setForeground(getErrorColor());
        labelMap.get(c).setForeground(getErrorColor());
    }
    
    private void handleValid(JTextField c) {
        c.setForeground(getForeground());
        labelMap.get(c).setForeground(getForeground());
    }
    
    private Color getErrorColor() {
        Color c=UIManager.getColor("nb.errorForeground");
        if (c == null) {
            c = Color.RED;
        }
        return c;
    }
    
    // Variables declaration - do not modify
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel xLabel;
    private javax.swing.JTextField xField;
    private javax.swing.JLabel yLabel;
    private javax.swing.JTextField yField;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField widthField;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JTextField heightField;
    // End of variables declaration

    private InsetsEditor editor;

}

