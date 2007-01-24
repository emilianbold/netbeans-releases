package org.netbeans.installer.infra.server.client.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.infra.server.ejb.ManagerException;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.Platform;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class CreateBundle extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        
        String[] registries = request.getParameterValues("registry");
        
        PrintWriter out = response.getWriter();
        
        try {
            String userAgent = request.getHeader("User-Agent");
            
            Platform platform = SystemUtils.getCurrentPlatform();
            if (userAgent.contains("Windows")) {
                platform = Platform.WINDOWS;
            }
            if (userAgent.contains("PPC Mac OS")) {
                platform = Platform.MACOS_X_PPC;
            }
            if (userAgent.contains("Intel Mac OS")) {
                platform = Platform.MACOS_X_X86;
            }
            if (userAgent.contains("Linux")) {
                platform = Platform.LINUX;
            }
            if (userAgent.contains("SunOS i86pc")) {
                platform = Platform.SOLARIS_X86;
            }
            if (userAgent.contains("SunOS sun4u")) {
                platform = Platform.SOLARIS_SPARC;
            }
            
            
            if (request.getParameter("platform") != null) {
                try {
                    platform = StringUtils.parsePlatform(request.getParameter("platform"));
                } catch (ParseException e) {
                    e.printStackTrace(out);
                }
            }
            
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
            out.println("<html>");
            out.println("    <head>");
            out.println("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
            out.println("        <title>Registries Manager</title>");
            out.println("        <link rel=\"stylesheet\" href=\"admin/css/main.css\" type=\"text/css\"/>");
            out.println("        <script src=\"js/main.js\" type=\"text/javascript\"></script>");
            out.println("    </head>");
            out.println("    <body>");
            out.println("        <p>");
            out.println("            Select the components that you would like to include in the bundle and click Next.");
            out.println("        </p>");
            out.println("        <form name=\"Form\" action=\"create-bundle\" method=\"post\">");
            
            String registriesUrl = "";
            
            for (String registry: registries) {
                registriesUrl += "&registry=" + URLEncoder.encode(registry, "UTF-8");
                out.println("            <input type=\"hidden\" name=\"registry\" value=\"" + registry + "\"/>");
            }
            out.println("            <input type=\"hidden\" name=\"registries\" value=\"" + registriesUrl + "\"/>");
            out.println("            <input type=\"hidden\" name=\"platform\" value=\"" + platform + "\"/>");
            
            out.println("        <select id=\"platforms-select\" onchange=\"update_target_platform()\">");
            for (Platform temp: Platform.values()) {
                out.println("            <option value=\"" + temp.getName() + "\"" + (temp.equals(platform) ? " selected" : "") + ">" + temp.getDisplayName() + "</option>");
            }
            out.println("        </select>");
            
            out.println("        <div class=\"registry\">");
            buildRegistryTable(out, manager.getRoot(platform, registries), platform);
            out.println("        </div>");
            
            out.println("            <input type=\"submit\" value=\"Create Bundle\"/>");
            
            out.println("        </form>");
            out.println("        <br/>");
            out.println("        <p class=\"small\">" + userAgent + "</p>");
            out.println("    </body>");
            out.println("</html>");
        } catch (ManagerException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(out);
        }
        
        
        out.close();
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String[] registries = request.getParameterValues("registry");
            String[] components = request.getParameterValues("component");
            Platform platform = StringUtils.parsePlatform(request.getParameter("platform"));
            
            File bundle = manager.createBundle(platform, registries, components);
            
            final InputStream  input  = new FileInputStream(bundle);
            final OutputStream output = response.getOutputStream();
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + bundle.getName());
            
            StreamUtils.transferData(input, output);
            
            input.close();
            output.close();
        } catch (ParseException e) {
            e.printStackTrace(response.getWriter());
        } catch (ManagerException e) {
            e.printStackTrace(response.getWriter());
        }
    }
    
    private void buildRegistryTable(PrintWriter out, RegistryNode root, Platform platform) {
        out.println("            <table class=\"registry\">");
        
        buildRegistryNodes(out, root.getChildren(), platform);
        
        out.println("            </table>");
    }
    
    private void buildRegistryNodes(PrintWriter out, List<RegistryNode> nodes, Platform platform) {
        for (RegistryNode node: nodes) {
            if (node instanceof Product) {
                if (!((Product) node).getSupportedPlatforms().contains(platform)) {
                    continue;
                }
            }
            
            String icon        = null;
            String displayName = node.getDisplayName();
            String treeHandle  = null;
            
            if (node.getIconUri() == null) {
                icon = "img/default-icon.png";
            } else {
                icon = node.getIconUri().getRemote().toString();
            }
            
            if (node.getChildren().size() > 0) {
                treeHandle  = "img/tree-handle-open.png";
            } else {
                treeHandle  = "img/tree-handle-empty.png";
            }
            
            String id          = null;
            
            String uid         = node.getUid();
            String version     = null;
            String type        = null;
            String platforms   = null;
            String title       = "";
            
            if (node instanceof Product) {
                version   = ((Product) node).getVersion().toString();
                platforms = StringUtils.asString(((Product) node).getSupportedPlatforms(), " ");
                title     = StringUtils.asString(((Product) node).getSupportedPlatforms());
                type      = "component";
                
                id = uid + "_" + version + "_" + platforms.replace(" ", "_") + "_" + type;
            }
            
            if (node instanceof Group) {
                type = "group";
                
                id = uid + "_" + type;
            }
            
            out.println("                <tr id=\"" + id + "\">");
            
            out.println("                    <td class=\"tree-handle\"><img src=\"" + treeHandle + "\" onclick=\"_expand('" + id + "-children')\"/></td>");
            out.println("                    <td class=\"icon\"><img src=\"" + icon + "\"/></td>");
            if (version != null) {
                out.println("                    <td class=\"checkbox\"><input type=\"checkbox\" name=\"component\" value=\"" + uid + "," + version + "\"/></td>");
            } else {
                out.println("                    <td class=\"checkbox\"></td>");
            }
            out.println("                    <td class=\"display-name\" title=\"" + title + "\">" + displayName + "</td>");
            
            out.println("                </tr>");
            
            if (node.getChildren().size() > 0) {
                out.println("                <tr id=\"" + id + "-children\">");
                
                out.println("                    <td class=\"tree-handle\"></td>");
                out.println("                    <td colspan=\"3\" class=\"children\">");
                out.println("                    <table class=\"registry\">");
                buildRegistryNodes(out, node.getChildren(), platform);
                out.println("                    </table>");
                out.println("                    </td>");
                
                out.println("                </tr>");
            }
        }
    }
}
