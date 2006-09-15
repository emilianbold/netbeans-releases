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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.ini4j.Ini;
import org.netbeans.modules.subversion.util.FileUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * Handles the Subversions <b>servers</b> and <b>config</b> configuration files.</br>
 * Everytime the singleton instance is created are the values from the commandline clients
 * configuration directory (and registry folder in case of windows) merged into the Subversion 
 * modules configuration files. Already present proxy setting values wan't be changed, 
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
    private Ini servers = null;
    /** the Ini instance holding the configuration values stored in the <b>config</b>
     * file used by the Subversion module */
    private Ini config = null;

    private static final String UNIX_CONFIG_DIR = ".subversion/"; // NOI18N
    private static final String[] AUTH_FOLDERS = new String [] {"auth/svn.simple", "auth/svn.username", "auth/svn.username"}; // NOI18N
    private static final String GROUPS = "groups"; // NOI18N
    private static final String WINDOWS_USER_APPDATA = getAPPDATA();
    private static final String WINDOWS_CONFIG_DIR = WINDOWS_USER_APPDATA + "\\Subversion"; // NOI18N
    private static final String WINDOWS_GLOBAL_CONFIG_DIR = getGlobalAPPDATA() + "\\Subversion"; // NOI18N
    private static final List<String> DEFAULT_GLOBAL_IGNORES = parseGlobalIgnores("*.o *.lo *.la #*# .*.rej *.rej .*~ *~ .#* .DS_Store");

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
            Ini.Section auth = (Ini.Section) file.get("auth"); // NOI18N
            if(auth == null) {
                auth = file.add("auth"); // NOI18N
            }
            auth.put("store-auth-creds", "no"); // NOI18N
        }
    }

    /**
     * Creates a new instance
     */
    private SvnConfigFiles() {      
        // copy config file        
        config = copyConfigFileToIDEConfigDir("config", new ConfigIniFilePatcher()); // NOI18N
        // get the nb servers file merged with the systems servers files
        servers = loadNetbeansIniFile("servers"); // NOI18N
        
        // and store it so any commnand against the repository may use it
        storeServers();
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
     * Returns a {@link org.netbeans.modules.subversion.config.ProxyDescriptor}.
     * The ProxyDescritor is created from the proxy settings for the given 
     * Hostname or IP address stored in the <b>servers</b> file used by the 
     * Subversion module.
     *
     * @param host the host. May be the host name or an IP address
     * @return a {@link org.netbeans.modules.subversion.config.ProxyDescriptor}
     *
     */
    public ProxyDescriptor getProxyDescriptor(String host) {
        if(host == null || host.equals("")) { // NOI18N
            return ProxyDescriptor.DIRECT;
        }
        Ini.Section group = getServerGroup(host);
        if(group==null) {
            // no proxy specified -> direct
            return ProxyDescriptor.DIRECT;
        }
        String proxyHost = group.get("http-proxy-host"); // NOI18N
        if(proxyHost == null || proxyHost.length() == 0) {
            // no host specified -> direct
            return ProxyDescriptor.DIRECT;
        }
        String proxyPortString = group.get("http-proxy-port"); // NOI18N
        int proxyPort;
        if(proxyPortString == null || proxyPortString.length() == 0) {
            proxyPort = 0; // XXX
        } else {
            proxyPort = Integer.parseInt(proxyPortString); // XXX what if null ?
        }
        String username = group.get("http-proxy-username"); // NOI18N
        String password = group.get("http-proxy-password"); // NOI18N
        return new ProxyDescriptor(ProxyDescriptor.TYPE_HTTP, proxyHost, proxyPort, username, password);    
    }

    /**
     * Stores the proxy host, port, username and password from the given  
     * {@link org.netbeans.modules.subversion.config.ProxyDescriptor} in the 
     * <b>servers</b> file used by the Subversion module.  
     *
     * @param pd the {@link org.netbeans.modules.subversion.config.ProxyDescriptor}
     * @param host the host
     */
    public void setProxy(ProxyDescriptor pd, String host) {

        if(pd != null && pd.getHost() != null) {

            Ini.Section group = getServerGroup(host);
            if(group==null) {
                group = getServerGroup(pd);
                if(group==null) {
                    group = addServerGroup(host);
                    setProxy(group, pd);
                } else {
                    String groupName = group.getName();
                    String groupsHosts = getServerGroups().get(groupName);
                    getServerGroups().put(groupName, groupsHosts + "," + host); // NOI18N
                }
            } else {
                setProxy(group, pd);
            }            
            
        } else {
            // no proxy host means no proxy at all
            removeFromServerGroup(host);
        }

        storeServers();
    }

    private void storeServers() {
        try {
            File file = FileUtil.normalizeFile(new File(getNBConfigPath() + "/servers")); // NOI18N
            file.getParentFile().mkdirs();
            servers.store(FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    private void setProxy(final Ini.Section group, final ProxyDescriptor pd) {

        group.put("http-proxy-host", pd.getHost()); // NOI18N
        group.put("http-proxy-port", String.valueOf(pd.getPort())); // NOI18N
        if(pd.getUserName()!=null) {
            group.put("http-proxy-username", pd.getUserName()); // NOI18N
        }
        if(pd.getPassword()!=null) {
            group.put("http-proxy-password", pd.getPassword()); // NOI18N
        }
    }

    /**
     * Returns the miscellany/global-ignores setting from the config file.
     *
     * @return a list with the inore patterns
     *
     */
    public List<String> getGlobalIgnores() {
        Ini.Section miscellany = config.get("miscellany"); // NOI18N
        if (miscellany != null) {
            String ignores = miscellany.get("global-ignores"); // NOI18N
            if (ignores != null && ignores.trim().length() > 0) {
                return parseGlobalIgnores(ignores);
            }
        }
        return DEFAULT_GLOBAL_IGNORES;
    }

    private static List<String> parseGlobalIgnores(String ignores) {
        StringTokenizer st = new StringTokenizer(ignores, " "); // XXX what if the space is a part of a pattern? // NOI18N
        List<String> ret = new ArrayList<String>(10);
        while (st.hasMoreTokens()) {
            String entry = st.nextToken();
            if (!entry.equals("")) // NOI18N
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
            String path = System.getProperty("user.home") ; // NOI18N
            return path + "/" + UNIX_CONFIG_DIR; // NOI18N
        } else if (Utilities.isWindows()){
            return WINDOWS_CONFIG_DIR;
        } else {
            // XXX Mac (e.g. mkleint)
        }
        return ""; // NOI18N
    }

    /**
     * Returns the path for the Sunbversion configuration dicectory used 
     * by the Netbeans Subversion module.
     *
     * @return the path
     *
     */ 
    public static String getNBConfigPath() {
        String nbHome = System.getProperty("netbeans.user"); // NOI18N
        return nbHome + "/config/svn/config/"; // NOI18N
    }
    
    /** 
     * Adds a new group into the <b>servers</b> config file used by the Subversion module. </br>
     * The result is a new section with a generated name and an new entry in the groups section 
     * assotiating the new group with the given host.
     *
     * @param host the host name 
     * @return the Ini.Section newly created
     */
    private Ini.Section addServerGroup(String host) {
        Ini.Section groups = getServerGroups();
        int idx = 0;
        String name = "group0"; // NOI18N
        while(groups.get(name)!=null) {
            idx++;
            name = "group" + idx; // NOI18N
        }
        
        Ini.Section group = servers.add(name);
        groups.put(name, host);
        return group;
    }

    /**
     * Removes a group from the <b>servers</b> config file used by the Subversion module. </br>
     * The entry in the groups section will always be removed. The according section holding the proxy settings will 
     * be removed as long there is no more host assotiated with the group.
     * 
     * @param host the host which has to removed from the groups section
     */
    private void removeFromServerGroup(String host) {
        Ini.Section group = getServerGroup(host);
        if(group != null) {
            String groupName = group.getName();
            String[] hosts = getServerGroups().get(groupName).split(","); // NOI18N
            if(hosts.length == 1) {
                getServerGroups().remove(groupName);
                servers.remove(group);
            } else {
                StringBuffer newHosts = new StringBuffer();
                for (int i = 0; i < hosts.length; i++) {
                    if(!hosts[i].trim().equals(host)) {
                        newHosts.append(hosts[i]);
                        if(i < hosts.length - 1) {
                            newHosts.append(","); // NOI18N
                        }
                    }
                }
                getServerGroups().put(groupName, newHosts.toString());
            }            
        }
    }

    /**
     * Retruns the groups section from <b>servers</b> config file used by the Subversion module. </br>
     *
     * @return the groups section
     */ 
    private Ini.Section getServerGroups() {
        Ini.Section groups = servers.get(GROUPS);
        if(groups==null) {
            groups = servers.add("groups"); // NOI18N
        }
        return groups;
    }

    /**
     * Returns the section from the <b>servers</b> config file used by the Subversion module which 
     * is holding the proxy settings for the given host
     *
     * @param host the host
     * @return the section holding the proxy settings for the given host
     */ 
    private Ini.Section getServerGroup(String host) {
        if(host == null || host.equals("")) { // NOI18N
            return null;
        }
        Ini.Section groups = getServerGroups();
        for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            String value = groups.get(key).trim();
            
            if(match(value, host)) {
                return servers.get(key);
            }

            if (match(value, host)) {
                return (Ini.Section) servers.get(key);
            }            
        }
        return null;
    }

   /**
     * Returns the section from the <b>servers</b> config file used by the Subversion module which 
     * is holding the proxy settings from the given {@link org.netbeans.modules.subversion.config.ProxyDescriptor}
     *
     * @param pd the {@link org.netbeans.modules.subversion.config.ProxyDescriptor}
     * @return the section holding the proxy settings for the given {@link org.netbeans.modules.subversion.config.ProxyDescriptor}
     */ 
    private Ini.Section getServerGroup(ProxyDescriptor pd) {
        for (Iterator<Ini.Section> it = servers.values().iterator(); it.hasNext();) {
            Ini.Section group = it.next();
            if (group.getName().equals(GROUPS)) {
                continue;
            }
            if( pd.getHost().equals(group.get("http-proxy-host")) && // NOI18N
                String.valueOf(pd.getPort()).equals(group.get("http-proxy-port")) && // NOI18N
                (pd.getUserName()==null || pd.getUserName().equals(group.get("http-proxy-username")))  && // NOI18N
                (pd.getPassword()==null || pd.getPassword().equals(group.get("http-proxy-password"))) ) // NOI18N
            {
                return group;
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
        String[] values = value.split(","); // NOI18N
        for (int i = 0; i < values.length; i++) {
            value = values[i].trim();

            if(value.equals("*") || value.equals(host) ) { // NOI18N
                return true;
            }

            int idx = value.indexOf("*"); // NOI18N
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
        String[] valueSegments = value.split("."); // NOI18N
        String[] hostSegments = host.split("."); // NOI18N

        int idx = 0;
        for (int i = 0; i < hostSegments.length; i++) {
            if( !valueSegments[idx].equals("*") && // NOI18N
                !valueSegments[idx].equals(hostSegments[i]) )
            {
                return false;
            }
            if( !valueSegments[idx].equals("*") ) { // NOI18N
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
     * Loads the ini configuration file from the configuration directory used by the Netbeans Subversion module.
     *
     * @param fileName the file name
     * @return an Ini instance holding the cofiguration file. 
     */    
    private Ini loadNetbeansIniFile(String fileName) {
        File file = FileUtil.normalizeFile(new File(getNBConfigPath() + "/" + fileName)); // NOI18N
        Ini nbIni = null;
        try {
            if(file.exists()) {
                nbIni = new Ini(FileUtils.createInputStream(file));
            }            
        } catch (FileNotFoundException ex) {
            // do nothing
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        Ini system = loadSystemIniFile(fileName);
        if(nbIni==null) {
            nbIni = system;
        } else {
            mergeWithoutProxyConfigurations(system, nbIni);
        }            
        return nbIni;
    }

    /**
     * Loads the ini configuration file from the directory used by 
     * the Subversion commandline client. The settings are loaded and merged together in 
     * in the folowing order:
     * <ol>
     *  <li> The per-user INI files
     *  <li> The per-user Registry values (in case of Windows)
     *  <li> The system-wide INI files
     *  <li> The system-wide Registry values (in case of Windows)
     * </ol> 
     *
     * @param fileName the file name
     * @return an Ini instance holding the cofiguration file. 
     */       
    private Ini loadSystemIniFile(String fileName) {
        // config files from userdir
        String filePath = getUserConfigPath() + "/" + fileName; // NOI18N
        File file = FileUtil.normalizeFile(new File(filePath));
        Ini system = null;
        try {            
            system = new Ini(new FileReader(file));
        } catch (FileNotFoundException ex) {
            system = new Ini();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        String registryFileName = null;
        if(Utilities.isWindows()) {
            registryFileName = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
            mergeFromRegistry("HKEY_LOCAL_MACHINE", registryFileName, system); // NOI18N
        }

        Ini global = null;      
        try {
            global = new Ini(new FileReader(getGlobalConfigPath() + "/" + fileName)); // NOI18N
        } catch (FileNotFoundException ex) {
            // just doesn't exist - ignore
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
                
        if(global != null) {
            merge(global, system);
        }
        
        if(Utilities.isWindows()) {
            mergeFromRegistry("HKEY_CURRENT_USER", registryFileName, system); // NOI18N
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
     * Merges all sections/keys/values from source into target as long
     * as they are no proxy configuration values. 
     *
     * @param source the source ini file
     * @param target the target ini file in which the values from the source file are going to be merged
     */
    private void mergeWithoutProxyConfigurations(Ini source, Ini target) {
        // add changes from source
        for (Iterator<String> itSections = source.keySet().iterator(); itSections.hasNext();) {
            String sectionName = itSections.next();
            Ini.Section sourceSection = source.get( sectionName );
            Ini.Section targetSection = target.get( sectionName );
            
            for (Iterator<String> itVariables = sourceSection.keySet().iterator(); itVariables.hasNext();) {
                String key = itVariables.next();

                if(!isProxyConfigurationKey(key)) {
                    if(targetSection == null) {
                        targetSection = target.add(sectionName);
                    }                    
                    targetSection.put(key, sourceSection.get(key));
                }
            }
        }

        // delete from target what's missing in source
        //List toRemove = new ArrayList();
        for (Iterator<String> itSections = target.keySet().iterator(); itSections.hasNext();) {
            String sectionName = itSections.next();

            if(sectionName.equals(GROUPS)) {
                continue;
            }

            Ini.Section sourceSection = source.get( sectionName );
            Ini.Section targetSection = target.get( sectionName );

            if(sourceSection == null) {
                // the whole section is missing -> drop it as long there is no "proxy key"
                if(!isProxyConfigurationSection(targetSection)) {
                    itSections.remove();
                }                
                continue;
            }                    

            for (Iterator<String> itVariables = targetSection.keySet().iterator(); itVariables.hasNext();) {
                String key = itVariables.next();
                if(sourceSection.get(key) == null && !isProxyConfigurationKey(key)) {
                    // a variable is missing -> drop it
                    itVariables.remove();
                }
            }
        }        
    }

    /**
     * Evaluates if the section holds some proxy setting values.
     *
     * @param section the section
     * @return true if the section holds some proxy setting values. Otherwise false
     */    
    private boolean isProxyConfigurationSection(Ini.Section section) {
        Collection<String> keys = section.keySet();
        return keys.contains("http-proxy-host")     || // NOI18N
               keys.contains("http-proxy-port")     || // NOI18N
               keys.contains("http-proxy-username") || // NOI18N
               keys.contains("http-proxy-password");   // NOI18N
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
            return "/etc/subversion"; // NOI18N
        } else if (Utilities.isWindows()){
            // XXX
        } else {
            // XXX
        }
        return ""; // NOI18N
    }

    /**
     * Merges all sections/keys/values from the given registry folder into the given ini file. 
     * The registry key name is keyPrefix + "\\Software\\Tigris.org\\Subversion\\" + svnFile
     *
     * @param keyPrefix the registry key prefix
     * @param svnFile the last registry key folder
     * @param iniFile the target ini file in which the values from the registry folder are going to be merged
     */   
    // XXX shouldn't be in this case also used the merge only the values witch aren't already present logic?
    private void mergeFromRegistry(String keyPrefix, String svnFile, Ini iniFile) {
        String key = keyPrefix + "\\Software\\Tigris.org\\Subversion\\" + svnFile; // NOI18N
        String tmpDirPath = System.getProperty("netbeans.user") + "/config/svn/tmp";  // XXX maybe an another location... XXX java.io.tmpdir create temp file // NOI18N
        File tmpDir = FileUtil.normalizeFile(new File(tmpDirPath));
        tmpDir.mkdirs();        
        String tmpFilePath = System.getProperty("netbeans.user") + "/config/svn/tmp/out.reg"; // NOI18N
        File tmpFile = FileUtil.normalizeFile(new File(tmpFilePath));
        
        String[] cmdLine = new String[] {
            "regedit.exe", "/e" , tmpFile.getAbsolutePath(), key       // XXX don't have to use regedit.exe // NOI18N
        };
        
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmdLine);
            StreamHandler shErr = new StreamHandler(p.getErrorStream());
            StreamHandler shIn = new StreamHandler(p.getInputStream());
            shErr.start();
            shIn.start();
            p.waitFor(); // XXX check the exit value
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);     
            return;
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);     
            return;
        }                 

        if(!tmpFile.exists()) {
            // no keys - no file
            return;
        }

        key = "[" + key + "\\";     // for parsing purposes // NOI18N
        BufferedInputStream is = null;        
        BufferedReader br = null;
        try {
            is = FileUtils.createInputStream(tmpFile);                                    
            br = new BufferedReader(new InputStreamReader(is, "Unicode"));    // XXX hm, unicode...         // NOI18N
            String line = ""; // NOI18N
            Ini.Section section = null;
            while( (line = br.readLine()) != null ) {
                line = line.trim();            
                if(line.startsWith(key)) {
                    String sectionName = line.substring(key.length(), line.length()-1).trim(); 
                    if(sectionName.length() != 0) {                        
                        section = (Ini.Section) iniFile.get(sectionName);
                        if(section == null) {
                            section = iniFile.add(sectionName);
                        }                            
                    }                    
                } else {
                    if( line.startsWith("\"#") && section != null ) // NOI18N
                    {
                        String[] elements = line.split("\"=\""); // NOI18N
                        String variable = elements[0].substring(2);
                        String value = elements[1].substring(0, elements[1].length()-1);
                        if(!section.containsKey(variable)) {
                            section.put(variable, value);
                        }                        
                    }                    
                }
            }            
            
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);     
        } finally {
            try {
                if(tmpFile != null) {
                    tmpFile.delete();
                }
                if (br != null) {        
                    br.close();
                }                                
                if (is != null) {        
                    is.close();
                }                
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);     
            }                              
        }
        
    }

    /**
     * Returns the value for the %APPDATA% env variable on windows
     *
     */
    private static String getAPPDATA() {
        if(Utilities.isWindows()) {
            String appdata = System.getProperty("Env-APPDATA"); // should work on XP // NOI18N
            if(appdata == null || appdata.trim().equals("")) { // NOI18N
                appdata = getWindowsProperty("APPDATA"); // NOI18N
            }
            return appdata;
        }
        return ""; // NOI18N
    }

    /**
     * Returns the value for the %ALLUSERSPROFILE% + the last foder segment from %APPDATA% env variables on windows
     *
     */
    private static String getGlobalAPPDATA() {
        if(Utilities.isWindows()) {
            String globalProfile = System.getProperty("Env-ALLUSERSPROFILE"); // should work on XP // NOI18N
            if(globalProfile == null || globalProfile.trim().equals("")) { // NOI18N
                globalProfile = getWindowsProperty("ALLUSERSPROFILE"); // NOI18N
            }
            String appdataPath = WINDOWS_USER_APPDATA;
            if(appdataPath == null || appdataPath.equals("")) { // NOI18N
                return ""; // NOI18N
            }
            String appdata = ""; // NOI18N
            int idx = appdataPath.lastIndexOf("\\"); // NOI18N
            if(idx > -1) {
                appdata = appdataPath.substring(idx + 1);
                if(appdata.trim().equals("")) { // NOI18N
                    int previdx = appdataPath.lastIndexOf("\\", idx); // NOI18N
                    if(idx > -1) {
                        appdata = appdataPath.substring(previdx + 1, idx);
                    }
                }
            } else {
                return ""; // NOI18N
            }
            return globalProfile + "/" + appdata; // NOI18N
        }
        return ""; // NOI18N
    }
    
    /**
     * Returns a the value for thegvien specified windows env variable
     */
    private static String getWindowsProperty(String propertyKey) {
        Process p = null;
        Properties propVals = new Properties();
        Runtime r = Runtime.getRuntime();
        try {
            p = r.exec("cmd /C set"); // NOI18N
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = br.readLine()) != null) {
                int index = line.indexOf('=');
                String key = line.substring(0, index);
                String value = line.substring(index + 1);
                propVals.setProperty(key, value);
            }    
            return propVals.getProperty(propertyKey);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e); 
        } 
        return ""; // NOI18N
    }
    
    private class StreamHandler extends Thread {
        private InputStream is;
        private byte[] inputBuffer = new byte[1024];
        StreamHandler(InputStream is) {
            this.is = is;
        }
        public void run() {
            try {
                while(is.read(inputBuffer) != -1) {
                    // nothing to do
                };
                is.close();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }
}
