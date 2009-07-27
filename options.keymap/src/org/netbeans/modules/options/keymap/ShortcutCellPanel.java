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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import org.openide.util.Utilities;

/**
 * Panel representing one shortcut cell inside keymap table
 * @author Max Sauer
 */
public class ShortcutCellPanel extends javax.swing.JPanel implements Comparable, Popupable {
    
    private Popup popup;
    private final SpecialkeyPanel specialkeyList;

    PopupFactory factory = PopupFactory.getSharedInstance();
    /** Creates new form ShortcutCell */
    public ShortcutCellPanel() {
        initComponents();
        specialkeyList = new SpecialkeyPanel(this, scField);

        // close the popup when user clicks elsewhere
        changeButton.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                hidePopup();
            }
        });

        // close popup when escape pressed and return to editing state
        changeButton.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    //XXX: find a better way
                    JTable table = (JTable) scField.getParent().getParent();
                    final int editingRow = table.getEditingRow();
                    table.editCellAt(editingRow, 1);
                    table.setRowSelectionInterval(editingRow, editingRow);
                    scField.requestFocus();
                }
            }

        });

        //set a different icon for more button in edit mode, since it offers different popup
        scField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                changeButton.setText(""); // NOI18N
                changeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/options/keymap/more.png")));
            }

            @Override
            public void focusLost(FocusEvent e) {
                changeButton.setIcon(null);
                changeButton.setText("..."); // NOI18N
            }
        });
    }

    ShortcutCellPanel(String displayedShortcut) {
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

    public void hidePopup() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
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

        scField.setText(org.openide.util.NbBundle.getMessage(ShortcutCellPanel.class, "ShortcutCellPanel.scField.text")); // NOI18N
        scField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scField.setPreferredSize(new java.awt.Dimension(0, 15));

        changeButton.setBackground(new java.awt.Color(204, 204, 204));
        org.openide.awt.Mnemonics.setLocalizedText(changeButton, org.openide.util.NbBundle.getMessage(ShortcutCellPanel.class, "ShortcutCellPanel.changeButton.text")); // NOI18N
        changeButton.setMaximumSize(new java.awt.Dimension(20, 15));
        changeButton.setMinimumSize(new java.awt.Dimension(20, 15));
        changeButton.setPreferredSize(new java.awt.Dimension(20, 15));
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
                .add(scField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(changeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(scField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(changeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void changeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        JComponent tf = (JComponent) evt.getSource();
        Point p = new Point(tf.getX(), tf.getY());
        SwingUtilities.convertPointToScreen(p, this);
        //show special key popup
        if (popup == null) {
            if (Utilities.isUnix()) {
                // #156869 workaround, force HW for Linux
                popup = PopupFactory.getSharedInstance().getPopup(null, specialkeyList, p.x, p.y);
            } else {
                popup = factory.getPopup(this, specialkeyList, p.x, p.y);
            }
        }
        popup.show();
    }//GEN-LAST:event_changeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton changeButton;
    private javax.swing.JTextField scField;
    // End of variables declaration//GEN-END:variables

    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }

    public Popup getPopup() {
        return popup;
    }

    void setButtontext(String text) {
        changeButton.setText(text);
    }

}
