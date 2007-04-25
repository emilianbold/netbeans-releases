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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;

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
    
    public static enum Type {
        INSTALLED,
        BACKUP,
        UPDATE
    }
    
    public Map<String, UpdateUnit> getUpdateUnits () {
        List<UpdateUnitProvider> updates = UpdateUnitProviderImpl.getUpdateUnitProviders (true);
        
        Map<String, UpdateUnit> mappedImpl = appendInstalldeModules (new HashMap<String, UpdateUnit> ());
        
        for (UpdateUnitProvider up : updates) {
            UpdateUnitProviderImpl impl = Trampoline.API.impl (up);
            mappedImpl = appendUpdateItems (mappedImpl, impl.getUpdateProvider ());
        }
        
        return mappedImpl;
    }
    
    public Map<String, UpdateUnit> getUpdateUnits (UpdateProvider provider) {
        Collection<UpdateItem> items = null;
        try {
             items = provider.getUpdateItems ().values();

        } catch(IOException iex) {
            Exceptions.printStackTrace(iex);
            return new HashMap<String, UpdateUnit>();
        }
        Map<String, UpdateUnit> temp  = appendUpdateItems (appendInstalldeModules (new HashMap<String, UpdateUnit> ()), provider);
        Map<String, UpdateUnit> retval = new HashMap<String, UpdateUnit>();
        assert items != null;
        for (UpdateItem updateItem : items) {
            String codename = null;            
            UpdateItemImpl itemImpl = Trampoline.SPI.impl(updateItem);
            //very bad pattern
            if (itemImpl instanceof ModuleItem) {
                codename = ((ModuleItem)itemImpl).getCodeName();
            } else if (itemImpl instanceof LocalizationItem) {
                codename = ((LocalizationItem)itemImpl).getCodeName();
            } else if (itemImpl instanceof FeatureItem) {
                codename = ((FeatureItem)itemImpl).getCodeName();                
            } else if (itemImpl instanceof NativeComponentItem) {
                codename = ((NativeComponentItem)itemImpl).getCodeName();                
            } else {
                assert false;
            } 
            UpdateUnit unit = temp.get(codename);
            if (unit != null) {
                retval.put(codename, unit);
            }            
        }
        return retval;
    }
    
    Map<String, UpdateUnit> appendInstalldeModules (Map<String, UpdateUnit> originalUnits) {
        assert originalUnits != null : "Map of original UnitImpl cannot be null";

        Map<String, ModuleInfo> installed = ModuleProvider.getInstalledModules ();
        assert installed != null;
        
        // append installed modules
        for (String moduleId : installed.keySet ()) {
            // create module element
            UpdateElement moduleEl = Trampoline.API.createUpdateElement (new UpdateElementImpl (installed.get (moduleId)));
            
            // add element to map
            addElement (originalUnits, moduleEl, Type.INSTALLED);
        }
        
        return originalUnits;
    }
    
    Map<String, UpdateUnit> appendUpdateItems (Map<String, UpdateUnit> originalUnits, UpdateProvider provider) {
        assert originalUnits != null : "Map of original UnitImpl cannot be null";

        Map<String, UpdateItem> items;
        try {
            items = provider.getUpdateItems ();
        } catch (IOException ioe) {
            log.log (Level.INFO, "Cannot read UpdateItem from UpdateProvider " + provider);
            return originalUnits;
        }
        
        assert items != null : "UpdatesProvider[" + provider.getName () + "] should return non-null items.";
        
        // append updates
        for (String simpleItemId : items.keySet ()) {
            
            // create module element
            UpdateItemImpl itemImpl = Trampoline.SPI.impl (items.get (simpleItemId));
            
            UpdateElement updateEl = null;
            if (itemImpl instanceof ModuleItem) {
                updateEl = Trampoline.API.createUpdateElement (new UpdateElementImpl ((ModuleItem) itemImpl, provider.getDisplayName()));
            } else if (itemImpl instanceof LocalizationItem) {
                updateEl = Trampoline.API.createUpdateElement (new UpdateElementImpl ((LocalizationItem) itemImpl,provider.getDisplayName()));
            } else if (itemImpl instanceof FeatureItem) {
                updateEl = Trampoline.API.createUpdateElement (new UpdateElementImpl ((FeatureItem) itemImpl,provider.getDisplayName()));
            } else if (itemImpl instanceof NativeComponentItem) {
                updateEl = Trampoline.API.createUpdateElement (new UpdateElementImpl ((NativeComponentItem) itemImpl,provider.getDisplayName()));
            } else {
                assert false;
            }
            
            // add element to map
            addElement (originalUnits, updateEl, Type.UPDATE);
        }
        
        return originalUnits;
    }
    
    private void addElement (Map<String, UpdateUnit> impls, UpdateElement element, Type type) {
        // find if corresponding element exists
        UpdateUnit unit = impls.get (element.getCodeName ());
        UpdateUnitImpl impl;
        
        // #101515: Plugin Manager must filter updates by platform dependency
        UpdateElementImpl elImpl = Trampoline.API.impl (element);
        UpdateItemImpl itemImpl = elImpl.getUpdateItemImpl ();
        if (itemImpl != null && itemImpl instanceof ModuleItem) {
            ModuleItem moduleItem = (ModuleItem) itemImpl;
            for (Dependency d : moduleItem.getModuleInfo ().getDependencies ()) {
                if (Dependency.TYPE_REQUIRES == d.getType ()) {
                    //log.log (Level.FINEST, "Dependency: NAME: " + d.getName () + ", TYPE: " + d.getType () + ": " + d.toString ());
                    if (d.getName ().startsWith ("org.openide.modules.os")) { // NOI18N
                        for (ModuleInfo info : ModuleProvider.getInstalledModules ().values ()) {
                            if (Arrays.asList (info.getProvides ()).contains (d.getName ())) {
                                log.log (Level.FINEST, element + " which requires OS " + d + " succeed.");
                                break;
                            }
                        }
                        log.log (Level.FINE, element + " which requires OS " + d + " fails.");
                        return ;
                    }
                }
            }
        }
        
        if (unit == null) {
            impl = new UpdateUnitImpl (element.getCodeName ());
            unit = Trampoline.API.createUpdateUnit (impl);
            impls.put (unit.getCodeName (), unit);
        } else {
            impl = Trampoline.API.impl (unit);
        }
        
        switch (type) {
            case INSTALLED :
                impl.setInstalled (element);
                break;
            case UPDATE :
                impl.addUpdate (element);
                break;
            case BACKUP :
                impl.setBackup (element);
                break;
            default :
                assert false : "Unkown type of UpdateElement: " + type;
        }
    }
    
}
