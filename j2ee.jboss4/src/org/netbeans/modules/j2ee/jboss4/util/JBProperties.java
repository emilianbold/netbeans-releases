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
package org.netbeans.modules.j2ee.jboss4.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.customizer.CustomizerSupport;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbCollections;

/**
 * Helper class that makes it easier to access and set JBoss instance properties.
 *
 * @author sherold
 */
public class JBProperties {

    /** Java platform property which is used as a java platform ID */
    public static final String PLAT_PROP_ANT_NAME = "platform.ant.name"; //NOI18N

    // properties
    public  static final String PROP_PROXY_ENABLED = "proxy_enabled";   // NOI18N
    private static final String PROP_JAVA_PLATFORM = "java_platform";   // NOI18N
    private static final String PROP_JAVA_OPTS     = "java_opts";       // NOI18N
    private static final String PROP_SOURCES       = "sources";         // NOI18N
    private static final String PROP_JAVADOCS      = "javadocs";        // NOI18N
    private static final String PROP_SERVER_DIR    = "server-dir";      // NOI18N
    private static final String PROP_ROOT_DIR      = "root-dir";        // NOI18N

    // default values
    private static final String  DEF_VALUE_JAVA_OPTS     = ""; // NOI18N
    private static final boolean DEF_VALUE_PROXY_ENABLED = true;

    private final InstanceProperties ip;
    private final JBDeploymentManager manager;

    // credentials initialized with default values
    private String username = "admin"; // NOI18N
    private String password = "admin"; // NOI18N

    /** timestamp of the jmx-console-users.properties file when it was parsed for the last time */
    private long updateCredentialsTimestamp;

    private static final Logger LOGGER = Logger.getLogger(JBProperties.class.getName());


    /** Creates a new instance of JBProperties */
    public JBProperties(JBDeploymentManager manager) {
        this.manager = manager;
        ip = manager.getInstanceProperties();
    }

    public boolean supportsJavaEE5ejb3() {
        return new File(getServerDir(), "deploy/ejb3.deployer").exists() || // JBoss 4 // NOI18N
               new File(getServerDir(), "deployers/ejb3.deployer").exists(); // JBoss 5 // NOI18N
    }

    public boolean supportsJavaEE5web() {
        return new File(getServerDir(), "deployers/jbossweb.deployer").exists(); // JBoss 5 // NOI18N
    }

    public File getServerDir() {
        return new File(ip.getProperty(PROP_SERVER_DIR));
    }

    public File getRootDir() {
        return new File(ip.getProperty(PROP_ROOT_DIR));
    }

    public File getLibsDir() {
        return new File(getServerDir(), "lib"); // NOI18N
    }

    public boolean getProxyEnabled() {
        String val = ip.getProperty(PROP_PROXY_ENABLED);
        return val != null ? Boolean.valueOf(val).booleanValue()
                           : DEF_VALUE_PROXY_ENABLED;
    }

    public void setProxyEnabled(boolean enabled) {
        ip.setProperty(PROP_PROXY_ENABLED, Boolean.toString(enabled));
    }

    public JavaPlatform getJavaPlatform() {
        String currentJvm = ip.getProperty(PROP_JAVA_PLATFORM);
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform[] installedPlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
        for (int i = 0; i < installedPlatforms.length; i++) {
            String platformName = (String)installedPlatforms[i].getProperties().get(PLAT_PROP_ANT_NAME);
            if (platformName != null && platformName.equals(currentJvm)) {
                return installedPlatforms[i];
            }
        }
        // return default platform if none was set
        return jpm.getDefaultPlatform();
    }

    public void setJavaPlatform(JavaPlatform javaPlatform) {
        ip.setProperty(PROP_JAVA_PLATFORM, (String)javaPlatform.getProperties().get(PLAT_PROP_ANT_NAME));
    }

    public String getJavaOpts() {
        String val = ip.getProperty(PROP_JAVA_OPTS);
        return val != null ? val
                           : DEF_VALUE_JAVA_OPTS;
    }

    public void setJavaOpts(String javaOpts) {
        ip.setProperty(PROP_JAVA_OPTS, javaOpts);
    }

    public List<URL> getClasses() {
        List<URL> list = new ArrayList<URL>();
        try {
            File rootDir = getRootDir();
            File serverDir = getServerDir();
            list.add(fileToUrl(new File(rootDir, "client/jboss-j2ee.jar")));  // NOI18N

            File wsClientLib = new File(rootDir, "client/jbossws-client.jar"); // NOI18N
            if (wsClientLib.exists()) {
                list.add(fileToUrl(wsClientLib));
            }

            addFiles(new File(rootDir, "lib"), list); // NOI18N
            addFiles(new File(serverDir, "/lib"), list); // NOI18N
            if (supportsJavaEE5ejb3()) {
                File ejb3deployer = new File(serverDir, "/deploy/ejb3.deployer/");  // NOI18N
                if (ejb3deployer.exists()) {
                    addFiles(ejb3deployer, list);
                } else if ((ejb3deployer = new File(serverDir, "/deployers/ejb3.deployer/")).exists()) { // NOI18N
                    addFiles(ejb3deployer, list);
                }
            }

            File jsfAPI = new File(serverDir, "/deploy/jboss-web.deployer/jsf-libs/jsf-api.jar"); // NOI18N
            if (jsfAPI.exists()) {
                try {
                    list.add(fileToUrl(jsfAPI));
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            } else if ((jsfAPI = new File(serverDir, "/deploy/jbossweb-tomcat55.sar/jsf-libs/myfaces-api.jar")).exists()) { // NOI18N
                try {
                    list.add(fileToUrl(jsfAPI));
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            } else if ((jsfAPI = new File(serverDir, "/deployers/jbossweb.deployer/jsf-libs/jsf-api.jar")).exists()) { // NOI18N
                try {
                    list.add(fileToUrl(jsfAPI));
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            File jsfIMPL = new File(serverDir, "/deploy/jboss-web.deployer/jsf-libs/jsf-impl.jar"); // NOI18N
            if (jsfIMPL.exists()) {
                try {
                    list.add(fileToUrl(jsfIMPL));
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            } else if ((jsfIMPL = new File(serverDir, "/deploy/jbossweb-tomcat55.sar/jsf-libs/myfaces-impl.jar")).exists()) { // NOI18N
                try {
                    list.add(fileToUrl(jsfIMPL));
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            } else if ((jsfIMPL = new File(serverDir, "/deployers/jbossweb.deployer/jsf-libs/jsf-impl.jar")).exists()) { // NOI18N
                try {
                    list.add(fileToUrl(jsfIMPL));
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
        } catch (MalformedURLException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        return list;
    }

    private static class FF implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar") || new File(dir, name).isDirectory(); // NOI18N
        }
    }

    private void addFiles(File folder, List l) {
        File files [] = folder.listFiles(new FF());
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addFiles(files[i], l);
            } else {
                try {
                    l.add(fileToUrl(files[i]));
                } catch (MalformedURLException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }
        }
    }

    public List<URL> getSources() {
        String path = ip.getProperty(PROP_SOURCES);
        if (path == null) {
            return new ArrayList<URL>();
        }
        return CustomizerSupport.tokenizePath(path);
    }

    public void setSources(List<URL> path) {
        ip.setProperty(PROP_SOURCES, CustomizerSupport.buildPath(path));
        manager.getJBPlatform().notifyLibrariesChanged();
    }

    public List<URL> getJavadocs() {
        String path = ip.getProperty(PROP_JAVADOCS);
        if (path == null) {
            ArrayList<URL> list = new ArrayList<URL>();
            try {
                File j2eeDoc = InstalledFileLocator.getDefault().locate("docs/javaee5-doc-api.zip", null, false); // NOI18N
                if (j2eeDoc != null) {
                    list.add(fileToUrl(j2eeDoc));
                }
            } catch (MalformedURLException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            return list;
        }
        return CustomizerSupport.tokenizePath(path);
    }

    public void setJavadocs(List<URL> path) {
        ip.setProperty(PROP_JAVADOCS, CustomizerSupport.buildPath(path));
        manager.getJBPlatform().notifyLibrariesChanged();
    }

    public synchronized String getUsername() {
        updateCredentials();
        return username;
    }

    public synchronized String getPassword() {
        updateCredentials();
        return password;
    }

    // private helper methods -------------------------------------------------

    private synchronized void updateCredentials() {
        File usersPropFile = new File(getServerDir(), "/conf/props/jmx-console-users.properties");
        long lastModified = usersPropFile.lastModified();
        if (lastModified == updateCredentialsTimestamp) {
            LOGGER.log(Level.FINER, "Credentials are up-to-date.");
            return;
        }
        Properties usersProps = new Properties();
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(usersPropFile));
            try {
                usersProps.load(is);
            } finally {
                is.close();
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, usersPropFile + " not found.", e);
            return;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error while reading " + usersPropFile, e);
            return;
        }

        Enumeration<String> names = NbCollections.checkedEnumerationByFilter(usersProps.propertyNames(), String.class, false);
        if (names.hasMoreElements()) {
            username = names.nextElement();
            password = usersProps.getProperty(username);
        }

        updateCredentialsTimestamp = lastModified;
    }

    /** Return URL representation of the specified file. */
    private static URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }
}
