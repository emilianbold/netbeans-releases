/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import org.ini4j.Ini;
import org.netbeans.modules.subversion.util.FileUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * Handles the Subversions <b>servers</b> and <b>config</b> configuration files.</br>
 * Everytime the singleton instance is created are the values from the commandline clients
 * configuration directory merged into the Subversion modules configuration files. 
 * (registry on windows are ignored). 
 * Already present proxy setting values wan't be changed, 
 * the remaining values are always taken from the commandline clients configuration files. 
 * The only exception is the 'store-auth-creds' key, which is always set to 'no'.
 * 
 * @author Tomas Stupka
 */
public class SvnConfigFiles {    

    /** the only SvnConfigFiles instance */
    private static SvnConfigFiles instance;

    /** the Ini instance holding the configuration values stored in the <b>servers</b>
     * file used by the Subversion module */    
    private Ini svnServers = null;
    
    /** the Ini instance holding the configuration values stored in the <b>config</b>
     * file used by the Subversion module */
    private Ini config = null;

    private static final String UNIX_CONFIG_DIR = ".subversion/";                                                               // NOI18N
    private static final String GROUPS_SECTION = "groups";                                                                      // NOI18N
    private static final String GLOBAL_SECTION = "global";                                                                      // NOI18N
    private static final String WINDOWS_USER_APPDATA = getAPPDATA();
    private static final String WINDOWS_CONFIG_DIR = WINDOWS_USER_APPDATA + "\\Subversion";                                     // NOI18N
    private static final String WINDOWS_GLOBAL_CONFIG_DIR = getGlobalAPPDATA() + "\\Subversion";                                // NOI18N
    private static final List<String> DEFAULT_GLOBAL_IGNORES = 
            parseGlobalIgnores("*.o *.lo *.la #*# .*.rej *.rej .*~ *~ .#* .DS_Store");                                          // NOI18N
    
    private interface IniFilePatcher {
        void patch(Ini file);
    }

    /**
     * The value for the 'store-auth-creds' key in the config cofiguration file is alway set to 'no'
     * so the commandline client wan't create a file holding the authentication credentials when
     * a svn command is called. The reason for this is that the Subverion module holds the credentials
     * in files with the same format as the commandline client but with a different name.
     */
    private class ConfigIniFilePatcher implements IniFilePatcher {
        public void patch(Ini file) {
            // patch store-auth-creds to "no"
            Ini.Section auth = (Ini.Section) file.get("auth");                  // NOI18N
            if(auth == null) {
                auth = file.add("auth");                                        // NOI18N
            }
            auth.put("store-auth-creds", "no");                                 // NOI18N
        }
    }

    /**
     * Creates a new instance
     */
    private SvnConfigFiles() {      
        // copy config file        
        config = copyConfigFileToIDEConfigDir("config", new ConfigIniFilePatcher());    // NOI18N
        // get the system servers file 
        svnServers = loadSystemIniFile("servers");        
    }
    
    /**
     * Returns a singleton instance.
     *
     * @return the SvnConfigFiles instance
     */
    public static SvnConfigFiles getInstance() {
        if(instance==null) {
            instance = new SvnConfigFiles();                    
        }
        return instance;
    }

    /**
     * Stores the proxy host, port, username and password from the given  
     * {@link org.netbeans.modules.subversion.config.ProxyDescriptor} in the 
     * <b>servers</b> file used by the Subversion module.  
     *     
     * @param host the host
     */
    public void setProxy(String host) {
     
        assert host != null && !host.trim().equals(""): "can\'t do anything for a null host";               // NOI18N
         
        if(host.startsWith("file:///")) {
            // a proxy will be needed only for remote repositories
            return;
        }
        
        ProxySelector ps = ProxySelector.getDefault();
        List<Proxy> proxies = null;
        try {
            proxies = ps.select(new java.net.URI(host));            
        } catch (URISyntaxException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }        
                                
        Proxy proxy = null;
        for(Proxy p : proxies) {                        
            if( p.type().equals(Proxy.Type.HTTP) || 
                p.type().equals(Proxy.Type.DIRECT) ) 
            {                                
                proxy = p;                
                if (proxy.type().equals(Proxy.Type.DIRECT)) {
                    break;
                }                   
            }                                             
        }                   
        
        Ini nbServers = new Ini();
        Ini.Section nbGlobalSection = nbServers.add(GLOBAL_SECTION);
        Ini.Section svnGlobalSection = svnServers.get(GLOBAL_SECTION);
        if(proxy.type().equals(Proxy.Type.DIRECT)) {
            // no proxy host means no proxy at all                                                
            if(svnGlobalSection != null) {
                // if there is a global section than get the no proxy settings                                                                 
                mergeNonProxyKeys(svnGlobalSection, nbGlobalSection);                
            }
        } else {            
            // get the proxy 
            SocketAddress sa = proxy.address();            
            InetSocketAddress proxyAddress = (InetSocketAddress) sa;
            
            nbGlobalSection.put("http-proxy-host", proxyAddress.getHostName());                     // NOI18N
            nbGlobalSection.put("http-proxy-port", Integer.toString(proxyAddress.getPort()));       // NOI18N            
            
            // and the authentication
            Preferences prefs = org.openide.util.NbPreferences.root ().node ("org/netbeans/core");  // NOI18N    
            boolean useAuth = prefs.getBoolean ("useProxyAuthentication", false);                   // NOI18N    
            if(useAuth) {
                String username = prefs.get ("proxyAuthenticationUsername", "");                    // NOI18N
                String password = prefs.get ("proxyAuthenticationPassword", "");                    // NOI18N    
                
                nbGlobalSection.put("http-proxy-username", username);                               // NOI18N
                nbGlobalSection.put("http-proxy-password", password);                               // NOI18N            
            }            
            
            // we have a proxy for the host, so check 
            // if in the there are also some no proxy settings 
            // we should get from the original svn servers file
            Ini.Section svnHostGroup = getServerGroup(host);
            if(svnGlobalSection != null) {
                // if there is a global section than get the no proxy settings                                                                 
                mergeNonProxyKeys(svnGlobalSection, nbGlobalSection);                
            }            
            if(svnHostGroup != null) {
                mergeNonProxyKeys(svnHostGroup, nbGlobalSection);                
            }
        }        
        storeIni(nbServers, "servers");                                                       // NOI18N    
    }        
    
    private void mergeNonProxyKeys(Ini.Section source, Ini.Section target) {
        for (String key : source.keySet()) {
            if(!isProxyConfigurationKey(key)) {
                target.put(key, source.get(key));                                                
            }                    
        }
    }
    
    public void setExternalCommand(String tunnelName, String command) {
        Ini.Section tunnels = getSection(config, "tunnels", true);
        tunnels.put(tunnelName, command);
        storeIni(config, "config");                                                     // NOI18N
    }

    public String getExternalCommand(String tunnelName) {
        Ini.Section tunnels = getSection(config, "tunnels", true);
        String cmd = tunnels.get(tunnelName);
        return cmd != null ? cmd : "";        
    }
    
    private Ini.Section getSection(Ini ini, String key, boolean create) {
        Ini.Section section = ini.get(key);
        if(section == null) {
            return ini.add(key);
        }
        return section;
    }
    
    private void storeIni(Ini ini, String iniFile) {
        try {
            File file = FileUtil.normalizeFile(new File(getNBConfigPath() + "/" + iniFile));   // NOI18N
            file.getParentFile().mkdirs();
            ini.store(FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }    

    /**
     * Returns the miscellany/global-ignores setting from the config file.
     *
     * @return a list with the inore patterns
     *
     */
    public List<String> getGlobalIgnores() {
        Ini.Section miscellany = config.get("miscellany");                      // NOI18N
        if (miscellany != null) {
            String ignores = miscellany.get("global-ignores");                  // NOI18N
            if (ignores != null && ignores.trim().length() > 0) {
                return parseGlobalIgnores(ignores);
            }
        }
        return DEFAULT_GLOBAL_IGNORES;
    }

    private static List<String> parseGlobalIgnores(String ignores) {
        StringTokenizer st = new StringTokenizer(ignores, " ");                 // NOI18N
        List<String> ret = new ArrayList<String>(10);
        while (st.hasMoreTokens()) {
            String entry = st.nextToken();
            if (!entry.equals(""))                                              // NOI18N
                ret.add(entry);
        }
        return ret;
    }

    /**
     * Returns the path for the Sunbversion configuration dicectory used 
     * by the systems Subversion commandline client.
     *
     * @return the path
     *
     */ 
    public static String getUserConfigPath() {        
        if(Utilities.isUnix()) {
            String path = System.getProperty("user.home") ;                     // NOI18N
            return path + "/" + UNIX_CONFIG_DIR;                                // NOI18N
        } else if (Utilities.isWindows()){
            return WINDOWS_CONFIG_DIR;
        } 
        return "";                                                              // NOI18N
    }

    /**
     * Returns the path for the Sunbversion configuration dicectory used 
     * by the Netbeans Subversion module.
     *
     * @return the path
     *
     */ 
    public static String getNBConfigPath() {
        String nbHome = System.getProperty("netbeans.user");                    // NOI18N
        return nbHome + "/config/svn/config/";                                  // NOI18N
    }
    
    /**
     * Returns the section from the <b>servers</b> config file used by the Subversion module which 
     * is holding the proxy settings for the given host
     *
     * @param host the host
     * @return the section holding the proxy settings for the given host
     */ 
    private Ini.Section getServerGroup(String host) {
        if(host == null || host.equals("")) {                                   // NOI18N
            return null;
        }
        Ini.Section groups = svnServers.get(GROUPS_SECTION);
        if(groups != null) {
            for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                String value = groups.get(key);
                if(value != null) {     
                    // XXX the same pattern everywhere when calling match()
                    value = value.trim();                    
                    if(value != null && match(value, host)) {
                        return svnServers.get(key);
                    }      
                }
            }
        }
        return null;
    }
       
    /**
     * Evaluates if the given hostaname or IP address is in the given value String.
     *
     * @param value the value String. A list of host names or IP addresses delimited by ",". 
     *                          (e.g 192.168.0.1,*.168.0.1, some.domain.com, *.anything.com, ...)
     * @param host the hostname or IP address
     * @return true if the host name or IP address was found in the values String, otherwise false.
     */
    private boolean match(String value, String host) {                    
        String[] values = value.split(",");                                     // NOI18N
        for (int i = 0; i < values.length; i++) {
            value = values[i].trim();

            if(value.equals("*") || value.equals(host) ) {                      // NOI18N
                return true;
            }

            int idx = value.indexOf("*");                                       // NOI18N
            if(idx > -1 && matchSegments(value, host) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates if the given hostaname or IP address matches with the given value String representing 
     * a hostaname or IP adress with one or more "*" wildcards in it.
     *
     * @param value the value String. A host name or IP addresse with a "*" wildcard. (e.g *.168.0.1 or *.anything.com)
     * @param host the hostname or IP address
     * @return true if the host name or IP address matches with the values String, otherwise false.
     */
    private boolean matchSegments(String value, String host) {
        String[] valueSegments = value.split(".");                              // NOI18N
        String[] hostSegments = host.split(".");                                // NOI18N

        int idx = 0;
        for (int i = 0; i < hostSegments.length; i++) {
            if( !valueSegments[idx].equals("*") &&                              // NOI18N
                !valueSegments[idx].equals(hostSegments[i]) )
            {
                return false;
            }
            if( !valueSegments[idx].equals("*") ) {                             // NOI18N
                idx++;
            }
        }
        return false;
    }

    /**
     * Copies the given configuration file from the Subversion commandline client
     * configuration directory into the configuration directory used by the Netbeans Subversion module. </br>
     */
    private Ini copyConfigFileToIDEConfigDir(String fileName, IniFilePatcher patcher) {
        Ini systemIniFile = loadSystemIniFile(fileName);

        patcher.patch(systemIniFile);

        File file = FileUtil.normalizeFile(new File(getNBConfigPath() + "/" + fileName)); // NOI18N
        try {
            file.getParentFile().mkdirs();
            systemIniFile.store(FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
        }
        return systemIniFile;
    }

    /**
     * Loads the ini configuration file from the directory used by 
     * the Subversion commandline client. The settings are loaded and merged together in 
     * in the folowing order:
     * <ol>
     *  <li> The per-user INI files
     *  <li> The system-wide INI files
     * </ol> 
     *
     * @param fileName the file name
     * @return an Ini instance holding the cofiguration file. 
     */       
    private Ini loadSystemIniFile(String fileName) {
        // config files from userdir
        String filePath = getUserConfigPath() + "/" + fileName;                         // NOI18N
        File file = FileUtil.normalizeFile(new File(filePath));
        Ini system = null;
        try {            
            system = new Ini(new FileReader(file));
        } catch (FileNotFoundException ex) {
            system = new Ini();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        Ini global = null;      
        try {
            global = new Ini(new FileReader(getGlobalConfigPath() + "/" + fileName));   // NOI18N
        } catch (FileNotFoundException ex) {
            // just doesn't exist - ignore
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
                
        if(global != null) {
            merge(global, system);
        }        

        if(system.size() < 1) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Could not load the file " + filePath + ". Falling back on svn defaults."); // NOI18N
        }
        return system;
    }

    /**
     * Merges only sections/keys/values into target which are not already present in source
     * 
     * @param source the source ini file
     * @param target the target ini file in which the values from the source file are going to be merged
     */
    private void merge(Ini source, Ini target) {
        for (Iterator<String> itSections = source.keySet().iterator(); itSections.hasNext();) {
            String sectionName = itSections.next();
            Ini.Section sourceSection = source.get( sectionName );
            Ini.Section targetSection = target.get( sectionName );

            if(targetSection == null) {
                targetSection = target.add(sectionName);
            }

            for (Iterator<String> itVariables = sourceSection.keySet().iterator(); itVariables.hasNext();) {
                String key = itVariables.next();

                if(!targetSection.containsKey(key)) {
                    targetSection.put(key, sourceSection.get(key));
                }
            }            
        }
    }
   
    /**
     * Evaluates if the value stored under the key is a proxy setting value.
     *
     * @param key the key
     * @return true if the value stored under the key is a proxy setting value. Otherwise false
     */
    private boolean isProxyConfigurationKey(String key) {
        return key.equals("http-proxy-host")     || // NOI18N
               key.equals("http-proxy-port")     || // NOI18N
               key.equals("http-proxy-username") || // NOI18N
               key.equals("http-proxy-password");   // NOI18N
    }
    
    /**
     * Return the path for the systemwide command lines configuration directory 
     */
    private static String getGlobalConfigPath () {
        if(Utilities.isUnix()) {
            return "/etc/subversion";               // NOI18N
        } else if (Utilities.isWindows()){
            return WINDOWS_GLOBAL_CONFIG_DIR;
        } 
        return "";                                  // NOI18N
    }

    /**
     * Returns the value for the %APPDATA% env variable on windows
     *
     */
    private static String getAPPDATA() {
        String appdata = "";
        if(Utilities.isWindows()) {
            appdata = System.getenv("APPDATA");// NOI18N
        }
        return appdata!= null? appdata: "";
    }

    /**
     * Returns the value for the %ALLUSERSPROFILE% + the last foder segment from %APPDATA% env variables on windows
     *
     */
    private static String getGlobalAPPDATA() {
        if(Utilities.isWindows()) {
            String globalProfile = System.getenv("ALLUSERSPROFILE");                                // NOI18N
            if(globalProfile == null || globalProfile.trim().equals("")) {                          // NOI18N
                globalProfile = "";
            }
            String appdataPath = WINDOWS_USER_APPDATA;
            if(appdataPath == null || appdataPath.equals("")) {                                     // NOI18N
                return "";                                                                          // NOI18N
            }
            String appdata = "";                                                                    // NOI18N
            int idx = appdataPath.lastIndexOf("\\");                                                // NOI18N
            if(idx > -1) {
                appdata = appdataPath.substring(idx + 1);
                if(appdata.trim().equals("")) {                                                     // NOI18N
                    int previdx = appdataPath.lastIndexOf("\\", idx);                               // NOI18N
                    if(idx > -1) {
                        appdata = appdataPath.substring(previdx + 1, idx);
                    }
                }
            } else {
                return "";                                                                          // NOI18N
            }
            return globalProfile + "/" + appdata;                                                   // NOI18N
        }
        return "";                                                                                  // NOI18N
    }
        
}
