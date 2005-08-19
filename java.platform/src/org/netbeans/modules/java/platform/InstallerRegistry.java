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

package org.netbeans.modules.java.platform;

import java.io.IOException;
import java.lang.ref.*;
import java.util.*;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;

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
public class InstallerRegistry {
    static final String INSTALLER_REGISTRY_FOLDER = "org-netbeans-api-java/platform/installers"; // NOI18N
    
    static Reference defaultInstance = new WeakReference(null);
    
    private Provider provider;
    private List/*<GeneralPlatformInstall>*/ platformInstalls;      //Used by unit test
    
    InstallerRegistry(FileObject registryResource) {
        assert registryResource != null;
        this.provider = new Provider (registryResource);
    }
    
    /**
     * Used only by unit tests
     */
    InstallerRegistry (GeneralPlatformInstall[] platformInstalls) {
        assert platformInstalls != null;
        this.platformInstalls = Arrays.asList(platformInstalls);
    }
    
    /**
     * Returns all registered Java platform installers, in the order as
     * they are specified by the module layer(s).
     */
    public List/*<PlatformInstall>*/  getInstallers () {
        return filter(getAllInstallers(),PlatformInstall.class);
    }
    
    public List/*<CustomPlatformIntall>*/ getCustomInstallers () {
        return filter(getAllInstallers(),CustomPlatformInstall.class);
    }
    
    public List/*<GeneralPlatformInstall>*/ getAllInstallers () {
        if (this.platformInstalls != null) {
            //In the unit test
            return platformInstalls;
        }
        else {
            Object o = Collections.EMPTY_LIST;        
            try {
                assert this.provider != null;
                o = provider.instanceCreate();
            } catch (IOException ex) {
            } catch (ClassNotFoundException ex) {
            }
            return (List) o;
        }
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
    
    
    /**
     * Used only by Unit tests.
     * Sets the {@link InstallerRegistry#defaultInstance} to the new InstallerRegistry instance which 
     * always returns the given GeneralPlatformInstalls
     * @return an instance of InstallerRegistry which has to be hold by strong reference during the test
     */
    static InstallerRegistry prepareForUnitTest (GeneralPlatformInstall[] platformInstalls) {
        InstallerRegistry regs = new InstallerRegistry (platformInstalls);
        defaultInstance = new WeakReference(regs);
        return regs;
    }
        
    
    private static List/*<T>*/ filter (List list, Class/*<T>*/ clazz) {
        List result = new ArrayList (list.size());
        for (Iterator it = list.iterator(); it.hasNext();) {
            Object item = it.next();
            if (clazz.isInstance(item)) {
                result.add (item);
            }
        }
        return result;
    }
    
    private static class Provider extends FolderInstance {
        
        Provider (FileObject registryResource) {            
            super(DataFolder.findFolder(registryResource));
        }
        
        
        protected Object createInstance(InstanceCookie[] cookies) throws java.io.IOException, ClassNotFoundException {
            List installers = new ArrayList(cookies.length);
            for (int i = 0; i < cookies.length; i++) {
                InstanceCookie cake = cookies[i];
                Object o = null;
                try {
                    if (cake instanceof InstanceCookie.Of &&
                        !((((InstanceCookie.Of)cake).instanceOf(PlatformInstall.class))  ||
                        (((InstanceCookie.Of)cake).instanceOf(CustomPlatformInstall.class))))
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
}
