package org.netbeans.installer.infra.server.client.servlets;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import org.netbeans.installer.utils.StreamUtils;

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
        
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        out.println("<html>");
        out.println("    <head>");
        out.println("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
        out.println("        <title>Registries Manager</title>");
        out.println("        <link rel=\"stylesheet\" href=\"css/main.css\" type=\"text/css\"/>");
        out.println("        <script src=\"js/main.js\" type=\"text/javascript\"></script>");
        out.println("    </head>");
        out.println("    <body>");
        out.println("        <p>");
        out.println("            Select the components that you would like to include in the bundle and click Next.");
        out.println("        </p>");
        out.println("        <form name=\"Form\" action=\"create-bundle\" method=\"post\">");
        
        for (String registry: registries) {
            out.println("            <input type=\"hidden\" name=\"registry\" value=\"" + registry + "\"/>");
        }
        
        out.println("        <div class=\"registry\">");
        buildRegistryTable(out, manager.getRoot(registries));
        out.println("        </div>");
        
        out.println("            <input type=\"submit\" value=\"Create Bundle\"/>");
            
        out.println("        </form>");
        out.println("    </body>");
        out.println("</html>");
        
        out.close();
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] registries = request.getParameterValues("registry");
        String[] components = request.getParameterValues("component");
        
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=nbi-bundle.jar");
        
        final InputStream  input  = new FileInputStream(manager.createBundle(registries, components));
        final OutputStream output = response.getOutputStream();
        
        StreamUtils.transferData(input, output);
        
        input.close();
        output.close();
    }
    
    private void buildRegistryTable(PrintWriter out, ProductTreeNode root) {
        out.println("            <table class=\"registry\">");
        
        buildRegistryNodes(out, root.getChildren());
        
        out.println("            </table>");
    }
    
    private void buildRegistryNodes(PrintWriter out, List<ProductTreeNode> nodes) {
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
            
            String id = uid + "-" + version + "-" + type;
            
            out.println("                <tr id=\"" + id + "\">");
            
            out.println("                    <td class=\"tree-handle\"><img src=\"" + treeHandle + "\" onclick=\"_expand('" + id + "-children')\"/></td>");
            out.println("                    <td class=\"icon\"><img src=\"" + icon + "\"/></td>");
            if (version != null) {
                out.println("                    <td class=\"checkbox\"><input type=\"checkbox\" name=\"component\" value=\"" + uid + "," + version + "\"/></td>");
            } else {
                out.println("                    <td class=\"checkbox\"></td>");
            }
            out.println("                    <td class=\"display-name\">" + displayName + "</td>");
            
            out.println("                </tr>");
            
            if (node.getChildren().size() > 0) {
                out.println("                <tr id=\"" + id + "-children\">");
                
                out.println("                    <td class=\"tree-handle\"></td>");
                out.println("                    <td colspan=\"3\" class=\"children\">");
                out.println("                    <table class=\"registry\">");
                buildRegistryNodes(out, node.getChildren());
                out.println("                    </table>");
                out.println("                    </td>");
                
                out.println("                </tr>");
            }
        }
    }
}
