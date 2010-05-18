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
package org.netbeans.modules.jdbcwizard.builder;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class to hold procedure metadata.
 * 
 * @author
 */
public class Procedure {
    /** Procedure type */
    public static final String PROCEDURE = "PROCEDURE";

    /** Function type */
    public static final String FUNCTION = "FUNCTION";

    /** Unknown type */
    public static final String UNKNOWN = "UNKNOWN";

    /** Parameter type IN */
    public static final String IN = "IN";

    /** Parameter type IN/OUT */
    public static final String INOUT = "INOUT";

    /** Parameter type OUT */
    public static final String OUT = "OUT";

    /** Parameter type RETURN */
    public static final String RETURN = "RETURN";

    /** Parameter type RESULT */
    public static final String RESULT = "RESULT";

    /** Procedure types */
    public static final String[] PROCTYPES = { Procedure.PROCEDURE, Procedure.FUNCTION, Procedure.UNKNOWN };

    /** Procedure parameter types */
    public static final String[] PARAMTYPES = { Procedure.IN, Procedure.INOUT, Procedure.OUT, Procedure.RETURN, Procedure.RESULT };

    private String name = ""; // original name of procedure

    private String javaName = ""; // java name of procedure

    private String catalog = ""; // catalog

    private String schema = ""; // schema

    private String type = ""; // type: PROCEDURE, FUNCTION, UNKNOWN

    private int numParameters = 0; // number of parameters

    private Parameter[] parameters; // array of parameters

    private boolean hasReturn = false; // indicates whether has any out parameters
    private String callableStatementString;
    /**
     * Creates an instance of Procedure with the given attributes.
     * 
     * @param pname Procedure name
     * @param pcatalog Catalog name
     * @param pschema Schema name
     * @param ptype Procedure type
     */
    public Procedure(final String pname, final String pcatalog, final String pschema, final String ptype) {
        this.name = pname;
        this.catalog = pcatalog;
        this.schema = pschema;
        this.type = ptype;
    }

    /**
     * Creates an instance of Procedure with the given attributes.
     * 
     * @param pname Procedure name
     * @param jname Procedure java name
     * @param pcatalog Catalog name
     * @param pschema Schema name
     * @param ptype Procedure type
     */
    public Procedure(final String pname, final String jname, final String pcatalog, final String pschema, final String ptype) {
        this.name = pname;
        this.javaName = jname;
        this.catalog = pcatalog;
        this.schema = pschema;
        this.type = ptype;
    }

    public Procedure(final Procedure p) {
        this.name = p.getName();
        this.javaName = p.getJavaName();
        this.catalog = p.getCatalog();
        this.schema = p.getSchema();
        this.type = p.getType();
        this.hasReturn = p.getHasReturn();
        this.cloneParameters(p.getParameters());
    }

    /**
     * Get the procedure name.
     * 
     * @return Procedure name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the procedure java name.
     * 
     * @return Procedure java name
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
     * Get the Procedure type.
     * 
     * @return Procedure type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the number of procedure parameters.
     * 
     * @return Number of procedure parameters
     */
    public int getNumParameters() {
        return this.numParameters;
    }

    /**
     * Get the procedure parameter list.
     * 
     * @return Procedure parameter list
     */
    public Parameter[] getParameters() {
        return this.parameters;
    }

    /**
     * Indicates whether the procedure has a return.
     * 
     * @return Indicates whether the procedure has a return.
     */
    public boolean getHasReturn() {
        return this.hasReturn;
    }

    /**
     * Set the procedure name.
     * 
     * @param newName Procedure name
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Set the procedure java name.
     * 
     * @param newJavaName Procedure java name
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
     * Set the procedure type.
     * 
     * @param newType Procedure type
     */
    public void setType(final String newType) {
        this.type = newType;
    }

    /**
     * Set the procedure parameter list.
     * 
     * @param newParameters Procedure parameter list
     */
    public void setParameters(final Parameter[] newParameters) {
        this.parameters = newParameters;

        // update the number of parameters
        if (this.parameters != null) {
            this.numParameters = this.parameters.length;
        } else {
            this.numParameters = 0;
        }
    }

    /**
     * Clone the procedure parameter list.
     * 
     * @param newParameters Procedure parameter list
     */
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
     * Set the Procedure has return flag.
     * 
     * @param newHasReturn Has Return Flag
     */
    public void setHasReturn(final boolean newHasReturn) {
        this.hasReturn = newHasReturn;
    }

    // added abey for allowing ResultSets to be added to a Procedure.
    private ArrayList resultSetColumns = new ArrayList();

    /**
     * Set the ResultSet Columns of the Procedure.
     * 
     * @param rsCols array of the ResultSetColumns of this Procedure.
     */
    public void setResultSetColumns(final ResultSetColumns[] rsCols) {
        this.resultSetColumns = new ArrayList();
        for (int i = 0; i < rsCols.length; i++) {
            this.resultSetColumns.add(rsCols[i]);
        }
    }

    /**
     * Set the ResultSet Columns of the Procedure.
     * 
     * @param rsCols the Arraylist containing the ResultSetColumns of this Procedure.
     */
    public void setResultSetColumns(final ArrayList rsCols) {
        this.resultSetColumns = rsCols;
    }

    /**
     * Add a ResultSetColumns to this Procedure.
     * 
     * @param rs ResultSetColumns to be added to this Procedure.
     */
    public void addResultSet(final ResultSetColumns rs) {
        this.resultSetColumns.add(rs);
    }

    public boolean removeResultSet(final ResultSetColumns rs) {
        final ResultSetColumns rsCols = this.searchResultSetColumns(rs.getName());
        if (rsCols != null) {
            this.resultSetColumns.remove(rsCols);
            return true;
        }
        return false;
    }

    /**
     * get the ResultSet Columns of the Procedure.
     * 
     * @return an array containing the ResultSetColumns of this Procedure.
     */
    public ResultSetColumns[] getResultSetColumnsArray() {
        final Object[] objArray = this.resultSetColumns.toArray();
        final ResultSetColumns[] result = new ResultSetColumns[objArray.length];

        for (int i = 0; i < objArray.length; i++) {
            final ResultSetColumns rsCols = (ResultSetColumns) objArray[i];
            result[i] = rsCols;
        }
        return result;
    }

    /**
     * get the ResultSet Columns of the Procedure.
     * 
     * @return an Arraylist containing the ResultSetColumns of this Procedure.
     */
    public ArrayList getResultSetColumns() {
        return this.resultSetColumns;
    }

    /**
     * Searches this Procedure for any ResultSet Columns of the given name.
     * 
     * @return ResultSetColumns added to this Procedure if its name matches, else null.
     */
    public ResultSetColumns searchResultSetColumns(final String rsName) {
        if (this.resultSetColumns != null) {
            final Iterator rsColsIter = this.resultSetColumns.iterator();

            while (rsColsIter.hasNext()) {
                final ResultSetColumns rsCols = (ResultSetColumns) rsColsIter.next();
                if (rsCols.getName().equals(rsName)) {
                    return rsCols;
                }
            }
        }
        return null;
    }

    public void setCallableStmtString(String cstmtString) {
        this.callableStatementString = cstmtString;
    }
    
    public String getCallableStmtString() {
        return this.callableStatementString;
    }
}
