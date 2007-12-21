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

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.dbmodel;


/**
 * Class to hold prepared statement metadata.
 *
 * @author Susan Chen
 * @version 
 */
public class PrepStmt {
    private String name = "";                   // name of prepared statement
    private String javaName = "";               // java name of prepared statement
    private String catalog = "";                // catalog
    private String schema = "";                 // schema
    private String sqlText = "";                // SQL text
    private int numParameters = 0;              // number of parameters
    private Parameter[] parameters;             // array of parameters
    private int numResultSetColumns = 0;        // number of resultset columns
    private ResultSetColumn[] resultsetColumns; // array of resultset columns

    /** 
     * Creates a new instance of PrepStmt.
     */
    public PrepStmt() {
        name = "";
        catalog = "";
        schema = "";
        sqlText = "";
        numParameters = 0;
        parameters = null;
        numResultSetColumns = 0;
        resultsetColumns = null;
    }

    /**
     * Creates a new instance of PrepStmt with the given name.
     * @param pname Prepared statement name.
     */
    public PrepStmt(String pname) {
        name = pname;
        catalog = "";
        schema = "";
        sqlText = "";
        numParameters = 0;
        parameters = null;
        numResultSetColumns = 0;
        resultsetColumns = null;
    }
        
    /**
     * Creates a new instance of PrepStmt with the given attributes.
     *
     * @param pname Prepared statement name
     * @param pcatalog Catalog name
     * @param pschema Schema name
     * @param psqltext Prepared statement SQL text
     */
    public PrepStmt(String pname, String pcatalog, String pschema, String psqltext) {
        name = pname;
        javaName = "";
        catalog = pcatalog;
        schema = pschema;
        sqlText = psqltext;
        numParameters = 0;
        parameters = null;
        numResultSetColumns = 0;
        resultsetColumns = null;
    }
    
    /**
     * Creates a new instance of PrepStmt with the given attributes.
     *
     * @param pname Prepared statement name
     * @param pname Prepared statement java name
     * @param pcatalog Catalog name
     * @param pschema Schema name
     * @param psqltext Prepared statement SQL text
     */
    public PrepStmt(String pname, String jname, String pcatalog, String pschema, String psqltext) {
        name = pname;
        javaName = jname;
        catalog = pcatalog;
        schema = pschema;
        sqlText = psqltext;
        numParameters = 0;
        parameters = null;
        numResultSetColumns = 0;
        resultsetColumns = null;
    }
    
    public PrepStmt(PrepStmt p) {
        name = p.getName();
        javaName = p.getJavaName();
        catalog = p.getCatalog();
        schema = p.getSchema();
        sqlText = p.getSQLText();
        cloneParameters(p.getParameters());
        cloneResultSetColumns(p.getResultSetColumns());
    }

    /**
     * Get the prepared statement name.
     *
     * @return Prepared statement name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the prepared statement java name.
     *
     * @return Prepared statement java name
     */
    public String getJavaName() {
        return javaName;
    }

    /**
     * Get the catalog name.
     *
     * @return Catalog name
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Get the schema name.
     *
     * @return Schema name
     */
    public String getSchema() {
        return schema;
    }
     
     /**
     * Get the Prepared statement SQL text.
     *
     * @return Prepared statement SQL text.
     */   
    public String getSQLText() {
        return sqlText;
    }
    
    /**
     * Get the number of parameters in the prepared statement.
     *
     * @return Number of parameters
     */
    public int getNumParameters() {
        return numParameters;
    }
    
    /**
     * Get the Prepared Statement parameter list.
     *
     * @return Parameter list
     */
    public Parameter[] getParameters() {
        return parameters;
    }
    
    /** 
     * Get the number of resultset columns.
     *
     * @return Number of resultset columns
     */
    public int getNumResultSetColumns() {
        return numResultSetColumns;
    }
    
    /**
     * Get the Prepared Statement resultset columns list.
     *
     * @return ResultSet column list
     */
    public ResultSetColumn[] getResultSetColumns() {
        return resultsetColumns;
    }
    
    /**
     * Set the prepared statement name.
     *
     * @param newName Prepared statement name
     */
    public void setName(String newName) {
        name = newName;
    }
       
    /**
     * Set the prepared statement java name.
     *
     * @param newJavaName Prepared statement java name
     */
    public void setJavaName(String newJavaName) {
        javaName = newJavaName;
    }
       
    /**
     * Set the catalog name.
     *
     * @param newCatalog Catalog name
     */
    public void setCatalog(String newCatalog) {
        catalog = newCatalog;
    }
    
    /**
     * Set the schema name.
     *
     * @param newSchema Schema name
     */
    public void setSchema(String newSchema) {
        schema = newSchema;
    }

    /**
     * Set the SQL text.
     *
     * @param newSQLText SQL text
     */
    public void setSQLText(String newSQLText) {
        sqlText = newSQLText;
    }
        
    /**
     * Set the prepared statement parameter list.
     *
     * @param newParameters Parameter list
     */
    public void setParameters(Parameter[] newParameters) {
        parameters = newParameters;
        
        // update the number of parameters
        if (parameters != null) { 
            numParameters = parameters.length;
        }
    }
    
    public void cloneParameters(Parameter[] newParameters) {
        if (newParameters != null) {
            numParameters = newParameters.length;
            if (numParameters > 0) {
                parameters = new Parameter[numParameters];
                for (int i = 0; i < numParameters; i++) {
                    parameters[i] = new Parameter(newParameters[i]);
                }
            }
        }
    }

    /**
     * Set the prepared statement resultset column list.
     *
     * @param newResultSetColumns Resultset column list
     */
    public void setResultSetColumns (ResultSetColumn[] newResultSetColumns) {
        resultsetColumns = newResultSetColumns;
        
        // update the number of resultset columns
        if (resultsetColumns != null) {
            numResultSetColumns = resultsetColumns.length;
        }
    }   

    public void cloneResultSetColumns(ResultSetColumn[] newResultSetColumns) {
        if (newResultSetColumns != null) {
            numResultSetColumns = newResultSetColumns.length;
            if (numResultSetColumns > 0) {
                resultsetColumns = new ResultSetColumn[numResultSetColumns];
                for (int i = 0; i < numResultSetColumns; i++) {
                    resultsetColumns[i] = new ResultSetColumn(newResultSetColumns[i]);
                }
            }
        }
    }
}
