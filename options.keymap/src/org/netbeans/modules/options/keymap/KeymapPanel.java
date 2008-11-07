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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Max Sauer
 */
public class KeymapPanel extends javax.swing.JPanel implements ActionListener {

    // Delay times for incremental search [ms]
    private static final int SEARCH_DELAY_TIME_LONG = 300; // < 3 chars
    private static final int SEARCH_DELAY_TIME_SHORT = 20; // >= 3 chars

    private static KeymapViewModel keymapModel;
    private TableSorter sorter;

    private JPopupMenu popup = new JPopupMenu();

    //search fields
    private Popup searchPopup;
    JList list  = new JList();

    /** Creates new form KeymapPanel */
    public KeymapPanel() {
        sorter = new TableSorter(getModel());
        initComponents();
        actionsTable = new JTable() {
            int lastRow;
            int lastColumn;

            @Override
            public boolean editCellAt(int row, int column) {
                lastRow = row;
                lastColumn = column;
                
                boolean editCellAt = super.editCellAt(row, column);
                ((DefaultCellEditor) getCellEditor(lastRow, lastColumn)).getComponent().requestFocus();
               return editCellAt;
            }

            @Override
            protected void processKeyEvent(KeyEvent e) {

                if (!isEditing())
                    super.processKeyEvent(e);
                else {
                    Component component = ((DefaultCellEditor) getCellEditor(lastRow, lastColumn)).getComponent();
                    component.requestFocus();
                    component.dispatchEvent(new KeyEvent(component, e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
                }
            }
        };
        actionsTable.setModel(sorter);
        jScrollPane1.setViewportView(actionsTable);

        sorter.setTableHeader(actionsTable.getTableHeader());
        sorter.getTableHeader().setReorderingAllowed(false);
        actionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        actionsTable.setDefaultRenderer(ShortcutCell.class, new ButtonCellRenderer(actionsTable.getDefaultRenderer(ButtonCellRenderer.class)));

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getModel().setSearchText(searchField.getText());
                getModel().update();
            }
        };

        final Timer searchDelayTimer = new Timer(SEARCH_DELAY_TIME_LONG, al);
        searchDelayTimer.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                searchSCField.setText("");
                ((ShortcutListener)searchSCField.getKeyListeners()[0]).clear();
                
                if (searchField.getText().length() > 3)
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_SHORT);
                searchDelayTimer.restart();
            }

            public void removeUpdate(DocumentEvent e) {
                if (searchField.getText().length() > 3)
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_LONG);
                searchDelayTimer.restart();
            }

            public void changedUpdate(DocumentEvent e) {
                searchSCField.setText("");
                getModel().setSearchText(searchField.getText());
                getModel().update();
            }
        });

        searchSCField.addKeyListener(new ShortcutListener(false));

        ActionListener al2 = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                narrowByShortcut();
            }
        };

        final Timer searchDelayTimer2 = new Timer(SEARCH_DELAY_TIME_SHORT, al2);
        searchDelayTimer2.setRepeats(false);
        searchSCField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                searchField.setText("");
                searchDelayTimer2.restart();
            }

            public void removeUpdate(DocumentEvent e) {
                searchDelayTimer2.restart();
            }

            public void changedUpdate(DocumentEvent e) {
                searchDelayTimer2.restart();
            }
        });

        actionsTable.addMouseListener(new ButtonCellMouseListener(actionsTable));
        TableColumn column = actionsTable.getColumnModel().getColumn(1);
        column.setCellEditor(new ButtonCellEditor(getModel()));
        setColumnWidths();

        popup.add(new ShortcutPopupPanel(actionsTable, popup));
        cbProfile.addActionListener(this);
        bDelete.addActionListener(this);
        bDuplicate.addActionListener(this);

        list.setListData(new String[] {"TAB", "ESCAPE"});//NOI18N
        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchPopup != null) {
                    searchPopup.hide();
                    searchPopup = null;
                }

                int index = list.locationToIndex(new Point(e.getX(), e.getY()));
                String scText = searchSCField.getText();
                final String space = " "; //NOI18N
                if (scText.length() == 0 || scText.endsWith(space) || scText.endsWith("+"))
                    searchSCField.setText(scText + list.getModel().getElementAt(index));
                else
                    searchSCField.setText(scText + space + list.getModel().getElementAt(index));

                    
            }
        });
    }

    private void deleteCurrentProfile() {
        String currentProfile = (String) cbProfile.getSelectedItem();
        getModel().deleteProfile(currentProfile);
        if (getModel ().isCustomProfile (currentProfile)) {
            cbProfile.removeItem (currentProfile);
            cbProfile.setSelectedIndex (0);
        }
    }

    //todo: maerge with update
    private void narrowByShortcut() {
        if (searchSCField.getText().length() != 0) {
            String searchText = searchSCField.getText();
            getModel().getDataVector().removeAllElements();
            for (String category : getModel().getCategories().get("")) {
                for (Object o : getModel().getItems(category)) {
                    if (o instanceof ShortcutAction) {
                        ShortcutAction sca = (ShortcutAction) o;
                        String[] shortcuts = getModel().getShortcuts(sca);
                        for (int i = 0; i < shortcuts.length; i++) {
                            String shortcut = shortcuts[i];
                            if (searched(shortcut, searchText))
                                getModel().addRow(new Object[]{new ActionHolder(sca, false), new ShortcutCell(shortcut), category, ""});
                        }
                    }
                }
            }
            getModel().fireTableDataChanged();
        } else
            getModel().update();
    }

    static KeymapViewModel getModel() {
        if (keymapModel == null)
            keymapModel = new KeymapViewModel();
        return keymapModel;
    }

    //controller methods
    void applyChanges() {
        stopCurrentCellEditing();
        getModel().apply();
    }

    void cancel() {
        stopCurrentCellEditing();
        if (keymapModel == null)
            return;
        keymapModel.cancel();
    }

    boolean dataValid() {
        return true;
    }

    boolean isChanged() {
        return getModel().isChanged();
    }

    void update() {
        getModel().refreshActions();

        //do not remember search state
        getModel().setSearchText(""); //NOI18N
        searchSCField.setText("");
        ((ShortcutListener)searchSCField.getKeyListeners()[0]).clear();
        searchField.setText(""); //NOI18N

        //update model
        getModel().update();

        //setup profiles
        String currentProfile = getModel().getCurrentProfile();
        List keymaps = getModel().getProfiles();
        cbProfile.removeAllItems();
        int i, k = keymaps.size();
        for (i = 0; i < k; i++)
            cbProfile.addItem(keymaps.get(i));

        cbProfile.setSelectedItem (currentProfile);
    }

    //controller method end

    private void stopCurrentCellEditing() {
        int row = actionsTable.getEditingRow();
        int col = actionsTable.getEditingColumn();
        if (row != -1)
            actionsTable.getCellEditor(row,col).stopCellEditing();
    }

    /**
     * @param shortcut shortcut compared with searched text
     * @return true if search text is empty || shortcut starts with or contains
     * searchtext
     */
    private boolean searched(String shortcut, String searchText) {
        //shortcut.equals(searchSCField.getText())
        if (searchText.length() == 0 || shortcut.startsWith(searchText) ||
                shortcut.contains(searchText))
            return true;
        else
            return false;
    }


    /**
     * Adjust column widths
     */
    private void setColumnWidths() {
        TableColumn column = null;
        for (int i = 0; i < actionsTable.getColumnCount(); i++) {
            column = actionsTable.getColumnModel().getColumn(i);
            switch (i) {
                case 0:
                    column.setPreferredWidth(250);
                    break;
                case 1:
                    column.setPreferredWidth(175);
                    break;
                case 2:
                    column.setPreferredWidth(60);
                    break;
                case 3:
                    column.setPreferredWidth(60);
                    break;
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lProfile = new javax.swing.JLabel();
        cbProfile = new javax.swing.JComboBox();
        bDuplicate = new javax.swing.JButton();
        bDelete = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        actionsTable = new javax.swing.JTable();
        spShortcuts = new javax.swing.JScrollPane();
        liShortcuts = new javax.swing.JList();
        jSeparator1 = new javax.swing.JSeparator();
        searchField = new javax.swing.JTextField();
        searchLabel = new javax.swing.JLabel();
        searchSCLabel = new javax.swing.JLabel();
        searchSCField = new javax.swing.JTextField();
        moreButton = new javax.swing.JButton();

        lProfile.setLabelFor(cbProfile);
        org.openide.awt.Mnemonics.setLocalizedText(lProfile, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "CTL_Keymap_Name")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bDuplicate, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "CTL_Duplicate")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bDelete, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "CTL_Delete")); // NOI18N

        actionsTable.setModel(sorter);
        jScrollPane1.setViewportView(actionsTable);

        spShortcuts.setViewportView(liShortcuts);

        jSeparator1.setForeground(new java.awt.Color(153, 153, 153));

        searchField.setText(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchField.text")); // NOI18N

        searchLabel.setLabelFor(searchField);
        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchLabel.text")); // NOI18N

        searchSCLabel.setLabelFor(searchSCField);
        org.openide.awt.Mnemonics.setLocalizedText(searchSCLabel, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchSCLabel.text")); // NOI18N

        searchSCField.setText(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchSCField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moreButton, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.moreButton.text")); // NOI18N
        moreButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        moreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moreButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                            .add(spShortcuts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(lProfile)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cbProfile, 0, 200, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(bDuplicate)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(bDelete))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                .add(searchLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(searchField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(searchSCLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(searchSCField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, 0)
                                .add(moreButton)))))
                .add(0, 0, 0))
        );

        layout.linkSize(new java.awt.Component[] {searchField, searchSCField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lProfile)
                    .add(cbProfile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bDuplicate)
                    .add(bDelete))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(moreButton)
                    .add(searchSCField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(searchSCLabel)
                    .add(searchField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(searchLabel))
                .add(12, 12, 12)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(spShortcuts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchField.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Shows popup with ESC and TAB keys
     */
    private void moreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moreButtonActionPerformed
        if (searchPopup == null) {
            JComponent tf = (JComponent) evt.getSource();
            Point p = new Point(tf.getX(), tf.getY());
            SwingUtilities.convertPointToScreen(p, this);
            //show special key popup
            searchPopup = PopupFactory.getSharedInstance().getPopup(this, list, p.x, p.y);
            searchPopup.show();
        }
}//GEN-LAST:event_moreButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable actionsTable;
    private javax.swing.JButton bDelete;
    private javax.swing.JButton bDuplicate;
    private javax.swing.JComboBox cbProfile;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lProfile;
    private javax.swing.JList liShortcuts;
    private javax.swing.JButton moreButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchSCField;
    private javax.swing.JLabel searchSCLabel;
    private javax.swing.JScrollPane spShortcuts;
    // End of variables declaration//GEN-END:variables


    class ButtonCellMouseListener implements MouseListener {

        private JTable table;

        public ButtonCellMouseListener(JTable table) {
            this.table = table;
        }

        public void mouseClicked(MouseEvent e) {
            forwardEvent(e);
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        private void forwardEvent(MouseEvent e) {
            Point p = new Point(e.getX(), e.getY());
            int row = table.rowAtPoint(p);
            int col = table.columnAtPoint(p);
            Object valueAt = table.getValueAt(row, col);
            if (valueAt instanceof ShortcutCell) {
                Rectangle cellRect = table.getCellRect(row, col, false);
                ShortcutCell scCell = (ShortcutCell) valueAt;
                JButton button = scCell.getButton();
                if (e.getX() > (cellRect.x + cellRect.width - button.getWidth())) { //inside changeButton
//                    MouseEvent buttonEvent = SwingUtilities.convertMouseEvent(table, e, button);
//                    button.dispatchEvent(buttonEvent);
//                    button.doClick();

                    boolean isShortcutSet = scCell.getTextField().getText().length() != 0;
                    ShortcutPopupPanel panel = (ShortcutPopupPanel) popup.getComponents()[0];
                    panel.setDisplayAddAlternative(isShortcutSet);
                    panel.setRow(row);
                    popup.show(table, e.getX(), e.getY());
                }
            }
        }
    }

    private static String loc (String key) {
        return NbBundle.getMessage (KeymapPanel.class, key);
    }

    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key));
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key));
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c,
                loc ("CTL_" + key)
            );
        } else {
            Mnemonics.setLocalizedText (
                (JLabel) c,
                loc ("CTL_" + key)
            );
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource ();

        if (source == bDelete) {
            deleteCurrentProfile ();
        } else
        if (source == cbProfile) {
            String profile = (String) cbProfile.getSelectedItem ();
            if (profile != null)
                getModel().setCurrentProfile(profile);
            getModel().update();

            if (getModel ().isCustomProfile (profile))
                loc (bDelete, "Delete");                          // NOI18N
            else
                loc (bDelete, "Restore");                         // NOI18N
        } else
        if (source == bDuplicate) {
            InputLine il = new InputLine (
                loc ("CTL_Create_New_Profile_Message"),                // NOI18N
                loc ("CTL_Create_New_Profile_Title")                   // NOI18N
            );
            il.setInputText ((String) cbProfile.
                getSelectedItem ());
            DialogDisplayer.getDefault ().notify (il);
            if (il.getValue () == NotifyDescriptor.OK_OPTION) {
                String newProfile = il.getInputText ();
                Iterator it = getModel ().getProfiles ().iterator ();
                while (it.hasNext ())
                    if (newProfile.equals (it.next ())) {
                        Message md = new Message (
                            loc ("CTL_Duplicate_Profile_Name"),        // NOI18N
                            Message.ERROR_MESSAGE
                        );
                        DialogDisplayer.getDefault ().notify (md);
                        return;
                    }
                getModel ().cloneProfile (newProfile);
                cbProfile.addItem (il.getInputText ());
                cbProfile.setSelectedItem (il.getInputText ());
            }
            return;
        }
    }


}
