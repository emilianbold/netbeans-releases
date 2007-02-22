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

package org.netbeans.modules.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
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
    private static final Lookup.Template<NavigatorPanel> NAV_PANEL_TEMPLATE = 
            new Lookup.Template<NavigatorPanel>(NavigatorPanel.class);
    
    /** singleton instance */
    private static ProviderRegistry instance;
    
    /** Mapping between mime types and provider instances. Note that 
     * Collections.EMPTY_LIST serves as special value telling us that
     * we already searched for providers for specific content type and found
     * no providers. This ensures no useless repetitive searches. 
     */
    private Map<String, Collection<? extends NavigatorPanel>> contentTypes2Providers;


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
     * @return Collection of providers, which implements NavigatorPanel interface.
     * Never return null, only empty List if no provider exists for given content type.
     */
    public Collection<? extends NavigatorPanel> getProviders (String contentType) {
        if (contentTypes2Providers == null) {
            contentTypes2Providers = new HashMap<String, Collection<? extends NavigatorPanel>>(15);
        }
        Collection<? extends NavigatorPanel> result = contentTypes2Providers.get(contentType);
        if (result == null) {
            // load and instantiate provider classes
            result = loadProviders(contentType);
            contentTypes2Providers.put(contentType, result);
        }
            
        return result;
    }
    
    /******* private stuff ***********/

    
    /** Returns collection of NavigatorPanels or empty collection if no provider
     * exist for given content type
     */
    private Collection<? extends NavigatorPanel> loadProviders (String contentType) {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = fs.findResource(PANELS_FOLDER + contentType);

        if (fo == null) {
            // no available providers or malformed contentType 
            Logger.getAnonymousLogger().fine("No providers for content type " + contentType); //NOI18N
            return Collections.emptyList();
        }
        
        DataFolder.Container dContainer;
        try {
            dContainer = DataFolder.findContainer(fo);
        } catch (IllegalArgumentException exc) {
            ErrorManager.getDefault().annotate(exc,
                "Navigator content type " + contentType +
                " is probably malformed, as it doesn't point to folder.");            
            ErrorManager.getDefault().notify(ErrorManager.WARNING, exc);
            return Collections.emptyList();
        }
        
        FolderLookup fLookup = new FolderLookup(dContainer, "");
        Lookup.Result<NavigatorPanel> result = fLookup.getLookup().lookup(NAV_PANEL_TEMPLATE);

        return result.allInstances();
    }

}
