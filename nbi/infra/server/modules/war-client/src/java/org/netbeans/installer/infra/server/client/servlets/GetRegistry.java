package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.RegistryManager;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class GetRegistry extends HttpServlet {
    @EJB
    private RegistryManager registryManager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/xml");
        
        String registry = request.getParameter("registry");
        
        response.getWriter().write(registryManager.getRegistry(registry));
        response.getWriter().close();
    }
}
