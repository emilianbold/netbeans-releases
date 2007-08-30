/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.wizards;

import java.awt.Component;
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
import java.util.Collections;

/**
 * @author
 */
public class JDBCWizardSelectionPanel extends javax.swing.JPanel implements WizardDescriptor.Panel, ActionListener,
        ListSelectionListener {
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
    
    private static final String NEW_DATA_SOURCE = "New DataSource...";

    int visibleCt;

    String dbtype;
    
    private int selTableLen = 0;
    
    private static class ConnectionWrapper {
        private DatabaseConnection conn;

        ConnectionWrapper(final DatabaseConnection conn) {
            this.conn = conn;
        }

        public DatabaseConnection getDatabaseConnection() {
            return this.conn;
        }

        public String toString() {
            return this.conn.getDisplayName();
        }
    }

    private class AddJDBCDataSourceActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent evt) {
            JDBCWizardSelectionPanel.this.dbschemaComboBoxActionPerformed(evt);
        }
    }

    /** Creates new form JDBCWizardSelectionPanel */
    public JDBCWizardSelectionPanel(final String title) {
        if (title != null && title.trim().length() != 0) {
            this.setName(title);
        }
    }

    /**
     * initializing componentas
     */
    public void initialize() {
        this.initComponents();
        this.initDataSourceCombo();
        this.datasourceComboBox.addActionListener(new AddJDBCDataSourceActionListener());
    }

    public void initDataSourceCombo() {
        providers = new DefaultComboBoxModel();
        final DatabaseConnection[] conns = ConnectionManager.getDefault().getConnections();
        if(conns.length == 1){
        	providers.addElement("");
        }
        if (conns.length > 0) {
            for (int i = 0; i < conns.length; i++) {
                providers.addElement(new ConnectionWrapper(conns[i]));
            }
        } else {
            providers.addElement(NbBundle.getMessage( JDBCWizardSelectionPanel.class, "LBL_None"));
        }
       
        this.datasourceComboBox.setModel(providers);
        this.datasourceComboBox.setSelectedIndex(0);

    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    private void initComponents() {
        this.jPanel1 = new javax.swing.JPanel();
        this.jLabel1 = new javax.swing.JLabel();
        this.datasourceComboBox = new javax.swing.JComboBox();
        this.jPanel2 = new javax.swing.JPanel();

        this.availableTablesList = new javax.swing.JList();
        this.jScrollPane2 = new javax.swing.JScrollPane(this.availableTablesList);

        this.selectedTablesList = new javax.swing.JList();
        this.jScrollPane3 = new javax.swing.JScrollPane(this.selectedTablesList);

        this.addButton = new javax.swing.JButton(JDBCWizardSelectionPanel.LBL_ADD);
        this.removeButton = new javax.swing.JButton(JDBCWizardSelectionPanel.LBL_REMOVE);
        this.addAllButton = new javax.swing.JButton(NbBundle.getMessage( JDBCWizardSelectionPanel.class, "LBL_ADD_ALL"));
        this.removeAllButton = new javax.swing.JButton(NbBundle.getMessage( JDBCWizardSelectionPanel.class, "LBL_REMOVE_ALL"));
		

        this.availableTablesLabel = new javax.swing.JLabel();
        this.selectedTablesLabel = new javax.swing.JLabel();

        // Adding actionlisteners
        this.addButton.setActionCommand(JDBCWizardSelectionPanel.LBL_ADD);
        this.removeButton.setActionCommand(JDBCWizardSelectionPanel.LBL_REMOVE);
        this.addAllButton.setActionCommand(NbBundle.getMessage( JDBCWizardSelectionPanel.class, "LBL_ADD_ALL"));
        this.removeAllButton.setActionCommand(NbBundle.getMessage( JDBCWizardSelectionPanel.class, "LBL_REMOVE_ALL"));

        this.addButton.addActionListener(this);
        this.removeButton.addActionListener(this);
        this.addAllButton.addActionListener(this);
        this.removeAllButton.addActionListener(this);
        this.addButton.setEnabled(false);
        this.removeButton.setEnabled(false);
        this.addAllButton.setEnabled(false);
        this.removeAllButton.setEnabled(false);
        this.jLabel1.setText(NbBundle.getMessage( JDBCWizardSelectionPanel.class, "LBL_DataSource"));

        this.availableTablesLabel.setText(NbBundle.getMessage( JDBCWizardSelectionPanel.class, "LBL_AvailableTables_jLabell"));

        this.selectedTablesLabel.setText(NbBundle.getMessage( JDBCWizardSelectionPanel.class, "LBL_SelectedTables_jLabell"));
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().add(44, 44, 44).add(
                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(
                                org.jdesktop.layout.GroupLayout.LEADING,
                                layout.createSequentialGroup().add(this.jLabel1,
                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(25, 25, 25).add(
                                        this.datasourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 288,
                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(
                                layout.createSequentialGroup().add(
                                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                                layout.createSequentialGroup().add(this.jScrollPane2,
                                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110,
                                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                                                        org.jdesktop.layout.LayoutStyle.RELATED,
                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(
                                                        layout.createParallelGroup(
                                                                org.jdesktop.layout.GroupLayout.TRAILING, false).add(
                                                                this.addButton,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116,
                                                                Short.MAX_VALUE).add(this.removeButton,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE).add(this.addAllButton,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE).add(this.removeAllButton,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE)).add(26, 26, 26)).add(
                                                layout.createSequentialGroup().add(this.availableTablesLabel).add(190, 190,
                                                        190))).add(
                                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                                this.selectedTablesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(this.jScrollPane3,
                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111,
                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))).addContainerGap(35,
                        Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().add(53, 53, 53).add(
                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(this.jLabel1).add(
                                this.datasourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18,
                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(
                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                layout.createSequentialGroup().add(89, 89, 89).add(this.addButton).addPreferredGap(
                                        org.jdesktop.layout.LayoutStyle.RELATED).add(this.removeButton).add(16, 16, 16).add(
                                        this.addAllButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                                        this.removeAllButton)).add(
                                layout.createSequentialGroup().add(31, 31, 31).add(
                                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(
                                                this.availableTablesLabel).add(this.selectedTablesLabel)).add(14, 14, 14).add(
                                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                                this.jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 163,
                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(this.jScrollPane3,
                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)))).addContainerGap(
                        44, Short.MAX_VALUE)));
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
        Set oldConnections = new HashSet(Arrays.asList(ConnectionManager.getDefault().getConnections()));
        
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
    
    		    final String[][] tableList = DBMetaData.getTablesOnly("", this.selectedConnection.getSchema(), "", false,connection);
    		    if ("ORACLE".equalsIgnoreCase(this.dbtype) && DBMetaData.getDatabaseMajorVersion(connection) >= 10) { // NOI18N
                    recycleBinTables = DBMetaData.getOracleRecycleBinTables(connection);
                } else {
                    recycleBinTables = Collections.EMPTY_LIST;
                }

    			String[] currTable = null;
    			List tableNamesList = new ArrayList();
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

        if (largestString.length() < JDBCWizardSelectionPanel.LBL_SOURCE_MSG.length()) {
            largestString = JDBCWizardSelectionPanel.LBL_SOURCE_MSG;
        } else if (largestString.length() < JDBCWizardSelectionPanel.LBL_DEST_MSG.length()) {
            largestString = JDBCWizardSelectionPanel.LBL_DEST_MSG;
        }

        this.srcRenderer = new DBModelNameCellRenderer(largestString);
        this.destRenderer = new DBModelNameCellRenderer(largestString);

        this.visibleCt = Math.min(Math.max(JDBCWizardSelectionPanel.MINIMUM_VISIBLE, this.listModel.getMaximumListSize()), JDBCWizardSelectionPanel.MAXIMUM_VISIBLE);

        this.addButton.setModel(this.listModel.getAddButtonModel());
        this.removeButton.setModel(this.listModel.getRemoveButtonModel());
        this.addAllButton.setModel(this.listModel.getAddAllButtonModel());
        this.removeAllButton.setModel(this.listModel.getRemoveAllButtonModel());
        
        this.availableTablesList = new JList((this.listModel.getSourceList()).toArray());
        this.availableTablesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.availableTablesList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2 && e.getSource() instanceof JList) {
                    final JList list = (JList) e.getSource();
                    final int[] indices = list.getSelectedIndices();
                    final Object[] selections = list.getSelectedValues();
                    JDBCWizardSelectionPanel.this.listModel.add(selections, indices);
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
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2 && e.getSource() instanceof JList) {
                    final JList list = (JList) e.getSource();
                    final int[] indices = list.getSelectedIndices();
                    final Object[] selections = list.getSelectedValues();
                    JDBCWizardSelectionPanel.this.listModel.remove(selections, indices);
                }
            }
        });

        this.selectedTablesList.setPrototypeCellValue(this.destRenderer);
        this.selectedTablesList.setCellRenderer(this.destRenderer);
        this.selectedTablesList.setVisibleRowCount(this.visibleCt);

        this.selectedTablesList.addListSelectionListener(this);
        this.jScrollPane2.setViewportView(this.availableTablesList);
        this.jScrollPane3.setViewportView(this.selectedTablesList);
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
         return new HelpCtx(JDBCWizardSelectionPanel.class);
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
	        		this.jScrollPane2.setViewportView(this.availableTablesList);
	        	}
         	}
        	if(this.selectedTablesList != null){
        		if(listModel != null){
	        		this.listModel.getSourceList().clear();
	        		this.jScrollPane3.setViewportView(this.selectedTablesList);
        		}
        	}
        	return;
        }
        
		if(listModel!= null){
			List selList = this.listModel.getDestinationList();
        	List selTabList = new ArrayList();
        	Iterator itr = selList.iterator();
        	while(itr.hasNext()){
        		DBTable tabObj = (DBTable)populateDBTable((String)itr.next());
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
		 final String[][] tableList = DBMetaData.getTablesOnly("", "", "", false,connection);
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
                        return ((DBTable)ffTable);
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
     * @see JDBCWizardPanel#addChangeListener
     */
    public final void addChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.add(l);
        }
    }

    /**
     * @see JDBCWizardPanel#removeChangeListener
     */
    public final void removeChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.remove(l);
        }
    }

    /**
     * @see JDBCWizardPanel#fireChangeEvent
     */
    public void fireChangeEvent() {
        Iterator it;

        synchronized (this.listeners) {
            it = new HashSet(this.listeners).iterator();
        }

        final ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    /**
     * @see JDBCWizardPanel#isValid
     */
    public boolean isValid() {
        boolean returnVal = false;
         if (selTableLen > 0) {
			returnVal = true;
		}
        return returnVal;
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
            JDBCWizardSelectionPanel.this.fireChangeEvent();
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

            JDBCWizardSelectionPanel.this.fireChangeEvent();
            this.updateUI();
        }

        /**
         * Add a ChangeListener to this model.
         * 
         * @param l ChangeListener to add
         */
        public void addChangeListener(final ChangeListener l) {
            if (l != null) {
                synchronized (this.changeListeners) {
                    this.changeListeners.add(l);
                }
            }
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
            JDBCWizardSelectionPanel.this.fireChangeEvent();
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
            JDBCWizardSelectionPanel.this.fireChangeEvent();
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
            JDBCWizardSelectionPanel.this.availableTablesList = new JList((JDBCWizardSelectionPanel.this.listModel.getSourceList()).toArray());
            JDBCWizardSelectionPanel.this.selectedTablesList = new JList((JDBCWizardSelectionPanel.this.listModel.getDestinationList()).toArray());
            JDBCWizardSelectionPanel.this.availableTablesList.setPrototypeCellValue(JDBCWizardSelectionPanel.this.srcRenderer);
            JDBCWizardSelectionPanel.this.availableTablesList.setCellRenderer(JDBCWizardSelectionPanel.this.srcRenderer);
            JDBCWizardSelectionPanel.this.availableTablesList.setVisibleRowCount(JDBCWizardSelectionPanel.this.visibleCt);

            JDBCWizardSelectionPanel.this.selectedTablesList.setPrototypeCellValue(JDBCWizardSelectionPanel.this.destRenderer);
            JDBCWizardSelectionPanel.this.selectedTablesList.setCellRenderer(JDBCWizardSelectionPanel.this.destRenderer);
            JDBCWizardSelectionPanel.this.selectedTablesList.setVisibleRowCount(JDBCWizardSelectionPanel.this.visibleCt);

            JDBCWizardSelectionPanel.this.jScrollPane2.setViewportView(JDBCWizardSelectionPanel.this.availableTablesList);
            JDBCWizardSelectionPanel.this.jScrollPane3.setViewportView(JDBCWizardSelectionPanel.this.selectedTablesList);
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

        if (JDBCWizardSelectionPanel.LBL_ADD.equals(cmd)) {
            final int[] indices = this.availableTablesList.getSelectedIndices();
            if (indices.length <= 1) {
            final Object[] selections = this.availableTablesList.getSelectedValues();
            this.listModel.add(selections, indices);
            } else {
                Object[] options = { "OK" };
                JOptionPane.showOptionDialog(WindowManager.getDefault().getMainWindow(),
                        NbBundle.getMessage(JDBCWizardSelectionPanel.class, "WARNING_IN_SELECTING_TABLES"), "Warning", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            }
        } else if (JDBCWizardSelectionPanel.LBL_ADD_ALL.equals(cmd)) {
            this.listModel.addAll();
        } else if (JDBCWizardSelectionPanel.LBL_REMOVE.equals(cmd)) {
            final int[] indices = this.selectedTablesList.getSelectedIndices();
            final Object[] selections = this.selectedTablesList.getSelectedValues();
            this.listModel.remove(selections, indices);
        } else if (JDBCWizardSelectionPanel.LBL_REMOVE_ALL.equals(cmd)) {
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
	
	private ListTransferModel listModel;

    private final List dsList = new ArrayList();

    // private JList destList;

    private final List destColl = new ArrayList();

    // private JList sourceList;

    public static final String LBL_DEST_MSG = "Selected Tables:";

    public static final String LBL_SOURCE_MSG = "Available Tables:";

    /** Maximum number of visible items in lists */
    public static final int MAXIMUM_VISIBLE = 10;

    /** Minimum number of visible items in lists */
    public static final int MINIMUM_VISIBLE = 5;

    /** Indicates addition of item(s). */
    public static final String LBL_ADD = ">";

    /** Label indicating that all elements should be moved. */
    public static final String LBL_ALL = "ALL";

    /** Indicates addition of all source items. */
    public static final String LBL_ADD_ALL = JDBCWizardSelectionPanel.LBL_ALL + " " + JDBCWizardSelectionPanel.LBL_ADD;

    /** Indicates removal of item(s). */
    public static final String LBL_REMOVE = "<";

    /** Indicates removal of all destination items. */
    public static final String LBL_REMOVE_ALL = JDBCWizardSelectionPanel.LBL_REMOVE + " " + JDBCWizardSelectionPanel.LBL_ALL;

    private DatabaseModel dbmodel;

    private javax.swing.JButton addButton;

    private javax.swing.JButton removeButton;

    private javax.swing.JButton addAllButton;

    private javax.swing.JButton removeAllButton;

    private javax.swing.JComboBox datasourceComboBox;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel availableTablesLabel;

    private javax.swing.JLabel selectedTablesLabel;

    private javax.swing.JList availableTablesList;

    private javax.swing.JList selectedTablesList;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JScrollPane jScrollPane3;
}
