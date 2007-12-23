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
package org.netbeans.modules.jdbcwizard.builder.wsdl;

import java.io.File;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBConnectionDefinition;

public class GenerateWSDL {
    private String mSrcDirectoryLocation;
    private String mBuildDirectoryLocation;
    private String mWSDLFileName;
    private DBTable mTable;
    private String mDBType;
    private String mJNDIName;
    private DBConnectionDefinition dbinfo;

    public GenerateWSDL() {}

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

    public void setWSDLFileName(final String wsdlFileName) {
        this.mWSDLFileName = wsdlFileName;
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
    
    public void setDBInfo(DBConnectionDefinition dbinfo){
        this.dbinfo = dbinfo;
    }

    public void execute() {
        final File srcDir = new File(this.mSrcDirectoryLocation);
        if (!srcDir.exists()) {
            throw new IllegalArgumentException("Directory " + this.mSrcDirectoryLocation + " does not exist.");
        }
        final String srcDirPath = srcDir.getAbsolutePath();
        final WSDLGenerator wsdlgen = new WSDLGenerator(this.mTable, this.mWSDLFileName, srcDirPath, this.mDBType, this.mJNDIName);
        wsdlgen.setTopEleName();
        wsdlgen.setXSDName();
        wsdlgen.setDBInfo(this.dbinfo);
        wsdlgen.generateWSDL();
    }
}
