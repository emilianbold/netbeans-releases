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

package com.netbeans.ddl.impl;

import java.sql.*;
import java.util.*;
import com.netbeans.ddl.*;

/** 
* @author Slavek Psenicka
*/
public class Specification implements DBSpec {

	/** Used DBConnection */
	private HashMap desc;
	
	/** Used JDBC Connection */
	private Connection jdbccon;
	
	public static final String CREATE_TABLE = "CreateTableCommand";
	public static final String RENAME_TABLE = "RenameTableCommand";
	public static final String DROP_TABLE = "DropTableCommand";
	public static final String COMMENT_TABLE = "CommentTableCommand";
	public static final String ADD_COLUMN = "AddColumnCommand";
	public static final String RENAME_COLUMN = "RenameColumnCommand";
	public static final String REMOVE_COLUMN = "RemoveColumnCommand";
	public static final String CREATE_INDEX = "CreateIndexCommand";
	public static final String DROP_INDEX = "DropIndexCommand";
	public static final String ADD_CONSTRAINT = "AddConstraintCommand";
	public static final String DROP_CONSTRAINT = "DropConstraintCommand";	
	public static final String CREATE_VIEW = "CreateViewCommand";
	public static final String DROP_VIEW = "DropViewCommand";
	public static final String CREATE_PROCEDURE = "CreateProcedureCommand";
	public static final String DROP_PROCEDURE = "DropProcedureCommand";
	public static final String CREATE_FUNCTION = "CreateFunctionCommand";
	public static final String DROP_FUNCTION = "DropFunctionCommand";
	public static final String CREATE_TRIGGER = "CreateTriggerCommand";
	public static final String DROP_TRIGGER = "DropTriggerCommand";
	
	/** Constructor */
	public Specification(HashMap description)
	{
		desc = description;
	}

	/** Returns all database properties */
	public Map getProperties()
	{
		return (Map)desc;
	}

	/** Returns command description */
	public Map getCommandProperties(String command)
	{
		return (Map)desc.get(command);
	}

	/** Returns used connection */
	public DBConnection getConnection()
	{
		return (DBConnection)desc.get("connection");
	}

	/** Opens JDBC Connection. 
	* This method usually calls command when it need to process something. 
	* But you can call it explicitly and leave connection open until last
	* command gets executed. Don't forget to close it.
	*/
	public Connection openJDBCConnection()
	throws DDLException
	{
		if (jdbccon != null) throw new DDLException("connection open");
		DBConnection dbcon = getConnection();
		if (dbcon == null) throw new DDLException("none connection specified");
		try {
			jdbccon = dbcon.createJDBCConnection();
		} catch (Exception e) {
			throw new DDLException("none connection specified");
		}
		
		return jdbccon;
	}

	/** Returns JDBC connection.
	* Commands must test if the connection is not open yet; if you simply call
	* openJDBCConnection without test (and the connection will be open by user),
	* a DDLException throws. This is a self-checking mechanism; you must always
	* close used connection.
	*/ 
	public Connection getJDBCConnection()
	{
		return jdbccon;
	}
	
	public void closeJDBCConnection()
	throws DDLException
	{
		if (jdbccon == null) throw new DDLException("no connection open");
		try {
			jdbccon.close();
			jdbccon = null;
		} catch (SQLException e) {
			throw new DDLException("unable to close connection");
		}	
	}	

	/** Creates command identified by commandName. Command names will include 
	* create/rename/drop table/view/index/column and comment table/column. It 
	* returns null if command specified by commandName was not found. Used 
	* system allows developers to extend db-specification files and simply 
	* address new commands (everybody can implement createXXXCommand()).
	*/
	public DDLCommand createCommand(String commandName)
	throws CommandNotSupportedException
	{
		return createCommand(commandName, null);
	}
	
	/** Creates command identified by commandName on table tableName. 
	* Returns null if command specified by commandName was not found. It does not
	* check tableName existency; it simply waits for relevant execute() command
	* which fires SQLException.
	*/	
	public DDLCommand createCommand(String commandName, String tableName)
	throws CommandNotSupportedException
	{
		Class cmdclass;
		AbstractCommand cmd;
		HashMap cprops = (HashMap)desc.get(commandName);
		String classname = (String)cprops.get("Class");
		try {
			cmdclass = Class.forName(classname);
			cmd = (AbstractCommand)cmdclass.newInstance();
		} catch (Exception e) {
			throw new CommandNotSupportedException(commandName, "unable to find or init class "+classname+" for command "+commandName+"("+e+")");
		}

		cmd.setObjectName(tableName);
		cmd.setSpecification(this);
		cmd.setFormat((String)cprops.get("Format"));
		return cmd;
	}
	
	/** Create table command 
	* @param tableName Name of the table
	*/
	public CreateTable createCommandCreateTable(String tableName)
	throws CommandNotSupportedException
	{
		return (CreateTable)createCommand(CREATE_TABLE, tableName);
	}
	
	/** Comment table command 
	* @param tableName Name of the table
	* @param comment New comment
	*/
	public CommentTable createCommandCommentTable(String tableName, String comment)
	throws CommandNotSupportedException
	{
		CommentTable cmd = (CommentTable)createCommand(COMMENT_TABLE, tableName);
		cmd.setComment(comment);
		return cmd;
	}
	
	/** Drop table command
	* @param tableName Name of the table
	*/
	public AbstractCommand createCommandDropTable(String tableName)
	throws CommandNotSupportedException
	{
		return (AbstractCommand)createCommand(DROP_TABLE, tableName);
	}
	
	/** Drop table command
	* @param tableName Name of the table
	*/
	public RenameTable createCommandRenameTable(String tableName, String newName)
	throws CommandNotSupportedException
	{
		RenameTable cmd = (RenameTable)createCommand(RENAME_TABLE, tableName);
		cmd.setNewName(newName);
		return cmd;
	}
		
	/** Add column */
	public AddColumn createCommandAddColumn(String tableName)
	throws CommandNotSupportedException
	{
		return (AddColumn)createCommand(ADD_COLUMN, tableName);
	}

	/** Rename column */
	public RenameColumn createCommandRenameColumn(String tableName)
	throws CommandNotSupportedException
	{
		RenameColumn cmd = (RenameColumn)createCommand(RENAME_COLUMN, tableName);
		return cmd;
	}

	/** Remove column
	* @param tableName Name of the table
	*/
	public RemoveColumn createCommandRemoveColumn(String tableName)
	throws CommandNotSupportedException
	{
		RemoveColumn rcol = (RemoveColumn)createCommand(REMOVE_COLUMN, tableName);
		return rcol;
	}

	/** Create index
	* @param indexName Name of index
	* @param tableName Name of the table
	*/
	public CreateIndex createCommandCreateIndex(String indexName, String tableName)
	throws CommandNotSupportedException
	{
		CreateIndex cicmd = (CreateIndex)createCommand(CREATE_INDEX, indexName);
		cicmd.setTableName(tableName);
		return cicmd;
	}	

	/** Drop index
	* @param indexName Name of index
	*/
	public AbstractCommand createCommandDropIndex(String indexName)
	throws CommandNotSupportedException
	{
		return (AbstractCommand)createCommand(DROP_INDEX, indexName);
	}	
		
	/** Create view
	* @param viewname Name of index
	*/
	public CreateView createCommandCreateView(String viewname)
	throws CommandNotSupportedException
	{
		return (CreateView)createCommand(CREATE_VIEW, viewname);
	}	
	
	/** Drop view
	* @param viewname Name of index
	*/
	public AbstractCommand createCommandDropView(String viewname)
	throws CommandNotSupportedException
	{
		return (AbstractCommand)createCommand(DROP_VIEW, viewname);
	}	
	
	/** Create procedure
	* @param viewname Name of procedure
	*/
	public CreateProcedure createCommandCreateProcedure(String name)
	throws CommandNotSupportedException
	{
		return (CreateProcedure)createCommand(CREATE_PROCEDURE, name);
	}	
	
	/** Drop procedure
	* @param viewname Name of procedure
	*/
	public AbstractCommand createCommandDropProcedure(String name)
	throws CommandNotSupportedException
	{
		return (AbstractCommand)createCommand(DROP_PROCEDURE, name);
	}	
	
	/** Create function
	* @param viewname Name of function
	*/
	public CreateFunction createCommandCreateFunction(String name)
	throws CommandNotSupportedException
	{
		return (CreateFunction)createCommand(CREATE_FUNCTION, name);
	}	
	
	/** Drop function
	* @param viewname Name of function
	*/
	public AbstractCommand createCommandDropFunction(String name)
	throws CommandNotSupportedException
	{
		return (AbstractCommand)createCommand(DROP_FUNCTION, name);
	}	

	/** Create trigger
	* @param viewname Name of trigger
	*/
	public CreateTrigger createCommandCreateTrigger(String name, String tablename, int timing)
	throws CommandNotSupportedException
	{
		CreateTrigger ctrig = (CreateTrigger)createCommand(CREATE_TRIGGER, name);
		ctrig.setTableName(tablename);
		ctrig.setTiming(timing);
		return ctrig;
	}	
	
	/** Drop trigger
	* @param viewname Name of trigger
	*/
	public AbstractCommand createCommandDropTrigger(String name)
	throws CommandNotSupportedException
	{
		return (AbstractCommand)createCommand(DROP_TRIGGER, name);
	}	
			
	/** Returns DBType where maps specified java type */
	public String getType(int type)
	{
		String typestr = "";
		Map typemap = (Map)desc.get("TypeMap");

		switch(type) {
			case java.sql.Types.ARRAY: typestr = "ARRAY"; break;
			case java.sql.Types.BIGINT: typestr = "BIGINT"; break;
			case java.sql.Types.BINARY: typestr = "BINARY"; break;
			case java.sql.Types.BIT: typestr = "BIT"; break;
			case java.sql.Types.BLOB: typestr = "BLOB"; break;
			case java.sql.Types.CHAR: typestr = "CHAR"; break;
			case java.sql.Types.CLOB: typestr = "CLOB"; break;
			case java.sql.Types.DATE: typestr = "DATE"; break;
			case java.sql.Types.DECIMAL: typestr = "DECIMAL"; break;
			case java.sql.Types.DISTINCT: typestr = "DISTINCT"; break;
			case java.sql.Types.DOUBLE: typestr = "DOUBLE"; break;
			case java.sql.Types.FLOAT: typestr = "FLOAT"; break;
			case java.sql.Types.INTEGER: typestr = "INTEGER"; break;
			case java.sql.Types.JAVA_OBJECT: typestr = "JAVA_OBJECT"; break;
			case java.sql.Types.LONGVARBINARY: typestr = "LONGVARBINARY"; break;
			case java.sql.Types.LONGVARCHAR: typestr = "LONGVARCHAR"; break;
			case java.sql.Types.NUMERIC: typestr = "NUMERIC"; break;
			case java.sql.Types.REAL: typestr = "REAL"; break;
			case java.sql.Types.REF: typestr = "REF"; break;
			case java.sql.Types.SMALLINT: typestr = "SMALLINT"; break;
			case java.sql.Types.TIME: typestr = "TIME"; break;
			case java.sql.Types.TIMESTAMP: typestr = "TIMESTAMP"; break;
			case java.sql.Types.TINYINT: typestr = "TINYINT"; break;
			case java.sql.Types.VARBINARY: typestr = "VARBINARY"; break;
			case java.sql.Types.VARCHAR: typestr = "VARCHAR"; break;
		}			

		return (String)typemap.get("java.sql.Types."+typestr);
	}
}

/*
* <<Log>>
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
