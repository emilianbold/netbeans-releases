package org.netbeans.installer.infra.server.admin.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ks152834
 * @version
 */
public class AddRegistry extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("command", "add-registry");
        
        request.getRequestDispatcher("/run-command").forward(request, response);
    }
}
