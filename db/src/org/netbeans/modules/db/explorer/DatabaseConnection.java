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
import org.openide.TopManager;
import java.lang.reflect.*;

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
	/** Driver URL and name */
	private String drv, drvname;
	
	/** Database URL */
	private String db;
	
	/** User login name */
	private String usr;
	
	/** User password */
	private String pwd = "";

	/** Remembers password */
	private Boolean rpwd = Boolean.FALSE;

	/** The support for firing property changes */
	private PropertyChangeSupport propertySupport;

	/** Connection name */
	private String name;

	public static final String PROP_DRIVER = "driver";
	public static final String PROP_DATABASE = "database";
	public static final String PROP_USER = "user";
	public static final String PROP_PASSWORD = "password";
	public static final String PROP_DRIVERNAME = "drivername";
	public static final String PROP_NAME = "name";

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

	public String getDriverName()
	{
		return drvname;
	}
	
	public void setDriverName(String name)
	{
		if (name == null || name.equals(drvname)) return;
		String olddrv = drvname;
		drvname = name;
		propertySupport.firePropertyChange(PROP_DRIVERNAME, olddrv, drvname);
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

	/** Returns name of the connection */
	public String getName()
	{
		return name;
	}
	
	/** Sets user login name 
	* Fires propertychange event.
	* @param user New login name
	*/
	public void setName(String value)
	{
		if (name == null || name.equals(value)) return;
		String old = name;
		name = value;
		propertySupport.firePropertyChange(PROP_NAME, old, name);
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
/*
			ClassLoader syscl = TopManager.getDefault().currentClassLoader();
			Class cl = syscl.loadClass("java.sql.DriverManager");
			cl = syscl.loadClass(drv);
			DriverManager.registerDriver((Driver)cl.newInstance());

			Method gmet = DriverManager.class.getDeclaredMethod("getConnection", new Class[] {String.class, Properties.class, ClassLoader.class});
			gmet.setAccessible(true);
			gmet.invoke(DriverManager.class, new Object[] {db, dbprops, syscl});		

	    	Connection connection = (Connection)gmet.invoke(DriverManager.class, new Object[] {db, dbprops, syscl});
*/
			Class.forName(drv);
			Connection connection = DriverManager.getConnection(db, dbprops);	
			return connection;

		} catch (Exception e) {
			throw new DDLException("cannot establish a connection to "+db+" using "+drv+"("+e+")");
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
			return db.equals(con.getDatabase());		
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
//		rpwd = (Boolean)in.readObject();
		rpwd = new Boolean(false);
		name = (String)in.readObject();
	}

	/** Writes object to stream */
	private void writeObject(java.io.ObjectOutputStream out)
	throws java.io.IOException 
	{
		out.writeObject(drv);
		out.writeObject(db);
		out.writeObject(usr);
		out.writeObject(pwd);
//		out.writeObject(rpwd);
		out.writeObject(name);
	}
	
	public String toString()
	{
		return "drv: "+drv+" db: "+db+" usr: "+usr;
	}

}


/*
 * <<Log>>
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         9/23/99  Slavek Psenicka Bug #3311
 *  8    Gandalf   1.7         9/13/99  Slavek Psenicka 
 *  7    Gandalf   1.6         8/19/99  Slavek Psenicka English
 *  6    Gandalf   1.5         7/21/99  Slavek Psenicka driver name
 *  5    Gandalf   1.4         6/15/99  Slavek Psenicka support for 
 *       in-repository mounted jdbc drivers (instead of classpath entry)
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka Debug mode
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
