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
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverSupport;

public class DriverNodeInfo extends DatabaseNodeInfo {
        
    static final long serialVersionUID =6994829681095273161L;
   
    public DriverNodeInfo() {
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
        try {
            JDBCDriverManager.getDefault().removeDriver(getJDBCDriver());
        } catch (DatabaseException e) {
            // PENDING
        }
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
        JDBCDriver[] drvs = JDBCDriverManager.getDefault().getDrivers(getURL());
        for (int i = 0; i < drvs.length; i++)
            if (drvs[i].getName().equals(getName()))
                return JDBCDriverSupport.isAvailable(drvs[i]);
        
        return false;
    }
    
    private JDBCDriver getJDBCDriver() {
        JDBCDriver[] drvs = JDBCDriverManager.getDefault().getDrivers(getURL());
        for (int i = 0; i < drvs.length; i++) {
            if (drvs[i].getName().equals(getName()))
                return drvs[i];
        }
        return null;
    }
}
