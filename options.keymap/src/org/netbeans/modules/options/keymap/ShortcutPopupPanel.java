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

package org.netbeans.modules.options.keymap;

import java.awt.Point;
import java.awt.event.KeyEvent;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.openide.util.NbBundle;

/**
 * Popup panel for changing shortcuts, invoked by mouseclick over [...] button
 * inside keymap options panel
 * @author Max Sauer
 */
public class ShortcutPopupPanel extends javax.swing.JPanel {

    private static final AbstractListModel modelWithAddAlternative = new Model(true);
    private static final AbstractListModel modelWithoutAddAltenrnative = new Model(false);
    private static AbstractListModel model = new DefaultListModel();

    private int row;
    private JTable table;
    private JPopupMenu pm;
    /** whether 'add alternative' should be displayed */
    private boolean displayAlternative;

    /** Creates new form ShortcutPopup */
    ShortcutPopupPanel(JTable table, JPopupMenu pm) {
        initComponents();
        this.table = table;
        this.pm = pm;
        }

    public void setRow(int row) {
        this.row = row;
    }

    /**
     * Set whether 'Add Alternative' menu item should be displayed
     */
    void setDisplayAddAlternative(boolean shortcutSet) {
        model = shortcutSet ? modelWithAddAlternative : modelWithoutAddAltenrnative;
        list.setModel(model);
        this.displayAlternative = shortcutSet;
        this.setPreferredSize(list.getPreferredSize());
    }

    private void addAlternative() {
        String category = (String) table.getValueAt(row, 2);
        ShortcutAction action = ((ActionHolder) table.getValueAt(row, 0)).getAction();
        Object[] newRow = new Object[]{new ActionHolder(action, true), "", category, ""};
        ((DefaultTableModel) ((TableSorter) table.getModel()).getTableModel()).insertRow(row + 1, newRow);
        pm.setVisible(false);
        table.editCellAt(row + 1, 1);
    }

    private void clear() {
        pm.setVisible(false);
        String scText = (String)table.getValueAt(row, 1);
        ShortcutAction action = ((ActionHolder) table.getValueAt(row, 0)).getAction();
        KeymapViewModel keymapViewModel = (KeymapViewModel) ((TableSorter) table.getModel()).getTableModel();
        if (scText.length() != 0)
            keymapViewModel.removeShortcut(action, scText);
        if (((ActionHolder) table.getValueAt(row, 0)).isAlternative())
            //alternative SC, remove row
            keymapViewModel.removeRow(row);
        else {
            table.setValueAt("",row, 1); // NOI18N
            keymapViewModel.update();
        }
        return;
    }

    private void resetToDefault() {
        pm.setVisible(false);
        ShortcutAction action = ((ActionHolder) table.getValueAt(row, 0)).getAction();
        KeymapViewModel mod = (KeymapViewModel) ((TableSorter) table.getModel()).getTableModel();
        mod.revertShortcutsToDefault(action);
        mod.fireTableDataChanged();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        list.setModel(model);
        list.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                listMouseMoved(evt);
            }
        });
        list.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                listKeyPressed(evt);
            }
        });
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(list);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void listMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseMoved
        list.setSelectedIndex(list.locationToIndex(new Point(evt.getX(), evt.getY())));
    }//GEN-LAST:event_listMouseMoved

    private void listMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseClicked
        int index = list.locationToIndex(new Point(evt.getX(), evt.getY()));
        if (displayAlternative) {
        switch (index) {
            case 0: //edit
                pm.setVisible(false);
                table.editCellAt(row, 1);
                break;
            case 1: {//add alternative
                addAlternative();
                break;
            }
            case 2: {//reset to default
                resetToDefault();
                break;
            }
            case 3: {//clear
                clear();
                break;
            }
            default:
                throw new UnsupportedOperationException("Invalid popup selection item"); // NOI18N
            }
        } else {
            switch (index) {
            case 0: //edit
                pm.setVisible(false);
                table.editCellAt(row, 1);
                break;
            case 1: {
                resetToDefault();
                break;
            }
            case 2: {
                clear();
                break;
            }
            default:
                throw new UnsupportedOperationException("Invalid popup selection item"); // NOI18N
            }

        }

    }//GEN-LAST:event_listMouseClicked

    private void listKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listKeyPressed
        int index = list.getSelectedIndex();
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            list.setSelectedIndex(index == 0 ? model.getSize() - 1 : index - 1);
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            list.setSelectedIndex(index == (model.getSize() - 1) ? 0 : index + 1);
        }
        evt.consume();
    }//GEN-LAST:event_listKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList list;
    // End of variables declaration//GEN-END:variables


    private static class Model extends AbstractListModel {
        private boolean displayAlternative;

        public Model(boolean displayAlternative) {
            this.displayAlternative = displayAlternative;
        }

        String[] elms = {
            NbBundle.getMessage(ShortcutPopupPanel.class, "Edit"), //NOI18N
            NbBundle.getMessage(ShortcutPopupPanel.class, "Add_Alternative"), //NOI18N
            NbBundle.getMessage(ShortcutPopupPanel.class, "Reset_to_Default"), //NOI18N
            NbBundle.getMessage(ShortcutPopupPanel.class, "Clear") //NOI18N
        };

        String[] elms0 = {
            elms[0], elms[2], elms[3]
        };

        public int getSize() {
            return displayAlternative == true ? elms.length : elms0.length;
        }

        public Object getElementAt(int index) {
            return displayAlternative == true ? elms[index] : elms0[index];
        }

    }

}
