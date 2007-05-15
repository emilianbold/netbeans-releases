/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.infra.server.client.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.ejb.EJB;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.infra.server.ejb.ManagerException;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class GetEngine extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            File engine = manager.getEngine();
            
            response.setContentType("application/java-archive");
            
            response.setHeader("Content-Disposition",
                    "attachment; filename=nbi-engine.jar");
            response.setHeader("Content-Length",
                    Long.toString(engine.length()));
            response.setHeader("Last-Modified",
                    StringUtils.httpFormat(new Date(engine.lastModified())));
            
            final InputStream  input  = new FileInputStream(engine);
            final OutputStream output = response.getOutputStream();
            
            StreamUtils.transferData(input, output);
            
            input.close();
            output.close();
        } catch (ManagerException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            e.printStackTrace(response.getWriter());
        }
    }
}
