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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.notifications.NotificationDisplayerImpl;
import org.netbeans.modules.notifications.NotificationImpl;
import org.netbeans.swing.etable.ETable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.netbeans.modules.notifications//NotificationCenter//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "NotificationCenterTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.netbeans.modules.notifications.NotificationCenterTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_NotificationCenterAction",
        preferredID = "NotificationCenterTopComponent")
public final class NotificationCenterTopComponent extends TopComponent {

    private static final int PREVIEW_DETAILS_REFRESH_DELAY = 300;
    private static final int FILTER_REFRESH_DELAY = 300;
    private NotificationCenterManager notificationManager;
    private JPanel detailsPanel;
    private NotificationTable notificationTable;
    private Timer previewRefreshTimer;
    private JScrollPane notificationScroll;
    private SearchField searchField;
    private final Timer filterTimer;
    private final SearchDocumentListener searchDocumentListener;

    public NotificationCenterTopComponent() {
        initComponents();
        init();
        filterTimer = new Timer(FILTER_REFRESH_DELAY, new SearchTimerListener());
        filterTimer.stop();
        searchDocumentListener = new SearchDocumentListener(filterTimer);
        setName(NbBundle.getMessage(NotificationCenterTopComponent.class, "CTL_NotificationCenterTopComponent"));
        setToolTipText(NbBundle.getMessage(NotificationCenterTopComponent.class, "HINT_NotificationCenterTopComponent"));
    }

    private void init() {
        detailsPanel = new JPanel(new GridLayout(1, 1));
        splitPane.setRightComponent(detailsPanel);

        notificationManager = NotificationCenterManager.getInstance();
        initLeft();
    }

    private void initLeft() {
        final JPanel pnlLeft = new JPanel(new GridBagLayout());
        notificationTable = (NotificationTable) notificationManager.getComponent();
        initNotificationTable();
        notificationScroll = new JScrollPane(notificationTable);
        JComponent toolbar = createToolbar();
        pnlLeft.add(toolbar, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(3, 3, 0, 3), 0, 0));

        pnlLeft.add(notificationScroll, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0));
        SwingUtilities.invokeLater(new Runnable() {
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

    private JComponent createToolbar() {
        JPanel pnlToolbar = new JPanel(new GridBagLayout());
        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.setFocusable(false);
        toolBar.setFloatable(false);
        toolBar.add(new FiltersMenuButton(notificationManager.getActiveFilter()));
        pnlToolbar.add(toolBar, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        searchField = new SearchField();
        pnlToolbar.add(searchField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        return pnlToolbar;
    }

    private NotificationImpl getSelectedNotification() {
        int selectedRowIndex = notificationTable.getSelectedRow();
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();

        splitPane.setContinuousLayout(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 904, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        String dummyText = "<html>The Netbeans IDE has detected that your system is using most of your available system resources. We recommend shutting down other applications and windows.</html>";
        // TODO add custom code on component opening
        NotificationDisplayerImpl.getInstance().notify("Test notification aaaaaa ",
                new ImageIcon(ImageUtilities.loadImage("org/openide/awt/resources/unknown.gif")),
                new JLabel(dummyText), new JLabel(dummyText),
                NotificationDisplayer.Priority.NORMAL,
                NotificationDisplayer.Category.INFO);
        NotificationDisplayerImpl.getInstance().notify("IMPORTANT Test notification aaaaaa",
                new ImageIcon(ImageUtilities.loadImage("org/openide/awt/resources/unknown.gif")),
                new JLabel(dummyText), new JLabel(dummyText),
                NotificationDisplayer.Priority.HIGH,
                NotificationDisplayer.Category.WARNING);
        NotificationDisplayerImpl.getInstance().notify("LESS IMPORTANT Test notification cccc",
                new ImageIcon(ImageUtilities.loadImage("org/openide/awt/resources/unknown.gif")),
                new JLabel(dummyText), new JLabel(dummyText),
                NotificationDisplayer.Priority.HIGH,
                NotificationDisplayer.Category.INFO);

        searchField.addDocumentListener(searchDocumentListener);
    }

    @Override
    public void componentClosed() {
        NotificationCenterManager.tcClosed();
        searchField.removeDocumentListener(searchDocumentListener);
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
