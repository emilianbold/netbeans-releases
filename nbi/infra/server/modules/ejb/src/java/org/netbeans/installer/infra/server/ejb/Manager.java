
package org.netbeans.installer.infra.server.ejb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.ejb.Local;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductTreeNode;

/**
 * This is the business interface for RegistryManager enterprise bean.
 */
@Local
public interface Manager {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final File   ROOT            = new File("D:\\temp\\nbi-server");
    public static final File   TEMP            = new File(ROOT, "temp");
    public static final File   REGISTRIES      = new File(ROOT, "registries");
    public static final File   UPLOADS         = new File(TEMP, "uploads");
    public static final File   NBI             = new File(TEMP, ".nbi");
    
    public static final File   REGISTRIES_LIST = new File(ROOT, "registries.list");
    public static final File   ENGINE          = new File(ROOT, "nbi-engine.jar");
    
    public static final String COMPONENTS      = "components";
    public static final String GROUPS          = "groups";
    public static final String REGISTRY_XML    = "registry.xml";
    
    // registry operations //////////////////////////////////////////////////////////
    void addRegistry(String registry) throws IOException;
    
    void removeRegistry(String registry) throws IOException;
    
    String getRegistry(String name) throws IOException;
    
    List<String> getRegistries() throws IOException;
    
    // engine operations ////////////////////////////////////////////////////////////
    File getEngine() throws IOException;
    
    void updateEngine(File engine) throws IOException;
    
    // component operations /////////////////////////////////////////////////////////
    void updateComponent(String name, File archive, String parentUid,
            String parentVersion, String uriPrefix) throws IOException;
    
    void removeComponent(String name, String uid, String version) throws IOException;
    
    // group operations /////////////////////////////////////////////////////////////
    void updateGroup(String name, File archive, String parentUid,
            String parentVersion, String uriPrefix) throws IOException;
    
    void removeGroup(String name, String uid) throws IOException;
    
    // miscellanea //////////////////////////////////////////////////////////////////
    File getFile(String name, String file) throws IOException;
    
    ProductTreeNode getRoot(String... names) throws IOException;
    
    List<ProductComponent> getComponents(String... names) throws IOException;
    
    File createBundle(String[] names, String[] components) throws IOException;
}
