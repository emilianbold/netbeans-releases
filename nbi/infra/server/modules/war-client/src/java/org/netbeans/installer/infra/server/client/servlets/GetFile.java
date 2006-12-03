package org.netbeans.installer.infra.server.client.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.RegistryManager;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class GetFile extends HttpServlet {
    @EJB
    private RegistryManager registryManager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String registry     = request.getParameter("registry");
        final String path         = request.getParameter("file");
        
        final File   file         = registryManager.getRegistryFile(registry, path);
        
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        response.setHeader("Content-Length", Long.toString(file.length()));
        response.setHeader("Last-Modified", StringUtils.httpFormat(new Date(file.lastModified())));
        
        final InputStream  input  = new FileInputStream(file);
        final OutputStream output = response.getOutputStream();
        
        StreamUtils.transferData(input, output);
        
        input.close();
        output.close();
    }
}
