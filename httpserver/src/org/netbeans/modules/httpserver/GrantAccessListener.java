/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.httpserver;

import java.util.EventListener;

/** Listener that can grant access when a client is asking for a
* resource.
*
* @author Jaroslav Tulach
*/
public interface GrantAccessListener extends EventListener {
    /** Listener is notified about a request from a client and
    * can grant access to specified resource. This can be done
    * by calling <CODE>ev.grantAccess ()</CODE>.
    *
    * @param ev the event describing the request
    */
    public void grantAccess (GrantAccessEvent ev);
}
