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

package com.netbeans.ddl;

import java.sql.*;
import java.util.*;
import com.netbeans.ddl.*;

/** 
* Interface for commands.
* DatabaseSpecification instances keeps information about used database type (object was 
* created using factory's method createSpecification()) and used connection info. 
* It should be used as factory for DDLCommands.
* It also converts java classes into native database types and vice versa.
*
* @author Slavek Psenicka
*/
public interface DatabaseSpecification {

	/** Returns database metadata */
	public DatabaseMetaData getMetaData() throws SQLException;	

	public String getMetaDataAdaptorClassName();
	public void setMetaDataAdaptorClassName(String name);
	
	/** Returns used connection */
	public DBConnection getConnection();

	/** Creates and returns java.sql.Connection object */
	public Connection openJDBCConnection() throws DDLException;
	
	/** Returns java.sql.Connection, if present and open */
	public Connection getJDBCConnection();

	/** Returns factory */
	public DatabaseSpecificationFactory getSpecificationFactory();
	
	/** Sets factory */
	public void setSpecificationFactory(DatabaseSpecificationFactory fac);
		
	/** Closes the connection. 
	* If you forget to close the connection, next open should throw
	* DDLException. This is an internal dummy-trap.
	*/
	public void closeJDBCConnection() throws DDLException;

	/** Returns all database properties.
	* It contains all command properties. Used to obtain settings independent
	* on commands.
	*/
	public Map getProperties();

	/** Returns properties of command.
	* This description should be used for formatting commands, it contains
	* available information for DatabaseSpecification. 
	* @param command Name of command. 
	*/
	public Map getCommandProperties(String command);

	/** Creates command identified by commandName. Command names will include 
	* create/rename/drop table/view/index/column and comment table/column. It 
	* returns null if command specified by commandName was not found. Used 
	* system allows developers to extend db-specification files and simply 
	* address new commands (everybody can implement createXXXCommand()).
	* @param command Name of command. 
	*/
	public DDLCommand createCommand(String commandName) throws CommandNotSupportedException;
	
	/** Returns DBType where maps specified java type.
	*/
	public String getType(int sqltype);
}

/*
* <<Log>>
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         9/13/99  Slavek Psenicka 
*  1    Gandalf   1.0         9/10/99  Slavek Psenicka 
* $
*/
