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

package org.netbeans.modules.web.monitor.server;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * MonitorExtraActions.java
 *
 *
 * Created: Wed Oct 17 17:16:22 2001
 *
 * @author Ana von Klopp
 * @version
 */

/**
 * Containers who wish to provide servlet information and/or ability
 * to reset the session cookie to the HTTP Monitor must implement one
 * or both methods from this interface. 
 */
public interface MonitorExtraActions {
    

    /** 
     * This method returns a handle on the servlet that processes the
     * request. 
     */
    public Servlet getServlet(HttpServletRequest request, 
				       FilterChain chain);
         

    /**
     * This method evaluates the cookies that come in through the
     * headers for a JSESSIONID cookie. If such a cookie is present,
     * the method replaces the current session with the session
     * corresponding to the ID from the cookie, if the session is
     * still present. If the session no longer exists, or if the
     * request did not include a session cookie, any existing session
     * will no longer associated with the request. 
     */
    public void replaceSessionID(HttpServletRequest request); 


    public boolean canReplaceSessionID();
    
}

    

   
    
