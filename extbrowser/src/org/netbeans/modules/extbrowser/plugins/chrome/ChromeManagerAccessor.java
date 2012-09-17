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
package org.netbeans.modules.extbrowser.plugins.chrome;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;

import org.json.simple.JSONObject;
import org.netbeans.modules.extbrowser.plugins.ExtensionManager;
import org.netbeans.modules.extbrowser.plugins.ExtensionManager.ExtensitionStatus;
import org.netbeans.modules.extbrowser.plugins.ExtensionManagerAccessor;
import org.netbeans.modules.extbrowser.plugins.PluginLoader;
import org.netbeans.modules.extbrowser.plugins.Utils;


import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;



/**
 * @author ads
 *
 */
public class ChromeManagerAccessor implements ExtensionManagerAccessor {
    
    private static final String NO_WEB_STORE_SWITCH=
            "netbeans.extbrowser.manual_chrome_plugin_install"; // NOI18N
    
    // XXX: change it to the real plugin url for FCS
    private static final String PLUGIN_PAGE= 
            "https://chrome.google.com/webstore/detail/icpgjfneehieebagbmdbhnlpiopdcmna?utm_campaign=en&utm_source=en-et-na-us-oc-webstrhm";// NOI18N

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor#getManager()
     */
    @Override
    public BrowserExtensionManager getManager() {
        return new ChromeExtensionManager();
    }

    
    static class ChromeExtensionManager extends AbstractBrowserExtensionManager {
        
        private static final String LAST_USED = "\"last_used\":";               // NOI18N
        
        private static final String VERSION = "\"version\":";                   // NOI18N
        
        private static final String STATE = "\"state\":";                       // NOI18N
        
        private static final String PLUGIN_NAME = "NetBeans IDE Support Plugin";// NOI18N
        
        private static final String EXTENSION_PATH = "modules/lib/netbeans-ros-chrome-plugin.crx"; // NOI18N

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor.BrowserExtensionManager#isInstalled()
         */
        @Override
        public ExtensionManager.ExtensitionStatus isInstalled() {
            while (true) {
                ExtensionManager.ExtensitionStatus result = isInstalledImpl();
                if (result == ExtensionManager.ExtensitionStatus.DISABLED) {
                    NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                            NbBundle.getMessage(ChromeExtensionManager.class, 
                                    "LBL_ChromePluginIsDisabled"),                   // NOI18N
                                        NotifyDescriptor.ERROR_MESSAGE);
                    descriptor.setTitle(NbBundle.getMessage(ChromeExtensionManager.class, 
                            "TTL_ChromePluginIsDisabled"));                             // NOI18N
                    if (DialogDisplayer.getDefault().notify(descriptor) != DialogDescriptor.OK_OPTION) {
                        return result;
                    }
                    continue;
                }
                return result;
            }
        }
        
        private ExtensionManager.ExtensitionStatus isInstalledImpl() {
            File defaultProfile = getDefaultProfile();
            if ( defaultProfile == null ){
                return ExtensionManager.ExtensitionStatus.MISSING;
            }
            File[] prefs = defaultProfile.listFiles( new FileFinder("preferences"));
            if ( prefs == null || prefs.length == 0){
                return ExtensionManager.ExtensitionStatus.MISSING;
            }
            JSONObject preferences = Utils.readFile( prefs[0] );
            if (preferences == null) {
                return ExtensionManager.ExtensitionStatus.MISSING;
            }
            JSONObject extensions = (JSONObject)preferences.get("extensions");
            if (extensions == null) {
                return ExtensionManager.ExtensitionStatus.MISSING;
            }
            JSONObject settings = (JSONObject)extensions.get("settings");
            if (extensions == null) {
                return ExtensionManager.ExtensitionStatus.MISSING;
            }
            for (Object item : settings.entrySet()) {
                Map.Entry e = (Map.Entry)item;
                JSONObject extension = (JSONObject)e.getValue();
                if (extension != null) {
                    String path = (String)extension.get("path");
                    if (path != null && (path.indexOf("/extbrowser/plugins/chrome") != -1 
                            || path.indexOf("\\extbrowser\\plugins\\chrome") != -1)) 
                    {
                        return ExtensionManager.ExtensitionStatus.INSTALLED;
                    }
                    JSONObject manifest = (JSONObject)extension.get("manifest");
                    if (manifest != null && PLUGIN_NAME.equals((String)manifest.get("name"))) {
                        String version = (String)manifest.get("version");
                        if (isUpdateRequired( version )){
                            return ExtensionManager.ExtensitionStatus.NEEDS_UPGRADE;
                        }
                        Number n = (Number)extension.get("state");
                        if (n != null && n.intValue() != 1) {
                            return ExtensionManager.ExtensitionStatus.DISABLED;
                        }
                        return ExtensionManager.ExtensitionStatus.INSTALLED;
                    }
                }
            }
            return ExtensionManager.ExtensitionStatus.MISSING;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor.BrowserExtensionManager#install(org.netbeans.modules.web.plugins.PluginLoader)
         */
        @Override
        public boolean install( PluginLoader loader, 
                ExtensionManager.ExtensitionStatus currentStatus ) 
        {
            File extensionFile = InstalledFileLocator.getDefault().locate(
                    EXTENSION_PATH,PLUGIN_MODULE_NAME, false);
            
            if ( extensionFile == null ){
                Logger.getLogger(ChromeExtensionManager.class.getCanonicalName()).
                    severe("Could not find chrome extension in installation directory");   // NOI18N
                return false;
            }
            
            String useManualInstallation = System.getProperty( NO_WEB_STORE_SWITCH );
            try {
                if ( useManualInstallation !=null ){
                    return manualInstallPluginDialog(loader, currentStatus, extensionFile);
                }
                else {
                    alertGoogleWebStore();
                    return false;
                }
            }
            catch( IOException e ){
                Logger.getLogger( ChromeExtensionManager.class.getCanonicalName()).
                    log(Level.INFO , null ,e );
                return false;
            }
            
           /* NotifyDescriptor installDesc = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(ChromeExtensionManager.class, 
                            currentStatus == ExtensionManager.ExtensitionStatus.MISSING ? 
                        "LBL_InstallMsg" : "LBL_UpgradeMsg"),                                  // NOI18N
                    NbBundle.getMessage(ChromeExtensionManager.class, 
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
                Logger.getLogger( ChromeExtensionManager.class.getCanonicalName()).
                    log(Level.INFO , null ,e );
                return false;
            }
            return true;*/
        }
        
        @Override
        protected String getCurrentPluginVersion(){
            File extensionFile = InstalledFileLocator.getDefault().locate(
                    EXTENSION_PATH,PLUGIN_MODULE_NAME, false);
            String content = Utils.readZip( extensionFile, "manifest.json");    // NOI18N
            int index = content.indexOf(VERSION);
            if ( index == -1){
                return null;
            }
            index = content.indexOf(',',index);
            return getValue(content, 0, index , VERSION);
        }
        
        private String getValue(String content, int start , int end , String key){
            String part = content.substring( start , end );
            int index = part.indexOf(key);
            if ( index == -1 ){
                return null;
            }
            String value = part.substring( index +key.length() ).trim();
            return Utils.unquote(value);
        }
        
        private File getDefaultProfile() {
            String[] userData = getUserData();
            if ( userData != null ){
                for (String dataDir : userData) {
                    File dir = new File(dataDir);
                    if (dir.isDirectory() && dir.exists()) {
                        File[] localState = dir.listFiles( 
                                new FileFinder("local state"));         // NOI18N
                        boolean guessDefault = localState == null || 
                                    localState.length == 0;
                        
                        if ( !guessDefault ) {
                            JSONObject localStateContent = Utils.readFile( localState[0]);
                            if (localStateContent != null) {
                                JSONObject profile = (JSONObject)localStateContent.get("profile");
                                if (profile != null) {
                                    String prof = (String)profile.get("last_used");
                                    if (prof != null) {
                                        prof = Utils.unquote(prof);
                                        File[] listFiles = dir.listFiles( new FileFinder( 
                                                prof , true));
                                        if ( listFiles != null && listFiles.length >0 ){
                                            return listFiles[0];
                                        } else {
                                            guessDefault = true;
                                        }
                                    }
                                }
                            }
                        }
                        
                        if( guessDefault ) {
                            File[] listFiles = dir.listFiles( 
                                    new FileFinder("default"));  // NOI18N
                            if ( listFiles!= null && listFiles.length >0 ) { 
                                    return listFiles[0];
                            }
                        }

                    }
                }
            }
            return null;
        }
        
        protected String[] getUserData(){
            // see http://www.chromium.org/user-experience/user-data-directory
            // TODO - this will not work for Chromium on Windows and Mac
            if (Utilities.isWindows()) {
                ArrayList<String> result = new ArrayList<String>();
                String localAppData = System.getenv("LOCALAPPDATA");                // NOI18N
                if (localAppData != null) {
                    result.add(localAppData+"\\Google\\Chrome\\User Data");
                }
                String appData = System.getenv("APPDATA");                // NOI18N
                if (appData != null) {
                    // we are in C:\Documents and Settings\<username>\Application Data\ on XP
                    File f = new File(appData);
                    if (f.exists()) {
                        String fName = f.getName();
                        f = new File(f.getParentFile(),"Local Settings");
                        f = new File(f, fName);
                        if (f.exists())
                            result.add(f.getPath()+"\\Google\\Chrome\\User Data");
                    }
                }
                return result.toArray(new String[result.size()]);
            } 
            else if (Utilities.isMac()) {
                return Utils.getUserPaths("/Library/Application Support/Google/Chrome");// NOI18N
            } 
            else {
                return Utils.getUserPaths("/.config/google-chrome", "/.config/chrome");// NOI18N
            }
        }
        
        private boolean manualInstallPluginDialog( PluginLoader loader,
                ExtensionManager.ExtensitionStatus currentStatus,
                File extensionFile ) throws IOException
        {
            JButton continueButton = new JButton(NbBundle.getMessage(
                    ChromeExtensionManager.class, "LBL_Continue"));                // NOI18N
            continueButton.getAccessibleContext().setAccessibleName(NbBundle.
                    getMessage(ChromeExtensionManager.class, "ACSN_Continue"));    // NOI18N
            continueButton.getAccessibleContext().setAccessibleDescription(NbBundle.
                    getMessage(ChromeExtensionManager.class, "ACSD_Continue"));    // NOI18N
            DialogDescriptor descriptor = new DialogDescriptor(
                    new ChromeInfoPanel(extensionFile.getCanonicalPath(), 
                            loader, currentStatus), 
                    NbBundle.getMessage(ChromeExtensionManager.class, 
                            "TTL_InstallExtension"), true, 
                    new Object[]{continueButton, 
                            DialogDescriptor.CANCEL_OPTION}, continueButton, 
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
            while (true) {
                Object result = DialogDisplayer.getDefault().notify(descriptor);
                if (result == continueButton) {
                    ExtensitionStatus status = isInstalled();
                    if ( status!= ExtensitionStatus.INSTALLED){
                        continue;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }
        
        private void alertGoogleWebStore(){
            JButton goToButton = new JButton(NbBundle.getMessage(
                    ChromeExtensionManager.class, "LBL_GoToWebStore"));            // NOI18N
            goToButton.getAccessibleContext().setAccessibleName(NbBundle.
                    getMessage(ChromeExtensionManager.class, "ACSN_GoToWebStore"));    // NOI18N
            goToButton.getAccessibleContext().setAccessibleDescription(NbBundle.
                    getMessage(ChromeExtensionManager.class, "ACSD_GoToWebStore"));    // NOI18N
            DialogDescriptor descriptor = new DialogDescriptor(
                    new WebStorePanel(), 
                    NbBundle.getMessage(ChromeExtensionManager.class, 
                            "TTL_InstallExtension"), true, 
                    new Object[]{goToButton, 
                            DialogDescriptor.CANCEL_OPTION}, goToButton, 
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
            Object result = DialogDisplayer.getDefault().notify(descriptor);
            if ( result == goToButton ){
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(
                            URI.create(PLUGIN_PAGE).toURL());
                }
                catch(MalformedURLException e){
                    assert false;
                }
            }
        }
        
        static private class FileFinder implements FileFilter {
            FileFinder(String name){
                this( name, false );
            }
            
            FileFinder(String name , boolean caseSensitive ){
                myName = name;
                isCaseSensitive = caseSensitive;
            }
            
            /* (non-Javadoc)
             * @see java.io.FileFilter#accept(java.io.File)
             */
            @Override
            public boolean accept( File file ) {
                if ( isCaseSensitive ){
                    return file.getName().equals( myName);
                }
                else {
                    return file.getName().toLowerCase(Locale.US).equals( myName);
                }
            }
            private String myName;
            private boolean isCaseSensitive;
        }
    }
}
