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

package org.netbeans.modules.db.runtime;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 * This class managers the list of registered database runtimes. Database runtimes
 * encapsulate instances of a database server which can be automatically started 
 * by the IDE when a connection is being made to this server.
 *
 * @see org.netbeans.spi.db.explorer.DatabaseRuntime
 *
 * @author Nam Nguyen, Andrei Badea
 */
public final class DatabaseRuntimeManager {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseRuntimeManager.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    /**
     * The path where the runtimes are registered in the SystemFileSystem.
     */
    private static final String RUNTIMES_PATH = "Databases/Runtimes"; // NOI18N
    
    /**
     * The singleton database runtime manager instance.
     */
    private static DatabaseRuntimeManager DEFAULT = null;
    
    /**
     * The Lookup.Result instance containing all the DatabaseRuntime instances.
     */
    private Lookup.Result result = getLookupResult();
    
    /**
     * Returns the singleton database runtime manager instance.
     */
    public static synchronized DatabaseRuntimeManager getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new DatabaseRuntimeManager();
        }
        return DEFAULT;
    }
    
    public DatabaseRuntime[] getRuntimes() {
        Collection runtimes = result.allInstances();
        return (DatabaseRuntime[])runtimes.toArray(new DatabaseRuntime[runtimes.size()]);
    }
    
    /**
     * Returns the runtimes registered for the specified JDBC driver.
     *
     * @param jdbcDriverClassName the JDBC driver to search for; must not be null.
     *
     * @return the runtime registered for the specified JDBC driver or null
     *         if no runtime is registered for this driver.
     *
     * @throws NullPointerException if the specified JDBC driver is null.
     */
    public DatabaseRuntime[] getRuntimes(String jdbcDriverClassName) {
        if (jdbcDriverClassName == null) {
            throw new NullPointerException();
        }
        List/*<DatabaseRuntime>*/ runtimeList = new LinkedList();
        for (Iterator i = result.allInstances().iterator(); i.hasNext();) {
            DatabaseRuntime runtime = (DatabaseRuntime)i.next();
            if (LOG) {
                LOGGER.log(Level.FINE, "Runtime: " + runtime.getClass().getName() + " for driver " + runtime.getJDBCDriverClass()); // NOI18N
            }
            if (jdbcDriverClassName.equals(runtime.getJDBCDriverClass())) {
                runtimeList.add(runtime);
            }
        }
        return (DatabaseRuntime[])runtimeList.toArray(new DatabaseRuntime[runtimeList.size()]);
    }
    
    private synchronized Lookup.Result getLookupResult() {
        return Lookups.forPath(RUNTIMES_PATH).lookupResult(DatabaseRuntime.class);
    }
}
