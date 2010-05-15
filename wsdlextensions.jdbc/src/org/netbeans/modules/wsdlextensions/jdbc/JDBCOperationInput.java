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

package org.netbeans.modules.wsdlextensions.jdbc;

/**
* @author Venkat P
*/
public interface JDBCOperationInput extends JDBCComponent {

	public static final String JDBC_OPERATIONTYPE_PROPERTY = "operationType";
	public static final String JDBC_NUMBERRECORD_PROPERTY  = "numberOfRecords";

	public static final String JDBC_PARAMORDER_PROPERTY = "paramOrder";
	public static final String JDBC_SQL_PROPERTY = "sql";
	public static final String JDBC_PKNAME_PROPERTY = "PKName";
	public static final String JDBC_MARKCOLUMN_PROPERTY = "MarkColumnName";
	public static final String JDBC_TABLENAME_PROPERTY = "TableName";
	public static final String JDBC_MOVEROWTABLE_PROPERTY = "MoveRowToTableName";
	public static final String JDBC_POSTPROCESS_PROPERTY = "PollingPostProcessing";
	public static final String JDBC_MARKCOLVALUE_PROPERTY = "MarkColumnValue";
	public static final String JDBC_TRANSACTION_PROPERTY = "Transaction";

    public String getOperationType();
    public void setOperationType(String opType);

	public int getNumberOfRecords();
    public void setNumberOfRecords(int numRecords);	

	public void setParamOrder(String paramOrder);
	public String getParamOrder();
	
	public void setSql(String sql);
	public String getSql();
	
	public String getPKName();
	public void setPKName(String pkName);
	
	public String getMarkColumnName();
	public void setMarkColumnName(String colName);
	
	public String getTransaction();
	public void setTransaction(String transaction);
	
	public String getTableName();
	public void setTableName(String tableName);
	
	public String getMoveRowToTableName();
	public void setMoveRowToTableName(String movRowTable);
	
	public String getPollingPostProcessing();
	public void setPollingPostProcessing(String pollProcess);
	
	public String getMarkColumnValue();
	public void setMarkColumnValue(String markColValue);	

}

