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

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.util.*;
import java.sql.SQLException;
import com.netbeans.ide.*;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.options.SystemOption;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;

/** Root system option. It stores a list of available drivers and open connections.
* These connections will be restored at startup, drivers will be placed in Drivers
* directory owned by Database node.
*
* @author Slavek Psenicka
*/
public class DatabaseOption extends SystemOption 
{
	private static Vector drivers;
	private static Vector connections;

	public DatabaseOption()
	{
		super();
		drivers = new Vector();
		connections = new Vector();
	}	

	/** Returns vector of registered drivers */
	public Vector getAvailableDrivers() 
	{
		Vector rvec = null;
		if (drivers.size() == 0) {			
			Map xxx = (Map)DatabaseNodeInfo.getGlobalNodeInfo(DatabaseNode.DRIVER_LIST);
			Vector def = (Vector)xxx.get("defaultdriverlist");
			if (def != null && def.size()>0) {
				rvec = new Vector(def.size());
				Enumeration defe = def.elements();
				while(defe.hasMoreElements()) {
					Object rit = defe.nextElement();
					if (rit instanceof Map) rit = new DatabaseDriver((String)((Map)rit).get("name"), (String)((Map)rit).get("driver"));
					if (rit != null) rvec.add(rit);
				}				
			} else rvec = new Vector();
			drivers = rvec;
		}
    	return drivers;
  	}

	/** Sets vector of available drivers.
	* @param c Vector with drivers
	*/
	public void setAvailableDrivers(Vector c) 
	{
    	drivers = c;
  	}

	/** Returns vector of saved connections */
	public Vector getConnections() 
	{
		if (connections == null) connections = new Vector();
    	return connections;
  	}

	/** Sets vector of open connections.
	* @param c Vector with connections
	*/
	public void setConnections(Vector c) 
	{
    	connections = c;
  	}

	/** Name of the option */
	public String displayName() 
	{
    	return "DBNode connection options";
  	}

	/** Description of object */
	public String toString()
	{
		return drivers.size()+" drivers, "+connections.size()+" connections";
	}

	/** Writes data
	* @param out ObjectOutputStream
	*/
  	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeObject(getAvailableDrivers());
		out.writeObject(getConnections());
	}
	
	/** Reads data
	* @param in ObjectInputStream
	*/
 	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		drivers = (Vector)in.readObject();
		connections = (Vector)in.readObject();
	}
}

/*
 * <<Log>>
 */
