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

import org.netbeans.modules.autoupdate.updateprovider.NativeComponentItem;
import org.netbeans.modules.autoupdate.updateprovider.FeatureItem;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.autoupdate.updateprovider.LocalizationItem;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.modules.autoupdate.updateprovider.ArtificialFeaturesProvider;
import org.netbeans.modules.autoupdate.updateprovider.BackupUpdateProvider;
import org.netbeans.modules.autoupdate.updateprovider.InstalledUpdateProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitFactory {
    
    /** Creates a new instance of UpdateItemFactory */
    private UpdateUnitFactory () {}
    
    private static final UpdateUnitFactory INSTANCE = new UpdateUnitFactory ();
    private final Logger log = Logger.getLogger (this.getClass ().getName ());
    public static UpdateUnitFactory getDefault () {
        return INSTANCE;
    }
    
    public Map<String, UpdateUnit> getUpdateUnits () {
        List<UpdateUnitProvider> updates = UpdateUnitProviderImpl.getUpdateUnitProviders (true);
        
        // append installed units
        Map<String, UpdateUnit> mappedImpl = appendUpdateItems (new HashMap<String, UpdateUnit> (), InstalledModuleProvider.getDefault ());
        
        try {
            // append units from ArtificialFeaturesProvider
            mappedImpl = appendUpdateItems (mappedImpl, new ArtificialFeaturesProvider (InstalledModuleProvider.getDefault ().getUpdateItems ().values ()));
        } catch (IOException ioe) {
            log.log (Level.INFO, "Cannot read UpdateItems from InstalledModuleProvider " + InstalledModuleProvider.getDefault (), ioe);
        }
        
        for (UpdateUnitProvider up : updates) {
            UpdateUnitProviderImpl impl = Trampoline.API.impl (up);
            
            // append units from provider
            mappedImpl = appendUpdateItems (mappedImpl, impl.getUpdateProvider ());
            
            // append units from ArtificialFeaturesProvider
            try {
                mappedImpl = appendUpdateItems (mappedImpl, new ArtificialFeaturesProvider (impl.getUpdateProvider ().getUpdateItems ().values ()));
            } catch (IOException ioe) {
                log.log (Level.INFO, "Cannot read UpdateItems from ArtificialFeaturesProvider.", ioe);
            }
        }
        
        return mappedImpl;
    }
    
    public Map<String, UpdateUnit> getUpdateUnits (UpdateProvider provider) {
        
        // prepare items accessible in provider
        Collection<UpdateItem> itemsFromProvider = null;
        Collection<UpdateItem> artificialItems = null;
        
        try {
            itemsFromProvider = provider.getUpdateItems ().values();
        } catch (IOException ioe) {
            log.log (Level.INFO, "Cannot read UpdateItems from UpdateProvider " + provider, ioe);
            itemsFromProvider = Collections.emptySet ();
        }
        
        try {
            artificialItems = new ArtificialFeaturesProvider (itemsFromProvider).getUpdateItems ().values ();
        } catch(IOException ioe) {
            log.log (Level.INFO, "Cannot read UpdateItems from ArtificialFeaturesProvider.", ioe);
            artificialItems = Collections.emptySet ();
        }
        
        Collection<UpdateItem> items = new HashSet<UpdateItem> (itemsFromProvider);
        items.addAll (artificialItems);
        
        // check installed units
        Map<String, UpdateUnit> temp  = appendUpdateItems (new HashMap<String, UpdateUnit> (), InstalledModuleProvider.getDefault ());
        
        // append units from provider
        temp = appendUpdateItems (temp, provider);
        
        // append units from ArtificialFeaturesProvider
        temp = appendUpdateItems (temp, new ArtificialFeaturesProvider (itemsFromProvider));
        
        assert items != null : provider + " UpdateProvider cannot returns null items.";
        Map<String, UpdateUnit> retval = new HashMap<String, UpdateUnit>();
        for (UpdateItem updateItem : items) {
            UpdateItemImpl itemImpl = Trampoline.SPI.impl(updateItem);
            UpdateUnit unit = temp.get (itemImpl.getCodeName ());
            if (unit != null) {
                retval.put (itemImpl.getCodeName (), unit);
            }            
        }
        
        return retval;
    }
    
    Map<String, UpdateUnit> appendUpdateItems (Map<String, UpdateUnit> originalUnits, UpdateProvider provider) {
        assert originalUnits != null : "Map of original UnitImpl cannot be null";

        Map<String, UpdateItem> items;
        try {
            items = provider.getUpdateItems ();
        } catch (IOException ioe) {
            log.log (Level.INFO, "Cannot read UpdateItem from UpdateProvider " + provider, ioe);
            return originalUnits;
        }
        
        assert items != null : "UpdateProvider[" + provider.getName () + "] should return non-null items.";
        
        // append updates
        for (String simpleItemId : items.keySet ()) {

            // create UpdateItemImpl
            UpdateItemImpl itemImpl = Trampoline.SPI.impl (items.get (simpleItemId));

            UpdateElement updateEl = null;
            if (itemImpl instanceof InstalledModuleItem) {
                updateEl = Trampoline.API.createUpdateElement (new ModuleUpdateElementImpl ((InstalledModuleItem) itemImpl, null));
            } else if (itemImpl instanceof ModuleItem) {
                updateEl = Trampoline.API.createUpdateElement (new ModuleUpdateElementImpl ((ModuleItem) itemImpl, provider.getDisplayName ()));
            } else if (itemImpl instanceof LocalizationItem) {
                updateEl = Trampoline.API.createUpdateElement (new LocalizationUpdateElementImpl ((LocalizationItem) itemImpl, provider.getDisplayName ()));
            } else if (itemImpl instanceof NativeComponentItem) {
                updateEl = Trampoline.API.createUpdateElement (new NativeComponentUpdateElementImpl ((NativeComponentItem) itemImpl, provider.getDisplayName ()));
            } else if (itemImpl instanceof FeatureItem) {
                updateEl = Trampoline.API.createUpdateElement (new FeatureUpdateElementImpl ((FeatureItem) itemImpl, provider.getDisplayName ()));
            } else {
                assert false : "Unknown type of UpdateElement " + updateEl;
            }

            // add element to map
            if (updateEl != null) {
                addElement (originalUnits, updateEl, provider);
            }
        }

        return originalUnits;
    }
    
    private void addElement (Map<String, UpdateUnit> impls, UpdateElement element, UpdateProvider provider) {
        // find if corresponding element exists
        UpdateUnit unit = impls.get (element.getCodeName ());
        
        // XXX: it's should be moved in UI what should filter all elements w/ broken dependencies
        // #101515: Plugin Manager must filter updates by platform dependency
        boolean passed = false;
        UpdateElementImpl elImpl = Trampoline.API.impl (element);
        if (elImpl instanceof ModuleUpdateElementImpl && elImpl.getModuleInfos () != null && elImpl.getModuleInfos ().size() == 1) {
            for (Dependency d : elImpl.getModuleInfos ().get (0).getDependencies ()) {
                if (Dependency.TYPE_REQUIRES == d.getType ()) {
                    //log.log (Level.FINEST, "Dependency: NAME: " + d.getName () + ", TYPE: " + d.getType () + ": " + d.toString ());
                    if (d.getName ().startsWith ("org.openide.modules.os")) { // NOI18N
                        for (ModuleInfo info : InstalledModuleProvider.getInstalledModules ().values ()) {
                            if (Arrays.asList (info.getProvides ()).contains (d.getName ())) {
                                log.log (Level.FINEST, element + " which requires OS " + d + " succeed.");
                                passed = true;
                                break;
                            }
                        }
                        if (! passed) {
                            log.log (Level.FINE, element + " which requires OS " + d + " fails.");
                            return ;
                        }
                    }
                }
            }
        }
        
        UpdateUnitImpl unitImpl = null;
        
        if (unit == null) {
            switch (elImpl.getType ()) {
            case MODULE :
                unitImpl = new ModuleUpdateUnitImpl (element.getCodeName ());
                break;
            case FEATURE :
                unitImpl = new FeatureUpdateUnitImpl (element.getCodeName ());
                break;
            case CUSTOM_HANDLED_COMPONENT :
                unitImpl = new NativeComponentUpdateUnitImpl (element.getCodeName ());
                break;
            case LOCALIZATION :
                unitImpl = new LocalizationUpdateUnitImpl (element.getCodeName ());
                break;
            default:
                assert false : "Unsupported for type " + elImpl.getType ();
            }
            unit = Trampoline.API.createUpdateUnit (unitImpl);
            impls.put (unit.getCodeName (), unit);
        } else {
            unitImpl = Trampoline.API.impl (unit);
        }
        
        if (provider instanceof InstalledUpdateProvider) {
            unitImpl.setInstalled (element);
        } else if (provider instanceof BackupUpdateProvider) {
            unitImpl.setBackup (element);
        } else {
            // suppose common UpdateProvider
            unitImpl.addUpdate (element);
        }
        
        // set UpdateUnit into element
        elImpl.setUpdateUnit (unit);
        
    }
    
}
