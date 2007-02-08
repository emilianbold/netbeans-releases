package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
public class Install extends HttpServlet {
    @EJB
    private Manager registryManager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String[] names = request.getParameterValues("registry");
            if ((names == null) || (names.length == 0)) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Please define a registry");
                
                return;
            }
            
            response.setContentType("application/x-java-jnlp-file");
            String codebase =
                    getServerUrl(request) + getServletContext().getContextPath();
            
            response.getWriter().write(registryManager.getJnlp(names, codebase));
            response.getWriter().close();
        } catch (ManagerException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(response.getWriter());
        }
    }
    
    private String getServerUrl(HttpServletRequest request) throws MalformedURLException {
        URL url = new URL(request.getRequestURL().toString());
        String string = url.toString();
        
        return string.substring(0, string.indexOf(url.getFile()));
    }
}
