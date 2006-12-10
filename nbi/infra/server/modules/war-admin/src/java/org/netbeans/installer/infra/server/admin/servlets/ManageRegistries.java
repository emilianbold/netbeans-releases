package org.netbeans.installer.infra.server.admin.servlets;

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
import org.netbeans.installer.product.ProductGroup;
import org.netbeans.installer.product.ProductTreeNode;

/**
 *
 * @author ks152834
 * @version
 */
public class ManageRegistries extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        
        List<String> registries = manager.getRegistries();
        
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        out.println("<html>");
        out.println("    <head>");
        out.println("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
        out.println("        <title>Registries Manager</title>");
        out.println("        <link rel=\"stylesheet\" href=\"css/main.css\" type=\"text/css\"/>");
        out.println("        <script src=\"js/main.js\" type=\"text/javascript\"></script>");
        out.println("    </head>");
        out.println("    <body onload=\"update_current_registry()\">");
        out.println("        <div class=\"top-menu\">");
        out.println("            <a href=\"javascript: add_registry();\">Add Registry</a> |");
        if (registries.size() > 0) {
            out.println("            <a href=\"javascript: remove_registry();\">Remove Registry</a> |");
        } else {
            out.println("            Remove Registry |");
        }
        out.println("            <a href=\"javascript: update_engine();\">Update Engine</a>");
        out.println("        </div>");
        out.println("        ");
        if (registries.size() == 0) {
            out.println("        <p>");
            out.println("            Currently there are no existing registries on this server.");
            out.println("        </p>");
        } else {
            String selected = request.getParameter("registry");
            
            out.println("        <select id=\"registries-select\" onchange=\"update_current_registry()\">");
            for (String registry: registries) {
                out.println("            <option value=\"" + registry + "\"" + (registry.equals(selected) ? " selected" : "") + ">" + registry + "</option>");
            }
            out.println("        </select>");
            
            for (String registry: registries) {
                ProductTreeNode node = manager.getRoot(registry);
                out.println("        <div class=\"registry\" id=\"registry-" + registry + "\">");
                
                buildRegistryTable(out, registry, node);
                
                out.println("        </div>");
            }
        }
        out.println("        ");
        out.println("        <form name=\"Form\" method=\"post\" enctype=\"multipart/form-data\">");
        out.println("            <input type=\"hidden\" name=\"fallback_base\" value=\"" + request.getRequestURL() + "\"/>");
        out.println("            <input type=\"hidden\" name=\"fallback\"/>");
        out.println("            <input type=\"hidden\" name=\"uid\"/>");
        out.println("            <input type=\"hidden\" name=\"version\"/>");
        out.println("            <div class=\"pop-up\" id=\"form-registry\">");
        out.println("                <table>");
        out.println("                    <tr>");
        out.println("                        <td colspan=\"2\">Please define a name for a new registry.</td>");
        out.println("                    </tr>");
        out.println("                    <tr>");
        out.println("                        <td style=\"width: 100%\"><input type=\"text\" name=\"registry\" style=\"width: 100%\"/></td>");
        out.println("                        <td><input type=\"submit\"/></td>");
        out.println("                    </tr>");
        out.println("                    <tr>");
        out.println("                        <td colspan=\"2\"><a href=\"javascript: close_form_registry()\">close window</a></td>");
        out.println("                    </tr>");
        out.println("                </table>");
        out.println("            </div>");
        out.println("            <div class=\"pop-up\" id=\"form-archive\">");
        out.println("                <table>");
        out.println("                    <tr>");
        out.println("                        <td colspan=\"2\">Please point to a package.</td>");
        out.println("                    </tr>");
        out.println("                    <tr>");
        out.println("                        <td style=\"width: 100%\"><input type=\"file\" name=\"archive\" style=\"width: 100%\"/></td>");
        out.println("                        <td><input type=\"submit\"/></td>");
        out.println("                    </tr>");
        out.println("                    <tr>");
        out.println("                        <td colspan=\"2\"><a href=\"javascript: close_form_archive()\">close window</a></td>");
        out.println("                    </tr>");
        out.println("                </table>");
        out.println("            </div>");
        out.println("        </form>");
        out.println("    </body>");
        out.println("</html>");
        
        out.close();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    private void buildRegistryTable(PrintWriter out, String registry, ProductTreeNode root) {
        out.println("            <table class=\"registry\">");
        
        final ArrayList<ProductTreeNode> nodes = new ArrayList<ProductTreeNode>();
        nodes.add(root);
        
        buildRegistryNodes(out, registry, nodes);
        
        out.println("            </table>");
    }
    
    private void buildRegistryNodes(PrintWriter out, String registry, List<ProductTreeNode> nodes) {
        for (ProductTreeNode node: nodes) {
            String icon        = null;
            String displayName = node.getDisplayName();
            String treeHandle  = null;
            
            if (node.getIconUri() == null) {
                icon = "img/default-icon.png";
            } else {
                icon = node.getIconUri();
            }
            
            if (node.getChildren().size() > 0) {
                treeHandle  = "img/tree-handle-open.png";
            } else {
                treeHandle  = "img/tree-handle-empty.png";
            }
            
            String uid     = node.getUid();
            String version = null;
            String type    = null;
            
            if (node instanceof ProductComponent) {
                version = ((ProductComponent) node).getVersion().toString();
                type    = "component";
            }
            
            if (node instanceof ProductGroup) {
                type = "group";
            }
            
            String id = registry + "-" + uid + "-" + version + "-" + type;
            
            out.println("                <tr id=\"" + id + "\">");
            
            out.println("                    <td class=\"tree-handle\"><img src=\"" + treeHandle + "\" onclick=\"_expand('" + id + "-children')\"/></td>");
            out.println("                    <td class=\"icon\"><img src=\"" + icon + "\"/></td>");
            out.println("                    <td class=\"display-name\">" + displayName + "</td>");
            if (node.getParent() != null) {
                out.println("                    <td class=\"option\"><a href=\"javascript: remove_component('" + uid + "', '" + version + "')\">Remove</a></td>");
            } else {
                out.println("                    <td class=\"option\"></td>");
            }
            out.println("                    <td class=\"option\"><a href=\"javascript: add_component('" + uid + "', '" + version + "')\">Add Component</a></td>");
            out.println("                    <td class=\"option\"><a href=\"javascript: add_group('" + uid + "', '" + version + "')\">Add Group</a></td>");
            
            out.println("                </tr>");
            
            if (node.getChildren().size() > 0) {
                out.println("                <tr id=\"" + id + "-children\">");
                
                out.println("                    <td class=\"tree-handle\"></td>");
                out.println("                    <td colspan=\"5\" class=\"children\">");
                out.println("                    <table class=\"registry\">");
                buildRegistryNodes(out, registry, node.getChildren());
                out.println("                    </table>");
                out.println("                    </td>");
                
                out.println("                </tr>");
            }
        }
    }
}
