/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            JDBCDriver driver = getJDBCDriver();
            if (driver != null) {
                JDBCDriverManager.getDefault().removeDriver(driver);
            }
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
        JDBCDriver driver = getJDBCDriver();
        if (driver != null) {
            return JDBCDriverSupport.isAvailable(driver);
        } else {
            return false;
        }
    }
    
    public JDBCDriver getJDBCDriver() {
        DatabaseDriver dbdrv = getDatabaseDriver();
        if (dbdrv == null) {
            return null;
        }
        return dbdrv.getJDBCDriver();
    }
}
