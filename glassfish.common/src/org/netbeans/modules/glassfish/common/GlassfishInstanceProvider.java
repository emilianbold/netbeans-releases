/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.RegisteredDDCatalog;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.spi.Utils;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Peter Williams
 * @author vince kraemer
 */
public final class GlassfishInstanceProvider implements ServerInstanceProvider {

    static final String INSTANCE_FO_ATTR = "InstanceFOPath"; // NOI18N
    private volatile static GlassfishInstanceProvider singleton;
    private volatile static GlassfishInstanceProvider singletonEe6;

    public static GlassfishInstanceProvider getEe6() {
        String v3Root = System.getProperty("org.glassfish.v3ee6.installRoot");
        if ("true".equals(System.getProperty("org.glassfish.v3.enableExperimentalFeatures")) ||
               (null != v3Root && v3Root.trim().length() > 0) ) {
            if (singletonEe6 == null) {
                singletonEe6 = new GlassfishInstanceProvider(new String[] {"deployer:gfv3ee6"},
                        new String[] {"/GlassFishEE6/Instances"},
                        "GlassFish v3", "org.glassfish.v3ee6.installRoot",
                        "GlassFish v3 Domain",
                        "Personal GlassFish v3 Domain",
                        "GlassFish_v3", "http://java.net/download/glassfish/v3/promoted/latest-glassfish.zip",
                        "http://serverplugins.netbeans.org/glassfishv3/ee6zipfilename.txt",
                        "last-v3ee6-install-root",
                        new String[] { "lib"+File.separator+"schemas"+File.separator+"web-app_3_0.xsd" },
                        new String[0], true);
                RegisteredDDCatalog catalog = getDDCatalog();
                if (null != catalog) {
                    catalog.registerEE6RunTimeDDCatalog(singletonEe6);
                    refreshCatalogFromFirstInstance(singletonEe6, catalog);
                }
            }
        }
        return singletonEe6;
    }

    public static GlassfishInstanceProvider getPrelude() {
        if ("true".equals(System.getProperty("org.glassfish.v3.disablePreludeSupport"))) {
            return singleton;
        }
        String[] uriFragments;
        String[] instanceDirs;
        String v3Root = System.getProperty("org.glassfish.v3ee6.installRoot");
        if (!("true".equals(System.getProperty("org.glassfish.v3.enableExperimentalFeatures"))) &&
               (null == v3Root || v3Root.trim().length() < 1) ) {
            uriFragments = new String[] { "deployer:gfv3", "deployer:gfv3ee6" };
            instanceDirs = new String[] { "/GlassFish/Instances", "/GlassFishEE6/Instances" };
        } else {
            uriFragments = new String[] { "deployer:gfv3" };
            instanceDirs = new String[] { "/GlassFish/Instances" };
        }
        if(singleton == null) {
            singleton = new GlassfishInstanceProvider(uriFragments, instanceDirs,
                    "GlassFish v3 Prelude", "org.glassfish.v3.installRoot",
                        "GlassFish v3 Prelude Domain",
                        "Personal GlassFish v3 Prelude Domain",
                        "GlassFish_v3_Prelude", "http://java.net/download/glassfish/v3-prelude/release/glassfish-v3-prelude-ml.zip",
                        "http://serverplugins.netbeans.org/glassfishv3/preludezipfilename.txt",
                        "last-install-root", new String[0],
                        new String[] { "lib"+File.separator+"schemas"+File.separator+"web-app_3_0.xsd" }, false);
            RegisteredDDCatalog catalog = getDDCatalog();
            if (null != catalog) {
                catalog.registerPreludeRunTimeDDCatalog(singleton);
                refreshCatalogFromFirstInstance(singleton, catalog);
            }
        }
        return singleton;
    }
    
    private final Map<String, GlassfishInstance> instanceMap = 
            Collections.synchronizedMap(new HashMap<String, GlassfishInstance>());
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

    private GlassfishInstanceProvider(String[] uriFragments, String[] instancesDirNames,
            String displayName, String propName, String defaultName, String personalName,
            String installName, String direct, String indirect, String prefKey,
            String[] requiredFiles, String[] excludedFiles, boolean needsJdk6) {
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
        try {
            registerDefaultInstance();
            loadServerInstances();
        } catch(RuntimeException ex) {
            getLogger().log(Level.INFO, null, ex);
        }
    }

    public static synchronized boolean initialized() {
        return singleton != null || singletonEe6 != null;
    }

    public static Logger getLogger() {
        return Logger.getLogger("glassfish");
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
        return requiredFiles;
    }

    public String[] getExcludedFiles() {
        return excludedFiles;
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
        return instanceMap.get(uri);
    }

    public <T> T getInstanceByCapability(String uri, Class <T> serverFacadeClass) {
        T result = null;
        GlassfishInstance instance = instanceMap.get(uri);
        if(instance != null) {
            result = instance.getLookup().lookup(serverFacadeClass);
        }
        return result;
    }
    
    public <T> List<T> getInstancesByCapability(Class<T> serverFacadeClass) {
        List<T> result = new ArrayList<T>();
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
    public List<ServerInstance> getInstances() {
//        return new ArrayList<ServerInstance>(instanceMap.values());
        List<ServerInstance> result = new  ArrayList<ServerInstance>();
        synchronized (instanceMap) {
            for (GlassfishInstance instance : instanceMap.values()) {
                result.add(instance.getCommonInstance());
            }
        }
        return result;
    }
    
    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    // Additional interesting API's
    public boolean hasServer(String uri) {
        return getInstance(uri) != null;
    }
    
    public ServerInstance getInstance(String uri) {
//        return instanceMap.get(uri);
        GlassfishInstance instance = instanceMap.get(uri);
        return instance == null ? null : instance.getCommonInstance();
    }

    public String formatUri(String glassfishRoot, String hostName, int httpPort) {
        return "[" + glassfishRoot + "]"+uriFragments[0]+":" + hostName + ":" + httpPort;
    }

    String getInstancesDirName() {
        return instancesDirNames[0];
    }

    // ------------------------------------------------------------------------
    // Internal use only.  Used by Installer.close() to quickly identify and
    // shutdown any instances we started during this IDE session.
    // ------------------------------------------------------------------------
    Collection<GlassfishInstance> getInternalInstances() {
        return instanceMap.values();
    }

    boolean requiresJdk6OrHigher() {
        return needsJdk6;
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
                            } else {
                                getLogger().finer("Unable to create glassfish instance for " + instanceFOs[i].getPath());
                            }
                        } catch(IOException ex) {
                            getLogger().log(Level.INFO, null, ex);
                        }
                    }
                }
            }
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
            instance = GlassfishInstance.create(ip,this);
        } else {
            getLogger().finer("GlassFish folder " + instanceFO.getPath() + " is not a valid install.");
            instanceFO.delete();
        }

        return instance;
    }

    private void writeInstanceToFile(GlassfishInstance instance) throws IOException {
        String glassfishRoot = instance.getGlassfishRoot();
        if(glassfishRoot == null) {
            getLogger().log(Level.SEVERE, NbBundle.getMessage(GlassfishInstanceProvider.class, "MSG_NullServerFolder"));
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
                String name = FileUtil.findFreeFileName(dir, "instance", null);
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
        }
        return result;    
    }
    
    private static boolean isValidGlassfishFolder(String folderName) {
        boolean result = false;
        if(folderName != null) {
            File f = new File(folderName);
            // !PW FIXME better heuristics to identify a valid V3 install
            result = f.exists();
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
    
    private static int getIntAttribute(FileObject fo, String attrName, int defValue) {
        int result = defValue;
        String value = getStringAttribute(fo, attrName);
        if(value != null) {
            try {
                result = Integer.parseInt(value);
            } catch(NumberFormatException ex) {
                getLogger().log(Level.FINER, ex.getLocalizedMessage(), ex);
            }
        }
        return result;
    }
    
    private void registerDefaultInstance() {
        final boolean needToRegisterDefaultServer =
                !NbPreferences.forModule(this.getClass()).getBoolean(ServerUtilities.PROP_FIRST_RUN, false);

        if (needToRegisterDefaultServer) {
            try {
                String candidate = System.getProperty(installRootPropName); //NOI18N

                if (null != candidate) {
                    File f = new File(candidate);
                    if (isValidHomeFolder(candidate) && f.exists()) {
                        Map<String, String> ip = new HashMap<String, String>();
                        ip.put(GlassfishModule.INSTALL_FOLDER_ATTR,
                                f.getCanonicalPath());
                        ip.put(GlassfishModule.GLASSFISH_FOLDER_ATTR,
                                f.getCanonicalPath() + File.separator + "glassfish");
                        if (Utils.canWrite(f)) { // f.canWrite()) {
                            ip.put(GlassfishModule.DISPLAY_NAME_ATTR, defaultDomainName);
                            ip.put(GlassfishModule.HTTPPORT_ATTR,
                                    Integer.toString(8080));
                            ip.put(GlassfishModule.ADMINPORT_ATTR,
                                    Integer.toString(4848));
                            GlassfishInstance gi = GlassfishInstance.create(ip,this);
                            addServerInstance(gi);
                            NbPreferences.forModule(this.getClass()).putBoolean(ServerUtilities.PROP_FIRST_RUN, true);
                        } else {
                            ip.put(GlassfishModule.DISPLAY_NAME_ATTR, defaultPersonalDomainName);
                            String domainsFolderValue = System.getProperty("netbeans.user"); // NOI18N
                            String domainNameValue = "Glassfishv3PreludeDomain";    // NOI18N
                            ip.put(GlassfishModule.DOMAINS_FOLDER_ATTR, domainsFolderValue);
                            ip.put(GlassfishModule.DOMAIN_NAME_ATTR, domainNameValue);
                            
                            CreateDomain cd = new CreateDomain("anonymous", "", new File(f,"glassfish"), ip, this);
                            cd.start();
                        }
                    }
                }
            } catch (IOException ex) {
                getLogger().log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
