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

package org.netbeans.modules.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;

/**
 * Storage/lookup of NavigatorPanel providers. Providers are mapped to
 * mime types they support. 
 *
 * @author Dafe Simonek
 */
class ProviderRegistry {
    
    /** folder in layer file system where navigator panels are searched for */
    private static final String PANELS_FOLDER = "/Navigator/Panels/"; //NOI18N
    /** template for finding all NavigatorPanel instances in lookup */
    private static final Lookup.Template NAV_PANEL_TEMPLATE = 
            new Lookup.Template(NavigatorPanel.class);
    
    /** singleton instance */
    private static ProviderRegistry instance;
    
    /** Mapping between mime types and provider instances. Note that 
     * Collections.EMPTY_LIST serves as special value telling us that
     * we already searched for providers for specific content type and found
     * no providers. This ensures no useless repetitive searches. 
     * <String, List<NavigatorPanel> > */
    private Map contentTypes2Providers;


    /** Singleton, no external instantiation */
    private ProviderRegistry () {
    }

    /********* public area *********/
    
    public static ProviderRegistry getInstance () {
        if (instance == null ) {
            instance = new ProviderRegistry();
        }
        return instance;
    }
    
    /** Finds appropriate providers for given data content type
     * (similar to mime type)
     * and returns list of provider classes.
     *
     * @return List<Class> of providers, which implements NavigatorPanel interface.
     * Never return null, only empty List if no provider exists for given content type.
     */
    public List getProviders (String contentType) {
        if (contentTypes2Providers == null) {
            contentTypes2Providers = new HashMap(15);
        }
        List result = (List)contentTypes2Providers.get(contentType);
        if (result == null) {
            // load and instantiate provider classes
            result = loadProviders(contentType);
            contentTypes2Providers.put(contentType, result);
        }
            
        return result;
    }
    
    /******* private stuff ***********/

    
    /** Returns List<NavigatorPanel> or Collections.EMPTY_LIST if no provider
     * exist for given content type
     */
    private List loadProviders (String contentType) {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = fs.findResource(PANELS_FOLDER + contentType);

        if (fo == null) {
            // no available providers or malformed contentType 
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, 
                "No providers for content type " + contentType); //NOI18N
            return Collections.EMPTY_LIST;
        }
        
        DataFolder.Container dContainer;
        try {
            dContainer = DataFolder.findContainer(fo);
        } catch (IllegalArgumentException exc) {
            ErrorManager.getDefault().annotate(exc,
                "Navigator content type " + contentType +
                " is probably malformed, as it doesn't point to folder.");            
            ErrorManager.getDefault().notify(ErrorManager.WARNING, exc);
            return Collections.EMPTY_LIST;
        }
        
        FolderLookup fLookup = new FolderLookup(dContainer, "");
        Lookup.Result result = fLookup.getLookup().lookup(NAV_PANEL_TEMPLATE);

        return (List)result.allInstances();
    }

}
