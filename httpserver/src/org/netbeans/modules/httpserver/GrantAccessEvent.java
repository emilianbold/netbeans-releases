/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.httpserver;

import java.util.EventObject;
import java.net.InetAddress;

// TODO:
// The idea is that the HttpServer will have listeners
// and when request arrives, it creates this event
// for (l in listeners) {
//   l.grantAccess (accessEvent);
//   if (accessEvent.isGranted ()) {
//     ...
//     return;
//   };
// }



/** This event is sent to access listeners to
* ask them, whether the access to specified resource is
* allowed.
*
* @author Jaroslav Tulach
*/
public abstract class GrantAccessEvent extends EventObject {
  /** is access granted */
  private boolean granted = false;

  /** Creates new AccessEvent. Used only in this package by
  * the HttpServer to create new access event when a resource
  * is requested.
  *
  * @param httpServer the server 
  */
  GrantAccessEvent(HttpServerModule httpServer) {
    super (httpServer);
  }
  
  /** The Inet address that initiated the connection.
  * @return the inet address
  */
  public abstract InetAddress getClientAddress ();
  
  // JST:
  // It could be useful to also somehow describe the resource 
  // requested, like resourceString or FileObject name, etc.
  // I do not know about all details, so add it if think it is useful
  
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