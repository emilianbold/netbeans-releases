/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.xml.cookies;

/**
 * Cookie observer interface. It can be implemented by cookie clients that
 * are insterested in progress of their request.
 *
 * @author      Petr Kuzel
 * @deprecated  XML Tools API candidate
 * @since       0.3
 */
public interface CookieObserver {

    /**
     * Receive a cookie message. Implementation (handling code) must not
     * invoke directly or indirecly any source cookie method.
     * Implementation should be as fast as possible.
     * @param msg Received cookie message never <code>null</code>.
     */
    public void receive(CookieMessage msg);
        
}
