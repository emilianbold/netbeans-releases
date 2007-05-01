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
package org.netbeans.modules.etl.ui.view.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.DesignTimeDBConnectionProvider;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerConnectionUtil;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;

import com.sun.sql.framework.utils.StringUtil;
import com.sun.sql.framework.utils.Logger;

/**
 * Presents choice source and target tables for inclusion in an ETL Definition.
 *
 * @author Sanjeeth Duvuru
 * @version $Revision$
 */
public class ETLTableSelectionWizard extends ETLWizard {
    
    private static final String LOG_CATEGORY = ETLTableSelectionWizard.class.getName();
    
    class Descriptor extends ETLWizardDescriptor {
        public Descriptor(WizardDescriptor.Iterator iter) {
            super(iter, context);
        }
    }
    
    class WizardIterator extends ETLWizardIterator {
        
        private List panels;
        
        private List srcModels;
        
        private List targetModels;
        
        public WizardIterator(ETLDefinitionImpl def) {
            srcModels = getDatabaseModels(def, def.getSourceDatabaseModels(), SQLConstants.SOURCE_DBMODEL);
            targetModels = getDatabaseModels(def, def.getTargetDatabaseModels(), SQLConstants.TARGET_DBMODEL);
            List currModels = new ArrayList();
            panels = new ArrayList(2);            
            sourceTransferPanel = new ETLCollaborationWizardTransferFinishPanel(NbBundle.getMessage(ETLTableSelectionWizard.class,
                    "TITLE_tblwizard_selectsources"), srcModels, currModels, true, true);            
            panels.add(sourceTransferPanel);            
            targetTransferPanel = new ETLCollaborationWizardTransferFinishPanel(NbBundle.getMessage(ETLTableSelectionWizard.class,
                    "TITLE_tblwizard_selecttargets"), targetModels, currModels, false, true);            
            panels.add(targetTransferPanel);
        }
        
        public String name() {
            return "Select Source/Target Tables";
        }
        
        protected List createPanels() {
            return Collections.unmodifiableList(panels);
        }
        
        protected String[] createSteps() {
            try {
                return new String[] { NbBundle.getMessage(ETLTableSelectionWizard.class, "STEP_tblwizard_sources"),
                NbBundle.getMessage(ETLTableSelectionWizard.class, "STEP_tblwizard_targets")};
            } catch (MissingResourceException e) {
                Logger.printThrowable(Logger.DEBUG, LOG_CATEGORY, "createPanelTitles()", "Could not locate steps strings.", e);
                return new String[] {};
            }
        }
        
        // this will return list of already existing dbmodels
        // this will also remove already existing dbModels from passed all DBModel list
        // (i.e. dbModels)
        private List getDatabaseModels(ETLDefinitionImpl def, Collection dbModels, int type) {
            ArrayList list = new ArrayList();
            Iterator it = dbModels.iterator();
            
            List selModels = Collections.EMPTY_LIST;
            if (type == org.netbeans.modules.sql.framework.model.SQLConstants.TARGET_DBMODEL) {
                selModels = def.getTargetDatabaseModels();
            } else {
                selModels = def.getSourceDatabaseModels();
            }
            
            // This flag is used to find out if a DatabaseModel is already used
            // if so then we delete that DatabaseModel from the passed in all available
            // dbmodel list
            boolean dbModelAlreadyUsed = false;
            
            while (it.hasNext()) {
                // this is already a dbmodel as passed in this metohd
                SQLDBModel newDbModel = (SQLDBModel) it.next();
                
                Iterator selIter = selModels.iterator();
                while (selIter.hasNext()) {
                    SQLDBModel selModel = (SQLDBModel) selIter.next();
                    if (isIdenticalModel(selModel, newDbModel)) {
                        dbModelAlreadyUsed = true;
                        break;
                    }
                }
                DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
                DatabaseConnection dbconn = null;
                if (dbModelAlreadyUsed) {
                    DesignTimeDBConnectionProvider provider = new DesignTimeDBConnectionProvider();
                    try {
                        for(int i = 0; i < connections.length; i++) {
                            if(connections[i].getDatabaseURL().equals(
                                    newDbModel.getETLDBConnectionDefinition().getConnectionURL())) {
                                dbconn = connections[i];
                                break;
                            }
                        }
                        if(dbconn == null) {
                            JDBCDriver drv = DBExplorerConnectionUtil.registerDriverInstance(
                                    newDbModel.getETLDBConnectionDefinition().getDriverClass());
                            dbconn = DatabaseConnection.create(drv, newDbModel.getETLDBConnectionDefinition().getConnectionURL(),
                                    newDbModel.getETLDBConnectionDefinition().getUserName(), (String)null,
                                    newDbModel.getETLDBConnectionDefinition().getPassword(), true);
                            ConnectionManager.getDefault().addConnection(dbconn);
                        }
                    } catch (Exception ex) {
                        //ignore
                    }
                    list.add(dbconn);
                    // remove this db model from passed in all dbmodel list
                    it.remove();
                    // reset the flag so that it can be set again
                    dbModelAlreadyUsed = false;
                }
            }
            
            return list;
        }
        
        private boolean isIdenticalModel(SQLDBModel model1, SQLDBModel model2) {
            boolean identical = model1.getModelName().equals(model2.getModelName());
            
            final DBConnectionDefinition c1 = model1.getConnectionDefinition();
            final DBConnectionDefinition c2 = model2.getConnectionDefinition();
            
            if (c1 != null && c2 != null) {
                identical &= StringUtil.isIdentical(c1.getConnectionURL(), c2.getConnectionURL())
                && StringUtil.isIdentical(c1.getUserName(), c2.getUserName()) && StringUtil.isIdentical(c1.getPassword(), c2.getPassword());
            }
            
            return identical;
        }
        
        protected List createPanels(WizardDescriptor wiz) {
            storeSettings(wiz, srcModels, targetModels);
            return createPanels();
        }
        
        public void storeSettings(Object settings, List srcModels, List targetModels) {
            WizardDescriptor wd = null;
            if (settings instanceof ETLWizardContext) {
                ETLWizardContext wizardContext = (ETLWizardContext) settings;
                wd = (WizardDescriptor) wizardContext
                        .getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
                
            } else if (settings instanceof WizardDescriptor) {
                wd = (WizardDescriptor) settings;
            }
            
            if (wd != null) {
                // Don't commit if user didn't click next.
                // if (wd.getValue() != WizardDescriptor.NEXT_OPTION) {
                // return;
                // }
                wd.putProperty(ETLCollaborationWizard.DATABASE_SOURCES, srcModels
                        .toArray());
                wd.putProperty(ETLCollaborationWizard.DATABASE_TARGETS, targetModels
                        .toArray());
            }
        }
    }
    
    /** Constant string key for storing/retrieving destination OTDs from wizard context. */
    public static final String DESTINATION_MODELS = ETLCollaborationWizard.TARGET_DB;
    
    /** Constant string key for storing/retrieving destination tables from wizard context. */
    public static final String DESTINATION_TABLES = "destination_tables"; // NOI18N
    
    /** Constant string key for storing/retrieving source OTDs from wizard context. */
    public static final String SOURCE_MODELS = ETLCollaborationWizard.SOURCE_DB;
    
    /** Constant string key for storing/retrieving source tables from wizard context. */
    public static final String SOURCE_TABLES = "source_tables"; // NOI18N
    
    /* Log4J category string */
    //private static final String LOG_CATEGORY = ETLTableSelectionWizard.class.getName();
    
    /**
     * Iterates through the given List, removing db model that have no DBTable instances where
     * isSelected() returns true.
     *
     * @param List of DatabaseModel instances to be filtered
     * @return List (possibly empty) of DatabaseModel instances containing only selected
     *         DBTable instances.
     */
    public static final List removeModelsWithNoSelectedTables(List list) {
        List filteredList = Collections.EMPTY_LIST;
        
        if (list != null && !list.isEmpty()) {
            filteredList = new ArrayList(list.size());
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                SQLDBModel dbModel = (SQLDBModel) iter.next();
                
                // Don't filter out unselected tables from OTDs just yet...user might
                // go back and change their selections, but the underlying DBModel
                // will be out-of-sync with the GUI.
                if (hasSelectedTables(dbModel)) {
                    filteredList.add(dbModel);
                }
            }
        }
        
        return filteredList;
    }
    
    static final void removeUnselectedTables(List otds) {
        Iterator it = otds.iterator();
        while (it.hasNext()) {
            removeUnselectedTables((SQLDBModel) it.next());
        }
    }
    
    static final void removeUnselectedTables(SQLDBModel dbModel) {
        List tables = dbModel.getTables();
        Iterator it = tables.iterator();
        
        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            if (!table.isSelected() || !table.isEditable()) {
                dbModel.deleteTable(dbModel.getFullyQualifiedTableName(table));
            }
        }
    }
    
    /*
     * Indicates whether the given SQLDBModel instance has at least one table
     * selected by the user. Does not remove unselected tables from <code> dbModel </code>
     * as is the case with <code> removeUnselectedTables </code> . @param dbModel
     * SQLDBModel instance whose tables are to be inspected @return true if
     * <code> dbModel </code> has at least one selected table.
     */
    private static final boolean hasSelectedTables(SQLDBModel dbModel) {
        boolean hasSelected = false;
        
        List tables = dbModel.getTables();
        Iterator it = tables.iterator();
        
        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            if (table.isSelected() && table.isEditable()) {
                hasSelected = true;
                break;
            }
        }
        
        return hasSelected;
    }
    
    /* Defines panels to be displayed */
    private WizardDescriptor descriptor;
    
    private ETLDefinitionImpl etlDef;
    
    /* Wizard iterator; handles display and movement among wizard panels */
    private ETLWizardIterator iterator;
    
    private ETLCollaborationWizardTransferFinishPanel sourceTransferPanel;
    
    private ETLCollaborationWizardTransferFinishPanel targetTransferPanel;
    
    /**
     * Creates a new instance of DatabaseOTDWizard
     *
     * @param def ETLDefinitionProjectElement containing table data
     */
    public ETLTableSelectionWizard(ETLDefinitionImpl def) {
        etlDef = def;
    }
    
    /**
     * @see ETLWizard#getDescriptor
     */
    public WizardDescriptor getDescriptor() {
        return descriptor;
    }
    
    /**
     * @see ETLWizard#getIterator
     */
    public WizardDescriptor.Iterator getIterator() {
        return iterator;
    }
    
    /**
     * Gets List of destination OTDs as selected by user.
     *
     * @return List (possibly empty) of selected destination OTDs
     */
    public List getSelectedDestinationModels() {
        return getSelectedModelsOfType(ETLTableSelectionWizard.DESTINATION_MODELS);
    }
    
    /**
     * Gets List of source Models as selected by user.
     *
     * @return List (possibly empty) of selected source OTDs
     */
    public List getSelectedSourceModels() {
        return getSelectedModelsOfType(ETLTableSelectionWizard.SOURCE_MODELS);
    }
    
    /**
     * Initializes iterator and descriptor for this wizard.
     */
    public void initialize() {
        iterator = new WizardIterator(etlDef);
        descriptor = new Descriptor(iterator);
        if (sourceTransferPanel != null) {
            sourceTransferPanel.updatePanelState();
        }
        
        if (targetTransferPanel != null) {
            targetTransferPanel.updatePanelState();
        }
    }
    
    /**
     * Performs processing to handle cancellation of this wizard.
     */
    protected void cancel() {
    }
    
    /**
     * Performs processing to cleanup any resources used by this wizard.
     */
    protected void cleanup() {
    }
    
    /**
     * Performs processing to handle committal of data gathered by this wizard.
     */
    protected void commit() {
        // Create union of source and target model lists, then remove unselected tables
        // from them.
        List models = new ArrayList(getSelectedSourceModels());
        models.addAll(getSelectedDestinationModels());
        
        ETLTableSelectionWizard.removeUnselectedTables(models);
    }
    
    /**
     * @see org.netbeans.modules.etl.ui.view.wizards.ETLWizard#getDialogTitle()
     */
    protected String getDialogTitle() {
        return NbBundle.getMessage(ETLTableSelectionWizard.class, "TITLE_dlg_selecttables");
    }
    
    private List getSelectedModelsOfType(String typeKey) {
        List selections = Collections.EMPTY_LIST;
        
        if (descriptor != null && typeKey != null) {
            selections = ETLTableSelectionWizard.removeModelsWithNoSelectedTables((List) descriptor.getProperty(typeKey));
        }
        
        return selections;
    }
}

