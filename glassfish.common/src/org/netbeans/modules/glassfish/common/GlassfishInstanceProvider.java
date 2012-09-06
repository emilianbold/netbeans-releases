/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.swing.event.ChangeListener;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.spi.CommandFactory;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.RegisteredDDCatalog;
import org.netbeans.modules.glassfish.spi.ServerCommand;
import org.netbeans.modules.glassfish.spi.ServerCommand.SetPropertyCommand;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Peter Williams
 * @author vince kraemer
 */
public final class GlassfishInstanceProvider implements ServerInstanceProvider, LookupListener {
    public static final String GLASSFISH_AUTOREGISTERED_INSTANCE = "glassfish_autoregistered_instance";

    static final String INSTANCE_FO_ATTR = "InstanceFOPath"; // NOI18N
    private static final String AUTOINSTANCECOPIED = "autoinstance-copied"; // NOI18N

    private volatile static GlassfishInstanceProvider ee6Provider;
    private volatile static GlassfishInstanceProvider preludeProvider;

    public static final String EE6_DEPLOYER_FRAGMENT = "deployer:gfv3ee6"; // NOI18N
    public static final String EE6WC_DEPLOYER_FRAGMENT = "deployer:gfv3ee6wc"; // NOI18N
    public static final String PRELUDE_DEPLOYER_FRAGMENT = "deployer:gfv3"; // NOI18N
    static private String EE6_INSTANCES_PATH = "/GlassFishEE6/Instances"; // NOI18N
    static private String EE6WC_INSTANCES_PATH = "/GlassFishEE6WC/Instances"; // NOI18N
    static private String PRELUDE_INSTANCES_PATH = "/GlassFish/Instances"; // NOI18N

    static public String PRELUDE_DEFAULT_NAME = "GlassFish_v3_Prelude"; //NOI18N
    static public String EE6WC_DEFAULT_NAME = "GlassFish_Server_3.1"; // NOI18N

    public static List<GlassfishInstanceProvider> getProviders(boolean initialize) {
        List<GlassfishInstanceProvider> providerList = new ArrayList<GlassfishInstanceProvider>();
        if(initialize) {
            getPrelude();
            getEe6();
        }
        if(preludeProvider != null) {
            providerList.add(preludeProvider);
        }
        if(ee6Provider != null) {
            providerList.add(ee6Provider);
        }
        return providerList;
    }
    
    public static GlassfishInstanceProvider getEe6() {
        if (ee6Provider != null) {
            return ee6Provider;
        }
        else {
            boolean runInit = false;
            synchronized(GlassfishInstanceProvider.class) {
                if (ee6Provider == null) {
                    runInit = true;
                    ee6Provider = new GlassfishInstanceProvider(
                            new String[]{EE6_DEPLOYER_FRAGMENT, EE6WC_DEPLOYER_FRAGMENT},
                            new String[]{EE6_INSTANCES_PATH, EE6WC_INSTANCES_PATH},
                            null,
                            true, 
                            new String[]{"docs/javaee6-doc-api.zip"}, // NOI18N
                            new String[]{"--nopassword"}, // NOI18N
                            new CommandFactory()  {

                        @Override
                        public SetPropertyCommand getSetPropertyCommand(String name, String value) {
                            return new ServerCommand.SetPropertyCommand(name, value,
                                    "DEFAULT={0}={1}"); // NOI18N
                        }

                    });
                }
            }
            if (runInit) {
                ee6Provider.init();                
            }
            return ee6Provider;
        }
    }

    public static GlassfishInstanceProvider getPrelude() {
        if (preludeProvider != null) {
            return preludeProvider;
        }
        else {
            boolean runInit = false;
            synchronized(GlassfishInstanceProvider.class) {
                if (preludeProvider == null) {
                    runInit = true;
                    preludeProvider = new GlassfishInstanceProvider(
                            new String[]{PRELUDE_DEPLOYER_FRAGMENT},
                            new String[]{PRELUDE_INSTANCES_PATH},
                            org.openide.util.NbBundle.getMessage(GlassfishInstanceProvider.class,
                                "STR_PRELUDE_SERVER_NAME", new Object[]{}), // NOI18N
                            false,
                            new String[]{"docs/javaee6-doc-api.zip"}, // NOI18N
                            null,
                            new CommandFactory()  {

                                @Override
                                public SetPropertyCommand getSetPropertyCommand(String name, String value) {
                                    return new ServerCommand.SetPropertyCommand(name, value,
                                            "target={0}&value={1}"); // NOI18N
                                }

                            });
                }
            }
            if (runInit) {
                preludeProvider.init();                
            }
            return preludeProvider;
        }
    }

    public static final Set<String> activeRegistrationSet = Collections.synchronizedSet(new HashSet<String>());
    
    private final Map<String, GlassfishInstance> instanceMap =
            Collections.synchronizedMap(new HashMap<String, GlassfishInstance>());
    private static final Set<String> activeDisplayNames = Collections.synchronizedSet(new HashSet<String>());
    private final ChangeSupport support = new ChangeSupport(this);

    final private String[] instancesDirNames;
    final private String displayName;
    final private String[] uriFragments;
    final private boolean needsJdk6;
    final private String[] javadocFilenames;
    final private List<String> noPasswordOptions;
    final private CommandFactory cf;
    final private Lookup.Result<RegisteredDDCatalog> lookupResult = Lookups.forPath(Util.GF_LOOKUP_PATH).lookupResult(RegisteredDDCatalog.class);
    
    @SuppressWarnings("LeakingThisInConstructor")
    private GlassfishInstanceProvider(
            String[] uriFragments, 
            String[] instancesDirNames,
            String displayName, 
            boolean needsJdk6,
            String[] javadocFilenames,
            String[] noPasswordOptionsArray, 
            CommandFactory cf 
            ) {
        this.instancesDirNames = instancesDirNames;
        this.displayName = displayName;
        this.uriFragments = uriFragments;
        this.needsJdk6 = needsJdk6;
        this.javadocFilenames = javadocFilenames;
        this.noPasswordOptions = new ArrayList<String>();
        if (null != noPasswordOptionsArray) {
            noPasswordOptions.addAll(Arrays.asList(noPasswordOptionsArray));
        }
        this.cf = cf;
        lookupResult.allInstances();
        
        lookupResult.addLookupListener(this); 
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Logger.getLogger("glassfish").log(Level.FINE, "***** resultChanged fired ********  {0}", hashCode()); // NOI18N
        RegisteredDDCatalog catalog = getDDCatalog();
        if (null != catalog) {
            if (this.equals(preludeProvider)) {
                catalog.registerPreludeRunTimeDDCatalog(this);
            } else {
                catalog.registerEE6RunTimeDDCatalog(this);
            }
        }
        refreshCatalogFromFirstInstance(this, getDDCatalog());
    }

    /**
     * Check providers initialization status.
     * <p>
     * @return <code>true</code> when at least one of the providers
     *         is initialized or <code>false</code> otherwise.
     */
    public static synchronized boolean initialized() {
        return preludeProvider != null || ee6Provider != null;
    }

    public static Logger getLogger() {
        return Logger.getLogger("glassfish"); // NOI18N
    }

    private static RegisteredDDCatalog getDDCatalog() {
        return Lookups.forPath(Util.GF_LOOKUP_PATH).lookup(RegisteredDDCatalog.class);
    }

    private static void refreshCatalogFromFirstInstance(GlassfishInstanceProvider gip, RegisteredDDCatalog catalog) {
        GlassfishInstance firstInstance = gip.getFirstServerInstance();
        if (null != firstInstance) {
            catalog.refreshRunTimeDDCatalog(gip, firstInstance.getGlassfishRoot());
        }
    }

    public String[] getAssociatedJavaDoc() {
        return javadocFilenames.clone();
    }

    private GlassfishInstance getFirstServerInstance() {
        if (!instanceMap.isEmpty()) {
            return instanceMap.values().iterator().next();
        }
        return null;
    }

    public void addServerInstance(GlassfishInstance si) {
        synchronized(instanceMap) {
            try {
                instanceMap.put(si.getDeployerUri(), si);
                activeDisplayNames.add(si.getDisplayName());
                if (instanceMap.size() == 1) { // only need to do if this first of this type
                    RegisteredDDCatalog catalog = getDDCatalog();
                    if (null != catalog) {
                        catalog.refreshRunTimeDDCatalog(this, si.getGlassfishRoot());
                    }
                }
                writeInstanceToFile(si,true);
            } catch(IOException ex) {
                getLogger().log(Level.INFO, null, ex);
            }
        }

        support.fireChange();
    }

    public boolean removeServerInstance(GlassfishInstance si) {
        boolean result = false;
        synchronized(instanceMap) {
            if(instanceMap.remove(si.getDeployerUri()) != null) {
                result = true;
                removeInstanceFromFile(si.getDeployerUri());
                activeDisplayNames.remove(si.getDisplayName());
                // If this was the last of its type, need to remove the
                // resolver catalog contents
                if (instanceMap.isEmpty()) {
                    RegisteredDDCatalog catalog = getDDCatalog();
                    if (null != catalog) {
                        catalog.refreshRunTimeDDCatalog(this, null);
                    }
                }
            }
        }

        if(result) {
            support.fireChange();
        }

        return result;
    }
    
    public Lookup getLookupFor(ServerInstance instance) {
        synchronized (instanceMap) {
            for (GlassfishInstance gfInstance : instanceMap.values()) {
                if (gfInstance.getCommonInstance().equals(instance)) {
                    return gfInstance.getLookup();
                }
            }
            return null;
        }
    }
    
    public ServerInstanceImplementation getInternalInstance(String uri) {
        //init();
        return instanceMap.get(uri);
    }

    public <T> T getInstanceByCapability(String uri, Class <T> serverFacadeClass) {
        T result = null;
        //init();
        GlassfishInstance instance = instanceMap.get(uri);
        if(instance != null) {
            result = instance.getLookup().lookup(serverFacadeClass);
        }
        return result;
    }
    
    public <T> List<T> getInstancesByCapability(Class<T> serverFacadeClass) {
        List<T> result = new ArrayList<T>();
        //init();
        synchronized (instanceMap) {
            for (GlassfishInstance instance : instanceMap.values()) {
                T serverFacade = instance.getLookup().lookup(serverFacadeClass);
                if(serverFacade != null) {
                    result.add(serverFacade);
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------------
    // ServerInstanceProvider interface implementation
    // ------------------------------------------------------------------------
    @Override
    public List<ServerInstance> getInstances() {
//        return new ArrayList<ServerInstance>(instanceMap.values());
        List<ServerInstance> result = new  ArrayList<ServerInstance>();
        //init();
        synchronized (instanceMap) {
            for (GlassfishInstance instance : instanceMap.values()) {
                ServerInstance si = instance.getCommonInstance();
                if (null != si) {
                    result.add(si);
                } else {
                    String message = "invalid commonInstance for " + instance.getDeployerUri(); // NOI18N
                    Logger.getLogger("glassfish").log(Level.WARNING, message);   // NOI18N
                    if (null != instance.getDeployerUri())
                        instanceMap.remove(instance.getDeployerUri());
                }
            }
        }
        return result;
    }
    
    @Override
    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    // Additional interesting API's
    public boolean hasServer(String uri) {
        return getInstance(uri) != null;
    }
    
    public ServerInstance getInstance(String uri) {
//        return instanceMap.get(uri);
        //init();
        ServerInstance rv = null;
        GlassfishInstance instance = instanceMap.get(uri);
        if (null != instance) {
            rv = instance.getCommonInstance();
            if (null == rv) {
                String message = "invalid commonInstance for " + instance.getDeployerUri(); // NOI18N
                Logger.getLogger("glassfish").log(Level.WARNING, message);
                if (null != instance.getDeployerUri())
                    instanceMap.remove(instance.getDeployerUri());
            }
        }
        return rv;
    }

    String getInstancesDirName() {
        return instancesDirNames[0];
    }

    // ------------------------------------------------------------------------
    // Internal use only.  Used by Installer.close() to quickly identify and
    // shutdown any instances we started during this IDE session.
    // ------------------------------------------------------------------------
    Collection<GlassfishInstance> getInternalInstances() {
        //init();
        return instanceMap.values();
    }

    boolean requiresJdk6OrHigher() {
        return needsJdk6;
    }

    private void init() {
        synchronized (instanceMap) {
                try {
                    loadServerInstances();
                } catch (RuntimeException ex) {
                    getLogger().log(Level.INFO, null, ex);
                }
                RegisteredDDCatalog catalog = getDDCatalog();
                if (null != catalog) {
                    if (this.equals(preludeProvider)) {
                        catalog.registerPreludeRunTimeDDCatalog(this);
                    } else {
                        catalog.registerEE6RunTimeDDCatalog(this);
                    }
                    refreshCatalogFromFirstInstance(this, catalog);
                }
        }

    }
    
    // ------------------------------------------------------------------------
    // Persistence for server instances.
    // ------------------------------------------------------------------------
    private void loadServerInstances() {
        FileObject installedInstance = null;
        int savedj = -1;
        for (int j = 0; j < instancesDirNames.length ; j++ ) {
            FileObject dir = getRepositoryDir(instancesDirNames[j], false);
            if(dir != null) {
                FileObject[] instanceFOs = dir.getChildren();
                if(instanceFOs != null && instanceFOs.length > 0) {
                    for(int i = 0; i < instanceFOs.length; i++) {
                        try {
                            if (GLASSFISH_AUTOREGISTERED_INSTANCE.equals(instanceFOs[i].getName())) {
                                installedInstance = instanceFOs[i];
                                savedj = j;
                                continue;
                            }
                            GlassfishInstance si = readInstanceFromFile(instanceFOs[i],uriFragments[j]);
                            if(si != null) {
                                activeDisplayNames.add(si.getDisplayName());
                            } else {
                                getLogger().log(Level.FINER, "Unable to create glassfish instance for {0}", // NOI18N
                                        instanceFOs[i].getPath()); 
                            }
                        } catch(IOException ex) {
                            getLogger().log(Level.INFO, null, ex);
                        }
                    }
                }
            }
        }
        if (null != installedInstance && null == NbPreferences.forModule(this.getClass()).get(AUTOINSTANCECOPIED,null)) {
            try {
                GlassfishInstance igi = readInstanceFromFile(installedInstance, uriFragments[savedj]);
                try {
                    NbPreferences.forModule(this.getClass()).put(AUTOINSTANCECOPIED, "true"); // NOI18N
                    NbPreferences.forModule(this.getClass()).flush();
                } catch (BackingStoreException ex) {
                    Logger.getLogger("glassfish").log(Level.INFO, "auto-registered instance may reappear", ex); // NOI18N
                }
                activeDisplayNames.add(igi.getDisplayName());
            } catch (IOException ex) {
                getLogger().log(Level.INFO, null, ex);
            }
        }
        for (GlassfishInstance gi : instanceMap.values()) {
            GlassfishInstance.updateModuleSupport(gi);
        }
    }

    // Password from keyring (GlassfishModule.PASSWORD_ATTR) is read on demand
    // using code in GlassfishInstance.Props class.
    private GlassfishInstance readInstanceFromFile(FileObject instanceFO, String uriFragment) throws IOException {
        GlassfishInstance instance = null;

        String installRoot = getStringAttribute(instanceFO, GlassfishModule.INSTALL_FOLDER_ATTR);
        String glassfishRoot = getStringAttribute(instanceFO, GlassfishModule.GLASSFISH_FOLDER_ATTR);
        
        // Existing installs may lack "installRoot", but glassfishRoot and 
        // installRoot are the same in that case.
        if(installRoot == null) {
            installRoot = glassfishRoot;
        }

        if(isValidHomeFolder(installRoot) && isValidGlassfishFolder(glassfishRoot)) {
            // collect attributes and pass to create()
            Map<String, String> ip = new HashMap<String, String>();
            Enumeration<String> iter = instanceFO.getAttributes();
            while(iter.hasMoreElements()) {
                String name = iter.nextElement();
                String value = getStringAttribute(instanceFO, name);
                ip.put(name, value);
            }
            ip.put(INSTANCE_FO_ATTR, instanceFO.getName());
            instance = GlassfishInstance.create(ip,this,false);
        } else {
            getLogger().log(Level.FINER, "GlassFish folder {0} is not a valid install.", instanceFO.getPath()); // NOI18N
            instanceFO.delete();
        }

        return instance;
    }

    private void writeInstanceToFile(GlassfishInstance instance,boolean search) throws IOException {
        String glassfishRoot = instance.getGlassfishRoot();
        if(glassfishRoot == null) {
            getLogger().log(Level.SEVERE, NbBundle.getMessage(GlassfishInstanceProvider.class, "MSG_NullServerFolder")); // NOI18N
            return;
        }

        String url = instance.getDeployerUri();

        // For GFV3 managed instance files
        {
            FileObject dir = getRepositoryDir(instancesDirNames[0], true);
            FileObject[] instanceFOs = dir.getChildren();
            FileObject instanceFO = null;

            for(int i = 0; search && i < instanceFOs.length; i++) {
                if(url.equals(instanceFOs[i].getAttribute(GlassfishModule.URL_ATTR))) {
                    instanceFO = instanceFOs[i];
                }
            }

            if(instanceFO == null) {
                String name = FileUtil.findFreeFileName(dir, "instance", null); // NOI18N
                instanceFO = dir.createData(name);
            }

            Map<String, String> attrMap = instance.getProperties();
            for(Map.Entry<String, String> entry: attrMap.entrySet()) {
                String key = entry.getKey();
                if(!filterKey(key)) {
                    Object currentValue = instanceFO.getAttribute(key);
                    if (null != currentValue && currentValue.equals(entry.getValue())) {
                        // do nothing
                    } else {
                        if (key.equals(GlassfishModule.PASSWORD_ATTR)) {
                            String serverName = attrMap.get(GlassfishModule.DISPLAY_NAME_ATTR);
                            String userName = attrMap.get(GlassfishModule.USERNAME_ATTR);
                            Keyring.save(GlassfishInstance.passwordKey(
                                    serverName, userName),
                                    entry.getValue().toCharArray(),
                                    "GlassFish administrator user password");
                        } else {
                            instanceFO.setAttribute(key, entry.getValue());
                        }
                    }
                }
            }
            
            instance.putProperty(INSTANCE_FO_ATTR, instanceFO.getName());
            instance.getCommonSupport().setFileObject(instanceFO);
        }
    }
    
    private static boolean filterKey(String key) {
        return INSTANCE_FO_ATTR.equals(key);
    }

    private void removeInstanceFromFile(String url) {
        FileObject instanceFO = getInstanceFileObject(url);
        if(instanceFO != null && instanceFO.isValid()) {
            try {
                instanceFO.delete();
            } catch(IOException ex) {
                getLogger().log(Level.INFO, null, ex);
            }
        }
    }

    private FileObject getInstanceFileObject(String url) {
        for (String instancesDirName : instancesDirNames) {
            FileObject dir = getRepositoryDir(instancesDirName, false);
            if(dir != null) {
                FileObject[] installedServers = dir.getChildren();
                for(int i = 0; i < installedServers.length; i++) {
                    String val = getStringAttribute(installedServers[i], GlassfishModule.URL_ATTR);
                    if(val != null && val.equals(url) &&
                            !GLASSFISH_AUTOREGISTERED_INSTANCE.equals(installedServers[i].getName())) {
                        return installedServers[i];
                    }
                }
            }
        }
        return null;
    }

    private FileObject getRepositoryDir(String path, boolean create) {
        FileObject dir = FileUtil.getConfigFile(path);
        if(dir == null && create) {
            try {
                dir = FileUtil.createFolder(FileUtil.getConfigRoot(), path);
            } catch(IOException ex) {
                getLogger().log(Level.INFO, null, ex);
            }
        }
        return dir;
    }

    private static boolean isValidHomeFolder(String folderName) {
        boolean result = false;
        if(folderName != null) {
            File f = new File(folderName);
            // !PW FIXME better heuristics to identify a valid V3 install
            result = f.exists();
            result = result && f.isDirectory();
            result = result && f.canRead();
        }
        return result;    
    }
    
    private static boolean isValidGlassfishFolder(String folderName) {
        boolean result = false;
        if(folderName != null) {
            File f = new File(folderName);
            // !PW FIXME better heuristics to identify a valid V3 install
            result = f.exists();
            result = result && f.isDirectory();
            result = result && f.canRead();
        }
        return result;    
    }

    private static String getStringAttribute(FileObject fo, String attrName) {
        return getStringAttribute(fo, attrName, null);
    }

    private static String getStringAttribute(FileObject fo, String attrName, String defValue) {
        String result = defValue;
        Object attr = fo.getAttribute(attrName);
        if(attr instanceof String) {
            result = (String) attr;
        }
        return result;
    }
        
    String[] getNoPasswordCreatDomainCommand(String startScript, String jarLocation, 
            String domainDir, String portBase, String uname, String domain) {
            List<String> retVal = new ArrayList<String>();
        retVal.addAll(Arrays.asList(new String[] {startScript,
                    "-client",  // NOI18N
                    "-jar",  // NOI18N
                    jarLocation,
                    "create-domain", //NOI18N
                    "--user", //NOI18N
                    uname,
                    "--domaindir", //NOI18N
                    domainDir}));
        if (null != portBase) {
            retVal.add("--portbase"); //NOI18N
            retVal.add(portBase);
        }
        if (noPasswordOptions.size() > 0) {
            retVal.addAll(noPasswordOptions);
        }
        retVal.add(domain);
        return retVal.toArray(new String[retVal.size()]);
    }

    CommandFactory getCommandFactory() {
       return cf;
    }

    String getDisplayName(String deployerUri) {
        if (null == displayName) {
            return deployerUri.contains(EE6WC_DEPLOYER_FRAGMENT) ?
                org.openide.util.NbBundle.getMessage(GlassfishInstanceProvider.class,
                    "STR_V31_SERVER_NAME", new Object[]{}) : // NOI18N
                org.openide.util.NbBundle.getMessage(GlassfishInstanceProvider.class,
                    "STR_V3_SERVER_NAME", new Object[]{}); // NOI18N
                    
        } else {
            return displayName;
        }
    }
}
