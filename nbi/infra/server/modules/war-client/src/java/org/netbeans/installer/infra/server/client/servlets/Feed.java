package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
        final PrintWriter out        = response.getWriter();
        final String[]    registries = request.getParameterValues("registry");
        
        List<ProductComponent> components;
        String                 feedType;
        
        // if the user did not specify any registry to look for the components,
        // we cannot guess for him - will return an empty feed
        if ((registries == null) || (registries.length == 0)) {
            components = new ArrayList<ProductComponent>();
        } else {
            components = manager.getComponents(registries);
        }
        
        feedType = request.getParameter("feed-type");
        if (feedType == null) {
            feedType = "rss";
        }
        
        response.setContentType("application/octet-stream");
        
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        if (feedType.equals("rss")) {
            buildRss(components, out);
        }
        
        out.close();
    }
    
    private void buildRss(List<ProductComponent> components, PrintWriter out) throws IOException {
        out.println("<rss version=\"2.0\">");
        out.println("    <channel>");
        
        out.println("        <title><![CDATA[NetBeans Installer Components Feed]]></title>");
        out.println("        <link>http://localhost/</link>");
        out.println("        <description><![CDATA[NetBeans Installer Components Feed]]></description>");
        
        for (ProductComponent component: components) {
            out.println("            <item>");
            out.println("                <title><![CDATA[" + component.getDisplayName() + "]]></title>");
            out.println("                <link>http://localhost/</link>");
            out.println("                <description><![CDATA[" + component.getDescription() + "]]></description>");
            out.println("            </item>");
        }
        
        out.println("    </channel>");
        out.println("</rss>");
    }
}
