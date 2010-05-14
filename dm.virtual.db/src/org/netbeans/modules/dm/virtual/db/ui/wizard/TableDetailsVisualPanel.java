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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JPanel;

import org.netbeans.modules.dm.virtual.db.bootstrap.VirtualTableBootstrapParserFactory;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionFactory;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.openide.util.NbBundle;


public final class TableDetailsVisualPanel extends JPanel {

    private TableDetailsPanel owner;
    private VirtualDatabaseModel currentModel;
    private VirtualDBTable currentTable;
    private String url;
    private static Map<String, String> encodingMap = new HashMap<String, String>();
    private static Map<String, String> typeMap = new HashMap<String, String>();

    static {
        encodingMap.put("ASCII (ISO646-US)", "US-ASCII");
    }

    static {
        typeMap.put(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_SpreadSheet"), PropertyKeys.SPREADSHEET);
        typeMap.put(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_WEbRowSet"), PropertyKeys.WEBROWSET);
        typeMap.put(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_Web"), PropertyKeys.WEB);
        typeMap.put(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_Xml"), PropertyKeys.XML);
        typeMap.put(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_Delimited"), PropertyKeys.DELIMITED);
        typeMap.put(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_FWFlatFile"), PropertyKeys.FIXEDWIDTH);
        typeMap.put(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_RSS"), PropertyKeys.RSS);
    }


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
        return NbBundle.getMessage(TableDetailsVisualPanel.class, "TITLE_TableDetailsWizard");
    }

    /*
     * Checks whether the contents of the table name text field are valid. @return
     * INVALID_NAME if name is invalid, DUPLICATE_NAME if name is already in use in the
     * current VirtualDatabaseModel, or OK if name is valid.
     */
    private boolean checkTableName(String tableName) {
        String newName = tableName.trim();

        if (!VirtualDBUtil.isValid(newName, "[A-Za-z][A-Za-z0-9_]*")) {
            setError(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_Error1"));
            return false;
        } else if (!isUniqueTableName(newName)) {
            setError(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_Error2"));
            return false;
        }

        if (isAxionReservedName(newName)) {
            setError(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_Error3"));
            return false;
        }
        setError("");
        return true;
    }

    public void setDBModel(VirtualDatabaseModel model) {
        currentModel = model;
    }

    public void setCurrentTable(VirtualDBTable table) {
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
        String path = url.substring(url.indexOf(":", ind + 1) + 1) + File.separator;
        File f = new File(path + tblName);
        if (f.exists()) {
            return false;
        }
        VirtualDBTable match = currentModel.getFileMatchingTableName(tblName);
        return (match == null) || (match == currentTable);
    }

    private boolean isAxionReservedName(String newName) {
        boolean isReservedName = false;
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = VirtualDBConnectionFactory.getInstance().getConnection(url);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(NbBundle.getMessage(TableDetailsVisualPanel.class, "TITLE_TableDetailsWizard")));

        jLabel1.setDisplayedMnemonic('T');
        jLabel1.setLabelFor(tableName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableName"));

        jLabel2.setDisplayedMnemonic('E');
        jLabel2.setLabelFor(encodingCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_Encoding"));

        encodingCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_EncodingList") }));
        encodingCombo.setSelectedItem(new String[] {  });

        jLabel3.setDisplayedMnemonic('y');
        jLabel3.setLabelFor(typeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableType"));

        typeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_Delimited"),NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_FWFlatFile"),NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_RSS"),NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_SpreadSheet"),NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_Xml"),NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_Web"),NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableType_WEbRowSet") }));

        error.setForeground(new java.awt.Color(255, 102, 102));

        jLabel4.setLabelFor(resourceUrl);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_ResourceURL"));

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
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(20, 20, 20)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel4)
                        .add(34, 34, 34)))
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, error, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                    .add(tableName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableName"));
        jLabel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableName"));
        tableName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableName"));
        tableName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableName"));
        jLabel2.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_Encoding"));
        jLabel2.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_Encoding"));
        encodingCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_Encoding"));
        encodingCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_Encoding"));
        jLabel3.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableType"));
        jLabel3.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableType"));
        typeCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableType"));
        typeCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_TableType"));
        jLabel4.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_ResourceURL"));
        jLabel4.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_ResourceURL"));
        resourceUrl.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_ResourceURL"));
        resourceUrl.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableDetailsVisualPanel.class, "LBL_TableDetailsWizard_ResourceURL"));

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

    public void guessParserType(VirtualDBTable table) {
        String type = VirtualTableBootstrapParserFactory.getInstance().getParserType(table);
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
        return VirtualDBUtil.isNullString(fileName) ? "<table name>" : VirtualDBUtil.createTableNameFromFileName(fileName);
    }

    public void setFileName(String fileName) {
        tableName.setText(getTableName(fileName));
    }

    public void setResourceUrl(String text) {
        resourceUrl.setToolTipText(text);
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
