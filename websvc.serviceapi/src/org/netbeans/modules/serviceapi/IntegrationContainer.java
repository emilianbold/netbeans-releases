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
package org.netbeans.modules.serviceapi;

import java.util.Collection;

/**
 * Represent service module container, with information about 
 * service provider-consumer relationships.
 *
 * @author Nam Nguyen
 * @author Chris Webster
 * @author Jiri Kopsa
 */
public abstract class IntegrationContainer extends ServiceModuleContainer {
    /**
     * Property event for service connection in this container.
     */
    public static final String SERVICE_CONNECTION_ADDED_PROPERTY = "sericeConnectionAdded";
    public static final String SERVICE_CONNECTION_REMOVED_PROPERTY = "sericeConnectionRemoved";
    
    /**
     * @return list of service provider-consumer connections.
     */
    public abstract Collection<ServiceConnection> getServiceConnections();

    /**
     * Creates service connection from given service interface.
     * @return a connection between the two endpoints; or null if the 
     * given service interfaces don't match.
     * @param consumer the consumer service interface
     * @param provider the provider service interface
     */
    public abstract ServiceConnection createConnection(
            ServiceInterface consumer, ServiceInterface provider);
    
    /**
     * Removes the service connection.  Note that the service interfaces still 
     * exist in the containing service module.
     *
     * @return true if the connection is removed successfully; otherwise return false.
     * @param connection the service connection to remove.
     */
    public abstract boolean removeConnection(ServiceConnection connection);
}
