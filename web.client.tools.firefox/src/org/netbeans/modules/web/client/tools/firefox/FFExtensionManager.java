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
package org.netbeans.modules.web.client.tools.firefox;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.web.client.tools.api.FirefoxBrowserUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
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
    private static final long BUTTON_DELAY = 10000L; // 10 second delay
    private static final long FIREFOX_CHECK_PERIOD = 700L; // 700ms delay
    
    private static final String PROFILE_LOCK_WINDOWS = "parent.lock";
    private static final String PROFILE_LOCK = "lock";
    
    private static final String FIREBUG_1_2_MIN_VERSION = "1.2.1";
    
    private static final String EXTENSION_CACHE = "extensions.cache";
    private static final String UNINSTALL_KEYWORD = "needs-uninstall";
    private static final String INSTALL_KEYWORD = "needs-install";
    
    private static final String FIREBUG_EXTENSION_ID = "firebug@software.joehewitt.com"; // NOI18N
    
    private static final String FIREBUG_EXTENSION_PATH = "modules/ext/firebug-1.2.1-fx.xpi"; // NOI18N
               
    private static final String FIREFOX_EXTENSION_ID = "netbeans-firefox-extension@netbeans.org"; // NOI18N

    private static final String FIREFOX_EXTENSION_PATH = "modules/ext/netbeans-firefox-extension.xpi"; // NOI18N
    
    private static final String CHECKSUM_FILENAME = "netbeans-firefox-extension.jar.MD5"; // NOI18N
    
    public static boolean installFirefoxExtensions(HtmlBrowser.Factory browser) {
        File customProfile = FirefoxBrowserUtils.getProfileFromPreferences();
        File defaultProfile = customProfile != null ? customProfile : FirefoxBrowserUtils.getDefaultProfile();
        
        if (defaultProfile == null) {
            Log.getLogger().severe("Could not find Firefox default profile.  Firefox debugging not available.");
            return false;
        }

        File nbExtensionFile = InstalledFileLocator.getDefault().locate(FIREFOX_EXTENSION_PATH,
                "org.netbeans.modules.web.client.tools.firefox.extension", // NOI18N
                false);
        if (nbExtensionFile == null) {
            Log.getLogger().severe("Could not find firefox extension in installation directory");
            return false;
        }
        
        File firebugExtensionFile = InstalledFileLocator.getDefault().locate(FIREBUG_EXTENSION_PATH,
                "org.netbeans.modules.web.client.tools.firefox.extension", // NOI18N 
                false);
        if (firebugExtensionFile == null) {
            Log.getLogger().severe("Could not find firebug extension in installation directory");
            return false;
        }
        
        boolean nbExtInstall = extensionRequiresInstall(browser, FIREFOX_EXTENSION_ID, 
                null, nbExtensionFile, defaultProfile, true);
        
        boolean firebugInstall = extensionRequiresInstall(browser, FIREBUG_EXTENSION_ID,
                    FIREBUG_1_2_MIN_VERSION, firebugExtensionFile, defaultProfile, false);
        
        if (!nbExtInstall || !firebugInstall) {
            List<String> extensionIds = new LinkedList<String>();
            
            if (!nbExtInstall) {
                extensionIds.add(FIREFOX_EXTENSION_ID);
            }
            if (!firebugInstall) {
                extensionIds.add(FIREBUG_EXTENSION_ID);
            }
            
            boolean extensionsInitialized = checkExtensionInstall(extensionIds, defaultProfile);
            if (!extensionsInitialized) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        NbBundle.getMessage(FFExtensionManager.class, "FIREFOX_NOT_RESTARTED_MSG"),
                        NotifyDescriptor.ERROR_MESSAGE);
                
                nd.setTitle(NbBundle.getMessage(FFExtensionManager.class, "FIREFOX_NOT_RESTARTED_TITLE"));
                DialogDisplayer.getDefault().notify(nd);
                return false;
            }
        }
        
        boolean installSuccess = true;

        if (nbExtInstall || firebugInstall) {
            // Ask the user if they want to install the extensions
            String dialogMsg = Utilities.isMac() ? NbBundle.getMessage(FFExtensionManager.class, "INSTALL_EXTENSIONS_MSG_MACOS") :
                NbBundle.getMessage(FFExtensionManager.class, "INSTALL_EXTENSIONS_MSG");
            
            NotifyDescriptor installDesc = new NotifyDescriptor.Confirmation(
                    dialogMsg,
                    NbBundle.getMessage(FFExtensionManager.class, "INSTALL_EXTENSIONS_TITLE"),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(installDesc);
            if (result != NotifyDescriptor.OK_OPTION) {
                return false;
            }
            
            if (isFirefoxRunning(defaultProfile)) {
                boolean cancelled = !displayFirefoxRunningDialog(defaultProfile);
                if (cancelled) {
                    return false;
                }
            }
        }
        
        if (nbExtInstall) {
            installSuccess &= installExtension(defaultProfile, nbExtensionFile, FIREFOX_EXTENSION_ID);
        }
        
        if (firebugInstall) {
            installSuccess &= installExtension(defaultProfile, firebugExtensionFile, FIREBUG_EXTENSION_ID);
        }
        
        // HACK
        // If the extensions installed successfully and make sure Firefox notices them
        if (installSuccess && (nbExtInstall || firebugInstall)) {
            File extensionsDotRdf = new File(defaultProfile, "extensions.rdf"); // NOI18N
            if (extensionsDotRdf.exists()) {
                extensionsDotRdf.delete();
            }
        }
        
        return installSuccess;
    }
    
    public static boolean extensionRequiresInstall(HtmlBrowser.Factory browser, String extensionId, 
            String minExtVersion, File extensionFile, File defaultProfile, boolean isNbExtension) {
        
        File extensionDir = new File(defaultProfile, "extensions" + File.separator + extensionId);
        String extensionVersion = getVersion(extensionDir);
        
        boolean isInstalling = checkExtensionCache(extensionId, extensionDir, INSTALL_KEYWORD);
        
        // if the user did not restart before the check was made (user presses 'Continue'
        // without waiting for Firefox to restart)
        if (isInstalling) {
            return false;
        }
        
        if ( extensionUpdateRequired(extensionVersion, minExtVersion) || 
                (isNbExtension && !checkExtensionChecksum(extensionFile, defaultProfile, extensionId))) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean installExtension(File profileDir, File extensionXPI, String extensionId) {
        File extensionDir = new File(profileDir, "extensions" + File.separator + extensionId); // NOI18N
        
        // keep a backup of an existing extension just in case
        File backupFolder = null;
        if (extensionDir.exists()) {
            String tmp = extensionId + "-tmp"; // NOI18N
            
            do {
                tmp += "0";
                backupFolder = new File(extensionDir.getParentFile(), tmp);
            } while (backupFolder.exists());
            
            if (!extensionDir.renameTo(backupFolder)) {
                Log.getLogger().warning("Could not create backup for existing extension: " + extensionId);
                rmDir(extensionDir);
                backupFolder = null;
            }
        }
        
        // copy the archive
        boolean copySuccessful = false;
        try {
            extractFiles(extensionXPI, extensionDir);
            copySuccessful = true;
        } catch (IOException ex) {
            Log.getLogger().log(Level.SEVERE, "Could not copy extension: " + extensionId, ex);
            return false;
        } finally {
            if (backupFolder != null) {
                if (copySuccessful) {
                    rmDir(backupFolder);
                } else {
                    rmDir(extensionDir);
                    boolean movedBack = backupFolder.renameTo(extensionDir);
                    if (!movedBack) {
                        Log.getLogger().warning("Could not restore old extension: " + extensionId);
                    }
                }
            }
        }
        
        return true;
    }
    
    private static void rmDir(File folder) {
        if (!folder.exists()) {
            return;
        }
        
        File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                rmDir(child);
            }
        }
        folder.delete();
    }
    
    private static void extractFiles(File zipFile, File destDir) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                
                if (entry.isDirectory()) {
                    File newFolder = new File(destDir, fileName);
                    newFolder.mkdirs();
                } else {
                    File file = new File(destDir, fileName);
                    if (file.exists() && file.isDirectory()) {
                        throw new IOException("Cannot write normal file to existing directory with the same path");
                    }
                    
                    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
                    InputStream input = zip.getInputStream(entry);
                    
                    try {
                        final byte[] buffer = new byte[4096];
                        int len;
                        while ((len = input.read(buffer)) >= 0) {
                            output.write(buffer, 0, len);
                        }
                    } finally {
                        output.close();
                        input.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }
    
    private static boolean displayFirefoxRunningDialog(final File profileDir) {
        String dialogText = NbBundle.getMessage(FFExtensionManager.class, "FIREFOX_RUNNING_MSG");
        String dialogTitle = NbBundle.getMessage(FFExtensionManager.class, "FIREFOX_RUNNING_TITLE");

        final JButton ok = new JButton();
        Mnemonics.setLocalizedText(ok, NbBundle.getMessage(FFExtensionManager.class, "INSTALL_BUTTON"));
        ok.setEnabled(false);

        JButton cancel = new JButton();
        Mnemonics.setLocalizedText(cancel, NbBundle.getMessage(FFExtensionManager.class, "CANCEL_BUTTON"));

        Object[] options = new Object[]{ok, cancel};
        DialogDescriptor dd = new DialogDescriptor(dialogText, dialogTitle, true, 
                options, cancel, DialogDescriptor.BOTTOM_ALIGN, null, null);

        dd.setClosingOptions(new Object[] { cancel });
        
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        final boolean[] cancelled = { true };
        
        final TimerTask runningCheck = new TimerTask() {
            @Override
            public void run() {
                if (dialog.isVisible() && !isFirefoxRunning(profileDir)) {
                    cancelled[0] = false;
                    dialog.setVisible(false);
                    this.cancel();
                }
            }
        };
        
        ok.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                cancelled[0] = false;
                dialog.setVisible(false);
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
        
        Timer profileCheckTimer = new Timer();
        profileCheckTimer.schedule(runningCheck, FIREFOX_CHECK_PERIOD, FIREFOX_CHECK_PERIOD);
        
        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
            timer.cancel();
            profileCheckTimer.cancel();
        }
        
        return !cancelled[0];
    }
    
    private static boolean isFirefoxRunning(File profileDir) {
        if (Utilities.isWindows()) {
            return new File(profileDir, PROFILE_LOCK_WINDOWS).exists();
        } else if (Utilities.isMac()) {
            // XXX TODO Figure out how to detect if FF is running on MacOS
            return false;
        } else {
            String[] fileNames = profileDir.list();
            if (fileNames != null) {
                for (String fileName : fileNames) {
                    if (fileName.equals(PROFILE_LOCK)) {
                        return true;
                    }
                }
            }
            
            return false;
        }
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
            } else if (extValue > minValue) {
                return false;
            }
        }
        
        return false;
    }
    
    private static List<Integer> getVersionParts(String version) {
        List<Integer> result = new ArrayList<Integer>();
        
        StringTokenizer tokens = new StringTokenizer(version, ".");
        while (tokens.hasMoreTokens()) {
            String nextToken = tokens.nextToken();
            try {
                if (nextToken.contains("a")) {
                    int index = nextToken.indexOf("a");

                    String first = nextToken.substring(0, index);
                    String second = nextToken.substring(index + 1, nextToken.length());

                    // version xxbyy is greater than any version xx-1 without a beta
                    // but less than version xx without a beta
                    result.add(new Integer(Integer.valueOf(first).intValue() - 1));
                    result.add(Integer.valueOf(-1));
                    result.add(Integer.valueOf(second));
                } else if (nextToken.contains("b")) {
                    int index = nextToken.indexOf("b");

                    String first = nextToken.substring(0, index);
                    String second = nextToken.substring(index + 1, nextToken.length());

                    // version xxbyy is greater than any version xx-1 without a beta
                    // but less than version xx without a beta
                    result.add(new Integer(Integer.valueOf(first).intValue() - 1));
                    result.add(Integer.valueOf(second));
                } else {
                    result.add(Integer.valueOf(nextToken));
                }
            } catch (NumberFormatException ex) {
                // skip values that are not numbers
            }
        }
        
        return result;
    }
    
    
    /**
     * 
     * @param extensionXpi
     * @param profileDir
     * @param extensionId
     * @return true if the checksums match
     */
    private static boolean checkExtensionChecksum(File extensionXpi, File profileDir, String extensionId) {
        if (extensionXpi == null) return true;
        
        File extensionDir = new File(new File(profileDir, "extensions"), extensionId); // NOI18N
        File checksumFile = new File(extensionDir, CHECKSUM_FILENAME);
        
        if (checksumFile.exists() && checksumFile.isFile()) {
            ZipFile extensionZip = null;
            try {
                extensionZip = new ZipFile(extensionXpi);
                ZipEntry entry = extensionZip.getEntry(CHECKSUM_FILENAME);
                
                if (entry != null) {                    
                    BufferedInputStream profileInput  = new BufferedInputStream(new FileInputStream(checksumFile));
                    InputStream xpiInput = extensionZip.getInputStream(entry);
                    
                    try {
                        final byte[] profileBuffer = new byte[512];
                        final byte[] xpiBuffer = new byte[512];
                        int profileLen, xpiLen;
                        
                        do {
                            profileLen = profileInput.read(profileBuffer);
                            xpiLen = xpiInput.read(xpiBuffer);
                            
                            if (profileLen != xpiLen) {
                                return false;
                            }
                            
                            for (int i = 0; i < profileLen; i++) {
                                if (profileBuffer[i] != xpiBuffer[i]) {
                                    return false;
                                }
                            }
                            
                        } while (profileLen >= 0);
                        
                        return true;
                    } finally {
                        profileInput.close();
                        xpiInput.close();
                    }
                    
                }
            } catch (IOException ex) {
                Log.getLogger().log(Level.SEVERE, "Error checking extension XPI", ex);
            } finally {
                if (extensionZip != null) {
                    try {
                        extensionZip.close();
                    }catch (IOException ex) {
                        Log.getLogger().log(Level.SEVERE, "Error closing zip file", ex);
                    }
                }
            }
        }
        
        return false;
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
    
    private static boolean checkExtensionInstall(List<String> extensionIDs, File profileDir) {
        File extensionCache = new File(profileDir, EXTENSION_CACHE);
        if (extensionCache.exists() && extensionCache.isFile()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(extensionCache));
                
                while (br.ready() && extensionIDs.size() > 0) {
                    String nextLine = br.readLine();
                    if (nextLine != null) {
                        String[] words = nextLine.split("\\s");
                        for (String element : words) {
                            if (extensionIDs.contains(element)) {
                                extensionIDs.remove(element);
                            }
                        }
                    }
                }
                
                return extensionIDs.isEmpty();
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
        
        return true;
    }    
    
    /**
     * Checks extension.cache to determine if the extension has been scheduled for removal
     * 
     * @param extensionID the Firefox extension ID to check
     * @param profileDir the profile directory
     * @param keywords the keyword in the extension.cache to check
     * 
     * @return true if the keyword was found for the given extension
     */
    private static boolean checkExtensionCache(String extensionID, File profileDir, String keyword) {
        File extensionCache = new File(profileDir, EXTENSION_CACHE);
        if (extensionCache.exists() && extensionCache.isFile()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(extensionCache));
                boolean foundExtension = false;
                
                while (br.ready() && !foundExtension) {
                    String nextLine = br.readLine();
                    if (nextLine != null) {
                        String[] words = nextLine.split("\\s");
                        for (String element : words) {
                            if (element.equals(extensionID)) {
                                foundExtension = true;
                            } else if (foundExtension == true && element.equals(keyword)) {
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
    
    private static boolean isHttpURLValid(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int response = connection.getResponseCode();

            return response == HttpURLConnection.HTTP_OK;

        } catch (Exception ex) {
            return false;
        }
    }

    private static File getURLResource(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setReadTimeout(5000);

            File f = File.createTempFile("firebug-1.2.0b9", "xpi"); // NOI18N
            BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(f));
            
            try {
                final byte[] buffer = new byte[4096];
                int len;
                while ((len = input.read(buffer)) >= 0) {
                    output.write(buffer, 0, len);
                }
            } finally {
                output.close();
                input.close();
            }
            
            return f;
        } catch (Exception ex) {
            Log.getLogger().log(Level.INFO, "Unable to get Firebug 1.2.0b9 from http://www.getfirebug.com", ex);
        }
        
        return null;
    }
    
}
