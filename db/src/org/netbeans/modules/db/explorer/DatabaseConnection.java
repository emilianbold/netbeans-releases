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

package org.netbeans.modules.db.explorer;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.*;

/**
* Connection information
* This class encapsulates all information needed for connection to database
* (database and driver url, login name, password and schema name). It can create JDBC 
* connection and feels to be a bean (has propertychange support and customizer).
* Instances of this class uses explorer option to store information about
* open connection.
*/
public class DatabaseConnection implements DBConnection {
    
    static final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
    
    static final long serialVersionUID =4554639187416958735L;
    
    /** Driver URL and name */
    private String drv, drvname;

    /** Database URL */
    private String db;

    /** User login name */
    private String usr;

    /** Schema name */
    private String schema;

    /** User password */
    private String pwd = ""; //NOI18N

    /** Remembers password */
    private Boolean rpwd = Boolean.FALSE;

    /** The support for firing property changes */
    private PropertyChangeSupport propertySupport;

    /** Connection name */
    private String name;

    private static final String SUPPORT = "_schema_support"; //NOI18N
    public static final String PROP_DRIVER = "driver"; //NOI18N
    public static final String PROP_DATABASE = "database"; //NOI18N
    public static final String PROP_USER = "user"; //NOI18N
    public static final String PROP_PASSWORD = "password"; //NOI18N
    public static final String PROP_SCHEMA = "schema"; //NOI18N
    public static final String PROP_DRIVERNAME = "drivername"; //NOI18N
    public static final String PROP_NAME = "name"; //NOI18N

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
        name = null;
        name = getName();
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
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_DRIVERNAME, olddrv, drvname);
    }

    /** Returns database URL */
    public String getDatabase()
    {
        if(db==null)
            db = new String();
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
        name = null;
        name = getName();
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_DATABASE, olddb, db);
    }

    /** Returns user login name */
    public String getUser()
    {
        if(usr==null)
            usr = new String();
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
        name = null;
        name = getName();
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_USER, oldusr, usr);
    }

    /** Returns name of the connection */
    public String getName()
    {
        if(name==null)
            if((getSchema()==null)||(getSchema().length()==0))
                name = MessageFormat.format(bundle.getString("ConnectionNodeUniqueName"), // NOI18N
                        new String[] {getDatabase(), getUser(), 
                        bundle.getString("SchemaIsNotSet")});
            else
                name = MessageFormat.format(bundle.getString("ConnectionNodeUniqueName"), // NOI18N
                        new String[] {getDatabase(), getUser(), getSchema()});
        return name;
    }

    /** Sets user name of the connection
    * Fires propertychange event.
    * @param value New connection name
    */
    public void setName(String value)
    {
        if (name == null || name.equals(value)) return;
        String old = name;
        name = value;
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_NAME, old, name);
    }

    /** Returns user schema name */
    public String getSchema()
    {
        if(schema==null)
            schema = new String();
        return schema;
    }

    /** Sets user schema name
    * Fires propertychange event.
    * @param user New login name
    */
    public void setSchema(String schema_name)
    {
        if (schema_name == null || schema_name.equals(schema)) return;
        String oldschema = schema;
        schema = schema_name;
        name = null;
        name = getName();
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_SCHEMA, oldschema, schema);
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
        if(propertySupport!=null)
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
        if (drv == null || db == null || usr == null || pwd == null )
            throw new DDLException(bundle.getString("EXC_InsufficientConnInfo"));

        Properties dbprops = new Properties();
        dbprops.put("user", usr); //NOI18N
        dbprops.put("password", pwd); //NOI18N

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
            
        } catch (SQLException e) {
            // hack for Pointbase Network Server
            String message = MessageFormat.format(bundle.getString("EXC_CannotEstablishConnection"), new String[] {db, drv, e.getMessage()}); // NOI18N
            if(drv.equals(PointbasePlus.DRIVER))
                if(e.getErrorCode()==PointbasePlus.ERR_SERVER_REJECTED)
                    message = MessageFormat.format(bundle.getString("EXC_PointbaseServerRejected"), new String[] {message}); // NOI18N
            throw new DDLException(message);
        } catch (Exception exc) {
            String message = MessageFormat.format(bundle.getString("EXC_CannotEstablishConnection"), new String[] {db, drv, exc.getMessage()}); // NOI18N
            throw new DDLException(message);
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
            return toString().equals(con.toString());
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
        schema = (String)in.readObject();
        rpwd = new Boolean(false);
        name = (String)in.readObject();

        // boston setting/pilsen setting?
        if((name!=null)&&(name.equals(DatabaseConnection.SUPPORT))) {
            // pilsen
        } else {
            // boston
            schema = null;
        }
        name = null;
        name = getName();
    }

    /** Writes object to stream */
    private void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException
    {
        out.writeObject(drv);
        out.writeObject(db);
        out.writeObject(usr);
        out.writeObject(schema);
        out.writeObject(DatabaseConnection.SUPPORT);
    }

    public String toString() {
        return "Driver:" + drv +
                "Database:" + db.toLowerCase() + 
                "User:" + usr.toLowerCase() + 
                "Schema:" + schema.toLowerCase();
    }

}
