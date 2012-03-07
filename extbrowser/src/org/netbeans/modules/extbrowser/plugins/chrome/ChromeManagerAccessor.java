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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.extbrowser.plugins.ExtensionManagerAccessor;
import org.netbeans.modules.extbrowser.plugins.PluginLoader;
import org.netbeans.modules.extbrowser.plugins.Utils;


import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;



/**
 * @author ads
 *
 */
public class ChromeManagerAccessor implements ExtensionManagerAccessor {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor#getManager()
     */
    @Override
    public BrowserExtensionManager getManager() {
        return new ChromeExtensionManager();
    }

    
    private static class ChromeExtensionManager extends AbstractBrowserExtensionManager {
        
        private static final String LAST_USED = "\"last_used\":";               // NOI18N
        
        private static final String VERSION = "\"version\":";                   // NOI18N
        
        private static final String STATE = "\"state\":";                       // NOI18N
        
        private static final String PLUGIN_NAME = "NetBeans IDE Support Plugin";// NOI18N
        
        private static final String EXTENSION_PATH = "modules/ext/netbeans-ros-chrome-plugin.crx"; // NOI18N

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor.BrowserExtensionManager#isInstalled()
         */
        @Override
        public boolean isInstalled() {
            File defaultProfile = getDefaultProfile();
            if ( defaultProfile == null ){
                return false;
            }
            File[] prefs = defaultProfile.listFiles( new FileFinder("preferences"));
            if ( prefs == null || prefs.length == 0){
                return false;
            }
            String preferences = Utils.readFile( prefs[0] );
            int index = preferences.indexOf(PLUGIN_NAME);
            if ( index == -1 ){
                return false;
            }
            String firstPart = preferences.substring( 0, index );
            int start = firstPart.lastIndexOf('}');
            if ( start == -1){
                return false;
            }
            int end = preferences.indexOf( '}', start+1);
            if ( end == -1 ){
                return false;
            }
            String version = getValue(preferences, start, end, VERSION);
            if ( isUpdateRequired( version )){
                return false;
            }
            start = end;
            end = preferences.indexOf('}' , start +1);
            if ( end == -1 ){
                return false;
            }
            String state = getValue(preferences, start, end, STATE);
            try {
                boolean isEnabled =  Byte.valueOf((byte)1).equals(Byte.parseByte(state ));
                
                if ( !isEnabled ){
                    NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                            NbBundle.getMessage(ChromeExtensionManager.class, 
                                    "LBL_ChromePluginIsDisabled"),                   // NOI18N
                                        NotifyDescriptor.ERROR_MESSAGE);
                    
                    descriptor.setTitle(NbBundle.getMessage(ChromeExtensionManager.class, 
                            "TTL_ChromePluginIsDisabled"));                             // NOI18N
                    DialogDisplayer.getDefault().notify(descriptor);
                }
                
                return true;
            }
            catch ( NumberFormatException e ){
                return false;
            }
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor.BrowserExtensionManager#install(org.netbeans.modules.web.plugins.PluginLoader)
         */
        @Override
        public boolean install( PluginLoader loader) {
            File extensionFile = InstalledFileLocator.getDefault().locate(
                    EXTENSION_PATH,PLUGIN_MODULE_NAME, false);
            
            if ( extensionFile == null ){
                Logger.getLogger(ChromeExtensionManager.class.getCanonicalName()).
                    severe("Could not find chrome extension in installation directory");   // NOI18N
                return false;
            }
            
            NotifyDescriptor installDesc = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(ChromeExtensionManager.class, 
                            "LBL_InstallMsg"),                                  // NOI18N
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
            return true;
        }
        
        /*
         *  TODO : this method should automatically retrieve current plugin 
         *  version to avoid manual source update
         */
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
                            String localStateContent = Utils.readFile( localState[0]);
                            int index = localStateContent.indexOf("\"profile\":");       // NOI18N
                            if ( index == -1){
                                guessDefault = true;
                            }
                            else {
                                index = localStateContent.indexOf( LAST_USED , index); 
                            }
                            if ( index == -1){
                                guessDefault = true;
                            }
                            else {
                                int end = localStateContent.indexOf( '}', 
                                        index +LAST_USED.length());
                                if ( end == -1){
                                    guessDefault = true;
                                }
                                else {
                                    String profile = localStateContent.substring( 
                                            index +LAST_USED.length(), end ).trim();
                                    profile = Utils.unquote( profile );
                                    File[] listFiles = dir.listFiles( new FileFinder( 
                                            profile , true));
                                    if ( listFiles != null && listFiles.length >0 ){
                                        return listFiles[0];
                                    }
                                    else {
                                        guessDefault = true;
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
        
        private String[] getUserData(){
            if (Utilities.isWindows()) {
                String localAppData = System.getenv("LOCALAPPDATA");                // NOI18N
                return new String[]{ localAppData+"\\Google\\Chrome\\User Data"};   // NOI18N
            } 
            else if (Utilities.isMac()) {
                return Utils.getUserPaths("/Library/Application Support/Google/Chrome");// NOI18N
            } 
            else {
                return Utils.getUserPaths("/.config/google-chrome", "/.config/chrome");// NOI18N
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
