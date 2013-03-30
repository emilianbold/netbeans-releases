/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.notifications.center;

import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.notifications.NotificationImpl;
import org.netbeans.modules.notifications.NotificationSettings;
import org.netbeans.swing.etable.ETable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.CloseButtonFactory;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.netbeans.modules.notifications//NotificationCenter//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "NotificationCenterTopComponent",
        iconBase = "org/netbeans/modules/notifications/resources/notificationsTC.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false, position = 123)
@ActionID(category = "Window", id = "org.netbeans.modules.notifications.NotificationCenterTopComponent")
@ActionReference(path = "Menu/Window/Tools", position = 650)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_NotificationCenterAction",
        preferredID = "NotificationCenterTopComponent")
public final class NotificationCenterTopComponent extends TopComponent {

    private static final int PREVIEW_DETAILS_REFRESH_DELAY = 300;
    private static final int FILTER_REFRESH_DELAY = 300;
    private static final int TABLE_REFRESH_PERIOD = 60000;
    private final NotificationCenterManager notificationManager;
    private JPanel detailsPanel;
    private NotificationTable notificationTable;
    private Timer previewRefreshTimer;
    private JScrollPane notificationScroll;
    private SearchField searchField;
    private final Timer filterTimer;
    private final Timer tableRefreshTimer;
    private final SearchDocumentListener searchDocumentListener;
    private final KeyListener escSearchKeyListener;
    private final KeyAdapter searchKeyAdapter;
    private final NotificationTable.ProcessKeyEventListener tableKeyListener;
    private JPanel pnlSearch;
    private JToggleButton btnSearch;

    public NotificationCenterTopComponent() {
        notificationManager = NotificationCenterManager.getInstance();
        filterTimer = new Timer(FILTER_REFRESH_DELAY, new SearchTimerListener());
        filterTimer.stop();
        tableRefreshTimer = new Timer(TABLE_REFRESH_PERIOD, new RefreshTimerListener());
        tableRefreshTimer.stop();
        searchDocumentListener = new SearchDocumentListener(filterTimer);
        escSearchKeyListener = new EscKeyListener();
        searchKeyAdapter = new SearchKeyAdapter();
        tableKeyListener = new TableKeyListener();
        setName(NbBundle.getMessage(NotificationCenterTopComponent.class, "CTL_NotificationCenterTopComponent"));
        setToolTipText(NbBundle.getMessage(NotificationCenterTopComponent.class, "HINT_NotificationCenterTopComponent"));
    }

    private void init() {
        initComponents();
        detailsPanel = new JPanel(new GridLayout(1, 1));
        splitPane.setRightComponent(detailsPanel);

        toolBar.setFocusable(false);
        toolBar.setFloatable(false);
        btnSearch = new JToggleButton(ImageUtilities.loadImageIcon("org/netbeans/modules/notifications/resources/find16.png", true));
        btnSearch.setToolTipText(NbBundle.getMessage(NotificationCenterTopComponent.class, "LBL_SearchToolTip"));
        btnSearch.setFocusable(false);
        btnSearch.setSelected(NotificationSettings.isSearchVisible());
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSearchVisible(btnSearch.isSelected());
            }
        });
        toolBar.add(btnSearch);
        toolBar.add(new FiltersMenuButton(notificationManager.getActiveFilter()));

        initLeft();
    }

    private void initLeft() {
        final JPanel pnlLeft = new JPanel(new GridBagLayout());
        notificationTable = (NotificationTable) notificationManager.getComponent();
        initNotificationTable();
        notificationScroll = new JScrollPane(notificationTable);

        pnlSearch = new JPanel(new GridBagLayout());
        searchField = new SearchField();
        pnlSearch.add(searchField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 6, 3, 3), 0, 0));

        pnlSearch.add(new JLabel(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 0, 0));

        JButton clearButton = CloseButtonFactory.createCloseButton();
        clearButton.addActionListener(new CloseSearchAction());
        pnlSearch.add(clearButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0));
        setSearchVisible(btnSearch.isSelected());
        
        pnlLeft.add(pnlSearch, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        pnlLeft.add(notificationScroll, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                splitPane.setLeftComponent(pnlLeft);
                splitPane.setDividerLocation(0.6);
                splitPane.validate(); // Have to validate to properly update column sizes
                updateTableColumnSizes();
            }
        });
    }

    private void initNotificationTable() {
        notificationTable.getActionMap().put("delete", new AbstractAction() { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                NotificationImpl notification = getSelectedNotification();
                if (notification != null) {
                    notification.clear();
                }
            }
        });
        notificationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                scheduleDetailsRefresh();
            }
        });
    }

    private void updateTableColumnSizes() {
        ETable table = notificationTable;
        Font font = notificationScroll.getFont();
        FontMetrics fm = notificationScroll.getFontMetrics(font.deriveFont(Font.BOLD));
        int maxCharWidth = fm.charWidth('A'); // NOI18N
        int inset = 10;
        TableColumnModel columnModel = table.getColumnModel();

        TableColumn priorityColumn = columnModel.getColumn(0);
        String priorName = priorityColumn.getHeaderValue().toString();
        priorityColumn.setPreferredWidth(fm.stringWidth(priorName) + inset);

        TableColumn dateColumn = columnModel.getColumn(2);
        dateColumn.setPreferredWidth(15 * maxCharWidth + inset);

        TableColumn categoryColumn = columnModel.getColumn(3);
        categoryColumn.setPreferredWidth(7 * maxCharWidth + inset);

        TableColumn messageColumn = columnModel.getColumn(1);
        Insets insets = notificationScroll.getBorder().getBorderInsets(notificationScroll);
        int remainingWidth = notificationScroll.getParent().getWidth() - insets.left - insets.right;
        remainingWidth -= 3 * columnModel.getColumnMargin();
        remainingWidth -= priorityColumn.getPreferredWidth();
        remainingWidth -= dateColumn.getPreferredWidth();
        remainingWidth -= categoryColumn.getPreferredWidth();
        messageColumn.setPreferredWidth(remainingWidth);
    }

    private NotificationImpl getSelectedNotification() {
        int selectedRowIndex = notificationTable.convertRowIndexToModel(notificationTable.getSelectedRow());
        if (selectedRowIndex != -1 && selectedRowIndex < notificationTable.getRowCount()) {
            return ((NotificationTableModel) notificationTable.getModel()).getEntry(selectedRowIndex);
        }
        return null;
    }

    private void scheduleDetailsRefresh() {
        if (previewRefreshTimer == null) {
            previewRefreshTimer = new Timer(PREVIEW_DETAILS_REFRESH_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showDetails();
                }
            });
            previewRefreshTimer.setRepeats(false);
        }
        previewRefreshTimer.restart();
    }

    private void showDetails() {
        NotificationImpl selected = getSelectedNotification();
        detailsPanel.removeAll();
        if (selected != null) {
            selected.markAsRead(true);
            JComponent popupComponent = selected.getDetailsComponent();
            detailsPanel.add(popupComponent);
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void setSearchVisible(boolean visible) {
        if (!visible) {
            searchField.clear();
        }
        pnlSearch.setVisible(visible);
        if (visible != btnSearch.isSelected()) {
            btnSearch.setSelected(visible);
        }
        this.revalidate();
        this.repaint();
        if (visible) {
            searchField.requestFocus();
        }
        NotificationSettings.setSearchVisible(visible);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        splitPane = new javax.swing.JSplitPane();
        toolBar = new javax.swing.JToolBar();

        setLayout(new java.awt.GridBagLayout());

        splitPane.setContinuousLayout(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(splitPane, gridBagConstraints);

        toolBar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolBar.setRollover(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(toolBar, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        removeAll();
        init();

        searchField.addDocumentListener(searchDocumentListener);
        searchField.addSearchKeyListener(escSearchKeyListener);
        notificationTable.addProcessKeyEventListener(tableKeyListener);
        tableRefreshTimer.restart();
    }

    @Override
    public void componentClosed() {
        NotificationCenterManager.tcClosed();
        searchField.removeDocumentListener(searchDocumentListener);
        searchField.removeSearchKeyListener(escSearchKeyListener);
        notificationTable.removeProcessKeyEventListener(tableKeyListener);
        tableRefreshTimer.stop();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private class TableKeyListener implements NotificationTable.ProcessKeyEventListener {

        @Override
        public void processKeyEvent(KeyEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (pnlSearch.isVisible()) {
                searchField.setCaretPosition(searchField.getText().length());
                searchField.processSearchKeyEvent(e);
                searchField.requestFocus();
            } else {
                switch (e.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        searchKeyAdapter.keyPressed(e);
                        break;
                    case KeyEvent.KEY_RELEASED:
                        searchKeyAdapter.keyReleased(e);
                        break;
                    case KeyEvent.KEY_TYPED:
                        searchKeyAdapter.keyTyped(e);
                        break;
                }
            }
        }
    }

    private class SearchKeyAdapter extends KeyAdapter {

        @Override
        public void keyTyped(KeyEvent e) {
            int modifiers = e.getModifiers();
            int keyCode = e.getKeyCode();
            char c = e.getKeyChar();

            //#43617 - don't eat + and -
            //#98634 - and all its duplicates dont't react to space
            if ((c == '+') || (c == '-') || (c == ' ')) {
                return; // NOI18N
            }
            if (((modifiers > 0) && (modifiers != KeyEvent.SHIFT_MASK)) || e.isActionKey()) {
                return;
            }
            if (Character.isISOControl(c)
                    || (keyCode == KeyEvent.VK_SHIFT)
                    || (keyCode == KeyEvent.VK_ESCAPE)) {
                return;
            }
            setSearchVisible(true);

            final KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
            searchField.setText(String.valueOf(stroke.getKeyChar()));
            e.consume();
        }
    }

    private class RefreshTimerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            notificationTable.revalidate();
            notificationTable.repaint();
        }
    }

    private class EscKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == Event.ESCAPE) {
                if (searchField.isEmpty()) {
                    setSearchVisible(false);
                } else {
                    searchField.clear();
                }
            }
        }
    }

    private class CloseSearchAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            setSearchVisible(false);
        }
    }

    private class SearchTimerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            notificationManager.setMessageFilter(searchField.getText().trim());
            filterTimer.stop();
        }
    }

    private class SearchDocumentListener implements DocumentListener {

        private final Timer timer;

        public SearchDocumentListener(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            timer.restart();
        }
    }
}
