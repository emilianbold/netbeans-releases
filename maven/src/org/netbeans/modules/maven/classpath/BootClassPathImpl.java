/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.maven.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.Restriction;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Milos Kleint
 */
public final class BootClassPathImpl implements ClassPathImplementation, PropertyChangeListener {

    private List<? extends PathResourceImplementation> resourcesCache;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final @NonNull NbMavenProjectImpl project;
    private PlatformSources lastValue = new PlatformSources(null, null);
    private boolean activePlatformValid = true;
    private JavaPlatformManager platformManager;
    private final EndorsedClassPathImpl ecpImpl;
        //lock for this class and EndorsedCPI
    final Object LOCK = new Object();

    

    @SuppressWarnings("LeakingThisInConstructor")
    BootClassPathImpl(@NonNull NbMavenProjectImpl project, EndorsedClassPathImpl ecpImpl) {
        this.project = project;
        this.ecpImpl = ecpImpl;
        ecpImpl.setBCP(this);
        ecpImpl.addPropertyChangeListener(this);
    }

    public @Override List<? extends PathResourceImplementation> getResources() {
        synchronized (LOCK) {
            if (this.resourcesCache == null) {
                ArrayList<PathResourceImplementation> result = new ArrayList<PathResourceImplementation> ();
                boolean[] includeJDK = { true };
                result.addAll(ecpImpl.getResources(includeJDK));
                lastValue = createJavaPlatformOrigin(project);
                if (includeJDK[0]) {
                    for (ClassPath.Entry entry : findActivePlatform().getBootstrapLibraries().entries()) {
                        result.add(ClassPathSupport.createResource(entry.getURL()));
                    }
                }
                resourcesCache = Collections.unmodifiableList (result);
            }
            return this.resourcesCache;
        }
    }

    public @Override void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public @Override void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    @NonNull JavaPlatform findActivePlatform () {
        synchronized (LOCK) {
            activePlatformValid = true;
            if (platformManager == null) {
                platformManager = JavaPlatformManager.getDefault();
                platformManager.addPropertyChangeListener(WeakListeners.propertyChange(this, platformManager));
                NbMavenProject watch = project.getProjectWatcher();
                watch.addPropertyChangeListener(this);
            }

            //TODO ideally we would handle this by toolchains in future.

            //only use the default auximpl otherwise we get recursive calls problems.
            PlatformSources val = createJavaPlatformOrigin(project);

            JavaPlatform plat = getActivePlatform(val);
            if (plat == null) {
                //TODO report how?
                Logger.getLogger(BootClassPathImpl.class.getName()).log(Level.FINE, "Cannot find java platform with id of ''{0}''", val); //NOI18N
                plat = platformManager.getDefaultPlatform();
                activePlatformValid = false;
            }
            //Invalid platform ID or default platform
            return plat;
        }
    }
    
    /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     */
    public static JavaPlatform getActivePlatform (@NonNull final PlatformSources activePlatform) {
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
      
            JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification ("j2se",null));   //NOI18N
            if (activePlatform.hintProperty != null) {
                for (JavaPlatform platform : installedPlatforms) {
                    String antName = platform.getProperties().get("platform.ant.name");        //NOI18N
                    if (antName != null && antName.equals(activePlatform.hintProperty)) {
                        return platform;
                    }
                }
            }
            if (activePlatform.enforcerRange != null) {
                try {
                    Map<ArtifactVersion, JavaPlatform> found = new TreeMap<ArtifactVersion, JavaPlatform>();
                    VersionRange range = VersionRange.createFromVersionSpec(activePlatform.enforcerRange);
                    for (JavaPlatform platform : installedPlatforms) {
                        String enfVersion = enforcerNormalizedJDKVersion(platform);
                        ArtifactVersion ver = new DefaultArtifactVersion(enfVersion);
                        if (containsVersion(range, ver)) {
                            found.put(ver, platform);
                        }
                    }
                    if (!found.isEmpty()) {
                        return found.values().iterator().next();
                    }
                } catch (InvalidVersionSpecificationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (activePlatform.hintProperty == null && activePlatform.enforcerRange == null) {
                return pm.getDefaultPlatform();
            }
            return null;
  
    }
    
    private static final ThreadLocal<Boolean> semaphore = new ThreadLocal<Boolean>() {

        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
        
    };
    
    public @NonNull static PlatformSources createJavaPlatformOrigin(NbMavenProjectImpl project) {
        String val = project.getAuxProps().get(Constants.HINT_JDK_PLATFORM, true);
        Boolean recursiveCall = semaphore.get();
        if (recursiveCall) {
            //this ugly piece is necessary to avoid stackoverflow due to PluginPropertyUtils.createEvaluator caling back to BootClassPathImpl to get active platform's properties.
            return new PlatformSources(val, null);
        }
        try {
            semaphore.set(Boolean.TRUE);
            //String source = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, Constants.SOURCE_PARAM, null, "maven.compiler.source");
            String enforcerRange = PluginPropertyUtils.getPluginPropertyBuildable(project, Constants.GROUP_APACHE_PLUGINS, "maven-enforcer-plugin", "enforce", new PluginPropertyUtils.ConfigurationBuilder<String>() {
                @Override
                public String build(Xpp3Dom configRoot, ExpressionEvaluator eval) {
                    if (configRoot != null) {
                        Xpp3Dom rules = configRoot.getChild("rules");
                        if (rules != null) {
                            Xpp3Dom java = rules.getChild("requireJavaVersion");
                            if (java != null) {
                                Xpp3Dom version = java.getChild("version");
                                if (version != null) {
                                    String value = version.getValue();
                                    if (value == null) {
                                        return null;
                                    }
                                    String valueT = value.trim();
                                    try {
                                        Object evaluated = eval.evaluate(valueT);
                                        return evaluated != null ? evaluated.toString() : valueT;
                                    } catch (ExpressionEvaluationException ex) {
                                        //TODO log?
                                        return valueT;
                                    }
                                }
                            }
                        }
                    }
                    return null;
                }
            });
            return new PlatformSources(val, enforcerRange);
        } finally {
            semaphore.set(Boolean.FALSE);
        }

    }
    
    public static final class PlatformSources {
    
        public final String hintProperty;
        public final String enforcerRange;

        public PlatformSources(@NullAllowed String hintProperty, @NullAllowed String enforcerRange) {
            this.hintProperty = hintProperty;
            this.enforcerRange = enforcerRange;
        }
        
        public boolean valuesDefined() {
            return hintProperty != null || enforcerRange != null;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + (this.hintProperty != null ? this.hintProperty.hashCode() : 0);
            hash = 89 * hash + (this.enforcerRange != null ? this.enforcerRange.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PlatformSources other = (PlatformSources) obj;
            if ((this.hintProperty == null) ? (other.hintProperty != null) : !this.hintProperty.equals(other.hintProperty)) {
                return false;
            }
            if ((this.enforcerRange == null) ? (other.enforcerRange != null) : !this.enforcerRange.equals(other.enforcerRange)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "jdk{" + "hintProperty=" + hintProperty + ", enforcerRange=" + enforcerRange + '}';
        }
        
        
    }
    
    /**
     * Converts a jdk string from platform's java.version sys property, like 1.5.0-11b12 to a maven/enforcer format
     *
     * @param jdkVersion to be converted.
     * @return the converted string.
     */
    public static String enforcerNormalizedJDKVersion( JavaPlatform platform ) {
        String jdkVersion = null;
        String ver = platform.getSystemProperties().get("java.version");
        for (int i = 0; i < ver.length(); i++) {
            char ch = ver.charAt(i);
            if (ch >= '0' && ch <= '9') {
                jdkVersion = ver.substring(i);
                break;
            }
        }
        assert jdkVersion != null;
        jdkVersion = jdkVersion.replaceAll("_|-", ".").trim();
        List<String> tokens = Arrays.asList( jdkVersion.split("\\.") );
        StringBuilder buffer = new StringBuilder();

        Iterator<String> iter = tokens.iterator();
        for ( int i = 0; i < tokens.size() && i < 4; i++ ) {
            //4 is a magic constant in enforcer, only 4 numbers are consider interesting.
            String section = iter.next();
            section = section.replaceAll( "[^0-9]", "" );

            if ( !section.trim().isEmpty() )
            {
                buffer.append( Integer.parseInt( section.trim() ) );

                if ( i != 2 )
                {
                    buffer.append( '.' );
                }
                else
                {
                    buffer.append( '-' );
                }
            }
        }
        String version =  buffer.toString();
        if (version.endsWith(".")) {
            version = version.substring(0, version.length() - 1);
        }
        if (version.endsWith("-")) {
            version = version.substring(0, version.length() - 1);
        }
        return version;
    }
    
    /**
     * this is how enforcer compares ranges to versions. differs slightly from the default VersionRange approach.
     *
     * @param allowedRange range of allowed versions.
     * @param theVersion the version to be checked.
     * @return true if the version is contained by the range.
     */
    public static boolean containsVersion( VersionRange allowedRange, ArtifactVersion theVersion )
    {
        boolean matched = false;
        ArtifactVersion recommendedVersion = allowedRange.getRecommendedVersion();
        if (recommendedVersion == null) {
            @SuppressWarnings("unchecked")
            List<Restriction> restrictions = allowedRange.getRestrictions();
            for (Restriction restriction : restrictions) {
                if (restriction.containsVersion(theVersion)) {
                    matched = true;
                    break;
                }
            }
        } else {
            // only singular versions ever have a recommendedVersion
            @SuppressWarnings("unchecked")
            int compareTo = recommendedVersion.compareTo(theVersion);
            matched = (compareTo <= 0);
        }
        return matched;
    }

    public @Override void propertyChange(PropertyChangeEvent evt) {
        PlatformSources newVal = createJavaPlatformOrigin(project);
        if (evt.getSource() == project && evt.getPropertyName().equals(NbMavenProjectImpl.PROP_PROJECT)) {
            if (ecpImpl.resetCache()) {
                resetCache();
            } else {
                //Active platform was changed
                if ( !newVal.equals(lastValue)) {
                    resetCache ();
                }
            }
        }
        else if (evt.getSource() == platformManager && 
                JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(evt.getPropertyName()) 
                && lastValue.valuesDefined()) {
            lastValue = newVal;
            //Platform definitions were changed, check if the platform was not resolved or deleted
            if (activePlatformValid) {
                if (getActivePlatform (lastValue) == null) {
                    //the platform was removed
                    resetCache();
                }
            }
            else {
                if (getActivePlatform (lastValue) != null) {
                    //platform was added
                    resetCache();
                }
            }
        } else if (evt.getSource() == ecpImpl) {
            resetCache();
        }
    }
    
    /**
     * Resets the cache and firesPropertyChange
     */
    private void resetCache () {
        synchronized (LOCK) {
            resourcesCache = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }
    
    @Override public boolean equals(Object obj) {
        return obj instanceof BootClassPathImpl && project.equals(((BootClassPathImpl) obj).project);
    }

    @Override public int hashCode() {
        return project.hashCode() ^ 191;
    }
    
}
