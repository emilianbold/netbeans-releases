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

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;

import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverManager;

public class DriverNodeInfo extends DatabaseNodeInfo {
        
    static final long serialVersionUID =6994829681095273161L;

    private Lookup.Result driversResult = Lookup.getDefault().lookup(new Lookup.Template(JDBCDriver.class));
    private LookupListener lookupListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            try {
                getParent().refreshChildren();
            } catch (DatabaseException exc) {
                //PENDING
            }
        }
    };
    
    static int counter = 0;
    public DriverNodeInfo() {
        if (counter == 0) {
            driversResult.addLookupListener(lookupListener);
            counter ++;
        }
    }    

    public DatabaseDriver getDatabaseDriver() {
        return (DatabaseDriver)get(DatabaseNodeInfo.DBDRIVER);
    }

    public void setDatabaseDriver(DatabaseDriver drv) {
        put(DatabaseNodeInfo.NAME, drv.getName());
        put(DatabaseNodeInfo.URL, drv.getURL());
        put(DatabaseNodeInfo.PREFIX, drv.getDatabasePrefix());
//        put(DatabaseNodeInfo.ADAPTOR_CLASSNAME, drv.getDatabaseAdaptor());
        put(DatabaseNodeInfo.DBDRIVER, drv);
    }

    public void delete() throws IOException {
        JDBCDriverManager.getDefault().removeDriver(getJDBCDriver());
    }
    
    public String getIconBase() {
        return (String) ((checkDriverFiles()) ? get("iconbaseprefered") : get("iconbasepreferednotinstalled")); //NOI18N
    }

    public void setIconBase(String base) {
        if (checkDriverFiles())
            put("iconbaseprefered", base); //NOI18N
        else
            put("iconbasepreferednotinstalled", base); //NOI18N
    }

    private boolean checkDriverFiles() {
        JDBCDriver[] drvs = JDBCDriverManager.getDefault().getDriver(getURL());
        for (int i = 0; i < drvs.length; i++)
            if (drvs[i].getName().equals(getName()))
                return drvs[i].isAvailable();
        
        return false;
    }
    
    private JDBCDriver getJDBCDriver() {
        JDBCDriver[] drvs = JDBCDriverManager.getDefault().getDriver(getURL());
        for (int i = 0; i < drvs.length; i++) {
            if (drvs[i].getName().equals(getName()))
                return drvs[i];
        }
        return null;
    }
}
