package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.EngineManager;
import org.netbeans.installer.utils.StreamUtils;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class GetEngine extends HttpServlet {
    @EJB
    private EngineManager engineManager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        
        final InputStream  input  = engineManager.getEngine();
        final OutputStream output = response.getOutputStream();
        
        StreamUtils.transferData(input, output);
        
        input.close();
        output.close();
    }
}
