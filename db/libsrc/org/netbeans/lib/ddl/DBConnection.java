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

package org.netbeans.lib.ddl;

import java.sql.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.lang.String;
import java.util.Properties;

/** 
* Connection information.
* This interface defines information needed for connection to database
* (database and driver url, login name and password). It can create JDBC 
* connection and feels to be a bean (has propertychange support and customizer).
* Instances of this class uses explorer option to store information about
* open connection.
*
* @author Slavek Psenicka
*/

public interface DBConnection extends java.io.Serializable 
{	
	/** Returns driver URL */
	public String getDriver();
	
	/** Sets driver URL 
	* Fires propertychange event.
	* @param driver DNew driver URL
	*/
	public void setDriver(String driver);

	/** Returns database URL */
	public String getDatabase();
	
	/** Sets database URL 
	* Fires propertychange event.
	* @param database New database URL
	*/
	public void setDatabase(String database);

	/** Returns user login name */
	public String getUser();
	
	/** Sets user login name 
	* Fires propertychange event.
	* @param user New login name
	*/
	public void setUser(String user);

	/** Returns if password should be remembered */
	public boolean rememberPassword();
	
	/** Sets password should be remembered 
	* @param flag New flag
	*/
	public void setRememberPassword(boolean flag);

	/** Returns password */
	public String getPassword();
	
	/** Sets password 
	* Fires propertychange event.
	* @param password New password
	*/
	public void setPassword(String password);

	/** Creates JDBC connection 
	* Uses DriverManager to create connection to specified database. Throws 
	* DDLException if none of driver/database/user/password is set or if 
	* driver or database does not exist or is inaccessible.
	*/
	public Connection createJDBCConnection() throws DDLException;
}


/*
 * <<Log>>
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
 * $
 */
