// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
// </editor-fold>
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
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


/**
 *
 * @author Peter Williams
 */
public final class GlassfishInstanceProvider implements ServerInstanceProvider {

    static final String DIR_GLASSFISH_INSTANCES = "/GlassFish/Instances"; //NOI18N
    static final String INSTANCE_FO_ATTR = "InstanceFOPath"; // NOI18N
    private static GlassfishInstanceProvider singleton;
    
    private final Map<String, GlassfishInstance> instanceMap = 
            Collections.synchronizedMap(new HashMap<String, GlassfishInstance>());
    private final ChangeSupport support = new ChangeSupport(this);

    private GlassfishInstanceProvider() {
        try {
            registerDefaultInstance();
            loadServerInstances();
        } catch(Exception ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex);
        }
    }

    public static synchronized boolean initialized() {
        return singleton != null;
    }

    public static synchronized GlassfishInstanceProvider getDefault() {
        if(singleton == null) {
            singleton = new GlassfishInstanceProvider();
        }
        return singleton;
    }

    public static Logger getLogger() {
        return Logger.getLogger("glassfish");
    }

    public void addServerInstance(GlassfishInstance si) {
        synchronized(instanceMap) {
            try {
                instanceMap.put(si.getDeployerUri(), si);
                writeInstanceToFile(si);
            } catch(IOException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, null, ex);
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

    // ------------------------------------------------------------------------
    // Internal use only.  Used by Installer.close() to quickly identify and
    // shutdown any instances we started during this IDE session.
    // ------------------------------------------------------------------------
    Collection<GlassfishInstance> getInternalInstances() {
        return instanceMap.values();
    }
    
    // ------------------------------------------------------------------------
    // Persistence for server instances.
    // ------------------------------------------------------------------------
    private void loadServerInstances() {
        FileObject dir = getRepositoryDir(DIR_GLASSFISH_INSTANCES, false);
        if(dir != null) {
            FileObject[] instanceFOs = dir.getChildren();
            if(instanceFOs != null && instanceFOs.length > 0) {
                for(int i = 0; i < instanceFOs.length; i++) {
                    try {
                        GlassfishInstance si = readInstanceFromFile(instanceFOs[i]);
                        if(si != null) {
                            instanceMap.put(si.getDeployerUri(), si);
                        } else {
                            Logger.getLogger("glassfish").finer("Unable to create glassfish instance for " + instanceFOs[i].getPath());
                        }
                    } catch(IOException ex) {
                        Logger.getLogger("glassfish").log(Level.INFO, null, ex);
                    }
                }
            }
        }
    }

    private GlassfishInstance readInstanceFromFile(FileObject instanceFO) throws IOException {
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
            instance = GlassfishInstance.create(ip);
        } else {
            Logger.getLogger("glassfish").finer("GlassFish folder " + instanceFO.getPath() + " is not a valid V3 install.");
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
            FileObject dir = getRepositoryDir(DIR_GLASSFISH_INSTANCES, true);
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
        FileObject dir = getRepositoryDir(DIR_GLASSFISH_INSTANCES, false);
        if(dir != null) {
            FileObject[] installedServers = dir.getChildren();
            for(int i = 0; i < installedServers.length; i++) {
                String val = getStringAttribute(installedServers[i], GlassfishModule.URL_ATTR);
                if(val != null && val.equals(url)) {
                    return installedServers[i];
                }
            }
        }
        return null;
    }

    private FileObject getRepositoryDir(String path, boolean create) {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject dir = fs.findResource(path);
        if(dir == null && create) {
            try {
                dir = FileUtil.createFolder(fs.getRoot(), path);
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
                Logger.getLogger("glassfish").log(Level.FINER, ex.getLocalizedMessage(), ex);
            }
        }
        return result;
    }
    
    private void registerDefaultInstance() {
        final boolean needToRegisterDefaultServer =
                !NbPreferences.forModule(this.getClass()).getBoolean(ServerUtilities.PROP_FIRST_RUN, false);

        if (needToRegisterDefaultServer) {
            try {
                final String INSTALL_ROOT_PROP_NAME = "org.glassfish.v3.installRoot";
                String candidate = System.getProperty(INSTALL_ROOT_PROP_NAME); //NOI18N

                if (null != candidate) {
                    File f = new File(candidate);
                    if (isValidHomeFolder(candidate) && f.exists()) {
                        Map<String, String> ip = new HashMap<String, String>();
                        ip.put(GlassfishModule.INSTALL_FOLDER_ATTR,
                                f.getCanonicalPath());
                        ip.put(GlassfishModule.GLASSFISH_FOLDER_ATTR,
                                f.getCanonicalPath() + File.separator + "glassfish");
                        if (f.canWrite()) {
                            ip.put(GlassfishModule.DISPLAY_NAME_ATTR,
                                NbBundle.getMessage(this.getClass(), "DEFAULT_DOMAIN_NAME"));
                            ip.put(GlassfishModule.HTTPPORT_ATTR,
                                    Integer.toString(8080));
                            ip.put(GlassfishModule.ADMINPORT_ATTR,
                                    Integer.toString(4848));
                            GlassfishInstance gi = GlassfishInstance.create(ip);
                            addServerInstance(gi);
                            NbPreferences.forModule(this.getClass()).putBoolean(ServerUtilities.PROP_FIRST_RUN, true);
                        } else {
                            ip.put(GlassfishModule.DISPLAY_NAME_ATTR,
                                NbBundle.getMessage(this.getClass(), "PERSONAL_DOMAIN_NAME")); // NOI18N
                            String domainsFolderValue = System.getProperty("netbeans.user"); // NOI18N
                            String domainNameValue = "GlassfishV3Domain";    // NOI18N
                            ip.put(GlassfishModule.DOMAINS_FOLDER_ATTR, domainsFolderValue);
                            ip.put(GlassfishModule.DOMAIN_NAME_ATTR, domainNameValue);
                            
                            CreateDomain cd = new CreateDomain("anonymous", "", new File(f,"glassfish"), ip);
                            cd.start();
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
