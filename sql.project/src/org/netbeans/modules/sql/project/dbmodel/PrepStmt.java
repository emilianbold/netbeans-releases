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
