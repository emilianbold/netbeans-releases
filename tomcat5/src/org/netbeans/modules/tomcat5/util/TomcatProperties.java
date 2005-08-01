/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Utility class that makes it easier to access and set Tomcat instance properties.
 *
 * @author sherold
 */
public class TomcatProperties {
    
    public static final String DEBUG_TYPE_SOCKET = "SEL_debuggingType_socket";  // NOI18N
    public static final String DEBUG_TYPE_SHARED = "SEL_debuggingType_shared";  // NOI18N
    
    public static final String BUNDLED_TOMCAT_SETTING = "J2EE/BundledTomcat/Setting"; // NOI18N
    
    // properties    
    private static final String PROP_URL           = InstanceProperties.URL_ATTR;
    private static final String PROP_USERNAME      = InstanceProperties.USERNAME_ATTR;
    private static final String PROP_PASSWORD      = InstanceProperties.PASSWORD_ATTR;
    public  static final String PROP_SERVER_PORT   = InstanceProperties.HTTP_PORT_NUMBER;
    private static final String PROP_DISPLAY_NAME  = InstanceProperties.DISPLAY_NAME_ATTR;
    public  static final String PROP_SHUTDOWN      = "admin_port";      //NOI18N
    public  static final String PROP_MONITOR       = "monitor_enabled"; // NOI18N   
    private static final String PROP_CUSTOM_SCRIPT = "custom_script_enabled"; // NOI18N
    private static final String PROP_SCRIPT_PATH   = "script_path";     // NOI18N
    private static final String PROP_FORCE_STOP    = "forceStopOption"; // NOI18N    
    private static final String PROP_DEBUG_TYPE    = "debug_type";      // NOI18N
    private static final String PROP_DEBUG_PORT    = "debugger_port";   // NOI18N
    private static final String PROP_SHARED_MEM    = "shared_memory";   // NOI18N    
    private static final String PROP_JAVA_PLATFORM = "java_platform";   // NOI18N
    private static final String PROP_JAVA_OPTS     = "java_opts";       // NOI18N
    private static final String PROP_SEC_MANAGER   = "securityStartupOption"; // NOI18N    
    private static final String PROP_SOURCES       = "sources";         // NOI18N
    private static final String PROP_JAVADOCS      = "javadocs";        // NOI18N
    private static final String PROP_OPEN_LOG      = "openContextLogOnRun"; // NOI18N
    /** server.xml check timestamp */
    private static final String PROP_TIMESTAMP     = "timestamp";       // NOI18N
    private static final String PROP_HOST          = "host";            // NOI18N
    private static final String PROP_BUNDLED_TOMCAT = "is_it_bundled_tomcat";       // NOI18N
    public  static final String PROP_RUNNING_CHECK_TIMEOUT = "runningCheckTimeout"; // NOI18N
    
    // default values
    private static final boolean DEF_VALUE_SEC_MANAGER   = false;
    private static final boolean DEF_VALUE_CUSTOM_SCRIPT = false;
    private static final String  DEF_VALUE_SCRIPT_PATH   = ""; // NOI18N
    private static final boolean DEF_VALUE_FORCE_STOP    = false;
    private static final String  DEF_VALUE_JAVA_OPTS     = ""; // NOI18N
    private static final String  DEF_VALUE_DEBUG_TYPE = Utilities.isWindows() ? DEBUG_TYPE_SHARED 
                                                                              : DEBUG_TYPE_SOCKET;
    private static final boolean DEF_VALUE_MONITOR       = true;
    private static final int     DEF_VALUE_DEBUG_PORT    = 11555;
    private static final String  DEF_VALUE_SHARED_MEM    = "tomcat_shared_memory_id"; // NOI18N
    private static final boolean DEF_VALUE_OPEN_LOG      = true;
    private static final String  DEF_VALUE_HOST          = "localhost"; // NOI18N
    public  static final int     DEF_VALUE_RUNNING_CHECK_TIMEOUT = 1000;
    private static final String  DEF_VALUE_DISPLAY_NAME  = 
            NbBundle.getMessage(TomcatProperties.class, "LBL_DefaultDisplayName");
    
    private TomcatManager tm;
    private InstanceProperties ip;
    private File homeDir;
    private File baseDir;
    
    /** Creates a new instance of TomcatProperties */
    public TomcatProperties(TomcatManager tm) {
        this.tm = tm;
        this.ip = tm.getInstanceProperties();
        String catalinaHome = null;
        String catalinaBase = null;
        String uri = ip.getProperty(PROP_URL); // NOI18N
        final String home = "home=";    // NOI18N
        final String base = ":base=";   // NOI18N
        final String uriString = "http://";  // NOI18N
        int uriOffset = uri.indexOf (uriString);
        int homeOffset = uri.indexOf (home) + home.length ();
        int baseOffset = uri.indexOf (base, homeOffset);
        if (homeOffset >= home.length ()) {
            int homeEnd = baseOffset > 0 ? baseOffset : (uriOffset > 0 ? uriOffset - 1 : uri.length ());
            int baseEnd = uriOffset > 0 ? uriOffset - 1 : uri.length ();
            catalinaHome= uri.substring (homeOffset, homeEnd);
            if (baseOffset > 0) {
                catalinaBase = uri.substring (baseOffset + base.length (), baseEnd);
            }
            // Bundled Tomcat home and base dirs can be specified as attributes
            // specified in BUNDLED_TOMCAT_SETTING file. Tomcat manager URL can 
            // then look like "tomcat:home=$bundled_home:base=$bundled_base" and
            // therefore remains valid even if Tomcat version changes. (issue# 40659)
            if (catalinaHome.length() > 0 && catalinaHome.charAt(0) == '$') {
                FileSystem fs = Repository.getDefault().getDefaultFileSystem();
                FileObject fo = fs.findResource(BUNDLED_TOMCAT_SETTING);
                if (fo != null) {
                    catalinaHome = fo.getAttribute(catalinaHome.substring(1)).toString();
                    if (catalinaBase != null && catalinaBase.length() > 0 
                        && catalinaBase.charAt(0) == '$') {
                        catalinaBase = fo.getAttribute(catalinaBase.substring(1)).toString();
                    }
                }
            }
        }
        assert catalinaHome != null : "CATALINA_HOME must not be null."; // NOI18N
        homeDir = new File(catalinaHome);
        if (!homeDir.isAbsolute ()) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault();
            homeDir = ifl.locate(catalinaHome, null, false);
        }
        if (catalinaBase != null) {
            baseDir = new File(catalinaBase);
            if (!baseDir.isAbsolute ()) {
                InstalledFileLocator ifl = InstalledFileLocator.getDefault();
                baseDir = ifl.locate(catalinaBase, null, false);
                if (baseDir == null) {
                    baseDir = new File(System.getProperty("netbeans.user"), catalinaBase);   // NOI18N
                }
            }
        }        
        
//        //parse the old format for backward compatibility
//        if (uriOffset > 0) {
//            String theUri = uri.substring (uriOffset + uriString.length ());
//            int portIndex = theUri.indexOf (':');
//            String host = theUri.substring (0, portIndex - 1);
//            setHost (host);
//            //System.out.println("host:"+host);
//            int portEnd = theUri.indexOf ('/');
//            portEnd = portEnd > 0 ? portEnd : theUri.length ();
//            String port = theUri.substring (portIndex, portEnd - 1);
//            //System.out.println("port:"+port);
//            try {
//                setServerPort (Integer.valueOf (port));
//            } catch (NumberFormatException nef) {
//                org.openide.ErrorManager.getDefault ().log (nef.getLocalizedMessage ());
//            }
//        }
    }
    
    /** Return CATALINA_HOME directory.*/
    public File getCatalinaHome() {
        return homeDir;
    }
    
    /** Return CATALINA_BASE directory or null if not defined. */
    public File getCatalinaBase() {
        return baseDir;
    }    
    
    /** Return CATALINA_BASE directory if defined, CATALINA_HOME otherwise. */
    public File getCatalinaDir() {
        return baseDir == null ? homeDir : baseDir;
    }
    
    public String getUsername() {
        String val = ip.getProperty(PROP_USERNAME);
        return val != null ? val : ""; // NOI18N
    }
    
    public void setUsername(String value) {
        ip.setProperty(PROP_USERNAME, value);
    }
    
    public String getPassword() {
        String val = ip.getProperty(PROP_PASSWORD);
        return val != null ? val : ""; // NOI18N
    }
    
    public void setPassword(String value) {
        ip.setProperty(PROP_PASSWORD, value);
    }
    
    public String getJavaPlatform() {
        String currentJvm = ip.getProperty(PROP_JAVA_PLATFORM);
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform[] curJvms = jpm.getPlatforms(currentJvm, new Specification("J2SE", null)); // NOI18N
        if (currentJvm == null || curJvms.length == 0) {
            return jpm.getDefaultPlatform().getDisplayName();
        } else {
            return curJvms[0].getDisplayName();
        }
    }
    
    public void setJavaPlatform(String javaPlatform) {
        ip.setProperty(PROP_JAVA_PLATFORM, javaPlatform);
    }
    
    public String getJavaOpts() {
        String val = ip.getProperty(PROP_JAVA_OPTS);
        return val != null ? val 
                           : DEF_VALUE_JAVA_OPTS;
    }
    
    public void setJavaOpts(String javaOpts) {
        ip.setProperty(PROP_JAVA_OPTS, javaOpts);
    }
    
    public boolean getSecManager() {
        String val = ip.getProperty(PROP_SEC_MANAGER);
        return val != null ? Boolean.valueOf(val).booleanValue()
                           : DEF_VALUE_SEC_MANAGER;
    }
    
    public void setSecManager(boolean enabled) {
        ip.setProperty(PROP_SEC_MANAGER, Boolean.toString(enabled));
    }
    
    
    public boolean getCustomScript() {
        String val = ip.getProperty(PROP_CUSTOM_SCRIPT);
        return val != null ? Boolean.valueOf(val).booleanValue()
                           : DEF_VALUE_CUSTOM_SCRIPT;
    }
    
    public void setCustomScript(boolean enabled) {
        ip.setProperty(PROP_CUSTOM_SCRIPT, Boolean.toString(enabled));
    }
    
    public String getScriptPath() {
        String val = ip.getProperty(PROP_SCRIPT_PATH);
        return val != null ? val 
                           : DEF_VALUE_SCRIPT_PATH;
    }
    
    public void setScriptPath(String path) {
        ip.setProperty(PROP_SCRIPT_PATH, path);
    }
    
    public boolean getForceStop() {
        if (Utilities.isWindows()) {
            return false;
        }
        String val = ip.getProperty(PROP_FORCE_STOP);
        return val != null ? Boolean.valueOf(val).booleanValue()
                           : DEF_VALUE_FORCE_STOP;
    }
    
    public void setForceStop(boolean enabled) {
        ip.setProperty(PROP_FORCE_STOP, Boolean.toString(enabled));
    }
    
    public String getDebugType() {
        String val = ip.getProperty(PROP_DEBUG_TYPE);
        if ((DEBUG_TYPE_SHARED.equalsIgnoreCase(val) && Utilities.isWindows()) 
                || DEBUG_TYPE_SOCKET.equalsIgnoreCase(val)) {
            return val;
        }
        return DEF_VALUE_DEBUG_TYPE;
    }
    
    public void setDebugType(String type) {
        ip.setProperty(PROP_DEBUG_TYPE, type);
    }
    
    public boolean getMonitor() {
        String val = ip.getProperty(PROP_MONITOR);
        return val != null ? Boolean.valueOf(val).booleanValue()
                           : DEF_VALUE_MONITOR;
    }
    
    public void setMonitor(boolean enabled) {
        ip.setProperty(PROP_MONITOR, Boolean.toString(enabled));
    }
    
    public int getDebugPort() {
        String val = ip.getProperty(PROP_DEBUG_PORT);
                
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
            }
        }
        return DEF_VALUE_DEBUG_PORT;
    }
    
    public void setDebugPort(int port) {
        ip.setProperty(PROP_DEBUG_PORT, Integer.toString(port));
    }
    
    
    public int getServerPort() {
        String val = ip.getProperty(PROP_SERVER_PORT);
        
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
            }
        }
        return -1;
    }
    
    /** this needs to be kept in sync with value in the server.xml conf file */
    public void setServerPort(int port) {
        ip.setProperty(PROP_SERVER_PORT, Integer.toString(port));
    }
    
    public int getShutdownPort() {
        String val = ip.getProperty(PROP_SHUTDOWN);
        
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
            }
        }
        return -1;
    }
    
    /** this needs to be kept in sync with value in the server.xml conf file */
    public void setShutdownPort(int port) {
        ip.setProperty(PROP_SHUTDOWN, Integer.toString(port));
    }
    
    public String getSharedMem() {
        String val = ip.getProperty(PROP_SHARED_MEM);
        return val != null && val.length() > 0 ? val 
                                               : DEF_VALUE_SHARED_MEM;
    }
    
    public void setSharedMem(String val) {
        ip.setProperty(PROP_SHARED_MEM, val);
    }
    
    public List/*<URL>*/ getClasses() {
        String[] nbFilter = new String[] {
            "httpmonitor", "schema2beans"
        };
        
        String[] implFilter = new String[] {
            "-impl.jar"
        };
        // tomcat and jwsdp libs
        List retValue = listUrls(new File(homeDir, "common/lib"),  nbFilter);    // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jaxb/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jaxp/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jaxr/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jaxrpc/lib"),  implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jstl/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jwsdp-shared/lib"), implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "saaj/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "xmldsig/lib"), implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(baseDir, "shared/lib"),  nbFilter));   // NOI18N
        return retValue;
    }
    
    public List/*<URL>*/ getSources() {
        String path = ip.getProperty(PROP_SOURCES);
        if (path == null) {
            return new ArrayList();
        }
        return tokenizePath(path);
    }
                                                                                                                                                                           
    public void setSources(List/*<URL>*/ path) {
        ip.setProperty(PROP_SOURCES, buildPath(path));
        tm.getTomcatPlatform().notifyLibrariesChanged();
    }
    
    public List/*<URL>*/ getJavadocs() {
        String path = ip.getProperty(PROP_JAVADOCS);
        if (path == null) {                
            ArrayList list = new ArrayList();
            // tomcat docs
            File tomcatDoc = new File(homeDir, "webapps/tomcat-docs"); // NOI18N
            if (!tomcatDoc.exists()) {
                tomcatDoc = InstalledFileLocator.getDefault().locate("docs/j2eeri-1_4-doc-api.zip", null, false); // NOI18N
            }
            try {                
                if (tomcatDoc.exists()) {
                    list.add(Utils.fileToUrl(tomcatDoc));
                }                
                // jwsdp docs
                File docs = new File(homeDir, "docs"); // NOI18N
                if (docs.exists()) {
                    list.add(Utils.fileToUrl(docs));
                }
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(e);
            }
            return list;
        }
        return tokenizePath(path);
    }
                                                                                                                                                                           
    public void setJavadocs(List/*<URL>*/ path) {
        ip.setProperty(PROP_JAVADOCS, buildPath(path));
        tm.getTomcatPlatform().notifyLibrariesChanged();
    }
    
    public void setOpenContextLogOnRun(boolean val) {
        ip.setProperty(PROP_OPEN_LOG, Boolean.valueOf(val).toString());
    }
    
    public boolean getOpenContextLogOnRun() {
        Object val = ip.getProperty(PROP_OPEN_LOG);
        if (val != null) {
            return Boolean.valueOf(val.toString()).booleanValue();
        }
        return DEF_VALUE_OPEN_LOG;
    }
    
        
    public void setTimestamp(long timestamp) {
        ip.setProperty(PROP_TIMESTAMP, Long.toString(timestamp));
    }
    
    /** Return last server.xml check timestamp, or -1 if not set */
    public long getTimestamp() {
        String val = ip.getProperty(PROP_TIMESTAMP);        
        if (val != null) {
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException nfe) {
            }
        }
        return -1;
    }
    
    public File getServerXml() {
        return new File(getCatalinaDir(), "conf/server.xml"); // NIO18N
    }
    
    public String getHost () {
        String val = ip.getProperty(PROP_HOST);
        return val != null ? val : DEF_VALUE_HOST;
    }
    
    public int getRunningCheckTimeout() {
        String val = ip.getProperty(PROP_RUNNING_CHECK_TIMEOUT);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
            }
        }
        return DEF_VALUE_RUNNING_CHECK_TIMEOUT;
    }
    
    public boolean isBundledTomcat() {
        String val = ip.getProperty(PROP_BUNDLED_TOMCAT);
        return val != null ? Boolean.valueOf(val).booleanValue()
                           : false;
    }
    
    public String getDisplayName() {
        String val = ip.getProperty(PROP_DISPLAY_NAME);
        return val != null && val.length() > 0 ? val 
                                               : DEF_VALUE_DISPLAY_NAME;
    }
    
    // private helper methods -------------------------------------------------
    
    private static String buildPath(List/*<URL>*/ path) {
        String PATH_SEPARATOR = System.getProperty("path.separator"); // NOI18N
        StringBuffer sb = new StringBuffer(path.size() * 16);
        for (Iterator i = path.iterator(); i.hasNext(); ) {
            sb.append(Utils.urlToString((URL)i.next()));
            if (i.hasNext()) {
                sb.append(PATH_SEPARATOR);
            }
        }
        return sb.toString();
    }
    
    /**
     * Split an Ant-style path specification into components.
     * Tokenizes on <code>:</code> and <code>;</code>, paying
     * attention to DOS-style components such as <samp>C:\FOO</samp>.
     * Also removes any empty components.
     * @param path an Ant-style path (elements arbitrary) using DOS or Unix separators
     * @return a tokenization of that path into components
     */
    private static List/*<URL>*/ tokenizePath(String path) {
        try {
            List/*<URL>*/ l = new ArrayList();
            StringTokenizer tok = new StringTokenizer(path, ":;", true); // NOI18N
            char dosHack = '\0';
            char lastDelim = '\0';
            int delimCount = 0;
            while (tok.hasMoreTokens()) {
                String s = tok.nextToken();
                if (s.length() == 0) {
                    // Strip empty components.
                    continue;
                }
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if (c == ':' || c == ';') {
                        // Just a delimiter.
                        lastDelim = c;
                        delimCount++;
                        continue;
                    }
                }
                if (dosHack != '\0') {
                    // #50679 - "C:/something" is also accepted as DOS path
                    if (lastDelim == ':' && delimCount == 1 && (s.charAt(0) == '\\' || s.charAt(0) == '/')) {
                        // We had a single letter followed by ':' now followed by \something or /something
                        s = "" + dosHack + ':' + s;
                        // and use the new token with the drive prefix...
                    } else {
                        // Something else, leave alone.
                        l.add(Utils.fileToUrl(new File(Character.toString(dosHack))));
                        // and continue with this token too...
                    }
                    dosHack = '\0';
                }
                // Reset count of # of delimiters in a row.
                delimCount = 0;
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                        // Probably a DOS drive letter. Leave it with the next component.
                        dosHack = c;
                        continue;
                    }
                }
                l.add(Utils.fileToUrl(new File(s)));
            }
            if (dosHack != '\0') {
                //the dosHack was the last letter in the input string (not followed by the ':')
                //so obviously not a drive letter.
                //Fix for issue #57304
                l.add(Utils.fileToUrl(new File(Character.toString(dosHack))));
            }
            return l;
        } catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(e);
            return new ArrayList();
        }
    }
    
    
    private static List/*<URL>*/ listUrls(final File folder, final String[] filter) {
        File[] jars = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (!name.endsWith(".jar") || !dir.equals(folder)) {
                    return false;
                }
                for (int i = 0; i < filter.length; i++) {
                    if (name.indexOf(filter[i]) != -1) {
                        return false;
                    }
                }
                return true;
            }
        });
        if (jars == null) {
            return new ArrayList();
        }
        List/*<URL>*/ urls = new ArrayList(jars.length);
        for (int i = 0; i < jars.length; i++) {
            try {
                urls.add(Utils.fileToUrl(jars[i]));
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return urls;
    }
}
