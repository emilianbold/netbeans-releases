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

package org.netbeans.spi.db.explorer;

/**
 * Represents a database server instance, typically a local installation which can be
 * started when a connection to this server is being made, or stopped upon shutdown.
 *
 * <p>Implementations of this class should be put in the Databases/Runtimes folder
 * in the default filesystem.</p>
 *
 * @author Nam Nguyen, Andrei Badea
 */
public interface DatabaseRuntime {
    
    /**
     * Returns the JDBC driver class which is used to make connections to the 
     * represented database server.
     *
     * <p>When a connection is being made, only the database runtimes which have
     * the same JDBC driver as the driver used by this connection are considered 
     * for further usage (e.g., starting the database server).</p>
     * 
     * @return the fully-qualified class name of the driver used to make
     * connections to the represented database server.
     */
    public String getJDBCDriverClass();
    
    /**
     * Returns whether this runtime accepts this connection url (the connection url
     * would cause a connection to be made to the database server instance 
     * represented by this runtime).
     *
     * @param url the connection url
     * 
     * @return true if the runtime accepts this connection url; false otherwise.
     */
    boolean acceptsConnectionUrl(String url);
    
    /**
     * Returns the state (running/not running) of the represented database server.
     *
     * @return true if the database server is running; false otherwise.
     */
    boolean isRunning();
    
    /**
     * Returns whether the database server can be started by a call to the 
     * {@link #start} method.
     *
     * @return true if the database server can be started; false
     * otherwise.
     */
    public boolean canStart();

    /**
     * Start the database server instance represented by this runtime.
     */
    void start();
    
    /**
     * Start the database server instance represented by this runtime.
     */
    void stop();
}
