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

package com.netbeans.enterprise.modules.db.explorer;

import java.sql.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.lang.String;
import java.util.Properties;
import com.netbeans.ddl.*;

/** 
* Connection information
* This class encapsulates all information needed for connection to database
* (database and driver url, login name and password). It can create JDBC 
* connection and feels to be a bean (has propertychange support and customizer).
* Instances of this class uses explorer option to store information about
* open connection.
*
* @author Slavek Psenicka
*/

public class DatabaseConnection extends Object implements DBConnection 
{
	/** Driver URL */
	private String drv;
	
	/** Database URL */
	private String db;
	
	/** User login name */
	private String usr;
	
	/** User password */
	private String pwd;

	/** Remembers password */
	private Boolean rpwd;

	/** The support for firing property changes */
	private PropertyChangeSupport propertySupport;

	public static final String PROP_DRIVER = "driver";
	public static final String PROP_DATABASE = "database";
	public static final String PROP_USER = "user";
	public static final String PROP_PASSWORD = "password";

	/** Default constructor */
	public DatabaseConnection()
	{
		propertySupport = new PropertyChangeSupport(this);
	}

	/** Advanced constructor
	* Allows to specify all needed information.
	* @param driver Driver URL
	* @param database Database URL
	* @param user User login name
	* @param password User password
	*/
	public DatabaseConnection(String driver, String database, String user, String password)
	{
		drv = driver;
		db = database;	
		usr = user;
		pwd = password;
		rpwd = Boolean.FALSE;
	}
	
	/** Returns driver URL */
	public String getDriver()
	{
		return drv;
	}
	
	/** Sets driver URL 
	* Fires propertychange event.
	* @param driver DNew driver URL
	*/
	public void setDriver(String driver)
	{
		if (driver == null || driver.equals(drv)) return;
		String olddrv = drv;
		drv = driver;
		propertySupport.firePropertyChange(PROP_DRIVER, olddrv, drv);
	}

	/** Returns database URL */
	public String getDatabase()
	{
		return db;
	}
	
	/** Sets database URL 
	* Fires propertychange event.
	* @param database New database URL
	*/
	public void setDatabase(String database)
	{
		if (database == null || database.equals(db)) return;
		String olddb = db;
		db = database;
		propertySupport.firePropertyChange(PROP_DATABASE, olddb, db);
	}

	/** Returns user login name */
	public String getUser()
	{
		return usr;
	}
	
	/** Sets user login name 
	* Fires propertychange event.
	* @param user New login name
	*/
	public void setUser(String user)
	{
		if (user == null || user.equals(usr)) return;
		String oldusr = usr;
		usr = user;
		propertySupport.firePropertyChange(PROP_USER, oldusr, usr);
	}

	/** Returns if password should be remembered */
	public boolean rememberPassword()
	{
		return rpwd.equals(Boolean.TRUE);
	}
	
	/** Sets password should be remembered 
	* @param flag New flag
	*/
	public void setRememberPassword(boolean flag)
	{
		rpwd = (flag ? Boolean.TRUE : Boolean.FALSE);
	}

	/** Returns password */
	public String getPassword()
	{
		return pwd;
	}
	
	/** Sets password 
	* Fires propertychange event.
	* @param password New password
	*/
	public void setPassword(String password)
	{
		if (password == null || password.equals(pwd)) return;
		String oldpwd = pwd;
		pwd = password;
		propertySupport.firePropertyChange(PROP_PASSWORD, oldpwd, pwd);
	}

	/** Creates JDBC connection 
	* Uses DriverManager to create connection to specified database. Throws 
	* DDLException if none of driver/database/user/password is set or if 
	* driver or database does not exist or is inaccessible.
	*/
	public Connection createJDBCConnection()
	throws DDLException
	{
		if (drv == null || db == null || usr == null || pwd == null) throw new DDLException("insufficient information to create a connection");
		Properties dbprops = new Properties();
		dbprops.put("user", usr);
		dbprops.put("password", pwd);
		
		try {
			Class.forName(drv);	
			return DriverManager.getConnection(db, dbprops);
		} catch (Exception e) {
			throw new DDLException("can't establish a connection to "+db+" using "+drv+"("+e+")");
		}
	}

	/** Add property change listener 
	* Registers a listener for the PropertyChange event. The connection object
	* should fire a PropertyChange event whenever somebody changes driver, database,
	* login name or password.
	*/
	public void addPropertyChangeListener (PropertyChangeListener l) {
		propertySupport.addPropertyChangeListener (l);
	}

	/** Remove property change listener
	* Remove a listener for the PropertyChange event.
	*/
	public void removePropertyChangeListener (PropertyChangeListener l) {
		propertySupport.removePropertyChangeListener (l);
	}
	
	public int hashCode()
	{
		return drv.hashCode()+db.hashCode()+usr.hashCode();
	}
	
	/** Compares two connections.
	* Returns true if driver, database and login name equals.
	*/
	public boolean equals(Object obj)
	{
		if (obj instanceof DBConnection) {
			DBConnection con = (DBConnection)obj;
			return (drv.equals(con.getDriver()) && db.equals(con.getDatabase()) && usr.equals(con.getUser()));	
		}
		
		return false;
	}
	
	/** Reads object from stream */
	private void readObject(java.io.ObjectInputStream in) 
	throws java.io.IOException, ClassNotFoundException 
	{
		drv = (String)in.readObject();
		db = (String)in.readObject();
		usr = (String)in.readObject();
		pwd = (String)in.readObject();
		rpwd = (Boolean)in.readObject();
	}

	/** Writes object to stream */
	private void writeObject(java.io.ObjectOutputStream out)
	throws java.io.IOException 
	{
		out.writeObject(drv);
		out.writeObject(db);
		out.writeObject(usr);
		out.writeObject(pwd);
		out.writeObject(rpwd);
	}
	
	public String toString()
	{
		return "drv: "+drv+" db: "+db+" usr: "+usr+" pwd: "+pwd+(rpwd.booleanValue() ? " (R)" : "");
	}

}

