/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.FilesList;

/**
 *
 * @author Kirill Sorokin
 */
public class NetBeansUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static void addCluster(File nbLocation, String clusterName) throws IOException {
        File netbeansclusters = new File(nbLocation, NETBEANS_CLUSTERS);
        
        List<String> list = FileUtils.readStringList(netbeansclusters);
        for (String string: list) {
            if (string.equals(clusterName)) {
                return;
            }
        }
        list.add(clusterName);
        
        FileUtils.writeStringList(netbeansclusters, list);
    }
    
    public static void removeCluster(File nbLocation, String clusterName) throws IOException {
        File netbeansclusters = new File(nbLocation, NETBEANS_CLUSTERS);
        
        List<String> list = FileUtils.readStringList(netbeansclusters);
        list.remove(clusterName);
        
        FileUtils.writeStringList(netbeansclusters, list);
    }
    
    public static FilesList createProductId(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        return FileUtils.writeFile(productid, NB_IDE_ID);
    }
    
    public static FilesList addPackId(File nbLocation, String packId) throws IOException {
        final File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        final File productid = new File(nbCluster, PRODUCT_ID);
        
        final String id;
        if (!productid.exists()) {
            id = NB_IDE_ID;
        } else {
            id = FileUtils.readFile(productid).trim();
        }
        
        final List<String> ids =
                new LinkedList(Arrays.asList(id.split(PACK_ID_SEPARATOR)));
        
        boolean packAdded = false;
        for (int i = 1; i < ids.size(); i++) {
            if (packId.equals(ids.get(i))) {
                return new FilesList();
            }
            
            if (packId.compareTo(ids.get(i)) < 0) {
                ids.add(i, packId);
                packAdded = true;
                break;
            }
        }
        
        if (!packAdded) {
            ids.add(packId);
        }
        
        return FileUtils.writeFile(
                productid,
                StringUtils.asString(ids, PACK_ID_SEPARATOR));
    }
    
    public static void removePackId(File nbLocation, String packId) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        String id;
        if (!productid.exists()) {
            id = NB_IDE_ID;
        } else {
            id = FileUtils.readFile(productid).trim();
        }
        
        String[] components = id.split(PACK_ID_SEPARATOR);
        
        StringBuilder builder = new StringBuilder(components[0]);
        for (int i = 1; i < components.length; i++) {
            if (!components[i].equals(packId)) {
                builder.append(PACK_ID_SEPARATOR).append(components[i]);
            }
        }
        
        FileUtils.writeFile(productid, builder);
    }
    
    public static void removeProductId(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        FileUtils.deleteFile(productid);
    }
    
    public static FilesList createLicenseAcceptedMarker(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File license_accepted = new File(nbCluster, LICENSE_ACCEPTED);
        
        if (!license_accepted.exists()) {
            return FileUtils.writeFile(license_accepted, "");
        } else {
            return new FilesList();
        }
    }
    
    public static void removeLicenseAcceptedMarker(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File license_accepted = new File(nbCluster, LICENSE_ACCEPTED);
        
        if (license_accepted.exists()) {
            FileUtils.deleteFile(license_accepted);
        }
    }
    
    public static void setJavaHome(File nbLocation, File javaHome) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        String correctJavaHome = StringUtils.escapeRegExp(javaHome.getAbsolutePath());
        
        contents = contents.replaceAll(
                "#?" + NETBEANS_JDKHOME + "\".*?\"",
                NETBEANS_JDKHOME + "\"" + correctJavaHome + "\"");
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static void setUserDir(File nbLocation, File userDir) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        String correctUserDir = StringUtils.escapeRegExp(userDir.getAbsolutePath());
        
        contents = contents.replaceAll(
                NETBEANS_USERDIR +
                "\".*?\"",
                NETBEANS_USERDIR +
                "\"" + correctUserDir + "\"");
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static String getJvmOption(File nbLocation, String name) throws IOException {
        return getJvmOption(nbLocation, name, "=");
    }
    
    public static String getJvmOption(File nbLocation, String name, String separator) throws IOException {
        final File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        final String pattern = StringUtils.format(
                NETBEANS_OPTIONS_PATTERN,
                StringUtils.escapeRegExp(name),
                StringUtils.escapeRegExp(separator));
        
        final Matcher matcher =
                Pattern.compile(pattern).matcher(FileUtils.readFile(netbeansconf));
        
        if (matcher.find()) {
            String value = matcher.group(4);
            if (value == null) {
                value = matcher.group(5);
            }
            if (value == null) {
                value = matcher.group(6);
            }
            
            return value;
        } else {
            return null;
        }
    }
    
    @Deprecated
    public static void addJvmOption(File nbLocation, String name) throws IOException {
        setJvmOption(nbLocation, name, null);
    }
    
    @Deprecated
    public static void addJvmOption(File nbLocation, String name, String value) throws IOException {
        setJvmOption(nbLocation, name, value);
    }
    
    @Deprecated
    public static void addJvmOption(File nbLocation, String name, String value, boolean quote) throws IOException {
        setJvmOption(nbLocation, name, value, quote);
    }
    
    @Deprecated
    public static void addJvmOption(File nbLocation, String name, String value, boolean quote, String separator) throws IOException {
        setJvmOption(nbLocation, name, value, quote, separator);
    }
    
    public static void setJvmOption(File nbLocation, String name) throws IOException {
        setJvmOption(nbLocation, name, null, false);
    }
    
    public static void setJvmOption(File nbLocation, String name, String value) throws IOException {
        setJvmOption(nbLocation, name, value, false);
    }
    
    public static void setJvmOption(File nbLocation, String name, String value, boolean quote) throws IOException {
        setJvmOption(nbLocation, name, value, quote, "=");
    }
    
    public static void setJvmOption(File nbLocation, String name, String value, boolean quote, String separator) throws IOException {
        final File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        final String option = "-J" + name + (value != null ?
            separator + (quote ? "\\\"" : "") + value + (quote ? "\\\"" : "") : "");
        
        final String pattern = StringUtils.format(
                NETBEANS_OPTIONS_PATTERN,
                StringUtils.escapeRegExp(name),
                StringUtils.escapeRegExp(separator));
        
        String contents = FileUtils.readFile(netbeansconf);
        final Matcher matcher =
                Pattern.compile(pattern).matcher(contents);
        
        if (matcher.find()) {
            contents = contents.replace(matcher.group(3), option);
        } else {
            contents = contents.replace(
                    NETBEANS_OPTIONS + "\"",
                    NETBEANS_OPTIONS + "\"" + option + " ");
        }
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static void removeJvmOption(File nbLocation, String name) throws IOException {
        removeJvmOption(nbLocation, name, "=");
    }
    
    public static void removeJvmOption(File nbLocation, String name, String separator) throws IOException {
        final File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        final String pattern = StringUtils.format(
                NETBEANS_OPTIONS_PATTERN,
                StringUtils.escapeRegExp(name),
                StringUtils.escapeRegExp(separator));
        
        final Matcher matcher =
                Pattern.compile(pattern).matcher(contents);
        
        if (matcher.find()) {
            contents = contents.replace(" " + matcher.group(3), "");
            contents = contents.replace(matcher.group(3) + " ", "");
            contents = contents.replace(matcher.group(3), "");
        }
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    /**
     * Get JVM memory value.
     *
     * @param nbLocation NetBeans home directory
     * @param memoryType Memory type that can be one of the following values
     *          <ul><li> <code>MEMORY_XMX</code></li>
     *              <li> <code>MEMORY_XMS</code></li>
     *              <li> <code>MEMORY_XSS</code></li>
     *          </ul>
     * @return The size of memory in bytes. <br>
     *         If there is no such option then return 0;
     */
    public static long getJvmMemorySize(File nbLocation, String memoryType) throws IOException {
        final String size = getJvmOption(nbLocation, memoryType, "");
        
        if (size != null) {
            return getJavaMemorySize(size);
        } else {
            return 0;
        }
    }
    
    /**
     * Get JVM memory value. <br>
     * If value is <i>zero</i> then remove the jvm option from netbeans options<br><br>
     * @param nbLocation NetBeans home directory
     * @param memoryType Memory type that can be one of the following values
     *           <ul><li> <code>MEMORY_XMX</code></li>
     *              <li> <code>MEMORY_XMS</code></li>
     *              <li> <code>MEMORY_XSS</code></li>
     *          </ul>
     * @param value Size of memory to be set
     */
    public static void setJvmMemorySize(File nbLocation, String memoryType, long size) throws IOException {
        setJvmOption(nbLocation, memoryType, formatJavaMemoryString(size), false, "");
    }
    
    public static File getNbCluster(File nbLocation) {
        for (File child: nbLocation.listFiles()) {
            if (child.isDirectory() && child.getName().matches(NB_CLUSTER_PATTERN)) {
                return child;
            }
        }
        
        return null;
    }
    
    /**
     * Get resolved netbeans user directory
     * @param nbLocation NetBeans home directory
     * @throws IOException if can`t get netbeans default userdir
     */
    public static File getNetBeansUserDirFile(File nbLocation) throws IOException {
        String dir = getNetBeansUserDir(nbLocation);
        dir = dir.replace(USER_HOME_TOKEN, System.getProperty("user.home"));
        return new File(dir);
    }
    
    /**
     * Get netbeans user directory as it is written in netbeans.conf
     * @param nbLocation NetBeans home directory
     * @throws IOException if can`t get netbeans default userdir
     */
    public static String getNetBeansUserDir(File nbLocation) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        String contents = FileUtils.readFile(netbeansconf);
        Matcher matcher = Pattern.compile(
                NEW_LINE_PATTERN + SPACES_PATTERN +
                NETBEANS_USERDIR +
                "\"(.*?)\"").matcher(contents);
        if(matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException("Can`t get netbeans userdir from " + netbeansconf);
        }
    }
    
    /**
     * Get jdkhome as it is written in netbeans.conf
     * @param nbLocation NetBeans home directory
     * @return JDK location
     * @throws IOException if can`t get netbeans_jdkhome value of netbeans.conf
     */
    public static String getJavaHome(File nbLocation) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        String contents = FileUtils.readFile(netbeansconf);
        
        Matcher matcher = Pattern.compile(
                NEW_LINE_PATTERN + SPACES_PATTERN +
                NETBEANS_JDKHOME +
                "\"(.*?)\"").matcher(contents);
        
        if(matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException("Can`t get netbeans javahome from " + netbeansconf);
        }
    }
    
    /**
     * Check if NetBeans is running
     * @param nbLocation NetBeans home directory
     * @return True if NetBeans is running
     * @throws IOException if can`t say for sure whether it is running or not
     */
    public static boolean isNbRunning(File nbLocation) throws IOException {
        return FileUtils.exists(getLockFile(nbLocation));
    }
    
    public static File getLockFile(File nbLocation) throws IOException {
        return new File(getNetBeansUserDirFile(nbLocation), "lock");
    }
    
    /**
     *  Test for running NetBeans IDE.<br>
     *  If the lock file exist - issue a warning but do not throw an exception
     */
    public static boolean warnNetbeansRunning(File nbLocation) {
        try {
            boolean isRunning = isNbRunning(nbLocation);
            if (isRunning) {
                if(!checkedAndRunning.contains(nbLocation)) {
                    checkedAndRunning.add(nbLocation);
                    final String message = ResourceUtils.getString(
                            NetBeansUtils.class,
                            "NU.warning.running"); // NOI18N
                    final String warning = StringUtils.format(
                            message,
                            nbLocation,
                            NetBeansUtils.getLockFile(nbLocation));
                    
                    ErrorManager.notifyWarning(warning);
                }
            } else {
                checkedAndRunning.remove(nbLocation);
            }
            
            return isRunning;
        } catch (IOException e) {
            ErrorManager.notifyDebug(
                    "Can`t say for sure if NetBeans is running or not",
                    e);
        }
        
        return false;
    }
    
    public static void updateNetBeansHome(final File nbLocation) throws IOException {
        FileUtils.modifyFile(
                new File(nbLocation, NETBEANS_CONF),
                NETBEANS_HOME_TOKEN,
                nbLocation.getAbsolutePath());
    }
    
    public static void runUpdater(File nbLocation) throws IOException {
        File jdkLocation = new File(getJavaHome(nbLocation));
        LogManager.log("running the NetBeans updater : ");
        LogManager.log("    nbLocation = " + nbLocation);
        LogManager.log("    jdkLocation = " + jdkLocation);
        
        List <File> classes = new ArrayList <File> ();
        List <File> nbDirs = new ArrayList <File> ();
        
        
        File netbeansclusters = new File(nbLocation, NETBEANS_CLUSTERS);
        List<String> list = FileUtils.readStringList(netbeansclusters);
        List<String> clusters = new ArrayList <String> ();
        
        File platformCluster = null;
        
        for(String s : list) {
            String cluster = s.trim();
            if(!cluster.startsWith("#") &&
                    cluster.equals("etc")) {
                nbDirs.add(new File(nbLocation, cluster));
                if(cluster.startsWith("platform")) {
                    platformCluster= new File(nbLocation, cluster);
                }
            }
        }
        
        String nbDirsString = StringUtils.asString(nbDirs, File.pathSeparator);
        
        LogManager.log("    adding classes to classpath");
        classes.add(new File(platformCluster,
                "lib" + File.separator + "boot.jar"));
        if(!SystemUtils.isMacOS()) {
            classes.add(new File(jdkLocation,
                    "lib" + File.separator + "tools.jar" ));
        }
        classes.add(new File(jdkLocation,
                "lib" + File.separator + "dt.jar"));
        classes.add(new File(platformCluster,
                "modules" + File.separator +
                "ext" + File.separator +
                "updater.jar"));
        String classpath = StringUtils.asString(classes, File.pathSeparator);
        
        String importClassProp = "netbeans.importclass";
        String nbHomeProp = "netbeans.home";
        String nbHome = platformCluster.getPath();
        String nbUserdir = getNetBeansUserDirFile(nbLocation).getPath();
        String nbUserdirProp = "netbeans.user";
        String nbDirsProp = "netbeans.dirs";
        String sysProp ="-D";
        String eq = "=";
        String java = JavaUtils.getExecutable(jdkLocation).getPath();
        LogManager.log("    executing updater...");
        SystemUtils.executeCommand(nbLocation, new String [] {
            java,
            sysProp + importClassProp + eq + UPDATER_CLASSNAME,
            sysProp + nbHomeProp    + eq + nbHome,
            sysProp + nbUserdirProp + eq + nbUserdir,
            sysProp + nbDirsProp    + eq + nbDirsString,
            "-Xms32m", "-XX:MaxPermSize=96m", "-Xverify:none", "-Xmx128m",
            "-cp", classpath,
            UPDATER_FRAMENAME, "--nosplash"});
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static long getJavaMemorySize(String string) {
        String suffix = string.substring(string.length() - 1);
        
        if(!suffix.matches(DIGITS_PATTERN)) {
            long value = Long.parseLong(string.substring(0, string.length() - 1));
            if(suffix.equalsIgnoreCase("k")) {
                value *= K;
            } else if(suffix.equalsIgnoreCase("m")) {
                value *= M;
            } else if(suffix.equalsIgnoreCase("g")) {
                value *= G;
            } else if(suffix.equalsIgnoreCase("t")) {
                value *= T;
            }
            return value;
        } else {
            return new Long(string).longValue() * M; // default - megabytes
        }
    }
    
    private static String formatJavaMemoryString(long size) {
        if((size > T) && (size % T == 0)) {
            return StringUtils.EMPTY_STRING + (size/T) + "t";
        } else if((size > G) && (size % G == 0)) {
            return StringUtils.EMPTY_STRING + (size/G) + "g";
        } else if((size > M) && (size % M == 0)) {
            return StringUtils.EMPTY_STRING + (size/M) + "m";
        }  else if((size > K) && (size % K == 0)) {
            return StringUtils.EMPTY_STRING + (size/K) + "k";
        } else {
            if(size > (10 * M)) {
                // round up to the nearest M value
                return StringUtils.EMPTY_STRING + (size/M + 1) + "m";
            } else {
                // round up to the nearest K value
                return StringUtils.EMPTY_STRING + (size/K + 1) + "k";
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private NetBeansUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String NETBEANS_CLUSTERS =
            "etc/netbeans.clusters"; // NOI18N
    public static final String NETBEANS_CONF =
            "etc/netbeans.conf"; // NOI18N
    public static final String PRODUCT_ID =
            "config/productid"; // NOI18N
    public static final String LICENSE_ACCEPTED =
            "var/license_accepted"; // NOI18N
    
    public static final String DIGITS_PATTERN =
            "[0-9]+"; // NOI18N
    public static final String CLUSTER_NUMBER_PATTERN =
            DIGITS_PATTERN + "(\\." + DIGITS_PATTERN + ")?"; // NOI18N
    
    public static final String NB_CLUSTER_PATTERN =
            "nb" + CLUSTER_NUMBER_PATTERN; // NOI18N
    public static final String NEW_LINE_PATTERN =
            "[\r\n|\n|\r]"; // NOI18N
    public static final String SPACES_PATTERN =
            "\\ *"; // NOI18N
    
    public static final String NETBEANS_USERDIR =
            "netbeans_default_userdir="; // NOI18N
    public static final String NETBEANS_JDKHOME =
            "netbeans_jdkhome="; // NOI18N
    public static final String NETBEANS_OPTIONS =
            "netbeans_default_options="; // NOI18N
    
    public static final String NETBEANS_OPTIONS_PATTERN =
            NETBEANS_OPTIONS + "\"(.*?)( ?)(-J{0}(?:{1}\\\\\\\"(.*?)\\\\\\\"|{1}(.*?)|())(?= |\"))( ?)(.*)?\"";
    
    public static final String NB_IDE_ID =
            "NB"; // NOI18N
    public static final String PACK_ID_SEPARATOR =
            "_"; // NOI18N
    public static final String MEMORY_XMX =
            "-Xmx"; // NOI18N
    public static final String MEMORY_XMS =
            "-Xms"; // NOI18N
    public static final String MEMORY_XSS =
            "-Xss"; // NOI18N
    public static final String USER_HOME_TOKEN =
            "${HOME}"; // NOI18N
    public static final String NETBEANS_HOME_TOKEN =
            "${NETBEANS_HOME}"; // NOI18N
    public static final String UPDATER_FRAMENAME = 
            "org.netbeans.updater.UpdaterFrame";
    public static final String UPDATER_CLASSNAME = 
            "org.netbeans.upgrade.AutoUpgrade";
    
    public static final long K =
            1024; // NOMAGI
    public static final long M =
            K * K;
    public static final long G =
            M * K;
    public static final long T =
            G * K;
    // one set for the whole installer
    private static Set <File> checkedAndRunning = new HashSet <File> ();
    
    private static final String MEMORY_SUFFIX_PATTERN =
            "[kKmMgGtT]?"; // NOI18N
}
