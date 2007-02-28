/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

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
    
    /** Checks whether everything is started.
     */
    static boolean isStarted() {
        return started;
    } 

    /** Initialize the lookup to delegate to NbTopManager.
    */
    public MainLookup () {
        super (new Lookup[] {
                   // #14722: pay attention also to META-INF/services/class.Name resources:
                   Lookups.metaInfServices(classLoader),
                   Lookups.singleton(classLoader),
                   Lookup.EMPTY, // will be moduleLookup
                   instanceLookup
               });
    }

    /** Called when a system classloader changes.
     */
    public static final void systemClassLoaderChanged (ClassLoader nue) {
        if (!(Lookup.getDefault() instanceof MainLookup)) {
            // May be called from MockServices.setServices even though we are not main lookup.
            return;
        }
        if (classLoader != nue) {
            classLoader = nue;
            MainLookup l = (MainLookup)Lookup.getDefault();
            Lookup[] delegates = l.getLookups();
            Lookup[] newDelegates = delegates.clone();
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
        newDelegates = delegates.clone();
        newDelegates[0] = Lookups.metaInfServices(classLoader);
        l.setLookups(newDelegates);
    }

    /** Called when Lookup<ModuleInfo> is ready from the ModuleManager.
     * @see "#28465"
     */
    public static final void moduleLookupReady(Lookup moduleLookup) {
        MainLookup l = (MainLookup)Lookup.getDefault();
        Lookup[] newDelegates = l.getLookups().clone();
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
    public static <T,R> void register(T obj, InstanceContent.Convertor<T,R> conv) {
        instanceContent.add(obj, conv);
    }
    
    /** Unregisters the service.
     */
    public static void unregister (Object obj) {
        instanceContent.remove (obj);
    }
    /** Unregisters the service registered with a convertor.
     */
    public static <T,R> void unregister (T obj, InstanceContent.Convertor<T,R> conv) {
        instanceContent.remove (obj, conv);
    }
    
    
    
    

    private final void doInitializeLookup () {
        //System.err.println("doInitializeLookup");

        // extend the lookup
        Lookup[] arr = new Lookup[] {
            getLookups()[0], // metaInfServicesLookup
            getLookups()[1], // ClassLoader lookup
            getLookups()[2], // ModuleInfo lookup
            instanceLookup, 
            CoreBridge.conditionallyLookupCacheLoad (),
        };
        StartLog.logProgress ("prepared other Lookups"); // NOI18N

        setLookups (arr);
        StartLog.logProgress ("Lookups set"); // NOI18N

        CoreBridge.lookupInitialized();
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
    
