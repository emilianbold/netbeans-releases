/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.action;

/**
 * This enum is used in ConnectProvider and ReconnectProvider to hold a state of connector (acceptability and searching state).
 *
 * @author David Kaspar
 */
public enum ConnectorState {

    /**
     * Accepted as a connection. Stops futher searching for a possible connection.
     */
    ACCEPT,

    /**
     * Rejected as a connection. Does not step futher seaching for a possible connection.
     */
    REJECT,

    /**
     * Rejected as a connection. Stops futher searching for a possible connection.
     */
    REJECT_AND_STOP

}
