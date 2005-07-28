/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarFile;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.driver.JDBCDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverManager;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.util.DriverListUtil;

/** Root system option. It stores a list of available drivers and open connections.
* These connections will be restored at startup, drivers will be placed in Drivers
* directory owned by Database node.
*/
public class DatabaseOption extends SystemOption {
    
    private static boolean debugMode;
    private static Vector drivers;
    private static Vector connections;
    private static int fetchlimit = 100;
    private static int fetchstep = 200;
    private static boolean autoConn = true;

    public static final String PROP_DEBUG_MODE = "debugMode"; //NOI18N
    public static final String PROP_FETCH_LIMIT = "fetchLimit"; //NOI18N
    public static final String PROP_FETCH_STEP = "fetchStep"; //NOI18N
    public static final String PROP_AUTO_CONNECTION = "autoConn"; //NOI18N

    static final long serialVersionUID =-13629330831657810L;
    
    public DatabaseOption() {
        super();
        drivers = new Vector();
        connections = new Vector();
        debugMode = false;
        
        deleteAdaptorsFolder();
    }

    /** Returns vector of registered drivers */
    public Vector getAvailableDrivers() {
        if (drivers.size() == 0) {
            //get serialized drivers
            Map xxx = (Map) DatabaseNodeInfo.getGlobalNodeInfo(DatabaseNode.DRIVER_LIST);
            drivers = createDrivers(xxx);
        }
        
        return drivers;
    }

    public boolean getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean flag) {
        if (debugMode == flag)
            return;
        
        debugMode = flag;
        firePropertyChange(PROP_DEBUG_MODE, !debugMode ? Boolean.TRUE : Boolean.FALSE, debugMode ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Sets vector of available drivers.
    * @param c Vector with drivers
    */
    public void setAvailableDrivers(Vector c) {
        drivers = c;
    }

    /** Returns vector of saved connections */
    public Vector getConnections() {
        if (connections == null)
            connections = new Vector();

        return connections;
    }

    /** Sets vector of open connections.
    * @param c Vector with connections
    */
    public void setConnections(Vector c) {
        connections = c;
    }

    public int getFetchLimit() {
        return fetchlimit;
    }

    public void setFetchLimit(int limit) {
        int old = fetchlimit;
        if (old == limit)
            return;
        
        fetchlimit = limit;
        firePropertyChange(PROP_FETCH_LIMIT, new Integer(old), new Integer(limit));
    }

    public int getFetchStep() {
        return fetchstep;
    }

    public void setFetchStep(int limit) {
        int old = fetchstep;
        if (old == limit)
            return;
        
        fetchstep = limit;
        firePropertyChange(PROP_FETCH_STEP, new Integer(old), new Integer(limit));
    }

    public boolean isAutoConn() {
        return autoConn;
    }
    
    public void setAutoConn(boolean newAutoConn) {
        boolean old = autoConn;
        if (old == newAutoConn)
            return;
        
        autoConn = newAutoConn;
        firePropertyChange(PROP_AUTO_CONNECTION, !autoConn ? Boolean.TRUE : Boolean.FALSE, autoConn ? Boolean.TRUE : Boolean.FALSE);
    }

    public void save() {
        firePropertyChange(null, null, null);
    }

    /** Name of the option */
    public String displayName() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("OptionName"); //NOI18N
    }

    /** Description of object */
    public String toString() {
        return drivers.size() + " drivers, " + connections.size() + " connections"; //NOI18N
    }
    
    /** Writes data
    * @param out ObjectOutputStream
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeObject(null);
        out.writeObject(getConnections());
        out.writeInt(fetchlimit);
    }

    /** Reads data
    * @param in ObjectInputStream
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        drivers = (Vector) in.readObject();
        if (drivers != null)
            lookForDrivers();

        connections = (Vector) in.readObject();
        fetchlimit = in.readInt();
    }
        
    private Vector createDrivers(Map drvMap) {
        Vector def = (Vector) drvMap.get("defaultdriverlist"); //NOI18N
        Vector rvec = null;
        if (def != null && def.size() > 0) {
            rvec = new Vector(def.size());
            Enumeration defe = def.elements();
            while (defe.hasMoreElements()) {
                Object rit = defe.nextElement();
                String name = (String) ((Map)rit).get("name"); //NOI18N
                String drv = (String) ((Map)rit).get("driver"); //NOI18N
                String prefix = (String) ((Map)rit).get("prefix"); //NOI18N
                String adaptor = (String) ((Map)rit).get("adaptor"); //NOI18N
                rit = new DatabaseDriver(name, drv, prefix, adaptor);
                if (rit != null)
                    rvec.add(rit);
            }
        } else
            rvec = new Vector();
        
        return rvec;
    }
    
    private void lookForDrivers() {
        StringBuffer sb = new StringBuffer();
        sb.append(File.separator);
        sb.append("lib");
        sb.append(File.separator);
        sb.append("ext");
        String libext = sb.toString();        
        String nbhome = System.getProperty("netbeans.home");
        
        preinstallDrivers(nbhome + libext);
    }
    
    private void preinstallDrivers(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory())
            return;
        
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".zip")); //NOI18N
            }
        });
        
        for (int i = 0; i < files.length; i++) {
            JarFile jf;
            String drv;

            try {
                jf = new JarFile(files[i]);
                Set drvs = DriverListUtil.getDrivers();
                Iterator it = drvs.iterator();
                while (it.hasNext()) {
                    drv = (String) it.next();
                    if (jf.getEntry(drv.replace('.', '/') + ".class") != null) {//NOI18N
                        JDBCDriver driver = new JDBCDriver(DriverListUtil.findFreeName(DriverListUtil.getName(drv)), drv, new URL[] {files[i].toURL()});
                        JDBCDriverManager.getDefault().addDriver(driver);
                    }
                }
                jf.close();
            } catch (IOException exc) {
                //PENDING
            }
        }
    }
    
    private void deleteAdaptorsFolder() {    
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Database"); //NOI18N
        try {
            if (fo != null)
                fo.delete();
        } catch (IOException exc) {
            //delete action failed - ignore
        }
    }
}
