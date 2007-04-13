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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import org.netbeans.api.autoupdate.*;
import org.netbeans.spi.autoupdate.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalog;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogCache;
import org.netbeans.modules.autoupdate.updateprovider.LocalNBMsProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


/** XXX <code>UpdateProvider</code> providers items for Autoupdate infrastructure. The items
 * are available on e.g. Update Center. Items can represents NetBeans Module,
 * its Localization, Feature as group of NetBeans Modules or special
 * components which needs own native installer to make them accessible in NetBeans product.
 *
 * @author Jiri Rechtacek
 */
public final class UpdateUnitProviderImpl {
    private UpdateProvider provider;
    private static Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.services.UpdateUnitProviderImpl");
    private static final String REMOVED_MASK ="_removed";
            
    public UpdateUnitProviderImpl (UpdateProvider provider) {
        this.provider = provider;
    }
    
    /** Name of provider, this name is used by Autoupdate infrastructure for manimulating
     * of providers.
     * 
     * @return name of provider
     */
    public String getName () {
        return getUpdateProvider ().getName ();
    }
    
    /** Display name of provider. This display name can be visualized in UI.
     * 
     * @return display name of provider
     */
    public String getDisplayName () {
        return loadDisplayName (getUpdateProvider ());
    }
    
    public void setDisplayName (String name) {
        storeDisplayName (getUpdateProvider (), name);
    }
    
    public URL getProviderURL () {
        return loadUrl (getUpdateProvider ());
    }
    
    public void setProviderURL (URL url) {
        storeUrl (getUpdateProvider (), url);
    }
    
    public List<UpdateUnit> getUpdateUnits () {
        return UpdateManagerImpl.getInstance().getUpdateUnits (getUpdateProvider ());
    }
    
    /** Make refresh of content of the provider. The content can by read from
     * a cache. The <code>force</code> parameter forces reading content from
     * remote server.
     * 
     * @param force if true then forces to reread the content from server
     * @return true if refresh succeed
     * @throws java.io.IOException when any network problem appreared
     */
    public boolean refresh (ProgressHandle handle, boolean force) throws IOException {
        boolean res = false;
        ProgressHandle ownHandle = null;
        if (handle == null) {
            ownHandle = ProgressHandleFactory.createHandle (NbBundle.getMessage (UpdateUnitProviderImpl.class, "UpdateUnitProviderImpl_CheckingForUpdates"));
            ownHandle.setInitialDelay (0);
            ownHandle.start ();
        }
        try {
            getUpdateProvider ().refresh (force);
        } finally {
            if (ownHandle != null) {
                ownHandle.finish ();
            }
            if (handle != null) {
                handle.progress (getDisplayName ());
            }
        }
        return res;
    }
    
    public void setEnable (boolean state) {
        storeState (getUpdateProvider (), state);
    }
    
    public boolean isEnabled () {
        return loadState (getUpdateProvider ().getName ());
    }
    
    public UpdateProvider getUpdateProvider () {
        assert provider != null : "UpdateProvider found.";
        return provider;
    }
    
    public static void remove (UpdateUnitProvider unitProvider) {
        UpdateUnitProviderImpl impl = Trampoline.API.impl(unitProvider);
        impl.remove();
    }   
    
    private void remove() {                        
        try {
            if (getPreferences().nodeExists(getName())) {
                getPreferences().node(getName()).removeNode();
                getPreferences().node(getName()+REMOVED_MASK).putBoolean(REMOVED_MASK, true);
            } else {
                getPreferences().node(getName()+REMOVED_MASK).putBoolean(REMOVED_MASK, true);
            }
            
        } catch(BackingStoreException bsx) {
            Exceptions.printStackTrace(bsx);
        }
    }
    
    // static factory methods
    public static UpdateUnitProvider createUpdateUnitProvider (String codeName, String displayName, URL url) {
        // store to Preferences
        storeProvider(codeName, displayName, url);
        
        AutoupdateCatalog catalog = new AutoupdateCatalog (codeName, displayName, url);
        
        return Trampoline.API.createUpdateUnitProvider (new UpdateUnitProviderImpl (catalog));
    }

    public static UpdateUnitProvider createUpdateUnitProvider (String name, File... files) {
        LocalNBMsProvider provider = new LocalNBMsProvider (name, files);
        return Trampoline.API.createUpdateUnitProvider (new UpdateUnitProviderImpl (provider));
    }
    
    public static List<UpdateUnitProvider> getUpdateUnitProviders (boolean onlyEnabled) {
        try {
            Lookup.getDefault ().lookup (
                    new Lookup.Template (
                        Class.forName ("org.netbeans.modules.autoupdate.AutoupdateType",
                        false,
                        Thread.currentThread ().getContextClassLoader ())
                    )).allInstances ();
        } catch (ClassNotFoundException x) {
            // here we can ignore this
            err.log (Level.FINEST, x.getMessage (), x);
        }
        Lookup.Result<UpdateProvider> result = Lookup.getDefault ().lookup (new Lookup.Template<UpdateProvider> (UpdateProvider.class));
//        result.addLookupListener (new LookupListener () {
//            public void resultChanged(LookupEvent ev) {
//                try {
//                    refresh ();
//                    err.log (Level.FINE, "Lookup.Result changed " + ev);
//                } catch (IOException ioe) {
//                    err.log (Level.INFO, ioe.getMessage (), ioe);
//                }
//            }
//        });
        
        Collection<? extends UpdateProvider> col = result.allInstances ();
        Map<String, UpdateProvider> providerMap = new HashMap<String, UpdateProvider> ();
        for (UpdateProvider provider : col) {
            try {
                if (getPreferences ().nodeExists(provider.getName()+REMOVED_MASK)) {
                    continue;
                }
            } catch(BackingStoreException bsx) {
                Exceptions.printStackTrace(bsx);
            }
            
            providerMap.put (provider.getName (), provider);
        }
        
        try {
            Preferences p = getPreferences ();
            String[] children = p.childrenNames ();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    UpdateProvider provider = loadProvider (children [i]);
                    if (provider != null && providerMap.containsKey (provider.getName ())) {
                        err.log (Level.INFO, "Customized Provider " + provider.getName ());
                    }
                    if (provider != null) {
                        providerMap.put (provider.getName (), provider);
                    }
                    
                    // check its state
                    boolean enabled = loadState (children [i]);
                    if (onlyEnabled && !enabled) {
                        providerMap.remove (children [i]);
                    }
                }
            }
        } catch (BackingStoreException bse) {
            err.log(Level.INFO, bse.getMessage(), bse);
        }
        
        List<UpdateUnitProvider> unitProviders = new ArrayList<UpdateUnitProvider> (providerMap.values ().size ());
        for (UpdateProvider p : providerMap.values ()) {
            UpdateUnitProviderImpl impl = new UpdateUnitProviderImpl (p);
            unitProviders.add (Trampoline.API.createUpdateUnitProvider (impl));
        }
        return unitProviders;
    }
    
    public static void refreshProviders (ProgressHandle handle, boolean force) throws IOException {
        List<UpdateUnitProvider> providers = getUpdateUnitProviders (true);
        for (UpdateUnitProvider p : providers) {
            p.refresh (handle, force);
        }
        if (force) {
            // store time of the last check
            AutoupdateSettings.setLastCheck (new Date ());
        }
        UpdateManagerImpl.getInstance().refresh();
    }
    
    private static void storeProvider (String codeName, String displayName, URL url) {
        Preferences providerPreferences = getPreferences ().node (codeName);
        assert providerPreferences != null : "Preferences node " + codeName + " found.";
        
        providerPreferences.put ("url", url.toString ());
        providerPreferences.put ("displayName", displayName);
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate"); // NOI18N
    }    
    
    /*private because tests*/ static UpdateProvider loadProvider (String codeName) {
        Preferences providerPreferences = getPreferences ().node (codeName);
        assert providerPreferences != null : "Preferences node " + codeName + " found.";
        
        String toUrl = providerPreferences.get ("url", null);
        String displayName = providerPreferences.get ("displayName", codeName);
        
        // filter Providers which store only its state
        if (toUrl == null) {
            return null;
        }
        
        URL url = null;
        try {
            url = new URL (toUrl);
        } catch (MalformedURLException mue) {
            assert false : mue;
        }
        
        return new AutoupdateCatalog (codeName, displayName, url);
    }
    
    private static boolean loadState (String codename) {
        Preferences providerPreferences = getPreferences ().node (codename);
        assert providerPreferences != null : "Preferences node " + codename + " found.";
        
        String enabled = providerPreferences.get ("enabled", null);
        
        return ! Boolean.FALSE.toString ().equals (enabled);
    }
    
    private static void storeState (UpdateProvider p, boolean isEnabled) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        providerPreferences.put ("enabled", Boolean.valueOf (isEnabled).toString ());
    }
    
    private static String loadDisplayName (UpdateProvider p) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        return providerPreferences.get ("displayName", p.getDisplayName ());
    }
    
    private static void storeDisplayName (UpdateProvider p, String displayName) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        // store only if differs
        if (displayName == null) {
            providerPreferences.remove ("displayName");
        } else if (! displayName.equals (p.getDisplayName ())) {
            providerPreferences.put ("displayName", displayName);
        }
    }
    
    private static URL loadUrl (UpdateProvider p) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        String urlSpec = null;
        if (p instanceof AutoupdateCatalog) {
            urlSpec = ((AutoupdateCatalog) p).getUpdateCenterURL ().toExternalForm ();
        }
        urlSpec = providerPreferences.get ("url", urlSpec);
        if (urlSpec == null || urlSpec.length () == 0) {
            return null;
        } else {
            int idx = urlSpec.indexOf ("?unique");
            if (idx != -1) {
                urlSpec = urlSpec.substring (0, idx);
            }
        }
        err.log (Level.FINE, "Use urlSpec " + urlSpec + " for Provider " + p);
        URL url = null;
        try {
            url = new URL (urlSpec);
        } catch (MalformedURLException x) {
            err.log (Level.INFO, x.getMessage(), x);
        }
        return url;
    }
    
    private static void storeUrl (UpdateProvider p, URL url) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        // store only if differs
        if (url == null) {
            providerPreferences.remove ("url");
        } else {
            URL orig = null;
            if (p instanceof AutoupdateCatalog) {
                orig = ((AutoupdateCatalog) p).getUpdateCenterURL ();
            }
            if (! url.equals (orig)) {
                providerPreferences.put ("url", url.toExternalForm ());
            }
        }
    }

}
