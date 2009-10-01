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
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.netbeans.modules.mashup.db.common.FlatfileDBConnectionFactory;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.netbeans.modules.mashup.db.ui.FlatfileResulSetPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.ResultSetTablePanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import com.sun.sql.framework.utils.StringUtil;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class PreviewDataPanel extends JPanel implements ActionListener {

    private static final String CMD_SHOWDATA = "ShowData"; // NOI18N
    private static final String LOG_CATEGORY = PreviewDataPanel.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(PreviewDataPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private FlatfileDBTable currentTable;
    private JButton previewBtn;
    private JTextField recordCount;
    private ResultSetTablePanel recordViewer;
    private FlatfileColumnTableModel tableModel;
    private JLabel parseErrorMessage;
    private JLabel totalRowsLabel;

    public PreviewDataPanel(FlatfileDBTable table) {
        currentTable = table;
        String nbBundle1 = mLoc.t("BUND220: Preview Table Content");
        String previewLabel =  nbBundle1.substring(15);
        setBorder(BorderFactory.createTitledBorder(previewLabel));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(createPreviewControls());
        setPreferredSize(new Dimension(205, 120));
        recordViewer = new ResultSetTablePanel();
        add(recordViewer);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e ActionEvent to handle
     */
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

        try {
            ct = Integer.parseInt(recordCount.getText());
        } catch (NumberFormatException nfe) {
            recordCount.setText(String.valueOf(25));
        }

        // Call helper to obtain result set and display.
        if (tableModel != null && !tableModel.getRowEntries().isEmpty()) {
            tableModel.updateColumns(currentTable);
        }

        FlatfileDBTable table = (FlatfileDBTable) currentTable.clone();

        table.setProperty(PropertyKeys.WIZARDFIELDCOUNT, new Integer(table.getColumnList().size()));
        String fname = table.getProperty(PropertyKeys.FILENAME);
        if (fname == null || fname.equals("")) {
            if (table.getLocalFilePath() != null && !table.getLocalFilePath().equals("") &&
                    table.getFileName() != null && !table.getFileName().equals("")) {
                ((FlatfileDBTableImpl) table).setOrPutProperty(PropertyKeys.FILENAME,
                        (new File(table.getLocalFilePath(), table.getFileName())).getAbsolutePath());
            }
        }
        String fileName = StringUtil.escapeControlChars(table.getProperty("URL"));
        ((FlatfileDBTableImpl) table).setOrPutProperty(PropertyKeys.FILENAME, fileName);
        if (table.getParserType().equalsIgnoreCase(PropertyKeys.WEB) ||
                table.getParserType().equalsIgnoreCase(PropertyKeys.RSS)) {
            Map properties = ((FlatfileDBTableImpl) table).getProperties();
            properties.remove(PropertyKeys.FILENAME);
            ((FlatfileDBTableImpl) table).setProperties(properties);
        }

        Connection conn = null;
        File previewDir = new File(System.getProperty("java.io.tmpdir"), ".preview");
        File metadataDir = new File(previewDir, ".metadata");

        File lockFile = new File(metadataDir, "lockfile.txt");
        FlatfileDBConnectionFactory factory = FlatfileDBConnectionFactory.getInstance();
        Object oldLockFlag = factory.getIgnoreLockProperty();
        try {
            factory.setIgnoreLockProperty("true");

            if (lockFile.exists()) {
                lockFile.delete();
            }

            if (!metadataDir.exists()) {
                metadataDir.mkdirs();
            }

            String url = FlatfileDBConnectionFactory.DEFAULT_FLATFILE_JDBC_URL_PREFIX + "preview:" + metadataDir;
            mLogger.infoNoloc(mLoc.t("EDIT075: Preview URL: {0}", url));
            conn = FlatfileDBConnectionFactory.getInstance().getConnection(url);
            Statement stmt = conn.createStatement();

            stmt.execute("DROP TABLE IF EXISTS " + table.getTableName());
            String create = table.getCreateStatementSQL();

            mLogger.infoNoloc(mLoc.t("EDIT076: Generated create statement: {0}", create));
            stmt.execute(create);

            ResultSet rs = stmt.executeQuery(table.getSelectStatementSQL(ct));
            recordViewer.clearView();
            recordViewer.setResultSet(rs);

            // get the count of all rows
            String countSql = "Select count(*) From " + table.getName();
            mLogger.infoNoloc(mLoc.t("EDIT077: Select count(*) statement used for total rows:{0}", countSql));

            stmt = conn.createStatement();
            ResultSet cntRs = stmt.executeQuery(countSql);

            // set the count
            if (cntRs == null) {
                totalRowsLabel.setText("");
            } else {
                if (cntRs.next()) {
                    int count = cntRs.getInt(1);
                    totalRowsLabel.setText(String.valueOf(count));
                }
            }

            stmt.execute("DROP TABLE " + table.getTableName());
        } catch (NoSuchElementException nse) {
            String errorMsg = "ERROR: Current sample file may be corrupt, or does not match specified datatypes.";
            String nbBundle2 = mLoc.t("BUND221: Could not preview current sample file.One of the column datatypes may not match the sample data,or the sample file may be corrupt.Try changing a column datatype and preview again, or supply a different sample file.");
            try {
                errorMsg =  nbBundle2.substring(15);
            } catch (MissingResourceException mre) {
                mLogger.infoNoloc(mLoc.t("EDIT078: Could not locate resource string for ERROR_bad_preview:{0}", mre));

            }
            showError("Sample file may be corrupt, or does not match specified datatypes", errorMsg, nse);
            isValid = false;
        } catch (SQLException se) {
            String errorMsg = "ERROR: Current sample file may be corrupt, or does not match specified datatypes.";
            String sqlExMsg = "Sample file may be corrupt, or does not match specified datatypes";
            try {
                sqlExMsg = stripExceptionHeaderFromMessage(se);
                String nbBundle3 = mLoc.t("BUND222: Could not preview current sample file.One or more values for field length, scale, datatype, or nullability do not agree with the current sample file.Review your field specifications and click 'Preview' again.Error:{0}",sqlExMsg);
                errorMsg = nbBundle3.substring(15);
            } catch (MissingResourceException mre) {
                mLogger.errorNoloc(mLoc.t("EDIT079: Could not locate resource string for ERROR_bad_preview {0}", LOG_CATEGORY), mre);
            }
            showError(sqlExMsg, errorMsg, se);
            isValid = false;
        } catch (Exception t) {
            String errMsg = "Unknown error occurred while obtaining ResultSet. ";
            showError("Unknown error: " + t.getMessage(), errMsg, unwrapThrowable(t));
            isValid = false;
        } finally {
            table.setProperty(PropertyKeys.FILENAME, table.getFileName());
            if (conn != null) {
                try {
                    conn.createStatement().execute("shutdown");
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

            FlatfileDBConnectionFactory.getInstance().setIgnoreLockProperty(oldLockFlag);
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
        mLogger.errorNoloc(mLoc.t("EDIT080: errorMsg {0}", LOG_CATEGORY), t);
    }

    /**
     * Overrides parent implementation to allow for addition of this instance as a
     * listener for various child components.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        if (previewBtn != null) {
            previewBtn.removeActionListener(this);
            previewBtn.addActionListener(this);
        }
    }

    public void clearData() {
        if (recordViewer != null) {
            recordViewer.clearView();
        }
    }

    /**
     * Overrides parent implementation to allow for removal of this instance as a listener
     * for various child components.
     */
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

    public void setTable(FlatfileDBTable table) {
        currentTable = table;
    }

    public void setTableModel(FlatfileColumnTableModel model) {
        tableModel = model;
    }

    public boolean showData(JLabel parseErrMsg) {
        if (currentTable != null && !currentTable.getColumnList().isEmpty()) {
            parseErrorMessage = parseErrMsg;
            return tryResulSet();
        }
        return false;
    }

    /*
     * Creates preview button and row count text field to control display of parsed output
     * colMetaTable.
     */
    private JPanel createPreviewControls() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

        // add refresh button
        URL url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/refresh16.png");
        previewBtn = new JButton(new ImageIcon(url));
        String nbBundle30 = mLoc.t("BUND223: Show data for this table definition");
        previewBtn.setToolTipText(nbBundle30.substring(15));
        previewBtn.getAccessibleContext().setAccessibleName(nbBundle30.substring(15));
        previewBtn.setMnemonic(nbBundle30.substring(15).charAt(0));
        previewBtn.setActionCommand(CMD_SHOWDATA);
        previewBtn.addActionListener(this);

        JPanel recordCountPanel = new JPanel();
        recordCountPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        String nbBundle50 = mLoc.t("BUND224: Limit rows:");
        JLabel lbl = new JLabel(nbBundle50.substring(15));
        lbl.getAccessibleContext().setAccessibleName(nbBundle50.substring(15));
        lbl.setDisplayedMnemonic('l');
        recordCountPanel.add(lbl);
        recordCount = new JTextField("25", 5);
        recordCountPanel.add(recordCount);
        lbl.setLabelFor(recordCount);
        recordCount.addActionListener(this);

        // add total row count label
        JPanel totalRowsPanel = new JPanel();
        FlowLayout fl = new FlowLayout();
        fl.setAlignment(FlowLayout.LEFT);
        totalRowsPanel.setLayout(fl);

        String nbBundle40 = mLoc.t("BUND263: Total rows:");
        JLabel totalRowsNameLabel = new JLabel(nbBundle40.substring(15));
        totalRowsNameLabel.getAccessibleContext().setAccessibleName(nbBundle40.substring(15));
        totalRowsNameLabel.setDisplayedMnemonic(nbBundle40.substring(15).charAt(0));
        totalRowsNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        totalRowsPanel.add(totalRowsNameLabel);

        totalRowsLabel = new JLabel();
        totalRowsPanel.add(totalRowsLabel);

        controlPanel.add(previewBtn);
        controlPanel.add(recordCountPanel);
        controlPanel.add(totalRowsPanel);

        return controlPanel;
    }

    private String stripExceptionHeaderFromMessage(Exception e) {
        String cookedMsg = null;
        String rawSqlMsg = e.getMessage();
        if (!StringUtil.isNullString(rawSqlMsg)) {
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

