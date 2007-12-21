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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class to hold procedure metadata.
 *
 * @author Susan Chen
 * @version 
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
    public static final String[] PROCTYPES = 
    {
        PROCEDURE, FUNCTION, UNKNOWN
    };
   
    /** Procedure parameter types */
    public static final String[] PARAMTYPES = 
    {
        IN, INOUT, OUT, RETURN, RESULT
    };
    
    private String name = "";           // original name of procedure   
    private String javaName = "";       // java name of procedure   
    private String catalog = "";        // catalog
    private String schema = "";         // schema
    private String type = "";           // type: PROCEDURE, FUNCTION, UNKNOWN
    private int numParameters = 0;      // number of parameters
    private Parameter[] parameters;     // array of parameters
    private boolean hasReturn = false;  // indicates whether has any out parameters

    /**
     * Creates an instance of Procedure with the given attributes.
     *
     * @param pname Procedure name
     * @param pcatalog Catalog name
     * @param pschema Schema name
     * @param ptype Procedure type
     */
    public Procedure(String pname, String pcatalog, String pschema, String ptype) {
        name = pname;
        catalog = pcatalog;
        schema = pschema;
        type = ptype;
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
    public Procedure(String pname, String jname, String pcatalog, String pschema, String ptype) {
        name = pname;
        javaName = jname;
        catalog = pcatalog;
        schema = pschema;
        type = ptype;
    }
    
    public Procedure(Procedure p) {
        name = p.getName();
        javaName = p.getJavaName();
        catalog = p.getCatalog();
        schema = p.getSchema();
        type = p.getType();
        hasReturn = p.getHasReturn();
        cloneParameters(p.getParameters());
    }

    /**
     * Get the procedure name.
     *
     * @return Procedure name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the procedure java name.
     *
     * @return Procedure java name
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
     * Get the Procedure type.
     *
     * @return Procedure type
     */    
    public String getType() {
        return type;
    }
    
    /**
     * Get the number of procedure parameters.
     *
     * @return Number of procedure parameters
     */
    public int getNumParameters() {
        return numParameters;
    }
    
    /**
     * Get the procedure parameter list.
     *
     * @return Procedure parameter list
     */
    public Parameter[] getParameters() {
        return parameters;
    }
    
    /**
     * Indicates whether the procedure has a return.
     *
     * @return Indicates whether the procedure has a return.
     */
    public boolean getHasReturn() {
        return hasReturn;
    }
    
    /**
     * Set the procedure name.
     *
     * @param newName Procedure name
     */
    public void setName(String newName) {
        name = newName;
    }
       
    /**
     * Set the procedure java name.
     *
     * @param newJavaName Procedure java name
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
     * Set the procedure type.
     *
     * @param newType Procedure type
     */
    public void setType(String newType) {
        type = newType;
    }
        
    /**
     * Set the procedure parameter list.
     *
     * @param newParameters Procedure parameter list
     */
    public void setParameters(Parameter[] newParameters) {
        parameters = newParameters;
        
        // update the number of parameters
        if (parameters != null) {
            numParameters = parameters.length;
        } else {
            numParameters = 0;
        }
    }
    
    /**
     * Clone the procedure parameter list.
     *
     * @param newParameters Procedure parameter list
     */
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
     * Set the Procedure has return flag.
     *
     * @param newHasReturn Has Return Flag
     */
    public void setHasReturn(boolean newHasReturn) {
        hasReturn = newHasReturn;
    }

    //added abey for allowing ResultSets to be added to a Procedure.
    private ArrayList resultSetColumns = new ArrayList();

    /**
     * Set the ResultSet Columns of the Procedure.
     *
     * @param rsCols array of the ResultSetColumns of this Procedure.
     */
    public void setResultSetColumns(ResultSetColumns[] rsCols){
        this.resultSetColumns = new ArrayList();
        for (int i = 0; i < rsCols.length; i++) {
            resultSetColumns.add(rsCols[i]);
        }
    }

    /**
     * Set the ResultSet Columns of the Procedure.
     *
     * @param rsCols the Arraylist containing the ResultSetColumns of this Procedure.
     */
    public void setResultSetColumns(ArrayList rsCols){
        this.resultSetColumns = rsCols;
    }

    /**
     * Add a ResultSetColumns to this Procedure.
     *
     * @param rs ResultSetColumns to be added to this Procedure.
     */
    public void addResultSet(ResultSetColumns rs){
        this.resultSetColumns.add(rs);
    }
    public boolean removeResultSet(ResultSetColumns rs){
        ResultSetColumns rsCols = searchResultSetColumns(rs.getName());
        if(rsCols!=null){
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
    public ResultSetColumns[] getResultSetColumnsArray(){
        Object[] objArray = resultSetColumns.toArray();
        ResultSetColumns[] result = new ResultSetColumns[objArray.length];

        for (int i = 0; i < objArray.length; i++) {
            ResultSetColumns rsCols = (ResultSetColumns)objArray[i];
            result[i] = rsCols;
        }
        return result;
    }

    /**
     * get the ResultSet Columns of the Procedure.
     *
     * @return an Arraylist containing the ResultSetColumns of this Procedure.
     */
    public ArrayList getResultSetColumns(){
        return resultSetColumns;
    }


    /**
     * Searches this Procedure for any ResultSet Columns of the given name.
     *
     * @return ResultSetColumns added to this Procedure if its name matches, else null.
     */
	public ResultSetColumns searchResultSetColumns (String rsName){
		if (resultSetColumns != null) {
			Iterator rsColsIter = resultSetColumns.iterator();

			while (rsColsIter.hasNext()) {
				ResultSetColumns rsCols = (ResultSetColumns)rsColsIter.next();
				if (rsCols.getName().equals(rsName)) {
					return rsCols;
				}
			}
		}
		return null;
	}

}
