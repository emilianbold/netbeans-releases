package org.netbeans.installer.infra.server.client.servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.infra.server.ejb.ManagerException;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class GetFile extends HttpServlet {
    @EJB
    private Manager manager;
    
    @Override
    protected void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException {
        try {
            final String registry = request.getParameter("registry");
            final String path = request.getParameter("file");
            
            final File file = manager.getFile(registry, path);
            final String filename = file.getName();
            
            final OutputStream output = response.getOutputStream();
            
            response.setContentType(
                    "application/octet-stream");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=" + filename);
            response.setHeader(
                    "Last-Modified",
                    StringUtils.httpFormat(new Date(file.lastModified())));
            response.setHeader(
                    "Accept-Ranges",
                    "bytes");
            
            try {
                Utils.transfer(request, response, output, file);
            } finally {
                output.close();
            }
        } catch (ManagerException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            e.printStackTrace(response.getWriter());
        }
    }
}
