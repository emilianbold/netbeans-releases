/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.client.javascript.debugger.firefox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.web.client.javascript.debugger.firefox.Launcher.LaunchDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * The Firefox extension installation manager
 *  
 * @author quynguyen
 */
public class FFExtensionManager {
    private static long BUTTON_DELAY = 8000L; // 8 seconds seems to work better
    
    private static final int EXTENSION_ALREADY_INSTALLED = 0;
    private static final int EXTENSION_INSTALL = 1;
    private static final int EXTENSION_NOT_INSTALLED = 2;
    
    private static final String FIREBUG_MIN_VERSION = "1.1.0b12";
    
    private static final String EXTENSION_CACHE = "extensions.cache";
    private static final String UNINSTALL_KEYWORD = "needs-uninstall";
    
    private static final String DEV_PROP = "netbeans.javascript.debugger.dev";
    private static final String FIREBUG_URI_PROP = "firebug.xpi.uri";

    private static final String FIREBUG_EXTENSION_ID = "firebug@software.joehewitt.com"; // NOI18N
    
    private static final String FIREBUG_XPI_URI = "http://www.getfirebug.com/releases/firebug/1.1/firebug-1.1.0b12.xpi"; // NOI18N
            
    private static final String FIREFOX_EXTENSION_ID = "netbeans-firefox-extension@netbeans.org"; // NOI18N

    private static final String FIREFOX_EXTENSION_PATH = "modules/ext/netbeans-firefox-extension.xpi"; // NOI18N

    private static final String APPDATA_CMD = "echo %AppData%"; // NOI18N

    private static final String[] WIN32_PROFILES_LOCATIONS = {
        "\\Mozilla\\Firefox\\" // NOI18N
    };
    private static final String[] LINUX_PROFILES_LOCATIONS = {
        "/.mozilla/firefox/" // NOI18N

    };
    private static final String[] MACOSX_PROFILES_LOCATIONS = {
        "/Library/Application Support/Firefox/", // NOI18N
        "/Library/Mozilla/Firefox/" // NOI18N

    };


    private static boolean extensionInstalled = false;

    public static boolean installFirefoxExtensions(HtmlBrowser.Factory browser) {
        File nbExtensionFile = InstalledFileLocator.getDefault().locate(FIREFOX_EXTENSION_PATH, null, false);
        if (nbExtensionFile == null) {
            Log.getLogger().severe("Could not find firefox extension in installation directory");
            return false;
        }
        
        File defaultProfile = getDefaultProfile();
        if (defaultProfile == null) {
            Log.getLogger().severe("Could not find Firefox default profile");
            return false;
        }        
        
        URI firebugURI = null;
        
        try {
            String firebugURIString = System.getProperty(FIREBUG_URI_PROP);
            if (firebugURIString != null && firebugURIString.length() > 0) {
                try {
                    firebugURI = new URI(firebugURIString);
                    
                    if (firebugURI.getScheme().equals("http") && !isHttpURLValid(firebugURI.toURL())) {
                        firebugURI = null;
                    }
                }catch (Exception ex) {
                    firebugURI = null;
                }
            }
            
            if (firebugURI == null) {
                firebugURI = new URI(FIREBUG_XPI_URI);
                if (!isHttpURLValid(firebugURI.toURL())) {
                    firebugURI = null;
                }
            }
        }catch (Exception ex) {
            firebugURI = null;
        }

        URI nbExtensionURI = nbExtensionFile.toURI();
        
        int nbExtInstall = checkFirefoxExtension(browser, FIREFOX_EXTENSION_ID, 
                null, nbExtensionURI, defaultProfile);
        
        int firebugInstall = checkFirefoxExtension(browser, FIREBUG_EXTENSION_ID, 
                FIREBUG_MIN_VERSION, firebugURI, defaultProfile);
        
        try {
            launchBrowser(browser, firebugInstall, firebugURI, nbExtInstall, nbExtensionURI);
        }catch (IOException ex) {
            Log.getLogger().log(Level.FINE, "Unexpected exception while launching browser", ex);
            return false;
        }
        
        extensionInstalled = true;
        
        if (nbExtInstall == EXTENSION_NOT_INSTALLED) {
            return false;
        }
        
        displayInstallDialog(firebugInstall, nbExtInstall, false);
        
        boolean continueCheck = true;
        
        do {
            nbExtInstall = checkFirefoxExtension(browser, FIREFOX_EXTENSION_ID, 
                null, nbExtensionURI, defaultProfile);
            firebugInstall = checkFirefoxExtension(browser, FIREBUG_EXTENSION_ID, 
                FIREBUG_MIN_VERSION, firebugURI, defaultProfile);
            
            if (nbExtInstall != EXTENSION_ALREADY_INSTALLED || firebugInstall != EXTENSION_ALREADY_INSTALLED) {
                try {
                    launchBrowser(browser, firebugInstall, firebugURI, nbExtInstall, nbExtensionURI);
                } catch (IOException ex) {
                    Log.getLogger().log(Level.FINE, "Unexpected exception while launching browser", ex);
                    return false;
                }                
                
                continueCheck = displayInstallDialog(firebugInstall, nbExtInstall, true);
            }
        }while ( (nbExtInstall != EXTENSION_ALREADY_INSTALLED || firebugInstall != EXTENSION_ALREADY_INSTALLED) && continueCheck);
        
        
        
        return true;
    }
    
    public static int checkFirefoxExtension(HtmlBrowser.Factory browser, String extensionId, 
            String minExtVersion, URI xpiURI, File defaultProfile) {
        
        File extensionDir = new File(defaultProfile, "extensions" + File.separator + extensionId);
        
        String extensionVersion = getVersion(extensionDir);
        File extensionFile = null;
        
        if (xpiURI != null && xpiURI.getScheme().equals("file")) {
            extensionFile = new File(xpiURI);
        }
        
        if (    extensionUpdateRequired(extensionVersion, minExtVersion) || 
                checkExtensionTimestamps(extensionFile, defaultProfile, extensionId) || 
                installDevExtension()) {
            
            if (xpiURI != null) {
                return EXTENSION_INSTALL;
            }
            
            return EXTENSION_NOT_INSTALLED;
        }
        
        return EXTENSION_ALREADY_INSTALLED;
    }
    
    
    private static void launchBrowser(HtmlBrowser.Factory browser, int firebugInstall, URI firebugURI, int nbExtInstall, URI nbExtensionURI) throws IOException {

        LaunchDescriptor launchDescriptor = new LaunchDescriptor(getBrowserExecutable(browser));
        List<String> uriList = new ArrayList<String>();
        
        if (firebugInstall == EXTENSION_INSTALL) {
            uriList.add(firebugURI.toString());
        }
        
        if (nbExtInstall == EXTENSION_INSTALL) {
            uriList.add(nbExtensionURI.toString());
        }
        
        if (uriList.size() > 0) {
            launchDescriptor.setURI(uriList);
            Launcher.launch(launchDescriptor);
        }        
    }
    
    private static boolean displayInstallDialog(int firebugInstall, int nbExtInstall, boolean useCancel) {
        
        String dialogText = null;
        String dialogTitle = null;
        
        if (firebugInstall == EXTENSION_INSTALL && nbExtInstall == EXTENSION_INSTALL) {
            dialogText = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_ALL_EXTENSIONS_URL_MSG");
            dialogTitle = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_EXTENSIONS_TITLE");
        }else if (firebugInstall == EXTENSION_INSTALL && nbExtInstall == EXTENSION_ALREADY_INSTALLED) {
            dialogText = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_FIREBUG_URL_MSG");
            dialogTitle = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_FIREBUG_TITLE");
        }else if (firebugInstall == EXTENSION_NOT_INSTALLED && nbExtInstall == EXTENSION_INSTALL) {
            dialogText = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_ALL_EXTENSIONS_NOURL_MSG");
            dialogTitle = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_EXTENSIONS_TITLE");
        }else if (firebugInstall == EXTENSION_NOT_INSTALLED && nbExtInstall == EXTENSION_ALREADY_INSTALLED) {
            dialogText = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_FIREBUG_NOURL_MSG");
            dialogTitle = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_FIREBUG_TITLE");
        }else if (firebugInstall == EXTENSION_ALREADY_INSTALLED && nbExtInstall == EXTENSION_INSTALL) {
            dialogText = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_EXTENSION_MSG");
            dialogTitle = NbBundle.getMessage(FFExtensionManager.class, "INSTALL_EXTENSION_TITLE");
        }
        
        if (dialogText == null || dialogTitle == null) {
            return true;
        }
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(dialogTitle);

        JLabel progressLabel = ProgressHandleFactory.createDetailLabelComponent(handle);
        JComponent progressBar = ProgressHandleFactory.createProgressComponent(handle);

        final JButton ok = new JButton();
        ok.setText(NbBundle.getMessage(FFExtensionManager.class, "OK_MSG"));
        ok.setEnabled(false);
        
        Object[] options = null;
        JButton cancel = null;
        
        if (useCancel) {
            cancel = new JButton();
            cancel.setText(NbBundle.getMessage(FFExtensionManager.class, "CANCEL_MSG"));
            
            options = new Object[] { ok, cancel };
        }else {
            options = new Object[] { ok };
        }
        
        
        
        NotifyDescriptor nd = new NotifyDescriptor(
                new Object[]{dialogText,
                    progressLabel,
                    progressBar
                },
                dialogTitle,
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE, options,
                NotifyDescriptor.OK_OPTION);

        // Start the progressbar animation
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                handle.start();
                handle.switchToIndeterminate();
            }
        });

        // enable button after BUTTON_DELAY seconds
        TimerTask enableButton = new TimerTask() {

            public void run() {
                ok.setEnabled(true);
            }
        };
        Timer timer = new Timer();
        timer.schedule(enableButton, BUTTON_DELAY);

        Object result = DialogDisplayer.getDefault().notify(nd);
        return (cancel != null && result != cancel);
    }
    
    private static boolean extensionUpdateRequired(String extVersion, String minVersion) {
        if (extVersion == null) {
            return true;
        }else if (minVersion == null) {
            return false;
        }
        
        List<Integer> extList = getVersionParts(extVersion);
        List<Integer> minList = getVersionParts(minVersion);
        
        for (int i = 0; i < Math.max(extList.size(), minList.size()); i++) {
            int extValue = (i >= extList.size()) ? 0 : extList.get(i).intValue();
            int minValue = (i >= minList.size()) ? 0 : minList.get(i).intValue();
            
            if (extValue < minValue) {
                return true;
            }
        }
        
        return false;
    }
    
    private static List<Integer> getVersionParts(String version) {
        List<Integer> result = new ArrayList<Integer>();
        
        StringTokenizer tokens = new StringTokenizer(version, ".");
        while (tokens.hasMoreTokens()) {
            String nextToken = tokens.nextToken();
            if (nextToken.contains("b")) {
                int index = nextToken.indexOf("b");
                
                String first = nextToken.substring(0, index);
                String second = nextToken.substring(index+1, nextToken.length());
                
                // version xxbyy is greater than any version xx-1 without a beta
                // but less than version xx without a beta
                result.add(new Integer(Integer.valueOf(first).intValue() - 1));
                result.add(Integer.valueOf(second));
            }else {
                result.add(Integer.valueOf(nextToken));
            }
        }
        
        return result;
    }
    
    
    // TODO remove this when real versioning is implemented
    private static boolean checkExtensionTimestamps(File extensionXpi, File profileDir, String extensionId) {
        if (extensionXpi == null) return false;
        
        File extensionDir = new File(new File(profileDir, "extensions"), extensionId);
        
        if (extensionDir.exists() && extensionDir.isDirectory()) {
            FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(extensionDir));
            FileObject xpiFO = FileUtil.toFileObject(FileUtil.normalizeFile(extensionXpi));
            
            Date extensionDate = xpiFO.lastModified();
            Date profileDate = dirFO.lastModified();
            
            return extensionDate.after(profileDate);
        }
        
        return false;
    }

    
    // TODO remove this when versioning is implemented
    private static boolean installDevExtension() {
        String val = System.getProperty(DEV_PROP);
        if (val != null && val.length() > 0) {
            return !extensionInstalled;
        }else {
            return false;
        }
    }
    
    private static String getVersion(File extensionDir) {
        File rdfFile = new File(extensionDir, "install.rdf"); // NOI18N
        
        if (rdfFile.isFile()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            Document doc = null;
            
            try {
                builder = factory.newDocumentBuilder();
                doc = builder.parse(rdfFile);
            }catch (Exception ex) {
                Log.getLogger().log(Level.WARNING, "Unexpected exception", ex);
                return null;
            }
            
            Node descriptionNode = null;
            
            for (Node node = doc.getDocumentElement().getFirstChild(); descriptionNode == null && node != null; node = node.getNextSibling()) {
                String nodeName = node.getNodeName();
                nodeName = (nodeName == null) ? "" : nodeName.toLowerCase();
                
                
                if (nodeName.equals("description") || nodeName.equals("rdf:description")) { // NOI18N
                    Node aboutNode = node.getAttributes().getNamedItem("about"); // NOI18N
                    if (aboutNode == null) {
                        aboutNode = node.getAttributes().getNamedItem("RDF:about"); // NOI18N
                    }
                    
                    if (aboutNode != null) {
                        String aboutText = aboutNode.getNodeValue();
                        aboutText = (aboutText == null) ? "" : aboutText.toLowerCase();
                        
                        if (aboutText.equals("urn:mozilla:install-manifest")) { // NOI18N
                            descriptionNode = node;
                        }
                    }
                }
            }
            
            if (descriptionNode == null) {
                return null;
            }
            
            // check node attributes for version info
            Node versionNode = descriptionNode.getAttributes().getNamedItem("em:version"); // NOI18N
            if (versionNode != null) {
                return versionNode.getNodeValue();
            }
            
            // check children nodes
            NodeList children = descriptionNode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String name = child.getNodeName();
                name = (name == null) ? "" : name.toLowerCase();
                
                if (name.equals("em:version")) { // NOI18N
                    return child.getTextContent();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Checks extension.cache to determine if the extension has been scheduled for removal
     * 
     * @param extensionID
     * @param profileDir
     * @return true if the extension will be uninstalled when Firefox restarts
     */
    private static boolean isUninstallingExtension(String extensionID, File profileDir) {
        File extensionCache = new File(profileDir, EXTENSION_CACHE);
        if (extensionCache.exists() && extensionCache.isFile()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(extensionCache));
                boolean foundExtension = false;
                
                while (br.ready() && !foundExtension) {
                    String nextLine = br.readLine();
                    if (nextLine != null) {
                        StringTokenizer tokens = new StringTokenizer(nextLine);
                        
                        while (tokens.hasMoreTokens()) {
                            String element = tokens.nextToken();
                            
                            if (element.equals(extensionID)) {
                                foundExtension = true;
                            }else if (foundExtension == true && element.equals(UNINSTALL_KEYWORD)) {
                                return true;
                            }
                        }
                    }
                }
            }catch (IOException ex) {
                Log.getLogger().log(Level.WARNING, "Error reading " + extensionCache.getAbsolutePath(), ex);
            }finally {
                if (br != null) {
                    try {
                        br.close();
                    }catch (IOException ex) {
                        Log.getLogger().log(Level.WARNING, "Unexpected exception", ex);
                    }
                }
            }
        }
        
        return false;
    }
    
    
    private static String[] getLocationsForOS() {
        if (Utilities.isWindows()) { // NOI18N
            return getUserPaths(WIN32_PROFILES_LOCATIONS);
        } else if (Utilities.isMac()) {
            return getUserPaths(MACOSX_PROFILES_LOCATIONS);
        } else {
            // assuming that linux/unix/sunos firefox paths are equivalent
            return getUserPaths(LINUX_PROFILES_LOCATIONS);
        }

    }

    private static String[] getUserPaths(String[] paths) {
        String[] result = new String[paths.length];
        String appRoot = getUserHome();

        if (appRoot == null) {
            return null;
        }
        for (int i = 0; i < paths.length; i++) {
            result[i] = appRoot + paths[i];
        }

        return result;
    }

    /**
     *
     * @return user home, %AppData% on Windows
     */
    private static String getUserHome() {
        String userHome = System.getProperty("user.home"); // NOI18N

        if (!Utilities.isWindows()) {
            return userHome;
        } else {
            String appData = userHome + File.separator + NbBundle.getMessage(FFExtensionManager.class, "WIN32_APPDATA_FOLDER");

            BufferedReader br = null;
            try {
                Process process = Runtime.getRuntime().exec(APPDATA_CMD);
                process.waitFor();

                InputStream input = process.getInputStream();
                br = new BufferedReader(new InputStreamReader(input));

                while (br.ready()) {
                    String nextLine = br.readLine();

                    if (nextLine.trim().length() == 0) continue;

                    File f = new File(nextLine.trim());
                    if (f.exists() && f.isDirectory()) {
                        return f.getAbsolutePath();
                    }
                }
            }catch (Exception ex) {
                Log.getLogger().info("Unable to run process: " + APPDATA_CMD);
            }finally {
                if (br != null) {
                    try {
                        br.close();
                    }catch (IOException ex) {
                    }
                }
            }

            return appData;
        }
    }

    private static File getDefaultProfile() {
        String[] firefoxDirs = getLocationsForOS();
        
        if (firefoxDirs != null) {
            for (String firefoxUserDir : firefoxDirs) {
                File dir = new File(firefoxUserDir);
                if (dir.isDirectory() && dir.exists()) {
                    List<FirefoxProfile> profiles = getAllProfiles(dir);
                    
                    if (profiles == null || profiles.size() == 0) {
                        // guess the default profile
                        File profilesDir = new File(dir, "Profiles"); // NOI18N
                        
                        if (profilesDir.isDirectory()) {
                            File[] childrenFiles = profilesDir.listFiles();
                            for (int i = 0; childrenFiles != null && i < childrenFiles.length; i++) {
                                File childFile = childrenFiles[i];
                                
                                if (childFile.isDirectory() && childFile.getAbsolutePath().endsWith(".default")) { // NOI18N
                                    return childFile;
                                }
                            }
                        }
                    }else {
                        // find a "default" profile
                        for (FirefoxProfile profile : profiles) {
                            if (profile.isDefaultProfile()) {
                                File profileDir = null;
                                
                                if (profile.isRelative()) {
                                    profileDir = new File(dir, profile.getPath());
                                }else {
                                    profileDir = new File(profile.getPath());
                                }
                                
                                if (profileDir.isDirectory()) {
                                    return profileDir;
                                }
                            }
                        }
                        
                        // otherwise pick the first valid profile
                        for (FirefoxProfile profile : profiles) {
                            File profileDir = null;

                            if (profile.isRelative()) {
                                profileDir = new File(dir, profile.getPath());
                            } else {
                                profileDir = new File(profile.getPath());
                            }

                            if (profileDir.isDirectory()) {
                                return profileDir;
                            }                            
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * readProfiles
     *
     * @param file
     * @return File[]
     */
    private static File[] readProfiles(File dir) {
        List<File> list = new ArrayList<File>();
        File profilesIni = new File(dir, "profiles.ini"); // NOI18N

        if (profilesIni.exists()) {
            LineNumberReader r = null;
            try {
                r = new LineNumberReader(new FileReader(profilesIni));
                String line;
                Map<String, Map<String, String>> sections = new HashMap<String, Map<String, String>>();
                Map<String, String> last = null;
                Pattern sectionPattern = Pattern.compile("^\\x5B(.*)\\x5D$"); // NOI18N

                Pattern valuePattern = Pattern.compile("^(.[^=]*)=(.*)$"); // NOI18N

                while ((line = r.readLine()) != null) {
                    Matcher matcher = sectionPattern.matcher(line);
                    if (matcher.find()) {
                        last = new HashMap<String, String>();
                        sections.put(matcher.group(1), last);
                        continue;
                    } else if (last == null) {
                        continue;
                    }
                    matcher = valuePattern.matcher(line);
                    if (matcher.find()) {
                        last.put(matcher.group(1), matcher.group(2));
                    }
                }
                for (Iterator i = sections.keySet().iterator(); i.hasNext();) {
                    String section = (String) i.next();
                    if (section.startsWith("Profile")) { // NOI18N

                        Map properties = (Map) sections.get(section);
                        String path = (String) properties.get("Path"); // NOI18N

                        String isRelative = (String) properties.get("IsRelative"); // NOI18N

                        File profile;
                        if (isRelative != null && "1".equals(isRelative)) { // NOI18N

                            profile = new File(dir, path);
                        } else {
                            profile = new File(path); // TODO: base64 decode ?

                        }
                        boolean def = properties.containsKey("Default"); // NOI18N

                        if (def) {
                            list.add(0, profile);
                        } else {
                            list.add(profile);
                        }
                    }
                }
            } catch (IOException e) {
                Log.getLogger().log(Level.SEVERE, "Could not read Firefox profiles", e);
            } finally {
                if (r != null) {
                    try {
                        r.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
        return (File[]) list.toArray(new File[list.size()]);
    }

    // XXX Copied from JSAbstractDebugger
    protected static String getBrowserExecutable(HtmlBrowser.Factory browser) {
        if (browser != null) {
            try {
                Method method = browser.getClass().getMethod("getBrowserExecutable");
                NbProcessDescriptor processDescriptor = (NbProcessDescriptor) method.invoke(browser);
                return processDescriptor.getProcessName();
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return "firefox"; // NOI18N

    }

    private static boolean isHttpURLValid(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            int response = connection.getResponseCode();
            
            return response == HttpURLConnection.HTTP_OK;
        }catch (Exception ex) {
            return false;
        }
    }
    
    private static List<FirefoxProfile> getAllProfiles(File dir) {
        File profileCfg = new File(dir, "profiles.ini"); // NOI18N
        try {
            BufferedReader reader = new BufferedReader(new FileReader(profileCfg));
            List<List<String>> profileData = new ArrayList<List<String>>();
            
            List<String> currentProfile = null;
            while(reader.ready()) {
                String line = reader.readLine().trim();
                
                if (line.startsWith("[") && line.endsWith("]")) { // NOI18N
                    if (currentProfile != null) {
                        profileData.add(currentProfile);
                    }
                    
                    currentProfile = new ArrayList<String>();
                    currentProfile.add(line);
                }else if (line.indexOf('=') > 0) { // NOI18N
                    currentProfile.add(line);
                }
            }
            
            if (currentProfile != null && !profileData.contains(currentProfile)) {
                profileData.add(currentProfile);
            }
            
            List<FirefoxProfile> allProfiles = new ArrayList<FirefoxProfile>();            
            
            for (List<String> profileText : profileData) {
                if (profileText.size() == 0) {
                    continue;
                }else if (!profileText.get(0).startsWith("[")) {
                    continue;
                }
                
                FirefoxProfile profile = new FirefoxProfile();
                boolean isValidProfile = false;
                for (String line : profileText) {
                    int index = line.indexOf('='); // NOI18N
                    
                    if (index > 0 && index < line.length()-1) {
                        String var = line.substring(0, index);
                        String val = line.substring(index+1, line.length());
                        
                        if (var.equals("Name")) { // NOI18N
                            isValidProfile = true;
                        }else if (var.equals("IsRelative")) { // NOI18N
                            if (val.equals("1")) { // NOI18N
                                profile.setRelative(true);
                            }
                        }else if (var.equals("Path")) { // NOI18N
                            profile.setPath(val);
                        }else if (var.equals("Default")) { // NOI18N
                            if (val.equals("1")) { // NOI18N
                                profile.setDefaultProfile(true);
                            }
                        }
                    }
                }
                
                if (isValidProfile) {
                    allProfiles.add(profile);
                }
            }
            
            return allProfiles;
        }catch (IOException ex) {
            Log.getLogger().log(Level.WARNING, "Could not read Firefox profiles", ex);
        }
        
        return null;
    }
    
    private static final class FirefoxProfile {
        private boolean relative = false;
        private boolean defaultProfile = false;
        private String path = "";
        
        public FirefoxProfile() {
        }

        public boolean isDefaultProfile() {
            return defaultProfile;
        }

        public void setDefaultProfile(boolean defaultProfile) {
            this.defaultProfile = defaultProfile;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isRelative() {
            return relative;
        }

        public void setRelative(boolean relative) {
            this.relative = relative;
        }
    }
}
