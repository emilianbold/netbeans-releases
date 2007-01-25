/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jdbcwizard.builder.wsdl;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
// <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.File;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;

/**
 * @author Administrator
 */
public class GenerateWSDL extends Task {

    private String mSrcDirectoryLocation;

    private String mBuildDirectoryLocation;

    private String mWSDLFileName;

    private DBTable mTable;

    private String mDBType;

    private String mJNDIName;

    public GenerateWSDL() {

    }

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getSrcDirectoryLocation() {
        return this.mSrcDirectoryLocation;
    }

    /**
     * @param srcDirectoryLocation The srcDirectoryLocation to set.
     */
    public void setSrcDirectoryLocation(final String srcDirectoryLocation) {
        this.mSrcDirectoryLocation = srcDirectoryLocation;
    }

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getBuildDirectoryLocation() {
        return this.mBuildDirectoryLocation;
    }

    /**
     * @param buildDirectoryLocation The buildDirectoryLocation to set.
     */
    public void setBuildDirectoryLocation(final String buildDirectoryLocation) {
        this.mBuildDirectoryLocation = buildDirectoryLocation;
    }

    /**
     * get wsdl file name
     * 
     * @return
     */
    public String getWSDLFileName() {
        return this.mWSDLFileName;
    }

    /**
     * set wsdl file name
     * 
     * @param wsdlFileName
     */
    public void setWSDLFileName(final String wsdlFileName) {
        this.mWSDLFileName = wsdlFileName;
    }

    /**
     * @param table
     */
    public void setDBTable(final DBTable table) {
        this.mTable = table;
    }

    /**
     * @param dbtype
     */
    public void setDBType(final String dbtype) {
        this.mDBType = dbtype;
    }

    /**
     * @param schemaName
     */
    public void setSchemaName(final String schemaName) {
    }

    /**
     * @param jndiName
     */
    public void setJNDIName(final String jndiName) {
        this.mJNDIName = jndiName;
    }

    /**
     * generate wsdl
     * 
     * @throws BuildException
     */
    public void execute() throws BuildException {
        final File srcDir = new File(this.mSrcDirectoryLocation);
        if (!srcDir.exists()) {
            throw new BuildException("Directory " + this.mSrcDirectoryLocation + " does not exit.");
        }
        try {
            final String srcDirPath = srcDir.getAbsolutePath();
            // pass dbtable object
            // get the xsd file name and xsd top element name
            final WSDLGenerator wsdlgen = new WSDLGenerator(this.mTable, this.mWSDLFileName, srcDirPath, this.mDBType, this.mJNDIName);
            wsdlgen.setTopEleName();
            wsdlgen.setXSDName();
            wsdlgen.generateWSDL();
        } catch (final Exception e) {
            throw new BuildException(e.getMessage());
        }
    }

    public static void main(final String[] args) {
        // GenerateWSDL tsk = new GenerateWSDL();
        // tsk.setSrcDirectoryLocation("c:/temp");
        // tsk.execute();
    }
}