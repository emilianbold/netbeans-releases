package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.product.ProductComponent;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class Feed extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/octet-stream");
        
        final PrintWriter            out        = response.getWriter();
        final String                 feedType   = request.getParameter("feed-type");
        final String[]               registries = request.getParameterValues("registry");
        final List<ProductComponent> components = manager.getComponents(registries);
        
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        
        
        out.close();
    }
}
