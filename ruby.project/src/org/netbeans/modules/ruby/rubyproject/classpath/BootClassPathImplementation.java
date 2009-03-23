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
package org.netbeans.modules.ruby.rubyproject.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformProvider;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

final class BootClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(BootClassPathImplementation.class.getName());
    
    private static final Pattern GEM_EXCLUDE_FILTER;
    private static final Pattern GEM_INCLUDE_FILTER;
    
    static {
        String userExcludes = System.getProperty("ruby.prj.excludegems");
        if (userExcludes == null) {
            // activerecord? Apparently often used outside of Rails
            String deflt = "^(rails|action[a-z]+|activesupport)-\\d+\\.\\d+\\.\\d+(-\\S+)?$"; // NOI18N
            GEM_EXCLUDE_FILTER = Pattern.compile(deflt);
        } else if ("none".equals(userExcludes)) {
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
        String userIncludes = System.getProperty("ruby.prj.includegems");
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

//    private static final String PLATFORM_ACTIVE = "platform.active";        //NOI18N
//    private static final String ANT_NAME = "platform.ant.name";             //NOI18N
//    private static final String J2SE = "j2se";                              //NOI18N

    private final PropertyEvaluator evaluator;
//    private JavaPlatformManager platformManager;
    //name of project active platform
    private String activePlatformName;
    //active platform is valid (not broken reference)
    private boolean isActivePlatformValid;
    private List<PathResourceImplementation> resourcesCache;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public BootClassPathImplementation(PropertyEvaluator evaluator) {
        if (evaluator != null) {
            assert evaluator != null;
            this.evaluator = evaluator;
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        } else {
            this.evaluator = null;
        }
    }
    
    public synchronized List<PathResourceImplementation> getResources() {
        if (this.resourcesCache == null) {
//            JavaPlatform jp = findActivePlatform ();
//            if (jp != null) {
                //TODO: May also listen on CP, but from Platform it should be fixed.
            List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();

            try {
                result.add(ClassPathSupport.createResource(RubyPlatform.getRubyStubs().getURL()));
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }

            RubyPlatform platform = new RubyPlatformProvider(evaluator).getPlatform();
            if (platform == null) {
                LOGGER.severe("Cannot resolve platform for project");
                return Collections.emptyList();
            }

            if (!platform.hasRubyGemsInstalled()) {
                LOGGER.fine("Not RubyGems installed, returning empty result");
                return Collections.emptyList();
            }
            
            // the rest of code depend on RubyGems to be installed

            GemManager gemManager = getGemManager();
            assert gemManager != null : "not null when RubyGems are installed";
            
            for (URL url : gemManager.getNonGemLoadPath()) {
                result.add(ClassPathSupport.createResource(url));
            }

            Map<String,URL> gemUrls = gemManager.getGemUrls();
            
            Pattern includeFilter = GEM_INCLUDE_FILTER;
            Pattern excludeFilter = GEM_EXCLUDE_FILTER;

            String include = evaluator.getProperty("ruby.includegems");
            String exclude = evaluator.getProperty("ruby.excludegems");
            try {
                if (include != null && include.length() > 0) {
                    includeFilter = Pattern.compile(include);
                }
                if (exclude != null && exclude.length() > 0) {
                    excludeFilter = Pattern.compile(exclude);
                }
            } catch (PatternSyntaxException pse) {
                Exceptions.printStackTrace(pse);
            }
            
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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

//    private JavaPlatform findActivePlatform () {
//        if (this.platformManager == null) {
//            this.platformManager = JavaPlatformManager.getDefault();
//            this.platformManager.addPropertyChangeListener(WeakListeners.propertyChange(this, this.platformManager));
//        }                
//        this.activePlatformName = evaluator.getProperty(PLATFORM_ACTIVE);
//        final JavaPlatform activePlatform = RubyProjectUtil.getActivePlatform (this.activePlatformName);
//        this.isActivePlatformValid = activePlatform != null;
//        return activePlatform;
//    }
//    
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

    private GemManager getGemManager() {
        // TODO: cache it(?)
        return new RubyPlatformProvider(evaluator).getPlatform().getGemManager();
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
    
}
