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

import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

import javax.servlet.ServletOutputStream;

/**
 *
 * @author Radim Kubacki
 */
public class WrapperServlet extends NbBaseServlet {

    private static final long serialVersionUID = 8009602136746998361L;
    
    /** Creates new WrapperServlet */
    public WrapperServlet () {
    }
    
    /** Processes the request for both HTTP GET and POST methods
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest (HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, java.io.IOException {
        if (!checkAccess(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                               NbBundle.getMessage(WrapperServlet.class, "MSG_HTTP_FORBIDDEN"));
            return;
        }
        // output your page here
        //String path = request.getPathInfo ();
        ServletOutputStream out = response.getOutputStream ();
        try {
            String requestURL = getRequestURL(request);
            //String requestURL = request.getRequestURL().toString(); this method is only in Servlet API 2.3
            URLMapper serverMapper = new HttpServerURLMapper();
            FileObject files[] = serverMapper.getFileObjects(new URL(requestURL));
            if ((files == null) || (files.length != 1)) {
                throw new IOException();
            }
            URL internal = URLMapper.findURL(files[0], URLMapper.INTERNAL);
            URLConnection conn = internal.openConnection();
            
            response.setContentType(conn.getContentType ());   // NOI18N
            // PENDING: copy all info - headers, length, encoding, ...
            
            InputStream in = conn.getInputStream ();
            byte [] buff = new byte [256];
            int len;

            while ((len = in.read (buff)) != -1) {
                out.write (buff, 0, len);
                out.flush();
            }
            in.close ();

        }
        catch (MalformedURLException ex) {
            try {
                response.sendError (HttpServletResponse.SC_NOT_FOUND,
                                   NbBundle.getMessage(WrapperServlet.class, "MSG_HTTP_NOT_FOUND"));
            }
            catch (IOException ex2) {}
        }
        catch (IOException ex) {
            try {
                response.sendError (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            catch (IOException ex2) {}
        }
        finally {
            try { out.close(); } catch (Exception ex) {}
        }
    }

    private String getRequestURL(HttpServletRequest request) throws UnknownHostException, MalformedURLException {
        HttpServerSettings settings = HttpServerSettings.getDefault();

        String pi = request.getPathInfo();
        if (pi.startsWith("/")) { // NOI18N
            pi = pi.substring(1);
        }
        URL reconstructedURL = new URL ("http",   // NOI18N
                              InetAddress.getLocalHost ().getHostName (), 
                              settings.getPort (),
                              settings.getWrapperBaseURL () + pi.toString());
        return reconstructedURL.toExternalForm();
    }

    /**
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return NbBundle.getMessage(WrapperServlet.class, "MSG_WrapperServletDescr");
    }

}
