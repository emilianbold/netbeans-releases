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

/*
 * ServerDebugInfo.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

/**
 * Class to communicate the debugging information between plugin, server api and IDE.
 * @author Martin Grebac
 * @version 0.1
 */

public class ServerDebugInfo {

    public static final String TRANSPORT_SOCKET = "dt_socket";  //NOI18N
    public static final String TRANSPORT_SHMEM = "dt_shmem";    //NOI18N

    /**
     * Holds value of property transport - socket or shared memory.
     */
    private String transport = TRANSPORT_SOCKET;

    /**
     * Holds value of property host - where the target vm is running.
     */
    private String host = "localhost";                          //NOI18N
    
    /**
     * Holds value of property shmemName - shared memory name of the target vm.
     */
    private String shmemName = "";
    
    /**
     * Holds value of property port - port number of the target vm..
     */
    private int port;
    
    public ServerDebugInfo(String host, String shmemName) {
        setTransport(TRANSPORT_SHMEM);
        setHost(host);
        setShmemName(shmemName);
    }
    
    public ServerDebugInfo(String host, int port) {
        setTransport(TRANSPORT_SOCKET);
        setHost(host);
        setPort(port);
    }
    
    /**
     * Getter for property transport.
     * @return Value of property transport.
     */
    public String getTransport() {
        return this.transport;
    }
    
    /**
     * Setter for property transport.
     * @param transport New value of property transport.
     */
    public void setTransport(String transport) {
        this.transport = transport;
    }
    
    /**
     * Getter for property host.
     * @return Value of property host.
     */
    public String getHost() {
        return this.host;
    }
    
    /**
     * Setter for property host.
     * @param host New value of property host.
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Getter for property address.
     * @return Value of property address.
     */
    public String getShmemName() {
        return this.shmemName;
    }
    
    /**
     * Setter for property address.
     * @param address New value of property address.
     */
    public void setShmemName(String shmemName) {
        this.shmemName = shmemName;
    }
    
    /**
     * Getter for property port.
     * @return Value of property port.
     */
    public int getPort() {
        return this.port;
    }
    
    /**
     * Setter for property port.
     * @param port New value of property port.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
}
