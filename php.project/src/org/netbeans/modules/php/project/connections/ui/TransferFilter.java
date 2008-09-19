/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.php.project.connections.ui;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Radek Matous
 */
public final class TransferFilter extends javax.swing.JPanel {
    private TransferFilterTable table = null;
    private TransferFileTableModel model = null;
    private DocumentListener dlForSearch;
    private FocusListener flForSearch;
    private String filter = "";
    private PopupActionSupport popupActionsSupport;
    private static final RequestProcessor SEARCH_PROCESSOR = new RequestProcessor("search-processor");
    private final RequestProcessor.Task searchTask = SEARCH_PROCESSOR.create(new Runnable() {

        public void run() {
            if (filter != null) {
                int row = getSelectedRow();
                final TransferFileUnit u = (row >= 0) ? getModel().getUnitAtRow(row) : null;
                final Map<Integer, Boolean> state = TransferFileTableModel.captureState(model.getData());
                Runnable runAftreWards = new Runnable() {

                    public void run() {
                        if (u != null) {
                            int row = findRow(u.getId());
                            restoreSelectedRow(row);
                        }
                        TransferFileTableModel.restoreState(model.getData(), state, model.isMarkedAsDefault());
                        refreshState();
                    }
                };
                model.setFilter(filter, runAftreWards);
            }
        }
    });
        
    //folders are not filtered although not showed to user
    public static Set<TransferFile> showUploadDialog(Set<TransferFile> transferFiles, long timestamp) {
        return showTransferDialog(transferFiles, TransferFileTableModel.Type.UPLOAD, timestamp);
    }

    //folders are not filtered although not showed to user
    public static Set<TransferFile> showDownloadDialog(Set<TransferFile> transferFiles) {
        return showTransferDialog(transferFiles, TransferFileTableModel.Type.DOWNLOAD, -1);
    }

    private static Set<TransferFile> showTransferDialog(Set<TransferFile> transferFiles, TransferFileDownloadModel.Type type, long timestamp) {
        TransferFileTableModel model = null;
        String title = null;
        switch (type) {
            case DOWNLOAD:
                model = new TransferFileDownloadModel(wrapTransferFiles(transferFiles, model));
                title = NbBundle.getMessage(TransferFilter.class, "Download_Title");
                break;
            case UPLOAD:
                model = new TransferFileUploadModel(wrapTransferFiles(transferFiles, timestamp));
                title = NbBundle.getMessage(TransferFilter.class, "Upload_Title");
                break;
            default:
                assert false;
        }
        TransferFilter panel = new TransferFilter(new TransferFilterTable(model));
        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(TransferFilter.class, "LBL_Ok"));
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                title,
                true,
                new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
                okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        if (DialogDisplayer.getDefault().notify(descriptor) == okButton) {
            return unwrapFileUnits(model.getFilteredUnits());
        }
        return Collections.<TransferFile>emptySet();
    }

    private static List<TransferFileUnit> wrapTransferFiles(Collection<TransferFile> toTransfer, TransferFileTableModel model) {
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>();
        for (TransferFile transferFile : toTransfer) {
            retval.add(new TransferFileUnit(transferFile, model.isMarkedAsDefault()));
        }
        return retval;
    }

    private static List<TransferFileUnit> wrapTransferFiles(Collection<TransferFile> toTransfer,  long timestamp) {
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>();
        boolean selected = timestamp == -1;
        for (TransferFile transferFile : toTransfer) {
            if (timestamp != -1) {
                // we have some timestamp
                selected = transferFile.isFile() && transferFile.getTimestamp() > timestamp;
            }
            retval.add(new TransferFileUnit(transferFile, selected));
        }
        return retval;
    }

    private static Set<TransferFile> unwrapFileUnits(List<TransferFileUnit> fileUnits) {
        Set<TransferFile> retval = new HashSet<TransferFile>();
        for (TransferFileUnit fUnit : fileUnits) {
            retval.add(fUnit.getTransferFile());
        }
        return retval;
    }

    private TransferFilter(TransferFilterTable table) {
        this.table = table;
        TableModel m = table.getModel();
        assert m instanceof TransferFileTableModel : m + " instanceof FileConfirmationTableModel.";
        this.model = (TransferFileTableModel) m;
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        initComponents();
        //TODO: for WINDOWS - don't paint background and let visible the native look
        /*
        if (UIManager.getLookAndFeel().getName().toLowerCase().startsWith("windows")) {//NOI18N
        setOpaque(false);
        }
         */
        initTab();
        listenOnSelection();
        refreshState();

        addUpdateUnitListener(new TransferFileTableChangeListener() {

            public void updateUnitsChanged() {
                model.fireTableDataChanged();
                refreshState();
            }

            public void buttonsChanged() {
                //UnitTab.this.manager.buttonsChanged();
            }

            public void filterChanged() {
                model.fireTableDataChanged();
                refreshState();
            }
        });
    }

    void focusTable() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                table.requestFocusInWindow();
            }
        });
    }
    
    TransferFileTableModel getModel() {
        return model;
    }

    TransferFilterTable getTable() {
        return table;
    }

    public String getHelpId() {
        return TransferFilter.class.getName() + '.' + model.getType(); // NOI18N
    }

    @Override
    public void addNotify() {
        super.addNotify();
        focusTable();
        if (dlForSearch == null) {
            tfSearch.getDocument().addDocumentListener(getDocumentListener());
        }

        if (flForSearch == null) {
            flForSearch = new FocusListener() {

                public void focusGained(FocusEvent e) {
                    tfSearch.selectAll();
                }

                public void focusLost(FocusEvent e) {
                    tfSearch.select(0, 0);
                }
            };
            tfSearch.addFocusListener(flForSearch);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (dlForSearch != null) {
            tfSearch.getDocument().removeDocumentListener(getDocumentListener());
        }
        dlForSearch = null;
        if (flForSearch != null) {
            tfSearch.removeFocusListener(flForSearch);
        }
        flForSearch = null;
    }

    public void refreshState() {
        final Collection<TransferFileUnit> units = model.getMarkedFileUnits();
        popupActionsSupport.tableDataChanged();

        if (units.size() == 0) {
            cleanSelectionInfo();
        } else {
            setSelectionInfo(units.size());
        }
    }

    private void initTab() {
        TabAction[] forPopup = null;

        switch (model.getType()) {
            case DOWNLOAD:
            case UPLOAD:
                forPopup = new TabAction[]{new CheckAllAction (), new UncheckAllAction(), new CheckAction()};
                break;
        }
        model.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                refreshState();
            }
        });
        table.addMouseListener(popupActionsSupport = new PopupActionSupport(forPopup));
    }

    private void cleanSelectionInfo() {
        lSelectionInfo.setText(" ");
        lWarning.setText(" ");
        lWarning.setIcon(null);
    }

    private void setSelectionInfo(int count) {
        String operationNameKey = null;
        switch (model.getType()) {
            case UPLOAD:
                operationNameKey = "FileConfirmationTableModel_Warning_Upload";
                break;
            case DOWNLOAD:
                operationNameKey = "FileConfirmationTableModel_Warning_Download";
                break;
        }
        String key = count == 1 ? "FileConfirmationPane_lHowManySelected_Single_Text" : "FileConfirmationPane_lHowManySelected_Many_Text";
        lSelectionInfo.setText((NbBundle.getMessage(TransferFilter.class, key, count)));
        Image loadImage = ImageUtilities.loadImage(
                "org/netbeans/modules/php/project/ui/resources/info_icon.png");//NOI18N
        lWarning.setIcon(new ImageIcon(loadImage));
        lWarning.setText(NbBundle.getMessage(TransferFilter.class, operationNameKey));
    }

    private void listenOnSelection() {
        table.getSelectionModel().setSelectionInterval(0, 0);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm =
                        (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                    //no rows are selected
                    popupActionsSupport.rowChanged(-1);
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    popupActionsSupport.rowChanged(selectedRow);
                //selectedRow is selected
                }
            }
        });
    }

    public void addUpdateUnitListener(TransferFileTableChangeListener l) {
        model.addUpdateUnitListener(l);
    }

    public void removeUpdateUnitListener(TransferFileTableChangeListener l) {
        model.removeUpdateUnitListener(l);
    }

    void fireUpdataUnitChange() {
        model.fireUpdataUnitChange();
    }

    DocumentListener getDocumentListener() {
        if (dlForSearch == null) {
            dlForSearch = new DocumentListener() {

                public void insertUpdate(DocumentEvent arg0) {
                    filter = tfSearch.getText().trim();
                    searchTask.schedule(350);
                }

                public void removeUpdate(DocumentEvent arg0) {
                    insertUpdate(arg0);
                }

                public void changedUpdate(DocumentEvent arg0) {
                    insertUpdate(arg0);
                }
            };
        }
        return dlForSearch;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lSelectionInfo = new javax.swing.JLabel();
        lSearch = new javax.swing.JLabel();
        tfSearch = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = table;
        lWarning = new javax.swing.JLabel();

        setFocusTraversalPolicy(new java.awt.FocusTraversalPolicy() {
            public java.awt.Component getDefaultComponent(java.awt.Container focusCycleRoot){
                return tfSearch;
            }//end getDefaultComponent

            public java.awt.Component getFirstComponent(java.awt.Container focusCycleRoot){
                return tfSearch;
            }//end getFirstComponent

            public java.awt.Component getLastComponent(java.awt.Container focusCycleRoot){
                return tfSearch;
            }//end getLastComponent

            public java.awt.Component getComponentAfter(java.awt.Container focusCycleRoot, java.awt.Component aComponent){
                return tfSearch;//end getComponentAfter
            }
            public java.awt.Component getComponentBefore(java.awt.Container focusCycleRoot, java.awt.Component aComponent){
                return tfSearch;//end getComponentBefore

            }}
        );

        org.openide.awt.Mnemonics.setLocalizedText(lSelectionInfo, org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.lSelectionInfo.text")); // NOI18N

        lSearch.setLabelFor(tfSearch);
        org.openide.awt.Mnemonics.setLocalizedText(lSearch, org.openide.util.NbBundle.getMessage(TransferFilter.class, "lSearch1.text")); // NOI18N

        jTable1.setModel(table.getModel());
        jScrollPane1.setViewportView(jTable1);
        jTable1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.jTable1.AccessibleContext.accessibleName")); // NOI18N
        jTable1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.jTable1.AccessibleContext.accessibleDescription")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lWarning)
                    .add(layout.createSequentialGroup()
                        .add(lSearch)
                        .add(4, 4, 4)
                        .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lSelectionInfo)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lSearch))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lSelectionInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(lWarning))
                .addContainerGap())
        );

        lSelectionInfo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.lSelectionInfo.AccessibleContext.accessibleName")); // NOI18N
        lSelectionInfo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.lSelectionInfo.AccessibleContext.accessibleDescription")); // NOI18N
        lSearch.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.lSearch.AccessibleContext.accessibleName")); // NOI18N
        lSearch.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransferFilter.class, "ACD_Search")); // NOI18N
        tfSearch.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.tfSearch.AccessibleContext.accessibleName")); // NOI18N
        tfSearch.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.tfSearch.AccessibleContext.accessibleDescription")); // NOI18N
        jScrollPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.jScrollPane1.AccessibleContext.accessibleName")); // NOI18N
        jScrollPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.jScrollPane1.AccessibleContext.accessibleDescription")); // NOI18N
        lWarning.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.lWarning.AccessibleContext.accessibleName")); // NOI18N
        lWarning.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.lWarning.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransferFilter.class, "TransferFilter.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private class PopupActionSupport extends MouseAdapter {

        private final TabAction[] actions;

        PopupActionSupport(TabAction[] actions) {
            this.actions = actions;
        }

        void rowChanged(int row) {
            TransferFileUnit u = null;
            if (row > -1) {
                u = model.getUnitAtRow(row);
            }

            for (TabAction action : actions) {
                if (action instanceof RowTabAction) {
                    RowTabAction rowAction = (RowTabAction) action;
                    rowAction.unitChanged(row, u);
                }
            }
        }

        void tableDataChanged() {
            Collection<TransferFileUnit> units = model.getMarkedUnits();
            for (TabAction action : actions) {
                action.tableDataChanged(units);
            }
        }

        private JPopupMenu createPopup() {
            JPopupMenu popup = new JPopupMenu();
            popup.removeAll();
            Set<String> categories2 = new HashSet<String>();
            List<String> categories = new ArrayList<String>();
            for (TabAction action : actions) {
                String categoryName = action.getActionCategory();
                if (categories2.add(categoryName)) {
                    categories.add(categoryName);
                }
            }
            for (String categoryName : categories) {
                boolean addSeparator = popup.getSubElements().length > 0;
                for (TabAction action : actions) {
                    String actionCategory = action.getActionCategory();
                    if ((categoryName != null && categoryName.equals(actionCategory)) || (categoryName == null && actionCategory == null)) {
                        if (action instanceof RowTabAction) {
                            RowTabAction rowAction = (RowTabAction) action;
                            if (rowAction.isVisible()) {
                                if (addSeparator) {
                                    addSeparator = false;
                                    popup.addSeparator();
                                }
                                popup.add(new JMenuItem(action));
                            }
                        } else {
                            if (addSeparator) {
                                addSeparator = false;
                                popup.addSeparator();
                            }
                            popup.add(new JMenuItem(action));
                        }
                    }
                }
            }
            return popup;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!maybeShowPopup(e)) {
                //int row = FileConfirmationPane.this.table.rowAtPoint(e.getPoint());
            }
        }

        private boolean maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                focusTable();
                showPopup(e.getPoint(), e.getComponent());
                return true;
            }
            return false;
        }
    }

    private void showPopup(Point e, Component invoker) {
        int row = TransferFilter.this.table.rowAtPoint(e);
        if (row >= 0) {
            table.getSelectionModel().setSelectionInterval(row, row);
            final JPopupMenu popup = popupActionsSupport.createPopup();
            if (popup != null && popup.getComponentCount() > 0) {
                popup.show(invoker, e.x, e.y);

            }
        }
    }

    private int getSelectedRow() {
        return table.getSelectedRow();
    }

    private void restoreSelectedRow(int row) {
        if (row < 0) {
            row = 0;
        }
        for (int temp = row; temp >= 0; temp--) {
            if (temp < table.getRowCount() && temp > -1) {
                table.getSelectionModel().setSelectionInterval(temp, temp);
                break;
            }
        }
    }

    int findRow(Integer id) {
        for (int i = 0; i < model.getRowCount(); i++) {
            TransferFileUnit u = model.getUnitAtRow(i);
            if (u != null && id.equals(u.getId())) {
                return i;
            }
        }
        return -1;
    }

    static String textForKey(String key) {
        JButton jb = new JButton();
        Mnemonics.setLocalizedText(jb, NbBundle.getMessage(TransferFilter.class, key));
        return jb.getText();
    }

    static int mnemonicForKey(String key) {
        JButton jb = new JButton();
        Mnemonics.setLocalizedText(jb, NbBundle.getMessage(TransferFilter.class, key));
        return jb.getMnemonic();
    }

    private abstract class TabAction extends AbstractAction {

        private String name;
        private String actionCategory;

        public TabAction(String nameKey, String actionCategoryKey) {
            super(textForKey(nameKey));
            this.actionCategory = actionCategoryKey;//(actionCategoryKey != null) ? NbBundle.getMessage(UnitTab.class, actionCategoryKey) : null;
            putValue(MNEMONIC_KEY, mnemonicForKey(nameKey));
            name = (String) getValue(NAME);
            putIntoActionMap(table);
        }

        public TabAction(String key, KeyStroke accelerator, String actionCategoryKey) {
            this(key, actionCategoryKey);
            putValue(ACCELERATOR_KEY, accelerator);
            putIntoActionMap(table);
        }

        protected String getActionName() {
            return name;
        }

        public String getActionCategory() {
            return getActionCategoryImpl();//NOI18N
        }

        protected String getActionCategoryImpl() {
            return actionCategory;
        }

        protected void setContextName(String name) {
            putValue(NAME, name);
        }

        public void putIntoActionMap(JComponent component) {
            KeyStroke ks = (KeyStroke) getValue(ACCELERATOR_KEY);
            Object key = getValue(NAME);
            if (ks == null) {
                ks = KeyStroke.getKeyStroke((Integer) getValue(MNEMONIC_KEY), KeyEvent.VK_ALT);
            }
            if (ks != null && key != null) {
                component.getInputMap(JComponent.WHEN_FOCUSED).put(ks, key);
                component.getActionMap().put(key, this);
            }
        }

        public final void performAction() {
            if (isEnabled()) {
                actionPerformed(null);
            }
        }

        public final void actionPerformed(ActionEvent e) {
            try {
                performerImpl();
            } finally {
            }
        }

        public void tableDataChanged() {
            tableDataChanged(model.getMarkedUnits());
        }

        public void tableDataChanged(Collection<TransferFileUnit> units) {
            setEnabled(units.size() > 0);
        }

        public abstract void performerImpl();
    }

    private abstract class RowTabAction extends TabAction {

        private TransferFileUnit u;
        private int row;

        public RowTabAction(String nameKey, String actionCategoryKey) {
            super(nameKey, actionCategoryKey);
        }

        public RowTabAction(String nameKey, KeyStroke accelerator, String actionCategoryKey) {
            super(nameKey, accelerator, actionCategoryKey);
        }

        public void unitChanged(int row, TransferFileUnit u) {
            this.u = u;
            this.row = row;
            unitChanged();
        }

        public final boolean isVisible() {
            return (u != null) ? isVisible(u) : isVisible(row);
        }

        private final void unitChanged() {
            if (u != null) {
                setEnabled(isEnabled(u));
                setContextName(getContextName(u));
            } else {
                setEnabled(isEnabled(row));
                setContextName(getContextName(row));
            }
        }

        @Override
        public void tableDataChanged() {
            unitChanged();
        }

        @Override
        public void tableDataChanged(Collection<TransferFileUnit> units) {
            unitChanged();
        }

        public final void performerImpl() {
            performerImpl(u);
        }

        protected boolean isVisible(TransferFileUnit u) {
            return u != null;
        }

        protected boolean isVisible(int row) {
            return false;
        }

        public abstract void performerImpl(TransferFileUnit u);

        protected abstract boolean isEnabled(TransferFileUnit u);

        protected boolean isEnabled(int row) {
            return false;
        }

        protected abstract String getContextName(TransferFileUnit u);

        protected String getContextName(int row) {
            return getActionName();
        }
    }

    private class UncheckAllAction extends RowTabAction {
        public UncheckAllAction () {
            super ("FileConfirmationPane_UncheckAllAction", "Uncheck");//NOI18N
        }
        public void performerImpl(TransferFileUnit notUsed) {
            final int row = getSelectedRow();
            Collection<TransferFileUnit> allUnits = model.getVisibleFileUnits();
            for (TransferFileUnit tfu : allUnits) {
                if (tfu != null && tfu.isMarked ()  && tfu.canBeMarked ()) {
                    tfu.setMarked (false);
                }
            }
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
        }

        @Override
        protected boolean isEnabled(TransferFileUnit u) {
            return true;
        }

        @Override
        protected String getContextName(TransferFileUnit u) {
            return getActionName ();
        }
    }

    private  class CheckAllAction extends RowTabAction {
        public CheckAllAction () {
            super ("FileConfirmationPane_CheckAllAction", "Check");//NOI18N
        }

        @Override
        public void performerImpl(TransferFileUnit notUsed) {
            final int row = getSelectedRow();
            Collection<TransferFileUnit> allUnits = model.getVisibleFileUnits();
            for (TransferFileUnit tfu : allUnits) {
                if (tfu != null && !tfu.isMarked () &&  tfu.canBeMarked ()) {
                    tfu.setMarked (true);
                }
            }
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
        }

        @Override
        protected boolean isEnabled(TransferFileUnit u) {
            return true;
        }

        @Override
        protected String getContextName(TransferFileUnit u) {
            return getActionName ();
        }
    }

    private class CheckAction extends RowTabAction {

        public CheckAction() {
            super("FileConfirmationPane_CheckAction", KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), null);
        }

        public void performerImpl(TransferFileUnit u) {
            final int row = getSelectedRow();
            if (u != null && u.canBeMarked()) {
                u.setMarked(!u.isMarked());
            }
            model.fireTableDataChanged();
            restoreSelectedRow(row);
        }

        protected boolean isEnabled(TransferFileUnit u) {
            return u != null && u.canBeMarked();
        }

        protected String getContextName(TransferFileUnit u) {
            return getActionName();
        }

        @Override
        protected boolean isVisible(TransferFileUnit u) {
            return false;
        }

        @Override
        protected boolean isVisible(int row) {
            return false;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lSearch;
    private javax.swing.JLabel lSelectionInfo;
    private javax.swing.JLabel lWarning;
    private javax.swing.JTextField tfSearch;
    // End of variables declaration//GEN-END:variables
}
