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
import java.io.*;
import java.lang.reflect.*;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.*;
import org.openide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;
import com.netbeans.enterprise.modules.db.explorer.dlg.UnsupportedDatabaseDialog;
import org.openide.TopManager;

public class ConnectionNodeInfo extends DatabaseNodeInfo
implements ConnectionOperations
{
  static final long serialVersionUID =-8322295510950137669L;
	public void connect(String dbsys)
	throws DatabaseException
	{
		String drvurl = getDriver();
		String dburl = getDatabase();
		
		Properties dbprops = getConnectionProperties();
		try {

			DatabaseConnection con = new DatabaseConnection(drvurl, dburl, getUser(), getPassword());
			Connection connection = con.createJDBCConnection();
			SpecificationFactory factory = (SpecificationFactory)getSpecificationFactory();
			Specification spec;
			DrvSpecification drvSpec;
      
			if (dbsys != null) {
				spec = (Specification)factory.createSpecification(con, dbsys, connection);
			} else spec = (Specification)factory.createSpecification(con, connection);
			setSpecification(spec);
      
      drvSpec = (DrvSpecification) factory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
			setDriverSpecification(drvSpec);
      
			setConnection(connection); // fires change
		} catch (DatabaseProductNotFoundException e) {
			
			UnsupportedDatabaseDialog dlg = new UnsupportedDatabaseDialog();
			dlg.show();
			switch (dlg.getResult()) {
				case UnsupportedDatabaseDialog.GENERIC: connect("GenericDatabaseSystem"); break;
				case UnsupportedDatabaseDialog.READONLY: connectReadOnly(); break;
				default: return;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void connect()
	throws DatabaseException
	{
		connect(null);
	}

	public void connectReadOnly()
	throws DatabaseException
	{
		setReadOnly(true);
		connect("GenericDatabaseSystem");
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
			if (cons.contains(cinfo)) cons.remove(cinfo);
//			throw new Exception("connection does not exist");
//			cons.remove(cinfo);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
}
/*
 * <<Log>>
 *  15   Gandalf   1.14        12/15/99 Radko Najman    driver adaptor
 *  14   Gandalf   1.13        11/27/99 Patrik Knakal   
 *  13   Gandalf   1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        9/23/99  Slavek Psenicka Bug #3311
 *  11   Gandalf   1.10        9/13/99  Slavek Psenicka 
 *  10   Gandalf   1.9         9/13/99  Slavek Psenicka 
 *  9    Gandalf   1.8         9/2/99   Slavek Psenicka Unsupported database and
 *       readonly feature
 *  8    Gandalf   1.7         7/21/99  Slavek Psenicka debug log
 *  7    Gandalf   1.6         6/15/99  Slavek Psenicka move to 
 *       live-connection-aware model
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/21/99  Slavek Psenicka new version
 *  4    Gandalf   1.3         5/14/99  Slavek Psenicka new version
 *  3    Gandalf   1.2         4/23/99  Slavek Psenicka Chyba createSpec pri 
 *       ConnectAs
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka Debug mode
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
