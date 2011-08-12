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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.common;

import com.sun.javacard.AID;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import javax.swing.text.JTextComponent;
import org.openide.util.HelpCtx;
public final class AIDPanel extends javax.swing.JPanel implements DocumentListener, FocusListener, ActionListener {
    private final ChangeSupport supp = new ChangeSupport(this);
    public AIDPanel() {
        initComponents();
        Mnemonics.setLocalizedText(generateButton, generateButton.getText());
        GuiUtils.filterNonHexadecimalKeys(pixField);
        GuiUtils.filterNonHexadecimalKeys(ridField);
        pixField.getDocument().addDocumentListener(this);
        ridField.getDocument().addDocumentListener(this);
        getAID();
        pixField.addFocusListener(this);
        ridField.addFocusListener(this);
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.EditAppletAID"); //NOI18N
    }

    public void removeChangeListener(ChangeListener listener) {
        supp.removeChangeListener(listener);
    }

    private void change() {
        fireChange();
    }

    boolean firing;
    private void fireChange() {
        if (firing) {
            return;
        }
        firing = true;
        try {
            supp.fireChange();
        } finally {
            firing = false;
        }
    }

    public void addChangeListener(ChangeListener listener) {
        supp.addChangeListener(listener);
    }

    @Override
    public void setEnabled(boolean val) {
        super.setEnabled(val);
        for (Component c : getComponents()) {
            c.setEnabled(val);
        }
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        pixField.requestFocus();
    }

    @Override
    public void setBackground (Color c) {
        super.setBackground(c);
        if (spacer != null) { //superclass constructor
            spacer.setBackground(c);
        }
    }

    private String pkg = Utils.generateRandomPackageName(); //NOI18N
    private String clazz = "MyApplet"; //NOI18N
    public void setClassFqn(String clazz) {
        int ix = clazz.lastIndexOf('.'); //NOI18N
        if (ix > 0 && ix != clazz.length() - 1) {
            this.clazz = clazz.substring(ix + 1);
            this.pkg = clazz.substring(0, ix);
        } else {
            this.clazz = clazz;
            //Need to do something...
            this.pkg = clazz;
        }
    }

    public void setAID (AID aid) {
        if (aid == null) {
            ridField.setText (Utils.getDefaultRIDasString());
            pixField.setText("");
        } else {
            pixField.setText(aid.getPixAsString());
            ridField.setText(aid.getRidAsString());
        }
        //refresh problem
        getAID();
    }

    public AID getAID() {
        String prob = this.problem;
        try {
            String aidString = "//aid/" + ridField.getText() + '/' + pixField.getText();
            try {
                AID result = AID.parse(aidString);
                problem = null;
                return result;
            } catch (IllegalArgumentException e) {
                //All exception messages in AID are localized
                problem = e.getMessage();
                return null;
            }
        } finally {
            if (prob == null ? this.problem != null : !prob.equals(this.problem)) { //equality test ok
                fireChange();
            }
        }
    }

    private String problem;
    public String getProblem() {
        return problem;
    }

    public void setTitle (String title) {
        titleLabel.setText(title);
        getAccessibleContext().setAccessibleName(title);
    }

    public void setGenerateButtonVisible(boolean val) {
        generateButton.setVisible(val);
        invalidate();
        revalidate();
        repaint();
    }

    @Override
    public void addFocusListener (FocusListener fl) {
        super.addFocusListener(fl);
        //Could be called by L&F in superclass constructor, so null
        //check fields
        if (ridField != null) {
            ridField.addFocusListener(fl);
        }
        if (pixField != null) {
            pixField.addFocusListener(fl);
        }
    }

    @Override
    public void removeFocusListener (FocusListener fl) {
        super.removeFocusListener(fl);
        if (ridField != null) {
            ridField.removeFocusListener(fl);
        }
        if (pixField != null) {
            pixField.removeFocusListener(fl);
        }
    }

    @Override
    public void addMouseListener (MouseListener ml) {
        super.addMouseListener(ml);
        if (ridField != null) {
            ridField.addMouseListener(ml);
        }
        if (pixField != null) {
            pixField.addMouseListener(ml);
        }
    }

    @Override
    public void removeMouseListener (MouseListener ml) {
        super.removeMouseListener(ml);
        if (ridField != null) {
            ridField.removeMouseListener(ml);
        }
        if (pixField != null) {
            pixField.removeMouseListener(ml);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        aidLabel = new javax.swing.JLabel();
        ridField = new javax.swing.JTextField();
        slash = new javax.swing.JLabel();
        pixField = new javax.swing.JTextField();
        ridHint = new javax.swing.JLabel();
        pixHint = new javax.swing.JLabel();
        generateButton = new javax.swing.JButton();
        titleLabel = new javax.swing.JLabel();
        spacer = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setToolTipText(org.openide.util.NbBundle.getMessage(AIDPanel.class, "AIDPanel.toolTipText", new Object[] {})); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        aidLabel.setText(NbBundle.getMessage(AIDPanel.class, "AIDPanel.aidLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(aidLabel, gridBagConstraints);

        ridField.setText(NbBundle.getMessage(AIDPanel.class, "AIDPanel.ridField.text")); // NOI18N
        ridField.setToolTipText(org.openide.util.NbBundle.getMessage(AIDPanel.class, "AIDPanel.ridField.toolTipText", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        add(ridField, gridBagConstraints);

        slash.setText(NbBundle.getMessage(AIDPanel.class, "AIDPanel.slash.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(slash, gridBagConstraints);

        pixField.setText(NbBundle.getMessage(AIDPanel.class, "AIDPanel.pixField.text")); // NOI18N
        pixField.setToolTipText(org.openide.util.NbBundle.getMessage(AIDPanel.class, "AIDPanel.pixField.toolTipText", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        add(pixField, gridBagConstraints);

        ridHint.setFont(ridHint.getFont().deriveFont((ridHint.getFont().getStyle() | java.awt.Font.ITALIC)));
        ridHint.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ridHint.setText(NbBundle.getMessage(AIDPanel.class, "AIDPanel.ridHint.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 3;
        add(ridHint, gridBagConstraints);

        pixHint.setFont(pixHint.getFont().deriveFont((pixHint.getFont().getStyle() | java.awt.Font.ITALIC)));
        pixHint.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pixHint.setText(NbBundle.getMessage(AIDPanel.class, "AIDPanel.pixHint.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 3;
        add(pixHint, gridBagConstraints);

        generateButton.setText(NbBundle.getMessage(AIDPanel.class, "AIDPanel.generateButton.text")); // NOI18N
        generateButton.setToolTipText(org.openide.util.NbBundle.getMessage(AIDPanel.class, "AIDPanel.generateButton.toolTipText", new Object[] {})); // NOI18N
        generateButton.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(generateButton, gridBagConstraints);

        titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getStyle() | java.awt.Font.BOLD));
        titleLabel.setLabelFor(ridField);
        titleLabel.setText(org.openide.util.NbBundle.getMessage(AIDPanel.class, "AIDPanel.titleLabel.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(titleLabel, gridBagConstraints);

        javax.swing.GroupLayout spacerLayout = new javax.swing.GroupLayout(spacer);
        spacer.setLayout(spacerLayout);
        spacerLayout.setHorizontalGroup(
            spacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 512, Short.MAX_VALUE)
        );
        spacerLayout.setVerticalGroup(
            spacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 4, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(spacer, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == generateButton) {
            AIDPanel.this.generateButtonActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
        AID old = getAID();
        AID aid = Utils.generateAppletAID(pkg, clazz);
        if (old != null && old.equals(aid)) {
            aid = Utils.generateRandomAppletAid(clazz);
        }
        setAID(aid);
        getAID();
        fireChange();
    }//GEN-LAST:event_generateButtonActionPerformed

    public void generateAid() {
        generateButtonActionPerformed(null);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aidLabel;
    private javax.swing.JButton generateButton;
    private javax.swing.JTextField pixField;
    private javax.swing.JLabel pixHint;
    private javax.swing.JTextField ridField;
    private javax.swing.JLabel ridHint;
    private javax.swing.JLabel slash;
    private javax.swing.JPanel spacer;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

    public void insertUpdate(DocumentEvent e) {
        change();
    }

    public void removeUpdate(DocumentEvent e) {
        change();
    }

    public void changedUpdate(DocumentEvent e) {
        change();
    }

    public void focusGained(FocusEvent e) {
        ((JTextComponent) e.getComponent()).selectAll();
    }

    public void focusLost(FocusEvent e) {
        //do nothing
    }
}
