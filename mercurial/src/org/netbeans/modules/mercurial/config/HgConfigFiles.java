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

package org.netbeans.modules.mercurial.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import org.ini4j.Ini;
import org.netbeans.modules.mercurial.Mercurial;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * Handles the Mercurial <b>hgrc</b> configuration file.</br>
 *
 * @author Padraig O'Briain
 */
public class HgConfigFiles {    

    /** The HgConfigFiles instance for user and system defaults */
    private static HgConfigFiles instance;

    /** the Ini instance holding the configuration values stored in the <b>hgrc</b>
     * file used by the Mercurial module */    
    private Ini hgrc = null;
    
    /** The repository directory if this instance is for a repository */
    private File dir;
    private static final String UNIX_CONFIG_DIR = ".hg/";                                                                       // NOI18N
    private static final String WINDOWS_USER_APPDATA = getAPPDATA();
    private static final String WINDOWS_CONFIG_DIR = WINDOWS_USER_APPDATA + "\\Mercurial";                                      // NOI18N
    private static final String WINDOWS_GLOBAL_CONFIG_DIR = getGlobalAPPDATA() + "\\Mercurial";                                 // NOI18N
    
    /**
     * Creates a new instance
     */
    private HgConfigFiles() {      
        // get the system hgrc file 
        hgrc = loadFile("hgrc");                                           // NOI18N  
    }
    
    /**
     * Returns a singleton instance
     *
     * @return the HgConfiles instance
     */
    public static HgConfigFiles getInstance() {
        if (instance==null) {
            instance = new HgConfigFiles();
        }
        return instance;
    }

    public HgConfigFiles(File file) {
        dir = file;
        hgrc = loadFile(file, "hgrc");                                              // NOI18N
    }
    
    public void setProperty(String name, String value) {
        if (name.equals("username")) { // NOI18N
            setProperty("ui", "username", value); // NOI18N
        } else if (name.equals("default-push")) { // NOI18N
            setProperty("paths", "default-push", value); // NOI18N
        } else if (name.equals("default-pull")) { // NOI18N
            setProperty("paths", "default", value); // NOI18N
        }
    }
 
    public void setProperty(String section, String name, String value) {
        Ini.Section inisection = getSection(hgrc, section, true);
        inisection.put(name, value);
        storeIni(hgrc, "hgrc"); // NOI18N
    }

    public void setUserName(String value) {
        setProperty("ui", "username", value); // NOI18N
    }

    public String getUserName() {
        return getUserName(true);
    }

    public Properties getProperties(String section) {
        Ini.Section inisection = getSection(hgrc, section, false);
        Properties props = new Properties();
        if (inisection != null) {
            Set<String> keys = inisection.keySet();
            for (String key : keys) {
                props.setProperty(key, inisection.get(key));
            }
        }
        return props;
    }

    public void clearProperties(String section) {
        Ini.Section inisection = getSection(hgrc, section, false);
        if (inisection != null) {
             inisection.clear();
             storeIni(hgrc, "hgrc"); // NOI18N 
         }
    }

    public void removeProperty(String section, String name) {
        Ini.Section inisection = getSection(hgrc, section, false);
        if (inisection != null) {
             inisection.remove(name);
             storeIni(hgrc, "hgrc"); // NOI18N 
         }
    }

    public String getDefaultPull(Boolean reload) {
        if (reload) {
            doReload();
        }
        return getProperty("paths", "default"); // NOI18N
    }

    public String getDefaultPush(Boolean reload) {
        if (reload) {
            doReload();
        }
        return getProperty("paths", "default-push"); // NOI18N
    }

    public String getUserName(Boolean reload) {
        if (reload) {
            doReload();
        }
        return getProperty("ui", "username");                                              // NOI18N
    }

    public String getProperty(String section, String name) {
        Ini.Section inisection = getSection(hgrc, section, true);
        String value = inisection.get(name);
        return value != null ? value : "";        // NOI18N 
    }
    
    private void doReload () {
        if (dir == null) {
            hgrc = loadFile("hgrc");                                            // NOI18N  
        } else {
            hgrc = loadFile(dir, "hgrc");                                       // NOI18N  
        }
    }

    private Ini.Section getSection(Ini ini, String key, boolean create) {
        Ini.Section section = ini.get(key);
        if(section == null && create) {
            return ini.add(key);
        }
        return section;
    }
    
    private void storeIni(Ini ini, String iniFile) {
        try {
            String filePath;
            if (dir != null) {
                filePath = dir.getAbsolutePath() + File.separator + ".hg" + File.separator + iniFile; // NOI18N 
            } else {
                filePath =  getUserConfigPath() + iniFile;
            }
            File file = FileUtil.normalizeFile(new File(filePath));
            file.getParentFile().mkdirs();
            ini.store(new BufferedOutputStream(new FileOutputStream(file)));
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        }
    }    

    /**
     * Returns the path for the Mercurial configuration directory
     *
     * @return the path
     *
     */ 
    public static String getUserConfigPath() {        
        if(Utilities.isUnix()) {
            String path = System.getProperty("user.home") ;                     // NOI18N
            return path + "/.";                                // NOI18N
        } else if (Utilities.isWindows()){
            return WINDOWS_CONFIG_DIR + "/"; // NOI18N 
        } 
        return "";                                                              // NOI18N
    }

    private Ini loadFile(File dir, String fileName) {
        String filePath = dir.getAbsolutePath() + File.separator + ".hg" + File.separator + fileName; // NOI18N 
        File file = FileUtil.normalizeFile(new File(filePath));
        Ini system = null;
        try {            
            system = new Ini(new FileReader(file));
        } catch (FileNotFoundException ex) {
            // ignore
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex); 
        }

        if(system == null) {
            system = new Ini();
            Mercurial.LOG.log(Level.WARNING, "Could not load the file " + filePath + ". Falling back on hg defaults."); // NOI18N
        }
        return system;
    }
    /**
     * Loads the configuration file  
     * The settings are loaded and merged together in the folowing order:
     * <ol>
     *  <li> The per-user configuration file, i.e ~/.hgrc
     *  <li> The system-wide file, i.e. /etc/mercurial/hgrc
     * </ol> 
     *
     * @param fileName the file name
     * @return an Ini instance holding the configuration file. 
     */       
    private Ini loadFile(String fileName) {
        // config files from userdir
        String filePath = getUserConfigPath() + fileName;
        File file = FileUtil.normalizeFile(new File(filePath));
        Ini system = null;
        try {            
            system = new Ini(new FileReader(file));
        } catch (FileNotFoundException ex) {
            // ignore
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex); 
        }

        if(system == null) {
            system = new Ini();
            Mercurial.LOG.log(Level.WARNING, "Could not load the file " + filePath + ". Falling back on hg defaults."); // NOI18N
        }
        
        Ini global = null;      
        try {
            global = new Ini(new FileReader(getGlobalConfigPath() + File.separator + fileName));   // NOI18N
        } catch (FileNotFoundException ex) {
            // just doesn't exist - ignore
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex); 
        }
         
        if(global != null) {
            merge(global, system);
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
     * Return the path for the systemwide command lines configuration directory 
     */
    private static String getGlobalConfigPath () {
        if(Utilities.isUnix()) {
            return "/etc/mercurial";               // NOI18N
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
        String appdata = ""; // NOI18N
        if(Utilities.isWindows()) {
            appdata = System.getenv("APPDATA");// NOI18N
        }
        return appdata!= null? appdata: ""; // NOI18N
    }

    /**
     * Returns the value for the %ALLUSERSPROFILE% + the last foder segment from %APPDATA% env variables on windows
     *
     */
    private static String getGlobalAPPDATA() {
        if(Utilities.isWindows()) {
            String globalProfile = System.getenv("ALLUSERSPROFILE");                                // NOI18N
            if(globalProfile == null || globalProfile.trim().equals("")) {                          // NOI18N
                globalProfile = "";                                                                 // NOI18N
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
