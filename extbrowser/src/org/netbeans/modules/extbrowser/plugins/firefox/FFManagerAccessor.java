/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.extbrowser.plugins.firefox;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.extbrowser.plugins.ExtensionManager;
import org.netbeans.modules.extbrowser.plugins.ExtensionManagerAccessor;
import org.netbeans.modules.extbrowser.plugins.PluginLoader;
import org.netbeans.modules.extbrowser.plugins.Utils;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



/**
 * @author ads
 *
 */
public class FFManagerAccessor implements ExtensionManagerAccessor {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor#getManager()
     */
    @Override
    public BrowserExtensionManager getManager() {
        return new FFExtensionManager();
    }
    
    private static class FFExtensionManager extends AbstractBrowserExtensionManager {
        
        private static final long BUTTON_DELAY = 10000L; // 10 second delay
        private static final long CHECK_PERIOD = 700L;   // 700ms delay
        
        private static final String PROFILE_LOCK_WINDOWS = "parent.lock";// NOI18N
        private static final String PROFILE_LOCK = "lock";               // NOI18N
        
        private static final String[] WIN32_PROFILES_LOCATIONS = {
            "\\Mozilla\\Firefox\\"                          // NOI18N
        };
        private static final String[] LINUX_PROFILES_LOCATIONS = {
            "/.mozilla/firefox/"                            // NOI18N

        };
        private static final String[] MACOSX_PROFILES_LOCATIONS = {
            "/Library/Application Support/Firefox/",        // NOI18N
            "/Library/Mozilla/Firefox/"                     // NOI18N

        };
        
        private static final String EXTENSION_ID = "ros-firefox-plugin@netbeans.org";   // NOI18N
        
        private static final String INSTALL_KEYWORD = "needs-install";              // NOI18N
        
        private static final String EXTENSION_CACHE = "extensions.cache";           // NOI18N
        
        private static final String EXTENSION_PATH = "modules/ext/netbeans-ros-firefox-plugin.xpi"; // NOI18N
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor.BrowserExtensionManager#isInstalled()
         */
        @Override
        public ExtensionManager.ExtensitionStatus isInstalled() {
            File defaultProfile = getDefaultProfile();
            File extensionDir = new File(defaultProfile, "extensions" +    // NOI18N 
                    File.separator + EXTENSION_ID);                  
            String extensionVersion = getVersion(extensionDir);
            
            if ( extensionVersion != null ){
                boolean isInstalling = checkExtensionCache(extensionDir,
                        INSTALL_KEYWORD);

                // if the user did not restart before the check was made (user
                // presses 'Continue'
                // without waiting for Firefox to restart)
                if (isInstalling) {
                    return ExtensionManager.ExtensitionStatus.MISSING;
                }
            }
            else {
                /*
                 *  Previous check is old plugin storage approach : extracted files in dir.
                 *  This check for packaged plugin stored as xpi file
                 */
                File xpi = new File(defaultProfile, "extensions" +             // NOI18N 
                        File.separator + EXTENSION_ID+".xpi");                 // NOI18N 
                extensionVersion =getVersion(xpi);
            }
            
            if ( isUpdateRequired(extensionVersion)) 
                    //|| !checkExtensionChecksum(defaultProfile) )
            {
                // return false if extension is not initialized
                if (!checkExtensionInstall(defaultProfile)) {
                    return ExtensionManager.ExtensitionStatus.INSTALLED;
                } else {
                    return ExtensionManager.ExtensitionStatus.MISSING;
                }
            } 
            else {
                return ExtensionManager.ExtensitionStatus.INSTALLED;
            }
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor.BrowserExtensionManager#install(org.netbeans.modules.web.plugins.PluginLoader)
         */
        @Override
        public boolean install(PluginLoader loader, ExtensionManager.ExtensitionStatus state) {
            File defaultProfile = getDefaultProfile();
            File extensionDir = new File(defaultProfile, "extensions" +    // NOI18N 
                    File.separator + EXTENSION_ID);
            
            File extensionFile = InstalledFileLocator.getDefault().locate(
                    EXTENSION_PATH,PLUGIN_MODULE_NAME, false);
            
            if ( extensionFile == null ){
                Logger.getLogger(FFExtensionManager.class.getCanonicalName()).
                    severe("Could not find firefox extension in installation directory");   // NOI18N
                return false;
            }
            
            if ( extensionDir.exists() && extensionDir.isDirectory() ){
                // use old style install 
                return oldStyleInstall(extensionFile);
            }
            // New installation approach : install add-on via opening URL in FF
            
            // Ask the user if they want to install the extensions
            NotifyDescriptor installDesc = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(FFExtensionManager.class, 
                            "LBL_InstallMsgWithoutRestart"),                    // NOI18N
                    NbBundle.getMessage(FFExtensionManager.class, 
                            "TTL_InstallExtension"),                            // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(installDesc);
            if (result != NotifyDescriptor.OK_OPTION) {
                return false;
            }
            try {
                loader.requestPluginLoad( new URL("file:///"+extensionFile.getCanonicalPath()));
            }
            catch( IOException e ){
                Logger.getLogger( FFExtensionManager.class.getCanonicalName()).
                    log(Level.INFO , null ,e );
                return false;
            }
            return true;
        }
        
        /*
         * Previous version installation: explicit unpack xpi into extensions dir.  
         */
        private boolean oldStyleInstall(File extensionFile){
            File defaultProfile = getDefaultProfile();
            File extensionDir = new File(defaultProfile, "extensions" +    // NOI18N 
                    File.separator + EXTENSION_ID);
            // plugin is not initialized
            if ( !checkExtensionInstall( defaultProfile ) ){
                NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                        NbBundle.getMessage(FFExtensionManager.class, 
                                "LBL_FirefoxNotRestarted"),                                 // NOI18N
                                    NotifyDescriptor.ERROR_MESSAGE);
                
                descriptor.setTitle(NbBundle.getMessage(FFExtensionManager.class, 
                        "TTL_FirefoxNotRestarted"));                                        // NOI18N
                DialogDisplayer.getDefault().notify(descriptor);
                return false;
            }
            
            // Ask the user if they want to install the extensions
            String dialogMsg = Utilities.isMac() ? NbBundle.getMessage(
                    FFExtensionManager.class, "LBL_InstallMsgMacOs") :          // NOI18N
                NbBundle.getMessage(FFExtensionManager.class, "LBL_InstallMsg");// NOI18N
            
            NotifyDescriptor installDesc = new NotifyDescriptor.Confirmation(
                    dialogMsg,
                    NbBundle.getMessage(FFExtensionManager.class, 
                            "TTL_InstallExtension"),                            // NOI18N
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
            
            boolean success = installExtension(extensionDir, extensionFile);
            
            if ( success ){
                // HACK
                // If the extensions installed successfully and make sure Firefox notices them
                File extensionsDotRdf = new File(defaultProfile, "extensions.rdf"); // NOI18N
                if (extensionsDotRdf.exists()) {
                    extensionsDotRdf.delete();
                }
            }
            return success;
        }
        
        /*
         *  TODO : this method should automatically retrieve current plugin 
         *  version to avoid manual source update
         */
        @Override
        protected String getCurrentPluginVersion(){
            File extensionFile = InstalledFileLocator.getDefault().locate(
                    EXTENSION_PATH,PLUGIN_MODULE_NAME, false);
            return getVersion(extensionFile);
        }
        
        private boolean installExtension(File extensionDir, File extensionFile) {      
            // keep a backup of an existing extension just in case
            File backupFolder = null;
            if (extensionDir.exists()) {
                String tmp = EXTENSION_ID + "-tmp"; // NOI18N
                
                do {
                    tmp += "0";
                    backupFolder = new File(extensionDir.getParentFile(), tmp);
                } 
                while (backupFolder.exists());
                
                if (!extensionDir.renameTo(backupFolder)) {
                    Logger.getLogger(FFExtensionManager.class.getCanonicalName()).
                        warning("Could not create backup for existing extension: " // NOI18N
                                + EXTENSION_ID);
                    Utils.rmDir(extensionDir);
                    backupFolder = null;
                }
            }
            
            // copy the archive
            boolean copySuccessful = false;
            try {
                Utils.extractFiles(extensionFile, extensionDir);
                copySuccessful = true;
            } 
            catch (IOException ex) {
                Logger.getLogger(FFExtensionManager.class.getCanonicalName()).
                    log(Level.SEVERE, "Could not copy extension: " + // NOI18N
                        EXTENSION_ID, ex);
                return false;
            } 
            finally {
                if (backupFolder != null) {
                    if (copySuccessful) {
                        Utils.rmDir(backupFolder);
                    } 
                    else {
                        Utils.rmDir(extensionDir);
                        boolean movedBack = backupFolder.renameTo(extensionDir);
                        if (!movedBack) {
                            Logger.getLogger(FFExtensionManager.class.getCanonicalName()).
                                warning("Could not restore old extension: " +   // NOI18N 
                                        EXTENSION_ID);
                        }
                    }
                }
            }
            
            return true;
        }
        
        private boolean isFirefoxRunning(File profileDir) {
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
        
        private String getVersion(File file) {
            Node descriptionNode = null;
            
            Document doc = getInstallRdfDocument(file);
            if ( doc == null ){
                return null;
            }

            for (Node node = doc.getDocumentElement().getFirstChild(); descriptionNode == null
                    && node != null; node = node.getNextSibling())
            {
                String nodeName = node.getNodeName();
                nodeName = (nodeName == null) ? "" : nodeName.toLowerCase();

                if (nodeName.equalsIgnoreCase("description")
                        || nodeName.equalsIgnoreCase("rdf:description")) // NOI18N
                {
                    Node aboutNode = node.getAttributes().getNamedItem("about"); // NOI18N
                    if (aboutNode == null) {
                        aboutNode = node.getAttributes().getNamedItem(
                                "RDF:about"); // NOI18N
                    }

                    if (aboutNode != null) {
                        String aboutText = aboutNode.getNodeValue();
                        aboutText = (aboutText == null) ? "" : aboutText
                                .toLowerCase(Locale.US);

                        if (aboutText.equals("urn:mozilla:install-manifest")) // NOI18N
                        {
                            descriptionNode = node;
                        }
                    }
                }
            }

            if (descriptionNode == null) {
                return null;
            }

            // check node attributes for version info
            Node versionNode = descriptionNode.getAttributes().getNamedItem(
                    "em:version"); // NOI18N
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

            return null;
        }
        
        private Document getInstallRdfDocument(File file  ){
            if ( !file.exists()){
                return null;
            }
            File rdfFile = null;
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            Document doc = null;
            
            try {
                builder = factory.newDocumentBuilder();
                
                if ( file.isDirectory() ){
                    rdfFile = new File(file, "install.rdf");                // NOI18N
                }
                else {
                    return Utils.parseZipXml(file, "install.rdf", builder);  // NOI18N
                }
                
                if ( rdfFile.isFile() ){
                    doc = builder.parse( rdfFile);
                }
                return doc;
            }
            catch (ParserConfigurationException ex) {
                Logger.getLogger( FFExtensionManager.class.getCanonicalName()).
                    log(Level.WARNING, null, ex);               // NOI18N     
            }
            catch (IOException ex) {
                Logger.getLogger( FFExtensionManager.class.getCanonicalName()).
                    log(Level.WARNING, null, ex);               // NOI18N     
            }
            catch (SAXException ex) {
                Logger.getLogger( FFExtensionManager.class.getCanonicalName()).
                    log(Level.WARNING, null, ex);               // NOI18N     
            }
            return null;
        }

        
        private File getDefaultProfile() {
            String[] firefoxDirs = getLocationsForOS();

            if (firefoxDirs != null) {
                for (String firefoxUserDir : firefoxDirs) {
                    File dir = new File(firefoxUserDir);
                    if (dir.isDirectory() && dir.exists()) {
                        List<FirefoxProfile> profiles = getAllProfiles(dir);

                        if (profiles == null || profiles.size() == 0) {
                            return guessDefaultProfile(dir);
                        }
                        else {
                            return findDefaultProfile(dir, profiles);
                        }
                    }
                }
            }
            return null;
        }

        private File findDefaultProfile( File dir, List<FirefoxProfile> profiles )
        {
            // find a "default" profile
            for (FirefoxProfile profile : profiles) {
                if (profile.isDefaultProfile()) {
                    File profileDir = null;

                    if (profile.isRelative()) {
                        profileDir = new File(dir,
                                profile.getPath());
                    }
                    else {
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
                    profileDir = new File(dir,
                            profile.getPath());
                }
                else {
                    profileDir = new File(profile.getPath());
                }

                if (profileDir.isDirectory()) {
                    return profileDir;
                }
            }
            return null;
        }

        private File guessDefaultProfile( File dir ) {
            // guess the default profile
            File profilesDir = new File(dir, "Profiles");       // NOI18N

            if (profilesDir.isDirectory()) {
                File[] childrenFiles = profilesDir.listFiles();
                for (int i = 0; childrenFiles != null
                        && i < childrenFiles.length; i++)
                {
                    File childFile = childrenFiles[i];

                    if (childFile.isDirectory()
                            && childFile.getAbsolutePath()
                                    .endsWith(".default"))      // NOI18N
                    { 
                        return childFile;
                    }
                }
            }
            return null;
        }

        private String[] getLocationsForOS() {
            if (Utilities.isWindows()) {
                return Utils.getUserPaths(WIN32_PROFILES_LOCATIONS);
            } 
            else if (Utilities.isMac()) {
                return Utils.getUserPaths(MACOSX_PROFILES_LOCATIONS);
            } 
            else {
                // assuming that linux/unix/sunos firefox paths are equivalent
                return Utils.getUserPaths(LINUX_PROFILES_LOCATIONS);
            }
        }
        
        private List<FirefoxProfile> getAllProfiles(File dir) {
            File profileCfg = new File(dir, "profiles.ini"); // NOI18N
            try {
                BufferedReader reader = new BufferedReader(new FileReader(profileCfg));
                Set<LinkedHashSet<String>> profileData = 
                    new HashSet<LinkedHashSet<String>>();
                
                LinkedHashSet<String> currentProfile = null;
                while(reader.ready()) {
                    String line = reader.readLine().trim();
                    
                    if (line.startsWith("[") && line.endsWith("]")) { // NOI18N
                        if (currentProfile != null) {
                            profileData.add(currentProfile);
                        }
                        
                        currentProfile = new LinkedHashSet<String>();
                        currentProfile.add(line);
                    }
                    else if (line.indexOf('=') > 0) { // NOI18N
                        currentProfile.add(line);
                    }
                }
                
                if (currentProfile != null) {
                    profileData.add(currentProfile);
                }
                
                List<FirefoxProfile> allProfiles = new ArrayList<FirefoxProfile>(
                        profileData.size());            
                
                for (LinkedHashSet<String> profileText : profileData) {
                    if (profileText.size() == 0) {
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
                            }
                            else if (var.equals("IsRelative")) { // NOI18N
                                if (val.equals("1")) { // NOI18N
                                    profile.setRelative(true);
                                }
                            }
                            else if (var.equals("Path")) { // NOI18N
                                profile.setPath(val);
                            }
                            else if (var.equals("Default")) { // NOI18N
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
            }
            catch (IOException ex) {
                Logger.getLogger(FFExtensionManager.class.getCanonicalName()).
                    log(Level.WARNING, "Could not read Firefox profiles", ex);  // NOI18N
            }
            
            return null;
        }
        
        /*
         * Checks extension.cache to determine if the extension has been scheduled for removal
         */
        private boolean checkExtensionCache(File profileDir, String keyword) 
        {
            File extensionCache = new File(profileDir, EXTENSION_CACHE);
            if (extensionCache.exists() && extensionCache.isFile()) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(extensionCache));
                    boolean foundExtension = false;
                    
                    while (br.ready() && !foundExtension) {
                        String nextLine = br.readLine();
                        if (nextLine != null) {
                            String[] words = nextLine.split("\\s");             // NOI18N
                            for (String element : words) {
                                if (element.equals(EXTENSION_ID)) {
                                    foundExtension = true;
                                    if ( keyword == null ){
                                        return true;
                                    }
                                } 
                                else if (foundExtension == true && 
                                        element.equals(keyword)) 
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
                catch (IOException ex) {
                    Logger.getLogger(FFExtensionManager.class.getCanonicalName()).
                        log(Level.WARNING, null, ex);
                }
                finally {
                    if (br != null) {
                        try {
                            br.close();
                        }
                        catch (IOException ex) {
                            Logger.getLogger(FFExtensionManager.class.
                                    getCanonicalName()).log(Level.WARNING, null, ex);
                        }
                    }
                }
            }
            
            return false;
        }
        
        private boolean checkExtensionInstall(File profileDir) 
        {   
            File extensionCache = new File(profileDir, EXTENSION_CACHE);
            if (extensionCache.exists() && extensionCache.isFile()) {
                return checkExtensionCache( profileDir , null );
            }
            else {
                return true;
            }
        }
        
        private boolean displayFirefoxRunningDialog(final File profileDir) {
            String dialogText = NbBundle.getMessage(FFExtensionManager.class, 
                    "LBL_FirefoxRunning");                              // NOI18N
            String dialogTitle = NbBundle.getMessage(FFExtensionManager.class, 
                    "TTL_FirefoxRunning");                              // NOI18N

            final JButton ok = new JButton();
            Mnemonics.setLocalizedText(ok, NbBundle.getMessage(
                    FFExtensionManager.class, "LBL_ForceInstall"));     // NOI18N
            ok.setEnabled(false);

            JButton cancel = new JButton();
            Mnemonics.setLocalizedText(cancel, 
                    NbBundle.getMessage(FFExtensionManager.class, "LBL_CanelButton"));// NOI18N

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
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelled[0] = false;
                    dialog.setVisible(false);
                }
                
            });

            // enable button after BUTTON_DELAY seconds
            TimerTask enableButton = new TimerTask() {
                @Override
                public void run() {
                    ok.setEnabled(true);
                }
            };
            Timer timer = new Timer();
            timer.schedule(enableButton, BUTTON_DELAY);
            
            Timer profileCheckTimer = new Timer();
            profileCheckTimer.schedule(runningCheck, CHECK_PERIOD, CHECK_PERIOD);
            
            try {
                dialog.setVisible(true);
            } finally {
                dialog.dispose();
                timer.cancel();
                profileCheckTimer.cancel();
            }
            
            return !cancelled[0];
        }
        
        private static final class FirefoxProfile {
            private boolean relative;
            private boolean defaultProfile;
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

}
