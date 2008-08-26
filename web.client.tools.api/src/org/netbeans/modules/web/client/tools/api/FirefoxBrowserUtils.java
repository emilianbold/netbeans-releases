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

package org.netbeans.modules.web.client.tools.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * Utilities for detecting Firefox versions and profile locations
 * 
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public class FirefoxBrowserUtils {
    
    public static final String PROFILE_PREF = "firefox-defaultProfile"; // NOI18N
    
    private static final String APPDATA_CMD = "cmd /c echo %AppData%"; // NOI18N

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
    
    public static File getProfileFromPreferences() {
        Preferences prefs = NbPreferences.forModule(FirefoxBrowserUtils.class);
        String location = prefs.get(PROFILE_PREF, "");
        if (location.length() > 0) {
            File f = new File(location);
            return f.isDirectory() ? f : null;
        }
        
        return null;
    }
    
    public static File getDefaultProfile() {
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
     * Checks $firefox_install_dir/defaults/pref/firefox.js for version string from
     * <code>prefs("general.useragent.extra.firefox", "Firefox/*****");</code>, or 
     * $profile_dir/compatibility.ini if the firefox installation directory cannot be
     * found.
     * 
     * @param browser
     * @param defaultProfile
     * @param actualVersion
     * @return true if Firefox 2 is found
     */
    public static boolean isFirefox2(HtmlBrowser.Factory browser, File defaultProfile, StringBuffer actualVersion) {
        String browserExecutable = getBrowserExecutable(browser);
        if (browserExecutable == null) {
            return isCompatibleFirefox(defaultProfile);
        }
        
        File firefox_js = new File(new File(browserExecutable).getParentFile(), "defaults/pref/firefox.js"); // NOI18N
        if (!firefox_js.exists()) {
            return isCompatibleFirefox(defaultProfile);
        }
        
        Pattern lineMatch = Pattern.compile("\\s*pref\\s*\\(\\s*\"general\\.useragent\\.extra\\.firefox\""); // NOI18N
        Pattern versionMatch = Pattern.compile("\"Firefox/[^\"]+\""); // NOI18N
        int majorVersion = -1;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(firefox_js));
            while (br.ready()) {
                String nextLine = br.readLine();
                if (lineMatch.matcher(nextLine).find()) {
                    Matcher matcher = versionMatch.matcher(nextLine);
                    if (matcher.find()) {
                        String version = matcher.group();
                        majorVersion = Integer.valueOf(version.substring(9, 10)).intValue();
                        if (actualVersion != null) {
                            actualVersion.append(version);
                        }
                        break;
                    }
                }
            }
            
            return majorVersion == 2;
        } catch (IOException ex) {
            Log.getLogger().log(Level.INFO, "Error reading Firefox version.", ex);
            return isCompatibleFirefox(defaultProfile);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Log.getLogger().log(Level.SEVERE, "Could not read Firefox version file", ex);
                }
            }
        }
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
            String appData = userHome + File.separator + NbBundle.getMessage(FirefoxBrowserUtils.class, "WIN32_APPDATA_FOLDER");

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
        return null;

    }

    /**
     * Check if the Firefox version is compatible by reading the compatibility.ini file in the
     * Firefox profile and then reading the value of key <code>LastVersion</code>.  This check is
     * only used if the Firefox installation directory is not available (installed on /usr/dist/exe,
     * for example)
     *
     * @param defaultProfile
     * @return true if the Firefox version is compatible
     */
    private static boolean isCompatibleFirefox(File defaultProfile) {
        if (defaultProfile == null) {
            defaultProfile = getDefaultProfile();
            if (defaultProfile == null) {
                Log.getLogger().warning("Could not find Firefox profile data");
                return false;
            }
        }
        File compatibilityDotIni = new File(defaultProfile, "compatibility.ini"); // NOI18N

        if (compatibilityDotIni.exists() && compatibilityDotIni.isFile() && compatibilityDotIni.canRead()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(compatibilityDotIni));
                String aLine;
                while ((aLine = bufferedReader.readLine()) != null) {
                    if (aLine.startsWith("LastVersion=")) { // NOI18N

                        aLine = aLine.substring(12);
                        return aLine.startsWith("2.0.0");
                    }
                }
            } catch (FileNotFoundException ex) {
                Log.getLogger().log(Level.INFO, "File not found: " + compatibilityDotIni.getAbsolutePath());
            } catch (IOException ex) {
                Log.getLogger().log(Level.INFO, "Error reading " + compatibilityDotIni.getAbsolutePath());
            }
        }
        return false;
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
