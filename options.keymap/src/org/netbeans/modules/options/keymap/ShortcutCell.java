/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * ShortcutCell.java
 *
 * Created on Oct 8, 2008, 10:35:16 AM
 */

package org.netbeans.modules.options.keymap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

/**
 * Panel representing one shortcut cell inside keymap table
 * @author Max Sauer
 */
public class ShortcutCell extends javax.swing.JPanel implements Comparable {
    
    private Popup popup;
    private static final String[] specialKeys = new String[]{"UP", "DOWN", "LEFT", "RIGHT", "ENTER", "ESCAPE"}; //NOI18N
    private final JList list = new JList(specialKeys);

    PopupFactory factory = PopupFactory.getSharedInstance();
    /** Creates new form ShortcutCell */
    public ShortcutCell() {
        initComponents();

        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(new Point(e.getX(), e.getY()));
                if (popup != null) {
                    popup.hide();
                    popup = null;
                }
                String text = scField.getText();
                if (text.endsWith("+")) // NOI18N
                    scField.setText(text + list.getModel().getElementAt(index));
                else
                    scField.setText(text + " " + list.getModel().getElementAt(index)); //NOI18N
                scField.requestFocus();
                scField.selectAll();
                scField.setCaretPosition(scField.getText().length()-1);
            }

        });
    }

    public ShortcutCell(String displayedShortcut) {
        this();
        setText(displayedShortcut);
    }

    /**
     * Sets the shortcut text represenation for this table cell
     * @param shortcut
     */
    public void setText(String shortcut) {
        scField.setText(shortcut);
    }

    @Override
    public String toString() {
        return scField.getText();
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        int buttonWidth = changeButton.getPreferredSize().width;
        scField.setPreferredSize(new Dimension(d.width - buttonWidth, d.height));

        changeButton.setPreferredSize(new Dimension(buttonWidth, d.height));
    }

    void setBgColor(Color col) {
//        this.setBackground(col);
        scField.setBackground(col);
        changeButton.setBackground(new java.awt.Color(204, 204, 204));
    }

    void setFgCOlor(Color col) {
//        this.setForeground(col);
        scField.setForeground(col);
    }

    public JButton getButton() {
        return changeButton;
    }

    public JTextField getTextField() {
        return scField;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scField = new ShortcutTextField();
        changeButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));
        setPreferredSize(new java.awt.Dimension(134, 15));

        scField.setText(org.openide.util.NbBundle.getMessage(ShortcutCell.class, "ShortcutCell.scField.text")); // NOI18N
        scField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scField.setPreferredSize(new java.awt.Dimension(0, 15));

        changeButton.setBackground(new java.awt.Color(204, 204, 204));
        org.openide.awt.Mnemonics.setLocalizedText(changeButton, org.openide.util.NbBundle.getMessage(ShortcutCell.class, "ShortcutCell.changeButton.text")); // NOI18N
        changeButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        changeButton.setMaximumSize(new java.awt.Dimension(20, 15));
        changeButton.setMinimumSize(new java.awt.Dimension(20, 15));
        changeButton.setPreferredSize(new java.awt.Dimension(20, 15));
        changeButton.setRolloverEnabled(true);
        changeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(scField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(changeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(changeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(scField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void changeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        JComponent tf = (JComponent) evt.getSource();
        Point p = new Point(tf.getX(), tf.getY());
        SwingUtilities.convertPointToScreen(p, this);
        //show special key popup
        if (popup == null)
            popup = factory.getPopup(this, list, p.x, p.y);
        popup.show();
    }//GEN-LAST:event_changeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton changeButton;
    private javax.swing.JTextField scField;
    // End of variables declaration//GEN-END:variables

    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }

}
