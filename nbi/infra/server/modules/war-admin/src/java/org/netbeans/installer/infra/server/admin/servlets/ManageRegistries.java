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
import org.netbeans.installer.infra.server.ejb.RegistryManager;
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
    private RegistryManager registryManager;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html>");
        out.println("<head>");
        out.println("<link rel=\"stylesheet\" href=\"css/main.css\" type=\"text/css\"/>");
        out.println("<script src=\"js/main.js\" type=\"text/javascript\"/>");
        out.println("</head>");
        out.println("<body>");
        
        for (String name: registryManager.getRegistries()) {
            ProductTreeNode node = registryManager.getRegistryRoot(name);
            out.println("<h1>" + name + "</h1>");
            out.println("<div id=\"registry-" + name + "\">");
            
            buildRegistryTable(out, name, node);
            
            out.println("</div>");
        }
        
        out.println("<div id=\"form-div\">");
        out.println("<form name=\"Form\" method=\"post\" enctype=\"multipart/form-data\">");
        out.println("<input type=\"hidden\" name=\"fallback\" value=\"" + request.getRequestURL() + "\"/>");
        out.println("<input type=\"hidden\" name=\"registry\"/>");
        out.println("<input type=\"hidden\" name=\"uid\"/>");
        out.println("<input type=\"hidden\" name=\"version\"/>");
        out.println("<input type=\"file\" name=\"archive\"/>");
        out.println("<input type=\"submit\"/>");
        out.println("</form>");
        out.println("</div>");
        
        out.println("</body>");
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
        out.println("<table class=\"nodes-tree\" style=\"width: 60%\">");
        
        final ArrayList<ProductTreeNode> nodes = new ArrayList<ProductTreeNode>();
        nodes.add(root);
        
        buildRegistryNodes(out, registry, nodes);
        
        out.println("</table>");
    }
    
    private void buildRegistryNodes(PrintWriter out, String registry, List<ProductTreeNode> nodes) {
        for (ProductTreeNode node: nodes) {
            final String icon        = node.getIconUri() == null ? "img/default-icon.png" : node.getIconUri();
            final String displayName = node.getDisplayName();
            final String treeHandle  = "img/tree-handle-open.png";
            
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
            
            out.println("<tr id=\"" + id + "\">");
            
            out.println("<td class=\"tree-handle\"><img src=\"" + treeHandle + "\" onclick=\"openclose('" + id + "-children')\"/></td>");
            out.println("<td class=\"icon\"><img src=\"" + icon + "\"/></td>");
            out.println("<td class=\"display-name\">" + displayName + "</td>");
            out.println("<td class=\"option\"><img src=\"img/delete.png\" onclick=\"remove('" + registry + "', '" + uid + "', '" + version + "')\"/></td>");
            out.println("<td class=\"option\"><img src=\"img/add-component.png\" onclick=\"addComponent('" + registry + "', '" + uid + "', '" + version + "')\"/></td>");
            out.println("<td class=\"option\"><img src=\"img/add-group.png\" onclick=\"addGroup('" + registry + "', '" + uid + "', '" + version + "')\"/></td>");
            
            out.println("</tr>");
            
            if (node.getChildren().size() > 0) {
                out.println("<tr id=\"" + id + "-children\">");
                
                out.println("<td class=\"tree-handle\">&nbsp;</td>");
                out.println("<td colspan=\"5\" class=\"children\">");
                out.println("<table class=\"nodes-tree\" style=\"margin: 0px 0px 0px 0px\">");
                buildRegistryNodes(out, registry, node.getChildren());
                out.println("</table>");
                out.println("</td>");
                
                out.println("</tr>");
            }
        }
    }
}
