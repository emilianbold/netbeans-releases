/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.ruby.railsprojects.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformProvider;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.railsprojects.RailsProjectUtil;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

final class BootClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {
    
    private static final Logger LOGGER = Logger.getLogger(BootClassPathImplementation.class.getName());
    
    // Flag for controlling last-minute workaround for issue #120231
    private static final boolean INCLUDE_NONLIBPLUGINS = Boolean.getBoolean("ruby.include_nonlib_plugins");
    
    private static final Pattern GEM_EXCLUDE_FILTER;
    private static final Pattern GEM_INCLUDE_FILTER;
    static {
        String userExcludes = System.getProperty("rails.prj.excludegems");
        if (userExcludes == null || "none".equals(userExcludes)) {
            GEM_EXCLUDE_FILTER = null;
        } else {
            Pattern p;
            try {
                p = Pattern.compile(userExcludes);
            } catch (PatternSyntaxException pse) {
                Logger.getAnonymousLogger().log(Level.WARNING,"Invalid regular expression: " + userExcludes);
                Logger.getAnonymousLogger().log(Level.WARNING, pse.toString());
                p = null;
            }
            GEM_EXCLUDE_FILTER = p;
        }
        String userIncludes = System.getProperty("rails.prj.includegems");
        if (userIncludes == null || "all".equals(userIncludes)) {
            GEM_INCLUDE_FILTER = null;
        } else {
            Pattern p;
            try {
                p = Pattern.compile(userIncludes);
            } catch (PatternSyntaxException pse) {
                Logger.getAnonymousLogger().log(Level.WARNING,"Invalid regular expression: " + userIncludes);
                Logger.getAnonymousLogger().log(Level.WARNING, pse.toString());
                p = null;
            }
            GEM_INCLUDE_FILTER = p;
        }
    }

    private File projectDirectory;
    private final PropertyEvaluator evaluator;
    //name of project active platform
    private String activePlatformName;
    //active platform is valid (not broken reference)
    private boolean isActivePlatformValid;
    private List<PathResourceImplementation> resourcesCache;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public BootClassPathImplementation(File projectDirectory, PropertyEvaluator evaluator) {
        this.projectDirectory = projectDirectory;
        assert evaluator != null;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    public synchronized List<PathResourceImplementation> getResources() {
        if (this.resourcesCache == null) {
                //TODO: May also listen on CP, but from Platform it should be fixed.
            List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();

            try {
                result.add(ClassPathSupport.createResource(RubyPlatform.getRubyStubs().getURL()));
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }

            RubyPlatform platform = new RubyPlatformProvider(evaluator).getPlatform();
            if (platform == null) {
                LOGGER.severe("Cannot resolve platform for project: " + projectDirectory);
                return Collections.emptyList();
            }
            
            if (!platform.hasRubyGemsInstalled()) {
                LOGGER.fine("Not RubyGems installed, returning empty result");
                return Collections.emptyList();
            }
            
            // the rest of code depend on RubyGems to be installed
            
            GemManager gemManager = platform.getGemManager();
            assert gemManager != null : "not null when RubyGems are installed";
            Map<String, URL> gemUrls = gemManager.getGemUrls();
            Map<String, String> gemVersions = gemManager.getGemVersions();

            for (URL url : gemManager.getNonGemLoadPath()) {
                result.add(ClassPathSupport.createResource(url));
            }
            
            // Perhaps I can filter vendor/rails iff the installation contains it

            Pattern includeFilter = GEM_INCLUDE_FILTER;
            Pattern excludeFilter = GEM_EXCLUDE_FILTER;

            String include = evaluator.getProperty("ruby.includegems");
            String exclude = evaluator.getProperty("ruby.excludegems");
            try {
                if (include != null) {
                    includeFilter = Pattern.compile(include);
                }
                if (exclude != null) {
                    excludeFilter = Pattern.compile(exclude);
                }
            } catch (PatternSyntaxException pse) {
                Exceptions.printStackTrace(pse);
            }

            gemUrls = adjustGemsForExplicitVersion(gemUrls);

            // Add in all the vendor/ paths, if any
            File vendor = new File(projectDirectory, "vendor");
            if (vendor.exists()) {
                List<URL> vendorPlugins = getVendorPlugins(vendor);
                for (URL url : vendorPlugins) {
                    result.add(ClassPathSupport.createResource(url));
                }

                
                // TODO - handle multiple gem versions in the same repository
                List<URL> combinedGems = mergeVendorGems(vendor,
                        new HashMap<String, String>(gemVersions),
                        new HashMap<String, URL>(gemUrls));
                for (URL url : combinedGems) {
                    if (includeFilter != null) {
                        String gem = getGemName(url);
                        if (includeFilter.matcher(gem).find()) {
                            result.add(ClassPathSupport.createResource(url));
                            continue;
                        }
                    }

                    if (excludeFilter != null) {
                        String gem = getGemName(url);
                        if (excludeFilter.matcher(gem).find()) {
                            continue;
                        }
                    }

                    result.add(ClassPathSupport.createResource(url));
                }

            } else {
                for (URL url : gemUrls.values()) {
                    if (includeFilter != null) {
                        String gem = getGemName(url);
                        if (includeFilter.matcher(gem).find()) {
                            result.add(ClassPathSupport.createResource(url));
                            continue;
                        }
                    }

                    if (excludeFilter != null) {
                        String gem = getGemName(url);
                        if (excludeFilter.matcher(gem).find()) {
                            continue;
                        }
                    }

                    result.add(ClassPathSupport.createResource(url));
                }
            }
            
            resourcesCache = Collections.unmodifiableList (result);
        // XXX
//            RubyInstallation.getInstance().removePropertyChangeListener(this);
//            RubyInstallation.getInstance().addPropertyChangeListener(this);
        }
        return this.resourcesCache;
    }

    private static String getGemName(URL gemUrl) {
        String urlString = gemUrl.getFile();
        if (urlString.endsWith("/lib/")) {
            urlString = urlString.substring(urlString.lastIndexOf('/', urlString.length()-6)+1,
                    urlString.length()-5);
        }
        
        return urlString;
    }

    /** Adjust the gem urls according to the RAILS_GEM_VERSION specified in config/environment.rb */
    private Map<String,URL> adjustGemsForExplicitVersion(Map<String, URL> gemUrls) {
        // Look for version specifications like
        //    RAILS_GEM_VERSION = '2.1.0' unless defined? RAILS_GEM_VERSION
        // in environment.rb
        File environment = new File(projectDirectory, "config" + File.separator + "environment.rb"); // NOI18N
        if (!environment.isFile()) {
            return gemUrls;
        }
        FileObject environmentFO = FileUtil.toFileObject(FileUtil.normalizeFile(environment));
        if (environmentFO == null) {
            return gemUrls;
        }

        String railsVersion = RailsProjectUtil.getSpecifiedRailsVersion(environmentFO);
        if (railsVersion == null) {
            // No version specified - no need to adjust anything
            return gemUrls;
        }

        // See if we've picked the right version
        String ACTIVERECORD = "activerecord"; // NOI18N
        URL activerecord = gemUrls.get(ACTIVERECORD);
        if (activerecord == null) {
            // Activerecord not found at all - not good for a Rails projects, but at least no point adjusting versions
            return gemUrls;
        }
        String activerecordUrl = activerecord.toExternalForm();
        if (activerecordUrl.indexOf(ACTIVERECORD+"-" + railsVersion) != -1) { // NOI18N
            // Already have the right version - we're done
            return gemUrls;
        }

        Pattern VERSION_PATTERN = Pattern.compile(".*activerecord-(\\d+\\.\\d+\\.\\d+).*"); // NOI18N
        Matcher m = VERSION_PATTERN.matcher(activerecordUrl);
        if (!m.matches()) {
            // Couldn't determine current version - don't attempt adjustments
            return gemUrls;
        }

        String defaultVersion = m.group(1);

        // Now attempt to fix the urls
        gemUrls.get("actionwebservice");
        String[] railsGems =  new String[] { "actionmailer", "actionpack", "activerecord",  // NOI18N
                                         "activeresource", "activesupport", "rails", // NOI18N
                                         "actionwebservice" }; // NOI18N    actionwebservice is Rails 1.x only
        boolean first = true;
        for (String gemName : railsGems) { // NOI18N
            URL url = gemUrls.get(gemName);
            if (url != null) {
                String urlString = url.toExternalForm();
                String replace = gemName + "-" + defaultVersion;
                int index = urlString.indexOf(replace);
                if (index != -1) {
                    try {
                        URL newUrl = new URL(urlString.replace(replace, gemName + "-" + railsVersion)); // NOI18N
                        if (first) {
                            first = false;
                            FileObject fo = URLMapper.findFileObject(newUrl);
                            if (fo == null) {
                                // Can't find this URL - the project is probably specifying a Rails
                                // project we don't have installed
                                return gemUrls;
                            }
                            // Replace map the first time - the one we were passed was read-only
                            gemUrls = new HashMap<String,URL>(gemUrls);
                        }
                        gemUrls.put(gemName, newUrl);
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        return gemUrls;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == RubyInstallation.getInstance() && evt.getPropertyName().equals("roots")) {
            resetCache();
        }
//        if (evt.getSource() == this.evaluator && evt.getPropertyName().equals(PLATFORM_ACTIVE)) {
//            //Active platform was changed
//            resetCache ();
//        }
//        else if (evt.getSource() == this.platformManager && JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(evt.getPropertyName()) && activePlatformName != null) {
//            //Platform definitions were changed, check if the platform was not resolved or deleted
//            if (this.isActivePlatformValid) {
//                if (RubyProjectUtil.getActivePlatform (this.activePlatformName) == null) {
//                    //the platform was not removed
//                    this.resetCache();
//                }
//            }
//            else {
//                if (RubyProjectUtil.getActivePlatform (this.activePlatformName) != null) {
//                    this.resetCache();
//                }
//            }
//        }
    }

    /**
     * Resets the cache and firesPropertyChange
     */
    private void resetCache () {
        synchronized (this) {
            resourcesCache = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }

    private List<URL> mergeVendorGems(File vendorFile, Map<String,String> gemVersions, Map<String,URL> gemUrls) {
        chooseGems(vendorFile.listFiles(), gemVersions, gemUrls);
        
        return new ArrayList<URL>(gemUrls.values());
    }
    
    private static void chooseGems(File[] gems, Map<String, String> gemVersions,
            Map<String, URL> gemUrls) {
        // Try to match foo-1.2.3, foo-bar-1.2.3, foo-bar-1.2.3-ruby
        Pattern GEM_FILE_PATTERN = Pattern.compile("(\\S|-)+-((\\d+)\\.(\\d+)\\.(\\d+))(-\\S+)?"); // NOI18N

        for (File f : gems) {
            if (!f.isDirectory()) {
                continue;
            }

            String n = f.getName();
            
            if ("plugins".equals(n)) {
                // Special cased separately
                continue;
            }

            if ("rails".equals(n)) { // NOI18N
                // Special case - what do we do here?
                chooseRails(f.listFiles(), gemVersions, gemUrls);
                continue;
            }

            if ("gems".equals(n) || "gems-jruby".equals(n)) { // NOI18N
                // Support both having gems in the vendor/ top directory as well as in a gems/ subdirectory            }
                chooseGems(f.listFiles(), gemVersions, gemUrls);
            }

            if (n.indexOf('-') == -1) {
                continue;
            }

            Matcher m = GEM_FILE_PATTERN.matcher(n);
            if (!m.matches()) {
                continue;
            }
            
            File lib = new File(f, "lib");
            if (lib.exists()) {
                try {
                    URL url = lib.toURI().toURL();
                    String name = m.group(1);
                    String version = m.group(2);
                    addGem(gemVersions, gemUrls, name, version, url);
                } catch (MalformedURLException mufe) {
                    Exceptions.printStackTrace(mufe);
                }
            }
        }
    }
    
    private static void addGem(Map<String, String> gemVersions, Map<String, URL> gemUrls,
            String name, String version, URL url) {
        if (!gemVersions.containsKey(name) ||
                Util.compareVersions(version, gemVersions.get(name)) > 0) {
            gemVersions.put(name, version);
            gemUrls.put(name, url);
        }
    }

    private static void chooseRails(File[] gems, Map<String, String> gemVersions,
            Map<String, URL> gemUrls) {
        for (File f : gems) {
            if (!f.isDirectory()) {
                continue;
            }
            
            String name = f.getName();
            // actionpack/lib/action_pack/version.r
            String middleName = name;
            if (name.indexOf('_') == -1) {
                if (name.startsWith("action") || name.startsWith("active")) {
                    middleName = name.substring(0, 6) + "_" +name.substring(6);
                }
            }
            File lib = new File(f, "lib");
            if (lib.exists()) {
                File versionFile = new File(lib, middleName + File.separator + "version.rb");
                if (versionFile.exists()) {
                    String version = RailsProjectUtil.getVersionString(versionFile);
                    if (version != null) {
                        try {
                            URL url = lib.toURI().toURL();
                            addGem(gemVersions, gemUrls, name, version, url);
                        } catch (MalformedURLException mufe) {
                            Exceptions.printStackTrace(mufe);
                        }
                    }
                }
            }
        }
    }

    private List<URL> getVendorPlugins(File vendor) {
        assert vendor != null;
        
        File plugins = new File(vendor, "plugins");
        if (!plugins.exists()) {
            return Collections.emptyList();
        }
        
        List<URL> urls = new ArrayList<URL>();

        for (File f : plugins.listFiles()) {
            File lib = new File(f, "lib");
            if (INCLUDE_NONLIBPLUGINS) {
                lib = f;
            }
            if (!lib.exists()) {
                continue;
            }
            // TODO - preindex via version lookup somehow?
            try {
                URL url = lib.toURI().toURL();
                urls.add(url);
                // TODO - find versions for the plugins?
                //Map<String, File> nameMap = gemFiles.get(name);
                //if (nameMap != null) {
                //    String version = nameMap.keySet().iterator().next();
                //    RubyInstallation.getInstance().setGemRoot(url, name+ "-" + version);
                //}
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return urls;
    }
}
