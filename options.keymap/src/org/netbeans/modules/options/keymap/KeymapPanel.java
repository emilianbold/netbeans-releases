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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
                JTextField textField = (JTextField) ((DefaultCellEditor) getCellEditor(row, column)).getComponent();
                boolean editCellAt = super.editCellAt(row, column);
                textField.requestFocus();
                return editCellAt;
            }

            @Override
            protected void processKeyEvent(KeyEvent e) {

                if (!isEditing())
                    super.processKeyEvent(e);
                else {
                    ((DefaultCellEditor) getCellEditor(lastRow, lastColumn)).getComponent().requestFocus();
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
                update();
            }
        };

        final Timer searchDelayTimer = new Timer(SEARCH_DELAY_TIME_LONG, al);
        searchDelayTimer.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                searchSCField.setText("");
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
                update();
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
            getModel().refreshActions();
            getModel().getDataVector().removeAllElements();
            for (String category : getModel().getCategories().get("")) {
                for (Object o : getModel().getItems(category)) {
                    if (o instanceof ShortcutAction) {
                        ShortcutAction sca = (ShortcutAction) o;
                        String[] shortcuts = getModel().getShortcuts(sca);
//                        String displayName = sca.getDisplayName();
                        for (int i = 0; i < shortcuts.length; i++) {
                            String shortcut = shortcuts[i];
                            if (shortcut.toString().equals(searchSCField.getText()))
                                getModel().addRow(new Object[]{new ActionHolder(sca, false), new ShortcutCell(shortcut, sca), category, ""});
                        }
                    }
                }
            }
        } else
            update();
    }

    static KeymapViewModel getModel() {
        if (keymapModel == null)
            keymapModel = new KeymapViewModel();
        return keymapModel;
    }

    //controller methods
    void applyChanges() {
        getModel().apply();
    }

    void cancel() {
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
        String searchText = searchField.getText().toLowerCase();
        getModel().setSearchText(searchText);

        String currentProfile = getModel().getCurrentProfile();
        getModel().update();

        // cbProfile
        List keymaps = getModel().getProfiles();
        cbProfile.removeAllItems();
        int i, k = keymaps.size();
        for (i = 0; i < k; i++)
            cbProfile.addItem(keymaps.get(i));

        cbProfile.setSelectedItem (currentProfile);
    }

    //controller method end

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .add(spShortcuts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(lProfile)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbProfile, 0, 162, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bDuplicate)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bDelete))
                    .add(layout.createSequentialGroup()
                        .add(searchLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchSCLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchSCField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
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
                    .add(searchField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(searchLabel)
                    .add(searchSCLabel)
                    .add(searchSCField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(12, 12, 12)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(spShortcuts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchField.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable actionsTable;
    private javax.swing.JButton bDelete;
    private javax.swing.JButton bDuplicate;
    private javax.swing.JComboBox cbProfile;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lProfile;
    private javax.swing.JList liShortcuts;
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
                    button.doClick();

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
