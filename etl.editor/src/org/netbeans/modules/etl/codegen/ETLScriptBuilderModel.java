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
package org.netbeans.modules.etl.codegen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.etl.codegen.impl.InternalDBMetadata;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.impl.ETLEngineImpl;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConstants;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DatabaseModel;

/**
 * @author Girish Patil
 * @version $Revision 1.0$
 */
public final class ETLScriptBuilderModel {

    // Folder under eTL working directory
    public static final String ETL_DESIGN_WORK_FOLDER = getDefaultWorkingFolder();
    // Pool name aka connection Definition name are for internal cross reference purposes.
    public static final String ETL_INSTANCE_DB_CONN_DEF_NAME = "IDB_CONN_DEF";
    public static final String ETL_INSTANCE_DB_FOLDER = "idb/";
    public static final String ETL_INSTANCE_DB_NAME = "InstanceDB";
    public static final String ETL_MONITOR_DB_CONN_DEF_NAME = "MDB_CONN_DEF";
    public static final String ETL_MONITOR_DB_FOLDER = "mdb/";
    public static final String ETL_MONITOR_DB_NAME = "MonitorDB";;
    private boolean connectionDefinitionOverridesApplied = false;
    private List<SQLDBConnectionDefinition> connectionDefinitions = null;
    private ETLEngine engine = null;
    private SQLDBConnectionDefinition instanceDb = null;
    private String instanceDBFolder = ETL_DESIGN_WORK_FOLDER + ETL_INSTANCE_DB_FOLDER;
    private String instanceDBName = ETL_INSTANCE_DB_NAME;
    private boolean memoryMonitorDB = true;
    private SQLDBConnectionDefinition monitorDb = null;
    private String monitorDBFolder = ETL_DESIGN_WORK_FOLDER + ETL_MONITOR_DB_FOLDER;
    private String monitorDBName = ETL_MONITOR_DB_NAME;
    private Map<String, String> nameToDatabaseMap = null;
    private Map oidToFFMetadata = new HashMap();
    private Map<String, SQLDBConnectionDefinition> databaseToConnectionDefintionMap = null;
    private Map<String, String> dbIdToNameMap = null;
    private boolean shutdownMonitorDB = false;
    private SQLDefinition sqlDefinition = null;
    private boolean useInstanceDB = false;
    private String workingFolder = ETL_DESIGN_WORK_FOLDER;

    private static final String getDefaultWorkingFolder() {
        String nbUsrDir = System.getProperty("netbeans.user");
        if ((nbUsrDir == null) || ("".equals(nbUsrDir))) {
            nbUsrDir = ".." + File.separator + "usrdir";
        }
        return nbUsrDir + File.separator + "eTL" + File.separator + "work/";
    }

    public void applyConnectionDefinitions() throws BaseException {
        // Get all connection definitions from SQLDefinition.
        connectionDefinitions = new ArrayList<SQLDBConnectionDefinition>();
        databaseToConnectionDefintionMap = new HashMap<String, SQLDBConnectionDefinition>();
        dbIdToNameMap = new HashMap<String, String>();
        nameToDatabaseMap = new HashMap<String, String>();

        SQLDBConnectionDefinition dbConnDef = null;
        String key = null;
        String qConnDefName = null;

        List dbModels = sqlDefinition.getAllDatabases();
        Iterator itr = dbModels.iterator();
        while (itr.hasNext()) {
            DatabaseModel dbModel = (DatabaseModel) itr.next();
            dbConnDef = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(dbModel.getConnectionDefinition());
            key = ETLCodegenUtil.getQualifiedObjectId((SQLDBModel) dbModel);

            if (key != null) {
                databaseToConnectionDefintionMap.put(key, dbConnDef);
            } else {
                key = dbConnDef.getName();
                databaseToConnectionDefintionMap.put(key, dbConnDef);
            }

            dbIdToNameMap.put(key, qConnDefName);
            nameToDatabaseMap.put(qConnDefName, key);
            connectionDefinitions.add(dbConnDef);
        }
        addMonitorAndInstanceConnectionDefinitions(connectionDefinitions);
        getEngine().setConnectionDefList(this.connectionDefinitions);
    }

    /**
     * ConnDefName to ConnDef. Note name is may not be same as in  Conn Def oid to
     * connDefName mapping.
     */
    public void applyConnectionDefinitions(Map name2connectionDefMap, Map<String, String> connDefNameMap, Map intDbConfigParams) throws BaseException {

        oidToFFMetadata = intDbConfigParams;

        nameToDatabaseMap = new HashMap<String, String>();
        connectionDefinitions = new ArrayList<SQLDBConnectionDefinition>();

        databaseToConnectionDefintionMap = new HashMap<String, SQLDBConnectionDefinition>();
        dbIdToNameMap = new HashMap<String, String>();

        nameToDatabaseMap = transposeMap(connDefNameMap);
        addMonitorAndInstanceConnectionDefinitions(connectionDefinitions);

        Iterator itr = name2connectionDefMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            String connDefName = (String) entry.getKey();
            SQLDBConnectionDefinition connDef = (SQLDBConnectionDefinition) entry.getValue();
            String qualifiedOid = nameToDatabaseMap.get(connDefName);

            if (SQLUtils.getSupportedDBType(connDef.getDBType()) == DBConstants.AXION) {
                //Fix for Axion Connection Definitions not being persisted in the engine file and causing a problem for 
                //Staging Strategy.
                connectionDefinitions.add(connDef);
                if (qualifiedOid != null) {
                    nameToDatabaseMap.remove(connDefName);
                    nameToDatabaseMap.put(ETL_INSTANCE_DB_CONN_DEF_NAME, qualifiedOid);
                    connDefNameMap.remove(qualifiedOid);
                    connDefNameMap.put(qualifiedOid, ETL_INSTANCE_DB_CONN_DEF_NAME);
                    databaseToConnectionDefintionMap.put(qualifiedOid, instanceDb);
                }
                itr.remove();
            } else {
                connectionDefinitions.add(connDef);
                databaseToConnectionDefintionMap.put(qualifiedOid, connDef);
            }
        }

        setOidToFFMetadataMap(intDbConfigParams);

        getEngine().setConnectionDefList(this.connectionDefinitions);
        this.connectionDefinitionOverridesApplied = true;
    }

    public void buildRuntimeDatabaseModel() {
        // Create runtime attributes from runtime input, output tables (if they exist).
        RuntimeDatabaseModel runtimeModel = this.sqlDefinition.getRuntimeDbModel();
        if (runtimeModel != null) {
            RuntimeInput inputTable = runtimeModel.getRuntimeInput();
            if (inputTable != null) {
                getEngine().setInputAttrMap(inputTable.getRuntimeAttributeMap());
            }

            RuntimeOutput outputTable = runtimeModel.getRuntimeOutput();
            if (outputTable != null) {
                getEngine().setOutputAttrMap(outputTable.getRuntimeAttributeMap());
            }
        }
    }

    public DBConnectionDefinition getConnectionDefinition(DBTable table) throws BaseException {
        return table.getParent().getConnectionDefinition();
    }

    public DBConnectionDefinition getConnectionDefinition(String name) throws BaseException {
        return (DBConnectionDefinition) databaseToConnectionDefintionMap.get(name);
    }

    public List getConnectionDefinitions() {
        return this.connectionDefinitions;
    }

    public ETLEngine getEngine() {
        if (engine == null) {
            engine = new ETLEngineImpl();
        }
        return engine;
    }

    public String getInstanceDBFolder() {
        return this.instanceDBFolder;
    }

    public String getInstanceDBName() {
        return this.instanceDBName;
    }

    public InternalDBMetadata getInternalMetadata(DBTable tTable) throws BaseException {
        SQLDBModel element = (SQLDBModel) tTable.getParent();
        if (element.getAttribute("refKey") == null) {
            return null;
        }
        String oid = (String) element.getAttribute("refKey").getAttributeValue();
        return (InternalDBMetadata) this.oidToFFMetadata.get(oid);
    }

    public String getMonitorDBFolder() {
        return this.monitorDBFolder;
    }

    public String getMonitorDBName() {
        return this.monitorDBName;
    }

    public SQLDefinition getSqlDefinition() {
        return this.sqlDefinition;
    }

    public String getWorkingFolder() {
        return this.workingFolder;
    }

    public boolean isConnectionDefinitionOverridesApplied() {
        return this.connectionDefinitionOverridesApplied;
    }

    public boolean isMemoryMonitorDB() {
        return this.memoryMonitorDB;
    }

    public boolean isShutdownMonitorDB() {
        return this.shutdownMonitorDB;
    }

    public boolean isUseInstanceDB() {
        return this.useInstanceDB;
    }

    public void setConnectionDefinitionOverridesApplied(boolean connectionDefinitionOverridesApplied) {
        this.connectionDefinitionOverridesApplied = connectionDefinitionOverridesApplied;
    }

    public void setConnectionDefinitions(List<SQLDBConnectionDefinition> connectionDefinitions) {
        this.connectionDefinitions = connectionDefinitions;
    }

    public void setEngine(ETLEngine engine) {
        this.engine = engine;
    }

    public void setInstanceDBFolder(String instanceDBFolder) {
        this.instanceDBFolder = instanceDBFolder;
    }

    public void setInstanceDBName(String instanceDBName) {
        this.instanceDBName = instanceDBName;
    }

    public void setMemoryMonitorDB(boolean memoryMonitorDB) {
        this.memoryMonitorDB = memoryMonitorDB;
    }

    public void setMonitorDBFolder(String monitiorDBFolder) {
        this.monitorDBFolder = monitiorDBFolder;
    }

    public void setMonitorDBName(String monitorDBName) {
        this.monitorDBName = monitorDBName;
    }

    public void setOidToFFMetadataMap(Map dbToMetadata) {
        //this.oidToFFMetadata.clear();
        //this.oidToFFMetadata.putAll(dbToMetadata);
    }

    public void setShutdownMonitorDB(boolean shutdownMonitorDB) {
        this.shutdownMonitorDB = shutdownMonitorDB;
    }

    public void setSqlDefinition(SQLDefinition sqlDefinition) {
        this.sqlDefinition = sqlDefinition;
    }

    public void setUseInstanceDB(boolean useInstanceDB) {
        this.useInstanceDB = useInstanceDB;
    }

    public void setWorkingFolder(String workingFolder) {
        this.workingFolder = workingFolder;
    }

    protected InternalDBMetadata getInternalMetadataFor(SQLDBTable table) throws BaseException {
        return (InternalDBMetadata) getOidToFFMetadataMap().get(table.getName());
    }

    private void addMonitorAndInstanceConnectionDefinitions(List<SQLDBConnectionDefinition> connDefs) throws BaseException {
        DBConnectionDefinitionTemplate connTemplate = new DBConnectionDefinitionTemplate();
        Map<String, String> args = new HashMap<String, String>(1);

        if (this.memoryMonitorDB) {
            monitorDb = connTemplate.getDBConnectionDefinition("AXIONMEMORYDB");
        } else {
            monitorDb = connTemplate.getDBConnectionDefinition("STCDBADAPTER");
        }

        // KEY_METADATA_DIR entry is ignored for memoryDatabase
        args.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, monitorDBName);
        args.put(DBConnectionDefinitionTemplate.KEY_METADATA_DIR, monitorDBFolder);
        monitorDb.setConnectionURL(StringUtil.replace(monitorDb.getConnectionURL(), args));
        monitorDb.setName(ETL_MONITOR_DB_CONN_DEF_NAME);
        this.databaseToConnectionDefintionMap.put(ETL_MONITOR_DB_CONN_DEF_NAME, monitorDb);
        connDefs.add(monitorDb);

        if (this.useInstanceDB) {
            args.clear();
            args.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, instanceDBName);
            args.put(DBConnectionDefinitionTemplate.KEY_METADATA_DIR, instanceDBFolder);

            instanceDb = connTemplate.getDBConnectionDefinition("STCDBADAPTER");
            instanceDb.setConnectionURL(StringUtil.replace(instanceDb.getConnectionURL(), args));
            instanceDb.setName(ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
            this.databaseToConnectionDefintionMap.put(ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME, instanceDb);
            connDefs.add(instanceDb);
        }
    }

    private Map getOidToFFMetadataMap() {
        return this.oidToFFMetadata;
    }

    private Map<String, String> transposeMap(Map origMap) {
        Map<String, String> trans = new HashMap<String, String>();

        if (origMap != null) {
            Iterator itr = origMap.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) itr.next();
                trans.put((String) mapEntry.getValue(), (String) mapEntry.getKey());
            }
        }
        return trans;
    }
}