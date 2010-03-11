/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.RegisteredDDCatalog;
import org.netbeans.modules.glassfish.spi.ServerCommand;
import org.netbeans.modules.glassfish.spi.ServerCommand.SetPropertyCommand;
import org.netbeans.modules.glassfish.spi.CommandFactory;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 * @author vince kraemer
 */
public final class GlassfishInstanceProvider implements ServerInstanceProvider {

    static final String INSTANCE_FO_ATTR = "InstanceFOPath"; // NOI18N
    private volatile static GlassfishInstanceProvider preludeProvider;
    private volatile static GlassfishInstanceProvider ee6Provider;

    static private String EE6_PROP_ROOT = "org.glassfish.v3ee6."; // NOI18N
    static private String INSTALL_ROOT_SUFFIX = "installRoot"; // NOI18N
    static private String EE6_INSTALL_ROOT_PROP = EE6_PROP_ROOT + INSTALL_ROOT_SUFFIX;
    static private String PRELUDE_PROP_ROOT = "org.glassfish.v3."; // NOI18N
    static private String EE6_DEPLOYER_FRAGMENT = "deployer:gfv3ee6"; // NOI18N
    static private String PRELUDE_DEPLOYER_FRAGMENT = "deployer:gfv3"; // NOI18N
    static private String EE6_INSTANCES_PATH = "/GlassFishEE6/Instances"; // NOI18N
    static private String PRELUDE_INSTANCES_PATH = "/GlassFish/Instances"; // NOI18N

    static public String PRELUDE_DEFAULT_NAME = "GlassFish_v3_Prelude"; //NOI18N
    
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

    public static synchronized GlassfishInstanceProvider getEe6() {
        if (ee6Provider == null) {
            ee6Provider = new GlassfishInstanceProvider(new String[]{EE6_DEPLOYER_FRAGMENT},
                    new String[]{EE6_INSTANCES_PATH},
                    org.openide.util.NbBundle.getMessage(GlassfishInstanceProvider.class, "STR_V3_SERVER_NAME", new Object[]{}), // NOI18N
                    EE6_INSTALL_ROOT_PROP,
                    org.openide.util.NbBundle.getMessage(GlassfishInstanceProvider.class, "STR_V3_AUTO_REGISTERED_NAME", new Object[]{}),  // NOI18N
                    org.openide.util.NbBundle.getMessage(GlassfishInstanceProvider.class, "STR_V3_AUTO_CREATED_NAME", new Object[]{}),  // NOI18N
                    "GlassFish_v3", // NOI18N
                    "http://java.net/download/glassfish/v3/release/glassfish-v3.zip", // NOI18N
                    "http://serverplugins.netbeans.org/glassfishv3/post68v3.txt", // NOI18N
                    "last-v3ee6-install-root", // NOI18N
                    new String[]{"lib" + File.separator + "schemas" + File.separator + "web-app_3_0.xsd"}, // NOI18N
                    new String[0],
                    true, new String[]{"docs/javaee6-doc-api.zip"}, // NOI18N
                    new String[]{"--nopassword"}, // NOI18N
                    new CommandFactory() {

                @Override
                public SetPropertyCommand getSetPropertyCommand(String name, String value) {
                    return new ServerCommand.SetPropertyCommand(name, value, "DEFAULT={0}={1}"); // NOI18N
                }
            });
            ee6Provider.init();
        }
        return ee6Provider;
    }

    public static synchronized GlassfishInstanceProvider getPrelude() {
        String[] uriFragments;
        String[] instanceDirs;
        uriFragments = new String[]{PRELUDE_DEPLOYER_FRAGMENT};
        instanceDirs = new String[]{PRELUDE_INSTANCES_PATH};
        if (preludeProvider == null) {
            preludeProvider = new GlassfishInstanceProvider(uriFragments, instanceDirs,
                    org.openide.util.NbBundle.getMessage(GlassfishInstanceProvider.class, "STR_PRELUDE_SERVER_NAME", new Object[]{}), // NOI18N
                    PRELUDE_PROP_ROOT + INSTALL_ROOT_SUFFIX,
                    org.openide.util.NbBundle.getMessage(GlassfishInstanceProvider.class, "STR_PRELUDE_AUTO_REGISTERED_NAME", new Object[]{}),  // NOI18N
                    org.openide.util.NbBundle.getMessage(GlassfishInstanceProvider.class, "STR_PRELUDE_AUTO_CREATED_NAME", new Object[]{}),  // NOI18N
                    PRELUDE_DEFAULT_NAME, 
                    "http://java.net/download/glassfish/v3-prelude/release/glassfish-v3-prelude-ml.zip", // NOI18N
                    "http://serverplugins.netbeans.org/glassfishv3/post68prelude.txt", // NOI18N
                    "last-install-root", // NOI18N
                    new String[0],
                    new String[]{"lib" + File.separator + "schemas" + File.separator + "web-app_3_0.xsd"}, // NOI18N
                    false,
                    new String[]{"docs/javaee6-doc-api.zip"}, // NOI18N
                    null,
                    new CommandFactory() {

                @Override
                        public SetPropertyCommand getSetPropertyCommand(String name, String value) {
                            return new ServerCommand.SetPropertyCommand(name, value, "target={0}&value={1}"); // NOI18N
                        }
                    });
            preludeProvider.init();
        }
        return preludeProvider;
    }

    public static final Set<String> activeRegistrationSet = Collections.synchronizedSet(new HashSet<String>());
    
    private final Map<String, GlassfishInstance> instanceMap =
            Collections.synchronizedMap(new HashMap<String, GlassfishInstance>());
    private static final Set<String> activeDisplayNames = Collections.synchronizedSet(new HashSet<String>());
    private final ChangeSupport support = new ChangeSupport(this);

    private String[] instancesDirNames;
    private String displayName;
    private String[] uriFragments;
    private String installRootPropName;
    private String defaultDomainName;
    private String defaultPersonalDomainName;
    private String defaultInstallName;
    private String directDownloadUrl;
    private String indirectDownloadUrl;
    private String installRootKey;
    private String[] requiredFiles;
    private String[] excludedFiles;
    private boolean needsJdk6;
    private String[] javadocFilenames;
    private List<String> noPasswordOptions;
    private CommandFactory cf;

    private GlassfishInstanceProvider(String[] uriFragments, String[] instancesDirNames,
            String displayName, String propName, String defaultName, String personalName,
            String installName, String direct, String indirect, String prefKey,
            String[] requiredFiles, String[] excludedFiles, boolean needsJdk6,
            String[] javadocFilenames,
            String[] noPasswordOptionsArray, CommandFactory cf) {
        this.instancesDirNames = instancesDirNames;
        this.displayName = displayName;
        this.uriFragments = uriFragments;
        this.installRootPropName = propName;
        this.defaultDomainName = defaultName;
        this.defaultPersonalDomainName = personalName;
        this.defaultInstallName = installName;
        this.directDownloadUrl = direct;
        this.indirectDownloadUrl = indirect;
        this.installRootKey = prefKey;
        this.requiredFiles = requiredFiles;
        this.excludedFiles = excludedFiles;
        this.needsJdk6 = needsJdk6;
        this.javadocFilenames = javadocFilenames;
        this.noPasswordOptions = new ArrayList<String>();
        if (null != noPasswordOptionsArray) {
            noPasswordOptions.addAll(Arrays.asList(noPasswordOptionsArray));
        }
        this.cf = cf;
    }

    public static synchronized boolean initialized() {
        return preludeProvider != null || ee6Provider != null;
    }

    public static Logger getLogger() {
        return Logger.getLogger("glassfish"); // NOI18N
    }

    private static RegisteredDDCatalog getDDCatalog() {
        return Lookup.getDefault().lookup(RegisteredDDCatalog.class);
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
                writeInstanceToFile(si);
            } catch(IOException ex) {
                getLogger().log(Level.INFO, null, ex);
            }
        }

        support.fireChange();
    }

    public String getDefaultInstallName() {
        return defaultInstallName;
    }

    public String getDirectDownloadUrl() {
        return directDownloadUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIndirectDownloadUrl() {
        return indirectDownloadUrl;
    }

    public String getInstallRootKey() {
        return installRootKey;
    }

    public String getInstallRootProperty() {
        return installRootPropName;
    }

    public String[] getRequiredFiles() {
        return requiredFiles.clone();
    }

    public String[] getExcludedFiles() {
        return excludedFiles.clone();
    }
    
    public String getNameOfBits() {
        return displayName;
    }

    public String getUriFragment() {
        return uriFragments[0];
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
                if (instanceMap.size() == 0) {
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
                result.add(instance.getCommonInstance());
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
        GlassfishInstance instance = instanceMap.get(uri);
        return instance == null ? null : instance.getCommonInstance();
    }

    public String formatUri(String glassfishRoot, String hostName, int httpPort) {
        return "[" + glassfishRoot + "]"+uriFragments[0]+":" + hostName + ":" + httpPort; // NOI18N
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
        for (int j = 0; j < instancesDirNames.length ; j++ ) {
            FileObject dir = getRepositoryDir(instancesDirNames[j], false);
            if(dir != null) {
                FileObject[] instanceFOs = dir.getChildren();
                if(instanceFOs != null && instanceFOs.length > 0) {
                    for(int i = 0; i < instanceFOs.length; i++) {
                        try {
                            GlassfishInstance si = readInstanceFromFile(instanceFOs[i],uriFragments[j]);
                            if(si != null) {
                                instanceMap.put(si.getDeployerUri(), si);
                                activeDisplayNames.add(si.getDisplayName());
                            } else {
                                getLogger().finer("Unable to create glassfish instance for " + instanceFOs[i].getPath()); // NOI18N
                            }
                        } catch(IOException ex) {
                            getLogger().log(Level.INFO, null, ex);
                        }
                    }
                }
            }
        }
        for (GlassfishInstance gi : instanceMap.values()) {
            gi.updateModuleSupport();
        }
    }

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
            getLogger().finer("GlassFish folder " + instanceFO.getPath() + " is not a valid install."); // NOI18N
            instanceFO.delete();
        }

        return instance;
    }

    private void writeInstanceToFile(GlassfishInstance instance) throws IOException {
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

            for(int i = 0; i < instanceFOs.length; i++) {
                if(url.equals(instanceFOs[i].getAttribute(GlassfishModule.URL_ATTR))) {
                    instanceFO = instanceFOs[i];
                }
            }

            if(instanceFO == null) {
                String name = FileUtil.findFreeFileName(dir, "instance", null); // NOI18N
                instanceFO = dir.createData(name);
            }

            CommonServerSupport css = instance.getCommonSupport();
            Map<String, String> attrMap = css.getInstanceProperties();
            for(Map.Entry<String, String> entry: attrMap.entrySet()) {
                String key = entry.getKey();
                if(!filterKey(key)) {
                    instanceFO.setAttribute(key, entry.getValue());
                }
            }
            
            css.setProperty(INSTANCE_FO_ATTR, instanceFO.getName());
            css.setFileObject(instanceFO);
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
                    if(val != null && val.equals(url)) {
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
        
    String[] getNoPasswordCreatDomainCommand(String startScript, String jarLocation, String domainDir, String portBase, String uname, String domain) {
        List<String> retVal = new ArrayList<String>();
        retVal.addAll(Arrays.asList(new String[] {startScript,
                    "-client",  // NOI18N
                    "-jar",  // NOI18N
                    jarLocation,
                    "create-domain", //NOI18N
                    "--user", //NOI18N
                    uname,
                    "--domaindir", //NOI18N
                    domainDir,
                    "--portbase", //NOI18N
                    portBase,}));
        if (noPasswordOptions.size() > 0) {
            retVal.addAll(noPasswordOptions);
        }
        retVal.add(domain);
        return retVal.toArray(new String[retVal.size()]);
    }

    CommandFactory getCommandFactory() {
       return cf;
    }
}
