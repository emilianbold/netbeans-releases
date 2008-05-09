package org.netbeans.modules.mashup.tables.wizard;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JPanel;

import org.netbeans.modules.mashup.db.bootstrap.FlatfileBootstrapParserFactory;
import org.netbeans.modules.mashup.db.common.FlatfileDBConnectionFactory;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;

import com.sun.sql.framework.utils.StringUtil;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


public final class TableDetailsVisualPanel extends JPanel {

    private static transient final Logger mLogger = Logger.getLogger(TableDetailsVisualPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private TableDetailsPanel owner;
    private FlatfileDatabaseModel currentModel;
    private FlatfileDBTable currentTable;
    private String url;
    private static Map<String, String> encodingMap = new HashMap<String, String>();
    private static Map<String, String> typeMap = new HashMap<String, String>();

    static {
        encodingMap.put("ASCII (ISO646-US)", "US-ASCII");
    }

    static {
        typeMap.put("Spreadsheet (MS Excel)", PropertyKeys.SPREADSHEET);
        typeMap.put("Web Row Set", PropertyKeys.WEBROWSET);
        typeMap.put("Web (HTML)", PropertyKeys.WEB);
        typeMap.put("XML", PropertyKeys.XML);
        typeMap.put("Delimited Flatfile", PropertyKeys.DELIMITED);
        typeMap.put("Fixed Width Flatfile", PropertyKeys.FIXEDWIDTH);
        typeMap.put("RSS", PropertyKeys.RSS);
    }

    /**
     * Creates new form ChooseTableVisualPanel
     */
    public TableDetailsVisualPanel(TableDetailsPanel panel) {
        owner = panel;
        initComponents();
        tableName.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                checkTableName(tableName.getText().trim());
                owner.fireChangeEvent();
            }
        });
        setMinimumSize(new Dimension(100, 100));
        setMaximumSize(new Dimension(100, 100));
        setPreferredSize(new Dimension(100, 100));
    }

    @Override
    public String getName() {
        String nbBundle1 = mLoc.t("BUND296: Enter Table Details");
        return nbBundle1.substring(15);
    }

    /*
     * Checks whether the contents of the table name text field are valid. @return
     * INVALID_NAME if name is invalid, DUPLICATE_NAME if name is already in use in the
     * current MashupDatabaseModel, or OK if name is valid.
     */
    private boolean checkTableName(String tableName) {
        String newName = tableName.trim();

        if (!StringUtil.isValid(newName, "[A-Za-z][A-Za-z0-9_]*")) {
            setError("Invalid Table Name.");
            return false;
        } else if (!isUniqueTableName(newName)) {
            setError("Duplicate Table Name");
            return false;
        }

        if (isAxionReservedName(newName)) {
            setError("Reserved Table Name used.");
            return false;
        }
        setError("");
        return true;
    }

    public void setDBModel(FlatfileDatabaseModel model) {
        currentModel = model;
    }

    public void setCurrentTable(FlatfileDBTable table) {
        currentTable = table;
    }

    public void setJDBCUrl(String jdbcURL) {
        url = jdbcURL;
    }

    public String getTableName() {
        return tableName.getText().trim();
    }

    public String getTableType() {
        String type = (String) typeCombo.getSelectedItem();
        return typeMap.get(type);
    }

    public String getEncoding() {
        String key = (String) encodingCombo.getSelectedItem();
        return encodingMap.get(key);
    }

    private void setError(String errorText) {
        error.setText(errorText);
    }

    private boolean isUniqueTableName(String tblName) {
        if (currentModel == null) {
            return false;
        }
        int ind = 0;
        ind = url.indexOf(":", ind + 1);
        ind = url.indexOf(":", ind + 1);
        String path = url.substring(url.indexOf(":", ind + 1) + 1) + "\\";
        File f = new File(path + tblName);
        if (f.exists()) {
            return false;
        }
        FlatfileDBTable match = currentModel.getFileMatchingTableName(tblName);
        return (match == null) || (match == currentTable);
    }

    private boolean isAxionReservedName(String newName) {
        boolean isReservedName = false;
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = FlatfileDBConnectionFactory.getInstance().getConnection(url);
            stmt = conn.createStatement();
            stmt.execute("create table " + newName + " (id int)");
        } catch (Exception se) {
            isReservedName = true;
        } finally {
            if (stmt != null) {
                try {
                    stmt.execute("drop table " + newName);
                    stmt.execute("shutdown");
                } catch (SQLException ignore) {
                // Ignore.
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignore) {
                // Ignore.
                }
            }
        }

        return isReservedName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tableName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        encodingCombo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox();
        error = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        resourceUrl = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(450, 350));
        setMinimumSize(new java.awt.Dimension(100, 100));
        setPreferredSize(new java.awt.Dimension(400, 200));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Enter Table Details"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Table Name");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Encoding");

        encodingCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ASCII (ISO646-US)" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "Table Type");

        typeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Delimited Flatfile", "Fixed Width Flatfile", "RSS", "Spreadsheet (MS Excel)", "XML", "Web (HTML)", "Web Row Set" }));

        error.setForeground(new java.awt.Color(255, 102, 102));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "Resource URL");

        resourceUrl.setForeground(new java.awt.Color(0, 0, 255));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(20, 20, 20)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel4)
                        .add(34, 34, 34)))
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, error, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                    .add(tableName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, encodingCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, typeCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(resourceUrl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 249, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tableName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(error, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(encodingCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(25, 25, 25)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(typeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(30, 30, 30)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(resourceUrl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    public boolean canAdvance() {
        return (typeCombo.getSelectedIndex() != -1 && checkTableName(tableName.getText().trim()));
    }

    public void guessParserType(FlatfileDBTable table) {
        String type = FlatfileBootstrapParserFactory.getInstance().getParserType(table);
        if (typeMap.containsValue(type)) {
            typeCombo.setSelectedIndex(0);
            Iterator it = typeMap.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                if (typeMap.get(key).equals(type)) {
                    typeCombo.setSelectedItem(key);
                    break;
                }
            }
        }
    }

    private String getTableName(String fileName) {
        // Use only fileName
        if (fileName.lastIndexOf("//") != -1) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        } else {
            if (fileName.lastIndexOf("/") != -1) {
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            if (fileName.lastIndexOf("\\") != -1) {
                fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            }

            if (fileName.lastIndexOf(".") != -1) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
        }
        return StringUtil.isNullString(fileName) ? "<table name>" : StringUtil.createTableNameFromFileName(fileName);
    }

    public void setFileName(String fileName) {
        tableName.setText(getTableName(fileName));
    }

    public void setResourceUrl(String text) {
        resourceUrl.setText(text.trim());
    }

    public String getResourceUrl() {
        return resourceUrl.getText().trim();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox encodingCombo;
    private javax.swing.JLabel error;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel resourceUrl;
    private javax.swing.JTextField tableName;
    private javax.swing.JComboBox typeCombo;
    // End of variables declaration//GEN-END:variables
}
