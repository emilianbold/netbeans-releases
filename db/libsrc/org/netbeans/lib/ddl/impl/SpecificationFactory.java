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

package org.netbeans.lib.ddl.impl;

import java.sql.*;
import java.util.Set;
import java.util.HashMap;
import java.util.Vector;
import java.net.URL;
import java.io.InputStream;
import java.util.Iterator;
import java.text.ParseException;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/** 
* The factory used for creating instances of Specification class. 
* SpecificationFactory collects information about available database 
* description files and is able to specify if system can control 
* the database (specified by product name or live connection) or not. 
* It also provides a list of supported databases. Information about databases
* reads from file org/netbeans/lib/ddl/DatabaseSpecification.plist. It's possible to replace it
* by setting db.specifications.file system property pointing to another one.
*
* @author Slavek Psenicka
*/
public class SpecificationFactory implements DatabaseSpecificationFactory, DriverSpecificationFactory {
			
	/** Database description file
	* You should use PListReader to parse it.
	*/		
	private final String dbFile = "org/netbeans/lib/ddl/resources/dbspec.plist";	
			
	/** Driver description file
	* You should use PListReader to parse it.
	*/		
	private final String drvFile = "org/netbeans/lib/ddl/resources/driverspec.plist";	
			
	/** Array of SpecificationFiles, found (but not read) files 
	* which describes database products.
	*/
	private HashMap dbSpecs;
  
	/** Array of SpecificationFiles, found (but not read) files 
	* which describes driver products.
	*/
	private HashMap drvSpecs;
	
	/** Debug information
	*/
	private boolean debug = false;
	
	/** Constructor.
	* Reads a bunch of specification files and prepares sfiles array. Files should
	* be read from default place or from folder specified by system property named
	* "db.specifications.folder".
	*/
	public SpecificationFactory ()
	throws DDLException
	{	
		String fileDB = System.getProperty("db.specifications.file");
		String fileDrv = System.getProperty("driver.specifications.file");
  	SpecificationParser parser;
    
		try {
			if (fileDB == null) {
				ClassLoader cl = getClass().getClassLoader();
				InputStream stream = cl.getResourceAsStream(dbFile);
				if (stream == null)
          throw new Exception("unable to open stream " + dbFile);
				parser = new SpecificationParser(stream);
				dbSpecs = parser.getData();
				stream.close();
			} else {
				parser = new SpecificationParser(fileDB);
				dbSpecs = parser.getData();
			}
		} catch (Exception e) {
			if (fileDB != null)
        throw new DDLException("unable to read specifications file " + fileDB + ", " + e.getMessage());
			else
        throw new DDLException("unable to read default specifications file, " + e.getMessage());
		}

		try {
			if (fileDrv == null) {
				ClassLoader cl = getClass().getClassLoader();
				InputStream stream = cl.getResourceAsStream(drvFile);
				if (stream == null)
          throw new Exception("unable to open stream " + drvFile);
				parser = new SpecificationParser(stream);
				drvSpecs = parser.getData();
				stream.close();
			} else {
				parser = new SpecificationParser(fileDrv);
				drvSpecs = parser.getData();
			}
		} catch (Exception e) {
			if (fileDrv != null)
        throw new DDLException("unable to read specifications file " + fileDrv + ", " + e.getMessage());
			else
        throw new DDLException("unable to read default specifications file, " + e.getMessage());
		}
	}
	
	/** Returns array of database products supported by system.
	* It returns string array only, if you need a Specification instance, use 
	* appropriate createSpecification method.
	*/
	public Set supportedDatabases()
	{
		return dbSpecs.keySet();
	}
	
	/** Returns true if database (specified by databaseProductName) is 
	* supported by system. Does not throw exception if it doesn't.
	*/	
	public boolean isDatabaseSupported(String databaseProductName)
	{
		return (dbSpecs.containsKey(databaseProductName));
	}
	
	/** Creates instance of DatabaseSpecification class; a database-specification
	* class. This object knows about used database and can be used as
	* factory for db-manipulating commands. It connects to the database 
	* and reads database metadata. Throws DatabaseProductNotFoundException if database
	* (obtained from database metadata) is not supported.
	*/
	public DatabaseSpecification createSpecification(DBConnection dbcon) 
	throws DatabaseProductNotFoundException, DDLException
	{
		Connection con = dbcon.createJDBCConnection();
		DatabaseSpecification spec = createSpecification(dbcon, con);
		try {
			con.close();
		} catch (SQLException ex) {
			throw new DDLException(ex.getMessage());
		}
		return spec;
	}

	/** Creates instance of DatabaseSpecification class; a database-specification
	* class. This object knows about used database and can be used as
	* factory for db-manipulating commands. It connects to the database 
	* and reads database metadata. Throws DatabaseProductNotFoundException if database
	* (obtained from database metadata) is not supported. Uses given Connection
	*/
	public DatabaseSpecification createSpecification(DBConnection dbcon, Connection jdbccon) 
	throws DatabaseProductNotFoundException, DDLException
	{
		String pn = null;
		try {
			boolean close = (jdbccon != null ? false : true);
			Connection con = (jdbccon != null ? jdbccon : dbcon.createJDBCConnection());
			DatabaseMetaData dmd = con.getMetaData();	
			pn = dmd.getDatabaseProductName().trim();
			
			DatabaseSpecification spec = createSpecification(dbcon, pn, con);
			if (close) con.close();
			return spec;
		} catch (SQLException e) {
			throw new DDLException("unable to connect to server");
		} catch (Exception e) {
			throw new DatabaseProductNotFoundException(pn, "unable to create specification, "+e.getMessage());
		}
	}
	
	/** Creates instance of DatabaseSpecification class; a database-specification
	* class. This object knows about used database and can be used as
	* factory for db-manipulating commands. It connects to database and
	* reads metadata as createSpecification(DBConnection connection), but always
	* uses specified databaseProductName. This is not recommended technique.
	*/
	public DatabaseSpecification createSpecification(DBConnection connection, String databaseProductName, Connection c) 
	throws DatabaseProductNotFoundException
	{
		HashMap product = (HashMap) dbSpecs.get(databaseProductName);
		
		if (product == null)
		  throw new DatabaseProductNotFoundException(databaseProductName);
		HashMap specmap = deepUnion(product, (HashMap) dbSpecs.get("GenericDatabaseSystem"), true);
		specmap.put("connection", connection);
		DatabaseSpecification spec = new Specification(specmap, c);
		spec.setSpecificationFactory(this);
		
		return spec;
	}

	/** Creates instance of DatabaseSpecification class; a database-specification
	* class. This object knows about used database and can be used as
	* factory for db-manipulating commands. It connects to database and
	* reads metadata as createSpecification(DBConnection connection), but always
	* uses specified databaseProductName. This is not recommended technique.
	*/
	public DatabaseSpecification createSpecification(String databaseProductName, Connection c) 
	throws DatabaseProductNotFoundException
	{
		HashMap product = (HashMap) dbSpecs.get(databaseProductName);
		if (product == null) throw new DatabaseProductNotFoundException(databaseProductName);
		HashMap specmap = deepUnion(product, (HashMap) dbSpecs.get("GenericDatabaseSystem"), true);
		return new Specification(specmap, c);
	}

	public DatabaseSpecification createSpecification(Connection c) 
	throws DatabaseProductNotFoundException, SQLException
	{
		return createSpecification(c, c.getMetaData().getDatabaseProductName().trim());
	}

	public DatabaseSpecification createSpecification(Connection c, String databaseProductName) 
	throws DatabaseProductNotFoundException
	{
		HashMap product = (HashMap) dbSpecs.get(databaseProductName);
		if (product == null) throw new DatabaseProductNotFoundException(databaseProductName);
		HashMap specmap = deepUnion(product, (HashMap) dbSpecs.get("GenericDatabaseSystem"), true);
		DatabaseSpecification spec = new Specification(specmap, c);
		spec.setSpecificationFactory(this);
		return spec;
	}

	/** Returns debug-mode flag
	*/
	public boolean isDebugMode()
	{
		return debug;
	}
	
	/** Sets debug-mode flag
	*/
	public void setDebugMode(boolean mode)
	{
		debug = mode;
	}

	/** Returns array of driver products supported by system.
	* It returns string array only, if you need a Specification instance, use 
	* appropriate createDriverSpecification method.
	*/
	public Set supportedDrivers()
	{
		return drvSpecs.keySet();
	}
	
	/** Returns true if driver (specified by driverName) is 
	* supported by system. Does not throw exception if it doesn't.
	*/	
	public boolean isDriverSupported(String driverName)
	{
		return (drvSpecs.containsKey(driverName));
	}

	/** Creates instance of DriverSpecification class; a driver-specification
	* class. This object knows about used driver.
  */
	public DriverSpecification createDriverSpecification(String driverName) {
		HashMap product = (HashMap) drvSpecs.get(driverName);
		if (product == null)
      product = (HashMap) drvSpecs.get("DefaultDriver");
		HashMap specmap = deepUnion(product, (HashMap) drvSpecs.get("DefaultDriver"), true);
		DriverSpecification spec = new DriverSpecification(specmap);
		spec.setDriverSpecificationFactory(this);
    
		return spec;
  }
  
	/** Creates deep copy of Map.
	* All items will be cloned. Used internally in this object.
	*/
	private HashMap deepClone(HashMap map)
	{
		HashMap newone = (HashMap)map.clone();
		Iterator it = newone.keySet().iterator();
		while (it.hasNext()) {
			Object newkey = it.next();
			Object deepobj = null, newobj = newone.get(newkey);
			if (newobj instanceof HashMap) 
				deepobj = deepClone((HashMap)newobj);
			else if (newobj instanceof String) 
				deepobj = (Object)new String((String)newobj);
			else if (newobj instanceof Vector)
				deepobj = ((Vector)newobj).clone();
			newone.put(newkey, deepobj);
		}
		
		return newone;
	}
	
	/** Joins base map with additional one. 
	* Copies keys only if not present in base map. Used internally in this object.
	*/
	private HashMap deepUnion(HashMap base, HashMap additional, boolean deep)
	{
		Iterator it = additional.keySet().iterator();
		while (it.hasNext()) {
			Object addkey = it.next();
			Object addobj = additional.get(addkey);
      
      //SQL92 types will be not added into databese type list
      if (addkey.equals("TypeMap"))
        continue;
      
			if (base.containsKey(addkey)) {
				Object baseobj = base.get(addkey);
				if (deep && (baseobj instanceof HashMap) && (addobj instanceof HashMap)) {
					deepUnion((HashMap)baseobj, (HashMap)addobj, deep);
				}				
			} else {
				if (addobj instanceof HashMap) 
					addobj = deepClone((HashMap)addobj);
				else if (addobj instanceof String) 
					addobj = (Object)new String((String)addobj);
				else if (addobj instanceof Vector)
					addobj = ((Vector)addobj).clone();
				base.put(addkey, addobj);
			}	
		}
		
		return base;	
	}
  
}

/*
* <<Log>>
*  13   Gandalf   1.12        1/25/00  Radko Najman    
*  12   Gandalf   1.11        1/12/00  Radko Najman    deepUnion() method is not
*       called for TypeMap key
*  11   Gandalf   1.10        12/15/99 Radko Najman    driverspec.plist
*  10   Gandalf   1.9         11/1/99  Radko Najman    getDatabaseProductName().trim()
*       
*  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  8    Gandalf   1.7         9/13/99  Slavek Psenicka 
*  7    Gandalf   1.6         9/10/99  Slavek Psenicka 
*  6    Gandalf   1.5         6/15/99  Slavek Psenicka adding support for live 
*       connection
*  5    Gandalf   1.4         5/14/99  Slavek Psenicka new version
*  4    Gandalf   1.3         4/23/99  Slavek Psenicka Chyba v createSpec pri 
*       ConnectAs
*  3    Gandalf   1.2         4/23/99  Slavek Psenicka Opravy v souvislosti se 
*       spravnym throwovanim :) CommandNotImplementedException
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
