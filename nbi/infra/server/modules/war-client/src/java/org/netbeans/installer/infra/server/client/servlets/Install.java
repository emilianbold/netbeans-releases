package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class Install extends HttpServlet {
    private static final String JNLP_STUB = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<jnlp spec=\"1.0+\" codebase=\"{0}\" href=\"{1}\">\n" + 
            "    <information>\n" + 
            "        <title>NetBeans Installer</title>\n" + 
            "        <vendor>Sun Microsystems, Inc.</vendor>\n" + 
            "        <description>NetBeans Installer Engine</description>\n" + 
            "        <description kind=\"short\">NetBeans Installer Engine</description>\n" + 
            "    </information>\n" + 
            "    <security>\n" + 
            "        <all-permissions/>\n" + 
            "    </security>\n" + 
            "    <resources>\n" + 
            "        <j2se version=\"1.5+\"/>\n" + 
            "        <j2se version=\"1.6+\"/>\n" + 
            "        <j2se version=\"1.6.0-rc\"/>\n" + 
            "        <j2se version=\"1.6.0-ea\"/>\n" + 
            "        <j2se version=\"1.6.0-beta\"/>\n" + 
            "        <j2se version=\"1.6.0-beta2\"/>\n" + 
            "        <jar href=\"{2}\"/>\n" + 
            "        <property name=\"nbi.product.remote.registries\" value=\"{3}\"/>\n" + 
            "    </resources>\n" + 
            "    <application-desc main-class=\"org.netbeans.installer.Installer\"/>\n" + 
            "</jnlp>";
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/x-java-jnlp-file");
//        response.setContentType("text/plain");
        
        String[] names = request.getParameterValues("registry");
        if ((names == null) || (names.length == 0)) {
            names = new String[]{"default"};
        }
        
        String codebase = 
                getServerUrl(request) + getServletContext().getContextPath();
        
        String jnlp = "install?true=true";
        for (String name: names) {
            jnlp += "&registry=" + URLEncoder.encode(name, "UTF-8");
        }
        
        String engine   = codebase + "/get-engine.jar";
        
        String registry = "";
        for (String name: names) {
            registry += codebase + "/get-registry?registry=" + URLEncoder.encode(name, "UTF-8") + "\n";
        }
        registry = registry.trim();
        
        response.getWriter().write(
                StringUtils.format(JNLP_STUB, codebase, jnlp, engine, registry));
        response.getWriter().close();
    }
    
    private String getServerUrl(HttpServletRequest request) throws MalformedURLException {
        URL    url    = new URL(request.getRequestURL().toString());
        String string = url.toString();
                
        return string.substring(0, string.indexOf(url.getFile()));
    }
}
