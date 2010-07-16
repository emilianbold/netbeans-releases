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

package org.netbeans.modules.wsdlextensions.jdbc.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperationInput;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCComponent;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCQName;
import org.w3c.dom.Element;

/**
 * @author 
 */
public class JDBCOperationInputImpl extends JDBCComponentImpl implements JDBCOperationInput {
    
    public JDBCOperationInputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public JDBCOperationInputImpl(WSDLModel model){
        this(model, createPrefixedElement(JDBCQName.INPUT.getQName(), model));
    }
    
    public void accept(JDBCComponent.Visitor visitor) {
        visitor.visit(this);
    }

	public int getNumberOfRecords() {
        String records = getAttribute(JDBCAttribute.JDBC_NUMBERRECORD_PROPERTY);
        int recordVal = 0;
        try {
                recordVal = Integer.parseInt(records);
        }catch (Exception e) {
                // just ignore
        }
        return recordVal;
    }

  // need to work on this for getting the values

	public String getOperationType() {
        String opType = getAttribute(JDBCAttribute.JDBC_OPERATIONTYPE_PROPERTY);
        String opTypeVal = null;
        if ( opType != null ) {
            try {
                opTypeVal = opType;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return opTypeVal;
    }

    public void setNumberOfRecords(int interval) {
        setAttribute(JDBC_NUMBERRECORD_PROPERTY, JDBCAttribute.JDBC_NUMBERRECORD_PROPERTY, "" + interval);
    }

	public void setOperationType(String opType) {
        setAttribute(JDBC_OPERATIONTYPE_PROPERTY, JDBCAttribute.JDBC_OPERATIONTYPE_PROPERTY, "" + opType);
    }

	public void setParamOrder(String paramOrder) {
		 setAttribute(JDBC_PARAMORDER_PROPERTY, JDBCAttribute.JDBC_PARAMORDER_PROPERTY, "" + paramOrder);
	}

	public String getParamOrder() {

		String paramOrder = getAttribute(JDBCAttribute.JDBC_PARAMORDER_PROPERTY);
        String paramOrderVal = null;
        if ( paramOrder != null ) {
            try {
                paramOrderVal = paramOrder;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return paramOrderVal;
	}

	public void setSql(String sql) {
		 setAttribute(JDBC_SQL_PROPERTY, JDBCAttribute.JDBC_SQL_PROPERTY, "" + sql);
	}

	public String getSql() {

		String opType = getAttribute(JDBCAttribute.JDBC_SQL_PROPERTY);
        String opTypeVal = null;
        if ( opType != null ) {
            try {
                opTypeVal = opType;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return opTypeVal;
	}

	public String getMarkColumnName(){
		
		String markcolumn = getAttribute(JDBCAttribute.JDBC_MARKCOLUMN_PROPERTY);
        String markcolumnVal = null;
        if ( markcolumn != null ) {
            try {
            	markcolumnVal = markcolumn;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return markcolumnVal;
	}

	public void setMarkColumnName(String markcol){
		 setAttribute(JDBC_MARKCOLUMN_PROPERTY, JDBCAttribute.JDBC_MARKCOLUMN_PROPERTY, "" + markcol);
	}
	
	
	public String getTransaction(){
		
		String transaction = getAttribute(JDBCAttribute.JDBC_TRANSACTION_PROPERTY);
        String transactionVal = null;
        if ( transaction != null ) {
            try {
            	transactionVal = transaction;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return transactionVal;
	}

	public void setTransaction(String TX){
		 setAttribute(JDBC_TRANSACTION_PROPERTY, JDBCAttribute.JDBC_TRANSACTION_PROPERTY, "" + TX);
	}
	
	
	public String getTableName(){
		
		String tableName = getAttribute(JDBCAttribute.JDBC_TABLENAME_PROPERTY);
        String tableNameVal = null;
        if ( tableName != null ) {
            try {
            	tableNameVal = tableName;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return tableNameVal;
	}

	public void setTableName(String table){
		 setAttribute(JDBC_TABLENAME_PROPERTY, JDBCAttribute.JDBC_TABLENAME_PROPERTY, "" + table);
	}
	
	
	public String getMoveRowToTableName(){
		
		String movRowTable = getAttribute(JDBCAttribute.JDBC_MOVEROWTABLE_PROPERTY);
        String movRowTableVal = null;
        if ( movRowTable != null ) {
            try {
            	movRowTableVal = movRowTable;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return movRowTableVal;
	}

	public void setMoveRowToTableName(String moverow){
		 setAttribute(JDBC_MOVEROWTABLE_PROPERTY, JDBCAttribute.JDBC_MOVEROWTABLE_PROPERTY, "" + moverow);
	}
	
	
	public String getPollingPostProcessing(){
		
		String postProcess = getAttribute(JDBCAttribute.JDBC_POSTPROCESS_PROPERTY);
        String postProcessVal = null;
        if ( postProcess != null ) {
            try {
            	postProcessVal = postProcess;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return postProcessVal;
	}

	public void setPollingPostProcessing(String process){
		 setAttribute(JDBC_POSTPROCESS_PROPERTY, JDBCAttribute.JDBC_POSTPROCESS_PROPERTY, "" + process);
	}
	
	
	public String getMarkColumnValue(){
		
		String markColumn = getAttribute(JDBCAttribute.JDBC_MARKCOLVALUE_PROPERTY);
        String markColumnVal = null;
        if ( markColumn != null ) {
            try {
            	markColumnVal = markColumn;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return markColumnVal;
	}

	public void setMarkColumnValue(String markcol){
		 setAttribute(JDBC_MARKCOLVALUE_PROPERTY, JDBCAttribute.JDBC_MARKCOLVALUE_PROPERTY, "" + markcol);
	}
	
	public String getPKName(){
		
		String pkname = getAttribute(JDBCAttribute.JDBC_PKNAME_PROPERTY);
        String pknameVal = null;
        if ( pkname != null ) {
            try {
                pknameVal = pkname;
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return pknameVal;
	}

	public void setPKName(String pkName){
		 setAttribute(JDBC_PKNAME_PROPERTY, JDBCAttribute.JDBC_PKNAME_PROPERTY, "" + pkName);
	}
}
