/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.enterprise.modules.db.explorer.infos;

import java.sql.*;
import java.util.*;
import java.io.IOException;
import com.netbeans.ddl.*;
import org.openide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.ddl.impl.*;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class ColumnNodeInfo extends DatabaseNodeInfo
{
	public boolean canAdd(Map propmap, String propname)
	{
		if (propname.equals("decdigits")) {
			int type = ((Integer)get("datatype")).intValue();
			if (type == java.sql.Types.FLOAT || type == java.sql.Types.REAL || type == java.sql.Types.DOUBLE) return true;
			else return false;
		}
		
		return super.canAdd(propmap, propname);
	}

	public Object getProperty(String key)
	{
		if (key.equals("isnullable")) {
			boolean eq = ((String)get(key)).toUpperCase().equals("YES");
			return new Boolean(eq);
		}
		return super.getProperty(key);
	}

	public void delete()
	throws IOException
	{
		try {
			String code = getCode();
			String table = (String)get(DatabaseNode.TABLE);
			Specification spec = (Specification)getSpecification();
			RemoveColumn cmd = (RemoveColumn)spec.createCommandRemoveColumn(table);
			cmd.removeColumn((String)get(code));
			cmd.execute();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
	
	public TableColumn getColumnSpecification()
	throws DatabaseException
	{
		TableColumn col = null;
		
		try {
			Specification spec = (Specification)getSpecification();
			CreateTable cmd = (CreateTable)spec.createCommandCreateTable("DUMMY");
			String code = getCode();
			DatabaseMetaData dmd = getConnection().getMetaData();
			
			if (code.equals(DatabaseNode.PRIMARY_KEY)) {
				col = (TableColumn)cmd.createPrimaryKeyColumn(getName());
			} else if (code.equals(DatabaseNode.FOREIGN_KEY)) {
				col = null;
			} else if (code.equals(DatabaseNode.COLUMN)) {
				col = (TableColumn)cmd.createColumn(getName());
			} else throw new DatabaseException("unknown code "+code);

			ResultSet rs = dmd.getColumns((String)get(DatabaseNode.CATALOG), getUser(), (String)get(DatabaseNode.TABLE), (String)get(code));
			rs.next();
			
			col.setColumnType(((Integer)get("datatype")).intValue());
			col.setColumnSize(rs.getInt(7));
			col.setNullAllowed(rs.getString(18).toUpperCase().equals("YES"));
			col.setDefaultValue(rs.getString("COLUMN_DEF"));

		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
		
		return col;
	}

	// catalog,schema,tablename,name,datatype,typename,
	// columnsize,bufflen,decdigits,radix,nullable,remarks,coldef,
	// reserved1,reserved2,octetlen,ordpos,isnullable

	public void setProperty(String key, Object obj)
	{
		try {
			if (key.equals("remarks")) setRemarks((String)obj);		
			else if (key.equals("isnullable")) {
				setNullAllowed(((Boolean)obj).booleanValue());
				obj = (((Boolean)obj).equals(Boolean.TRUE) ? "YES" : "NO");
			} else if (key.equals("columnsize")) setColumnSize((Integer)obj);
			else if (key.equals("decdigits")) setDecimalDigits((Integer)obj);
			else if (key.equals("coldef")) setDefaultValue((String)obj);
			else if (key.equals("datatype")) setDataType((Integer)obj);
			super.setProperty(key, obj);
		} catch (Exception e) {
			System.out.println("unable to set "+key+" = "+obj+", "+e.getMessage());
		}
	}

	public void setRemarks(String rem)
	throws DatabaseException
	{
		String tablename = (String)get(DatabaseNode.TABLE);
		Specification spec = (Specification)getSpecification();
		try {
			AbstractCommand cmd = spec.createCommandCommentTable(tablename, rem);
			cmd.execute();		
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void setColumnSize(Integer size)
	throws DatabaseException
	{
		try {
			Specification spec = (Specification)getSpecification();
			ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
			TableColumn col = getColumnSpecification();
			col.setColumnSize(size.intValue());
			cmd.setColumn(col);
			cmd.execute();		
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void setDecimalDigits(Integer size)
	throws DatabaseException
	{
		try {
			Specification spec = (Specification)getSpecification();
			ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
			TableColumn col = getColumnSpecification();
			col.setDecimalSize(size.intValue());
			cmd.setColumn(col);
			cmd.execute();		
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void setDefaultValue(String val)
	throws DatabaseException
	{
		try {
			Specification spec = (Specification)getSpecification();
			ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
			TableColumn col = getColumnSpecification();
			col.setDefaultValue(val);
			cmd.setColumn(col);
			cmd.execute();		
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
	
	public void setNullAllowed(boolean flag)
	throws DatabaseException
	{
		try {
			Specification spec = (Specification)getSpecification();
			ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
			TableColumn col = getColumnSpecification();
			col.setNullAllowed(flag);
			cmd.setColumn(col);
			cmd.execute();		
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void setDataType(Integer type)
	throws DatabaseException
	{
		try {
			Specification spec = (Specification)getSpecification();
			ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
			TableColumn col = getColumnSpecification();
			col.setColumnType(type.intValue());
			cmd.setColumn(col);
			cmd.execute();		
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
}
