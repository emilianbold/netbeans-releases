/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.platform;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import org.openide.modules.SpecificationVersion;

/**
 * JavaPlatformManager provides access to list of installed Java Platforms in the system. It can enumerate them,
 * assign serializable IDs to their instances. It also defines a `default' platform, which represents NetBeans'
 * own runtime environment.
 *
 * @author Radko Najman, Svata Dedic, Tomas Zezula
 */
public final class JavaPlatformManager {

    /**
     * Property name of the installedPlatforms property
     */
    public static final String PROP_INSTALLED_PLATFORMS="installedPlatforms";   //NOI18N

    private static JavaPlatformManager instance = null;

    private Lookup.Result/*<JavaPlatformProvider>*/ providers;
    private Collection/*<JavaPlatformProvider>*/ lastProviders = Collections.EMPTY_SET;
    private boolean providersValid = false;
    private PropertyChangeListener pListener;
    private Collection/*<JavaPlatform>*/ cachedPlatforms;
    private HashSet/*<PropertyChangeListener>*/ listeners;

    /** Creates a new instance of JavaPlatformManager */
    public JavaPlatformManager() {
    }
 
    /** Gets an instance of JavaPlatformManager. It the instance doesn't exist it will be created.
     * @return the instance of JavaPlatformManager
     */
    public static synchronized JavaPlatformManager getDefault() {
        if (instance == null)
            instance = new JavaPlatformManager();
            
        return instance;
    }
    
    /**
     * Returns default platform. The platform the IDE is running on.
     * @return the default platform, or null in case the default platform cannot
     *         be found (e.g. the j2seplatform module is not installed)
     */
    public JavaPlatform getDefaultPlatform() {
        Collection/*<JavaPlatformProvider>*/ instances = this.getProviders ();
        for (Iterator it = instances.iterator(); it.hasNext();) {
            JavaPlatformProvider provider = (JavaPlatformProvider) it.next();
            JavaPlatform defaultPlatform = provider.getDefaultPlatform ();
            if (defaultPlatform!=null) {
                return defaultPlatform;
            }
        }
        return null;
    }
    
    /** Gets an array of JavaPlatfrom objects.
     * @return the array of java platform definitions.
     */
    public synchronized JavaPlatform[] getInstalledPlatforms() {
        if (cachedPlatforms == null) {            
            Collection/*<JavaPlatformProvider>*/ instances = this.getProviders();
            cachedPlatforms = new HashSet ();
            for (Iterator it = instances.iterator(); it.hasNext(); ) {
                JavaPlatformProvider provider = (JavaPlatformProvider) it.next ();
                JavaPlatform[] platforms = provider.getInstalledPlatforms();
                for (int i = 0; i < platforms.length; i++) {
                    cachedPlatforms.add (platforms[i]);
                }
            }
        }
        return (JavaPlatform[]) cachedPlatforms.toArray(new JavaPlatform[cachedPlatforms.size()]);
    }

    /**
     * Returns platform given by display name and/or specification. 
     * @param platformDisplayName display name of platform or null for any name.
     * @param platformSpec Specification of platform or null for platform of any type, in the specification null means all.
     * Specification with null profiles means none or any profile. 
     * Specification with Profile(null,null) means any profile but at least 1.
     * For example Specification ("CLDC", new Profile[] { new Profile("MIMDP",null), new Profile(null,null)})
     * matches all CLDC platforms with MIDP profile of any versions and any additional profile.
     * @return JavaPlatform[], never returns null, may return empty array when no platform matches given
     * query.
     */
    public JavaPlatform[] getPlatforms (String platformDisplayName, Specification platformSpec) {
        JavaPlatform[] platforms = getInstalledPlatforms();
        Collection/*<JavaPlatform>*/ result = new ArrayList ();
        for (int i = 0; i < platforms.length; i++) {            
            String name = platformDisplayName == null ? null : platforms[i].getDisplayName(); //Don't ask for display name when not needed
            Specification spec = platformSpec == null ?  null : platforms[i].getSpecification(); //Don't ask for platform spec when not needed
            if ((platformDisplayName==null || name.equalsIgnoreCase(platformDisplayName)) &&
                (platformSpec == null || compatible (spec, platformSpec))) {
                result.add(platforms[i]);
            }
        }
        return (JavaPlatform[]) result.toArray(new JavaPlatform[result.size()]);
    }

    /**
     * Adds PropertyChangeListener to the JavaPlatformManager, the listener is notified
     * when the platform is added,removed or modified.
     * @param l the listener, can not be null
     */
    public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
        assert l != null : "Listener can not be null";  //NOI18N
        if (this.listeners == null) {
            this.listeners = new HashSet ();
        }
        this.listeners.add (l);
    }

    /**
     * Removes PropertyChangeListener to the JavaPlatformManager.
     * @param l the listener, can not be null
     */
    public synchronized void removePropertyChangeListener (PropertyChangeListener l) {
        assert l != null : "Listener can not be null";  //NOI18N
        if (this.listeners == null) {
            return;
        }
        this.listeners.remove (l);
    }

    private void firePropertyChange (String property) {
        Iterator it;
        synchronized (this) {
            if (this.listeners == null) {
                return;
            }
            it = ((Set)this.listeners.clone()).iterator();
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, property, null, null);
        while (it.hasNext()) {
            ((PropertyChangeListener)it.next()).propertyChange (event);
        }
    }

    private static boolean compatible (Specification platformSpec, Specification query) {
        String name = query.getName();
        SpecificationVersion version = query.getVersion();
        return ((name == null || name.equalsIgnoreCase (platformSpec.getName())) &&
            (version == null || version.equals (platformSpec.getVersion())) &&
            compatibleProfiles (platformSpec.getProfiles(), query.getProfiles()));
    }
    
    private static boolean compatibleProfiles (Profile[] platformProfiles, Profile[] query) {
        if (query == null) {
            return true;
        }
        else if (platformProfiles == null) {
            return false;
        }
        else {
            Collection/*<Profile>*/ covered = new HashSet ();
            for (int i=0; i<query.length; i++) {
                Profile pattern = query[i];
                boolean found = false;
                for (int j = 0; j< platformProfiles.length; j++) {
                    if (compatibleProfile(platformProfiles[j],pattern)) {
                        found = true;
                        covered.add (platformProfiles[j]);
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return covered.size() == platformProfiles.length;
        }        
    }
    
    private static boolean compatibleProfile (Profile platformProfile, Profile query) {
        String name = query.getName();
        SpecificationVersion version = query.getVersion();
        return ((name == null || name.equals (platformProfile.getName())) &&
               (version == null || version.equals (platformProfile.getVersion())));
    }
    
    private synchronized Collection/*<JavaPlatformProvider>*/ getProviders () {
        if (!this.providersValid) {            
            if (this.providers == null) {
                this.providers = Lookup.getDefault().lookup(new Lookup.Template(JavaPlatformProvider.class));
                this.providers.addLookupListener (new LookupListener () {
                    public void resultChanged(LookupEvent ev) {
                        resetCache (true);
                        JavaPlatformManager.this.firePropertyChange(PROP_INSTALLED_PLATFORMS);
                    }
                });
            }
            if (this.pListener == null ) {
                this.pListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        JavaPlatformManager.this.resetCache (false);
                        JavaPlatformManager.this.firePropertyChange(PROP_INSTALLED_PLATFORMS);
                    }
                };
            }
            Collection/*<JavaPlatformProvider>*/ instances = this.providers.allInstances();
            Collection/*<JavaPlatformProvider>*/ toAdd = new HashSet(instances);
            toAdd.removeAll (this.lastProviders);
            Collection/*<JavaPlatformProvider>*/ toRemove = new HashSet(this.lastProviders);
            toRemove.removeAll (instances);
            for (Iterator it = toRemove.iterator(); it.hasNext();) {
                JavaPlatformProvider provider = (JavaPlatformProvider) it.next ();
                provider.removePropertyChangeListener (pListener);
            }
            for (Iterator it = toAdd.iterator(); it.hasNext();) {
                JavaPlatformProvider provider = (JavaPlatformProvider) it.next ();
                provider.addPropertyChangeListener (pListener);
            }
            this.lastProviders = instances;                        
            providersValid = true;
        }
        return this.lastProviders;        
    }


    private synchronized void resetCache (boolean resetProviders) {
        JavaPlatformManager.this.cachedPlatforms = null;
        this.providersValid &= !resetProviders;
    }

}
