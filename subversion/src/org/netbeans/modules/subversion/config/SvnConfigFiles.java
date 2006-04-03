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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.ini4j.Ini;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * XXX test for robustness
 * @author Tomas Stupka
 */
public class SvnConfigFiles {
    
    private static SvnConfigFiles instance;

    private Ini servers = null;

    private static final String UNIX_CONFIG_DIR = ".subversion";
    private static final String WINDOWS_CONFIG_DIR = "Application Data/Subversion/";

    private final String[] AUTH_FOLDERS = new String [] {"auth/svn.simple", "auth/svn.username", "auth/svn.username"};   

    private SvnConfigFiles() {
        // copy config file        
        copyToIDEConfigDir();        
        // get the nb servers file merged with the systems servers files

        // XXX this could be expensive - don't run in awt !!!
        servers = loadNetbeansIniFile("servers"); 
    }

    public static SvnConfigFiles getInstance() {
        if(instance==null) {
            instance = new SvnConfigFiles();                    
        }        
        return instance;
    }

    public ProxyDescriptor getProxyDescriptor(SVNUrl url) {
        Ini.Section group = getGroup(url);
        if(group==null) {
            return null;
        }
        String host = (String) group.get("http-proxy-host");
        if(host == null || host.length() == 0) {
            return null;
        }
        String portString = (String) group.get("http-proxy-port");
        int port;
        if(portString == null || portString.length() == 0) {
            port = 0; // XXX
        } else {
            port = Integer.parseInt(portString); // XXX what if null ?
        }
        String username = (String) group.get("http-proxy-username");
        String password = (String) group.get("http-proxy-password");
        return  new ProxyDescriptor(ProxyDescriptor.TYPE_HTTP, host, port, username, password);    
    }

    public void setProxy(ProxyDescriptor pd, SVNUrl url) {
        Ini.Section group = getGroup(url);
        if(group==null) {
            group = addGroup(url);
        }
        group.put("http-proxy-host", pd.getHost());
        group.put("http-proxy-port", String.valueOf(pd.getPort()));
        if(pd.getUserName()!=null) {
            group.put("http-proxy-username", pd.getUserName());
        }
        if(pd.getPassword()!=null) {
            group.put("http-proxy-password", pd.getPassword());
        }        

        try {
            File file = FileUtil.normalizeFile(new File(getNBConfigDir() + "/servers"));
            file.delete();
            servers.store(FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            ex.printStackTrace(); //  XXX
        }
    }

    private Ini.Section addGroup(SVNUrl url) {
        Ini.Section groups = getGroups();
        int idx = 0;
        String name = "group0";
        while(groups.get(name)!=null) {
            name = "group" + ++idx;
        }
        
        Ini.Section group = servers.add(name);
        groups.put(name, url.getHost());
        return group;
    }

    private Ini.Section getGroups() {
        Ini.Section groups = (Ini.Section) servers.get("groups");
        if(groups==null) {
            groups = servers.add("groups");
        }
        return groups;
    }

    private Ini.Section getGroup(SVNUrl url) {
        Ini.Section groups = getGroups();
        for (Iterator it = groups.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            String value = ((String) groups.get(key)).trim();

            String host = url.getHost().trim();
            if(match(value, host)) {
                return (Ini.Section) servers.get(key);
            }

            InetAddress hostAddress = null;
            try {
                hostAddress = InetAddress.getByName(url.getHost().trim());
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

    // XXX test me
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

    private void copyToIDEConfigDir () {
        File file = FileUtil.normalizeFile(new File(getUserConfigPath() + "/config"));
        File targetConfigFile = new File(getNBConfigDir() + "/" + "config");
        targetConfigFile = FileUtil.normalizeFile(targetConfigFile);
        try {
            FileUtils.copyFile (file, targetConfigFile);
        } catch (IOException ex) {
            ex.printStackTrace(); // should not happen
        }
    }

    private Ini loadNetbeansIniFile(String fileName) {
        File file = FileUtil.normalizeFile(new File(getNBConfigDir() + "/" + fileName));        
        Ini nbIni = null;
        try {
            file.createNewFile();
            nbIni = new Ini(FileUtils.createInputStream(file));
        } catch (FileNotFoundException ex) {
            // do nothing
        } catch (IOException ex) {
            ex.printStackTrace(); // XXX
        }

        Ini system = loadSystemIniFile(fileName);
        if(nbIni==null) {
            nbIni = system;
        } else {
            mergeWithoutProxyConfigurations(system, nbIni);
        }            
        return nbIni;
    }

    private Ini loadSystemIniFile(String fileName) {
        // config files from userdir
        File file = FileUtil.normalizeFile(new File(getUserConfigPath() + "/config"));
        Ini system = null;
        try {            
            system = new Ini(new FileReader(file));
        } catch (FileNotFoundException ex) {
            // XXX create from registry?
        } catch (IOException ex) {
            ex.printStackTrace(); // XXX
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
            ex.printStackTrace();// XXX 
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
     *
     * Merge only sections/keys/values into target which are not present in source
     *
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
     *
     * Merge all sections/keys/values from source into target as long
     * as they are no proxy configuration values.
     *
     */
    private void mergeWithoutProxyConfigurations(Ini source, Ini target) {
        // add changes from source
        for (Iterator itSections = source.keySet().iterator(); itSections.hasNext();) {
            String sectionName = (String) itSections.next();
            Ini.Section sourceSection = (Ini.Section) source.get( sectionName );
            Ini.Section targetSection = (Ini.Section) target.get( sectionName );
            
            for (Iterator itVariables = sourceSection.keySet().iterator(); itVariables.hasNext();) {
                String key = (String) itVariables.next();

                if(!isProxyConfiguration(key)) {                    
                    if(targetSection == null) {
                        targetSection = target.add(sectionName);
                    }                    
                    targetSection.put(key, sourceSection.get(key));
                }
            }
        }

        // delete from target what's missing in source
        List toRemove = new ArrayList();
        for (Iterator itSections = target.keySet().iterator(); itSections.hasNext();) {
            String sectionName = (String) itSections.next();
            Ini.Section sourceSection = (Ini.Section) source.get( sectionName );
            Ini.Section targetSection = (Ini.Section) target.get( sectionName );

            if(sourceSection == null) {
                // the whole section is missing -> drop it
                toRemove.add( sectionName );
                continue;
            }                    

            for (Iterator itVariables = targetSection.keySet().iterator(); itVariables.hasNext();) {
                String key = (String) itVariables.next();
                if(!isProxyConfiguration(key)) {
                    // a variable is missing -> drop it
                    targetSection.remove(key);
                }
            }
        }
        for (Iterator it = toRemove.iterator(); it.hasNext();) {
            target.remove(it.next());
        }
    }

    private boolean isProxyConfiguration(String key) {
        return key.equals("http-proxy-host")     ||
               key.equals("http-proxy-port")     ||
               key.equals("http-proxy-username") ||
               key.equals("http-proxy-password");
    }

    public static String getUserConfigPath() {
        String path = System.getProperty("user.home") ;
        if(Utilities.isUnix()) {
            return path + "/" + UNIX_CONFIG_DIR;
        } else if (Utilities.isWindows()){
            return path + "/" + WINDOWS_CONFIG_DIR;
        } else {
            // XXX
        }
        return "";
    }

    public static String getGlobalConfigPath () {
        if(Utilities.isUnix()) {
            return "/etc/subversion";
        } else if (Utilities.isWindows()){
            // XXX
        } else {
            // XXX
        }
        return "";
    }

    public static String getNBConfigDir() {
        String nbHome = System.getProperty("netbeans.user");       
        return nbHome + "/config/svn/config/";
    }

    private void mergeFromRegistry(String keyPrefix, String svnFile, Ini iniFile) {
        String key = keyPrefix + "\\Software\\Tigris.org\\Subversion\\" + svnFile;
        String tmpDirPath = System.getProperty("netbeans.user") + "/config/svn/tmp";  // XXX maybe an another location... XXX create temp file
        File tmpDir = FileUtil.normalizeFile(new File(tmpDirPath));
        tmpDir.mkdirs();        
        String tmpFilePath = System.getProperty("netbeans.user") + "/config/svn/tmp/out.reg";                
        File tmpFile = FileUtil.normalizeFile(new File(tmpFilePath));
        
        String[] cmdLine = new String[] {
            "regedit.exe", "/e" , tmpFile.getAbsolutePath(), key       
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
