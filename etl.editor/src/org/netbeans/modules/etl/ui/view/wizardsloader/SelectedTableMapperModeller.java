/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.etl.ui.view.wizardsloader;

import com.sun.etl.exception.BaseException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.model.ETLDefinition;
import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.impl.AbstractDBTable;
import org.netbeans.modules.sql.framework.model.impl.RuntimeDatabaseModelImpl;
import org.netbeans.modules.sql.framework.model.impl.RuntimeOutputImpl;
import org.netbeans.modules.sql.framework.model.impl.SQLJoinTableImpl;
import org.netbeans.modules.sql.framework.model.impl.SQLJoinViewImpl;
import org.netbeans.modules.sql.framework.model.impl.SourceTableImpl;
import org.netbeans.modules.sql.framework.model.impl.TargetTableImpl;
import org.netbeans.modules.sql.framework.model.impl.VisibleSQLPredicateImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;

/**
 *
 * @author Manish Bharani
 */
public class SelectedTableMapperModeller {

    private String collbnameprifix = null;
    private List sortedSrcTbls = null;
    private List<AbstractDBTable> sortedTrgtTbls = null;
    private List<File> sortedBulkLoaderSrcFiles = null;
    HashMap<String, List> mappedTrgtModels = new HashMap<String, List>();
    HashMap<String, List> mappedSrcModels = new HashMap<String, List>();
    HashMap<String, List> srcselectionmap = new HashMap<String, List>(); /* Keeps track of source table names mapped to target */
    //Loggers
    private static transient final Logger mLogger = Logger.getLogger(SelectedTableMapperModeller.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    //Default ETL Stratagy
    int defaultStratagy = SQLDefinition.EXECUTION_STRATEGY_DEFAULT;
    //Flag if bulk loader
    private boolean IS_BULK_LOADER = false;
    // Visual Panel
    SelectedTableMapperVisualPanel vpanel;

    public SelectedTableMapperModeller(SelectedTableMapperVisualPanel vpanel) {
        this.vpanel = vpanel;
    }

    public void initModelMapper(List srcmodels, List<SQLDBModel> trgtmodels, SelectedTableMapperPanel owner, String collabname, boolean isBulkLoader) {
        this.collbnameprifix = collabname;
        IS_BULK_LOADER = isBulkLoader;
        // Create Sorted Target Tbl List
        processPersistedModelList(trgtmodels, false);
        // Create Sorted Source Tbl List
        if (IS_BULK_LOADER) {
            sortedBulkLoaderSrcFiles = getSortedFileList(srcmodels);
        } else {
            processPersistedModelList(srcmodels, true);
        }

        //Initialize Map to contain src indicies selected
        initModelStructures();
        //Create Target Models
        createModels(sortedTrgtTbls, trgtmodels, null, false);
    }

    protected boolean updateSelectedSourceIndexFromPanel(String trgttblkey, int[] selindicies) {

        List selectionindxlist = null;
        if (srcselectionmap.containsKey(trgttblkey)) {
            selectionindxlist = srcselectionmap.get(trgttblkey);
        } else {
            selectionindxlist = new ArrayList();
            srcselectionmap.put(trgttblkey, selectionindxlist);
        }

        // Relist the selected source tables each time selection changes
        selectionindxlist.clear();
        // Populate with latest selection index for the selected target
        for (int i = 0; i < selindicies.length; i++) {
            selectionindxlist.add(i, new Integer(selindicies[i]));
        }

        List<String> unmappedtbls = getUnmappedTargetTables();
        if (unmappedtbls.size() > 0) {
            vpanel.reportMapStatus(error1, false);
        } else {
            vpanel.reportMapStatus(success1, true);
        }

        return true;
    }

    protected void generateUserMappedModels(List<SQLDBModel> srcmastermodellist) {
        Iterator i = srcselectionmap.keySet().iterator();
        while (i.hasNext()) {
            String trgtkey = (String) i.next();

            //Create a DBTable list for src tables participating in user selected mapping 
            List<Integer> indicies = srcselectionmap.get(trgtkey);
            AbstractDBTable[] tempsrctblholder = new AbstractDBTable[indicies.size()];
            int count = 0;

            for (Integer currindex : indicies) {
                tempsrctblholder[count] = (AbstractDBTable) sortedSrcTbls.get(currindex.intValue());
                count++;
            }
            createModels(Arrays.asList(tempsrctblholder), srcmastermodellist, trgtkey, true);
        }
        generateETLArtifacts();
    }

    private void initModelStructures() {
        for (AbstractDBTable dbtable : sortedTrgtTbls) {
            String key = getSearchKey(dbtable);
            srcselectionmap.put(key, new ArrayList());
            mappedTrgtModels.put(key, new ArrayList<SQLDBModel>());
            mappedSrcModels.put(key, new ArrayList<SQLDBModel>());
        }
    }

    private void createModels(List<AbstractDBTable> tbls, List<SQLDBModel> masterdbmodels, String mapkey, boolean isSource) {
        if (isSource) {
            HashMap<DBConnectionDefinition, List> conndefToTblMap = new HashMap<DBConnectionDefinition, List>();
            for (AbstractDBTable tbl : tbls) {
                //Seach for this table in masterdbmodels and get connection def
                DBConnectionDefinition conndef = searchConnectionDefFromModels(tbl, masterdbmodels);
                if (conndef != null) {
                    if (conndefToTblMap.containsKey(conndef)) {
                        //Map the src table to connection definition
                        ((List) conndefToTblMap.get(conndef)).add(tbl);
                    } else {
                        //Add a new connection definition and Map the table
                        List<AbstractDBTable> tblsinmodel = new ArrayList<AbstractDBTable>();
                        tblsinmodel.add(tbl);
                        conndefToTblMap.put(conndef, tblsinmodel);
                    }
                } else {
                    mLogger.warnNoloc("Connection Definition for source table [" + ((SQLDBTable) tbl).getName() + "] is null");
                }
            }

            // Process ConnDef Map with tables
            Iterator i = conndefToTblMap.keySet().iterator();
            //Insert New Src models into its map
            List<SQLDBModel> modelmap = mappedSrcModels.get(mapkey);
            if (modelmap != null) {
                while (i.hasNext()) {
                    DBConnectionDefinition conndef = (DBConnectionDefinition) i.next();
                    //Each connectiondef will have its own model
                    SQLDBModel newmodel = SQLModelObjectFactory.getInstance().createDBModel(SQLConstants.SOURCE_DBMODEL);
                    String srcmodelname = searchModelName(conndef, masterdbmodels);
                    //Add conndef
                    newmodel.setConnectionDefinition(conndef);
                    //Add Model name
                    newmodel.setModelName(srcmodelname);
                    //Add tables
                    List<AbstractDBTable> srctbls = (List) conndefToTblMap.get(conndef);
                    for (AbstractDBTable srctbl : srctbls) {
                        newmodel.addTable((SQLDBTable) srctbl);
                    }
                    modelmap.add(newmodel);
                }
            } else {
                mLogger.warnNoloc("Unable to find a place to store new source models for key : " + mapkey);
            }
        } else {
            for (AbstractDBTable trgttbl : tbls) {
                String key = getSearchKey(trgttbl);
                SQLDBModel newmodel = SQLModelObjectFactory.getInstance().createDBModel(SQLConstants.TARGET_DBMODEL);
                //Add conndef
                newmodel.setConnectionDefinition(setModelNameAndfetchConnDef((TargetTableImpl) trgttbl, masterdbmodels, newmodel));
                //Add tables
                newmodel.addTable((SQLDBTable) trgttbl);
                ((List) mappedTrgtModels.get(key)).add(newmodel);
            }
        }
    }

    private DBConnectionDefinition searchConnectionDefFromModels(AbstractDBTable tbl, List<SQLDBModel> masterdbmodels) {
        for (SQLDBModel sqlmodel : masterdbmodels) {
            if (sqlmodel.containsTable((SQLDBTable) tbl)) {
                return sqlmodel.getConnectionDefinition();
            }
        }
        return null;
    }

    private String searchModelName(DBConnectionDefinition conndef, List<SQLDBModel> masterdbmodels) {
        for (SQLDBModel sqlmodel : masterdbmodels) {
            if (sqlmodel.getConnectionDefinition().equals(conndef)) {
                return sqlmodel.getModelName();
            }
        }
        return null;
    }

    private DBConnectionDefinition setModelNameAndfetchConnDef(AbstractDBTable tbl, List<SQLDBModel> masterdbmodels, SQLDBModel newmodel) {
        for (SQLDBModel dbmodel : masterdbmodels) {
            if (dbmodel.containsTable((AbstractDBTable) tbl)) {
                newmodel.setModelName(dbmodel.getModelName());
                return dbmodel.getConnectionDefinition();
            }
        }
        return null;
    }

    protected void generateETLArtifacts() {
        ETLDefinition etldefinition = null;

        for (AbstractDBTable trgtdbtable : sortedTrgtTbls) {
            String searchkey = getSearchKey(trgtdbtable);
            String trgttblname = ((SQLDBTable) trgtdbtable).getName();

            etldefinition = createEtlDefinitition(trgttblname);
            SQLDefinition sqldefinition = etldefinition.getSQLDefinition();
            sqldefinition.setDisplayName(this.collbnameprifix + "_" + trgttblname);
            sqldefinition.setVersion(ETLDefinitionImpl.DOC_VERSION);

            try {
                //Setting Target Models
                List<SQLDBModel> trgtmodels = (List) mappedTrgtModels.get(searchkey);
                SQLDBModel trgtmodel = trgtmodels.get(0);
                sqldefinition.addObject(trgtmodel);
                //Add Runtime output params, Each Target Model will always have one table in the mapped model
                addRuntimeOutput(sqldefinition, (SQLDBTable) trgtmodel.getTables().get(0));

                //Setting Source Models
                List<SQLDBModel> srcmodels = (List) mappedSrcModels.get(searchkey);

                SQLJoinView jview = testAndCreateTableJoin(srcmodels);
                if (jview != null) {
                    sqldefinition.addObject(jview);
                    List<AbstractDBTable> joinedSrcTbls = (List) jview.getSourceTables();
                    for (AbstractDBTable srctbl : joinedSrcTbls) {
                        sqldefinition.addObject(srctbl);
                        attemptAutoMap(srctbl, trgtdbtable);
                    }
                } else {
                    if (srcmodels.size() != 0) {
                        //This is a case when a single src tbl is involved
                        AbstractDBTable srctbl = (AbstractDBTable) ((SQLDBModel) srcmodels.get(0)).getTables().get(0);
                        sqldefinition.addObject(srctbl);
                        attemptAutoMap(srctbl, trgtdbtable);
                    }
                }

                //Add Runtime Input Params
                List<AbstractDBTable> srctbls = aggregateTablesFromModelList(srcmodels);
                for (AbstractDBTable srctbl : srctbls) {
                    addRuntimeInput(sqldefinition, srctbl);
                }
                
                SQLObjectUtil.setOrgProperties(trgtdbtable);
                addRuntimeInput(sqldefinition, trgtdbtable);

            } catch (BaseException ex) {
                mLogger.errorNoloc("Error adding SQL Objects to etl model", ex);
            }

            File etlfile = new File(ETLEditorSupport.PRJ_PATH + File.separator + "Collaborations" + File.separator + this.collbnameprifix + "_" + trgttblname + ".etl");
            boolean success = true;
            try {
                success = etlfile.createNewFile();
            } catch (IOException ex) {
                mLogger.errorNoloc("Unable to write etl model to file : " + etlfile.getName(), ex);
            }
            if (!success) {
                //XXX handle this
            } else {
                try {
                    writeEtlDefFile(etlfile, etldefinition.toXMLString(null));
                } catch (BaseException ex) {
                    mLogger.errorNoloc("Unable to write etl model to file : " + etlfile.getName(), ex);
                }
            }
        }
    }

    private SQLJoinView testAndCreateTableJoin(List<SQLDBModel> srcmodellist) {
        List<AbstractDBTable> allmodeltbls = aggregateTablesFromModelList(srcmodellist);
        if (allmodeltbls != null) {
            if (allmodeltbls.size() > 1) {
                try {
                    // Create the tbl join
                    SQLJoinView jv = new SQLJoinViewImpl();
                    jv.setAliasName("J0");
                    jv.setDisplayName("JoinView");
                    for (AbstractDBTable tbl : allmodeltbls) {
                        SQLJoinTable jt = new SQLJoinTableImpl();
                        SourceTable srctbl = (SourceTable) tbl;
                        srctbl.setUsedInJoin(true);
                        jt.setSourceTable(srctbl);
                        jv.addObject(jt);
                    }
                    handleAutoJoins(jv);
                    previousJoin = null;
                    return jv;
                } catch (Exception ex) {
                     mLogger.errorNoloc("Error in handling auto join ", ex);
                }
            } else {
                for (AbstractDBTable tbl : allmodeltbls) {
                    ((SourceTable) tbl).setUsedInJoin(false);
                }
                return null;
            }
        }
        return null;
    }

    private void attemptAutoMap(AbstractDBTable srctable, AbstractDBTable trgttbl) {
        TargetTableImpl tt = (TargetTableImpl) trgttbl;

        List<DBColumn> srcColumnList = ((SourceTableImpl) srctable).getColumnList();
        for (DBColumn srcCol : srcColumnList) {
            DBColumn col = tt.getColumn(srcCol.getName());
            if (col != null) {
                try {
                    tt.addInput(col.getName(), (SQLObject) srcCol);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(SelectedTableMapperModeller.class.getName()).log(Level.SEVERE,"Error while attempting auto map "+ex);
                }
            }
        }
    }

    private ETLDefinition createEtlDefinitition(String trgtname) {
        ETLDefinition etldef = new ETLDefinitionImpl();
        etldef.setDisplayName(this.collbnameprifix + "_" + trgtname);
        etldef.setExecutionStrategyCode(defaultStratagy);
        return etldef;
    }

    private List<AbstractDBTable> aggregateTablesFromModelList(List<SQLDBModel> modellist) {
        List<AbstractDBTable> aggrtbls = new ArrayList<AbstractDBTable>();
        for (SQLDBModel sqlmodel : modellist) {
            List<AbstractDBTable> dbtbls = (List) sqlmodel.getTables();
            for (AbstractDBTable tbl : dbtbls) {
                AbstractDBTable dbtbl = (AbstractDBTable) tbl;
                aggrtbls.add(dbtbl);
            }
        }
        return aggrtbls;
    }

    private void addRuntimeInput(SQLDefinition sqldef, SQLDBTable sTable) {
        try {
            SourceColumn runtimeArg = SQLObjectUtil.createRuntimeInput(sTable, sqldef);
            SQLObjectUtil.setOrgProperties(sTable);

            if (runtimeArg != null && (RuntimeInput) runtimeArg.getParent() != null) {
                RuntimeInput runtimeInput = (RuntimeInput) runtimeArg.getParent();
                // if runtime input is not in SQL definition then add it
                if ((sqldef.isTableExists(runtimeInput)) == null) {
                    sqldef.addObject((SQLObject) runtimeInput);
                    mLogger.infoNoloc("Successfully added runtime input arg for src table :  " + sTable.getDisplayName());
                }
            }
        } catch (BaseException ex) {
            mLogger.errorNoloc("Unable to add input runtime args", ex);
        }
    }

    private void addRuntimeOutput(SQLDefinition sqldef, SQLDBTable sTable) {
        RuntimeDatabaseModel rtModel = sqldef.getRuntimeDbModel();
        if (rtModel == null) {
            rtModel = new RuntimeDatabaseModelImpl();
        }
        RuntimeOutput rtOut = rtModel.getRuntimeOutput();
        SQLDBColumn column = null;
        if (rtOut == null) {
            rtOut = new RuntimeOutputImpl();
            // add STATUS
            column = SQLModelObjectFactory.getInstance().createTargetColumn("STATUS", Types.VARCHAR, 0, 0, true);
            column.setEditable(false);
            rtOut.addColumn(column);

            // add STARTTIME
            column = SQLModelObjectFactory.getInstance().createTargetColumn("STARTTIME", Types.TIMESTAMP, 0, 0, true);
            column.setEditable(false);
            rtOut.addColumn(column);

            // add ENDTIME
            column = SQLModelObjectFactory.getInstance().createTargetColumn("ENDTIME", Types.TIMESTAMP, 0, 0, true);
            column.setEditable(false);
            rtOut.addColumn(column);
        }

        String argName = SQLObjectUtil.getTargetTableCountRuntimeOutput((TargetTable) sTable);
        column = SQLModelObjectFactory.getInstance().createTargetColumn(argName, Types.INTEGER, 0, 0, true);
        column.setEditable(false);
        rtOut.addColumn(column);
        rtModel.addTable(rtOut);
        try {
            sqldef.addObject(rtModel);
            mLogger.infoNoloc("Successfully added runtime output arg for target table :  " + sTable.getDisplayName());
        } catch (BaseException ex) {
            mLogger.errorNoloc("Failed to add runtime output model to target", ex);
        }

    }

    protected List<String> getTableNames(boolean isSource) {
        List names = new ArrayList<String>();
        if (isSource) {
            if (IS_BULK_LOADER) {
                for (File blsrcfile : sortedBulkLoaderSrcFiles) {
                    names.add(blsrcfile.getName());
                }
            } else {
                List<AbstractDBTable> tables = this.sortedSrcTbls;
                for (AbstractDBTable dbtable : tables) {
                    names.add(((SQLDBTable) dbtable).getName());
                }
            }
        } else {
            List<AbstractDBTable> tables = this.sortedTrgtTbls;
            for (AbstractDBTable dbtable : tables) {
                names.add(((SQLDBTable) dbtable).getName());
            }
        }
        return names;
    }

    protected List<AbstractDBTable> getSortedSrcTables() {
        return this.sortedSrcTbls;
    }

    protected List<AbstractDBTable> getSortedTargetTables() {
        return this.sortedTrgtTbls;
    }

    protected HashMap<String, List> getMappedTargetModels() {
        return this.mappedTrgtModels;
    }

    protected HashMap<String, List> getMappedSrcModels() {
        return this.mappedSrcModels;
    }

    protected HashMap<String, List> getSelectedSrcTableIndexFromMapper() {
        return srcselectionmap;
    }

    private void processPersistedModelList(List<SQLDBModel> sqldbmodellist, boolean isSource) {
        //Collect all the tables from List of Models passed.
        List<AbstractDBTable> alltables = new ArrayList();
        for (SQLDBModel model : sqldbmodellist) {
            mLogger.infoNoloc("Collecting Tables from the model : " + model.getModelName());
            List<AbstractDBTable> tableslist = (List) model.getTables();
            for (AbstractDBTable table : tableslist) {
                alltables.add(table);
            }
        }
        mLogger.infoNoloc("Total no of tables in all the models : " + alltables.size());

        if (isSource) {
            sortedSrcTbls = getSortedTableList(alltables);
        } else {
            //Target Tables
            sortedTrgtTbls = getSortedTableList(alltables);
        }
    }

    private List<AbstractDBTable> getSortedTableList(List<AbstractDBTable> tablelist) {
        Map tempmap = new LinkedHashMap();
        List<AbstractDBTable> sortedtbllist = new ArrayList<AbstractDBTable>();
        for (AbstractDBTable dbtable : tablelist) {
            String sortkey = getSearchKey(dbtable);
            mLogger.infoNoloc("Ordering Table with Search Key [" + sortkey + "]");
            tempmap.put(sortkey, dbtable);
        }

        List mapKeys = new ArrayList(tempmap.keySet());

        TreeSet sortedSet = new TreeSet(mapKeys);
        Object[] sortedArray = sortedSet.toArray();

        //Ascending Sort
        for (int i = 0; i < sortedArray.length; i++) {
            sortedtbllist.add(i, (AbstractDBTable) tempmap.get(sortedArray[i]));
        }
        return sortedtbllist;
    }

    private List<File> getSortedFileList(List<File> tablelist) {
        Map tempmap = new LinkedHashMap();
        List<File> sortedfilelist = new ArrayList<File>();
        for (File dbfile : tablelist) {
            mLogger.infoNoloc("Ordering File [" + dbfile.getAbsolutePath() + "]");
            tempmap.put(dbfile.getAbsolutePath(), dbfile);
        }

        List mapKeys = new ArrayList(tempmap.keySet());

        TreeSet sortedSet = new TreeSet(mapKeys);
        Object[] sortedArray = sortedSet.toArray();

        //Ascending Sort
        for (int i = 0; i < sortedArray.length; i++) {
            sortedfilelist.add(i, (File) tempmap.get(sortedArray[i]));
        }
        return sortedfilelist;
    }

    protected String getSearchKey(AbstractDBTable dbtable) {
        if ((dbtable.getSchema() != null) && (dbtable.getSchema().length() > 0)) {
            return dbtable.getUniqueTableName() + "_" + dbtable.getSchema();
        } else {
            return dbtable.getUniqueTableName();
        }
    }

    private void writeEtlDefFile(File etlfile, String etldefxml) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(etlfile);
            fos.write(etldefxml.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException ex) {
            mLogger.errorNoloc("Error Writing etl definition : " + etlfile.getName(), ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                mLogger.errorNoloc("Error closing stream", ex);
            }
        }
    }
    SQLJoinOperator previousJoin = null;

 private void handleAutoJoins(SQLJoinView joinView) throws BaseException {
     
        ArrayList<SQLObject> tablesSoFar = new ArrayList<SQLObject>();
        tablesSoFar.addAll(joinView.getSQLJoinTables());

        for (int i = 1; i < tablesSoFar.size(); i++) {
            SQLObject sqlJoinTbl = tablesSoFar.get(i);
            Collection joins = joinView.getObjectsOfType(SQLConstants.JOIN);
            Iterator jIt = joins.iterator();
            while (jIt.hasNext()) {
                SQLJoinOperator join = (SQLJoinOperator) jIt.next();
                if (join.isRoot()) {
                    previousJoin = join;
                }
            }

            //now find all the auto joins between obj and rest of the joinTables tables
            //and add them as well
            List joinList = SQLObjectUtil.getAutoJoins((SQLJoinTable) sqlJoinTbl, tablesSoFar);
            //create a SQLJoinOperator
            SQLJoinOperator join = SQLModelObjectFactory.getInstance().createSQLJoinOperator();
            //add join to model before calling addInput on it since add Input
            //keep tracks of storing root join information, so join needs to have
            //ad id which is set if we add the join first
            ArrayList<SQLJoinOperator> newJoin = new ArrayList<SQLJoinOperator>();
            newJoin.add(join);
            addObjects(newJoin, joinView);

            SQLCondition joinCondition = join.getJoinCondition();

            if (previousJoin == null) {                
                join.addInput(SQLJoinOperator.LEFT, (SQLJoinTable) tablesSoFar.get(0));
            } else {                
                join.addInput(SQLJoinOperator.LEFT, previousJoin);
            }
            join.addInput(SQLJoinOperator.RIGHT, sqlJoinTbl);

            //previousJoin = join;

            //two or more table join so need to build composite condition
            if (joinList.size() > 0) {
                Iterator it1 = joinList.iterator();
                SQLPredicate previousPredicate = null;

                if (it1.hasNext()) {
                    SQLJoinOperator joinNew = (SQLJoinOperator) it1.next();
                    SQLCondition joinNewCondition = joinNew.getJoinCondition();
                    previousPredicate = joinNewCondition.getRootPredicate();
                }

                while (it1.hasNext()) {
                    SQLJoinOperator joinNew = (SQLJoinOperator) it1.next();
                    SQLCondition joinNewCondition = joinNew.getJoinCondition();
                    SQLPredicate predicate = joinNewCondition.getRootPredicate();

                    VisibleSQLPredicateImpl newPredicate = new VisibleSQLPredicateImpl();
                    newPredicate.setOperatorType("and");
                    newPredicate.addInput(SQLPredicate.LEFT, previousPredicate);
                    newPredicate.addInput(SQLPredicate.RIGHT, predicate);
                    previousPredicate = newPredicate;
                }
                SQLObjectUtil.migrateJoinCondition(previousPredicate, joinCondition);
                //  Set condition state from unknown to Graphical.
                joinCondition.setGuiMode(SQLCondition.GUIMODE_GRAPHICAL);
                joinCondition.getRootPredicate();

                join.setJoinConditionType(SQLJoinOperator.SYSTEM_DEFINED_CONDITION);
            }
        }

    //addObjects(allObjectList, joinView);
    }

    private void addObjects(List joinList, SQLJoinView joinView) throws BaseException {
        Iterator it = joinList.iterator();
        while (it.hasNext()) {
            SQLObject join = (SQLObject) it.next();
            joinView.addObject(join);
        }
    }

    protected List<String> getUnmappedTargetTables() {
        ArrayList<String> unmappedtbls = new ArrayList<String>();
        Iterator i = srcselectionmap.keySet().iterator();
        while (i.hasNext()) {
            String trgtsearchkey = (String) i.next();
            List srcindicies = srcselectionmap.get(trgtsearchkey);
            if (srcindicies != null) {
                if (srcindicies.size() == 0) {
                    unmappedtbls.add(trgtsearchkey);
                }
            }
        }
        return unmappedtbls;
    }    //Msgs
    private String error1 = "Some Target tables are still not mapped to any Source. You may map them now or later in collaborations.";
    private String success1 = "All Target tables mapped to Source Tables";
}
