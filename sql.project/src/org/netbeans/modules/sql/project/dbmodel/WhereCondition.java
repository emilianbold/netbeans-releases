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
 * @author neenad
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface WhereCondition {

	public String getColumnName();

	public void setColumnName(String newColName);

	public String getOperator();

	public String getLogicalOperator();

	public void setLogicalOperator(String newLogicalOper);

	public void setOperator(String newOper);

	public void setValue(String newVal);

	public String getValue();

	public String getColumnType();

	public void setColumnType(String newColType);
	
	public boolean getIsValueColumnName();
	
     public void setIsValueColumnName(boolean check);



}