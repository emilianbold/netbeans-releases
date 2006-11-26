package org.netbeans.installer.infra.server.admin.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class UpdateEngine extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("command", "update-engine");
        
        request.getRequestDispatcher("/run-command").forward(request, response);
    }
}
