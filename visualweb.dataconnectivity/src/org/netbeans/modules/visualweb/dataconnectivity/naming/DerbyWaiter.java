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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.dataconnectivity.naming;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverListener;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.util.Exceptions;


/**
 * Waits for the JDBCDriverManager to register the Derby driver
 * @author John Baker
 */
public class DerbyWaiter {

    private static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N[

    private boolean registered;       
    
    private final JDBCDriverListener jdbcDriverListener = new JDBCDriverListener() {
        public void driversChanged() {
            registerConnections();
        }
    };
    
    public DerbyWaiter() {
        JDBCDriverManager.getDefault().addDriverListener(jdbcDriverListener);
    }
    
    private synchronized void registerConnections() {
        if (registered) {
            return;
        }
        JDBCDriver[] drvsArray = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET);
        if (drvsArray.length > 0) {
            DatabaseSettingsImporter.getInstance().locateAndRegisterDrivers();
            DatabaseSettingsImporter.getInstance().locateAndRegisterConnections();
            registered = true;
            JDBCDriverManager.getDefault().removeDriverListener(jdbcDriverListener);            
        }
    }
}

  

