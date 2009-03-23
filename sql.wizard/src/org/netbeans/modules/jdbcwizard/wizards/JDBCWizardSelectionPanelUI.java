/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jdbcwizard.wizards;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.windows.WindowManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBConnectionDefinition;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DatabaseModel;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.impl.DatabaseModelImpl;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.impl.DBTableImpl;
import org.netbeans.modules.jdbcwizard.builder.DBMetaData;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.impl.DatabaseObjectFactory;
import org.netbeans.modules.jdbcwizard.builder.Table;
import org.netbeans.modules.jdbcwizard.builder.TableColumn;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBColumn;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.impl.DBColumnImpl;
import org.netbeans.modules.jdbcwizard.builder.util.XMLCharUtil;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

/**
 * @author
 */
public class JDBCWizardSelectionPanelUI extends javax.swing.JPanel implements ListSelectionListener, ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /* Set <ChangeListeners> */
    protected final Set listeners = new HashSet(1);

    private DatabaseConnection selectedConnection;
    
    DBConnectionDefinition def;

    DBModelNameCellRenderer srcRenderer;

    DBModelNameCellRenderer destRenderer;
    
    DefaultComboBoxModel providers;
    
    private static final String NEW_DATA_SOURCE = NbBundle.getMessage(JDBCWizardSelectionPanelUI.class,"LBL_NDS");

    int visibleCt;

    String dbtype;
    
    int selTableLen = 0;
    
    private JDBCWizardSelectionPanel wizardPanel;
    
    private static class ConnectionWrapper {
        private DatabaseConnection conn;

        ConnectionWrapper(final DatabaseConnection conn) {
            this.conn = conn;
        }

        public DatabaseConnection getDatabaseConnection() {
            return this.conn;
        }

        @Override
        public String toString() {
            return this.conn.getDisplayName();
        }
    }

    private class AddJDBCDataSourceActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent evt) {
            JDBCWizardSelectionPanelUI.this.dbschemaComboBoxActionPerformed(evt);
        }
    }

    /** Creates new form JDBCWizardSelectionPanel */
    public JDBCWizardSelectionPanelUI(JDBCWizardSelectionPanel wp, final String title) {
        wizardPanel = wp;
        if (title != null && title.trim().length() != 0) {
            this.setName(title);
        }
    }

    /**
     * initializing components
     */
    public void initialize() {
        this.initComponents();
        this.initDataSourceCombo();
        this.datasourceComboBox.addActionListener(new AddJDBCDataSourceActionListener());
    }

    public void initDataSourceCombo() {
        providers = new DefaultComboBoxModel();
        //int longinx = 0, longest = 0;
        final DatabaseConnection[] conns = ConnectionManager.getDefault().getConnections();
        if(conns.length == 1){
        	providers.addElement("");
        }
        if (conns.length > 0) {
            for (int i = 0; i < conns.length; i++) {
                providers.addElement(new ConnectionWrapper(conns[i]));
             /*   if(longest < (new ConnectionWrapper(conns[i])).getDatabaseConnection().getDisplayName().length()){
                longinx = i;
                longest = (new ConnectionWrapper(conns[i])).getDatabaseConnection().getDisplayName().length();
                }*/
            }
        } else {
            providers.addElement("<None>");
        }
       
        this.datasourceComboBox.setModel(providers);
        this.datasourceComboBox.setSelectedIndex(0);
        //this.datasourceComboBox.setPrototypeDisplayValue((new ConnectionWrapper(conns[longinx])).getDatabaseConnection().getDisplayName());

    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    private void initComponents() {
        this.dataSourcePanel = new javax.swing.JPanel();
        this.jLabel1 = new javax.swing.JLabel();
        this.datasourceComboBox = new javax.swing.JComboBox();
        this.availableTablesPanel = new javax.swing.JPanel();
        this.selectedTablesPanel = new javax.swing.JPanel();
        this.buttonsPanel = new javax.swing.JPanel();
        this.availableTablesList = new javax.swing.JList();
        this.availableTablesScrollPane = new javax.swing.JScrollPane(this.availableTablesList);
        this.selectedTablesList = new javax.swing.JList();
        this.selectedTablesScrollPane = new javax.swing.JScrollPane(this.selectedTablesList);
        this.addButton = new javax.swing.JButton(JDBCWizardSelectionPanelUI.LBL_ADD);
        this.removeButton = new javax.swing.JButton(JDBCWizardSelectionPanelUI.LBL_REMOVE);
        this.addAllButton = new javax.swing.JButton(JDBCWizardSelectionPanelUI.LBL_ADD_ALL);
        this.removeAllButton = new javax.swing.JButton(JDBCWizardSelectionPanelUI.LBL_REMOVE_ALL);
        this.availableTablesLabel = new javax.swing.JLabel();
        this.selectedTablesLabel = new javax.swing.JLabel();
        
        java.awt.GridBagConstraints gridBagConstraints;
        
        setLayout(new GridBagLayout());
        
        dataSourcePanel.add(this.jLabel1);
        dataSourcePanel.add(this.datasourceComboBox);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 26;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 39;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 10, 12);
        add(dataSourcePanel, gridBagConstraints);
        
        availableTablesPanel.setLayout(new BorderLayout());
        availableTablesPanel.add(availableTablesLabel,BorderLayout.NORTH);
        availableTablesPanel.add(availableTablesScrollPane, java.awt.BorderLayout.CENTER);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.gridheight = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 24, 21, 24);
        add(availableTablesPanel, gridBagConstraints);

        selectedTablesPanel.setLayout(new BorderLayout());
        selectedTablesPanel.add(selectedTablesLabel,BorderLayout.NORTH);
        selectedTablesPanel.add(selectedTablesScrollPane,BorderLayout.CENTER);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.gridheight = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(23, 23, 23, 23);
        add(selectedTablesPanel, gridBagConstraints);
        
        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(9, 9, 9, 9);
        buttonsPanel.add(addButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(9, 9, 9, 9);
        buttonsPanel.add(removeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(9, 9, 9, 9);
        buttonsPanel.add(addAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(9, 9, 9, 9);
        buttonsPanel.add(removeAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 17;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(13, 13, 13, 13);
        add(buttonsPanel, gridBagConstraints);
        

        // Adding actionlisteners
        this.addButton.setActionCommand(JDBCWizardSelectionPanelUI.LBL_ADD);
        this.removeButton.setActionCommand(JDBCWizardSelectionPanelUI.LBL_REMOVE);
        this.addAllButton.setActionCommand(JDBCWizardSelectionPanelUI.LBL_ADD_ALL);
        this.removeAllButton.setActionCommand(JDBCWizardSelectionPanelUI.LBL_REMOVE_ALL);

        this.addButton.addActionListener(this);
        this.removeButton.addActionListener(this);
        this.addAllButton.addActionListener(this);
        this.removeAllButton.addActionListener(this);
        this.addButton.setEnabled(false);
        this.removeButton.setEnabled(false);
        this.addAllButton.setEnabled(false);
        this.removeAllButton.setEnabled(false);
        this.jLabel1.setText(NbBundle.getMessage(JDBCWizardSelectionPanelUI.class, "LBL_DS"));

        this.availableTablesLabel.setText(NbBundle.getMessage(JDBCWizardSelectionPanelUI.class, "LBL_AVLB_TAB"));
        this.availableTablesLabel.setDisplayedMnemonic(NbBundle.getMessage(this.getClass(), "LBL_AVLB_TAB").charAt(0));
        this.selectedTablesLabel.setText(NbBundle.getMessage(JDBCWizardSelectionPanelUI.class, "LBL_SEL_TAB"));
        
        
    }// </editor-fold>

    /**
     * @param evt
     */
    private void dbschemaComboBoxActionPerformed(final java.awt.event.ActionEvent evt) {
        this.updateSourceSchema();
    }

    /**
     * Connect to the datasource selected in the combo
     */
    private void updateSourceSchema() {
        Set<DatabaseConnection> oldConnections = new HashSet<DatabaseConnection> (Arrays.asList(ConnectionManager.getDefault().getConnections()));
        
        final Object item = this.datasourceComboBox.getSelectedItem();
        if (item instanceof ConnectionWrapper) {
            final ConnectionWrapper cw = (ConnectionWrapper) item;
            this.selectedConnection = cw.getDatabaseConnection();
            ConnectionManager.getDefault().showConnectionDialog(this.selectedConnection);
            providers.removeElement("");
            this.persistModel();
        }/*else{
             ConnectionManager.getDefault().showAddConnectionDialog(null);
        }*/
        // try to find the new connection
        DatabaseConnection[] newConnections = ConnectionManager.getDefault().getConnections();
        if (newConnections.length == oldConnections.size()) {
            // no new connection, so...
            return;
        }
        
        providers.removeElement(new String(NEW_DATA_SOURCE));
        for (int i = 0; i < newConnections.length; i++) {
            if (!oldConnections.contains(newConnections[i])) {
                providers.addElement(new ConnectionWrapper(newConnections[i]));
                break;
            }
        }
    }

    public void persistModel() {
        //final DBMetaData meta = new DBMetaData();
    	final Connection connection = this.selectedConnection.getJDBCConnection();
    	List recycleBinTables = null;
        if(connection != null){
            try {
                //meta.connectDB(this.selectedConnection.getJDBCConnection());
                 def = DatabaseObjectFactory.createDBConnectionDefinition(
                        this.selectedConnection.getDisplayName(), this.selectedConnection.getDriverClass(),
                        this.selectedConnection.getDatabaseURL(), this.selectedConnection.getUser(),
                        this.selectedConnection.getPassword(), "Descriptive info here", DBMetaData.getDBType(connection));
                
                this.dbtype = DBMetaData.getDBType(this.selectedConnection.getJDBCConnection());
                this.dbmodel = new DatabaseModelImpl(this.selectedConnection.getDisplayName(), def);
    
    		    final String[][] tableList = DBMetaData.getTablesAndViews("", this.selectedConnection.getSchema(), "", true,connection);
                Integer oracleMajorVersion = null;
                try {
                    oracleMajorVersion = DBMetaData.getDatabaseMajorVersion(connection);
                } catch (SQLException x) {
                    // #155003: ignore this
                }
    		    if ("ORACLE".equalsIgnoreCase(this.dbtype) && oracleMajorVersion != null && oracleMajorVersion >= 10) { // NOI18N
                    recycleBinTables = DBMetaData.getOracleRecycleBinTables(connection);
                } else {
                    recycleBinTables = Collections.EMPTY_LIST;
                }

    			String[] currTable = null;
    			List<String> tableNamesList = new ArrayList<String> ();
                if (tableList != null) {
                    for (int i = 0; i < tableList.length; i++) {
                        currTable = tableList[i];
                        	if (!recycleBinTables.contains(currTable[DBMetaData.NAME])) {
                        		tableNamesList.add(currTable[DBMetaData.NAME]);
                            }
                        }
                       
                    }
    			this.initListModel(tableNamesList);           
                
            } catch (final Exception ex) {
                ex.printStackTrace();
                ErrorManager.getDefault().log(ErrorManager.ERROR, ex.getMessage());
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
      }
    }

    
    /**
     * 
     *
     */

    public void initListModel(List tableList) {
        final DefaultListModel tempListModel = new DefaultListModel();
        final Iterator itr = tableList.iterator();
        while (itr.hasNext()) {
            final String tabName = (String) itr.next();
            if (tabName != null) {
                tempListModel.addElement(tabName);
            }
        }
        this.listModel = new ListTransferModel();
        this.listModel.setSourceList(Arrays.asList(tempListModel.toArray()));
        
        String largestString = this.listModel.getPrototypeCell();

        if (largestString.length() < JDBCWizardSelectionPanelUI.LBL_SOURCE_MSG.length()) {
            largestString = JDBCWizardSelectionPanelUI.LBL_SOURCE_MSG;
        } else if (largestString.length() < JDBCWizardSelectionPanelUI.LBL_DEST_MSG.length()) {
            largestString = JDBCWizardSelectionPanelUI.LBL_DEST_MSG;
        }

        this.srcRenderer = new DBModelNameCellRenderer(largestString);
        this.destRenderer = new DBModelNameCellRenderer(largestString);

        this.visibleCt = Math.min(Math.max(JDBCWizardSelectionPanelUI.MINIMUM_VISIBLE, this.listModel.getMaximumListSize()), JDBCWizardSelectionPanelUI.MAXIMUM_VISIBLE);

        this.addButton.setModel(this.listModel.getAddButtonModel());
        this.removeButton.setModel(this.listModel.getRemoveButtonModel());
        this.addAllButton.setModel(this.listModel.getAddAllButtonModel());
        this.removeAllButton.setModel(this.listModel.getRemoveAllButtonModel());
        
        this.availableTablesList = new JList((this.listModel.getSourceList()).toArray());
        this.availableTablesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.availableTablesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2 && e.getSource() instanceof JList) {
                    final JList list = (JList) e.getSource();
                    final int[] indices = list.getSelectedIndices();
                    final Object[] selections = list.getSelectedValues();
                    JDBCWizardSelectionPanelUI.this.listModel.add(selections, indices);
                }
            }
        });
        this.availableTablesList.addListSelectionListener(this);
		this.availableTablesList.setPrototypeCellValue(this.srcRenderer);
        this.availableTablesList.setCellRenderer(this.srcRenderer);
        this.availableTablesList.setVisibleRowCount(this.visibleCt);
        
		this.selectedTablesList = new JList();
        this.selectedTablesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.selectedTablesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2 && e.getSource() instanceof JList) {
                    final JList list = (JList) e.getSource();
                    final int[] indices = list.getSelectedIndices();
                    final Object[] selections = list.getSelectedValues();
                    JDBCWizardSelectionPanelUI.this.listModel.remove(selections, indices);
                }
            }
        });
		this.selectedTablesList.setPrototypeCellValue(this.destRenderer);
        this.selectedTablesList.setCellRenderer(this.destRenderer);
        this.selectedTablesList.setVisibleRowCount(this.visibleCt);
        this.selectedTablesList.addListSelectionListener(this);
        this.availableTablesScrollPane.setViewportView(this.availableTablesList);
        this.selectedTablesScrollPane.setViewportView(this.selectedTablesList);
    }
    /**
     * 
     */
    public Component getComponent() {
        return this;
    }
    /**
     * 
     */
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
         return new HelpCtx(JDBCWizardSelectionPanelUI.class);
	  }

    /**
     * @param settings
     */
    public void readSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);

        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }
	
	}
    /**
     * @param settings
     */
    public void storeSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);

        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }
		
		final Object selectedOption = wd.getValue();
        if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
        }

		//populateDBModel();
        if(selectedOption.toString().equals("PREVIOUS_OPTION")){
        	//listModel = null;
        	if(this.availableTablesList != null){
        		if(listModel != null){
	        		this.listModel.getSourceList().clear();
	        		this.availableTablesScrollPane.setViewportView(this.availableTablesList);
	        	}
         	}
        	if(this.selectedTablesList != null){
        		if(listModel != null){
	        		this.listModel.getSourceList().clear();
	        		this.selectedTablesScrollPane.setViewportView(this.selectedTablesList);
        		}
        	}
        	return;
        }
        
		if(listModel!= null){
			List selList = this.listModel.getDestinationList();
        	List<DBTable> selTabList = new ArrayList<DBTable> ();
        	Iterator itr = selList.iterator();
        	while(itr.hasNext()){
        		DBTable tabObj = populateDBTable ((String) itr.next());
        		// By default make the table selected
        		tabObj.setSelected(true);
        		selTabList.add(tabObj);
        	}
        	if (wd != null) {
        		wd.putProperty(JDBCWizardContext.SELECTEDTABLES, selTabList.toArray());
        		wd.putProperty(JDBCWizardContext.DBTYPE, this.dbtype);
        		wd.putProperty(JDBCWizardContext.CONNECTION_INFO, def);
	        }
        }
    }
    /**
     * 
     * @param tableName
     * @return
     */
	public DBTable populateDBTable(String tableName){
		final Connection connection = this.selectedConnection.getJDBCConnection();
		try{
		 final String[][] tableList = DBMetaData.getTablesAndViews("", "", "", true,connection);
            DBTable ffTable = null;
            String[] currTable = null;
            if (tableList != null) {
                for (int i = 0; i < tableList.length; i++) {
                    currTable = tableList[i];
                    if(tableName.equals(currTable[DBMetaData.NAME])){
                        ffTable = new DBTableImpl(currTable[DBMetaData.NAME], currTable[DBMetaData.SCHEMA], currTable[DBMetaData.CATALOG]);
                        Table t = null;
                        String driverName = connection.getMetaData().getDriverName();
                        //For JDBC-ODBC driver we need to select columns by order otherwise, driver throws 
                        //Invalid Descriptor Index exception
                        if(driverName.startsWith("JDBC-ODBC")){
                        	t = DBMetaData.getTableMetaDataForODBCDriver(currTable[DBMetaData.CATALOG], currTable[DBMetaData.SCHEMA],
                                    currTable[DBMetaData.NAME], currTable[DBMetaData.TYPE],connection);
                        }else{
                        	t = DBMetaData.getTableMetaData(currTable[DBMetaData.CATALOG], currTable[DBMetaData.SCHEMA],
                                    currTable[DBMetaData.NAME], currTable[DBMetaData.TYPE],connection);
                            
                        }
                        
                        final TableColumn[] cols = t.getColumns();
                        TableColumn tc = null;
                        DBColumn ffColumn = null;
                        for (int j = 0; j < cols.length; j++) {
                            tc = cols[j];
                            ffColumn = new DBColumnImpl(tc.getName(), tc.getSqlTypeCode(), tc.getNumericScale(),
                                    tc.getNumericPrecision(), tc.getIsPrimaryKey(), tc.getIsForeignKey(),
                                    false /* isIndexed */, tc.getIsNullable());
                            final String ncName = XMLCharUtil.makeValidNCName(tc.getName());
                            ffColumn.setJavaName(ncName);
                            ffTable.addColumn(ffColumn);
                        }
                        return ffTable;
                    }
                    if(ffTable != null){
                        break;
                    }
                }
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            ErrorManager.getDefault().log(ErrorManager.ERROR, ex.getMessage());
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        }
        return null;
	}

    /**
     * Extends ChangeEvent to convey information on an item being transferred to or from the source
     * of the event.
     */
    public static class TransferEvent extends ChangeEvent {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /** Indicates addition of an item to the source of the event */
        public static final int ADDED = 0;

        /** Indicates removal of an item from the source of the event */
        public static final int REMOVED = 1;

        private Object item;

        private int type;

        /**
         * Create a new TransferEvent instance with the given source, item and type.
         * 
         * @param source source of this transfer event
         * @param item transferred item
         * @param type transfer type, either ADDED or REMOVED
         * @see #ADDED
         * @see #REMOVED
         */
        public TransferEvent(final Object source, final Object item, final int type) {
            super(source);
            this.item = item;
            this.type = type;
        }

        /**
         * Gets item that was transferred.
         * 
         * @return transferred item
         */
        public Object getItem() {
            return this.item;
        }

        /**
         * Gets type of transfer event.
         * 
         * @return ADDED or REMOVED
         */
        public int getType() {
            return this.type;
        }
    }

    /**
     * Container for ListModels associated with source and destination lists of a list transfer
     * panel. Holds ButtonModels for controls that indicate selected addition and bulk addition to
     * destination list and selected removal and bulk removal of items from the destination list.
     */
    class ListTransferModel {
        private ButtonModel addAllButtonModel;

        private ButtonModel addButtonModel;

        private HashSet changeListeners;

        private DefaultListModel dest;

        private String listPrototype;

        private ButtonModel removeAllButtonModel;

        private ButtonModel removeButtonModel;

        private DefaultListModel source;

        /**
         * Creates a new instance of ListTransferModel, using the data in the given collections to
         * initially populate the source and destination lists.
         * 
         * @param srcColl Collection used to populate source list
         * @param dstColl Collection used to populate destination list
         */
        public ListTransferModel() {
            this.listPrototype = "";
            this.source = new DefaultListModel();
            this.dest = new DefaultListModel();

            this.addButtonModel = new DefaultButtonModel();
            this.addAllButtonModel = new DefaultButtonModel();
            this.removeButtonModel = new DefaultButtonModel();
            this.removeAllButtonModel = new DefaultButtonModel();

            this.changeListeners = new HashSet();
        }

        /**
         * Moves indicated items from source to destination list.
         * 
         * @param selections array of selected items
         * @param indices array of indices, each element corresponding to the item in selections
         *            array
         */
        public void add(final Object[] selections, final int[] indices) {
            synchronized (this.dest) {
                synchronized (this.source) {
                    for (int i = 0; i < indices.length; i++) {
                        final Object element = selections[i];
                        this.dest.addElement(element);
                        this.source.removeElement(element);
                        this.fireTransferEvent(this.dest, element, TransferEvent.ADDED);
                    }

                    this.updateButtonState();
                }
            }
            selTableLen = this.dest.getSize();
            
            this.updateButtonState();
            this.updateUI();
            wizardPanel.fireChangeEvent();
        }

        /**
         * Moves all remaining items from source to destination list.
         */
        public void addAll() {
            synchronized (this.dest) {
                synchronized (this.source) {
                    final int size = this.source.getSize();
                    for (int i = 0; i < size; i++) {
                        final Object element = this.source.elementAt(i);
                        this.dest.addElement(element);
                        this.fireTransferEvent(this.dest, element, TransferEvent.ADDED);
                    }
                    this.source.removeAllElements();
                }
            }
            selTableLen = this.dest.getSize();
            this.updateButtonState();

            wizardPanel.fireChangeEvent();
            this.updateUI();
        }

        /**
         * Gets ButtonModel associated with add all button action.
         * 
         * @return add all ButtonModel
         */
        public ButtonModel getAddAllButtonModel() {
            return this.addAllButtonModel;
        }

        /**
         * Gets ButtonModel associated with add button action.
         * 
         * @return add ButtonModel
         */
        public ButtonModel getAddButtonModel() {
            return this.addButtonModel;
        }

        /**
         * Gets copy of current contents of destination list
         * 
         * @return List of current destination list contents
         */
        @SuppressWarnings("unchecked")
        public List getDestinationList() {
            final ArrayList dstList = new ArrayList();

            synchronized (this.dest) {
                this.dest.trimToSize();
                for (int i = 0; i < this.dest.size(); i++) {
                    dstList.add(this.dest.get(i));
                }
            }

            return dstList;
        }

        /**
         * Gets ListModel associated with destination list.
         * 
         * @return source ListModel
         */
        public ListModel getDestinationModel() {
            return this.dest;
        }

        /**
         * Gets maximum number of items expected in either the source or destination list.
         * 
         * @return maximum count of items in any one list
         */
        public int getMaximumListSize() {
            return this.source.size() + this.dest.size();
        }

        /**
         * Gets prototype String that has the largest width of an item in either list.
         * 
         * @return String whose length is the largest among the items in either list
         */
        public String getPrototypeCell() {
            return this.listPrototype;
        }

        /**
         * Gets ButtonModel associated with remove all button action
         * 
         * @return remove all ButtonModel
         */
        public ButtonModel getRemoveAllButtonModel() {
            return this.removeAllButtonModel;
        }

        /**
         * Gets ButtonModel associated with remove button action.
         * 
         * @return remove ButtonModel
         */
        public ButtonModel getRemoveButtonModel() {
            return this.removeButtonModel;
        }

        /**
         * Returns index of source item matching the given string.
         * 
         * @param searchStr string to search for in source list
         * @param startFrom index from which to start search
         * @return index of matching item, or -1 if no match exists
         */
        public int getSourceIndexFor(final String searchStr, final int startFrom) {
            int tsttFrom = 0;
            if (startFrom < 0 || startFrom > this.source.size()) {
                tsttFrom = 0;
            }

            if (searchStr != null && searchStr.trim().length() != 0) {
                return this.source.indexOf(searchStr, tsttFrom);
            }

            return -1;
        }

        /**
         * Gets copy of current contents of source list
         * 
         * @return List of current source list contents
         */
        @SuppressWarnings("unchecked")
        public List getSourceList() {
            final ArrayList srcList = new ArrayList();

            synchronized (this.source) {
                this.source.trimToSize();
                for (int i = 0; i < this.source.size(); i++) {
                    srcList.add(this.source.get(i));
                }
            }

            return srcList;
        }

        /**
         * Gets ListModel associated with source list.
         * 
         * @return source ListModel
         */
        public ListModel getSourceModel() {
            return this.source;
        }

        /**
         * Moves indicated items from destination to source list.
         * 
         * @param selections array of selected items
         * @param indices array of indices, each element corresponding to the item in selections
         *            array
         */
        public void remove(final Object[] selections, final int[] indices) {
            synchronized (this.dest) {
                synchronized (this.source) {
                    for (int i = 0; i < indices.length; i++) {
                        final Object element = selections[i];
                        this.source.addElement(element);
                        this.dest.removeElement(element);
                        this.fireTransferEvent(this.dest, element, TransferEvent.REMOVED);
                    }
                }
            }
            selTableLen = this.dest.getSize();
            this.updateButtonState();
            wizardPanel.fireChangeEvent();
            this.updateUI();
        }

        /**
         * Moves all remaining items from destination to source list.
         */
        public void removeAll() {
            synchronized (this.dest) {
                synchronized (this.source) {
                    final int size = this.dest.getSize();
                    for (int i = 0; i < size; i++) {
                        final Object element = this.dest.elementAt(i);
                        this.source.addElement(element);
                    }
                    this.dest.removeAllElements();
                }
            }
            selTableLen = this.dest.getSize();
            this.updateButtonState();
            wizardPanel.fireChangeEvent();
            this.updateUI();
        }

        /**
         * Remove a ChangeListener from this model.
         * 
         * @param l ChangeListener to remove
         */
        public void removeChangeListener(final ChangeListener l) {
            if (l != null) {
                synchronized (this.changeListeners) {
                    this.changeListeners.remove(l);
                }
            }
        }

        /**
         * Sets destination list to include contents of given list. Clears current contents before
         * adding items from newList.
         * 
         * @param newList List whose contents will supplant the current contents of the destination
         *            list
         */
        public void setDestinationList(final Collection newList) {
            if (newList == null) {
                throw new IllegalArgumentException("Must supply non-null Collection for newList");
            }

            if (this.dest == null) {
                this.dest = new DefaultListModel();
            }

            synchronized (this.dest) {
                this.dest.clear();

                final Iterator it = newList.iterator();
                while (it.hasNext()) {
                    final Object o = it.next();
                    this.dest.addElement(o);
                    if (o.toString().trim().length() > this.listPrototype.length()) {
                        this.listPrototype = o.toString().trim();
                    }
                }
            }

            this.updateButtonState();
        }

        /**
         * Sets source list to include contents of given list. Clears current contents before adding
         * items from newList.
         * 
         * @param newList List whose contents will supplant the current contents of the source list
         */
        public void setSourceList(final Collection newList) {
            if (newList == null) {
                throw new IllegalArgumentException("Must supply non-null Collection for newList");
            }

            if (this.source == null) {
                this.source = new DefaultListModel();
            }

            synchronized (this.source) {
                this.source.clear();

                final Iterator it = newList.iterator();
                while (it.hasNext()) {
                    final Object o = it.next();
                    this.source.addElement(o);
                    if (o.toString().trim().length() > this.listPrototype.length()) {
                        this.listPrototype = o.toString().trim();
                    }
                }
            }

            this.updateButtonState();
        }

        /**
         * Updates button states
         */
        public void updateButtonState() {
            final boolean canAdd = !this.source.isEmpty();
            final boolean canRemove = !this.dest.isEmpty();
            if(canRemove){
            	this.addButtonModel.setEnabled(false);
            	this.addAllButtonModel.setEnabled(false);
            	this.removeButtonModel.setEnabled(canRemove);
            	this.removeAllButtonModel.setEnabled(canRemove);
            } else{
            	this.addButtonModel.setEnabled(canAdd);
            	this.addAllButtonModel.setEnabled(canRemove);
            	this.removeButtonModel.setEnabled(canRemove);
            	this.removeAllButtonModel.setEnabled(canRemove);
            }
        }

        public void updateUI() {
            // List is not getting refreshed, temporary workaround to handle this.
            // Need to have a better way to refresh the ui
            JDBCWizardSelectionPanelUI.this.availableTablesList = new JList((JDBCWizardSelectionPanelUI.this.listModel.getSourceList()).toArray());
            JDBCWizardSelectionPanelUI.this.selectedTablesList = new JList((JDBCWizardSelectionPanelUI.this.listModel.getDestinationList()).toArray());
            JDBCWizardSelectionPanelUI.this.availableTablesList.setPrototypeCellValue(JDBCWizardSelectionPanelUI.this.srcRenderer);
            JDBCWizardSelectionPanelUI.this.availableTablesList.setCellRenderer(JDBCWizardSelectionPanelUI.this.srcRenderer);
            JDBCWizardSelectionPanelUI.this.availableTablesList.setVisibleRowCount(JDBCWizardSelectionPanelUI.this.visibleCt);

            JDBCWizardSelectionPanelUI.this.selectedTablesList.setPrototypeCellValue(JDBCWizardSelectionPanelUI.this.destRenderer);
            JDBCWizardSelectionPanelUI.this.selectedTablesList.setCellRenderer(JDBCWizardSelectionPanelUI.this.destRenderer);
            JDBCWizardSelectionPanelUI.this.selectedTablesList.setVisibleRowCount(JDBCWizardSelectionPanelUI.this.visibleCt);

            JDBCWizardSelectionPanelUI.this.availableTablesScrollPane.setViewportView(JDBCWizardSelectionPanelUI.this.availableTablesList);
            JDBCWizardSelectionPanelUI.this.selectedTablesScrollPane.setViewportView(JDBCWizardSelectionPanelUI.this.selectedTablesList);
        }

        private void fireTransferEvent(final Object src, final Object item, final int type) {
            if (src != null && item != null) {
                final TransferEvent e = new TransferEvent(src, item, type);
                synchronized (this.changeListeners) {
                    final Iterator iter = this.changeListeners.iterator();
                    while (iter.hasNext()) {
                        final ChangeListener l = (ChangeListener) iter.next();
                        l.stateChanged(e);
                    }
                }
            }
        }
    }

    /**
     * Invoked whenever one of the transfer buttons is clicked.
     * 
     * @param e ActionEvent to handle
     */
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();

        if (JDBCWizardSelectionPanelUI.LBL_ADD.equals(cmd)) {
            final int[] indices = this.availableTablesList.getSelectedIndices();
            if (indices.length <= 1) {
            final Object[] selections = this.availableTablesList.getSelectedValues();
            this.listModel.add(selections, indices);
            } else {
                Object[] options = { "OK" };
                JOptionPane.showOptionDialog(WindowManager.getDefault().getMainWindow(),
                        NbBundle.getMessage(JDBCWizardSelectionPanelUI.class, "WARNING_IN_SELECTING_TABLES"), "Warning", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            }
        } else if (JDBCWizardSelectionPanelUI.LBL_ADD_ALL.equals(cmd)) {
            this.listModel.addAll();
        } else if (JDBCWizardSelectionPanelUI.LBL_REMOVE.equals(cmd)) {
            final int[] indices = this.selectedTablesList.getSelectedIndices();
            final Object[] selections = this.selectedTablesList.getSelectedValues();
            this.listModel.remove(selections, indices);
        } else if (JDBCWizardSelectionPanelUI.LBL_REMOVE_ALL.equals(cmd)) {
            this.listModel.removeAll();
        } else {
            // Log this as an invalid or unknown command.
            // System.err.println("Unknown cmd: " + cmd);
        }
    }

    /**
     * Called whenever the value of the selection changes.
     * 
     * @param e the event that characterizes the change.
     */
    public void valueChanged(final ListSelectionEvent e) {
        final Object src = e.getSource();

        // Enforce mutually exclusive focus between source and destination
        // lists.
        if (this.availableTablesList.equals(src)) {
            if (!this.selectedTablesList.isSelectionEmpty()) {
                this.selectedTablesList.clearSelection();
            }
        } else if (this.selectedTablesList.equals(src)) {
            if (!this.availableTablesList.isSelectionEmpty()) {
                this.availableTablesList.clearSelection();
            }
        } else {
            // TODO Log unhandled ListSelectionEvent as DEBUG message.
        }
    }

    class DBModelNameCellRenderer extends DefaultListCellRenderer {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public DBModelNameCellRenderer(final String protoString) {
            super();
            this.setText(protoString.toString());
        }

        @Override
        public Component getListCellRendererComponent(final JList list,
                                                      final Object value,
                                                      final int index,
                                                      final boolean isSelected,
                                                      final boolean cellHasFocus) {

            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }

            this.setEnabled(list.isEnabled());
            this.setFont(list.getFont());
            this.setOpaque(true);

            if (value instanceof DBTable) {
                final DBTable model = (DBTable) value;
                if (model.getName() != null) {
                    this.setText(model.getName());
                } else {
                    this.setText(model.getName());
                }
            }else if (value instanceof String)
            {
		this.setText(value.toString());
            }

            return this;
        }

    }
	
	ListTransferModel listModel;

    private final List dsList = new ArrayList();

    // private JList destList;

    private final List destColl = new ArrayList();

    // private JList sourceList;

    public static final String LBL_DEST_MSG = NbBundle.getMessage(JDBCWizardSelectionPanelUI.class, "LBL_SEL_TAB");

    public static final String LBL_SOURCE_MSG = NbBundle.getMessage(JDBCWizardSelectionPanelUI.class, "LBL_AVLB_TAB");

    /** Maximum number of visible items in lists */
    public static final int MAXIMUM_VISIBLE = 10;

    /** Minimum number of visible items in lists */
    public static final int MINIMUM_VISIBLE = 5;

    /** Indicates addition of item(s). */
    public static final String LBL_ADD = ">";

    /** Label indicating that all elements should be moved. */
    public static final String LBL_ALL = NbBundle.getMessage(JDBCWizardSelectionPanelUI.class,"LBL_ALL");

    /** Indicates addition of all source items. */
    public static final String LBL_ADD_ALL = JDBCWizardSelectionPanelUI.LBL_ALL + " " + JDBCWizardSelectionPanelUI.LBL_ADD;

    /** Indicates removal of item(s). */
    public static final String LBL_REMOVE = "<";

    /** Indicates removal of all destination items. */
    public static final String LBL_REMOVE_ALL = JDBCWizardSelectionPanelUI.LBL_REMOVE + " " + JDBCWizardSelectionPanelUI.LBL_ALL;

    private DatabaseModel dbmodel;

    private javax.swing.JButton addButton;

    private javax.swing.JButton removeButton;

    private javax.swing.JButton addAllButton;

    private javax.swing.JButton removeAllButton;

    private javax.swing.JComboBox datasourceComboBox;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel availableTablesLabel;

    private javax.swing.JLabel selectedTablesLabel;

    javax.swing.JList availableTablesList;

    javax.swing.JList selectedTablesList;

    private javax.swing.JPanel dataSourcePanel;

    private javax.swing.JPanel availableTablesPanel;
    
    private javax.swing.JPanel selectedTablesPanel;
    
    private javax.swing.JPanel buttonsPanel;
    
    javax.swing.JScrollPane availableTablesScrollPane;

    javax.swing.JScrollPane selectedTablesScrollPane;

}
