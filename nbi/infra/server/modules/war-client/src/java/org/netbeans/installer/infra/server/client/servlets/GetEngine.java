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
    }
}
