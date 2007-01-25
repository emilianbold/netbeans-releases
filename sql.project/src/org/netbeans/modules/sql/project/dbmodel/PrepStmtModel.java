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

import java.util.Hashtable;
import java.util.ArrayList;

/**
 * @author neenad
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface PrepStmtModel {

	public final static int SQL_SELECT = 0;
	public final static int SQL_INSERT = 1;
	public final static int SQL_UPDATE = 2;
	public final static int SQL_DELETE = 3;
	public final static int SQL_CREATE = 4;
	public final static int SQL_ALTER = 5;
	public final static int SQL_DROP = 6;
    public final static int SQL_TRUNCATE = 7;

	public int getStatementType();

	public void setStatementType(int stmtType);

	public void addTable(Table table);

	public Hashtable getTables();

	public void setTables(Hashtable tableMap);

	public String getSQLText();

	public void setSQLText(String sqlStr);

	public void addWhere(WhereCondition whereCon);

	public void setWhere(ArrayList whereMap );

	public ArrayList getWhere();

	public String createSqlText();

	public boolean getChkDuplicate();

	public void setChkDuplicate(boolean newChkDuplicate);
    
    public void setTableAliasIncluded(boolean isTableAliasSet);
    
    public boolean isTableAliasIncluded();


}