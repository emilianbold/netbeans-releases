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

import org.netbeans.modules.ruby.rubyproject.RequiredGems;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformProvider;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.platform.RubyPreferences;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.gems.GemFilesParser;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.platform.gems.Gems;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.RailsProjectUtil;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
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

    private final File projectDirectory;
    private final RailsProject project;
    private final PropertyEvaluator evaluator;
    private List<PathResourceImplementation> resourcesCache;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final RequiredGems requiredGems;
    private final boolean forTests;
    private final GemFilter gemFilter;
    private final RubyPlatformProvider platformProvider;

    private RubyPlatform platform;

    public BootClassPathImplementation(RailsProject project, File projectDirectory, PropertyEvaluator evaluator, boolean forTests) {
        this.project = project;
        this.projectDirectory = projectDirectory;
        assert evaluator != null;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        RubyPreferences.addPropertyChangeListener(WeakListeners.propertyChange(this, RubyPreferences.getInstance()));
        this.forTests = forTests;
        RequiredGems[] reqs = RequiredGems.lookup(project);
        this.requiredGems = forTests ? reqs[1] : reqs[0];
        this.gemFilter = new GemFilter(evaluator);
        this.platformProvider = new RubyPlatformProvider(evaluator);
    }

    private synchronized RubyPlatform getPlatform() {
        if (platform == null) {
            platform = platformProvider.getPlatform();
        }
        return platform;
    }

    @Override
    public synchronized List<PathResourceImplementation> getResources() {
        if (this.resourcesCache == null) {
                //TODO: May also listen on CP, but from Platform it should be fixed.
            List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();

            try {
                result.add(ClassPathSupport.createResource(RubyPlatform.getRubyStubs().getURL()));
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (getPlatform() == null) {
                LOGGER.severe("Cannot resolve platform for project: " + projectDirectory);
                return Collections.emptyList();
            }
            
            if (!getPlatform().hasRubyGemsInstalled()) {
                LOGGER.fine("Not RubyGems installed, returning empty result");
                return Collections.emptyList();
            }
            
            // the rest of code depend on RubyGems to be installed
            
            GemManager gemManager = getPlatform().getGemManager();
            assert gemManager != null : "not null when RubyGems are installed";
            
            boolean useVendorGemsOnly = useVendorGemsOnly();
            Map<String, URL> gemUrls = !useVendorGemsOnly
                    ? gemManager.getGemUrls()
                    : new HashMap<String, URL>();
            Map<String, String> gemVersions = !useVendorGemsOnly
                    ? gemManager.getGemVersions()
                    : new HashMap<String, String>();

            for (URL url : gemManager.getNonGemLoadPath()) {
                result.add(ClassPathSupport.createResource(url));
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

                filterAndAddGems(combinedGems, result);

            } else {
                filterAndAddGems(gemUrls.values(), result);
            }
            
            resourcesCache = Collections.unmodifiableList (result);
        }
        
        return this.resourcesCache;
    }

    private void filterAndAddGems(Collection<URL> gemsToAdd, List<PathResourceImplementation> result) {
        Collection<URL> filtered = requiredGems.filterNotRequiredGems(gemsToAdd);
        for (URL url : filtered) {
            String gem = Gems.getGemName(url);
            if (gemFilter.include(gem)) {
                result.add(ClassPathSupport.createResource(url));
                continue;
            }
            if (gemFilter.exclude(gem)) {
                continue;
            }
            result.add(ClassPathSupport.createResource(url));
        }
        requiredGems.setIndexedGems(filtered);
    }

    private boolean useVendorGemsOnly() {
        return new File(projectDirectory, "vendor" + File.separator + "gems").exists() //NOI18N
                && RubyPreferences.isIndexVendorGemsOnly();
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
        URL activerecord = gemUrls.get(Gems.ACTIVERECORD);
        if (activerecord == null) {
            // Activerecord not found at all - not good for a Rails projects, but at least no point adjusting versions
            return gemUrls;
        }
        String activerecordUrl = activerecord.toExternalForm();
        if (activerecordUrl.indexOf(Gems.ACTIVERECORD + "-" + railsVersion) != -1) { // NOI18N
            // Already have the right version - we're done
            return gemUrls;
        }

        Pattern VERSION_PATTERN = Pattern.compile(".*" + Gems.ACTIVERECORD + "-" + GemFilesParser.VERSION_REGEX + ".*"); // NOI18N
        Matcher m = VERSION_PATTERN.matcher(activerecordUrl);
        if (!m.matches()) {
            // Couldn't determine current version - don't attempt adjustments
            return gemUrls;
        }

        String defaultVersion = m.group(1);

        // Now attempt to fix the urls
        boolean first = true;
        for (String gemName : Gems.getRailsGems()) { // NOI18N
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

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt.getSource() == RubyInstallation.getInstance() && evt.getPropertyName().equals("roots"))
                || evt.getSource() == RubyPreferences.getInstance() && evt.getPropertyName().equals(RubyPreferences.VENDOR_GEMS_PROPERTY)) {
            resetCache();
        }
        if (evt.getPropertyName().equals(SharedRubyProjectProperties.PLATFORM_ACTIVE)) {
            platform = RubyPlatformProvider.getPlatform((String) evt.getNewValue());
            resetCache();
        }
        if (evt.getPropertyName().equals(RequiredGems.REQUIRED_GEMS_TESTS_PROPERTY) && forTests) {
            requiredGems.setRequiredGems((String) evt.getNewValue());
            resetCache();
        }
        if (evt.getPropertyName().equals(RequiredGems.REQUIRED_GEMS_PROPERTY) && !forTests) {
            requiredGems.setRequiredGems((String) evt.getNewValue());
            resetCache();
        }
    }

    /**
     * Resets the cache and firesPropertyChange
     */
    private void resetCache () {
        synchronized (this) {
            resourcesCache = null;
            RubyIndex.resetCache();
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }

    private List<URL> mergeVendorGems(File vendorFile, Map<String,String> gemVersions, Map<String,URL> gemUrls) {
        chooseGems(vendorFile.listFiles(), gemVersions, gemUrls);
        
        return new ArrayList<URL>(gemUrls.values());
    }
    
    private static void chooseGems(File[] gems, Map<String, String> gemVersions,
            Map<String, URL> gemUrls) {

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

            String[] info = GemFilesParser.parseNameAndVersion(n);
            if (info == null) {
                continue;
            }

            File lib = new File(f, "lib");
            if (lib.exists()) {
                try {
                    URL url = lib.toURI().toURL();
                    addGem(gemVersions, gemUrls, info[0], info[1], url);
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
