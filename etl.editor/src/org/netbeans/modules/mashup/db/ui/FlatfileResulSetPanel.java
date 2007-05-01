/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import javax.swing.SwingUtilities;

import org.netbeans.modules.mashup.db.model.FlatfileDefinition;
import org.netbeans.modules.mashup.db.ui.model.FlatfileTable;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.netbeans.modules.sql.framework.ui.view.ResultSetTablePanel;
import org.openide.DialogDisplayer;

import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import com.sun.sql.framework.utils.Logger;

/**
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FlatfileResulSetPanel extends JPanel implements ActionListener, PropertyChangeListener {
    
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
            QueryViewWorkerThread queryThread = new QueryViewWorkerThread(node);
            queryThread.start();
        }
    }
    
    private class QueryViewWorkerThread extends SwingWorker {
        
        FlatfileNode node;
        private ResultSet cntRs;
        private Connection conn;
        private Throwable ex;
        private ResultSet rs;
        private Statement stmt;
        
        public QueryViewWorkerThread(FlatfileNode newNode) {
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
                    conn = otd.getJDBCConnection();
                    stmt = conn.createStatement();
                    String selectSQL = table.getSelectStatementSQL(ct);
                    Logger.print(Logger.DEBUG, FlatfileResulSetPanel.class.getName(), selectSQL);
                    rs = stmt.executeQuery(selectSQL);
                    recordViewer.clearView();
                    recordViewer.setResultSet(rs);
                    
                    // get the count of all rows
                    String countSql = "Select count(*) From " + table.getName();
                    Logger.print(Logger.DEBUG, FlatfileResulSetPanel.class.getName(), "Select count(*) statement used for total rows: \n" + countSql);
                    
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
                Logger.printThrowable(Logger.ERROR, FlatfileResulSetPanel.class.getName(), null, "Can't get contents for table ", e);
                recordViewer.clearView();
                totalRowsLabel.setText("0");
            }
            
            return "";
        }
        
        // Runs on the event-dispatching thread.
        public void finished() {
            try {
                if (this.ex != null) {
                    String errorMsg = NbBundle.getMessage(FlatfileResulSetPanel.class, "MSG_error_fetch_failed", this.ex.getMessage());
                    DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.ERROR_MESSAGE));
                }
                
                showDataBtn.setEnabled(true);
                recordCount.setEnabled(true);
                
                if (stmt != null) {
                    stmt.execute("shutdown");
                    stmt.close();
                }
                
            } catch (SQLException sqle) {
                Logger.printThrowable(Logger.ERROR, FlatfileResulSetPanel.class.getName(), null,
                        "Could not close statement after retrieving table contents.", sqle);
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
    
    public static Icon getOTDIcon() {
        Icon icon = null;
        try {
            icon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/mashup/db/ui/resource/images/OTD.png"));
            ;
        } catch (Exception ex) {
            // Log exception
        }
        return icon;
    }
    
    private FlatfileDefinition otd = null;
    private JTextField recordCount;
    private ResultSetTablePanel recordViewer;
    private JButton showDataBtn;
    private JLabel totalRowsLabel = null;
    
    private FlatfileTreeTableView treeView = null;
    
    public FlatfileResulSetPanel(FlatfileDefinition otdInstance) {
        super();
        otd = otdInstance;
        
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
        showDataBtn.setMnemonic('S');
        showDataBtn.setToolTipText("Show data for selected flat file table node");
        showDataBtn.setActionCommand(CMD_SHOW_DATA);
        
        JPanel recordCountPanel = new JPanel();
        recordCountPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        JLabel lbl = new JLabel("Limit number of rows");
        lbl.setDisplayedMnemonic('r');
        
        recordCountPanel.add(lbl);
        recordCount = new JTextField("25", 5);
        recordCountPanel.add(recordCount);
        lbl.setLabelFor(recordCount);
        
        // add total row count label
        JPanel totalRowsPanel = new JPanel();
        FlowLayout fl = new FlowLayout();
        fl.setAlignment(FlowLayout.LEFT);
        totalRowsPanel.setLayout(fl);
        
        JLabel totalRowsNameLabel = new JLabel("Total rows:");
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

