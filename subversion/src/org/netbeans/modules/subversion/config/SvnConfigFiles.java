/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.config;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
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
 * The only exception is the 'store-auth-creds'key, which is always set to 'no'.
 * 
 * @author Tomas Stupka
 */
public class SvnConfigFiles {
    
    /** the only SvnConfigFiles instance */
    private static SvnConfigFiles instance;
    /** the Ini instance holding the configuration values stored in the <b>servers</b> 
     * file used by the Subversion module */
    private Ini servers = null;

    private static final String UNIX_CONFIG_DIR = ".subversion";
    private static final String WINDOWS_CONFIG_DIR = "Application Data/Subversion/";
    private static final String[] AUTH_FOLDERS = new String [] {"auth/svn.simple", "auth/svn.username", "auth/svn.username"};   
    private static final String GROUPS = "groups";

    /**
     * Creates a new instance
     */
    private SvnConfigFiles() {
        // copy config file        
        copyConfigFileToIDEConfigDir();        
        // get the nb servers file merged with the systems servers files
        servers = loadNetbeansIniFile("servers"); 
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
        Ini.Section group = getGroup(host);
        if(group==null) {
            // no proxy specified -> direct
            return ProxyDescriptor.DIRECT;
        }
        String proxyHost = (String) group.get("http-proxy-host");
        if(proxyHost == null || proxyHost.length() == 0) {
            // no host specified -> direct
            return ProxyDescriptor.DIRECT;
        }
        String proxyPortString = (String) group.get("http-proxy-port");
        int proxyPort;
        if(proxyPortString == null || proxyPortString.length() == 0) {
            proxyPort = 0; // XXX
        } else {
            proxyPort = Integer.parseInt(proxyPortString); // XXX what if null ?
        }
        String username = (String) group.get("http-proxy-username");
        String password = (String) group.get("http-proxy-password");
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

            Ini.Section group = getGroup(host);
            if(group==null) {
                group = getGroup(pd);
                if(group==null) {
                    group = addGroup(host);

                    group.put("http-proxy-host", pd.getHost());
                    group.put("http-proxy-port", String.valueOf(pd.getPort()));
                    if(pd.getUserName()!=null) {
                        group.put("http-proxy-username", pd.getUserName());
                    }
                    if(pd.getPassword()!=null) {
                        group.put("http-proxy-password", pd.getPassword());
                    }
                } else {
                    String groupName = group.getName();
                    String groupsHosts = (String) getGroups().get(groupName);
                    getGroups().put(groupName, groupsHosts + "," + host);
                }
            }            
        } else {
            // no proxy host means no proxy at all
            removeFromGroup(host);
        }

        try {
            File file = FileUtil.normalizeFile(new File(getNBConfigDir() + "/servers"));
            file.delete();
            servers.store(FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    /**
     * Returns the path for the Sunbversion configuration dicectory used 
     * by the systems Subversion commandline client.
     *
     * @return the path
     *
     */ 
    public static String getUserConfigPath() {
        String path = System.getProperty("user.home") ;
        if(Utilities.isUnix()) {
            return path + "/" + UNIX_CONFIG_DIR;
        } else if (Utilities.isWindows()){
            return path + "/" + WINDOWS_CONFIG_DIR;
        } else {
            // XXX PETR
        }
        return "";
    }

    /**
     * Returns the path for the Sunbversion configuration dicectory used 
     * by the Netbeans Subversion module.
     *
     * @return the path
     *
     */ 
    public static String getNBConfigDir() {
        String nbHome = System.getProperty("netbeans.user");       
        return nbHome + "/config/svn/config/";
    }
    
    /** 
     * Adds a new group into the <b>servers</b> config file used by the Subversion module. </br>
     * The result is a new section with a generated name and an new entry in the groups section 
     * assotiating the new group with the given host.
     *
     * @param host the host name 
     * @return the Ini.Section newly created
     */
    private Ini.Section addGroup(String host) {
        Ini.Section groups = getGroups();
        int idx = 0;
        String name = "group0";
        while(groups.get(name)!=null) {
            idx++;
            name = "group" + idx;
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
    private void removeFromGroup(String host) {
        Ini.Section group = getGroup(host);
        if(group != null) {
            String groupName = group.getName();
            String[] hosts = ( (String) getGroups().get(groupName) ).split(",");
            if(hosts.length == 1) {
                getGroups().remove(groupName);
                servers.remove(group);
            } else {
                StringBuffer newHosts = new StringBuffer();
                for (int i = 0; i < hosts.length; i++) {
                    if(!hosts[i].trim().equals(host)) {
                        newHosts.append(hosts[i]);
                        if(i < hosts.length - 1) {
                            newHosts.append(",");
                        }
                    }
                }
                getGroups().put(groupName, newHosts.toString());
            }            
        }
    }

    /**
     * Retruns the groups section from <b>servers</b> config file used by the Subversion module. </br>
     *
     * @return the groups section
     */ 
    private Ini.Section getGroups() {
        Ini.Section groups = (Ini.Section) servers.get(GROUPS);
        if(groups==null) {
            groups = servers.add("groups");
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
    private Ini.Section getGroup(String host) {
        Ini.Section groups = getGroups();
        for (Iterator it = groups.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            String value = ((String) groups.get(key)).trim();
            
            if(match(value, host)) {
                return (Ini.Section) servers.get(key);
            }

            InetAddress hostAddress = null;
            try {
                hostAddress = InetAddress.getByName(host);
            } catch (UnknownHostException ex) {
                // behind a proxy ?
                return null;
            }
            if(match(value, hostAddress.getHostName()) || match(value, hostAddress.getHostAddress()) ) {
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
    private Ini.Section getGroup(ProxyDescriptor pd) {
        for (Iterator it = servers.values().iterator(); it.hasNext();) {
            Ini.Section group = (Ini.Section) it.next();
            if (group.getName().equals(GROUPS)) {
                continue;
            }
            if( pd.getHost().equals(group.get("http-proxy-host")) &&
                String.valueOf(pd.getPort()).equals(group.get("http-proxy-port")) &&
                (pd.getUserName()==null || pd.getUserName().equals(group.get("http-proxy-username")))  &&
                (pd.getPassword()==null || pd.getPassword().equals(group.get("http-proxy-password"))) )
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
        String[] values = value.split(",");
        for (int i = 0; i < values.length; i++) {
            value = values[i].trim();

            if(value.equals("*") || value.equals(host) ) {
                return true;
            }

            int idx = value.indexOf("*");
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
        String[] valueSegments = value.split(".");
        String[] hostSegments = host.split(".");

        int idx = 0;
        for (int i = 0; i < hostSegments.length; i++) {
            if( !valueSegments[idx].equals("*") &&
                !valueSegments[idx].equals(hostSegments[i]) )
            {
                return false;
            }
            if( !valueSegments[idx].equals("*") ) {
                idx++;
            }
        }
        return false;
    }

    /**
     * Copies the <b>config</b> configuration file from the Subversion commandline client 
     * configuration directory into the configuration directory used by the Netbeans Subversion module. </br>
     * The value for the 'store-auth-creds' key is alway set to 'no' so the commandline client wan't 
     * create a file holding the authentication credentials when a svn command is called. 
     * The reason for this is that the Subverion module holds the credentials in files with the 
     * same format as the commandline client but with a different name. 
     */
    private void copyConfigFileToIDEConfigDir () {      
        Ini config = loadSystemIniFile("config");

        // patch store-auth-creds to "no"
        Ini.Section auth = (Ini.Section)config.get("auth");
        if(auth == null) {
            auth = config.add("auth");
        }
        auth.put("store-auth-creds", "no");

        File file = FileUtil.normalizeFile(new File(getNBConfigDir() + "/config"));
        try {
            config.store(FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
        }
    }

    /**
     * Loads the ini configuration file from the configuration directory used by the Netbeans Subversion module.
     *
     * @param fileName the file name
     * @return an Ini instance holding the cofiguration file. 
     */    
    private Ini loadNetbeansIniFile(String fileName) {
        File file = FileUtil.normalizeFile(new File(getNBConfigDir() + "/" + fileName));       
        Ini nbIni = null;
        try {
            file.createNewFile();
            nbIni = new Ini(FileUtils.createInputStream(file));
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
     * Loads the ini configuration file from the configurat settings used by 
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
        File file = FileUtil.normalizeFile(new File(getUserConfigPath() + "/config"));
        Ini system = null;
        try {            
            system = new Ini(new FileReader(file));
        } catch (FileNotFoundException ex) {
            // XXX create from registry?
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        if(Utilities.isWindows()) {            
            mergeFromRegistry("HKEY_LOCAL_MACHINE", "Servers", system);
        }

        Ini global = null;        
        try {
            global = new Ini(new FileReader(getGlobalConfigPath() + "/" + fileName));
        } catch (FileNotFoundException ex) {
            // just doesn't exist - ignore
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
                
        if(global != null) {
            merge(global, system);
        }
        
        if(Utilities.isWindows()) {
            mergeFromRegistry("HKEY_CURRENT_USER", "Servers", system);
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
        for (Iterator itSections = source.keySet().iterator(); itSections.hasNext();) {
            String sectionName = (String) itSections.next();
            Ini.Section sourceSection = (Ini.Section) source.get( sectionName );
            Ini.Section targetSection = (Ini.Section) target.get( sectionName );

            if(targetSection == null) {
                targetSection = target.add(sectionName);
            }

            for (Iterator itVariables = sourceSection.keySet().iterator(); itVariables.hasNext();) {
                String key = (String) itVariables.next();

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
        for (Iterator itSections = source.keySet().iterator(); itSections.hasNext();) {
            String sectionName = (String) itSections.next();
            Ini.Section sourceSection = (Ini.Section) source.get( sectionName );
            Ini.Section targetSection = (Ini.Section) target.get( sectionName );
            
            for (Iterator itVariables = sourceSection.keySet().iterator(); itVariables.hasNext();) {
                String key = (String) itVariables.next();

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
        for (Iterator itSections = target.keySet().iterator(); itSections.hasNext();) {
            String sectionName = (String) itSections.next();

            if(sectionName.equals(GROUPS)) {
                continue;
            }

            Ini.Section sourceSection = (Ini.Section) source.get( sectionName );
            Ini.Section targetSection = (Ini.Section) target.get( sectionName );

            if(sourceSection == null) {
                // the whole section is missing -> drop it as long there is no "proxy key"
                if(!isProxyConfigurationSection(targetSection)) {
                    itSections.remove();
                }                
                continue;
            }                    

            for (Iterator itVariables = targetSection.keySet().iterator(); itVariables.hasNext();) {
                String key = (String) itVariables.next();
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
        Collection keys = section.keySet();
        return keys.contains("http-proxy-host")     ||
               keys.contains("http-proxy-port")     ||
               keys.contains("http-proxy-username") ||
               keys.contains("http-proxy-password");
    }
    
    /**
     * Evaluates if the value stored under the key is a proxy setting value.
     *
     * @param key the key
     * @return true if the value stored under the key is a proxy setting value. Otherwise false
     */
    private boolean isProxyConfigurationKey(String key) {
        return key.equals("http-proxy-host")     ||
               key.equals("http-proxy-port")     ||
               key.equals("http-proxy-username") ||
               key.equals("http-proxy-password");
    }
    
    /**
     * Return the path for the systemwide command lines configuration directory 
     */
    private static String getGlobalConfigPath () {
        if(Utilities.isUnix()) {
            return "/etc/subversion";
        } else if (Utilities.isWindows()){
            // XXX
        } else {
            // XXX
        }
        return "";
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
        String key = keyPrefix + "\\Software\\Tigris.org\\Subversion\\" + svnFile;
        String tmpDirPath = System.getProperty("netbeans.user") + "/config/svn/tmp";  // XXX maybe an another location... XXX PETR create temp file
        File tmpDir = FileUtil.normalizeFile(new File(tmpDirPath));
        tmpDir.mkdirs();        
        String tmpFilePath = System.getProperty("netbeans.user") + "/config/svn/tmp/out.reg";                
        File tmpFile = FileUtil.normalizeFile(new File(tmpFilePath));
        
        String[] cmdLine = new String[] {
            "regedit.exe", "/e" , tmpFile.getAbsolutePath(), key       // XXX don't have to use regedit.exe
        };
        
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmdLine);
            p.waitFor(); // XXX check the exit value, handle the streams
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

        key = "[" + key + "\\";     // for parsing purposes
        BufferedInputStream is = null;        
        BufferedReader br = null;
        try {
            is = FileUtils.createInputStream(tmpFile);                                    
            br = new BufferedReader(new InputStreamReader(is, "Unicode"));    // XXX hm, unicode...        
            String line = "";            
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
                    if( line.startsWith("\"#") && section != null )  
                    {
                        String[] elements = line.split("\"=\""); 
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

}
