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

package org.netbeans.core.startup;

import org.openide.util.Lookup;
import org.openide.util.lookup.*;
import org.openide.modules.*;

import org.netbeans.*;

/** The default lookup for the system.
 */
public final class MainLookup extends ProxyLookup {
    private static boolean started = false;
    /** currently effective ClassLoader */
    private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    /** inner access to dynamic lookup service for this top mangager */
    private static InstanceContent instanceContent = new InstanceContent ();
    /** dynamic lookup service for this top mangager */
    private static Lookup instanceLookup = new AbstractLookup (instanceContent);

    /** Someone called NbTopManager.get().
     * That means that subsequent calls to lookup on ModuleInfo
     * need not try to get it again.
     */
    public static void startedNbTopManager() {
        started = true;
    }

    /** Initialize the lookup to delegate to NbTopManager.
    */
    public MainLookup () {
        super (new Lookup[] {
                   // #14722: pay attention also to META-INF/services/class.Name resources:
                   Lookups.metaInfServices(classLoader),
                   Lookups.singleton(classLoader),
                   Lookup.EMPTY, // will be moduleLookup
               });
    }

    /** Called when a system classloader changes.
     */
    public static final void systemClassLoaderChanged (ClassLoader nue) {
        if (classLoader != nue) {
            classLoader = nue;
            MainLookup l = (MainLookup)Lookup.getDefault();
            Lookup[] delegates = l.getLookups();
            Lookup[] newDelegates = (Lookup[])delegates.clone();
            // Replace classloader.
            newDelegates[0] = Lookups.metaInfServices(classLoader);
            newDelegates[1] = Lookups.singleton(classLoader);
            l.setLookups(newDelegates);
        } else {
            moduleClassLoadersUp();
        }
    }

    /** Called when modules are about to be turned on.
     */
    public static final void moduleClassLoadersUp() {
        MainLookup l = (MainLookup)Lookup.getDefault();
        Lookup[] newDelegates = null;
        Lookup[] delegates = l.getLookups();
        newDelegates = (Lookup[])delegates.clone();
        newDelegates[0] = Lookups.metaInfServices(classLoader);
        l.setLookups(newDelegates);
    }

    /** Called when Lookup<ModuleInfo> is ready from the ModuleManager.
     * @see "#28465"
     */
    public static final void moduleLookupReady(Lookup moduleLookup) {
        MainLookup l = (MainLookup)Lookup.getDefault();
        Lookup[] newDelegates = (Lookup[])l.getLookups().clone();
        newDelegates[2] = moduleLookup;
        l.setLookups(newDelegates);
    }

    /** When all module classes are accessible thru systemClassLoader, this
     * method is called to initialize the FolderLookup.
     */

    public static final void modulesClassPathInitialized () {
        //System.err.println("mCPI");
    //StartLog.logStart ("NbTopManager$MainLookup: initialization of FolderLookup"); // NOI18N

        // replace the lookup by new one
        Lookup lookup = Lookup.getDefault ();
        StartLog.logProgress ("Got Lookup"); // NOI18N

        ((MainLookup)lookup).doInitializeLookup ();
    }

    //
    // 
    //
    
    /** Register new instance.
     */
    public static void register (Object obj) {
        instanceContent.add (obj);
    }
    
    /** Register new instance.
     * @param obj source
     * @param conv convertor which postponing an instantiation
     */
    public static void register(Object obj, InstanceContent.Convertor conv) {
        instanceContent.add(obj, conv);
    }
    
    /** Unregisters the service.
     */
    public static void unregister (Object obj) {
        instanceContent.remove (obj);
    }
    /** Unregisters the service registered with a convertor.
     */
    public static void unregister (Object obj, InstanceContent.Convertor conv) {
        instanceContent.remove (obj, conv);
    }
    
    
    
    

    private final void doInitializeLookup () {
        //System.err.println("doInitializeLookup");

        // extend the lookup
        Lookup[] arr = new Lookup[] {
            getLookups()[0], // metaInfServicesLookup
            getLookups()[1], // ClassLoader lookup
            getLookups()[2], // ModuleInfo lookup
            // XXX figure out how to put this ahead of MetaInfServicesLookup (for NonGuiMain):
            instanceLookup, 
            CoreBridge.conditionallyLookupCacheLoad (),
        };
        StartLog.logProgress ("prepared other Lookups"); // NOI18N

        setLookups (arr);
        StartLog.logProgress ("Lookups set"); // NOI18N

    //StartLog.logEnd ("NbTopManager$MainLookup: initialization of FolderLookup"); // NOI18N
    }

    public void storeCache() throws java.io.IOException {
        Lookup[] ls = getLookups();
        if (ls.length == 5) {
            // modulesClassPathInitialized has been called, so store folder lookup
            CoreBridge.getDefault ().lookupCacheStore (ls[4]);
        }
    }

    protected void beforeLookup(Lookup.Template templ) {
        Class type = templ.getType();

        // Force module system to be initialize by looking up ModuleInfo.
        // Good for unit tests, etc.
        if (!started && (type == ModuleInfo.class || type == org.netbeans.Module.class)) {
            Main.getModuleSystem ();
        }

        super.beforeLookup(templ);
    }
}
    
