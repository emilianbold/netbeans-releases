/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 *
 * @author Max Sauer
 */
@OptionsPanelController.Keywords(keywords={"#KW_KeymapOptions"}, location=OptionsDisplayer.KEYMAPS)
public class KeymapPanel extends javax.swing.JPanel implements ActionListener, Popupable {

    // Delay times for incremental search [ms]
    private static final int SEARCH_DELAY_TIME_LONG = 300; // < 3 chars
    private static final int SEARCH_DELAY_TIME_SHORT = 20; // >= 3 chars

    private volatile KeymapViewModel keymapModel;
    private TableSorter sorter;

    private JPopupMenu popup = new JPopupMenu();

    //search fields
    private Popup searchPopup;
    private SpecialkeyPanel specialkeyList;


    /** Creates new form KeymapPanel */
    public KeymapPanel() {
        sorter = new TableSorter(getModel());
        initComponents();
        specialkeyList = new SpecialkeyPanel(this, searchSCField);

        sorter.setTableHeader(actionsTable.getTableHeader());
        sorter.getTableHeader().setReorderingAllowed(false);
        actionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        actionsTable.setAutoscrolls(true);
        
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getModel().setSearchText(searchField.getText());
                getModel().update();
            }
        };

        final Timer searchDelayTimer = new Timer(SEARCH_DELAY_TIME_LONG, al);
        searchDelayTimer.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                searchSCField.setText("");
                ((ShortcutListener)searchSCField.getKeyListeners()[0]).clear();
                
                if (searchField.getText().length() > 3)
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_SHORT);
                searchDelayTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (searchField.getText().length() > 3)
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_LONG);
                searchDelayTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchSCField.setText("");
                getModel().setSearchText(searchField.getText());
                getModel().update();
            }
        });

        searchSCField.addKeyListener(new ShortcutListener(false));

        ActionListener al2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                narrowByShortcut();
            }
        };

        final Timer searchDelayTimer2 = new Timer(SEARCH_DELAY_TIME_SHORT, al2);
        searchDelayTimer2.setRepeats(false);
        searchSCField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                searchField.setText("");
                searchDelayTimer2.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchDelayTimer2.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchDelayTimer2.restart();
            }
        });

        actionsTable.addMouseListener(new ButtonCellMouseListener(actionsTable));
        actionsTable.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_CONTEXT_MENU &&
                    e.getKeyCode() != KeyEvent.VK_F2) {
                    return;
                }
		int leadRow = actionsTable.getSelectionModel().getLeadSelectionIndex();
		int leadColumn = actionsTable.getColumnModel().getSelectionModel().
		                   getLeadSelectionIndex();
		if (leadRow != -1 && leadColumn != -1 && !actionsTable.isEditing()) {
                    showPopupMenu(leadRow, leadColumn, -1, -1);
                    e.consume();
		}
            }
            
        });
        TableColumn column = actionsTable.getColumnModel().getColumn(1);
        column.setCellEditor(new ButtonCellEditor(getModel()));
        column.setCellRenderer(new ButtonCellRenderer(actionsTable.getDefaultRenderer(ButtonCellRenderer.class)));
        setColumnWidths();

        popup.add(new ShortcutPopupPanel(actionsTable, popup));
        cbProfile.addActionListener(this);
        manageButton.addActionListener(this);
    }

    private class KeymapTable extends JTable {
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
        
        private String selectedActionId;

        @Override
        public void valueChanged(ListSelectionEvent e) {
            super.valueChanged(e);
            if (!e.getValueIsAdjusting()) {
                int index = getSelectedRow();
                selectedActionId = getActionId(index);
            }
        }
        
        

        @Override
        public void sorterChanged(RowSorterEvent e) {
            String aid = selectedActionId;
            int colIndex = getSelectedColumn();
            super.sorterChanged(e);
            restoreSelection(aid, colIndex);
        }
        
        private void restoreSelection(String id, int colIndex) {
            if (id == null) {
                clearSelection();
                return;
            }
            TableModel tm = getModel();
            for (int i = 0; i < tm.getRowCount(); i++) {
                ActionHolder ah = (ActionHolder)tm.getValueAt(i, 0);
                if (ah != null && id.equals(ah.getAction().getId())) {
                    changeSelection(i, colIndex, false, false);
                    break;
                }
            }
        }
        
        private String getActionId(int modelIndex) {
            if (modelIndex >= 0 && modelIndex < getModel().getRowCount()) {
                ActionHolder h = (ActionHolder)getModel().getValueAt(modelIndex, 0);
                if (h != null) {
                    ShortcutAction sa = h.getAction();
                    return sa.getId();
                }
            }
            return null;
        }
        
        @Override
        public void tableChanged(TableModelEvent e) {
            String aid = selectedActionId;
            // preserve also table column selection:
            int colIndex = getSelectedColumn();
            super.tableChanged(e);
            restoreSelection(aid, colIndex);
        }

    }

    //todo: merge with update
    private void narrowByShortcut() {
        if (searchSCField.getText().length() != 0) {
            final String searchText = searchSCField.getText();
            getModel().runWithoutEvents(new Runnable() {
                public void run() {
                    getModel().getDataVector().removeAllElements();
                    for (String categorySet : getModel().getCategories().keySet()) {
                        for (String category : getModel().getCategories().get(categorySet)) {
                            for (Object o : getModel().getItems(category)) {
                                if (o instanceof ShortcutAction) {
                                    ShortcutAction sca = (ShortcutAction) o;
                                    String[] shortcuts = getModel().getShortcuts(sca);
                                    for (int i = 0; i < shortcuts.length; i++) {
                                        String shortcut = shortcuts[i];
                                        if (searched(shortcut, searchText))
                                            getModel().addRow(new Object[]{new ActionHolder(sca, false), shortcut, category, ""});
                                    }
                                }
                            }
                        }
                    }
                }
            });
            getModel().fireTableDataChanged();
        } else
            getModel().update();
    }

    KeymapViewModel getModel() {
        if (keymapModel == null) {
            KeymapViewModel tmpModel = new KeymapViewModel();
            synchronized (this) {
                if (keymapModel == null) {
                    keymapModel = tmpModel;
                }
            }
        }
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
        refreshProfileCombo ();
    }

    //controller method end


    private void refreshProfileCombo() {
        String currentProfile = getModel().getCurrentProfile();
        List keymaps = getModel().getProfiles();
        ComboBoxModel model = new DefaultComboBoxModel(keymaps.toArray());
        cbProfile.setModel(model);
        cbProfile.setSelectedItem(currentProfile);
    }

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
        manageButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        actionsTable = new KeymapTable();
        spShortcuts = new javax.swing.JScrollPane();
        liShortcuts = new javax.swing.JList();
        searchField = new javax.swing.JTextField();
        searchLabel = new javax.swing.JLabel();
        searchSCLabel = new javax.swing.JLabel();
        searchSCField = new javax.swing.JTextField();
        moreButton = new javax.swing.JButton();

        lProfile.setLabelFor(cbProfile);
        org.openide.awt.Mnemonics.setLocalizedText(lProfile, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "CTL_Keymap_Name")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageButton, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "CTL_Duplicate")); // NOI18N

        actionsTable.setModel(sorter);
        jScrollPane1.setViewportView(actionsTable);

        spShortcuts.setViewportView(liShortcuts);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 577, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap(103, Short.MAX_VALUE)
                            .addComponent(searchLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(searchSCLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(searchSCField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(moreButton))
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(spShortcuts, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(lProfile)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(cbProfile, 0, 379, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(manageButton))))))
                .addGap(0, 0, 0))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {searchField, searchSCField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lProfile)
                    .addComponent(cbProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(manageButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(moreButton)
                    .addComponent(searchSCField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchSCLabel)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchLabel))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(spShortcuts, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchField.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    public void hidePopup() {
        if (searchPopup != null) {
            searchPopup.hide();
            searchPopup = null;
        }
    }
    /**
     * Shows popup with ESC and TAB keys
     */
    private void moreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moreButtonActionPerformed
        if (searchPopup != null) {
            return;
        }
        JComponent tf = (JComponent) evt.getSource();
        Point p = new Point(tf.getX(), tf.getY());
        SwingUtilities.convertPointToScreen(p, this);
        //show special key popup
        searchPopup = PopupFactory.getSharedInstance().getPopup(this, specialkeyList, p.x, p.y);
        searchPopup.show();
}//GEN-LAST:event_moreButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable actionsTable;
    private javax.swing.JComboBox cbProfile;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lProfile;
    private javax.swing.JList liShortcuts;
    private javax.swing.JButton manageButton;
    private javax.swing.JButton moreButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchSCField;
    private javax.swing.JLabel searchSCLabel;
    private javax.swing.JScrollPane spShortcuts;
    // End of variables declaration//GEN-END:variables


    @Override
    public Popup getPopup() {
        return searchPopup;
    }

    class ButtonCellMouseListener implements MouseListener {

        private JTable table;

        public ButtonCellMouseListener(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            forwardEvent(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        private void forwardEvent(MouseEvent e) {
            Point p = new Point(e.getX(), e.getY());
            int row = table.rowAtPoint(p);
            int col = table.columnAtPoint(p);
            
            if (showPopupMenu(row, col, e.getX(), e.getY())) {
                e.consume();
            }
        }
    }
    
    private boolean showPopupMenu(int row, int col, int x, int y) {
        JTable table = actionsTable;
        
        if (col != 1) {
            return false;
        }
        
        Object valueAt = table.getValueAt(row, col);
        ShortcutCellPanel scCell = (ShortcutCellPanel) table.getCellRenderer(row, col).getTableCellRendererComponent(table, valueAt, true, true, row, col);
        Rectangle cellRect = table.getCellRect(row, col, false);
        JButton button = scCell.getButton();
        if (x < 0  || x > (cellRect.x + cellRect.width - button.getWidth())) { //inside changeButton
            boolean isShortcutSet = scCell.getTextField().getText().length() != 0;
            final ShortcutPopupPanel panel = (ShortcutPopupPanel) popup.getComponents()[0];
            panel.setDisplayAddAlternative(isShortcutSet);
            panel.setRow(row);

            if (x == -1 || y == -1) {
                x = button.getX() + 1;
                y = button.getY() + 1;
            }
            popup.show(table, x, y);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.requestFocus();
                }
            });
            popup.requestFocus();
            return true;
        }
        return false;
    }

    static String loc (String key) {
        return NbBundle.getMessage (KeymapPanel.class, key);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == cbProfile) {
            String profile = (String) cbProfile.getSelectedItem();
            if (profile != null)
                getModel().setCurrentProfile(profile);
            getModel().update();
        } else if (source == manageButton) {
            //remember previous profile state, in case user will cancel dialog
            Map<String, Map<ShortcutAction, Set<String>>> modifiedProfiles = getModel().getModifiedProfiles();
            Set<String> deletedProfiles = getModel().getDeletedProfiles();

            //show manage profiles dialog
            final ProfilesPanel profilesPanel = new ProfilesPanel(this);
            DialogDescriptor dd = new DialogDescriptor(profilesPanel, NbBundle.getMessage(KeymapPanel.class, "CTL_Manage_Keymap_Profiles"));
            DialogDisplayer.getDefault().notify(dd);

            if (dd.getValue().equals(DialogDescriptor.OK_OPTION)) {
                final String selectedProfile = profilesPanel.getSelectedProfile();
                getModel().setCurrentProfile(selectedProfile);
                refreshProfileCombo();

            } else {
                //revert changes
                getModel().setModifiedProfiles(modifiedProfiles);
                getModel().setDeletedProfiles(deletedProfiles);
            }
        }
        return;
    }
}
