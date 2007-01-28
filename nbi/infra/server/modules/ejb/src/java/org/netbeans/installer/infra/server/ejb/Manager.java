
package org.netbeans.installer.infra.server.ejb;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.ejb.Local;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.utils.helper.Platform;

/**
 * This is the business interface for RegistryManager enterprise bean.
 */
@Local
public interface Manager {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final File   ROOT            = new File("D:/temp/nbi-server");
    public static final File   TEMP            = new File(ROOT, "temp");
    public static final File   REGISTRIES      = new File(ROOT, "registries");
    public static final File   UPLOADS         = new File(TEMP, "uploads");
    public static final File   BUNDLES         = new File(TEMP, "bundles");
    public static final File   NBI             = new File(TEMP, ".nbi");
    
    public static final File   REGISTRIES_LIST = new File(ROOT, "registries.list");
    public static final File   ENGINE          = new File(ROOT, "nbi-engine.jar");
    
    public static final String COMPONENTS      = "components";
    public static final String GROUPS          = "groups";
    public static final String REGISTRY_XML    = "registry.xml";
    
    // registry operations //////////////////////////////////////////////////////////
    void addRegistry(String registry) throws ManagerException;
    
    void removeRegistry(String registry) throws ManagerException;
    
    String getRegistry(String name) throws ManagerException;
    
    List<String> getRegistries() throws ManagerException;
    
    // engine operations ////////////////////////////////////////////////////////////
    File getEngine() throws ManagerException;
    
    void updateEngine(File engine) throws ManagerException;
    
    // component operations /////////////////////////////////////////////////////////
    void updateComponent(String name, File archive, String parentUid, String parentVersion, String parentPlatforms, String uriPrefix) throws ManagerException;
    
    void removeComponent(String name, String uid, String version, String platforms) throws ManagerException;
    
    // group operations /////////////////////////////////////////////////////////////
    void updateGroup(String name, File archive, String parentUid, String parentVersion, String parentPlatforms, String uriPrefix) throws ManagerException;
    
    void removeGroup(String name, String uid) throws ManagerException;
    
    // miscellanea //////////////////////////////////////////////////////////////////
    File getFile(String name, String file) throws ManagerException;
    
    RegistryNode getRoot(Platform platform, String... names) throws ManagerException;
    
    List<Product> getComponents(Platform platform, String... names) throws ManagerException;
    
    File createBundle(Platform platform, String[] names, String[] components) throws ManagerException;
}
