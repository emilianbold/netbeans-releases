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
import java.util.Set;
import java.util.HashMap;
import java.util.Vector;
import java.net.URL;
import java.io.InputStream;
import java.util.Iterator;
import java.text.ParseException;
import com.netbeans.ddl.*;

/** 
* The factory used for creating instances of Specification class. 
* SpecificationFactory collects information about available database 
* description files and is able to specify if system can control 
* the database (specified by product name or live connection) or not. 
* It also provides a list of supported databases. Information about databases
* reads from file com/netbeans/ddl/DatabaseSpecification.plist. It's possible to replace it
* by setting db.specifications.file system property pointing to another one.
*
* @author Slavek Psenicka
*/
public class SpecificationFactory implements DatabaseSpecificationFactory {
			
	/** Database description file
	* You should use PListReader to parse it.
	*/		
	private final String sfile = "com/netbeans/ddl/resources/dbspec.plist";	
			
	/** Array of SpecificationFiles, found (but not read) files 
	* which describes database products.
	*/
	private HashMap specs; 
	
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
		String file = System.getProperty("db.specifications.file");
		try {
			SpecificationParser parser;
			if (file == null) {
				ClassLoader cl = getClass().getClassLoader();
				InputStream stream = cl.getResourceAsStream(sfile);
				if (stream == null) throw new Exception("unable to open stream "+sfile);
				parser = new SpecificationParser(stream);
				specs = parser.getData();
				stream.close();		
			} else {
				parser = new SpecificationParser(file);
				specs = parser.getData();
			}
		} catch (Exception e) {
			if (file != null) throw new DDLException("unable to read specifications file "+file+", "+e.getMessage());
			else throw new DDLException("unable to read default specifications file, "+e.getMessage());
		}
	}
	
	/** Returns array of database products supported by system.
	* It returns string array only, if you need a Specification instance, use 
	* appropriate createSpecification method.
	*/
	public Set supportedDatabases()
	{
		return specs.keySet();
	}
	
	/** Returns true if database (specified by databaseProductName) is 
	* supported by system. Does not throw exception if it doesn't.
	*/	
	public boolean isDatabaseSupported(String databaseProductName)
	{
		return (specs.containsKey(databaseProductName));
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
			pn = dmd.getDatabaseProductName();
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
		HashMap product = (HashMap)specs.get(databaseProductName);
		if (product == null) throw new DatabaseProductNotFoundException(databaseProductName);
		HashMap specmap = deepUnion(product, (HashMap)specs.get("GenericDatabaseSystem"), true);
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
		HashMap product = (HashMap)specs.get(databaseProductName);
		if (product == null) throw new DatabaseProductNotFoundException(databaseProductName);
		HashMap specmap = deepUnion(product, (HashMap)specs.get("GenericDatabaseSystem"), true);
		return new Specification(specmap, c);
	}

	public DatabaseSpecification createSpecification(Connection c) 
	throws DatabaseProductNotFoundException, SQLException
	{
		return createSpecification(c, c.getMetaData().getDatabaseProductName());
	}

	public DatabaseSpecification createSpecification(Connection c, String databaseProductName) 
	throws DatabaseProductNotFoundException
	{
		HashMap product = (HashMap)specs.get(databaseProductName);
		if (product == null) throw new DatabaseProductNotFoundException(databaseProductName);
		HashMap specmap = deepUnion(product, (HashMap)specs.get("GenericDatabaseSystem"), true);
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
