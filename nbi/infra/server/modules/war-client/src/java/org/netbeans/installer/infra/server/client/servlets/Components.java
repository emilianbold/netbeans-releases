package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.infra.server.ejb.ManagerException;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.Platform;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class Components extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> fixed = Arrays.asList("nb-ide");
        List<String> standard = Arrays.asList("nb-ide", "nb-mobility", "glassfish");
        List<String> selected = Arrays.asList("nb-mobility", "glassfish");
        
        try {
            response.setContentType("text/javascript; charset=UTF-8");
            PrintWriter out = response.getWriter();
            
            Registry registry = manager.loadRegistry("NetBeans 6.0");
            
            List<Product> products = registry.getProducts();
            List<Group> groups = registry.getGroups();
            
            Map<Integer, Integer> productMapping = new HashMap<Integer, Integer>();
            
            List<String> productsUids = new LinkedList<String>();
            List<String> productsVersions = new LinkedList<String>();
            List<String> productsDisplayNames = new LinkedList<String>();
            List<String> productsDownloadSizes = new LinkedList<String>();
            List<List<Platform>> productsPlatforms = new LinkedList<List<Platform>>();
            List<String> productsProperties = new LinkedList<String>();
            for (int i = 0; i < products.size(); i++) {
                final Product product = products.get(i);
                
                boolean existingFound = false;
                for (int j = 0; j < productsUids.size(); j++) {
                    if (productsUids.get(j).equals(product.getUid()) &&
                            productsVersions.get(j).equals(product.getVersion().toString())) {
                        productsPlatforms.get(j).addAll(product.getPlatforms());
                        productMapping.put(i, j);
                        existingFound = true;
                        break;
                    }
                }
                
                if (existingFound) {
                    continue;
                }
                
                long size = (long) Math.ceil(((double) product.getDownloadSize()) / 1024. / 1024.);
                productsUids.add(product.getUid());
                productsVersions.add(product.getVersion().toString());
                productsDisplayNames.add(product.getDisplayName());
                productsDownloadSizes.add(Long.toString(size));
                productsPlatforms.add(product.getPlatforms());
                
                String properties = "PROPERTY_NONE";
                if (fixed.contains(product.getUid())) {
                    properties += " | PROPERTY_FIXED";
                }
                if (standard.contains(product.getUid())) {
                    properties += " | PROPERTY_STANDARD";
                }
                if (selected.contains(product.getUid())) {
                    properties += " | PROPERTY_SELECTED";
                }
                productsProperties.add(properties);
                
                productMapping.put(i, productsUids.size() - 1);
            }
            out.println("components_uids = new Array(\n    \"" + StringUtils.asString(productsUids, "\", \n    \"") + "\");");
            out.println();
            out.println("components_versions = new Array(\n    \"" + StringUtils.asString(productsVersions, "\", \n    \"") + "\");");
            out.println();
            out.println("components_display_names = new Array(\n    \"" + StringUtils.asString(productsDisplayNames, "\", \n    \"") + "\");");
            out.println();
            out.println("components_download_sizes = new Array(\n    " + StringUtils.asString(productsDownloadSizes, ", \n    ") + ");");
            out.println();
            out.println("components_platforms = new Array(");
            for (int i = 0; i < productsPlatforms.size(); i++) {
                List<Platform> platforms = productsPlatforms.get(i);
                out.println("    new Array(\"" + StringUtils.asString(platforms, "\", \"") + "\")"  + (i < productsPlatforms.size() - 1 ? "," : ""));
            }
            out.println(");");
            out.println();
            out.println("components_properties = new Array(\n    " + StringUtils.asString(productsProperties, ", \n    ") + ");");
            out.println();
            
            List<Integer> default_group_components = new LinkedList<Integer>();
            for (int i = 0; i < productsUids.size(); i++) {
                default_group_components.add(Integer.valueOf(i));
            }
            
            List<List<Integer>> groups_components = new LinkedList<List<Integer>>();
            List<String> groups_display_names = new LinkedList<String>();
            for (Group group: groups) {
                if (group.getUid().equals("")) {
                    continue;
                }
                
                List<Integer> group_components = new LinkedList<Integer>();
                for (int i = 0; i < products.size(); i++) {
                    if (group.isAncestor(products.get(i))) {
                        if (!group_components.contains(Integer.valueOf(productMapping.get(i)))) {
                            group_components.add(Integer.valueOf(productMapping.get(i)));
                            default_group_components.remove(Integer.valueOf(productMapping.get(i)));
                        }
                    }
                }
                
                groups_components.add(group_components);
                groups_display_names.add(group.getDisplayName());
            }
            
            out.println("groups_components = new Array(");
            out.println("    new Array(" + StringUtils.asString(default_group_components, ", ") + "),");
            for (int i = 0; i < groups_components.size(); i++) {
                List<Integer> components = groups_components.get(i);
                
                out.println("    new Array(" + StringUtils.asString(components, ", ") + ")" + (i < groups_components.size() - 1 ? "," : ""));
            }
            out.println(");");
            out.println();
            out.println("groups_display_names = new Array(\n    \"\", \n    \"" + StringUtils.asString(groups_display_names, "\", \n    \"") + "\");");
            out.println();
            
            out.close();
        } catch (ManagerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    //            groups_components = new Array(
    //                new Array(0, 1, 2),
    //                new Array(3, 4)
    //            );
    //
    //            groups_display_names = new Array(
    //                "",
    //                "Runtimes"
    //            );
    
}