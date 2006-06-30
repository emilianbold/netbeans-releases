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

package org.netbeans.spi.db.explorer;

/**
 * Represents an instance of a database server, typically a local installation which can be
 * started when a connection to this server is being made, or stopped upon IDE shutdown.
 *
 * <p>Implementations of this class should be put in the Databases/Runtimes folder
 * in the default filesystem.</p>
 *
 * @author Nam Nguyen, Andrei Badea
 */
public interface DatabaseRuntime {

    /**
     * Returns the JDBC driver class which is used to make connections to the
     * represented database server instance.
     *
     * <p>When a connection is being made, only the database runtimes which have
     * the same JDBC driver as the driver used by this connection are considered 
     * for further usage (e.g., starting the database server instance).</p>
     * 
     * @return the fully-qualified class name of the driver used to make
     * connections to the represented database server instance.
     */
    public String getJDBCDriverClass();
    
    /**
     * Returns whether this runtime accepts this database URL (the database URL
     * would cause a connection to be made to the database server instance 
     * represented by this runtime).
     *
     * @param url the database URL
     * 
     * @return true if the runtime accepts this database URL; false otherwise.
     */
    boolean acceptsDatabaseURL(String url);
    
    /**
     * Returns the state (running/not running) of the represented database server
     * instance.
     *
     * @return true if the database server instance is running; false otherwise.
     */
    boolean isRunning();
    
    /**
     * Returns whether the database server instance can be started by a call to the 
     * {@link #start} method.
     *
     * @return true if the database server instance can be started; false
     * otherwise.
     */
    public boolean canStart();

    /**
     * Starts the database server instance represented by this runtime.
     */
    void start();
    
    /**
     * Stops the database server instance represented by this runtime.
     */
    void stop();
}
