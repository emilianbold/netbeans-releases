/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.platform;

import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import org.openide.modules.SpecificationVersion;
import org.netbeans.modules.java.platform.JavaPlatformProvider;

/**
 * JavaPlatformManager provides access to list of installed Java Platforms in the system. It can enumerate them,
 * assign serializable IDs to their instances. It also defines a `default' platform, which represents NetBeans'
 * own runtime environment.
 *
 * PENDING: Notification events about adding/removing a Platform
 *
 * @author Radko Najman, Svata Dedic
 */
public final class JavaPlatformManager {
    
    private static JavaPlatformManager instance = null;

    private Lookup.Result providers;
    private Collection lastProviders = Collections.EMPTY_SET;
    private PropertyChangeListener pListener;
    private Collection cachedPlatforms;

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
    
    public JavaPlatform getDefaultPlatform() {
        return (JavaPlatform)Lookup.getDefault().lookup(JavaPlatform.class);
    }
    
    /** Gets an array of JavaPlatfrom objects.
     * @return the array of java platform definitions.
     */
    public synchronized JavaPlatform[] getInstalledPlatforms() {
        if (cachedPlatforms == null) {
            if (this.providers == null) {
                this.providers = Lookup.getDefault().lookup(new Lookup.Template(JavaPlatformProvider.class));
                this.providers.addLookupListener (new LookupListener () {
                    public void resultChanged(LookupEvent ev) {
                        resetCache ();
                    }
                });
            }
            if (this.pListener == null ) {
                this.pListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        JavaPlatformManager.this.resetCache();
                    }
                };
            }
            Collection instances = this.providers.allInstances();
            Collection toAdd = new HashSet (instances);
            toAdd.removeAll (this.lastProviders);
            Collection toRemove = new HashSet (this.lastProviders);
            toRemove.removeAll (instances);
            cachedPlatforms = new HashSet ();
            for (Iterator it = toRemove.iterator(); it.hasNext();) {
                JavaPlatformProvider provider = (JavaPlatformProvider) it.next ();
                provider.removePropertyChangeListener (pListener);
            }
            for (Iterator it = instances.iterator(); it.hasNext(); ) {
                JavaPlatformProvider provider = (JavaPlatformProvider) it.next ();
                JavaPlatform[] platforms = provider.getInstalledPlatforms();
                for (int i = 0; i < platforms.length; i++) {
                    cachedPlatforms.add (platforms[i]);
                }
            }
            for (Iterator it = toAdd.iterator(); it.hasNext();) {
                JavaPlatformProvider provider = (JavaPlatformProvider) it.next ();
                provider.addPropertyChangeListener (pListener);
            }
            this.lastProviders = instances;
        }
        return (JavaPlatform[]) cachedPlatforms.toArray(new JavaPlatform[cachedPlatforms.size()]);
    }

    /**
     * Returns platform of given display name or null if such a platform
     * does not exist
     * @param platformDisplayName display name of platform or null for any name
     * @param platformSpec of platform or null for any type, in the specifiaction null means *
     * @return JavaPlatform[], never returns null
     */
    public JavaPlatform[] getPlatforms (String platformDisplayName, Specification platformSpec) {
        JavaPlatform[] platforms = getInstalledPlatforms();
        Collection result = new ArrayList ();
        for (int i = 0; i < platforms.length; i++) {
            String name = platforms[i].getDisplayName();
            Specification spec = platforms[i].getSpecification();
            if ((platformDisplayName==null || name.equalsIgnoreCase(platformDisplayName)) &&
                (platformSpec == null || compatible (spec, platformSpec))) {
                result.add(platforms[i]);
            }
        }
        return (JavaPlatform[]) result.toArray(new JavaPlatform[result.size()]);
    }


    private static boolean compatible (Specification platformSpec, Specification query) {
        String name = query.getName();
        SpecificationVersion version = query.getVersion();
        return ((name == null || name.equalsIgnoreCase (platformSpec.getName())) &&
            (version == null || version.equals (platformSpec.getVersion())));
    }


    private synchronized void resetCache () {
        JavaPlatformManager.this.cachedPlatforms = null;
    }

}
