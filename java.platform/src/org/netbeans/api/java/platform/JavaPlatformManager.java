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

import java.io.IOException;
import java.util.*;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.openide.util.Lookup;
import org.openide.modules.SpecificationVersion;

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
    public JavaPlatform[] getInstalledPlatforms() {
        List platforms = new ArrayList ();
        FileObject storage = Repository.getDefault().getDefaultFileSystem().findResource("Services/Platforms/org-netbeans-api-java-Platform");  //NOI18N
        if (storage != null) {
            try {
            FileObject[] platfomDefinitions = storage.getChildren();
            for (int i=0; i<platfomDefinitions.length;i++) {
                DataObject dobj = DataObject.find (platfomDefinitions[i]);
                InstanceCookie ic = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
                if (ic == null) {
                    continue;
                }
                else  if (ic instanceof InstanceCookie.Of) {
                    if (((InstanceCookie.Of)ic).instanceOf(JavaPlatform.class)) {
                        platforms.add (ic.instanceCreate());
                    }
                }
                else {
                    Object instance = ic.instanceCreate();
                    if (instance instanceof JavaPlatform) {
                        platforms.add (instance);
                    }
                }
            }
            }catch (ClassNotFoundException cnf) {
                ErrorManager.getDefault().notify (cnf);
            }
            catch (IOException ioe) {
                ErrorManager.getDefault().notify (ioe);
            }
        }
        return (JavaPlatform[])platforms.toArray(new JavaPlatform[platforms.size()]);
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

}
