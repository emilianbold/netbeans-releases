package org.netbeans.installer.infra.server.ejb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import javax.ejb.Stateless;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.applications.JDKUtils;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.ExtendedURI;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.progress.Progress;
import org.w3c.dom.Document;

/**
 *
 * @author Kirill Sorokin
 */
@Stateless
public class ManagerBean implements Manager {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Map<String, File> registries = new HashMap<String, File>();
    private Map<String, File> bundles    = new HashMap<String, File>();
    
    // constructor //////////////////////////////////////////////////////////////////
    public ManagerBean() {
        try {
            ROOT.mkdirs();
            
            TEMP.mkdirs();
            REGISTRIES.mkdirs();
            UPLOADS.mkdirs();
            BUNDLES.mkdirs();
            NBI.mkdirs();
            
            if (!REGISTRIES_LIST.exists()) {
                REGISTRIES_LIST.createNewFile();
            }
            
            loadRegistriesList();
            
            Locale.setDefault(new Locale("en", "US"));
            
            System.setProperty(
                    Installer.LOCAL_DIRECTORY_PATH_PROPERTY, NBI.getAbsolutePath());
            System.setProperty(
                    Installer.IGNORE_LOCK_FILE_PROPERTY, "true");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ManagerException e) {
            e.printStackTrace();
        }
    }
    
    // registry operations //////////////////////////////////////////////////////////
    public void addRegistry(String name) throws ManagerException {
        if (registries.get(name) == null) {
            registries.put(name, initializeRegistry(name));
        }
        
        saveRegistriesList();
    }
    
    public void removeRegistry(String name) throws ManagerException {
        try {
            if (registries.get(name) != null) {
                FileUtils.deleteFile(registries.get(name));
                registries.remove(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
        
        saveRegistriesList();
    }
    
    public String getRegistry(String name) throws ManagerException {
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir = registries.get(name);
        final File registryXml = new File(registryDir, REGISTRY_XML);
        
        try {
            return FileUtils.readFile(registryXml);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    public List<String> getRegistries() throws ManagerException {
        return new ArrayList<String>(registries.keySet());
    }
    
    // engine operations ////////////////////////////////////////////////////////////
    public File getEngine() throws ManagerException {
        return ENGINE;
    }
    
    public void updateEngine(File engine) throws ManagerException {
        deleteBundles();
        
        ENGINE.delete();
        
        try {
            FileUtils.moveFile(engine, ENGINE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    // component operations /////////////////////////////////////////////////////////
    public void updateComponent(String name, File archive, String parentUid, String parentVersion, String parentPlatforms, String uriPrefix) throws ManagerException {
        deleteBundles();
        
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir   = registries.get(name);
        final File componentsDir = new File(registryDir, COMPONENTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml   = new File(registryDir, REGISTRY_XML);
        
        String string = null;
        
        try {
            final Registry registry = new Registry(registryXml);
            
            final File descriptor = new File(componentsDir,
                    FileUtils.getJarAttribute(archive, "Product-Descriptor"));
            
            FileUtils.unjar(archive, componentsDir);
            
            FileUtils.modifyFile(descriptor,
                    "(\\>)resource:(.*?\\<\\/)",
                    "$1" + componentsDir.toURI() + "$2", true);
            
            final Product component = new Product().loadFromDom(
                    XMLUtils.loadXMLDocument(descriptor).getDocumentElement());
            
            final List<Product> existingComponents =
                    registry.getProductComponents(
                    component.getUid(),
                    component.getVersion(),
                    component.getSupportedPlatforms());
            
            if (existingComponents != null) {
                Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
                
                for (Product existing: existingComponents) {
                    existing.getParent().removeChild(existing);
                    nodes.offer(existing);
                }
                
                while (nodes.peek() != null) {
                    RegistryNode node = nodes.poll();
                    
                    if (node instanceof Product) {
                        Product temp = (Product) node;
                        FileUtils.deleteFile(new File(
                                componentsDir,
                                temp.getUid() + "/" + temp.getVersion() + "/" + StringUtils.asString(temp.getSupportedPlatforms(), " ")));
                    }
                    
                    if (node instanceof Group) {
                        Group temp = (Group) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
                
                // we need to unjar again, since the removal procedure has deleted
                // all the files from the newly updated component
                FileUtils.unjar(archive, componentsDir);
            }
            
            RegistryNode parent;
            
            List<Product> parents = null;
            if (!parentVersion.equals("null") && !parentPlatforms.equals("null")) {
                parents = registry.getProductComponents(
                        parentUid,
                        new Version(parentVersion),
                        StringUtils.parsePlatforms(parentPlatforms));
            }
            if (parents == null) {
                parent = registry.getProductGroup(parentUid);
                if (parent == null) {
                    parent = registry.getProductTreeRoot();
                }
            } else {
                parent = parents.get(0);
            }
            
            parent.addChild(component);
            
            for (ExtendedURI uri: component.getConfigurationLogicUris()) {
                string = uri.getRemote().getSchemeSpecificPart();
                string = string.substring(componentsDir.toURI().getSchemeSpecificPart().length());
                string = URLEncoder.encode("components/" + string, "UTF-8");
                uri.setLocal(new URI(uriPrefix + string));
            }
            
            for (ExtendedURI uri: component.getInstallationDataUris()) {
                string = uri.getRemote().getSchemeSpecificPart();
                string = string.substring(componentsDir.toURI().getSchemeSpecificPart().length());
                string = URLEncoder.encode("components/" + string, "UTF-8");
                uri.setLocal(new URI(uriPrefix + string));
            }
            
            string = component.getIconUri().getRemote().getSchemeSpecificPart();
            string = string.substring(componentsDir.toURI().getSchemeSpecificPart().length());
            string = URLEncoder.encode("components/" + string, "UTF-8");
            component.getIconUri().setLocal(new URI(uriPrefix + string));
            
            registry.saveProductRegistry(registryXml, new IconCorrectingFilter());
            
            archive.delete();
            descriptor.delete();
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (XMLException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    public void removeComponent(String name, String uid, String version, String platforms) throws ManagerException {
        deleteBundles();
        
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir   = registries.get(name);
        final File componentsDir = new File(registryDir, COMPONENTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml   = new File(registryDir, REGISTRY_XML);
        
        try {
            final Registry registry = new Registry(registryXml);
            
            final List<Product> existing = registry.getProductComponents(uid, new Version(version), StringUtils.parsePlatforms(platforms));
            
            if (existing != null) {
                existing.get(0).getParent().removeChild(existing.get(0));
                
                Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
                nodes.offer(existing.get(0));
                
                while(nodes.peek() != null) {
                    RegistryNode node = nodes.poll();
                    
                    if (node instanceof Product) {
                        Product temp = (Product) node;
                        FileUtils.deleteFile(new File(componentsDir, temp.getUid() + "/" + temp.getVersion()));
                    }
                    
                    if (node instanceof Group) {
                        Group temp = (Group) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            registry.saveProductRegistry(registryXml, new IconCorrectingFilter());
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not remove component", e);
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not remove component", e);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    // group operations /////////////////////////////////////////////////////////////
    public void updateGroup(String name, File archive, String parentUid, String parentVersion, String parentPlatforms, String uriPrefix) throws ManagerException {
        deleteBundles();
        
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir = registries.get(name);
        final File componentsDir = new File(registryDir, COMPONENTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml = new File(registryDir, REGISTRY_XML);
        
        String string = null;
        
        try {
            final Registry registry = new Registry(registryXml);
            
            final File descriptor = new File(groupsDir,
                    FileUtils.getJarAttribute(archive, "Group-Descriptor"));
            
            FileUtils.unjar(archive, groupsDir);
            
            FileUtils.modifyFile(descriptor,
                    "(\\>)resource:(.*?\\<\\/)",
                    "$1" + groupsDir.toURI() + "$2", true);
            
            final Group group = new Group().loadFromDom(
                    XMLUtils.loadXMLDocument(descriptor).getDocumentElement());
            final Group existing = registry.getProductGroup(
                    group.getUid());
            
            if (existing != null) {
                existing.getParent().removeChild(existing);
                
                Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
                nodes.offer(existing);
                
                while(nodes.peek() != null) {
                    RegistryNode node = nodes.poll();
                    
                    if (node instanceof Product) {
                        Product temp = (Product) node;
                        FileUtils.deleteFile(new File(componentsDir, temp.getUid() + "/" + temp.getVersion()));
                    }
                    
                    if (node instanceof Group) {
                        Group temp = (Group) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
                
                // we need to unjar again, since the removal procedure has deleted
                // all the files from the newly updated group
                FileUtils.unjar(archive, groupsDir);
            }
            
            RegistryNode parent;
            
            List<Product> parents = null;
            if (!parentVersion.equals("null") && !parentPlatforms.equals("null")) {
                parents = registry.getProductComponents(
                        parentUid,
                        new Version(parentVersion),
                        StringUtils.parsePlatforms(parentPlatforms));
            }
            if (parents == null) {
                parent = registry.getProductGroup(parentUid);
                if (parent == null) {
                    parent = registry.getProductTreeRoot();
                }
            } else {
                parent = parents.get(0);
            }
            
            parent.addChild(group);
            
            string = group.getIconUri().getRemote().getSchemeSpecificPart();
            string = string.substring(groupsDir.toURI().getSchemeSpecificPart().length());
            string = URLEncoder.encode("groups/" + string, "UTF-8");
            group.getIconUri().setLocal(new URI(uriPrefix + string));
            
            registry.saveProductRegistry(registryXml, new IconCorrectingFilter());
            
            archive.delete();
            descriptor.delete();
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update group", e);
        } catch (XMLException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update group", e);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update group", e);
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update group", e);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    public void removeGroup(String name, String uid) throws ManagerException {
        deleteBundles();
        
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir = registries.get(name);
        final File componentsDir = new File(registryDir, COMPONENTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml = new File(registryDir, REGISTRY_XML);
        
        try {
            final Registry registry = new Registry(registryXml);
            
            final Group existing = registry.getProductGroup(uid);
            
            if (existing != null) {
                existing.getParent().removeChild(existing);
                
                Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
                nodes.offer(existing);
                
                while(nodes.peek() != null) {
                    RegistryNode node = nodes.poll();
                    
                    if (node instanceof Product) {
                        Product temp = (Product) node;
                        FileUtils.deleteFile(new File(componentsDir, temp.getUid() + "/" + temp.getVersion()));
                    }
                    
                    if (node instanceof Group) {
                        Group temp = (Group) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            registry.saveProductRegistry(registryXml, new IconCorrectingFilter());
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not remove component", e);
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not remove component", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public File getFile(String name, String file) throws ManagerException {
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir = registries.get(name);
        
        return new File(registryDir, file);
    }
    
    public RegistryNode getRoot(Platform platform, String... names) throws ManagerException {
        if (names.length > 0) {
            List<File> files = new LinkedList<File>();
            
            for (String name: names) {
                if (registries.get(name) == null) {
                    addRegistry(name);
                }
                
                
                files.add(new File(registries.get(name), REGISTRY_XML));
            }
            
            try {
                return new Registry(platform, files).getProductTreeRoot();
            } catch (InitializationException e) {
                e.printStackTrace();
                throw new ManagerException("Could not load registry", e);
            }
        }
        
        return null;
    }
    
    public List<Product> getComponents(Platform platform, String... names) throws ManagerException {
        List<Product> components = new LinkedList<Product>();
        
        if (names.length > 0) {
            List<File> files = new LinkedList<File>();
            
            for (String name: names) {
                if (registries.get(name) == null) {
                    addRegistry(name);
                }
                
                
                files.add(new File(registries.get(name), REGISTRY_XML));
            }
            
            try {
                components.addAll(new Registry(platform, files).
                        queryComponents(new TrueFilter()));
            } catch (InitializationException e) {
                e.printStackTrace();
                throw new ManagerException("Could not load registry", e);
            }
        }
        
        return components;
    }
    
    public File createBundle(Platform platform, String[] names, String[] components) throws ManagerException {
        String key = "";
        for (String component: components) {
            key += component + ";";
        }
        key += platform.toString();
        
        if (bundles.get(key) != null) {
            return bundles.get(key);
        }
        
        try {
            File statefile = FileUtils.createTempFile(TEMP, false);
            File bundle    = FileUtils.createTempFile(BUNDLES, false);
            
            File javaHome  = new File(System.getProperty("java.home"));
            
            String     remote = "";
            List<File> files  = new LinkedList<File>();
            for (String name: names) {
                if (registries.get(name) == null) {
                    addRegistry(name);
                }
                File xml = new File(registries.get(name), REGISTRY_XML);
                
                files.add(xml);
                remote += xml.toURI().toString() + "\n";
            }
            remote = remote.trim();
            
            Registry registry = new Registry(platform, files);
            for (String string: components) {
                String[] parts = string.split(",");
                
                registry.getProductComponent(parts[0], new Version(parts[1])).
                        setStatus(Status.INSTALLED);
            }
            registry.saveStateFile(statefile, new Progress());
            
            ExecutionResults results = SystemUtils.executeCommand(
                    JDKUtils.getExecutable(javaHome).getAbsolutePath(),
                    "-Dnbi.product.remote.registries=" + remote,
                    "-jar",
                    ENGINE.getAbsolutePath(),
                    "--silent",
                    "--state",
                    statefile.getAbsolutePath(),
                    "--create-bundle",
                    bundle.getAbsolutePath(),
                    "--ignore-lock",
                    "--platform",
                    platform.toString());
            
            statefile.delete();
            
            if (results.getErrorCode() != 0) {
                throw new ManagerException("Could not create bundle - error in running the engine");
            }
            
            bundles.put(key, bundle);
            
            return bundle;
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void loadRegistriesList() throws ManagerException {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(REGISTRIES_LIST)));
            
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                registries.put(line, initializeRegistry(line));
            }
            
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    private void saveRegistriesList() throws ManagerException {
        try {
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(REGISTRIES_LIST)));
            
            try {
                for (String key: registries.keySet()) {
                    writer.println(key);
                }
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    private File initializeRegistry(String name) throws ManagerException {
        File directory   = new File(REGISTRIES, name);
        File registryxml = new File(directory, REGISTRY_XML);
        
        directory.mkdirs();
        
        if (!registryxml.exists()) {
            try {
                Document document = Registry.
                        getInstance().getEmptyRegistryDocument();
                XMLUtils.saveXMLDocument(document, registryxml);
            } catch (XMLException e) {
                e.printStackTrace();
                throw new ManagerException("Cannot initialize registry", e);
            }
        }
        
        return directory;
    }
    
    private void deleteBundles() throws ManagerException {
        for (String key: bundles.keySet()) {
            bundles.get(key).delete();
            bundles.remove(key);
        }
    }
}
