/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.runtime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;


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
    
    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance(DatabaseRuntimeManager.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);
    
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
                LOGGER.log(ErrorManager.INFORMATIONAL, "Runtime: " + runtime.getClass().getName() + " for driver " + runtime.getJDBCDriverClass()); // NOI18N
            }
            if (jdbcDriverClassName.equals(runtime.getJDBCDriverClass())) {
                runtimeList.add(runtime);
            }
        }
        return (DatabaseRuntime[])runtimeList.toArray(new DatabaseRuntime[runtimeList.size()]);
    }
    
    /**
     * Stops the running runtimes.
     */
    public void stopRuntimes() {
        for (Iterator i = result.allInstances().iterator(); i.hasNext();) {
            DatabaseRuntime runtime = (DatabaseRuntime)i.next();
            if (runtime.isRunning()) {
                runtime.stop();
            }
        }
    }
    
    private synchronized Lookup.Result getLookupResult() {
        if (result == null) {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(RUNTIMES_PATH);
            DataFolder folder = DataFolder.findFolder(fo);
            result = new FolderLookup(folder).getLookup().lookup(new Lookup.Template(DatabaseRuntime.class));
        }
        return result;
    }
}
