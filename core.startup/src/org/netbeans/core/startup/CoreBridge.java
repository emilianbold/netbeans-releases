/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.core.startup;

import org.openide.util.Lookup;

/** Interface to environment that the Module system needs around itself.
 *
 * @author Jaroslav Tulach
 */
public abstract class CoreBridge {

    public static CoreBridge getDefault () {
        CoreBridge b = (CoreBridge)Lookup.getDefault().lookup (CoreBridge.class);
        assert b != null : "Bridge has to be registered"; // NOI18N
        return b;
    }
    
    static void conditionallyLoaderPoolTransaction(boolean begin) {
        CoreBridge b = (CoreBridge)Lookup.getDefault().lookup(CoreBridge.class);
        if (b != null) {
            b.loaderPoolTransaction(begin);
        }
    }
    static Lookup conditionallyLookupCacheLoad () {
        CoreBridge b = (CoreBridge)Lookup.getDefault().lookup(CoreBridge.class);
        if (b != null) {
            return b.lookupCacheLoad (); 
        } else {
            return Lookup.EMPTY;
        }
    }
    
    /** Attaches or detaches to current category of actions.
     * @param category name or null
     */
    protected abstract void attachToCategory (Object category);/*
        ModuleActions.attachTo(category);
    */
    
    protected abstract void loadDefaultSection (
        ManifestSection ms, 
        org.openide.util.lookup.InstanceContent.Convertor convertor, 
        boolean add
    ); /*
        if (load) {
            if (convert) {
                NbTopManager.get().register(s, convertor);
            } else {
                NbTopManager.get().register(s);
            }
        } else {
            if (convert) {
                NbTopManager.get().unregister(s, convertor);
            } else {
                NbTopManager.get().unregister(s);
            }
        }
    */                                         
    
    protected abstract void loadActionSection(ManifestSection.ActionSection s, boolean load) throws Exception;/* {
        if (load) {
            ModuleActions.add(s);
        } else {
            ModuleActions.remove(s);
        }
    }
    */
    
    protected abstract void loadLoaderSection(ManifestSection.LoaderSection s, boolean load) throws Exception;/* {
        if (load) {
            LoaderPoolNode.add(s);
        } else {
            LoaderPoolNode.remove((DataLoader)s.getInstance());
        }
    }
*/
    
    protected abstract void loaderPoolTransaction (boolean begin); /*
        LoaderPoolNode.beginUpdates();
        LoaderPoolNode.endUpdates();
    */

    protected abstract void addToSplashMaxSteps (int cnt); /*
        Main.addToSplashMaxSteps (cnt);
    }*/
    protected abstract void incrementSplashProgressBar ();/* {
        Main.incrementSplashProgressBar ();
    }*/

    public abstract Lookup lookupCacheLoad ();
    public abstract void lookupCacheStore (Lookup l) throws java.io.IOException;
    
    /** Delegates to status displayer.
     */
    public abstract void setStatusText (String status);
    
    public abstract void initializePlaf (Class uiClass, int uiFontSize, java.net.URL themeURL);
}
