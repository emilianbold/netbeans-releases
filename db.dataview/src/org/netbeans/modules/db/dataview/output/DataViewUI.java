/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR parent HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of parent file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use parent file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include parent License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates parent
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied parent code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of parent file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include parent software in parent distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of parent file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.output;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.netbeans.modules.db.dataview.table.MultiColPatternFilter;
import org.netbeans.modules.db.dataview.table.ResultSetJXTable;
import static org.netbeans.modules.db.dataview.table.SuperPatternFilter.MODE.REGEX_FIND;
import org.openide.util.NbBundle;

/**
 * DataViewUI hosting display of design-level SQL test output.
 *
 * @author Ahimanikya Satapathy
 */
class DataViewUI extends JXPanel {

    private JXButton commit;
    private JXButton refreshButton;
    private JXButton truncateButton;
    private JXButton next;
    private JXButton last;
    private JXButton previous;
    private JXButton first;
    private JXButton deleteRow;
    private JXButton insert;
    private FixedSizeTextField refreshField;
    private JTextField matchBoxField;
    private JXLabel totalRowsLabel;
    private JXLabel limitRow;
    private JXButton[] editButtons = new JXButton[5];
    private DataViewTablePanel dataPanel;
    private final DataView dataView;
    private JXButton cancel;
    private DataViewActionHandler actionHandler;
    private String imgPrefix = "/org/netbeans/modules/db/dataview/images/"; // NOI18N

    private static final int MAX_TAB_LENGTH = 25;

    /** Shared mouse listener used for setting the border painting property
     * of the toolbar buttons and for invoking the popup menu.
     */
    private static final MouseListener sharedMouseListener = new org.openide.awt.MouseUtils.PopupMouseAdapter() {

        @Override
        public void mouseEntered(MouseEvent evt) {
            Object src = evt.getSource();

            if (src instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) evt.getSource();
                if (button.isEnabled()) {
                    button.setContentAreaFilled(true);
                    button.setBorderPainted(true);
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            Object src = evt.getSource();
            if (src instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) evt.getSource();
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
            }
        }

        protected void showPopup(MouseEvent evt) {
        }
    };

    DataViewUI(DataView dataView, boolean nbOutputComponent) {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT thread";  //NOI18N

        this.dataView = dataView;

        //do not show tab view if there is only one tab
        this.putClientProperty("TabPolicy", "HideWhenAlone"); //NOI18N

        this.putClientProperty("PersistenceType", "Never"); //NOI18N

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder());
        String sql = dataView.getSQLString();
        if (sql.length() > MAX_TAB_LENGTH) {
            String trimmed = NbBundle.getMessage(DataViewUI.class, "DataViewUI_TrimmedTabName", sql.substring(0, Math.min(sql.length(), MAX_TAB_LENGTH)));
            this.setName(trimmed);
        } else {
            this.setName(sql);
        }
        this.setToolTipText(sql);

        // Main pannel with toolbars
        JPanel panel = initializeMainPanel(nbOutputComponent);
        this.add(panel, BorderLayout.NORTH);

        actionHandler = new DataViewActionHandler(this, dataView);

        //add resultset data panel
        dataPanel = new DataViewTablePanel(dataView, this, actionHandler);
        this.add(dataPanel, BorderLayout.CENTER);
        dataPanel.revalidate();
        dataPanel.repaint();
    }

    JButton[] getEditButtons() {
        return editButtons;
    }

    void setEditable(boolean editable) {
        dataPanel.setEditable(editable);
    }

    boolean isEditable() {
        return dataPanel.isEditable();
    }

    void setTotalCount(int count) {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT thread";  //NOI18N
        if (count < 0) {
            int pageSize = dataView.getDataViewPageContext().getPageSize();
            int totalRows = dataView.getDataViewPageContext().getCurrentRows().size();
            String NA = NbBundle.getMessage(DataViewUI.class, "LBL_not_available");
            totalRowsLabel.setText(totalRows < pageSize ? totalRows + "" : NA);
        } else {
            totalRowsLabel.setText(count + "   " + dataView.getDataViewPageContext().pageOf());
        }
    }

    boolean isCommitEnabled() {
        return commit.isEnabled();
    }

    DataViewTableUI getDataViewTableUI() {
        return dataPanel.getDataViewTableUI();
    }

    UpdatedRowContext getUpdatedRowContext() {
        return dataPanel.getUpdatedRowContext();
    }

    void setCommitEnabled(boolean flag) {
        commit.setEnabled(flag);
    }

    void setCancelEnabled(boolean flag) {
        cancel.setEnabled(flag);
    }

    void setDataRows(List<Object[]> rows) {
        dataPanel.createTableModel(rows);
    }

    void resetValueAt(int row, int col) {
        Object val = dataView.getDataViewPageContext().getColumnData(row, col);
        dataPanel.setValueAt(val, row, col);
    }

    void syncPageWithTableModel() {
        List<Object[]> newrows = dataPanel.getPageDataFromTable();
        List<Object[]> oldRows = dataView.getDataViewPageContext().getCurrentRows();

        for (Integer row : dataView.getUpdatedRowContext().getUpdateKeys()) {
            newrows.set(row, oldRows.get(row));
        }
        dataView.getDataViewPageContext().setCurrentRows(newrows);
    }

    void disableButtons() {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT thread";  //NOI18N

        truncateButton.setEnabled(false);
        refreshButton.setEnabled(false);
        refreshField.setEnabled(false);
        matchBoxField.setEditable(false);

        first.setEnabled(false);
        previous.setEnabled(false);
        next.setEnabled(false);
        last.setEnabled(false);
        deleteRow.setEnabled(false);
        commit.setEnabled(false);
        cancel.setEnabled(false);
        insert.setEnabled(false);

        dataPanel.revalidate();
        dataPanel.repaint();
    }

    int getPageSize() {
        int pageSize = dataView.getDataViewPageContext().getPageSize();
        int totalCount = dataView.getDataViewPageContext().getTotalRows();
        try {
            int count = Integer.parseInt(refreshField.getText().trim());
            return count < 0 ? pageSize : count;
        } catch (NumberFormatException ex) {
            return totalCount < pageSize ? totalCount : pageSize;
        }
    }

    boolean isDirty() {
        return dataPanel.isDirty();
    }

    void resetToolbar(boolean wasError) {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT thread";  //NOI18N

        refreshButton.setEnabled(true);
        refreshField.setEnabled(true);
        matchBoxField.setEditable(true);
        deleteRow.setEnabled(false);
        DataViewPageContext dataPage = dataView.getDataViewPageContext();
        if (!wasError) {
            if (dataPage.hasPrevious()) {
                first.setEnabled(true);
                previous.setEnabled(true);
            }

            if (dataPage.hasNext()) {
                next.setEnabled(true);
                last.setEnabled(true);
            }

            if (dataPage.hasOnePageOnly()) {
                first.setEnabled(false);
                previous.setEnabled(false);
            }

            if (dataPage.isLastPage()) {
                next.setEnabled(false);
                last.setEnabled(false);
            }

            // editing controls
            if (!isEditable()) {
                commit.setEnabled(false);
                cancel.setEnabled(false);
                deleteRow.setEnabled(false);
                insert.setEnabled(false);
                truncateButton.setEnabled(false);
            } else {
                if (dataPage.hasRows()) {
                    truncateButton.setEnabled(true);
                } else {
                    deleteRow.setEnabled(false);
                    truncateButton.setEnabled(false);
                    dataPage.first();
                }
                insert.setEnabled(true);
                if (getUpdatedRowContext().getUpdateKeys().isEmpty()) {
                    commit.setEnabled(false);
                    cancel.setEnabled(false);
                } else {
                    commit.setEnabled(true);
                    cancel.setEnabled(true);
                }
            }
        } else {
            disableButtons();
        }

        refreshField.setText("" + dataPage.getPageSize());
        if (dataPanel != null) {
            dataPanel.revalidate();
            dataPanel.repaint();
        }
    }

    private ActionListener createOutputListener() {

        ActionListener outputListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object src = e.getSource();
                if (src.equals(refreshButton)) {
                    actionHandler.refreshActionPerformed();
                } else if (src.equals(first)) {
                    actionHandler.firstActionPerformed();
                } else if (src.equals(last)) {
                    actionHandler.lastActionPerformed();
                } else if (src.equals(next)) {
                    actionHandler.nextActionPerformed();
                } else if (src.equals(previous)) {
                    actionHandler.previousActionPerformed();
                } else if (src.equals(refreshField)) {
                    actionHandler.setMaxActionPerformed();
                } else if (src.equals(commit)) {
                    actionHandler.commitActionPerformed(false);
                } else if (src.equals(cancel)) {
                    actionHandler.cancelEditPerformed(false);
                } else if (src.equals(deleteRow)) {
                    actionHandler.deleteRecordActionPerformed();
                } else if (src.equals(insert)) {
                    actionHandler.insertActionPerformed();
                } else if (src.equals(truncateButton)) {
                    actionHandler.truncateActionPerformed();
                }
            }
        };

        return outputListener;
    }
    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);

    private void processButton(AbstractButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setMargin(BUTTON_INSETS);
        if (button instanceof AbstractButton) {
            button.addMouseListener(sharedMouseListener);
        }
        //Focus shouldn't stay in toolbar
        button.setFocusable(false);
    }

    private void initToolbar(JToolBar toolbar, ActionListener outputListener) {

        toolbar.addSeparator(new Dimension(10, 10));

        //add refresh button
        URL url = getClass().getResource(imgPrefix + "refresh.png"); // NOI18N
        refreshButton = new JXButton(new ImageIcon(url));
        refreshButton.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_refresh"));
        refreshButton.addActionListener(outputListener);
        processButton(refreshButton);

        toolbar.add(refreshButton);

        // add navigation buttons
        url = getClass().getResource(imgPrefix + "navigate_beginning.png"); // NOI18N
        first = new JXButton(new ImageIcon(url));
        first.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_first"));
        first.addActionListener(outputListener);
        first.setEnabled(false);
        processButton(first);
        toolbar.add(first);

        url = getClass().getResource(imgPrefix + "navigate_left.png"); // NOI18N
        previous = new JXButton(new ImageIcon(url));
        previous.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_previous"));
        previous.addActionListener(outputListener);
        previous.setEnabled(false);
        processButton(previous);
        toolbar.add(previous);

        url = getClass().getResource(imgPrefix + "navigate_right.png"); // NOI18N
        next = new JXButton(new ImageIcon(url));
        next.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_next"));
        next.addActionListener(outputListener);
        next.setEnabled(false);
        processButton(next);
        toolbar.add(next);

        url = getClass().getResource(imgPrefix + "navigate_end.png"); // NOI18N
        last = new JXButton(new ImageIcon(url));
        last.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_last"));
        last.addActionListener(outputListener);
        last.setEnabled(false);
        toolbar.add(last);
        processButton(last);
        toolbar.addSeparator(new Dimension(10, 10));

        //add limit row label
        limitRow = new JXLabel(NbBundle.getMessage(DataViewUI.class, "LBL_max_rows"));
        limitRow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(limitRow);

        //add refresh text field
        refreshField = new FixedSizeTextField(4);
        refreshField.setText("" + dataView.getDataViewPageContext().getPageSize()); // NOI18N
        refreshField.setMinimumSize(new Dimension(45, refreshField.getHeight()));
        refreshField.setSize(45, refreshField.getHeight());

        refreshField.addActionListener(outputListener);
        toolbar.add(refreshField);
        toolbar.addSeparator(new Dimension(10, 10));

        JXLabel totalRowsNameLabel = new JXLabel(NbBundle.getMessage(DataViewUI.class, "LBL_total_rows"));
        totalRowsNameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DataViewUI.class, "LBL_total_rows"));
        totalRowsNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        toolbar.add(totalRowsNameLabel);
        totalRowsLabel = new JXLabel();
        toolbar.add(totalRowsLabel);

        toolbar.addSeparator(new Dimension(10, 10));

        Box.Filler filler = new Box.Filler(new Dimension(getWidth(), getHeight()), new Dimension(800, getHeight()), new Dimension(getWidth(), getHeight()));
        toolbar.add(filler);

        // match box labble 
        JXLabel matchBoxRow = new JXLabel(NbBundle.getMessage(DataViewUI.class, "LBL_matchbox"));
        matchBoxRow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(matchBoxRow);

        //add matchbox text field
        matchBoxField = new JTextField(10);
        matchBoxField.setText(""); // NOI18N
        matchBoxField.setMinimumSize(new Dimension(35, matchBoxField.getHeight()));
        matchBoxField.setSize(35, matchBoxField.getHeight());

        matchBoxField.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                processKeyEvents();
            }

            public void keyPressed(KeyEvent e) {
                processKeyEvents();
            }

            public void keyReleased(KeyEvent e) {
                processKeyEvents();
            }
        });
        toolbar.add(matchBoxField);
    }

    private void processKeyEvents() {
        ResultSetJXTable table = getDataViewTableUI();
        int[] rows = new int[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++) {
            rows[i] = i;
        }
        {
            MultiColPatternFilter filterP = new MultiColPatternFilter(rows);
            filterP.setFilterStr(matchBoxField.getText(), REGEX_FIND);
            table.setFilters(new FilterPipeline(new Filter[]{filterP}));
        }
    }

    private void initVerticalToolbar(ActionListener outputListener) {

        URL url = getClass().getResource(imgPrefix + "row_add.png"); // NOI18N
        insert = new JXButton(new ImageIcon(url));
        insert.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_insert")+" (Alt+I)");
        insert.setMnemonic('I');
        insert.addActionListener(outputListener);
        insert.setEnabled(false);
        processButton(insert);
        editButtons[0] = insert;

        url = getClass().getResource(imgPrefix + "row_delete.png"); // NOI18N
        deleteRow = new JXButton(new ImageIcon(url));
        deleteRow.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_deleterow"));
        deleteRow.addActionListener(outputListener);
        deleteRow.setEnabled(false);
        processButton(deleteRow);
        editButtons[1] = deleteRow;

        url = getClass().getResource(imgPrefix + "row_commit.png"); // NOI18N
        commit = new JXButton(new ImageIcon(url));
        commit.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_commit_all"));
        commit.addActionListener(outputListener);
        commit.setEnabled(false);
        processButton(commit);
        editButtons[2] = commit;

        url = getClass().getResource(imgPrefix + "cancel_edits.png"); // NOI18N
        cancel = new JXButton(new ImageIcon(url));
        cancel.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_cancel_edits"));
        cancel.addActionListener(outputListener);
        cancel.setEnabled(false);
        processButton(cancel);
        editButtons[3] = cancel;

        //add truncate button
        url = getClass().getResource(imgPrefix + "table_truncate.png"); // NOI18N
        truncateButton = new JXButton(new ImageIcon(url));
        truncateButton.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_truncate_table")+" (Alt+T)");
        truncateButton.setMnemonic('T');
        truncateButton.addActionListener(outputListener);
        truncateButton.setEnabled(false);
        processButton(truncateButton);
        editButtons[4] = truncateButton;
    }

    private JPanel initializeMainPanel(boolean nbOutputComponent) {

        JXPanel panel = new JXPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        GridBagLayout gl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gl);

        ActionListener outputListener = createOutputListener();
        initVerticalToolbar(outputListener);
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        if (!nbOutputComponent) {
            JButton[] btns = getEditButtons();
            for (JButton btn : btns) {
                if (btn != null) {
                    toolbar.add(btn);
                }
            }
        }
        initToolbar(toolbar, outputListener);

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        panel.add(toolbar, c);
        this.validate();
        return panel;
    }

    public void enableDeleteBtn(boolean value) {
        deleteRow.setEnabled(value);
    }

    private class FixedSizeTextField extends JTextField {

        private int limit;

        FixedSizeTextField(int limit) {
            super();
            this.limit = limit;
            customize();
        }

        private void customize() {
            this.setDocument(new PlainDocument() {

                @Override
                public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                    if (getLength() + str.length() > limit) {
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        super.insertString(offset, str, a);

                    }
                }
            });

            this.addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(FocusEvent e) {
                    FixedSizeTextField.this.selectAll();
                }
            });
        }
    }
}
