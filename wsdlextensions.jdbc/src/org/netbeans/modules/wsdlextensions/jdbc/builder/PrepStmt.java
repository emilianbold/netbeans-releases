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
package org.netbeans.modules.wsdlextensions.jdbc.builder;

/**
 * Class to hold prepared statement metadata.
 * 
 * @author
 */
public class PrepStmt {
    private String name = ""; // name of prepared statement

    private String javaName = ""; // java name of prepared statement

    private String catalog = ""; // catalog

    private String schema = ""; // schema

    private String sqlText = ""; // SQL text

    private int numParameters = 0; // number of parameters

    private Parameter[] parameters; // array of parameters

    private int numResultSetColumns = 0; // number of resultset columns

    private ResultSetColumn[] resultsetColumns; // array of resultset columns

    private String paramOrder = null;

    /**
     * Creates a new instance of PrepStmt.
     */
    public PrepStmt() {
        this.name = "";
        this.catalog = "";
        this.schema = "";
        this.sqlText = "";
        this.numParameters = 0;
        this.parameters = null;
        this.numResultSetColumns = 0;
        this.resultsetColumns = null;
    }

    /**
     * Creates a new instance of PrepStmt with the given name.
     * 
     * @param pname Prepared statement name.
     */
    public PrepStmt(final String pname) {
        this.name = pname;
        this.catalog = "";
        this.schema = "";
        this.sqlText = "";
        this.numParameters = 0;
        this.parameters = null;
        this.numResultSetColumns = 0;
        this.resultsetColumns = null;
    }

    /**
     * Creates a new instance of PrepStmt with the given attributes.
     * 
     * @param pname Prepared statement name
     * @param pcatalog Catalog name
     * @param pschema Schema name
     * @param psqltext Prepared statement SQL text
     */
    public PrepStmt(final String pname, final String pcatalog, final String pschema, final String psqltext) {
        this.name = pname;
        this.javaName = "";
        this.catalog = pcatalog;
        this.schema = pschema;
        this.sqlText = psqltext;
        this.numParameters = 0;
        this.parameters = null;
        this.numResultSetColumns = 0;
        this.resultsetColumns = null;
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
    public PrepStmt(final String pname, final String jname, final String pcatalog, final String pschema, final String psqltext) {
        this.name = pname;
        this.javaName = jname;
        this.catalog = pcatalog;
        this.schema = pschema;
        this.sqlText = psqltext;
        this.numParameters = 0;
        this.parameters = null;
        this.numResultSetColumns = 0;
        this.resultsetColumns = null;
    }

    public PrepStmt(final PrepStmt p) {
        this.name = p.getName();
        this.javaName = p.getJavaName();
        this.catalog = p.getCatalog();
        this.schema = p.getSchema();
        this.sqlText = p.getSQLText();
        this.cloneParameters(p.getParameters());
        this.cloneResultSetColumns(p.getResultSetColumns());
    }

    /**
     * Get the prepared statement name.
     * 
     * @return Prepared statement name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the prepared statement java name.
     * 
     * @return Prepared statement java name
     */
    public String getJavaName() {
        return this.javaName;
    }

    /**
     * Get the catalog name.
     * 
     * @return Catalog name
     */
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * Get the schema name.
     * 
     * @return Schema name
     */
    public String getSchema() {
        return this.schema;
    }

    /**
     * Get the Prepared statement SQL text.
     * 
     * @return Prepared statement SQL text.
     */
    public String getSQLText() {
        return this.sqlText;
    }

    /**
     * Get the number of parameters in the prepared statement.
     * 
     * @return Number of parameters
     */
    public int getNumParameters() {
        return this.numParameters;
    }

    /**
     * Get the Prepared Statement parameter list.
     * 
     * @return Parameter list
     */
    public Parameter[] getParameters() {
        return this.parameters;
    }

    /**
     * Get the number of resultset columns.
     * 
     * @return Number of resultset columns
     */
    public int getNumResultSetColumns() {
        return this.numResultSetColumns;
    }

    /**
     * Get the Prepared Statement resultset columns list.
     * 
     * @return ResultSet column list
     */
    public ResultSetColumn[] getResultSetColumns() {
        return this.resultsetColumns;
    }

    /**
     * Get the parameter order of the columns.
     *
     * @return parameter order
     */
    public String getParamOrder() {
        return this.paramOrder;
    }
    /**
     * Set the prepared statement name.
     * 
     * @param newName Prepared statement name
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Set the prepared statement java name.
     * 
     * @param newJavaName Prepared statement java name
     */
    public void setJavaName(final String newJavaName) {
        this.javaName = newJavaName;
    }

    /**
     * Set the catalog name.
     * 
     * @param newCatalog Catalog name
     */
    public void setCatalog(final String newCatalog) {
        this.catalog = newCatalog;
    }

    /**
     * Set the schema name.
     * 
     * @param newSchema Schema name
     */
    public void setSchema(final String newSchema) {
        this.schema = newSchema;
    }

    /**
     * Set the SQL text.
     * 
     * @param newSQLText SQL text
     */
    public void setSQLText(final String newSQLText) {
        this.sqlText = newSQLText;
    }

    /**
     * Set the parameter order of the columns.
     *
     * @param parameter order
     */
    public void setParamOrder(String paramOrder) {
        if(paramOrder != null){
            this.paramOrder = paramOrder;
        }
    }

    /**
     * Set the prepared statement parameter list.
     * 
     * @param newParameters Parameter list
     */
    public void setParameters(final Parameter[] newParameters) {
        this.parameters = newParameters;

        // update the number of parameters
        if (this.parameters != null) {
            this.numParameters = this.parameters.length;
        }
    }

    public void cloneParameters(final Parameter[] newParameters) {
        if (newParameters != null) {
            this.numParameters = newParameters.length;
            if (this.numParameters > 0) {
                this.parameters = new Parameter[this.numParameters];
                for (int i = 0; i < this.numParameters; i++) {
                    this.parameters[i] = new Parameter(newParameters[i]);
                }
            }
        }
    }

    /**
     * Set the prepared statement resultset column list.
     * 
     * @param newResultSetColumns Resultset column list
     */
    public void setResultSetColumns(final ResultSetColumn[] newResultSetColumns) {
        this.resultsetColumns = newResultSetColumns;

        // update the number of resultset columns
        if (this.resultsetColumns != null) {
            this.numResultSetColumns = this.resultsetColumns.length;
        }
    }

    public void cloneResultSetColumns(final ResultSetColumn[] newResultSetColumns) {
        if (newResultSetColumns != null) {
            this.numResultSetColumns = newResultSetColumns.length;
            if (this.numResultSetColumns > 0) {
                this.resultsetColumns = new ResultSetColumn[this.numResultSetColumns];
                for (int i = 0; i < this.numResultSetColumns; i++) {
                    this.resultsetColumns[i] = new ResultSetColumn(newResultSetColumns[i]);
                }
            }
        }
    }
}
