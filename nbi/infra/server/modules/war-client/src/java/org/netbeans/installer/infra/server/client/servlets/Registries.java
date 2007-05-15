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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.infra.server.ejb.ManagerException;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class Registries extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; encoding=UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            
            List<String> registries = manager.getRegistries();
            
            
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
            out.println("<html>");
            out.println("    <head>");
            out.println("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
            out.println("        <title>Registries</title>");
            out.println("        <link rel=\"stylesheet\" href=\"admin/css/main.css\" type=\"text/css\"/>");
            out.println("        <script src=\"js/main.js\" type=\"text/javascript\"></script>");
            out.println("    </head>");
            out.println("    <body>");
            
            out.println("        <p>");
            out.println("            Please select the registries that you would like to work with. The either click Install to immediately launch the installer, or Next to select the components and create a bundle.");
            out.println("        </p>");
            
            out.println("        <form name=\"Form\" method=\"get\">");
            for (String registry: registries) {
                out.println("            <input type=\"checkbox\" name=\"registry\" value=\"" + registry + "\" checked/> " + registry + "<br/>");
            }
            out.println("            <br/><br/>");
            out.println("            <input type=\"button\" value=\"Install Now\" onclick=\"install_now()\">&nbsp;&nbsp;");
            out.println("            <input type=\"button\" value=\"Next &gt;\" onclick=\"create_bundle()\">");
            out.println("        </form>");
            
            
            out.println("    </body>");
            out.println("</html>");
        } catch (ManagerException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(response.getWriter());
        }
        
        out.close();
    }
}
