package org.netbeans.installer.infra.server.admin.servlets;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author ks152834
 * @version
 */
public class RemoveRegistry extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("command", "remove-registry");
        
        request.getRequestDispatcher("/run-command").forward(request, response);
    }
}
