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

package org.netbeans.modules.java.platform;

import java.io.IOException;
import java.lang.ref.*;
import java.util.*;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.netbeans.spi.java.platform.PlatformInstall;

/**
 * Simple helper class, which keeps track of registered PlatformInstallers.
 * It caches its [singleton] instance for a while.
 *
 * @author Svata Dedic
 */
public class InstallerRegistry extends FolderInstance {
    static final String INSTALLER_REGISTRY_FOLDER = "org-netbeans-api-java/platform/installers";
    
    static Reference defaultInstance = new WeakReference(null);
    
    InstallerRegistry(FileObject registryResource) {
        super(DataFolder.findFolder(registryResource));
    }
    /**
     * Returns all registered Java platform installers, in the order as
     * they are specified by the module layer(s).
     */
    public List  getInstallers() {
        Object o = Collections.EMPTY_LIST;
        
        try {
            o = instanceCreate();
        } catch (IOException ex) {
        } catch (ClassNotFoundException ex) {
        }
        return (List)o;
    }

    /**
     * Creates/acquires an instance of InstallerRegistry
     */
    public static InstallerRegistry getDefault() {
        Object o = defaultInstance.get();
        if (o != null)
            return (InstallerRegistry)o;
        o = new InstallerRegistry(Repository.getDefault().getDefaultFileSystem().findResource(
            INSTALLER_REGISTRY_FOLDER));
        defaultInstance = new WeakReference(o);
        return (InstallerRegistry)o;
    }
    
    protected Object createInstance(InstanceCookie[] cookies) throws java.io.IOException, ClassNotFoundException {
        List installers = new ArrayList(cookies.length);
        for (int i = 0; i < cookies.length; i++) {
            InstanceCookie cake = cookies[i];
            Object o = null;
            try {
                if (cake instanceof InstanceCookie.Of &&
                    !(((InstanceCookie.Of)cake).instanceOf(PlatformInstall.class)))
                    continue;
                o = cake.instanceCreate();
            } catch (IOException ex) {
            } catch (ClassNotFoundException ex) {
            }
            if (o != null)
                installers.add(o);
        }
        return installers;
    }
}
