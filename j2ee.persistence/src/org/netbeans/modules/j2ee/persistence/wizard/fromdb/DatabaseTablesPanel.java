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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.util.SourceLevelChecker;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseTablesPanel extends javax.swing.JPanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final DBSchemaManager dbschemaManager = new DBSchemaManager();

    private PersistenceGenerator persistenceGen;

    private SchemaElement sourceSchemaElement;
    private DatabaseConnection dbconn;
    private FileObject dbschemaFile;
    private String datasourceName;
    private TableClosure tableClosure;

    private boolean sourceSchemaUpdateEnabled;

    private Project project;

    public DatabaseTablesPanel() {
        initComponents();

        ListSelectionListener selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateButtons();
            }
        };
        availableTablesList.getSelectionModel().addListSelectionListener(selectionListener);
        selectedTablesList.getSelectionModel().addListSelectionListener(selectionListener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void initialize(Project project, DBSchemaFileList dbschemaFileList, PersistenceGenerator persistenceGen, TableSource tableSource, FileObject targetFolder) {
        this.persistenceGen = persistenceGen;
        this.project = project;

        boolean enabled = ProviderUtil.isValidServerInstanceOrNone(project);

        if (enabled) {
            boolean withDatasources = Util.isContainerManaged(project) || Util.isEjb21Module(project);
            if (withDatasources) {
                initializeWithDatasources();
            } else {
                initializeWithDbConnections();
            }

            DBSchemaUISupport.connect(dbschemaComboBox, dbschemaFileList);
            boolean hasDBSchemas = (dbschemaComboBox.getItemCount() > 0 && dbschemaComboBox.getItemAt(0) instanceof FileObject);
            if (!hasDBSchemas) {
                dbschemaRadioButton.setEnabled(hasDBSchemas);
                dbschemaComboBox.setEnabled(hasDBSchemas);
            }

            selectDefaultTableSource(tableSource, withDatasources, project, targetFolder);
        } else {
            datasourceRadioButton.setEnabled(false);
            datasourceComboBox.setEnabled(false);
            dbschemaRadioButton.setEnabled(false);
            dbschemaComboBox.setEnabled(false);
        }

        // hack to ensure the progress dialog displayed by updateSourceSchema()
        // is displayed on top of the wizard dialog. Needed because when initialize()
        // is called wizard dialog might be non-visible, so the progress dialog
        // would be displayed before the wizard dialog.
        sourceSchemaUpdateEnabled = true;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateSourceSchema();
            }
        });
    }

    private void initializeWithDatasources() {
        org.openide.awt.Mnemonics.setLocalizedText(datasourceRadioButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Datasource"));
        JPADataSourcePopulator dsPopulator = project.getLookup().lookup(JPADataSourcePopulator.class);
        dsPopulator.connect(datasourceComboBox);
    }

    private void initializeWithDbConnections() {
        org.openide.awt.Mnemonics.setLocalizedText(datasourceRadioButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_JDBCConnection"));
        DatabaseExplorerUIs.connect(datasourceComboBox, ConnectionManager.getDefault());
    }

    /**
     * Selects the default table source (cf. issue 74113).
     */
    private void selectDefaultTableSource(TableSource tableSource, boolean withDatasources, Project project, FileObject targetFolder) {
        if (tableSource == null) {
            // the wizard is invoked for the first time for this project
            // the first schema file found (in this package, if possible)
            // should be selected
            int dbschemaCount = dbschemaComboBox.getItemCount();
            if (targetFolder != null) {
                SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
                SourceGroup targetSourceGroup = SourceGroups.getFolderSourceGroup(sourceGroups, targetFolder);
                if (targetSourceGroup != null) {
                    for (int i=0; i<dbschemaCount; i++){
                        Object nextSchema = dbschemaComboBox.getItemAt(i);
                        if (nextSchema instanceof FileObject) {
                            FileObject parent = ((FileObject)nextSchema).getParent();
                            if (parent.equals(targetFolder)){
                                dbschemaComboBox.setSelectedIndex(i);
                                dbschemaRadioButton.setSelected(true);
                                return;
                            }
                        }
                    }
                }
            }
            if (dbschemaCount > 0 && dbschemaComboBox.getItemAt(0) instanceof FileObject) {
                dbschemaComboBox.setSelectedIndex(0);
                dbschemaRadioButton.setSelected(true);
                return;
            }
        } else {
            // the wizard has already been invoked -- try to select the previous table source
            String tableSourceName = tableSource.getName();
            switch (tableSource.getType()) {
            case DATA_SOURCE:
                // if the previous source was a data source, it should be selected
                // only if a database connection can be found for it and we can
                // connect to that connection without displaying a dialog
                if (withDatasources) {
                    if (selectDatasource(tableSourceName, false)) {
                        return;
                    }
                }
                break;

            case CONNECTION:
                // if the previous source was a database connection, it should be selected
                // only if we can connect to it without displaying a dialog
                if (!withDatasources) {
                    if (selectDbConnection(tableSourceName)) {
                        return;
                    }
                }
                break;

            case SCHEMA_FILE:
                // if the previous source was a dbschema file, it should be always selected
                if (selectDBSchemaFile(tableSourceName)) {
                    return;
                }
                break;
            }
        }
        
        //try to find pu for the project
        //nothing is selected based on previos selection, try to select based on persistence.xml
        boolean puExists = false;
        try {
            puExists = ProviderUtil.persistenceExists(project);
        } catch (InvalidPersistenceXmlException ex) {
        }

        if(puExists){
            PUDataObject pud = null;
            try {
                pud = ProviderUtil.getPUDataObject(project);
            } catch (InvalidPersistenceXmlException ex) {
                Exceptions.printStackTrace(ex);
            }
            PersistenceUnit pu = (pud !=null && pud.getPersistence().getPersistenceUnit().length==1) ? pud.getPersistence().getPersistenceUnit()[0] : null;
            if(pu !=null ){
                if(withDatasources){
                    String jtaDs = pu.getJtaDataSource();
                    if(jtaDs !=null ){
                        selectDatasource(jtaDs, true);
                    }
                    else {
                        String nJtaDs = pu.getNonJtaDataSource();
                        if(nJtaDs != null) {
                            selectDatasource(nJtaDs, true);
                        }
                    }
                } else {
                    //try to find jdbc connection
                    DatabaseConnection cn = ProviderUtil.getConnection(pu);
                    if(cn != null){
                        datasourceComboBox.setSelectedItem(cn);
                    }
                }
            }
        }

        // nothing got selected so far, so select the data source / connection
        // radio button, but don't select an actual data source or connection
        // (since this would cause the connect dialog to be displayed)
        datasourceRadioButton.setSelected(true);
    }

    /**
     * Finds the database connections whose database URL and user name equal
     * the database URL and the user name of the passed data source.
     *
     * @param  datasource the data source.
     *
     * @return the list of database connections; never null.
     *
     * @throws NullPointerException if the datasource parameter was null.
     */
    private static List<DatabaseConnection> findDatabaseConnections(JPADataSource datasource) {
        // copied from j2ee.common.DatasourceHelper (can't depend on that)
        if (datasource == null) {
            throw new NullPointerException("The datasource parameter cannot be null."); // NOI18N
        }
        String databaseUrl = datasource.getUrl();
        String user = datasource.getUsername();
        if (databaseUrl == null || user == null) {
            return Collections.emptyList();
        }
        List<DatabaseConnection> result = new ArrayList<DatabaseConnection>();
        for (DatabaseConnection dbconn : ConnectionManager.getDefault().getConnections()) {
            if (databaseUrl.equals(dbconn.getDatabaseURL()) && user.equals(dbconn.getUser())) {
                result.add(dbconn);
            }
        }
        if (result.size() > 0) {
            return Collections.unmodifiableList(result);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Tries to select the given data source and returns true if successful.
     * @param skipChecks if need just to select without verifications
     */
    private boolean selectDatasource(String jndiName, boolean skipChecks) {
        JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
        if (dsProvider == null){
            return false;
        }
        JPADataSource datasource = null;
        for (JPADataSource each : dsProvider.getDataSources()){
            if (jndiName.equals(each.getJndiName())){
                datasource = each;
            }
        }
        
        // The datasource can be null if the dsProvider.getDataSources() is empty 
        // or the jndiName can not be found. See issue 154641
        if(datasource == null) {
            return false;
        }

        List<DatabaseConnection> dbconns = findDatabaseConnections(datasource);
        if (dbconns.size() == 0) {
            return false;
        }
        if(!skipChecks){
            DatabaseConnection dbcon = dbconns.get(0);
            if (dbcon.getJDBCConnection() == null) {
                return false;
            }
        }
        boolean selected = false;
        for(int i=0; i<datasourceComboBox.getItemCount(); i++){
            Object item = datasourceComboBox.getItemAt(i);
            JPADataSource jpaDS = dsProvider != null ? dsProvider.toJPADataSource(item) : null;
            if(jpaDS!=null){
                if(datasource.getJndiName().equals(jpaDS.getJndiName()) && datasource.getUrl().equals(jpaDS.getUrl()) && datasource.getUsername().equals(jpaDS.getUsername())){
                    datasourceComboBox.setSelectedIndex(i);
                    selected = true;
                    break;
                }
            }
        }
        if (!selected) {
            return false;
        }
        datasourceRadioButton.setSelected(true);
        return true;
    }

    /**
     * Tries to select the given connection and returns true if successful.
     */
    private boolean selectDbConnection(String name) {
        DatabaseConnection dbcon = ConnectionManager.getDefault().getConnection(name);
        if (dbcon == null || dbcon.getJDBCConnection() == null) {
            return false;
        }
        datasourceComboBox.setSelectedItem(dbcon);
        if (!dbcon.equals(datasourceComboBox.getSelectedItem())) {
            return false;
        }
        datasourceRadioButton.setSelected(true);
        return true;
    }

    /**
     * Tries to select the given dbschema file and returns true if successful.
     */
    private boolean selectDBSchemaFile(String name) {
        FileObject dbschemaFl = FileUtil.toFileObject(new File(name));
        if (dbschemaFl == null) {
            return false;
        }
        dbschemaComboBox.setSelectedItem(dbschemaFl);
        if (!dbschemaFl.equals(dbschemaComboBox.getSelectedItem())) {
            return false;
        }
        dbschemaRadioButton.setSelected(true);
        return true;
    }

    public SchemaElement getSourceSchemaElement() {
        return sourceSchemaElement;
    }

    public DatabaseConnection getDatabaseConnection() {
        return dbconn;
    }

    public FileObject getDBSchemaFile() {
        return dbschemaFile;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public TableClosure getTableClosure() {
        return tableClosure;
    }

    private void updateSourceSchema() {
        if (!sourceSchemaUpdateEnabled) {
            return;
        }

        sourceSchemaElement = null;
        datasourceName = null;
        dbconn = null;
        dbschemaFile = null;


        if (datasourceRadioButton.isSelected()) {
            Object item = datasourceComboBox.getSelectedItem();
            JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
            JPADataSource jpaDS = dsProvider != null ? dsProvider.toJPADataSource(item) : null;
            if (jpaDS != null) {
                List<DatabaseConnection> dbconns = findDatabaseConnections(jpaDS);
                if (dbconns.size() > 0) {
                    dbconn = dbconns.get(0);
                } else {
                    String drvClass = jpaDS.getDriverClassName();
                    if (drvClass == null) {
                        notify(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_NoDriverClassName"));
                    } else {
                        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(drvClass);
                        if (drivers.length == 0) {
                            notify(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_NoDriverError", drvClass));
                        } else {
                            JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers(drvClass)[0];
                            dbconn = ConnectionManager.getDefault().showAddConnectionDialogFromEventThread(
                                    driver, jpaDS.getUrl(), jpaDS.getUsername(), jpaDS.getPassword());
                        }
                    }
                }
                if (dbconn != null) {
                    try {
                        sourceSchemaElement = dbschemaManager.getSchemaElement(dbconn);
                        datasourceName = jpaDS.getJndiName();
                    } catch (SQLException e) {
                        notify(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_DatabaseError"));
                    }
                }
            } else if (item instanceof DatabaseConnection) {
                dbconn = (DatabaseConnection)item;
                try {
                    sourceSchemaElement = dbschemaManager.getSchemaElement(dbconn);
                } catch (SQLException e) {
                    notify(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_DatabaseError"));
                }
            }
        } else if (dbschemaRadioButton.isSelected()) {
            Object item = dbschemaComboBox.getSelectedItem();
            if (item instanceof FileObject) {
                dbschemaFile = (FileObject)item;
                sourceSchemaElement = dbschemaManager.getSchemaElement(dbschemaFile);
            }
        }

        TableProvider tableProvider = null;

        if (sourceSchemaElement != null) {
            tableProvider = new DBSchemaTableProvider(sourceSchemaElement, persistenceGen);
        } else {
            tableProvider = new EmptyTableProvider();
        }

        tableClosure = new TableClosure(tableProvider);
        tableClosure.setClosureEnabled(tableClosureCheckBox.isSelected());

        TableUISupport.connectAvailable(availableTablesList, tableClosure);
        TableUISupport.connectSelected(selectedTablesList, tableClosure);

        updateButtons();

        changeSupport.fireChange();
    }

    private static void notify(String message) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }

    private void updateSourceSchemaComboBoxes() {
        datasourceComboBox.setEnabled(datasourceRadioButton.isSelected());
        dbschemaComboBox.setEnabled(dbschemaRadioButton.isSelected());
    }

    private void updateButtons() {
        Set<Table> addTables = TableUISupport.getSelectedTables(availableTablesList);
        addButton.setEnabled(tableClosure.canAddAllTables(addTables));

        addAllButton.setEnabled(tableClosure.canAddSomeTables(tableClosure.getAvailableTables()));

        Set<Table> tables = TableUISupport.getSelectedTables(selectedTablesList);
        removeButton.setEnabled(tableClosure.canRemoveAllTables(tables));

        removeAllButton.setEnabled(tableClosure.getSelectedTables().size() > 0);
        tableError.setText("");
        for (Table t : addTables) {
            if (t.isDisabled()) {
                if (t.getDisabledReason() instanceof Table.ExistingDisabledReason) {
                    String existingClass = ((Table.ExistingDisabledReason) t.getDisabledReason()).getFQClassName();
                    tableError.setText(NbBundle.getMessage(DatabaseTablesPanel.class, "MSG_Already_Mapped", new Object[] {t.getName(), existingClass}));
                    break;
                } else if (t.getDisabledReason() instanceof Table.NoPrimaryKeyDisabledReason) {
                    tableError.setText(NbBundle.getMessage(DatabaseTablesPanel.class, "MSG_No_Primary_Key", new Object[] {t.getName()}));
                    break;
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        schemaSource = new javax.swing.ButtonGroup();
        datasourceRadioButton = new javax.swing.JRadioButton();
        datasourceComboBox = new javax.swing.JComboBox();
        dbschemaRadioButton = new javax.swing.JRadioButton();
        dbschemaComboBox = new javax.swing.JComboBox();
        tablesPanel = new TablesPanel();
        availableTablesLabel = new javax.swing.JLabel();
        availableTablesScrollPane = new javax.swing.JScrollPane();
        availableTablesList = TableUISupport.createTableList();
        selectedTablesLabel = new javax.swing.JLabel();
        selectedTablesScrollPane = new javax.swing.JScrollPane();
        selectedTablesList = TableUISupport.createTableList();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addAllButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        tableClosureCheckBox = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableError = new javax.swing.JTextPane();

        setName(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_DatabaseTables")); // NOI18N

        schemaSource.add(datasourceRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(datasourceRadioButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Datasource")); // NOI18N
        datasourceRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                datasourceRadioButtonItemStateChanged(evt);
            }
        });

        datasourceComboBox.setEnabled(false);
        datasourceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datasourceComboBoxActionPerformed(evt);
            }
        });

        schemaSource.add(dbschemaRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(dbschemaRadioButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_DbSchema")); // NOI18N
        dbschemaRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                dbschemaRadioButtonItemStateChanged(evt);
            }
        });

        dbschemaComboBox.setEnabled(false);
        dbschemaComboBox.setNextFocusableComponent(availableTablesList);
        dbschemaComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbschemaComboBoxActionPerformed(evt);
            }
        });

        tablesPanel.setLayout(new java.awt.GridBagLayout());

        availableTablesLabel.setLabelFor(availableTablesList);
        org.openide.awt.Mnemonics.setLocalizedText(availableTablesLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_AvailableTables")); // NOI18N
        availableTablesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "TXT_AvailableTables")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        tablesPanel.add(availableTablesLabel, gridBagConstraints);

        availableTablesList.setNextFocusableComponent(addButton);
        availableTablesScrollPane.setViewportView(availableTablesList);
        availableTablesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSN_AvailableTables")); // NOI18N
        availableTablesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSD_AvailableTables")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tablesPanel.add(availableTablesScrollPane, gridBagConstraints);

        selectedTablesLabel.setLabelFor(selectedTablesList);
        org.openide.awt.Mnemonics.setLocalizedText(selectedTablesLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_SelectedTables")); // NOI18N
        selectedTablesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "TXT_SelectedTables")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        tablesPanel.add(selectedTablesLabel, gridBagConstraints);

        selectedTablesScrollPane.setViewportView(selectedTablesList);
        selectedTablesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSN_SelectedTables")); // NOI18N
        selectedTablesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSD_SelectedTables")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tablesPanel.add(selectedTablesScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Remove")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addAllButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_AddAll")); // NOI18N
        addAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 0);
        buttonPanel.add(addAllButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeAllButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_RemoveAll")); // NOI18N
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(removeAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 11);
        tablesPanel.add(buttonPanel, gridBagConstraints);

        tableClosureCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(tableClosureCheckBox, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_IncludeRelatedTables")); // NOI18N
        tableClosureCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "TXT_IncludeRelatedTables")); // NOI18N
        tableClosureCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableClosureCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tableClosureCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        tablesPanel.add(tableClosureCheckBox, gridBagConstraints);

        jScrollPane3.setBorder(null);

        tableError.setEditable(false);
        tableError.setOpaque(false);
        jScrollPane3.setViewportView(tableError);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datasourceRadioButton)
                    .add(dbschemaRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dbschemaComboBox, 0, 387, Short.MAX_VALUE)
                    .add(datasourceComboBox, 0, 387, Short.MAX_VALUE)))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, tablesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(datasourceRadioButton)
                    .add(datasourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dbschemaRadioButton)
                    .add(dbschemaComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 6, Short.MAX_VALUE)
                .add(tablesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 235, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableClosureCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tableClosureCheckBoxItemStateChanged
        tableClosure.setClosureEnabled(tableClosureCheckBox.isSelected());
    }//GEN-LAST:event_tableClosureCheckBoxItemStateChanged

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        tableClosure.removeAllTables();
        selectedTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void addAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllButtonActionPerformed
        tableClosure.addAllTables();
        availableTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_addAllButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Set<Table> tables = TableUISupport.getSelectedTables(selectedTablesList);
        tableClosure.removeTables(tables);
        selectedTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Set<Table> tables = TableUISupport.getSelectedTables(availableTablesList);
        tableClosure.addTables(tables);
        availableTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_addButtonActionPerformed

    private void dbschemaComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbschemaComboBoxActionPerformed
        updateSourceSchema();
    }//GEN-LAST:event_dbschemaComboBoxActionPerformed

    private void dbschemaRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_dbschemaRadioButtonItemStateChanged
        updateSourceSchemaComboBoxes();
        updateSourceSchema();
    }//GEN-LAST:event_dbschemaRadioButtonItemStateChanged

    private void datasourceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datasourceComboBoxActionPerformed
        datasourceComboBox.hidePopup();
        updateSourceSchema();
    }//GEN-LAST:event_datasourceComboBoxActionPerformed

    private void datasourceRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_datasourceRadioButtonItemStateChanged
        updateSourceSchemaComboBoxes();
        updateSourceSchema();
    }//GEN-LAST:event_datasourceRadioButtonItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAllButton;
    private javax.swing.JButton addButton;
    private javax.swing.JLabel availableTablesLabel;
    private javax.swing.JList availableTablesList;
    private javax.swing.JScrollPane availableTablesScrollPane;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JComboBox datasourceComboBox;
    private javax.swing.JRadioButton datasourceRadioButton;
    private javax.swing.JComboBox dbschemaComboBox;
    private javax.swing.JRadioButton dbschemaRadioButton;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.ButtonGroup schemaSource;
    private javax.swing.JLabel selectedTablesLabel;
    private javax.swing.JList selectedTablesList;
    private javax.swing.JScrollPane selectedTablesScrollPane;
    private javax.swing.JCheckBox tableClosureCheckBox;
    private javax.swing.JTextPane tableError;
    private javax.swing.JPanel tablesPanel;
    // End of variables declaration//GEN-END:variables

    private final class TablesPanel extends JPanel {

        @Override
        public void doLayout() {
            super.doLayout();

            Rectangle availableBounds = availableTablesScrollPane.getBounds();
            Rectangle selectedBounds = selectedTablesScrollPane.getBounds();

            if (Math.abs(availableBounds.width - selectedBounds.width) > 1) {
                GridBagConstraints buttonPanelConstraints = ((GridBagLayout)getLayout()).getConstraints(buttonPanel);
                int totalWidth = getWidth() - buttonPanel.getWidth() - buttonPanelConstraints.insets.left - buttonPanelConstraints.insets.right;
                int equalWidth = totalWidth / 2;
                int xOffset = equalWidth - availableBounds.width;

                availableBounds.width = equalWidth;
                availableTablesScrollPane.setBounds(availableBounds);

                Rectangle buttonBounds = buttonPanel.getBounds();
                buttonBounds.x += xOffset;
                buttonPanel.setBounds(buttonBounds);

                Rectangle labelBounds = selectedTablesLabel.getBounds();
                labelBounds.x += xOffset;
                selectedTablesLabel.setBounds(labelBounds);

                selectedBounds.x += xOffset;
                selectedBounds.width = totalWidth - equalWidth;
                selectedTablesScrollPane.setBounds(selectedBounds);

                Rectangle tableClosureBounds = tableClosureCheckBox.getBounds();
                tableClosureBounds.x += xOffset;
                tableClosureBounds.width = totalWidth - equalWidth;
                tableClosureCheckBox.setBounds(tableClosureBounds);
            }
        }
    }

    public static final class WizardPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private DatabaseTablesPanel component;
        private boolean componentInitialized;

        private WizardDescriptor wizardDescriptor;
        private Project project;
        private boolean cmp;

        boolean waitingForScan;
        
        private String title;
        
        public WizardPanel(String wizardTitle) {
            title = wizardTitle;
        }

        @Override
        public DatabaseTablesPanel getComponent() {
            if (component == null) {
                component = new DatabaseTablesPanel();
                component.addChangeListener(this);
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            if (cmp) {
                return new HelpCtx("org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp." + DatabaseTablesPanel.class.getSimpleName()); // NOI18N
            } else {
                return new HelpCtx(DatabaseTablesPanel.class);
            }
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            wizardDescriptor = settings;
            wizardDescriptor.putProperty("NewFileWizard_Title", title); // NOI18N
            
            if (!componentInitialized) {
                componentInitialized = true;

                project = Templates.getProject(wizardDescriptor);
                cmp = RelatedCMPWizard.isCMP(wizardDescriptor);
                RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);
                DBSchemaFileList dbschemaFileList = helper.getDBSchemaFileList();
                PersistenceGenerator persistenceGen = helper.getPersistenceGenerator();
                TableSource tableSource = helper.getTableSource();
                FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);

                getComponent().initialize(project, dbschemaFileList, persistenceGen, tableSource, targetFolder);
            }
        }

        @Override
        public boolean isValid() {
            if (!ProviderUtil.isValidServerInstanceOrNone(project)) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_MissingServer"));
                return false;
            }

            // TODO: RETOUCHE
            //            if (JavaMetamodel.getManager().isScanInProgress()) {
            if (false){
                if (!waitingForScan) {
                    waitingForScan = true;
                    RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: RETOUCHE
                            //                            JavaMetamodel.getManager().waitScanFinished();
                            waitingForScan = false;
                            changeSupport.fireChange();
                        }
                    });
                    setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "scanning-in-progress"));
                    task.schedule(0);
                }
                return false;
            }

            if (!cmp && SourceLevelChecker.isSourceLevel14orLower(project)) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_NeedProperSourceLevel"));
                return false;
            }

            if (getComponent().getSourceSchemaElement() == null) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_SelectTableSource"));
                return false;
            }

            if (getComponent().getTableClosure().getSelectedTables().size() <= 0) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_SelectTables"));
                return false;
            }

            // any view among selected tables?
            for (Table table : getComponent().getTableClosure().getSelectedTables()) {
                if (!table.isTable()) {
                    setWarningMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "MSG_ViewSelected"));
                    return true;
                }
            }

            setErrorMessage(" "); // NOI18N
            return true;
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);

            SchemaElement sourceSchemaElement = getComponent().getSourceSchemaElement();
            DatabaseConnection dbconn = getComponent().getDatabaseConnection();
            FileObject dbschemaFile = getComponent().getDBSchemaFile();
            String datasourceName = getComponent().getDatasourceName();

            if (dbschemaFile != null) {
                helper.setTableSource(sourceSchemaElement, dbschemaFile);
            } else {
                helper.setTableSource(sourceSchemaElement, dbconn, datasourceName);
            }
            helper.setTableClosure(getComponent().getTableClosure());
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            changeSupport.fireChange();
        }

        private void setErrorMessage(String errorMessage) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage);
        }

        private void setWarningMessage(String warningMessage) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, warningMessage);
        }
    }
}
