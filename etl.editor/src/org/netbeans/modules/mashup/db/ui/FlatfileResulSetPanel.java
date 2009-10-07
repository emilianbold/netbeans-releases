/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.mashup.db.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.netbeans.modules.mashup.db.model.FlatfileDefinition;
import org.netbeans.modules.mashup.db.ui.model.FlatfileTable;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.netbeans.modules.sql.framework.ui.output.dataview.ResultSetTablePanel;
import org.openide.DialogDisplayer;

import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FlatfileResulSetPanel extends JPanel implements ActionListener, PropertyChangeListener {

    private static transient final Logger mLogger = Logger.getLogger(FlatfileResulSetPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    private class FlatfileTableQuery {

        public FlatfileTableQuery() {
        }

        public void generateResult(final FlatfileNode node) {
            String title = "Loading flat file table";
            String msg = "Loading data from flat file table: " + node.getName();
            UIUtil.startProgressDialog(title, msg);
            generateData(node);
        }

        private void generateData(FlatfileNode node) {
            showDataBtn.setEnabled(false);
            recordCount.setEnabled(false);
            DataViewWorkerThread queryThread = new DataViewWorkerThread(node);
            queryThread.start();
        }
    }

    private class DataViewWorkerThread extends SwingWorker {

        FlatfileNode node;
        private ResultSet cntRs;
        private Connection conn;
        private Throwable ex;
        private ResultSet rs;
        private Statement stmt;

        public DataViewWorkerThread(FlatfileNode newNode) {
            node = newNode;
        }

        public Object construct() {

            int ct = 25;

            try {
                ct = Integer.parseInt(recordCount.getText());
            } catch (NumberFormatException nfe) {
                recordCount.setText(String.valueOf(25));
            }

            try {
                Object obj = node.getUserObject();
                if (obj instanceof FlatfileTable) {
                    FlatfileTable table = (FlatfileTable) obj;
                    conn = db.getJDBCConnection();
                    stmt = conn.createStatement();
                    String selectSQL = table.getSelectStatementSQL(ct);
                    mLogger.infoNoloc(mLoc.t("EDIT084: FlatfileResulSetPanel.class.getName(){0}", selectSQL));
                    rs = stmt.executeQuery(selectSQL);
                    recordViewer.clearView();
                    recordViewer.setResultSet(rs);

                    // get the count of all rows
                    String countSql = "Select count(*) From " + table.getName();
                    mLogger.infoNoloc(mLoc.t("EDIT085: Select count(*) statement used for total rows: {0}", countSql));
                    stmt = conn.createStatement();
                    cntRs = stmt.executeQuery(countSql);

                    // set the count
                    if (cntRs == null) {
                        totalRowsLabel.setText("");
                    } else {
                        if (cntRs.next()) {
                            int count = cntRs.getInt(1);
                            totalRowsLabel.setText(String.valueOf(count));
                        }
                    }

                } else {
                    totalRowsLabel.setText("");
                    recordViewer.clearView();
                }

            } catch (Exception e) {
                this.ex = e;
                mLogger.errorNoloc(mLoc.t("EDIT086: Can't get contents for table:{0}", FlatfileResulSetPanel.class.getName()), e);
                recordViewer.clearView();
                totalRowsLabel.setText("0");
            }

            return "";
        }

        // Runs on the event-dispatching thread.
        public void finished() {
            try {
                if (this.ex != null) {
                    String nbBundle3 = mLoc.t("BUND260: Error fetching data for tableCause: {0}",this.ex.getMessage());
                    String errorMsg = nbBundle3.substring(15); // NOI18N
                    DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.ERROR_MESSAGE));
                }

                showDataBtn.setEnabled(true);
                recordCount.setEnabled(true);

                if (stmt != null) {
                    stmt.close();
                }

            } catch (SQLException sqle) {
                mLogger.errorNoloc(mLoc.t("EDIT087: Could not close statement after retrieving table contents {0}", FlatfileResulSetPanel.class.getName()), sqle);
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                    }
                    conn = null;
                }
                UIUtil.stopProgressDialog();
            }
        }
    }
    private static final String CMD_SHOW_DATA = "Show Data"; // NOI18N

    public static Icon getDbIcon() {
        Icon icon = null;
        try {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/mashup/db/ui/resource/images/root.png", false);
        } catch (Exception ex) {
            // Log exception
        }
        return icon;
    }
    private FlatfileDefinition db = null;
    private JTextField recordCount;
    private ResultSetTablePanel recordViewer;
    private JButton showDataBtn;
    private JLabel totalRowsLabel = null;
    private FlatfileTreeTableView treeView = null;

    public FlatfileResulSetPanel(FlatfileDefinition dbInstance) {
        super();
        db = dbInstance;

        this.setBorder(BorderFactory.createTitledBorder("Selected Table Content"));
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(createControlPanel());

        recordViewer = new ResultSetTablePanel();
        this.add(recordViewer);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e ActionEvent to handle
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == recordCount) {
            showDataBtn.requestFocusInWindow();
            showDataBtn.doClick();
        } else if (src == showDataBtn) {
            new FlatfileTableQuery().generateResult((FlatfileNode) treeView.getCurrentNode());
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() == treeView) {
            new FlatfileTableQuery().generateResult((FlatfileNode) event.getNewValue());
        }
    }

    public void addActionListener(ActionListener listener) {
        showDataBtn.addActionListener(listener);
        recordCount.addActionListener(listener);
    }

    public void generateResult(FlatfileNode node) {
        new FlatfileTableQuery().generateResult(node);
    }

    public void setTreeView(FlatfileTreeTableView view) {
        this.treeView = view;
    }

    /*
     * Creates show data button and row count text field to control display of selected
     * table.
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

        // add refresh button
        URL url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/refresh16.png");
        showDataBtn = new JButton(new ImageIcon(url));
        String nbBundle30 = mLoc.t("BUND261: Show data for selected flat file table node");
        showDataBtn.getAccessibleContext().setAccessibleName(nbBundle30.substring(15));
        showDataBtn.setToolTipText(nbBundle30.substring(15));
        showDataBtn.setMnemonic(nbBundle30.substring(15).charAt(0));
        showDataBtn.setActionCommand(CMD_SHOW_DATA);

        JPanel recordCountPanel = new JPanel();
        recordCountPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        String nbBundle31 = mLoc.t("BUND262: Limit number of rows");
        JLabel lbl = new JLabel(nbBundle31.substring(15));
        lbl.getAccessibleContext().setAccessibleName(nbBundle31.substring(15));
        lbl.setDisplayedMnemonic(nbBundle31.substring(15).charAt(0));

        recordCountPanel.add(lbl);
        recordCount = new JTextField("25", 5);
        recordCountPanel.add(recordCount);
        lbl.setLabelFor(recordCount);

        // add total row count label
        JPanel totalRowsPanel = new JPanel();
        FlowLayout fl = new FlowLayout();
        fl.setAlignment(FlowLayout.LEFT);
        totalRowsPanel.setLayout(fl);

        String nbBundle51 = mLoc.t("BUND263: Total rows:");
        JLabel totalRowsNameLabel = new JLabel(nbBundle51.substring(15));
        totalRowsNameLabel.getAccessibleContext().setAccessibleName(nbBundle51.substring(15));
        totalRowsNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 8));
        totalRowsPanel.add(totalRowsNameLabel);

        totalRowsLabel = new JLabel();
        totalRowsPanel.add(totalRowsLabel);

        controlPanel.add(showDataBtn);
        controlPanel.add(recordCountPanel);
        controlPanel.add(totalRowsPanel);

        this.addActionListener(this);

        return controlPanel;
    }
}

