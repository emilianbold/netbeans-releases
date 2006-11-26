package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.RegistryManager;
import org.netbeans.installer.utils.StreamUtils;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class GetFile extends HttpServlet {
    @EJB
    private RegistryManager registryManager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/octet-stream");
        
        final String registry     = request.getParameter("registry");
        final String file         = request.getParameter("file");
        
        final InputStream  input  = registryManager.getRegistryFile(registry, file);
        final OutputStream output = response.getOutputStream();
        
        StreamUtils.transferData(input, output);
        
        input.close();
        output.close();
    }
}
