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

import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.sql.*;
import com.netbeans.ddl.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import org.openide.nodes.Node;
import com.netbeans.ddl.util.PListReader;
import com.netbeans.ddl.impl.*;
import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.DatabaseNode;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;
import com.netbeans.enterprise.modules.db.explorer.DatabaseDriver;
import com.netbeans.enterprise.modules.db.explorer.nodes.RootNode;

public class DriverNodeInfo extends DriverListNodeInfo 
{
	public DatabaseDriver getDatabaseDriver()
	{
		return (DatabaseDriver)get(DatabaseNodeInfo.DBDRIVER);
	}

	public void setDatabaseDriver(DatabaseDriver drv)
	{
		put(DatabaseNodeInfo.NAME, drv.getName());
		put(DatabaseNodeInfo.URL, drv.getURL());
		put(DatabaseNodeInfo.PREFIX, drv.getDatabasePrefix());
		put(DatabaseNodeInfo.DBDRIVER, drv);
	}

	public void delete()
	throws IOException
	{
		try {
			DatabaseDriver drv = getDatabaseDriver();
			Vector drvs = RootNode.getOption().getAvailableDrivers();
			int idx = drvs.indexOf(drv);
			if (idx != -1) drvs.removeElementAt(idx);
			else throw new DatabaseException("driver "+drv+" was not found");
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
}