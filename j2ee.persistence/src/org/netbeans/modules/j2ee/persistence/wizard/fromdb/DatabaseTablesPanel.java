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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Component;
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
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseTablesPanel extends javax.swing.JPanel {
    
    private DBSchemaManager dbschemaManager = new DBSchemaManager();
    
    private PersistenceGenerator persistenceGen;
    
    private SchemaElement sourceSchemaElement;
    private DatabaseConnection dbconn;
    private FileObject dbschemaFile;
    private String datasourceName;
    private TableClosure tableClosure;
    
    private ChangeSupport changeSupport = new ChangeSupport(this);
    
    private boolean sourceSchemaUpdateEnabled;
    
    private Project project;
    
    public DatabaseTablesPanel() {
        initComponents();
        
        ListSelectionListener selectionListener = new ListSelectionListener() {
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
            boolean withDatasources = Util.isSupportedJavaEEVersion(project) || Util.isEjb21Module(project);
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
                SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
                SourceGroup targetSourceGroup = SourceGroupSupport.getFolderSourceGroup(sourceGroups, targetFolder);
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
                    if (selectDatasource(tableSourceName)) {
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
     */
    private boolean selectDatasource(String jndiName) {
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
        
        List<DatabaseConnection> dbconns = findDatabaseConnections(datasource);
        if (dbconns.size() == 0) {
            return false;
        }
        DatabaseConnection dbconn = dbconns.get(0);
        if (dbconn.getJDBCConnection() == null) {
            return false;
        }
        datasourceComboBox.setSelectedItem(datasource);
        if (!datasource.equals(datasourceComboBox.getSelectedItem())) {
            return false;
        }
        datasourceRadioButton.setSelected(true);
        return true;
    }
    
    /**
     * Tries to select the given connection and returns true if successful.
     */
    private boolean selectDbConnection(String name) {
        DatabaseConnection dbconn = ConnectionManager.getDefault().getConnection(name);
        if (dbconn == null || dbconn.getJDBCConnection() == null) {
            return false;
        }
        datasourceComboBox.setSelectedItem(dbconn);
        if (!dbconn.equals(datasourceComboBox.getSelectedItem())) {
            return false;
        }
        datasourceRadioButton.setSelected(true);
        return true;
    }
    
    /**
     * Tries to select the given dbschema file and returns true if successful.
     */
    private boolean selectDBSchemaFile(String name) {
        FileObject dbschemaFile = FileUtil.toFileObject(new File(name));
        if (dbschemaFile == null) {
            return false;
        }
        dbschemaComboBox.setSelectedItem(dbschemaFile);
        if (!dbschemaFile.equals(dbschemaComboBox.getSelectedItem())) {
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
            if (item instanceof JPADataSource) {
                JPADataSource ds = (JPADataSource)item;
                List<DatabaseConnection> dbconns = findDatabaseConnections(ds);
                if (dbconns.size() > 0) {
                    dbconn = dbconns.get(0);
                } else {
                    String drvClass = ds.getDriverClassName();
                    if (drvClass == null) {
                        notify("ERR_NoDriverClassName");
                    } else {
                        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(drvClass);
                        if (drivers.length == 0) {
                            notify("ERR_NoDriverError");
                        } else {
                            JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers(drvClass)[0];
                            dbconn = ConnectionManager.getDefault().showAddConnectionDialogFromEventThread(
                                    driver, ds.getUrl(), ds.getUsername(), ds.getPassword());
                        }
                    }
                }
                if (dbconn != null) {
                    try {
                        sourceSchemaElement = dbschemaManager.getSchemaElement(dbconn);
                        datasourceName = ds.getJndiName();
                    } catch (SQLException e) {
                        notify("ERR_DatabaseError");
                    }
                }
            } else if (item instanceof DatabaseConnection) {
                dbconn = (DatabaseConnection)item;
                try {
                    sourceSchemaElement = dbschemaManager.getSchemaElement(dbconn);
                } catch (SQLException e) {
                    notify("ERR_DatabaseError");
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
    
    private static void notify(String msgName) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(
                NbBundle.getMessage(DatabaseTablesPanel.class, msgName),
                NotifyDescriptor.ERROR_MESSAGE);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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
        availableTablesList = new javax.swing.JList();
        selectedTablesLabel = new javax.swing.JLabel();
        selectedTablesScrollPane = new javax.swing.JScrollPane();
        selectedTablesList = new javax.swing.JList();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addAllButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        tableClosureCheckBox = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableError = new javax.swing.JTextPane();

        setName(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_DatabaseTables"));
        schemaSource.add(datasourceRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(datasourceRadioButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Datasource"));
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
        org.openide.awt.Mnemonics.setLocalizedText(dbschemaRadioButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_DbSchema"));
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
        org.openide.awt.Mnemonics.setLocalizedText(availableTablesLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_AvailableTables"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        tablesPanel.add(availableTablesLabel, gridBagConstraints);

        availableTablesList.setNextFocusableComponent(addButton);
        availableTablesScrollPane.setViewportView(availableTablesList);
        availableTablesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSN_AvailableTables"));
        availableTablesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSD_AvailableTables"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tablesPanel.add(availableTablesScrollPane, gridBagConstraints);

        selectedTablesLabel.setLabelFor(selectedTablesList);
        org.openide.awt.Mnemonics.setLocalizedText(selectedTablesLabel, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_SelectedTables"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        tablesPanel.add(selectedTablesLabel, gridBagConstraints);

        selectedTablesScrollPane.setViewportView(selectedTablesList);
        selectedTablesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSN_SelectedTables"));
        selectedTablesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "ACSD_SelectedTables"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tablesPanel.add(selectedTablesScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Add"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_Remove"));
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

        org.openide.awt.Mnemonics.setLocalizedText(addAllButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_AddAll"));
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

        org.openide.awt.Mnemonics.setLocalizedText(removeAllButton, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_RemoveAll"));
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
        org.openide.awt.Mnemonics.setLocalizedText(tableClosureCheckBox, org.openide.util.NbBundle.getMessage(DatabaseTablesPanel.class, "LBL_IncludeRelatedTables"));
        tableClosureCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableClosureCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
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
                    .add(dbschemaComboBox, 0, 364, Short.MAX_VALUE)
                    .add(datasourceComboBox, 0, 364, Short.MAX_VALUE)))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, tablesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        
        private DatabaseTablesPanel component;
        private boolean componentInitialized;
        
        private WizardDescriptor wizardDescriptor;
        private Project project;
        private boolean cmp;
        
        private ChangeSupport changeSupport = new ChangeSupport(this);
        
        boolean waitingForScan;
        
        public Component getComponent() {
            return getTypedComponent();
        }
        
        private DatabaseTablesPanel getTypedComponent() {
            if (component == null) {
                component = new DatabaseTablesPanel();
                component.addChangeListener(this);
            }
            return component;
        }
        
        public HelpCtx getHelp() {
            if (cmp) {
                return new HelpCtx("org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp." + DatabaseTablesPanel.class.getSimpleName()); // NOI18N
            } else {
                return new HelpCtx(DatabaseTablesPanel.class);
            }
        }
        
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }
        
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }
        
        public void readSettings(WizardDescriptor settings) {
            wizardDescriptor = settings;
            if (!componentInitialized) {
                componentInitialized = true;
                
                project = Templates.getProject(wizardDescriptor);
                cmp = RelatedCMPWizard.isCMP(wizardDescriptor);
                RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);
                DBSchemaFileList dbschemaFileList = helper.getDBSchemaFileList();
                PersistenceGenerator persistenceGen = helper.getPersistenceGenerator();
                TableSource tableSource = helper.getTableSource();
                FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);
                
                getTypedComponent().initialize(project, dbschemaFileList, persistenceGen, tableSource, targetFolder);
            }
        }
        
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
            
            if (!cmp && ProviderUtil.isSourceLevel14orLower(project)) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_NeedProperSourceLevel"));
                return false;
            }
            
            if (getTypedComponent().getSourceSchemaElement() == null) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_SelectTableSource"));
                return false;
            }
            
            if (getTypedComponent().getTableClosure().getSelectedTables().size() <= 0) {
                setErrorMessage(NbBundle.getMessage(DatabaseTablesPanel.class, "ERR_SelectTables"));
                return false;
            }
            
            setErrorMessage(" "); // NOI18N
            return true;
        }
        
        public void storeSettings(WizardDescriptor settings) {
            WizardDescriptor wiz = settings;
            Object buttonPressed = wiz.getValue();
            if (buttonPressed.equals(WizardDescriptor.NEXT_OPTION) ||
                    buttonPressed.equals(WizardDescriptor.FINISH_OPTION)) {
                RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);
                
                SchemaElement sourceSchemaElement = getTypedComponent().getSourceSchemaElement();
                DatabaseConnection dbconn = getTypedComponent().getDatabaseConnection();
                FileObject dbschemaFile = getTypedComponent().getDBSchemaFile();
                String datasourceName = getTypedComponent().getDatasourceName();
                
                if (dbschemaFile != null) {
                    helper.setTableSource(sourceSchemaElement, dbschemaFile);
                } else {
                    helper.setTableSource(sourceSchemaElement, dbconn, datasourceName);
                }
                helper.setTableClosure(getTypedComponent().getTableClosure());
            }
        }
        
        public void stateChanged(ChangeEvent event) {
            changeSupport.fireChange(event);
        }
        
        private void setErrorMessage(String errorMessage) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", errorMessage); // NOI18N
        }
    }
}
