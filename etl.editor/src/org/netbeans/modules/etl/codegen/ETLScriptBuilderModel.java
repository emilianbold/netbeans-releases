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

import org.netbeans.modules.etl.codegen.DBConnectionDefinitionTemplate;
import org.netbeans.modules.etl.codegen.impl.InternalDBMetadata;
import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.model.database.DatabaseModel;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.view.DataOutputPanel;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.impl.ETLEngineImpl;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConstants;
import com.sun.sql.framework.utils.StringUtil;

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
    public static final String ETL_MONITOR_DB_NAME = DataOutputPanel.ETL_MONITOR_DB_NAME;
    private boolean connectionDefinitionOverridesApplied = false;

    private List connectionDefinitions = null;
    private ETLEngine engine = null;
    private SQLDBConnectionDefinition instanceDb = null;

    private String instanceDBFolder = ETL_DESIGN_WORK_FOLDER + ETL_INSTANCE_DB_FOLDER;
    private String instanceDBName = ETL_INSTANCE_DB_NAME;

    private boolean memoryMonitorDB = true;
    private SQLDBConnectionDefinition monitorDb = null;
    private String monitorDBFolder = ETL_DESIGN_WORK_FOLDER + ETL_MONITOR_DB_FOLDER;
    private String monitorDBName = ETL_MONITOR_DB_NAME;

    private Map nameToOtdOidMap = null;
    private Map oidToFFMetadata = new HashMap();
    private Map otdOidToConnectionDefintionMap = null;
    private Map otdOidToNameMap = null;
    private boolean shutdownMonitorDB = false;
    private SQLDefinition sqlDefinition = null;
    private boolean useInstanceDB = false;
    private String workingFolder = ETL_DESIGN_WORK_FOLDER;

    private static final String getDefaultWorkingFolder() {
        String nbUsrDir = System.getProperty("netbeans.user");
        if ((nbUsrDir == null) || ("".equals(nbUsrDir))){
            nbUsrDir = ".." + File.separator + "usrdir" ;
        }
        return nbUsrDir + File.separator + "eTL"+ File.separator + "work/" ;
    }

    public void applyConnectionDefinitions() throws BaseException {
        // Get all connection definitions from SQLDefinition.
        connectionDefinitions = new ArrayList();
        otdOidToConnectionDefintionMap = new HashMap();
        otdOidToNameMap = new HashMap();
        nameToOtdOidMap = new HashMap();

        SQLDBConnectionDefinition dbConnDef = null;
        String key = null;
        String qConnDefName = null;        

        List dbModels = sqlDefinition.getAllOTDs();
        Iterator itr = dbModels.iterator();
        while (itr.hasNext()) {
            DatabaseModel dbModel = (DatabaseModel) itr.next();
            dbConnDef = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(dbModel.getConnectionDefinition());
            key = ETLCodegenUtil.getQualifiedOtdOid((SQLDBModel) dbModel);
            //RIT since there is no otd id in alaska just pass the connection for table.
//            qConnDefName = ETLCodegenUtil.getQualifiedConnectionDefinitionName((SQLDBModel) dbModel, dbConnDef.getName());
//            dbConnDef.setName(qConnDefName);
            
            if (key != null) {
                otdOidToConnectionDefintionMap.put(key, dbConnDef);
            } else {
                key = dbConnDef.getName();
                otdOidToConnectionDefintionMap.put(key, dbConnDef);
            }
            
            otdOidToNameMap.put(key, qConnDefName);
            nameToOtdOidMap.put(qConnDefName, key);
            connectionDefinitions.add(dbConnDef);
        }
//RIT commented monitor related code for now
        addMonitorAndInstanceConnectionDefinitions(connectionDefinitions);
        getEngine().setConnectionDefList(this.connectionDefinitions);
    }

    /**
     * ConnDefName to ConnDef. Note name is may not be same as in OTD Conn Def OTD oid to
     * connDefName mapping.
     */
    public void applyConnectionDefinitions(Map name2connectionDefMap, Map otdOid2ConnDefNameMap, Map intDbConfigParams) throws BaseException {
        
    	oidToFFMetadata = intDbConfigParams;
    	
    	nameToOtdOidMap = new HashMap();
        connectionDefinitions = new ArrayList();

        otdOidToConnectionDefintionMap = new HashMap();
        otdOidToNameMap = new HashMap();

        nameToOtdOidMap = transposeMap(otdOid2ConnDefNameMap);
//RIT commented monitor related code for now
        addMonitorAndInstanceConnectionDefinitions(connectionDefinitions);

        Iterator itr = name2connectionDefMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            String connDefName = (String) entry.getKey();
            DBConnectionDefinition connDef = (DBConnectionDefinition) entry.getValue();
            Object qualifiedOid = nameToOtdOidMap.get(connDefName);

            if (SQLUtils.getSupportedDBType(connDef.getDBType()) == DBConstants.AXION) {
                if (qualifiedOid != null) {
                    nameToOtdOidMap.remove(connDefName);
                    nameToOtdOidMap.put(ETL_INSTANCE_DB_CONN_DEF_NAME, qualifiedOid);
                    otdOid2ConnDefNameMap.remove(qualifiedOid);
                    otdOid2ConnDefNameMap.put(qualifiedOid, ETL_INSTANCE_DB_CONN_DEF_NAME);
                    otdOidToConnectionDefintionMap.put(qualifiedOid, instanceDb);
                }
                itr.remove();
            } else {
                connectionDefinitions.add(connDef);
                otdOidToConnectionDefintionMap.put(qualifiedOid, connDef);
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

    public String getConnectionDefinationName(TargetTable tTable) throws BaseException {
    //RIT since there is no otd id in alaska just pass the connection for table.
    	SQLDBModel element = (SQLDBModel) tTable.getParent();
        if(element.getConnectionDefinition().getDriverClass().equals("org.axiondb.jdbc.AxionDriver"))
        	return ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME;
        else
        return null;
        
    }

    public DBConnectionDefinition getConnectionDefinition(DBTable table) throws BaseException {
        DatabaseModel dbModel = table.getParent();
        DBConnectionDefinition conDef = dbModel.getConnectionDefinition();
        //RIT since there is no otd id in alaska just pass the connection for table.
        
//        String key = ETLCodegenUtil.getQualifiedOtdOid((SQLDBModel) dbModel);
//
//        if (this.otdOidToConnectionDefintionMap != null && !otdOidToConnectionDefintionMap.isEmpty()) {
//            SQLDBConnectionDefinition poolConDef = (SQLDBConnectionDefinition) this.otdOidToConnectionDefintionMap.get(key);
//            // TODO Check for unresolved tables at codegen validation time - this check
//            // and exception should not be necessary if it's caught during validation
//            if (poolConDef == null) {
//                throw new BaseException("Connection definition not found for table " + table
//                    + "; its OTD may not be linked or configured in Connectivity Map.");
//            }
//
//            // Create a local instance with OTD path populated for use in codegen.
//            poolConDef = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(poolConDef);
//            if (conDef instanceof SQLDBConnectionDefinition) {
//                poolConDef.setOTDPathName(((SQLDBConnectionDefinition) conDef).getOTDPathName());
//            }
//            conDef = poolConDef;
//        }
        return conDef;
    }

    public DBConnectionDefinition getConnectionDefinition(String name) throws BaseException {
        return (DBConnectionDefinition) otdOidToConnectionDefintionMap.get(name);
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
        if (tTable instanceof SourceTable) {
            //return (InternalDBMetadata) this.oidToFFMetadata.get(ETLCodegenUtil.resolveSourcePortName(tTable));
        } else {
            //return (InternalDBMetadata) this.oidToFFMetadata.get(ETLCodegenUtil.resolveTargetPortName(tTable));
        }
       
        SQLDBModel element = (SQLDBModel) tTable.getParent();
        if(element.getAttribute("refKey") == null)
        	return null;
		String oid = (String) element.getAttribute("refKey").getAttributeValue();
        return (InternalDBMetadata)this.oidToFFMetadata.get(oid);
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

    public void setConnectionDefinitions(List connectionDefinitions) {
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

    public void setOidToFFMetadataMap(Map otdOidToMetadata) {
        //this.oidToFFMetadata.clear();
        //this.oidToFFMetadata.putAll(otdOidToMetadata);
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
        //InternalDBMetadata dbm = null;

        if (table != null) {
            if (table instanceof TargetTable) {
                //return (InternalDBMetadata) getOidToFFMetadataMap().get(ETLCodegenUtil.resolveTargetPortName(table));
            } else {
                //return (InternalDBMetadata) getOidToFFMetadataMap().get(ETLCodegenUtil.resolveSourcePortName(table));
            }
        }

        return (InternalDBMetadata) getOidToFFMetadataMap().get(table.getName());
    }

    private void addMonitorAndInstanceConnectionDefinitions(List connDefs) throws BaseException {
        DBConnectionDefinitionTemplate connTemplate = new DBConnectionDefinitionTemplate();
        HashMap args = new HashMap(1);

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
        this.otdOidToConnectionDefintionMap.put(ETL_MONITOR_DB_CONN_DEF_NAME, monitorDb);
        connDefs.add(monitorDb);

        if (this.useInstanceDB) {
            args.clear();
            args.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, instanceDBName);
            args.put(DBConnectionDefinitionTemplate.KEY_METADATA_DIR, instanceDBFolder);

            instanceDb = connTemplate.getDBConnectionDefinition("STCDBADAPTER");
            instanceDb.setConnectionURL(StringUtil.replace(instanceDb.getConnectionURL(), args));
            instanceDb.setName(ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
            this.otdOidToConnectionDefintionMap.put(ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME, instanceDb);
            connDefs.add(instanceDb);
        }
    }

    private Map getOidToFFMetadataMap() {
        return this.oidToFFMetadata;
    }

    private Map transposeMap(Map origMap) {
        Map trans = new HashMap();

        if (origMap != null) {
            Iterator itr = origMap.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) itr.next();
                trans.put(mapEntry.getValue(), mapEntry.getKey());
            }
        }
        return trans;
    }
}
