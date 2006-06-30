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

package org.netbeans.modules.httpserver;

import java.util.EventObject;
import java.net.InetAddress;

/** This event is sent to access listeners to
* ask them, whether the access to specified resource is
* allowed.
*
* @author Jaroslav Tulach
*/
public class GrantAccessEvent extends EventObject {
    /** is access granted */
    private boolean granted = false;
    private InetAddress clientAddress;
    private String resource;

    /** Creates new AccessEvent. Used only in this package by
    * the HttpServer to create new access event when a resource
    * is requested.
    *
    * @param httpServer the server 
    */
    GrantAccessEvent(Object source, InetAddress clientAddress, String resource) {
        super (source);
        this.clientAddress = clientAddress;
        this.resource = resource;
    }

    /** The Inet address that initiated the connection.
    * @return the inet address
    */
    public InetAddress getClientAddress () {
        return clientAddress;
    }

    /** The resource to which access is requested */
    public String getResource() {
        return resource;
    }

    /** Allows access. The listener can use this method to grant
    * access the client and resource.
    */
    public void grantAccess () {
        granted = true;
    }

    /** Getter to test whether the access has been granted.
    * @return true if a listener granted the access
    */
    boolean isGranted () {
        return granted;
    }
}
