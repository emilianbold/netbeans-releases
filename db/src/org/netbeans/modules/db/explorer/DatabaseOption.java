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

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.util.*;
import java.sql.SQLException;
import org.openide.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.options.SystemOption;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;

/** Root system option. It stores a list of available drivers and open connections.
* These connections will be restored at startup, drivers will be placed in Drivers
* directory owned by Database node.
*
* @author Slavek Psenicka
*/
public class DatabaseOption extends SystemOption
{
    private static boolean debugMode;
    private static Vector drivers;
    private static Vector connections;
    private static int fetchlimit = 100;
    private static int fetchstep = 200;

    public static final String PROP_DEBUG_MODE = "debugMode";
    public static final String PROP_FETCH_LIMIT = "fetchLimit";
    public static final String PROP_FETCH_STEP = "fetchStep";

    static final long serialVersionUID =-13629330831657810L;
    public DatabaseOption()
    {
        super();
        drivers = new Vector();
        connections = new Vector();
        debugMode = false;
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
                    String name = (String)((Map)rit).get("name");
                    String drv = (String)((Map)rit).get("driver");
                    String prefix = (String)((Map)rit).get("prefix");
                    String adaptor = (String)((Map)rit).get("adaptor");
                    rit = new DatabaseDriver(name, drv, prefix, adaptor);
                    if (rit != null) rvec.add(rit);
                }
            } else rvec = new Vector();
            drivers = rvec;
        }
        return drivers;
    }

    public boolean getDebugMode()
    {
        return debugMode;
    }

    public void setDebugMode(boolean flag)
    {
        if (debugMode == flag) return;
        debugMode = flag;
        firePropertyChange(PROP_DEBUG_MODE, new Boolean(!debugMode), new Boolean(debugMode));
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
        if (connections == null)
            connections = new Vector();
        
        return connections;
    }

    /** Sets vector of open connections.
    * @param c Vector with connections
    */
    public void setConnections(Vector c)
    {
        connections = c;
    }

    public int getFetchLimit()
    {
        return fetchlimit;
    }

    public void setFetchLimit(int limit)
    {
        int old = fetchlimit;
        if (old == limit) return;
        fetchlimit = limit;
        firePropertyChange(PROP_FETCH_LIMIT, new Integer(old), new Integer(limit));
    }

    public int getFetchStep()
    {
        return fetchstep;
    }

    public void setFetchStep(int limit)
    {
        int old = fetchstep;
        if (old == limit) return;
        fetchstep = limit;
        firePropertyChange(PROP_FETCH_STEP, new Integer(old), new Integer(limit));
    }

    /** Name of the option */
    public String displayName()
    {
        return "Database Explorer";
    }

    /** Description of object */
    public String toString()
    {
        return drivers.size()+" drivers, "+connections.size()+" connections";
    }
    
    private void closeConnections() {
        try {
            Node n[] = TopManager.getDefault().getPlaces().nodes().environment().getChildren().findChild("Databases").getChildren().getNodes();
            for (int i = 0; i < n.length; i++)
                if (n[i] instanceof ConnectionNode)
                    ((ConnectionNodeInfo)((ConnectionNode)n[i]).getInfo()).disconnect();
        } catch (Exception exc) {
            //connection not closed
        }
    }

    /** Writes data
    * @param out ObjectOutputStream
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        closeConnections();
        
        out.writeObject(getAvailableDrivers());
        out.writeObject(getConnections());
        out.writeInt(fetchlimit);
    }

    /** Reads data
    * @param in ObjectInputStream
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        drivers = (Vector)in.readObject();
        connections = (Vector)in.readObject();
        fetchlimit = in.readInt();
    }
}

/*
 * <<Log>>
 */
