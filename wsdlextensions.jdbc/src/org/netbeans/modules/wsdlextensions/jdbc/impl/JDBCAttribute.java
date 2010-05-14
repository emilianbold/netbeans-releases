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
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author 
 *
 */
public enum JDBCAttribute implements Attribute {

	JDBC_OPERATIONTYPE_PROPERTY("operationType"),
	JDBC_NUMBERRECORD_PROPERTY("numberOfRecords"),
	JDBC_RETPARTNAME_PROPERTY("returnPartName"),
	JDBC_URL_PROPERTY("jndiName"),
	JDBC_PARAMORDER_PROPERTY("paramOrder"),
	JDBC_SQL_PROPERTY("sql"),
	JDBC_PKNAME_PROPERTY("PKName"),
	JDBC_MARKCOLUMN_PROPERTY("MarkColumnName"),
	JDBC_TABLENAME_PROPERTY("TableName"),
	JDBC_MOVEROWTABLE_PROPERTY("MoveRowToTableName"),
	JDBC_POSTPROCESS_PROPERTY("PollingPostProcessing"),
	JDBC_MARKCOLVALUE_PROPERTY("MarkColumnValue"),
	JDBC_TRANSACTION_PROPERTY("Transaction");
	
    
    private String name;
    private Class type;
    private Class subtype;
    
    JDBCAttribute(String name) {
        this(name, String.class);
    }
    
    JDBCAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    JDBCAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { return name; }
    
    public Class getType() {
        return type;
    }
    
    public String getName() { return name; }
    
    public Class getMemberType() { return subtype; }
}
