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
import com.netbeans.ddl.impl.*;
import org.openide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class ConnectionNodeInfo extends DatabaseNodeInfo
implements ConnectionOperations
{
	public void connect(String dbsys)
	throws DatabaseException
	{
		String drvurl = getDriver();
		String dburl = getDatabase();
		Properties dbprops = getConnectionProperties();
		try {
			Class xxx = Class.forName(drvurl);
	    	Connection connection = DriverManager.getConnection(dburl, dbprops);
			SpecificationFactory factory = (SpecificationFactory)getSpecificationFactory();
			System.out.println("factory: "+factory);
			Specification spec;
			
			if (dbsys != null) {
				spec = (Specification)factory.createSpec(new DatabaseConnection(drvurl, dburl, getUser(), getPassword()), dbsys);
			} else spec = (Specification)factory.createSpec(new DatabaseConnection(drvurl, dburl, getUser(), getPassword()));
			setSpecification(spec);
			setConnection(connection); // fires change
		} catch (DatabaseProductNotFoundException e) {
			throw new DatabaseException("database "+e.getDatabaseProductName()+" is not supported by system");	
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void connect()
	throws DatabaseException
	{
		connect(null);
	}

	public void disconnect()
	throws DatabaseException
	{
		Connection connection = getConnection();
		if (connection != null) {
			try {
		    	connection.close();
				setConnection(null); // fires change
			} catch (Exception e) {
				throw new DatabaseException("unable to disconnect; "+e.getMessage());	
			}
	    }
	}

	public void delete()
	throws IOException
	{
		try {
			disconnect();
			Vector cons = RootNode.getOption().getConnections();
			DatabaseConnection cinfo = (DatabaseConnection)getDatabaseConnection();
			if (!cons.contains(cinfo)) throw new Exception("connection does not exist");
			cons.remove(cinfo);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
}