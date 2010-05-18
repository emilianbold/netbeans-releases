/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionFactory;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.api.DataView;
import org.netbeans.modules.dm.virtual.db.model.DBExplorerUtil;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionDefinition;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class PreviewDataPanel extends JPanel implements ActionListener {

    private static final String LOG_CATEGORY = PreviewDataPanel.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(PreviewDataPanel.class.getName());
    private VirtualDBTable currentTable;
    private JButton previewBtn;
    private JTextField recordCount;
    private VirtualDBColumnTableModel tableModel;
    private JLabel parseErrorMessage;

    public PreviewDataPanel(VirtualDBTable table) {
        currentTable = table;
        String previewLabel = NbBundle.getMessage(PreviewDataPanel.class, "LBL_import_preview_table");
        setBorder(BorderFactory.createTitledBorder(previewLabel));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setPreferredSize(new Dimension(205, 120));
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == recordCount) {
            previewBtn.requestFocusInWindow();
            previewBtn.doClick();
        } else if (src == previewBtn) {
            tryResulSet();
        }
    }

    public boolean tryResulSet() {
        int ct = 25;
        boolean isValid = true;

        // Call helper to obtain result set and display.
        if (tableModel != null && !tableModel.getRowEntries().isEmpty()) {
            tableModel.updateColumns(currentTable);
        }

        VirtualDBTable table = (VirtualDBTable) ((VirtualDBTable) currentTable).clone();

        table.setProperty(PropertyKeys.WIZARDFIELDCOUNT, new Integer(table.getColumnList().size()));
        String fname = table.getProperty(PropertyKeys.FILENAME);
        if (fname == null || fname.equals("")) {
            if (table.getLocalFilePath() != null && !table.getLocalFilePath().equals("") &&
                    table.getFileName() != null && !table.getFileName().equals("")) {
                ((VirtualDBTable) table).setOrPutProperty(PropertyKeys.FILENAME,
                        (new File(table.getLocalFilePath(), table.getFileName())).getAbsolutePath());
            }
        }
        String fileName = VirtualDBUtil.escapeControlChars(table.getProperty("URL"));
        ((VirtualDBTable) table).setOrPutProperty(PropertyKeys.FILENAME, fileName);
        if (table.getParserType().equalsIgnoreCase(PropertyKeys.WEB) ||
                table.getParserType().equalsIgnoreCase(PropertyKeys.RSS)) {
            Map properties = ((VirtualDBTable) table).getProperties();
            properties.remove(PropertyKeys.FILENAME);
            ((VirtualDBTable) table).setProperties(properties);
        }

        Connection conn = null;
        DatabaseConnection previewDBConn = null;
        File previewDir = new File(System.getProperty("java.io.tmpdir"), ".preview");
        File metadataDir = new File(previewDir, ".metadata");

        File lockFile = new File(metadataDir, "lockfile.txt");
        VirtualDBConnectionFactory factory = VirtualDBConnectionFactory.getInstance();
        Object oldLockFlag = factory.getIgnoreLockProperty();
        try {
            factory.setIgnoreLockProperty("true");

            if (lockFile.exists()) {
                lockFile.delete();
            }

            if (!metadataDir.exists()) {
                metadataDir.mkdirs();
            }

            String url = VirtualDBConnectionFactory.VIRTUAL_DB_URL_PREFIX + NbBundle.getMessage(PreviewDataPanel.class, "LBL_preview") + metadataDir;
            mLogger.log(Level.INFO, NbBundle.getMessage(PreviewDataPanel.class, "LOG_PreviewURL", url));
            conn = factory.getConnection(url);
            Statement stmt = conn.createStatement();

            stmt.execute("DROP TABLE IF EXISTS " + table.getTableName());
            String create = table.getCreateStatementSQL();

            mLogger.log(Level.INFO, NbBundle.getMessage(PreviewDataPanel.class, "LOG_CreateStatement", create));
            stmt.execute(create);

            previewDBConn = DBExplorerUtil.createDatabaseConnection(VirtualDBConnectionDefinition.AXION_DRIVER, url, "sa", "sa", false);

            DataView dview = DataView.create(previewDBConn, table.getSelectStatementSQL(ct), ct, true);
            this.removeAll();
            this.add(dview.createComponents().get(0));

            // get the count of all rows
            String countSql = "Select count(*) From " + table.getName();
            mLogger.log(Level.INFO, NbBundle.getMessage(PreviewDataPanel.class, "LOG_CountStatement", countSql));
            stmt = conn.createStatement();


            stmt.execute("DROP TABLE " + table.getTableName());
        } catch (NoSuchElementException nse) {
            String errorMsg = NbBundle.getMessage(PreviewDataPanel.class, "MSG_SampleFileError");
            String nbBundle2 = NbBundle.getMessage(PreviewDataPanel.class, "MSG_SamplePreview");
            try {
                errorMsg = nbBundle2;
            } catch (MissingResourceException mre) {
                mLogger.log(Level.INFO, NbBundle.getMessage(PreviewDataPanel.class, "LOG_BadPreview", mre));
            }
            showError(NbBundle.getMessage(PreviewDataPanel.class, "MSG_SampleFile_corrupt"), errorMsg, nse);
            isValid = false;
        } catch (SQLException se) {
            String errorMsg = NbBundle.getMessage(PreviewDataPanel.class, "MSG_SampleFileError");
            String sqlExMsg = NbBundle.getMessage(PreviewDataPanel.class, "MSG_SampleFile_corrupt");
            try {
                sqlExMsg = stripExceptionHeaderFromMessage(se);
                String nbBundle3 = NbBundle.getMessage(PreviewDataPanel.class, "MSG_SampleFilePreview", sqlExMsg);
                errorMsg = nbBundle3;
            } catch (MissingResourceException mre) {
                mLogger.log(Level.SEVERE, NbBundle.getMessage(PreviewDataPanel.class, "LOG_BadPreview", LOG_CATEGORY), mre);
            }
            showError(sqlExMsg, errorMsg, se);
            isValid = false;
        } catch (Exception t) {
            String errMsg = NbBundle.getMessage(PreviewDataPanel.class, "MSG_ERROR_ObtainingResultSet");
            showError(NbBundle.getMessage(PreviewDataPanel.class, "MSG_Unknownerror") + t.getMessage(), errMsg, unwrapThrowable(t));
            isValid = false;
        } finally {
            table.setProperty(PropertyKeys.FILENAME, table.getFileName());
            if (conn != null) {
                try {
                    conn.createStatement().execute(NbBundle.getMessage(PreviewDataPanel.class, "CMD_shutdown"));
                    conn.close();
                } catch (SQLException ignore) {
                    // ignore
                }
            }

            if (lockFile != null && lockFile.exists()) {
                lockFile.delete();
            }

            if (metadataDir != null && metadataDir.exists()) {
                metadataDir.deleteOnExit();
            }

            if (previewDir != null && previewDir.exists()) {
                previewDir.deleteOnExit();
            }

            factory.setIgnoreLockProperty(oldLockFlag);
        }

        return isValid;
    }

    private void showError(String shortErrMsg, String errorMsg, Throwable t) {
        if (parseErrorMessage != null) {
            parseErrorMessage.setText(shortErrMsg);
            parseErrorMessage.revalidate();
            parseErrorMessage.repaint();
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg, NotifyDescriptor.WARNING_MESSAGE));
        }
        setEnabled(false);
        mLogger.log(Level.SEVERE, NbBundle.getMessage(PreviewDataPanel.class, "LOG_errorMsg", LOG_CATEGORY), t);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (previewBtn != null) {
            previewBtn.removeActionListener(this);
            previewBtn.addActionListener(this);
        }
    }

    public void clearData() {
    }

    @Override
    public void removeNotify() {
        if (previewBtn != null) {
            previewBtn.removeActionListener(this);
        }
        super.removeNotify();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (previewBtn != null) {
            previewBtn.setEnabled(enabled);
        }

        if (recordCount != null) {
            recordCount.setEnabled(enabled);
        }
    }

    public void setTable(VirtualDBTable table) {
        currentTable = table;
    }

    public void setTableModel(VirtualDBColumnTableModel model) {
        tableModel = model;
    }

    public boolean showData(JLabel parseErrMsg) {
        if (currentTable != null && !currentTable.getColumnList().isEmpty()) {
            parseErrorMessage = parseErrMsg;
            return tryResulSet();
        }
        return false;
    }

    private String stripExceptionHeaderFromMessage(Exception e) {
        String cookedMsg = null;
        String rawSqlMsg = e.getMessage();
        if (!VirtualDBUtil.isNullString(rawSqlMsg)) {
            int beginIndex = rawSqlMsg.lastIndexOf(":");
            int endIndex = rawSqlMsg.lastIndexOf(")") - 1;
            if (rawSqlMsg.length() == beginIndex) {
                beginIndex = -1;
            }

            if (endIndex < beginIndex || endIndex == -1) {
                endIndex = rawSqlMsg.length();
            }
            cookedMsg = rawSqlMsg.substring(beginIndex + 1, endIndex);
        }
        return cookedMsg;
    }

    private Throwable unwrapThrowable(Throwable t) {
        // Drill down to the root cause, if available.
        while (t.getCause() != null) {
            t = t.getCause();
            // Prevent infinite loops.
            if (t.getCause() == t) {
                break;
            }
        }
        return t;
    }
}

