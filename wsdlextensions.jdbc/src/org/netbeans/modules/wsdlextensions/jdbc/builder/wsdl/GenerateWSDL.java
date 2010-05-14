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
package org.netbeans.modules.wsdlextensions.jdbc.builder.wsdl;

import java.io.File;

import org.netbeans.modules.wsdlextensions.jdbc.builder.Procedure;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBTable;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBConnectionDefinition;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

public class GenerateWSDL {

    private String mSrcDirectoryLocation;
    private String mBuildDirectoryLocation;
    private String mWSDLFileName;
    private String mWSDLTargetNamespace;
    private DBTable mTable;
    private String mDBType;
    private String mJNDIName;
    private DBConnectionDefinition dbinfo;
    private Procedure mProcedure;
    private String sqlText;
    private String paramOrder;
    private String insertSelected;
    private String updateSelected;
    private String deleteSelected;
    private String findSelected;
    private String pollSelected;
    private String updateWhere;
    private String deleteWhere;
    private String findWhere;
    private String pollWhere;
    public GenerateWSDL() {
    }

    public String getSrcDirectoryLocation() {
        return this.mSrcDirectoryLocation;
    }

    public void setSrcDirectoryLocation(final String srcDirectoryLocation) {
        this.mSrcDirectoryLocation = srcDirectoryLocation;
    }

    public String getBuildDirectoryLocation() {
        return this.mBuildDirectoryLocation;
    }

    public void setBuildDirectoryLocation(final String buildDirectoryLocation) {
        this.mBuildDirectoryLocation = buildDirectoryLocation;
    }

    public String getWSDLFileName() {
        return this.mWSDLFileName;
    }

    public String getWSDLTargetNamespace() {
        return this.mWSDLTargetNamespace;
    }

    public void setStoredProcedure(Procedure procedure) {
        this.mProcedure = procedure;
    }

    public void setParamOrder(String paramOrder) {
        if(paramOrder != null){
            this.paramOrder = paramOrder;
        }
    }

    public void setWSDLFileName(final String wsdlFileName) {
        this.mWSDLFileName = wsdlFileName;
    }

    public void setWSDLTargetNamespace(final String wsdlTargetNamespace) {
        this.mWSDLTargetNamespace = wsdlTargetNamespace;
    }

    public void setDBTable(final DBTable table) {
        this.mTable = table;
    }

    public void setDBType(final String dbtype) {
        this.mDBType = dbtype;
    }

    public void setSchemaName(final String schemaName) {
    }

    public void setJNDIName(final String jndiName) {
        this.mJNDIName = jndiName;
    }

    public void setDBInfo(DBConnectionDefinition dbinfo) {
        this.dbinfo = dbinfo;
    }

    public void setSql(String sql) {
        this.sqlText = sql;
    }
    
    public void setOperations(String insertSelected, String updateSelected, String deleteSelected, String findSelected, String pollSelected){
    	this.insertSelected = insertSelected;
    	this.updateSelected = updateSelected;
    	this.deleteSelected = deleteSelected;
    	this.findSelected = findSelected;
    	this.pollSelected = pollSelected;
    }
    
    public void setWhereClause(String updateWhere, String deleteWhere, String findWhere, String pollWhere){
    	this.updateWhere = updateWhere;
    	this.deleteWhere = deleteWhere;
    	this.findWhere = findWhere;
    	this.pollWhere = pollWhere;
    }

    public void execute() {
        execute(null);
    }
    

    public WSDLModel execute(WSDLComponent wsdlComponent) {
        WSDLGenerator wsdlgen = null;
        WSDLModel wsdlModel = null;
        final File srcDir = new File(this.mSrcDirectoryLocation);
        if (!srcDir.exists()) {
            throw new IllegalArgumentException("Directory " + this.mSrcDirectoryLocation + " does not exist.");
        }
        final String srcDirPath = srcDir.getAbsolutePath();
        if (this.mTable != null) {
            wsdlgen = new WSDLGenerator(this.mTable, this.mWSDLFileName, srcDirPath, this.mDBType, this.mJNDIName, this.mWSDLTargetNamespace);
            wsdlgen.setOperations(insertSelected, updateSelected, deleteSelected, findSelected, pollSelected);
            wsdlgen.setWhereClause(updateWhere, deleteWhere, findWhere, pollWhere);
        } else if (this.mProcedure != null) {
            wsdlgen = new WSDLGenerator(this.mProcedure, this.mWSDLFileName, srcDirPath, this.mDBType, this.mJNDIName, this.mWSDLTargetNamespace);
        }
        if (wsdlgen != null) {
            wsdlgen.setTopEleName();
            wsdlgen.setXSDName();
            wsdlgen.setDBInfo(this.dbinfo);
            wsdlModel = wsdlgen.generateWSDL(wsdlComponent);
        }
        return wsdlModel;
    }

    public void executePrepStmt() {
        executePrepStmt(null);
    }

    public WSDLModel executePrepStmt(WSDLComponent wsdlComponent) {
        WSDLModel wsdlModel = null;
        final File srcDir = new File(this.mSrcDirectoryLocation);
        if (!srcDir.exists()) {
            throw new IllegalArgumentException("Directory " + this.mSrcDirectoryLocation + " does not exist.");
        }
        final String srcDirPath = srcDir.getAbsolutePath();
        final PrepStmtWSDLGenerator wsdlgen = new PrepStmtWSDLGenerator(this.mWSDLFileName, srcDirPath, this.mDBType, this.mJNDIName, this.mWSDLTargetNamespace);
        wsdlgen.setDBInfo(this.dbinfo);
        wsdlgen.setSql(this.sqlText);
		wsdlgen.setParamOrder(this.paramOrder);
        wsdlgen.parseSQLStatement();
        wsdlModel = wsdlgen.generatePrepStmtWSDL(wsdlComponent);
        return wsdlModel;
    }

    public void executeStoredProc() {
        executeStoredProc(null);
    }     
    
    public WSDLModel executeStoredProc(WSDLComponent wsdlComponent) {
        WSDLModel wsdlModel = null;
        final File srcDir = new File(this.mSrcDirectoryLocation);
        if (!srcDir.exists()) {
            throw new IllegalArgumentException("Directory " + this.mSrcDirectoryLocation + " does not exist.");
        }
        final String srcDirPath = srcDir.getAbsolutePath();
        final StoredProcWSDLGenerator wsdlgen = new StoredProcWSDLGenerator(this.mWSDLFileName, srcDirPath, this.mDBType, this.mJNDIName, this.mWSDLTargetNamespace);
        wsdlgen.setDBInfo(this.dbinfo);
        wsdlgen.setStoredProcedure(this.mProcedure);
        wsdlModel = wsdlgen.generateProcWSDL(wsdlComponent);
        return wsdlModel;
    }       
}
