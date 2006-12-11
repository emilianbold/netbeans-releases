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
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductGroup;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.product.ProductTreeNode;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.applications.JDKUtils;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.ExtendedURI;
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
        }
    }
    
    // registry operations //////////////////////////////////////////////////////////
    public void addRegistry(String name) throws IOException {
        if (registries.get(name) == null) {
            registries.put(name, initializeRegistry(name));
        }
        
        saveRegistriesList();
    }
    
    public void removeRegistry(String name) throws IOException {
        if (registries.get(name) != null) {
            FileUtils.deleteFile(registries.get(name));
            registries.remove(name);
        }
        
        saveRegistriesList();
    }
    
    public String getRegistry(String name) throws IOException {
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir = registries.get(name);
        final File registryXml = new File(registryDir, REGISTRY_XML);
        
        return FileUtils.readFile(registryXml);
    }
    
    public List<String> getRegistries() throws IOException {
        return new ArrayList<String>(registries.keySet());
    }
    
    // engine operations ////////////////////////////////////////////////////////////
    public File getEngine() throws IOException {
        return ENGINE;
    }
    
    public void updateEngine(File engine) throws IOException {
        ENGINE.delete();
        
        FileUtils.moveFile(engine, ENGINE);
    }
    
    // component operations /////////////////////////////////////////////////////////
    public void updateComponent(String name, File archive, String uid, String version, String uriPrefix) throws IOException {
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir   = registries.get(name);
        final File componentsDir = new File(registryDir, COMPONENTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml   = new File(registryDir, REGISTRY_XML);
        
        String string = null;
        
        try {
            final ProductRegistry registry = new ProductRegistry(registryXml);
            
            final File descriptor = new File(componentsDir,
                    FileUtils.getJarAttribute(archive, "Component-Descriptor"));
            
            FileUtils.unjar(archive, componentsDir);
            
            FileUtils.modifyFile(descriptor,
                    "(\\<icon.*\\>)resource:(.*\\<\\/icon\\>)",
                    "$1" + componentsDir.toURI() + "$2", true);
            
            final ProductComponent component = new ProductComponent().loadFromDom(
                    XMLUtils.loadXMLDocument(descriptor).getDocumentElement());
            final ProductComponent existing = registry.getProductComponent(
                    component.getUid(), component.getVersion());
            
            if (existing != null) {
                existing.getParent().removeChild(existing);
                
                Queue<ProductTreeNode> nodes = new LinkedList<ProductTreeNode>();
                nodes.offer(existing);
                
                while(nodes.peek() != null) {
                    ProductTreeNode node = nodes.poll();
                    
                    if (node instanceof ProductComponent) {
                        ProductComponent temp = (ProductComponent) node;
                        FileUtils.deleteFile(new File(componentsDir, temp.getUid() + "/" + temp.getVersion()));
                    }
                    
                    if (node instanceof ProductGroup) {
                        ProductGroup temp = (ProductGroup) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (ProductTreeNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
                
                // we need to unjar again, since the removal procedure has deleted
                // all the files from the newly updated component
                FileUtils.unjar(archive, componentsDir);
            }
            
            ProductTreeNode parent =
                    registry.getProductComponent(uid, new Version(version));
            if (parent == null) {
                parent = registry.getProductGroup(uid);
                if (parent == null) {
                    parent = registry.getProductTreeRoot();
                }
            }
            
            parent.addChild(component);
            
            for (ExtendedURI uri: component.getConfigurationLogicUris()) {
                string = uri.getRemoteUri().getSchemeSpecificPart();
                string = URLEncoder.encode("components/" + string, "UTF-8");
                uri.setLocalUri(new URI(uriPrefix + string));
            }
            
            for (ExtendedURI uri: component.getInstallationDataUris()) {
                string = uri.getRemoteUri().getSchemeSpecificPart();
                string = URLEncoder.encode("components/" + string, "UTF-8");
                uri.setLocalUri(new URI(uriPrefix + string));
            }
            
            string = new URI(component.getIconUri()).getSchemeSpecificPart();
            string = string.substring(componentsDir.toURI().getSchemeSpecificPart().length());
            string = URLEncoder.encode("components/" + string, "UTF-8");
            component.setIconUri(uriPrefix + string);
            
            registry.saveProductRegistry(registryXml, new TrueFilter());
            
            archive.delete();
            descriptor.delete();
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new IOException("Could not update component");
        } catch (XMLException e) {
            e.printStackTrace();
            throw new IOException("Could not update component");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IOException("Could not update component");
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new IOException("Could not update component");
        }
    }
    
    public void removeComponent(String name, String uid, String version) throws IOException {
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir   = registries.get(name);
        final File componentsDir = new File(registryDir, COMPONENTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml   = new File(registryDir, REGISTRY_XML);
        
        try {
            final ProductRegistry registry = new ProductRegistry(registryXml);
            
            final ProductComponent existing = registry.getProductComponent(
                    uid, new Version(version));
            
            if (existing != null) {
                existing.getParent().removeChild(existing);
                
                Queue<ProductTreeNode> nodes = new LinkedList<ProductTreeNode>();
                nodes.offer(existing);
                
                while(nodes.peek() != null) {
                    ProductTreeNode node = nodes.poll();
                    
                    if (node instanceof ProductComponent) {
                        ProductComponent temp = (ProductComponent) node;
                        FileUtils.deleteFile(new File(componentsDir, temp.getUid() + "/" + temp.getVersion()));
                    }
                    
                    if (node instanceof ProductGroup) {
                        ProductGroup temp = (ProductGroup) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (ProductTreeNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            registry.saveProductRegistry(registryXml, new TrueFilter());
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new IOException("Could not remove component");
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new IOException("Could not remove component");
        }
    }
    
    // group operations /////////////////////////////////////////////////////////////
    public void updateGroup(String name, File archive, String uid, String version, String uriPrefix) throws IOException {
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir = registries.get(name);
        final File componentsDir = new File(registryDir, COMPONENTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml = new File(registryDir, REGISTRY_XML);
        
        String string = null;
        
        try {
            final ProductRegistry registry = new ProductRegistry(registryXml);
            
            final File descriptor = new File(groupsDir,
                    FileUtils.getJarAttribute(archive, "Group-Descriptor"));
            
            FileUtils.unjar(archive, groupsDir);
            
            FileUtils.modifyFile(descriptor,
                    "(\\<icon.*\\>)resource:(.*\\<\\/icon\\>)",
                    "$1" + groupsDir.toURI() + "$2", true);
            
            final ProductGroup group = new ProductGroup().loadFromDom(
                    XMLUtils.loadXMLDocument(descriptor).getDocumentElement());
            final ProductGroup existing = registry.getProductGroup(
                    group.getUid());
            
            if (existing != null) {
                existing.getParent().removeChild(existing);
                
                Queue<ProductTreeNode> nodes = new LinkedList<ProductTreeNode>();
                nodes.offer(existing);
                
                while(nodes.peek() != null) {
                    ProductTreeNode node = nodes.poll();
                    
                    if (node instanceof ProductComponent) {
                        ProductComponent temp = (ProductComponent) node;
                        FileUtils.deleteFile(new File(componentsDir, temp.getUid() + "/" + temp.getVersion()));
                    }
                    
                    if (node instanceof ProductGroup) {
                        ProductGroup temp = (ProductGroup) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (ProductTreeNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
                
                // we need to unjar again, since the removal procedure has deleted
                // all the files from the newly updated group
                FileUtils.unjar(archive, groupsDir);
            }
            
            ProductTreeNode parent =
                    registry.getProductComponent(uid, new Version(version));
            if (parent == null) {
                parent = registry.getProductGroup(uid);
                if (parent == null) {
                    parent = registry.getProductTreeRoot();
                }
            }
            
            parent.addChild(group);
            
            string = new URI(group.getIconUri()).getSchemeSpecificPart();
            string = string.substring(groupsDir.toURI().getSchemeSpecificPart().length());
            string = URLEncoder.encode("groups/" + string, "UTF-8");
            group.setIconUri(uriPrefix + string);
            
            registry.saveProductRegistry(registryXml, new TrueFilter());
            
            archive.delete();
            descriptor.delete();
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new IOException("Could not update group");
        } catch (XMLException e) {
            e.printStackTrace();
            throw new IOException("Could not update group");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IOException("Could not update group");
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new IOException("Could not update group");
        }
    }
    
    public void removeGroup(String name, String uid) throws IOException {
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir = registries.get(name);
        final File componentsDir = new File(registryDir, COMPONENTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml = new File(registryDir, REGISTRY_XML);
        
        try {
            final ProductRegistry registry = new ProductRegistry(registryXml);
            
            final ProductGroup existing = registry.getProductGroup(uid);
            
            if (existing != null) {
                existing.getParent().removeChild(existing);
                
                Queue<ProductTreeNode> nodes = new LinkedList<ProductTreeNode>();
                nodes.offer(existing);
                
                while(nodes.peek() != null) {
                    ProductTreeNode node = nodes.poll();
                    
                    if (node instanceof ProductComponent) {
                        ProductComponent temp = (ProductComponent) node;
                        FileUtils.deleteFile(new File(componentsDir, temp.getUid() + "/" + temp.getVersion()));
                    }
                    
                    if (node instanceof ProductGroup) {
                        ProductGroup temp = (ProductGroup) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (ProductTreeNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            registry.saveProductRegistry(registryXml, new TrueFilter());
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new IOException("Could not remove component");
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new IOException("Could not remove component");
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public File getFile(String name, String file) throws IOException {
        if (registries.get(name) == null) {
            addRegistry(name);
        }
        
        final File registryDir = registries.get(name);
        
        return new File(registryDir, file);
    }
    
    public ProductTreeNode getRoot(String... names) throws IOException {
        if (names.length > 0) {
            List<File> files = new LinkedList<File>();
            
            for (String name: names) {
                if (registries.get(name) == null) {
                    addRegistry(name);
                }
                
                
                files.add(new File(registries.get(name), REGISTRY_XML));
            }
            
            try {
                return new ProductRegistry(files).getProductTreeRoot();
            } catch (InitializationException e) {
                e.printStackTrace();
                throw new IOException("Could not load registry");
            }
        }
        
        return null;
    }
    
    public List<ProductComponent> getComponents(String... names) throws IOException {
        List<ProductComponent> components = new LinkedList<ProductComponent>();
        
        if (names.length > 0) {
            List<File> files = new LinkedList<File>();
            
            for (String name: names) {
                if (registries.get(name) == null) {
                    addRegistry(name);
                }
                
                
                files.add(new File(registries.get(name), REGISTRY_XML));
            }
            
            try {
                components.addAll(new ProductRegistry(files).
                        queryComponents(new TrueFilter()));
            } catch (InitializationException e) {
                e.printStackTrace();
                throw new IOException("Could not load registry");
            }
        }
        
        return components;
    }
    
    public File createBundle(String[] names, String[] components) throws IOException {
        try {
            File statefile = FileUtils.createTempFile(TEMP, false);
            File bundle    = FileUtils.createTempFile(TEMP, false);
            
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
            
            ProductRegistry registry = new ProductRegistry(files);
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
                    "--ignore-lock");
            
            statefile.delete();
            
            if (results.getErrorCode() != 0) {
                throw new IOException("Could not load registry");
            }
            
            return bundle;
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new IOException("Could not load registry");
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new IOException("Could not load registry");
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void loadRegistriesList() throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(REGISTRIES_LIST)));
        
        String line = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            registries.put(line, initializeRegistry(line));
        }
        
        reader.close();
    }
    
    private void saveRegistriesList() throws IOException {
        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(REGISTRIES_LIST)));
        
        try {
            for (String key: registries.keySet()) {
                writer.println(key);
            }
        } finally {
            writer.close();
        }
    }
    
    private File initializeRegistry(String name) throws IOException {
        File directory   = new File(REGISTRIES, name);
        File registryxml = new File(directory, REGISTRY_XML);
        
        directory.mkdirs();
        
        if (!registryxml.exists()) {
            try {
                Document document = ProductRegistry.
                        getInstance().getEmptyRegistryDocument();
                XMLUtils.saveXMLDocument(document, registryxml);
            } catch (XMLException e) {
                e.printStackTrace();
                throw new IOException("Cannot initialize registry");
            }
        }
        
        return directory;
    }
}
