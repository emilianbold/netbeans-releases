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
import org.netbeans.modules.sql.framework.common.jdbc.DesignTimeDBConnectionProvider;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerConnectionUtil;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;

import com.sun.sql.framework.utils.StringUtil;
import com.sun.sql.framework.utils.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.netbeans.modules.mashup.db.ui.AxionDBConfiguration;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;

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
        public WizardDescriptor wiz;

        public WizardIterator(ETLDefinitionImpl def) {
            panels = new ArrayList(2);
            List srcModel = new ArrayList();
            List destModel = new ArrayList();
            List dbModels = getModelConnections();
            sourceTransferPanel = new ETLCollaborationWizardTransferFinishPanel(NbBundle.getMessage(ETLCollaborationWizard.class, "TITLE_tblwizard_selectsources"), dbModels, srcModel, true);
            panels.add(sourceTransferPanel);
            targetTransferPanel = new ETLCollaborationWizardTransferFinishPanel(NbBundle.getMessage(ETLCollaborationWizard.class, "TITLE_tblwizard_selecttargets"), dbModels, destModel, false);
            panels.add(targetTransferPanel);
        }

        private String getDefaultWorkingFolder() {
            File conf = AxionDBConfiguration.getConfigFile();
            Properties prop = new Properties();
            try {
                FileInputStream in = new FileInputStream(conf);
                prop.load(in);
            } catch (FileNotFoundException ex) {
            //ignore
            } catch (IOException ex) {
            //ignore
            }
            return prop.getProperty(AxionDBConfiguration.PROP_DB_LOC);
        }

        private List getModelConnections() {
            List model = new ArrayList();
            // add flatfile databases to db explorer.
            String workDir = getDefaultWorkingFolder();
            File f = new File(workDir);
            File[] db = null;
            if (f.exists()) {
                db = f.listFiles();
                for (int i = 0; i < db.length; i++) {
                    String ver = null;
                    try {
                        ver = db[i].getCanonicalPath() + "\\" + db[i].getName().toUpperCase() + ".VER";
                        File version = new File(ver);
                        if (version.exists()) {
                            String url = "jdbc:axiondb:" + db[i].getName() + ":" + getDefaultWorkingFolder() + db[i].getName();
                            DatabaseConnection con = ConnectionManager.getDefault().getConnection(url);
                            if (con == null) {
                                DBExplorerConnectionUtil.createConnection("org.axiondb.jdbc.AxionDriver", url, "sa", "sa");
                            }
                        }
                    } catch (Exception ex) {
                    //ignore
                    }
                }
            }
            DatabaseConnection[] conns = ConnectionManager.getDefault().getConnections();
            if (conns.length > 0) {
                for (int i = 0; i < conns.length; i++) {
                    if (conns[i] == null) {
                        Logger.print(Logger.INFO, LOG_CATEGORY, null, "Got Null connection.");
                        model.add("<NULL>");
                    } else {
                        model.add(conns[i]);
                    }
                }
            } else {
                model.add("<None>");
            }
            return model;
        }

        public String name() {
            return "Select Source/Target Tables";
        }

        protected List createPanels() {
            return Collections.unmodifiableList(panels);
        }

        protected String[] createSteps() {
            try {
                return new String[]{NbBundle.getMessage(ETLTableSelectionWizard.class, "STEP_tblwizard_sources"), NbBundle.getMessage(ETLTableSelectionWizard.class, "STEP_tblwizard_targets")};
            } catch (MissingResourceException e) {
                Logger.printThrowable(Logger.DEBUG, LOG_CATEGORY, "createPanelTitles()", "Could not locate steps strings.", e);
                return new String[] 
        {};
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
                        for (int i = 0; i < connections.length; i++) {
                            if (connections[i].getDatabaseURL().equals(newDbModel.getETLDBConnectionDefinition().getConnectionURL())) {
                                dbconn = connections[i];
                                break;
                            }
                        }
                        if (dbconn == null) {
                            JDBCDriver drv = DBExplorerConnectionUtil.registerDriverInstance(newDbModel.getETLDBConnectionDefinition().getDriverClass());
                            dbconn = DatabaseConnection.create(drv, newDbModel.getETLDBConnectionDefinition().getConnectionURL(), newDbModel.getETLDBConnectionDefinition().getUserName(), (String) null, newDbModel.getETLDBConnectionDefinition().getPassword(), true);
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
                identical &= StringUtil.isIdentical(c1.getConnectionURL(), c2.getConnectionURL()) && StringUtil.isIdentical(c1.getUserName(), c2.getUserName()) && StringUtil.isIdentical(c1.getPassword(), c2.getPassword());
            }

            return identical;
        }

        protected List createPanels(WizardDescriptor wiz) {
            List dbModels = getModelConnections();
            storeSettings(wiz, dbModels);
            return createPanels();
        }

        public void storeSettings(Object settings, List models) {
            WizardDescriptor wd = null;
            if (settings instanceof ETLWizardContext) {
                ETLWizardContext wizardContext = (ETLWizardContext) settings;
                wd = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);

            } else if (settings instanceof WizardDescriptor) {
                wd = (WizardDescriptor) settings;
            }
            if (wd != null) {
                // Don't commit if user didn't click next.
                // if (wd.getValue() != WizardDescriptor.NEXT_OPTION) {
                // return;
                // }
                wd.putProperty(ETLCollaborationWizard.DATABASE_SOURCES, models.toArray());
                wd.putProperty(ETLCollaborationWizard.DATABASE_TARGETS, models.toArray());
            }
        }
    }
    /** Constant string key for storing/retrieving destination Databases from wizard context. */
    public static final String DESTINATION_MODELS = ETLCollaborationWizard.TARGET_DB;
    /** Constant string key for storing/retrieving destination tables from wizard context. */
    public static final String DESTINATION_TABLES = "destination_tables"; // NOI18N

    /** Constant string key for storing/retrieving source Databases from wizard context. */
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

                // Don't filter out unselected tables from Databases just yet...user might
                // go back and change their selections, but the underlying DBModel
                // will be out-of-sync with the GUI.
                if (hasSelectedTables(dbModel)) {
                    filteredList.add(dbModel);
                }
            }
        }

        return filteredList;
    }

    static final void removeUnselectedTables(List db) {
        Iterator it = db.iterator();
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
     * Creates a new instance of DatabaseWizard
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
     * Gets List of destination Databases as selected by user.
     *
     * @return List (possibly empty) of selected destination Databases
     */
    public List getSelectedDestinationModels() {
        return getSelectedModelsOfType(ETLTableSelectionWizard.DESTINATION_MODELS);
    }

    /**
     * Gets List of source Models as selected by user.
     *
     * @return List (possibly empty) of selected source Databases
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

