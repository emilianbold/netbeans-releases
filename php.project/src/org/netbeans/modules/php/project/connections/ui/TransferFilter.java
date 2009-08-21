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
import javax.swing.JPanel;
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
 * @author  Radek Matous
 */
public final class TransferFilter extends JPanel {
    private static final long serialVersionUID = -1971424369225251471L;
    private static final ImageIcon INFO_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/php/project/ui/resources/info_icon.png")); // NOI18N

    final TransferFilterTable table;
    final TransferFileTableModel model;
    private DocumentListener dlForSearch;
    private FocusListener flForSearch;
    private volatile String filter = "";
    private PopupActionSupport popupActionsSupport;
    private static final RequestProcessor FILTER_PROCESSOR = new RequestProcessor("filter processor");
    private final RequestProcessor.Task searchTask = FILTER_PROCESSOR.create(new Runnable() {
        public void run() {
            if (filter != null) {
                int row = getSelectedRow();
                final TransferFileUnit unit = row != -1 ? getModel().getUnitAtRow(row) : null;
                final Map<Integer, Boolean> state = TransferFileTableModel.captureState(model.getData());
                Runnable runAfterWards = new Runnable() {
                    public void run() {
                        if (unit != null) {
                            int row = model.getRowForUnit(unit);
                            restoreSelectedRow(row);
                        }
                        TransferFileTableModel.restoreState(model.getData(), state, TransferFileTableModel.isMarkedAsDefault());
                        refreshState();
                    }
                };
                model.setFilter(filter, runAfterWards);
            }
        }
    });

    private TransferFilter(TransferFilterTable table) {
        this.table = table;
        TableModel m = table.getModel();
        assert m instanceof TransferFileTableModel : m + " instanceof TransferFileTableModel.";
        model = (TransferFileTableModel) m;
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        initComponents();
    }

    private static TransferFilter create(TransferFilterTable table) {
        TransferFilter transferFilter = new TransferFilter(table);
        transferFilter.initPopup();
        transferFilter.listenOnSelection();
        transferFilter.listenOnUnitChanges();
        transferFilter.refreshState();
        return transferFilter;
    }

    public static Set<TransferFile> showUploadDialog(Set<TransferFile> transferFiles, long timestamp) {
        return showTransferDialog(transferFiles, TransferFileTableModel.Type.UPLOAD, timestamp);
    }

    public static Set<TransferFile> showDownloadDialog(Set<TransferFile> transferFiles) {
        return showTransferDialog(transferFiles, TransferFileTableModel.Type.DOWNLOAD, -1);
    }

    public static TransferFilter getEmbeddableDownloadDialog(Set<TransferFile> transferFiles) {
        TransferFileTableModel model = new TransferFileDownloadModel(wrapTransferFiles(transferFiles));
        return TransferFilter.create(new TransferFilterTable(model));
    }

    public static Set<TransferFile> getSelectedFiles(TransferFilter transferFilter) {
        return unwrapFileUnits(transferFilter.getModel().getMarkedUnits());
    }

    public static boolean hasAnyTransferableFiles(Set<TransferFile> transferFiles) {
        if (transferFiles == null) {
            return false;
        }
        TransferFilter transferFilter = getEmbeddableDownloadDialog(transferFiles);
        return !unwrapFileUnits(transferFilter.getModel().getMarkedUnits()).isEmpty();
    }

    // folders are not filtered although not showed to user
    private static Set<TransferFile> showTransferDialog(Set<TransferFile> transferFiles, TransferFileDownloadModel.Type type, long timestamp) {
        TransferFileTableModel model = null;
        String title = null;
        switch (type) {
            case DOWNLOAD:
                model = new TransferFileDownloadModel(wrapTransferFiles(transferFiles));
                title = NbBundle.getMessage(TransferFilter.class, "Download_Title");
                break;
            case UPLOAD:
                model = new TransferFileUploadModel(wrapTransferFiles(transferFiles, timestamp));
                title = NbBundle.getMessage(TransferFilter.class, "Upload_Title");
                break;
            default:
                throw new IllegalArgumentException("Unknown model type: " + type);
        }
        TransferFilter transferFilter = TransferFilter.create(new TransferFilterTable(model));
        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(TransferFilter.class, "LBL_Ok"));
        DialogDescriptor descriptor = new DialogDescriptor(
                transferFilter,
                title,
                true,
                new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
                okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        if (DialogDisplayer.getDefault().notify(descriptor) == okButton) {
            return getSelectedFiles(transferFilter);
        }
        return Collections.<TransferFile>emptySet();
    }

    private static List<TransferFileUnit> wrapTransferFiles(Collection<TransferFile> toTransfer) {
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>(toTransfer.size());
        for (TransferFile transferFile : toTransfer) {
            retval.add(new TransferFileUnit(transferFile, TransferFileTableModel.isMarkedAsDefault()));
        }
        return retval;
    }

    private static List<TransferFileUnit> wrapTransferFiles(Collection<TransferFile> toTransfer,  long timestamp) {
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>(toTransfer.size());
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
        assert dlForSearch != null;
        tfSearch.getDocument().removeDocumentListener(getDocumentListener());
        dlForSearch = null;

        assert flForSearch != null;
        tfSearch.removeFocusListener(flForSearch);
        flForSearch = null;
    }

    public void refreshState() {
        int units = model.getMarkedUnits().size();
        popupActionsSupport.tableDataChanged();
        if (units == 0) {
            cleanSelectionInfo();
        } else {
            setSelectionInfo(units);
        }
    }

    private void initPopup() {
        TabAction[] forPopup = null;
        switch (model.getType()) {
            case DOWNLOAD:
            case UPLOAD:
                forPopup = new TabAction[] {new CheckAllAction(), new UncheckAllAction(), new CheckAction()};
                break;
            default:
                throw new IllegalArgumentException("Unknown model type: " + model.getType());
        }
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                refreshState();
            }
        });
        popupActionsSupport = new PopupActionSupport(forPopup);
        table.addMouseListener(popupActionsSupport);
    }

    private void cleanSelectionInfo() {
        lSelectionInfo.setText(" "); // NOI18N
        lWarning.setText(" "); // NOI18N
        lWarning.setIcon(null);
    }

    private void setSelectionInfo(int count) {
        String operationNameKey = null;
        switch (model.getType()) {
            case UPLOAD:
                operationNameKey = "FileConfirmationTableModel_Warning_Upload"; // NOI18N
                break;
            case DOWNLOAD:
                operationNameKey = "FileConfirmationTableModel_Warning_Download"; // NOI18N
                break;
            default:
                throw new IllegalArgumentException("Unknown model type: " + model.getType());
        }
        String key = count == 1 ? "FileConfirmationPane_lHowManySelected_Single_Text" : "FileConfirmationPane_lHowManySelected_Many_Text"; // NOI18N
        lSelectionInfo.setText((NbBundle.getMessage(TransferFilter.class, key, count)));
        lWarning.setIcon(INFO_ICON);
        lWarning.setText(NbBundle.getMessage(TransferFilter.class, operationNameKey));
    }

    private void listenOnSelection() {
        table.getSelectionModel().setSelectionInterval(0, 0);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                    popupActionsSupport.rowChanged(-1);
                } else {
                    popupActionsSupport.rowChanged(lsm.getMinSelectionIndex());
                }
            }
        });
    }

    private void listenOnUnitChanges() {
        addUpdateUnitListener(new TransferFileTableChangeListener() {
            public void updateUnitsChanged() {
                // no need to refresh table data (they are sorted so it's already done)
                refreshState();
            }

            public void filterChanged() {
                model.fireTableDataChanged();
                refreshState();
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
                public void insertUpdate(DocumentEvent event) {
                    processUpdate(event);
                }

                public void removeUpdate(DocumentEvent event) {
                    processUpdate(event);
                }

                public void changedUpdate(DocumentEvent event) {
                    processUpdate(event);
                }

                private void processUpdate(DocumentEvent event) {
                    filter = tfSearch.getText().trim();
                    searchTask.schedule(350);
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

    private final class PopupActionSupport extends MouseAdapter {
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
                assert action instanceof RowTabAction : "Need RowTabAction and not " + action.getClass().getName();
                RowTabAction rowAction = (RowTabAction) action;
                rowAction.unitChanged(row, u);
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
            Set<String> categories2 = new HashSet<String>(actions.length);
            List<String> categories = new ArrayList<String>(actions.length);
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
                        assert action instanceof RowTabAction : "Need RowTabAction and not " + action.getClass().getName();
                        RowTabAction rowAction = (RowTabAction) action;
                        if (rowAction.isVisible()) {
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
        int row = table.rowAtPoint(e);
        if (row >= 0) {
            table.getSelectionModel().setSelectionInterval(row, row);
            JPopupMenu popup = popupActionsSupport.createPopup();
            if (popup.getComponentCount() > 0) {
                popup.show(invoker, e.x, e.y);

            }
        }
    }

    int getSelectedRow() {
        return table.getSelectedRow();
    }

    private void restoreSelectedRow(int row) {
        if (row < 0) {
            row = 0;
        }
        int rowCount = table.getRowCount();
        for (int temp = row; temp >= 0; temp--) {
            if (temp < rowCount && temp > -1) {
                table.getSelectionModel().setSelectionInterval(temp, temp);
                break;
            }
        }
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
        private final String name;
        private final String actionCategory;

        public TabAction(String nameKey, String actionCategoryKey) {
            super(textForKey(nameKey));
            actionCategory = actionCategoryKey; //(actionCategoryKey != null) ? NbBundle.getMessage(UnitTab.class, actionCategoryKey) : null;
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
            return getActionCategoryImpl();
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
            performerImpl();
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
        private volatile TransferFileUnit unit;
        private volatile int row;

        public RowTabAction(String nameKey, String actionCategoryKey) {
            super(nameKey, actionCategoryKey);
        }

        public RowTabAction(String nameKey, KeyStroke accelerator, String actionCategoryKey) {
            super(nameKey, accelerator, actionCategoryKey);
        }

        public void unitChanged(int row, TransferFileUnit unit) {
            this.unit = unit;
            this.row = row;
            unitChanged();
        }

        public final boolean isVisible() {
            return (unit != null) ? isVisible(unit) : isVisible(row);
        }

        private void unitChanged() {
            if (unit != null) {
                setEnabled(isEnabled(unit));
                setContextName(getContextName(unit));
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
            performerImpl(unit);
            model.fireUpdataUnitChange();
        }

        protected boolean isVisible(TransferFileUnit unit) {
            return unit != null;
        }

        protected boolean isVisible(int row) {
            return false;
        }

        public abstract void performerImpl(TransferFileUnit unit);

        protected abstract boolean isEnabled(TransferFileUnit unit);

        protected boolean isEnabled(int row) {
            return false;
        }

        protected abstract String getContextName(TransferFileUnit unit);

        protected String getContextName(int row) {
            return getActionName();
        }
    }

    private class UncheckAllAction extends RowTabAction {
        private static final long serialVersionUID = -1506415995282022116L;

        public UncheckAllAction() {
            super("FileConfirmationPane_UncheckAllAction", "Uncheck"); // NOI18N
        }

        public void performerImpl(TransferFileUnit notUsed) {
            final int row = getSelectedRow();
            Collection<TransferFileUnit> allUnits = model.getVisibleFileUnits();
            for (TransferFileUnit tfu : allUnits) {
                if (tfu.isMarked()  && tfu.canBeMarked()) {
                    tfu.setMarked(false);
                }
            }
            model.fireTableDataChanged();
            restoreSelectedRow(row);
        }

        @Override
        protected boolean isEnabled(TransferFileUnit unit) {
            return true;
        }

        @Override
        protected String getContextName(TransferFileUnit unit) {
            return getActionName();
        }
    }

    private  class CheckAllAction extends RowTabAction {
        private static final long serialVersionUID = -2771565736665639L;

        public CheckAllAction() {
            super("FileConfirmationPane_CheckAllAction", "Check"); // NOI18N
        }

        @Override
        public void performerImpl(TransferFileUnit notUsed) {
            final int row = getSelectedRow();
            Collection<TransferFileUnit> allUnits = model.getVisibleFileUnits();
            for (TransferFileUnit tfu : allUnits) {
                if (!tfu.isMarked() &&  tfu.canBeMarked()) {
                    tfu.setMarked(true);
                }
            }
            model.fireTableDataChanged();
            restoreSelectedRow(row);
        }

        @Override
        protected boolean isEnabled(TransferFileUnit unit) {
            return true;
        }

        @Override
        protected String getContextName(TransferFileUnit unit) {
            return getActionName();
        }
    }

    private class CheckAction extends RowTabAction {
        private static final long serialVersionUID = 3205317962231792782L;

        public CheckAction() {
            super("FileConfirmationPane_CheckAction", KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), null); // NOI18N
        }

        public void performerImpl(TransferFileUnit unit) {
            final int row = getSelectedRow();
            if (unit != null && unit.canBeMarked()) {
                unit.setMarked(!unit.isMarked());
            }
            model.fireTableDataChanged();
            restoreSelectedRow(row);
        }

        protected boolean isEnabled(TransferFileUnit unit) {
            return unit.canBeMarked();
        }

        protected String getContextName(TransferFileUnit unit) {
            return getActionName();
        }

        @Override
        protected boolean isVisible(TransferFileUnit unit) {
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
