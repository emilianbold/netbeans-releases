/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.httpserver;

import java.io.IOException;
import java.util.HashSet;
import java.net.InetAddress;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.util.SharedClassObject;

/** Base servlet for servlets which access NetBeans Open APIs
*
* @author Petr Jiricka
* @version 0.11 May 5, 1999
*/
public abstract class NbBaseServlet extends HttpServlet {

    /** Initializes the servlet. */
    public void init() throws ServletException {
    }

    /** Processes the request for both HTTP GET and POST methods
    * @param request servlet request
    * @param response servlet response
    */
    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException;

    /** Performs the HTTP GET operation.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /** Performs the HTTP POST operation.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /**
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return NbBundle.getBundle(NbBaseServlet.class).getString("MSG_BaseServletDescr");
    }

    /** Checks whether access should be permitted according to HTTP Server module access settings
    * (localhost/anyhost, granted addesses)
    *  @return true if access is granted
    */
    protected boolean checkAccess(HttpServletRequest request) throws IOException {

        HttpServerSettings settings = (HttpServerSettings)SharedClassObject.findObject (HttpServerSettings.class);
        if (settings == null)
            return false;

        if (settings.getHostProperty ().getHost ().equals(HttpServerSettings.ANYHOST))
            return true;

        HashSet hs = settings.getGrantedAddressesSet();

        if (hs.contains(request.getRemoteAddr().trim()))
            return true;

        String pathI = request.getPathInfo();
        if (pathI == null)
            pathI = "";      // NOI18N
        // ask user
        try {
            String address = request.getRemoteAddr().trim();
            if (settings.allowAccess(InetAddress.getByName(address), pathI)) return true;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }

        return false;
    }

}
